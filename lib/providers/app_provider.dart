import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/models.dart';
import '../services/api_service.dart';

/// 全局状态：登录用户、首页数据、品类缓存
class AppProvider extends ChangeNotifier {
  final ApiService _api = ApiService.instance;

  User? user;
  HomeData? home;
  List<RecycleCategory> categories = [];
  bool loadingHome = false;
  String? homeError;
  bool initialized = false;

  bool get isLoggedIn => user != null;
  String get userId => user?.id ?? '';

  static const _kUserId = 'user_id';

  Future<void> init() async {
    await _api.init();
    final prefs = await SharedPreferences.getInstance();
    final uid = prefs.getString(_kUserId);
    if (uid != null && uid.isNotEmpty) {
      try {
        user = await _api.getUser(uid);
      } catch (e) {
        if (kDebugMode) debugPrint('恢复登录失败: $e');
      }
    }
    initialized = true;
    notifyListeners();
    if (user != null) await loadHome();
  }

  Future<void> _persist(User u) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_kUserId, u.id);
  }

  Future<void> login(String phone, String password) async {
    user = await _api.login(phone, password);
    await _persist(user!);
    notifyListeners();
    await loadHome();
  }

  Future<void> register(String phone, String password, String nickname) async {
    user = await _api.register(phone, password, nickname);
    await _persist(user!);
    notifyListeners();
    await loadHome();
  }

  Future<void> logout() async {
    user = null;
    home = null;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_kUserId);
    notifyListeners();
  }

  Future<void> loadHome() async {
    if (user == null || user!.role != 'user') return;
    loadingHome = true;
    homeError = null;
    notifyListeners();
    try {
      home = await _api.getHome(user!.id);
      user = home!.user;
      if (home!.categories.isNotEmpty) categories = home!.categories;
    } catch (e) {
      homeError = e.toString();
    } finally {
      loadingHome = false;
      notifyListeners();
    }
  }

  Future<void> refreshUser() async {
    if (user == null) return;
    try {
      user = await _api.getUser(user!.id);
      notifyListeners();
    } catch (_) {}
  }

  Future<List<RecycleCategory>> ensureCategories() async {
    if (categories.isEmpty) {
      categories = await _api.getCategories();
      notifyListeners();
    }
    return categories;
  }

  Future<String> signIn() async {
    if (user == null) throw ApiException('请先登录');
    user = await _api.signIn(user!.id);
    notifyListeners();
    await loadHome();
    return '签到成功，+10 积分';
  }

  Future<void> updateProfile({String? nickname, String? avatar}) async {
    if (user == null) return;
    user = await _api.updateProfile(user!.id, nickname: nickname, avatar: avatar);
    notifyListeners();
  }
}
