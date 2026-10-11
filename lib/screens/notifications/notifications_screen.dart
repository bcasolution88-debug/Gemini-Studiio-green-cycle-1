import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/notification_provider.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';

/// 消息中心（三端通用）
class NotificationsScreen extends StatefulWidget {
  /// 点击通知时的跳转回调（由各端注入，例如打开订单详情）
  final void Function(AppNotification n)? onOpen;
  const NotificationsScreen({super.key, this.onOpen});

  @override
  State<NotificationsScreen> createState() => _NotificationsScreenState();
}

class _NotificationsScreenState extends State<NotificationsScreen> {
  String _filter = 'all';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => context.read<NotificationProvider>().load());
  }

  IconData _icon(String t) {
    switch (t) {
      case 'order':
        return Icons.local_shipping_rounded;
      case 'points':
        return Icons.monetization_on_rounded;
      case 'wallet':
        return Icons.account_balance_wallet_rounded;
      default:
        return Icons.campaign_rounded;
    }
  }

  Color _color(String t) {
    switch (t) {
      case 'order':
        return AppColors.blue;
      case 'points':
        return AppColors.amber;
      case 'wallet':
        return AppColors.purple;
      default:
        return AppColors.teal;
    }
  }

  @override
  Widget build(BuildContext context) {
    final np = context.watch<NotificationProvider>();
    final list = _filter == 'all' ? np.items : np.items.where((e) => e.type == _filter).toList();
    return Scaffold(
      appBar: AppBar(
        title: Text('消息中心${np.unread > 0 ? ' (${np.unread})' : ''}'),
        actions: [
          if (np.unread > 0)
            TextButton(onPressed: np.markAllRead, child: const Text('全部已读')),
        ],
      ),
      body: Column(
        children: [
          SizedBox(
            height: 50,
            child: ListView(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
              children: [
                ('all', '全部'), ('order', '订单'), ('points', '积分'), ('wallet', '钱包'), ('system', '系统'),
              ].map((f) {
                final sel = f.$1 == _filter;
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: ChoiceChip(
                    label: Text(f.$2),
                    selected: sel,
                    showCheckmark: false,
                    onSelected: (_) => setState(() => _filter = f.$1),
                    labelStyle: TextStyle(color: sel ? Colors.white : AppColors.textDark, fontSize: 12),
                  ),
                );
              }).toList(),
            ),
          ),
          Expanded(
            child: np.loading && np.items.isEmpty
                ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
                : list.isEmpty
                    ? const EmptyState(icon: Icons.notifications_off_rounded, title: '暂无消息')
                    : RefreshIndicator(
                        color: AppColors.forest,
                        onRefresh: np.load,
                        child: ListView.builder(
                          padding: const EdgeInsets.fromLTRB(20, 4, 20, 30),
                          itemCount: list.length,
                          itemBuilder: (c, i) {
                            final n = list[i];
                            final col = _color(n.type);
                            return Dismissible(
                              key: ValueKey(n.id),
                              direction: DismissDirection.endToStart,
                              background: Container(
                                alignment: Alignment.centerRight,
                                padding: const EdgeInsets.only(right: 20),
                                margin: const EdgeInsets.only(bottom: 10),
                                decoration: BoxDecoration(color: AppColors.coral, borderRadius: BorderRadius.circular(AppRadius.lg)),
                                child: const Icon(Icons.delete_rounded, color: Colors.white),
                              ),
                              onDismissed: (_) => np.delete(n),
                              child: SoftCard(
                                margin: const EdgeInsets.only(bottom: 10),
                                padding: const EdgeInsets.all(14),
                                color: n.isRead ? Colors.white : col.withValues(alpha: 0.06),
                                onTap: () {
                                  np.markRead(n);
                                  widget.onOpen?.call(n);
                                },
                                child: Row(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Stack(children: [
                                      IconBadge(icon: _icon(n.type), color: col, size: 42, iconSize: 20, gradient: !n.isRead),
                                      if (!n.isRead)
                                        Positioned(
                                          right: 0,
                                          top: 0,
                                          child: Container(
                                            width: 10,
                                            height: 10,
                                            decoration: BoxDecoration(
                                                color: AppColors.coral, shape: BoxShape.circle, border: Border.all(color: Colors.white, width: 2)),
                                          ),
                                        ),
                                    ]),
                                    const SizedBox(width: 12),
                                    Expanded(
                                      child: Column(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          Text(n.title,
                                              style: TextStyle(fontWeight: n.isRead ? FontWeight.w600 : FontWeight.w800, fontSize: 14)),
                                          const SizedBox(height: 4),
                                          Text(n.body, style: const TextStyle(fontSize: 12, color: AppColors.textGrey, height: 1.4)),
                                          const SizedBox(height: 6),
                                          Text(n.createdAt.length >= 16 ? n.createdAt.substring(5, 16) : n.createdAt,
                                              style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                                        ],
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            );
                          },
                        ),
                      ),
          ),
        ],
      ),
    );
  }
}

/// 带未读角标的铃铛按钮
class NotificationBell extends StatelessWidget {
  final Color color;
  final Color? background;
  final VoidCallback onTap;
  const NotificationBell({super.key, required this.onTap, this.color = Colors.white, this.background});

  @override
  Widget build(BuildContext context) {
    final unread = context.select<NotificationProvider, int>((p) => p.unread);
    return GestureDetector(
      onTap: onTap,
      child: Stack(
        clipBehavior: Clip.none,
        children: [
          Container(
            width: 38,
            height: 38,
            decoration: BoxDecoration(
              color: background ?? Colors.white.withValues(alpha: 0.16),
              shape: BoxShape.circle,
            ),
            child: Icon(Icons.notifications_rounded, color: color, size: 20),
          ),
          if (unread > 0)
            Positioned(
              right: -2,
              top: -2,
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 2),
                decoration: BoxDecoration(
                  color: AppColors.coral,
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: Colors.white, width: 1.5),
                ),
                constraints: const BoxConstraints(minWidth: 18),
                child: Text(unread > 99 ? '99+' : '$unread',
                    textAlign: TextAlign.center,
                    style: const TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.w900)),
              ),
            ),
        ],
      ),
    );
  }
}

