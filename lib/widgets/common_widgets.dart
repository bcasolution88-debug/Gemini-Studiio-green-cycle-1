import 'package:flutter/material.dart';

import '../models/models.dart';
import '../theme/app_theme.dart';

/// 品类图标映射
IconData categoryIcon(String key) {
  switch (key) {
    case 'paper':
      return Icons.description_rounded;
    case 'plastic':
      return Icons.local_drink_rounded;
    case 'metal':
      return Icons.settings_rounded;
    case 'clothes':
      return Icons.checkroom_rounded;
    case 'electronic':
      return Icons.phone_android_rounded;
    case 'glass':
      return Icons.wine_bar_rounded;
    case 'appliance':
      return Icons.kitchen_rounded;
    case 'book':
      return Icons.menu_book_rounded;
    default:
      return Icons.recycling_rounded;
  }
}

IconData achievementIcon(String key) {
  switch (key) {
    case 'star':
      return Icons.star_rounded;
    case 'military_tech':
      return Icons.military_tech_rounded;
    case 'shield':
      return Icons.shield_rounded;
    case 'eco':
      return Icons.eco_rounded;
    case 'public':
      return Icons.public_rounded;
    case 'fitness_center':
      return Icons.fitness_center_rounded;
    case 'savings':
      return Icons.savings_rounded;
    case 'volunteer_activism':
      return Icons.volunteer_activism_rounded;
    default:
      return Icons.emoji_events_rounded;
  }
}

Color statusColor(String status) {
  switch (status) {
    case 'pending':
      return AppColors.orange;
    case 'accepted':
      return AppColors.blue;
    case 'in_progress':
      return AppColors.teal;
    case 'completed':
      return AppColors.forest;
    case 'cancelled':
      return AppColors.textGrey;
    default:
      return AppColors.textGrey;
  }
}

/// 圆角图标徽章
class IconBadge extends StatelessWidget {
  final IconData icon;
  final Color color;
  final double size;
  final double iconSize;
  final bool gradient;

  const IconBadge({
    super.key,
    required this.icon,
    required this.color,
    this.size = 52,
    this.iconSize = 26,
    this.gradient = true,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        gradient: gradient
            ? LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [color.withValues(alpha: 0.85), color],
              )
            : null,
        color: gradient ? null : color.withValues(alpha: 0.14),
        borderRadius: BorderRadius.circular(size * 0.32),
        boxShadow: gradient
            ? [
                BoxShadow(
                  color: color.withValues(alpha: 0.35),
                  blurRadius: 10,
                  offset: const Offset(0, 4),
                )
              ]
            : null,
      ),
      child: Icon(icon, color: gradient ? Colors.white : color, size: iconSize),
    );
  }
}

/// 白色圆角卡片
class SoftCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry padding;
  final EdgeInsetsGeometry? margin;
  final VoidCallback? onTap;
  final Color? color;
  final double radius;

  const SoftCard({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(16),
    this.margin,
    this.onTap,
    this.color,
    this.radius = AppRadius.lg,
  });

  @override
  Widget build(BuildContext context) {
    final card = Container(
      margin: margin,
      decoration: BoxDecoration(
        color: color ?? Colors.white,
        borderRadius: BorderRadius.circular(radius),
        boxShadow: AppShadows.card,
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          borderRadius: BorderRadius.circular(radius),
          onTap: onTap,
          child: Padding(padding: padding, child: child),
        ),
      ),
    );
    return card;
  }
}

/// 区块标题
class SectionHeader extends StatelessWidget {
  final String title;
  final String? action;
  final VoidCallback? onAction;

  const SectionHeader({super.key, required this.title, this.action, this.onAction});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 20, 20, 12),
      child: Row(
        children: [
          Container(
            width: 5,
            height: 18,
            decoration: BoxDecoration(
              gradient: AppColors.limeGradient,
              borderRadius: BorderRadius.circular(3),
            ),
          ),
          const SizedBox(width: 10),
          Text(title, style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w800)),
          const Spacer(),
          if (action != null)
            GestureDetector(
              onTap: onAction,
              child: Row(
                children: [
                  Text(action!, style: const TextStyle(color: AppColors.textGrey, fontSize: 13)),
                  const Icon(Icons.chevron_right_rounded, color: AppColors.textGrey, size: 18),
                ],
              ),
            ),
        ],
      ),
    );
  }
}

/// 状态标签
class StatusChip extends StatelessWidget {
  final String text;
  final Color color;
  const StatusChip({super.key, required this.text, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        text,
        style: TextStyle(color: color, fontSize: 12, fontWeight: FontWeight.w700),
      ),
    );
  }
}

