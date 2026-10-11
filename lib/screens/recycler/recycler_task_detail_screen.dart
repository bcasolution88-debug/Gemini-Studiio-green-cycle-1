import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';

/// 回收员：任务详情（接单 / 出发 / 称重完成）
class RecyclerTaskDetailScreen extends StatefulWidget {
  final String orderId;
  const RecyclerTaskDetailScreen({super.key, required this.orderId});

  @override
  State<RecyclerTaskDetailScreen> createState() => _RecyclerTaskDetailScreenState();
}

class _RecyclerTaskDetailScreenState extends State<RecyclerTaskDetailScreen> {
  final _api = ApiService.instance;
  RecycleOrder? _order;
  List<RecycleCategory> _cats = [];
  bool _loading = true;
  bool _acting = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final r = await Future.wait([
        _api.getOrder(widget.orderId),
        context.read<AppProvider>().ensureCategories(),
      ]);
      _order = r[0] as RecycleOrder;
      _cats = r[1] as List<RecycleCategory>;
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  String get _me => context.read<AppProvider>().userId;

  Future<void> _run(Future<RecycleOrder> Function() fn, String msg) async {
    setState(() => _acting = true);
    try {
      _order = await fn();
      if (mounted) showToast(context, msg);
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    } finally {
      if (mounted) setState(() => _acting = false);
    }
  }

