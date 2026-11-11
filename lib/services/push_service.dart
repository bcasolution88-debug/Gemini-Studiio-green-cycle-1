import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'api_service.dart';

/// 推送抽象层（FCM 就绪）。
///
/// 当前实现 = **设备 token 注册 + 前台即时轮询**，不依赖 Firebase 插件：
/// - 未接入 FCM 时，使用稳定的本地设备标识作为 token 注册到后端，
///   后端 `/api/push/register` 会记录设备并在有凭据时推送；
/// - 接入 FCM 时只需在 [PushTokenSource] 里改为返回 `FirebaseMessaging.getToken()`，
///   其余调用链（注册 / 换账号迁移 / 注销）保持不变。
///
/// 这样做可以保证：工程今天就能编译发布，接入 Firebase 时仅改一处。
abstract class PushTokenSource {
  Future<String?> getToken();

  /// token 刷新流（FCM 提供 onTokenRefresh；本地实现无刷新）
  Stream<String> get onTokenRefresh => const Stream.empty();
}

/// 默认实现：生成一次并持久化的本地设备标识（`local:` 前缀便于后端区分）。
class LocalDeviceTokenSource implements PushTokenSource {
  static const _kKey = 'push_local_device_token';

  @override
  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    var t = prefs.getString(_kKey);
    if (t == null || t.isEmpty) {
      final seed = DateTime.now().microsecondsSinceEpoch.toRadixString(36);
      final rnd = (DateTime.now().hashCode ^ seed.hashCode).toUnsigned(32).toRadixString(36);
      t = 'local:$seed$rnd';
      await prefs.setString(_kKey, t);
    }
    return t;
  }

  @override
  Stream<String> get onTokenRefresh => const Stream.empty();
}

class PushService {
  PushService._();
  static final PushService instance = PushService._();

  /// 接入 FCM 时替换此处即可：`PushService.instance.tokenSource = FcmTokenSource();`
  PushTokenSource tokenSource = LocalDeviceTokenSource();

  final ApiService _api = ApiService.instance;
  StreamSubscription<String>? _refreshSub;
  String? _registeredFor;
  String? _lastToken;

  /// 最近一次后端返回的推送通道状态（供「关于/设置」页展示）
  Map<String, dynamic>? providerStatus;

  String get _platform {
    if (kIsWeb) return 'web';
    switch (defaultTargetPlatform) {
      case TargetPlatform.iOS:
        return 'ios';
      default:
        return 'android';
    }
  }

  /// 登录后调用：向后端注册当前设备（幂等）。失败不抛出，仅记录。
  Future<void> register(String userId, {String? appVersion, String? locale}) async {
    try {
      final token = await tokenSource.getToken();
      if (token == null || token.isEmpty) return;
      final r = await _api.registerPushToken(userId, token,
          platform: _platform, appVersion: appVersion, locale: locale);
      providerStatus = (r['provider'] as Map?)?.cast<String, dynamic>();
      _registeredFor = userId;
      _lastToken = token;
      _refreshSub ??= tokenSource.onTokenRefresh.listen((t) {
        final uid = _registeredFor;
        if (uid != null) register(uid, appVersion: appVersion, locale: locale);
      });
      if (kDebugMode) {
        debugPrint('[push] registered ${token.substring(0, token.length.clamp(0, 14))}… '
            'provider=${providerStatus?['provider']} enabled=${providerStatus?['enabled']}');
      }
    } catch (e) {
      if (kDebugMode) debugPrint('[push] register failed: $e');
    }
  }

  /// 退出登录前调用：注销当前设备 token，避免推送串号。
  Future<void> unregister() async {
    final uid = _registeredFor;
    final token = _lastToken;
    _registeredFor = null;
    if (uid == null || token == null) return;
    try {
      await _api.unregisterPushToken(uid, token);
    } catch (e) {
      if (kDebugMode) debugPrint('[push] unregister failed: $e');
    }
  }
}
