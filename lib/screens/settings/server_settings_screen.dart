import 'package:flutter/material.dart';

import '../../services/api_service.dart';
import '../../theme/app_theme.dart';
import '../../widgets/common_widgets.dart';

/// 后端服务地址设置（方便在不同环境切换 API 地址）
class ServerSettingsScreen extends StatefulWidget {
  const ServerSettingsScreen({super.key});

  @override
  State<ServerSettingsScreen> createState() => _ServerSettingsScreenState();
}

class _ServerSettingsScreenState extends State<ServerSettingsScreen> {
  late final TextEditingController _ctrl;
  bool? _ok;
  bool _testing = false;

  @override
  void initState() {
    super.initState();
    _ctrl = TextEditingController(text: ApiService.instance.baseUrl);
  }

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  Future<void> _test() async {
    setState(() {
      _testing = true;
      _ok = null;
    });
    await ApiService.instance.setBaseUrl(_ctrl.text);
    final ok = await ApiService.instance.ping();
    if (mounted) {
      setState(() {
        _ok = ok;
        _testing = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('后端服务设置')),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            SoftCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('API 地址', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 16)),
                  const SizedBox(height: 6),
                  const Text('Flutter 前端通过此地址访问 FastAPI 后端服务。Android 真机请填写局域网 IP，例如 http://192.168.1.10:8000',
                      style: TextStyle(color: AppColors.textGrey, fontSize: 12)),
                  const SizedBox(height: 16),
                  TextField(
                    controller: _ctrl,
                    keyboardType: TextInputType.url,
                    decoration: const InputDecoration(
                        hintText: 'http://10.0.2.2:8000', prefixIcon: Icon(Icons.dns_rounded)),
                  ),
                  const SizedBox(height: 14),
                  Row(
                    children: [
                      Expanded(
                        child: ElevatedButton.icon(
                          onPressed: _testing ? null : _test,
                          icon: _testing
                              ? const SizedBox(
                                  width: 16, height: 16,
                                  child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                              : const Icon(Icons.wifi_tethering_rounded),
                          label: const Text('保存并测试连接'),
                        ),
                      ),
                      const SizedBox(width: 10),
                      OutlinedButton(
                        onPressed: () async {
                          await ApiService.instance.resetBaseUrl();
                          _ctrl.text = ApiService.instance.baseUrl;
                          setState(() => _ok = null);
                        },
                        child: const Text('恢复默认'),
                      ),
                    ],
                  ),
                  if (_ok != null) ...[
                    const SizedBox(height: 14),
                    Row(
                      children: [
                        Icon(_ok! ? Icons.check_circle_rounded : Icons.error_rounded,
                            color: _ok! ? AppColors.forest : AppColors.coral),
                        const SizedBox(width: 8),
                        Text(_ok! ? '连接成功，后端服务正常' : '连接失败，请检查地址或后端是否已启动',
                            style: TextStyle(color: _ok! ? AppColors.forest : AppColors.coral)),
                      ],
                    ),
                  ],
                ],
              ),
            ),
            const SizedBox(height: 16),
            SoftCard(
              color: AppColors.yellow.withValues(alpha: 0.15),
              child: const Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('本地启动后端', style: TextStyle(fontWeight: FontWeight.w800)),
                  SizedBox(height: 8),
                  Text('cd backend && ./run.sh\n接口文档：http://localhost:8000/docs',
                      style: TextStyle(fontFamily: 'monospace', fontSize: 12)),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
