import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import '../services/location_service.dart';
import '../theme/app_theme.dart';
import 'common_widgets.dart';

/// 订单地图：显示目的地（用户地址）与回收员实时位置，并连线
class OrderMap extends StatelessWidget {
  final LatLng destination;
  final LatLng? recycler;
  final String? recyclerAvatar;
  final double height;
  final bool interactive;
  final List<MapMarkerData> extraMarkers;

  const OrderMap({
    super.key,
    required this.destination,
    this.recycler,
    this.recyclerAvatar,
    this.height = 220,
    this.interactive = true,
    this.extraMarkers = const [],
  });

  @override
  Widget build(BuildContext context) {
    final points = <LatLng>[destination, if (recycler != null) recycler!, ...extraMarkers.map((e) => e.point)];
    final center = recycler == null
        ? destination
        : LatLng((destination.latitude + recycler!.latitude) / 2, (destination.longitude + recycler!.longitude) / 2);
    final distance = recycler == null ? null : LocationService.distanceKm(recycler!, destination);

    return ClipRRect(
      borderRadius: BorderRadius.circular(AppRadius.lg),
      child: SizedBox(
        height: height,
        child: Stack(
          children: [
            FlutterMap(
              options: MapOptions(
                initialCenter: center,
                initialZoom: recycler == null ? 15 : 14,
                initialCameraFit: points.length > 1
                    ? CameraFit.bounds(bounds: LatLngBounds.fromPoints(points), padding: const EdgeInsets.all(48))
                    : null,
                interactionOptions: InteractionOptions(
                  flags: interactive ? InteractiveFlag.all & ~InteractiveFlag.rotate : InteractiveFlag.none,
                ),
              ),
              children: [
                TileLayer(
                  urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                  userAgentPackageName: 'com.greencycle.recycle',
                ),
                if (recycler != null)
                  PolylineLayer(polylines: [
                    Polyline(
                      points: [recycler!, destination],
                      color: AppColors.forest.withValues(alpha: 0.75),
                      strokeWidth: 4,
                      pattern: const StrokePattern.dotted(),
                    ),
                  ]),
                MarkerLayer(markers: [
                  Marker(
                    point: destination,
                    width: 44,
                    height: 52,
                    alignment: Alignment.topCenter,
                    child: const _Pin(color: AppColors.coral, icon: Icons.home_rounded),
                  ),
                  if (recycler != null)
                    Marker(
                      point: recycler!,
                      width: 48,
                      height: 48,
                      child: _RecyclerDot(avatar: recyclerAvatar ?? ''),
                    ),
                  ...extraMarkers.map((m) => Marker(
                        point: m.point,
                        width: 48,
                        height: 56,
                        alignment: Alignment.topCenter,
                        child: GestureDetector(
                          onTap: m.onTap,
                          child: _RecyclerDot(avatar: m.avatar, label: m.label, badge: m.badge),
                        ),
                      )),
                ]),
              ],
            ),
            if (distance != null)
              Positioned(
                left: 12,
                top: 12,
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(20),
                    boxShadow: AppShadows.card,
                  ),
                  child: Row(mainAxisSize: MainAxisSize.min, children: [
                    const Icon(Icons.near_me_rounded, size: 14, color: AppColors.forest),
                    const SizedBox(width: 4),
                    Text(
                      distance < 1
                          ? '距离 ${(distance * 1000).toStringAsFixed(0)} m · 约 ${(distance * 1000 / 100).ceil()} 分钟'
                          : '距离 ${distance.toStringAsFixed(1)} km · 约 ${(distance / 0.4).ceil()} 分钟',
                      style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700),
                    ),
                  ]),
                ),
              ),
            Positioned(
              right: 6,
              bottom: 4,
              child: Text('© OpenStreetMap',
                  style: TextStyle(fontSize: 9, color: Colors.black.withValues(alpha: 0.5))),
            ),
          ],
        ),
      ),
    );
  }
}

class MapMarkerData {
  final LatLng point;
  final String avatar;
  final String label;
  final int badge;
  final VoidCallback? onTap;
  MapMarkerData({required this.point, this.avatar = '', this.label = '', this.badge = 0, this.onTap});
}

class _Pin extends StatelessWidget {
  final Color color;
  final IconData icon;
  const _Pin({required this.color, required this.icon});

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 36,
          height: 36,
          decoration: BoxDecoration(
            color: color,
            shape: BoxShape.circle,
            border: Border.all(color: Colors.white, width: 3),
            boxShadow: [BoxShadow(color: color.withValues(alpha: 0.4), blurRadius: 8, offset: const Offset(0, 3))],
          ),
          child: Icon(icon, color: Colors.white, size: 18),
        ),
        Container(width: 3, height: 10, color: color),
      ],
    );
  }
}

class _RecyclerDot extends StatelessWidget {
  final String avatar;
  final String label;
  final int badge;
  const _RecyclerDot({required this.avatar, this.label = '', this.badge = 0});

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Stack(
          clipBehavior: Clip.none,
          children: [
            Container(
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                boxShadow: [BoxShadow(color: AppColors.forest.withValues(alpha: 0.45), blurRadius: 10, spreadRadius: 2)],
              ),
              child: Avatar(avatar, size: 40, border: AppColors.lime),
            ),
            if (badge > 0)
              Positioned(
                right: -4,
                top: -4,
                child: Container(
                  padding: const EdgeInsets.all(4),
                  decoration: const BoxDecoration(color: AppColors.coral, shape: BoxShape.circle),
                  child: Text('$badge', style: const TextStyle(color: Colors.white, fontSize: 9, fontWeight: FontWeight.w900)),
                ),
              ),
          ],
        ),
        if (label.isNotEmpty)
          Container(
            margin: const EdgeInsets.only(top: 2),
            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 1),
            decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(8), boxShadow: AppShadows.card),
            child: Text(label, style: const TextStyle(fontSize: 9, fontWeight: FontWeight.w800)),
          ),
      ],
    );
  }
}
