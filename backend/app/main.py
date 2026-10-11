"""绿循环 Green Cycle 后端 API（FastAPI + SQLite）"""
import random
import string
import uuid
from datetime import datetime

import csv
import io

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse

from .database import db_cursor, init_db, rows_to_list
from .schemas import (
    AddressGeoIn,
    ArticleIn,
    AssignOrderRequest,
    CompleteOrderRequest,
    CreateOrderRequest,
    LocationUpdateRequest,
    LoginRequest,
    MarkReadRequest,
    PayOrderRequest,
    ProductIn,
    ReviewWithdrawRequest,
    WithdrawRequest,
    RecyclerActionRequest,
    RedeemRequest,
    RegisterRequest,
    SignInRequest,
    UpdateCategoryRequest,
    UpdateProductRequest,
    UpdateProfileRequest,
    UpdateRedemptionStatusRequest,
    UpdateUserStatusRequest,
)
from .seed import ADMIN_ID, DEMO_USER_ID, RECYCLERS, seed_if_empty

app = FastAPI(title="绿循环 Green Cycle API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

LEVEL_THRESHOLDS = [0, 500, 1500, 3000, 6000, 12000, 25000]
LEVEL_NAMES = ["环保新人", "环保学徒", "环保能手", "环保达人", "环保专家", "环保大师", "地球守护者"]


def _now() -> str:
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


def _ok(data=None, message="success"):
    return {"code": 0, "message": message, "data": data}


def _notify(conn, user_id, ntype: str, title: str, body: str, related_id=None):
    if not user_id:
        return
    conn.execute(
        "INSERT INTO notifications VALUES (?,?,?,?,?,?,?,?)",
        (f"nt_{uuid.uuid4().hex[:10]}", user_id, ntype, title, body, related_id, 0, _now()),
    )


def _notify_admins(conn, ntype: str, title: str, body: str, related_id=None):
    for r in conn.execute("SELECT id FROM users WHERE role='admin'").fetchall():
        _notify(conn, r["id"], ntype, title, body, related_id)


RECYCLER_COMMISSION = 0.15  # 回收员服务费比例（按订单金额）
RECYCLER_BASE_FEE = 8.0     # 每单基础服务费（元）


def _level_for(total_points: int) -> int:
    level = 1
    for i, th in enumerate(LEVEL_THRESHOLDS):
        if total_points >= th:
            level = i + 1
    return min(level, len(LEVEL_THRESHOLDS))


def _user_payload(row) -> dict:
    u = dict(row)
    u.pop("password", None)
    u.setdefault("role", "user")
    u.setdefault("status", "active")
    lvl = u["level"]
    u["level_name"] = LEVEL_NAMES[min(lvl - 1, len(LEVEL_NAMES) - 1)]
    next_th = LEVEL_THRESHOLDS[lvl] if lvl < len(LEVEL_THRESHOLDS) else None
    u["next_level_points"] = next_th
    u["points_to_next_level"] = max(0, next_th - u["total_points"]) if next_th else 0
    return u


def _get_user_or_404(conn, user_id: str):
    row = conn.execute("SELECT * FROM users WHERE id = ?", (user_id,)).fetchone()
    if not row:
        raise HTTPException(404, "用户不存在")
    return row


@app.on_event("startup")
def on_startup():
    init_db()
    seed_if_empty()


@app.get("/")
def root():
    return _ok({"service": "green-cycle-api", "docs": "/docs"})


@app.get("/api/health")
def health():
    return _ok({"status": "ok", "time": _now()})


# ---------------- 认证 / 用户 ----------------
@app.post("/api/auth/login")
def login(req: LoginRequest):
    with db_cursor() as conn:
        row = conn.execute(
            "SELECT * FROM users WHERE phone = ? AND password = ?", (req.phone, req.password)
        ).fetchone()
        if not row:
            raise HTTPException(401, "手机号或密码错误")
        if row["status"] == "disabled":
            raise HTTPException(403, "账号已被禁用，请联系管理员")
        return _ok({"token": f"tk_{row['id']}", "user": _user_payload(row)})


@app.post("/api/auth/register")
def register(req: RegisterRequest):
    with db_cursor() as conn:
        if conn.execute("SELECT 1 FROM users WHERE phone = ?", (req.phone,)).fetchone():
            raise HTTPException(400, "该手机号已注册")
        uid = f"u_{uuid.uuid4().hex[:10]}"
        conn.execute(
            "INSERT INTO users VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
            (uid, req.phone, req.password, req.nickname,
             f"https://i.pravatar.cc/150?img={random.randint(1, 70)}", "user", "active", 1, 100, 100, 0, 0, _now()),
        )
        conn.execute(
            "INSERT INTO point_records VALUES (?,?,?,?,?,?,?)",
            (f"pr_{uuid.uuid4().hex[:8]}", uid, "earn", 100, "新用户注册奖励", None, _now()),
        )
        row = conn.execute("SELECT * FROM users WHERE id = ?", (uid,)).fetchone()
        return _ok({"token": f"tk_{uid}", "user": _user_payload(row)})


@app.get("/api/users/{user_id}")
def get_user(user_id: str):
    with db_cursor() as conn:
        return _ok(_user_payload(_get_user_or_404(conn, user_id)))


@app.put("/api/users/{user_id}")
def update_user(user_id: str, req: UpdateProfileRequest):
    with db_cursor() as conn:
        _get_user_or_404(conn, user_id)
        if req.nickname:
            conn.execute("UPDATE users SET nickname = ? WHERE id = ?", (req.nickname, user_id))
        if req.avatar:
            conn.execute("UPDATE users SET avatar = ? WHERE id = ?", (req.avatar, user_id))
        return _ok(_user_payload(_get_user_or_404(conn, user_id)))


@app.get("/api/users/{user_id}/stats")
def user_stats(user_id: str):
    with db_cursor() as conn:
        u = _get_user_or_404(conn, user_id)
        order_count = conn.execute(
            "SELECT COUNT(*) FROM orders WHERE user_id = ? AND status = 'completed'", (user_id,)
        ).fetchone()[0]
        month = datetime.now().strftime("%Y-%m")
        month_row = conn.execute(
            "SELECT COALESCE(SUM(actual_weight),0), COALESCE(SUM(carbon_saved),0), COALESCE(SUM(points_earned),0) "
            "FROM orders WHERE user_id = ? AND status='completed' AND created_at LIKE ?",
            (user_id, f"{month}%"),
        ).fetchone()
        # 最近 6 个月趋势
        trend = rows_to_list(conn.execute(
            "SELECT substr(created_at,1,7) AS month, COALESCE(SUM(actual_weight),0) AS weight, "
            "COALESCE(SUM(carbon_saved),0) AS carbon FROM orders WHERE user_id=? AND status='completed' "
            "GROUP BY month ORDER BY month DESC LIMIT 6",
            (user_id,),
        ).fetchall())
        by_cat = rows_to_list(conn.execute(
            "SELECT oi.category_name AS name, COALESCE(SUM(oi.actual_weight),0) AS weight FROM order_items oi "
            "JOIN orders o ON o.id = oi.order_id WHERE o.user_id=? AND o.status='completed' "
            "GROUP BY oi.category_name ORDER BY weight DESC",
            (user_id,),
        ).fetchall())
        # 成就
        achievements = rows_to_list(conn.execute("SELECT * FROM achievements").fetchall())
        redeem_public = conn.execute(
            "SELECT COUNT(*) FROM redemptions r JOIN products p ON p.id=r.product_id WHERE r.user_id=? AND p.category='公益'",
            (user_id,),
        ).fetchone()[0]
        values = {"orders": order_count, "carbon": u["total_carbon"], "weight": u["total_weight"],
                  "points": u["total_points"], "redeem": redeem_public}
        for a in achievements:
            cur = values.get(a["condition_type"], 0)
            a["current"] = cur
            a["unlocked"] = cur >= a["condition_value"]
        return _ok({
            "order_count": order_count,
            "total_weight": u["total_weight"],
            "total_carbon": u["total_carbon"],
            "total_points": u["total_points"],
            "month_weight": round(month_row[0], 2),
            "month_carbon": round(month_row[1], 2),
            "month_points": month_row[2],
            "trees_equivalent": round(u["total_carbon"] / 18.3, 1),
            "trend": list(reversed(trend)),
            "by_category": by_cat,
            "achievements": achievements,
        })


@app.post("/api/users/sign-in")
def sign_in(req: SignInRequest):
    with db_cursor() as conn:
        _get_user_or_404(conn, req.user_id)
        today = datetime.now().strftime("%Y-%m-%d")
        exists = conn.execute(
            "SELECT 1 FROM point_records WHERE user_id=? AND title='每日签到' AND created_at LIKE ?",
            (req.user_id, f"{today}%"),
        ).fetchone()
        if exists:
            raise HTTPException(400, "今日已签到")
        _add_points(conn, req.user_id, 10, "每日签到", None)
        return _ok(_user_payload(_get_user_or_404(conn, req.user_id)), "签到成功 +10 积分")


def _add_points(conn, user_id: str, amount: int, title: str, related_id):
    conn.execute(
        "INSERT INTO point_records VALUES (?,?,?,?,?,?,?)",
        (f"pr_{uuid.uuid4().hex[:8]}", user_id, "earn" if amount > 0 else "spend", amount, title, related_id, _now()),
    )
    conn.execute("UPDATE users SET points = points + ? WHERE id = ?", (amount, user_id))
    if amount > 0:
        conn.execute("UPDATE users SET total_points = total_points + ? WHERE id = ?", (amount, user_id))
    total = conn.execute("SELECT total_points FROM users WHERE id=?", (user_id,)).fetchone()[0]
    conn.execute("UPDATE users SET level = ? WHERE id = ?", (_level_for(total), user_id))


# ---------------- 品类 ----------------
@app.get("/api/categories")
def categories():
    with db_cursor() as conn:
        return _ok(rows_to_list(conn.execute("SELECT * FROM categories ORDER BY sort_order").fetchall()))


# ---------------- 地址 ----------------
@app.get("/api/addresses")
def list_addresses(user_id: str = Query(...)):
    with db_cursor() as conn:
        return _ok(rows_to_list(conn.execute(
            "SELECT * FROM addresses WHERE user_id=? ORDER BY is_default DESC", (user_id,)
        ).fetchall()))


@app.post("/api/addresses")
def create_address(req: AddressGeoIn):
    with db_cursor() as conn:
        aid = f"addr_{uuid.uuid4().hex[:8]}"
        if req.is_default:
            conn.execute("UPDATE addresses SET is_default=0 WHERE user_id=?", (req.user_id,))
        # 未提供坐标时在朝阳区范围内生成一个演示坐标
        lat = req.lat if req.lat is not None else round(39.92 + random.uniform(-0.02, 0.02), 5)
        lng = req.lng if req.lng is not None else round(116.46 + random.uniform(-0.03, 0.03), 5)
        conn.execute(
            "INSERT INTO addresses VALUES (?,?,?,?,?,?,?,?,?)",
            (aid, req.user_id, req.contact_name, req.phone, req.region, req.detail, int(req.is_default), lat, lng),
        )
        return _ok(dict(conn.execute("SELECT * FROM addresses WHERE id=?", (aid,)).fetchone()))


@app.delete("/api/addresses/{address_id}")
def delete_address(address_id: str):
    with db_cursor() as conn:
        conn.execute("DELETE FROM addresses WHERE id=?", (address_id,))
        return _ok(True)


# ---------------- 订单 ----------------
def _order_with_items(conn, order_row) -> dict:
    o = dict(order_row)
    o["items"] = rows_to_list(conn.execute(
        "SELECT * FROM order_items WHERE order_id=?", (o["id"],)
    ).fetchall())
    o["recycler_location"] = None
    if o.get("recycler_id") and o.get("status") in ("accepted", "in_progress"):
        loc = conn.execute("SELECT * FROM recycler_locations WHERE recycler_id=?", (o["recycler_id"],)).fetchone()
        if loc:
            o["recycler_location"] = dict(loc)
    return o


@app.get("/api/orders")
def list_orders(user_id: str = Query(...), status: str = Query(None)):
    with db_cursor() as conn:
        sql = "SELECT * FROM orders WHERE user_id=?"
        params = [user_id]
        if status and status != "all":
            sql += " AND status=?"
            params.append(status)
        sql += " ORDER BY created_at DESC"
        rows = conn.execute(sql, params).fetchall()
        return _ok([_order_with_items(conn, r) for r in rows])


@app.get("/api/orders/{order_id}")
def get_order(order_id: str):
    with db_cursor() as conn:
        row = conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()
        if not row:
            raise HTTPException(404, "订单不存在")
        return _ok(_order_with_items(conn, row))


@app.post("/api/orders")
def create_order(req: CreateOrderRequest):
    with db_cursor() as conn:
        _get_user_or_404(conn, req.user_id)
        addr = conn.execute("SELECT * FROM addresses WHERE id=? AND user_id=?", (req.address_id, req.user_id)).fetchone()
        if not addr:
            raise HTTPException(400, "地址不存在")
        if not req.items:
            raise HTTPException(400, "请至少选择一种回收品类")
        oid = f"o_{uuid.uuid4().hex[:10]}"
        order_no = "GC" + datetime.now().strftime("%Y%m%d%H%M%S") + "".join(random.choices(string.digits, k=3))
        est = sum(i.estimated_weight for i in req.items)
        now = _now()
        conn.execute(
            "INSERT INTO orders VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            (oid, order_no, req.user_id, "pending", f"{addr['region']} {addr['detail']}",
             addr["contact_name"], addr["phone"], req.appointment_time, req.remark or "",
             est, None, None, 0, 0, None, None, None, None, addr["lat"], addr["lng"], 0, None, now, now),
        )
        for it in req.items:
            cat = conn.execute("SELECT * FROM categories WHERE id=?", (it.category_id,)).fetchone()
            if not cat:
                raise HTTPException(400, f"品类不存在: {it.category_id}")
            conn.execute(
                "INSERT INTO order_items (order_id, category_id, category_name, estimated_weight) VALUES (?,?,?,?)",
                (oid, cat["id"], cat["name"], it.estimated_weight),
            )
        _notify(conn, req.user_id, "order", "预约成功", f"订单 {order_no} 已提交，正在为您匹配回收员", oid)
        for r in conn.execute("SELECT id FROM users WHERE role='recycler' AND status='active'").fetchall():
            _notify(conn, r["id"], "order", "新的待接订单", f"{addr['region']} 新增一笔预约（预估 {est:.1f}kg），去接单大厅看看", oid)
        return _ok(_order_with_items(conn, conn.execute("SELECT * FROM orders WHERE id=?", (oid,)).fetchone()), "预约成功")


