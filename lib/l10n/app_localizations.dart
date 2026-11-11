import 'package:flutter/material.dart';

/// 轻量级多语言实现（中文 / English）
///
/// 使用方式：`context.l10n.home` 或 `AppLocalizations.of(context).home`
/// 新增文案时只需在 [_zh] 与 [_en] 两张表中各加一行。
class AppLocalizations {
  final Locale locale;
  AppLocalizations(this.locale);

  static const supportedLocales = [Locale('zh'), Locale('en')];

  static AppLocalizations of(BuildContext context) =>
      Localizations.of<AppLocalizations>(context, AppLocalizations) ?? AppLocalizations(const Locale('zh'));

  static const LocalizationsDelegate<AppLocalizations> delegate = _Delegate();

  bool get isZh => locale.languageCode == 'zh';

  // ---- 运行时文案翻译表（中文原文 → English）----
  // 用法：context.tr('中文')；未收录的文案原样返回，保证中文环境零风险。
  static const Map<String, String> _zh2en = {
    '待接单': 'Pending',
    '已接单': 'Accepted',
    '回收员正在赶来': 'Recycler on the way',
    '已完成': 'Completed',
    '已取消': 'Cancelled',
    '待发放': 'Processing',
    '已发货': 'Shipped',
    '已打款': 'Paid',
    '已驳回': 'Rejected',
    '审核中': 'Under review',
    '未知': 'Unknown',
    "暂无数据": "No data",
    "更多": "More",
    "环保资讯": "Eco News",
    "积分商城热门": "Hot in Points Mall",
    "已签到": "Checked in",
    "签到 +10": "Check in +10",
    "我的碳积分": "My Carbon Points",
    "已达最高等级": "Max level reached",
    "回收品类 · 今日价格": "Categories · Today's Prices",
    "本周环保成就": "This Week's Impact",
    "开始你的第一单回收": "Start your first pickup",
    "立即预约": "Book now",
    "单回收": "pickups",
    "kg 重量": "kg collected",
    "kg 减碳": "kg CO₂ saved",
    "积分": "points",
    "回收员 2 小时内上门 · 称重即结算 · 首单额外 +50 积分": "Pickup within 2h · Settle on weighing · +50 pts for first order",
    "进行中的订单": "Active order",
    "全部": "All",
    "进行中": "In progress",
    "我的订单": "My Orders",
    "还没有回收订单": "No pickups yet",
    "预约第一单，立得 50 积分 + 减碳报告": "Book your first pickup for 50 pts + a carbon report",
    "去预约": "Book a pickup",
    "请选择上门地址": "Please select a pickup address",
    "预约上门回收": "Book a Pickup",
    "选择回收品类": "Choose categories",
    "可多选，拖动滑块设置预估重量": "Multi-select · drag slider for estimated weight",
    "上门地址": "Pickup address",
    "上门时间": "Pickup time",
    "备注": "Notes",
    "选填": "optional",
    "例如：请提前电话联系、物品在门口等": "e.g. call before arrival, items at the door",
    "请选择或新增上门地址": "Select or add a pickup address",
    "更改": "Change",
    "预估 ": "Est. ",
    "确认预约": "Confirm booking",
    "请至少选择一种回收品类": "Select at least one category",
    "已下单": "Placed",
    "上门中": "On the way",
    "预约成功！回收员将尽快接单": "Booked! A recycler will accept soon",
    "取消预约": "Cancel booking",
    "确定要取消本次上门回收预约吗？": "Cancel this pickup booking?",
    "再想想": "Not now",
    "确定取消": "Yes, cancel",
    "已取消预约": "Booking cancelled",
    "订单状态已更新": "Order status updated",
    "准时上门": "On time",
    "称重公正": "Fair weighing",
    "态度友好": "Friendly",
    "搬运细心": "Careful handling",
    "沟通顺畅": "Good communication",
    "感谢评价，+5 积分": "Thanks for rating, +5 pts",
    "我的评价": "My Review",
    "评价回收员": "Rate the Recycler",
    "评价 +5 积分": "Rate +5 pts",
    "很差": "Poor",
    "较差": "Fair",
    "一般": "OK",
    "满意": "Good",
    "非常满意": "Excellent",
    "还有什么想说的（选填）": "Anything else? (optional)",
    "提交评价": "Submit review",
    "订单详情": "Order Details",
    "回收员实时位置": "Recycler live location",
    " 认证回收员 · 已实名": " Verified recycler",
    "回收明细": "Pickup Items",
    "实际总重": "Total weight",
    "回收金额": "Amount",
    "获得积分": "Points earned",
    "减碳量": "CO₂ saved",
    "回收款": "Payment",
    "待回收员支付": "Awaiting payment",
    "预约信息": "Booking Info",
    "订单编号": "Order No.",
    "联系人": "Contact",
    "下单时间": "Placed at",
    "订单已提交，正在为您匹配附近的回收员": "Submitted — matching a nearby recycler",
    "回收员": "Recycler",
    "订单已取消": "Order cancelled",
    "模拟：回收员接单": "Demo: recycler accepts",
    "模拟：回收员出发": "Demo: recycler departs",
    "模拟：称重完成": "Demo: weighing done",
    "推进状态": "Advance",
    "生活用品": "Daily goods",
    "优惠券": "Coupons",
    "绿植": "Plants",
    "文具": "Stationery",
    "数码": "Digital",
    "服饰": "Apparel",
    "公益": "Charity",
    "积分商城": "Points Mall",
    "用积分兑换环保好物": "Redeem eco goods with points",
    "该分类暂无商品": "No items in this category",
    "确认兑换": "Confirm redemption",
    "取消": "Cancel",
    "确认": "Confirm",
    "兑换成功": "Redeemed",
    "兑换码 ": "Code ",
    "好的": "OK",
    "热门": "Hot",
    " 积分": " pts",
    "商品介绍": "About",
    "兑换说明": "How it works",
    "兑换数量": "Quantity",
    "我的积分": "My points",
    "已兑完": "Sold out",
    "分类知识": "Sorting Tips",
    "环保科普": "Eco Science",
    "平台动态": "Updates",
    "人物故事": "Stories",
    "分类知识 · 环保科普 · 绿色生活": "Sorting · Science · Green living",
    "暂无资讯": "No articles yet",
    "修改昵称": "Edit nickname",
    "保存": "Save",
    "昵称已更新": "Nickname updated",
    "退出登录": "Log out",
    "确定要退出当前账号吗？": "Log out of this account?",
    "退出": "Log out",
    "我的": "Me",
    "环保成就": "Achievements",
    "我的服务": "My Services",
    "减碳报告": "Carbon Report",
    "查看回收趋势与环保贡献": "Trends & environmental impact",
    "积分明细": "Points History",
    "收支记录一目了然": "All earnings & spending",
    "兑换记录": "Redemptions",
    "查看兑换码与发货状态": "Codes & shipping status",
    "地址管理": "Addresses",
    "上门回收地址": "Pickup addresses",
    "消息中心": "Notifications",
    "订单进度、积分到账提醒": "Order progress & points alerts",
    "后端服务设置": "Server Settings",
    "切换 API 地址": "Switch API endpoint",
    "可用积分": "Points",
    "回收 kg": "kg recycled",
    "减碳 kg": "kg CO₂",
    "回收次数": "Pickups",
    "全部已读": "Mark all read",
    "订单": "Orders",
    "钱包": "Wallet",
    "系统": "System",
    "一切安好，暂无新消息": "All clear — no new messages",
    "刚刚": "Just now",
    "一键预约，回收员上门": "One tap to book a pickup",
    "选品类、拖重量、挑时段，回收员 2 小时内到家，现场称重即时结算到账。": "Pick categories, set weight and time. A recycler arrives within 2 hours and settles on the spot.",
    "回收即赚碳积分": "Recycle, earn carbon points",
    "每一公斤可回收物都换成积分与减碳量，兑好礼、看报告，环保看得见。": "Every kilo becomes points and CO₂ savings — redeem rewards and track your impact.",
    "和小绿一起守护地球": "Protect the planet with Xiaolü",
    "实时追踪回收员位置、评价服务、解锁环保成就——从第一单开始。": "Track recyclers live, rate the service, unlock achievements — starting with your first order.",
    "跳过": "Skip",
    "开始使用": "Get started",
    "下一步": "Next",
  };

