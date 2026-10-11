import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';

/// 管理后台：数据概览
class AdminDashboardScreen extends StatefulWidget {
  const AdminDashboardScreen({super.key});

  @override
  State<AdminDashboardScreen> createState() => _AdminDashboardScreenState();
}

class _AdminDashboardScreenState extends State<AdminDashboardScreen> {
  AdminDashboard? _d;
  bool _loading = true;
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
      _d = await ApiService.instance.adminDashboard(context.read<AppProvider>().userId);
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  static const _catColors = {
    '纸类': AppColors.blue, '塑料': AppColors.orange, '金属': AppColors.purple, '旧衣物': AppColors.pink,
    '电子产品': AppColors.teal, '玻璃': AppColors.green, '大家电': Color(0xFF5C6BC0), '书籍': Color(0xFF8D6E63),
  };

  @override
  Widget build(BuildContext context) {
    if (_loading && _d == null) {
      return const Center(child: CircularProgressIndicator(color: AppColors.forest));
    }
    if (_error != null && _d == null) return ErrorState(message: _error!, onRetry: _load);
    final d = _d!;
    return RefreshIndicator(
      color: AppColors.forest,
      onRefresh: _load,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // 今日
          Container(
            padding: const EdgeInsets.all(18),
            decoration: BoxDecoration(
              gradient: AppColors.headerGradient,
              borderRadius: BorderRadius.circular(AppRadius.lg),
              boxShadow: AppShadows.soft,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(children: [
                  const Icon(Icons.today_rounded, color: AppColors.lime, size: 18),
                  const SizedBox(width: 6),
                  const Text('今日实时', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w800)),
                  const Spacer(),
                  Text('下拉刷新', style: TextStyle(color: Colors.white.withValues(alpha: 0.7), fontSize: 11)),
                ]),
                const SizedBox(height: 14),
                Row(children: [
                  _todayStat('新增订单', '${d.todayNewOrders}'),
                  _todayStat('完成订单', '${d.todayOrdersCompleted}'),
                  _todayStat('回收重量', '${d.todayWeight.toStringAsFixed(1)}kg'),
                  _todayStat('结算金额', '¥${d.todayAmount.toStringAsFixed(0)}'),
                ]),
              ],
            ),
          ),
          const SizedBox(height: 14),
          // KPI 网格
          GridView.count(
            crossAxisCount: 2,
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            mainAxisSpacing: 12,
            crossAxisSpacing: 12,
            childAspectRatio: 1.75,
            children: [
              _kpi('注册用户', '${d.users}', '本月新增 ${d.newUsersMonth}', Icons.people_rounded, AppColors.blue),
              _kpi('回收员', '${d.recyclers}', '在岗服务中', Icons.electric_moped_rounded, AppColors.teal),
              _kpi('累计订单', '${d.ordersTotal}', '已完成', Icons.receipt_long_rounded, AppColors.purple),
              _kpi('累计回收', '${d.weightTotal.toStringAsFixed(1)} kg', '结算 ¥${d.amountTotal.toStringAsFixed(0)}', Icons.scale_rounded, AppColors.orange),
              _kpi('累计减碳', '${d.carbonTotal.toStringAsFixed(1)} kg', '≈ ${(d.carbonTotal / 18.3).toStringAsFixed(1)} 棵树', Icons.eco_rounded, AppColors.forest),
              _kpi('积分兑换', '${d.redemptionsTotal}', '待发放 ${d.pendingRedemptions} · 消耗 ${d.redemptionsPoints} 分', Icons.card_giftcard_rounded, AppColors.pink),
            ],
          ),
          const SizedBox(height: 14),
          // 订单状态
          SoftCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('订单状态分布', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                const SizedBox(height: 14),
                Row(
                  children: [
                    for (final s in ['pending', 'accepted', 'in_progress', 'completed', 'cancelled'])
                      Expanded(
                        child: Column(children: [
                          Container(
                            width: 40, height: 40,
                            decoration: BoxDecoration(color: statusColor(s).withValues(alpha: 0.14), shape: BoxShape.circle),
                            child: Center(
                              child: Text('${d.statusCounts[s] ?? 0}',
                                  style: TextStyle(fontWeight: FontWeight.w900, color: statusColor(s), fontSize: 15)),
                            ),
                          ),
                          const SizedBox(height: 6),
                          Text(_statusLabel(s), style: const TextStyle(fontSize: 10, color: AppColors.textGrey)),
                        ]),
                      ),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 14),
          // 趋势
          SoftCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('近 14 天回收趋势（kg）', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                const SizedBox(height: 14),
                SizedBox(height: 140, child: _Bars(data: d.trend)),
              ],
            ),
          ),
          const SizedBox(height: 14),
          // 品类
          SoftCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('品类回收占比', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                const SizedBox(height: 12),
                ...d.byCategory.map((c) {
                  final name = c['name']?.toString() ?? '';
                  final w = (c['weight'] as num?)?.toDouble() ?? 0;
                  final ratio = d.weightTotal == 0 ? 0.0 : (w / d.weightTotal).clamp(0.0, 1.0);
                  final color = _catColors[name] ?? AppColors.forest;
                  return Padding(
                    padding: const EdgeInsets.only(bottom: 10),
                    child: Row(children: [
                      SizedBox(width: 56, child: Text(name, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600))),
                      Expanded(
                        child: ClipRRect(
                          borderRadius: BorderRadius.circular(5),
                          child: LinearProgressIndicator(
                              value: ratio, minHeight: 9, backgroundColor: AppColors.bg, valueColor: AlwaysStoppedAnimation(color)),
                        ),
                      ),
                      const SizedBox(width: 8),
                      SizedBox(
                          width: 86,
                          child: Text('${w.toStringAsFixed(1)}kg · ${c['orders']}单',
                              textAlign: TextAlign.end,
                              style: TextStyle(fontSize: 11, color: color, fontWeight: FontWeight.w700))),
                    ]),
                  );
                }),
              ],
            ),
          ),
          const SizedBox(height: 14),
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(child: _rank('回收员排行', d.topRecyclers, (m) => '${m['orders']} 单 · ${(m['weight'] as num?)?.toStringAsFixed(1) ?? 0}kg')),
              const SizedBox(width: 12),
              Expanded(child: _rank('用户贡献榜', d.topUsers, (m) => '${(m['total_weight'] as num?)?.toStringAsFixed(1) ?? 0}kg · 减碳 ${(m['total_carbon'] as num?)?.toStringAsFixed(0) ?? 0}')),
            ],
          ),
          const SizedBox(height: 20),
        ],
      ),
    );
  }

  Widget _todayStat(String l, String v) => Expanded(
        child: Column(children: [
          Text(v, style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w900)),
          const SizedBox(height: 2),
          Text(l, style: const TextStyle(color: Colors.white70, fontSize: 10)),
        ]),
      );

  Widget _kpi(String title, String value, String sub, IconData icon, Color color) => SoftCard(
        padding: const EdgeInsets.all(14),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(title, style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
                  const SizedBox(height: 4),
                  FittedBox(
                    fit: BoxFit.scaleDown,
                    alignment: Alignment.centerLeft,
                    child: Text(value, style: TextStyle(fontSize: 20, fontWeight: FontWeight.w900, color: color)),
                  ),
                  const SizedBox(height: 2),
                  Text(sub, style: const TextStyle(fontSize: 10, color: AppColors.textGrey), maxLines: 1, overflow: TextOverflow.ellipsis),
                ],
              ),
            ),
            IconBadge(icon: icon, color: color, size: 40, iconSize: 20),
          ],
        ),
      );

  Widget _rank(String title, List<Map<String, dynamic>> list, String Function(Map<String, dynamic>) sub) => SoftCard(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 14)),
            const SizedBox(height: 10),
            if (list.isEmpty) const Text('暂无数据', style: TextStyle(color: AppColors.textGrey, fontSize: 12)),
            ...list.asMap().entries.map((e) {
              final m = e.value;
              final medal = [AppColors.amber, const Color(0xFF9E9E9E), const Color(0xFFCD7F32)];
              return Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: Row(children: [
                  Container(
                    width: 18, height: 18,
                    decoration: BoxDecoration(
                        color: e.key < 3 ? medal[e.key] : AppColors.divider, shape: BoxShape.circle),
                    child: Center(
                        child: Text('${e.key + 1}',
                            style: const TextStyle(fontSize: 10, color: Colors.white, fontWeight: FontWeight.w900))),
                  ),
                  const SizedBox(width: 6),
                  Avatar(m['avatar']?.toString() ?? '', size: 24),
                  const SizedBox(width: 6),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(m['nickname']?.toString() ?? '', style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700), maxLines: 1, overflow: TextOverflow.ellipsis),
                        Text(sub(m), style: const TextStyle(fontSize: 10, color: AppColors.textGrey), maxLines: 1, overflow: TextOverflow.ellipsis),
                      ],
                    ),
                  ),
                ]),
              );
            }),
          ],
        ),
      );

  String _statusLabel(String s) {
    switch (s) {
      case 'pending':
        return '待接单';
      case 'accepted':
        return '已接单';
      case 'in_progress':
        return '上门中';
      case 'completed':
        return '已完成';
      default:
        return '已取消';
    }
  }
}