@app.post("/api/orders/{order_id}/cancel")
def cancel_order(order_id: str):
    with db_cursor() as conn:
        row = conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()
        if not row:
            raise HTTPException(404, "订单不存在")
        if row["status"] not in ("pending", "accepted"):
            raise HTTPException(400, "当前状态无法取消")
        conn.execute("UPDATE orders SET status='cancelled', updated_at=? WHERE id=?", (_now(), order_id))
        return _ok(_order_with_items(conn, conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()), "已取消")


def _settle_order(conn, order_row, items, actuals: dict):
    """称重结算：写入实际重量、金额、积分、减碳，并发放积分"""
    amount = points = carbon = weight = 0.0
    for it in items:
        cat = conn.execute("SELECT * FROM categories WHERE id=?", (it["category_id"],)).fetchone()
        actual = float(actuals.get(it["id"], it["estimated_weight"]))
        conn.execute("UPDATE order_items SET actual_weight=? WHERE id=?", (actual, it["id"]))
        weight += actual
        amount += actual * cat["price_per_kg"]
        points += actual * cat["points_per_kg"]
        carbon += actual * cat["carbon_per_kg"]
    points = int(points)
    conn.execute(
        "UPDATE orders SET status='completed', actual_weight=?, amount=?, points_earned=?, carbon_saved=?, updated_at=? WHERE id=?",
        (round(weight, 2), round(amount, 2), points, round(carbon, 2), _now(), order_row["id"]),
    )
    conn.execute(
        "UPDATE users SET total_weight = total_weight + ?, total_carbon = total_carbon + ? WHERE id=?",
        (round(weight, 2), round(carbon, 2), order_row["user_id"]),
    )
    names = "、".join(i["category_name"] for i in items)
    _add_points(conn, order_row["user_id"], points, f"回收订单 {names}", order_row["id"])
    # 回收员服务费入账
    if order_row["recycler_id"]:
        fee = round(RECYCLER_BASE_FEE + amount * RECYCLER_COMMISSION, 2)
        conn.execute(
            "INSERT INTO wallet_records VALUES (?,?,?,?,?,?,?)",
            (f"wr_{uuid.uuid4().hex[:8]}", order_row["recycler_id"], "income", fee,
             f"订单服务费 {order_row['order_no']}", order_row["id"], _now()),
        )
        _notify(conn, order_row["recycler_id"], "wallet", "服务费已入账",
                f"订单 {order_row['order_no']} 完成，服务费 ¥{fee:.2f} 已入账钱包", order_row["id"])
    _notify(conn, order_row["user_id"], "points", "回收完成，积分到账",
            f"{names} 实际 {weight:.1f}kg，回收款 ¥{amount:.2f}，+{points} 积分已到账", order_row["id"])