  Future<void> _complete() async {
    final o = _order!;
    final ctrls = {
      for (final it in o.items) it.categoryId: TextEditingController(text: it.estimatedWeight.toStringAsFixed(1))
    };
    final ok = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (c) => Padding(
        padding: EdgeInsets.only(bottom: MediaQuery.of(c).viewInsets.bottom),
        child: _WeighSheet(order: o, cats: _cats, ctrls: ctrls),
      ),
    );
    if (ok != true) return;
    final items = o.items
        .map((it) => {
              'category_id': it.categoryId,
              'actual_weight': double.tryParse(ctrls[it.categoryId]!.text) ?? it.estimatedWeight,
            })
        .toList();
    await _run(() => _api.recyclerComplete(o.id, _me, items), '回收完成，已为用户结算');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('任务详情')),
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
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  gradient: LinearGradient(colors: [c, c.withValues(alpha: 0.75)]),
                  borderRadius: BorderRadius.circular(AppRadius.lg),
                  boxShadow: AppShadows.soft,
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(_title(o.status),
                              style: const TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.w900)),
                          const SizedBox(height: 4),
                          Text('上门时间 ${o.appointmentTime}',
                              style: TextStyle(color: Colors.white.withValues(alpha: 0.9), fontSize: 13)),
                        ],
                      ),
                    ),
                    Icon(_icon(o.status), color: Colors.white.withValues(alpha: 0.9), size: 48),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              SoftCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('客户信息', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        const IconBadge(icon: Icons.person_rounded, color: AppColors.blue, size: 44, iconSize: 22),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(o.contactName, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                              Text(o.contactPhone, style: const TextStyle(color: AppColors.textGrey, fontSize: 13)),
                            ],
                          ),
                        ),
                        const IconBadge(icon: Icons.phone_rounded, color: AppColors.forest, size: 40, iconSize: 20),
                        const SizedBox(width: 8),
                        const IconBadge(icon: Icons.navigation_rounded, color: AppColors.orange, size: 40, iconSize: 20),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Icon(Icons.location_on_rounded, size: 18, color: AppColors.coral),
                        const SizedBox(width: 6),
                        Expanded(child: Text(o.addressText, style: const TextStyle(fontSize: 14, height: 1.4))),
                      ],
                    ),
                    if (o.remark.isNotEmpty) ...[
                      const SizedBox(height: 10),
                      Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          color: AppColors.yellow.withValues(alpha: 0.2),
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: Row(children: [
                          const Icon(Icons.sticky_note_2_rounded, size: 16, color: AppColors.amber),
                          const SizedBox(width: 6),
                          Expanded(child: Text('备注：${o.remark}', style: const TextStyle(fontSize: 13))),
                        ]),
                      ),
                    ],
                  ],
                ),
              ),
              const SizedBox(height: 16),
              SoftCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('回收物品', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                    const SizedBox(height: 12),
                    ...o.items.map((it) {
                      final cat = _cats.where((c) => c.id == it.categoryId).firstOrNull;
                      final color = cat == null ? AppColors.forest : AppColors.fromHex(cat.color);
                      return Padding(
                        padding: const EdgeInsets.only(bottom: 10),
                        child: Row(
                          children: [
                            IconBadge(icon: categoryIcon(cat?.icon ?? ''), color: color, size: 36, iconSize: 18),
                            const SizedBox(width: 10),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(it.categoryName, style: const TextStyle(fontWeight: FontWeight.w700)),
                                  if (cat != null)
                                    Text('¥${cat.pricePerKg.toStringAsFixed(1)}/kg · ${cat.pointsPerKg}积分/kg',
                                        style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                                ],
                              ),
                            ),
                            Text(
                              it.actualWeight != null
                                  ? '实际 ${it.actualWeight!.toStringAsFixed(1)} kg'
                                  : '预估 ${it.estimatedWeight.toStringAsFixed(1)} kg',
                              style: TextStyle(fontWeight: FontWeight.w800, color: color, fontSize: 13),
                            ),
                          ],
                        ),
                      );
                    }),
                    if (o.status == 'completed') ...[
                      const Divider(height: 20),
                      _kv('实际总重', '${o.actualWeight?.toStringAsFixed(1)} kg'),
                      _kv('应付金额', '¥${o.amount?.toStringAsFixed(2)}', AppColors.coral),
                      _kv('用户获得积分', '+${o.pointsEarned}', AppColors.amber),
                      _kv('减碳量', '${o.carbonSaved.toStringAsFixed(2)} kg', AppColors.forest),
                    ],
                  ],
                ),
              ),
              const SizedBox(height: 16),
              SoftCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _kv('订单编号', o.orderNo),
                    _kv('下单时间', o.createdAt),
                    _kv('更新时间', o.updatedAt),
                  ],
                ),
              ),
            ],
          ),
        ),
        if (o.status != 'completed' && o.status != 'cancelled') _actions(o),
      ],
    );
  }

  Widget _actions(RecycleOrder o) {
    Widget btn;
    switch (o.status) {
      case 'pending':
        btn = ElevatedButton.icon(
          onPressed: _acting ? null : () => _run(() => _api.recyclerAccept(o.id, _me), '接单成功'),
          icon: const Icon(Icons.how_to_reg_rounded),
          label: const Text('立即接单'),
        );
        break;
      case 'accepted':
        btn = ElevatedButton.icon(
          onPressed: _acting ? null : () => _run(() => _api.recyclerDepart(o.id, _me), '已出发，请注意安全'),
          icon: const Icon(Icons.electric_moped_rounded),
          label: const Text('出发上门'),
        );
        break;
      default:
        btn = FilledButton.icon(
          onPressed: _acting ? null : _complete,
          icon: const Icon(Icons.scale_rounded),
          label: const Text('称重 · 完成回收'),
        );
    }
    return Container(
      padding: const EdgeInsets.fromLTRB(20, 12, 20, 12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(AppRadius.lg)),
        boxShadow: AppShadows.soft,
      ),
      child: SafeArea(top: false, child: SizedBox(width: double.infinity, height: 50, child: btn)),
    );
  }

  Widget _kv(String k, String v, [Color? color]) => Padding(
        padding: const EdgeInsets.only(bottom: 8),
        child: Row(children: [
          SizedBox(width: 90, child: Text(k, style: const TextStyle(color: AppColors.textGrey, fontSize: 13))),
          Expanded(
              child: Text(v,
                  style: TextStyle(
                      fontSize: 13,
                      fontWeight: color != null ? FontWeight.w800 : FontWeight.w500,
                      color: color ?? AppColors.textDark))),
        ]),
      );

  String _title(String s) {
    switch (s) {
      case 'pending':
        return '待接单';
      case 'accepted':
        return '已接单 · 待出发';
      case 'in_progress':
        return '上门中';
      case 'completed':
        return '已完成';
      case 'cancelled':
        return '已取消';
      default:
        return s;
    }
  }

  IconData _icon(String s) {
    switch (s) {
      case 'pending':
        return Icons.schedule_rounded;
      case 'accepted':
        return Icons.how_to_reg_rounded;
      case 'in_progress':
        return Icons.electric_moped_rounded;
      case 'completed':
        return Icons.verified_rounded;
      default:
        return Icons.cancel_rounded;
    }
  }
}