  /// 按当前语言翻译一段中文 UI 文案
  String tr(String zh) => isZh ? zh : (_zh2en[zh] ?? zh);

  String t(String key) => (isZh ? _zh[key] : _en[key]) ?? _zh[key] ?? key;

  // ---- 通用 ----
  String get appName => t('appName');
  String get slogan => t('slogan');
  String get home => t('home');
  String get orders => t('orders');
  String get shop => t('shop');
  String get news => t('news');
  String get mine => t('mine');
  String get oneTapRecycle => t('oneTapRecycle');
  String get login => t('login');
  String get register => t('register');
  String get logout => t('logout');
  String get phone => t('phone');
  String get password => t('password');
  String get nickname => t('nickname');
  String get confirm => t('confirm');
  String get cancel => t('cancel');
  String get save => t('save');
  String get retry => t('retry');
  String get loadFailed => t('loadFailed');
  String get language => t('language');
  String get settings => t('settings');
  String get notifications => t('notifications');
  String get markAllRead => t('markAllRead');
  String get noMessages => t('noMessages');

  // ---- 用户端 ----
  String get myCarbonPoints => t('myCarbonPoints');
  String get signIn => t('signIn');
  String get signed => t('signed');
  String get categoriesToday => t('categoriesToday');
  String get bookPickup => t('bookPickup');
  String get bookNow => t('bookNow');
  String get activeOrder => t('activeOrder');
  String get hotProducts => t('hotProducts');
  String get ecoNews => t('ecoNews');
  String get more => t('more');
  String get carbonReport => t('carbonReport');
  String get pointsHistory => t('pointsHistory');
  String get redemptions => t('redemptions');
  String get addresses => t('addresses');
  String get serverSettings => t('serverSettings');
  String get achievements => t('achievements');
  String get myServices => t('myServices');
  String get liveLocation => t('liveLocation');
  String get navigate => t('navigate');
  String get call => t('call');

