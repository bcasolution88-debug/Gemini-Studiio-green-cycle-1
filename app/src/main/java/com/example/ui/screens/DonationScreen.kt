package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.DonationDrive
import com.example.data.model.Language
import com.example.service.AppFirebaseMessagingService
import com.example.ui.components.BilingualHeader
import com.example.ui.components.DonationBadge
import com.example.ui.components.DonationSuccessDialog

import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import com.example.data.repository.DonationScheduleRepository
import com.example.ui.components.DonationCalendarDialog

@Composable
fun DonationScreen(
    drives: List<DonationDrive>,
    lang: Language,
    onDriveClick: (DonationDrive) -> Unit,
    communityWishlists: List<com.example.data.model.CommunityWishlistItem> = emptyList(),
    onMatchWishlist: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var showReminderDialog by remember { mutableStateOf(false) }
    var reminderSettings by remember { mutableStateOf(com.example.util.DonationReminderManager.getSettings(context)) }

    if (showReminderDialog) {
        DonationReminderDialog(
            currentSettings = reminderSettings,
            lang = lang,
            onDismiss = { showReminderDialog = false },
            onSave = { newSettings ->
                com.example.util.DonationReminderManager.saveSettings(context, newSettings)
                reminderSettings = newSettings
                showReminderDialog = false
                val toast = if (lang == Language.ZH) "✅ 捐赠与储物清理提醒已保存！" else "✅ Recurring donation reminder saved!"
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
            },
            onTest = {
                com.example.util.DonationReminderManager.sendDonationReminderNotification(
                    context = context,
                    settings = reminderSettings,
                    isTest = true,
                    lang = lang
                )
            }
        )
    }

    // Requirement: Calendar-Based Scheduling Tool State
    var showCalendarSchedulerDialog by remember { mutableStateOf(false) }

    if (showCalendarSchedulerDialog) {
        DonationCalendarDialog(
            initialDriveTitle = if (lang == Language.ZH) "爱心捐赠物品日历预约" else "Donation Item Scheduled Pickup",
            lang = lang,
            onDismiss = { showCalendarSchedulerDialog = false },
            onScheduledSuccess = { schedule ->
                val toast = if (lang == Language.ZH) "✅ 预约成功！已在 [${schedule.scheduledDateDisplayZh} ${schedule.scheduledTimeSlotZh}] 上门，中台实时同步中！" else "✅ Appointment confirmed for ${schedule.scheduledDateDisplayEn}!"
                Toast.makeText(context, toast, Toast.LENGTH_LONG).show()
            }
        )
    }

    // Requirement 3: Category Filtering System State
    val categories = if (lang == Language.ZH) {
        listOf("全部", "废旧电子/家电", "纺织旧衣/鞋帽", "家居用品/家具", "图书文具/玩具")
    } else {
        listOf("All", "Electronic Waste", "Textiles", "Household Goods", "Books & Toys")
    }
    var selectedCategoryFilter by remember { mutableStateOf(categories[0]) }

    // Requirement 2: Real-time Courier Collection Notification Stage State (0..3)
    var courierStep by remember { mutableIntStateOf(1) }

    var activeFormDrive by remember { mutableStateOf<DonationDrive?>(null) }
    var celebrationDrive by remember { mutableStateOf<DonationDrive?>(null) }
    var donationItemTitle by remember { mutableStateOf("") }
    var donationQuantityText by remember { mutableStateOf("1") }
    var donorContactPhone by remember { mutableStateOf("13800138001") }

    if (celebrationDrive != null) {
        val drive = celebrationDrive!!
        DonationSuccessDialog(
            itemTitle = donationItemTitle.ifBlank { drive.title(lang) },
            donorTarget = drive.targetGroup(lang),
            lang = lang,
            onDismiss = {
                celebrationDrive = null
                donationItemTitle = ""
            }
        )
    }

    if (activeFormDrive != null) {
        val drive = activeFormDrive!!
        Dialog(onDismissRequest = { activeFormDrive = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_donation_submit_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (lang == Language.ZH) "❤️ 快捷提交爱心捐赠物品" else "❤️ Direct Item Donation Request",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63)
                    )

                    Text(
                        text = "项目: ${drive.title(lang)} (${drive.targetGroup(lang)})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = donationItemTitle,
                        onValueChange = { donationItemTitle = it },
                        label = { Text(if (lang == Language.ZH) "捐赠物品名称 (例: 儿童保温羽绒服/绘本)" else "Item Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("donation_item_title_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = donationQuantityText,
                        onValueChange = { donationQuantityText = it },
                        label = { Text(if (lang == Language.ZH) "捐赠数量 (件/套)" else "Quantity") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = donorContactPhone,
                        onValueChange = { donorContactPhone = it },
                        label = { Text(if (lang == Language.ZH) "联系电话 (顺丰免费上门揽收)" else "Contact Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { activeFormDrive = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(if (lang == Language.ZH) "取消" else "Cancel")
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                celebrationDrive = drive
                                activeFormDrive = null
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("submit_quick_donation_btn"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                        ) {
                            Text(
                                text = if (lang == Language.ZH) "🚀 确认并领取积分" else "Confirm & Earn Points",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    val filteredDrives = drives.filter { drive ->
        when (selectedCategoryFilter) {
            "全部", "All" -> true
            "废旧电子/家电", "Electronic Waste", "⚡ 废旧电子/家电", "⚡ Electronic Waste" -> drive.category == "Electronic Waste"
            "纺织旧衣/鞋帽", "Textiles", "👕 纺织旧衣/鞋帽", "👕 Textiles" -> drive.category == "Textiles"
            "家居用品/家具", "Household Goods", "🛋️ 家居用品/家具", "🛋️ Household Goods" -> drive.category == "Household Goods"
            "图书文具/玩具", "Books & Toys", "📚 图书文具/玩具", "📚 Books & Toys" -> drive.category == "Books & Toys"
            else -> true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BilingualHeader(
            title = if (lang == Language.ZH) "公益捐赠与爱心专区" else "Charity Donation Portal"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Donation Banner Image
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.height(130.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_donation_banner_1785408838723),
                            contentDescription = "Donation Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (lang == Language.ZH) "让闲置充满爱心·精准定向捐赠" else "Turn Items into Love · Directed Donation",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (lang == Language.ZH) "可获得专有电子捐赠证书与绿碳积分" else "Earn digital certificates & green eco credits",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Public Welfare Impact Dashboard (Requirement 4)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("public_welfare_impact_dashboard"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = "Impact",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == Language.ZH) "🌱 平台公益减碳与转移影响力大屏" else "🌱 Public Welfare Impact Dashboard",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DonationStatItem("1,850 kg", if (lang == Language.ZH) "转移废弃物" else "Waste Diverted", Icons.Default.Recycling)
                            DonationStatItem("3,720 kg", if (lang == Language.ZH) "CO₂减排量" else "CO₂ Reduced", Icons.Default.Eco)
                            DonationStatItem("1,240 人", if (lang == Language.ZH) "受助人群" else "People Helped", Icons.Default.VolunteerActivism)
                            DonationStatItem("96.4%", if (lang == Language.ZH) "爱心转赠率" else "Rehome Rate", Icons.Default.Verified)
                        }
                    }
                }
            }

            // Calendar-based Scheduling Tool Item
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calendar_pickup_scheduler_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Calendar Scheduler",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (lang == Language.ZH) "📅 爱心捐赠上门日历预约系统" else "📅 Calendar Donation Pickup Scheduler",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (lang == Language.ZH) "自主选择10日内最佳揽收时段 · 实时派单至站长中台" else "Pick preferred time slot & sync with admin dashboard",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { showCalendarSchedulerDialog = true },
                                modifier = Modifier.testTag("open_calendar_scheduler_btn"),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = if (lang == Language.ZH) "日历选时段" else "Schedule",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Display list of currently scheduled pickup appointments
                        val activeSchedules = DonationScheduleRepository.getSchedules()
                        if (activeSchedules.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (lang == Language.ZH) "📋 我的预约行程 (管理员中台实时同步中)" else "📋 My Scheduled Pickups (Synced)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                activeSchedules.take(2).forEach { sch ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🗓️ ${sch.scheduledDateDisplay(lang)}  |  ${sch.scheduledTimeSlot(lang)}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (sch.status == "CONFIRMED") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (sch.status == "CONFIRMED") (if (lang == Language.ZH) "✅ 已接单" else "Confirmed") else (if (lang == Language.ZH) "⏳ 待站长确认" else "Pending Sync"),
                                                        fontSize = 10.sp,
                                                        color = if (sch.status == "CONFIRMED") Color(0xFF2E7D32) else Color(0xFFE65100),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "📦 物品: ${sch.itemDescription} (${sch.itemQuantity})",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "📍 地址: ${sch.pickupAddress} (${sch.donorPhone})",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                DonationReminderCard(
                    settings = reminderSettings,
                    lang = lang,
                    onOpenSettings = { showReminderDialog = true }
                )
            }

            // NGO Community Wishlist Feature
            if (communityWishlists.isNotEmpty()) {
                item {
                    CommunityWishlistSection(
                        lang = lang,
                        wishlists = communityWishlists,
                        onMatchClick = { wishId ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onMatchWishlist(wishId)
                        }
                    )
                }
            }

            // Category-Specific Monthly Targets Visual Progress Card (Requirement #3)

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("monthly_category_targets_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Recycling,
                                    contentDescription = "Category Targets",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == Language.ZH) "🎯 8月社区特定品类回收月度目标" else "🎯 Category Monthly Recycling Targets",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (lang == Language.ZH) "距结奖 5 天" else "5 days left",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Text(
                            text = if (lang == Language.ZH) "达成月度品类目标全区居民将解锁社区环保基金与双倍绿碳积分奖励" else "Reaching category monthly targets unlocks community eco-grants and double points.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        // Category 1: Clothing & Textiles
                        CategoryTargetProgressItem(
                            categoryName = if (lang == Language.ZH) "👕 废旧衣物与织物 (Textiles)" else "👕 Clothing & Textiles",
                            currentKg = 4250,
                            targetKg = 5000,
                            color = Color(0xFFE91E63),
                            hintText = if (lang == Language.ZH) "还差 750 kg 达成! 上门回收双倍积分" else "750 kg left! Double points for pickup"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category 2: Plastics & Bottles
                        CategoryTargetProgressItem(
                            categoryName = if (lang == Language.ZH) "🍾 塑料瓶与硬质塑料 (Plastics)" else "🍾 Plastics & Bottles",
                            currentKg = 11200,
                            targetKg = 12000,
                            color = Color(0xFF0288D1),
                            hintText = if (lang == Language.ZH) "已达成 93%! 冲刺全区标杆社区" else "93% completed! Sprinting for benchmark"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category 3: Electronics & Appliances
                        CategoryTargetProgressItem(
                            categoryName = if (lang == Language.ZH) "💻 废旧家电与数码 (Electronics)" else "💻 Electronics & Appliances",
                            currentKg = 1800,
                            targetKg = 3000,
                            color = Color(0xFFED6C02),
                            hintText = if (lang == Language.ZH) "已达成 60%, 免费上门拆解服务中" else "60% completed, free disassembly active"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category 4: Paper & Heavy Metal
                        CategoryTargetProgressItem(
                            categoryName = if (lang == Language.ZH) "📦 废纸箱与重质金属 (Paper & Metal)" else "📦 Paper & Heavy Metal",
                            currentKg = 18500,
                            targetKg = 20000,
                            color = Color(0xFF2E7D32),
                            hintText = if (lang == Language.ZH) "即将达成! 24H智能分类箱已开启奖励" else "Almost there! 24H Smart Bins active"
                        )
                    }
                }
            }

            // Community Milestones Section with Progress Bars (Requirement 4)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("community_milestones_section"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Milestones",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == Language.ZH) "🏆 社区公益里程碑与达成进度" else "🏆 Community Welfare Milestones",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Milestone 1
                        CommunityMilestoneItem(
                            title = if (lang == Language.ZH) "大凉山儿童冬衣及图书筹集 (目标:1000件)" else "Daliangshan Winter Clothes Drive (Goal: 1000)",
                            current = 680,
                            target = 1000,
                            unit = if (lang == Language.ZH) "件" else "items",
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Milestone 2
                        CommunityMilestoneItem(
                            title = if (lang == Language.ZH) "独居老人小家电焕新援助 (目标:500件)" else "Elders Home Appliance Aid (Goal: 500)",
                            current = 340,
                            target = 500,
                            unit = if (lang == Language.ZH) "件" else "items",
                            color = Color(0xFFE91E63)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Milestone 3
                        CommunityMilestoneItem(
                            title = if (lang == Language.ZH) "全社区垃圾源头减量突破 (目标:100,000kg)" else "Community Source Reduction (Goal: 100,000kg)",
                            current = 85000,
                            target = 100000,
                            unit = "kg",
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            // User Donation Tracking Card (Requirement 4)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_donation_tracker_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = "My Donations",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == Language.ZH) "📦 我的个人捐赠追踪与电子证书" else "📦 My Donation Tracker",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (lang == Language.ZH) "已获3张证书" else "3 Certs",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live Courier Collection Real-time Journey Card (Requirement 2)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                                .testTag("courier_collection_tracker_card")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = "Courier",
                                        tint = Color(0xFF0288D1),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (lang == Language.ZH) "🚚 捐赠包裹物流与快递揽收实时追踪" else "🚚 Donation Parcel Courier Pickup Tracker",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = when (courierStep) {
                                        0 -> if (lang == Language.ZH) "待接单" else "Pending"
                                        1 -> if (lang == Language.ZH) "已派单" else "Assigned"
                                        2 -> if (lang == Language.ZH) "已揽收" else "Collected"
                                        else -> if (lang == Language.ZH) "已送达" else "Delivered"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0288D1)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = when (courierStep) {
                                    0 -> if (lang == Language.ZH) "已为您匹配最近的顺丰绿色回收专员..." else "Matching nearest courier..."
                                    1 -> if (lang == Language.ZH) "顺丰快递员王师傅 (138****9012) 已接单，预计20分钟内到社区揽收【闲置羽绒服与微波炉】" else "Courier Wang (138****9012) assigned, arriving in 20 mins."
                                    2 -> if (lang == Language.ZH) "【包裹 #DON-2026-882】顺丰快递员已现场揽收签收！正转运发往绿色拆解仓及大凉山定向受赠点。" else "Parcel #DON-2026-882 collected by courier! In transit to Eco Depot."
                                    else -> if (lang == Language.ZH) "【包裹 #DON-2026-882】已送达大凉山偏远山区小学！已生成区块链减碳证书与爱心回执。" else "Parcel delivered to school! Digital carbon certificate created."
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    courierStep = (courierStep + 1) % 4
                                    val (title, body) = when (courierStep) {
                                        1 -> Pair(
                                            if (lang == Language.ZH) "🚚 顺丰快递员已接单上门揽收！" else "🚚 Courier Accepted Your Donation Pickup!",
                                            if (lang == Language.ZH) "顺丰快递员王师傅 (138****9012) 已接单，正前往三里屯社区揽收【闲置暖心羽绒服与微波炉】。" else "Courier Wang (138****9012) accepted pickup for your donated items."
                                        )
                                        2 -> Pair(
                                            if (lang == Language.ZH) "📦 顺丰快递员已成功现场揽收签收！" else "📦 Courier Collected Your Donated Items!",
                                            if (lang == Language.ZH) "【爱心捐赠包裹 #DON-2026-882】已完成现场拆包验视与揽收，正发往绿色拆解与转运仓。获得绿碳积分 +100！" else "Donation parcel collected & inspected. Heading to Eco Depot! +100 Eco Points."
                                        )
                                        3 -> Pair(
                                            if (lang == Language.ZH) "🚛 捐赠物品已送达并完成签收！" else "🚛 Donation Parcel Delivered to School!",
                                            if (lang == Language.ZH) "大凉山偏远山区小学已全数签收您的捐赠物资！感谢您的爱心善举，减碳证书已更新！" else "Delivered & signed by school! Digital donation certificate updated."
                                        )
                                        else -> Pair(
                                            if (lang == Language.ZH) "🔔 重新预约快递上门揽收服务" else "🔔 Scheduled New Courier Pickup",
                                            if (lang == Language.ZH) "新的爱心捐赠物品已发起快递预约，等待专员接单。" else "New donation pickup requested."
                                        )
                                    }
                                    AppFirebaseMessagingService.sendLocalItemStatusNotification(context, title, body)
                                    Toast.makeText(context, if (lang == Language.ZH) "🔔 已触发本地推送通知: $title" else "Push Sent: $title", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("courier_collection_notification_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Trigger Notification",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lang == Language.ZH) "🔔 模拟/推送快递上门揽收实时动态" else "🔔 Push Courier Collection Real-time Alert",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Completed User Donation Record
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("儿童暖心羽绒服与绘本", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("受赠方: 大凉山偏远山区小学", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("已受赠签收", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }

            // Requirement 3: Filtering System for Donation Zone Categories
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (lang == Language.ZH) "正在进行的定向捐赠项目" else "Active Targeted Donation Drives",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = if (lang == Language.ZH) "共 ${filteredDrives.size} 个适用项目" else "${filteredDrives.size} drives",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Filter Chips (Requirement 3)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("donation_category_filter_row")
                    ) {
                        items(categories) { categoryName ->
                            val isSelected = categoryName == selectedCategoryFilter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedCategoryFilter = categoryName
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .testTag("donation_category_chip_${categoryName}")
                            ) {
                                Text(
                                    text = categoryName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (filteredDrives.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (lang == Language.ZH) "🔍 暂无符合 \"$selectedCategoryFilter\" 品类的捐赠项目" else "No drives found in \"$selectedCategoryFilter\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { selectedCategoryFilter = categories[0] },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(if (lang == Language.ZH) "重置并查看全部" else "Reset to All")
                            }
                        }
                    }
                }
            } else {
                items(filteredDrives) { drive ->
                    DonationDriveCard(
                        drive = drive,
                        lang = lang,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            activeFormDrive = drive
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun CategoryTargetProgressItem(
    categoryName: String,
    currentKg: Int,
    targetKg: Int,
    color: Color,
    hintText: String
) {
    val progress = (currentKg.toFloat() / targetKg.toFloat()).coerceIn(0f, 1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$currentKg / $targetKg kg (${(progress * 100).toInt()}%)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "⚡ $hintText",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CommunityMilestoneItem(
    title: String,
    current: Int,
    target: Int,
    unit: String,
    color: Color
) {
    val progress = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "$current / $target $unit (${(progress * 100).toInt()}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun DonationStatItem(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DonationDriveCard(
    drive: DonationDrive,
    lang: Language,
    onClick: () -> Unit
) {
    val progress = drive.collectedItems.toFloat() / drive.goalItems.toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("donation_card_${drive.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE91E63).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Target",
                            tint = Color(0xFFE91E63)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = drive.title(lang),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "目标群体: ${drive.targetGroup(lang)}",
                            fontSize = 11.sp,
                            color = Color(0xFFE91E63)
                        )
                    }
                }

                DonationBadge(donorTarget = drive.targetGroup(lang), lang = lang)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = drive.description(lang),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "捐赠进度: ${drive.collectedItems} / ${drive.goalItems}件",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
            ) {
                Text(
                    text = if (lang == Language.ZH) "参与捐赠物品" else "Donate to this Drive",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CommunityWishlistSection(
    lang: Language,
    wishlists: List<com.example.data.model.CommunityWishlistItem>,
    onMatchClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("community_wishlist_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = "Community Wishlist",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == Language.ZH) "🤝 NGO社区爱心需求清单" else "🤝 NGO Community Wishlist",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (lang == Language.ZH) "${wishlists.size}个机构急需" else "${wishlists.size} NGOs Needing",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = if (lang == Language.ZH) "本地避难所、小动物基地与儿童启智馆直接列出所需物资，一键匹配爱心投递！" else "Local NGOs & shelters list active needs. Match your donation directly!",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            wishlists.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.ngoName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (item.urgencyLevel == "HIGH") {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFD32F2F).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (lang == Language.ZH) "🔥 极度急需" else "🔥 Urgent Need",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.neededTitle(lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.description(lang),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress Indicator
                        val progress = (item.currentCount.toFloat() / item.targetCount.toFloat()).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${item.currentCount}/${item.targetCount}份 (${(progress * 100).toInt()}%)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📍 ${item.address(lang)}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = { onMatchClick(item.id) },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("match_wishlist_btn_${item.id}"),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lang == Language.ZH) "匹配爱心捐赠" else "Match Donation",
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonationReminderCard(
    settings: com.example.util.DonationReminderSettings,
    lang: Language,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recurring_donation_reminder_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Reminder",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == Language.ZH) "⏰ 闲置物品定期捐赠与清理提醒" else "⏰ Recurring Pantry Donation Reminder",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (settings.isEnabled) {
                        if (lang == Language.ZH) "已开启 · 频次: ${settings.frequencyLabel(lang)} (${settings.categoryLabel(lang)})"
                        else "Enabled · ${settings.frequencyLabel(lang)} (${settings.categoryLabel(lang)})"
                    } else {
                        if (lang == Language.ZH) "未开启定期清理提醒" else "Reminder Disabled"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .testTag("open_donation_reminder_dialog_btn")
                    .height(36.dp),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(
                    text = if (lang == Language.ZH) "⚙️ 提醒设置" else "⚙️ Settings",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DonationReminderDialog(
    currentSettings: com.example.util.DonationReminderSettings,
    lang: Language,
    onDismiss: () -> Unit,
    onSave: (com.example.util.DonationReminderSettings) -> Unit,
    onTest: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(currentSettings.isEnabled) }
    var frequency by remember { mutableStateOf(currentSettings.frequency) }
    var categoryFocus by remember { mutableStateOf(currentSettings.categoryFocus) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("donation_reminder_settings_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == Language.ZH) "⏰ 定期清理与捐赠提醒设置" else "⏰ Set Donation Reminder",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Close")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == Language.ZH) "启用定期清理推送" else "Enable Cleanout Push",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    androidx.compose.material3.Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        modifier = Modifier.testTag("toggle_donation_reminder_switch")
                    )
                }

                Text(
                    text = if (lang == Language.ZH) "提醒频次" else "Frequency",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "MONTHLY" to (if (lang == Language.ZH) "每月 1 日" else "Monthly"),
                        "BIWEEKLY" to (if (lang == Language.ZH) "每两周" else "Bi-Weekly"),
                        "QUARTERLY" to (if (lang == Language.ZH) "每季度" else "Quarterly")
                    ).forEach { (key, label) ->
                        val isSel = frequency == key
                        OutlinedButton(
                            onClick = { frequency = key },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Text(
                    text = if (lang == Language.ZH) "重点清理储物品类" else "Target Category Focus",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "ALL" to (if (lang == Language.ZH) "📦 全品类储物清理 (粮仓/橱柜/书架)" else "📦 All Pantry Items"),
                        "CLOTHES" to (if (lang == Language.ZH) "👕 闲置衣物鞋帽 (衣柜四季整理)" else "👕 Apparel & Clothing"),
                        "BOOKS" to (if (lang == Language.ZH) "📚 闲置图书文具 (二手书橱)" else "📚 Books & Stationeries"),
                        "ELECTRONICS" to (if (lang == Language.ZH) "⚡ 旧电子小家电 (抽屉闲置品)" else "⚡ Electronics & Gadgets")
                    ).forEach { (key, label) ->
                        val isSel = categoryFocus == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryFocus = key }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onTest,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("test_donation_reminder_notification_btn"),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text(if (lang == Language.ZH) "🚀 测试提醒" else "Test Push", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onSave(com.example.util.DonationReminderSettings(isEnabled, frequency, 1, categoryFocus))
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("save_donation_reminder_btn"),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text(if (lang == Language.ZH) "保存设置" else "Save Reminder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

