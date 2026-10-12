package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "donation_receipts")
data class DonationReceipt(
    @PrimaryKey val receiptId: String,
    val donorName: String,
    val donorPhone: String? = null,
    val recipientOrganization: String,
    val itemTitle: String,
    val category: String,
    val estimatedValue: Double = 0.0,
    val co2SavedKg: Double = 3.5,
    val status: String = "COMPLETED",
    val completedTimestamp: Long = System.currentTimeMillis(),
    val digitalHash: String = "",
    val remarks: String? = null
) {
    fun formattedDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(completedTimestamp))
    }

    fun generatePlainTextReceipt(lang: Language): String {
        val phoneStr = if (!donorPhone.isNullOrBlank()) "($donorPhone)" else ""
        return if (lang == Language.ZH) {
            """
            ================================================
            📜 【GreenCycle 绿碳回收】官方公益捐赠电子收据
            ================================================
            收据编号 (Receipt ID) : $receiptId
            开具时间 (Issue Date)  : ${formattedDate()}
            存证哈希 (Digital Hash): $digitalHash
            
            -----------------[ 捐赠明细 ]-----------------
            捐 赠 人 (Donor)      : $donorName $phoneStr
            受赠机构 (Recipient)  : $recipientOrganization
            捐赠物资 (Item Name)  : $itemTitle
            物资类别 (Category)   : $category
            评估价值 (Est. Value) : ¥${String.format(Locale.US, "%.2f", estimatedValue)}
            环保贡献 (CO₂ Saved)  : ${String.format(Locale.US, "%.1f", co2SavedKg)} kg 碳减排
            
            -----------------[ 官方认证 ]-----------------
            验收入库状态 : [ 已验收入库 COMPLETED ]
            发号机构     : GreenCycle 绿碳循环公益互助平台
            防伪印章     : [已加盖爱心捐赠电子校验公章]
            
            感谢您为绿色环保与社区公益事业做出的积极贡献！
            ================================================
            """.trimIndent()
        } else {
            """
            ================================================
            📜 [GreenCycle Eco-Recycle] Official Donation Receipt
            ================================================
            Receipt ID     : $receiptId
            Issue Date     : ${formattedDate()}
            Digital Hash   : $digitalHash
            
            -----------------[ DONATION DETAILS ]-----------------
            Donor Name     : $donorName $phoneStr
            Recipient NGO  : $recipientOrganization
            Donated Item   : $itemTitle
            Item Category  : $category
            Estimated Val  : ¥${String.format(Locale.US, "%.2f", estimatedValue)}
            CO₂ Impact     : ${String.format(Locale.US, "%.1f", co2SavedKg)} kg CO₂ Saved
            
            -----------------[ OFFICIAL VERIFICATION ]-----------------
            Status         : [ COMPLETED & VERIFIED IN STOCK ]
            Issuing Entity : GreenCycle Charity Platform
            Official Seal  : [DIGITALLY SEALED & VERIFIED]
            
            Thank you for your generous contribution to sustainability and community welfare!
            ================================================
            """.trimIndent()
        }
    }
}
