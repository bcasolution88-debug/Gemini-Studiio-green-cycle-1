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