/// 网络图片（带占位）
class NetImage extends StatelessWidget {
  final String url;
  final double? width;
  final double? height;
  final BoxFit fit;
  final BorderRadius? radius;

  const NetImage(this.url, {super.key, this.width, this.height, this.fit = BoxFit.cover, this.radius});

  @override
  Widget build(BuildContext context) {
    final img = url.isEmpty
        ? _placeholder()
        : Image.network(
            url,
            width: width,
            height: height,
            fit: fit,
            loadingBuilder: (c, child, p) => p == null ? child : _placeholder(),
            errorBuilder: (c, e, s) => _placeholder(),
          );
    return radius == null ? img : ClipRRect(borderRadius: radius!, child: img);
  }

  Widget _placeholder() => Container(
        width: width,
        height: height,
        color: AppColors.bg,
        child: const Icon(Icons.image_rounded, color: AppColors.divider, size: 32),
      );
}

/// 头像
class Avatar extends StatelessWidget {
  final String url;
  final double size;
  final Color? border;
  const Avatar(this.url, {super.key, this.size = 48, this.border});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        border: border != null ? Border.all(color: border!, width: 2.5) : null,
        color: AppColors.lime.withValues(alpha: 0.3),
      ),
      clipBehavior: Clip.antiAlias,
      child: url.isEmpty
          ? Icon(Icons.person_rounded, size: size * 0.6, color: AppColors.forest)
          : Image.network(
              url,
              fit: BoxFit.cover,
              errorBuilder: (c, e, s) =>
                  Icon(Icons.person_rounded, size: size * 0.6, color: AppColors.forest),
            ),
    );
  }
}

/// 空状态
class EmptyState extends StatelessWidget {
  final IconData icon;
  final String title;
  final String? subtitle;
  final Widget? action;
  const EmptyState({super.key, required this.icon, required this.title, this.subtitle, this.action});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 96,
              height: 96,
              decoration: BoxDecoration(
                color: AppColors.lime.withValues(alpha: 0.18),
                shape: BoxShape.circle,
              ),
              child: Icon(icon, size: 44, color: AppColors.forest),
            ),
            const SizedBox(height: 16),
            Text(title, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700)),
            if (subtitle != null) ...[
              const SizedBox(height: 6),
              Text(subtitle!, textAlign: TextAlign.center,
                  style: const TextStyle(color: AppColors.textGrey, fontSize: 13)),
            ],
            if (action != null) ...[const SizedBox(height: 20), action!],
          ],
        ),
      ),
    );
  }
}

/// 错误状态
class ErrorState extends StatelessWidget {
  final String message;
  final VoidCallback onRetry;
  const ErrorState({super.key, required this.message, required this.onRetry});

  @override
  Widget build(BuildContext context) {
    return EmptyState(
      icon: Icons.cloud_off_rounded,
      title: '加载失败',
      subtitle: message,
      action: ElevatedButton.icon(
        onPressed: onRetry,
        icon: const Icon(Icons.refresh_rounded),
        label: const Text('重试'),
      ),
    );
  }
}

/// 订单卡片
class OrderCard extends StatelessWidget {
  final RecycleOrder order;
  final VoidCallback? onTap;
  const OrderCard({super.key, required this.order, this.onTap});

  @override
  Widget build(BuildContext context) {
    final c = statusColor(order.status);
    return SoftCard(
      onTap: onTap,
      margin: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text('订单号 ${order.orderNo}',
                  style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
              const Spacer(),
              StatusChip(text: order.statusText, color: c),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              IconBadge(icon: Icons.recycling_rounded, color: c, size: 44, iconSize: 22),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(order.categoryNames,
                        style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w700),
                        maxLines: 1, overflow: TextOverflow.ellipsis),
                    const SizedBox(height: 4),
                    Text(
                      order.status == 'completed'
                          ? '实际 ${order.actualWeight?.toStringAsFixed(1) ?? '-'} kg · +${order.pointsEarned} 积分'
                          : '预估 ${order.estimatedWeight.toStringAsFixed(1)} kg · ${order.appointmentTime}',
                      style: const TextStyle(fontSize: 12, color: AppColors.textGrey),
                    ),
                  ],
                ),
              ),
              const Icon(Icons.chevron_right_rounded, color: AppColors.textGrey),
            ],
          ),
        ],
      ),
    );
  }
}

void showToast(BuildContext context, String msg, {bool error = false}) {
  ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(SnackBar(
      content: Text(msg),
      backgroundColor: error ? AppColors.coral : AppColors.deepGreen,
    ));
}
