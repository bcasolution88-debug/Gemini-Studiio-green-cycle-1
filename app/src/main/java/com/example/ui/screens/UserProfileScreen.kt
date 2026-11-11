package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import com.example.ui.components.BugReportDialog
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.data.model.LocalLanguageManager
import com.example.data.model.RecycleItem
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import com.example.util.ShareUtils
import com.example.ui.components.EnvironmentalImpactChart
import com.example.ui.components.BilingualHeader
import com.example.ui.components.RecyclingHistoryTimeline
import com.example.ui.components.StatusChip
import com.example.ui.theme.LocalThemeManager
import androidx.compose.material.icons.filled.ReceiptLong
import com.example.data.model.DonationReceipt
import com.example.ui.components.DonationReceiptDialog
import com.example.util.DonationReceiptExporter

import androidx.compose.runtime.collectAsState
import com.example.data.repository.UserAuthRepository
import com.example.ui.components.AuthDialog
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.Button

data class ProfileMenuItem(
    val id: String,
    val titleZh: String,
    val titleEn: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun UserProfileScreen(
    lang: Language,
    items: List<RecycleItem> = emptyList(),
    onItemClick: (RecycleItem) -> Unit = {},
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeManager = LocalThemeManager.current
    val langManager = LocalLanguageManager.current

    val authUser by UserAuthRepository.currentUser.collectAsState()
    val isLoggedIn by UserAuthRepository.isLoggedIn.collectAsState()
    var isFirestoreSynced by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var activeReceipt by remember { mutableStateOf<DonationReceipt?>(null) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showBugReportDialog by remember { mutableStateOf(false) }

    if (activeReceipt != null) {
        DonationReceiptDialog(
            receipt = activeReceipt!!,
            lang = lang,
            onDismiss = { activeReceipt = null }
        )
    }

    if (showAuthDialog) {
        AuthDialog(
            initialTab = 0,
            onDismiss = { showAuthDialog = false },
            onSuccess = { showAuthDialog = false }
        )
    }

    val ongoingItems = items.filter { it.status == "PENDING" || it.status == "MATCHING" || it.status == "SCHEDULED" }
    val completedItems = items.filter { it.status == "COMPLETED" || it.isDonation }

    val menuItems = listOf(
        ProfileMenuItem("my_posts", "我的发布", "My Posts", Icons.Default.PostAdd, Color(0xFF2E7D32)),
        ProfileMenuItem("my_orders", "我的订单", "My Orders", Icons.Default.Assignment, Color(0xFFED6C02)),
        ProfileMenuItem("my_donations", "捐赠记录", "Donations", Icons.Default.CardGiftcard, Color(0xFFE91E63)),
        ProfileMenuItem("saved_stations", "收藏站点", "Saved Stations", Icons.Default.Store, Color(0xFF0288D1)),
        ProfileMenuItem("messages", "消息通知", "Messages", Icons.Default.Notifications, Color(0xFF7B1FA2)),
        ProfileMenuItem("admin", "管理员控制台", "Admin Console", Icons.Default.AdminPanelSettings, Color(0xFF1B5E20))
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
        BilingualHeader(
            title = if (lang == Language.ZH) "个人中心 (绿碳身份已同步)" else "User Profile (Green Auth)",
            onAuthClick = { showAuthDialog = true }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Avatar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = authUser.displayName,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "账号: ${authUser.email}",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            // Login / Switch Account Action Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .clickable { showAuthDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("profile_switch_account_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = "Login/Register",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (lang == Language.ZH) "登录/切换" else "Sign In",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProfileStatItem("${authUser.recyclePoints} 积分", if (lang == Language.ZH) "绿碳积分 (Credits)" else "Green Credits")
                            ProfileStatItem("${authUser.co2SavedKg} kg", if (lang == Language.ZH) "累计减碳量" else "CO2 Saved")
                            ProfileStatItem("${authUser.completedRecycles} 次", if (lang == Language.ZH) "完成回收" else "Recycled")
                        }
                    }
                }
            }

            // Green Credits Accumulated Card (Requirement #2)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = "Green Credits",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == Language.ZH) "绿碳积分 (Green Credits) 明细" else "Accumulated Green Credits Breakdown",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondary)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Lv.2 绿碳先锋",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            GreenCreditDetailItem("爱心捐赠获得", "+200 积分", Color(0xFFE91E63))
                            GreenCreditDetailItem("旧物回收交接", "+120 积分", Color(0xFF2E7D32))
                            GreenCreditDetailItem("社区互动打卡", "+50 积分", Color(0xFF0288D1))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Native Social Share Button (Requirement #2)
                        OutlinedButton(
                            onClick = {
                                ShareUtils.shareGreenCreditsAchievement(
                                    context = context,
                                    displayName = authUser.displayName,
                                    points = authUser.recyclePoints,
                                    co2SavedKg = authUser.co2SavedKg,
                                    completedRecycles = authUser.completedRecycles,
                                    lang = lang
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("share_green_credits_btn"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (lang == Language.ZH) "一键原生社交分享绿碳成就 / Share Achievement" else "Share Green Credits Achievement",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Environmental Impact Data Visualization Chart (Requirement #2)
            item {
                EnvironmentalImpactChart(lang = lang)
            }

            // Aggregated Activity Profile Card (Total Recycling Count, Forum Activity & Donation Badges)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("aggregated_activity_profile_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (lang == Language.ZH) "📊 个人环保全维度聚合看板 / Activity Overview" else "Aggregated Profile & Eco Activity",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Row 1: Total Recycling & Forum Activity Aggregation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Total Recycling Count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (lang == Language.ZH) "♻️ 累计回收总量" else "Total Recycling",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${items.size} 单",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (lang == Language.ZH) "完结交接: ${completedItems.size} 单" else "Completed: ${completedItems.size}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Community Forum Activity
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (lang == Language.ZH) "💬 社区论坛动态" else "Forum Activity",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "3 篇帖子",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (lang == Language.ZH) "点赞与互动: 24 次" else "Interactions: 24",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Row 2: Badges Earned for Donations & Eco Achievements
                        Text(
                            text = if (lang == Language.ZH) "🏅 获得勋章与捐赠荣誉 (Badges Earned)" else "Badges Earned for Donations",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val donationCount = items.count { it.isDonation }
                            val hasCompleted = completedItems.isNotEmpty()

                            DonationBadgeChip(
                                badgeName = if (lang == Language.ZH) "❤️ 爱心捐赠使者" else "Donation Envoy",
                                isUnlocked = donationCount > 0,
                                lang = lang
                            )
                            DonationBadgeChip(
                                badgeName = if (lang == Language.ZH) "🌱 零废弃达人" else "Zero Waste Expert",
                                isUnlocked = hasCompleted || items.isNotEmpty(),
                                lang = lang
                            )
                            DonationBadgeChip(
                                badgeName = if (lang == Language.ZH) "📚 书香接力勋章" else "Book Relay Medal",
                                isUnlocked = true,
                                lang = lang
                            )
                            DonationBadgeChip(
                                badgeName = if (lang == Language.ZH) "🏆 绿碳领航员" else "Green Pioneer",
                                isUnlocked = authUser.recyclePoints >= 100,
                                lang = lang
                            )
                        }
                    }
                }
            }

            // Quick Persistent Settings (Sophisticated Dark Theme & Language) (Requirement #3)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (themeManager.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (lang == Language.ZH) "全局 'Sophisticated Dark' 主题" else "Global Sophisticated Dark Theme",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (lang == Language.ZH) "开启后设置自动持久化同步至 Firestore" else "Preference saved in local preferences & Firestore",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = themeManager.isDarkMode,
                                onCheckedChange = { themeManager.toggleDarkMode() },
                                modifier = Modifier.testTag("dark_mode_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == Language.ZH) "中英文界面双语切换" else "Language (ZH/EN)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { langManager.toggleLanguage() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (lang == Language.ZH) "中文" else "English",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Profile Section Tabs (History of Posted Items, Ongoing Requests, Completed / Donations) (Requirement #2)
            item {
                Text(
                    text = if (lang == Language.ZH) "我的回收与活动记录" else "My Activities & Recycle History",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                val tabs = listOf(
                    if (lang == Language.ZH) "⏳ 历程时间轴 (Timeline)" else "⏳ Timeline",
                    if (lang == Language.ZH) "进行中订单 (${ongoingItems.size})" else "Ongoing (${ongoingItems.size})",
                    if (lang == Language.ZH) "我的全部发布 (${items.size})" else "Posted Items (${items.size})",
                    if (lang == Language.ZH) "已完成与捐赠 (${completedItems.size})" else "Completed (${completedItems.size})"
                )

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 0.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            // Tab Content Rendering
            when (selectedTabIndex) {
                0 -> {
                    // Recycling History Timeline View
                    item {
                        RecyclingHistoryTimeline(
                            items = items,
                            lang = lang,
                            onItemClick = onItemClick
                        )
                    }
                }
                1 -> {
                    // Ongoing Recycle Requests
                    if (ongoingItems.isEmpty()) {
                        item { EmptyStateCard(if (lang == Language.ZH) "暂无进行中的回收单" else "No ongoing requests") }
                    } else {
                        items(ongoingItems) { item ->
                            ProfileItemCard(item = item, lang = lang, onClick = { onItemClick(item) })
                        }
                    }
                }
                2 -> {
                    // Posted Items History
                    if (items.isEmpty()) {
                        item { EmptyStateCard(if (lang == Language.ZH) "暂无发布历史" else "No posted items") }
                    } else {
                        items(items) { item ->
                            ProfileItemCard(item = item, lang = lang, onClick = { onItemClick(item) })
                        }
                    }
                }
                3 -> {
                    // Completed / Donations
                    if (completedItems.isEmpty()) {
                        item { EmptyStateCard(if (lang == Language.ZH) "暂无完结记录" else "No completed records") }
                    } else {
                        items(completedItems) { item ->
                            ProfileItemCard(
                                item = item,
                                lang = lang,
                                onClick = { onItemClick(item) },
                                onReceiptClick = { targetItem ->
                                    activeReceipt = DonationReceiptExporter.createReceiptFromItem(targetItem)
                                }
                            )
                        }
                    }
                }
            }

            // 6 Quick Feature Menu Items
            item {
                Text(
                    text = if (lang == Language.ZH) "快捷功能菜单" else "Quick Menus",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    menuItems.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { item ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onNavigate(item.id) }
                                        .testTag("profile_menu_${item.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(item.color.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (lang == Language.ZH) item.titleZh else item.titleEn,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            if (rowItems.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    // Floating Action Button to report bugs / submit suggestions
    ExtendedFloatingActionButton(
        onClick = { showBugReportDialog = true },
        icon = { Icon(Icons.Default.BugReport, contentDescription = "Report Bug") },
        text = { Text(if (lang == Language.ZH) "意见与Bug反馈" else "Report Bug", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
            .testTag("profile_bug_report_fab")
    )

    if (showBugReportDialog) {
        BugReportDialog(
            lang = lang,
            userEmail = authUser?.email ?: "",
            onDismiss = { showBugReportDialog = false }
        )
    }
}
}

@Composable
private fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
private fun GreenCreditDetailItem(title: String, pts: String, color: Color) {
    Column {
        Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
        Text(text = pts, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileItemCard(
    item: RecycleItem,
    lang: Language,
    onClick: () -> Unit,
    onReceiptClick: ((RecycleItem) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("profile_item_card_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusChip(status = item.status, lang = lang)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (item.isDonation) "❤️ 指定捐赠" else "期望价: ¥${item.expectedPrice.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "成色: ${item.condition}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (item.assignedRecipientName != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "指派回收员: ${item.assignedRecipientName} (${item.scheduledTime ?: "等候对接"})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if ((item.isDonation || item.status == "COMPLETED") && onReceiptClick != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onReceiptClick(item) },
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("view_donation_receipt_btn_${item.id}"),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Receipt",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lang == Language.ZH) "📄 查看/导出捐赠收据" else "📄 Download Receipt",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonationBadgeChip(
    badgeName: String,
    isUnlocked: Boolean,
    lang: Language
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = badgeName,
                fontSize = 10.sp,
                fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal,
                color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Text(
                text = if (isUnlocked) (if (lang == Language.ZH) "已解锁" else "Unlocked") else (if (lang == Language.ZH) "未解锁" else "Locked"),
                fontSize = 8.sp,
                color = if (isUnlocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
            )
        }
    }
}