/// 应用内横幅提醒（新消息到达时顶部弹出）
void showInAppBanner(BuildContext context, AppNotification n, {VoidCallback? onTap}) {
  final overlay = Overlay.maybeOf(context);
  if (overlay == null) return;
  late OverlayEntry entry;
  entry = OverlayEntry(
    builder: (ctx) => _BannerWidget(
      n: n,
      onTap: () {
        entry.remove();
        onTap?.call();
      },
      onDismiss: () => entry.remove(),
    ),
  );
  overlay.insert(entry);
}

class _BannerWidget extends StatefulWidget {
  final AppNotification n;
  final VoidCallback onTap;
  final VoidCallback onDismiss;
  const _BannerWidget({required this.n, required this.onTap, required this.onDismiss});

  @override
  State<_BannerWidget> createState() => _BannerWidgetState();
}

class _BannerWidgetState extends State<_BannerWidget> with SingleTickerProviderStateMixin {
  late final AnimationController _c =
      AnimationController(vsync: this, duration: const Duration(milliseconds: 320))..forward();
  bool _closed = false;

  @override
  void initState() {
    super.initState();
    Future.delayed(const Duration(seconds: 4), _close);
  }

  Future<void> _close() async {
    if (_closed || !mounted) return;
    _closed = true;
    await _c.reverse();
    widget.onDismiss();
  }

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final top = MediaQuery.of(context).padding.top;
    return Positioned(
      top: top + 8,
      left: 14,
      right: 14,
      child: SlideTransition(
        position: Tween<Offset>(begin: const Offset(0, -1.4), end: Offset.zero)
            .animate(CurvedAnimation(parent: _c, curve: Curves.easeOutBack)),
        child: Material(
          color: Colors.transparent,
          child: GestureDetector(
            onTap: () {
              _closed = true;
              widget.onTap();
            },
            onVerticalDragEnd: (d) {
              if ((d.primaryVelocity ?? 0) < 0) _close();
            },
            child: Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(AppRadius.md),
                boxShadow: [
                  BoxShadow(color: AppColors.deepGreen.withValues(alpha: 0.2), blurRadius: 24, offset: const Offset(0, 8)),
                ],
                border: Border.all(color: AppColors.lime.withValues(alpha: 0.6)),
              ),
              child: Row(
                children: [
                  ClipRRect(
                    borderRadius: BorderRadius.circular(10),
                    child: Image.asset('assets/icons/app_icon.png', width: 40, height: 40),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(children: [
                          Expanded(
                              child: Text(widget.n.title,
                                  style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 14),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis)),
                          const Text('刚刚', style: TextStyle(fontSize: 10, color: AppColors.textGrey)),
                        ]),
                        const SizedBox(height: 2),
                        Text(widget.n.body,
                            style: const TextStyle(fontSize: 12, color: AppColors.textGrey),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
