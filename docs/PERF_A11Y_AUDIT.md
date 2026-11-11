# 绿循环 Green Cycle · 性能与无障碍审计报告

> 版本：v1.3.1+6 → v1.3.2+7（首批修复）→ **v1.3.3+8（剩余建议全部落地）** · 日期：2026-11-11
> 方法：静态扫描（产物体积 / Dart 反模式）+ 运行时实测（Playwright + Chrome DevTools Protocol，Flutter Web CanvasKit，390×844@2x）+ 语义树导出（11 个页面）+ 320px 窄屏溢出检测。
> 脚本：`/home/user/ui_shots/audit_perf.py`、`audit_a11y.py`、`audit_textscale.py`（审计工具，不入仓库）。
> 说明：Web 实测反映 Dart 层逻辑与布局开销；Android 原生包的启动与帧率以 `scripts/device_smoke.sh` 真机数据为准。

---

## 一、结论速览

| 维度 | 评级 | 关键数据 |
|---|---|---|
| 运行时帧率 | 🟢 优 | 订单列表滚动 60 fps，0 掉帧，最差帧 17 ms |
| 内存 | 🟢 优 | JS 堆登录后 40 MB → 30 秒后 30 MB（GC 正常回收，无增长） |
| 网络开销 | 🟢 优 | 首屏 20 个请求；空闲 30 s 仅 2 次轮询（`unread-count`，15 s 间隔） |
| 首屏加载（Web） | 🟡 中 | Flutter 首帧 3.99 s；传输 4.28 MB（`main.dart.js` 3.9 MB 占 92%） |
| 主线程长任务（Web） | 🟡 中 | 登录 + 首页 30 s 内 64 个 >50 ms 任务，最长 1039 ms（dart2js 初始化 + 首次布局，为 Web 固有开销） |
| 产物体积 | 🟡 中 | `build/web` 105 MB，其中 **74 MB 是 `ui_design` 设计图库**（非应用代码，部署时应剔除） |
| 无障碍 · 标签 | 🟢 优（修复后） | 可交互元素无标签 3 → **0** |
| 无障碍 · 触控面积 | 🟢 优（修复后） | <44dp 触控目标 59 → **11**（剩余为视觉性小角标，见下） |
| 无障碍 · 对比度 | 🟢 优 | v1.1 已按 WCAG AA 全面调整（`AppColors.readable()`、语义色 token） |
| 窄屏 / 大字 | 🟢 优 | 320 px 宽度下 5 个用户端页面 0 次 RenderFlex 溢出 |

**本次已修复 5 项，剩余建议 6 项（见第四节）。**

---

## 二、性能审计

### 2.1 产物体积（静态）

| 项 | 大小 | 评价 |
|---|---|---|
| `build/web/main.dart.js` | 3.9 MB（gzip 后约 1.0 MB） | 正常范围；可通过 `--wasm` 或拆分延迟加载进一步优化 |
| `build/web/canvaskit/` | 26 MB（按需加载单变体 ≈ 1.5 MB） | 默认行为，仅加载 chromium 变体 |
| `build/web/ui_design/` | **74 MB** | ❗ 设计图库被打进 Web 产物。系从 `docs/ui_design` 复制到 `build/web` 供预览。生产部署请排除 |
| 字体 | MaterialIcons 27 KB + 系统 Roboto/Noto 子集（gstatic 按需） | 无自定义字体，无需子集化 |
| `assets/` | 2 张图标 80 KB | 极小 |
| 后端静态图 | 28 张，972 KB，最大 149 KB（600×800） | 合理；已加按显示尺寸解码（见修复 #4） |

**APK**：已在 V1.2 完成 R8 + 资源裁剪 + ABI 切分（arm64 20.5 MB）。

### 2.2 启动（Web 实测）

| 指标 | 值 |
|---|---|
| TTFB | 6.5 ms（本地） |
| DOMContentLoaded / load | 70 ms / 100 ms |
| First Paint / FCP | 3752 ms |
| Flutter 首帧可交互 | 3.99 s |
| 首屏请求数 / 传输量 | 20 / 4.28 MB |

分析：时间几乎全部消耗在下载 + 解析 3.9 MB `main.dart.js` 与 CanvasKit wasm，属 Flutter Web 的固有特征，与业务代码无关。**原生 Android 不存在此问题**（AOT 编译）。若 Web 端对外使用，建议：开启服务器 gzip/brotli（Caddy 已配置 `encode zstd gzip`，可降至约 1 MB）+ 长缓存 + 首屏骨架 HTML。

### 2.3 运行时（Web 实测）

