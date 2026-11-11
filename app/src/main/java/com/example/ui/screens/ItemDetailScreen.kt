package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.material.icons.filled.Share
import com.example.util.ShareUtils
import com.example.data.model.Language
import com.example.data.model.RecycleItem
import com.example.ui.components.BilingualHeader
import com.example.ui.components.DonationBadge
import com.example.ui.components.MediaGalleryView
import com.example.ui.components.RecycleProgressStepper
import com.example.ui.components.StatusChip
import com.example.ui.components.VideoPlayerSimulator

@Composable
fun ItemDetailScreen(
    item: RecycleItem,
    lang: Language,
    onViewAgreementClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BilingualHeader(
                title = if (lang == Language.ZH) "待回收物品详情" else "Item Details"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Horizontal Media Gallery View (Images + Video)
                MediaGalleryView(
                    imageUrls = item.getImages(),
                    videoUrl = item.videoUrl,
                    postTitle = item.title,
                    lang = lang
                )

                // Recycle Progress Stepper Component (Requirement 3)
                RecycleProgressStepper(
                    currentStatus = item.status,
                    lang = lang
                )

                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusChip(status = item.status, lang = lang)
                        }

                        if (item.isDonation) {
                            Spacer(modifier = Modifier.height(8.dp))
                            DonationBadge(donorTarget = item.donorTarget, lang = lang)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (item.isDonation) {
                                    if (lang == Language.ZH) "❤️ 指定捐赠: ${item.donorTarget ?: "公益机构"}" else "❤️ Donation: ${item.donorTarget}"
                                } else if (item.estimatedPrice != null && item.estimatedPrice > 0) {
                                    if (lang == Language.ZH) "评估价: ¥${item.estimatedPrice}" else "Eval Price: ¥${item.estimatedPrice}"
                                } else if (item.expectedPrice > 0) {
                                    "期望价: ¥${item.expectedPrice.toInt()}"
                                } else {
                                    if (lang == Language.ZH) "待管理员评估价格" else "Awaiting Admin Price Eval"
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "新旧: ${item.condition}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Short Video Player if available (Requirement #3)
                if (item.videoUrl != null) {
                    Text(
                        text = if (lang == Language.ZH) "实拍短视频展示" else "Short Video Preview",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    VideoPlayerSimulator(
                        videoTitle = item.title,
                        lang = lang
                    )
                }

                // Description
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (lang == Language.ZH) "物品描述" else "Description",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.location,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Share item button
                            OutlinedButton(
                                onClick = {
                                    ShareUtils.shareRecycleItem(
                                        context = context,
                                        title = item.title,
                                        condition = item.condition,
                                        priceOrDonation = if (item.isDonation) "爱心公益捐赠" else "评估期望价: ¥${item.expectedPrice.toInt()}",
                                        location = item.location,
                                        lang = lang
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("item_share_btn")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lang == Language.ZH) "分享此物品" else "Share Item",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Recycling Progress Timeline (Requirement #5)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (lang == Language.ZH) "对接安排进度条" else "Progress Tracking",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TrackingStepRow("1. 提交待回收信息", "信息审核中", true)
                        TrackingStepRow("2. 管理员评估与派单", if (item.assignedRecipientName != null) "已指派: ${item.assignedRecipientName}" else "等候指派中", item.assignedRecipientName != null)
                        TrackingStepRow("3. 双方确认与预约上门", if (item.scheduledTime != null) item.scheduledTime!! else "等候预约", item.scheduledTime != null)
                        TrackingStepRow("4. 电子协议签署与交接完成", if (item.status == "COMPLETED") "交接完成" else "等候上门完结", item.status == "COMPLETED")
                    }
                }

                // Assigned Collector / Recipient Card (Requirement #2)
                if (item.assignedRecipientName != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Recipient",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == Language.ZH) "管理员指派接收人信息" else "Assigned Recipient Info",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "姓名/称呼: ${item.assignedRecipientName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "所属回收站点: ${item.assignedStationName ?: "平台认证官方回收站"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "预约时间段: ${item.scheduledTime ?: "待确认"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val phone = item.contactPhone.ifBlank { "8613800138000" }
                                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        try {
                                            context.startActivity(callIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "拨打电话: $phone", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("call_recipient_btn"),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (lang == Language.ZH) "拨打电话" else "Call Collector",
                                        fontSize = 12.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (item.assignedRecipientName != null) {
                                            onViewAgreementClick("ORD_${item.id}")
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("view_agreement_btn"),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(Icons.Default.Assignment, contentDescription = "Agreement", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (lang == Language.ZH) "电子协议" else "Agreement",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to contact station via WhatsApp / Messaging Intent
        ExtendedFloatingActionButton(
            onClick = {
                val stationName = item.assignedStationName ?: "朝阳区回收站"
                val phone = item.contactPhone.ifBlank { "8613800138000" }
                val msg = if (lang == Language.ZH) {
                    "您好，我是关于物品[${item.title}]的咨询人，请问该回收单对接安排进展如何？"
                } else {
                    "Hello, I am inquiring about recycling item [${item.title}]. Please advise on pickup schedule."
                }

                val whatsappUri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(msg)}")
                val whatsappIntent = Intent(Intent.ACTION_VIEW, whatsappUri)

                try {
                    context.startActivity(whatsappIntent)
                    Toast.makeText(context, if (lang == Language.ZH) "正在启动 WhatsApp 联系 $stationName" else "Opening WhatsApp...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                        putExtra("sms_body", msg)
                    }
                    try {
                        context.startActivity(smsIntent)
                        Toast.makeText(context, if (lang == Language.ZH) "正在启动短信发送至 $stationName" else "Opening SMS...", Toast.LENGTH_SHORT).show()
                    } catch (ex: Exception) {
                        Toast.makeText(context, "联系电话: $phone", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
                .testTag("whatsapp_station_fab"),
            containerColor = Color(0xFF25D366),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "WhatsApp Chat",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (lang == Language.ZH) "💬 WhatsApp 联系站点" else "💬 WhatsApp Station",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TrackingStepRow(title: String, subtitle: String, isFinished: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isFinished) Icons.Default.CheckCircle else Icons.Default.Schedule,
            contentDescription = null,
            tint = if (isFinished) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFinished) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
