import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// 语言设置（中文 / English），持久化到本地
class LocaleProvider extends ChangeNotifier {
  static const _key = 'app_locale';
  Locale _locale = const Locale('zh');
  Locale get locale => _locale;
  bool get isZh => _locale.languageCode == 'zh';

  Future<void> init() async {
    final prefs = await SharedPreferences.getInstance();
    final code = prefs.getString(_key);
    if (code == 'en' || code == 'zh') _locale = Locale(code!);
  }

  Future<void> setLocale(Locale l) async {
    if (l == _locale) return;
    _locale = l;
    notifyListeners();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_key, l.languageCode);
  }

  Future<void> toggle() => setLocale(isZh ? const Locale('en') : const Locale('zh'));
}
