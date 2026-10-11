import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';
import 'recycler_task_detail_screen.dart';

/// 回收员钱包：余额 / 收益 / 待结算订单 / 提现
class RecyclerWalletScreen extends StatefulWidget {
  const RecyclerWalletScreen({super.key});

  @override
  State<RecyclerWalletScreen> createState() => _RecyclerWalletScreenState();
}

class _RecyclerWalletScreenState extends State<RecyclerWalletScreen> with SingleTickerProviderStateMixin {
  final _api = ApiService.instance;
  late final TabController _tab;
  WalletData? _w;
  bool _loading = true;
  String? _error;

  String get _me => context.read<AppProvider>().userId;

  @override
  void initState() {
    super.initState();
    _tab = TabController(length: 3, vsync: this);
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  @override
  void dispose() {
    _tab.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      _w = await _api.recyclerWallet(_me);
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _withdraw() async {
    final w = _w!;
    if (w.balance < 10) {
      showToast(context, '余额不足 ¥10，暂不可提现', error: true);
      return;
    }
    final amount = TextEditingController(text: w.balance.toStringAsFixed(2));
    final account = TextEditingController(text: '微信 ${context.read<AppProvider>().user?.phone.replaceRange(3, 7, '****') ?? ''}');
    final ok = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (c) => Padding(
        padding: EdgeInsets.only(bottom: MediaQuery.of(c).viewInsets.bottom),
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(24, 20, 24, 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Text('申请提现', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800)),
              Text('可提现 ¥${w.balance.toStringAsFixed(2)} · 单笔不低于 ¥10 · 1-3 个工作日到账',
                  style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
              const SizedBox(height: 16),
              TextField(
                controller: amount,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: const InputDecoration(labelText: '提现金额', prefixText: '¥ '),
              ),
              const SizedBox(height: 12),
              TextField(controller: account, decoration: const InputDecoration(labelText: '收款账户（微信 / 支付宝 / 银行卡）')),
              const SizedBox(height: 16),
              SizedBox(height: 50, child: FilledButton(onPressed: () => Navigator.pop(c, true), child: const Text('提交申请'))),
            ],
          ),
        ),
      ),
    );
    if (ok != true) return;
    final amt = double.tryParse(amount.text) ?? 0;
    try {
      await _api.recyclerWithdraw(_me, amt, account.text.trim());
      if (!mounted) return;
      showToast(context, '提现申请已提交，等待平台审核');
      _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('我的钱包')),
      body: _loading && _w == null
          ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
          : _error != null && _w == null
              ? ErrorState(message: _error!, onRetry: _load)
              : _body(_w!),
    );
  }

  Widget _body(WalletData w) {
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 4, 20, 0),
          child: Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              gradient: AppColors.headerGradient,
              borderRadius: BorderRadius.circular(AppRadius.lg),
              boxShadow: AppShadows.soft,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(children: [
                  Text('可提现余额', style: TextStyle(color: Colors.white.withValues(alpha: 0.85), fontSize: 13)),
                  const Spacer(),
                  if (w.pendingWithdraw > 0)
                    StatusChip(text: '提现中 ¥${w.pendingWithdraw.toStringAsFixed(2)}', color: AppColors.yellow),
                ]),
                const SizedBox(height: 4),
                Row(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text('¥${w.balance.toStringAsFixed(2)}',
                        style: const TextStyle(color: Colors.white, fontSize: 36, fontWeight: FontWeight.w900, height: 1)),
                    const Spacer(),
                    SizedBox(
                      height: 40,
                      child: FilledButton.icon(
                        onPressed: _withdraw,
                        icon: const Icon(Icons.outbox_rounded, size: 18),
                        label: const Text('提现'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                Row(children: [
                  _mini('本月收益', '¥${w.incomeMonth.toStringAsFixed(2)}'),
                  _mini('累计收益', '¥${w.incomeTotal.toStringAsFixed(2)}'),
                  _mini('已提现', '¥${w.withdrawn.toStringAsFixed(2)}'),
                ]),
                const SizedBox(height: 10),
                Text('收益规则：每单基础服务费 ¥${w.baseFee.toStringAsFixed(0)} + 订单金额 × ${(w.commissionRate * 100).toStringAsFixed(0)}%',
                    style: TextStyle(color: Colors.white.withValues(alpha: 0.75), fontSize: 11)),
              ],
            ),
          ),
        ),
        const SizedBox(height: 8),
        TabBar(
          controller: _tab,
          labelColor: AppColors.deepGreen,
          unselectedLabelColor: AppColors.textGrey,
          indicatorColor: AppColors.lime,
          indicatorWeight: 3,
          labelStyle: const TextStyle(fontWeight: FontWeight.w800, fontSize: 13),
          tabs: [
            Tab(text: '待结算 (${w.unpaidOrders.length})'),
            const Tab(text: '收益明细'),
            Tab(text: '提现记录 (${w.withdrawals.length})'),
          ],
        ),
        Expanded(
          child: TabBarView(
            controller: _tab,
            children: [_unpaid(w), _records(w), _withdrawals(w)],
          ),
        ),
      ],
    );
  }

  Widget _mini(String l, String v) => Expanded(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(v, style: const TextStyle(color: Colors.white, fontSize: 15, fontWeight: FontWeight.w900)),
          Text(l, style: const TextStyle(color: Colors.white70, fontSize: 10)),
        ]),
      );

  Widget _unpaid(WalletData w) {
    if (w.unpaidOrders.isEmpty) {
      return const EmptyState(icon: Icons.price_check_rounded, title: '所有订单均已结算', subtitle: '称重完成后记得向用户支付回收款并确认');
    }
    return RefreshIndicator(
      color: AppColors.forest,
      onRefresh: _load,
      child: ListView.builder(
        padding: const EdgeInsets.all(20),
        itemCount: w.unpaidOrders.length,
        itemBuilder: (c, i) {
          final o = w.unpaidOrders[i];
          return SoftCard(
            margin: const EdgeInsets.only(bottom: 10),
            onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => RecyclerTaskDetailScreen(orderId: o.id))).then((_) => _load()),
            child: Row(children: [
              const IconBadge(icon: Icons.pending_actions_rounded, color: AppColors.coral, size: 42, iconSize: 20),
              const SizedBox(width: 12),
              Expanded(
                child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Text('${o.contactName} · ${o.categoryNames}', style: const TextStyle(fontWeight: FontWeight.w800), maxLines: 1, overflow: TextOverflow.ellipsis),
                  Text('${o.actualWeight?.toStringAsFixed(1)}kg · ${o.updatedAt.length >= 16 ? o.updatedAt.substring(5, 16) : ''}',
                      style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
                ]),
              ),
              Text('¥${o.amount?.toStringAsFixed(2)}', style: const TextStyle(fontWeight: FontWeight.w900, color: AppColors.coral, fontSize: 16)),
              const Icon(Icons.chevron_right_rounded, color: AppColors.textGrey),
            ]),
          );
        },
      ),
    );
  }

  Widget _records(WalletData w) {
    if (w.records.isEmpty) return const EmptyState(icon: Icons.receipt_long_rounded, title: '暂无收益记录');
    return ListView.builder(
      padding: const EdgeInsets.all(20),
      itemCount: w.records.length,
      itemBuilder: (c, i) {
        final r = w.records[i];
        final plus = r.amount > 0;
        final color = r.type == 'income' ? AppColors.forest : (r.type == 'withdraw' ? AppColors.coral : AppColors.blue);
        return SoftCard(
          margin: const EdgeInsets.only(bottom: 10),
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          child: Row(children: [
            IconBadge(
                icon: r.type == 'income' ? Icons.add_circle_rounded : (r.type == 'withdraw' ? Icons.outbox_rounded : Icons.replay_rounded),
                color: color, size: 40, iconSize: 20, gradient: false),
            const SizedBox(width: 12),
            Expanded(
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(r.title, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13), maxLines: 1, overflow: TextOverflow.ellipsis),
                Text(r.createdAt.length >= 16 ? r.createdAt.substring(0, 16) : r.createdAt, style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
              ]),
            ),
            Text('${plus ? '+' : ''}${r.amount.toStringAsFixed(2)}',
                style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16, color: plus ? AppColors.forest : AppColors.coral)),
          ]),
        );
      },
    );
  }

  Widget _withdrawals(WalletData w) {
    if (w.withdrawals.isEmpty) return const EmptyState(icon: Icons.outbox_rounded, title: '暂无提现记录');
    return ListView.builder(
      padding: const EdgeInsets.all(20),
      itemCount: w.withdrawals.length,
      itemBuilder: (c, i) {
        final d = w.withdrawals[i];
        final color = d.status == 'approved' ? AppColors.forest : (d.status == 'rejected' ? AppColors.coral : AppColors.orange);
        return SoftCard(
          margin: const EdgeInsets.only(bottom: 10),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Row(children: [
              Text('¥${d.amount.toStringAsFixed(2)}', style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 18)),
              const Spacer(),
              StatusChip(text: d.statusText, color: color),
            ]),
            const SizedBox(height: 6),
            Text('收款账户：${d.account}', style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
            Text('申请时间：${d.createdAt}', style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
            if (d.reviewedAt != null) Text('处理时间：${d.reviewedAt}', style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
            if (d.remark.isNotEmpty) Text('备注：${d.remark}', style: const TextStyle(fontSize: 12, color: AppColors.coral)),
          ]),
        );
      },
    );
  }
}
