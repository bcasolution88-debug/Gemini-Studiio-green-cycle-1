import 'dart:async';

import 'package:flutter/foundation.dart';

import '../models/models.dart';
import '../services/api_service.dart';

/// 消息通知：定时轮询未读数，发现新消息时通过 [onNewMessage] 回调弹出提醒
class NotificationProvider extends ChangeNotifier {
  final ApiService _api = ApiService.instance;

  String? _userId;
  Timer? _timer;
  String _since = '';
  int unread = 0;
  List<AppNotification> items = [];
  bool loading = false;

  /// 新消息回调（由 UI 层注册，用于弹出横幅）
  void Function(AppNotification n)? onNewMessage;

  static const Duration pollInterval = Duration(seconds: 15);

  void start(String userId) {
    if (_userId == userId && _timer != null) return;
    stop();
    _userId = userId;
    _since = '';
    unread = 0;
    items = [];
    _poll();
    _timer = Timer.periodic(pollInterval, (_) => _poll());
  }

  void stop() {
    _timer?.cancel();
    _timer = null;
    _userId = null;
    unread = 0;
    items = [];
    notifyListeners();
  }

  Future<void> _poll() async {
    final uid = _userId;
    if (uid == null) return;
    try {
      final d = await _api.unreadCount(uid, since: _since);
      final newUnread = (d['unread'] as num?)?.toInt() ?? 0;
      final newItems = ((d['new_items'] as List?) ?? [])
          .map((e) => AppNotification.fromJson(Map<String, dynamic>.from(e)))
          .toList();
      final serverTime = d['server_time']?.toString() ?? '';
      if (_since.isNotEmpty && newItems.isNotEmpty && onNewMessage != null) {
        onNewMessage!(newItems.first);
      }
      _since = serverTime;
      if (newUnread != unread) {
        unread = newUnread;
        notifyListeners();
      }
    } catch (e) {
      if (kDebugMode) debugPrint('通知轮询失败: $e');
    }
  }

  /// 立即刷新（例如用户操作后）
  Future<void> refreshNow() => _poll();

  Future<void> load() async {
    final uid = _userId;
    if (uid == null) return;
    loading = true;
    notifyListeners();
    try {
      final r = await _api.notifications(uid);
      unread = r.$1;
      items = r.$2;
    } catch (e) {
      if (kDebugMode) debugPrint('加载通知失败: $e');
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  Future<void> markRead(AppNotification n) async {
    final uid = _userId;
    if (uid == null || n.isRead) return;
    items = items.map((e) => e.id == n.id ? e.copyWith(isRead: true) : e).toList();
    unread = (unread - 1).clamp(0, 9999);
    notifyListeners();
    try {
      await _api.markRead(uid, ids: [n.id]);
    } catch (_) {}
  }

  Future<void> markAllRead() async {
    final uid = _userId;
    if (uid == null) return;
    items = items.map((e) => e.copyWith(isRead: true)).toList();
    unread = 0;
    notifyListeners();
    try {
      await _api.markRead(uid);
    } catch (_) {}
  }

  Future<void> delete(AppNotification n) async {
    final uid = _userId;
    if (uid == null) return;
    items = items.where((e) => e.id != n.id).toList();
    if (!n.isRead) unread = (unread - 1).clamp(0, 9999);
    notifyListeners();
    try {
      await _api.deleteNotification(uid, n.id);
    } catch (_) {}
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
