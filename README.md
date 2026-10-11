# 绿循环 Green Cycle - APP 平台

绿色回收循环经济平台移动端 APP，包含 **Flutter 前端** 与 **FastAPI 后端** 的完整项目。

| 项目 | 技术栈 |
|---|---|
| 前端 | Flutter 3.35 / Dart 3.9 · Provider · http · shared_preferences · intl |
| 后端 | Python 3 · FastAPI · SQLite（零外部依赖，开箱即用） |
| 包名 | `com.greencycle.recycle` |

## 功能概览

- **首页**：碳积分 / 等级进度、回收品类与今日价格、预约上门回收入口、进行中订单、热门商品、环保资讯、每日签到
- **预约回收**：多品类选择 + 预估重量滑块、地址管理、上门时间段、实时预估金额 / 积分 / 减碳量
- **订单**：全部 / 待接单 / 进行中 / 已完成分类，订单详情含进度步骤条、回收员信息、回收明细、取消预约，并提供状态流转模拟
- **积分商城**：分类筛选、商品详情、积分兑换（生成兑换码）、兑换记录
- **环保资讯**：分类知识 / 环保科普 / 平台动态 / 人物故事，文章详情、阅读量、点赞
- **我的**：个人资料修改、累计数据、环保成就徽章、减碳报告（月度趋势、品类分布、植树当量）、积分明细、地址管理、后端地址设置、退出登录
- **账号**：手机号 + 密码登录 / 注册，本地持久化登录状态

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

```
手机号：13912345678
密  码：123456
```

### 4. 打包

```bash
flutter build web --release
flutter build apk --release --dart-define=API_BASE_URL=https://your-api.example.com
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

统一响应格式：`{"code": 0, "message": "success", "data": ...}`
