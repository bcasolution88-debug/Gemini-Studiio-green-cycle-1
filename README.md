# 绿循环 Green Cycle - APP 平台

绿色回收循环经济平台移动端 APP，包含 **Flutter 前端（用户端 / 回收员端 / 管理后台 三端合一）** 与 **FastAPI 后端** 的完整项目。

| 项目 | 技术栈 |
|---|---|
| 前端 | Flutter 3.35 / Dart 3.9 · Provider · http · shared_preferences · intl · flutter_map · geolocator · url_launcher |
| 后端 | Python 3 · FastAPI · SQLite（零外部依赖，开箱即用） |
| 包名 | `com.greencycle.recycle` |

## 三端角色

登录后根据账号角色自动进入对应端：

| 角色 | 演示账号 | 功能 |
|---|---|---|
| 👤 用户端 | `13912345678` / `123456` | 预约回收、订单跟踪、积分商城、环保资讯、减碳报告 |
| 🛵 回收员端 | `13800001111` / `123456` | 接单大厅抢单、我的任务、出发上门、现场称重结算、业绩统计 |
| 🛠 管理后台 | `13900000000` / `admin123` | 数据概览、订单管理（指派/取消）、用户管理（禁用/改角色）、品类调价、商品/资讯发布、兑换发货 |

其他回收员：`13800002222`、`13800003333`（密码 `123456`）

## 功能概览

### 用户端
- **首页**：碳积分 / 等级进度、回收品类与今日价格、预约上门回收入口、进行中订单、热门商品、环保资讯、每日签到
- **预约回收**：多品类选择 + 预估重量滑块、地址管理、上门时间段、实时预估金额 / 积分 / 减碳量
- **订单**：全部 / 待接单 / 进行中 / 已完成分类，订单详情含进度步骤条、回收员信息、回收明细、取消预约，并提供状态流转模拟
- **积分商城**：分类筛选、商品详情、积分兑换（生成兑换码）、兑换记录
- **环保资讯**：分类知识 / 环保科普 / 平台动态 / 人物故事，文章详情、阅读量、点赞
- **我的**：个人资料修改、累计数据、环保成就徽章、减碳报告（月度趋势、品类分布、植树当量）、积分明细、地址管理、后端地址设置、退出登录
- **账号**：手机号 + 密码登录 / 注册，本地持久化登录状态

### 跨端能力
- **消息通知**：订单状态变更（接单/出发/完成/取消/指派）、积分到账、回收款支付、提现审核结果自动生成通知；前端 15 秒轮询，铃铛角标 + 顶部横幅提醒，点击直达订单
- **地图与定位**：OpenStreetMap 地图，回收员开启位置共享后（GPS，Web/无权限时自动退化为模拟行进），用户端订单详情实时显示回收员位置与距离；一键调起系统地图导航 / 拨号
- **多语言**：简体中文 / English，「我的」页一键切换并持久化（`lib/l10n/app_localizations.dart`）。v1.2 起用户端页面全部覆盖：除 `t(key)` 键表外新增 `context.tr('中文原文')` 运行时字典（160+ 条），新增文案只需在 `_zh2en` 加一行；订单/兑换状态等模型文案也在展示处翻译
- **首次引导 + 空态**：首次启动 3 页引导（本地标记 `onboarding_seen_v1`），全部列表空态给出"下一步动作"提示而非仅"暂无数据"
- **首页本周战报**：首页顶部卡片按状态切换——有进行中订单显示订单进度；否则显示本周完成单数 / 重量 / 减碳 / 积分（≈ 种树 · 少开车 km）；无记录时显示首单引导
- **后台桌面宽屏**：管理后台在宽度 ≥ 1000px 时自动切换为左侧导航栏 + 1180px 内容区 + 表格布局（订单 `DataTable`、KPI 4 列），窄屏仍为底部导航
- **推送就绪（FCM v1 抽象层）**：见下文「推送」一节
- **三端 l10n 全覆盖（v1.3）**：回收员端与管理后台所有文案同样接入 `context.tr`，`_zh2en` 字典 400+ 条；`StatusChip` / `EmptyState` / `SectionHeader` / `ErrorState` 等公共组件内部自动翻译
- **无障碍（v1.3）**：全部 `IconButton` 带 `tooltip`（读屏可读）；状态标签、加载态、错误态带 `Semantics` 语义与 `liveRegion`；骨架屏对读屏隐藏内部装饰块
- **统一加载 / 错误态（v1.3）**：列表页统一 `ListSkeleton` 微光骨架屏，详情页 `LoadingState`，失败统一 `ErrorState`（带重试）

