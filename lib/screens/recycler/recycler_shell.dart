import 'package:flutter/material.dart';

import '../../theme/app_theme.dart';
import 'recycler_hall_screen.dart';
import 'recycler_profile_screen.dart';
import 'recycler_tasks_screen.dart';

/// 回收员端主框架：接单大厅 / 我的任务 / 我的
class RecyclerShell extends StatefulWidget {
  const RecyclerShell({super.key});

  @override
  State<RecyclerShell> createState() => _RecyclerShellState();
}

class _RecyclerShellState extends State<RecyclerShell> {
  int _index = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _index,
        children: [
          RecyclerHallScreen(onGoTasks: () => setState(() => _index = 1)),
          const RecyclerTasksScreen(),
          const RecyclerProfileScreen(),
        ],
      ),
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
          boxShadow: [
            BoxShadow(
              color: AppColors.deepGreen.withValues(alpha: 0.10),
              blurRadius: 20,
              offset: const Offset(0, -4),
            ),
          ],
        ),
        child: ClipRRect(
          borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
          child: NavigationBar(
            selectedIndex: _index,
            onDestinationSelected: (i) => setState(() => _index = i),
            backgroundColor: Colors.white,
            indicatorColor: AppColors.lime.withValues(alpha: 0.3),
            height: 66,
            destinations: const [
              NavigationDestination(
                  icon: Icon(Icons.storefront_outlined),
                  selectedIcon: Icon(Icons.storefront_rounded, color: AppColors.deepGreen),
                  label: '接单大厅'),
              NavigationDestination(
                  icon: Icon(Icons.assignment_outlined),
                  selectedIcon: Icon(Icons.assignment_rounded, color: AppColors.deepGreen),
                  label: '我的任务'),
              NavigationDestination(
                  icon: Icon(Icons.person_outline_rounded),
                  selectedIcon: Icon(Icons.person_rounded, color: AppColors.deepGreen),
                  label: '我的'),
            ],
          ),
        ),
      ),
    );
  }
}