| 场景 | 指标 | 值 | 评价 |
|---|---|---|---|
| 首页 + 登录 30 s | 长任务数（>50 ms） | 64，其中 >100 ms 29 个，最长 1039 ms | 主要集中在启动 0–4 s；稳定后无长任务 |
| 首页 + 登录 | JS 堆 | 40.2 MB → 30.2 MB（30 s 后） | 无泄漏 |
| 空闲 30 s | 网络请求 | 2 次（`notifications/unread-count`） | 轮询 15 s 一次，payload 极小 |
| 订单列表静止 | fps | 61 / 0 掉帧 | — |
| 订单列表快速滚动 8 次 | fps / 掉帧 / 最差帧 | 60 / 0 / 17 ms | 🟢 |

### 2.4 Dart 代码反模式扫描

| 检查项 | 结果 | 处置 |
|---|---|---|
| `Image.network` 未限制解码尺寸 | 2 处（`NetImage`、`Avatar`） | ✅ 已修复：按显示尺寸 × DPR（上限 2x）设置 `cacheWidth/Height` |
| 定时器未释放 | 5 个 `Timer.periodic`，均有对应 `dispose()`/`stop()` | ✅ 无问题 |
| `ListView(` 非 builder | 12 处，均为 ≤10 项的静态短列表（设置菜单等） | ✅ 可接受 |
| `shrinkWrap: true` | 2 处（嵌套在可滚动父级内的短列表） | 🟡 可接受；若列表增长改为 Sliver |
| `Opacity` widget | 2 处（后台禁用用户行、地图离线回收员） | 🟡 低频静态，可改 `withValues(alpha)` 着色 |
| `context.watch<AppProvider>()` 整页重建 | 14 处 | 🟡 建议高频页改 `context.select`（已在后台侧栏角标使用 2 处） |
| `BackdropFilter` | 0 | ✅ |
| `RepaintBoundary` | 0 | 🟡 地图/图表页可加，隔离动画区域 |
| `setState(() {})` 空重建 | 2 处（Tab 监听、称重输入） | 🟡 可接受 |
| 阴影 `BoxShadow` | 10 处 | ✅ 正常 |

---

## 三、无障碍审计

### 3.1 语义树实测（11 个页面，修复前 → 修复后）

| 页面 | 可交互元素 | 无标签 | <44dp 触控目标 |
|---|---|---|---|
| 登录 | 10 | 0 → 0 | 7 → 5 |
| 首页 | 23 | 1 → **0** | 4 → 2 |
| 订单列表 | 18 | 1 → **0** | 1 → **0** |
| 订单详情 | 4 | 0 → 0 | 2 → **0** |
| 商城 | 17 | 0 → 0 | 6 → **0** |
| 我的 | 17 | 0 → 0 | 6 → **0** |
| 预约下单 | 17 | 0 → 0 | 6 → **0** |
| 回收员大厅 | 23 | 1 → **0** | 8 → 2 |
| 回收员任务 | 17 | 0 → 0 | 1 → **0** |
| 后台概览 | 23 | 0 → 0 | 3 → 2 |
| 后台订单 | 18 | 0 → 0 | 15 → **0** |
| **合计** | 187 | **3 → 0** | **59 → 11** |

修复前的 3 个无标签元素：首页/订单页中央「一键回收」FAB（纯 `GestureDetector`）、回收员「接单开关」`Switch`。
修复前 59 个小目标主要来源：`IconButton` 默认 40dp、`ChoiceChip`/`FilterChip` 30–36dp、`SectionHeader`「更多 ›」19dp 高、后台筛选 Chip。

**剩余 11 个 <44dp**（均为可接受或非主要操作）：
- 登录页 3 个"演示账号快速填入"Chip（31dp 高，演示功能，生产可移除）+ 2 个登录/注册 Tab（40dp，内部含 48dp 命中区，为视觉尺寸）
- 通知铃铛角标数字（38dp，铃铛本身 ≥48dp，角标为装饰）×3 页
- 首页/回收员页横向商品卡片 12×200 的边缘节点（被裁剪的滚动残留，非真实目标）×2

### 3.2 其他维度