@app.post("/api/orders/{order_id}/advance")
def advance_order(order_id: str):
    """模拟订单流转：pending -> accepted -> in_progress -> completed（演示用）"""
    flow = {"pending": "accepted", "accepted": "in_progress", "in_progress": "completed"}
    with db_cursor() as conn:
        row = conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()
        if not row:
            raise HTTPException(404, "订单不存在")
        cur = row["status"]
        if cur not in flow:
            raise HTTPException(400, "订单已结束")
        nxt = flow[cur]
        if nxt == "accepted":
            rec = random.choice(RECYCLERS)
            conn.execute(
                "UPDATE orders SET status=?, recycler_id=?, recycler_name=?, recycler_phone=?, recycler_avatar=?, updated_at=? WHERE id=?",
                (nxt, rec[0], rec[1], rec[2], rec[3], _now(), order_id),
            )
            _notify(conn, row["user_id"], "order", "回收员已接单", f"{rec[1]} 已接单，将按预约时间上门", order_id)
        elif nxt == "completed":
            items = conn.execute("SELECT * FROM order_items WHERE order_id=?", (order_id,)).fetchall()
            actuals = {it["id"]: round(it["estimated_weight"] * random.uniform(0.9, 1.1), 1) for it in items}
            _settle_order(conn, row, items, actuals)
        else:
            conn.execute("UPDATE orders SET status=?, updated_at=? WHERE id=?", (nxt, _now(), order_id))
            _notify(conn, row["user_id"], "order", "回收员正在赶来", f"{row['recycler_name'] or '回收员'} 已出发，请保持电话畅通", order_id)
        return _ok(_order_with_items(conn, conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()))


# ---------------- 积分 ----------------
@app.get("/api/points/records")
def point_records(user_id: str = Query(...)):
    with db_cursor() as conn:
        return _ok(rows_to_list(conn.execute(
            "SELECT * FROM point_records WHERE user_id=? ORDER BY created_at DESC", (user_id,)
        ).fetchall()))


# ---------------- 商城 ----------------
@app.get("/api/products")
def products(category: str = Query(None)):
    with db_cursor() as conn:
        if category and category != "全部":
            rows = conn.execute("SELECT * FROM products WHERE category=? ORDER BY is_hot DESC, sold DESC", (category,)).fetchall()
        else:
            rows = conn.execute("SELECT * FROM products ORDER BY is_hot DESC, sold DESC").fetchall()
        return _ok(rows_to_list(rows))


@app.get("/api/products/{product_id}")
def product_detail(product_id: str):
    with db_cursor() as conn:
        row = conn.execute("SELECT * FROM products WHERE id=?", (product_id,)).fetchone()
        if not row:
            raise HTTPException(404, "商品不存在")
        return _ok(dict(row))


@app.post("/api/redemptions")
def redeem(req: RedeemRequest):
    with db_cursor() as conn:
        user = _get_user_or_404(conn, req.user_id)
        prod = conn.execute("SELECT * FROM products WHERE id=?", (req.product_id,)).fetchone()
        if not prod:
            raise HTTPException(404, "商品不存在")
        need = prod["points"] * req.quantity
        if prod["stock"] < req.quantity:
            raise HTTPException(400, "库存不足")
        if user["points"] < need:
            raise HTTPException(400, f"积分不足，还差 {need - user['points']} 积分")
        rid = f"rd_{uuid.uuid4().hex[:8]}"
        code = "GC-RD-" + "".join(random.choices(string.ascii_uppercase + string.digits, k=6))
        conn.execute(
            "INSERT INTO redemptions VALUES (?,?,?,?,?,?,?,?,?,?)",
            (rid, req.user_id, prod["id"], prod["name"], prod["image"], need, req.quantity, "pending", code, _now()),
        )
        conn.execute("UPDATE products SET stock = stock - ?, sold = sold + ? WHERE id=?", (req.quantity, req.quantity, prod["id"]))
        _add_points(conn, req.user_id, -need, f"兑换 {prod['name']}", rid)
        return _ok(dict(conn.execute("SELECT * FROM redemptions WHERE id=?", (rid,)).fetchone()), "兑换成功")


@app.get("/api/redemptions")
def redemptions(user_id: str = Query(...)):
    with db_cursor() as conn:
        return _ok(rows_to_list(conn.execute(
            "SELECT * FROM redemptions WHERE user_id=? ORDER BY created_at DESC", (user_id,)
        ).fetchall()))


# ---------------- 资讯 ----------------
@app.get("/api/articles")
def articles(category: str = Query(None)):
    with db_cursor() as conn:
        if category and category != "全部":
            rows = conn.execute("SELECT * FROM articles WHERE category=? ORDER BY published_at DESC", (category,)).fetchall()
        else:
            rows = conn.execute("SELECT * FROM articles ORDER BY published_at DESC").fetchall()
        return _ok(rows_to_list(rows))


@app.get("/api/articles/{article_id}")
def article_detail(article_id: str):
    with db_cursor() as conn:
        conn.execute("UPDATE articles SET views = views + 1 WHERE id=?", (article_id,))
        row = conn.execute("SELECT * FROM articles WHERE id=?", (article_id,)).fetchone()
        if not row:
            raise HTTPException(404, "文章不存在")
        return _ok(dict(row))


