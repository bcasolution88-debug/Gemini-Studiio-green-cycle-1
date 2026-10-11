import 'package:flutter/material.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:provider/provider.dart';

import 'providers/app_provider.dart';
import 'screens/auth/login_screen.dart';
import 'screens/main_shell.dart';
import 'theme/app_theme.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await initializeDateFormatting('zh_CN');
  runApp(
    ChangeNotifierProvider(
      create: (_) => AppProvider()..init(),
      child: const GreenCycleApp(),
    ),
  );
}

class GreenCycleApp extends StatelessWidget {
  const GreenCycleApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '绿循环 Green Cycle',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      home: const _Root(),
    );
  }
}

class _Root extends StatelessWidget {
  const _Root();

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppProvider>();
    if (!app.initialized) return const _Splash();
    return app.isLoggedIn ? const MainShell() : const LoginScreen();
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
