package com.example.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.data.model.Language

data class ZeroWasteTutorial(
    val id: String,
    val titleZh: String,
    val titleEn: String,
    val descriptionZh: String,
    val descriptionEn: String,
    val authorNameZh: String,
    val authorNameEn: String,
    val isPinnedByAdmin: Boolean = true,
    val pinnedBadgeTextZh: String = "📌 站长官方精选置顶",
    val pinnedBadgeTextEn: String = "📌 Admin Pinned Spotlight",
    val categoryTagZh: String,
    val categoryTagEn: String,
    val videoTitleZh: String,
    val videoTitleEn: String,
    val duration: String,
    val difficultyLevelZh: String,
    val difficultyLevelEn: String,
    val materialListZh: List<String>,
    val materialListEn: List<String>,
    val stepsZh: List<String>,
    val stepsEn: List<String>,
    var likesCount: Int = 128,
    var savedCount: Int = 86,
    var isLiked: Boolean = false,
    var isSaved: Boolean = false
) {
    fun title(lang: Language): String = if (lang == Language.ZH) titleZh else titleEn
    fun description(lang: Language): String = if (lang == Language.ZH) descriptionZh else descriptionEn
    fun authorName(lang: Language): String = if (lang == Language.ZH) authorNameZh else authorNameEn
    fun pinnedBadgeText(lang: Language): String = if (lang == Language.ZH) pinnedBadgeTextZh else pinnedBadgeTextEn
    fun categoryTag(lang: Language): String = if (lang == Language.ZH) categoryTagZh else categoryTagEn
    fun videoTitle(lang: Language): String = if (lang == Language.ZH) videoTitleZh else videoTitleEn
    fun difficultyLevel(lang: Language): String = if (lang == Language.ZH) difficultyLevelZh else difficultyLevelEn
    fun materialList(lang: Language): List<String> = if (lang == Language.ZH) materialListZh else materialListEn
    fun steps(lang: Language): List<String> = if (lang == Language.ZH) stepsZh else stepsEn
}

