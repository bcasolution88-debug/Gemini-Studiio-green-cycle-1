"""初始化示例数据"""
import uuid
from datetime import datetime, timedelta

from .database import db_cursor

DEMO_USER_ID = "u_demo_001"
ADMIN_ID = "u_admin_001"


def _now(offset_days: int = 0, offset_hours: int = 0) -> str:
    return (datetime.now() + timedelta(days=offset_days, hours=offset_hours)).strftime("%Y-%m-%d %H:%M:%S")


CATEGORIES = [
    ("c_paper", "纸类", "paper", "#42A5F5", 1.2, 12, 1.5, "报纸、书本、纸箱、办公用纸等", 1),
    ("c_plastic", "塑料", "plastic", "#FFA726", 0.8, 8, 2.0, "饮料瓶、塑料盒、塑料袋等", 2),
    ("c_metal", "金属", "metal", "#AB47BC", 2.5, 25, 3.2, "易拉罐、铁器、铜铝制品等", 3),
    ("c_clothes", "旧衣物", "clothes", "#EC407A", 0.5, 6, 3.6, "衣服、鞋子、包包、床上用品等", 4),
    ("c_electronic", "电子产品", "electronic", "#26A69A", 5.0, 50, 8.0, "手机、电脑、小家电等（上门估价）", 5),
    ("c_glass", "玻璃", "glass", "#66BB6A", 0.3, 4, 0.6, "玻璃瓶、玻璃器皿等", 6),
    ("c_appliance", "大家电", "appliance", "#5C6BC0", 3.0, 30, 6.5, "冰箱、洗衣机、空调等（上门估价）", 7),
    ("c_book", "书籍", "book", "#8D6E63", 1.5, 15, 1.8, "教材、图书、杂志等", 8),
]