@app.post("/api/articles/{article_id}/like")
def like_article(article_id: str):
    with db_cursor() as conn:
        conn.execute("UPDATE articles SET likes = likes + 1 WHERE id=?", (article_id,))
        row = conn.execute("SELECT * FROM articles WHERE id=?", (article_id,)).fetchone()
        return _ok(dict(row))


# ---------------- 首页聚合 ----------------
@app.get("/api/home")
def home(user_id: str = Query(DEMO_USER_ID)):
    with db_cursor() as conn:
        user = _get_user_or_404(conn, user_id)
        cats = rows_to_list(conn.execute("SELECT * FROM categories ORDER BY sort_order LIMIT 8").fetchall())
        active = conn.execute(
            "SELECT * FROM orders WHERE user_id=? AND status IN ('pending','accepted','in_progress') ORDER BY created_at DESC LIMIT 1",
            (user_id,),
        ).fetchone()
        hot = rows_to_list(conn.execute("SELECT * FROM products WHERE is_hot=1 ORDER BY sold DESC LIMIT 6").fetchall())
        news = rows_to_list(conn.execute("SELECT * FROM articles ORDER BY published_at DESC LIMIT 3").fetchall())
        today = datetime.now().strftime("%Y-%m-%d")
        today_carbon = conn.execute(
            "SELECT COALESCE(SUM(carbon_saved),0) FROM orders WHERE user_id=? AND status='completed' AND updated_at LIKE ?",
            (user_id, f"{today}%"),
        ).fetchone()[0]
        signed = conn.execute(
            "SELECT 1 FROM point_records WHERE user_id=? AND title='每日签到' AND created_at LIKE ?",
            (user_id, f"{today}%"),
        ).fetchone() is not None
        return _ok({
            "user": _user_payload(user),
            "today_carbon": round(today_carbon, 2),
            "signed_today": signed,
            "categories": cats,
            "active_order": _order_with_items(conn, active) if active else None,
            "hot_products": hot,
            "news": news,
        })


# =====================================================================
# 回收员端
# =====================================================================
def _require_role(conn, user_id: str, *roles):
    row = _get_user_or_404(conn, user_id)
    if row["role"] not in roles:
        raise HTTPException(403, "无权限执行此操作")
    return row


@app.get("/api/recycler/hall")
def recycler_hall(recycler_id: str = Query(...)):
    """接单大厅：所有待接单订单"""
    with db_cursor() as conn:
        _require_role(conn, recycler_id, "recycler", "admin")
        rows = conn.execute(
            "SELECT * FROM orders WHERE status='pending' ORDER BY appointment_time ASC, created_at ASC"
        ).fetchall()
        return _ok([_order_with_items(conn, r) for r in rows])


@app.get("/api/recycler/tasks")
def recycler_tasks(recycler_id: str = Query(...), status: str = Query("active")):
    """我的任务：active = 已接单 + 上门中；completed = 已完成；all = 全部"""
    with db_cursor() as conn:
        _require_role(conn, recycler_id, "recycler", "admin")
        if status == "active":
            rows = conn.execute(
                "SELECT * FROM orders WHERE recycler_id=? AND status IN ('accepted','in_progress') ORDER BY appointment_time ASC",
                (recycler_id,),
            ).fetchall()
        elif status == "completed":
            rows = conn.execute(
                "SELECT * FROM orders WHERE recycler_id=? AND status='completed' ORDER BY updated_at DESC",
                (recycler_id,),
            ).fetchall()
        else:
            rows = conn.execute(
                "SELECT * FROM orders WHERE recycler_id=? ORDER BY created_at DESC", (recycler_id,)
            ).fetchall()
        return _ok([_order_with_items(conn, r) for r in rows])


@app.post("/api/recycler/orders/{order_id}/accept")
def recycler_accept(order_id: str, req: RecyclerActionRequest):
    with db_cursor() as conn:
        rec = _require_role(conn, req.recycler_id, "recycler", "admin")
        row = conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()
        if not row:
            raise HTTPException(404, "订单不存在")
        if row["status"] != "pending":
            raise HTTPException(400, "该订单已被接单或已取消")
        conn.execute(
            "UPDATE orders SET status='accepted', recycler_id=?, recycler_name=?, recycler_phone=?, recycler_avatar=?, updated_at=? WHERE id=?",
            (rec["id"], rec["nickname"], rec["phone"], rec["avatar"], _now(), order_id),
        )
        _notify(conn, row["user_id"], "order", "回收员已接单", f"{rec['nickname']} 已接单，将于 {row['appointment_time']} 上门", order_id)
        return _ok(_order_with_items(conn, conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()), "接单成功")


@app.post("/api/recycler/orders/{order_id}/depart")
def recycler_depart(order_id: str, req: RecyclerActionRequest):
    with db_cursor() as conn:
        _require_role(conn, req.recycler_id, "recycler", "admin")
        row = conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()
        if not row or row["recycler_id"] != req.recycler_id and row["recycler_id"] is not None:
            raise HTTPException(404, "订单不存在或不属于您")
        if row["status"] != "accepted":
            raise HTTPException(400, "当前状态无法出发")
        conn.execute("UPDATE orders SET status='in_progress', updated_at=? WHERE id=?", (_now(), order_id))
        _notify(conn, row["user_id"], "order", "回收员正在赶来", f"{row['recycler_name'] or '回收员'} 已出发，可在订单详情查看实时位置", order_id)
        return _ok(_order_with_items(conn, conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()), "已出发")


@app.post("/api/recycler/orders/{order_id}/complete")
def recycler_complete(order_id: str, req: CompleteOrderRequest):
    """称重完成：按品类填写实际重量并结算"""
    with db_cursor() as conn:
        _require_role(conn, req.recycler_id, "recycler", "admin")
        row = conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()
        if not row:
            raise HTTPException(404, "订单不存在")
        if row["status"] not in ("accepted", "in_progress"):
            raise HTTPException(400, "当前状态无法完成")
        items = conn.execute("SELECT * FROM order_items WHERE order_id=?", (order_id,)).fetchall()
        weight_by_cat = {i.category_id: i.actual_weight for i in req.items}
        actuals = {it["id"]: weight_by_cat.get(it["category_id"], it["estimated_weight"]) for it in items}
        _settle_order(conn, row, items, actuals)
        return _ok(_order_with_items(conn, conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()), "回收完成")


@app.get("/api/recycler/stats")
def recycler_stats(recycler_id: str = Query(...)):
    with db_cursor() as conn:
        rec = _require_role(conn, recycler_id, "recycler", "admin")
        today = datetime.now().strftime("%Y-%m-%d")
        month = datetime.now().strftime("%Y-%m")
        def agg(where, params):
            r = conn.execute(
                f"SELECT COUNT(*), COALESCE(SUM(actual_weight),0), COALESCE(SUM(amount),0), COALESCE(SUM(carbon_saved),0) "
                f"FROM orders WHERE recycler_id=? AND status='completed' {where}", (recycler_id, *params)
            ).fetchone()
            return {"count": r[0], "weight": round(r[1], 1), "amount": round(r[2], 2), "carbon": round(r[3], 1)}
        active = conn.execute(
            "SELECT COUNT(*) FROM orders WHERE recycler_id=? AND status IN ('accepted','in_progress')", (recycler_id,)
        ).fetchone()[0]
        pending = conn.execute("SELECT COUNT(*) FROM orders WHERE status='pending'").fetchone()[0]
        balance = conn.execute("SELECT COALESCE(SUM(amount),0) FROM wallet_records WHERE recycler_id=?", (recycler_id,)).fetchone()[0]
        unpaid = conn.execute("SELECT COUNT(*) FROM orders WHERE recycler_id=? AND status='completed' AND paid=0", (recycler_id,)).fetchone()[0]
        return _ok({
            "recycler": _user_payload(rec),
            "balance": round(balance, 2),
            "unpaid_orders": unpaid,
            "today": agg("AND updated_at LIKE ?", (f"{today}%",)),
            "month": agg("AND updated_at LIKE ?", (f"{month}%",)),
            "total": agg("", ()),
            "active_tasks": active,
            "hall_pending": pending,
            "rating": 4.9,
        })


