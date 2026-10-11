// 数据模型（与后端 API 字段一一对应，均使用安全解析）

double _d(dynamic v) => v == null ? 0 : (v is num ? v.toDouble() : double.tryParse('$v') ?? 0);
int _i(dynamic v) => v == null ? 0 : (v is num ? v.toInt() : int.tryParse('$v') ?? 0);
String _s(dynamic v, [String def = '']) => v?.toString() ?? def;
bool _b(dynamic v) => v == true || v == 1 || v == '1' || v == 'true';

class User {
  final String id;
  final String phone;
  final String nickname;
  final String avatar;
  final String role; // user / recycler / admin
  final String status; // active / disabled
  final int completedOrders;
  final int level;
  final String levelName;
  final int points;
  final int totalPoints;
  final double totalWeight;
  final double totalCarbon;
  final int? nextLevelPoints;
  final int pointsToNextLevel;

  User({
    required this.id,
    required this.phone,
    required this.nickname,
    required this.avatar,
    this.role = 'user',
    this.status = 'active',
    this.completedOrders = 0,
    required this.level,
    required this.levelName,
    required this.points,
    required this.totalPoints,
    required this.totalWeight,
    required this.totalCarbon,
    required this.nextLevelPoints,
    required this.pointsToNextLevel,
  });

  factory User.fromJson(Map<String, dynamic> j) => User(
        id: _s(j['id']),
        phone: _s(j['phone']),
        nickname: _s(j['nickname'], '环保用户'),
        avatar: _s(j['avatar']),
        role: _s(j['role'], 'user'),
        status: _s(j['status'], 'active'),
        completedOrders: _i(j['completed_orders']),
        level: _i(j['level']),
        levelName: _s(j['level_name'], '环保新人'),
        points: _i(j['points']),
        totalPoints: _i(j['total_points']),
        totalWeight: _d(j['total_weight']),
        totalCarbon: _d(j['total_carbon']),
        nextLevelPoints: j['next_level_points'] == null ? null : _i(j['next_level_points']),
        pointsToNextLevel: _i(j['points_to_next_level']),
      );

  double get levelProgress {
    if (nextLevelPoints == null || nextLevelPoints == 0) return 1;
    final p = 1 - pointsToNextLevel / nextLevelPoints!;
    return p.clamp(0, 1);
  }

  bool get isRecycler => role == 'recycler';
  bool get isAdmin => role == 'admin';
  bool get isDisabled => status == 'disabled';
  String get roleText => isAdmin ? '管理员' : (isRecycler ? '回收员' : '用户');
}

class RecycleCategory {
  final String id;
  final String name;
  final String icon;
  final String color;
  final double pricePerKg;
  final int pointsPerKg;
  final double carbonPerKg;
  final String description;

  RecycleCategory({
    required this.id,
    required this.name,
    required this.icon,
    required this.color,
    required this.pricePerKg,
    required this.pointsPerKg,
    required this.carbonPerKg,
    required this.description,
  });

  factory RecycleCategory.fromJson(Map<String, dynamic> j) => RecycleCategory(
        id: _s(j['id']),
        name: _s(j['name']),
        icon: _s(j['icon']),
        color: _s(j['color'], '#43A047'),
        pricePerKg: _d(j['price_per_kg']),
        pointsPerKg: _i(j['points_per_kg']),
        carbonPerKg: _d(j['carbon_per_kg']),
        description: _s(j['description']),
      );
}

class Address {
  final String id;
  final String contactName;
  final String phone;
  final String region;
  final String detail;
  final bool isDefault;
  final double? lat;
  final double? lng;

  Address({
    required this.id,
    required this.contactName,
    required this.phone,
    required this.region,
    required this.detail,
    required this.isDefault,
    this.lat,
    this.lng,
  });

  factory Address.fromJson(Map<String, dynamic> j) => Address(
        id: _s(j['id']),
        contactName: _s(j['contact_name']),
        phone: _s(j['phone']),
        region: _s(j['region']),
        detail: _s(j['detail']),
        isDefault: _b(j['is_default']),
        lat: j['lat'] == null ? null : _d(j['lat']),
        lng: j['lng'] == null ? null : _d(j['lng']),
      );

  String get full => '$region $detail';
}

