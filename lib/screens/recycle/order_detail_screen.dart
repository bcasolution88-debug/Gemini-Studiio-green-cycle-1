import 'dart:async';

import 'package:flutter/material.dart';
import 'package:latlong2/latlong.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../services/location_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';
import '../../widgets/order_map.dart';

/// 订单详情 + 进度跟踪
class OrderDetailScreen extends StatefulWidget {
  final String orderId;
  final bool justCreated;
  const OrderDetailScreen({super.key, required this.orderId, this.justCreated = false});

  @override
  State<OrderDetailScreen> createState() => _OrderDetailScreenState();
}

class _OrderDetailScreenState extends State<OrderDetailScreen> {
  final _api = ApiService.instance;
  RecycleOrder? _order;
  bool _loading = true;
  bool _acting = false;
  String? _error;
  Timer? _liveTimer;

  static const _steps = [
    ('pending', '已下单', Icons.edit_calendar_rounded),
    ('accepted', '已接单', Icons.how_to_reg_rounded),
    ('in_progress', '上门中', Icons.electric_moped_rounded),
    ('completed', '已完成', Icons.verified_rounded),
  ];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _load();
      if (widget.justCreated) showToast(context, '预约成功！回收员将尽快接单');
    });
  }

  @override
  void dispose() {
    _liveTimer?.cancel();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      _order = await _api.getOrder(widget.orderId);
      _syncLiveTimer();
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  /// 上门中时每 6 秒刷新一次回收员位置
  void _syncLiveTimer() {
    final live = _order?.status == 'in_progress' || _order?.status == 'accepted';
    if (live && _liveTimer == null) {
      _liveTimer = Timer.periodic(const Duration(seconds: 6), (_) async {
        try {
          final o = await _api.getOrder(widget.orderId);
          if (mounted) setState(() => _order = o);
          if (!o.isActive) {
            _liveTimer?.cancel();
            _liveTimer = null;
          }
        } catch (_) {}
      });
    } else if (!live) {
      _liveTimer?.cancel();
      _liveTimer = null;
    }
  }

  Future<void> _cancel() async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: const Text('取消预约'),
        content: const Text('确定要取消本次上门回收预约吗？'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('再想想')),
          TextButton(
              onPressed: () => Navigator.pop(c, true),
              child: const Text('确定取消', style: TextStyle(color: AppColors.moneyText))),
        ],
      ),
    );
    if (ok != true) return;
    await _act(() => _api.cancelOrder(widget.orderId), '已取消预约');
  }

  Future<void> _advance() async {
    await _act(() => _api.advanceOrder(widget.orderId), '订单状态已更新');
  }

  int _rateStars = 5;
  final Set<String> _rateTags = {};
  final _rateCtrl = TextEditingController();
  static const _tagOptions = ['准时上门', '称重公正', '态度友好', '搬运细心', '沟通顺畅'];

  Future<void> _submitRating() async {
    final me = context.read<AppProvider>().userId;
    await _act(
      () => _api.rateOrder(widget.orderId, me, _rateStars, tags: _rateTags.toList(), comment: _rateCtrl.text),
      '感谢评价，+5 积分',
    );
  }

  Widget _ratingCard(RecycleOrder o) {
    if (o.isRated) {
      return SoftCard(
        margin: const EdgeInsets.only(bottom: 16),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('我的评价', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
          const SizedBox(height: 8),
          Row(children: List.generate(5, (i) => Icon(Icons.star_rounded, size: 22, color: i < (o.rating ?? 0) ? AppColors.amber : AppColors.divider))),
          if (o.ratingTags.isNotEmpty) ...[
            const SizedBox(height: 8),
            Wrap(spacing: 6, runSpacing: 6, children: o.ratingTags.map((t) => StatusChip(text: t, color: AppColors.teal)).toList()),
          ],
          if ((o.ratingComment ?? '').isNotEmpty) ...[
            const SizedBox(height: 8),
            Text(o.ratingComment!, style: const TextStyle(fontSize: 13, color: AppColors.textGrey)),
          ],
        ]),
      );
    }
    return SoftCard(
      margin: const EdgeInsets.only(bottom: 16),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(children: [
          const Text('评价回收员', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
          const Spacer(),
          const StatusChip(text: '评价 +5 积分', color: AppColors.amber),
        ]),
        const SizedBox(height: 10),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: List.generate(5, (i) => GestureDetector(
            onTap: () => setState(() => _rateStars = i + 1),
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 4),
              child: Icon(Icons.star_rounded, size: 38, color: i < _rateStars ? AppColors.amber : AppColors.divider),
            ),
          )),
        ),
        Center(child: Text(['很差', '较差', '一般', '满意', '非常满意'][_rateStars - 1],
            style: const TextStyle(fontSize: 12, color: AppColors.textGrey, fontWeight: FontWeight.w600))),
        const SizedBox(height: 10),
        Wrap(
          spacing: 8, runSpacing: 8,
          children: _tagOptions.map((t) {
            final on = _rateTags.contains(t);
            return ChoiceChip(
              label: Text(t, style: TextStyle(fontSize: 12, color: on ? Colors.white : AppColors.textDark)),
              selected: on,
              selectedColor: AppColors.forest,
              onSelected: (_) => setState(() => on ? _rateTags.remove(t) : _rateTags.add(t)),
            );
          }).toList(),
        ),
        const SizedBox(height: 10),
        TextField(
          controller: _rateCtrl,
          maxLength: 100,
          decoration: const InputDecoration(hintText: '还有什么想说的（选填）', counterText: '', isDense: true),
        ),
        const SizedBox(height: 10),
        SizedBox(
          width: double.infinity,
          height: 46,
          child: ElevatedButton.icon(
            onPressed: _acting ? null : _submitRating,
            icon: const Icon(Icons.rate_review_rounded, size: 18),
            label: const Text('提交评价'),
          ),
        ),
      ]),
    );
  }

  Future<void> _act(Future<RecycleOrder> Function() fn, String msg) async {
    setState(() => _acting = true);
    try {
      _order = await fn();
      if (!mounted) return;
      showToast(context, msg);
      context.read<AppProvider>().loadHome();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    } finally {
      if (mounted) setState(() => _acting = false);
    }
  }

  int get _stepIndex {
    final s = _order?.status ?? 'pending';
    final i = _steps.indexWhere((e) => e.$1 == s);
    return i < 0 ? 0 : i;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('订单详情')),
      body: _loading
          ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
          : _error != null
              ? ErrorState(message: _error!, onRetry: _load)
              : _body(),
    );
  }

  Widget _body() {
    final o = _order!;
    final c = statusColor(o.status);
    return Column(
      children: [
        Expanded(
          child: ListView(
            padding: const EdgeInsets.fromLTRB(20, 4, 20, 20),
            children: [
              // 状态卡
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  gradient: LinearGradient(colors: [c, c.withValues(alpha: 0.75)]),
                  borderRadius: BorderRadius.circular(AppRadius.lg),
                  boxShadow: AppShadows.soft,
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(o.statusText,
                        style: const TextStyle(
                            color: Colors.white, fontSize: 22, fontWeight: FontWeight.w900)),
                    const SizedBox(height: 4),
                    Text(_statusHint(o),
                        style: TextStyle(color: Colors.white.withValues(alpha: 0.9), fontSize: 13)),
                    if (o.status != 'cancelled') ...[
                      const SizedBox(height: 20),
                      _stepper(),
                    ],
                  ],
                ),
              ),
              const SizedBox(height: 16),

              // 实时地图
              if (o.hasLocation && o.status != 'cancelled') ...[
                SoftCard(
                  padding: const EdgeInsets.all(12),
                  margin: const EdgeInsets.only(bottom: 16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(children: [
                        const Icon(Icons.map_rounded, size: 18, color: AppColors.forest),
                        const SizedBox(width: 6),
                        Text(o.recyclerLocation != null ? '回收员实时位置' : '上门地址',
                            style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                        const Spacer(),
                        if (o.recyclerLocation != null)
                          Row(children: [
                            Container(width: 8, height: 8, decoration: const BoxDecoration(color: AppColors.green, shape: BoxShape.circle)),
                            const SizedBox(width: 4),
                            Text('更新于 ${o.recyclerLocation!.updatedAt.length >= 16 ? o.recyclerLocation!.updatedAt.substring(11, 16) : ''}',
                                style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                          ]),
                      ]),
                      const SizedBox(height: 10),
                      OrderMap(
                        destination: LatLng(o.lat!, o.lng!),
                        recycler: o.recyclerLocation == null ? null : LatLng(o.recyclerLocation!.lat, o.recyclerLocation!.lng),
                        recyclerAvatar: o.recyclerAvatar,
                        height: 200,
                      ),
                    ],
                  ),
                ),
              ],

              // 评价回收员（订单完成后）
              if (o.status == 'completed' && o.recyclerName != null) _ratingCard(o),

              // 回收员
              if (o.recyclerName != null)
                SoftCard(
                  margin: const EdgeInsets.only(bottom: 16),
                  child: Row(
                    children: [
                      Avatar(o.recyclerAvatar ?? '', size: 52, border: AppColors.lime, name: o.recyclerName),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(o.recyclerName!,
                                style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                            const SizedBox(height: 2),
                            Row(children: [
                              const Icon(Icons.star_rounded, size: 14, color: AppColors.amber),
                              Text(o.isRated ? ' 您已评价 ${o.rating} 星' : ' 认证回收员 · 已实名',
                                  style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
                            ]),
                          ],
                        ),
                      ),
                      GestureDetector(
                        onTap: () => LocationService.dial(o.recyclerPhone ?? ''),
                        child: const IconBadge(
                          icon: Icons.phone_rounded,
                          color: AppColors.forest,
                          size: 42,
                          iconSize: 20,
                        ),
                      ),
                    ],
                  ),
                ),

              // 回收明细
              SoftCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('回收明细', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                    const SizedBox(height: 12),
                    ...o.items.map((it) => Padding(
                          padding: const EdgeInsets.only(bottom: 10),
                          child: Row(
                            children: [
                              const Icon(Icons.recycling_rounded, size: 18, color: AppColors.forest),
                              const SizedBox(width: 8),
                              Text(it.categoryName, style: const TextStyle(fontWeight: FontWeight.w600)),
                              const Spacer(),
                              Text(
                                it.actualWeight != null
                                    ? '实际 ${it.actualWeight!.toStringAsFixed(1)} kg'
                                    : '预估 ${it.estimatedWeight.toStringAsFixed(1)} kg',
                                style: const TextStyle(color: AppColors.textGrey, fontSize: 13),
                              ),
                            ],
                          ),
                        )),
                    if (o.status == 'completed') ...[
                      const Divider(height: 20),
                      _kv('实际总重', '${o.actualWeight?.toStringAsFixed(1) ?? '-'} kg'),
                      _kv('回收金额', '¥${o.amount?.toStringAsFixed(2) ?? '-'}', color: AppColors.moneyText),
                      _kv('获得积分', '+${o.pointsEarned}', color: AppColors.pointsText),
                      _kv('减碳量', '${o.carbonSaved.toStringAsFixed(2)} kg CO₂', color: AppColors.forest),
                      _kv('回收款', o.paid ? '已通过${o.payMethodText}支付' : '待回收员支付',
                          color: o.paid ? AppColors.forest : AppColors.orangeText),
                    ],
                  ],
                ),
              ),
              const SizedBox(height: 16),

              // 预约信息
              SoftCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('预约信息', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                    const SizedBox(height: 12),
                    _kv('订单编号', o.orderNo),
                    _kv('上门时间', o.appointmentTime),
                    _kv('联系人', '${o.contactName} ${o.contactPhone}'),
                    _kv('上门地址', o.addressText),
                    if (o.remark.isNotEmpty) _kv('备注', o.remark),
                    _kv('下单时间', o.createdAt),
                  ],
                ),
              ),
            ],
          ),
        ),
        if (o.isActive) _actions(o),
      ],
    );
  }

  Widget _stepper() {
    final idx = _stepIndex;
    return Row(
      children: List.generate(_steps.length * 2 - 1, (i) {
        if (i.isOdd) {
          final done = (i ~/ 2) < idx;
          return Expanded(
            child: Container(
              height: 3,
              margin: const EdgeInsets.only(bottom: 22),
              color: done ? Colors.white : Colors.white.withValues(alpha: 0.3),
            ),
          );
        }
        final s = i ~/ 2;
        final done = s <= idx;
        return Column(
          children: [
            Container(
              width: 34,
              height: 34,
              decoration: BoxDecoration(
                color: done ? Colors.white : Colors.white.withValues(alpha: 0.25),
                shape: BoxShape.circle,
              ),
              child: Icon(_steps[s].$3,
                  size: 18, color: done ? statusColor(_order!.status) : AppColors.onDarkSoft),
            ),
            const SizedBox(height: 6),
            Text(_steps[s].$2,
                style: TextStyle(
                    fontSize: 11,
                    color: Colors.white.withValues(alpha: done ? 1 : 0.6),
                    fontWeight: done ? FontWeight.w800 : FontWeight.w500)),
          ],
        );
      }),
    );
  }

  Widget _kv(String k, String v, {Color? color}) => Padding(
        padding: const EdgeInsets.only(bottom: 8),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(width: 72, child: Text(k, style: const TextStyle(color: AppColors.textGrey, fontSize: 13))),
            Expanded(
              child: Text(v,
                  style: TextStyle(
                      fontSize: 13,
                      fontWeight: color != null ? FontWeight.w800 : FontWeight.w500,
                      color: color ?? AppColors.textDark)),
            ),
          ],
        ),
      );

  String _statusHint(RecycleOrder o) {
    switch (o.status) {
      case 'pending':
        return '订单已提交，正在为您匹配附近的回收员';
      case 'accepted':
        return '${o.recyclerName ?? '回收员'} 已接单，将按预约时间上门';
      case 'in_progress':
        return '${o.recyclerName ?? '回收员'} 正在赶来，请保持电话畅通';
      case 'completed':
        return '感谢您为地球减碳 ${o.carbonSaved.toStringAsFixed(1)} kg';
      case 'cancelled':
        return '订单已取消';
      default:
        return '';
    }
  }

  Widget _actions(RecycleOrder o) {
    return Container(
      padding: const EdgeInsets.fromLTRB(20, 12, 20, 12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(AppRadius.lg)),
        boxShadow: AppShadows.soft,
      ),
      child: SafeArea(
        top: false,
        child: Row(
          children: [
            if (o.canCancel)
              Expanded(
                child: OutlinedButton(
                  onPressed: _acting ? null : _cancel,
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.textGrey,
                    side: const BorderSide(color: AppColors.divider),
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.lg)),
                  ),
                  child: const Text('取消预约'),
                ),
              ),
            if (o.canCancel) const SizedBox(width: 12),
            Expanded(
              flex: 2,
              child: ElevatedButton.icon(
                onPressed: _acting ? null : _advance,
                icon: const Icon(Icons.fast_forward_rounded, size: 18),
                label: Text(_advanceLabel(o.status)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _advanceLabel(String s) {
    switch (s) {
      case 'pending':
        return '模拟：回收员接单';
      case 'accepted':
        return '模拟：回收员出发';
      case 'in_progress':
        return '模拟：称重完成';
      default:
        return '推进状态';
    }
  }
}
