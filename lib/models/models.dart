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

  Address({
    required this.id,
    required this.contactName,
    required this.phone,
    required this.region,
    required this.detail,
    required this.isDefault,
  });

  factory Address.fromJson(Map<String, dynamic> j) => Address(
        id: _s(j['id']),
        contactName: _s(j['contact_name']),
        phone: _s(j['phone']),
        region: _s(j['region']),
        detail: _s(j['detail']),
        isDefault: _b(j['is_default']),
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
  final String? recyclerName;
  final String? recyclerPhone;
  final String? recyclerAvatar;
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
    required this.recyclerName,
    required this.recyclerPhone,
    required this.recyclerAvatar,
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
        recyclerName: j['recycler_name']?.toString(),
        recyclerPhone: j['recycler_phone']?.toString(),
        recyclerAvatar: j['recycler_avatar']?.toString(),
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
  final String productName;
  final String productImage;
  final int points;
  final int quantity;
  final String status;
  final String code;
  final String createdAt;

  Redemption({
    required this.id,
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
