import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

import '../models/models.dart';

class ApiException implements Exception {
  final String message;
  final int? statusCode;
  ApiException(this.message, [this.statusCode]);
  @override
  String toString() => message;
}

/// 后端 API 服务层
///
/// 后端地址优先级：用户在「设置」中保存的地址 > 编译参数 API_BASE_URL > 默认地址
class ApiService {
  ApiService._();
  static final ApiService instance = ApiService._();

  static const String _defaultBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'https://8000-ihxjxuux9vizfjmghfrwk-2e77fc33.sandbox.novita.ai',
  );
  static const String _prefKey = 'api_base_url';

  String _baseUrl = _defaultBaseUrl;
  String get baseUrl => _baseUrl;

  Future<void> init() async {
    final prefs = await SharedPreferences.getInstance();
    final saved = prefs.getString(_prefKey);
    if (saved != null && saved.isNotEmpty) _baseUrl = saved;
  }

  Future<void> setBaseUrl(String url) async {
    _baseUrl = url.trim().replaceAll(RegExp(r'/+$'), '');
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_prefKey, _baseUrl);
  }

  Future<void> resetBaseUrl() async {
    _baseUrl = _defaultBaseUrl;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_prefKey);
  }

  Uri _uri(String path, [Map<String, String>? query]) =>
      Uri.parse('$_baseUrl$path').replace(queryParameters: query);

  Future<dynamic> _handle(Future<http.Response> future) async {
    http.Response res;
    try {
      res = await future.timeout(const Duration(seconds: 15));
    } catch (e) {
      if (kDebugMode) debugPrint('API 请求失败: $e');
      throw ApiException('网络连接失败，请检查后端服务是否可用');
    }
    Map<String, dynamic> body;
    try {
      body = jsonDecode(utf8.decode(res.bodyBytes)) as Map<String, dynamic>;
    } catch (_) {
      throw ApiException('服务器返回了无效数据 (${res.statusCode})', res.statusCode);
    }
    if (res.statusCode >= 400) {
      final detail = body['detail'] ?? body['message'] ?? '请求失败';
      throw ApiException(detail.toString(), res.statusCode);
    }
    if (body['code'] != null && body['code'] != 0) {
      throw ApiException(body['message']?.toString() ?? '请求失败');
    }
    return body['data'];
  }

  Future<dynamic> _get(String path, [Map<String, String>? q]) =>
      _handle(http.get(_uri(path, q)));

  Future<dynamic> _post(String path, [Object? data]) => _handle(http.post(
        _uri(path),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(data ?? {}),
      ));

  Future<dynamic> _put(String path, Object data) => _handle(http.put(
        _uri(path),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode(data),
      ));

  Future<dynamic> _delete(String path) => _handle(http.delete(_uri(path)));

  List<T> _list<T>(dynamic data, T Function(Map<String, dynamic>) f) =>
      ((data as List?) ?? []).map((e) => f(Map<String, dynamic>.from(e))).toList();

  // ---------- 健康检查 ----------
  Future<bool> ping() async {
    try {
      await _get('/api/health');
      return true;
    } catch (_) {
      return false;
    }
  }

  // ---------- 认证 ----------
  Future<User> login(String phone, String password) async {
    final d = await _post('/api/auth/login', {'phone': phone, 'password': password});
    return User.fromJson(Map<String, dynamic>.from(d['user']));
  }

  Future<User> register(String phone, String password, String nickname) async {
    final d = await _post('/api/auth/register',
        {'phone': phone, 'password': password, 'nickname': nickname});
    return User.fromJson(Map<String, dynamic>.from(d['user']));
  }

  Future<User> getUser(String id) async =>
      User.fromJson(Map<String, dynamic>.from(await _get('/api/users/$id')));

  Future<User> updateProfile(String id, {String? nickname, String? avatar}) async =>
      User.fromJson(Map<String, dynamic>.from(await _put('/api/users/$id', {
        if (nickname != null) 'nickname': nickname,
        if (avatar != null) 'avatar': avatar,
      })));

  Future<UserStats> getStats(String id) async =>
      UserStats.fromJson(Map<String, dynamic>.from(await _get('/api/users/$id/stats')));

  Future<User> signIn(String id) async =>
      User.fromJson(Map<String, dynamic>.from(await _post('/api/users/sign-in', {'user_id': id})));

  // ---------- 首页 ----------
  Future<HomeData> getHome(String userId) async =>
      HomeData.fromJson(Map<String, dynamic>.from(await _get('/api/home', {'user_id': userId})));

  // ---------- 品类 ----------
  Future<List<RecycleCategory>> getCategories() async =>
      _list(await _get('/api/categories'), RecycleCategory.fromJson);

  // ---------- 地址 ----------
  Future<List<Address>> getAddresses(String userId) async =>
      _list(await _get('/api/addresses', {'user_id': userId}), Address.fromJson);

  Future<Address> createAddress({
    required String userId,
    required String contactName,
    required String phone,
    required String region,
    required String detail,
    bool isDefault = false,
  }) async =>
      Address.fromJson(Map<String, dynamic>.from(await _post('/api/addresses', {
        'user_id': userId,
        'contact_name': contactName,
        'phone': phone,
        'region': region,
        'detail': detail,
        'is_default': isDefault,
      })));

  Future<void> deleteAddress(String id) => _delete('/api/addresses/$id');

  // ---------- 订单 ----------
  Future<List<RecycleOrder>> getOrders(String userId, {String status = 'all'}) async =>
      _list(await _get('/api/orders', {'user_id': userId, 'status': status}), RecycleOrder.fromJson);

  Future<RecycleOrder> getOrder(String id) async =>
      RecycleOrder.fromJson(Map<String, dynamic>.from(await _get('/api/orders/$id')));

  Future<RecycleOrder> createOrder({
    required String userId,
    required String addressId,
    required String appointmentTime,
    required List<Map<String, dynamic>> items,
    String remark = '',
  }) async =>
      RecycleOrder.fromJson(Map<String, dynamic>.from(await _post('/api/orders', {
        'user_id': userId,
        'address_id': addressId,
        'appointment_time': appointmentTime,
        'items': items,
        'remark': remark,
      })));

  Future<RecycleOrder> cancelOrder(String id) async =>
      RecycleOrder.fromJson(Map<String, dynamic>.from(await _post('/api/orders/$id/cancel')));

  Future<RecycleOrder> advanceOrder(String id) async =>
      RecycleOrder.fromJson(Map<String, dynamic>.from(await _post('/api/orders/$id/advance')));

  // ---------- 积分 ----------
  Future<List<PointRecord>> getPointRecords(String userId) async =>
      _list(await _get('/api/points/records', {'user_id': userId}), PointRecord.fromJson);

  // ---------- 商城 ----------
  Future<List<Product>> getProducts({String? category}) async => _list(
      await _get('/api/products', category == null ? null : {'category': category}),
      Product.fromJson);

  Future<Redemption> redeem(String userId, String productId, {int quantity = 1}) async =>
      Redemption.fromJson(Map<String, dynamic>.from(await _post('/api/redemptions', {
        'user_id': userId,
        'product_id': productId,
        'quantity': quantity,
      })));

  Future<List<Redemption>> getRedemptions(String userId) async =>
      _list(await _get('/api/redemptions', {'user_id': userId}), Redemption.fromJson);

  // ---------- 资讯 ----------
  Future<List<Article>> getArticles({String? category}) async => _list(
      await _get('/api/articles', category == null ? null : {'category': category}),
      Article.fromJson);

  Future<Article> getArticle(String id) async =>
      Article.fromJson(Map<String, dynamic>.from(await _get('/api/articles/$id')));

  Future<Article> likeArticle(String id) async =>
      Article.fromJson(Map<String, dynamic>.from(await _post('/api/articles/$id/like')));
}

