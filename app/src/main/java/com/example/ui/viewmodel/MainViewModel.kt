package com.example.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CategoryRepository
import com.example.data.model.DonationDrive
import com.example.data.model.ForumPost
import com.example.data.model.MajorCategory
import com.example.data.model.OrderAgreement
import com.example.data.model.RecycleItem
import com.example.data.model.RecycleStation
import com.example.data.model.SupplyItem
import com.example.data.repository.RecycleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

import android.content.Context
import com.example.service.AppFirebaseMessagingService

class MainViewModel(private val repository: RecycleRepository) : ViewModel() {

    val items: StateFlow<List<RecycleItem>> = repository.allItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val forumPosts: StateFlow<List<ForumPost>> = repository.allForumPosts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stations: StateFlow<List<RecycleStation>> = repository.allStations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val orders: StateFlow<List<OrderAgreement>> = repository.allOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyValue()
    )

    var searchQuery by mutableStateOf("")
    var selectedCategory: MajorCategory? by mutableStateOf(null)
    var selectedItemDetail: RecycleItem? by mutableStateOf(null)
    var selectedPostDetail: ForumPost? by mutableStateOf(null)
    var selectedStationDetail: RecycleStation? by mutableStateOf(null)
    var selectedOrderDetail: OrderAgreement? by mutableStateOf(null)
    var selectedDonationDrive: DonationDrive? by mutableStateOf(null)

    val donationDrives: List<DonationDrive> get() = repository.donationDrives
    val supplyItems: List<SupplyItem> get() = repository.supplyItems
    
    // Community Wishlist & Sustainability Events state
    var communityWishlists by mutableStateOf<List<com.example.data.model.CommunityWishlistItem>>(repository.communityWishlists)
    var sustainabilityEvents by mutableStateOf<List<com.example.data.model.SustainabilityEvent>>(repository.sustainabilityEvents)

    // Gemini Green Tip of the Week state
    var weeklyGreenTip by mutableStateOf<com.example.service.GreenTipResult?>(null)
    var isGreenTipLoading by mutableStateOf(false)

    init {
        refreshWeeklyGreenTip()
    }

    fun refreshWeeklyGreenTip() {
        viewModelScope.launch {
            isGreenTipLoading = true
            val categories = items.value.map { it.subCategoryId }.distinct().take(4)
            weeklyGreenTip = com.example.service.GeminiService.generateWeeklyGreenTip(categories)
            isGreenTipLoading = false
        }
    }



    fun matchWishlistItem(context: Context? = null, wishlistId: String) {
        if (repository.matchWishlistItem(wishlistId)) {
            communityWishlists = repository.communityWishlists.toList()
            context?.let { ctx ->
                val wish = communityWishlists.find { it.id == wishlistId }
                AppFirebaseMessagingService.sendLocalItemStatusNotification(
                    context = ctx,
                    title = "🎁 社区爱心匹配成功 / Donation Match Confirmed",
                    message = "您的闲置物料成功与【${wish?.ngoName ?: "NGO机构"}】需求相匹配！管理员已安排专员上门受捐。"
                )
            }
        }
    }

    fun toggleJoinEvent(context: Context? = null, eventId: String) {
        val updated = repository.toggleJoinEvent(eventId)
        if (updated != null) {
            sustainabilityEvents = repository.sustainabilityEvents.toList()
            if (updated.isJoined && context != null) {
                AppFirebaseMessagingService.sendLocalItemStatusNotification(
                    context = context,
                    title = "🎉 环保活动报名成功 / Event Registration Confirmed",
                    message = "您已报名【${updated.titleZh}】！活动时间: ${updated.dateTextZh}，集合地点: ${updated.locationNameZh}。"
                )
            }
        }
    }


    // Actions
    fun publishRecycleItem(
        title: String,
        majorCategoryId: String,
        subCategoryId: String,
        condition: String,
        description: String,
        expectedPrice: Double,
        isDonation: Boolean,
        donorTarget: String?,
        images: List<String>,
        videoUrl: String?,
        location: String,
        contactPhone: String,
        contactWechat: String?
    ) {
        val newItem = RecycleItem(
            id = "item_${System.currentTimeMillis()}",
            title = title,
            majorCategoryId = majorCategoryId,
            subCategoryId = subCategoryId,
            condition = condition,
            description = description,
            expectedPrice = expectedPrice,
            isDonation = isDonation,
            donorTarget = donorTarget,
            imageListJson = if (images.isEmpty()) "placeholder_1,placeholder_2,placeholder_3" else images.joinToString(","),
            videoUrl = videoUrl,
            location = location,
            contactPhone = contactPhone,
            contactWechat = contactWechat,
            status = "PENDING"
        )

        viewModelScope.launch {
            repository.addItem(newItem)
            // Auto add to forum if video/images present
            val newPost = ForumPost(
                id = "post_${System.currentTimeMillis()}",
                authorName = "发布用户",
                authorAvatar = "",
                title = "【待回收发布】$title",
                content = description,
                imageListJson = newItem.imageListJson,
                videoUrl = videoUrl,
                tagsJson = "待回收,新发布,社区旧物",
                isPendingRecycle = true,
                relatedItemId = newItem.id
            )
            repository.addForumPost(newPost)
        }
    }

    // Admin Dispatch / Price Assessment Action
    fun adminAssignRecipient(
        context: Context? = null,
        itemId: String,
        recipientName: String,
        recipientPhone: String,
        recipientWechat: String,
        stationName: String,
        scheduledTime: String,
        scheduledLocation: String,
        estimatedPrice: Double,
        evaluation: String? = null
    ) {
        viewModelScope.launch {
            val item = repository.getItemById(itemId) ?: return@launch
            val updated = item.copy(
                status = "SCHEDULED",
                assignedRecipientName = recipientName,
                assignedRecipientPhone = recipientPhone,
                assignedRecipientWechat = recipientWechat,
                assignedStationName = stationName,
                scheduledTime = scheduledTime,
                scheduledLocation = scheduledLocation,
                estimatedPrice = estimatedPrice,
                evaluation = evaluation
            )
            repository.updateItem(updated)

            // Trigger FCM Push Notification
            context?.let {
                AppFirebaseMessagingService.sendLocalItemStatusNotification(
                    context = it,
                    title = "📅 预约上门通知 / Pickup Scheduled",
                    message = "【${item.title}】管理员已为您指派回收专员: $recipientName，预约时间: $scheduledTime ($stationName)",
                    itemId = item.id
                )
            }

            // Create Order / Electronic Agreement
            val newOrder = OrderAgreement(
                orderId = "ORD_${System.currentTimeMillis().toString().takeLast(8)}",
                itemId = item.id,
                itemTitle = item.title,
                sellerName = "平台发布用户 (${item.contactPhone.takeLast(4)})",
                sellerPhone = item.contactPhone,
                collectorName = recipientName,
                collectorPhone = recipientPhone,
                collectorWechat = recipientWechat,
                stationName = stationName,
                scheduledTime = scheduledTime,
                location = scheduledLocation,
                finalPrice = estimatedPrice,
                status = "SCHEDULED",
                digitalSignature = "GreenCycle_System_Verified_Signature",
                isSigned = true
            )
            repository.addOrder(newOrder)
        }
    }

    fun signAgreement(context: Context? = null, orderId: String, signature: String) {
        viewModelScope.launch {
            val order = repository.getOrderById(orderId) ?: return@launch
            val updated = order.copy(
                digitalSignature = signature,
                isSigned = true,
                status = "COMPLETED"
            )
            repository.updateOrder(updated)

            // Also update associated item to COMPLETED
            val item = repository.getItemById(order.itemId)
            if (item != null) {
                repository.updateItem(item.copy(status = "COMPLETED"))
            }

            // Trigger FCM Push Notification
            context?.let {
                AppFirebaseMessagingService.sendLocalItemStatusNotification(
                    context = it,
                    title = "🎉 回收/捐赠完结通知 / Recycle Job Completed",
                    message = "【${order.itemTitle}】电子协议签署成功，回收/捐赠服务已交接完结！已为您累加绿碳积分。",
                    itemId = order.itemId
                )
            }
        }
    }

    fun registerStation(
        name: String,
        address: String,
        phone: String,
        wechat: String,
        businessHours: String,
        tags: String,
        services: String,
        priceList: String
    ) {
        val station = RecycleStation(
            id = "sta_${System.currentTimeMillis()}",
            name = name,
            rating = 5.0f,
            address = address,
            latitude = 39.9200,
            longitude = 116.4200,
            phone = phone,
            wechat = wechat,
            businessHours = businessHours,
            tagsJson = tags,
            photoUrl = "",
            serviceCategoriesJson = services,
            priceListJson = priceList,
            isVerified = true,
            distanceKm = 1.1
        )
        viewModelScope.launch {
            repository.addStation(station)
        }
    }

    // Forum Reactions & Comments Actions
    fun toggleLikePost(postId: String) {
        viewModelScope.launch {
            repository.toggleLikePost(postId)
        }
    }

    fun toggleHeartPost(postId: String) {
        viewModelScope.launch {
            repository.toggleHeartPost(postId)
        }
    }

    fun toggleHelpfulPost(postId: String) {
        viewModelScope.launch {
            repository.toggleHelpfulPost(postId)
        }
    }

    fun addForumComment(context: Context? = null, post: ForumPost, commentText: String) {
        viewModelScope.launch {
            val updatedPost = post.copy(commentsCount = post.commentsCount + 1)
            repository.updateForumPost(updatedPost)

            // Trigger FCM Push Notification
            context?.let {
                AppFirebaseMessagingService.sendLocalForumReplyNotification(
                    context = it,
                    title = "💬 社区新回复通知 / Community Reply Alert",
                    message = "【${post.title.take(18)}...】收到热心回复: \"$commentText\"",
                    postId = post.id
                )
            }
        }
    }

    private fun <T> emptyValue(): List<T> = emptyList()
}
