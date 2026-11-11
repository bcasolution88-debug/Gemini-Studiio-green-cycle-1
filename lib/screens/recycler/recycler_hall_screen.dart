import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../main.dart' show openNotificationTarget;
import '../../widgets/common_widgets.dart';
import '../notifications/notifications_screen.dart';
import 'recycler_task_detail_screen.dart';
import 'recycler_wallet_screen.dart';

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
  bool _toggling = false;

  bool get _online => _stats?.online ?? true;

  Future<void> _toggleOnline(bool v) async {
    if (_toggling) return;
    final me = context.read<AppProvider>().userId;
    if (!v) {
      final ok = await showDialog<bool>(
        context: context,
        builder: (c) => AlertDialog(
          title: const Text('暂停接单？'),
          content: const Text('暂停后将不再收到新订单提醒，也无法抢单；已接的订单不受影响，可随时重新开启。'),
          actions: [
            TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('取消')),
            ElevatedButton(
              style: ElevatedButton.styleFrom(backgroundColor: AppColors.coral),
              onPressed: () => Navigator.pop(c, true),
              child: const Text('暂停接单'),
            ),
          ],
        ),
      );
      if (ok != true) return;
    }
    setState(() => _toggling = true);
    try {
      await _api.setRecyclerOnline(me, v);
      if (!mounted) return;
      showToast(context, v ? '已开启接单，新订单将实时提醒' : '已暂停接单');
      await _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    } finally {
      if (mounted) setState(() => _toggling = false);
    }
  }

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
      final r = await Future.wait([
        _api.recyclerHall(id),
        _api.recyclerStats(id),
      ]);
      _orders = r[0] as List<RecycleOrder>;
      _stats = r[1] as RecyclerStats;
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _accept(RecycleOrder o) async {
    if (!_online) {
      showToast(context, '当前为「暂停接单」状态，请先开启接单', error: true);
      return;
    }
    final me = context.read<AppProvider>().userId;
    final ok = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: const Text('确认接单'),
        content: Text(
          '${o.categoryNames} · 预估 ${o.estimatedWeight.toStringAsFixed(1)}kg\n上门时间：${o.appointmentTime}\n地址：${o.addressText}',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(c, false),
            child: const Text('取消'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(c, true),
            child: const Text('接单'),
          ),
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
                  borderRadius: BorderRadius.vertical(
                    bottom: Radius.circular(36),
                  ),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Avatar(
                          user?.avatar ?? '',
                          size: 44,
                          border: AppColors.lime,
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                '${user?.nickname ?? '回收员'}，辛苦了',
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 18,
                                  fontWeight: FontWeight.w900,
                                ),
                              ),
                              Text(
                                '回收员端 · 服务评分 ${_stats?.rating.toStringAsFixed(1) ?? '-'}',
                                style: TextStyle(
                                  color: Colors.white.withValues(alpha: 0.85),
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ),
                        NotificationBell(
                          onTap: () => Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) => NotificationsScreen(
                                onOpen: (n) =>
                                    openNotificationTarget(context, n),
                              ),
                            ),
                          ).then((_) => _load()),
                        ),
                        const SizedBox(width: 8),
                        GestureDetector(
                          onTap: () => Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) => const RecyclerWalletScreen(),
                            ),
                          ).then((_) => _load()),
                          child: Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 12,
                              vertical: 8,
                            ),
                            decoration: BoxDecoration(
                              gradient: AppColors.sunGradient,
                              borderRadius: BorderRadius.circular(20),
                            ),
                            child: Row(
                              children: [
                                const Icon(
                                  Icons.account_balance_wallet_rounded,
                                  size: 14,
                                  color: AppColors.deepGreen,
                                ),
                                const SizedBox(width: 4),
                                Text(
                                  '¥${_stats?.balance.toStringAsFixed(0) ?? '-'}',
                                  style: const TextStyle(
                                    fontSize: 12,
                                    fontWeight: FontWeight.w800,
                                    color: AppColors.deepGreen,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 20),
                    Row(
                      children: [
                        _stat('今日完成', '${_stats?.today.count ?? 0}', '单'),
                        _stat(
                          '今日回收',
                          _stats?.today.weight.toStringAsFixed(1) ?? '0',
                          'kg',
                        ),
                        _stat(
                          '今日金额',
                          '¥${_stats?.today.amount.toStringAsFixed(0) ?? '0'}',
                          '',
                        ),
                        _stat(
                          '进行中',
                          '${_stats?.activeTasks ?? 0}',
                          '单',
                          highlight: true,
                          onTap: widget.onGoTasks,
                        ),
                      ],
                    ),
                    const SizedBox(height: 14),
                    _OnlineBar(online: _online, busy: _toggling, onChanged: _toggleOnline),
                  ],
                ),
              ),
            ),
            if (!_online && !_loading)
              SliverToBoxAdapter(
                child: Container(
                  margin: const EdgeInsets.fromLTRB(16, 14, 16, 0),
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                  decoration: BoxDecoration(
                    color: AppColors.coral.withValues(alpha: 0.10),
                    borderRadius: BorderRadius.circular(AppRadius.md),
                    border: Border.all(color: AppColors.coral.withValues(alpha: 0.35)),
                  ),
                  child: Row(children: [
                    const Icon(Icons.pause_circle_filled_rounded, color: AppColors.moneyText, size: 20),
                    const SizedBox(width: 10),
                    const Expanded(
                      child: Text('您已暂停接单，新订单不会提醒您，也无法抢单。',
                          style: TextStyle(fontSize: 12.5, color: AppColors.moneyText, fontWeight: FontWeight.w600)),
                    ),
                    TextButton(
                      onPressed: _toggling ? null : () => _toggleOnline(true),
                      style: TextButton.styleFrom(foregroundColor: AppColors.forest, padding: const EdgeInsets.symmetric(horizontal: 8)),
                      child: const Text('开启', style: TextStyle(fontWeight: FontWeight.w800)),
                    ),
                  ]),
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
                child: Center(
                  child: CircularProgressIndicator(color: AppColors.forest),
                ),
              )
            else if (_error != null)
              SliverFillRemaining(
                hasScrollBody: false,
                child: ErrorState(message: _error!, onRetry: _load),
              )
            else if (_orders.isEmpty)
              const SliverFillRemaining(
                hasScrollBody: false,
                child: EmptyState(
                  icon: Icons.inbox_rounded,
                  title: '暂无待接订单',
                  subtitle: '下拉刷新查看最新订单',
                ),
              )
            else
              SliverList.builder(
                itemCount: _orders.length,
                itemBuilder: (c, i) => _HallCard(
                  order: _orders[i],
                  enabled: _online,
                  accepting: _acceptingId == _orders[i].id,
                  onAccept: () => _accept(_orders[i]),
                  onTap: () => Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (_) =>
                          RecyclerTaskDetailScreen(orderId: _orders[i].id),
                    ),
                  ).then((_) => _load()),
                ),
              ),
            const SliverToBoxAdapter(child: SizedBox(height: 30)),
          ],
        ),
      ),
    );
  }

  Widget _stat(
    String l,
    String v,
    String unit, {
    bool highlight = false,
    VoidCallback? onTap,
  }) => Expanded(
    child: GestureDetector(
      onTap: onTap,
      child: Container(
        margin: const EdgeInsets.only(right: 8),
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 8),
        decoration: BoxDecoration(
          color: highlight
              ? AppColors.lime.withValues(alpha: 0.9)
              : Colors.white.withValues(alpha: 0.14),
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
                  fontWeight: FontWeight.w900,
                ),
                children: [
                  TextSpan(
                    text: unit,
                    style: TextStyle(
                      fontSize: 10,
                      color: highlight ? AppColors.deepGreen : AppColors.onDarkSoft,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 2),
            Text(
              l,
              style: TextStyle(
                fontSize: 10,
                color: highlight ? AppColors.deepGreen : AppColors.onDarkSoft,
              ),
            ),
          ],
        ),
      ),
    ),
  );
}

