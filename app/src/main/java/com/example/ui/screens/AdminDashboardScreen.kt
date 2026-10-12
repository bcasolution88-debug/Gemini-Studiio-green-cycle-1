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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.data.model.RecycleItem
import com.example.ui.components.BilingualHeader

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.OutlinedButton
import com.example.data.repository.DonationScheduleRepository

@Composable
fun AdminDashboardScreen(
    pendingItems: List<RecycleItem>,
    lang: Language,
    onAssignRecipient: (
        itemId: String,
        recipientName: String,
        recipientPhone: String,
        recipientWechat: String,
        stationName: String,
        scheduledTime: String,
        scheduledLocation: String,
        estimatedPrice: Double,
        evaluation: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var selectedItemForAssign by remember { mutableStateOf<RecycleItem?>(null) }
    var recipientName by remember { mutableStateOf("王师傅 (阳光回收站专员)") }
    var recipientPhone by remember { mutableStateOf("13911112222") }
    var recipientWechat by remember { mutableStateOf("recycle_wang_88") }
    var stationName by remember { mutableStateOf("朝阳绿洲示范废品环保总站") }
    var scheduledTime by remember { mutableStateOf("2026-08-01 14:00 - 16:00") }
    var estimatedPriceText by remember { mutableStateOf("260") }
    var evaluationMetadata by remember { mutableStateOf("【管理员评估】物品完好，分类符合环保回收标准，具备再利用价值。") }
    var isDispatched by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BilingualHeader(
            title = if (lang == Language.ZH) "管理员中台强管控中心" else "Admin Control Dashboard"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metrics Overview Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard("${pendingItems.size}件", if (lang == Language.ZH) "待审核评估" else "Pending Audit", Color(0xFFED6C02), Modifier.weight(1f))
                AdminMetricCard("98.5%", if (lang == Language.ZH) "接单完成率" else "Match Rate", Color(0xFF2E7D32), Modifier.weight(1f))
                AdminMetricCard("28个", if (lang == Language.ZH) "合作回收站点" else "Stations", Color(0xFF0288D1), Modifier.weight(1f))
            }

            // Dispatch Tool Header
            Text(
                text = if (lang == Language.ZH) "待评估与指派回收员工作流" else "Matching & Evaluation Workflow",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            pendingItems.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "描述: ${item.description}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "上门地址: ${item.location} (${item.contactPhone})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!item.evaluation.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🏷️ Firestore评估元数据: ${item.evaluation}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("admin_evaluation_badge")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                selectedItemForAssign = item
                                isDispatched = false
                            },
                            modifier = Modifier.fillMaxWidth().testTag("assign_item_${item.id}"),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AssignmentInd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (lang == Language.ZH) "安排指派接收人与估价" else "Assign & Evaluate")
                        }
                    }
                }
            }

            // Category Management Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == Language.ZH) "7大主分类与二级分类无线拓展管理" else "Category Tree Management",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (lang == Language.ZH) "后台支持实时增删改、排序、拖拽调整与热门分类图标上传" else "Supports real-time category updates & sorting",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Calendar Scheduled Pickups Request Sync Section
            val scheduledPickups = DonationScheduleRepository.getSchedules()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_calendar_pickups_sync_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Sync",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (lang == Language.ZH) "📅 爱心捐赠日历预约中台调度看板" else "📅 Calendar Donation Pickup Sync",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (lang == Language.ZH) "实时接收用户端捐赠日历预约 · 快速站长确认与派单" else "Live user donation appointments sync & courier dispatch",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${scheduledPickups.size} 单",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (scheduledPickups.isEmpty()) {
                        Text(
                            text = if (lang == Language.ZH) "暂无用户提交的日历预约单" else "No calendar appointments yet",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            scheduledPickups.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "🗓️ ${item.scheduledDateDisplay(lang)} ${item.scheduledTimeSlot(lang)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (item.status == "CONFIRMED") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (item.status == "CONFIRMED") (if (lang == Language.ZH) "✅ 已接单" else "Confirmed") else (if (lang == Language.ZH) "⏳ 待中台确认" else "Pending"),
                                                    fontSize = 10.sp,
                                                    color = if (item.status == "CONFIRMED") Color(0xFF2E7D32) else Color(0xFFE65100),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "👤 捐赠人: ${item.donorName} (${item.donorPhone})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "📍 地址: ${item.pickupAddress}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "📦 捐赠描述: ${item.itemDescription} [${item.itemQuantity}]",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (!item.assignedCourier.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "🚚 派发快递: ${item.assignedCourier}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (item.status != "CONFIRMED") {
                                                Button(
                                                    onClick = {
                                                        DonationScheduleRepository.updateStatus(
                                                            scheduleId = item.id,
                                                            newStatus = "CONFIRMED",
                                                            courierName = "顺丰公益绿色小哥 (13800138000)"
                                                        )
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(34.dp)
                                                        .testTag("admin_confirm_pickup_${item.id}"),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (lang == Language.ZH) "✅ 确认接单" else "Confirm",
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            } else {
                                                OutlinedButton(
                                                    onClick = {
                                                        DonationScheduleRepository.updateStatus(
                                                            scheduleId = item.id,
                                                            newStatus = "COMPLETED"
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(34.dp),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        text = if (lang == Language.ZH) "🎉 标记揽收完成" else "Mark Complete",
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
                }
            }
        }

        // Assign Modal
        if (selectedItemForAssign != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (lang == Language.ZH) "指派接收人与价格评估" else "Assign Collector",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (!isDispatched) {
                            OutlinedTextField(
                                value = recipientName,
                                onValueChange = { recipientName = it },
                                label = { Text("接收人/回收员姓名") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = recipientPhone,
                                onValueChange = { recipientPhone = it },
                                label = { Text("回收员联系电话") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = stationName,
                                onValueChange = { stationName = it },
                                label = { Text("合作回收站点名称") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = scheduledTime,
                                onValueChange = { scheduledTime = it },
                                label = { Text("预约上门时间段") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = estimatedPriceText,
                                onValueChange = { estimatedPriceText = it },
                                label = { Text("在线评估参考价格 (¥)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = evaluationMetadata,
                                onValueChange = { evaluationMetadata = it },
                                label = { Text("管理员评估元数据 (录入 Firestore)") },
                                minLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_evaluation_input")
                            )

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val price = estimatedPriceText.toDoubleOrNull() ?: 200.0
                                    onAssignRecipient(
                                        selectedItemForAssign!!.id,
                                        recipientName,
                                        recipientPhone,
                                        recipientWechat,
                                        stationName,
                                        scheduledTime,
                                        selectedItemForAssign!!.location,
                                        price,
                                        evaluationMetadata
                                    )
                                    isDispatched = true
                                },
                                modifier = Modifier.fillMaxWidth().testTag("confirm_dispatch_btn"),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lang == Language.ZH) "确认指派、评估并通知" else "Dispatch & Save Evaluation")
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (lang == Language.ZH) "指派成功！系统已通过App与短信/微信自动推送给双方" else "Dispatched & Notice Sent!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Button(
                                onClick = { selectedItemForAssign = null },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(if (lang == Language.ZH) "完成" else "Done")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, fontSize = 10.sp, color = color)
        }
    }
}
