import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../providers/app_provider.dart';
import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';
import '../settings/server_settings_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  bool _isRegister = false;
  bool _loading = false;
  bool _obscure = true;
  final _phone = TextEditingController(text: '13912345678');
  final _password = TextEditingController(text: '123456');
  final _nickname = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _phone.dispose();
    _password.dispose();
    _nickname.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _loading = true);
    final app = context.read<AppProvider>();
    try {
      if (_isRegister) {
        await app.register(_phone.text.trim(), _password.text, _nickname.text.trim());
      } else {
        await app.login(_phone.text.trim(), _password.text);
      }
    } on ApiException catch (e) {
      if (mounted) showToast(context, e.message, error: true);
    } catch (e) {
      if (mounted) showToast(context, '操作失败，请稍后重试', error: true);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          Container(
            height: 360,
            decoration: const BoxDecoration(
              gradient: AppColors.headerGradient,
              borderRadius: BorderRadius.vertical(bottom: Radius.circular(56)),
            ),
          ),
          Positioned(
            top: -40,
            right: -40,
            child: _bubble(180, AppColors.lime.withValues(alpha: 0.18)),
          ),
          Positioned(
            top: 120,
            left: -50,
            child: _bubble(140, Colors.white.withValues(alpha: 0.08)),
          ),
          SafeArea(
            child: SingleChildScrollView(
              padding: const EdgeInsets.symmetric(horizontal: 24),
              child: Column(
                children: [
                  const SizedBox(height: 16),
                  Align(
                    alignment: Alignment.topRight,
                    child: IconButton(
                      tooltip: '后端服务设置',
                      onPressed: () => Navigator.push(context,
                          MaterialPageRoute(builder: (_) => const ServerSettingsScreen())),
                      icon: const Icon(Icons.settings_rounded, color: Colors.white70),
                    ),
                  ),
                  ClipRRect(
                    borderRadius: BorderRadius.circular(26),
                    child: Image.asset('assets/icons/app_icon.png', width: 96, height: 96),
                  ),
                  const SizedBox(height: 14),
                  const Text('绿循环',
                      style: TextStyle(color: Colors.white, fontSize: 30, fontWeight: FontWeight.w900)),
                  const SizedBox(height: 6),
                  Text('回收 · 积分 · 减碳 · 美好生活',
                      style: TextStyle(color: Colors.white.withValues(alpha: 0.85), fontSize: 14)),
                  const SizedBox(height: 36),
                  Container(
                    padding: const EdgeInsets.all(24),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(AppRadius.xl),
                      boxShadow: AppShadows.soft,
                    ),
                    child: Form(
                      key: _formKey,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          _tabs(),
                          const SizedBox(height: 22),
                          if (_isRegister) ...[
                            TextFormField(
                              controller: _nickname,
                              decoration: const InputDecoration(
                                  labelText: '昵称', prefixIcon: Icon(Icons.face_rounded)),
                              validator: (v) => (v == null || v.trim().isEmpty) ? '请输入昵称' : null,
                            ),
                            const SizedBox(height: 14),
                          ],
                          TextFormField(
                            controller: _phone,
                            keyboardType: TextInputType.phone,
                            decoration: const InputDecoration(
                                labelText: '手机号', prefixIcon: Icon(Icons.phone_android_rounded)),
                            validator: (v) =>
                                (v == null || v.trim().length != 11) ? '请输入 11 位手机号' : null,
                          ),
                          const SizedBox(height: 14),
                          TextFormField(
                            controller: _password,
                            obscureText: _obscure,
                            decoration: InputDecoration(
                              labelText: '密码',
                              prefixIcon: const Icon(Icons.lock_rounded),
                              suffixIcon: IconButton(
                                icon: Icon(_obscure
                                    ? Icons.visibility_off_rounded
                                    : Icons.visibility_rounded),
                                onPressed: () => setState(() => _obscure = !_obscure),
                              ),
                            ),
                            validator: (v) => (v == null || v.length < 6) ? '密码至少 6 位' : null,
                          ),
                          const SizedBox(height: 24),
                          SizedBox(
                            height: 52,
                            child: ElevatedButton(
                              onPressed: _loading ? null : _submit,
                              child: _loading
                                  ? const SizedBox(
                                      width: 22,
                                      height: 22,
                                      child: CircularProgressIndicator(
                                          color: Colors.white, strokeWidth: 2.5))
                                  : Text(_isRegister ? '注册并登录' : '登录'),
                            ),
                          ),
                          if (!_isRegister) ...[
                            const SizedBox(height: 14),
                            Container(
                              padding: const EdgeInsets.all(12),
                              decoration: BoxDecoration(
                                color: AppColors.yellow.withValues(alpha: 0.18),
                                borderRadius: BorderRadius.circular(AppRadius.sm),
                              ),
                              child: const Row(
                                children: [
                                  Icon(Icons.info_outline_rounded, size: 18, color: AppColors.amber),
                                  SizedBox(width: 8),
                                  Expanded(
                                    child: Text('演示账号：13912345678 / 123456',
                                        style: TextStyle(fontSize: 12, color: AppColors.textDark)),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 32),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _tabs() {
    return Container(
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: AppColors.bg,
        borderRadius: BorderRadius.circular(AppRadius.md),
      ),
      child: Row(
        children: [
          _tab('登录', !_isRegister, () => setState(() => _isRegister = false)),
          _tab('注册', _isRegister, () => setState(() => _isRegister = true)),
        ],
      ),
    );
  }

  Widget _tab(String text, bool selected, VoidCallback onTap) {
    return Expanded(
      child: GestureDetector(
        onTap: onTap,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          padding: const EdgeInsets.symmetric(vertical: 10),
          decoration: BoxDecoration(
            color: selected ? AppColors.deepGreen : Colors.transparent,
            borderRadius: BorderRadius.circular(AppRadius.sm),
          ),
          alignment: Alignment.center,
          child: Text(text,
              style: TextStyle(
                  color: selected ? Colors.white : AppColors.textGrey,
                  fontWeight: FontWeight.w700)),
        ),
      ),
    );
  }

  Widget _bubble(double size, Color color) => Container(
        width: size,
        height: size,
        decoration: BoxDecoration(color: color, shape: BoxShape.circle),
      );
}
