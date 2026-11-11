"""
推送抽象层（FCM HTTP v1 就绪）。

设计目标：
- 业务代码只调用 `push_to_user(conn, user_id, title, body, data)`，不感知推送厂商。
- 未配置凭据时**不报错、不阻塞**，仅记录日志（开发/测试环境默认行为）。
- 配置 `GC_FCM_SERVICE_ACCOUNT`（服务账号 JSON 文件路径）后自动启用真实发送：
    * 依赖 `google-auth` 获取 OAuth2 访问令牌（scope: firebase.messaging）
    * 调用 https://fcm.googleapis.com/v1/projects/{project_id}/messages:send
    * 返回 UNREGISTERED / INVALID_ARGUMENT 时自动清理失效 token
- 发送在后台线程执行，不占用请求线程。

环境变量：
    GC_FCM_SERVICE_ACCOUNT   服务账号 JSON 路径（必需，启用真实推送）
    GC_FCM_PROJECT_ID        可选；缺省时从服务账号 JSON 的 project_id 读取
    GC_PUSH_DRY_RUN=1        可选；即使有凭据也只打日志不发送
"""
from __future__ import annotations

import json
import logging
import os
import threading
from datetime import datetime
from typing import Dict, Iterable, List, Optional

log = logging.getLogger("greencycle.push")
if not log.handlers:  # uvicorn 默认不输出应用 logger，确保推送记录可见
    _h = logging.StreamHandler()
    _h.setFormatter(logging.Formatter("%(levelname)s:     %(message)s"))
    log.addHandler(_h)
    log.setLevel(logging.INFO)
    log.propagate = False

FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging"
_SA_PATH = os.environ.get("GC_FCM_SERVICE_ACCOUNT", "").strip()
_DRY_RUN = os.environ.get("GC_PUSH_DRY_RUN", "0") == "1"

_credentials = None
_project_id: Optional[str] = os.environ.get("GC_FCM_PROJECT_ID") or None
_lock = threading.Lock()


def _now() -> str:
    return datetime.utcnow().isoformat(timespec="seconds") + "Z"


# ---------------------------------------------------------------- token registry

def register_token(conn, user_id: str, token: str, platform: str = "android",
                   app_version: Optional[str] = None, locale: Optional[str] = None) -> None:
    """UPSERT 设备 token；同一 token 换账号登录时自动迁移到新用户。"""
    conn.execute(
        """INSERT INTO device_tokens(token, user_id, platform, app_version, locale, updated_at)
           VALUES (?,?,?,?,?,?)
           ON CONFLICT(token) DO UPDATE SET
             user_id=excluded.user_id, platform=excluded.platform,
             app_version=excluded.app_version, locale=excluded.locale,
             updated_at=excluded.updated_at""",
        (token, user_id, platform, app_version, locale, _now()),
    )


def unregister_token(conn, token: str, user_id: Optional[str] = None) -> int:
    if user_id:
        cur = conn.execute("DELETE FROM device_tokens WHERE token=? AND user_id=?", (token, user_id))
    else:
        cur = conn.execute("DELETE FROM device_tokens WHERE token=?", (token,))
    return cur.rowcount


def tokens_for_user(conn, user_id: str) -> List[Dict]:
    rows = conn.execute(
        "SELECT token, platform, locale FROM device_tokens WHERE user_id=?", (user_id,)
    ).fetchall()
    return [dict(r) for r in rows]


def token_count(conn) -> int:
    return int(conn.execute("SELECT COUNT(*) FROM device_tokens").fetchone()[0])


# ---------------------------------------------------------------- provider status

def provider_status() -> Dict:
    """供 /api/health 与后台展示：当前推送通道是否就绪。"""
    enabled = bool(_SA_PATH) and os.path.exists(_SA_PATH)
    return {
        "provider": "fcm_v1",
        "enabled": enabled and not _DRY_RUN,
        "dry_run": _DRY_RUN,
        "configured": enabled,
        "project_id": _project_id,
        "hint": None if enabled else "设置 GC_FCM_SERVICE_ACCOUNT=<service-account.json> 以启用真实推送",
    }


