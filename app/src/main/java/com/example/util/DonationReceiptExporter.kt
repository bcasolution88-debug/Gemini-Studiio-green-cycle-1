package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.data.model.CommunityWishlistItem
import com.example.data.model.DonationReceipt
import com.example.data.model.Language
import com.example.data.model.OrderAgreement
import com.example.data.model.RecycleItem
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object DonationReceiptExporter {

    fun exportReceiptToPdf(context: Context, receipt: DonationReceipt, lang: Language): Boolean {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // 1. Header Banner
        paint.color = Color.parseColor("#1B5E20")
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val headerTitle = if (lang == Language.ZH) "GreenCycle 绿碳回收 - 官方公益捐赠电子收据" else "GreenCycle - Official Charity Donation Receipt"
        canvas.drawText(headerTitle, 30f, 48f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val headerSub = if (lang == Language.ZH) "区块链存证认证编号: ${receipt.receiptId}" else "Blockchain Certificate ID: ${receipt.receiptId}"
        canvas.drawText(headerSub, 30f, 75f, paint)

        // 2. Certificate Border
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#2E7D32")
        paint.strokeWidth = 2f
        canvas.drawRoundRect(RectF(25f, 120f, 570f, 750f), 12f, 12f, paint)

        paint.style = Paint.Style.FILL

        // 3. Document Details
        var startY = 160f
        val lineGap = 34f

        fun drawDetailLine(labelZh: String, labelEn: String, value: String, isBold: Boolean = false, isAccent: Boolean = false) {
            paint.textSize = 12f
            paint.color = Color.parseColor("#555555")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val label = if (lang == Language.ZH) labelZh else labelEn
            canvas.drawText(label, 45f, startY, paint)

            paint.color = if (isAccent) Color.parseColor("#1B5E20") else Color.parseColor("#222222")
            paint.typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(value, 210f, startY, paint)

            val linePaint = Paint().apply {
                color = Color.parseColor("#E0E0E0")
                strokeWidth = 1f
            }
            canvas.drawLine(45f, startY + 10f, 550f, startY + 10f, linePaint)

            startY += lineGap
        }

        drawDetailLine("收据编号 (Receipt ID)", "Receipt ID", receipt.receiptId, isBold = true, isAccent = true)
        drawDetailLine("捐 赠 人 (Donor Name)", "Donor Name", receipt.donorName + (if (!receipt.donorPhone.isNullOrBlank()) " (${receipt.donorPhone})" else ""))
        drawDetailLine("受赠机构 (Recipient NGO)", "Recipient NGO", receipt.recipientOrganization, isBold = true)
        drawDetailLine("捐赠物资 (Donated Item)", "Donated Item", receipt.itemTitle)
        drawDetailLine("物资类别 (Category)", "Category", receipt.category)
        drawDetailLine("评估价值 (Est. Value)", "Est. Value", "¥ " + String.format(Locale.US, "%.2f", receipt.estimatedValue))
        drawDetailLine("减碳贡献 (CO₂ Impact)", "CO₂ Impact", String.format(Locale.US, "%.1f", receipt.co2SavedKg) + " kg CO₂ e", isAccent = true)
        drawDetailLine("验收入库 (Status)", "Status", if (lang == Language.ZH) "已验收入库 (COMPLETED)" else "COMPLETED & IN STOCK", isBold = true)
        drawDetailLine("签发时间 (Issue Date)", "Issue Date", receipt.formattedDate())
        drawDetailLine("数字存证哈希 (Digital Hash)", "Digital Hash", receipt.digitalHash)

        // 4. Draw Official Red Stamp / Seal
        val sealCenterX = 450f
        val sealCenterY = 620f
        val sealRadius = 55f

        val sealPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = Color.parseColor("#D32F2F")
            strokeWidth = 3f
        }
        canvas.drawCircle(sealCenterX, sealCenterY, sealRadius, sealPaint)
        canvas.drawCircle(sealCenterX, sealCenterY, sealRadius - 4f, sealPaint)

        sealPaint.style = Paint.Style.FILL
        sealPaint.textSize = 9f
        sealPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        sealPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(if (lang == Language.ZH) "★ 中华公益 ★" else "★ CHARITY ★", sealCenterX, sealCenterY - 14f, sealPaint)
        canvas.drawText(if (lang == Language.ZH) "绿碳循环捐赠" else "VERIFIED SEAL", sealCenterX, sealCenterY + 4f, sealPaint)
        canvas.drawText(if (lang == Language.ZH) "验收入库专用章" else "OFFICIAL STAMP", sealCenterX, sealCenterY + 20f, sealPaint)

        // 5. Footer Note
        paint.color = Color.parseColor("#777777")
        paint.textSize = 11f
        paint.textAlign = Paint.Align.CENTER
        val footerText = if (lang == Language.ZH) "感谢您对绿色环保与乡村社区公益事业做出的卓越贡献！" else "Thank you for your valuable contribution to environmental sustainability!"
        canvas.drawText(footerText, 297f, 780f, paint)

        pdfDocument.finishPage(page)

        val fileName = "DonationReceipt_${receipt.receiptId}.pdf"
        var isSaved = false

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        pdfDocument.writeTo(out)
                    }
                    isSaved = true
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { out ->
                    pdfDocument.writeTo(out)
                }
                isSaved = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        if (isSaved) {
            val successMsg = if (lang == Language.ZH) "📄 PDF捐赠收据已导出存至【下载 (Downloads)】: $fileName" else "📄 PDF receipt saved to Downloads: $fileName"
            Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
        } else {
            val errorMsg = if (lang == Language.ZH) "导出PDF收据失败" else "Failed to export PDF receipt"
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
        }

        return isSaved
    }

    fun downloadReceiptToDevice(context: Context, receipt: DonationReceipt, lang: Language): Boolean {
        val fileName = "DonationReceipt_${receipt.receiptId}.txt"
        val content = receipt.generatePlainTextReceipt(lang)

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray(Charsets.UTF_8))
                        outputStream.flush()
                    }
                    val successMsg = if (lang == Language.ZH) {
                        "✅ 捐赠收据已导出至系统【下载文件夹 (Downloads)】: $fileName"
                    } else {
                        "✅ Receipt exported to Downloads folder: $fileName"
                    }
                    Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                    true
                } else {
                    false
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { out ->
                    out.write(content.toByteArray(Charsets.UTF_8))
                    out.flush()
                }
                val successMsg = if (lang == Language.ZH) {
                    "✅ 捐赠收据已导出存至: ${file.absolutePath}"
                } else {
                    "✅ Receipt saved to: ${file.absolutePath}"
                }
                Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMsg = if (lang == Language.ZH) {
                "导出收据失败: ${e.localizedMessage ?: "未知错误"}"
            } else {
                "Failed to export receipt: ${e.localizedMessage ?: "Unknown error"}"
            }
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun shareReceipt(context: Context, receipt: DonationReceipt, lang: Language) {
        val text = receipt.generatePlainTextReceipt(lang)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val chooserTitle = if (lang == Language.ZH) "导出/分享捐赠收据" else "Share Donation Receipt"
        context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
    }

    fun createReceiptFromItem(item: RecycleItem, defaultDonor: String = "爱心捐赠用户"): DonationReceipt {
        val hash = "0x" + (item.id + item.createdAt.toString()).hashCode().toString(16).takeLast(10).uppercase()
        val org = item.donorTarget ?: item.assignedStationName ?: "中华公益希望救助基金会 / Hope Charity"
        val value = if (item.expectedPrice > 0) item.expectedPrice else (item.estimatedPrice ?: 50.0)
        return DonationReceipt(
            receiptId = "RCT-${item.id.takeLast(6).uppercase()}",
            donorName = defaultDonor,
            donorPhone = item.contactPhone,
            recipientOrganization = org,
            itemTitle = item.title,
            category = item.subCategoryId.ifBlank { "闲置捐赠" },
            estimatedValue = value,
            co2SavedKg = (value * 0.18 + 2.5).coerceAtLeast(1.5),
            status = "COMPLETED",
            completedTimestamp = System.currentTimeMillis(),
            digitalHash = hash,
            remarks = item.description
        )
    }

    fun createReceiptFromOrder(order: OrderAgreement): DonationReceipt {
        val hash = "0x" + (order.orderId + order.createdAt.toString()).hashCode().toString(16).takeLast(10).uppercase()
        val valAmt = if (order.finalPrice > 0) order.finalPrice else 60.0
        return DonationReceipt(
            receiptId = "RCT-${order.orderId.takeLast(6).uppercase()}",
            donorName = order.sellerName,
            donorPhone = order.sellerPhone,
            recipientOrganization = order.stationName.ifBlank { "阳光社区爱心驿站" },
            itemTitle = order.itemTitle,
            category = "爱心物资回收",
            estimatedValue = valAmt,
            co2SavedKg = (valAmt * 0.2 + 3.0).coerceAtLeast(2.0),
            status = "COMPLETED",
            completedTimestamp = order.createdAt,
            digitalHash = order.digitalSignature ?: hash
        )
    }

    fun createReceiptFromWishlist(wishlist: CommunityWishlistItem, donorName: String = "爱心市民"): DonationReceipt {
        val hash = "0x" + (wishlist.id + System.currentTimeMillis()).hashCode().toString(16).takeLast(10).uppercase()
        return DonationReceipt(
            receiptId = "RCT-NGO-${wishlist.id.takeLast(4).uppercase()}",
            donorName = donorName,
            recipientOrganization = wishlist.ngoName,
            itemTitle = wishlist.neededItemTitleZh,
            category = "NGO社区急需清单匹配",
            estimatedValue = 88.0,
            co2SavedKg = 4.2,
            status = "COMPLETED",
            completedTimestamp = System.currentTimeMillis(),
            digitalHash = hash,
            remarks = wishlist.descriptionZh
        )
    }
}