object ZeroWasteTutorialRepository {
    private val _tutorials = mutableStateListOf<ZeroWasteTutorial>(
        ZeroWasteTutorial(
            id = "tut_001",
            titleZh = "🌱 PET透明塑料瓶变身「自动吸水多肉阳台盆栽」",
            titleEn = "🌱 PET Bottle Self-Watering Sukulent Planter DIY",
            descriptionZh = "站长示范如何利用喝完的矿泉水瓶与棉绳，制作无需天天浇水的自动虹吸灌溉无土绿植盆栽。",
            descriptionEn = "Learn how to convert plastic water bottles and cotton wicks into zero-water-loss self-watering indoor planters.",
            authorNameZh = "朝阳绿洲站长 (环保教导员)",
            authorNameEn = "Station Admin Zhang",
            isPinnedByAdmin = true,
            categoryTagZh = "塑料塑料瓶",
            categoryTagEn = "Plastic Bottles",
            videoTitleZh = "🎬 短视频教学: 3分钟废弃塑料瓶剪裁与自动虹吸试棉绳搭建",
            videoTitleEn = "🎬 Short Video: 3-Min Bottle Cutting & Cotton Wick Setup",
            duration = "02:15",
            difficultyLevelZh = "★☆☆ 极易上手 (适合亲子)",
            difficultyLevelEn = "★☆☆ Super Easy (Kid Friendly)",
            materialListZh = listOf("大号透明塑料瓶 1个", "吸水棉绳/旧衣布条 20cm", "美工刀/剪刀", "营养土/陶粒"),
            materialListEn = listOf("1 Large Plastic Water Bottle", "20cm Cotton Wick String", "Scissors / Utility Knife", "Potting Soil & Pebble Base"),
            stepsZh = listOf(
                "沿塑料瓶中上部1/3处用剪刀平整剪开，分为上半漏斗与下半蓄水杯",
                "在瓶盖中央打孔，穿入20cm棉绳并结扣固定，下端悬挂至蓄水杯中",
                "将上半漏斗倒扣于蓄水杯上，填入营养土与多肉植物即可自行吸收水分"
            ),
            stepsEn = listOf(
                "Cut the plastic bottle evenly at top 1/3 into funnel & water base",
                "Punch a hole in the cap, thread a 20cm cotton string as a siphon wick",
                "Invert funnel into base, fill soil & succulent plants for auto-siphon watering"
            ),
            likesCount = 342,
            savedCount = 210
        ),
        ZeroWasteTutorial(
            id = "tut_002",
            titleZh = "📦 快递瓦楞纸箱翻新「多格分装桌面收纳整理柜」",
            titleEn = "📦 Upcycled Shipping Box Multi-Grid Desk Organizer",
            descriptionZh = "拆解废弃网购包装纸箱，利用插接榫卯结构拼接精致收纳盒，解决日常化妆品与文具杂乱。",
            descriptionEn = "Transform discarded delivery cardboard boxes into stylish partitioned drawers and desk organizer trays.",
            authorNameZh = "张站长 & 社区手作达人",
            authorNameEn = "Station Admin & Craft Guild",
            isPinnedByAdmin = true,
            categoryTagZh = "瓦楞纸箱",
            categoryTagEn = "Cardboard Box",
            videoTitleZh = "🎬 视频演示: 瓦楞纸包边加固与模块化插接板裁剪",
            videoTitleEn = "🎬 Video Guide: Cardboard Edge Reinforcement & Partition Slots",
            duration = "03:40",
            difficultyLevelZh = "★★☆ 实用中等",
            difficultyLevelEn = "★★☆ Intermediate Craft",
            materialListZh = listOf("废旧瓦楞纸箱 2-3个", "热熔胶枪/双面胶", "废旧包装布/彩纸", "直尺与切割垫"),
            materialListEn = listOf("2-3 Discarded Delivery Boxes", "Hot Glue Gun / Double Tape", "Wrapping Paper / Fabric Scratches", "Ruler & Cutting Mat"),
            stepsZh = listOf(
                "裁切纸箱箱盖，保留主体四方框架作为收纳柜外壳",
                "测量内径尺寸，裁切纵横插接板并开槽，组合为多格隔板",
                "包裹废旧花布或废报纸进行表面美化与包边打磨加固"
            ),
            stepsEn = listOf(
                "Trim cardboard flaps and secure the core frame shell",
                "Measure inner dimensions, cut interlocking slot dividers",
                "Wrap with scrap fabric or colorful eco paper for durable finishing"
            ),
            likesCount = 289,
            savedCount = 175
        ),
        ZeroWasteTutorial(
            id = "tut_003",
            titleZh = "👕 旧棉服/废旧T恤零缝纫翻新「便携时尚环保购物袋」",
            titleEn = "👕 No-Sew Upcycled T-Shirt Eco Canvas Tote Bag",
            descriptionZh = "无需针线！只需一把剪刀，将起球不穿的纯棉T恤底部剪成流苏打结，变身大容量承重买菜袋。",
            descriptionEn = "Zero sewing skill required! Use scissors to knot the bottom of an old T-shirt into a heavy-duty washable grocery bag.",
            authorNameZh = "环保老王 (社区改造辅导员)",
            authorNameEn = "Master Wang (Zero-Waste)",
            isPinnedByAdmin = true,
            categoryTagZh = "纺织旧衣",
            categoryTagEn = "Textiles & Clothes",
            videoTitleZh = "🎬 视频教程: T恤领口袖口剪裁与底部流苏高强度防漏打结法",
            videoTitleEn = "🎬 Video: Collar Trimming & Fringe Knotting for High Load Capacity",
            duration = "01:50",
            difficultyLevelZh = "★☆☆ 零基础 5分钟完成",
            difficultyLevelEn = "★☆☆ 5-Min Quick DIY",
            materialListZh = listOf("不穿的旧纯棉T恤 1件", "布艺剪刀 1把", "记号笔"),
            materialListEn = listOf("1 Discarded Cotton T-Shirt", "1 Fabric Scissors", "Fabric Marker"),
            stepsZh = listOf(
                "剪掉T恤的领口与双侧袖子，保留双肩作为提手",
                "在T恤下摆向上剪出5cm长的平行流苏条（宽约1.5cm）",
                "将前后对应的流苏条打双死结，全部系紧后翻面即成大容量便携袋"
            ),
            stepsEn = listOf(
                "Cut off the shirt collar and sleeves, leaving shoulders as tote handles",
                "Cut 5cm parallel fringe strips along the bottom hemline (1.5cm width)",
                "Tie front and back fringe strips in double knots, turn inside out for a durable bag"
            ),
            likesCount = 512,
            savedCount = 380
        ),
        ZeroWasteTutorial(
            id = "tut_004",
            titleZh = "💻 废旧电路板与废弃灯泡「工业风极客创意桌面台灯」",
            titleEn = "💻 E-Waste Circuit Board & Retro Geek Desk Lamp",
            descriptionZh = "将废旧电脑主板、废旧螺栓与LED小灯珠拼装组合，制作极具未来感与科技废土风格的工业艺术小夜灯。",
            descriptionEn = "Combine retired motherboards, metal scrap bolts, and low-voltage LED beads into futuristic e-waste industrial art lamps.",
            authorNameZh = "Gemini AI Vision & 站长联合出品",
            authorNameEn = "Gemini AI & Station Admin",
            isPinnedByAdmin = true,
            categoryTagZh = "废旧电子",
            categoryTagEn = "E-Waste Art",
            videoTitleZh = "🎬 极客拆解视频: 废主板切割打磨与低压5V USB小灯珠电路焊接",
            videoTitleEn = "🎬 Geek Video: Motherboard Cutting & Low Voltage 5V USB Circuiting",
            duration = "04:10",
            difficultyLevelZh = "★★★ 极客创客向",
            difficultyLevelEn = "★★★ Geek Maker Level",
            materialListZh = listOf("报废电脑主板/内存条 1块", "废弃钨丝灯泡空壳 1只", "5V USB低压LED线", "绝缘热缩管与胶"),
            materialListEn = listOf("1 Scrapped PC Motherboard", "1 Vintage Blown Light Bulb", "5V Low Voltage USB LED Strand", "Heat Shrink Tubing"),
            stepsZh = listOf(
                "用小型锯切割无电路风险的主板边缘作为坚固台灯底座",
                "将废灯泡内部掏空，置入5V安全电压LED发光小灯珠",
                "使用废螺栓固定灯架，焊接USB供电线，展现硬核工业回收美学"
            ),
            stepsEn = listOf(
                "Cut motherboard corner safely as a sturdy metallic base",
                "Hollow out blown light bulb and insert 5V safe LED strands",
                "Assemble with bolts & USB wire to showcase hard-core upcycled art"
            ),
            likesCount = 195,
            savedCount = 142
        )
    )

    fun getTutorials(): List<ZeroWasteTutorial> = _tutorials

    fun addTutorial(tutorial: ZeroWasteTutorial) {
        _tutorials.add(0, tutorial)
    }

    fun togglePin(id: String) {
        val index = _tutorials.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = _tutorials[index]
            _tutorials[index] = old.copy(isPinnedByAdmin = !old.isPinnedByAdmin)
        }
    }

    fun toggleLike(id: String) {
        val index = _tutorials.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = _tutorials[index]
            val newLiked = !old.isLiked
            _tutorials[index] = old.copy(
                isLiked = newLiked,
                likesCount = if (newLiked) old.likesCount + 1 else old.likesCount - 1
            )
        }
    }

    fun toggleSave(id: String) {
        val index = _tutorials.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = _tutorials[index]
            val newSaved = !old.isSaved
            _tutorials[index] = old.copy(
                isSaved = newSaved,
                savedCount = if (newSaved) old.savedCount + 1 else old.savedCount - 1
            )
        }
    }
}
