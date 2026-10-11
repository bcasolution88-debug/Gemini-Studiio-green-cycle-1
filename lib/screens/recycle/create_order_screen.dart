import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';
import 'address_screen.dart';
import 'order_detail_screen.dart';

/// 预约上门回收
class CreateOrderScreen extends StatefulWidget {
  final RecycleCategory? initialCategory;
  const CreateOrderScreen({super.key, this.initialCategory});

  @override
  State<CreateOrderScreen> createState() => _CreateOrderScreenState();
}

class _CreateOrderScreenState extends State<CreateOrderScreen> {
  final _api = ApiService.instance;
  List<RecycleCategory> _categories = [];
  List<Address> _addresses = [];
  Address? _address;
  final Map<String, double> _weights = {}; // categoryId -> kg
  DateTime _date = DateTime.now().add(const Duration(days: 1));
  String _slot = '14:00-16:00';
  final _remark = TextEditingController();
  bool _loading = true;
  bool _submitting = false;
  String? _error;

  static const _slots = ['09:00-11:00', '11:00-13:00', '14:00-16:00', '16:00-18:00', '18:00-20:00'];

  @override
  void initState() {
    super.initState();
    if (widget.initialCategory != null) _weights[widget.initialCategory!.id] = 5;
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  @override
  void dispose() {
    _remark.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final app = context.read<AppProvider>();
      final results = await Future.wait([
        app.ensureCategories(),
        _api.getAddresses(app.userId),
      ]);
      _categories = results[0] as List<RecycleCategory>;
      _addresses = results[1] as List<Address>;
      _address = _addresses.where((a) => a.isDefault).firstOrNull ?? _addresses.firstOrNull;
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  double get _totalWeight => _weights.values.fold(0, (a, b) => a + b);

  int get _estPoints => _weights.entries.fold(0, (sum, e) {
        final c = _categories.where((c) => c.id == e.key).firstOrNull;
        return sum + (c == null ? 0 : (e.value * c.pointsPerKg).round());
      });

  double get _estAmount => _weights.entries.fold(0.0, (sum, e) {
        final c = _categories.where((c) => c.id == e.key).firstOrNull;
        return sum + (c == null ? 0 : e.value * c.pricePerKg);
      });

  double get _estCarbon => _weights.entries.fold(0.0, (sum, e) {
        final c = _categories.where((c) => c.id == e.key).firstOrNull;
        return sum + (c == null ? 0 : e.value * c.carbonPerKg);
      });

  Future<void> _pickDate() async {
    final d = await showDatePicker(
      context: context,
      initialDate: _date,
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 14)),
    );
    if (d != null) setState(() => _date = d);
  }

  Future<void> _pickAddress() async {
    final userId = context.read<AppProvider>().userId;
    final a = await Navigator.push<Address>(
      context,
      MaterialPageRoute(builder: (_) => const AddressScreen(selectMode: true)),
    );
    if (a != null) {
      setState(() => _address = a);
    } else {
      // 可能新增了地址，刷新
      final list = await _api.getAddresses(userId);
      if (mounted) {
        setState(() {
          _addresses = list;
          _address ??= list.firstOrNull;
        });
      }
    }
  }

