"""认证与鉴权：JWT 签发/校验、密码哈希、全局请求守卫"""
import hashlib
import hmac
import os
import secrets
from datetime import datetime, timedelta, timezone

import jwt
from fastapi import HTTPException, Request

# 生产环境务必通过环境变量设置强随机密钥：export GC_JWT_SECRET="$(openssl rand -hex 32)"
_DEFAULT_SECRET = "green-cycle-dev-secret-change-me"
JWT_SECRET = os.environ.get("GC_JWT_SECRET", _DEFAULT_SECRET)
JWT_SECRET_IS_DEFAULT = JWT_SECRET == _DEFAULT_SECRET
JWT_ALG = "HS256"
TOKEN_TTL_DAYS = int(os.environ.get("GC_TOKEN_TTL_DAYS", "30"))

# ---------------- 密码哈希（PBKDF2-SHA256，兼容历史明文并在登录时自动升级） ----------------
_PBKDF2_PREFIX = "pbkdf2$"
_ITER = 120_000


def hash_password(plain: str) -> str:
    salt = secrets.token_hex(16)
    dk = hashlib.pbkdf2_hmac("sha256", plain.encode(), salt.encode(), _ITER)
    return f"{_PBKDF2_PREFIX}{_ITER}${salt}${dk.hex()}"


def verify_password(plain: str, stored: str) -> bool:
    if not stored:
        return False
    if not stored.startswith(_PBKDF2_PREFIX):
        # 历史明文密码
        return hmac.compare_digest(plain, stored)
    try:
        _, iters, salt, hexdk = stored.split("$", 3)
        dk = hashlib.pbkdf2_hmac("sha256", plain.encode(), salt.encode(), int(iters))
        return hmac.compare_digest(dk.hex(), hexdk)
    except ValueError:
        return False


def needs_rehash(stored: str) -> bool:
    return not (stored or "").startswith(_PBKDF2_PREFIX)


# ---------------- JWT ----------------
def create_token(user_id: str, role: str) -> str:
    now = datetime.now(timezone.utc)
    payload = {"sub": user_id, "role": role, "iat": now, "exp": now + timedelta(days=TOKEN_TTL_DAYS)}
    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALG)


def decode_token(token: str) -> dict:
    try:
        return jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALG])
    except jwt.ExpiredSignatureError:
        raise HTTPException(401, "登录已过期，请重新登录")
    except jwt.InvalidTokenError:
        raise HTTPException(401, "登录凭证无效，请重新登录")


# ---------------- 全局守卫 ----------------
# 无需登录即可访问的路径（精确匹配或前缀）
PUBLIC_EXACT = {"/", "/api/health", "/api/auth/login", "/api/auth/register", "/docs", "/redoc", "/openapi.json"}
PUBLIC_PREFIX = ("/static/", "/api/categories", "/api/products", "/api/articles")

IDENTITY_KEYS = ("user_id", "recycler_id", "admin_id")


def _extract_token(request: Request) -> str | None:
    auth = request.headers.get("authorization") or request.headers.get("Authorization")
    if auth and auth.lower().startswith("bearer "):
        return auth[7:].strip()
    # 浏览器直接下载（CSV 导出）无法携带 header，允许 query 传 token
    if request.url.path.startswith("/api/admin/export/"):
        return request.query_params.get("token")
    return None


def _is_public(path: str, method: str) -> bool:
    if path in PUBLIC_EXACT:
        return True
    if method == "GET" and path.startswith(PUBLIC_PREFIX):
        return True
    # 点赞允许任意登录用户，但不公开
    return False


async def _body_identity(request: Request) -> dict:
    if request.method not in ("POST", "PUT", "PATCH"):
        return {}
    if not (request.headers.get("content-type") or "").startswith("application/json"):
        return {}
    try:
        body = await request.json()
    except Exception:
        return {}
    return body if isinstance(body, dict) else {}


async def auth_guard(request: Request):
    """FastAPI 全局依赖：校验 token、角色与身份参数一致性，并把当前用户挂到 request.state.user"""
    path = request.url.path
    if _is_public(path, request.method):
        return None

    token = _extract_token(request)
    if not token:
        raise HTTPException(401, "未登录或登录已失效")
    payload = decode_token(token)
    uid = payload.get("sub")

    # 延迟导入避免循环依赖
    from .database import db_cursor

    with db_cursor() as conn:
        row = conn.execute("SELECT * FROM users WHERE id = ?", (uid,)).fetchone()
        if not row:
            raise HTTPException(401, "账号不存在，请重新登录")
        if row["status"] == "deleted":
            raise HTTPException(401, "账号已注销")
        if row["status"] == "disabled":
            raise HTTPException(403, "账号已被禁用，请联系管理员")
        me = dict(row)
    me.pop("password", None)
    role = me["role"]
    is_admin = role == "admin"

    # 角色路由前缀
    if path.startswith("/api/admin/") and not is_admin:
        raise HTTPException(403, "需要管理员权限")
    if path.startswith("/api/recycler/") and role not in ("recycler", "admin"):
        # 用户端允许读取回收员实时位置（订单跟踪）
        if not (request.method == "GET" and path.startswith("/api/recycler/location/")):
            raise HTTPException(403, "需要回收员权限")

    # 身份参数一致性：query / body / path 中出现的 *_id 必须是自己（管理员除外）
    if not is_admin:
        claimed = set()
        for k in IDENTITY_KEYS:
            v = request.query_params.get(k)
            if v:
                claimed.add(v)
            v = request.path_params.get(k)
            if v:
                claimed.add(v)
        body = await _body_identity(request)
        for k in IDENTITY_KEYS:
            v = body.get(k)
            if isinstance(v, str) and v:
                claimed.add(v)
        for v in claimed:
            if v != me["id"]:
                raise HTTPException(403, "无权访问他人数据")

    request.state.user = me
    return me


def current_user(request: Request) -> dict:
    """在处理函数中获取当前登录用户（由 auth_guard 注入）"""
    me = getattr(request.state, "user", None)
    if me is None:
        raise HTTPException(401, "未登录或登录已失效")
    return me
