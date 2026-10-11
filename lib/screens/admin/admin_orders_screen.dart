import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';
import '../recycle/order_detail_screen.dart';

/// 管理后台：订单管理（筛选 / 搜索 / 指派 / 取消）
class AdminOrdersScreen extends StatefulWidget {
  const AdminOrdersScreen({super.key});

  @override
  State<AdminOrdersScreen> createState() => _AdminOrdersScreenState();
}

class _AdminOrdersScreenState extends State<AdminOrdersScreen> {
  final _api = ApiService.instance;
  final _search = TextEditingController();
  Timer? _debounce;
  List<RecycleOrder> _list = [];
  String _status = 'all';
  bool _loading = true;
  String? _error;

  static const _statuses = [
    ('all', '全部'),
    ('pending', '待接单'),
    ('accepted', '已接单'),
    ('in_progress', '上门中'),
    ('completed', '已完成'),
    ('cancelled', '已取消'),
  ];

  String get _admin => context.read<AppProvider>().userId;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  @override
  void dispose() {
    _search.dispose();
    _debounce?.cancel();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      _list = await _api.adminOrders(_admin, status: _status, keyword: _search.text.trim());
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  void _onSearch(String _) {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 400), _load);
  }

  Future<void> _assign(RecycleOrder o) async {
    List<User> recyclers;
    try {
      recyclers = await _api.adminRecyclers(_admin);
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
      return;
    }
    if (!mounted) return;
    final picked = await showModalBottomSheet<User>(
      context: context,
      builder: (c) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Padding(
              padding: EdgeInsets.all(16),
              child: Text('指派回收员', style: TextStyle(fontSize: 17, fontWeight: FontWeight.w800)),
            ),
            ...recyclers.map((r) => ListTile(
                  leading: Avatar(r.avatar, size: 42),
                  title: Text(r.nickname, style: const TextStyle(fontWeight: FontWeight.w700)),
                  subtitle: Text(r.phone),
                  trailing: o.recyclerId == r.id ? const StatusChip(text: '当前', color: AppColors.forest) : null,
                  onTap: () => Navigator.pop(c, r),
                )),
            const SizedBox(height: 8),
          ],
        ),
      ),
    );
    if (picked == null) return;
    try {
      await _api.adminAssign(_admin, o.id, picked.id);
      if (!mounted) return;
      showToast(context, '已指派给 ${picked.nickname}');
      _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    }
  }

  Future<void> _cancel(RecycleOrder o) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: const Text('取消订单'),
        content: Text('确定取消订单 ${o.orderNo} 吗？'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('返回')),
          TextButton(
              onPressed: () => Navigator.pop(c, true),
              child: const Text('确定取消', style: TextStyle(color: AppColors.coral))),
        ],
      ),
    );
    if (ok != true) return;
    try {
      await _api.adminCancel(_admin, o.id);
      if (!mounted) return;
      showToast(context, '订单已取消');
      _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          color: Colors.white,
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
          child: Column(
            children: [
              TextField(
                controller: _search,
                onChanged: _onSearch,
                decoration: InputDecoration(
                  hintText: '搜索订单号 / 联系人 / 手机号 / 地址',
                  prefixIcon: const Icon(Icons.search_rounded),
                  suffixIcon: _search.text.isEmpty
                      ? null
                      : IconButton(
                          icon: const Icon(Icons.clear_rounded),
                          onPressed: () {
                            _search.clear();
                            _load();
                          }),
                  fillColor: AppColors.bg,
                  contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                ),
              ),
              const SizedBox(height: 10),
              SizedBox(
                height: 36,
                child: ListView.separated(
                  scrollDirection: Axis.horizontal,
                  itemCount: _statuses.length,
                  separatorBuilder: (_, __) => const SizedBox(width: 8),
                  itemBuilder: (c, i) {
                    final sel = _statuses[i].$1 == _status;
                    return ChoiceChip(
                      label: Text(_statuses[i].$2),
                      selected: sel,
                      showCheckmark: false,
                      backgroundColor: AppColors.bg,
                      onSelected: (_) {
                        setState(() => _status = _statuses[i].$1);
                        _load();
                      },
                      labelStyle: TextStyle(color: sel ? Colors.white : AppColors.textDark, fontSize: 12),
                    );
                  },
                ),
              ),
            ],
          ),
        ),
        Expanded(
          child: _loading
              ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
              : _error != null
                  ? ErrorState(message: _error!, onRetry: _load)
                  : _list.isEmpty
                      ? const EmptyState(icon: Icons.inbox_rounded, title: '没有符合条件的订单')
                      : RefreshIndicator(
                          color: AppColors.forest,
                          onRefresh: _load,
                          child: ListView.builder(
                            padding: const EdgeInsets.fromLTRB(16, 12, 16, 20),
                            itemCount: _list.length,
                            itemBuilder: (c, i) => _row(_list[i]),
                          ),
                        ),
        ),
      ],
    );
  }

  Widget _row(RecycleOrder o) {
    final col = statusColor(o.status);
    return SoftCard(
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(14),
      onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => OrderDetailScreen(orderId: o.id))).then((_) => _load()),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(children: [
            Text(o.orderNo, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700, color: AppColors.textGrey)),
            const Spacer(),
            StatusChip(text: o.statusText, color: col),
          ]),
          const SizedBox(height: 8),
          Row(children: [
            Expanded(
              child: Text(o.categoryNames,
                  style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15), maxLines: 1, overflow: TextOverflow.ellipsis),
            ),
            Text(
              o.status == 'completed'
                  ? '${o.actualWeight?.toStringAsFixed(1)}kg · ¥${o.amount?.toStringAsFixed(1)}'
                  : '预估 ${o.estimatedWeight.toStringAsFixed(1)}kg',
              style: TextStyle(fontSize: 13, fontWeight: FontWeight.w800, color: col),
            ),
          ]),
          const SizedBox(height: 6),
          Text('${o.contactName} ${o.contactPhone} · ${o.appointmentTime}',
              style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
          Text(o.addressText, style: const TextStyle(fontSize: 12, color: AppColors.textGrey), maxLines: 1, overflow: TextOverflow.ellipsis),
          const SizedBox(height: 10),
          Row(children: [
            if (o.recyclerName != null) ...[
              Avatar(o.recyclerAvatar ?? '', size: 22),
              const SizedBox(width: 6),
              Text(o.recyclerName!, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700)),
            ] else
              const Text('未指派回收员', style: TextStyle(fontSize: 12, color: AppColors.textGrey)),
            const Spacer(),
            if (o.status == 'pending' || o.status == 'accepted') ...[
              _miniBtn(o.status == 'pending' ? '指派' : '改派', Icons.person_add_alt_1_rounded, AppColors.blue, () => _assign(o)),
              const SizedBox(width: 8),
            ],
            if (o.isActive) _miniBtn('取消', Icons.cancel_outlined, AppColors.coral, () => _cancel(o)),
          ]),
        ],
      ),
    );
  }

  Widget _miniBtn(String t, IconData i, Color c, VoidCallback onTap) => InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
          decoration: BoxDecoration(color: c.withValues(alpha: 0.1), borderRadius: BorderRadius.circular(16)),
          child: Row(mainAxisSize: MainAxisSize.min, children: [
            Icon(i, size: 14, color: c),
            const SizedBox(width: 4),
            Text(t, style: TextStyle(fontSize: 12, color: c, fontWeight: FontWeight.w700)),
          ]),
        ),
      );
}