class OrderItem {
  final String categoryId;
  final String categoryName;
  final double estimatedWeight;
  final double? actualWeight;

  OrderItem({
    required this.categoryId,
    required this.categoryName,
    required this.estimatedWeight,
    this.actualWeight,
  });

  factory OrderItem.fromJson(Map<String, dynamic> j) => OrderItem(
        categoryId: _s(j['category_id']),
        categoryName: _s(j['category_name']),
        estimatedWeight: _d(j['estimated_weight']),
        actualWeight: j['actual_weight'] == null ? null : _d(j['actual_weight']),
      );
}

class RecycleOrder {
  final String id;
  final String orderNo;
  final String status;
  final String addressText;
  final String contactName;
  final String contactPhone;
  final String appointmentTime;
  final String remark;
  final double estimatedWeight;
  final double? actualWeight;
  final double? amount;
  final int pointsEarned;
  final double carbonSaved;
  final String? recyclerId;
  final String? recyclerName;
  final String? recyclerPhone;
  final String? recyclerAvatar;
  final double? lat;
  final double? lng;
  final bool paid;
  final String? payMethod;
  final RecyclerLocation? recyclerLocation;
  final String createdAt;
  final String updatedAt;
  final List<OrderItem> items;

  RecycleOrder({
    required this.id,
    required this.orderNo,
    required this.status,
    required this.addressText,
    required this.contactName,
    required this.contactPhone,
    required this.appointmentTime,
    required this.remark,
    required this.estimatedWeight,
    required this.actualWeight,
    required this.amount,
    required this.pointsEarned,
    required this.carbonSaved,
    this.recyclerId,
    required this.recyclerName,
    required this.recyclerPhone,
    required this.recyclerAvatar,
    this.lat,
    this.lng,
    this.paid = false,
    this.payMethod,
    this.recyclerLocation,
    required this.createdAt,
    required this.updatedAt,
    required this.items,
  });

  factory RecycleOrder.fromJson(Map<String, dynamic> j) => RecycleOrder(
        id: _s(j['id']),
        orderNo: _s(j['order_no']),
        status: _s(j['status'], 'pending'),
        addressText: _s(j['address_text']),
        contactName: _s(j['contact_name']),
        contactPhone: _s(j['contact_phone']),
        appointmentTime: _s(j['appointment_time']),
        remark: _s(j['remark']),
        estimatedWeight: _d(j['estimated_weight']),
        actualWeight: j['actual_weight'] == null ? null : _d(j['actual_weight']),
        amount: j['amount'] == null ? null : _d(j['amount']),
        pointsEarned: _i(j['points_earned']),
        carbonSaved: _d(j['carbon_saved']),
        recyclerId: j['recycler_id']?.toString(),
        recyclerName: j['recycler_name']?.toString(),
        recyclerPhone: j['recycler_phone']?.toString(),
        recyclerAvatar: j['recycler_avatar']?.toString(),
        lat: j['lat'] == null ? null : _d(j['lat']),
        lng: j['lng'] == null ? null : _d(j['lng']),
        paid: _b(j['paid']),
        payMethod: j['pay_method']?.toString(),
        recyclerLocation: j['recycler_location'] == null
            ? null
            : RecyclerLocation.fromJson(Map<String, dynamic>.from(j['recycler_location'])),
        createdAt: _s(j['created_at']),
        updatedAt: _s(j['updated_at']),
        items: ((j['items'] as List?) ?? [])
            .map((e) => OrderItem.fromJson(Map<String, dynamic>.from(e)))
            .toList(),
      );

  String get statusText {
    switch (status) {
      case 'pending':
        return '待接单';
      case 'accepted':
        return '已接单';
      case 'in_progress':
        return '回收员正在赶来';
      case 'completed':
        return '已完成';
      case 'cancelled':
        return '已取消';
      default:
        return status;
    }
  }

  bool get isActive => status == 'pending' || status == 'accepted' || status == 'in_progress';
  bool get canCancel => status == 'pending' || status == 'accepted';
  String get categoryNames => items.map((e) => e.categoryName).join('、');
  bool get hasLocation => lat != null && lng != null;
  String get payMethodText {
    switch (payMethod) {
      case 'cash':
        return '现金';
      case 'wechat':
        return '微信';
      case 'alipay':
        return '支付宝';
      default:
        return '未结算';
    }
  }
}