  // ---- 回收员端 ----
  String get hall => t('hall');
  String get myTasks => t('myTasks');
  String get wallet => t('wallet');
  String get grab => t('grab');
  String get depart => t('depart');
  String get weighAndComplete => t('weighAndComplete');
  String get markPaid => t('markPaid');
  String get withdraw => t('withdraw');
  String get balance => t('balance');
  String get startTracking => t('startTracking');
  String get stopTracking => t('stopTracking');

  // ---- 管理后台 ----
  String get adminConsole => t('adminConsole');
  String get dashboard => t('dashboard');
  String get orderMgmt => t('orderMgmt');
  String get userMgmt => t('userMgmt');
  String get operations => t('operations');
  String get export => t('export');
  String get withdrawReview => t('withdrawReview');
  String get recyclerMap => t('recyclerMap');

  static const Map<String, String> _zh = {
    'appName': '绿循环',
    'slogan': '让每一份资源都循环起来',
    'home': '首页',
    'orders': '订单',
    'shop': '商城',
    'news': '资讯',
    'mine': '我的',
    'oneTapRecycle': '一键回收',
    'login': '登录',
    'register': '注册',
    'logout': '退出登录',
    'phone': '手机号',
    'password': '密码',
    'nickname': '昵称',
    'confirm': '确定',
    'cancel': '取消',
    'save': '保存',
    'retry': '重试',
    'loadFailed': '加载失败',
    'language': '语言 / Language',
    'settings': '设置',
    'notifications': '消息中心',
    'markAllRead': '全部已读',
    'noMessages': '暂无消息',
    'myCarbonPoints': '我的碳积分',
    'signIn': '签到 +10',
    'signed': '已签到',
    'categoriesToday': '回收品类 · 今日价格',
    'bookPickup': '预约上门回收',
    'bookNow': '立即预约',
    'activeOrder': '进行中的订单',
    'hotProducts': '积分商城热门',
    'ecoNews': '环保资讯',
    'more': '更多',
    'carbonReport': '减碳报告',
    'pointsHistory': '积分明细',
    'redemptions': '兑换记录',
    'addresses': '地址管理',
    'serverSettings': '后端服务设置',
    'achievements': '环保成就',
    'myServices': '我的服务',
    'liveLocation': '实时位置',
    'navigate': '导航',
    'call': '拨打电话',
    'hall': '接单大厅',
    'myTasks': '我的任务',
    'wallet': '钱包',
    'grab': '抢单',
    'depart': '出发上门',
    'weighAndComplete': '称重 · 完成回收',
    'markPaid': '确认已付款',
    'withdraw': '提现',
    'balance': '可提现余额',
    'startTracking': '开启位置共享',
    'stopTracking': '关闭位置共享',
    'adminConsole': '绿循环管理后台',
    'dashboard': '数据概览',
    'orderMgmt': '订单管理',
    'userMgmt': '用户管理',
    'operations': '运营管理',
    'export': '数据导出',
    'withdrawReview': '提现审核',
    'recyclerMap': '回收员地图',
  };

