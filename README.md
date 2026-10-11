# 绿循环 Green Cycle - APP 平台

绿色回收循环经济平台移动端 APP，包含 **Flutter 前端（用户端 / 回收员端 / 管理后台 三端合一）** 与 **FastAPI 后端** 的完整项目。

| 项目 | 技术栈 |
|---|---|
| 前端 | Flutter 3.35 / Dart 3.9 · Provider · http · shared_preferences · intl |
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

### 回收员端
- **接单大厅**：今日业绩概览、待接订单列表（品类/重量/时间/地址）、一键抢单
- **我的任务**：进行中 / 已完成，任务详情含客户信息、回收物品、备注
- **作业流程**：接单 → 出发上门 → 现场称重（按品类填写实际重量，实时计算应付金额与积分）→ 完成结算
- **我的**：本月 / 累计业绩（单数、重量、金额、减碳）

### 管理后台
- **数据概览**：今日实时、核心 KPI、订单状态分布、近 14 天趋势、品类占比、回收员排行、用户贡献榜
- **订单管理**：状态筛选 + 关键词搜索，指派 / 改派回收员，取消订单
- **用户管理**：按角色筛选、搜索、禁用 / 启用账号、变更角色
- **运营管理**：品类回收价 / 积分 / 减碳系数调整、商品增删改、兑换发货流转、资讯发布 / 删除

## 目录结构

```
.
├── lib/
│   ├── main.dart                 # 入口（启动页 → 登录 / 主框架）
│   ├── theme/app_theme.dart      # 多彩插画风格主题
│   ├── models/models.dart        # 数据模型（安全解析）
│   ├── services/api_service.dart # REST API 封装
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
│       ├── admin/       管理后台：数据概览 / 订单 / 用户 / 运营
│       └── settings/    后端地址设置
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
flutter build apk --release --dart-define=API_BASE_URL=https://your-api.example.com
```

签名配置示例 `android/key.properties`（已加入 .gitignore）：

```
storePassword=xxx
keyPassword=xxx
keyAlias=release
storeFile=../release-key.jks
```

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

所有回收员 / 管理接口均校验账号角色（`recycler_id` / `admin_id`）。

统一响应格式：`{"code": 0, "message": "success", "data": ...}`