### 回收员端
- **接单大厅**：今日业绩概览、待接订单列表（品类/重量/时间/地址）、一键抢单
- **我的任务**：进行中 / 已完成，任务详情含客户信息、回收物品、备注
- **作业流程**：接单 → 出发上门 → 现场称重（按品类填写实际重量，实时计算应付金额与积分）→ 完成结算
- **我的**：本月 / 累计业绩（单数、重量、金额、减碳）
- **钱包与结算**：称重完成后「确认已付款」（现金/微信/支付宝）；每单服务费（基础 ¥8 + 订单金额 15%）自动入账；待结算订单 / 收益明细 / 提现记录；申请提现（≥ ¥10，金额冻结待审核）

### 管理后台
- **数据概览**：今日实时、核心 KPI、订单状态分布、近 14 天趋势、品类占比、回收员排行、用户贡献榜
- **订单管理**：状态筛选 + 关键词搜索，指派 / 改派回收员，取消订单
- **用户管理**：按角色筛选、搜索、禁用 / 启用账号、变更角色
- **运营管理**：品类回收价 / 积分 / 减碳系数调整、商品增删改、兑换发货流转、资讯发布 / 删除
- **财务导出**：提现审核（打款 / 驳回退款）；订单 / 用户 / 兑换 / 提现 / 积分流水 / 品类价格 6 类数据一键导出 CSV（UTF-8 BOM，Excel 直接打开）
- **回收员地图**：全部在线回收员实时分布、任务负载、待接订单与最近回收员距离

## 安全与鉴权（v1.1.0+）

- **JWT 鉴权**：登录/注册返回 `token`，客户端对所有受保护接口携带 `Authorization: Bearer <token>`；公开接口仅 `/api/health`、`/api/auth/*`、`GET /api/categories|products|articles`、`/static/*`。
- **身份一致性**：请求中的 `user_id / recycler_id / admin_id` 必须与 token 所属用户一致（管理员除外），否则 403；订单/地址接口另有所有权校验。
- **密码**：PBKDF2-SHA256 哈希存储；历史明文密码在首次登录时自动升级。
- **生产部署必须设置**：
  ```bash
  export GC_JWT_SECRET="$(openssl rand -hex 32)"   # 不设置将使用开发默认值（不安全）
  export GC_TOKEN_TTL_DAYS=30                       # 可选，token 有效期
  ```
- **静态资源**：商品图 / 文章封面 / 头像存放于 `backend/static/`，数据库保存相对路径（`/static/...`），前端按当前后端地址自动拼接，不依赖外部图床。

## 推送（v1.2.0+，FCM 就绪）

推送分三层，**今天即可编译发布，接入 Firebase 时只改一处**：

| 层 | 位置 | 现状 |
|---|---|---|
| 设备 token 注册 | `POST /api/push/register` / `unregister` / `GET /api/push/devices`，表 `device_tokens`（UPSERT，换账号自动迁移） | ✅ 已启用 |
| 发送器 | `backend/app/push.py`：`_notify()` 写站内信后调用 `push_to_user()`；FCM HTTP v1 发送、失效 token 自动清理、后台线程 | ✅ 无凭据时 dry-run 仅打日志 |
| 客户端 | `lib/services/push_service.dart`：`PushTokenSource` 抽象，默认 `LocalDeviceTokenSource`；登录注册 / 退出注销；`_Root` 监听前台恢复立即刷新通知 | ✅ 已启用 |

启用真实 FCM 推送：

```bash
pip install google-auth
export GC_FCM_SERVICE_ACCOUNT=/path/to/firebase-service-account.json   # 必需
export GC_FCM_PROJECT_ID=your-project-id                              # 可选，默认读 JSON 中 project_id
export GC_PUSH_DRY_RUN=1                                              # 可选，联调时只打日志
```

