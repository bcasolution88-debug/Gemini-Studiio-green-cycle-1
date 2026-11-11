#!/usr/bin/env bash
# A1: 生产 Web 构建 —— 排除设计图库、生成带长缓存的产物
# 用法：scripts/build_web_release.sh [API_BASE_URL]
set -euo pipefail
cd "$(dirname "$0")/.."
API="${1:-${API_BASE_URL:-}}"
DEFINES=(--dart-define=flutter.inspector.structuredErrors=false --dart-define=SHOW_DEMO_ACCOUNTS=false)
[ -n "$API" ] && DEFINES+=(--dart-define=API_BASE_URL="$API")
flutter build web --release "${DEFINES[@]}"
rm -rf build/web/ui_design                       # 74 MB 设计图库仅供沙箱预览
# 写入 Caddy/Nginx 可用的缓存提示：带 hash 的资源永久缓存，index.html 不缓存
cat > build/web/_headers <<'H'
/index.html
  Cache-Control: no-cache
/flutter_bootstrap.js
  Cache-Control: no-cache
/version.json
  Cache-Control: no-cache
/main.dart.js
  Cache-Control: public, max-age=31536000, immutable
/canvaskit/*
  Cache-Control: public, max-age=31536000, immutable
/assets/*
  Cache-Control: public, max-age=604800
H
echo "✅ build/web 产物：$(du -sh build/web | cut -f1)（已排除 ui_design，演示账号已关闭）"