/// 回收员端 & 管理后台 API
extension RoleApi on ApiService {
  // ---------- 回收员 ----------
  Future<List<RecycleOrder>> recyclerHall(String recyclerId) async =>
      _list(await _get('/api/recycler/hall', {'recycler_id': recyclerId}), RecycleOrder.fromJson);

  Future<List<RecycleOrder>> recyclerTasks(String recyclerId, {String status = 'active'}) async =>
      _list(await _get('/api/recycler/tasks', {'recycler_id': recyclerId, 'status': status}),
          RecycleOrder.fromJson);

  Future<RecycleOrder> recyclerAccept(String orderId, String recyclerId) async =>
      RecycleOrder.fromJson(Map<String, dynamic>.from(
          await _post('/api/recycler/orders/$orderId/accept', {'recycler_id': recyclerId})));

  Future<RecycleOrder> recyclerDepart(String orderId, String recyclerId) async =>
      RecycleOrder.fromJson(Map<String, dynamic>.from(
          await _post('/api/recycler/orders/$orderId/depart', {'recycler_id': recyclerId})));

  Future<RecycleOrder> recyclerComplete(
          String orderId, String recyclerId, List<Map<String, dynamic>> items) async =>
      RecycleOrder.fromJson(Map<String, dynamic>.from(await _post(
          '/api/recycler/orders/$orderId/complete', {'recycler_id': recyclerId, 'items': items})));