客户端接入 Firebase 时：添加 `firebase_core` / `firebase_messaging` 依赖与 `google-services.json`，实现一个 `PushTokenSource`（`getToken → FirebaseMessaging.instance.getToken()`，`onTokenRefresh → FirebaseMessaging.instance.onTokenRefresh`），在 `main()` 中 `PushService.instance.tokenSource = FcmTokenSource();` 即可。`GET /api/health` 的 `push` 字段可查看当前通道状态。

## 目录结构

> 仓库根目录下的 `app/`、`build.gradle.kts`、`settings.gradle.kts`、`gradle/`、`metadata.json`、`.env.example` 为 **Google AI Studio 生成的原生 Android（Kotlin / Compose）独立工程**（包名 `com.aistudio.greencycle.recycle`），与本 Flutter 项目相互独立、互不依赖。Flutter 工程使用 `android/` 目录，后端位于 `backend/`。
>
> **决策（已确认）：两套工程长期并行保留。** 原生工程由 AI Studio 侧维护，本仓库不对其做改动；Flutter 工程（`lib/`、`android/`、`backend/`）为当前发布的正式版本（APK/AAB 均由 Flutter 工程产出）。两者共用同一 FastAPI 后端接口契约（见 `backend/app/main.py`）。
>
> **UI 设计图**：`docs/ui_design/`（58 屏真实运行截图、三端总览、PDF 设计文档与 Figma 风格分组画板）。

```
.
├── lib/
│   ├── main.dart                 # 入口（启动页 → 登录 / 主框架）
│   ├── theme/app_theme.dart      # 多彩插画风格主题
│   ├── models/models.dart        # 数据模型（安全解析）
│   ├── services/api_service.dart # REST API 封装
│   ├── services/push_service.dart # 推送抽象层（FCM 就绪）
├── scripts/device_smoke.sh      # 真机冒烟脚本
├── docs/QA_REGRESSION_CHECKLIST.md # 真机回归清单
│   ├── providers/app_provider.dart
│   ├── widgets/common_widgets.dart
│   └── screens/
│       ├── auth/        登录 / 注册
│       ├── home/        首页
│       ├── recycle/     预约回收 / 订单列表 / 订单详情 / 地址
│       ├── shop/        商城 / 商品详情 / 兑换记录
│       ├── news/        资讯 / 文章详情
│       ├── profile/     我的 / 积分明细 / 减碳报告
│       ├── recycler/    回收员端：接单大厅 / 我的任务 / 任务详情(称重) / 我的
│       ├── admin/       管理后台：数据概览 / 订单 / 用户 / 运营 / 财务导出 / 回收员地图
│       ├── notifications/ 消息中心（三端共用）
│       └── settings/    后端地址设置
├── lib/l10n/            多语言（zh / en）
├── lib/widgets/order_map.dart  地图组件
├── lib/services/location_service.dart  定位上报 / 导航 / 拨号
├── store_assets/        Google Play 上架素材（图标 / 大图 / 5 张截图 / 文案 LISTING.md）
├── backend/
│   ├── app/main.py      # FastAPI 路由
│   ├── app/database.py  # SQLite 表结构
│   ├── app/seed.py      # 示例数据
│   ├── app/schemas.py   # 请求模型
│   ├── requirements.txt
│   └── run.sh
├── android/             # Android 工程（包名 com.greencycle.recycle，已集成图标）
└── assets/icons/        # APP 图标
```

## 快速开始

### 1. 启动后端

```bash
cd backend
./run.sh            # 等价于 pip install -r requirements.txt && uvicorn app.main:app --port 8000
```

- 接口文档：<http://localhost:8000/docs>
- 首次启动自动建表并写入示例数据（品类、商品、资讯、成就、演示账号与历史订单）

### 2. 运行前端

```bash
flutter pub get

# Web 预览
flutter run -d chrome --dart-define=API_BASE_URL=http://localhost:8000

# Android 模拟器（10.0.2.2 指向宿主机）
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8000

# Android 真机（填写电脑局域网 IP）
flutter run --dart-define=API_BASE_URL=http://192.168.x.x:8000
```

