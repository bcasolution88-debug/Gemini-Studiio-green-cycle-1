import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../l10n/app_localizations.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';
import '../../main.dart' show openNotificationTarget;
import '../news/article_detail_screen.dart';
import '../notifications/notifications_screen.dart';
import '../recycle/create_order_screen.dart';
import '../recycle/order_detail_screen.dart';
import '../shop/product_detail_screen.dart';

class HomeScreen extends StatelessWidget {
  final ValueChanged<int> onNavigate;
  const HomeScreen({super.key, required this.onNavigate});

  @override
  Widget build(BuildContext context) {
    // A3: 仅订阅首页所需字段，避免用户积分/其他状态变更时整页重建
    final home = context.select<AppProvider, HomeData?>((a) => a.home);
    final loadingHome = context.select<AppProvider, bool>((a) => a.loadingHome);
    final app = context.read<AppProvider>();

    return Scaffold(
      backgroundColor: AppColors.bg,
      body: RefreshIndicator(
        color: AppColors.forest,
        onRefresh: app.loadHome,
        child: home == null
            ? (loadingHome
                  ? const ListSkeleton(
                      items: 4,
                      itemHeight: 120,
                      withAvatar: false,
                    )
                  : ListView(
                      children: [
                        const SizedBox(height: 160),
                        ErrorState(
                          message: app.homeError ?? context.tr('暂无数据'),
                          onRetry: app.loadHome,
                        ),
                      ],
                    ))
            : CustomScrollView(
                slivers: [
                  SliverToBoxAdapter(child: _Header(home: home)),
                  SliverToBoxAdapter(
                    child: _CategoryStrip(categories: home.categories),
                  ),
                  // 动态卡：有进行中订单 → 订单进度；否则 → 本周环保成就 + 预约入口
                  SliverToBoxAdapter(
                    child: home.activeOrder != null
                        ? _ActiveOrder(order: home.activeOrder!)
                        : _WeekCard(
                            week: home.week,
                            onCreate: () => _openCreate(context),
                          ),
                  ),
                  SliverToBoxAdapter(
                    child: SectionHeader(
                      title: context.tr('积分商城热门'),
                      action: context.tr('更多'),
                      onAction: () => onNavigate(2),
                    ),
                  ),
                  SliverToBoxAdapter(
                    child: _HotProducts(products: home.hotProducts),
                  ),
                  SliverToBoxAdapter(
                    child: SectionHeader(
                      title: context.tr('环保资讯'),
                      action: context.tr('更多'),
                      onAction: () => onNavigate(3),
                    ),
                  ),
                  SliverList.builder(
                    itemCount: home.news.length,
                    itemBuilder: (c, i) => _NewsTile(article: home.news[i]),
                  ),
                  const SliverToBoxAdapter(child: SizedBox(height: 110)),
                ],
              ),
      ),
    );
  }

  void _openCreate(BuildContext context, [RecycleCategory? cat]) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => CreateOrderScreen(initialCategory: cat),
      ),
    );
  }
}

// ---------------- 顶部：深绿曲线头部 + 悬浮积分卡 ----------------
class _Header extends StatelessWidget {
  final HomeData home;
  const _Header({required this.home});

