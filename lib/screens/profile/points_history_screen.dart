import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';

/// 积分明细
class PointsHistoryScreen extends StatefulWidget {
  const PointsHistoryScreen({super.key});

  @override
  State<PointsHistoryScreen> createState() => _PointsHistoryScreenState();
}

class _PointsHistoryScreenState extends State<PointsHistoryScreen> {
  List<PointRecord> _list = [];
  bool _loading = true;
  String? _error;
  int _filter = 0; // 0 全部 1 收入 2 支出

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
      _list = await ApiService.instance.getPointRecords(context.read<AppProvider>().userId);
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  List<PointRecord> get _filtered {
    if (_filter == 1) return _list.where((r) => r.amount > 0).toList();
    if (_filter == 2) return _list.where((r) => r.amount < 0).toList();
    return _list;
  }

  @override
  Widget build(BuildContext context) {
    final user = context.watch<AppProvider>().user;
    final earned = _list.where((r) => r.amount > 0).fold(0, (a, b) => a + b.amount);
    final spent = _list.where((r) => r.amount < 0).fold(0, (a, b) => a + b.amount.abs());
    return Scaffold(
      appBar: AppBar(title: const Text('积分明细')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 4, 20, 12),
            child: Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                gradient: AppColors.sunGradient,
                borderRadius: BorderRadius.circular(AppRadius.lg),
                boxShadow: AppShadows.soft,
              ),
              child: Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('可用积分', style: TextStyle(color: AppColors.deepGreen, fontSize: 12)),
                        Text('${user?.points ?? 0}',
                            style: const TextStyle(
                                color: AppColors.deepGreen, fontSize: 34, fontWeight: FontWeight.w900)),
                      ],
                    ),
                  ),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Text('累计获得 +$earned',
                          style: const TextStyle(color: AppColors.deepGreen, fontWeight: FontWeight.w700, fontSize: 13)),
                      const SizedBox(height: 4),
                      Text('累计消耗 -$spent',
                          style: const TextStyle(color: AppColors.deepGreen, fontWeight: FontWeight.w700, fontSize: 13)),
                    ],
                  ),
                ],
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: Row(
              children: ['全部', '收入', '支出'].asMap().entries.map((e) {
                final sel = _filter == e.key;
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: ChoiceChip(
                    label: Text(e.value),
                    selected: sel,
                    showCheckmark: false,
                    onSelected: (_) => setState(() => _filter = e.key),
                    labelStyle: TextStyle(color: sel ? Colors.white : AppColors.textDark, fontSize: 13),
                  ),
                );
              }).toList(),
            ),
          ),
          Expanded(
            child: _loading
                ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
                : _error != null
                    ? ErrorState(message: _error!, onRetry: _load)
                    : _filtered.isEmpty
                        ? const EmptyState(icon: Icons.receipt_rounded, title: '暂无记录')
                        : ListView.separated(
                            padding: const EdgeInsets.fromLTRB(20, 12, 20, 30),
                            itemCount: _filtered.length,
                            separatorBuilder: (_, __) => const SizedBox(height: 10),
                            itemBuilder: (c, i) {
                              final r = _filtered[i];
                              final earn = r.amount > 0;
                              return SoftCard(
                                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                                child: Row(
                                  children: [
                                    IconBadge(
                                      icon: earn ? Icons.add_circle_rounded : Icons.remove_circle_rounded,
                                      color: earn ? AppColors.forest : AppColors.coral,
                                      size: 42,
                                      iconSize: 22,
                                      gradient: false,
                                    ),
                                    const SizedBox(width: 12),
                                    Expanded(
                                      child: Column(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          Text(r.title,
                                              style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14),
                                              maxLines: 1,
                                              overflow: TextOverflow.ellipsis),
                                          const SizedBox(height: 3),
                                          Text(r.createdAt.length >= 16 ? r.createdAt.substring(0, 16) : r.createdAt,
                                              style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                                        ],
                                      ),
                                    ),
                                    Text('${earn ? '+' : ''}${r.amount}',
                                        style: TextStyle(
                                            fontSize: 17,
                                            fontWeight: FontWeight.w900,
                                            color: earn ? AppColors.forest : AppColors.coral)),
                                  ],
                                ),
                              );
                            },
                          ),
          ),
        ],
      ),
    );
  }
}
