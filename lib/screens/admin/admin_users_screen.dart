import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';

/// 管理后台：用户管理（用户 / 回收员 / 管理员，启用禁用、角色变更）
class AdminUsersScreen extends StatefulWidget {
  const AdminUsersScreen({super.key});

  @override
  State<AdminUsersScreen> createState() => _AdminUsersScreenState();
}

class _AdminUsersScreenState extends State<AdminUsersScreen> {
  final _api = ApiService.instance;
  final _search = TextEditingController();
  Timer? _debounce;
  List<User> _list = [];
  String _role = 'all';
  bool _loading = true;
  String? _error;

  static const _roles = [('all', '全部'), ('user', '用户'), ('recycler', '回收员'), ('admin', '管理员')];

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
      _list = await _api.adminUsers(_admin, role: _role, keyword: _search.text.trim());
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _toggleStatus(User u) async {
    final disable = !u.isDisabled;
    final ok = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: Text(disable ? '禁用账号' : '启用账号'),
        content: Text('${disable ? '禁用' : '启用'}「${u.nickname}」（${u.phone}）？'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('取消')),
          ElevatedButton(
              onPressed: () => Navigator.pop(c, true),
              style: disable ? ElevatedButton.styleFrom(backgroundColor: AppColors.coral) : null,
              child: const Text('确定')),
        ],
      ),
    );
    if (ok != true) return;
    try {
      await _api.adminUpdateUser(_admin, u.id, status: disable ? 'disabled' : 'active');
      if (!mounted) return;
      showToast(context, disable ? '已禁用' : '已启用');
      _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    }
  }

  Future<void> _changeRole(User u) async {
    final role = await showModalBottomSheet<String>(
      context: context,
      builder: (c) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Padding(
              padding: const EdgeInsets.all(16),
              child: Text('变更「${u.nickname}」角色', style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w800)),
            ),
            for (final r in [('user', '普通用户', Icons.person_rounded, AppColors.forest),
                             ('recycler', '回收员', Icons.electric_moped_rounded, AppColors.blue),
                             ('admin', '管理员', Icons.admin_panel_settings_rounded, AppColors.purple)])
              ListTile(
                leading: IconBadge(icon: r.$3, color: r.$4, size: 40, iconSize: 20),
                title: Text(r.$2, style: const TextStyle(fontWeight: FontWeight.w700)),
                trailing: u.role == r.$1 ? const Icon(Icons.check_rounded, color: AppColors.forest) : null,
                onTap: () => Navigator.pop(c, r.$1),
              ),
            const SizedBox(height: 8),
          ],
        ),
      ),
    );
    if (role == null || role == u.role) return;
    try {
      await _api.adminUpdateUser(_admin, u.id, role: role);
      if (!mounted) return;
      showToast(context, '角色已更新');
      _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    }
  }

  Color _roleColor(String r) => r == 'admin' ? AppColors.purple : (r == 'recycler' ? AppColors.blue : AppColors.forest);

  @override
  Widget build(BuildContext context) {
    final me = context.watch<AppProvider>().userId;
    return Column(
      children: [
        Container(
          color: Colors.white,
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
          child: Column(
            children: [
              TextField(
                controller: _search,
                onChanged: (_) {
                  _debounce?.cancel();
                  _debounce = Timer(const Duration(milliseconds: 400), _load);
                },
                decoration: const InputDecoration(
                  hintText: '搜索昵称 / 手机号',
                  prefixIcon: Icon(Icons.search_rounded),
                  fillColor: AppColors.bg,
                  contentPadding: EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                ),
              ),
              const SizedBox(height: 10),
              Row(
                children: _roles.map((r) {
                  final sel = r.$1 == _role;
                  return Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: ChoiceChip(
                      label: Text(r.$2),
                      selected: sel,
                      showCheckmark: false,
                      backgroundColor: AppColors.bg,
                      onSelected: (_) {
                        setState(() => _role = r.$1);
                        _load();
                      },
                      labelStyle: TextStyle(color: sel ? Colors.white : AppColors.textDark, fontSize: 12),
                    ),
                  );
                }).toList(),
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
                      ? const EmptyState(icon: Icons.people_outline_rounded, title: '没有符合条件的用户')
                      : RefreshIndicator(
                          color: AppColors.forest,
                          onRefresh: _load,
                          child: ListView.builder(
                            padding: const EdgeInsets.fromLTRB(16, 12, 16, 20),
                            itemCount: _list.length,
                            itemBuilder: (c, i) {
                              final u = _list[i];
                              final rc = _roleColor(u.role);
                              return SoftCard(
                                margin: const EdgeInsets.only(bottom: 10),
                                padding: const EdgeInsets.all(14),
                                child: Row(
                                  children: [
                                    Opacity(opacity: u.isDisabled ? 0.4 : 1, child: Avatar(u.avatar, size: 48, border: rc)),
                                    const SizedBox(width: 12),
                                    Expanded(
                                      child: Column(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          Row(children: [
                                            Flexible(
                                              child: Text(u.nickname,
                                                  style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15),
                                                  maxLines: 1, overflow: TextOverflow.ellipsis),
                                            ),
                                            const SizedBox(width: 6),
                                            StatusChip(text: u.roleText, color: rc),
                                            if (u.isDisabled) ...[
                                              const SizedBox(width: 4),
                                              const StatusChip(text: '已禁用', color: AppColors.coral),
                                            ],
                                          ]),
                                          const SizedBox(height: 4),
                                          Text(u.phone, style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
                                          const SizedBox(height: 2),
                                          Text(
                                            u.isRecycler
                                                ? '完成回收 ${u.completedOrders} 单'
                                                : (u.isAdmin
                                                    ? '平台管理员'
                                                    : 'Lv.${u.level} · ${u.points} 积分 · 回收 ${u.totalWeight.toStringAsFixed(1)}kg · ${u.completedOrders} 单'),
                                            style: const TextStyle(fontSize: 11, color: AppColors.textGrey),
                                            maxLines: 1, overflow: TextOverflow.ellipsis,
                                          ),
                                        ],
                                      ),
                                    ),
                                    if (u.id != me)
                                      PopupMenuButton<String>(
                                        icon: const Icon(Icons.more_vert_rounded, color: AppColors.textGrey),
                                        onSelected: (v) => v == 'status' ? _toggleStatus(u) : _changeRole(u),
                                        itemBuilder: (_) => [
                                          PopupMenuItem(
                                            value: 'status',
                                            child: Row(children: [
                                              Icon(u.isDisabled ? Icons.check_circle_outline : Icons.block_rounded,
                                                  size: 18, color: u.isDisabled ? AppColors.forest : AppColors.coral),
                                              const SizedBox(width: 8),
                                              Text(u.isDisabled ? '启用账号' : '禁用账号'),
                                            ]),
                                          ),
                                          const PopupMenuItem(
                                            value: 'role',
                                            child: Row(children: [
                                              Icon(Icons.swap_horiz_rounded, size: 18, color: AppColors.blue),
                                              SizedBox(width: 8),
                                              Text('变更角色'),
                                            ]),
                                          ),
                                        ],
                                      ),
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
