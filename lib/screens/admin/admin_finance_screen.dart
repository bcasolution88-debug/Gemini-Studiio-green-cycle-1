import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';

/// 管理后台：财务与导出（提现审核 / 数据导出）
class AdminFinanceScreen extends StatefulWidget {
  const AdminFinanceScreen({super.key});

  @override
  State<AdminFinanceScreen> createState() => _AdminFinanceScreenState();
}

class _AdminFinanceScreenState extends State<AdminFinanceScreen> with SingleTickerProviderStateMixin {
  late final TabController _tab;

  @override
  void initState() {
    super.initState();
    _tab = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tab.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          color: Colors.white,
          child: TabBar(
            controller: _tab,
            labelColor: AppColors.deepGreen,
            unselectedLabelColor: AppColors.textGrey,
            indicatorColor: AppColors.lime,
            indicatorWeight: 3,
            labelStyle: const TextStyle(fontWeight: FontWeight.w800, fontSize: 13),
            tabs: const [Tab(text: '提现审核'), Tab(text: '数据导出')],
          ),
        ),
        Expanded(child: TabBarView(controller: _tab, children: const [_WithdrawalsTab(), _ExportTab()])),
      ],
    );
  }
}

// ======================= 提现审核 =======================
class _WithdrawalsTab extends StatefulWidget {
  const _WithdrawalsTab();
  @override
  State<_WithdrawalsTab> createState() => _WithdrawalsTabState();
}

class _WithdrawalsTabState extends State<_WithdrawalsTab> with AutomaticKeepAliveClientMixin {
  List<Withdrawal> _list = [];
  String _status = 'pending';
  bool _loading = true;
  String? _error;

  @override
  bool get wantKeepAlive => true;

  String get _admin => context.read<AppProvider>().userId;

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
      _list = await ApiService.instance.adminWithdrawals(_admin, status: _status);
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _review(Withdrawal w, bool approve) async {
    final remark = TextEditingController();
    final ok = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: Text(approve ? '确认打款' : '驳回申请'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('${w.recyclerName} · ¥${w.amount.toStringAsFixed(2)}\n收款账户：${w.account}'),
            const SizedBox(height: 12),
            TextField(controller: remark, decoration: InputDecoration(labelText: approve ? '备注（选填）' : '驳回原因')),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('取消')),
          ElevatedButton(
            onPressed: () => Navigator.pop(c, true),
            style: approve ? null : ElevatedButton.styleFrom(backgroundColor: AppColors.coral),
            child: Text(approve ? '确认已打款' : '驳回'),
          ),
        ],
      ),
    );
    if (ok != true) return;
    try {
      await ApiService.instance.adminReviewWithdrawal(_admin, w.id, approve, remark: remark.text.trim());
      if (!mounted) return;
      showToast(context, approve ? '已打款，回收员将收到通知' : '已驳回，金额已退回回收员钱包');
      _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);
    return Column(
      children: [
        Container(
          color: Colors.white,
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
          child: Row(
            children: [('pending', '待审核'), ('approved', '已打款'), ('rejected', '已驳回'), ('all', '全部')].map((s) {
              final sel = s.$1 == _status;
              return Padding(
                padding: const EdgeInsets.only(right: 8),
                child: ChoiceChip(
                  label: Text(s.$2),
                  selected: sel,
                  showCheckmark: false,
                  backgroundColor: AppColors.bg,
                  onSelected: (_) {
                    setState(() => _status = s.$1);
                    _load();
                  },
                  labelStyle: TextStyle(color: sel ? Colors.white : AppColors.textDark, fontSize: 12),
                ),
              );
            }).toList(),
          ),
        ),
        Expanded(
          child: _loading
              ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
              : _error != null
                  ? ErrorState(message: _error!, onRetry: _load)
                  : _list.isEmpty
                      ? const EmptyState(icon: Icons.task_alt_rounded, title: '暂无记录')
                      : RefreshIndicator(
                          color: AppColors.forest,
                          onRefresh: _load,
                          child: ListView.builder(
                            padding: const EdgeInsets.all(16),
                            itemCount: _list.length,
                            itemBuilder: (c, i) {
                              final w = _list[i];
                              final color = w.status == 'approved' ? AppColors.forest : (w.status == 'rejected' ? AppColors.coral : AppColors.orange);
                              return SoftCard(
                                margin: const EdgeInsets.only(bottom: 10),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(children: [
                                      const IconBadge(icon: Icons.outbox_rounded, color: AppColors.purple, size: 40, iconSize: 20),
                                      const SizedBox(width: 10),
                                      Expanded(
                                        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                                          Text(w.recyclerName, style: const TextStyle(fontWeight: FontWeight.w800)),
                                          Text(w.account, style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
                                        ]),
                                      ),
                                      Column(crossAxisAlignment: CrossAxisAlignment.end, children: [
                                        Text('¥${w.amount.toStringAsFixed(2)}', style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 18)),
                                        StatusChip(text: w.statusText, color: color),
                                      ]),
                                    ]),
                                    const SizedBox(height: 8),
                                    Text('申请：${w.createdAt}${w.reviewedAt != null ? '  处理：${w.reviewedAt}' : ''}',
                                        style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                                    if (w.remark.isNotEmpty) Text('备注：${w.remark}', style: const TextStyle(fontSize: 11, color: AppColors.coral)),
                                    if (w.status == 'pending') ...[
                                      const SizedBox(height: 10),
                                      Row(children: [
                                        Expanded(
                                          child: OutlinedButton(
                                            onPressed: () => _review(w, false),
                                            style: OutlinedButton.styleFrom(foregroundColor: AppColors.coral, side: const BorderSide(color: AppColors.coral)),
                                            child: const Text('驳回'),
                                          ),
                                        ),
                                        const SizedBox(width: 10),
                                        Expanded(
                                          flex: 2,
                                          child: ElevatedButton.icon(
                                            onPressed: () => _review(w, true),
                                            icon: const Icon(Icons.price_check_rounded, size: 18),
                                            label: const Text('确认打款'),
                                          ),
                                        ),
                                      ]),
                                    ],
                                  ],
                                ),
                              );
                            },
                          ),
                        ),
        ),
      ],
    );
  }
}

