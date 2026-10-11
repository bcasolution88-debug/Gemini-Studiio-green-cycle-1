import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';

/// 减碳报告
class CarbonReportScreen extends StatefulWidget {
  const CarbonReportScreen({super.key});

  @override
  State<CarbonReportScreen> createState() => _CarbonReportScreenState();
}

class _CarbonReportScreenState extends State<CarbonReportScreen> {
  UserStats? _stats;
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
      _stats = await ApiService.instance.getStats(context.read<AppProvider>().userId);
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  static const _catColors = {
    '纸类': AppColors.blue,
    '塑料': AppColors.orange,
    '金属': AppColors.purple,
    '旧衣物': AppColors.pink,
    '电子产品': AppColors.teal,
    '玻璃': AppColors.green,
    '大家电': Color(0xFF5C6BC0),
    '书籍': Color(0xFF8D6E63),
  };

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('减碳报告')),
      body: _loading
          ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
          : _error != null
              ? ErrorState(message: _error!, onRetry: _load)
              : _body(_stats!),
    );
  }

  Widget _body(UserStats s) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 4, 20, 30),
      children: [
        // 总览
        Container(
          padding: const EdgeInsets.all(22),
          decoration: BoxDecoration(
            gradient: AppColors.headerGradient,
            borderRadius: BorderRadius.circular(AppRadius.lg),
            boxShadow: AppShadows.soft,
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(children: [
                const Icon(Icons.eco_rounded, color: AppColors.lime, size: 22),
                const SizedBox(width: 6),
                Text('累计减碳', style: TextStyle(color: Colors.white.withValues(alpha: 0.9))),
              ]),
              const SizedBox(height: 6),
              Row(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(s.totalCarbon.toStringAsFixed(1),
                      style: const TextStyle(color: Colors.white, fontSize: 44, fontWeight: FontWeight.w900, height: 1)),
                  const Padding(
                    padding: EdgeInsets.only(bottom: 6, left: 6),
                    child: Text('kg CO₂', style: TextStyle(color: Colors.white70, fontSize: 14)),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.white.withValues(alpha: 0.14),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.park_rounded, color: AppColors.lime, size: 30),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text('相当于种植了 ${s.treesEquivalent.toStringAsFixed(1)} 棵树一年的固碳量',
                          style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w700, fontSize: 13)),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),

        // 本月
        Row(
          children: [
            _miniStat('本月回收', '${s.monthWeight.toStringAsFixed(1)} kg', Icons.scale_rounded, AppColors.blue),
            const SizedBox(width: 12),
            _miniStat('本月减碳', '${s.monthCarbon.toStringAsFixed(1)} kg', Icons.eco_rounded, AppColors.forest),
            const SizedBox(width: 12),
            _miniStat('本月积分', '+${s.monthPoints}', Icons.monetization_on_rounded, AppColors.amber),
          ],
        ),
        const SizedBox(height: 16),

        // 趋势
        SoftCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('回收趋势（近 6 个月）', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
              const SizedBox(height: 16),
              SizedBox(height: 150, child: _BarChart(data: s.trend)),
            ],
          ),
        ),
        const SizedBox(height: 16),

        // 品类分布
        SoftCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('品类分布', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
              const SizedBox(height: 14),
              if (s.byCategory.isEmpty)
                const Text('暂无数据', style: TextStyle(color: AppColors.textGrey))
              else
                ...s.byCategory.map((c) {
                  final name = c['name']?.toString() ?? '';
                  final w = (c['weight'] as num?)?.toDouble() ?? 0;
                  final ratio = s.totalWeight == 0 ? 0.0 : (w / s.totalWeight).clamp(0.0, 1.0);
                  final color = _catColors[name] ?? AppColors.forest;
                  return Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: Row(
                      children: [
                        SizedBox(width: 58, child: Text(name, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600))),
                        Expanded(
                          child: ClipRRect(
                            borderRadius: BorderRadius.circular(6),
                            child: LinearProgressIndicator(
                              value: ratio,
                              minHeight: 10,
                              backgroundColor: AppColors.bg,
                              valueColor: AlwaysStoppedAnimation(color),
                            ),
                          ),
                        ),
                        const SizedBox(width: 10),
                        SizedBox(
                          width: 62,
                          child: Text('${w.toStringAsFixed(1)} kg',
                              textAlign: TextAlign.end,
                              style: TextStyle(fontSize: 12, color: color, fontWeight: FontWeight.w800)),
                        ),
                      ],
                    ),
                  );
                }),
            ],
          ),
        ),
      ],
    );
  }

  Widget _miniStat(String l, String v, IconData i, Color c) => Expanded(
        child: SoftCard(
          padding: const EdgeInsets.all(14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Icon(i, color: c, size: 22),
              const SizedBox(height: 8),
              Text(v, style: TextStyle(fontSize: 15, fontWeight: FontWeight.w900, color: c)),
              const SizedBox(height: 2),
              Text(l, style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
            ],
          ),
        ),
      );
}

class _BarChart extends StatelessWidget {
  final List<Map<String, dynamic>> data;
  const _BarChart({required this.data});

  @override
  Widget build(BuildContext context) {
    if (data.isEmpty) {
      return const Center(child: Text('暂无数据', style: TextStyle(color: AppColors.textGrey)));
    }
    final maxW = data.map((e) => (e['weight'] as num?)?.toDouble() ?? 0).fold(0.0, (a, b) => a > b ? a : b);
    return Row(
      crossAxisAlignment: CrossAxisAlignment.end,
      children: data.map((e) {
        final w = (e['weight'] as num?)?.toDouble() ?? 0;
        final h = maxW == 0 ? 0.0 : (w / maxW);
        final month = (e['month']?.toString() ?? '').split('-').last;
        return Expanded(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              Text(w.toStringAsFixed(1), style: const TextStyle(fontSize: 10, color: AppColors.textGrey)),
              const SizedBox(height: 4),
              Container(
                height: 90 * h + 6,
                margin: const EdgeInsets.symmetric(horizontal: 8),
                decoration: BoxDecoration(
                  gradient: AppColors.limeGradient,
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              const SizedBox(height: 6),
              Text('$month月', style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
            ],
          ),
        );
      }).toList(),
    );
  }
}