也可以在 APP 内「我的 → 后端服务设置」中随时修改并测试 API 地址。

### 3. 演示账号

见上文「三端角色」表格，登录页上可点击标签快速填入。

### 4. 打包

```bash
flutter build web --release

# Android APK（需在 android/key.properties 配置签名，否则回退到 debug 签名）
# 推荐：按 ABI 切分（每包约 17–21 MB，比通用包小 ~65%）
flutter build apk --release --split-per-abi --dart-define=API_BASE_URL=https://your-api.example.com
# 输出：app-arm64-v8a-release.apk（绝大多数现代手机）/ app-armeabi-v7a-release.apk / app-x86_64-release.apk
# 通用包（含 3 套 ABI，约 55 MB，仅用于内测分发）
flutter build apk --release --dart-define=API_BASE_URL=https://your-api.example.com

# Google Play 上架用 AAB
flutter build appbundle --release --dart-define=API_BASE_URL=https://your-api.example.com
# 输出：build/app/outputs/bundle/release/app-release.aab
```

上架素材与文案见 `store_assets/LISTING.md`。

**体积优化（v1.3.0+）**：release 构建已启用 R8 代码压缩 + 资源裁剪（`android/app/proguard-rules.pro`），AAB 启用 language / density / ABI 三维切分，Google Play 下发给单台设备的安装体积约 18–20 MB。通用 APK 体积主要来自三套 `libflutter.so` / `libapp.so`（约 53 MB），无法再压缩，请优先分发按 ABI 切分包或 AAB。

签名配置示例 `android/key.properties`（已加入 .gitignore）：

```
storePassword=xxx
keyPassword=xxx
keyAlias=release
storeFile=../release-key.jks
```

## 生产部署（Docker）

```bash
cd backend
cp .env.example .env            # 填写 GC_JWT_SECRET / GC_ADMIN_PHONE / GC_ADMIN_PASSWORD / API_DOMAIN / ACME_EMAIL
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml logs -f api
curl https://$API_DOMAIN/api/health
```

- `Dockerfile`：多阶段构建、非 root 运行、内置健康检查；数据卷 `/data` 持久化 SQLite
- `docker-compose.prod.yml`：API 不暴露公网端口，由 **Caddy** 反向代理并自动签发 Let's Encrypt 证书（`Caddyfile` 含 HSTS 等安全头与静态资源缓存）
- `docker-compose.yml`：本地开发（带演示数据、代码热更新）`docker compose up`
- `backup_db.sh`：SQLite 在线一致性备份 + 保留最近 14 份，建议加入 crontab：`0 3 * * * /opt/greencycle/backend/backup_db.sh /backups`

**生产模式（`GC_ENV=production`）行为**：未设置 `GC_JWT_SECRET` 拒绝启动；不写入演示账号/订单，仅按 `GC_ADMIN_PHONE/PASSWORD` 创建初始管理员（密码哈希）；CORS 仅放行 `GC_CORS_ORIGINS`。所有变量说明见 `backend/.env.example`。

> SQLite 适合单实例（`GC_WORKERS=1`）。日订单超过数万时建议迁移 PostgreSQL（`database.py` 仅需替换连接层）。

## 性能与无障碍审计

见 `docs/PERF_A11Y_AUDIT.md`（v1.3.2）：滚动 60 fps / 0 掉帧，内存无泄漏，空闲 30 s 仅 2 次轮询；可交互元素无标签 3 → 0，<44dp 触控目标 59 → 11；320 px 窄屏 0 溢出。
**部署 Web 端时请排除 `build/web/ui_design/`（74 MB 设计图库，仅沙箱预览用）**，可用 `flutter build web --release && rm -rf build/web/ui_design`。

## 上架与真机回归

- **Play 资料**：`store_assets/LISTING.md`（名称/说明/截图 7 张 + 平板 2 张/数据安全表单/内容分级/上架核对清单）
- **隐私政策 & 账号删除页**：随后端部署于 `/static/legal/privacy.html`、`/static/legal/account-deletion.html`；App 内「我的 → 隐私政策 / 注销账号」
- **账号注销接口**：`POST /api/account/delete`（密码校验、进行中订单/待审核提现拦截、个人信息即时匿名化、token 失效）
- **真机冒烟**：`scripts/device_smoke.sh`（自动识别 ABI 安装、冷启动计时、崩溃/ANR 扫描、权限审计、后端连通性）
- **回归清单**：`docs/QA_REGRESSION_CHECKLIST.md`（设备矩阵 + 9 大模块 90+ 用例 + 发布判定标准）