  @override
  Widget build(BuildContext context) {
    final u = home.user;
    final topPad = MediaQuery.of(context).padding.top;
    return Stack(
      clipBehavior: Clip.none,
      children: [
        Container(
          height: 236 + topPad,
          decoration: const BoxDecoration(
            gradient: AppColors.headerGradient,
            borderRadius: BorderRadius.vertical(bottom: Radius.circular(48)),
          ),
        ),
        Positioned(
          right: -30,
          top: -20,
          child: Container(
            width: 160,
            height: 160,
            decoration: BoxDecoration(
              color: AppColors.lime.withValues(alpha: 0.16),
              shape: BoxShape.circle,
            ),
          ),
        ),
        Positioned(right: 24, top: topPad + 58, child: _Mascot()),
        Padding(
          padding: EdgeInsets.fromLTRB(20, topPad + 14, 20, 0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  const Icon(
                    Icons.location_on_rounded,
                    color: AppColors.lime,
                    size: 18,
                  ),
                  const SizedBox(width: 4),
                  const Text(
                    '北京市 朝阳区',
                    style: TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const Spacer(),
                  _SignInButton(signed: home.signedToday),
                  const SizedBox(width: 8),
                  NotificationBell(
                    onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => NotificationsScreen(
                          onOpen: (n) => openNotificationTarget(context, n),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 22),
              Text(
                'Hi, ${u.nickname}~',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 26,
                  fontWeight: FontWeight.w900,
                ),
              ),
              const SizedBox(height: 6),
              Row(
                children: [
                  const Icon(
                    Icons.eco_rounded,
                    color: AppColors.lime,
                    size: 16,
                  ),
                  const SizedBox(width: 4),
                  Text(
                    '今日已减碳 ${home.todayCarbon.toStringAsFixed(1)}kg · 累计 ${u.totalCarbon.toStringAsFixed(1)}kg',
                    style: TextStyle(
                      color: Colors.white.withValues(alpha: 0.9),
                      fontSize: 13,
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
          top: topPad + 150,
          child: _PointsCard(user: u),
        ),
      ],
    );
  }
}

class _Mascot extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 84,
      height: 84,
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.14),
        shape: BoxShape.circle,
        border: Border.all(
          color: Colors.white.withValues(alpha: 0.3),
          width: 2,
        ),
      ),
      padding: const EdgeInsets.all(8),
      child: ClipOval(
        child: Image.asset('assets/icons/app_icon.png', fit: BoxFit.cover),
      ),
    );
  }
}

class _SignInButton extends StatelessWidget {
  final bool signed;
  const _SignInButton({required this.signed});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: signed
          ? null
          : () async {
              try {
                final msg = await context.read<AppProvider>().signIn();
                if (context.mounted) showToast(context, msg);
              } catch (e) {
                if (context.mounted) {
                  showToast(context, e.toString(), error: true);
                }
              }
            },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 7),
        decoration: BoxDecoration(
          gradient: signed ? null : AppColors.sunGradient,
          color: signed ? Colors.white.withValues(alpha: 0.16) : null,
          borderRadius: BorderRadius.circular(20),
        ),
        child: Row(
          children: [
            Icon(
              signed ? Icons.check_rounded : Icons.calendar_today_rounded,
              size: 14,
              color: signed ? Colors.white : AppColors.deepGreen,
            ),
            const SizedBox(width: 4),
            Text(
              signed ? '已签到' : context.tr('签到 +10'),
              style: TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w800,
                color: signed ? Colors.white : AppColors.deepGreen,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _PointsCard extends StatelessWidget {
  final User user;
  const _PointsCard({required this.user});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.fromLTRB(20, 18, 20, 18),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(AppRadius.lg),
        boxShadow: AppShadows.soft,
      ),
      child: Column(
        children: [
          Row(
            children: [
              Container(
                width: 46,
                height: 46,
                decoration: const BoxDecoration(
                  gradient: AppColors.sunGradient,
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.monetization_on_rounded,
                  color: Colors.white,
                  size: 28,
                ),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      context.tr('我的碳积分'),
                      style: TextStyle(color: AppColors.textGrey, fontSize: 12),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      _fmt(user.points),
                      style: const TextStyle(
                        fontSize: 28,
                        fontWeight: FontWeight.w900,
                        height: 1.1,
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 12,
                  vertical: 6,
                ),
                decoration: BoxDecoration(
                  gradient: AppColors.limeGradient,
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Row(
                  children: [
                    const Icon(
                      Icons.workspace_premium_rounded,
                      size: 16,
                      color: AppColors.deepGreen,
                    ),
                    const SizedBox(width: 4),
                    Text(
                      '${user.levelName} Lv.${user.level}',
                      style: const TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w800,
                        color: AppColors.deepGreen,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          ClipRRect(
            borderRadius: BorderRadius.circular(6),
            child: LinearProgressIndicator(
              value: user.levelProgress,
              minHeight: 8,
              backgroundColor: AppColors.bg,
              valueColor: const AlwaysStoppedAnimation(AppColors.green),
            ),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Text(
                user.nextLevelPoints == null
                    ? '已达最高等级'
                    : '距离下一等级还需 ${user.pointsToNextLevel} 分',
                style: const TextStyle(fontSize: 12, color: AppColors.textGrey),
              ),
              const Spacer(),
              Text(
                '累计 ${_fmt(user.totalPoints)} 分',
                style: const TextStyle(fontSize: 12, color: AppColors.textGrey),
              ),
            ],
          ),
        ],
      ),
    );
  }

  String _fmt(int n) => n.toString().replaceAllMapped(
    RegExp(r'(\d)(?=(\d{3})+(?!\d))'),
    (m) => '${m[1]},',
  );
}

// ---------------- 品类横向彩色标签 ----------------
class _CategoryStrip extends StatelessWidget {
  final List<RecycleCategory> categories;
  const _CategoryStrip({required this.categories});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 68),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SectionHeader(title: context.tr('回收品类 · 今日价格')),
          SizedBox(
            height: 112,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 20),
              itemCount: categories.length,
              separatorBuilder: (_, __) => const SizedBox(width: 12),
              itemBuilder: (c, i) {
                final cat = categories[i];
                final color = AppColors.fromHex(cat.color);
                return Semantics(
                  button: true,
                  label: '${cat.name} ¥${cat.pricePerKg.toStringAsFixed(1)}/kg',
                  hint: context.tr('预约上门回收'),
                  excludeSemantics: true,
                  child: GestureDetector(
                    onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => CreateOrderScreen(initialCategory: cat),
                      ),
                    ),
                    child: Container(
                      width: 92,
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(AppRadius.md),
                        boxShadow: AppShadows.card,
                      ),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          IconBadge(
                            icon: categoryIcon(cat.icon),
                            color: color,
                            size: 46,
                            iconSize: 24,
                          ),
                          const SizedBox(height: 8),
                          Text(
                            cat.name,
                            style: const TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            '¥${cat.pricePerKg.toStringAsFixed(1)}/kg',
                            style: TextStyle(
                              fontSize: 11,
                              color: AppColors.readable(color),
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

// ---------------- 本周环保成就（无进行中订单时） ----------------
class _WeekCard extends StatelessWidget {
  final WeekStats week;
  final VoidCallback onCreate;
  const _WeekCard({required this.week, required this.onCreate});

  @override
  Widget build(BuildContext context) {
    final hasData = week.orders > 0;
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 20, 20, 0),
      child: GestureDetector(
        onTap: onCreate,
        child: Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            gradient: const LinearGradient(
              begin: Alignment.centerLeft,
              end: Alignment.centerRight,
              colors: [Color(0xFF2E7D32), Color(0xFF66BB6A)],
            ),
            borderRadius: BorderRadius.circular(AppRadius.lg),
            boxShadow: AppShadows.soft,
          ),
          child: Stack(
            children: [
              Positioned(
                right: -24,
                bottom: -36,
                child: Container(
                  width: 140,
                  height: 140,
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.10),
                    shape: BoxShape.circle,
                  ),
                ),
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      const Icon(
                        Icons.eco_rounded,
                        color: AppColors.lime,
                        size: 18,
                      ),
                      const SizedBox(width: 6),
                      Text(
                        hasData ? '本周环保成就' : context.tr('开始你的第一单回收'),
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 15,
                          fontWeight: FontWeight.w900,
                        ),
                      ),
                      const Spacer(),
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 14,
                          vertical: 7,
                        ),
                        decoration: BoxDecoration(
                          gradient: AppColors.sunGradient,
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            const Icon(
                              Icons.electric_moped_rounded,
                              size: 16,
                              color: AppColors.deepGreen,
                            ),
                            const SizedBox(width: 4),
                            Text(
                              context.tr('立即预约'),
                              style: TextStyle(
                                color: AppColors.deepGreen,
                                fontWeight: FontWeight.w900,
                                fontSize: 13,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 14),
                  if (hasData)
                    Row(
                      children: [
                        _metric('${week.orders}', context.tr('单回收')),
                        _metric(
                          week.weight.toStringAsFixed(1),
                          context.tr('kg 重量'),
                        ),
                        _metric(
                          week.carbon.toStringAsFixed(1),
                          context.tr('kg 减碳'),
                        ),
                        _metric('+${week.points}', context.tr('积分')),
                      ],
                    )
                  else
                    Text(
                      context.tr('回收员 2 小时内上门 · 称重即结算 · 首单额外 +50 积分'),
                      style: TextStyle(
                        color: Colors.white.withValues(alpha: 0.92),
                        fontSize: 12.5,
                      ),
                    ),
                  if (hasData) ...[
                    const SizedBox(height: 10),
                    Text(
                      '≈ 种下 ${week.trees.toStringAsFixed(1)} 棵树 · 少开车 ${week.carKm.toStringAsFixed(0)} km',
                      style: const TextStyle(
                        color: AppColors.lime,
                        fontSize: 12,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _metric(String v, String l) => Expanded(
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          v,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 20,
            fontWeight: FontWeight.w900,
            height: 1.1,
          ),
        ),
        Text(
          l,
          style: TextStyle(
            color: Colors.white.withValues(alpha: 0.85),
            fontSize: 11,
          ),
        ),
      ],
    ),
  );
}

// ---------------- 进行中订单 ----------------
class _ActiveOrder extends StatelessWidget {
  final RecycleOrder order;
  const _ActiveOrder({required this.order});

  @override
  Widget build(BuildContext context) {
    final c = statusColor(order.status);
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
      child: SoftCard(
        onTap: () => Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => OrderDetailScreen(orderId: order.id),
          ),
        ),
        child: Row(
          children: [
            order.recyclerAvatar != null
                ? Avatar(
                    order.recyclerAvatar!,
                    size: 50,
                    border: c,
                    name: order.recyclerName,
                  )
                : IconBadge(
                    icon: Icons.hourglass_top_rounded,
                    color: c,
                    size: 50,
                  ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(
                        context.tr('进行中的订单'),
                        style: TextStyle(
                          fontSize: 12,
                          color: AppColors.textGrey,
                        ),
                      ),
                      const SizedBox(width: 8),
                      StatusChip(text: context.tr(order.statusText), color: c),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    order.recyclerName != null
                        ? '${order.recyclerName} · ${order.categoryNames}'
                        : '${order.categoryNames} · 等待回收员接单',
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w700,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ),
            ),
            const Icon(Icons.chevron_right_rounded, color: AppColors.textGrey),
          ],
        ),
      ),
    );
  }
}

// ---------------- 热门商品 ----------------
class _HotProducts extends StatelessWidget {
  final List<Product> products;
  const _HotProducts({required this.products});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 200,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 20),
        itemCount: products.length,
        separatorBuilder: (_, __) => const SizedBox(width: 12),
        itemBuilder: (c, i) {
          final p = products[i];
          return Semantics(
            button: true,
            label: '${p.name} · ${p.points} ${context.tr('积分')}',
            hint: context.tr('去兑换'),
            excludeSemantics: true,
            child: GestureDetector(
              onTap: () => Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => ProductDetailScreen(product: p),
                ),
              ),
              child: Container(
                width: 140,
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(AppRadius.md),
                  boxShadow: AppShadows.card,
                ),
                clipBehavior: Clip.antiAlias,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    NetImage(p.image, height: 110, width: 140),
                    Padding(
                      padding: const EdgeInsets.fromLTRB(10, 8, 10, 8),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            p.name,
                            style: const TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w700,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                          const SizedBox(height: 6),
                          Row(
                            children: [
                              const Icon(
                                Icons.monetization_on_rounded,
                                size: 14,
                                color: AppColors.amber,
                              ),
                              const SizedBox(width: 3),
                              Text(
                                '${p.points}',
                                style: const TextStyle(
                                  fontSize: 14,
                                  fontWeight: FontWeight.w900,
                                  color: AppColors.moneyText,
                                ),
                              ),
                              const Spacer(),
                              Text(
                                '已兑${p.sold}',
                                style: const TextStyle(
                                  fontSize: 10,
                                  color: AppColors.textGrey,
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}

// ---------------- 资讯条目 ----------------
class _NewsTile extends StatelessWidget {
  final Article article;
  const _NewsTile({required this.article});

  @override
  Widget build(BuildContext context) {
    return SoftCard(
      margin: const EdgeInsets.fromLTRB(20, 0, 20, 12),
      padding: const EdgeInsets.all(12),
      onTap: () => Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => ArticleDetailScreen(article: article),
        ),
      ),
      child: Row(
        children: [
          NetImage(
            article.cover,
            width: 92,
            height: 72,
            radius: BorderRadius.circular(12),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  article.title,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    height: 1.3,
                  ),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    StatusChip(text: article.category, color: AppColors.teal),
                    const Spacer(),
                    const Icon(
                      Icons.remove_red_eye_outlined,
                      size: 13,
                      color: AppColors.textGrey,
                    ),
                    const SizedBox(width: 3),
                    Text(
                      '${article.views}',
                      style: const TextStyle(
                        fontSize: 11,
                        color: AppColors.textGrey,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
