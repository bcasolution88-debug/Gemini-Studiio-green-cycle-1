package com.example.ui.components

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Language
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ScanSampleItem(
    val id: String,
    val labelZh: String,
    val labelEn: String,
    val title: String,
    val categoryId: String,
    val condition: String,
    val weightKg: String,
    val ecoPoints: Int,
    val recyclingAdvice: String
)

data class RecognizedScanResult(
    val title: String,
    val categoryId: String,
    val condition: String,
    val description: String,
    val weightKg: String,
    val ecoPoints: Int
)

@Composable
fun QuickScanCameraDialog(
    lang: Language,
    onDismiss: () -> Unit,
    onApplyScanResult: (RecognizedScanResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isPermissionGranted by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var recognizedResult by remember { mutableStateOf<ScanSampleItem?>(null) }

    val scanSamples = remember {
        listOf(
            ScanSampleItem(
                id = "sample_1",
                labelZh = "📱 废旧数码平板",
                labelEn = "Digital Electronics",
                title = "旧iPad/电子书阅读器 (含原装壳)",
                categoryId = "cat_elec",
                condition = "良好",
                weightKg = "0.85 kg",
                ecoPoints = 200,
                recyclingAdvice = "检测到内置可重利用贵金属与锂电池，专员上门无害化安全拆解。"
            ),
            ScanSampleItem(
                id = "sample_2",
                labelZh = "🧥 废旧羽绒服/织物",
                labelEn = "Old Textiles/Clothing",
                title = "九成新冬季儿童保温羽绒服",
                categoryId = "cat_clothes",
                condition = "全新/微瑕",
                weightKg = "1.50 kg",
                ecoPoints = 150,
                recyclingAdvice = "成色极佳！系统已自动匹配【爱心公益捐赠专区】定向寄往偏远小学。"
            ),
            ScanSampleItem(
                id = "sample_3",
                labelZh = "📦 废纸箱/瓦楞纸",
                labelEn = "Cardboard Boxes",
                title = "大号快递加厚瓦楞纸箱",
                categoryId = "cat_paper",
                condition = "一般",
                weightKg = "3.20 kg",
                ecoPoints = 80,
                recyclingAdvice = "高纯度造纸原料，再生纤维可节省水资源15升。"
            ),
            ScanSampleItem(
                id = "sample_4",
                labelZh = "🔌 废旧家电微波炉",
                labelEn = "Home Appliance",
                title = "旧格兰仕微波炉 (加热正常)",
                categoryId = "cat_elec",
                condition = "可正常使用",
                weightKg = "8.50 kg",
                ecoPoints = 350,
                recyclingAdvice = "包含铜线电机与高价值零部件，提供免费打包搬运。"
            )
        )
    }

    var activeSample by remember { mutableStateOf(scanSamples.first()) }

    // Scanner Laser Animation Y Offset
    val scanLineY = remember { Animatable(0f) }
    LaunchedEffect(isScanning) {
        if (isScanning) {
            scanLineY.animateTo(
                targetValue = 200f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    fun triggerAiRecognition() {
        scope.launch {
            isScanning = true
            recognizedResult = null
            delay(1500)
            isScanning = false
            recognizedResult = activeSample
            Toast.makeText(
                context,
                if (lang == Language.ZH) "✨ 识别成功！已识别到【${activeSample.title}】" else "✨ Identified: ${activeSample.title}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF121212),
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .testTag("quick_scan_camera_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar with Permission and Flash Controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == Language.ZH) "📷 相机智能快扫描识别" else "📷 AI Camera Quick Scan",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isFlashOn = !isFlashOn }) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = "Flash",
                                    tint = if (isFlashOn) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.6f)
                                )
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
                }

                if (!isPermissionGranted) {
                    // Dynamic Camera Permission Simulator Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Permission",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (lang == Language.ZH) "需要访问摄像头权限" else "Camera Permission Required",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (lang == Language.ZH) "GreenCycle 使用设备相机实时扫描识别废旧物资品类，预估材质重量与绿碳积分。" else "GreenCycle uses your camera to scan waste items and pre-fill recycling requests.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                isPermissionGranted = true
                                triggerAiRecognition()
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(48.dp)
                                .testTag("grant_camera_permission_btn"),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                        ) {
                            Text(
                                text = if (lang == Language.ZH) "开启相机并自动识别" else "Allow & Start Scan",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Camera Viewfinder & Object Recognition Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.2f)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF212121))
                            .border(2.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Viewfinder Overlay Grid
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val rectSize = 220.dp.toPx()
                            val left = (w - rectSize) / 2
                            val top = (h - rectSize) / 2

                            // Reticle corners
                            val cornerLen = 30.dp.toPx()
                            val color = if (isScanning) Color(0xFFFFD54F) else Color(0xFF00E676)
                            val strokeW = 4.dp.toPx()

                            // Top-Left
                            drawLine(color, Offset(left, top), Offset(left + cornerLen, top), strokeW)
                            drawLine(color, Offset(left, top), Offset(left, top + cornerLen), strokeW)
                            // Top-Right
                            drawLine(color, Offset(left + rectSize, top), Offset(left + rectSize - cornerLen, top), strokeW)
                            drawLine(color, Offset(left + rectSize, top), Offset(left + rectSize, top + cornerLen), strokeW)
                            // Bottom-Left
                            drawLine(color, Offset(left, top + rectSize), Offset(left + cornerLen, top + rectSize), strokeW)
                            drawLine(color, Offset(left, top + rectSize), Offset(left, top + rectSize - cornerLen), strokeW)
                            // Bottom-Right
                            drawLine(color, Offset(left + rectSize, top + rectSize), Offset(left + rectSize - cornerLen, top + rectSize), strokeW)
                            drawLine(color, Offset(left + rectSize, top + rectSize), Offset(left + rectSize, top + rectSize - cornerLen), strokeW)

                            // Scanning Line
                            if (isScanning) {
                                drawLine(
                                    color = Color(0xFFFFD54F),
                                    start = Offset(left, top + scanLineY.value),
                                    end = Offset(left + rectSize, top + scanLineY.value),
                                    strokeWidth = 3.dp.toPx()
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isScanning) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (lang == Language.ZH) "⚡ Gemini AI 多模态视觉分类中..." else "⚡ AI Object Recognition Processing...",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = activeSample.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (lang == Language.ZH) "置信度: 98.6% (高精度已对焦)" else "Confidence: 98.6%",
                                    color = Color(0xFF00E676),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Sample Image Frame Selector (Simulates scanning different physical items)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = if (lang == Language.ZH) "🔍 切换扫描对比样例废件" else "🔍 Switch Camera Scan Target",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(scanSamples) { sample ->
                                val isSelected = activeSample.id == sample.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) Color(0xFF00E676) else Color(0xFF2A2A2A))
                                        .clickable {
                                            activeSample = sample
                                            triggerAiRecognition()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = sample.labelZh,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Scan Recognition Result Detail Sheet
                    recognizedResult?.let { result ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .testTag("scan_recognition_result_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Check",
                                            tint = Color(0xFF00E676),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (lang == Language.ZH) "AI 智能识别完成" else "AI Item Recognition Complete",
                                            color = Color(0xFF00E676),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "+${result.ecoPoints} 积分",
                                            color = Color(0xFF00E676),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    text = "📦 物品名称: ${result.title}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "⚖️ 预估重量: ${result.weightKg} | 成色: ${result.condition}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )

                                Text(
                                    text = "💡 建议: ${result.recyclingAdvice}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        onApplyScanResult(
                                            RecognizedScanResult(
                                                title = result.title,
                                                categoryId = result.categoryId,
                                                condition = result.condition,
                                                description = "${result.recyclingAdvice} (AI快扫描预估重量 ${result.weightKg})",
                                                weightKg = result.weightKg,
                                                ecoPoints = result.ecoPoints
                                            )
                                        )
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("apply_scan_result_btn"),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                                ) {
                                    Text(
                                        text = if (lang == Language.ZH) "⚡ 一键带入回收预约单" else "Pre-fill Recycling Request",
                                        color = Color.Black,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}
