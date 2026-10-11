import 'package:flutter/material.dart';

import '../../models/models.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';
import 'article_detail_screen.dart';

/// 环保资讯
class NewsScreen extends StatefulWidget {
  const NewsScreen({super.key});

  @override
  State<NewsScreen> createState() => _NewsScreenState();
}

class _NewsScreenState extends State<NewsScreen> {
  List<Article> _all = [];
  String _cat = '全部';
  bool _loading = true;
  String? _error;

  static const _cats = ['全部', '分类知识', '环保科普', '平台动态', '人物故事'];

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
      _all = await ApiService.instance.getArticles();
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  List<Article> get _filtered => _cat == '全部' ? _all : _all.where((a) => a.category == _cat).toList();

  @override
  Widget build(BuildContext context) {
    final topPad = MediaQuery.of(context).padding.top;
    final list = _filtered;
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
                      const Text('环保资讯',
                          style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.w900)),
                      const SizedBox(height: 4),
                      Text('分类知识 · 环保科普 · 绿色生活',
                          style: TextStyle(color: Colors.white.withValues(alpha: 0.85), fontSize: 13)),
                    ],
                  ),
                ),
                const Icon(Icons.menu_book_rounded, color: AppColors.lime, size: 40),
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
                      color: sel ? Colors.white : AppColors.textDark, fontSize: 13, fontWeight: FontWeight.w700),
                );
              },
            ),
          ),
          Expanded(
            child: _loading
                ? const Center(child: CircularProgressIndicator(color: AppColors.forest))
                : _error != null
                    ? ErrorState(message: _error!, onRetry: _load)
                    : list.isEmpty
                        ? const EmptyState(icon: Icons.article_rounded, title: '暂无资讯')
                        : RefreshIndicator(
                            color: AppColors.forest,
                            onRefresh: _load,
                            child: ListView.builder(
                              padding: const EdgeInsets.fromLTRB(20, 4, 20, 110),
                              itemCount: list.length,
                              itemBuilder: (c, i) =>
                                  i == 0 && _cat == '全部' ? _FeaturedCard(a: list[i]) : _ArticleTile(a: list[i]),
                            ),
                          ),
          ),
        ],
      ),
    );
  }
}

class _FeaturedCard extends StatelessWidget {
  final Article a;
  const _FeaturedCard({required this.a});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => ArticleDetailScreen(article: a))),
      child: Container(
        height: 220,
        margin: const EdgeInsets.only(bottom: 14),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(AppRadius.lg),
          boxShadow: AppShadows.soft,
        ),
        clipBehavior: Clip.antiAlias,
        child: Stack(
          fit: StackFit.expand,
          children: [
            NetImage(a.cover),
            Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [Colors.transparent, Colors.black.withValues(alpha: 0.75)],
                ),
              ),
            ),
            Positioned(
              left: 16,
              right: 16,
              bottom: 16,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      gradient: AppColors.sunGradient,
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Text(a.category,
                        style: const TextStyle(
                            fontSize: 11, fontWeight: FontWeight.w800, color: AppColors.deepGreen)),
                  ),
                  const SizedBox(height: 8),
                  Text(a.title,
                      style: const TextStyle(
                          color: Colors.white, fontSize: 17, fontWeight: FontWeight.w800, height: 1.3),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis),
                  const SizedBox(height: 6),
                  Text('${a.author} · ${a.views} 阅读',
                      style: TextStyle(color: Colors.white.withValues(alpha: 0.85), fontSize: 12)),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ArticleTile extends StatelessWidget {
  final Article a;
  const _ArticleTile({required this.a});

  @override
  Widget build(BuildContext context) {
    return SoftCard(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(12),
      onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => ArticleDetailScreen(article: a))),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(a.title,
                    style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w700, height: 1.3),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis),
                const SizedBox(height: 6),
                Text(a.summary,
                    style: const TextStyle(fontSize: 12, color: AppColors.textGrey, height: 1.4),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis),
                const SizedBox(height: 10),
                Row(
                  children: [
                    StatusChip(text: a.category, color: AppColors.teal),
                    const Spacer(),
                    const Icon(Icons.remove_red_eye_outlined, size: 13, color: AppColors.textGrey),
                    Text(' ${a.views}  ', style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                    const Icon(Icons.favorite_border_rounded, size: 13, color: AppColors.textGrey),
                    Text(' ${a.likes}', style: const TextStyle(fontSize: 11, color: AppColors.textGrey)),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(width: 12),
          NetImage(a.cover, width: 100, height: 100, radius: BorderRadius.circular(14)),
        ],
      ),
    );
  }
}
