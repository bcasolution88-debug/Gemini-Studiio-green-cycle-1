import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../models/models.dart';
import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';

/// 管理后台：运营管理（品类价格 / 商品 / 兑换发货 / 资讯）
class AdminOperationsScreen extends StatefulWidget {
  const AdminOperationsScreen({super.key});

  @override
  State<AdminOperationsScreen> createState() => _AdminOperationsScreenState();
}

class _AdminOperationsScreenState extends State<AdminOperationsScreen> with SingleTickerProviderStateMixin {
  late final TabController _tab;

  @override
  void initState() {
    super.initState();
    _tab = TabController(length: 4, vsync: this);
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
            tabs: const [Tab(text: '品类价格'), Tab(text: '商品管理'), Tab(text: '兑换发货'), Tab(text: '资讯管理')],
          ),
        ),
        Expanded(
          child: TabBarView(
            controller: _tab,
            children: const [_CategoriesTab(), _ProductsTab(), _RedemptionsTab(), _ArticlesTab()],
          ),
        ),
      ],
    );
  }
}

// ======================= 品类价格 =======================
class _CategoriesTab extends StatefulWidget {
  const _CategoriesTab();
  @override
  State<_CategoriesTab> createState() => _CategoriesTabState();
}

class _CategoriesTabState extends State<_CategoriesTab> with AutomaticKeepAliveClientMixin {
  List<RecycleCategory> _list = [];
  bool _loading = true;
  String? _error;

  @override
  bool get wantKeepAlive => true;

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
      _list = await ApiService.instance.getCategories();
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _edit(RecycleCategory c) async {
    final price = TextEditingController(text: c.pricePerKg.toStringAsFixed(2));
    final points = TextEditingController(text: '${c.pointsPerKg}');
    final carbon = TextEditingController(text: c.carbonPerKg.toStringAsFixed(2));
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text('编辑「${c.name}」'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(controller: price, keyboardType: const TextInputType.numberWithOptions(decimal: true), decoration: const InputDecoration(labelText: '回收单价 ¥/kg')),
            const SizedBox(height: 10),
            TextField(controller: points, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: '积分 /kg')),
            const SizedBox(height: 10),
            TextField(controller: carbon, keyboardType: const TextInputType.numberWithOptions(decimal: true), decoration: const InputDecoration(labelText: '减碳量 kg CO₂ /kg')),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('取消')),
          ElevatedButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('保存')),
        ],
      ),
    );
    if (ok != true || !mounted) return;
    final app = context.read<AppProvider>();
    try {
      await ApiService.instance.adminUpdateCategory(
        app.userId,
        c.id,
        price: double.tryParse(price.text),
        points: int.tryParse(points.text),
        carbon: double.tryParse(carbon.text),
      );
      if (!mounted) return;
      showToast(context, '已更新');
      app.categories = [];
      _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);
    if (_loading) return const Center(child: CircularProgressIndicator(color: AppColors.forest));
    if (_error != null) return ErrorState(message: _error!, onRetry: _load);
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _list.length,
      itemBuilder: (c, i) {
        final cat = _list[i];
        final color = AppColors.fromHex(cat.color);
        return SoftCard(
          margin: const EdgeInsets.only(bottom: 10),
          padding: const EdgeInsets.all(14),
          onTap: () => _edit(cat),
          child: Row(
            children: [
              IconBadge(icon: categoryIcon(cat.icon), color: color, size: 46, iconSize: 22),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(cat.name, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15)),
                    const SizedBox(height: 2),
                    Text(cat.description, style: const TextStyle(fontSize: 11, color: AppColors.textGrey), maxLines: 1, overflow: TextOverflow.ellipsis),
                  ],
                ),
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text('¥${cat.pricePerKg.toStringAsFixed(2)}/kg', style: TextStyle(fontWeight: FontWeight.w900, color: color, fontSize: 14)),
                  Text('${cat.pointsPerKg}分 · ${cat.carbonPerKg.toStringAsFixed(1)}kgCO₂', style: const TextStyle(fontSize: 10, color: AppColors.textGrey)),
                ],
              ),
              const SizedBox(width: 6),
              const Icon(Icons.edit_rounded, size: 18, color: AppColors.textGrey),
            ],
          ),
        );
      },
    );
  }
}

