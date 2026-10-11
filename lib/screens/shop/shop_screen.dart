import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';
import 'product_detail_screen.dart';
import 'redemptions_screen.dart';

/// 积分商城
class ShopScreen extends StatefulWidget {
  const ShopScreen({super.key});

  @override
  State<ShopScreen> createState() => _ShopScreenState();
}

class _ShopScreenState extends State<ShopScreen> {
  final _api = ApiService.instance;
  List<Product> _all = [];
  String _cat = '全部';
  bool _loading = true;
  String? _error;

  static const _cats = ['全部', '生活用品', '优惠券', '绿植', '文具', '数码', '服饰', '公益'];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      _all = await _api.getProducts();
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  List<Product> get _filtered =>
      _cat == '全部' ? _all : _all.where((p) => p.category == _cat).toList();

  @override
  Widget build(BuildContext context) {
    final user = context.watch<AppProvider>().user;
    final topPad = MediaQuery.of(context).padding.top;
    return Scaffold(
      body: Column(
        children: [
          Container(
            padding: EdgeInsets.fromLTRB(20, topPad + 14, 20, 20),
            decoration: const BoxDecoration(
              gradient: AppColors.headerGradient,
              borderRadius: BorderRadius.vertical(bottom: Radius.circular(32)),
            ),
            child: Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text('积分商城',
                          style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.w900)),
                      const SizedBox(height: 4),
                      Text('用积分兑换环保好物',
                          style: TextStyle(color: Colors.white.withValues(alpha: 0.85), fontSize: 13)),
                    ],
                  ),
                ),
                GestureDetector(
                  onTap: () => Navigator.push(
                      context, MaterialPageRoute(builder: (_) => const RedemptionsScreen())),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Row(
                      children: [
                        const Icon(Icons.monetization_on_rounded, color: AppColors.amber, size: 20),
                        const SizedBox(width: 6),
                        Text('${user?.points ?? 0}',
                            style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16)),
                        const SizedBox(width: 4),
                        const Icon(Icons.chevron_right_rounded, size: 18, color: AppColors.textGrey),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
          SizedBox(
            height: 56,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
              itemCount: _cats.length,
              separatorBuilder: (_, __) => const SizedBox(width: 8),
              itemBuilder: (c, i) {
                final sel = _cats[i] == _cat;
                return ChoiceChip(
                  label: Text(_cats[i]),
                  selected: sel,
                  showCheckmark: false,
                  onSelected: (_) => setState(() => _cat = _cats[i]),
                  labelStyle: TextStyle(
                      color: sel ? Colors.white : AppColors.textDark,
                      fontSize: 13,
                      fontWeight: FontWeight.w700),
                );
              },
            ),
          ),
          Expanded(
            child: _loading
                ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
                : _error != null
                    ? ErrorState(message: _error!, onRetry: _load)
                    : _filtered.isEmpty
                        ? const EmptyState(icon: Icons.card_giftcard_rounded, title: '该分类暂无商品')
                        : RefreshIndicator(
                            color: AppColors.forest,
                            onRefresh: _load,
                            child: GridView.builder(
                              padding: const EdgeInsets.fromLTRB(20, 4, 20, 110),
                              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                                crossAxisCount: 2,
                                mainAxisSpacing: 14,
                                crossAxisSpacing: 14,
                                childAspectRatio: 0.72,
                              ),
                              itemCount: _filtered.length,
                              itemBuilder: (c, i) => _ProductCard(
                                product: _filtered[i],
                                onTap: () async {
                                  await Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) => ProductDetailScreen(product: _filtered[i])));
                                  _load();
                                },
                              ),
                            ),
                          ),
          ),
        ],
      ),
    );
  }
}

class _ProductCard extends StatelessWidget {
  final Product product;
  final VoidCallback onTap;
  const _ProductCard({required this.product, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final p = product;
    return GestureDetector(
      onTap: onTap,
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(AppRadius.lg),
          boxShadow: AppShadows.card,
        ),
        clipBehavior: Clip.antiAlias,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Stack(
              children: [
                AspectRatio(aspectRatio: 1.1, child: NetImage(p.image, width: double.infinity)),
                if (p.isHot)
                  Positioned(
                    left: 10,
                    top: 10,
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                      decoration: BoxDecoration(
                        gradient: const LinearGradient(colors: [AppColors.coral, AppColors.pink]),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: const Text('HOT',
                          style: TextStyle(
                              color: Colors.white, fontSize: 10, fontWeight: FontWeight.w900)),
                    ),
                  ),
              ],
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(12, 10, 12, 10),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(p.name,
                      style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w700),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis),
                  const SizedBox(height: 3),
                  Text(p.description,
                      style: const TextStyle(fontSize: 11, color: AppColors.textGrey),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      const Icon(Icons.monetization_on_rounded, size: 16, color: AppColors.amber),
                      const SizedBox(width: 3),
                      Text('${p.points}',
                          style: const TextStyle(
                              fontSize: 16, fontWeight: FontWeight.w900, color: AppColors.coral)),
                      const Spacer(),
                      Text('库存 ${p.stock}',
                          style: const TextStyle(fontSize: 10, color: AppColors.textGrey)),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
