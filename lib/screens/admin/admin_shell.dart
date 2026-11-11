import 'package:flutter/material.dart';

import '../../l10n/app_localizations.dart';
import 'package:provider/provider.dart';

import '../../main.dart' show openNotificationTarget;
import '../../providers/app_provider.dart';
import '../../providers/notification_provider.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';
import '../notifications/notifications_screen.dart';
import 'admin_dashboard_screen.dart';
import 'admin_finance_screen.dart';
import 'admin_map_screen.dart';
import 'admin_operations_screen.dart';
import 'admin_orders_screen.dart';
import 'admin_users_screen.dart';

/// 桌面断点：≥ 此宽度切换为「左侧导航栏 + 居中内容区」的后台布局
const double kAdminDesktopBreakpoint = 1000;

/// 内容区最大宽度（桌面）：避免表格/卡片在超宽屏上过度拉伸
const double kAdminContentMaxWidth = 1180;

/// 管理后台主框架：数据概览 / 订单管理 / 用户管理 / 运营管理 / 财务导出
///
/// - 手机（< 1000px）：顶部 AppBar + 底部 NavigationBar（原布局）
/// - 桌面（≥ 1000px）：左侧 NavigationRail（含品牌区、地图、消息、退出）+ 内容最大宽度 1180
class AdminShell extends StatefulWidget {
  const AdminShell({super.key});

  @override
  State<AdminShell> createState() => _AdminShellState();
}

class _AdminShellState extends State<AdminShell> {
  int _index = 0;

  static const _titles = ['数据概览', '订单管理', '用户管理', '运营管理', '财务导出'];
  static const _icons = [
    (Icons.dashboard_outlined, Icons.dashboard_rounded, '概览'),
    (Icons.receipt_long_outlined, Icons.receipt_long_rounded, '订单'),
    (Icons.people_outline_rounded, Icons.people_rounded, '用户'),
    (Icons.tune_outlined, Icons.tune_rounded, '运营'),
    (Icons.account_balance_outlined, Icons.account_balance_rounded, '财务'),
  ];

  static const _pages = [
    AdminDashboardScreen(),
    AdminOrdersScreen(),
    AdminUsersScreen(),
    AdminOperationsScreen(),
    AdminFinanceScreen(),
  ];

  void _openMap() => Navigator.push(
    context,
    MaterialPageRoute(builder: (_) => const AdminMapScreen()),
  );

