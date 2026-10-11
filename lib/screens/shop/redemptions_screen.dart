import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';

/// 兑换记录
class RedemptionsScreen extends StatefulWidget {
  const RedemptionsScreen({super.key});

  @override
  State<RedemptionsScreen> createState() => _RedemptionsScreenState();
}

class _RedemptionsScreenState extends State<RedemptionsScreen> {
  List<Redemption> _list = [];
  bool _loading = true;
  String? _error;

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
      _list = await ApiService.instance.getRedemptions(context.read<AppProvider>().userId);
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Color _c(String s) {
    switch (s) {
      case 'pending':
        return AppColors.orange;
      case 'shipped':
        return AppColors.blue;
      default:
        return AppColors.forest;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('兑换记录')),
      body: _loading
          ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
          : _error != null
              ? ErrorState(message: _error!, onRetry: _load)
              : _list.isEmpty
                  ? const EmptyState(
                      icon: Icons.card_giftcard_rounded, title: '暂无兑换记录', subtitle: '快去商城兑换环保好物吧')
                  : RefreshIndicator(
                      color: AppColors.forest,
                      onRefresh: _load,
                      child: ListView.builder(
                        padding: const EdgeInsets.fromLTRB(20, 8, 20, 30),
                        itemCount: _list.length,
                        itemBuilder: (c, i) {
                          final r = _list[i];
                          return SoftCard(
                            margin: const EdgeInsets.only(bottom: 12),
                            padding: const EdgeInsets.all(12),
                            child: Row(
                              children: [
                                NetImage(r.productImage, width: 72, height: 72, radius: BorderRadius.circular(14)),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Row(children: [
                                        Expanded(
                                          child: Text(r.productName,
                                              style: const TextStyle(fontWeight: FontWeight.w800),
                                              maxLines: 1,
                                              overflow: TextOverflow.ellipsis),
                                        ),
                                        StatusChip(text: r.statusText, color: _c(r.status)),
                                      ]),
                                      const SizedBox(height: 6),
                                      Text('兑换码 ${r.code} · x${r.quantity}',
                                          style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
                                      const SizedBox(height: 4),
                                      Row(children: [
                                        Text('-${r.points} 积分',
                                            style: const TextStyle(
                                                color: AppColors.coral, fontWeight: FontWeight.w800, fontSize: 13)),
                                        const Spacer(),
                                        Text(r.createdAt.length >= 16 ? r.createdAt.substring(0, 16) : r.createdAt,
                                            style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                                      ]),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                          );
                        },
                      ),
                    ),
    );
  }
}
