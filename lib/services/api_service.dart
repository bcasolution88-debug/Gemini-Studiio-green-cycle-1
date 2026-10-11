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
