package com.example.data.model

data class SubCategory(
    val id: String,
    val nameZh: String,
    val nameEn: String,
    val iconName: String,
    val isWasteRecycleRateAvailable: Boolean = false,
    val wasteMarketRate: String? = null
) {
    fun name(lang: Language): String = if (lang == Language.ZH) nameZh else nameEn
}

data class MajorCategory(
    val id: String,
    val nameZh: String,
    val nameEn: String,
    val iconName: String,
    val descriptionZh: String,
    val descriptionEn: String,
    val subCategories: List<SubCategory>,
    val requiresSpecialLogistics: Boolean = false
) {
    fun name(lang: Language): String = if (lang == Language.ZH) nameZh else nameEn
    fun description(lang: Language): String = if (lang == Language.ZH) descriptionZh else descriptionEn
}

object CategoryRepository {
    val majorCategories = listOf(
        MajorCategory(
            id = "cat_home",
            nameZh = "家居用品",
            nameEn = "Home Supplies",
            iconName = "chair",
            descriptionZh = "家具、床品、各类居家生活用品",
            descriptionEn = "Furniture, bedding & household items",
            subCategories = listOf(
                SubCategory("sub_furniture", "家具", "Furniture", "chair"),
                SubCategory("sub_decor", "装饰", "Decorations", "brush"),
                SubCategory("sub_storage", "收纳", "Storage Boxes", "inventory_2")
            )
        ),
        MajorCategory(
            id = "cat_office",
            nameZh = "办公用品",
            nameEn = "Office Supplies",
            iconName = "desk",
            descriptionZh = "办公桌椅、文具耗材与办公设备",
            descriptionEn = "Office desks, chairs, stationery & machines",
            subCategories = listOf(
                SubCategory("sub_desk_chair", "桌椅", "Desks & Chairs", "desk"),
                SubCategory("sub_stationery", "文具", "Stationery", "edit"),
                SubCategory("sub_office_equipment", "办公设备", "Office Equipment", "print")
            )
        ),
        MajorCategory(
            id = "cat_kitchen",
            nameZh = "餐饮厨具",
            nameEn = "Kitchenware",
            iconName = "soup_kitchen",
            descriptionZh = "锅具、餐具与厨房小家电",
            descriptionEn = "Cookware, tableware & small kitchen appliances",
            subCategories = listOf(
                SubCategory("sub_cookware", "锅具", "Cookware", "soup_kitchen"),
                SubCategory("sub_tableware", "餐具", "Tableware", "restaurant"),
                SubCategory("sub_small_appliances", "小家电", "Small Appliances", "microwave")
            )
        ),
        MajorCategory(
            id = "cat_appliances",
            nameZh = "家用电器",
            nameEn = "Home Appliances",
            iconName = "kitchen",
            descriptionZh = "冰箱、洗衣机、空调等常见大中型家电",
            descriptionEn = "Refrigerators, washing machines & ACs",
            subCategories = listOf(
                SubCategory("sub_fridge", "冰箱", "Refrigerators", "kitchen"),
                SubCategory("sub_washer", "洗衣机", "Washing Machines", "local_laundry_service"),
                SubCategory("sub_ac", "空调", "Air Conditioners", "ac_unit")
            )
        ),
        MajorCategory(
            id = "cat_electronics",
            nameZh = "电子产品",
            nameEn = "Electronics",
            iconName = "smartphone",
            descriptionZh = "手机、平板、电脑、配件数码",
            descriptionEn = "Smartphones, tablets, PCs & accessories",
            subCategories = listOf(
                SubCategory("sub_phone", "手机类", "Smartphones", "smartphone"),
                SubCategory("sub_tablet", "平板类", "Tablets", "tablet_mac"),
                SubCategory("sub_desktop", "台式电脑", "Desktop PCs", "desktop_windows"),
                SubCategory("sub_laptop", "笔记本类", "Laptops", "laptop"),
                SubCategory("sub_acc", "配件", "Accessories", "headphones")
            )
        ),
        MajorCategory(
            id = "cat_bottles",
            nameZh = "饮料瓶类",
            nameEn = "Beverage Bottles",
            iconName = "local_drink",
            descriptionZh = "塑料瓶、玻璃瓶、金属罐，实时对接废品回收参考价",
            descriptionEn = "Plastic, glass bottles & metal cans with live scrap rates",
            subCategories = listOf(
                SubCategory("sub_plastic_bottle", "塑料瓶", "Plastic Bottles", "local_drink", true, "¥2.2 - ¥2.8 / kg"),
                SubCategory("sub_glass_bottle", "玻璃瓶", "Glass Bottles", "wine_bar", true, "¥0.3 - ¥0.5 / kg"),
                SubCategory("sub_metal_can", "金属罐", "Metal Cans", "inventory", true, "¥4.5 - ¥5.8 / kg")
            )
        ),
        MajorCategory(
            id = "cat_oversized",
            nameZh = "超大型物品",
            nameEn = "Over-sized Items",
            iconName = "local_shipping",
            descriptionZh = "大型沙发、整套大床、大型柜体，需特殊重型物流与人工拆卸",
            descriptionEn = "Large sofas, king beds & heavy cabinets requiring heavy freight",
            requiresSpecialLogistics = true,
            subCategories = listOf(
                SubCategory("sub_sofa", "沙发", "Sofas & Couches", "weekend"),
                SubCategory("sub_bed", "床具", "Beds & Mattresses", "bed"),
                SubCategory("sub_heavy_furniture", "大型家具", "Heavy Furniture", "table_bar"),
                SubCategory("sub_heavy_appliance", "大型家电", "Heavy Appliances", "tv")
            )
        )
    )
}