class _Bars extends StatelessWidget {
  final List<Map<String, dynamic>> data;
  const _Bars({required this.data});

  @override
  Widget build(BuildContext context) {
    if (data.isEmpty) return const Center(child: Text('近 14 天暂无完成订单', style: TextStyle(color: AppColors.textGrey, fontSize: 12)));
    final maxW = data.map((e) => (e['weight'] as num?)?.toDouble() ?? 0).fold(0.0, (a, b) => a > b ? a : b);
    return Row(
      crossAxisAlignment: CrossAxisAlignment.end,
      children: data.map((e) {
        final w = (e['weight'] as num?)?.toDouble() ?? 0;
        final h = maxW == 0 ? 0.0 : w / maxW;
        final day = (e['day']?.toString() ?? '').split('-').last;
        return Expanded(
          child: Tooltip(
            message: '${e['day']}\n${w.toStringAsFixed(1)} kg · ${e['orders']} 单',
            child: Column(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                Text(w.toStringAsFixed(0), style: const TextStyle(fontSize: 9, color: AppColors.textGrey)),
                const SizedBox(height: 3),
                Container(
                  height: 90 * h + 4,
                  margin: const EdgeInsets.symmetric(horizontal: 3),
                  decoration: BoxDecoration(gradient: AppColors.limeGradient, borderRadius: BorderRadius.circular(5)),
                ),
                const SizedBox(height: 4),
                Text(day, style: const TextStyle(fontSize: 9, color: AppColors.textGrey)),
              ],
            ),
          ),
        );
      }).toList(),
    );
  }
}
