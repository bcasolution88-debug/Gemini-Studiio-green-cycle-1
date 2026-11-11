import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:provider/provider.dart';

import 'l10n/app_localizations.dart';
import 'providers/app_provider.dart';
import 'providers/locale_provider.dart';
import 'providers/notification_provider.dart';
import 'screens/admin/admin_shell.dart';
import 'screens/auth/login_screen.dart';
import 'screens/main_shell.dart';
import 'screens/notifications/notifications_screen.dart';
import 'screens/recycle/order_detail_screen.dart';
import 'screens/recycler/recycler_shell.dart';
import 'screens/onboarding/onboarding_screen.dart';
import 'screens/recycler/recycler_task_detail_screen.dart';
import 'services/push_service.dart';
import 'theme/app_theme.dart';
import 'widgets/common_widgets.dart';

final GlobalKey<NavigatorState> rootNavigatorKey = GlobalKey<NavigatorState>();

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await initializeDateFormatting('zh_CN');
  await initializeDateFormatting('en_US');
  final localeProvider = LocaleProvider();
  await localeProvider.init();
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AppProvider()..init()),
        ChangeNotifierProvider(create: (_) => NotificationProvider()),
        ChangeNotifierProvider.value(value: localeProvider),
      ],
      child: const GreenCycleApp(),
    ),
  );
}

class GreenCycleApp extends StatelessWidget {
  const GreenCycleApp({super.key});

  @override
  Widget build(BuildContext context) {
    final locale = context.watch<LocaleProvider>().locale;
    return MaterialApp(
      title: 'Green Cycle',
      debugShowCheckedModeBanner: false,
      navigatorKey: rootNavigatorKey,
      theme: AppTheme.light,
      locale: locale,
      supportedLocales: AppLocalizations.supportedLocales,
      localizationsDelegates: const [
        AppLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      home: const _Root(),
    );
  }
}

class _Root extends StatefulWidget {
  const _Root();

  @override
  State<_Root> createState() => _RootState();
}

class _RootState extends State<_Root> with WidgetsBindingObserver {
  /// null = 未检查；true = 需要显示引导
  bool? _needOnboarding;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  /// 应用回到前台时立即刷新通知（推送就绪：即使没有系统推送，回前台也能秒级同步）
  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed && _trackingUser != null) {
      context.read<NotificationProvider>().refreshNow();
    }
  }

  Future<void> _checkOnboarding() async {
    final v = await OnboardingScreen.shouldShow();
    if (mounted) setState(() => _needOnboarding = v);
  }

  String? _trackingUser;

  void _syncNotifications(AppProvider app) {
    final np = context.read<NotificationProvider>();
    final uid = app.user?.id;
    if (uid == _trackingUser) return;
    _trackingUser = uid;
    if (uid == null) {
      np.stop();
      PushService.instance.unregister();
    } else {
      np.onNewMessage = (n) {
        final ctx = rootNavigatorKey.currentContext;
        if (ctx != null) showInAppBanner(ctx, n, onTap: () => openNotificationTarget(ctx, n));
      };
      np.start(uid);
      PushService.instance.register(uid,
          appVersion: kAppVersion, locale: context.read<LocaleProvider>().locale.languageCode);
    }
  }

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppProvider>();
    WidgetsBinding.instance.addPostFrameCallback((_) => _syncNotifications(app));
    if (!app.initialized) return const _Splash();
    final u = app.user;
    if (u == null) {
      if (app.sessionExpired) {
        app.clearSessionExpiredFlag();
        WidgetsBinding.instance.addPostFrameCallback((_) {
          final ctx = rootNavigatorKey.currentContext;
          if (ctx != null) showToast(ctx, '登录已过期，请重新登录', error: true);
        });
      }
      return const LoginScreen();
    }
    if (u.isAdmin) return const AdminShell();
    if (u.isRecycler) return const RecyclerShell();
    // 用户端：首次登录显示 3 页引导
    if (_needOnboarding == null) {
      _checkOnboarding();
      return const _Splash();
    }
    if (_needOnboarding == true) {
      return OnboardingScreen(onDone: () => setState(() => _needOnboarding = false));
    }
    return const MainShell();
  }
}

/// 根据通知类型跳转到对应页面（三端共用）
void openNotificationTarget(BuildContext context, dynamic n) {
  final app = context.read<AppProvider>();
  final rel = n.relatedId as String?;
  final type = n.type as String;
  if (rel == null || rel.isEmpty) return;
  if (type == 'order' || (type == 'points' && rel.startsWith('o_')) || (type == 'wallet' && rel.startsWith('o_'))) {
    if (app.user?.isRecycler == true) {
      Navigator.of(context).push(MaterialPageRoute(builder: (_) => RecyclerTaskDetailScreen(orderId: rel)));
    } else {
      Navigator.of(context).push(MaterialPageRoute(builder: (_) => OrderDetailScreen(orderId: rel)));
    }
  }
}

class _Splash extends StatelessWidget {
  const _Splash();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(gradient: AppColors.headerGradient),
        child: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(28),
                child: Image.asset('assets/icons/app_icon.png', width: 110, height: 110),
              ),
              const SizedBox(height: 20),
              const Text('绿循环',
                  style: TextStyle(color: Colors.white, fontSize: 28, fontWeight: FontWeight.w900)),
              const SizedBox(height: 6),
              Text('让每一份资源都循环起来',
                  style: TextStyle(color: Colors.white.withValues(alpha: 0.85), fontSize: 14)),
              const SizedBox(height: 36),
              const SizedBox(
                width: 28,
                height: 28,
                child: CircularProgressIndicator(color: AppColors.lime, strokeWidth: 3),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