// ======================= 商品管理 =======================
class _ProductsTab extends StatefulWidget {
  const _ProductsTab();
  @override
  State<_ProductsTab> createState() => _ProductsTabState();
}

class _ProductsTabState extends State<_ProductsTab> with AutomaticKeepAliveClientMixin {
  List<Product> _list = [];
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
      _list = await ApiService.instance.getProducts();
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _edit([Product? p]) async {
    final name = TextEditingController(text: p?.name ?? '');
    final desc = TextEditingController(text: p?.description ?? '');
    final image = TextEditingController(text: p?.image ?? 'https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=400');
    final points = TextEditingController(text: '${p?.points ?? 500}');
    final stock = TextEditingController(text: '${p?.stock ?? 100}');
    String category = p?.category ?? '生活用品';
    bool hot = p?.isHot ?? false;
    const cats = ['生活用品', '优惠券', '绿植', '文具', '数码', '服饰', '公益'];
    final ok = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setS) => Padding(
          padding: EdgeInsets.only(bottom: MediaQuery.of(ctx).viewInsets.bottom),
          child: SingleChildScrollView(
            padding: const EdgeInsets.fromLTRB(24, 20, 24, 24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text(p == null ? '新增商品' : '编辑商品', style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w800)),
                const SizedBox(height: 16),
                TextField(controller: name, decoration: const InputDecoration(labelText: '商品名称')),
                const SizedBox(height: 10),
                TextField(controller: desc, decoration: const InputDecoration(labelText: '商品描述')),
                const SizedBox(height: 10),
                TextField(controller: image, decoration: const InputDecoration(labelText: '图片 URL')),
                const SizedBox(height: 10),
                Row(children: [
                  Expanded(child: TextField(controller: points, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: '所需积分'))),
                  const SizedBox(width: 10),
                  Expanded(child: TextField(controller: stock, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: '库存'))),
                ]),
                const SizedBox(height: 10),
                DropdownButtonFormField<String>(
                  initialValue: category,
                  decoration: const InputDecoration(labelText: '分类'),
                  items: cats.map((c) => DropdownMenuItem(value: c, child: Text(c))).toList(),
                  onChanged: (v) => setS(() => category = v ?? category),
                ),
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  value: hot,
                  activeThumbColor: AppColors.forest,
                  title: const Text('设为热门'),
                  onChanged: (v) => setS(() => hot = v),
                ),
                SizedBox(height: 48, child: ElevatedButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('保存'))),
              ],
            ),
          ),
        ),
      ),
    );
    if (ok != true) return;
    final data = {
      'name': name.text.trim(),
      'description': desc.text.trim(),
      'image': image.text.trim(),
      'points': int.tryParse(points.text) ?? 0,
      'stock': int.tryParse(stock.text) ?? 0,
      'category': category,
      'is_hot': hot,
    };
    try {
      if (p == null) {
        await ApiService.instance.adminCreateProduct(_admin, data);
      } else {
        await ApiService.instance.adminUpdateProduct(_admin, p.id, data);
      }
      if (!mounted) return;
      showToast(context, '已保存');
      _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    }
  }

  Future<void> _delete(Product p) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: const Text('删除商品'),
        content: Text('确定删除「${p.name}」？'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('取消')),
          TextButton(onPressed: () => Navigator.pop(c, true), child: const Text('删除', style: TextStyle(color: AppColors.coral))),
        ],
      ),
    );
    if (ok != true) return;
    try {
      await ApiService.instance.adminDeleteProduct(_admin, p.id);
      _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);
    return Scaffold(
      backgroundColor: Colors.transparent,
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _edit(),
        backgroundColor: AppColors.forest,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.add),
        label: const Text('新增商品'),
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
          : _error != null
              ? ErrorState(message: _error!, onRetry: _load)
              : ListView.builder(
                  padding: const EdgeInsets.fromLTRB(16, 16, 16, 90),
                  itemCount: _list.length,
                  itemBuilder: (c, i) {
                    final p = _list[i];
                    return SoftCard(
                      margin: const EdgeInsets.only(bottom: 10),
                      padding: const EdgeInsets.all(12),
                      onTap: () => _edit(p),
                      child: Row(
                        children: [
                          NetImage(p.image, width: 64, height: 64, radius: BorderRadius.circular(12)),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(children: [
                                  Flexible(child: Text(p.name, style: const TextStyle(fontWeight: FontWeight.w800), maxLines: 1, overflow: TextOverflow.ellipsis)),
                                  if (p.isHot) ...[const SizedBox(width: 6), const StatusChip(text: 'HOT', color: AppColors.coral)],
                                ]),
                                const SizedBox(height: 3),
                                Text('${p.category} · 库存 ${p.stock} · 已兑 ${p.sold}', style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                                const SizedBox(height: 3),
                                Text('${p.points} 积分', style: const TextStyle(fontWeight: FontWeight.w900, color: AppColors.coral)),
                              ],
                            ),
                          ),
                          IconButton(onPressed: () => _delete(p), icon: const Icon(Icons.delete_outline_rounded, color: AppColors.textGrey)),
                        ],
                      ),
                    );
                  },
                ),
    );
  }
}

