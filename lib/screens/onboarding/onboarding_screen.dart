import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../l10n/app_localizations.dart';
import '../../theme/app_theme.dart';

/// 首次登录引导（用户端，仅显示一次）
class OnboardingScreen extends StatefulWidget {
  final VoidCallback onDone;
  const OnboardingScreen({super.key, required this.onDone});

  static const _kSeen = 'onboarding_seen_v1';

  static Future<bool> shouldShow() async {
    final p = await SharedPreferences.getInstance();
    return !(p.getBool(_kSeen) ?? false);
  }

  static Future<void> markSeen() async {
    final p = await SharedPreferences.getInstance();
    await p.setBool(_kSeen, true);
  }

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> {
  final _ctrl = PageController();
  int _page = 0;

  static const _pages = [
    (
      Icons.electric_moped_rounded,
      '一键预约，回收员上门',
      '选品类、拖重量、挑时段，回收员 2 小时内到家，现场称重即时结算到账。',
      AppColors.forest,
    ),
    (
      Icons.stars_rounded,
      '回收即赚碳积分',
      '每一公斤可回收物都换成积分与减碳量，兑好礼、看报告，环保看得见。',
      AppColors.amber,
    ),
    (
      Icons.eco_rounded,
      '和小绿一起守护地球',
      '实时追踪回收员位置、评价服务、解锁环保成就——从第一单开始。',
      AppColors.teal,
    ),
  ];

  Future<void> _finish() async {
    await OnboardingScreen.markSeen();
    widget.onDone();
  }

  @override
  Widget build(BuildContext context) {
    final last = _page == _pages.length - 1;
    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        child: Column(
          children: [
            Align(
              alignment: Alignment.topRight,
              child: TextButton(
                onPressed: _finish,
                child: Text(context.tr('跳过'), style: TextStyle(color: AppColors.textGrey, fontWeight: FontWeight.w700)),
              ),
            ),
            Expanded(
              child: PageView.builder(
                controller: _ctrl,
                itemCount: _pages.length,
                onPageChanged: (i) => setState(() => _page = i),
                itemBuilder: (c, i) {
                  final (icon, title, body, color) = _pages[i];
                  return Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 36),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Container(
                          width: 190,
                          height: 190,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            gradient: LinearGradient(
                              begin: Alignment.topLeft,
                              end: Alignment.bottomRight,
                              colors: [color.withValues(alpha: 0.18), color.withValues(alpha: 0.05)],
                            ),
                          ),
                          child: Stack(
                            alignment: Alignment.center,
                            children: [
                              Positioned(
                                right: 22,
                                top: 26,
                                child: Container(
                                  width: 36,
                                  height: 36,
                                  decoration: const BoxDecoration(color: AppColors.lime, shape: BoxShape.circle),
                                ),
                              ),
                              Icon(icon, size: 96, color: color),
                            ],
                          ),
                        ),
                        const SizedBox(height: 40),
                        Text(context.tr(title),
                            textAlign: TextAlign.center,
                            style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w900, color: AppColors.textDark)),
                        const SizedBox(height: 14),
                        Text(context.tr(body),
                            textAlign: TextAlign.center,
                            style: const TextStyle(fontSize: 14.5, color: AppColors.textGrey, height: 1.6)),
                      ],
                    ),
                  );
                },
              ),
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(
                _pages.length,
                (i) => AnimatedContainer(
                  duration: const Duration(milliseconds: 250),
                  margin: const EdgeInsets.symmetric(horizontal: 4),
                  width: i == _page ? 24 : 8,
                  height: 8,
                  decoration: BoxDecoration(
                    color: i == _page ? AppColors.forest : AppColors.divider,
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(28, 28, 28, 24),
              child: SizedBox(
                width: double.infinity,
                height: 52,
                child: ElevatedButton(
                  onPressed: last
                      ? _finish
                      : () => _ctrl.nextPage(duration: const Duration(milliseconds: 300), curve: Curves.easeOut),
                  child: Text(last ? '开始使用' : context.tr('下一步'), style: TextStyle(fontSize: 16, fontWeight: FontWeight.w800)),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