def _load_credentials():
    """惰性加载 google-auth 凭据；未安装/未配置时返回 None。"""
    global _credentials, _project_id
    if _credentials is not None:
        return _credentials
    if not _SA_PATH or not os.path.exists(_SA_PATH):
        return None
    with _lock:
        if _credentials is not None:
            return _credentials
        try:
            from google.oauth2 import service_account  # type: ignore
        except ImportError:
            log.warning("[push] 已配置 GC_FCM_SERVICE_ACCOUNT 但未安装 google-auth：pip install google-auth")
            return None
        try:
            creds = service_account.Credentials.from_service_account_file(_SA_PATH, scopes=[FCM_SCOPE])
            if not _project_id:
                with open(_SA_PATH, "r", encoding="utf-8") as f:
                    _project_id = json.load(f).get("project_id")
            _credentials = creds
            log.info("[push] FCM v1 已启用，project_id=%s", _project_id)
        except Exception as e:  # noqa: BLE001
            log.error("[push] 加载服务账号失败：%s", e)
            return None
    return _credentials


def _access_token() -> Optional[str]:
    creds = _load_credentials()
    if creds is None:
        return None
    try:
        from google.auth.transport.requests import Request  # type: ignore
        if not creds.valid:
            creds.refresh(Request())
        return creds.token
    except Exception as e:  # noqa: BLE001
        log.error("[push] 获取访问令牌失败：%s", e)
        return None


# ---------------------------------------------------------------- sending

def _build_message(token: str, title: str, body: str, data: Optional[Dict[str, str]]) -> Dict:
    payload: Dict = {
        "token": token,
        "notification": {"title": title, "body": body},
        "android": {
            "priority": "HIGH",
            "notification": {"channel_id": "greencycle_default", "sound": "default", "color": "#1B5E20"},
        },
        "apns": {"payload": {"aps": {"sound": "default", "badge": 1}}},
    }
    if data:
        payload["data"] = {k: str(v) for k, v in data.items() if v is not None}
    return {"message": payload}


def _send_one(db_path: str, token: str, title: str, body: str, data: Optional[Dict[str, str]]) -> None:
    """在后台线程中真实发送单条消息；失效 token 自动删除。"""
    import sqlite3
    import urllib.error
    import urllib.request

    access = _access_token()
    if not access or not _project_id:
        return
    url = f"https://fcm.googleapis.com/v1/projects/{_project_id}/messages:send"
    req = urllib.request.Request(
        url,
        data=json.dumps(_build_message(token, title, body, data)).encode("utf-8"),
        headers={"Authorization": f"Bearer {access}", "Content-Type": "application/json; UTF-8"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            resp.read()
        log.info("[push] sent → %s… (%s)", token[:12], title)
    except urllib.error.HTTPError as e:
        detail = e.read().decode("utf-8", "ignore")
        log.warning("[push] FCM %s for %s…: %s", e.code, token[:12], detail[:200])
        if e.code in (400, 404) and ("UNREGISTERED" in detail or "INVALID_ARGUMENT" in detail):
            try:
                c = sqlite3.connect(db_path)
                c.execute("DELETE FROM device_tokens WHERE token=?", (token,))
                c.commit()
                c.close()
                log.info("[push] 已清理失效 token %s…", token[:12])
            except Exception as ce:  # noqa: BLE001
                log.error("[push] 清理 token 失败：%s", ce)
    except Exception as e:  # noqa: BLE001
        log.error("[push] 发送异常：%s", e)


def push_to_tokens(db_path: str, tokens: Iterable[str], title: str, body: str,
                   data: Optional[Dict[str, str]] = None) -> int:
    """对一组 token 发送；返回“已排队”数量。无凭据时仅记录日志。"""
    tokens = [t for t in tokens if t]
    if not tokens:
        return 0
    status = provider_status()
    if not status["enabled"]:
        log.info("[push][dry-run] %d device(s) ← %s | %s | data=%s", len(tokens), title, body, data or {})
        return len(tokens)
    for t in tokens:
        threading.Thread(target=_send_one, args=(db_path, t, title, body, data), daemon=True).start()
    return len(tokens)


def push_to_user(conn, db_path: str, user_id: str, title: str, body: str,
                 data: Optional[Dict[str, str]] = None) -> int:
    """按用户查 token 后发送。供 `_notify` 调用，始终不抛异常。"""
    try:
        toks = [r["token"] for r in tokens_for_user(conn, user_id)]
        return push_to_tokens(db_path, toks, title, body, data)
    except Exception as e:  # noqa: BLE001
        log.error("[push] push_to_user 失败：%s", e)
        return 0
