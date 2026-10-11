import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';

/// 地址管理 / 选择
class AddressScreen extends StatefulWidget {
  final bool selectMode;
  const AddressScreen({super.key, this.selectMode = false});

  @override
  State<AddressScreen> createState() => _AddressScreenState();
}

class _AddressScreenState extends State<AddressScreen> {
  final _api = ApiService.instance;
  List<Address> _list = [];
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
      _list = await _api.getAddresses(context.read<AppProvider>().userId);
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _add() async {
    final added = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (_) => const _AddAddressSheet(),
    );
    if (added == true) _load();
  }

  Future<void> _delete(Address a) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: const Text('删除地址'),
        content: Text('确定删除「${a.detail}」吗？'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('取消')),
          TextButton(
              onPressed: () => Navigator.pop(c, true),
              child: const Text('删除', style: TextStyle(color: AppColors.coral))),
        ],
      ),
    );
    if (ok == true) {
      await _api.deleteAddress(a.id);
      _load();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.selectMode ? '选择地址' : '我的地址')),
      body: _loading
          ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
          : _error != null
              ? ErrorState(message: _error!, onRetry: _load)
              : _list.isEmpty
                  ? EmptyState(
                      icon: Icons.location_off_rounded,
                      title: '还没有地址',
                      subtitle: '添加上门地址后即可预约回收',
                      action: ElevatedButton.icon(
                          onPressed: _add, icon: const Icon(Icons.add), label: const Text('新增地址')),
                    )
                  : ListView.builder(
                      padding: const EdgeInsets.fromLTRB(20, 8, 20, 100),
                      itemCount: _list.length,
                      itemBuilder: (c, i) {
                        final a = _list[i];
                        return SoftCard(
                          margin: const EdgeInsets.only(bottom: 12),
                          onTap: widget.selectMode ? () => Navigator.pop(context, a) : null,
                          child: Row(
                            children: [
                              IconBadge(
                                  icon: Icons.home_rounded,
                                  color: a.isDefault ? AppColors.forest : AppColors.blue,
                                  size: 44,
                                  iconSize: 22),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(children: [
                                      Text(a.contactName,
                                          style: const TextStyle(fontWeight: FontWeight.w800)),
                                      const SizedBox(width: 8),
                                      Text(a.phone,
                                          style: const TextStyle(
                                              color: AppColors.textGrey, fontSize: 12)),
                                      if (a.isDefault) ...[
                                        const SizedBox(width: 8),
                                        const StatusChip(text: '默认', color: AppColors.forest),
                                      ],
                                    ]),
                                    const SizedBox(height: 4),
                                    Text(a.full, style: const TextStyle(fontSize: 13)),
                                  ],
                                ),
                              ),
                              IconButton(
                                onPressed: () => _delete(a),
                                icon: const Icon(Icons.delete_outline_rounded,
                                    color: AppColors.textGrey, size: 20),
                              ),
                            ],
                          ),
                        );
                      },
                    ),
      floatingActionButton: _list.isEmpty
          ? null
          : FloatingActionButton.extended(
              onPressed: _add,
              backgroundColor: AppColors.forest,
              foregroundColor: Colors.white,
              icon: const Icon(Icons.add),
              label: const Text('新增地址'),
            ),
    );
  }
}

class _AddAddressSheet extends StatefulWidget {
  const _AddAddressSheet();

  @override
  State<_AddAddressSheet> createState() => _AddAddressSheetState();
}

class _AddAddressSheetState extends State<_AddAddressSheet> {
  final _name = TextEditingController();
  final _phone = TextEditingController();
  final _region = TextEditingController(text: '北京市 朝阳区');
  final _detail = TextEditingController();
  bool _default = false;
  bool _saving = false;
  final _key = GlobalKey<FormState>();

  @override
  void dispose() {
    _name.dispose();
    _phone.dispose();
    _region.dispose();
    _detail.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_key.currentState!.validate()) return;
    setState(() => _saving = true);
    try {
      await ApiService.instance.createAddress(
        userId: context.read<AppProvider>().userId,
        contactName: _name.text.trim(),
        phone: _phone.text.trim(),
        region: _region.text.trim(),
        detail: _detail.text.trim(),
        isDefault: _default,
      );
      if (mounted) Navigator.pop(context, true);
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom),
      child: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(24, 16, 24, 24),
        child: Form(
          key: _key,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                      color: AppColors.divider, borderRadius: BorderRadius.circular(2)),
                ),
              ),
              const SizedBox(height: 16),
              const Text('新增地址',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800)),
              const SizedBox(height: 16),
              TextFormField(
                controller: _name,
                decoration: const InputDecoration(labelText: '联系人'),
                validator: (v) => (v == null || v.trim().isEmpty) ? '请输入联系人' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _phone,
                keyboardType: TextInputType.phone,
                decoration: const InputDecoration(labelText: '手机号'),
                validator: (v) => (v == null || v.trim().length != 11) ? '请输入 11 位手机号' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _region,
                decoration: const InputDecoration(labelText: '所在区域'),
                validator: (v) => (v == null || v.trim().isEmpty) ? '请输入区域' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _detail,
                decoration: const InputDecoration(labelText: '详细地址（小区、楼栋、门牌号）'),
                validator: (v) => (v == null || v.trim().isEmpty) ? '请输入详细地址' : null,
              ),
              SwitchListTile(
                contentPadding: EdgeInsets.zero,
                value: _default,
                activeThumbColor: AppColors.forest,
                onChanged: (v) => setState(() => _default = v),
                title: const Text('设为默认地址'),
              ),
              SizedBox(
                height: 50,
                child: ElevatedButton(
                  onPressed: _saving ? null : _save,
                  child: _saving
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                      : const Text('保存'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