## 测试

```bash
# 前端静态分析 + 单元测试
flutter analyze && flutter test

# 后端端到端业务闭环测试（需先启动后端，建议使用干净数据库）
cd backend && rm -f green_cycle.db && uvicorn app.main:app --port 8000 &
python3 backend/tests/test_e2e.py
```

`test_e2e.py` 覆盖：注册登录与角色 → 预约（通知触发）→ 抢单/出发/位置 → 称重结算（金额/积分/减碳/服务费精确校验）→ 确认付款 → 提现冻结/驳回退款/打款 → 商城兑换发货 → 管理后台指派/取消/权限/禁用/调价/概览/地图/6 类导出 → 通知已读删除 → 统计/签到/首页，共 94 项断言（含 JWT 鉴权、接单开关/评价、推送 token 注册、账号注销）。

## 后端 API 一览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/auth/login · /api/auth/register | 登录 / 注册 |
| GET / PUT | /api/users/{id} | 用户信息 / 修改资料 |
| GET | /api/users/{id}/stats | 统计、趋势、成就 |
| POST | /api/users/sign-in | 每日签到 |
| GET | /api/home | 首页聚合数据 |
| GET | /api/categories | 回收品类与价格 |
| GET / POST / DELETE | /api/addresses | 地址管理 |
| GET / POST | /api/orders | 订单列表 / 创建预约 |
| GET | /api/orders/{id} | 订单详情 |
| POST | /api/orders/{id}/cancel · /advance | 取消 / 模拟流转 |
| GET | /api/points/records | 积分流水 |
| GET | /api/products · /api/products/{id} | 商品 |
| GET / POST | /api/redemptions | 兑换记录 / 兑换 |
| GET | /api/articles · /api/articles/{id} | 资讯 |
| POST | /api/articles/{id}/like | 点赞 |
| **回收员端** | | |
| GET | /api/recycler/hall | 接单大厅（待接单） |
| GET | /api/recycler/tasks?status=active\|completed | 我的任务 |
| POST | /api/recycler/orders/{id}/accept · /depart · /complete | 接单 / 出发 / 称重完成 |
| GET | /api/recycler/stats | 业绩统计 |
| **管理后台** | | |
| GET | /api/admin/dashboard | 数据概览 |
| GET | /api/admin/orders | 订单列表（筛选/搜索） |
| POST | /api/admin/orders/{id}/assign · /cancel | 指派 / 取消 |
| GET / PUT | /api/admin/users | 用户列表 / 状态与角色 |
| GET | /api/admin/recyclers | 可指派回收员 |
| PUT | /api/admin/categories/{id} | 品类调价 |
| POST / PUT / DELETE | /api/admin/products | 商品管理 |
| GET / PUT | /api/admin/redemptions | 兑换发货 |
| POST / DELETE | /api/admin/articles | 资讯管理 |
| **通知 / 定位 / 钱包 / 导出** | | |
| GET | /api/notifications · /unread-count | 消息列表 / 轮询未读 |
| POST / DELETE | /api/notifications/read · /{id} | 标记已读 / 删除 |
| POST / GET | /api/recycler/location | 上报 / 查询回收员位置 |
| GET | /api/admin/locations | 全部回收员位置 |
| POST | /api/recycler/orders/{id}/pay | 确认向用户支付回收款 |
| GET | /api/recycler/wallet | 钱包概览 / 明细 / 待结算 |
| POST | /api/recycler/withdraw | 申请提现 |
| GET / POST | /api/admin/withdrawals · /{id}/review | 提现列表 / 审核 |
| GET | /api/admin/export/{orders\|users\|redemptions\|withdrawals\|points\|categories} | CSV 导出 |

所有回收员 / 管理接口均校验账号角色（`recycler_id` / `admin_id`）。

统一响应格式：`{"code": 0, "message": "success", "data": ...}`