class RecyclerLocation {
  final String recyclerId;
  final double lat;
  final double lng;
  final double? heading;
  final double? speed;
  final String updatedAt;
  final String nickname;
  final String avatar;
  final String phone;
  final int activeTasks;

  RecyclerLocation({
    required this.recyclerId,
    required this.lat,
    required this.lng,
    this.heading,
    this.speed,
    required this.updatedAt,
    this.nickname = '',
    this.avatar = '',
    this.phone = '',
    this.activeTasks = 0,
  });

  factory RecyclerLocation.fromJson(Map<String, dynamic> j) => RecyclerLocation(
        recyclerId: _s(j['recycler_id']),
        lat: _d(j['lat']),
        lng: _d(j['lng']),
        heading: j['heading'] == null ? null : _d(j['heading']),
        speed: j['speed'] == null ? null : _d(j['speed']),
        updatedAt: _s(j['updated_at']),
        nickname: _s(j['nickname']),
        avatar: _s(j['avatar']),
        phone: _s(j['phone']),
        activeTasks: _i(j['active_tasks']),
      );
}

class AppNotification {
  final String id;
  final String type; // order / points / wallet / system
  final String title;
  final String body;
  final String? relatedId;
  final bool isRead;
  final String createdAt;

  AppNotification({
    required this.id,
    required this.type,
    required this.title,
    required this.body,
    this.relatedId,
    required this.isRead,
    required this.createdAt,
  });

  factory AppNotification.fromJson(Map<String, dynamic> j) => AppNotification(
        id: _s(j['id']),
        type: _s(j['type'], 'system'),
        title: _s(j['title']),
        body: _s(j['body']),
        relatedId: j['related_id']?.toString(),
        isRead: _b(j['is_read']),
        createdAt: _s(j['created_at']),
      );

  AppNotification copyWith({bool? isRead}) => AppNotification(
        id: id, type: type, title: title, body: body, relatedId: relatedId,
        isRead: isRead ?? this.isRead, createdAt: createdAt);
}

class WalletRecord {
  final String id;
  final String type; // income / payout / withdraw
  final double amount;
  final String title;
  final String? relatedId;
  final String createdAt;

  WalletRecord({
    required this.id,
    required this.type,
    required this.amount,
    required this.title,
    this.relatedId,
    required this.createdAt,
  });

  factory WalletRecord.fromJson(Map<String, dynamic> j) => WalletRecord(
        id: _s(j['id']),
        type: _s(j['type']),
        amount: _d(j['amount']),
        title: _s(j['title']),
        relatedId: j['related_id']?.toString(),
        createdAt: _s(j['created_at']),
      );
}

class Withdrawal {
  final String id;
  final String recyclerId;
  final String recyclerName;
  final double amount;
  final String account;
  final String status; // pending / approved / rejected
  final String remark;
  final String createdAt;
  final String? reviewedAt;

  Withdrawal({
    required this.id,
    required this.recyclerId,
    required this.recyclerName,
    required this.amount,
    required this.account,
    required this.status,
    required this.remark,
    required this.createdAt,
    this.reviewedAt,
  });

  factory Withdrawal.fromJson(Map<String, dynamic> j) => Withdrawal(
        id: _s(j['id']),
        recyclerId: _s(j['recycler_id']),
        recyclerName: _s(j['recycler_name']),
        amount: _d(j['amount']),
        account: _s(j['account']),
        status: _s(j['status'], 'pending'),
        remark: _s(j['remark']),
        createdAt: _s(j['created_at']),
        reviewedAt: j['reviewed_at']?.toString(),
      );

  String get statusText => status == 'approved' ? '已打款' : (status == 'rejected' ? '已驳回' : '审核中');
}

class WalletData {
  final double balance;
  final double incomeTotal;
  final double incomeMonth;
  final double withdrawn;
  final double pendingWithdraw;
  final double paidToUsersTotal;
  final double commissionRate;
  final double baseFee;
  final List<WalletRecord> records;
  final List<Withdrawal> withdrawals;
  final List<RecycleOrder> unpaidOrders;