  Future<void> _submit() async {
    if (_weights.isEmpty) {
      showToast(context, '请至少选择一种回收品类', error: true);
      return;
    }
    if (_address == null) {
      showToast(context, '请选择上门地址', error: true);
      return;
    }
    setState(() => _submitting = true);
    try {
      final app = context.read<AppProvider>();
      final order = await _api.createOrder(
        userId: app.userId,
        addressId: _address!.id,
        appointmentTime: '${DateFormat('yyyy-MM-dd').format(_date)} $_slot',
        items: _weights.entries
            .map((e) => {'category_id': e.key, 'estimated_weight': e.value})
            .toList(),
        remark: _remark.text.trim(),
      );
      await app.loadHome();
      if (!mounted) return;
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (_) => OrderDetailScreen(orderId: order.id, justCreated: true)),
      );
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('预约上门回收')),
      body: _loading
          ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
          : _error != null
              ? ErrorState(message: _error!, onRetry: _load)
              : Column(
                  children: [
                    Expanded(
                      child: ListView(
                        padding: const EdgeInsets.fromLTRB(20, 4, 20, 20),
                        children: [
                          _title('选择回收品类', '可多选，拖动滑块设置预估重量'),
                          _categoryGrid(),
                          const SizedBox(height: 20),
                          _title('上门地址', null),
                          _addressCard(),
                          const SizedBox(height: 20),
                          _title('上门时间', null),
                          _timeCard(),
                          const SizedBox(height: 20),
                          _title('备注', '选填'),
                          TextField(
                            controller: _remark,
                            maxLines: 2,
                            decoration: const InputDecoration(hintText: '例如：请提前电话联系、物品在门口等'),
                          ),
                        ],
                      ),
                    ),
                    _bottomBar(),
                  ],
                ),
    );
  }

  Widget _title(String t, String? sub) => Padding(
        padding: const EdgeInsets.only(bottom: 10, top: 6),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text(t, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w800)),
            if (sub != null) ...[
              const SizedBox(width: 8),
              Text(sub, style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
            ],
          ],
        ),
      );

  Widget _categoryGrid() {
    return Column(
      children: [
        GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 4,
            mainAxisSpacing: 10,
            crossAxisSpacing: 10,
            childAspectRatio: 0.82,
          ),
          itemCount: _categories.length,
          itemBuilder: (c, i) {
            final cat = _categories[i];
            final color = AppColors.fromHex(cat.color);
            final selected = _weights.containsKey(cat.id);
            return GestureDetector(
              onTap: () => setState(() {
                if (selected) {
                  _weights.remove(cat.id);
                } else {
                  _weights[cat.id] = 5;
                }
              }),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 180),
                decoration: BoxDecoration(
                  color: selected ? color.withValues(alpha: 0.12) : Colors.white,
                  borderRadius: BorderRadius.circular(AppRadius.md),
                  border: Border.all(color: selected ? color : AppColors.divider, width: selected ? 2 : 1),
                ),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    IconBadge(icon: categoryIcon(cat.icon), color: color, size: 40, iconSize: 20, gradient: selected),
                    const SizedBox(height: 6),
                    Text(cat.name, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700)),
                    Text('¥${cat.pricePerKg.toStringAsFixed(1)}/kg',
                        style: TextStyle(fontSize: 10, color: color)),
                  ],
                ),
              ),
            );
          },
        ),
        if (_weights.isNotEmpty) ...[
          const SizedBox(height: 12),
          SoftCard(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
            child: Column(
              children: _weights.keys.map((id) {
                final cat = _categories.firstWhere((c) => c.id == id);
                final color = AppColors.fromHex(cat.color);
                final w = _weights[id]!;
                return Row(
                  children: [
                    Icon(categoryIcon(cat.icon), color: color, size: 20),
                    const SizedBox(width: 8),
                    SizedBox(width: 52, child: Text(cat.name, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13))),
                    Expanded(
                      child: SliderTheme(
                        data: SliderThemeData(
                          activeTrackColor: color,
                          thumbColor: color,
                          inactiveTrackColor: color.withValues(alpha: 0.15),
                          overlayColor: color.withValues(alpha: 0.12),
                          trackHeight: 4,
                        ),
                        child: Slider(
                          value: w,
                          min: 0.5,
                          max: 50,
                          divisions: 99,
                          onChanged: (v) => setState(() => _weights[id] = (v * 2).round() / 2),
                        ),
                      ),
                    ),
                    SizedBox(
                      width: 56,
                      child: Text('${w.toStringAsFixed(1)}kg',
                          textAlign: TextAlign.end,
                          style: TextStyle(fontWeight: FontWeight.w800, color: color, fontSize: 13)),
                    ),
                  ],
                );
              }).toList(),
            ),
          ),
        ],
      ],
    );
  }

  Widget _addressCard() {
    return SoftCard(
      onTap: _pickAddress,
      child: Row(
        children: [
          const IconBadge(icon: Icons.location_on_rounded, color: AppColors.coral, size: 44, iconSize: 22),
          const SizedBox(width: 12),
          Expanded(
            child: _address == null
                ? const Text('请选择或新增上门地址', style: TextStyle(color: AppColors.textGrey))
                : Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(children: [
                        Text(_address!.contactName, style: const TextStyle(fontWeight: FontWeight.w800)),
                        const SizedBox(width: 8),
                        Text(_address!.phone, style: const TextStyle(color: AppColors.textGrey, fontSize: 12)),
                      ]),
                      const SizedBox(height: 4),
                      Text(_address!.full, style: const TextStyle(fontSize: 13), maxLines: 2, overflow: TextOverflow.ellipsis),
                    ],
                  ),
          ),
          const Icon(Icons.chevron_right_rounded, color: AppColors.textGrey),
        ],
      ),
    );
  }

  Widget _timeCard() {
    return SoftCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          InkWell(
            onTap: _pickDate,
            child: Row(
              children: [
                const Icon(Icons.calendar_month_rounded, color: AppColors.forest),
                const SizedBox(width: 10),
                Text(DateFormat('yyyy年MM月dd日 EEEE', 'zh_CN').format(_date),
                    style: const TextStyle(fontWeight: FontWeight.w700)),
                const Spacer(),
                const Text('更改', style: TextStyle(color: AppColors.forest, fontSize: 13)),
              ],
            ),
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: _slots.map((s) {
              final sel = s == _slot;
              return ChoiceChip(
                label: Text(s),
                selected: sel,
                onSelected: (_) => setState(() => _slot = s),
                labelStyle: TextStyle(color: sel ? Colors.white : AppColors.textDark, fontSize: 12),
                backgroundColor: AppColors.bg,
                showCheckmark: false,
              );
            }).toList(),
          ),
        ],
      ),
    );
  }

  Widget _bottomBar() {
    return Container(
      padding: const EdgeInsets.fromLTRB(20, 14, 20, 14),
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
                  Row(children: [
                    const Text('预估 ', style: TextStyle(fontSize: 12, color: AppColors.textGrey)),
                    Text('¥${_estAmount.toStringAsFixed(1)}',
                        style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w900, color: AppColors.coral)),
                    const SizedBox(width: 10),
                    const Icon(Icons.monetization_on_rounded, size: 16, color: AppColors.amber),
                    Text(' +$_estPoints', style: const TextStyle(fontWeight: FontWeight.w800, color: AppColors.amber)),
                  ]),
                  Text('共 ${_totalWeight.toStringAsFixed(1)}kg · 预计减碳 ${_estCarbon.toStringAsFixed(1)}kg',
                      style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                ],
              ),
            ),
            SizedBox(
              height: 48,
              child: ElevatedButton(
                onPressed: _submitting ? null : _submit,
                child: _submitting
                    ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                    : const Text('确认预约'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
