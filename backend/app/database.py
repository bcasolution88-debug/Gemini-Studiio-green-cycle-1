"""SQLite 数据库初始化与连接管理"""
import os
import sqlite3
from contextlib import contextmanager

DB_PATH = os.environ.get("GREEN_CYCLE_DB", os.path.join(os.path.dirname(__file__), "..", "green_cycle.db"))

SCHEMA = """
CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    phone TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    nickname TEXT NOT NULL,
    avatar TEXT,
    role TEXT NOT NULL DEFAULT 'user',
    status TEXT NOT NULL DEFAULT 'active',
    level INTEGER NOT NULL DEFAULT 1,
    points INTEGER NOT NULL DEFAULT 0,
    total_points INTEGER NOT NULL DEFAULT 0,
    total_weight REAL NOT NULL DEFAULT 0,
    total_carbon REAL NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    icon TEXT NOT NULL,
    color TEXT NOT NULL,
    price_per_kg REAL NOT NULL,
    points_per_kg INTEGER NOT NULL,
    carbon_per_kg REAL NOT NULL,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS addresses (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    contact_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    region TEXT NOT NULL,
    detail TEXT NOT NULL,
    is_default INTEGER NOT NULL DEFAULT 0,
    lat REAL,
    lng REAL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS orders (
    id TEXT PRIMARY KEY,
    order_no TEXT UNIQUE NOT NULL,
    user_id TEXT NOT NULL,
    status TEXT NOT NULL,
    address_text TEXT NOT NULL,
    contact_name TEXT NOT NULL,
    contact_phone TEXT NOT NULL,
    appointment_time TEXT NOT NULL,
    remark TEXT,
    estimated_weight REAL NOT NULL DEFAULT 0,
    actual_weight REAL,
    amount REAL,
    points_earned INTEGER NOT NULL DEFAULT 0,
    carbon_saved REAL NOT NULL DEFAULT 0,
    recycler_id TEXT,
    recycler_name TEXT,
    recycler_phone TEXT,
    recycler_avatar TEXT,
    lat REAL,
    lng REAL,
    paid INTEGER NOT NULL DEFAULT 0,
    pay_method TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id TEXT NOT NULL,
    category_id TEXT NOT NULL,
    category_name TEXT NOT NULL,
    estimated_weight REAL NOT NULL,
    actual_weight REAL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE IF NOT EXISTS point_records (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    type TEXT NOT NULL,
    amount INTEGER NOT NULL,
    title TEXT NOT NULL,
    related_id TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS products (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    image TEXT NOT NULL,
    points INTEGER NOT NULL,
    stock INTEGER NOT NULL,
    category TEXT NOT NULL,
    is_hot INTEGER NOT NULL DEFAULT 0,
    sold INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS redemptions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    product_id TEXT NOT NULL,
    product_name TEXT NOT NULL,
    product_image TEXT NOT NULL,
    points INTEGER NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    status TEXT NOT NULL,
    code TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS articles (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    summary TEXT NOT NULL,
    content TEXT NOT NULL,
    cover TEXT NOT NULL,
    category TEXT NOT NULL,
    author TEXT NOT NULL,
    views INTEGER NOT NULL DEFAULT 0,
    likes INTEGER NOT NULL DEFAULT 0,
    published_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS notifications (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    related_id TEXT,
    is_read INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS recycler_locations (
    recycler_id TEXT PRIMARY KEY,
    lat REAL NOT NULL,
    lng REAL NOT NULL,
    heading REAL,
    speed REAL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS wallet_records (
    id TEXT PRIMARY KEY,
    recycler_id TEXT NOT NULL,
    type TEXT NOT NULL,              -- income / payout / withdraw
    amount REAL NOT NULL,            -- 正为收入，负为支出
    title TEXT NOT NULL,
    related_id TEXT,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS withdrawals (
    id TEXT PRIMARY KEY,
    recycler_id TEXT NOT NULL,
    recycler_name TEXT NOT NULL,
    amount REAL NOT NULL,
    account TEXT NOT NULL,
    status TEXT NOT NULL,            -- pending / approved / rejected
    remark TEXT,
    created_at TEXT NOT NULL,
    reviewed_at TEXT
);

CREATE TABLE IF NOT EXISTS achievements (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    icon TEXT NOT NULL,
    condition_type TEXT NOT NULL,
    condition_value REAL NOT NULL
);
"""


def get_connection() -> sqlite3.Connection:
    conn = sqlite3.connect(DB_PATH, check_same_thread=False)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON")
    return conn


@contextmanager
def db_cursor():
    conn = get_connection()
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


def init_db():
    with db_cursor() as conn:
        conn.executescript(SCHEMA)


def rows_to_list(rows):
    return [dict(r) for r in rows]