  WalletData({
    required this.balance,
    required this.incomeTotal,
    required this.incomeMonth,
    required this.withdrawn,
    required this.pendingWithdraw,
    required this.paidToUsersTotal,
    required this.commissionRate,
    required this.baseFee,
    required this.records,
    required this.withdrawals,
    required this.unpaidOrders,
  });

  factory WalletData.fromJson(Map<String, dynamic> j) => WalletData(
        balance: _d(j['balance']),
        incomeTotal: _d(j['income_total']),
        incomeMonth: _d(j['income_month']),
        withdrawn: _d(j['withdrawn']),
        pendingWithdraw: _d(j['pending_withdraw']),
        paidToUsersTotal: _d(j['paid_to_users_total']),
        commissionRate: _d(j['commission_rate']),
        baseFee: _d(j['base_fee']),
        records: ((j['records'] as List?) ?? []).map((e) => WalletRecord.fromJson(Map<String, dynamic>.from(e))).toList(),
        withdrawals: ((j['withdrawals'] as List?) ?? []).map((e) => Withdrawal.fromJson(Map<String, dynamic>.from(e))).toList(),
        unpaidOrders: ((j['unpaid_orders'] as List?) ?? []).map((e) => RecycleOrder.fromJson(Map<String, dynamic>.from(e))).toList(),
      );
}

class PointRecord {
  final String id;
  final String type;
  final int amount;
  final String title;
  final String createdAt;

  PointRecord({
    required this.id,
    required this.type,
    required this.amount,
    required this.title,
    required this.createdAt,
  });

  factory PointRecord.fromJson(Map<String, dynamic> j) => PointRecord(
        id: _s(j['id']),
        type: _s(j['type']),
        amount: _i(j['amount']),
        title: _s(j['title']),
        createdAt: _s(j['created_at']),
      );
}

class Product {
  final String id;
  final String name;
  final String description;
  final String image;
  final int points;
  final int stock;
  final String category;
  final bool isHot;
  final int sold;

  Product({
    required this.id,
    required this.name,
    required this.description,
    required this.image,
    required this.points,
    required this.stock,
    required this.category,
    required this.isHot,
    required this.sold,
  });

  factory Product.fromJson(Map<String, dynamic> j) => Product(
        id: _s(j['id']),
        name: _s(j['name']),
        description: _s(j['description']),
        image: _s(j['image']),
        points: _i(j['points']),
        stock: _i(j['stock']),
        category: _s(j['category']),
        isHot: _b(j['is_hot']),
        sold: _i(j['sold']),
      );
}

class Redemption {
  final String id;
  final String userNickname;
  final String userPhone;
  final String productName;
  final String productImage;
  final int points;
  final int quantity;
  final String status;
  final String code;
  final String createdAt;

  Redemption({
    required this.id,
    this.userNickname = '',
    this.userPhone = '',
    required this.productName,
    required this.productImage,
    required this.points,
    required this.quantity,
    required this.status,
    required this.code,
    required this.createdAt,
  });

  factory Redemption.fromJson(Map<String, dynamic> j) => Redemption(
        id: _s(j['id']),
        userNickname: _s(j['user_nickname']),
        userPhone: _s(j['user_phone']),
        productName: _s(j['product_name']),
        productImage: _s(j['product_image']),
        points: _i(j['points']),
        quantity: _i(j['quantity']),
        status: _s(j['status']),
        code: _s(j['code']),
        createdAt: _s(j['created_at']),
      );

  String get statusText {
    switch (status) {
      case 'pending':
        return '待发放';
      case 'shipped':
        return '已发货';
      case 'completed':
        return '已完成';
      default:
        return status;
    }
  }
}

class Article {
  final String id;
  final String title;
  final String summary;
  final String content;
  final String cover;
  final String category;
  final String author;
  final int views;
  final int likes;
  final String publishedAt;

  Article({
    required this.id,
    required this.title,
    required this.summary,
    required this.content,
    required this.cover,
    required this.category,
    required this.author,
    required this.views,
    required this.likes,
    required this.publishedAt,
  });

  factory Article.fromJson(Map<String, dynamic> j) => Article(
        id: _s(j['id']),
        title: _s(j['title']),
        summary: _s(j['summary']),
        content: _s(j['content']),
        cover: _s(j['cover']),
        category: _s(j['category']),
        author: _s(j['author']),
        views: _i(j['views']),
        likes: _i(j['likes']),
        publishedAt: _s(j['published_at']),
      );
}

