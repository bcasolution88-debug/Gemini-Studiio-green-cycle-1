import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../main.dart' show openNotificationTarget;
import '../../providers/locale_provider.dart';
import '../../widgets/common_widgets.dart';
import '../notifications/notifications_screen.dart';
import '../recycle/address_screen.dart';
import '../settings/server_settings_screen.dart';
import '../shop/redemptions_screen.dart';
import 'carbon_report_screen.dart';
import 'points_history_screen.dart';

/// 我的
class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  UserStats? _stats;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    try {
      final app = context.read<AppProvider>();
      await app.refreshUser();
      _stats = await ApiService.instance.getStats(app.userId);
    } catch (_) {
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _editNickname() async {
    final app = context.read<AppProvider>();
    final ctrl = TextEditingController(text: app.user?.nickname ?? '');
    final v = await showDialog<String>(
      context: context,
      builder: (c) => AlertDialog(
        title: const Text('修改昵称'),
        content: TextField(controller: ctrl, maxLength: 20, autofocus: true),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c), child: const Text('取消')),
          ElevatedButton(onPressed: () => Navigator.pop(c, ctrl.text.trim()), child: const Text('保存')),
        ],
      ),
    );
    if (v != null && v.isNotEmpty) {
      try {
        await app.updateProfile(nickname: v);
        if (mounted) showToast(context, '昵称已更新');
      } catch (e) {
        if (mounted) showToast(context, e.toString(), error: true);
      }
    }
  }

  Future<void> _logout() async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: const Text('退出登录'),
        content: const Text('确定要退出当前账号吗？'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('取消')),
          TextButton(
              onPressed: () => Navigator.pop(c, true),
              child: const Text('退出', style: TextStyle(color: AppColors.coral))),
        ],
      ),
    );
    if (ok == true && mounted) context.read<AppProvider>().logout();
  }

  @override
  Widget build(BuildContext context) {
    final user = context.watch<AppProvider>().user;
    final topPad = MediaQuery.of(context).padding.top;
    if (user == null) return const SizedBox.shrink();
    return Scaffold(
      body: RefreshIndicator(
        color: AppColors.forest,
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.only(bottom: 110),
          children: [
            // 头部
            Stack(
              clipBehavior: Clip.none,
              children: [
                Container(
                  height: 230 + topPad,
                  decoration: const BoxDecoration(
                    gradient: AppColors.headerGradient,
                    borderRadius: BorderRadius.vertical(bottom: Radius.circular(48)),
                  ),
                ),
                Positioned(
                  right: -30,
                  top: 40,
                  child: Container(
                    width: 150,
                    height: 150,
                    decoration: BoxDecoration(
                        color: AppColors.lime.withValues(alpha: 0.15), shape: BoxShape.circle),
                  ),
                ),
                Padding(
                  padding: EdgeInsets.fromLTRB(20, topPad + 14, 20, 0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          const Text('我的',
                              style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.w900)),
                          const Spacer(),
                          IconButton(
                            onPressed: () => Navigator.push(context,
                                MaterialPageRoute(builder: (_) => const ServerSettingsScreen())),
                            icon: const Icon(Icons.settings_rounded, color: Colors.white),
                          ),
                        ],
                      ),
                      const SizedBox(height: 14),
                      Row(
                        children: [
                          Avatar(user.avatar, size: 72, border: AppColors.lime),
                          const SizedBox(width: 16),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                GestureDetector(
                                  onTap: _editNickname,
                                  child: Row(
                                    children: [
                                      Text(user.nickname,
                                          style: const TextStyle(
                                              color: Colors.white, fontSize: 20, fontWeight: FontWeight.w900)),
                                      const SizedBox(width: 6),
                                      const Icon(Icons.edit_rounded, color: Colors.white70, size: 16),
                                    ],
                                  ),
                                ),
                                const SizedBox(height: 6),
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                                  decoration: BoxDecoration(
                                    gradient: AppColors.sunGradient,
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  child: Text('${user.levelName} Lv.${user.level}',
                                      style: const TextStyle(
                                          fontSize: 12, fontWeight: FontWeight.w800, color: AppColors.deepGreen)),
                                ),
                                const SizedBox(height: 6),
                                Text(user.phone.replaceRange(3, 7, '****'),
                                    style: TextStyle(color: Colors.white.withValues(alpha: 0.8), fontSize: 12)),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                Positioned(
                  left: 20,
                  right: 20,
                  top: topPad + 170,
                  child: _statCard(user),
                ),
              ],
            ),
            const SizedBox(height: 60),

            // 成就
            SectionHeader(
              title: '环保成就',
              action: _stats == null ? null : '${_stats!.achievements.where((a) => a.unlocked).length}/${_stats!.achievements.length}',
            ),
            if (_loading && _stats == null)
              const Padding(
                padding: EdgeInsets.all(20),
                child: Center(child: CircularProgressIndicator(color: AppColors.forest)),
              )
            else if (_stats != null)
              SizedBox(
                height: 118,
                child: ListView.separated(
                  scrollDirection: Axis.horizontal,
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  itemCount: _stats!.achievements.length,
                  separatorBuilder: (_, __) => const SizedBox(width: 10),
                  itemBuilder: (c, i) => _AchievementBadge(a: _stats!.achievements[i]),
                ),
              ),

            // 菜单
            const SectionHeader(title: '我的服务'),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: SoftCard(
                padding: const EdgeInsets.symmetric(vertical: 6),
                child: Column(
                  children: [
                    _menu(Icons.bar_chart_rounded, AppColors.forest, '减碳报告', '查看回收趋势与环保贡献',
                        () => Navigator.push(context, MaterialPageRoute(builder: (_) => const CarbonReportScreen()))),
                    _menu(Icons.monetization_on_rounded, AppColors.amber, '积分明细', '收支记录一目了然',
                        () => Navigator.push(context, MaterialPageRoute(builder: (_) => const PointsHistoryScreen()))),
                    _menu(Icons.card_giftcard_rounded, AppColors.pink, '兑换记录', '查看兑换码与发货状态',
                        () => Navigator.push(context, MaterialPageRoute(builder: (_) => const RedemptionsScreen()))),
                    _menu(Icons.location_on_rounded, AppColors.coral, '地址管理', '上门回收地址',
                        () => Navigator.push(context, MaterialPageRoute(builder: (_) => const AddressScreen()))),
                    _menu(Icons.notifications_rounded, AppColors.teal, '消息中心', '订单进度、积分到账提醒',
                        () => Navigator.push(context,
                            MaterialPageRoute(builder: (_) => NotificationsScreen(onOpen: (n) => openNotificationTarget(context, n))))),
                    _menu(Icons.translate_rounded, AppColors.purple, '语言 / Language',
                        context.watch<LocaleProvider>().isZh ? '简体中文 · 点击切换 English' : 'English · Tap to switch 中文',
                        () => context.read<LocaleProvider>().toggle()),
                    _menu(Icons.dns_rounded, AppColors.blue, '后端服务设置', '切换 API 地址',
                        () => Navigator.push(context, MaterialPageRoute(builder: (_) => const ServerSettingsScreen())),
                        last: true),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: OutlinedButton(
                onPressed: _logout,
                style: OutlinedButton.styleFrom(
                  foregroundColor: AppColors.coral,
                  side: BorderSide(color: AppColors.coral.withValues(alpha: 0.4)),
                  padding: const EdgeInsets.symmetric(vertical: 14),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.lg)),
                ),
                child: const Text('退出登录', style: TextStyle(fontWeight: FontWeight.w700)),
              ),
            ),
            const SizedBox(height: 10),
            const Center(
              child: Text('绿循环 Green Cycle v1.0.0',
                  style: TextStyle(color: AppColors.textGrey, fontSize: 11)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _statCard(User u) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 18, horizontal: 10),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(AppRadius.lg),
        boxShadow: AppShadows.soft,
      ),
      child: Row(
        children: [
          _stat('${u.points}', '可用积分', AppColors.amber),
          _divider(),
          _stat(u.totalWeight.toStringAsFixed(1), '回收 kg', AppColors.blue),
          _divider(),
          _stat(u.totalCarbon.toStringAsFixed(1), '减碳 kg', AppColors.forest),
          _divider(),
          _stat('${_stats?.orderCount ?? '-'}', '回收次数', AppColors.purple),
        ],
      ),
    );
  }

  Widget _divider() => Container(width: 1, height: 32, color: AppColors.divider);

  Widget _stat(String v, String l, Color c) => Expanded(
        child: Column(
          children: [
            Text(v, style: TextStyle(fontSize: 18, fontWeight: FontWeight.w900, color: c)),
            const SizedBox(height: 2),
            Text(l, style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
          ],
        ),
      );

  Widget _menu(IconData icon, Color color, String title, String sub, VoidCallback onTap, {bool last = false}) {
    return Column(
      children: [
        ListTile(
          onTap: onTap,
          leading: IconBadge(icon: icon, color: color, size: 42, iconSize: 20),
          title: Text(title, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 15)),
          subtitle: Text(sub, style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
          trailing: const Icon(Icons.chevron_right_rounded, color: AppColors.textGrey),
        ),
        if (!last) const Divider(height: 1, indent: 72, endIndent: 16),
      ],
    );
  }
}

class _AchievementBadge extends StatelessWidget {
  final Achievement a;
  const _AchievementBadge({required this.a});

  @override
  Widget build(BuildContext context) {
    final color = a.unlocked ? AppColors.amber : AppColors.textGrey;
    return Tooltip(
      message: '${a.description}\n进度 ${a.current.toStringAsFixed(0)}/${a.conditionValue.toStringAsFixed(0)}',
      child: Container(
        width: 96,
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 8),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(AppRadius.md),
          boxShadow: AppShadows.card,
        ),
        child: Column(
          children: [
            Container(
              width: 46,
              height: 46,
              decoration: BoxDecoration(
                gradient: a.unlocked ? AppColors.sunGradient : null,
                color: a.unlocked ? null : AppColors.bg,
                shape: BoxShape.circle,
              ),
              child: Icon(achievementIcon(a.icon), color: a.unlocked ? Colors.white : AppColors.divider, size: 24),
            ),
            const SizedBox(height: 8),
            Text(a.name,
                style: TextStyle(fontSize: 12, fontWeight: FontWeight.w700, color: a.unlocked ? AppColors.textDark : color),
                maxLines: 1,
                overflow: TextOverflow.ellipsis),
            const SizedBox(height: 6),
            ClipRRect(
              borderRadius: BorderRadius.circular(3),
              child: LinearProgressIndicator(
                value: a.progress,
                minHeight: 4,
                backgroundColor: AppColors.bg,
                valueColor: AlwaysStoppedAnimation(a.unlocked ? AppColors.amber : AppColors.green),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
