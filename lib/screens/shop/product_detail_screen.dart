import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';

class ProductDetailScreen extends StatefulWidget {
  final Product product;
  const ProductDetailScreen({super.key, required this.product});

  @override
  State<ProductDetailScreen> createState() => _ProductDetailScreenState();
}

class _ProductDetailScreenState extends State<ProductDetailScreen> {
  int _qty = 1;
  bool _loading = false;

  Future<void> _redeem() async {
    final app = context.read<AppProvider>();
    final p = widget.product;
    final need = p.points * _qty;
    if ((app.user?.points ?? 0) < need) {
      showToast(context, '积分不足，还差 ${need - (app.user?.points ?? 0)} 积分', error: true);
      return;
    }
    final ok = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: const Text('确认兑换'),
        content: Text('使用 $need 积分兑换「${p.name}」x$_qty ？'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('取消')),
          ElevatedButton(onPressed: () => Navigator.pop(c, true), child: const Text('确认')),
        ],
      ),
    );
    if (ok != true) return;
    setState(() => _loading = true);
    try {
      final r = await ApiService.instance.redeem(app.userId, p.id, quantity: _qty);
      await app.refreshUser();
      if (!mounted) return;
      await showDialog(
        context: context,
        builder: (c) => AlertDialog(
          title: const Row(children: [
            Icon(Icons.celebration_rounded, color: AppColors.amber),
            SizedBox(width: 8),
            Text('兑换成功'),
          ]),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('已为您兑换 ${r.productName} x${r.quantity}'),
              const SizedBox(height: 10),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.lime.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(children: [
                  const Text('兑换码  ', style: TextStyle(color: AppColors.textGrey)),
                  Text(r.code,
                      style: const TextStyle(
                          fontWeight: FontWeight.w900, fontSize: 16, letterSpacing: 1)),
                ]),
              ),
            ],
          ),
          actions: [
            ElevatedButton(onPressed: () => Navigator.pop(c), child: const Text('好的')),
          ],
        ),
      );
      if (mounted) Navigator.pop(context);
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final p = widget.product;
    final points = context.watch<AppProvider>().user?.points ?? 0;
    return Scaffold(
      body: Column(
        children: [
          Expanded(
            child: CustomScrollView(
              slivers: [
                SliverAppBar(
                  expandedHeight: 320,
                  pinned: true,
                  backgroundColor: AppColors.forest,
                  foregroundColor: Colors.white,
                  flexibleSpace: FlexibleSpaceBar(
                    background: NetImage(p.image, width: double.infinity, height: 320),
                  ),
                ),
                SliverToBoxAdapter(
                  child: Container(
                    transform: Matrix4.translationValues(0, -24, 0),
                    decoration: const BoxDecoration(
                      color: AppColors.bg,
                      borderRadius: BorderRadius.vertical(top: Radius.circular(AppRadius.xl)),
                    ),
                    padding: const EdgeInsets.fromLTRB(20, 24, 20, 20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            StatusChip(text: p.category, color: AppColors.teal),
                            if (p.isHot) ...[
                              const SizedBox(width: 8),
                              const StatusChip(text: '热门', color: AppColors.coral),
                            ],
                            const Spacer(),
                            Text('已兑换 ${p.sold} 件',
                                style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
                          ],
                        ),
                        const SizedBox(height: 12),
                        Text(p.name,
                            style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w900)),
                        const SizedBox(height: 10),
                        Row(
                          children: [
                            const Icon(Icons.monetization_on_rounded, color: AppColors.amber, size: 24),
                            const SizedBox(width: 6),
                            Text('${p.points}',
                                style: const TextStyle(
                                    fontSize: 28, fontWeight: FontWeight.w900, color: AppColors.coral)),
                            const Text(' 积分',
                                style: TextStyle(color: AppColors.textGrey, fontSize: 14)),
                            const Spacer(),
                            Text('库存 ${p.stock}',
                                style: const TextStyle(fontSize: 13, color: AppColors.textGrey)),
                          ],
                        ),
                        const SizedBox(height: 20),
                        SoftCard(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Text('商品介绍',
                                  style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                              const SizedBox(height: 8),
                              Text(p.description,
                                  style: const TextStyle(height: 1.6, color: AppColors.textDark)),
                              const SizedBox(height: 14),
                              const Divider(),
                              const SizedBox(height: 10),
                              const Text('兑换说明',
                                  style: TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                              const SizedBox(height: 8),
                              const Text(
                                  '• 兑换成功后将生成兑换码，可在「兑换记录」中查看\n• 实物商品 3-5 个工作日内发货\n• 优惠券类商品请在有效期内使用\n• 积分一经兑换不可退回',
                                  style: TextStyle(height: 1.7, fontSize: 13, color: AppColors.textGrey)),
                            ],
                          ),
                        ),
                        const SizedBox(height: 16),
                        SoftCard(
                          child: Row(
                            children: [
                              const Text('兑换数量', style: TextStyle(fontWeight: FontWeight.w700)),
                              const Spacer(),
                              _qtyBtn(Icons.remove_rounded, _qty > 1 ? () => setState(() => _qty--) : null),
                              SizedBox(
                                width: 44,
                                child: Text('$_qty',
                                    textAlign: TextAlign.center,
                                    style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16)),
                              ),
                              _qtyBtn(Icons.add_rounded,
                                  _qty < 10 && _qty < p.stock ? () => setState(() => _qty++) : null),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.fromLTRB(20, 12, 20, 12),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: const BorderRadius.vertical(top: Radius.circular(AppRadius.lg)),
              boxShadow: AppShadows.soft,
            ),
            child: SafeArea(
              top: false,
              child: Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('我的积分', style: TextStyle(fontSize: 11, color: AppColors.textGrey)),
                        Text('$points',
                            style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w900)),
                      ],
                    ),
                  ),
                  SizedBox(
                    height: 48,
                    child: FilledButton(
                      onPressed: _loading || p.stock == 0 ? null : _redeem,
                      child: _loading
                          ? const SizedBox(
                              width: 20, height: 20,
                              child: CircularProgressIndicator(strokeWidth: 2, color: AppColors.deepGreen))
                          : Text(p.stock == 0 ? '已兑完' : '立即兑换 (${p.points * _qty} 积分)'),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _qtyBtn(IconData icon, VoidCallback? onTap) => InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(10),
        child: Container(
          width: 34,
          height: 34,
          decoration: BoxDecoration(
            color: onTap == null ? AppColors.bg : AppColors.lime.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(10),
          ),
          child: Icon(icon, size: 18, color: onTap == null ? AppColors.divider : AppColors.deepGreen),
        ),
      );
}