// ======================= 数据导出 =======================
class _ExportTab extends StatefulWidget {
  const _ExportTab();
  @override
  State<_ExportTab> createState() => _ExportTabState();
}

class _ExportTabState extends State<_ExportTab> with AutomaticKeepAliveClientMixin {
  String _orderStatus = 'all';
  String? _busy;

  @override
  bool get wantKeepAlive => true;

  static const _datasets = [
    ('orders', '订单数据', '订单号、状态、品类、重量、金额、积分、回收员、结算', Icons.receipt_long_rounded, AppColors.blue),
    ('users', '用户数据', '昵称、手机、角色、等级、积分、累计回收', Icons.people_rounded, AppColors.teal),
    ('redemptions', '兑换记录', '用户、商品、积分、兑换码、发货状态', Icons.card_giftcard_rounded, AppColors.pink),
    ('withdrawals', '提现记录', '回收员、金额、账户、审核状态', Icons.outbox_rounded, AppColors.purple),
    ('points', '积分流水', '用户、收支、积分、说明', Icons.monetization_on_rounded, AppColors.amber),
    ('categories', '品类价格表', '单价、积分、减碳系数', Icons.category_rounded, AppColors.forest),
  ];

  Future<void> _export(String dataset) async {
    final api = ApiService.instance;
    final admin = context.read<AppProvider>().userId;
    final uri = api.exportUri(admin, dataset, status: dataset == 'orders' ? _orderStatus : 'all');
    setState(() => _busy = dataset);
    try {
      // Web / 移动端：直接用浏览器打开下载链接（后端返回 Content-Disposition: attachment）
      final ok = await launchUrl(uri, mode: kIsWeb ? LaunchMode.platformDefault : LaunchMode.externalApplication);
      if (!ok) throw Exception('无法打开下载链接');
      if (mounted) showToast(context, '已开始下载 CSV（Excel 可直接打开）');
    } catch (_) {
      // 兜底：拉取内容并复制到剪贴板
      try {
        final csv = await api.exportCsv(admin, dataset, status: dataset == 'orders' ? _orderStatus : 'all');
        await Clipboard.setData(ClipboardData(text: csv));
        if (mounted) showToast(context, '下载不可用，CSV 内容已复制到剪贴板（${csv.split('\n').length - 1} 行）');
      } catch (e) {
        if (mounted) showToast(context, e.toString(), error: true);
      }
    } finally {
      if (mounted) setState(() => _busy = null);
    }
  }

  Future<void> _copyLink(String dataset) async {
    final uri = ApiService.instance.exportUri(context.read<AppProvider>().userId, dataset,
        status: dataset == 'orders' ? _orderStatus : 'all');
    await Clipboard.setData(ClipboardData(text: uri.toString()));
    if (mounted) showToast(context, '下载链接已复制');
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        SoftCard(
          color: AppColors.yellow.withValues(alpha: 0.15),
          padding: const EdgeInsets.all(14),
          child: const Row(children: [
            Icon(Icons.info_outline_rounded, color: AppColors.amber),
            SizedBox(width: 10),
            Expanded(
              child: Text('导出为 UTF-8 (BOM) 编码的 CSV 文件，可直接用 Excel / WPS / Numbers 打开，中文不乱码。',
                  style: TextStyle(fontSize: 12, height: 1.4)),
            ),
          ]),
        ),
        const SizedBox(height: 14),
        ..._datasets.map((d) {
          final busy = _busy == d.$1;
          return SoftCard(
            margin: const EdgeInsets.only(bottom: 12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(children: [
                  IconBadge(icon: d.$4, color: d.$5, size: 44, iconSize: 22),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                      Text(d.$2, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                      Text(d.$3, style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                    ]),
                  ),
                ]),
                if (d.$1 == 'orders') ...[
                  const SizedBox(height: 10),
                  Wrap(
                    spacing: 6,
                    runSpacing: 6,
                    children: [('all', '全部'), ('pending', '待接单'), ('in_progress', '上门中'), ('completed', '已完成'), ('cancelled', '已取消')].map((s) {
                      final sel = s.$1 == _orderStatus;
                      return ChoiceChip(
                        label: Text(s.$2),
                        selected: sel,
                        showCheckmark: false,
                        backgroundColor: AppColors.bg,
                        visualDensity: VisualDensity.compact,
                        onSelected: (_) => setState(() => _orderStatus = s.$1),
                        labelStyle: TextStyle(color: sel ? Colors.white : AppColors.textDark, fontSize: 11),
                      );
                    }).toList(),
                  ),
                ],
                const SizedBox(height: 12),
                Row(children: [
                  Expanded(
                    child: ElevatedButton.icon(
                      onPressed: busy ? null : () => _export(d.$1),
                      icon: busy
                          ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                          : const Icon(Icons.download_rounded, size: 18),
                      label: const Text('下载 CSV'),
                      style: ElevatedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 12)),
                    ),
                  ),
                  const SizedBox(width: 10),
                  OutlinedButton.icon(
                    onPressed: () => _copyLink(d.$1),
                    icon: const Icon(Icons.link_rounded, size: 18),
                    label: const Text('复制链接'),
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 14),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.lg)),
                    ),
                  ),
                ]),
              ],
            ),
          );
        }),
      ],
    );
  }
}
