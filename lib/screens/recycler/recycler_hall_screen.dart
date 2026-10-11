import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';
import 'recycler_task_detail_screen.dart';

/// 接单大厅
class RecyclerHallScreen extends StatefulWidget {
  final VoidCallback onGoTasks;
  const RecyclerHallScreen({super.key, required this.onGoTasks});

  @override
  State<RecyclerHallScreen> createState() => _RecyclerHallScreenState();
}

class _RecyclerHallScreenState extends State<RecyclerHallScreen> {
  final _api = ApiService.instance;
  List<RecycleOrder> _orders = [];
  RecyclerStats? _stats;
  bool _loading = true;
  String? _error;
  String? _acceptingId;

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
      final id = context.read<AppProvider>().userId;
      final r = await Future.wait([_api.recyclerHall(id), _api.recyclerStats(id)]);
      _orders = r[0] as List<RecycleOrder>;
      _stats = r[1] as RecyclerStats;
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _accept(RecycleOrder o) async {
    final me = context.read<AppProvider>().userId;
    final ok = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: const Text('确认接单'),
        content: Text('${o.categoryNames} · 预估 ${o.estimatedWeight.toStringAsFixed(1)}kg\n上门时间：${o.appointmentTime}\n地址：${o.addressText}'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('取消')),
          ElevatedButton(onPressed: () => Navigator.pop(c, true), child: const Text('接单')),
        ],
      ),
    );
    if (ok != true) return;
    setState(() => _acceptingId = o.id);
    try {
      await _api.recyclerAccept(o.id, me);
      if (!mounted) return;
      showToast(context, '接单成功，请按时上门');
      await _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    } finally {
      if (mounted) setState(() => _acceptingId = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    final topPad = MediaQuery.of(context).padding.top;
    final user = context.watch<AppProvider>().user;
    return Scaffold(
      body: RefreshIndicator(
        color: AppColors.forest,
        onRefresh: _load,
        child: CustomScrollView(
          slivers: [
            SliverToBoxAdapter(
              child: Container(
                padding: EdgeInsets.fromLTRB(20, topPad + 14, 20, 24),
                decoration: const BoxDecoration(
                  gradient: AppColors.headerGradient,
                  borderRadius: BorderRadius.vertical(bottom: Radius.circular(36)),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Avatar(user?.avatar ?? '', size: 44, border: AppColors.lime),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('${user?.nickname ?? '回收员'}，辛苦了',
                                  style: const TextStyle(
                                      color: Colors.white, fontSize: 18, fontWeight: FontWeight.w900)),
                              Text('回收员端 · 服务评分 ${_stats?.rating.toStringAsFixed(1) ?? '-'}',
                                  style: TextStyle(color: Colors.white.withValues(alpha: 0.85), fontSize: 12)),
                            ],
                          ),
                        ),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                          decoration: BoxDecoration(
                            gradient: AppColors.sunGradient,
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: const Row(children: [
                            Icon(Icons.circle, size: 8, color: AppColors.deepGreen),
                            SizedBox(width: 4),
                            Text('接单中',
                                style: TextStyle(
                                    fontSize: 12, fontWeight: FontWeight.w800, color: AppColors.deepGreen)),
                          ]),
                        ),
                      ],
                    ),
                    const SizedBox(height: 20),
                    Row(
                      children: [
                        _stat('今日完成', '${_stats?.today.count ?? 0}', '单'),
                        _stat('今日回收', _stats?.today.weight.toStringAsFixed(1) ?? '0', 'kg'),
                        _stat('今日金额', '¥${_stats?.today.amount.toStringAsFixed(0) ?? '0'}', ''),
                        GestureDetector(
                          onTap: widget.onGoTasks,
                          child: _stat('进行中', '${_stats?.activeTasks ?? 0}', '单', highlight: true),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            SliverToBoxAdapter(
              child: SectionHeader(
                title: '待接单 (${_orders.length})',
                action: '刷新',
                onAction: _load,
              ),
            ),
            if (_loading)
              const SliverFillRemaining(
                  hasScrollBody: false,
                  child: Center(child: CircularProgressIndicator(color: AppColors.forest)))
            else if (_error != null)
              SliverFillRemaining(hasScrollBody: false, child: ErrorState(message: _error!, onRetry: _load))
            else if (_orders.isEmpty)
              const SliverFillRemaining(
                hasScrollBody: false,
                child: EmptyState(icon: Icons.inbox_rounded, title: '暂无待接订单', subtitle: '下拉刷新查看最新订单'),
              )
            else
              SliverList.builder(
                itemCount: _orders.length,
                itemBuilder: (c, i) => _HallCard(
                  order: _orders[i],
                  accepting: _acceptingId == _orders[i].id,
                  onAccept: () => _accept(_orders[i]),
                  onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(
                          builder: (_) => RecyclerTaskDetailScreen(orderId: _orders[i].id))).then((_) => _load()),
                ),
              ),
            const SliverToBoxAdapter(child: SizedBox(height: 30)),
          ],
        ),
      ),
    );
  }

  Widget _stat(String l, String v, String unit, {bool highlight = false}) => Expanded(
        child: Container(
          margin: const EdgeInsets.only(right: 8),
          padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 8),
          decoration: BoxDecoration(
            color: highlight ? AppColors.lime.withValues(alpha: 0.9) : Colors.white.withValues(alpha: 0.14),
            borderRadius: BorderRadius.circular(14),
          ),
          child: Column(
            children: [
              RichText(
                text: TextSpan(
                  text: v,
                  style: TextStyle(
                      color: highlight ? AppColors.deepGreen : Colors.white,
                      fontSize: 18,
                      fontWeight: FontWeight.w900),
                  children: [
                    TextSpan(
                        text: unit,
                        style: TextStyle(
                            fontSize: 10, color: highlight ? AppColors.deepGreen : Colors.white70)),
                  ],
                ),
              ),
              const SizedBox(height: 2),
              Text(l,
                  style: TextStyle(
                      fontSize: 10, color: highlight ? AppColors.deepGreen : Colors.white70)),
            ],
          ),
        ),
      );
}

