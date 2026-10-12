package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language

data class MonthlyImpactData(
    val monthLabel: String,
    val wasteDivertedKg: Float,
    val co2SavedKg: Float
)

@Composable
fun EnvironmentalImpactChart(
    lang: Language,
    modifier: Modifier = Modifier
) {
    var selectedMetricIndex by remember { mutableIntStateOf(0) } // 0 = Waste Diverted, 1 = CO2 Saved

    val monthlyData = listOf(
        MonthlyImpactData("1月", 8.2f, 14.5f),
        MonthlyImpactData("2月", 12.0f, 21.0f),
        MonthlyImpactData("3月", 15.5f, 26.8f),
        MonthlyImpactData("4月", 11.2f, 19.4f),
        MonthlyImpactData("5月", 18.0f, 31.2f),
        MonthlyImpactData("6月", 19.6f, 34.0f)
    )

    val totalWaste = monthlyData.sumOf { it.wasteDivertedKg.toDouble() }
    val totalCo2 = monthlyData.sumOf { it.co2SavedKg.toDouble() }
    val equivalentTrees = (totalCo2 / 18.0).coerceAtLeast(1.0)

    val activeColor = if (selectedMetricIndex == 0) Color(0xFF2E7D32) else Color(0xFF0288D1)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("environmental_impact_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = "Impact Graph",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (lang == Language.ZH) "个人环保影响力可视化图表" else "Environmental Impact Visualizer",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (lang == Language.ZH) "基于旧物循环与绿色回收数据自动计算" else "Calculated from recycled items & donations",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Metric Toggle Pills
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selectedMetricIndex == 0) Color(0xFF2E7D32) else Color.Transparent)
                            .clickable { selectedMetricIndex = 0 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (lang == Language.ZH) "回收量" else "Waste",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedMetricIndex == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selectedMetricIndex == 1) Color(0xFF0288D1) else Color.Transparent)
                            .clickable { selectedMetricIndex = 1 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (lang == Language.ZH) "减碳量" else "CO₂",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedMetricIndex == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Impact Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ImpactStatBadge(
                    icon = Icons.Default.DeleteSweep,
                    value = "${String.format("%.1f", totalWaste)} kg",
                    label = if (lang == Language.ZH) "累计转移废弃物" else "Total Waste Diverted",
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ImpactStatBadge(
                    icon = Icons.Default.Co2,
                    value = "${String.format("%.1f", totalCo2)} kg",
                    label = if (lang == Language.ZH) "减排碳足迹" else "CO₂ Saved",
                    color = Color(0xFF0288D1),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ImpactStatBadge(
                    icon = Icons.Default.Park,
                    value = "${String.format("%.1f", equivalentTrees)} 棵",
                    label = if (lang == Language.ZH) "相当于植树" else "Trees Planted",
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Chart Canvas Rendering
            val values = if (selectedMetricIndex == 0) monthlyData.map { it.wasteDivertedKg } else monthlyData.map { it.co2SavedKg }
            val maxValue = (values.maxOrNull() ?: 40f) * 1.25f

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(8.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val bottomAxisY = canvasHeight - 36f
                val chartHeight = bottomAxisY - 20f

                val barWidth = 28f
                val totalBars = monthlyData.size
                val spacing = (canvasWidth - (totalBars * barWidth)) / (totalBars + 1)

                // Gridlines
                val gridPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    alpha = 50
                    strokeWidth = 2f
                }
                drawContext.canvas.nativeCanvas.drawLine(0f, bottomAxisY, canvasWidth, bottomAxisY, gridPaint)
                drawContext.canvas.nativeCanvas.drawLine(0f, bottomAxisY - chartHeight * 0.5f, canvasWidth, bottomAxisY - chartHeight * 0.5f, gridPaint)

                monthlyData.forEachIndexed { index, item ->
                    val valX = spacing + index * (barWidth + spacing)
                    val value = if (selectedMetricIndex == 0) item.wasteDivertedKg else item.co2SavedKg
                    val barHeight = (value / maxValue) * chartHeight

                    val barTop = bottomAxisY - barHeight

                    // Draw rounded top bar
                    drawRoundRect(
                        color = activeColor,
                        topLeft = Offset(valX, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(10f, 10f)
                    )

                    // Draw value text above bar
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        "${value.toInt()}",
                        valX + barWidth / 2f,
                        barTop - 6f,
                        textPaint
                    )

                    // Draw month label under bar
                    val labelPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        item.monthLabel,
                        valX + barWidth / 2f,
                        bottomAxisY + 28f,
                        labelPaint
                    )
                }
            }
        }
    }
}

@Composable
private fun ImpactStatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