class _HallCard extends StatelessWidget {
  final RecycleOrder order;
  final bool accepting;
  final bool enabled;
  final VoidCallback onAccept;
  final VoidCallback onTap;
  const _HallCard({
    required this.order,
    required this.accepting,
    this.enabled = true,
    required this.onAccept,
    required this.onTap,
  });

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
              const IconBadge(
                icon: Icons.schedule_rounded,
                color: AppColors.orange,
                size: 40,
                iconSize: 20,
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      o.appointmentTime,
                      style: const TextStyle(
                        fontWeight: FontWeight.w800,
                        fontSize: 15,
                      ),
                    ),
                    Text(
                      '订单 ${o.orderNo}',
                      style: const TextStyle(
                        fontSize: 11,
                        color: AppColors.textGrey,
                      ),
                    ),
                  ],
                ),
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    '${o.estimatedWeight.toStringAsFixed(1)} kg',
                    style: const TextStyle(
                      fontWeight: FontWeight.w900,
                      fontSize: 16,
                      color: AppColors.forest,
                    ),
                  ),
                  const Text(
                    '预估重量',
                    style: TextStyle(fontSize: 10, color: AppColors.textGrey),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 6,
            runSpacing: 6,
            children: o.items
                .map(
                  (it) => StatusChip(
                    text:
                        '${it.categoryName} ${it.estimatedWeight.toStringAsFixed(1)}kg',
                    color: AppColors.teal,
                  ),
                )
                .toList(),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              const Icon(
                Icons.location_on_rounded,
                size: 16,
                color: AppColors.coral,
              ),
              const SizedBox(width: 4),
              Expanded(
                child: Text(
                  o.addressText,
                  style: const TextStyle(fontSize: 13),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              const Icon(Icons.person_rounded, size: 16, color: AppColors.blue),
              const SizedBox(width: 4),
              Text(
                '${o.contactName} ${o.contactPhone}',
                style: const TextStyle(fontSize: 13, color: AppColors.textGrey),
              ),
              const Spacer(),
              SizedBox(
                height: 36,
                child: ElevatedButton(
                  onPressed: accepting ? null : onAccept,
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(horizontal: 18),
                    backgroundColor: enabled ? null : AppColors.textGrey,
                  ),
                  child: accepting
                      ? const SizedBox(
                          width: 16,
                          height: 16,
                          child: CircularProgressIndicator(
                            strokeWidth: 2,
                            color: Colors.white,
                          ),
                        )
                      : Text(enabled ? '抢单' : '已暂停', style: const TextStyle(fontSize: 14)),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

/// 接单状态开关（深绿头部内的胶囊条）
class _OnlineBar extends StatelessWidget {
  final bool online;
  final bool busy;
  final ValueChanged<bool> onChanged;
  const _OnlineBar({required this.online, required this.busy, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    final c = online ? AppColors.lime : AppColors.coral;
    return Container(
      padding: const EdgeInsets.fromLTRB(14, 10, 10, 10),
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.14),
        borderRadius: BorderRadius.circular(AppRadius.md),
        border: Border.all(color: c.withValues(alpha: 0.6)),
      ),
      child: Row(
        children: [
          AnimatedContainer(
            duration: const Duration(milliseconds: 250),
            width: 10,
            height: 10,
            decoration: BoxDecoration(
              color: c,
              shape: BoxShape.circle,
              boxShadow: [BoxShadow(color: c.withValues(alpha: 0.7), blurRadius: online ? 8 : 0, spreadRadius: online ? 1 : 0)],
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(online ? '接单中' : '已暂停接单',
                    style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w800, fontSize: 14)),
                Text(online ? '新订单将实时提醒您' : '不接收新单，已接订单不受影响',
                    style: const TextStyle(color: AppColors.onDarkSoft, fontSize: 11)),
              ],
            ),
          ),
          if (busy)
            const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
          else
            Switch.adaptive(
              value: online,
              onChanged: onChanged,
              activeThumbColor: AppColors.deepGreen,
              activeTrackColor: AppColors.lime,
              inactiveThumbColor: Colors.white,
              inactiveTrackColor: AppColors.coral.withValues(alpha: 0.8),
            ),
        ],
      ),
    );
  }
}