class _WeighSheet extends StatefulWidget {
  final RecycleOrder order;
  final List<RecycleCategory> cats;
  final Map<String, TextEditingController> ctrls;
  const _WeighSheet({required this.order, required this.cats, required this.ctrls});

  @override
  State<_WeighSheet> createState() => _WeighSheetState();
}

class _WeighSheetState extends State<_WeighSheet> {
  double get _amount => widget.order.items.fold(0.0, (s, it) {
        final cat = widget.cats.where((c) => c.id == it.categoryId).firstOrNull;
        final w = double.tryParse(widget.ctrls[it.categoryId]!.text) ?? 0;
        return s + (cat == null ? 0 : w * cat.pricePerKg);
      });

  int get _points => widget.order.items.fold(0, (s, it) {
        final cat = widget.cats.where((c) => c.id == it.categoryId).firstOrNull;
        final w = double.tryParse(widget.ctrls[it.categoryId]!.text) ?? 0;
        return s + (cat == null ? 0 : (w * cat.pointsPerKg).toInt());
      });

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(24, 16, 24, 24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Center(
            child: Container(
                width: 40, height: 4,
                decoration: BoxDecoration(color: AppColors.divider, borderRadius: BorderRadius.circular(2))),
          ),
          const SizedBox(height: 16),
          const Text('现场称重', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800)),
          const Text('请填写各品类实际重量（kg）', style: TextStyle(fontSize: 12, color: AppColors.textGrey)),
          const SizedBox(height: 16),
          ...widget.order.items.map((it) {
            final cat = widget.cats.where((c) => c.id == it.categoryId).firstOrNull;
            final color = cat == null ? AppColors.forest : AppColors.fromHex(cat.color);
            return Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Row(
                children: [
                  IconBadge(icon: categoryIcon(cat?.icon ?? ''), color: color, size: 40, iconSize: 20),
                  const SizedBox(width: 10),
                  Expanded(
                    flex: 2,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(it.categoryName, style: const TextStyle(fontWeight: FontWeight.w700)),
                        Text('预估 ${it.estimatedWeight.toStringAsFixed(1)} kg',
                            style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                      ],
                    ),
                  ),
                  Expanded(
                    flex: 2,
                    child: TextField(
                      controller: widget.ctrls[it.categoryId],
                      keyboardType: const TextInputType.numberWithOptions(decimal: true),
                      textAlign: TextAlign.center,
                      onChanged: (_) => setState(() {}),
                      decoration: const InputDecoration(
                        suffixText: 'kg',
                        contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                      ),
                    ),
                  ),
                ],
              ),
            );
          }),
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: AppColors.lime.withValues(alpha: 0.2),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text('应付用户', style: TextStyle(fontSize: 11, color: AppColors.textGrey)),
                      Text('¥${_amount.toStringAsFixed(2)}',
                          style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w900, color: AppColors.coral)),
                    ],
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    const Text('用户获得积分', style: TextStyle(fontSize: 11, color: AppColors.textGrey)),
                    Text('+$_points', style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w900, color: AppColors.amber)),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          SizedBox(
            height: 50,
            child: FilledButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('确认完成并结算'),
            ),
          ),
        ],
      ),
    );
  }
}
