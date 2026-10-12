package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Language
import com.example.data.model.OrderAgreement

data class TimelineNode(
    val stepTitle: String,
    val timestamp: String,
    val statusBadge: String,
    val technicianName: String,
    val technicianNote: String,
    val isCompleted: Boolean
)

@Composable
fun ActivityTimelineDialog(
    order: OrderAgreement,
    lang: Language,
    onDismiss: () -> Unit,
    onViewAgreementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val timelineNodes = listOf(
        TimelineNode(
            stepTitle = if (lang == Language.ZH) "📝 1. 预约单在线提交与立项" else "📝 1. Request Submitted",
            timestamp = "2026-08-04 09:30",
            statusBadge = if (lang == Language.ZH) "已完成" else "Completed",
            technicianName = if (lang == Language.ZH) "智能估价系统" else "AI Auto System",
            technicianNote = if (lang == Language.ZH) "用户上传品类照片并提交预约。系统根据成色与市场牌价核算基础预估价 ¥${order.finalPrice}。" else "Request recorded with automated initial estimate of ¥${order.finalPrice}.",
            isCompleted = true
        ),
        TimelineNode(
            stepTitle = if (lang == Language.ZH) "🔍 2. 站长派单与估价核验" else "🔍 2. Admin Review & Dispatch",
            timestamp = "2026-08-04 10:05",
            statusBadge = if (lang == Language.ZH) "已派单" else "Dispatched",
            technicianName = "张国强 (朝阳站长)",
            technicianNote = if (lang == Language.ZH) "站点已派单给资深揽收员【${order.collectorName}】。已备齐防撕封条与便携式高精度电子秤。" else "Assigned to Senior Technician ${order.collectorName}.",
            isCompleted = true
        ),
        TimelineNode(
            stepTitle = if (lang == Language.ZH) "🚚 3. 专员上门精准称重与质检" else "🚚 3. On-Site Inspection & Weight",
            timestamp = "2026-08-04 11:15",
            statusBadge = if (lang == Language.ZH) "已接单" else "In Progress",
            technicianName = "${order.collectorName} (工号 #SF-8890)",
            technicianNote = if (lang == Language.ZH) "【技师现场备注】: 现场核验物品保存完好，成色符合A级旧件标准。称实重 8.5kg，符合加价提能奖励机制！" else "Technician note: Item inspected in great condition. Weight: 8.5kg.",
            isCompleted = order.status in listOf("已完成", "Completed", "处理中", "In Progress")
        ),
        TimelineNode(
            stepTitle = if (lang == Language.ZH) "♻️ 4. 环保拆解与防篡改存证" else "♻️ 4. Eco Dismantling & Certificate",
            timestamp = "2026-08-04 11:45",
            statusBadge = if (lang == Language.ZH) "处理中" else "Processing",
            technicianName = if (lang == Language.ZH) "望京分拣仓质检员" else "Sorting Hub Inspector",
            technicianNote = if (lang == Language.ZH) "旧件已生成区块链可溯源电子标签 (Hash: 0x98f...a12)。无害化拆解率 100%。" else "Generated blockchain traceability tag for eco sorting.",
            isCompleted = order.status in listOf("已完成", "Completed")
        ),
        TimelineNode(
            stepTitle = if (lang == Language.ZH) "🌟 5. 绿碳积分发放与款项秒到" else "🌟 5. Payout & Eco Credits Rewarded",
            timestamp = "2026-08-04 12:00",
            statusBadge = if (lang == Language.ZH) "已完结" else "Settled",
            technicianName = if (lang == Language.ZH) "绿碳钱包结算中心" else "Settlement Hub",
            technicianNote = if (lang == Language.ZH) "回收估价金额 ¥${order.finalPrice} 已打入账户，同步发放 +180 绿碳积分！" else "Payout of ¥${order.finalPrice} sent to wallet with +180 credits.",
            isCompleted = order.status in listOf("已完成", "Completed")
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .testTag("activity_timeline_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Timeline",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (lang == Language.ZH) "回收流程全轨迹与技师备注" else "Recycling Request Activity Timeline",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "单号: ${order.orderId}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Summary Order Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = order.itemTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "上门专员: ${order.collectorName} (${order.collectorPhone})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "¥${order.finalPrice}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Timeline Step Items
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(timelineNodes) { index, node ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Timeline Icon & Vertical Line Connector
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(32.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (node.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (node.isCompleted) Icons.Default.CheckCircle else Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                if (index < timelineNodes.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(80.dp)
                                            .background(if (node.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Step Detail Card
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (node.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (node.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = node.stepTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Text(
                                        text = node.timestamp,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "👤 操作主体: ${node.technicianName}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = node.technicianNote,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Footer Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, if (lang == Language.ZH) "正在拨打专员电话 ${order.collectorPhone}..." else "Calling ${order.collectorPhone}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (lang == Language.ZH) "电话联系" else "Call", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onViewAgreementClick,
                        modifier = Modifier.weight(1.3f).testTag("view_timeline_electronic_agreement_btn"),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "Agreement", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (lang == Language.ZH) "查看电子存证协议" else "View Agreement", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
