import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';
import 'recycler_task_detail_screen.dart';

/// 我的任务
class RecyclerTasksScreen extends StatefulWidget {
  const RecyclerTasksScreen({super.key});

  @override
  State<RecyclerTasksScreen> createState() => _RecyclerTasksScreenState();
}

class _RecyclerTasksScreenState extends State<RecyclerTasksScreen> with SingleTickerProviderStateMixin {
  late final TabController _tab;
  final _api = ApiService.instance;
  List<RecycleOrder> _active = [];
  List<RecycleOrder> _done = [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _tab = TabController(length: 2, vsync: this);
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  @override
  void dispose() {
    _tab.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final id = context.read<AppProvider>().userId;
      final r = await Future.wait([
        _api.recyclerTasks(id, status: 'active'),
        _api.recyclerTasks(id, status: 'completed'),
      ]);
      _active = r[0];
      _done = r[1];
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final topPad = MediaQuery.of(context).padding.top;
    return Scaffold(
      body: Column(
        children: [
          Container(
            padding: EdgeInsets.fromLTRB(20, topPad + 14, 20, 0),
            decoration: const BoxDecoration(
              gradient: AppColors.headerGradient,
              borderRadius: BorderRadius.vertical(bottom: Radius.circular(32)),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    const Text('我的任务',
                        style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.w900)),
                    const Spacer(),
                    IconButton(onPressed: _load, icon: const Icon(Icons.refresh_rounded, color: Colors.white)),
                  ],
                ),
                TabBar(
                  controller: _tab,
                  indicatorColor: AppColors.lime,
                  indicatorWeight: 3,
                  labelColor: Colors.white,
                  unselectedLabelColor: Colors.white60,
                  labelStyle: const TextStyle(fontWeight: FontWeight.w800),
                  dividerColor: Colors.transparent,
                  tabs: [Tab(text: '进行中 (${_active.length})'), Tab(text: '已完成 (${_done.length})')],
                ),
              ],
            ),
          ),
          Expanded(
            child: _loading
                ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
                : _error != null
                    ? ErrorState(message: _error!, onRetry: _load)
                    : TabBarView(
                        controller: _tab,
                        children: [_list(_active, true), _list(_done, false)],
                      ),
          ),
        ],
      ),
    );
  }

  Widget _list(List<RecycleOrder> list, bool active) {
    if (list.isEmpty) {
      return EmptyState(
        icon: active ? Icons.assignment_turned_in_rounded : Icons.history_rounded,
        title: active ? '暂无进行中任务' : '暂无已完成任务',
        subtitle: active ? '去接单大厅抢单吧' : null,
      );
    }
    return RefreshIndicator(
      color: AppColors.forest,
      onRefresh: _load,
      child: ListView.builder(
        padding: const EdgeInsets.only(top: 10, bottom: 30),
        itemCount: list.length,
        itemBuilder: (c, i) {
          final o = list[i];
          final col = statusColor(o.status);
          return SoftCard(
            margin: const EdgeInsets.fromLTRB(20, 0, 20, 12),
            onTap: () => Navigator.push(context,
                    MaterialPageRoute(builder: (_) => RecyclerTaskDetailScreen(orderId: o.id)))
                .then((_) => _load()),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    IconBadge(
                        icon: o.status == 'completed'
                            ? Icons.verified_rounded
                            : (o.status == 'in_progress' ? Icons.electric_moped_rounded : Icons.how_to_reg_rounded),
                        color: col,
                        size: 42,
                        iconSize: 22),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(o.categoryNames,
                              style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis),
                          const SizedBox(height: 2),
                          Text(o.appointmentTime, style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
                        ],
                      ),
                    ),
                    StatusChip(text: active ? (o.status == 'accepted' ? '待出发' : '上门中') : '已完成', color: col),
                  ],
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    const Icon(Icons.location_on_rounded, size: 14, color: AppColors.coral),
                    const SizedBox(width: 4),
                    Expanded(
                      child: Text(o.addressText,
                          style: const TextStyle(fontSize: 12, color: AppColors.textGrey),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      active
                          ? '预估 ${o.estimatedWeight.toStringAsFixed(1)}kg'
                          : '${o.actualWeight?.toStringAsFixed(1) ?? '-'}kg · ¥${o.amount?.toStringAsFixed(1) ?? '-'}',
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.w800, color: col),
                    ),
                  ],
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
