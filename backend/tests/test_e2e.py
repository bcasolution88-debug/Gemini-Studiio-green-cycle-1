"""端到端业务闭环测试（针对运行中的后端，默认 http://localhost:8000）

用法：
    cd backend && rm -f green_cycle.db && uvicorn app.main:app --port 8000 &
    python3 tests/test_e2e.py
"""
import json
import os
import sys
import urllib.error
import urllib.request

B = os.environ.get("GC_API", "http://localhost:8000")
fail = 0

# ---- JWT 鉴权：按请求中的身份参数自动选择对应账号的 token ----
TOKENS = {}          # user_id -> token
ACCOUNTS = {         # 预置演示账号（user_id -> (phone, password)）
    "r_001": ("13800001111", "123456"),
    "r_002": ("13800002222", "123456"),
    "r_003": ("13800003333", "123456"),
    "u_admin_001": ("13900000000", "admin123"),
    "u_demo_001": ("13912345678", "123456"),
}
import re as _re
_ID_RE = _re.compile(r"(?:user_id|recycler_id|admin_id)=([A-Za-z0-9_]+)")
_AS = None           # 显式指定本次调用身份（优先级最高）


def _raw(method, path, data=None, token=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    return urllib.request.Request(B + path, method=method, headers=headers,
                                  data=json.dumps(data).encode() if data is not None else None)


def token_for(uid):
    if uid == "__none__":
        return None
    if uid in TOKENS:
        return TOKENS[uid]
    if uid in ACCOUNTS:
        phone, pw = ACCOUNTS[uid]
        with urllib.request.urlopen(_raw("POST", "/api/auth/login", {"phone": phone, "password": pw}), timeout=15) as r:
            TOKENS[uid] = json.loads(r.read())["data"]["token"]
        return TOKENS[uid]
    return None


def _infer_identity(path, data):
    if _AS:
        return _AS
    m = _ID_RE.search(path)
    if m:
        return m.group(1)
    if isinstance(data, dict):
        for k in ("user_id", "recycler_id", "admin_id"):
            if data.get(k):
                return data[k]
    m = _re.search(r"/api/users/([A-Za-z0-9_]+)(?:/|$)", path)   # 排除 /api/users/sign-in
    if m:
        return m.group(1)
    return None


def call(method, path, data=None, expect_ok=True, as_user=None):
    ident = as_user or _infer_identity(path, data)
    req = _raw(method, path, data, token_for(ident) if ident else None)
    try:
        with urllib.request.urlopen(req, timeout=15) as r:
            body = r.read()
            if r.headers.get("content-type", "").startswith("text/csv"):
                return body.decode("utf-8-sig")
            return json.loads(body)["data"]
    except urllib.error.HTTPError as e:
        msg = json.loads(e.read()).get("detail")
        if expect_ok:
            print(f"   ❌ {method} {path} -> {e.code} {msg}")
            sys.exit(1)
        return ("ERR", e.code, msg)


def check(cond, s):
    global fail
    if cond:
        print("   ✅", s)
    else:
        fail += 1
        print("   ❌", s)


print("【1】用户注册 / 登录 / 角色 / 错误密码")
u = call("POST", "/api/auth/register", {"phone": "13611112222", "password": "123456", "nickname": "审核用户"})
uid = u["user"]["id"]
TOKENS[uid] = u["token"]; ACCOUNTS[uid] = ("13611112222", "123456")
check(u["token"].count(".") == 2, "注册返回 JWT token")
check(u["user"]["points"] == 100 and u["user"]["role"] == "user", "注册送 100 积分，角色 user")
rec = call("POST", "/api/auth/login", {"phone": "13800001111", "password": "123456"})["user"]
check(rec["role"] == "recycler", "回收员登录 role=recycler")
adm = call("POST", "/api/auth/login", {"phone": "13900000000", "password": "admin123"})["user"]
check(adm["role"] == "admin", "管理员登录 role=admin")
bad = call("POST", "/api/auth/login", {"phone": "13611112222", "password": "wrongpw"}, expect_ok=False)
check(bad[1] == 401, "错误密码 401")
short = call("POST", "/api/auth/login", {"phone": "13611112222", "password": "abc"}, expect_ok=False)
check(short[1] == 422, "密码不足 6 位 422 参数校验")

print("【2】地址 → 预约（经纬度）→ 通知触发")
addr = call("POST", "/api/addresses", {"user_id": uid, "contact_name": "审核用户", "phone": "13611112222",
                                       "region": "北京市 朝阳区", "detail": "测试路 1 号", "is_default": True})
check(addr["lat"] is not None and addr["lng"] is not None, "地址自动带经纬度")
o = call("POST", "/api/orders", {"user_id": uid, "address_id": addr["id"], "appointment_time": "2026-10-12 14:00-16:00",
                                 "items": [{"category_id": "c_paper", "estimated_weight": 10},
                                           {"category_id": "c_metal", "estimated_weight": 4}], "remark": "审核"})
oid = o["id"]
check(o["status"] == "pending" and o["lat"] is not None and len(o["items"]) == 2, "订单 pending，含坐标与 2 个品类")
check(any("预约成功" in i["title"] for i in call("GET", f"/api/notifications?user_id={uid}")["items"]), "用户收到「预约成功」通知")
check(any(i["related_id"] == oid for i in call("GET", f"/api/notifications?user_id={rec['id']}")["items"]), "回收员收到「新的待接订单」通知")

print("【2.5】JWT 鉴权守卫")
check(call("GET", f"/api/home?user_id={uid}", expect_ok=False, as_user="__none__")[1] == 401, "无 token 访问受保护接口 401")
check(call("GET", f"/api/orders?user_id=u_demo_001", expect_ok=False, as_user=uid)[1] == 403, "携带他人 user_id 403")
check(call("POST", "/api/users/sign-in", {"user_id": "u_demo_001"}, expect_ok=False, as_user=uid)[1] == 403, "body 伪造他人身份 403")
check(call("GET", f"/api/orders/{oid}", expect_ok=False, as_user="u_demo_001")[1] == 403, "他人读取我的订单 403")
check(call("GET", f"/api/orders/{oid}", as_user="r_001")["id"] == oid, "回收员可查看待接订单")
check(call("GET", "/api/categories", as_user="__none__") and True, "公开接口（品类）无需登录")

print("【3】接单大厅 → 抢单 → 重复抢单拦截 → 位置上报 → 出发")
check(any(x["id"] == oid for x in call("GET", f"/api/recycler/hall?recycler_id={rec['id']}")), "新订单出现在接单大厅")
a = call("POST", f"/api/recycler/orders/{oid}/accept", {"recycler_id": rec["id"]})
check(a["status"] == "accepted" and a["recycler_id"] == rec["id"], "抢单成功 accepted")
dup = call("POST", f"/api/recycler/orders/{oid}/accept", {"recycler_id": "r_002"}, expect_ok=False)
check(dup[1] == 400, "他人重复抢单被拦截 400")
call("POST", "/api/recycler/location", {"recycler_id": rec["id"], "lat": 39.93, "lng": 116.46, "heading": 45, "speed": 6})
d = call("POST", f"/api/recycler/orders/{oid}/depart", {"recycler_id": rec["id"]})
check(d["status"] == "in_progress", "出发 in_progress")
od = call("GET", f"/api/orders/{oid}", as_user=uid)
check(od["recycler_location"] and abs(od["recycler_location"]["lat"] - 39.93) < 1e-6, "订单详情携带回收员实时位置")
check(any("正在赶来" in i["title"] for i in call("GET", f"/api/notifications?user_id={uid}")["items"]), "用户收到「回收员正在赶来」通知")
check(any(x["id"] == oid for x in call("GET", f"/api/recycler/tasks?recycler_id={rec['id']}&status=active")), "回收员「我的任务」含该订单")

print("【4】称重完成 → 积分/减碳/服务费结算")
before = call("GET", f"/api/users/{uid}")["points"]
wb = call("GET", f"/api/recycler/wallet?recycler_id={rec['id']}")["balance"]
c = call("POST", f"/api/recycler/orders/{oid}/complete",
         {"recycler_id": rec["id"], "items": [{"category_id": "c_paper", "actual_weight": 9.5},
                                               {"category_id": "c_metal", "actual_weight": 4.2}]})
exp_amount = round(9.5 * 1.2 + 4.2 * 2.5, 2)
exp_points = int(9.5 * 12 + 4.2 * 25)
exp_carbon = round(9.5 * 1.5 + 4.2 * 3.2, 2)
check(c["status"] == "completed" and abs(c["amount"] - exp_amount) < 0.01, f"金额 {c['amount']} == {exp_amount}")
check(c["points_earned"] == exp_points, f"积分 {c['points_earned']} == {exp_points}")
check(abs(c["carbon_saved"] - exp_carbon) < 0.01, f"减碳 {c['carbon_saved']} == {exp_carbon}")
after = call("GET", f"/api/users/{uid}")
check(after["points"] - before == exp_points, "用户积分到账")
check(abs(after["total_weight"] - 13.7) < 0.01, "用户累计重量 +13.7")
wa = call("GET", f"/api/recycler/wallet?recycler_id={rec['id']}")
exp_fee = round(8 + exp_amount * 0.15, 2)
check(abs(wa["balance"] - wb - exp_fee) < 0.01, f"回收员服务费入账 ¥{exp_fee}")
check(any(x["id"] == oid for x in wa["unpaid_orders"]), "订单出现在「待结算」列表")
check(any(p["related_id"] == oid for p in call("GET", f"/api/points/records?user_id={uid}")), "积分流水记录生成")

print("【5】确认付款 → 用户通知 → 重复付款拦截")
p = call("POST", f"/api/recycler/orders/{oid}/pay", {"recycler_id": rec["id"], "pay_method": "wechat"})
check(p["paid"] == 1 and p["pay_method"] == "wechat", "标记已付款 wechat")
dup = call("POST", f"/api/recycler/orders/{oid}/pay", {"recycler_id": rec["id"], "pay_method": "cash"}, expect_ok=False)
check(dup[1] == 400, "重复付款拦截")
check(any("回收款已支付" in i["title"] for i in call("GET", f"/api/notifications?user_id={uid}")["items"]), "用户收到「回收款已支付」通知")
wa = call("GET", f"/api/recycler/wallet?recycler_id={rec['id']}")
check(not any(x["id"] == oid for x in wa["unpaid_orders"]), "待结算列表已移除")

print("【6】提现（校验/冻结）→ 管理员通知 → 驳回退款 → 通过打款")
bal = wa["balance"]
check(call("POST", "/api/recycler/withdraw", {"recycler_id": rec["id"], "amount": bal + 100, "account": "微信"}, expect_ok=False)[1] == 400, "超额提现拦截")
check(call("POST", "/api/recycler/withdraw", {"recycler_id": rec["id"], "amount": 5, "account": "微信"}, expect_ok=False)[1] == 400, "低于 ¥10 拦截")
w1 = call("POST", "/api/recycler/withdraw", {"recycler_id": rec["id"], "amount": 10, "account": "微信 138****1111"})
wa2 = call("GET", f"/api/recycler/wallet?recycler_id={rec['id']}")
check(abs(wa2["balance"] - (bal - 10)) < 0.01 and abs(wa2["pending_withdraw"] - 10) < 0.01, "提现后余额冻结扣减，pending=10")
check(any(i["related_id"] == w1["id"] for i in call("GET", f"/api/notifications?user_id={adm['id']}")["items"]), "管理员收到提现审核通知")
r1 = call("POST", f"/api/admin/withdrawals/{w1['id']}/review?admin_id={adm['id']}", {"approve": False, "remark": "账户信息不全"})
check(r1["status"] == "rejected", "驳回成功")
check(abs(call("GET", f"/api/recycler/wallet?recycler_id={rec['id']}")["balance"] - bal) < 0.01, "驳回后金额退回余额")
check(any("被驳回" in i["title"] for i in call("GET", f"/api/notifications?user_id={rec['id']}")["items"]), "回收员收到驳回通知")
w2 = call("POST", "/api/recycler/withdraw", {"recycler_id": rec["id"], "amount": 10, "account": "支付宝"})
r2 = call("POST", f"/api/admin/withdrawals/{w2['id']}/review?admin_id={adm['id']}", {"approve": True})
check(r2["status"] == "approved", "通过打款")
check(call("POST", f"/api/admin/withdrawals/{w2['id']}/review?admin_id={adm['id']}", {"approve": True}, expect_ok=False)[1] == 400, "重复审核拦截")
check(call("GET", f"/api/recycler/wallet?recycler_id={rec['id']}")["withdrawn"] >= 10, "已提现累计更新")

print("【6.5】回收员接单开关 / 用户评价")
check(call("POST", "/api/recycler/online", {"recycler_id": "r_003", "online": False})["online"] is False, "r_003 暂停接单")
check(call("POST", f"/api/recycler/orders/{oid}/accept", {"recycler_id": "r_003"}, expect_ok=False)[1] == 400, "暂停状态抢单被拦截 400")
call("POST", "/api/recycler/online", {"recycler_id": "r_003", "online": True})
rated = call("POST", f"/api/orders/{oid}/rate", {"user_id": uid, "rating": 5, "tags": ["准时上门", "称重公正"], "comment": "很好"})
check(rated["rating"] == 5 and "准时上门" in (rated["rating_tags"] or ""), "评价成功 5 星 + 标签")
check(call("POST", f"/api/orders/{oid}/rate", {"user_id": uid, "rating": 4}, expect_ok=False)[1] == 400, "重复评价拦截 400")
check(call("GET", f"/api/recycler/stats?recycler_id={rec['id']}")["rating_count"] == 129, "回收员评分计数 +1")
check(any("收到用户评价" in i["title"] for i in call("GET", f"/api/notifications?user_id={rec['id']}")["items"]), "回收员收到评价通知")

print("【6.8】推送就绪：设备 token 注册 / 查询 / 注销 / 身份校验")
tok = "fcm_test_token_" + uid
reg = call("POST", "/api/push/register", {"user_id": uid, "token": tok, "platform": "android", "app_version": "1.2.0", "locale": "zh"})
check(reg["registered"] is True and reg["devices"] >= 1, "注册设备 token")
check(reg["provider"]["provider"] == "fcm_v1" and "enabled" in reg["provider"], "返回推送通道状态")
check(call("POST", "/api/push/register", {"user_id": uid, "token": tok, "platform": "android"})["devices"] == reg["devices"], "重复注册幂等（UPSERT）")
check(any(d["token"] == tok for d in call("GET", f"/api/push/devices?user_id={uid}")["devices"]), "查询设备列表包含 token")
check(call("POST", "/api/push/register", {"user_id": "r_001", "token": "x" * 20, "platform": "android"}, as_user=uid, expect_ok=False)[1] == 403, "冒充他人注册 token 403")
check(call("GET", "/api/health")["push"]["provider"] == "fcm_v1", "健康检查暴露推送状态")
check(call("POST", "/api/push/unregister", {"user_id": uid, "token": tok})["removed"] == 1, "注销 token")

print("【7】商城兑换 → 积分扣减/库存 → 管理员发货")
pts = call("GET", f"/api/users/{uid}")["points"]
prod = next(p for p in call("GET", "/api/products") if p["points"] <= pts and p["stock"] > 0)
rd = call("POST", "/api/redemptions", {"user_id": uid, "product_id": prod["id"], "quantity": 1})
check(rd["status"] == "pending" and rd["code"].startswith("GC-RD-"), "兑换生成兑换码")
check(call("GET", f"/api/users/{uid}")["points"] == pts - prod["points"], "积分扣减正确")
check(call("GET", f"/api/products/{prod['id']}")["stock"] == prod["stock"] - 1, "库存 -1")
check(call("POST", "/api/redemptions", {"user_id": uid, "product_id": "p_008", "quantity": 1}, expect_ok=False)[1] == 400, "积分不足拦截")
check(call("PUT", f"/api/admin/redemptions/{rd['id']}?admin_id={adm['id']}", {"status": "shipped"})["status"] == "shipped", "管理员发货")

print("【8】管理后台：指派/取消/权限/用户管理/调价/概览/地图/导出")
o2 = call("POST", "/api/orders", {"user_id": uid, "address_id": addr["id"], "appointment_time": "2026-10-13 09:00-11:00",
                                  "items": [{"category_id": "c_clothes", "estimated_weight": 5}]})
asg = call("POST", f"/api/admin/orders/{o2['id']}/assign?admin_id={adm['id']}", {"recycler_id": "r_002"})
check(asg["status"] == "accepted" and asg["recycler_id"] == "r_002", "管理员指派")
check(any(i["related_id"] == o2["id"] for i in call("GET", "/api/notifications?user_id=r_002")["items"]), "被指派回收员收到通知")
check(call("POST", f"/api/admin/orders/{o2['id']}/cancel?admin_id={adm['id']}")["status"] == "cancelled", "管理员取消")
check(call("GET", f"/api/admin/dashboard?admin_id={uid}", expect_ok=False)[1] == 403, "普通用户访问管理接口 403")
check(call("GET", f"/api/recycler/hall?recycler_id={uid}", expect_ok=False)[1] == 403, "普通用户访问回收员接口 403")
check(call("PUT", f"/api/admin/users/{uid}?admin_id={adm['id']}", {"status": "disabled"})["status"] == "disabled", "禁用用户")
check(call("POST", "/api/auth/login", {"phone": "13611112222", "password": "123456"}, expect_ok=False)[1] == 403, "禁用用户登录 403")
call("PUT", f"/api/admin/users/{uid}?admin_id={adm['id']}", {"status": "active"})
cat = call("PUT", f"/api/admin/categories/c_paper?admin_id={adm['id']}", {"price_per_kg": 1.5})
check(abs(cat["price_per_kg"] - 1.5) < 1e-6, "品类调价生效")
call("PUT", f"/api/admin/categories/c_paper?admin_id={adm['id']}", {"price_per_kg": 1.2})
dash = call("GET", f"/api/admin/dashboard?admin_id={adm['id']}")
check(dash["orders_total"] >= 12 and "trend" in dash and "top_recyclers" in dash, f"概览数据完整（完成订单 {dash['orders_total']}）")
locs = call("GET", f"/api/admin/locations?admin_id={adm['id']}")
check(len(locs) == 3 and all("nickname" in l for l in locs), "回收员地图 3 人位置")
for ds in ["orders", "users", "redemptions", "withdrawals", "points", "categories"]:
    lines = call("GET", f"/api/admin/export/{ds}?admin_id={adm['id']}").strip().split("\n")
    check(len(lines) >= 2 and "," in lines[0], f"导出 {ds}: {len(lines) - 1} 行")

print("【9】通知轮询 / 已读 / 删除")
uc = call("GET", f"/api/notifications/unread-count?user_id={uid}")
check(uc["unread"] > 0 and "server_time" in uc, f"未读 {uc['unread']}，含 server_time")
call("POST", "/api/notifications/read", {"user_id": uid})
check(call("GET", f"/api/notifications/unread-count?user_id={uid}")["unread"] == 0, "全部已读后未读=0")
first = call("GET", f"/api/notifications?user_id={uid}")["items"][0]["id"]
call("DELETE", f"/api/notifications/{first}?user_id={uid}")
check(not any(i["id"] == first for i in call("GET", f"/api/notifications?user_id={uid}")["items"]), "删除通知生效")

print("【10】用户端统计 / 签到 / 首页聚合")
st = call("GET", f"/api/users/{uid}/stats")
check(st["order_count"] == 1 and any(a["unlocked"] for a in st["achievements"]), "统计正确，解锁「初出茅庐」成就")
call("POST", "/api/users/sign-in", {"user_id": uid})
check(call("POST", "/api/users/sign-in", {"user_id": uid}, expect_ok=False)[1] == 400, "签到成功，重复签到拦截")
home = call("GET", f"/api/home?user_id={uid}")
check(home["signed_today"] and len(home["categories"]) == 8 and len(home["hot_products"]) > 0, "首页聚合完整")

print(f"\n{'=' * 50}\n{'✅ 全部通过' if fail == 0 else f'❌ {fail} 项失败'}")
sys.exit(1 if fail else 0)
