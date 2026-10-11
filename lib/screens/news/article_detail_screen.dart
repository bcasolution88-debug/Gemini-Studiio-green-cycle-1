import 'package:flutter/material.dart';

import '../../models/models.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';

class ArticleDetailScreen extends StatefulWidget {
  final Article article;
  const ArticleDetailScreen({super.key, required this.article});

  @override
  State<ArticleDetailScreen> createState() => _ArticleDetailScreenState();
}

class _ArticleDetailScreenState extends State<ArticleDetailScreen> {
  late Article _a = widget.article;
  bool _liked = false;

  @override
  void initState() {
    super.initState();
    // 记录阅读量
    ApiService.instance.getArticle(_a.id).then((a) {
      if (mounted) setState(() => _a = a);
    }).catchError((_) {});
  }

  Future<void> _like() async {
    if (_liked) return;
    setState(() => _liked = true);
    try {
      final a = await ApiService.instance.likeArticle(_a.id);
      if (mounted) setState(() => _a = a);
    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: CustomScrollView(
        slivers: [
          SliverAppBar(
            expandedHeight: 260,
            pinned: true,
            backgroundColor: AppColors.forest,
            foregroundColor: Colors.white,
            flexibleSpace: FlexibleSpaceBar(
              background: Stack(
                fit: StackFit.expand,
                children: [
                  NetImage(_a.cover),
                  Container(
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topCenter,
                        end: Alignment.bottomCenter,
                        colors: [Colors.black.withValues(alpha: 0.2), Colors.black.withValues(alpha: 0.5)],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          SliverToBoxAdapter(
            child: Container(
              transform: Matrix4.translationValues(0, -24, 0),
              decoration: const BoxDecoration(
                color: AppColors.bg,
                borderRadius: BorderRadius.vertical(top: Radius.circular(AppRadius.xl)),
              ),
              padding: const EdgeInsets.fromLTRB(22, 26, 22, 40),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  StatusChip(text: _a.category, color: AppColors.teal),
                  const SizedBox(height: 12),
                  Text(_a.title,
                      style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w900, height: 1.35)),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      const IconBadge(icon: Icons.person_rounded, color: AppColors.forest, size: 32, iconSize: 16),
                      const SizedBox(width: 8),
                      Text(_a.author, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13)),
                      const SizedBox(width: 10),
                      Text(_a.publishedAt.length >= 10 ? _a.publishedAt.substring(0, 10) : _a.publishedAt,
                          style: const TextStyle(color: AppColors.textGrey, fontSize: 12)),
                      const Spacer(),
                      const Icon(Icons.remove_red_eye_outlined, size: 14, color: AppColors.textGrey),
                      Text(' ${_a.views}', style: const TextStyle(color: AppColors.textGrey, fontSize: 12)),
                    ],
                  ),
                  const SizedBox(height: 20),
                  SoftCard(
                    padding: const EdgeInsets.all(18),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: AppColors.lime.withValues(alpha: 0.18),
                            borderRadius: BorderRadius.circular(12),
                            border: const Border(left: BorderSide(color: AppColors.green, width: 4)),
                          ),
                          child: Text(_a.summary,
                              style: const TextStyle(fontSize: 14, height: 1.6, color: AppColors.forest, fontWeight: FontWeight.w600)),
                        ),
                        const SizedBox(height: 16),
                        Text(_a.content, style: const TextStyle(fontSize: 15, height: 1.8)),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),
                  Center(
                    child: GestureDetector(
                      onTap: _like,
                      child: AnimatedContainer(
                        duration: const Duration(milliseconds: 200),
                        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                        decoration: BoxDecoration(
                          gradient: _liked
                              ? const LinearGradient(colors: [AppColors.coral, AppColors.pink])
                              : null,
                          color: _liked ? null : Colors.white,
                          borderRadius: BorderRadius.circular(30),
                          boxShadow: AppShadows.card,
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(_liked ? Icons.favorite_rounded : Icons.favorite_border_rounded,
                                color: _liked ? Colors.white : AppColors.coral),
                            const SizedBox(width: 8),
                            Text('${_liked ? '已点赞' : '点赞'} ${_a.likes}',
                                style: TextStyle(
                                    fontWeight: FontWeight.w800,
                                    color: _liked ? Colors.white : AppColors.textDark)),
                          ],
                        ),
                      ),
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
}
