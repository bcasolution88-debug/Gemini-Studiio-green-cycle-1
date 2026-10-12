package com.example.data.repository

import com.example.data.local.RecycleDao
import com.example.data.model.DonationDrive
import com.example.data.model.ForumPost
import com.example.data.model.OrderAgreement
import com.example.data.model.RecycleItem
import com.example.data.model.RecycleStation
import com.example.data.model.SupplyItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecycleRepository(private val dao: RecycleDao) {

    val allItems: Flow<List<RecycleItem>> = dao.getAllItems()
    val allForumPosts: Flow<List<ForumPost>> = dao.getAllForumPosts()
    val allStations: Flow<List<RecycleStation>> = dao.getAllStations()
    val allOrders: Flow<List<OrderAgreement>> = dao.getAllOrders()
    val allDonationReceipts: Flow<List<com.example.data.model.DonationReceipt>> = dao.getAllDonationReceipts()

    init {
        // Seed initial data if empty
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfEmpty()
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        val currentItems = dao.getAllItems().first()
        if (currentItems.isEmpty()) {
            // Seed Items locally & in Firestore
            sampleItems.forEach { item ->
                dao.insertItem(item)
                FirestoreManager.saveItemPost(item)
            }
            sampleForumPosts.forEach { dao.insertForumPost(it) }
            sampleStations.forEach { station ->
                dao.insertStation(station)
                FirestoreManager.saveStationLocation(station)
            }
            sampleOrders.forEach { dao.insertOrder(it) }
            
            // Seed initial User Profile in Firestore
            val defaultProfile = com.example.data.model.UserProfile()
            FirestoreManager.saveUserProfile(defaultProfile)
        }

        if (dao.getAllDonationReceipts().first().isEmpty()) {
            sampleReceipts.forEach { dao.insertDonationReceipt(it) }
        }
    }

    suspend fun saveDonationReceipt(receipt: com.example.data.model.DonationReceipt) = dao.insertDonationReceipt(receipt)
    suspend fun getDonationReceiptById(id: String) = dao.getDonationReceiptById(id)

    suspend fun getItemById(id: String): RecycleItem? = dao.getItemById(id)
    suspend fun addItem(item: RecycleItem) {
        dao.insertItem(item)
        FirestoreManager.saveItemPost(item)
    }
    suspend fun updateItem(item: RecycleItem) {
        dao.updateItem(item)
        FirestoreManager.saveItemPost(item)
    }

    suspend fun getForumPostById(id: String): ForumPost? = dao.getForumPostById(id)
    suspend fun addForumPost(post: ForumPost) = dao.insertForumPost(post)
    suspend fun updateForumPost(post: ForumPost) = dao.updateForumPost(post)

    suspend fun toggleLikePost(postId: String) {
        val post = dao.getForumPostById(postId) ?: return
        val newIsLiked = !post.isLiked
        val newCount = if (newIsLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
        dao.updateForumPost(post.copy(isLiked = newIsLiked, likesCount = newCount))
    }

    suspend fun toggleHeartPost(postId: String) {
        val post = dao.getForumPostById(postId) ?: return
        val newIsHearted = !post.isHearted
        val newCount = if (newIsHearted) post.heartCount + 1 else (post.heartCount - 1).coerceAtLeast(0)
        dao.updateForumPost(post.copy(isHearted = newIsHearted, heartCount = newCount))
    }

    suspend fun toggleHelpfulPost(postId: String) {
        val post = dao.getForumPostById(postId) ?: return
        val newIsHelpful = !post.isHelpful
        val newCount = if (newIsHelpful) post.helpfulCount + 1 else (post.helpfulCount - 1).coerceAtLeast(0)
        dao.updateForumPost(post.copy(isHelpful = newIsHelpful, helpfulCount = newCount))
    }

    suspend fun addStation(station: RecycleStation) {
        dao.insertStation(station)
        FirestoreManager.saveStationLocation(station)
    }

    suspend fun getOrderById(orderId: String): OrderAgreement? = dao.getOrderById(orderId)
    suspend fun addOrder(order: OrderAgreement) = dao.insertOrder(order)
    suspend fun updateOrder(order: OrderAgreement) = dao.updateOrder(order)

    // Predefined donation drives
    val donationDrives = listOf(
        DonationDrive(
            id = "don_1",
            titleZh = "大凉山儿童冬季暖心闲置衣物与保暖织物捐赠",
            titleEn = "Warm Winter Clothes & Textiles for Daliangshan Kids",
            targetGroupZh = "大凉山偏远山区小学",
            targetGroupEn = "Remote Mountain Schools in Daliangshan",
            descriptionZh = "收集8成新以上羽绒服、厚外套、围巾棉帽及床单被褥，统一打包点对点寄送当地校方。",
            descriptionEn = "Collect down jackets, coats, winter scarves and bedsheets for remote pupils.",
            coverImage = "",
            goalItems = 1000,
            collectedItems = 680,
            partnerOrganization = "绿之洲公益基金会",
            category = "Textiles"
        ),
        DonationDrive(
            id = "don_2",
            titleZh = "社区困难独居老人闲置家居用品与厨具援助",
            titleEn = "Household Goods & Kitchenware Aid for Senior Citizens",
            targetGroupZh = "城市社区高龄困难独居老人",
            targetGroupEn = "Low-income Elderly living alone in city community",
            descriptionZh = "收集旧木椅、保温杯、餐具套盒、收纳箱等常用居家生活物资，安全消毒后分发到位。",
            descriptionEn = "Collect wooden chairs, thermal bottles, tableware & storage boxes for elders.",
            coverImage = "",
            goalItems = 500,
            collectedItems = 340,
            partnerOrganization = "爱心暖阳志愿者协会",
            category = "Household Goods"
        ),
        DonationDrive(
            id = "don_3",
            titleZh = "乡村公益电子阅览室与废旧电脑数码再利用计划",
            titleEn = "Rural School E-Waste & Computer Repurposing Project",
            targetGroupZh = "乡村完小电子阅览室",
            targetGroupEn = "Rural Primary School Digital Reading Room",
            descriptionZh = "回收企业或个人废旧笔记本、显示器、台式主机，硬件安全拆解组装并安装教学系统后捐赠。",
            descriptionEn = "Recycle old laptops, monitors & PCs, dismantle, repair and install educational tools for schools.",
            coverImage = "",
            goalItems = 300,
            collectedItems = 210,
            partnerOrganization = "数字乡村公益行动",
            category = "Electronic Waste"
        ),
        DonationDrive(
            id = "don_4",
            titleZh = "偏远小学儿童图书绘本与益智玩具漂流计划",
            titleEn = "Children Books & Educational Toys Floating Library Drive",
            targetGroupZh = "偏远乡村托幼机构与小学图书角",
            targetGroupEn = "Rural Kindergarten & Primary School Libraries",
            descriptionZh = "募集无破损儿童中英文绘本、科普读物、木质积木与智力拼图，搭建乡村流动阅读角。",
            descriptionEn = "Collect picture books, science readers, blocks and puzzles to build rural floating reading corners.",
            coverImage = "",
            goalItems = 800,
            collectedItems = 520,
            partnerOrganization = "童心书香公益计划",
            category = "Books & Toys"
        )
    )

    // Predefined supply items posted by admin/stations
    val supplyItems = listOf(
        SupplyItem(
            id = "sup_1",
            title = "八成新实木九成新升降办公椅 (绿色循环认证)",
            category = "办公用品",
            priceType = "FREE",
            price = 0.0,
            condition = "85新",
            stationName = "朝阳废品环保站点",
            stock = 5,
            images = emptyList(),
            description = "企业搬迁退役办公椅，气压杆检测合格，面料已高压清洗打蜡，完全免费领取！"
        ),
        SupplyItem(
            id = "sup_2",
            title = "九成新海尔双门210L节能冰箱",
            category = "家用电器",
            priceType = "LOW_PRICE",
            price = 280.0,
            condition = "90新",
            stationName = "海淀中关村再利用中心",
            stock = 2,
            images = emptyList(),
            description = "站点回收后经过电机氟利昂检测与全面杀菌清洗，冷冻制冷极佳，享受3个月免费质保。"
        ),
        SupplyItem(
            id = "sup_3",
            title = "戴尔 24寸 1080P IPS 高清显示器",
            category = "电子产品",
            priceType = "TRADE_IN",
            price = 120.0,
            condition = "92新",
            stationName = "丰台科技园区回收站",
            stock = 3,
            images = emptyList(),
            description = "支持旧显示器或50kg废纸旧金属折抵差价换购！"
        )
    )

    companion object {
        val sampleItems = listOf(
            RecycleItem(
                id = "item_1",
                title = "九成新全实木双人床+厚弹簧床垫 (含特殊重货拆装)",
                majorCategoryId = "cat_oversized",
                subCategoryId = "sub_bed",
                condition = "90新",
                description = "因搬家急售，全橡木框架非常稳固无异响，带有厚软舒适床垫。需要重货搬运与拆卸。",
                expectedPrice = 350.0,
                isDonation = false,
                imageListJson = "placeholder_bed_1,placeholder_bed_2,placeholder_bed_3",
                videoUrl = "sample_video_bed.mp4",
                location = "北京市朝阳区三里屯街道12号院",
                contactPhone = "13800138001",
                contactWechat = "green_owner_88",
                status = "MATCHING",
                assignedRecipientName = "张师傅 (朝阳重货回收专员)",
                assignedRecipientPhone = "13911112222",
                assignedRecipientWechat = "recycle_zhang_888",
                assignedStationName = "朝阳绿洲环保回收总站",
                scheduledTime = "2026-08-01 14:00 - 16:00",
                scheduledLocation = "北京市朝阳区三里屯街道12号院2号楼3单元601",
                estimatedPrice = 320.0
            ),
            RecycleItem(
                id = "item_2",
                title = "小天鹅 10kg 波轮洗烘一体机 (功能完好)",
                majorCategoryId = "cat_appliances",
                subCategoryId = "sub_washer",
                condition = "85新",
                description = "自用2年，洗涤脱水烘干均正常，无修无故障。更换变频滚筒故出售。",
                expectedPrice = 200.0,
                isDonation = false,
                imageListJson = "placeholder_washer_1,placeholder_washer_2",
                videoUrl = null,
                location = "北京市海淀区中关村南大街",
                contactPhone = "13700001111",
                contactWechat = "haidian_user_01",
                status = "SCHEDULED",
                assignedRecipientName = "李工程师 (家电评估师)",
                assignedRecipientPhone = "13699998888",
                assignedRecipientWechat = "appliance_li_11",
                assignedStationName = "中关村智能回收站点",
                scheduledTime = "2026-07-31 10:00 - 11:30",
                scheduledLocation = "北京市海淀区中关村南大街18号3号楼502",
                estimatedPrice = 220.0
            ),
            RecycleItem(
                id = "item_3",
                title = "批量塑料饮料瓶、听装易拉罐与废旧纸箱 (约35kg)",
                majorCategoryId = "cat_bottles",
                subCategoryId = "sub_plastic_bottle",
                condition = "废品回收",
                description = "已按PET塑料瓶、铝制易拉罐、硬纸板分类打包整理，方便上门装车地磅称重。",
                expectedPrice = 0.0,
                isDonation = false,
                imageListJson = "placeholder_bottle_1,placeholder_bottle_2",
                videoUrl = null,
                location = "北京市丰台区方庄社区",
                contactPhone = "13512345678",
                contactWechat = "fangzhuang_recycle",
                status = "PENDING",
                assignedRecipientName = null,
                assignedRecipientPhone = null,
                assignedRecipientWechat = null,
                assignedStationName = null,
                scheduledTime = null,
                scheduledLocation = null,
                estimatedPrice = null
            ),
            RecycleItem(
                id = "item_4",
                title = "闲置童书绘本与儿童拼图玩具 (指定公益捐赠)",
                majorCategoryId = "cat_home",
                subCategoryId = "sub_decor",
                condition = "95新",
                description = "孩子长大了，精选了40余本无破损撕页的经典中文绘本与益智拼图，希望能捐给山区孩子。",
                expectedPrice = 0.0,
                isDonation = true,
                donorTarget = "大凉山偏远山区小学",
                imageListJson = "placeholder_books_1,placeholder_books_2",
                videoUrl = null,
                location = "北京市西城区金融街",
                contactPhone = "13988887777",
                contactWechat = "book_donor_bj",
                status = "COMPLETED",
                assignedRecipientName = "王爱心专员 (捐赠流向追踪组)",
                assignedRecipientPhone = "13866665555",
                assignedRecipientWechat = "donation_wang_love",
                assignedStationName = "西城区爱心转运站",
                scheduledTime = "2026-07-28 15:00",
                scheduledLocation = "北京市西城区金融街22号1楼快递站点",
                estimatedPrice = 0.0
            )
        )

        val sampleForumPosts = listOf(
            ForumPost(
                id = "post_1",
                authorName = "环保达人小绿",
                authorAvatar = "",
                title = "【待回收实拍】搬家清理！九成新宜家落地灯与实木小书桌短视频展示",
                content = "下周末就要退房啦，手头有一盏宜家落地灯和一把全实木学习椅子，保存非常完好，附上现场拍摄30秒全方位视频，欢迎管理员对接评估上门！",
                imageListJson = "placeholder_lamp_1,placeholder_lamp_2,placeholder_lamp_3",
                videoUrl = "sample_video_furniture.mp4",
                tagsJson = "待回收,搬家清仓,家具废品,视频展示",
                likesCount = 28,
                isLiked = true,
                commentsCount = 12,
                sharesCount = 5,
                isPendingRecycle = true,
                relatedItemId = "item_1"
            ),
            ForumPost(
                id = "post_2",
                authorName = "绿色家园志愿者",
                authorAvatar = "",
                title = "大家整理的废旧易拉罐与塑料瓶，上门回收称重心得与价格参考表",
                content = "今天朝阳站点的王师傅上门收了45公斤PET矿泉水瓶和铝罐，现场给地磅称重结清，PET塑料瓶现在¥2.5/kg，铝罐高达到¥5.2/kg！家里有囤瓶子的朋友快预约！",
                imageListJson = "placeholder_bottle_forum_1,placeholder_bottle_forum_2",
                videoUrl = null,
                tagsJson = "废品价目表,塑料瓶回收,经验分享,站点推荐",
                likesCount = 64,
                isLiked = false,
                commentsCount = 31,
                sharesCount = 18,
                isPendingRecycle = false,
                relatedItemId = "item_3"
            )
        )

        val sampleStations = listOf(
            RecycleStation(
                id = "sta_1",
                name = "朝阳绿洲示范废品环保总站 (官方认证)",
                rating = 4.9f,
                address = "北京市朝阳区东四环南路58号",
                latitude = 39.9080,
                longitude = 116.4800,
                phone = "010-87654321",
                wechat = "cy_green_station_888",
                businessHours = "08:00 - 20:00 (周一至周日)",
                tagsJson = "官方认证,重型卡车,免费上门拆卸,地磅精准称重,废品高价",
                photoUrl = "",
                serviceCategoriesJson = "超大型物品,家用电器,电子产品,饮料瓶类,办公用品",
                priceListJson = "PET塑料瓶:¥2.6/kg;铝制易拉罐:¥5.5/kg;废铜线:¥52.0/kg;废旧冰箱:¥80-150/台;废硬纸板:¥1.2/kg",
                isVerified = true,
                distanceKm = 0.8
            ),
            RecycleStation(
                id = "sta_2",
                name = "中关村数码与家电循环再利用中心",
                rating = 4.8f,
                address = "北京市海淀区中关村南大街1号",
                latitude = 39.9800,
                longitude = 116.3100,
                phone = "010-62345678",
                wechat = "zgc_recycle_center",
                businessHours = "09:00 - 19:30",
                tagsJson = "数码高价,数据安全擦除,现场测试,家电维修再利用",
                photoUrl = "",
                serviceCategoriesJson = "电子产品,家用电器,办公用品",
                priceListJson = "废旧笔记本:¥100-800/台;二手液晶显示器:¥80-300/台;废旧手机:¥30-1200/台",
                isVerified = true,
                distanceKm = 2.4
            ),
            RecycleStation(
                id = "sta_3",
                name = "丰台区爱心便民废品回收点",
                rating = 4.6f,
                address = "北京市丰台区方庄南路16号",
                latitude = 39.8600,
                longitude = 116.4300,
                phone = "010-67891234",
                wechat = "ft_fengzhuang_recycle",
                businessHours = "07:30 - 19:00",
                tagsJson = "社区便民,积分奖励,捐赠转运点,支持打包上门",
                photoUrl = "",
                serviceCategoriesJson = "饮料瓶类,家居用品,餐饮厨具",
                priceListJson = "塑料瓶:¥2.4/kg;玻璃瓶:¥0.4/kg;废报纸纸箱:¥1.3/kg",
                isVerified = true,
                distanceKm = 3.5
            )
        )

        val sampleOrders = listOf(
            OrderAgreement(
                orderId = "ORD_20260729_881",
                itemId = "item_1",
                itemTitle = "九成新全实木双人床+厚弹簧床垫",
                sellerName = "张女士 (三里屯用户)",
                sellerPhone = "13800138001",
                collectorName = "张师傅 (朝阳重货回收专员)",
                collectorPhone = "13911112222",
                collectorWechat = "recycle_zhang_888",
                stationName = "朝阳绿洲环保回收总站",
                scheduledTime = "2026-08-01 14:00 - 16:00",
                location = "北京市朝阳区三里屯街道12号院2号楼3单元601",
                finalPrice = 320.0,
                status = "SCHEDULED",
                digitalSignature = "Zhang_Sig_Verified_Digital_2026",
                isSigned = true
            )
        )

        val sampleCommunityWishlists = mutableListOf(
            com.example.data.model.CommunityWishlistItem(
                id = "wish_1",
                ngoName = "朝阳区关爱流浪动物救助中心",
                categoryTag = "织物与废纸板",
                neededItemTitleZh = "紧急急需: 旧毛毯/吸水大棉被/废旧瓦楞纸箱 (用于犬舍冬季保温防潮)",
                neededItemTitleEn = "Urgent Need: Old Blankets, Cotton Quilts & Cardboard Boxes for Animal Shelter",
                targetCount = 50,
                currentCount = 32,
                urgencyLevel = "HIGH",
                addressZh = "北京市朝阳区黑庄户乡关爱动物基站",
                addressEn = "Animal Rescue Base, Heizhuanghu, Chaoyang, Beijing",
                contactPhone = "010-85886611",
                descriptionZh = "基地目前有120余只救助小动物，急需大量废旧羊毛毯、棉被与干净厚纸箱，用于铺垫犬舍床铺。",
                descriptionEn = "Shelter hosts 120+ rescued animals requiring old blankets, quilts and thick boxes for winter bedding.",
                matchedCategoryIds = listOf("paper", "textile")
            ),
            com.example.data.model.CommunityWishlistItem(
                id = "wish_2",
                ngoName = "阳光特殊教育儿童启智绘本馆",
                categoryTag = "儿童图书与玩具",
                neededItemTitleZh = "急需: 二手儿童插画绘本/无毒塑胶乐高拼装积木",
                neededItemTitleEn = "Urgent Need: Pre-loved Picture Books & Non-toxic Plastic Lego Blocks",
                targetCount = 30,
                currentCount = 18,
                urgencyLevel = "HIGH",
                addressZh = "北京市海淀区中关村南大街12号院",
                addressEn = "No. 12 Zhongguancun South St, Haidian, Beijing",
                contactPhone = "010-62118899",
                descriptionZh = "为听障与特殊儿童开设的免费感统阅读室，征集各类7-9成新儿童拼图、早教绘本与大型安全积木。",
                descriptionEn = "Free sensory reading room collecting 70-90% new picture books, puzzles, and safe building blocks.",
                matchedCategoryIds = listOf("appliance_digital", "furniture_wood")
            ),
            com.example.data.model.CommunityWishlistItem(
                id = "wish_3",
                ngoName = "海淀区绿色社区老年公寓",
                categoryTag = "闲置家电与家居用品",
                neededItemTitleZh = "需求: 闲置电热水壶/折叠轮椅/小型取暖器",
                neededItemTitleEn = "Needed: Second-hand Electric Kettles, Wheelchairs & Small Heaters",
                targetCount = 15,
                currentCount = 11,
                urgencyLevel = "NORMAL",
                addressZh = "北京市海淀区清华东路28号",
                addressEn = "No. 28 Qinghua East Rd, Haidian, Beijing",
                contactPhone = "010-82335544",
                descriptionZh = "为困难独居老人提供居家康复辅助器具与安全小家电，支持上门提货评估。",
                descriptionEn = "Providing assisted mobility devices and home appliances for low-income seniors.",
                matchedCategoryIds = listOf("home_appliances", "metal_scrap")
            )
        )

        val sampleSustainabilityEvents = mutableListOf(
            com.example.data.model.SustainabilityEvent(
                id = "event_1",
                titleZh = "🏃‍♂️ 奥林匹克公园周末捡跑 (Plogging) & 废塑料换植株市集",
                titleEn = "🏃‍♂️ Olympic Park Weekend Plogging & Waste Plastic Exchange",
                organizerZh = "北京绿色低碳捡跑俱乐部",
                organizerEn = "Beijing Low-Carbon Plogging Club",
                dateTextZh = "本周六 09:00 - 11:30",
                dateTextEn = "This Saturday 09:00 - 11:30",
                locationNameZh = "奥林匹克森林公园南门集散广场",
                locationNameEn = "South Gate Plaza, Olympic Forest Park",
                latitude = 39.9920,
                longitude = 116.3970,
                distanceKm = 1.2,
                currentAttendees = 28,
                maxAttendees = 40,
                isJoined = false,
                tagsJson = "周末捡跑,零废弃公园,废塑料换植株",
                descriptionZh = "边跑步边拾捡公园沿途废弃塑料瓶与易拉罐！现场设置废塑料就地分类地磅，拾捡满2kg即可兑换绿植盆栽一盆。",
                descriptionEn = "Jog & pick up waste plastics/cans along park routes! Collect 2kg waste to redeem a potted green plant."
            ),
            com.example.data.model.SustainabilityEvent(
                id = "event_2",
                titleZh = "🔧 798创客空间: 弃置家电电路板拆解 & 铜铝手工DIY工作坊",
                titleEn = "🔧 798 Maker Space: E-Waste PCB Dismantling & Copper Art DIY Workshop",
                organizerZh = "朝阳废弃物再利用极客实验室",
                organizerEn = "Chaoyang Recycling Geek Lab",
                dateTextZh = "本周日 14:00 - 17:00",
                dateTextEn = "This Sunday 14:00 - 17:00",
                locationNameZh = "798艺术区B08创客工坊",
                locationNameEn = "Maker Workshop B08, 798 Art District",
                latitude = 39.9840,
                longitude = 116.4960,
                distanceKm = 3.5,
                currentAttendees = 15,
                maxAttendees = 20,
                isJoined = false,
                tagsJson = "废品拆解,电子废弃物,变废为宝DIY",
                descriptionZh = "专业工程师现场指导拆解废弃电脑主机与微波炉，分离高纯度铜线与芯片，并教授用铜线制作复古朋克饰品。",
                descriptionEn = "Engineers guide PC/microwaving dismantling, separating high-purity copper and crafting steampunk art."
            ),
            com.example.data.model.SustainabilityEvent(
                id = "event_3",
                titleZh = "📦 三里屯社区闲置旧物盲盒互换与旧书零抛弃市集",
                titleEn = "📦 Sanlitun Pre-loved Item Blind Box Swap & Book Zero-Waste Market",
                organizerZh = "三里屯街道环保治理办公室",
                organizerEn = "Sanlitun Community Eco Governance Office",
                dateTextZh = "下周六 10:00 - 16:00",
                dateTextEn = "Next Saturday 10:00 - 16:00",
                locationNameZh = "三里屯太古里北区下沉广场",
                locationNameEn = "Sunken Plaza, Taikoo Li North, Sanlitun",
                latitude = 39.9360,
                longitude = 116.4540,
                distanceKm = 2.1,
                currentAttendees = 45,
                maxAttendees = 100,
                isJoined = false,
                tagsJson = "旧物盲盒,闲置互换,二手书籍",
                descriptionZh = "带上家中闲置物品（玩具、饰品、书籍、摆件），参与以物易物无金钱交易旧物大集，现场设专业旧货估价站。",
                descriptionEn = "Bring your pre-loved toys, books, and decor for non-monetary bartering with professional appraisal booth."
            )
        )
    }

    // Community Wishlists & Events In-Memory Repos
    val communityWishlists = mutableListOf<com.example.data.model.CommunityWishlistItem>(
        com.example.data.model.CommunityWishlistItem(
            id = "wish_1",
            ngoName = "朝阳区关爱流浪动物救助中心",
            categoryTag = "织物与废纸板",
            neededItemTitleZh = "紧急急需: 旧毛毯/吸水大棉被/废旧瓦楞纸箱 (用于犬舍冬季保温防潮)",
            neededItemTitleEn = "Urgent Need: Old Blankets, Cotton Quilts & Cardboard Boxes for Animal Shelter",
            targetCount = 50,
            currentCount = 32,
            urgencyLevel = "HIGH",
            addressZh = "北京市朝阳区黑庄户乡关爱动物基站",
            addressEn = "Animal Rescue Base, Heizhuanghu, Chaoyang, Beijing",
            contactPhone = "010-85886611",
            descriptionZh = "基地目前有120余只救助小动物，急需大量废旧羊毛毯、棉被与干净厚纸箱，用于铺垫犬舍床铺。",
            descriptionEn = "Shelter hosts 120+ rescued animals requiring old blankets, quilts and thick boxes for winter bedding.",
            matchedCategoryIds = listOf("paper", "textile")
        ),
        com.example.data.model.CommunityWishlistItem(
            id = "wish_2",
            ngoName = "阳光特殊教育儿童启智绘本馆",
            categoryTag = "儿童图书与玩具",
            neededItemTitleZh = "急需: 二手儿童插画绘本/无毒塑胶乐高拼装积木",
            neededItemTitleEn = "Urgent Need: Pre-loved Picture Books & Non-toxic Plastic Lego Blocks",
            targetCount = 30,
            currentCount = 18,
            urgencyLevel = "HIGH",
            addressZh = "北京市海淀区中关村南大街12号院",
            addressEn = "No. 12 Zhongguancun South St, Haidian, Beijing",
            contactPhone = "010-62118899",
            descriptionZh = "为听障与特殊儿童开设的免费感统阅读室，征集各类7-9成新儿童拼图、早教绘本与大型安全积木。",
            descriptionEn = "Free sensory reading room collecting 70-90% new picture books, puzzles, and safe building blocks.",
            matchedCategoryIds = listOf("appliance_digital", "furniture_wood")
        ),
        com.example.data.model.CommunityWishlistItem(
            id = "wish_3",
            ngoName = "海淀区绿色社区老年公寓",
            categoryTag = "闲置家电与家居用品",
            neededItemTitleZh = "需求: 闲置电热水壶/折叠轮椅/小型取暖器",
            neededItemTitleEn = "Needed: Second-hand Electric Kettles, Wheelchairs & Small Heaters",
            targetCount = 15,
            currentCount = 11,
            urgencyLevel = "NORMAL",
            addressZh = "北京市海淀区清华东路28号",
            addressEn = "No. 28 Qinghua East Rd, Haidian, Beijing",
            contactPhone = "010-82335544",
            descriptionZh = "为困难独居老人提供居家康复辅助器具与安全小家电，支持上门提货评估。",
            descriptionEn = "Providing assisted mobility devices and home appliances for low-income seniors.",
            matchedCategoryIds = listOf("home_appliances", "metal_scrap")
        )
    )

    val sustainabilityEvents = mutableListOf<com.example.data.model.SustainabilityEvent>(
        com.example.data.model.SustainabilityEvent(
            id = "event_1",
            titleZh = "🏃‍♂️ 奥林匹克公园周末捡跑 (Plogging) & 废塑料换植株市集",
            titleEn = "🏃‍♂️ Olympic Park Weekend Plogging & Waste Plastic Exchange",
            organizerZh = "北京绿色低碳捡跑俱乐部",
            organizerEn = "Beijing Low-Carbon Plogging Club",
            dateTextZh = "本周六 09:00 - 11:30",
            dateTextEn = "This Saturday 09:00 - 11:30",
            locationNameZh = "奥林匹克森林公园南门集散广场",
            locationNameEn = "South Gate Plaza, Olympic Forest Park",
            latitude = 39.9920,
            longitude = 116.3970,
            distanceKm = 1.2,
            currentAttendees = 28,
            maxAttendees = 40,
            isJoined = false,
            tagsJson = "周末捡跑,零废弃公园,废塑料换植株",
            descriptionZh = "边跑步边拾捡公园沿途废弃塑料瓶与易拉罐！现场设置废塑料就地分类地磅，拾捡满2kg即可兑换绿植盆栽一盆。",
            descriptionEn = "Jog & pick up waste plastics/cans along park routes! Collect 2kg waste to redeem a potted green plant."
        ),
        com.example.data.model.SustainabilityEvent(
            id = "event_2",
            titleZh = "🔧 798创客空间: 弃置家电电路板拆解 & 铜铝手工DIY工作坊",
            titleEn = "🔧 798 Maker Space: E-Waste PCB Dismantling & Copper Art DIY Workshop",
            organizerZh = "朝阳废弃物再利用极客实验室",
            organizerEn = "Chaoyang Recycling Geek Lab",
            dateTextZh = "本周日 14:00 - 17:00",
            dateTextEn = "This Sunday 14:00 - 17:00",
            locationNameZh = "798艺术区B08创客工坊",
            locationNameEn = "Maker Workshop B08, 798 Art District",
            latitude = 39.9840,
            longitude = 116.4960,
            distanceKm = 3.5,
            currentAttendees = 15,
            maxAttendees = 20,
            isJoined = false,
            tagsJson = "废品拆解,电子废弃物,变废为宝DIY",
            descriptionZh = "专业工程师现场指导拆解废弃电脑主机与微波炉，分离高纯度铜线与芯片，并教授用铜线制作复古朋克饰品。",
            descriptionEn = "Engineers guide PC/microwaving dismantling, separating high-purity copper and crafting steampunk art."
        ),
        com.example.data.model.SustainabilityEvent(
            id = "event_3",
            titleZh = "📦 三里屯社区闲置旧物盲盒互换与旧书零抛弃市集",
            titleEn = "📦 Sanlitun Pre-loved Item Blind Box Swap & Book Zero-Waste Market",
            organizerZh = "三里屯街道环保治理办公室",
            organizerEn = "Sanlitun Community Eco Governance Office",
            dateTextZh = "下周六 10:00 - 16:00",
            dateTextEn = "Next Saturday 10:00 - 16:00",
            locationNameZh = "三里屯太古里北区下沉广场",
            locationNameEn = "Sunken Plaza, Taikoo Li North, Sanlitun",
            latitude = 39.9360,
            longitude = 116.4540,
            distanceKm = 2.1,
            currentAttendees = 45,
            maxAttendees = 100,
            isJoined = false,
            tagsJson = "旧物盲盒,闲置互换,二手书籍",
            descriptionZh = "带上家中闲置物品（玩具、饰品、书籍、摆件），参与以物易物无金钱交易旧物大集，现场设专业旧货估价站。",
            descriptionEn = "Bring your pre-loved toys, books, and decor for non-monetary bartering with professional appraisal booth."
        )
    )

    val sampleReceipts = listOf(
        com.example.data.model.DonationReceipt(
            receiptId = "RCT-DON-2026-8801",
            donorName = "张爱心 (GreenDonor)",
            donorPhone = "138****8899",
            recipientOrganization = "中华公益希望救助基金会 / Hope Charity",
            itemTitle = "八成新儿童冬季长款羽绒服与羊毛衫",
            category = "织物与衣物 (Textiles)",
            estimatedValue = 180.0,
            co2SavedKg = 8.5,
            status = "COMPLETED",
            completedTimestamp = System.currentTimeMillis() - 86400000L * 3,
            digitalHash = "0x8F92A110BC91",
            remarks = "定向捐赠大凉山偏远山区小学"
        ),
        com.example.data.model.DonationReceipt(
            receiptId = "RCT-DON-2026-8802",
            donorName = "李绿碳 (EcoVolunteer)",
            donorPhone = "139****7766",
            recipientOrganization = "爱心暖阳社区互助协会 / Community Aid",
            itemTitle = "闲置台式LED高显色护眼台灯与木质书架",
            category = "居家用品 (Household)",
            estimatedValue = 120.0,
            co2SavedKg = 5.2,
            status = "COMPLETED",
            completedTimestamp = System.currentTimeMillis() - 86400000L * 7,
            digitalHash = "0x7E81B220CD82",
            remarks = "社区独居老人帮扶计划"
        )
    )

    fun matchWishlistItem(wishlistId: String): Boolean {
        val index = communityWishlists.indexOfFirst { it.id == wishlistId }
        if (index != -1) {
            val item = communityWishlists[index]
            communityWishlists[index] = item.copy(currentCount = (item.currentCount + 1).coerceAtMost(item.targetCount))
            return true
        }
        return false
    }

    fun toggleJoinEvent(eventId: String): com.example.data.model.SustainabilityEvent? {
        val index = sustainabilityEvents.indexOfFirst { it.id == eventId }
        if (index != -1) {
            val ev = sustainabilityEvents[index]
            val newJoined = !ev.isJoined
            val newAttendees = if (newJoined) (ev.currentAttendees + 1).coerceAtMost(ev.maxAttendees) else (ev.currentAttendees - 1).coerceAtLeast(0)
            val updated = ev.copy(isJoined = newJoined, currentAttendees = newAttendees)
            sustainabilityEvents[index] = updated
            return updated
        }
        return null
    }
}

