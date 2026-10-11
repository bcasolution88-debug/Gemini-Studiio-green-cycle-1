import 'package:flutter/material.dart';

import '../theme/app_theme.dart';
import 'home/home_screen.dart';
import 'news/news_screen.dart';
import 'profile/profile_screen.dart';
import 'recycle/create_order_screen.dart';
import 'recycle/orders_screen.dart';
import 'shop/shop_screen.dart';

/// 主框架：底部 5 Tab + 中央「一键回收」按钮
class MainShell extends StatefulWidget {
  const MainShell({super.key});

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  int _index = 0;

  late final List<Widget> _pages = [
    HomeScreen(onNavigate: (i) => setState(() => _index = i)),
    const OrdersScreen(),
    const ShopScreen(),
    const NewsScreen(),
    const ProfileScreen(),
  ];

  void _openCreateOrder() {
    Navigator.push(context, MaterialPageRoute(builder: (_) => const CreateOrderScreen()));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      extendBody: true,
      body: IndexedStack(index: _index, children: _pages),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
      floatingActionButton: _RecycleFab(onTap: _openCreateOrder),
      bottomNavigationBar: _BottomBar(
        index: _index,
        onTap: (i) => setState(() => _index = i),
      ),
    );
  }
}

class _RecycleFab extends StatelessWidget {
  final VoidCallback onTap;
  const _RecycleFab({required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 64,
        height: 64,
        margin: const EdgeInsets.only(top: 30),
        decoration: BoxDecoration(
          gradient: AppColors.limeGradient,
          shape: BoxShape.circle,
          border: Border.all(color: Colors.white, width: 4),
          boxShadow: [
            BoxShadow(
              color: AppColors.lime.withValues(alpha: 0.5),
              blurRadius: 16,
              offset: const Offset(0, 6),
            ),
          ],
        ),
        child: const Icon(Icons.recycling_rounded, color: AppColors.deepGreen, size: 32),
      ),
    );
  }
}

class _BottomBar extends StatelessWidget {
  final int index;
  final ValueChanged<int> onTap;
  const _BottomBar({required this.index, required this.onTap});

  static const _items = [
    (Icons.home_rounded, Icons.home_outlined, '首页'),
    (Icons.receipt_long_rounded, Icons.receipt_long_outlined, '订单'),
    (Icons.card_giftcard_rounded, Icons.card_giftcard_outlined, '商城'),
    (Icons.article_rounded, Icons.article_outlined, '资讯'),
    (Icons.person_rounded, Icons.person_outline_rounded, '我的'),
  ];

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
        boxShadow: [
          BoxShadow(
            color: AppColors.deepGreen.withValues(alpha: 0.10),
            blurRadius: 24,
            offset: const Offset(0, -6),
          ),
        ],
      ),
      child: SafeArea(
        top: false,
        child: SizedBox(
          height: 64,
          child: Row(
            children: [
              _item(0),
              _item(1),
              const Expanded(
                child: Center(
                  child: Padding(
                    padding: EdgeInsets.only(top: 34),
                    child: Text('一键回收',
                        style: TextStyle(
                            fontSize: 10, color: AppColors.forest, fontWeight: FontWeight.w800)),
                  ),
                ),
              ),
              _item(2),
              _item(3),
              _item(4),
            ],
          ),
        ),
      ),
    );
  }

  Widget _item(int i) {
    final (active, inactive, label) = _items[i];
    final selected = index == i;
    return Expanded(
      child: InkWell(
        onTap: () => onTap(i),
        borderRadius: BorderRadius.circular(20),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
              decoration: BoxDecoration(
                color: selected ? AppColors.lime.withValues(alpha: 0.28) : Colors.transparent,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Icon(selected ? active : inactive,
                  color: selected ? AppColors.deepGreen : AppColors.textGrey, size: 24),
            ),
            const SizedBox(height: 3),
            Text(label,
                style: TextStyle(
                    fontSize: 11,
                    fontWeight: selected ? FontWeight.w800 : FontWeight.w500,
                    color: selected ? AppColors.deepGreen : AppColors.textGrey)),
          ],
        ),
      ),
    );
  }
}
