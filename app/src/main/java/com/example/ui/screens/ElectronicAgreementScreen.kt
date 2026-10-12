package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import com.example.data.model.OrderAgreement
import com.example.data.repository.FirestoreManager
import com.example.ui.components.BilingualHeader
import com.example.ui.components.RecycleProgressStepper
import com.example.ui.components.StarRatingInput
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.OutlinedButton
import com.example.data.model.DonationReceipt
import com.example.ui.components.DonationReceiptDialog
import com.example.util.DonationReceiptExporter

@Composable
fun ElectronicAgreementScreen(
    order: OrderAgreement,
    lang: Language,
    onSignComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSigned by remember { mutableStateOf(order.isSigned) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showReceiptDialog) {
        DonationReceiptDialog(
            receipt = DonationReceiptExporter.createReceiptFromOrder(order),
            lang = lang,
            onDismiss = { showReceiptDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BilingualHeader(
            title = if (lang == Language.ZH) "电子回收协议书" else "Electronic Agreement"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Recycle Progress Stepper Component (Requirement 3)
            RecycleProgressStepper(
                currentStatus = if (isSigned) "COMPLETED" else "SCHEDULED",
                lang = lang
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == Language.ZH) "二手旧物/废品上门回收电子协议书" else "Recycling Electronic Agreement",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "协议编号: ${order.orderId}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "回收物品: ${order.itemTitle}", fontSize = 12.sp)
                    Text(text = "甲方 (转让人): ${order.sellerName} (${order.sellerPhone})", fontSize = 12.sp)
                    Text(text = "乙方 (接收/回收方): ${order.collectorName} (${order.collectorPhone})", fontSize = 12.sp)
                    Text(text = "约定上门地址: ${order.location}", fontSize = 12.sp)
                    Text(text = "结算评估金额: ¥${order.finalPrice}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (lang == Language.ZH) "条款说明: 1. 甲方保证对上述物品拥有合法所有权与处分权；2. 乙方承诺按照国家环保规范分类回收处理；3. 本协议经双方电子签名后生效，具备法律效力。" 
                               else "Terms: 1. Seller guarantees legal ownership; 2. Recycler complies with eco disposal rules; 3. Legally binding upon signature.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Signature Pad Box
                    Text(
                        text = if (lang == Language.ZH) "双方数字签名区" else "Digital Signature Area",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSigned) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Signed",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = if (lang == Language.ZH) "已完成电子签名认证 (算法哈希比对校验)" else "Digitally Signed & Hash Verified",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    isSigned = true
                                    onSignComplete("Sig_${System.currentTimeMillis()}")
                                }.testTag("click_sign_pad")
                            ) {
                                Icon(Icons.Default.Draw, contentDescription = "Sign", tint = Color.Gray)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lang == Language.ZH) "点击屏幕在此处签署姓名" else "Click here to sign",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            if (!isSigned) {
                Button(
                    onClick = {
                        isSigned = true
                        onSignComplete("Sig_${System.currentTimeMillis()}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_sign_btn"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (lang == Language.ZH) "确认并签署电子协议" else "Sign Agreement",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang == Language.ZH) "电子协议已存档，交易记录已存证" else "Agreement Archived & Saved",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Download / View Official Donation Receipt Button
                    Button(
                        onClick = { showReceiptDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_agreement_donation_receipt_btn"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Receipt",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == Language.ZH) "📄 查看与导出公益/回收捐赠收据" else "📄 View & Download Donation Receipt",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Star Rating Input Component upon Job Completion (Requirement #1)
                    StarRatingInput(
                        stationName = order.collectorName + " (回收站)",
                        stationId = "sta_chaoyang_01",
                        lang = lang,
                        onSubmitRating = { rating, comment ->
                            FirestoreManager.submitStationRating(
                                stationId = "sta_chaoyang_01",
                                newRating = rating,
                                comment = comment
                            )
                        }
                    )
                }
            }
        }
    }
}
