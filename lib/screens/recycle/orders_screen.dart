import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';
import 'create_order_screen.dart';
import 'order_detail_screen.dart';

/// 我的回收订单
class OrdersScreen extends StatefulWidget {
  const OrdersScreen({super.key});

  @override
  State<OrdersScreen> createState() => _OrdersScreenState();
}

class _OrdersScreenState extends State<OrdersScreen> with SingleTickerProviderStateMixin {
  late final TabController _tab;
  final _api = ApiService.instance;
  List<RecycleOrder> _all = [];
  bool _loading = true;
  String? _error;

  static const _tabs = [
    ('all', '全部'),
    ('pending', '待接单'),
    ('in_progress', '进行中'),
    ('completed', '已完成'),
  ];

  @override
  void initState() {
    super.initState();
    _tab = TabController(length: _tabs.length, vsync: this);
    _tab.addListener(() => setState(() {}));
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
      _all = await _api.getOrders(context.read<AppProvider>().userId);
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  List<RecycleOrder> _filter(String key) {
    switch (key) {
      case 'pending':
        return _all.where((o) => o.status == 'pending').toList();
      case 'in_progress':
        return _all.where((o) => o.status == 'accepted' || o.status == 'in_progress').toList();
      case 'completed':
        return _all.where((o) => o.status == 'completed' || o.status == 'cancelled').toList();
      default:
        return _all;
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
                    const Text('我的订单',
                        style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.w900)),
                    const Spacer(),
                    IconButton(
                        onPressed: _load,
                        icon: const Icon(Icons.refresh_rounded, color: Colors.white)),
                  ],
                ),
                const SizedBox(height: 6),
                TabBar(
                  controller: _tab,
                  isScrollable: false,
                  indicatorColor: AppColors.lime,
                  indicatorWeight: 3,
                  indicatorSize: TabBarIndicatorSize.label,
                  labelColor: Colors.white,
                  unselectedLabelColor: Colors.white60,
                  labelStyle: const TextStyle(fontWeight: FontWeight.w800),
                  dividerColor: Colors.transparent,
                  tabs: _tabs.map((t) => Tab(text: t.$2)).toList(),
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
                        children: _tabs.map((t) {
                          final list = _filter(t.$1);
                          if (list.isEmpty) {
                            return EmptyState(
                              icon: Icons.inbox_rounded,
                              title: '暂无订单',
                              subtitle: '预约上门回收，赚取碳积分',
                              action: ElevatedButton(
                                onPressed: () => Navigator.push(context,
                                    MaterialPageRoute(builder: (_) => const CreateOrderScreen())),
                                child: const Text('去预约'),
                              ),
                            );
                          }
                          return RefreshIndicator(
                            color: AppColors.forest,
                            onRefresh: _load,
                            child: ListView.builder(
                              padding: const EdgeInsets.only(top: 10, bottom: 110),
                              itemCount: list.length,
                              itemBuilder: (c, i) => OrderCard(
                                order: list[i],
                                onTap: () async {
                                  await Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) => OrderDetailScreen(orderId: list[i].id)));
                                  _load();
                                },
                              ),
                            ),
                          );
                        }).toList(),
                      ),
          ),
        ],
      ),
    );
  }
}