| 检查 | 结果 |
|---|---|
| 色彩对比度（WCAG 1.4.3 AA） | v1.1 已系统性修复：灰文本 #5F6F66（4.6:1）、金额/积分/状态色全部使用深色 token，`StatusChip` 文本经 `AppColors.readable()`；本次未发现回退 |
| 320 px 窄屏（等效文字放大 1.22x） | 首页/订单/商城/资讯/我的滚动全程 **0 次溢出告警**，底部操作栏不遮挡 |
| 读屏语义 | 全部 `IconButton` 含 `tooltip`；`StatusChip`「状态: xxx」；`ListSkeleton`/`LoadingState`「加载中」liveRegion；`ErrorState` liveRegion；骨架装饰块 `ExcludeSemantics` |
| 动效 | 仅骨架微光（1.1 s 循环）与页面过渡；无自动播放/闪烁内容 |
| 键盘/焦点（Web） | Material 组件默认支持 Tab 焦点；`GestureDetector` 卡片不可聚焦（见建议 B3） |

---

## 四、本次修复（已随 v1.3.2 提交）

| # | 修复 | 文件 |
|---|---|---|
| 1 | 全局 `materialTapTargetSize: padded` + `IconButton` 最小 48×48 | `theme/app_theme.dart` |
| 2 | 「一键回收」FAB 增加 `Semantics(button, label, hint)`，图标 `ExcludeSemantics` | `screens/main_shell.dart` |
| 3 | `SectionHeader` 的「更多 ›」改为 `InkWell` + 48dp 最小约束 + 按钮语义 | `widgets/common_widgets.dart` |
| 4 | `NetImage` / `Avatar` 按显示尺寸解码（`cacheWidth/Height`，DPR 上限 2x），减少位图内存 | `widgets/common_widgets.dart` |
| 5 | 回收员「接单开关」增加 `Semantics(label/value/toggled)` | `screens/recycler/recycler_hall_screen.dart` |

回归：`flutter analyze` 0 · `flutter test` 通过 · 后端 e2e 94/94 · 登录页布局无变化（Chip 命中区扩大但视觉不变）。

## 五、剩余建议 → 已全部落地（v1.3.3）

| # | 建议 | 落地方式 | 实测效果 |
|---|---|---|---|
| A1 | 部署 Web 时排除 `ui_design` | 新增 `scripts/build_web_release.sh`：release 构建 + 删除 `ui_design` + 关闭演示账号 + 生成 `_headers` 缓存策略 | 产物 **105 MB → 32 MB** |
| A2 | Web 首屏骨架 + 长缓存 | `web/index.html` 内联品牌色启动页（logo/标题/进度条，`prefers-reduced-motion` 适配），监听 `flutter-first-frame` 淡出；`_headers` 对 `main.dart.js`/`canvaskit` 设 `immutable`；Web 图标/manifest 换为应用图标与品牌色 | **FCP 3752 ms → 96 ms**；Flutter 首帧 3.99 s → 3.38 s |
| A3 | `watch` → `select` | 首页仅订阅 `home`/`loadingHome`；商城仅订阅 `user` | 用户积分/通知变更不再触发首页整页重建 |
| A4 | 地图 `RepaintBoundary` | `widgets/order_map.dart` 整体包裹，三处地图页（订单详情/回收员任务/后台地图）同时受益 | 地图瓦片与标记动画隔离为独立图层 |
| B1 | 演示 Chip | 高度 31 → 36dp，加 `Semantics(button/selected)`；新增 `--dart-define=SHOW_DEMO_ACCOUNTS=false`（`kShowDemoAccounts`），正式包整块移除 | 正式包登录页小目标 5 → 0 |
| B2 | 内容卡片语义 | 首页品类卡/热门商品卡、商城商品卡、资讯卡 4 类卡片加 `Semantics(button, label, hint)`，内部装饰 `excludeSemantics` | 读屏朗读"纸类 ¥1.2/kg，按钮，预约上门回收"等完整信息 |
| B3 | TalkBack 走查 | 写入 `docs/QA_REGRESSION_CHECKLIST.md` 7.3 专项（T1–T17，含期望朗读文案） | 待真机执行 |

复测（v1.3.3）：可交互元素 189 个，无标签 **0**，<44dp **11**（全部为铃铛角标数字、演示 Chip 与横向滚动裁剪残留）；滚动 60 fps / 0 掉帧；堆内存 50 → 30 MB 正常回收；空闲 30 s 仍只有 2 次请求。

---

## 附录：测量脚本要点

- 性能：CDP `Performance.getMetrics`（JS 堆）、`PerformanceObserver('longtask')`、`requestAnimationFrame` 采样 2 s 计算 fps 与最差帧、`performance.getEntriesByType('resource')` 统计传输。
- 无障碍：点击 `flt-semantics-placeholder` 启用 Flutter 语义树后，遍历 `flt-semantics` 节点读取 `role/aria-label/flt-tappable/boundingRect`。
- 窄屏：320 px 视口 + 监听 console 中 `RenderFlex overflowed`。
