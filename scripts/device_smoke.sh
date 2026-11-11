#!/usr/bin/env bash
# 绿循环 真机冒烟脚本：自动识别设备 ABI → 安装对应 APK → 冷启动计时 → 采集崩溃日志 → 基础检查
# 用法：
#   scripts/device_smoke.sh                  # 使用 build/app/outputs/flutter-apk 下最新的分 ABI 包
#   scripts/device_smoke.sh path/to/app.apk  # 指定 APK
#   API_BASE_URL=https://api.example.com scripts/device_smoke.sh   # 校验后端连通性
set -euo pipefail
PKG="com.greencycle.recycle"
ACT="$PKG/.MainActivity"
APK_DIR="build/app/outputs/flutter-apk"
LOG_DIR="build/device_smoke"; mkdir -p "$LOG_DIR"
TS=$(date +%Y%m%d_%H%M%S)

ok(){ echo "  ✅ $*"; }; warn(){ echo "  ⚠️  $*"; }; fail(){ echo "  ❌ $*"; FAILED=1; }
FAILED=0

echo "【0】设备"
command -v adb >/dev/null || { echo "未找到 adb，请安装 Android platform-tools"; exit 1; }
DEV_COUNT=$(adb devices | grep -cw device || true)
[ "$DEV_COUNT" -ge 1 ] || { echo "未检测到已授权的设备（adb devices）"; exit 1; }
ABI=$(adb shell getprop ro.product.cpu.abi | tr -d '\r')
MODEL=$(adb shell getprop ro.product.model | tr -d '\r')
SDK=$(adb shell getprop ro.build.version.sdk | tr -d '\r')
REL=$(adb shell getprop ro.build.version.release | tr -d '\r')
echo "  设备：$MODEL · Android $REL (API $SDK) · ABI $ABI"
[ "$SDK" -ge 21 ] && ok "API ≥ 21（minSdk）" || fail "系统版本低于 minSdk"

echo "【1】选择 APK"
if [ $# -ge 1 ]; then APK="$1"; else
  case "$ABI" in
    arm64-v8a)   APK="$APK_DIR/app-arm64-v8a-release.apk" ;;
    armeabi-v7a) APK="$APK_DIR/app-armeabi-v7a-release.apk" ;;
    x86_64)      APK="$APK_DIR/app-x86_64-release.apk" ;;
    *)           APK="$APK_DIR/app-release.apk" ;;
  esac
  [ -f "$APK" ] || APK="$APK_DIR/app-release.apk"
fi
[ -f "$APK" ] || { echo "APK 不存在：$APK（先执行 flutter build apk --release --split-per-abi）"; exit 1; }
SIZE=$(du -h "$APK" | cut -f1); echo "  使用：$APK ($SIZE)"

echo "【2】安装（覆盖升级，保留数据）"
adb install -r -d "$APK" >/dev/null && ok "安装成功" || fail "安装失败"
VER=$(adb shell dumpsys package $PKG | grep -m1 versionName | tr -d '\r' | sed 's/.*versionName=//')
VCODE=$(adb shell dumpsys package $PKG | grep -m1 versionCode | tr -d '\r' | sed 's/.*versionCode=\([0-9]*\).*/\1/')
echo "  已装版本：$VER ($VCODE)"
TSDK=$(adb shell dumpsys package $PKG | grep -m1 targetSdk | tr -d '\r' | sed 's/.*targetSdk=\([0-9]*\).*/\1/')
[ "${TSDK:-0}" -ge 34 ] && ok "targetSdk=$TSDK" || warn "targetSdk=$TSDK（Play 可能要求更高）"