class Achievement {
  final String id;
  final String name;
  final String description;
  final String icon;
  final double conditionValue;
  final double current;
  final bool unlocked;

  Achievement({
    required this.id,
    required this.name,
    required this.description,
    required this.icon,
    required this.conditionValue,
    required this.current,
    required this.unlocked,
  });

  factory Achievement.fromJson(Map<String, dynamic> j) => Achievement(
        id: _s(j['id']),
        name: _s(j['name']),
        description: _s(j['description']),
        icon: _s(j['icon']),
        conditionValue: _d(j['condition_value']),
        current: _d(j['current']),
        unlocked: _b(j['unlocked']),
      );

  double get progress => conditionValue == 0 ? 1 : (current / conditionValue).clamp(0, 1);
}

class UserStats {
  final int orderCount;
  final double totalWeight;
  final double totalCarbon;
  final int totalPoints;
  final double monthWeight;
  final double monthCarbon;
  final int monthPoints;
  final double treesEquivalent;
  final List<Map<String, dynamic>> trend;
  final List<Map<String, dynamic>> byCategory;
  final List<Achievement> achievements;

  UserStats({
    required this.orderCount,
    required this.totalWeight,
    required this.totalCarbon,
    required this.totalPoints,
    required this.monthWeight,
    required this.monthCarbon,
    required this.monthPoints,
    required this.treesEquivalent,
    required this.trend,
    required this.byCategory,
    required this.achievements,
  });

  factory UserStats.fromJson(Map<String, dynamic> j) => UserStats(
        orderCount: _i(j['order_count']),
        totalWeight: _d(j['total_weight']),
        totalCarbon: _d(j['total_carbon']),
        totalPoints: _i(j['total_points']),
        monthWeight: _d(j['month_weight']),
        monthCarbon: _d(j['month_carbon']),
        monthPoints: _i(j['month_points']),
        treesEquivalent: _d(j['trees_equivalent']),
        trend: ((j['trend'] as List?) ?? []).map((e) => Map<String, dynamic>.from(e)).toList(),
        byCategory: ((j['by_category'] as List?) ?? []).map((e) => Map<String, dynamic>.from(e)).toList(),
        achievements: ((j['achievements'] as List?) ?? [])
            .map((e) => Achievement.fromJson(Map<String, dynamic>.from(e)))
            .toList(),
      );
}

class HomeData {
  final User user;
  final double todayCarbon;
  final bool signedToday;
  final List<RecycleCategory> categories;
  final RecycleOrder? activeOrder;
  final List<Product> hotProducts;
  final List<Article> news;

  HomeData({
    required this.user,
    required this.todayCarbon,
    required this.signedToday,
    required this.categories,
    required this.activeOrder,
    required this.hotProducts,
    required this.news,
  });

  factory HomeData.fromJson(Map<String, dynamic> j) => HomeData(
        user: User.fromJson(Map<String, dynamic>.from(j['user'] ?? {})),
        todayCarbon: _d(j['today_carbon']),
        signedToday: _b(j['signed_today']),
        categories: ((j['categories'] as List?) ?? [])
            .map((e) => RecycleCategory.fromJson(Map<String, dynamic>.from(e)))
            .toList(),
        activeOrder: j['active_order'] == null
            ? null
            : RecycleOrder.fromJson(Map<String, dynamic>.from(j['active_order'])),
        hotProducts: ((j['hot_products'] as List?) ?? [])
            .map((e) => Product.fromJson(Map<String, dynamic>.from(e)))
            .toList(),
        news: ((j['news'] as List?) ?? [])
            .map((e) => Article.fromJson(Map<String, dynamic>.from(e)))
            .toList(),
      );
}

// ---------------- 回收员端 ----------------
class RecyclerAgg {
  final int count;
  final double weight;
  final double amount;
  final double carbon;
  RecyclerAgg({required this.count, required this.weight, required this.amount, required this.carbon});
  factory RecyclerAgg.fromJson(Map<String, dynamic> j) => RecyclerAgg(
        count: _i(j['count']),
        weight: _d(j['weight']),
        amount: _d(j['amount']),
        carbon: _d(j['carbon']),
      );
}