PRODUCTS = [
    ("p_001", "环保帆布袋", "100%纯棉帆布，可重复使用，告别塑料袋", "https://images.unsplash.com/photo-1597484662317-c93a56409db5?w=400", 300, 120, "生活用品", 1, 856),
    ("p_002", "竹纤维牙刷套装", "可降解竹柄，软毛护龈，4支装", "https://images.unsplash.com/photo-1607613009820-a29f7bb81c04?w=400", 450, 80, "生活用品", 1, 623),
    ("p_003", "咖啡饮品券", "合作咖啡店任意中杯饮品一杯", "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400", 800, 200, "优惠券", 1, 1204),
    ("p_004", "不锈钢保温杯", "500ml双层真空，12小时保温", "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=400", 1500, 45, "生活用品", 0, 312),
    ("p_005", "多肉植物盆栽", "随机品种，附环保陶土盆", "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=400", 600, 60, "绿植", 1, 445),
    ("p_006", "再生纸笔记本", "100%再生纸制作，A5尺寸 120页", "https://images.unsplash.com/photo-1531346878377-a5be20888e57?w=400", 250, 300, "文具", 0, 978),
    ("p_007", "超市购物券 20元", "合作超市通用，满50可用", "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=400", 1000, 150, "优惠券", 0, 567),
    ("p_008", "太阳能充电宝", "10000mAh，支持太阳能应急充电", "https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?w=400", 3200, 20, "数码", 0, 89),
    ("p_009", "有机棉T恤", "环保染料，有机棉材质，多色可选", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400", 1800, 35, "服饰", 0, 156),
    ("p_010", "植树公益捐赠", "以您的名义在沙漠种下一棵树", "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=400", 500, 9999, "公益", 1, 2341),
]

ARTICLES = [
    ("a_001", "垃圾分类全攻略：这些常见误区你中招了吗？", "很多人以为用过的纸巾是可回收物，其实它属于其他垃圾……", "垃圾分类看似简单，实际上有不少容易混淆的地方。\n\n1. 用过的纸巾、湿巾：属于其他垃圾，不可回收。\n2. 快递盒：撕掉胶带后属于可回收物。\n3. 外卖餐盒：需清洗干净后才可回收。\n4. 玻璃制品：属于可回收物，但碎玻璃需包好。\n5. 大骨头：属于其他垃圾而非厨余垃圾。\n\n正确分类，从我做起，让资源循环起来！", "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=600", "分类知识", "绿循环小编", 12580, 892, _now(-2)),
    ("a_002", "一个塑料瓶的旅程：从垃圾到再生纤维", "你知道吗？8个塑料瓶就能制作一件T恤……", "塑料瓶被回收后，会经历分拣、清洗、粉碎、熔融等工序，最终变成再生聚酯纤维（rPET）。\n\n这些纤维可用于制作衣物、包袋、地毯等产品。每回收1吨塑料瓶，可节约6吨石油，减少约3吨二氧化碳排放。\n\n下次喝完饮料，记得把瓶子投入可回收桶哦！", "https://images.unsplash.com/photo-1605600659908-0ef719419d41?w=600", "环保科普", "环保研究所", 8932, 654, _now(-4)),
    ("a_003", "旧衣物去哪儿了？揭秘回收后的三种归宿", "每年约有2600万吨旧衣物被丢弃，仅有不到10%被有效回收……", "回收的旧衣物通常有三种归宿：\n\n一、八成新以上的衣物经消毒后捐赠给需要的人群；\n二、品相一般的衣物出口或拆解为再生纤维；\n三、无法利用的衣物经处理后用于制作保温材料、工业抹布等。\n\n请在投递前清洗干净并打包好，便于后续处理。", "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=600", "环保科普", "绿循环小编", 6721, 423, _now(-6)),
    ("a_004", "绿循环上线「碳积分商城」，回收即可兑好礼", "即日起，所有回收订单均可获得碳积分，积分可在商城兑换环保好物……", "为鼓励更多用户参与资源回收，绿循环正式推出碳积分体系：\n\n- 每回收1kg纸类可获12积分\n- 每回收1kg金属可获25积分\n- 电子产品按估价给予积分奖励\n\n积分可兑换环保帆布袋、咖啡券、绿植盆栽等多种好礼，更可参与植树公益活动。", "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=600", "平台动态", "绿循环官方", 15230, 1876, _now(-1)),
    ("a_005", "废旧电池的正确处理方式", "一节5号电池可污染1平方米土地60年……", "废旧电池含有汞、镉、铅等重金属，随意丢弃会严重污染土壤和水源。\n\n正确处理方式：\n1. 普通干电池：目前已实现低汞化，可作为其他垃圾投放\n2. 纽扣电池、充电电池、手机电池：属于有害垃圾，需投入专门回收箱\n3. 可通过绿循环预约上门回收电子产品，专业处理", "https://images.unsplash.com/photo-1619641805634-b867f535e3c4?w=600", "分类知识", "环保研究所", 5432, 321, _now(-8)),
    ("a_006", "本月减碳榜单：这些社区回收量领先", "朝阳区阳光花园社区以月回收量12.6吨登顶榜首……", "10月社区回收排行榜出炉！\n\n第一名：阳光花园社区 12.6吨\n第二名：绿地中央广场 10.8吨\n第三名：万科城市之光 9.3吨\n\n感谢每一位参与回收的居民，你们的每一份努力都在让地球变得更好。", "https://images.unsplash.com/photo-1518531933037-91b2f5f229cc?w=600", "平台动态", "绿循环官方", 4210, 567, _now(-3)),
    ("a_007", "厨余垃圾变肥料：家庭堆肥入门指南", "在阳台就能完成的厨余堆肥，让果皮菜叶变废为宝……", "家庭堆肥步骤：\n\n1. 准备一个带盖透气的堆肥桶\n2. 厨余垃圾（果皮、菜叶、咖啡渣）与干料（落叶、碎纸）按1:3混合\n3. 保持湿润，每周翻动一次\n4. 约2-3个月后即可获得黑褐色的有机肥\n\n注意：肉类、油脂、乳制品不宜堆肥。", "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=600", "环保科普", "绿色生活家", 3890, 276, _now(-10)),
    ("a_008", "回收员的一天：穿梭在城市中的「绿色使者」", "凌晨5点，回收员老王已经开始了一天的工作……", "老王是绿循环的一名回收员，每天要上门服务20多个家庭。\n\n「以前大家觉得收废品不体面，现在通过APP预约，居民都很客气，还经常给我们递水。」老王笑着说。\n\n每一位回收员都是城市循环经济的毛细血管，向他们致敬！", "https://images.unsplash.com/photo-1604187351574-c75ca79f5807?w=600", "人物故事", "绿循环小编", 7654, 1123, _now(-5)),
]

ACHIEVEMENTS = [
    ("ach_001", "初出茅庐", "完成首次回收", "star", "orders", 1),
    ("ach_002", "回收达人", "累计完成10次回收", "military_tech", "orders", 10),
    ("ach_003", "环保卫士", "累计完成30次回收", "shield", "orders", 30),
    ("ach_004", "减碳先锋", "累计减碳超过50kg", "eco", "carbon", 50),
    ("ach_005", "地球守护者", "累计减碳超过200kg", "public", "carbon", 200),
    ("ach_006", "百斤王者", "累计回收重量超过100kg", "fitness_center", "weight", 100),
    ("ach_007", "积分富翁", "累计获得5000积分", "savings", "points", 5000),
    ("ach_008", "爱心使者", "完成一次公益兑换", "volunteer_activism", "redeem", 1),
]

# (id, 姓名, 手机, 头像)
RECYCLERS = [
    ("r_001", "王师傅", "13800001111", "https://i.pravatar.cc/150?img=12"),
    ("r_002", "李师傅", "13800002222", "https://i.pravatar.cc/150?img=33"),
    ("r_003", "张师傅", "13800003333", "https://i.pravatar.cc/150?img=51"),
]

EXTRA_USERS = [
    ("u_002", "13700000002", "陈晨", "https://i.pravatar.cc/150?img=5", 2, 620, 980, 21.5, 38.2),
    ("u_003", "13700000003", "林夕", "https://i.pravatar.cc/150?img=9", 3, 1450, 1860, 42.0, 75.6),
    ("u_004", "13700000004", "周末", "https://i.pravatar.cc/150?img=15", 1, 180, 180, 4.2, 6.8),
    ("u_005", "13700000005", "赵小禾", "https://i.pravatar.cc/150?img=20", 5, 5200, 7300, 160.3, 290.1),
    ("u_006", "13700000006", "吴桐", "https://i.pravatar.cc/150?img=25", 2, 300, 1100, 25.0, 44.0),
]


def seed_if_empty():
    with db_cursor() as conn:
        cnt = conn.execute("SELECT COUNT(*) FROM categories").fetchone()[0]
        if cnt > 0:
            return

        conn.executemany(
            "INSERT INTO categories VALUES (?,?,?,?,?,?,?,?,?)", CATEGORIES
        )
        conn.executemany("INSERT INTO products VALUES (?,?,?,?,?,?,?,?,?)", PRODUCTS)
        conn.executemany("INSERT INTO articles VALUES (?,?,?,?,?,?,?,?,?,?)", ARTICLES)
        conn.executemany("INSERT INTO achievements VALUES (?,?,?,?,?,?)", ACHIEVEMENTS)

        # 演示用户 / 回收员 / 管理员
        conn.execute(
            "INSERT INTO users VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
            (DEMO_USER_ID, "13912345678", "123456", "小绿", "https://i.pravatar.cc/150?img=47",
             "user", "active", 4, 2860, 4120, 86.4, 148.6, _now(-120)),
        )
        for rid, name, phone, avatar in RECYCLERS:
            conn.execute(
                "INSERT INTO users VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                (rid, phone, "123456", name, avatar, "recycler", "active", 1, 0, 0, 0, 0, _now(-200)),
            )
        conn.execute(
            "INSERT INTO users VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
            (ADMIN_ID, "13900000000", "admin123", "平台管理员", "https://i.pravatar.cc/150?img=60",
             "admin", "active", 1, 0, 0, 0, 0, _now(-365)),
        )
        for uid, phone, nick, avatar, lvl, pts, tpts, w, c in EXTRA_USERS:
            conn.execute(
                "INSERT INTO users VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                (uid, phone, "123456", nick, avatar, "user", "active", lvl, pts, tpts, w, c, _now(-90)),
            )
            conn.execute(
                "INSERT INTO addresses VALUES (?,?,?,?,?,?,?)",
                (f"addr_{uid}", uid, nick, phone, "北京市 朝阳区", f"绿地中央广场 {uid[-1]}号楼 {uid[-1]}0{uid[-1]}", 1),
            )
        conn.execute(
            "INSERT INTO addresses VALUES (?,?,?,?,?,?,?)",
            ("addr_001", DEMO_USER_ID, "小绿", "13912345678", "北京市 朝阳区", "阳光花园小区 3号楼 2单元 801", 1),
        )
        conn.execute(
            "INSERT INTO addresses VALUES (?,?,?,?,?,?,?)",
            ("addr_002", DEMO_USER_ID, "小绿", "13912345678", "北京市 海淀区", "中关村科技园 创新大厦 12层", 0),
        )

        # 历史订单
        history = [
            ("o_001", "completed", -30, [("c_paper", "纸类", 8.5, 8.2), ("c_plastic", "塑料", 3.0, 2.8)], 0),
            ("o_002", "completed", -22, [("c_clothes", "旧衣物", 12.0, 11.5)], 1),
            ("o_003", "completed", -15, [("c_metal", "金属", 4.0, 4.3), ("c_glass", "玻璃", 2.0, 2.0)], 2),
            ("o_004", "completed", -8, [("c_paper", "纸类", 6.0, 6.4), ("c_book", "书籍", 5.0, 5.0)], 0),
            ("o_005", "completed", -3, [("c_electronic", "电子产品", 1.5, 1.5)], 1),
            ("o_006", "in_progress", 0, [("c_paper", "纸类", 5.0, None), ("c_plastic", "塑料", 2.0, None)], 2),
            ("o_007", "pending", 1, [("c_clothes", "旧衣物", 8.0, None)], None),
        ]
        cat_map = {c[0]: c for c in CATEGORIES}
        for idx, (oid, status, day_off, items, recycler_idx) in enumerate(history):
            est = sum(i[2] for i in items)
            actual = sum(i[3] for i in items if i[3] is not None) if status == "completed" else None
            amount = points = carbon = 0
            if status == "completed":
                for cid, _, _, aw in items:
                    c = cat_map[cid]
                    amount += aw * c[4]
                    points += int(aw * c[5])
                    carbon += aw * c[6]
            rec = RECYCLERS[recycler_idx] if recycler_idx is not None else (None, None, None, None)
            created = _now(day_off, -2)
            conn.execute(
                "INSERT INTO orders VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                (oid, f"GC{datetime.now().strftime('%Y%m')}{1000 + idx:04d}", DEMO_USER_ID, status,
                 "北京市 朝阳区 阳光花园小区 3号楼 2单元 801", "小绿", "13912345678",
                 _now(day_off).replace(_now(day_off)[11:], "14:00-16:00"), "请提前电话联系",
                 est, actual, round(amount, 2) if actual else None, points, round(carbon, 2),
                 rec[0], rec[1], rec[2], rec[3], created, created),
            )
            for cid, cname, ew, aw in items:
                conn.execute(
                    "INSERT INTO order_items (order_id, category_id, category_name, estimated_weight, actual_weight) VALUES (?,?,?,?,?)",
                    (oid, cid, cname, ew, aw),
                )
            if status == "completed" and points > 0:
                conn.execute(
                    "INSERT INTO point_records VALUES (?,?,?,?,?,?,?)",
                    (f"pr_{oid}", DEMO_USER_ID, "earn", points, f"回收订单 {'、'.join(i[1] for i in items)}", oid, created),
                )

        # 其他用户的订单（供回收员接单大厅 / 管理后台使用）
        others = [
            # (oid, user_id, status, day_off, slot, items, recycler_idx)
            ("o_101", "u_002", "pending", 0, "09:00-11:00", [("c_paper", "纸类", 10.0, None)], None),
            ("o_102", "u_003", "pending", 0, "16:00-18:00", [("c_metal", "金属", 6.0, None), ("c_plastic", "塑料", 4.0, None)], None),
            ("o_103", "u_005", "pending", 1, "11:00-13:00", [("c_appliance", "大家电", 45.0, None)], None),
            ("o_104", "u_004", "pending", 1, "14:00-16:00", [("c_clothes", "旧衣物", 7.0, None)], None),
            ("o_105", "u_006", "accepted", 0, "14:00-16:00", [("c_book", "书籍", 12.0, None)], 0),
            ("o_106", "u_002", "in_progress", 0, "09:00-11:00", [("c_glass", "玻璃", 3.0, None)], 0),
            ("o_107", "u_003", "completed", -1, "14:00-16:00", [("c_paper", "纸类", 9.0, 9.4)], 0),
            ("o_108", "u_005", "completed", -2, "16:00-18:00", [("c_electronic", "电子产品", 2.0, 2.0), ("c_metal", "金属", 5.0, 5.5)], 0),
            ("o_109", "u_006", "completed", -5, "09:00-11:00", [("c_plastic", "塑料", 6.0, 5.8)], 1),
            ("o_110", "u_002", "completed", -9, "11:00-13:00", [("c_clothes", "旧衣物", 15.0, 14.2)], 2),
            ("o_111", "u_004", "cancelled", -4, "14:00-16:00", [("c_paper", "纸类", 3.0, None)], None),
            ("o_112", "u_005", "completed", -40, "14:00-16:00", [("c_metal", "金属", 20.0, 21.0)], 0),
            ("o_113", "u_003", "completed", -70, "09:00-11:00", [("c_paper", "纸类", 14.0, 13.6)], 1),
        ]
        user_map = {u[0]: u for u in EXTRA_USERS}
        for idx, (oid, uid, status, day_off, slot, items, recycler_idx) in enumerate(others):
            u = user_map[uid]
            est = sum(i[2] for i in items)
            actual = sum(i[3] for i in items if i[3] is not None) if status == "completed" else None
            amount = points = carbon = 0
            if status == "completed":
                for cid, _, _, aw in items:
                    c = cat_map[cid]
                    amount += aw * c[4]
                    points += int(aw * c[5])
                    carbon += aw * c[6]
            rec = RECYCLERS[recycler_idx] if recycler_idx is not None else (None, None, None, None)
            created = _now(day_off, -3)
            conn.execute(
                "INSERT INTO orders VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                (oid, f"GC{datetime.now().strftime('%Y%m')}{2000 + idx:04d}", uid, status,
                 f"北京市 朝阳区 绿地中央广场 {uid[-1]}号楼 {uid[-1]}0{uid[-1]}", u[2], u[1],
                 f"{_now(day_off)[:10]} {slot}", "",
                 est, actual, round(amount, 2) if actual else None, points, round(carbon, 2),
                 rec[0], rec[1], rec[2], rec[3], created, created),
            )
            for cid, cname, ew, aw in items:
                conn.execute(
                    "INSERT INTO order_items (order_id, category_id, category_name, estimated_weight, actual_weight) VALUES (?,?,?,?,?)",
                    (oid, cid, cname, ew, aw),
                )

        # 兑换记录
        conn.execute(
            "INSERT INTO redemptions VALUES (?,?,?,?,?,?,?,?,?,?)",
            ("rd_001", DEMO_USER_ID, "p_001", "环保帆布袋", PRODUCTS[0][3], 300, 1, "shipped", "GC-RD-7A3F9K", _now(-12)),
        )
        conn.execute(
            "INSERT INTO point_records VALUES (?,?,?,?,?,?,?)",
            ("pr_rd_001", DEMO_USER_ID, "spend", -300, "兑换 环保帆布袋", "rd_001", _now(-12)),
        )
        conn.execute(
            "INSERT INTO redemptions VALUES (?,?,?,?,?,?,?,?,?,?)",
            ("rd_002", DEMO_USER_ID, "p_003", "咖啡饮品券", PRODUCTS[2][3], 800, 1, "completed", "GC-RD-2M8X4Q", _now(-20)),
        )
        conn.execute(
            "INSERT INTO point_records VALUES (?,?,?,?,?,?,?)",
            ("pr_rd_002", DEMO_USER_ID, "spend", -800, "兑换 咖啡饮品券", "rd_002", _now(-20)),
        )
        conn.execute(
            "INSERT INTO point_records VALUES (?,?,?,?,?,?,?)",
            ("pr_signin_001", DEMO_USER_ID, "earn", 10, "每日签到", None, _now(0, -5)),
        )