echo "【3】权限声明"
PERMS=$(adb shell dumpsys package $PKG | sed -n '/requested permissions/,/install permissions/p' | tr -d '\r')
echo "$PERMS" | grep -q INTERNET && ok "INTERNET" || fail "缺少 INTERNET"
echo "$PERMS" | grep -q ACCESS_FINE_LOCATION && ok "ACCESS_FINE_LOCATION（回收员位置共享）" || warn "未声明精确定位"
echo "$PERMS" | grep -q ACCESS_BACKGROUND_LOCATION && fail "声明了后台定位（Play 需单独审核，应移除）" || ok "未声明后台定位"
echo "$PERMS" | grep -qE "READ_CONTACTS|READ_SMS|CAMERA|READ_EXTERNAL_STORAGE" && warn "声明了多余的敏感权限" || ok "无多余敏感权限"

echo "【4】冷启动"
adb shell am force-stop $PKG; adb logcat -c
START=$(adb shell am start -W -n "$ACT" 2>/dev/null | tr -d '\r')
echo "$START" | grep -q "Status: ok" && ok "Activity 启动成功" || fail "Activity 启动失败"
TT=$(echo "$START" | grep -m1 TotalTime | sed 's/.*: //'); echo "  冷启动 TotalTime：${TT:-?} ms"
[ "${TT:-9999}" -lt 3000 ] && ok "冷启动 < 3s" || warn "冷启动偏慢"
sleep 6

echo "【5】崩溃 / ANR 扫描（首 6 秒）"
adb logcat -d > "$LOG_DIR/logcat_$TS.txt"
if grep -qE "FATAL EXCEPTION|AndroidRuntime: .*Process: $PKG" "$LOG_DIR/logcat_$TS.txt"; then fail "发现 FATAL EXCEPTION → $LOG_DIR/logcat_$TS.txt"; else ok "无 Java 崩溃"; fi
grep -q "ANR in $PKG" "$LOG_DIR/logcat_$TS.txt" && fail "发现 ANR" || ok "无 ANR"
grep -qE "flutter.*(Unhandled Exception|EXCEPTION CAUGHT)" "$LOG_DIR/logcat_$TS.txt" && warn "Flutter 层有未处理异常，见日志" || ok "无 Flutter 未处理异常"
adb shell pidof $PKG >/dev/null && ok "进程存活" || fail "进程已退出"

echo "【6】内存 / 包体"
MEM=$(adb shell dumpsys meminfo $PKG 2>/dev/null | grep -m1 "TOTAL PSS" | awk '{print $3}')
[ -n "${MEM:-}" ] && echo "  PSS：$((MEM/1024)) MB" && { [ "$MEM" -lt 350000 ] && ok "内存 < 350MB" || warn "内存偏高"; }
INST=$(adb shell du -sk /data/app/*/$PKG*/ 2>/dev/null | awk '{s+=$1} END{print s}')
[ -n "${INST:-}" ] && echo "  安装占用：$((INST/1024)) MB"

echo "【7】后端连通性"
if [ -n "${API_BASE_URL:-}" ]; then
  if adb shell "curl -s -m 5 $API_BASE_URL/api/health" 2>/dev/null | grep -q '"status":"ok"'; then ok "设备可访问 $API_BASE_URL"
  elif curl -s -m 5 "$API_BASE_URL/api/health" | grep -q '"status":"ok"'; then warn "电脑可访问，但设备侧 curl 不可用/不可达，请在 App 内确认"
  else fail "后端不可达：$API_BASE_URL"; fi
  echo "$API_BASE_URL" | grep -q '^https://' && ok "HTTPS" || warn "非 HTTPS（Android 9+ 默认禁止明文流量）"
else warn "未设置 API_BASE_URL，跳过"; fi

echo "【8】截图留档"
adb exec-out screencap -p > "$LOG_DIR/launch_$TS.png" 2>/dev/null && ok "已保存 $LOG_DIR/launch_$TS.png"

echo; echo "=================================================="
if [ "$FAILED" -eq 0 ]; then echo "✅ 冒烟通过：$MODEL / $ABI / v$VER · 请继续执行 docs/QA_REGRESSION_CHECKLIST.md"; else echo "❌ 冒烟存在失败项，请查看上方输出与 $LOG_DIR/"; exit 1; fi
