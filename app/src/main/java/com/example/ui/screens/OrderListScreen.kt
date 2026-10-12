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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.History
import com.example.data.model.Language
import com.example.data.model.OrderAgreement
import com.example.ui.components.ActivityTimelineDialog
import com.example.ui.components.BilingualHeader
import com.example.ui.components.StatusChip
import androidx.compose.material.icons.filled.ReceiptLong
import com.example.data.model.DonationReceipt
import com.example.ui.components.DonationReceiptDialog
import com.example.util.DonationReceiptExporter

@Composable
fun OrderListScreen(
    orders: List<OrderAgreement>,
    lang: Language,
    onOrderClick: (OrderAgreement) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTimelineOrder by remember { mutableStateOf<OrderAgreement?>(null) }
    var activeReceiptOrder by remember { mutableStateOf<OrderAgreement?>(null) }

    if (activeTimelineOrder != null) {
        val targetOrder = activeTimelineOrder!!
        ActivityTimelineDialog(
            order = targetOrder,
            lang = lang,
            onDismiss = { activeTimelineOrder = null },
            onViewAgreementClick = {
                activeTimelineOrder = null
                onOrderClick(targetOrder)
            }
        )
    }

    if (activeReceiptOrder != null) {
        DonationReceiptDialog(
            receipt = DonationReceiptExporter.createReceiptFromOrder(activeReceiptOrder!!),
            lang = lang,
            onDismiss = { activeReceiptOrder = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BilingualHeader(
            title = if (lang == Language.ZH) "回收订单与电子协议管理" else "Orders & Agreements"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                OrderCard(
                    order = order,
                    lang = lang,
                    onClick = { onOrderClick(order) },
                    onTimelineClick = { activeTimelineOrder = order },
                    onReceiptClick = { activeReceiptOrder = order }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun OrderCard(
    order: OrderAgreement,
    lang: Language,
    onClick: () -> Unit,
    onTimelineClick: (() -> Unit)? = null,
    onReceiptClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("order_card_${order.orderId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "订单号: ${order.orderId}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusChip(status = order.status, lang = lang)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = order.itemTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "回收员: ${order.collectorName} (${order.collectorPhone})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "预约时间: ${order.scheduledTime}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "估价金额: ¥${order.finalPrice}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (onReceiptClick != null) {
                        OutlinedButton(
                            onClick = onReceiptClick,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("view_order_receipt_btn_${order.orderId}")
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (lang == Language.ZH) "收据" else "Receipt", fontSize = 11.sp)
                        }
                    }

                    if (onTimelineClick != null) {
                        OutlinedButton(
                            onClick = onTimelineClick,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("view_order_timeline_btn_${order.orderId}")
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (lang == Language.ZH) "轨迹" else "Timeline", fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("view_order_agreement_btn")
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (lang == Language.ZH) "协议" else "Agreement", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
