import 'dart:async';

import 'package:flutter/material.dart';
import 'package:latlong2/latlong.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';
import '../../widgets/order_map.dart';

/// 管理后台：回收员实时分布地图 + 待接单位置
class AdminMapScreen extends StatefulWidget {
  const AdminMapScreen({super.key});

  @override
  State<AdminMapScreen> createState() => _AdminMapScreenState();
}

class _AdminMapScreenState extends State<AdminMapScreen> {
  final _api = ApiService.instance;
  List<RecyclerLocation> _recyclers = [];
  List<RecycleOrder> _pending = [];
  bool _loading = true;
  String? _error;
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _load();
      _timer = Timer.periodic(const Duration(seconds: 8), (_) => _load(silent: true));
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _load({bool silent = false}) async {
    if (!silent) {
      setState(() {
        _loading = true;
        _error = null;
      });
    }
    try {
      final admin = context.read<AppProvider>().userId;
      final r = await Future.wait([_api.adminLocations(admin), _api.adminOrders(admin, status: 'pending')]);
      _recyclers = r[0] as List<RecyclerLocation>;
      _pending = r[1] as List<RecycleOrder>;
    } catch (e) {
      if (!silent) _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('回收员实时分布'),
        actions: [IconButton(onPressed: _load, icon: const Icon(Icons.refresh_rounded))],
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
          : _error != null
              ? ErrorState(message: _error!, onRetry: _load)
              : _body(),
    );
  }

  Widget _body() {
    final pendingWithLoc = _pending.where((o) => o.hasLocation).toList();
    final center = _recyclers.isNotEmpty
        ? LatLng(_recyclers.first.lat, _recyclers.first.lng)
        : (pendingWithLoc.isNotEmpty ? LatLng(pendingWithLoc.first.lat!, pendingWithLoc.first.lng!) : const LatLng(39.92, 116.46));
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(16),
          child: OrderMap(
            destination: center,
            height: 320,
            extraMarkers: [
              ..._recyclers.map((r) => MapMarkerData(
                    point: LatLng(r.lat, r.lng),
                    avatar: r.avatar,
                    label: r.nickname,
                    badge: r.activeTasks,
                    onTap: () => _showRecycler(r),
                  )),
            ],
          ),
        ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Row(children: [
            _legend(AppColors.lime, '回收员 (${_recyclers.length})'),
            const SizedBox(width: 16),
            _legend(AppColors.coral, '中心点'),
            const Spacer(),
            Text('每 8 秒自动刷新', style: TextStyle(fontSize: 11, color: AppColors.textGrey.withValues(alpha: 0.8))),
          ]),
        ),
        const SizedBox(height: 8),
        Expanded(
          child: ListView(
            padding: const EdgeInsets.fromLTRB(16, 4, 16, 20),
            children: [
              const Text('在线回收员', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
              const SizedBox(height: 8),
              ..._recyclers.map((r) => SoftCard(
                    margin: const EdgeInsets.only(bottom: 8),
                    padding: const EdgeInsets.all(12),
                    onTap: () => _showRecycler(r),
                    child: Row(children: [
                      Avatar(r.avatar, size: 40, border: AppColors.lime),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                          Text(r.nickname, style: const TextStyle(fontWeight: FontWeight.w800)),
                          Text('${r.lat.toStringAsFixed(4)}, ${r.lng.toStringAsFixed(4)} · 更新 ${r.updatedAt.length >= 16 ? r.updatedAt.substring(11, 16) : ''}',
                              style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                        ]),
                      ),
                      StatusChip(text: r.activeTasks > 0 ? '任务中 ${r.activeTasks}' : '空闲', color: r.activeTasks > 0 ? AppColors.blue : AppColors.forest),
                    ]),
                  )),
              const SizedBox(height: 12),
              Text('待接订单 (${_pending.length})', style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
              const SizedBox(height: 8),
              ..._pending.map((o) => SoftCard(
                    margin: const EdgeInsets.only(bottom: 8),
                    padding: const EdgeInsets.all(12),
                    child: Row(children: [
                      const IconBadge(icon: Icons.schedule_rounded, color: AppColors.orange, size: 36, iconSize: 18),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                          Text('${o.contactName} · ${o.categoryNames}', style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13), maxLines: 1, overflow: TextOverflow.ellipsis),
                          Text('${o.appointmentTime} · ${o.addressText}', style: const TextStyle(fontSize: 11, color: AppColors.textGrey), maxLines: 1, overflow: TextOverflow.ellipsis),
                        ]),
                      ),
                      if (o.hasLocation && _recyclers.isNotEmpty)
                        Text(_nearest(o), style: const TextStyle(fontSize: 11, color: AppColors.forest, fontWeight: FontWeight.w700)),
                    ]),
                  )),
            ],
          ),
        ),
      ],
    );
  }

  String _nearest(RecycleOrder o) {
    RecyclerLocation? best;
    double bestD = double.infinity;
    for (final r in _recyclers) {
      final d = _dist(LatLng(r.lat, r.lng), LatLng(o.lat!, o.lng!));
      if (d < bestD) {
        bestD = d;
        best = r;
      }
    }
    if (best == null) return '';
    return '最近 ${best.nickname} ${bestD.toStringAsFixed(1)}km';
  }

  double _dist(LatLng a, LatLng b) => const Distance().as(LengthUnit.Kilometer, a, b);

  Widget _legend(Color c, String t) => Row(children: [
        Container(width: 10, height: 10, decoration: BoxDecoration(color: c, shape: BoxShape.circle)),
        const SizedBox(width: 4),
        Text(t, style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
      ]);

  void _showRecycler(RecyclerLocation r) {
    showModalBottomSheet(
      context: context,
      builder: (c) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Row(children: [
            Avatar(r.avatar, size: 56, border: AppColors.lime),
            const SizedBox(width: 14),
            Expanded(
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, mainAxisSize: MainAxisSize.min, children: [
                Text(r.nickname, style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 18)),
                Text(r.phone, style: const TextStyle(color: AppColors.textGrey)),
                const SizedBox(height: 4),
                Text('进行中任务 ${r.activeTasks} · 速度 ${(r.speed ?? 0).toStringAsFixed(1)} m/s', style: const TextStyle(fontSize: 12)),
                Text('最后更新 ${r.updatedAt}', style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
              ]),
            ),
          ]),
        ),
      ),
    );
  }
}