// ======================= 兑换发货 =======================
class _RedemptionsTab extends StatefulWidget {
  const _RedemptionsTab();
  @override
  State<_RedemptionsTab> createState() => _RedemptionsTabState();
}

class _RedemptionsTabState extends State<_RedemptionsTab> with AutomaticKeepAliveClientMixin {
  List<Redemption> _list = [];
  String _status = 'all';
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
      _list = await ApiService.instance.adminRedemptions(_admin, status: _status);
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _setStatus(Redemption r, String s) async {
    try {
      await ApiService.instance.adminUpdateRedemption(_admin, r.id, s);
      if (!mounted) return;
      showToast(context, '状态已更新');
      _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    }
  }

  Color _c(String s) => s == 'pending' ? AppColors.orange : (s == 'shipped' ? AppColors.blue : AppColors.forest);

  @override
  Widget build(BuildContext context) {
    super.build(context);
    return Column(
      children: [
        Container(
          color: Colors.white,
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
          child: Row(
            children: [('all', '全部'), ('pending', '待发放'), ('shipped', '已发货'), ('completed', '已完成')].map((s) {
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
                      ? const EmptyState(icon: Icons.card_giftcard_rounded, title: '暂无兑换记录')
                      : ListView.builder(
                          padding: const EdgeInsets.all(16),
                          itemCount: _list.length,
                          itemBuilder: (c, i) {
                            final r = _list[i];
                            return SoftCard(
                              margin: const EdgeInsets.only(bottom: 10),
                              padding: const EdgeInsets.all(12),
                              child: Row(
                                children: [
                                  NetImage(r.productImage, width: 60, height: 60, radius: BorderRadius.circular(12)),
                                  const SizedBox(width: 12),
                                  Expanded(
                                    child: Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        Row(children: [
                                          Flexible(child: Text('${r.productName} x${r.quantity}', style: const TextStyle(fontWeight: FontWeight.w800), maxLines: 1, overflow: TextOverflow.ellipsis)),
                                          const SizedBox(width: 6),
                                          StatusChip(text: r.statusText, color: _c(r.status)),
                                        ]),
                                        const SizedBox(height: 3),
                                        Text('${r.userNickname} ${r.userPhone}', style: const TextStyle(fontSize: 12, color: AppColors.textGrey)),
                                        Text('兑换码 ${r.code} · ${r.points} 积分', style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                                      ],
                                    ),
                                  ),
                                  if (r.status == 'pending')
                                    TextButton(onPressed: () => _setStatus(r, 'shipped'), child: const Text('发货'))
                                  else if (r.status == 'shipped')
                                    TextButton(onPressed: () => _setStatus(r, 'completed'), child: const Text('完成')),
                                ],
                              ),
                            );
                          },
                        ),
        ),
      ],
    );
  }
}

// ======================= 资讯管理 =======================
class _ArticlesTab extends StatefulWidget {
  const _ArticlesTab();
  @override
  State<_ArticlesTab> createState() => _ArticlesTabState();
}

class _ArticlesTabState extends State<_ArticlesTab> with AutomaticKeepAliveClientMixin {
  List<Article> _list = [];
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
      _list = await ApiService.instance.getArticles();
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _create() async {
    final title = TextEditingController();
    final summary = TextEditingController();
    final content = TextEditingController();
    final cover = TextEditingController(text: 'https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=600');
    String category = '平台动态';
    const cats = ['分类知识', '环保科普', '平台动态', '人物故事'];
    final ok = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setS) => Padding(
          padding: EdgeInsets.only(bottom: MediaQuery.of(ctx).viewInsets.bottom),
          child: SingleChildScrollView(
            padding: const EdgeInsets.fromLTRB(24, 20, 24, 24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Text('发布资讯', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800)),
                const SizedBox(height: 16),
                TextField(controller: title, decoration: const InputDecoration(labelText: '标题')),
                const SizedBox(height: 10),
                TextField(controller: summary, decoration: const InputDecoration(labelText: '摘要')),
                const SizedBox(height: 10),
                TextField(controller: content, maxLines: 5, decoration: const InputDecoration(labelText: '正文')),
                const SizedBox(height: 10),
                TextField(controller: cover, decoration: const InputDecoration(labelText: '封面图 URL')),
                const SizedBox(height: 10),
                DropdownButtonFormField<String>(
                  initialValue: category,
                  decoration: const InputDecoration(labelText: '分类'),
                  items: cats.map((c) => DropdownMenuItem(value: c, child: Text(c))).toList(),
                  onChanged: (v) => setS(() => category = v ?? category),
                ),
                const SizedBox(height: 16),
                SizedBox(height: 48, child: ElevatedButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('发布'))),
              ],
            ),
          ),
        ),
      ),
    );
    if (ok != true) return;
    if (title.text.trim().isEmpty || content.text.trim().isEmpty) {
      if (mounted) showToast(context, '标题和正文不能为空', error: true);
      return;
    }
    try {
      await ApiService.instance.adminCreateArticle(_admin, {
        'title': title.text.trim(),
        'summary': summary.text.trim().isEmpty ? title.text.trim() : summary.text.trim(),
        'content': content.text.trim(),
        'cover': cover.text.trim(),
        'category': category,
      });
      if (!mounted) return;
      showToast(context, '已发布');
      _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    }
  }

  Future<void> _delete(Article a) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (c) => AlertDialog(
        title: const Text('删除资讯'),
        content: Text('确定删除「${a.title}」？'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(c, false), child: const Text('取消')),
          TextButton(onPressed: () => Navigator.pop(c, true), child: const Text('删除', style: TextStyle(color: AppColors.coral))),
        ],
      ),
    );
    if (ok != true) return;
    try {
      await ApiService.instance.adminDeleteArticle(_admin, a.id);
      _load();
    } catch (e) {
      if (mounted) showToast(context, e.toString(), error: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);
    return Scaffold(
      backgroundColor: Colors.transparent,
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _create,
        backgroundColor: AppColors.forest,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.post_add_rounded),
        label: const Text('发布资讯'),
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
          : _error != null
              ? ErrorState(message: _error!, onRetry: _load)
              : ListView.builder(
                  padding: const EdgeInsets.fromLTRB(16, 16, 16, 90),
                  itemCount: _list.length,
                  itemBuilder: (c, i) {
                    final a = _list[i];
                    return SoftCard(
                      margin: const EdgeInsets.only(bottom: 10),
                      padding: const EdgeInsets.all(12),
                      child: Row(
                        children: [
                          NetImage(a.cover, width: 72, height: 56, radius: BorderRadius.circular(10)),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(a.title, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 13), maxLines: 2, overflow: TextOverflow.ellipsis),
                                const SizedBox(height: 4),
                                Row(children: [
                                  StatusChip(text: a.category, color: AppColors.teal),
                                  const SizedBox(width: 8),
                                  Text('${a.views} 阅读 · ${a.likes} 赞', style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                                ]),
                              ],
                            ),
                          ),
                          IconButton(onPressed: () => _delete(a), icon: const Icon(Icons.delete_outline_rounded, color: AppColors.textGrey)),
                        ],
                      ),
                    );
                  },
                ),
    );
  }
}
