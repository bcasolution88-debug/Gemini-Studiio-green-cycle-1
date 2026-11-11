package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@Composable
fun RecyclingHistoryTimeline(
    items: List<RecycleItem>,
    lang: Language,
    onItemClick: (RecycleItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredItems = when (selectedFilter) {
        "PENDING" -> items.filter { it.status == "PENDING" }
        "EVALUATED" -> items.filter { it.status == "MATCHING" || !it.evaluation.isNullOrBlank() }
        "COMPLETED" -> items.filter { it.status == "COMPLETED" || it.isDonation }
        else -> items
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recycling_history_timeline_view")
    ) {
        // Status Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            val filters = listOf(
                "ALL" to if (lang == Language.ZH) "全部轨迹 (${items.size})" else "All (${items.size})",
                "PENDING" to if (lang == Language.ZH) "待处理 (Pending)" else "Pending",
                "EVALUATED" to if (lang == Language.ZH) "已定级评估 (Evaluated)" else "Evaluated",
                "COMPLETED" to if (lang == Language.ZH) "已完结/捐赠 (Completed)" else "Completed"
            )

            items(filters) { (key, label) ->
                val isSelected = selectedFilter == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { selectedFilter = key }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (filteredItems.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("timeline_empty_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PendingActions,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (lang == Language.ZH) "暂无相对应状态的回收历史记录" else "No recycling history under this status",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                filteredItems.forEachIndexed { index, item ->
                    TimelineItemRow(
                        item = item,
                        isLast = index == filteredItems.size - 1,
                        lang = lang,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineItemRow(
    item: RecycleItem,
    isLast: Boolean,
    lang: Language,
    onClick: () -> Unit
) {
    val nodeColor = when {
        item.status == "COMPLETED" || item.isDonation -> Color(0xFF2E7D32)
        !item.evaluation.isNullOrBlank() || item.status == "MATCHING" || item.status == "SCHEDULED" -> Color(0xFF0288D1)
        else -> Color(0xFFED6C02)
    }

    val nodeIcon = when {
        item.status == "COMPLETED" || item.isDonation -> Icons.Default.CheckCircle
        !item.evaluation.isNullOrBlank() -> Icons.Default.RateReview
        item.status == "SCHEDULED" -> Icons.Default.Schedule
        else -> Icons.Default.HourglassTop
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_node_${item.id}")
    ) {
        // Timeline vertical indicator line & node icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(nodeColor.copy(alpha = 0.15f))
                    .border(1.5.dp, nodeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = nodeIcon,
                    contentDescription = null,
                    tint = nodeColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(110.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        // Timeline item detail card
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() },
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
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusChip(status = item.status, lang = lang)
                }

                Spacer(modifier = Modifier.height(4.dp))

                val majorCat = com.example.data.model.CategoryRepository.majorCategories.find { it.id == item.majorCategoryId }
                val subCat = majorCat?.subCategories?.find { it.id == item.subCategoryId }
                val catText = if (majorCat != null) "${majorCat.name(lang)} > ${subCat?.name(lang) ?: item.subCategoryId}" else "${item.majorCategoryId} > ${item.subCategoryId}"

                Text(
                    text = "分类: $catText",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Timeline Status Flow Progress Bar (Pending -> Evaluated -> Scheduled -> Completed)
                Spacer(modifier = Modifier.height(8.dp))
                TimelineStatusStepper(status = item.status, hasEvaluation = !item.evaluation.isNullOrBlank(), lang = lang)

                // Evaluation metadata if evaluated
                if (!item.evaluation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "🏷️ Firestore评估元数据: ${item.evaluation}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val priceDisplay = item.estimatedPrice ?: item.expectedPrice
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (item.isDonation) "爱心无偿捐赠" else "预估/定价: ¥${priceDisplay.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "+${(priceDisplay * 10).toInt()} 碳积分",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineStatusStepper(
    status: String,
    hasEvaluation: Boolean,
    lang: Language
) {
    val stepIndex = when {
        status == "COMPLETED" -> 3
        status == "SCHEDULED" -> 2
        hasEvaluation || status == "MATCHING" -> 1
        else -> 0
    }

    val steps = listOf(
        if (lang == Language.ZH) "1.待处理" else "Pending",
        if (lang == Language.ZH) "2.已评估" else "Evaluated",
        if (lang == Language.ZH) "3.已接单" else "Scheduled",
        if (lang == Language.ZH) "4.已完成" else "Completed"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, label ->
            val isActive = idx <= stepIndex
            val stepColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = stepColor
            )

            if (idx < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 4.dp)
                        .background(if (idx < stepIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
    }
}
