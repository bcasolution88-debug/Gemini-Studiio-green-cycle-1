import 'dart:async';
import 'dart:math' as math;

import 'package:flutter/foundation.dart';
import 'package:geolocator/geolocator.dart';
import 'package:latlong2/latlong.dart';
import 'package:url_launcher/url_launcher.dart';

import 'api_service.dart';

/// 定位与导航服务
///
/// - 回收员端：持续获取 GPS 并上报到后端（Web / 无权限时退化为沿路线模拟移动，便于演示）
/// - 通用：打开系统地图 App 导航、拨打电话
class LocationService {
  LocationService._();
  static final LocationService instance = LocationService._();

  StreamSubscription<Position>? _sub;
  Timer? _simTimer;
  LatLng? last;
  bool get isTracking => _sub != null || _simTimer != null;

  /// 开始上报位置。[fallbackStart] 为无法获得真实定位时的模拟起点，[target] 为模拟终点。
  Future<String> startTracking(
    String recyclerId, {
    LatLng? fallbackStart,
    LatLng? target,
  }) async {
    stopTracking();
    final api = ApiService.instance;

    Future<void> report(LatLng p, {double? heading, double? speed}) async {
      last = p;
      try {
        await api.updateLocation(recyclerId, p.latitude, p.longitude, heading: heading, speed: speed);
      } catch (e) {
        if (kDebugMode) debugPrint('上报位置失败: $e');
      }
    }

    // 尝试真实 GPS
    try {
      if (!kIsWeb) {
        var perm = await Geolocator.checkPermission();
        if (perm == LocationPermission.denied) perm = await Geolocator.requestPermission();
        final enabled = await Geolocator.isLocationServiceEnabled();
        if (enabled &&
            (perm == LocationPermission.always || perm == LocationPermission.whileInUse)) {
          final first = await Geolocator.getCurrentPosition().timeout(const Duration(seconds: 8));
          await report(LatLng(first.latitude, first.longitude),
              heading: first.heading, speed: first.speed);
          _sub = Geolocator.getPositionStream(
            locationSettings: const LocationSettings(accuracy: LocationAccuracy.high, distanceFilter: 10),
          ).listen((p) => report(LatLng(p.latitude, p.longitude), heading: p.heading, speed: p.speed));
          return 'gps';
        }
      }
    } catch (e) {
      if (kDebugMode) debugPrint('GPS 不可用，使用模拟定位: $e');
    }

    // 退化：模拟沿直线向目标移动（演示用）
    final start = fallbackStart ?? const LatLng(39.9150, 116.4400);
    final end = target ?? LatLng(start.latitude + 0.012, start.longitude + 0.015);
    var cur = start;
    await report(cur);
    _simTimer = Timer.periodic(const Duration(seconds: 5), (t) async {
      final dLat = end.latitude - cur.latitude;
      final dLng = end.longitude - cur.longitude;
      final dist = math.sqrt(dLat * dLat + dLng * dLng);
      if (dist < 0.0004) {
        cur = end;
        await report(cur, speed: 0);
        t.cancel();
        _simTimer = null;
        return;
      }
      // 每次前进约 8%，带一点随机抖动
      final step = 0.08 + math.Random().nextDouble() * 0.04;
      cur = LatLng(
        cur.latitude + dLat * step + (math.Random().nextDouble() - 0.5) * 0.0002,
        cur.longitude + dLng * step + (math.Random().nextDouble() - 0.5) * 0.0002,
      );
      final heading = (math.atan2(dLng, dLat) * 180 / math.pi + 360) % 360;
      await report(cur, heading: heading, speed: 6.5);
    });
    return 'simulated';
  }

  void stopTracking() {
    _sub?.cancel();
    _sub = null;
    _simTimer?.cancel();
    _simTimer = null;
  }

  /// 打开外部地图导航（Android 优先 geo: Intent，其他平台用高德/谷歌网页）
  static Future<bool> navigateTo(double lat, double lng, {String label = '目的地'}) async {
    final candidates = <Uri>[
      if (!kIsWeb && defaultTargetPlatform == TargetPlatform.android)
        Uri.parse('geo:$lat,$lng?q=$lat,$lng(${Uri.encodeComponent(label)})'),
      Uri.parse('https://uri.amap.com/navigation?to=$lng,$lat,${Uri.encodeComponent(label)}&mode=car&coordinate=wgs84'),
      Uri.parse('https://www.google.com/maps/dir/?api=1&destination=$lat,$lng'),
    ];
    for (final u in candidates) {
      try {
        if (await canLaunchUrl(u)) {
          if (await launchUrl(u, mode: LaunchMode.externalApplication)) return true;
        }
      } catch (_) {}
    }
    // 最后兜底：直接尝试打开网页
    try {
      return await launchUrl(candidates.last, mode: LaunchMode.externalApplication);
    } catch (_) {
      return false;
    }
  }

  static Future<bool> dial(String phone) async {
    final u = Uri.parse('tel:$phone');
    try {
      return await launchUrl(u);
    } catch (_) {
      return false;
    }
  }

  /// 两点距离（公里）
  static double distanceKm(LatLng a, LatLng b) {
    const r = 6371.0;
    final dLat = _rad(b.latitude - a.latitude);
    final dLng = _rad(b.longitude - a.longitude);
    final h = math.sin(dLat / 2) * math.sin(dLat / 2) +
        math.cos(_rad(a.latitude)) * math.cos(_rad(b.latitude)) * math.sin(dLng / 2) * math.sin(dLng / 2);
    return 2 * r * math.asin(math.sqrt(h));
  }

  static double _rad(double d) => d * math.pi / 180;
}
