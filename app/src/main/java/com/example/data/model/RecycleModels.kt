package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recycle_items")
data class RecycleItem(
    @PrimaryKey val id: String,
    val title: String,
    val majorCategoryId: String,
    val subCategoryId: String,
    val condition: String,
    val description: String,
    val expectedPrice: Double,
    val isDonation: Boolean = false,
    val donorTarget: String? = null,
    val imageListJson: String, // comma separated or JSON string
    val videoUrl: String? = null,
    val location: String,
    val latitude: Double = 39.9042,
    val longitude: Double = 116.4074,
    val contactPhone: String,
    val contactWechat: String? = null,
    val status: String = "PENDING", // PENDING, MATCHING, SCHEDULED, COMPLETED, CANCELLED
    val assignedRecipientName: String? = null,
    val assignedRecipientPhone: String? = null,
    val assignedRecipientWechat: String? = null,
    val assignedStationName: String? = null,
    val scheduledTime: String? = null,
    val scheduledLocation: String? = null,
    val estimatedPrice: Double? = null,
    val evaluation: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getImages(): List<String> {
        return if (imageListJson.isBlank()) emptyList() else imageListJson.split(",")
    }

    fun statusDisplayName(lang: Language): String {
        return when (status) {
            "PENDING" -> AppStrings.statusPending(lang)
            "MATCHING" -> AppStrings.statusMatching(lang)
            "SCHEDULED" -> AppStrings.statusScheduled(lang)
            "COMPLETED" -> AppStrings.statusCompleted(lang)
            "CANCELLED" -> AppStrings.statusCancelled(lang)
            else -> status
        }
    }
}

@Entity(tableName = "forum_posts")
data class ForumPost(
    @PrimaryKey val id: String,
    val authorName: String,
    val authorAvatar: String,
    val title: String,
    val content: String,
    val imageListJson: String,
    val videoUrl: String? = null,
    val tagsJson: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val heartCount: Int = 0,
    val isHearted: Boolean = false,
    val helpfulCount: Int = 0,
    val isHelpful: Boolean = false,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isPendingRecycle: Boolean = true,
    val relatedItemId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getImages(): List<String> = if (imageListJson.isBlank()) emptyList() else imageListJson.split(",")
    fun getTags(): List<String> = if (tagsJson.isBlank()) emptyList() else tagsJson.split(",")
}

data class ForumComment(
    val id: String,
    val postId: String,
    val authorName: String,
    val authorAvatar: String = "",
    val content: String,
    val timestamp: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)

@Entity(tableName = "recycle_stations")
data class RecycleStation(
    @PrimaryKey val id: String,
    val name: String,
    val rating: Float,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val wechat: String?,
    val businessHours: String,
    val tagsJson: String,
    val photoUrl: String,
    val serviceCategoriesJson: String,
    val priceListJson: String, // e.g. "废塑料:¥2.5/kg;铜线:¥48/kg;旧冰箱:¥80/台"
    val isVerified: Boolean = true,
    val distanceKm: Double = 1.2
) {
    fun getTags(): List<String> = if (tagsJson.isBlank()) emptyList() else tagsJson.split(",")
    fun getServiceCategories(): List<String> = if (serviceCategoriesJson.isBlank()) emptyList() else serviceCategoriesJson.split(",")
    fun getPriceList(): List<Pair<String, String>> {
        if (priceListJson.isBlank()) return emptyList()
        return priceListJson.split(";").mapNotNull {
            val parts = it.split(":")
            if (parts.size >= 2) Pair(parts[0], parts[1]) else null
        }
    }
}

@Entity(tableName = "recycle_orders")
data class OrderAgreement(
    @PrimaryKey val orderId: String,
    val itemId: String,
    val itemTitle: String,
    val sellerName: String,
    val sellerPhone: String,
    val collectorName: String,
    val collectorPhone: String,
    val collectorWechat: String?,
    val stationName: String,
    val scheduledTime: String,
    val location: String,
    val finalPrice: Double,
    val status: String, // MATCHING, SCHEDULED, COMPLETED, CANCELLED
    val digitalSignature: String? = null,
    val isSigned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class DonationDrive(
    val id: String,
    val titleZh: String,
    val titleEn: String,
    val targetGroupZh: String,
    val targetGroupEn: String,
    val descriptionZh: String,
    val descriptionEn: String,
    val coverImage: String,
    val goalItems: Int,
    val collectedItems: Int,
    val partnerOrganization: String,
    val category: String = "Textiles" // "Electronic Waste", "Textiles", "Household Goods", "Books & Toys"
) {
    fun title(lang: Language) = if (lang == Language.ZH) titleZh else titleEn
    fun targetGroup(lang: Language) = if (lang == Language.ZH) targetGroupZh else targetGroupEn
    fun description(lang: Language) = if (lang == Language.ZH) descriptionZh else descriptionEn
}

data class SupplyItem(
    val id: String,
    val title: String,
    val category: String,
    val priceType: String, // FREE, LOW_PRICE, TRADE_IN
    val price: Double,
    val condition: String,
    val stationName: String,
    val stock: Int,
    val images: List<String>,
    val description: String
)

data class CommunityWishlistItem(
    val id: String,
    val ngoName: String,
    val categoryTag: String,
    val neededItemTitleZh: String,
    val neededItemTitleEn: String,
    val targetCount: Int,
    val currentCount: Int,
    val urgencyLevel: String, // "HIGH", "NORMAL"
    val addressZh: String,
    val addressEn: String,
    val contactPhone: String,
    val descriptionZh: String,
    val descriptionEn: String,
    val matchedCategoryIds: List<String> = emptyList()
) {
    fun neededTitle(lang: Language) = if (lang == Language.ZH) neededItemTitleZh else neededItemTitleEn
    fun address(lang: Language) = if (lang == Language.ZH) addressZh else addressEn
    fun description(lang: Language) = if (lang == Language.ZH) descriptionZh else descriptionEn
}

data class SustainabilityEvent(
    val id: String,
    val titleZh: String,
    val titleEn: String,
    val organizerZh: String,
    val organizerEn: String,
    val dateTextZh: String,
    val dateTextEn: String,
    val locationNameZh: String,
    val locationNameEn: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val currentAttendees: Int,
    val maxAttendees: Int,
    val isJoined: Boolean = false,
    val tagsJson: String, // e.g. "环保捡跑,废品拆解,旧物集市"
    val descriptionZh: String,
    val descriptionEn: String
) {
    fun title(lang: Language) = if (lang == Language.ZH) titleZh else titleEn
    fun organizer(lang: Language) = if (lang == Language.ZH) organizerZh else organizerEn
    fun dateText(lang: Language) = if (lang == Language.ZH) dateTextZh else dateTextEn
    fun locationName(lang: Language) = if (lang == Language.ZH) locationNameZh else locationNameEn
    fun description(lang: Language) = if (lang == Language.ZH) descriptionZh else descriptionEn
    fun getTags(): List<String> = if (tagsJson.isBlank()) emptyList() else tagsJson.split(",")
}