class _HallCard extends StatelessWidget {
  final RecycleOrder order;
  final bool accepting;
  final VoidCallback onAccept;
  final VoidCallback onTap;
  const _HallCard({required this.order, required this.accepting, required this.onAccept, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final o = order;
    return SoftCard(
      onTap: onTap,
      margin: const EdgeInsets.fromLTRB(20, 0, 20, 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const IconBadge(icon: Icons.schedule_rounded, color: AppColors.orange, size: 40, iconSize: 20),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(o.appointmentTime,
                        style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                    Text('订单 ${o.orderNo}', style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                  ],
                ),
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text('${o.estimatedWeight.toStringAsFixed(1)} kg',
                      style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16, color: AppColors.forest)),
                  const Text('预估重量', style: TextStyle(fontSize: 10, color: AppColors.textGrey)),
                ],
              ),
            ],
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 6,
            runSpacing: 6,
            children: o.items
                .map((it) => StatusChip(
                    text: '${it.categoryName} ${it.estimatedWeight.toStringAsFixed(1)}kg',
                    color: AppColors.teal))
                .toList(),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              const Icon(Icons.location_on_rounded, size: 16, color: AppColors.coral),
              const SizedBox(width: 4),
              Expanded(
                child: Text(o.addressText,
                    style: const TextStyle(fontSize: 13), maxLines: 1, overflow: TextOverflow.ellipsis),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              const Icon(Icons.person_rounded, size: 16, color: AppColors.blue),
              const SizedBox(width: 4),
              Text('${o.contactName} ${o.contactPhone}', style: const TextStyle(fontSize: 13, color: AppColors.textGrey)),
              const Spacer(),
              SizedBox(
                height: 36,
                child: ElevatedButton(
                  onPressed: accepting ? null : onAccept,
                  style: ElevatedButton.styleFrom(padding: const EdgeInsets.symmetric(horizontal: 18)),
                  child: accepting
                      ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                      : const Text('抢单', style: TextStyle(fontSize: 14)),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
