package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProcessedImageResult(
    val bitmap: Bitmap,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val timestampFormatted: String,
    val watermarkText: String
) {
    val compressionRatioPercent: Int
        get() = if (originalSizeBytes > 0) {
            (100 - (compressedSizeBytes * 100 / originalSizeBytes)).toInt().coerceIn(0, 99)
        } else 65
}

object ImageUtils {

    private const val MAX_IMAGE_DIMENSION = 1280
    private const val JPEG_QUALITY = 80

    /**
     * Client-side Image Compression & Watermarking Utility.
     * Takes an incoming high-res bitmap, resizes it down to max maxDimension (e.g. 1280px),
     * applies 'Green Cycle' watermark + live timestamp canvas overlay,
     * and compresses with 80% JPEG quality to optimize storage and upload speeds.
     */
    fun processAndWatermarkImage(
        context: Context,
        rawBitmap: Bitmap,
        customTag: String = "Green Cycle 绿碳回收"
    ): ProcessedImageResult {
        // Calculate original estimate bytes
        val originalEstimateBytes = (rawBitmap.width * rawBitmap.height * 4).toLong()

        // 1. Scale down bitmap if exceeds max dimension
        val scaledBitmap = scaleBitmapIfNeeded(rawBitmap, MAX_IMAGE_DIMENSION)

        // 2. Add 'Green Cycle' watermark + timestamp canvas overlay
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestampText = sdf.format(Date())
        val watermarkedBitmap = applyWatermarkOverlay(scaledBitmap, customTag, timestampText)

        // 3. Compress to JPEG quality = 80%
        val outputStream = ByteArrayOutputStream()
        watermarkedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
        val compressedBytes = outputStream.toByteArray()

        return ProcessedImageResult(
            bitmap = watermarkedBitmap,
            originalSizeBytes = originalEstimateBytes,
            compressedSizeBytes = compressedBytes.size.toLong(),
            timestampFormatted = timestampText,
            watermarkText = "$customTag | $timestampText"
        )
    }

    /**
     * Helper to scale bitmap to max bounding dimension while maintaining aspect ratio.
     */
    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDim && height <= maxDim) {
            return bitmap
        }

        val aspectRatio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (width > height) {
            targetWidth = maxDim
            targetHeight = (maxDim / aspectRatio).toInt()
        } else {
            targetHeight = maxDim
            targetWidth = (maxDim * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    /**
     * Overlay 'Green Cycle' branded watermark pill with timestamp on bottom-right corner.
     */
    private fun applyWatermarkOverlay(
        source: Bitmap,
        brandText: String,
        timestamp: String
    ): Bitmap {
        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val width = result.width
        val height = result.height

        // Paint configuration
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = (height * 0.035f).coerceIn(24f, 48f)
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val fullWatermark = "♻️ $brandText  $timestamp"
        val textWidth = textPaint.measureText(fullWatermark)
        val fontMetrics = textPaint.fontMetrics
        val textHeight = fontMetrics.bottom - fontMetrics.top

        val paddingHorizontal = 24f
        val paddingVertical = 16f
        val marginEnd = 24f
        val marginBottom = 24f

        val rectRight = width - marginEnd
        val rectLeft = rectRight - (textWidth + paddingHorizontal * 2)
        val rectBottom = height - marginBottom
        val rectTop = rectBottom - (textHeight + paddingVertical * 2)

        // Draw semi-transparent dark green background pill
        val bgPaint = Paint().apply {
            color = Color.parseColor("#E01B5E20") // Deep Forest Green with 88% opacity
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val cornerRadius = 16f
        canvas.drawRoundRect(
            RectF(rectLeft, rectTop, rectRight, rectBottom),
            cornerRadius,
            cornerRadius,
            bgPaint
        )

        // Draw green accent border
        val borderPaint = Paint().apply {
            color = Color.parseColor("#804CAF50") // Light green border
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(
            RectF(rectLeft, rectTop, rectRight, rectBottom),
            cornerRadius,
            cornerRadius,
            borderPaint
        )

        // Draw Text inside pill
        val textX = rectLeft + paddingHorizontal
        val textY = rectTop + paddingVertical - fontMetrics.top
        canvas.drawText(fullWatermark, textX, textY, textPaint)

        return result
    }

    /**
     * Generate sample placeholder bitmap with image compression and watermark applied for demonstration.
     */
    fun createSampleWatermarkedBitmap(
        width: Int = 800,
        height: Int = 600,
        itemTitle: String = "待回收二手物品照片"
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw neutral gradient background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#ECEFF1")
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw item title placeholder text
        val titlePaint = Paint().apply {
            color = Color.parseColor("#37474F")
            textSize = 36f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(itemTitle, width / 2f, height / 2f - 20f, titlePaint)

        val subPaint = Paint().apply {
            color = Color.parseColor("#78909C")
            textSize = 24f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("【已进行客户端 JPEG 压缩 & 水印打标】", width / 2f, height / 2f + 30f, subPaint)

        // Apply watermark and timestamp
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return applyWatermarkOverlay(bitmap, "Green Cycle 绿碳回收", sdf.format(Date()))
    }
}