  void _openNotifications() => Navigator.push(
    context,
    MaterialPageRoute(
      builder: (_) => NotificationsScreen(
        onOpen: (n) => openNotificationTarget(context, n),
      ),
    ),
  );

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppProvider>();
    final isDesktop =
        MediaQuery.sizeOf(context).width >= kAdminDesktopBreakpoint;
    return isDesktop ? _buildDesktop(app) : _buildMobile(app);
  }

  // ---------------- 手机布局 ----------------
  Widget _buildMobile(AppProvider app) {
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
              child: Image.asset(
                'assets/icons/app_icon.png',
                width: 28,
                height: 28,
              ),
            ),
            const SizedBox(width: 10),
            Text(
              '${context.tr('绿循环')} ${context.tr('管理后台')} · ${context.tr(_titles[_index])}',
              style: const TextStyle(
                color: Colors.white,
                fontSize: 16,
                fontWeight: FontWeight.w800,
              ),
            ),
          ],
        ),
        actions: [
          IconButton(
            tooltip: context.tr('回收员地图'),
            onPressed: _openMap,
            icon: const Icon(Icons.map_rounded),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4),
            child: Center(
              child: NotificationBell(
                background: Colors.white.withValues(alpha: 0.12),
                onTap: _openNotifications,
              ),
            ),
          ),
          IconButton(
            tooltip: context.tr('退出登录'),
            onPressed: () => app.logout(),
            icon: const Icon(Icons.logout_rounded),
          ),
        ],
      ),
      body: IndexedStack(index: _index, children: _pages),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        backgroundColor: Colors.white,
        indicatorColor: AppColors.lime.withValues(alpha: 0.3),
        destinations: [
          for (final (o, f, l) in _icons)
            NavigationDestination(
              icon: Icon(o),
              selectedIcon: Icon(f, color: AppColors.deepGreen),
              label: context.tr(l),
            ),
        ],
      ),
    );
  }

  // ---------------- 桌面布局 ----------------
  Widget _buildDesktop(AppProvider app) {
    final user = app.user;
    return Scaffold(
      backgroundColor: const Color(0xFFF1F4F2),
      body: Row(
        children: [
          // 左侧导航栏
          Container(
            width: 232,
            decoration: const BoxDecoration(gradient: AppColors.headerGradient),
            child: SafeArea(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Padding(
                    padding: const EdgeInsets.fromLTRB(20, 22, 20, 18),
                    child: Row(
                      children: [
                        ClipRRect(
                          borderRadius: BorderRadius.circular(10),
                          child: Image.asset(
                            'assets/icons/app_icon.png',
                            width: 40,
                            height: 40,
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                context.tr('绿循环'),
                                style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 17,
                                  fontWeight: FontWeight.w900,
                                ),
                              ),
                              Text(
                                context.tr('管理后台'),
                                style: TextStyle(
                                  color: AppColors.onDarkSoft,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                  const Divider(
                    color: Colors.white24,
                    height: 1,
                    indent: 16,
                    endIndent: 16,
                  ),
                  const SizedBox(height: 10),
                  for (var i = 0; i < _icons.length; i++)
                    _RailItem(
                      icon: _index == i ? _icons[i].$2 : _icons[i].$1,
                      label: context.tr(_titles[i]),
                      selected: _index == i,
                      onTap: () => setState(() => _index = i),
                    ),
                  const SizedBox(height: 10),
                  const Divider(
                    color: Colors.white24,
                    height: 1,
                    indent: 16,
                    endIndent: 16,
                  ),
                  const SizedBox(height: 10),
                  _RailItem(
                    icon: Icons.map_rounded,
                    label: context.tr('回收员实时地图'),
                    selected: false,
                    onTap: _openMap,
                  ),
                  _RailItem(
                    icon: Icons.notifications_rounded,
                    label: context.tr('消息中心'),
                    selected: false,
                    onTap: _openNotifications,
                    trailing: const _RailBellBadge(),
                  ),
                  const Spacer(),
                  Padding(
                    padding: const EdgeInsets.fromLTRB(16, 8, 16, 16),
                    child: Row(
                      children: [
                        Avatar(
                          user?.avatar ?? '',
                          size: 36,
                          border: AppColors.lime,
                          name: user?.nickname,
                        ),
                        const SizedBox(width: 10),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                user?.nickname ?? '管理员',
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontWeight: FontWeight.w800,
                                  fontSize: 13,
                                ),
                              ),
                              Text(
                                context.tr('超级管理员'),
                                style: TextStyle(
                                  color: AppColors.onDarkSoft,
                                  fontSize: 11,
                                ),
                              ),
                            ],
                          ),
                        ),
                        IconButton(
                          tooltip: context.tr('退出登录'),
                          onPressed: () => app.logout(),
                          icon: const Icon(
                            Icons.logout_rounded,
                            color: Colors.white,
                            size: 20,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          // 内容区
          Expanded(
            child: Column(
              children: [
                // 顶部标题条
                Container(
                  height: 56,
                  padding: const EdgeInsets.symmetric(horizontal: 28),
                  decoration: const BoxDecoration(
                    color: Colors.white,
                    border: Border(
                      bottom: BorderSide(color: AppColors.divider),
                    ),
                  ),
                  child: Row(
                    children: [
                      Text(
                        context.tr(_titles[_index]),
                        style: const TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w900,
                          color: AppColors.textDark,
                        ),
                      ),
                      const SizedBox(width: 12),
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 10,
                          vertical: 4,
                        ),
                        decoration: BoxDecoration(
                          color: AppColors.lime.withValues(alpha: 0.25),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(
                          context.tr('桌面版'),
                          style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.w800,
                            color: AppColors.deepGreen,
                          ),
                        ),
                      ),
                      const Spacer(),
                      Text(
                        '${MediaQuery.sizeOf(context).width.toInt()} px',
                        style: const TextStyle(
                          fontSize: 11,
                          color: AppColors.textGrey,
                        ),
                      ),
                    ],
                  ),
                ),
                Expanded(
                  child: Align(
                    alignment: Alignment.topCenter,
                    child: ConstrainedBox(
                      constraints: const BoxConstraints(
                        maxWidth: kAdminContentMaxWidth,
                      ),
                      child: IndexedStack(index: _index, children: _pages),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _RailItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool selected;
  final VoidCallback onTap;
  final Widget? trailing;
  const _RailItem({
    required this.icon,
    required this.label,
    required this.selected,
    required this.onTap,
    this.trailing,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 2),
      child: Material(
        color: selected ? AppColors.lime : Colors.transparent,
        borderRadius: BorderRadius.circular(12),
        child: InkWell(
          borderRadius: BorderRadius.circular(12),
          onTap: onTap,
          hoverColor: Colors.white.withValues(alpha: 0.10),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
            child: Row(
              children: [
                Icon(
                  icon,
                  size: 20,
                  color: selected ? AppColors.deepGreen : Colors.white,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    label,
                    style: TextStyle(
                      color: selected ? AppColors.deepGreen : Colors.white,
                      fontWeight: selected ? FontWeight.w900 : FontWeight.w600,
                      fontSize: 14,
                    ),
                  ),
                ),
                if (trailing != null) trailing!,
              ],
            ),
          ),
        ),
      ),
    );
  }
}

/// 导航栏消息未读角标（读取 NotificationProvider.unread）
class _RailBellBadge extends StatelessWidget {
  const _RailBellBadge();

  @override
  Widget build(BuildContext context) {
    final unread = context.select<NotificationProvider, int>((p) => p.unread);
    if (unread <= 0) return const SizedBox.shrink();
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
      decoration: BoxDecoration(
        color: AppColors.coral,
        borderRadius: BorderRadius.circular(10),
      ),
      constraints: const BoxConstraints(minWidth: 20),
      child: Text(
        unread > 99 ? '99+' : '$unread',
        textAlign: TextAlign.center,
        style: const TextStyle(
          color: Colors.white,
          fontSize: 10,
          fontWeight: FontWeight.w900,
        ),
      ),
    );
  }
}