# =====================================================================
# 管理后台
# =====================================================================
@app.get("/api/admin/dashboard")
def admin_dashboard(admin_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        today = datetime.now().strftime("%Y-%m-%d")
        month = datetime.now().strftime("%Y-%m")
        users = conn.execute("SELECT COUNT(*) FROM users WHERE role='user'").fetchone()[0]
        recyclers = conn.execute("SELECT COUNT(*) FROM users WHERE role='recycler'").fetchone()[0]
        new_users_month = conn.execute(
            "SELECT COUNT(*) FROM users WHERE role='user' AND created_at LIKE ?", (f"{month}%",)
        ).fetchone()[0]
        total = conn.execute(
            "SELECT COUNT(*), COALESCE(SUM(actual_weight),0), COALESCE(SUM(amount),0), COALESCE(SUM(carbon_saved),0), COALESCE(SUM(points_earned),0) "
            "FROM orders WHERE status='completed'"
        ).fetchone()
        today_row = conn.execute(
            "SELECT COUNT(*), COALESCE(SUM(actual_weight),0), COALESCE(SUM(amount),0) FROM orders WHERE status='completed' AND updated_at LIKE ?",
            (f"{today}%",),
        ).fetchone()
        today_new = conn.execute("SELECT COUNT(*) FROM orders WHERE created_at LIKE ?", (f"{today}%",)).fetchone()[0]
        status_counts = {r[0]: r[1] for r in conn.execute("SELECT status, COUNT(*) FROM orders GROUP BY status").fetchall()}
        by_cat = rows_to_list(conn.execute(
            "SELECT oi.category_name AS name, COALESCE(SUM(oi.actual_weight),0) AS weight, COUNT(DISTINCT oi.order_id) AS orders "
            "FROM order_items oi JOIN orders o ON o.id=oi.order_id WHERE o.status='completed' GROUP BY oi.category_name ORDER BY weight DESC"
        ).fetchall())
        trend = rows_to_list(conn.execute(
            "SELECT substr(updated_at,1,10) AS day, COUNT(*) AS orders, COALESCE(SUM(actual_weight),0) AS weight "
            "FROM orders WHERE status='completed' AND updated_at >= date('now','-13 days') GROUP BY day ORDER BY day"
        ).fetchall())
        top_recyclers = rows_to_list(conn.execute(
            "SELECT u.id, u.nickname, u.avatar, COUNT(o.id) AS orders, COALESCE(SUM(o.actual_weight),0) AS weight "
            "FROM users u LEFT JOIN orders o ON o.recycler_id=u.id AND o.status='completed' "
            "WHERE u.role='recycler' GROUP BY u.id ORDER BY orders DESC, weight DESC LIMIT 5"
        ).fetchall())
        top_users = rows_to_list(conn.execute(
            "SELECT id, nickname, avatar, total_weight, total_carbon, total_points FROM users WHERE role='user' ORDER BY total_weight DESC LIMIT 5"
        ).fetchall())
        redemptions = conn.execute("SELECT COUNT(*), COALESCE(SUM(points),0) FROM redemptions").fetchone()
        pending_redemptions = conn.execute("SELECT COUNT(*) FROM redemptions WHERE status='pending'").fetchone()[0]
        pending_withdrawals = conn.execute("SELECT COUNT(*), COALESCE(SUM(amount),0) FROM withdrawals WHERE status='pending'").fetchone()
        return _ok({
            "pending_withdrawals": pending_withdrawals[0],
            "pending_withdrawals_amount": round(pending_withdrawals[1], 2),
            "users": users,
            "recyclers": recyclers,
            "new_users_month": new_users_month,
            "orders_total": total[0],
            "weight_total": round(total[1], 1),
            "amount_total": round(total[2], 2),
            "carbon_total": round(total[3], 1),
            "points_total": total[4],
            "today_orders_completed": today_row[0],
            "today_weight": round(today_row[1], 1),
            "today_amount": round(today_row[2], 2),
            "today_new_orders": today_new,
            "status_counts": status_counts,
            "by_category": by_cat,
            "trend": trend,
            "top_recyclers": top_recyclers,
            "top_users": top_users,
            "redemptions_total": redemptions[0],
            "redemptions_points": redemptions[1],
            "pending_redemptions": pending_redemptions,
        })


@app.get("/api/admin/orders")
def admin_orders(admin_id: str = Query(...), status: str = Query("all"), keyword: str = Query("")):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        sql = "SELECT * FROM orders WHERE 1=1"
        params = []
        if status != "all":
            sql += " AND status=?"
            params.append(status)
        if keyword:
            sql += " AND (order_no LIKE ? OR contact_name LIKE ? OR contact_phone LIKE ? OR address_text LIKE ?)"
            params += [f"%{keyword}%"] * 4
        sql += " ORDER BY created_at DESC LIMIT 200"
        rows = conn.execute(sql, params).fetchall()
        return _ok([_order_with_items(conn, r) for r in rows])


@app.post("/api/admin/orders/{order_id}/assign")
def admin_assign(order_id: str, req: AssignOrderRequest, admin_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        rec = conn.execute("SELECT * FROM users WHERE id=? AND role='recycler'", (req.recycler_id,)).fetchone()
        if not rec:
            raise HTTPException(404, "回收员不存在")
        row = conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()
        if not row:
            raise HTTPException(404, "订单不存在")
        if row["status"] not in ("pending", "accepted"):
            raise HTTPException(400, "当前状态无法指派")
        conn.execute(
            "UPDATE orders SET status='accepted', recycler_id=?, recycler_name=?, recycler_phone=?, recycler_avatar=?, updated_at=? WHERE id=?",
            (rec["id"], rec["nickname"], rec["phone"], rec["avatar"], _now(), order_id),
        )
        _notify(conn, rec["id"], "order", "平台为您指派了订单", f"订单 {row['order_no']} · {row['appointment_time']} · {row['address_text']}", order_id)
        _notify(conn, row["user_id"], "order", "回收员已接单", f"{rec['nickname']} 将为您服务，上门时间 {row['appointment_time']}", order_id)
        return _ok(_order_with_items(conn, conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()), "指派成功")


@app.post("/api/admin/orders/{order_id}/cancel")
def admin_cancel(order_id: str, admin_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        row = conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()
        if not row:
            raise HTTPException(404, "订单不存在")
        if row["status"] in ("completed", "cancelled"):
            raise HTTPException(400, "订单已结束")
        conn.execute("UPDATE orders SET status='cancelled', updated_at=? WHERE id=?", (_now(), order_id))
        _notify(conn, row["user_id"], "order", "订单已被平台取消", f"订单 {row['order_no']} 已取消，如有疑问请联系客服", order_id)
        if row["recycler_id"]:
            _notify(conn, row["recycler_id"], "order", "订单已取消", f"平台取消了订单 {row['order_no']}", order_id)
        return _ok(_order_with_items(conn, conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()), "已取消")


@app.get("/api/admin/users")
def admin_users(admin_id: str = Query(...), role: str = Query("all"), keyword: str = Query("")):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        sql = "SELECT * FROM users WHERE 1=1"
        params = []
        if role != "all":
            sql += " AND role=?"
            params.append(role)
        if keyword:
            sql += " AND (nickname LIKE ? OR phone LIKE ?)"
            params += [f"%{keyword}%"] * 2
        sql += " ORDER BY created_at DESC LIMIT 200"
        rows = conn.execute(sql, params).fetchall()
        result = []
        for r in rows:
            u = _user_payload(r)
            if r["role"] == "recycler":
                u["completed_orders"] = conn.execute(
                    "SELECT COUNT(*) FROM orders WHERE recycler_id=? AND status='completed'", (r["id"],)
                ).fetchone()[0]
            else:
                u["completed_orders"] = conn.execute(
                    "SELECT COUNT(*) FROM orders WHERE user_id=? AND status='completed'", (r["id"],)
                ).fetchone()[0]
            result.append(u)
        return _ok(result)


@app.put("/api/admin/users/{user_id}")
def admin_update_user(user_id: str, req: UpdateUserStatusRequest, admin_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        _get_user_or_404(conn, user_id)
        if req.status in ("active", "disabled"):
            conn.execute("UPDATE users SET status=? WHERE id=?", (req.status, user_id))
        if req.role in ("user", "recycler", "admin"):
            conn.execute("UPDATE users SET role=? WHERE id=?", (req.role, user_id))
        return _ok(_user_payload(_get_user_or_404(conn, user_id)))


@app.get("/api/admin/recyclers")
def admin_recyclers(admin_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        rows = conn.execute("SELECT * FROM users WHERE role='recycler' AND status='active' ORDER BY nickname").fetchall()
        return _ok([_user_payload(r) for r in rows])


@app.put("/api/admin/categories/{category_id}")
def admin_update_category(category_id: str, req: UpdateCategoryRequest, admin_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        if not conn.execute("SELECT 1 FROM categories WHERE id=?", (category_id,)).fetchone():
            raise HTTPException(404, "品类不存在")
        for field in ("price_per_kg", "points_per_kg", "carbon_per_kg", "description"):
            v = getattr(req, field)
            if v is not None:
                conn.execute(f"UPDATE categories SET {field}=? WHERE id=?", (v, category_id))
        return _ok(dict(conn.execute("SELECT * FROM categories WHERE id=?", (category_id,)).fetchone()))


@app.post("/api/admin/products")
def admin_create_product(req: ProductIn, admin_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        pid = f"p_{uuid.uuid4().hex[:8]}"
        conn.execute(
            "INSERT INTO products VALUES (?,?,?,?,?,?,?,?,?)",
            (pid, req.name, req.description, req.image, req.points, req.stock, req.category, int(req.is_hot), 0),
        )
        return _ok(dict(conn.execute("SELECT * FROM products WHERE id=?", (pid,)).fetchone()))


@app.put("/api/admin/products/{product_id}")
def admin_update_product(product_id: str, req: UpdateProductRequest, admin_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        if not conn.execute("SELECT 1 FROM products WHERE id=?", (product_id,)).fetchone():
            raise HTTPException(404, "商品不存在")
        for field in ("name", "description", "image", "points", "stock", "category"):
            v = getattr(req, field)
            if v is not None:
                conn.execute(f"UPDATE products SET {field}=? WHERE id=?", (v, product_id))
        if req.is_hot is not None:
            conn.execute("UPDATE products SET is_hot=? WHERE id=?", (int(req.is_hot), product_id))
        return _ok(dict(conn.execute("SELECT * FROM products WHERE id=?", (product_id,)).fetchone()))


@app.delete("/api/admin/products/{product_id}")
def admin_delete_product(product_id: str, admin_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        conn.execute("DELETE FROM products WHERE id=?", (product_id,))
        return _ok(True)


@app.get("/api/admin/redemptions")
def admin_redemptions(admin_id: str = Query(...), status: str = Query("all")):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        if status == "all":
            rows = conn.execute(
                "SELECT r.*, u.nickname AS user_nickname, u.phone AS user_phone FROM redemptions r JOIN users u ON u.id=r.user_id ORDER BY r.created_at DESC"
            ).fetchall()
        else:
            rows = conn.execute(
                "SELECT r.*, u.nickname AS user_nickname, u.phone AS user_phone FROM redemptions r JOIN users u ON u.id=r.user_id WHERE r.status=? ORDER BY r.created_at DESC",
                (status,),
            ).fetchall()
        return _ok(rows_to_list(rows))


@app.put("/api/admin/redemptions/{redemption_id}")
def admin_update_redemption(redemption_id: str, req: UpdateRedemptionStatusRequest, admin_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        if req.status not in ("pending", "shipped", "completed"):
            raise HTTPException(400, "状态不合法")
        conn.execute("UPDATE redemptions SET status=? WHERE id=?", (req.status, redemption_id))
        row = conn.execute("SELECT * FROM redemptions WHERE id=?", (redemption_id,)).fetchone()
        if not row:
            raise HTTPException(404, "记录不存在")
        return _ok(dict(row))


@app.post("/api/admin/articles")
def admin_create_article(req: ArticleIn, admin_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        aid = f"a_{uuid.uuid4().hex[:8]}"
        conn.execute(
            "INSERT INTO articles VALUES (?,?,?,?,?,?,?,?,?,?)",
            (aid, req.title, req.summary, req.content, req.cover, req.category, req.author, 0, 0, _now()),
        )
        return _ok(dict(conn.execute("SELECT * FROM articles WHERE id=?", (aid,)).fetchone()))


@app.delete("/api/admin/articles/{article_id}")
def admin_delete_article(article_id: str, admin_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        conn.execute("DELETE FROM articles WHERE id=?", (article_id,))
        return _ok(True)


# =====================================================================
# 消息通知
# =====================================================================
@app.get("/api/notifications")
def list_notifications(user_id: str = Query(...), limit: int = Query(50)):
    with db_cursor() as conn:
        rows = conn.execute(
            "SELECT * FROM notifications WHERE user_id=? ORDER BY created_at DESC LIMIT ?", (user_id, limit)
        ).fetchall()
        unread = conn.execute("SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0", (user_id,)).fetchone()[0]
        return _ok({"unread": unread, "items": rows_to_list(rows)})


@app.get("/api/notifications/unread-count")
def unread_count(user_id: str = Query(...), since: str = Query("")):
    """轮询接口：返回未读数与 since 之后的新消息（用于前端弹出提醒）"""
    with db_cursor() as conn:
        unread = conn.execute("SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0", (user_id,)).fetchone()[0]
        new_items = []
        if since:
            new_items = rows_to_list(conn.execute(
                "SELECT * FROM notifications WHERE user_id=? AND created_at > ? ORDER BY created_at DESC LIMIT 5",
                (user_id, since),
            ).fetchall())
        return _ok({"unread": unread, "new_items": new_items, "server_time": _now()})


@app.post("/api/notifications/read")
def mark_read(req: MarkReadRequest):
    with db_cursor() as conn:
        if req.ids:
            qs = ",".join("?" * len(req.ids))
            conn.execute(f"UPDATE notifications SET is_read=1 WHERE user_id=? AND id IN ({qs})", (req.user_id, *req.ids))
        else:
            conn.execute("UPDATE notifications SET is_read=1 WHERE user_id=?", (req.user_id,))
        return _ok(True)


@app.delete("/api/notifications/{notification_id}")
def delete_notification(notification_id: str, user_id: str = Query(...)):
    with db_cursor() as conn:
        conn.execute("DELETE FROM notifications WHERE id=? AND user_id=?", (notification_id, user_id))
        return _ok(True)


# =====================================================================
# 实时定位
# =====================================================================
@app.post("/api/recycler/location")
def update_location(req: LocationUpdateRequest):
    with db_cursor() as conn:
        _require_role(conn, req.recycler_id, "recycler", "admin")
        conn.execute(
            "INSERT INTO recycler_locations (recycler_id, lat, lng, heading, speed, updated_at) VALUES (?,?,?,?,?,?) "
            "ON CONFLICT(recycler_id) DO UPDATE SET lat=excluded.lat, lng=excluded.lng, heading=excluded.heading, "
            "speed=excluded.speed, updated_at=excluded.updated_at",
            (req.recycler_id, req.lat, req.lng, req.heading, req.speed, _now()),
        )
        return _ok({"recycler_id": req.recycler_id, "lat": req.lat, "lng": req.lng, "updated_at": _now()})


@app.get("/api/recycler/location/{recycler_id}")
def get_location(recycler_id: str):
    with db_cursor() as conn:
        row = conn.execute("SELECT * FROM recycler_locations WHERE recycler_id=?", (recycler_id,)).fetchone()
        if not row:
            raise HTTPException(404, "暂无位置信息")
        return _ok(dict(row))


@app.get("/api/admin/locations")
def admin_locations(admin_id: str = Query(...)):
    """管理后台：全部回收员实时位置"""
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        rows = conn.execute(
            "SELECT l.*, u.nickname, u.avatar, u.phone, "
            "(SELECT COUNT(*) FROM orders o WHERE o.recycler_id=l.recycler_id AND o.status IN ('accepted','in_progress')) AS active_tasks "
            "FROM recycler_locations l JOIN users u ON u.id=l.recycler_id WHERE u.status='active'"
        ).fetchall()
        return _ok(rows_to_list(rows))


# =====================================================================
# 回收员钱包 / 收款结算 / 提现
# =====================================================================
@app.post("/api/recycler/orders/{order_id}/pay")
def recycler_mark_paid(order_id: str, req: PayOrderRequest):
    """回收员确认已向用户支付回收款"""
    with db_cursor() as conn:
        _require_role(conn, req.recycler_id, "recycler", "admin")
        row = conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()
        if not row:
            raise HTTPException(404, "订单不存在")
        if row["status"] != "completed":
            raise HTTPException(400, "订单尚未完成称重")
        if row["paid"]:
            raise HTTPException(400, "该订单已结算")
        if req.pay_method not in ("cash", "wechat", "alipay"):
            raise HTTPException(400, "支付方式不合法")
        conn.execute("UPDATE orders SET paid=1, pay_method=?, updated_at=? WHERE id=?", (req.pay_method, _now(), order_id))
        names = {"cash": "现金", "wechat": "微信", "alipay": "支付宝"}[req.pay_method]
        _notify(conn, row["user_id"], "wallet", "回收款已支付", f"订单 {row['order_no']} 回收款 ¥{row['amount']:.2f} 已通过{names}支付", order_id)
        return _ok(_order_with_items(conn, conn.execute("SELECT * FROM orders WHERE id=?", (order_id,)).fetchone()), "已标记结算")


@app.get("/api/recycler/wallet")
def recycler_wallet(recycler_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, recycler_id, "recycler", "admin")
        balance = conn.execute("SELECT COALESCE(SUM(amount),0) FROM wallet_records WHERE recycler_id=?", (recycler_id,)).fetchone()[0]
        income_total = conn.execute(
            "SELECT COALESCE(SUM(amount),0) FROM wallet_records WHERE recycler_id=? AND type='income'", (recycler_id,)
        ).fetchone()[0]
        month = datetime.now().strftime("%Y-%m")
        income_month = conn.execute(
            "SELECT COALESCE(SUM(amount),0) FROM wallet_records WHERE recycler_id=? AND type='income' AND created_at LIKE ?",
            (recycler_id, f"{month}%"),
        ).fetchone()[0]
        withdrawn = conn.execute(
            "SELECT COALESCE(SUM(amount),0) FROM withdrawals WHERE recycler_id=? AND status='approved'", (recycler_id,)
        ).fetchone()[0]
        pending_wd = conn.execute(
            "SELECT COALESCE(SUM(amount),0) FROM withdrawals WHERE recycler_id=? AND status='pending'", (recycler_id,)
        ).fetchone()[0]
        records = rows_to_list(conn.execute(
            "SELECT * FROM wallet_records WHERE recycler_id=? ORDER BY created_at DESC LIMIT 100", (recycler_id,)
        ).fetchall())
        withdrawals = rows_to_list(conn.execute(
            "SELECT * FROM withdrawals WHERE recycler_id=? ORDER BY created_at DESC LIMIT 50", (recycler_id,)
        ).fetchall())
        unpaid = rows_to_list(conn.execute(
            "SELECT * FROM orders WHERE recycler_id=? AND status='completed' AND paid=0 ORDER BY updated_at DESC", (recycler_id,)
        ).fetchall())
        paid_total = conn.execute(
            "SELECT COALESCE(SUM(amount),0) FROM orders WHERE recycler_id=? AND status='completed' AND paid=1", (recycler_id,)
        ).fetchone()[0]
        return _ok({
            "balance": round(balance, 2),
            "income_total": round(income_total, 2),
            "income_month": round(income_month, 2),
            "withdrawn": round(withdrawn, 2),
            "pending_withdraw": round(pending_wd, 2),
            "paid_to_users_total": round(paid_total, 2),
            "commission_rate": RECYCLER_COMMISSION,
            "base_fee": RECYCLER_BASE_FEE,
            "records": records,
            "withdrawals": withdrawals,
            "unpaid_orders": [_order_with_items(conn, conn.execute("SELECT * FROM orders WHERE id=?", (o["id"],)).fetchone()) for o in unpaid],
        })


@app.post("/api/recycler/withdraw")
def recycler_withdraw(req: WithdrawRequest):
    with db_cursor() as conn:
        rec = _require_role(conn, req.recycler_id, "recycler", "admin")
        balance = conn.execute("SELECT COALESCE(SUM(amount),0) FROM wallet_records WHERE recycler_id=?", (req.recycler_id,)).fetchone()[0]
        if req.amount < 10:
            raise HTTPException(400, "单次提现金额不得低于 ¥10")
        if req.amount > balance + 1e-6:
            raise HTTPException(400, f"余额不足，当前可提现 ¥{balance:.2f}")
        wid = f"wd_{uuid.uuid4().hex[:8]}"
        conn.execute(
            "INSERT INTO withdrawals VALUES (?,?,?,?,?,?,?,?,?)",
            (wid, req.recycler_id, rec["nickname"], round(req.amount, 2), req.account, "pending", "", _now(), None),
        )
        # 冻结：先从余额扣除，驳回时退回
        conn.execute(
            "INSERT INTO wallet_records VALUES (?,?,?,?,?,?,?)",
            (f"wr_{uuid.uuid4().hex[:8]}", req.recycler_id, "withdraw", -round(req.amount, 2), f"提现申请 {req.account}", wid, _now()),
        )
        _notify(conn, req.recycler_id, "wallet", "提现申请已提交", f"¥{req.amount:.2f} 提现申请审核中，预计 1-3 个工作日到账", wid)
        _notify_admins(conn, "wallet", "新的提现申请", f"{rec['nickname']} 申请提现 ¥{req.amount:.2f}，请及时审核", wid)
        return _ok(dict(conn.execute("SELECT * FROM withdrawals WHERE id=?", (wid,)).fetchone()), "提现申请已提交")


@app.get("/api/admin/withdrawals")
def admin_withdrawals(admin_id: str = Query(...), status: str = Query("all")):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        if status == "all":
            rows = conn.execute("SELECT * FROM withdrawals ORDER BY (status='pending') DESC, created_at DESC").fetchall()
        else:
            rows = conn.execute("SELECT * FROM withdrawals WHERE status=? ORDER BY created_at DESC", (status,)).fetchall()
        return _ok(rows_to_list(rows))


@app.post("/api/admin/withdrawals/{withdrawal_id}/review")
def admin_review_withdrawal(withdrawal_id: str, req: ReviewWithdrawRequest, admin_id: str = Query(...)):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        row = conn.execute("SELECT * FROM withdrawals WHERE id=?", (withdrawal_id,)).fetchone()
        if not row:
            raise HTTPException(404, "提现记录不存在")
        if row["status"] != "pending":
            raise HTTPException(400, "该申请已处理")
        new_status = "approved" if req.approve else "rejected"
        conn.execute(
            "UPDATE withdrawals SET status=?, remark=?, reviewed_at=? WHERE id=?",
            (new_status, req.remark or "", _now(), withdrawal_id),
        )
        if not req.approve:
            # 退回冻结金额
            conn.execute(
                "INSERT INTO wallet_records VALUES (?,?,?,?,?,?,?)",
                (f"wr_{uuid.uuid4().hex[:8]}", row["recycler_id"], "payout", row["amount"], "提现被驳回，金额退回", withdrawal_id, _now()),
            )
            _notify(conn, row["recycler_id"], "wallet", "提现申请被驳回", f"¥{row['amount']:.2f} 已退回钱包。原因：{req.remark or '未说明'}", withdrawal_id)
        else:
            _notify(conn, row["recycler_id"], "wallet", "提现已到账", f"¥{row['amount']:.2f} 已打款至 {row['account']}", withdrawal_id)
        return _ok(dict(conn.execute("SELECT * FROM withdrawals WHERE id=?", (withdrawal_id,)).fetchone()), "审核完成")


# =====================================================================
# 管理后台数据导出（CSV，带 UTF-8 BOM 以兼容 Excel）
# =====================================================================
def _csv_response(filename: str, header: list, rows: list) -> StreamingResponse:
    buf = io.StringIO()
    buf.write("\ufeff")
    w = csv.writer(buf)
    w.writerow(header)
    for r in rows:
        w.writerow(r)
    buf.seek(0)
    return StreamingResponse(
        iter([buf.getvalue()]),
        media_type="text/csv; charset=utf-8",
        headers={"Content-Disposition": f'attachment; filename="{filename}"'},
    )


@app.get("/api/admin/export/{dataset}")
def admin_export(dataset: str, admin_id: str = Query(...), status: str = Query("all"),
                 date_from: str = Query(""), date_to: str = Query("")):
    with db_cursor() as conn:
        _require_role(conn, admin_id, "admin")
        stamp = datetime.now().strftime("%Y%m%d_%H%M")
        if dataset == "orders":
            sql = "SELECT * FROM orders WHERE 1=1"
            params = []
            if status != "all":
                sql += " AND status=?"; params.append(status)
            if date_from:
                sql += " AND created_at >= ?"; params.append(date_from)
            if date_to:
                sql += " AND created_at <= ?"; params.append(date_to + " 23:59:59")
            sql += " ORDER BY created_at DESC"
            rows = conn.execute(sql, params).fetchall()
            status_map = {"pending": "待接单", "accepted": "已接单", "in_progress": "上门中", "completed": "已完成", "cancelled": "已取消"}
            out = []
            for o in rows:
                items = conn.execute("SELECT category_name, estimated_weight, actual_weight FROM order_items WHERE order_id=?", (o["id"],)).fetchall()
                out.append([
                    o["order_no"], status_map.get(o["status"], o["status"]), o["contact_name"], o["contact_phone"], o["address_text"],
                    o["appointment_time"], "；".join(f"{i['category_name']}({i['estimated_weight']}kg)" for i in items),
                    o["estimated_weight"], o["actual_weight"] or "", o["amount"] or "", o["points_earned"], o["carbon_saved"],
                    o["recycler_name"] or "", "是" if o["paid"] else "否", o["pay_method"] or "", o["created_at"], o["updated_at"],
                ])
            return _csv_response(f"orders_{stamp}.csv",
                ["订单号", "状态", "联系人", "手机号", "地址", "预约时间", "品类明细", "预估重量kg", "实际重量kg", "金额", "积分", "减碳kg", "回收员", "已结算", "支付方式", "创建时间", "更新时间"], out)
        if dataset == "users":
            rows = conn.execute("SELECT * FROM users ORDER BY created_at DESC").fetchall()
            role_map = {"user": "用户", "recycler": "回收员", "admin": "管理员"}
            out = [[u["id"], u["nickname"], u["phone"], role_map.get(u["role"], u["role"]), "正常" if u["status"] == "active" else "禁用",
                    u["level"], u["points"], u["total_points"], u["total_weight"], u["total_carbon"], u["created_at"]] for u in rows]
            return _csv_response(f"users_{stamp}.csv",
                ["ID", "昵称", "手机号", "角色", "状态", "等级", "可用积分", "累计积分", "累计回收kg", "累计减碳kg", "注册时间"], out)
        if dataset == "redemptions":
            rows = conn.execute("SELECT r.*, u.nickname, u.phone FROM redemptions r JOIN users u ON u.id=r.user_id ORDER BY r.created_at DESC").fetchall()
            st = {"pending": "待发放", "shipped": "已发货", "completed": "已完成"}
            out = [[r["id"], r["nickname"], r["phone"], r["product_name"], r["quantity"], r["points"], st.get(r["status"], r["status"]), r["code"], r["created_at"]] for r in rows]
            return _csv_response(f"redemptions_{stamp}.csv", ["ID", "用户", "手机号", "商品", "数量", "积分", "状态", "兑换码", "时间"], out)
        if dataset == "withdrawals":
            rows = conn.execute("SELECT * FROM withdrawals ORDER BY created_at DESC").fetchall()
            st = {"pending": "待审核", "approved": "已打款", "rejected": "已驳回"}
            out = [[w["id"], w["recycler_name"], w["amount"], w["account"], st.get(w["status"], w["status"]), w["remark"] or "", w["created_at"], w["reviewed_at"] or ""] for w in rows]
            return _csv_response(f"withdrawals_{stamp}.csv", ["ID", "回收员", "金额", "收款账户", "状态", "备注", "申请时间", "审核时间"], out)
        if dataset == "points":
            rows = conn.execute("SELECT p.*, u.nickname FROM point_records p JOIN users u ON u.id=p.user_id ORDER BY p.created_at DESC").fetchall()
            out = [[p["id"], p["nickname"], "收入" if p["amount"] > 0 else "支出", p["amount"], p["title"], p["created_at"]] for p in rows]
            return _csv_response(f"points_{stamp}.csv", ["ID", "用户", "类型", "积分", "说明", "时间"], out)
        if dataset == "categories":
            rows = conn.execute("SELECT * FROM categories ORDER BY sort_order").fetchall()
            out = [[c["id"], c["name"], c["price_per_kg"], c["points_per_kg"], c["carbon_per_kg"], c["description"]] for c in rows]
            return _csv_response(f"categories_{stamp}.csv", ["ID", "品类", "单价/kg", "积分/kg", "减碳kg/kg", "说明"], out)
        raise HTTPException(404, "不支持的数据集")
