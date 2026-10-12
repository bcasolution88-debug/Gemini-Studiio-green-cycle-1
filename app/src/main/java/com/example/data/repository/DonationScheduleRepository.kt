package com.example.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.data.model.Language

data class DonationPickupSchedule(
    val id: String,
    val driveTitleZh: String,
    val driveTitleEn: String,
    val donorName: String,
    val donorPhone: String,
    val pickupAddress: String,
    val itemCategory: String,
    val itemDescription: String,
    val itemQuantity: String,
    val scheduledDate: String, // e.g., "2026-08-11"
    val scheduledDateDisplayZh: String, // e.g., "08月11日 周二"
    val scheduledDateDisplayEn: String, // e.g., "Aug 11 Tue"
    val scheduledTimeSlotZh: String, // e.g., "09:00 - 11:30 (晨间绿色专线)"
    val scheduledTimeSlotEn: String, // e.g., "09:00 - 11:30 (Morning Express)"
    val status: String = "PENDING", // PENDING, CONFIRMED, DISPATCHED, COMPLETED
    val assignedCourier: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun driveTitle(lang: Language): String = if (lang == Language.ZH) driveTitleZh else driveTitleEn
    fun scheduledDateDisplay(lang: Language): String = if (lang == Language.ZH) scheduledDateDisplayZh else scheduledDateDisplayEn
    fun scheduledTimeSlot(lang: Language): String = if (lang == Language.ZH) scheduledTimeSlotZh else scheduledTimeSlotEn
}

object DonationScheduleRepository {
    private val _schedules = mutableStateListOf<DonationPickupSchedule>(
        DonationPickupSchedule(
            id = "SCH_1001",
            driveTitleZh = "山区儿童衣物爱心捐赠",
            driveTitleEn = "Charity Apparel for Children",
            donorName = "张女士",
            donorPhone = "13800138001",
            pickupAddress = "北京市朝阳区绿洲家园 8号楼2单元601",
            itemCategory = "旧衣鞋帽",
            itemDescription = "儿童羽绒服3件、防寒棉鞋2双（全新/95新）",
            itemQuantity = "5 件",
            scheduledDate = "2026-08-11",
            scheduledDateDisplayZh = "08月11日 周二",
            scheduledDateDisplayEn = "Aug 11 Tue",
            scheduledTimeSlotZh = "09:00 - 11:30 (晨间绿色专线)",
            scheduledTimeSlotEn = "09:00 - 11:30 (Morning Express)",
            status = "CONFIRMED",
            assignedCourier = "顺丰小哥 王师傅 (13911112222)"
        ),
        DonationPickupSchedule(
            id = "SCH_1002",
            driveTitleZh = "偏远小学图书与体育用品捐赠",
            driveTitleEn = "Books & Sports Gear Donation",
            donorName = "李先生",
            donorPhone = "13900139002",
            pickupAddress = "北京市海淀区中关村南大街 12号院3号楼",
            itemCategory = "图书玩具",
            itemDescription = "科普童话全套12本、儿童篮球与跳绳",
            itemQuantity = "15 本/套",
            scheduledDate = "2026-08-12",
            scheduledDateDisplayZh = "08月12日 周三",
            scheduledDateDisplayEn = "Aug 12 Wed",
            scheduledTimeSlotZh = "14:00 - 16:30 (下午快捷接单)",
            scheduledTimeSlotEn = "14:00 - 16:30 (Afternoon Express)",
            status = "PENDING",
            assignedCourier = null
        )
    )

    fun getSchedules(): List<DonationPickupSchedule> = _schedules

    fun addSchedule(schedule: DonationPickupSchedule) {
        _schedules.add(0, schedule)
    }

    fun updateStatus(scheduleId: String, newStatus: String, courierName: String? = null) {
        val index = _schedules.indexOfFirst { it.id == scheduleId }
        if (index != -1) {
            val old = _schedules[index]
            _schedules[index] = old.copy(
                status = newStatus,
                assignedCourier = courierName ?: old.assignedCourier
            )
        }
    }
}