  Future<RecyclerStats> recyclerStats(String recyclerId) async => RecyclerStats.fromJson(
      Map<String, dynamic>.from(await _get('/api/recycler/stats', {'recycler_id': recyclerId})));

  // ---------- 管理后台 ----------
  Future<AdminDashboard> adminDashboard(String adminId) async => AdminDashboard.fromJson(
      Map<String, dynamic>.from(await _get('/api/admin/dashboard', {'admin_id': adminId})));

  Future<List<RecycleOrder>> adminOrders(String adminId,
          {String status = 'all', String keyword = ''}) async =>
      _list(await _get('/api/admin/orders', {'admin_id': adminId, 'status': status, 'keyword': keyword}),
          RecycleOrder.fromJson);

  Future<RecycleOrder> adminAssign(String adminId, String orderId, String recyclerId) async =>
      RecycleOrder.fromJson(Map<String, dynamic>.from(await _post(
          '/api/admin/orders/$orderId/assign?admin_id=$adminId', {'recycler_id': recyclerId})));

  Future<RecycleOrder> adminCancel(String adminId, String orderId) async =>
      RecycleOrder.fromJson(Map<String, dynamic>.from(
          await _post('/api/admin/orders/$orderId/cancel?admin_id=$adminId')));

  Future<List<User>> adminUsers(String adminId, {String role = 'all', String keyword = ''}) async =>
      _list(await _get('/api/admin/users', {'admin_id': adminId, 'role': role, 'keyword': keyword}),
          User.fromJson);

  Future<User> adminUpdateUser(String adminId, String userId, {String? status, String? role}) async =>
      User.fromJson(Map<String, dynamic>.from(await _put('/api/admin/users/$userId?admin_id=$adminId', {
        if (status != null) 'status': status,
        if (role != null) 'role': role,
      })));

  Future<List<User>> adminRecyclers(String adminId) async =>
      _list(await _get('/api/admin/recyclers', {'admin_id': adminId}), User.fromJson);

  Future<RecycleCategory> adminUpdateCategory(String adminId, String id,
          {double? price, int? points, double? carbon}) async =>
      RecycleCategory.fromJson(
          Map<String, dynamic>.from(await _put('/api/admin/categories/$id?admin_id=$adminId', {
        if (price != null) 'price_per_kg': price,
        if (points != null) 'points_per_kg': points,
        if (carbon != null) 'carbon_per_kg': carbon,
      })));

  Future<Product> adminCreateProduct(String adminId, Map<String, dynamic> data) async =>
      Product.fromJson(
          Map<String, dynamic>.from(await _post('/api/admin/products?admin_id=$adminId', data)));

  Future<Product> adminUpdateProduct(String adminId, String id, Map<String, dynamic> data) async =>
      Product.fromJson(
          Map<String, dynamic>.from(await _put('/api/admin/products/$id?admin_id=$adminId', data)));

  Future<void> adminDeleteProduct(String adminId, String id) =>
      _delete('/api/admin/products/$id?admin_id=$adminId');

  Future<List<Redemption>> adminRedemptions(String adminId, {String status = 'all'}) async =>
      _list(await _get('/api/admin/redemptions', {'admin_id': adminId, 'status': status}),
          Redemption.fromJson);