  static const Map<String, String> _en = {
    'appName': 'Green Cycle',
    'slogan': 'Keep every resource in the loop',
    'home': 'Home',
    'orders': 'Orders',
    'shop': 'Shop',
    'news': 'News',
    'mine': 'Me',
    'oneTapRecycle': 'Recycle',
    'login': 'Sign in',
    'register': 'Sign up',
    'logout': 'Sign out',
    'phone': 'Phone',
    'password': 'Password',
    'nickname': 'Nickname',
    'confirm': 'OK',
    'cancel': 'Cancel',
    'save': 'Save',
    'retry': 'Retry',
    'loadFailed': 'Failed to load',
    'language': 'Language / 语言',
    'settings': 'Settings',
    'notifications': 'Notifications',
    'markAllRead': 'Mark all read',
    'noMessages': 'No messages',
    'myCarbonPoints': 'My Carbon Points',
    'signIn': 'Check-in +10',
    'signed': 'Checked in',
    'categoriesToday': "Categories · Today's prices",
    'bookPickup': 'Book a Pickup',
    'bookNow': 'Book now',
    'activeOrder': 'Active order',
    'hotProducts': 'Popular rewards',
    'ecoNews': 'Eco news',
    'more': 'More',
    'carbonReport': 'Carbon report',
    'pointsHistory': 'Points history',
    'redemptions': 'Redemptions',
    'addresses': 'Addresses',
    'serverSettings': 'Server settings',
    'achievements': 'Achievements',
    'myServices': 'My services',
    'liveLocation': 'Live location',
    'navigate': 'Navigate',
    'call': 'Call',
    'hall': 'Job board',
    'myTasks': 'My tasks',
    'wallet': 'Wallet',
    'grab': 'Accept',
    'depart': 'Depart',
    'weighAndComplete': 'Weigh & complete',
    'markPaid': 'Mark as paid',
    'withdraw': 'Withdraw',
    'balance': 'Available balance',
    'startTracking': 'Share location',
    'stopTracking': 'Stop sharing',
    'adminConsole': 'Green Cycle Admin',
    'dashboard': 'Dashboard',
    'orderMgmt': 'Orders',
    'userMgmt': 'Users',
    'operations': 'Operations',
    'export': 'Export',
    'withdrawReview': 'Withdrawals',
    'recyclerMap': 'Recycler map',
  };
}

class _Delegate extends LocalizationsDelegate<AppLocalizations> {
  const _Delegate();

  @override
  bool isSupported(Locale locale) => ['zh', 'en'].contains(locale.languageCode);

  @override
  Future<AppLocalizations> load(Locale locale) async => AppLocalizations(locale);

  @override
  bool shouldReload(covariant LocalizationsDelegate<AppLocalizations> old) => false;
}

extension L10nX on BuildContext {
  AppLocalizations get l10n => AppLocalizations.of(this);
}

extension L10nTr on BuildContext {
  /// 运行时文案翻译：`context.tr('确认预约')`
  String tr(String zh) => AppLocalizations.of(this).tr(zh);
}
