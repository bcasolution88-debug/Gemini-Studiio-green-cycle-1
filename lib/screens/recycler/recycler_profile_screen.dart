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
import '../settings/server_settings_screen.dart';
import 'recycler_wallet_screen.dart';

/// 回收员：我的
class RecyclerProfileScreen extends StatefulWidget {
  const RecyclerProfileScreen({super.key});

  @override
  State<RecyclerProfileScreen> createState() => _RecyclerProfileScreenState();
}

class _RecyclerProfileScreenState extends State<RecyclerProfileScreen> {
  RecyclerStats? _stats;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    try {
      _stats = await ApiService.instance.recyclerStats(context.read<AppProvider>().userId);
    } catch (_) {
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppProvider>();
    final user = app.user;
    final topPad = MediaQuery.of(context).padding.top;
    if (user == null) return const SizedBox.shrink();
    return Scaffold(
      body: RefreshIndicator(
        color: AppColors.forest,
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.only(bottom: 30),
          children: [
            Container(
              padding: EdgeInsets.fromLTRB(20, topPad + 20, 20, 28),
              decoration: const BoxDecoration(
                gradient: AppColors.headerGradient,
                borderRadius: BorderRadius.vertical(bottom: Radius.circular(40)),
              ),
              child: Row(
                children: [
                  Avatar(user.avatar, size: 72, border: AppColors.lime),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(user.nickname,
                            style: const TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.w900)),
                        const SizedBox(height: 6),
                        Row(
                          children: [
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                              decoration: BoxDecoration(
                                gradient: AppColors.sunGradient,
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: const Text('认证回收员',
                                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.w800, color: AppColors.deepGreen)),
                            ),
                            const SizedBox(width: 8),
                            const Icon(Icons.star_rounded, color: AppColors.yellow, size: 16),
                            Text(' ${_stats?.rating.toStringAsFixed(1) ?? '-'}',
                                style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w700)),
                          ],
                        ),
                        const SizedBox(height: 6),
                        Text(user.phone, style: TextStyle(color: Colors.white.withValues(alpha: 0.8), fontSize: 12)),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            if (_loading && _stats == null)
              const Padding(
                  padding: EdgeInsets.all(30),
                  child: Center(child: CircularProgressIndicator(color: AppColors.forest)))
            else if (_stats != null) ...[
              const SectionHeader(title: '业绩统计'),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Column(
                  children: [
                    _aggCard('本月', _stats!.month, AppColors.forest),
                    const SizedBox(height: 12),
                    _aggCard('累计', _stats!.total, AppColors.blue),
                  ],
                ),
              ),
            ],
            const SectionHeader(title: '服务设置'),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: SoftCard(
                padding: const EdgeInsets.symmetric(vertical: 6),
                child: Column(
                  children: [
                    ListTile(
                      leading: const IconBadge(icon: Icons.account_balance_wallet_rounded, color: AppColors.amber, size: 42, iconSize: 20),
                      title: const Text('我的钱包', style: TextStyle(fontWeight: FontWeight.w700)),
                      subtitle: Text('余额 ¥${_stats?.balance.toStringAsFixed(2) ?? '-'} · 待结算 ${_stats?.unpaidOrders ?? 0} 单', style: const TextStyle(fontSize: 12)),
                      trailing: const Icon(Icons.chevron_right_rounded, color: AppColors.textGrey),
                      onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const RecyclerWalletScreen())).then((_) => _load()),
                    ),
                    const Divider(height: 1, indent: 72, endIndent: 16),
                    ListTile(
                      leading: const IconBadge(icon: Icons.notifications_rounded, color: AppColors.pink, size: 42, iconSize: 20),
                      title: const Text('消息中心', style: TextStyle(fontWeight: FontWeight.w700)),
                      trailing: const Icon(Icons.chevron_right_rounded, color: AppColors.textGrey),
                      onTap: () => Navigator.push(context,
                          MaterialPageRoute(builder: (_) => NotificationsScreen(onOpen: (n) => openNotificationTarget(context, n)))),
                    ),
                    const Divider(height: 1, indent: 72, endIndent: 16),
                    ListTile(
                      leading: const IconBadge(icon: Icons.translate_rounded, color: AppColors.purple, size: 42, iconSize: 20),
                      title: const Text('语言 / Language', style: TextStyle(fontWeight: FontWeight.w700)),
                      subtitle: Text(context.watch<LocaleProvider>().isZh ? '简体中文' : 'English', style: const TextStyle(fontSize: 12)),
                      trailing: const Icon(Icons.swap_horiz_rounded, color: AppColors.textGrey),
                      onTap: () => context.read<LocaleProvider>().toggle(),
                    ),
                    const Divider(height: 1, indent: 72, endIndent: 16),
                    ListTile(
                      leading: const IconBadge(icon: Icons.map_rounded, color: AppColors.teal, size: 42, iconSize: 20),
                      title: const Text('服务区域', style: TextStyle(fontWeight: FontWeight.w700)),
                      subtitle: const Text('北京市 朝阳区 · 海淀区', style: TextStyle(fontSize: 12)),
                      trailing: const Icon(Icons.chevron_right_rounded, color: AppColors.textGrey),
                      onTap: () => showToast(context, '服务区域由平台统一配置'),
                    ),
                    const Divider(height: 1, indent: 72, endIndent: 16),
                    ListTile(
                      leading: const IconBadge(icon: Icons.dns_rounded, color: AppColors.blue, size: 42, iconSize: 20),
                      title: const Text('后端服务设置', style: TextStyle(fontWeight: FontWeight.w700)),
                      trailing: const Icon(Icons.chevron_right_rounded, color: AppColors.textGrey),
                      onTap: () => Navigator.push(
                          context, MaterialPageRoute(builder: (_) => const ServerSettingsScreen())),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: OutlinedButton(
                onPressed: () => app.logout(),
                style: OutlinedButton.styleFrom(
                  foregroundColor: AppColors.coral,
                  side: BorderSide(color: AppColors.coral.withValues(alpha: 0.4)),
                  padding: const EdgeInsets.symmetric(vertical: 14),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.lg)),
                ),
                child: const Text('退出登录', style: TextStyle(fontWeight: FontWeight.w700)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _aggCard(String title, RecyclerAgg a, Color color) => SoftCard(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(children: [
              Container(width: 4, height: 16, decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(2))),
              const SizedBox(width: 8),
              Text(title, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
            ]),
            const SizedBox(height: 14),
            Row(
              children: [
                _v('${a.count}', '完成单数', color),
                _v(a.weight.toStringAsFixed(1), '回收 kg', color),
                _v('¥${a.amount.toStringAsFixed(0)}', '结算金额', color),
                _v(a.carbon.toStringAsFixed(1), '减碳 kg', color),
              ],
            ),
          ],
        ),
      );

  Widget _v(String v, String l, Color c) => Expanded(
        child: Column(children: [
          Text(v, style: TextStyle(fontSize: 18, fontWeight: FontWeight.w900, color: c)),
          const SizedBox(height: 2),
          Text(l, style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
        ]),
      );
}
