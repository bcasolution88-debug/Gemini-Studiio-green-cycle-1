package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.Language
import com.example.data.repository.DonationPickupSchedule
import com.example.data.repository.DonationScheduleRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CalendarDateOption(
    val dateIso: String,           // e.g., "2026-08-11"
    val displayZh: String,         // e.g., "08月11日 周二"
    val displayEn: String,         // e.g., "Aug 11 Tue"
    val dayOfWeekZh: String,       // e.g., "周二"
    val dayOfWeekEn: String,       // e.g., "Tue"
    val dateNum: String,           // e.g., "11"
    val badgeStatusZh: String,     // e.g., "名额充足", "火爆"
    val badgeStatusEn: String      // e.g., "Available", "Popular"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DonationCalendarDialog(
    initialDriveTitle: String = "爱心捐赠上门揽收",
    lang: Language,
    onDismiss: () -> Unit,
    onScheduledSuccess: (DonationPickupSchedule) -> Unit
) {
    // Generate 10 upcoming calendar date options
    val calendarOptions = remember {
        val list = mutableListOf<CalendarDateOption>()
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEEE", Locale.CHINESE)
        val dayFormatEn = SimpleDateFormat("EEE", Locale.ENGLISH)
        val numFormat = SimpleDateFormat("dd", Locale.getDefault())
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthDayZh = SimpleDateFormat("MM月dd日", Locale.CHINESE)
        val monthDayEn = SimpleDateFormat("MMM dd", Locale.ENGLISH)

        for (i in 0..9) {
            val dateIso = isoFormat.format(cal.time)
            val dayZh = when (i) {
                0 -> "今日"
                1 -> "明日"
                else -> dayFormat.format(cal.time).replace("星期", "周")
            }
            val dayEn = when (i) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> dayFormatEn.format(cal.time)
            }
            val num = numFormat.format(cal.time)
            val dateDispZh = "${monthDayZh.format(cal.time)} $dayZh"
            val dateDispEn = "${monthDayEn.format(cal.time)} $dayEn"
            val badgeZh = if (i % 3 == 0) "🔥 热门" else "✅ 充裕"
            val badgeEn = if (i % 3 == 0) "Popular" else "Available"

            list.add(
                CalendarDateOption(
                    dateIso = dateIso,
                    displayZh = dateDispZh,
                    displayEn = dateDispEn,
                    dayOfWeekZh = dayZh,
                    dayOfWeekEn = dayEn,
                    dateNum = num,
                    badgeStatusZh = badgeZh,
                    badgeStatusEn = badgeEn
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val timeSlotsZh = listOf(
        "⚡ 09:00 - 11:30 (晨间绿色专线)",
        "☀️ 13:30 - 16:00 (下午快捷揽收)",
        "🌙 16:30 - 19:00 (傍晚顺路服务)",
        "🌟 19:30 - 21:00 (夜间便民预约)"
    )

    val timeSlotsEn = listOf(
        "⚡ 09:00 - 11:30 (Morning Express)",
        "☀️ 13:30 - 16:00 (Afternoon Express)",
        "🌙 16:30 - 19:00 (Evening Pickup)",
        "🌟 19:30 - 21:00 (Night Slot)"
    )

    var selectedDateOption by remember { mutableStateOf(calendarOptions.first()) }
    var selectedTimeSlot by remember { mutableStateOf(if (lang == Language.ZH) timeSlotsZh.first() else timeSlotsEn.first()) }

    var donorName by remember { mutableStateOf("张女士") }
    var donorPhone by remember { mutableStateOf("13800138001") }
    var pickupAddress by remember { mutableStateOf("北京市朝阳区绿洲家园 8号楼2单元601") }
    var itemCategory by remember { mutableStateOf("闲置旧衣/图书") }
    var itemDescription by remember { mutableStateOf("整理出旧童装与绘本，打包完好约8kg") }
    var itemQuantity by remember { mutableStateOf("1 箱 (约8kg)") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("donation_calendar_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Dialog Title Header
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
                                contentDescription = "Calendar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (lang == Language.ZH) "📅 爱心捐赠上门预约日历" else "📅 Calendar Donation Pickup",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (lang == Language.ZH) "选择顺丰/公益专车上门时段 · 实时同步中台" else "Pick preferred time slot & sync with admin",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Calendar Date Selection Carousel
                Text(
                    text = if (lang == Language.ZH) "1. 选择上门日期 (近10天)" else "1. Select Pickup Date",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(calendarOptions) { option ->
                        val isSelected = option.dateIso == selectedDateOption.dateIso
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedDateOption = option }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("calendar_date_chip_${option.dateIso}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (lang == Language.ZH) option.dayOfWeekZh else option.dayOfWeekEn,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = option.dateNum,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (lang == Language.ZH) option.badgeStatusZh else option.badgeStatusEn,
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Preferred Time Slot Selector
                Text(
                    text = if (lang == Language.ZH) "2. 选择时间段" else "2. Select Preferred Time Slot",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                val currentSlots = if (lang == Language.ZH) timeSlotsZh else timeSlotsEn
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    currentSlots.forEach { slot ->
                        val isSelected = slot == selectedTimeSlot
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedTimeSlot = slot }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = slot,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Contact & Address Form
                OutlinedTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it },
                    label = { Text(if (lang == Language.ZH) "📍 详细上门揽收地址" else "📍 Detailed Pickup Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = donorName,
                        onValueChange = { donorName = it },
                        label = { Text(if (lang == Language.ZH) "联系人" else "Contact") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = donorPhone,
                        onValueChange = { donorPhone = it },
                        label = { Text(if (lang == Language.ZH) "电话号码" else "Phone") },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = itemDescription,
                    onValueChange = { itemDescription = it },
                    label = { Text(if (lang == Language.ZH) "📦 捐赠物品与估重描述" else "📦 Donated Items Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Submit Button
                Button(
                    onClick = {
                        val newSchedule = DonationPickupSchedule(
                            id = "SCH_${System.currentTimeMillis().toString().takeLast(5)}",
                            driveTitleZh = initialDriveTitle,
                            driveTitleEn = initialDriveTitle,
                            donorName = donorName,
                            donorPhone = donorPhone,
                            pickupAddress = pickupAddress,
                            itemCategory = itemCategory,
                            itemDescription = itemDescription,
                            itemQuantity = itemQuantity,
                            scheduledDate = selectedDateOption.dateIso,
                            scheduledDateDisplayZh = selectedDateOption.displayZh,
                            scheduledDateDisplayEn = selectedDateOption.displayEn,
                            scheduledTimeSlotZh = selectedTimeSlot,
                            scheduledTimeSlotEn = selectedTimeSlot,
                            status = "PENDING",
                            assignedCourier = null
                        )

                        DonationScheduleRepository.addSchedule(newSchedule)
                        onScheduledSuccess(newSchedule)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("confirm_donation_schedule_btn"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = "Confirm",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lang == Language.ZH) "🚀 确认上门预约并同步管理员中台" else "🚀 Confirm Appointment & Sync Admin",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
