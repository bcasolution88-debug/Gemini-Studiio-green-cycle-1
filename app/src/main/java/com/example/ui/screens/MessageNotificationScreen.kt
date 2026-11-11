package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material.icons.filled.Store
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
import com.example.data.model.Language
import com.example.ui.components.BilingualHeader
import com.example.ui.components.StationAdminChatDialog

data class NotificationItem(
    val id: String,
    val title: String,
    val content: String,
    val time: String,
    val type: String
)

data class SiteAdminChatThread(
    val id: String,
    val stationName: String,
    val adminName: String,
    val phone: String,
    val lastMsg: String,
    val time: String,
    val unreadCount: Int
)

@Composable
fun MessageNotificationScreen(
    lang: Language,
    modifier: Modifier = Modifier
) {
    var activeChatStation by remember { mutableStateOf<SiteAdminChatThread?>(null) }

    val siteAdminThreads = listOf(
        SiteAdminChatThread(
            id = "t1",
            stationName = if (lang == Language.ZH) "朝阳路绿源综合回收站" else "Chaoyang Green Recycling Station",
            adminName = if (lang == Language.ZH) "张站长 (值班组长)" else "Station Admin Zhang",
            phone = "13800138001",
            lastMsg = if (lang == Language.ZH) "已收到您的上门预约申请！已安排师傅今日15:00接单。" else "Pickup request confirmed for today 15:00!",
            time = "10:17",
            unreadCount = 1
        ),
        SiteAdminChatThread(
            id = "t2",
            stationName = if (lang == Language.ZH) "爱心衣物与二手书公益捐赠站" else "Charity Apparel & Books Donation Point",
            adminName = if (lang == Language.ZH) "李站长 (捐赠协调员)" else "Coordinator Li",
            phone = "13900139002",
            lastMsg = if (lang == Language.ZH) "旧衣服已通过顺丰绿色专线寄往爱心小学，感谢您的慷慨！" else "Donated apparel dispatched to primary school. Thank you!",
            time = "昨天",
            unreadCount = 0
        )
    )

    val sampleNotifications = listOf(
        NotificationItem("n1", "【管理员中台指派通知】", "您的待回收物品《全实木双人床》已指派回收员张师傅，预约上门时间为2026-08-01 14:00。", "10分钟前", "DISPATCH"),
        NotificationItem("n2", "【电子协议生成】", "订单ORD_20260729_881的电子回收协议书已正式生成，请前往查看与数字签名。", "1小时前", "AGREEMENT"),
        NotificationItem("n3", "【社区互动提醒】", "用户“环保达人小绿”点赞了您发布的待回收帖《搬家清理落地灯》。", "3小时前", "FORUM")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BilingualHeader(
            title = if (lang == Language.ZH) "消息中心与站长沟通通道" else "Notifications & Admin Chat"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Site Admin Messaging Threads Section
            item {
                Text(
                    text = if (lang == Language.ZH) "🏢 站点值班站长实时沟通 (预约 & 捐赠)" else "🏢 Recycling Site Admin Direct Chat",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            items(siteAdminThreads) { thread ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeChatStation = thread }
                        .testTag("admin_chat_thread_${thread.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = "Admin Chat",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = thread.stationName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = thread.time,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "👤 ${thread.adminName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = thread.lastMsg,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        if (thread.unreadCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${thread.unreadCount}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Standard System Notifications Header
            item {
                Text(
                    text = if (lang == Language.ZH) "🔔 系统通知与物流消息" else "🔔 System Notifications & Orders",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(sampleNotifications) { notif ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (notif.type) {
                                    "DISPATCH" -> Icons.Default.Person
                                    "AGREEMENT" -> Icons.Default.Assignment
                                    else -> Icons.Default.Chat
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = notif.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = notif.time,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.content,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Active Chat Dialog
        activeChatStation?.let { thread ->
            StationAdminChatDialog(
                stationName = thread.stationName,
                stationPhone = thread.phone,
                lang = lang,
                onDismiss = { activeChatStation = null }
            )
        }
    }
}
