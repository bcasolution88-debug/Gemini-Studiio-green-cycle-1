package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Language
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class StationChatMessage(
    val id: String,
    val senderName: String,
    val isFromUser: Boolean,
    val text: String,
    val timestamp: String
)

@Composable
fun StationAdminChatDialog(
    stationName: String,
    stationPhone: String?,
    lang: Language,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            StationChatMessage(
                id = "1",
                senderName = if (lang == Language.ZH) "张站长 (回收站负责人)" else "Station Admin",
                isFromUser = false,
                text = if (lang == Language.ZH) "您好！我是【$stationName】的值班站长。请问您有什么回收品类、价格评估或上门预约时间的问题需要咨询？" else "Hello! I am the administrator for $stationName. How can I help you with recycling items, estimates or scheduling?",
                timestamp = "10:14"
            )
        )
    }

    val quickQuestions = if (lang == Language.ZH) listOf(
        "旧家电可以免费上门回收吗？",
        "今天晚上8点还可以预约吗？",
        "废纸箱和塑料瓶怎么计价？",
        "爱心衣物捐赠有重量限制吗？"
    ) else listOf(
        "Free pickup for old electronics?",
        "Available for pickup at 8 PM?",
        "Pricing for cardboard & plastic?",
        "Any weight limit for clothing donation?"
    )

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        val timeNow = "10:16"
        messages.add(
            StationChatMessage(
                id = System.currentTimeMillis().toString(),
                senderName = if (lang == Language.ZH) "我" else "Me",
                isFromUser = true,
                text = userText,
                timestamp = timeNow
            )
        )
        inputText = ""

        // Simulate Station Admin Response
        scope.launch {
            isTyping = true
            delay(1200)
            isTyping = false

            val replyText = when {
                userText.contains("免费上门") || userText.contains("Free pickup") || userText.contains("预约") -> {
                    if (lang == Language.ZH) "是的！满10kg或包含电视/冰箱等大件废旧家电，我们全城提供免费上门打包上楼服务。您随时可以通过在线提交预约单！"
                    else "Yes! Orders over 10kg or containing large appliances receive free home pickup!"
                }
                userText.contains("计价") || userText.contains("价格") || userText.contains("Pricing") -> {
                    if (lang == Language.ZH) "目前废纸箱 ¥1.2/kg，塑料瓶 ¥2.5/kg，废旧有色金属 ¥15/kg。提交照片后我可以帮您实时估价！"
                    else "Cardboard is ¥1.2/kg, plastic bottles ¥2.5/kg, and metals ¥15/kg. Send a picture for instant estimate!"
                }
                userText.contains("衣物") || userText.contains("捐赠") || userText.contains("donation") || userText.contains("clothing") -> {
                    if (lang == Language.ZH) "爱心捐赠无重量限制！1件也可以接单，我们将由合作的顺丰绿色专线统一消毒清洗后寄往乡村小学。"
                    else "No weight limit for donations! Even single items are welcomed and cleaned for charity primary schools."
                }
                else -> {
                    if (lang == Language.ZH) "收到您的咨询！【$stationName】营业时间为 08:00 - 21:00，您可以随时提出问题，或直接点击上方电话图标联系站长。"
                    else "Received! Working hours are 08:00 - 21:00. Feel free to call us directly."
                }
            }

            messages.add(
                StationChatMessage(
                    id = (System.currentTimeMillis() + 1).toString(),
                    senderName = if (lang == Language.ZH) "张站长 (回收站负责人)" else "Station Admin",
                    isFromUser = false,
                    text = replyText,
                    timestamp = "10:17"
                )
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("station_admin_chat_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(14.dp)
                ) {
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = "Station",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stationName,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = if (lang == Language.ZH) "在线 (站长张国强 | 响应时间 < 2分钟)" else "Online (Admin Response < 2 min)",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Chat Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubbleItem(message = msg)
                    }

                    if (isTyping) {
                        item {
                            Row(
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == Language.ZH) "站长正在输入回复中..." else "Admin is typing...",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Quick Actions Bar (Scheduled Pickup & Donation Coordination)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                sendMessage(if (lang == Language.ZH) "📅 【预约上门】请求预约今日 15:00-18:00 上门回收（估重 > 10kg）" else "📅 【Schedule Pickup】Request pickup today 15:00-18:00 (> 10kg)")
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang == Language.ZH) "📅 预约上门" else "📅 Schedule Pickup",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFCE4EC))
                            .clickable {
                                sendMessage(if (lang == Language.ZH) "❤️ 【捐赠咨询】我想咨询旧衣物/闲置书本的公益捐赠流程" else "❤️ 【Donation Inquiry】Inquire about clothing/book charity donation")
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang == Language.ZH) "❤️ 捐赠咨询" else "❤️ Donation Inquiry",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC2185B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8F5E9))
                            .clickable {
                                sendMessage(if (lang == Language.ZH) "📸 【照片估价】发送物品估价照片请站长审核评估" else "📸 【Photo Estimate】Send item photo for admin price estimate")
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang == Language.ZH) "📸 拍照估价" else "📸 Photo Estimate",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                // Quick Questions Chips
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = if (lang == Language.ZH) "💡 快捷问答" else "💡 Quick Questions",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        items(quickQuestions) { q ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                    .clickable { sendMessage(q) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = q,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (lang == Language.ZH) "向站长咨询品类、价格或预约..." else "Ask about items, price or schedule...",
                                fontSize = 11.sp
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("station_chat_input_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    Button(
                        onClick = { sendMessage(inputText) },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("send_station_chat_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(message: StationChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = "${message.senderName} · ${message.timestamp}",
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isFromUser) 16.dp else 2.dp,
                        bottomEnd = if (message.isFromUser) 2.dp else 16.dp
                    )
                )
                .background(
                    if (message.isFromUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 13.sp,
                color = if (message.isFromUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