  Future<Redemption> adminUpdateRedemption(String adminId, String id, String status) async =>
      Redemption.fromJson(Map<String, dynamic>.from(
          await _put('/api/admin/redemptions/$id?admin_id=$adminId', {'status': status})));

  Future<Article> adminCreateArticle(String adminId, Map<String, dynamic> data) async =>
      Article.fromJson(
          Map<String, dynamic>.from(await _post('/api/admin/articles?admin_id=$adminId', data)));

  Future<void> adminDeleteArticle(String adminId, String id) =>
      _delete('/api/admin/articles/$id?admin_id=$adminId');

  // ---------- 通知 ----------
  Future<(int, List<AppNotification>)> notifications(String userId) async {
    final d = Map<String, dynamic>.from(await _get('/api/notifications', {'user_id': userId}));
    return ((d['unread'] as num?)?.toInt() ?? 0, _list(d['items'], AppNotification.fromJson));
  }

  Future<Map<String, dynamic>> unreadCount(String userId, {String since = ''}) async =>
      Map<String, dynamic>.from(
          await _get('/api/notifications/unread-count', {'user_id': userId, 'since': since}));

  Future<void> markRead(String userId, {List<String>? ids}) =>
      _post('/api/notifications/read', {'user_id': userId, if (ids != null) 'ids': ids});

  Future<void> deleteNotification(String userId, String id) =>
      _delete('/api/notifications/$id?user_id=$userId');

  // ---------- 定位 ----------
  Future<void> updateLocation(String recyclerId, double lat, double lng,
          {double? heading, double? speed}) =>
      _post('/api/recycler/location', {
        'recycler_id': recyclerId,
        'lat': lat,
        'lng': lng,
        if (heading != null) 'heading': heading,
        if (speed != null) 'speed': speed,
      });

  Future<RecyclerLocation> recyclerLocation(String recyclerId) async =>
      RecyclerLocation.fromJson(
          Map<String, dynamic>.from(await _get('/api/recycler/location/$recyclerId')));

  Future<List<RecyclerLocation>> adminLocations(String adminId) async =>
      _list(await _get('/api/admin/locations', {'admin_id': adminId}), RecyclerLocation.fromJson);

  // ---------- 钱包 ----------
  Future<WalletData> recyclerWallet(String recyclerId) async => WalletData.fromJson(
      Map<String, dynamic>.from(await _get('/api/recycler/wallet', {'recycler_id': recyclerId})));

  Future<RecycleOrder> recyclerMarkPaid(String orderId, String recyclerId, String method) async =>
      RecycleOrder.fromJson(Map<String, dynamic>.from(await _post(
          '/api/recycler/orders/$orderId/pay', {'recycler_id': recyclerId, 'pay_method': method})));

  Future<Withdrawal> recyclerWithdraw(String recyclerId, double amount, String account) async =>
      Withdrawal.fromJson(Map<String, dynamic>.from(await _post('/api/recycler/withdraw',
          {'recycler_id': recyclerId, 'amount': amount, 'account': account})));

  Future<List<Withdrawal>> adminWithdrawals(String adminId, {String status = 'all'}) async =>
      _list(await _get('/api/admin/withdrawals', {'admin_id': adminId, 'status': status}),
          Withdrawal.fromJson);

  Future<Withdrawal> adminReviewWithdrawal(String adminId, String id, bool approve,
          {String remark = ''}) async =>
      Withdrawal.fromJson(Map<String, dynamic>.from(await _post(
          '/api/admin/withdrawals/$id/review?admin_id=$adminId',
          {'approve': approve, 'remark': remark})));

  // ---------- 导出 ----------
  Uri exportUri(String adminId, String dataset,
          {String status = 'all', String dateFrom = '', String dateTo = ''}) =>
      _uri('/api/admin/export/$dataset', {
        'admin_id': adminId,
        'status': status,
        if (dateFrom.isNotEmpty) 'date_from': dateFrom,
        if (dateTo.isNotEmpty) 'date_to': dateTo,
      });

  Future<String> exportCsv(String adminId, String dataset, {String status = 'all'}) async {
    final res = await http.get(exportUri(adminId, dataset, status: status));
    if (res.statusCode >= 400) throw ApiException('导出失败 (${res.statusCode})');
    return utf8.decode(res.bodyBytes);
  }
}
