import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../providers/app_provider.dart';
import '../../theme/app_theme.dart';
import 'admin_dashboard_screen.dart';
import 'admin_operations_screen.dart';
import 'admin_orders_screen.dart';
import 'admin_users_screen.dart';

/// 管理后台主框架：数据概览 / 订单管理 / 用户管理 / 运营管理
class AdminShell extends StatefulWidget {
  const AdminShell({super.key});

  @override
  State<AdminShell> createState() => _AdminShellState();
}

class _AdminShellState extends State<AdminShell> {
  int _index = 0;

  static const _titles = ['数据概览', '订单管理', '用户管理', '运营管理'];

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppProvider>();
    return Scaffold(
      backgroundColor: const Color(0xFFF1F4F2),
      appBar: AppBar(
        backgroundColor: AppColors.deepGreen,
        foregroundColor: Colors.white,
        centerTitle: false,
        title: Row(
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: Image.asset('assets/icons/app_icon.png', width: 28, height: 28),
            ),
            const SizedBox(width: 10),
            Text('绿循环管理后台 · ${_titles[_index]}',
                style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.w800)),
          ],
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 4),
            child: Center(
              child: Text(app.user?.nickname ?? '',
                  style: const TextStyle(color: Colors.white70, fontSize: 13)),
            ),
          ),
          IconButton(
            tooltip: '退出登录',
            onPressed: () => app.logout(),
            icon: const Icon(Icons.logout_rounded),
          ),
        ],
      ),
      body: IndexedStack(
        index: _index,
        children: const [
          AdminDashboardScreen(),
          AdminOrdersScreen(),
          AdminUsersScreen(),
          AdminOperationsScreen(),
        ],
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        backgroundColor: Colors.white,
        indicatorColor: AppColors.lime.withValues(alpha: 0.3),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.dashboard_outlined), selectedIcon: Icon(Icons.dashboard_rounded, color: AppColors.deepGreen), label: '概览'),
          NavigationDestination(icon: Icon(Icons.receipt_long_outlined), selectedIcon: Icon(Icons.receipt_long_rounded, color: AppColors.deepGreen), label: '订单'),
          NavigationDestination(icon: Icon(Icons.people_outline_rounded), selectedIcon: Icon(Icons.people_rounded, color: AppColors.deepGreen), label: '用户'),
          NavigationDestination(icon: Icon(Icons.tune_outlined), selectedIcon: Icon(Icons.tune_rounded, color: AppColors.deepGreen), label: '运营'),
        ],
      ),
    );
  }
}
