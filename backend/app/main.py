"""绿循环 Green Cycle 后端 API（FastAPI + SQLite）"""
import random
import string
import uuid
from datetime import datetime

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware

from .database import db_cursor, init_db, rows_to_list
from .schemas import (
    AddressIn,
    CreateOrderRequest,
    LoginRequest,
    RedeemRequest,
    RegisterRequest,
    SignInRequest,
    UpdateProfileRequest,
)
from .seed import DEMO_USER_ID, RECYCLERS, seed_if_empty

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


def _level_for(total_points: int) -> int:
    level = 1
    for i, th in enumerate(LEVEL_THRESHOLDS):
        if total_points >= th:
            level = i + 1
    return min(level, len(LEVEL_THRESHOLDS))


def _user_payload(row) -> dict:
    u = dict(row)
    u.pop("password", None)
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
        return _ok({"token": f"tk_{row['id']}", "user": _user_payload(row)})


@app.post("/api/auth/register")
def register(req: RegisterRequest):
    with db_cursor() as conn:
        if conn.execute("SELECT 1 FROM users WHERE phone = ?", (req.phone,)).fetchone():
            raise HTTPException(400, "该手机号已注册")
        uid = f"u_{uuid.uuid4().hex[:10]}"
        conn.execute(
            "INSERT INTO users VALUES (?,?,?,?,?,?,?,?,?,?,?)",
            (uid, req.phone, req.password, req.nickname,
             f"https://i.pravatar.cc/150?img={random.randint(1, 70)}", 1, 100, 100, 0, 0, _now()),
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
def create_address(req: AddressIn):
    with db_cursor() as conn:
        aid = f"addr_{uuid.uuid4().hex[:8]}"
        if req.is_default:
            conn.execute("UPDATE addresses SET is_default=0 WHERE user_id=?", (req.user_id,))
        conn.execute(
            "INSERT INTO addresses VALUES (?,?,?,?,?,?,?)",
            (aid, req.user_id, req.contact_name, req.phone, req.region, req.detail, int(req.is_default)),
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
            "INSERT INTO orders VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            (oid, order_no, req.user_id, "pending", f"{addr['region']} {addr['detail']}",
             addr["contact_name"], addr["phone"], req.appointment_time, req.remark or "",
             est, None, None, 0, 0, None, None, None, now, now),
        )
        for it in req.items:
            cat = conn.execute("SELECT * FROM categories WHERE id=?", (it.category_id,)).fetchone()
            if not cat:
                raise HTTPException(400, f"品类不存在: {it.category_id}")
            conn.execute(
                "INSERT INTO order_items (order_id, category_id, category_name, estimated_weight) VALUES (?,?,?,?)",
                (oid, cat["id"], cat["name"], it.estimated_weight),
            )
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
                "UPDATE orders SET status=?, recycler_name=?, recycler_phone=?, recycler_avatar=?, updated_at=? WHERE id=?",
                (nxt, rec[0], rec[1], rec[2], _now(), order_id),
            )
        elif nxt == "completed":
            items = conn.execute("SELECT * FROM order_items WHERE order_id=?", (order_id,)).fetchall()
            amount = points = carbon = weight = 0.0
            for it in items:
                cat = conn.execute("SELECT * FROM categories WHERE id=?", (it["category_id"],)).fetchone()
                actual = round(it["estimated_weight"] * random.uniform(0.9, 1.1), 1)
                conn.execute("UPDATE order_items SET actual_weight=? WHERE id=?", (actual, it["id"]))
                weight += actual
                amount += actual * cat["price_per_kg"]
                points += actual * cat["points_per_kg"]
                carbon += actual * cat["carbon_per_kg"]
            points = int(points)
            conn.execute(
                "UPDATE orders SET status=?, actual_weight=?, amount=?, points_earned=?, carbon_saved=?, updated_at=? WHERE id=?",
                (nxt, round(weight, 2), round(amount, 2), points, round(carbon, 2), _now(), order_id),
            )
            conn.execute(
                "UPDATE users SET total_weight = total_weight + ?, total_carbon = total_carbon + ? WHERE id=?",
                (round(weight, 2), round(carbon, 2), row["user_id"]),
            )
            names = "、".join(i["category_name"] for i in items)
            _add_points(conn, row["user_id"], points, f"回收订单 {names}", order_id)
        else:
            conn.execute("UPDATE orders SET status=?, updated_at=? WHERE id=?", (nxt, _now(), order_id))
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