class RecyclerStats {
  final User recycler;
  final RecyclerAgg today;
  final RecyclerAgg month;
  final RecyclerAgg total;
  final int activeTasks;
  final int hallPending;
  final double rating;
  final double balance;
  final int unpaidOrders;

  RecyclerStats({
    required this.recycler,
    required this.today,
    required this.month,
    required this.total,
    required this.activeTasks,
    required this.hallPending,
    required this.rating,
    this.balance = 0,
    this.unpaidOrders = 0,
  });

  factory RecyclerStats.fromJson(Map<String, dynamic> j) => RecyclerStats(
        recycler: User.fromJson(Map<String, dynamic>.from(j['recycler'] ?? {})),
        today: RecyclerAgg.fromJson(Map<String, dynamic>.from(j['today'] ?? {})),
        month: RecyclerAgg.fromJson(Map<String, dynamic>.from(j['month'] ?? {})),
        total: RecyclerAgg.fromJson(Map<String, dynamic>.from(j['total'] ?? {})),
        activeTasks: _i(j['active_tasks']),
        hallPending: _i(j['hall_pending']),
        rating: _d(j['rating']),
        balance: _d(j['balance']),
        unpaidOrders: _i(j['unpaid_orders']),
      );
}

// ---------------- 管理后台 ----------------
class AdminDashboard {
  final int users;
  final int recyclers;
  final int newUsersMonth;
  final int ordersTotal;
  final double weightTotal;
  final double amountTotal;
  final double carbonTotal;
  final int pointsTotal;
  final int todayOrdersCompleted;
  final double todayWeight;
  final double todayAmount;
  final int todayNewOrders;
  final Map<String, int> statusCounts;
  final List<Map<String, dynamic>> byCategory;
  final List<Map<String, dynamic>> trend;
  final List<Map<String, dynamic>> topRecyclers;
  final List<Map<String, dynamic>> topUsers;
  final int redemptionsTotal;
  final int redemptionsPoints;
  final int pendingRedemptions;
  final int pendingWithdrawals;
  final double pendingWithdrawalsAmount;

  AdminDashboard({
    required this.users,
    required this.recyclers,
    required this.newUsersMonth,
    required this.ordersTotal,
    required this.weightTotal,
    required this.amountTotal,
    required this.carbonTotal,
    required this.pointsTotal,
    required this.todayOrdersCompleted,
    required this.todayWeight,
    required this.todayAmount,
    required this.todayNewOrders,
    required this.statusCounts,
    required this.byCategory,
    required this.trend,
    required this.topRecyclers,
    required this.topUsers,
    required this.redemptionsTotal,
    required this.redemptionsPoints,
    required this.pendingRedemptions,
    this.pendingWithdrawals = 0,
    this.pendingWithdrawalsAmount = 0,
  });

  static List<Map<String, dynamic>> _lm(dynamic v) =>
      ((v as List?) ?? []).map((e) => Map<String, dynamic>.from(e)).toList();

  factory AdminDashboard.fromJson(Map<String, dynamic> j) => AdminDashboard(
        users: _i(j['users']),
        recyclers: _i(j['recyclers']),
        newUsersMonth: _i(j['new_users_month']),
        ordersTotal: _i(j['orders_total']),
        weightTotal: _d(j['weight_total']),
        amountTotal: _d(j['amount_total']),
        carbonTotal: _d(j['carbon_total']),
        pointsTotal: _i(j['points_total']),
        todayOrdersCompleted: _i(j['today_orders_completed']),
        todayWeight: _d(j['today_weight']),
        todayAmount: _d(j['today_amount']),
        todayNewOrders: _i(j['today_new_orders']),
        statusCounts: Map<String, dynamic>.from(j['status_counts'] ?? {})
            .map((k, v) => MapEntry(k, _i(v))),
        byCategory: _lm(j['by_category']),
        trend: _lm(j['trend']),
        topRecyclers: _lm(j['top_recyclers']),
        topUsers: _lm(j['top_users']),
        redemptionsTotal: _i(j['redemptions_total']),
        redemptionsPoints: _i(j['redemptions_points']),
        pendingRedemptions: _i(j['pending_redemptions']),
        pendingWithdrawals: _i(j['pending_withdrawals']),
        pendingWithdrawalsAmount: _d(j['pending_withdrawals_amount']),
      );
}
