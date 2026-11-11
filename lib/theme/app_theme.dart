import 'package:flutter/material.dart';

/// 应用版本（与 pubspec.yaml 保持一致；打包时同步更新）
const String kAppVersion = '1.3.3';

/// 登录页是否显示「演示账号快速填入」。默认开启便于预览/内测；
/// 商店正式包构建时加 `--dart-define=SHOW_DEMO_ACCOUNTS=false` 关闭。
const bool kShowDemoAccounts = bool.fromEnvironment('SHOW_DEMO_ACCOUNTS', defaultValue: true);

/// 绿循环 - 多彩插画风格主题
class AppColors {
  static const Color deepGreen = Color(0xFF1B5E20);
  static const Color forest = Color(0xFF2E7D32);
  static const Color green = Color(0xFF43A047);
  static const Color lime = Color(0xFFAEEA00);
  static const Color limeSoft = Color(0xFFC6FF00);
  static const Color yellow = Color(0xFFFFD54F);
  static const Color amber = Color(0xFFFFB300);
  static const Color coral = Color(0xFFFF7043);
  static const Color pink = Color(0xFFEC407A);
  static const Color blue = Color(0xFF42A5F5);
  static const Color purple = Color(0xFFAB47BC);
  static const Color teal = Color(0xFF26A69A);
  static const Color orange = Color(0xFFFFA726);

  static const Color bg = Color(0xFFF4F8F3);
  static const Color card = Colors.white;
  static const Color textDark = Color(0xFF1F2A24);
  /// 次级文字：由 #7A8A80（3.6:1）加深为 #5F6F66（白底 5.2:1，满足 WCAG AA）
  static const Color textGrey = Color(0xFF5F6F66);
  static const Color divider = Color(0xFFE6EDE7);

  /// 深绿头部上的次级白字（原 white70 仅 3.2:1，提升到 92% 不透明度 ≈ 4.6:1）
  static const Color onDarkSoft = Color(0xEBFFFFFF);

  // ---- 文字安全色（白底对比度 ≥ 4.5:1）----
  // 浅亮色（lime / yellow / coral / teal / orange 等）仅用于背景、图标与装饰；
  // 作为文字色时请使用下列加深版本。
  static const Color moneyText = Color(0xFFD84315);   // 金额（coral 加深，5.0:1）
  static const Color pointsText = Color(0xFFE65100);  // 积分（amber 加深，4.8:1）
  static const Color tealText = Color(0xFF00796B);    // 品类标签 / 进行中（4.7:1）
  static const Color orangeText = Color(0xFFE65100);  // 待接单（4.8:1）
  static const Color blueText = Color(0xFF1565C0);    // 已接单 / 用户（5.4:1）
  static const Color pinkText = Color(0xFFC2185B);    // 商城 / 兑换（5.5:1）
  static const Color purpleText = Color(0xFF7B1FA2);  // 运营 / 提现（7.0:1）

  /// 将任意品牌色映射为可读的文字色：亮度过高时自动加深
  static Color readable(Color c) {
    final hsl = HSLColor.fromColor(c);
    if (hsl.lightness <= 0.42) return c;
    return hsl.withLightness(0.32).withSaturation((hsl.saturation + 0.1).clamp(0, 1)).toColor();
  }

  static const LinearGradient headerGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF43A047)],
  );

  static const LinearGradient limeGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [Color(0xFF7CB342), Color(0xFFAEEA00)],
  );

  static const LinearGradient sunGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [Color(0xFFFFB300), Color(0xFFFFD54F)],
  );

  static Color fromHex(String hex) {
    final h = hex.replaceAll('#', '');
    return Color(int.parse('FF$h', radix: 16));
  }
}

class AppRadius {
  static const double sm = 12;
  static const double md = 18;
  static const double lg = 24;
  static const double xl = 32;
}

class AppShadows {
  static List<BoxShadow> soft = [
    BoxShadow(
      color: AppColors.deepGreen.withValues(alpha: 0.08),
      blurRadius: 18,
      offset: const Offset(0, 6),
    ),
  ];
  static List<BoxShadow> card = [
    BoxShadow(
      color: Colors.black.withValues(alpha: 0.05),
      blurRadius: 12,
      offset: const Offset(0, 4),
    ),
  ];
}

class AppTheme {
  static ThemeData get light {
    final base = ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(
        seedColor: AppColors.forest,
        primary: AppColors.forest,
        secondary: AppColors.lime,
        tertiary: AppColors.yellow,
        surface: AppColors.card,
      ),
      scaffoldBackgroundColor: AppColors.bg,
      fontFamily: 'Roboto',
    );
    return base.copyWith(
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        scrolledUnderElevation: 0,
        centerTitle: true,
        foregroundColor: AppColors.textDark,
        titleTextStyle: TextStyle(
          color: AppColors.textDark,
          fontSize: 18,
          fontWeight: FontWeight.w700,
        ),
      ),
      cardTheme: CardThemeData(
        color: AppColors.card,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppRadius.lg),
        ),
        margin: EdgeInsets.zero,
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.forest,
          foregroundColor: Colors.white,
          elevation: 0,
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppRadius.lg),
          ),
          textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: AppColors.yellow,
          foregroundColor: AppColors.deepGreen,
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(AppRadius.lg),
          ),
          textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.w800),
        ),
      ),
      // 无障碍：所有 Material 按钮/Chip/IconButton 触控面积 ≥ 48dp
      materialTapTargetSize: MaterialTapTargetSize.padded,
      visualDensity: VisualDensity.standard,
      iconButtonTheme: const IconButtonThemeData(
        style: ButtonStyle(minimumSize: WidgetStatePropertyAll(Size(48, 48))),
      ),
      chipTheme: ChipThemeData(
        backgroundColor: Colors.white,
        selectedColor: AppColors.deepGreen,
        labelStyle: const TextStyle(fontWeight: FontWeight.w600),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
          side: BorderSide.none,
        ),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: Colors.white,
        contentPadding: const EdgeInsets.symmetric(horizontal: 18, vertical: 16),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.md),
          borderSide: BorderSide.none,
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.md),
          borderSide: const BorderSide(color: AppColors.divider),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.md),
          borderSide: const BorderSide(color: AppColors.forest, width: 1.6),
        ),
      ),
      dividerTheme: const DividerThemeData(color: AppColors.divider, thickness: 1),
      bottomSheetTheme: const BottomSheetThemeData(
        backgroundColor: Colors.white,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(AppRadius.xl)),
        ),
      ),
      dialogTheme: DialogThemeData(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppRadius.lg),
        ),
      ),
      snackBarTheme: SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
        backgroundColor: AppColors.deepGreen,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppRadius.md),
        ),
      ),
      textTheme: base.textTheme.apply(
        bodyColor: AppColors.textDark,
        displayColor: AppColors.textDark,
      ),
    );
  }
}
