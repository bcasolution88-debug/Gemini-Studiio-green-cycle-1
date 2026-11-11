package com.example.ui.components

import android.graphics.Bitmap
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.Language
import com.example.ui.screens.MapPoint
import com.example.ui.screens.MapPointType
import kotlin.math.roundToInt

@Composable
fun StationArOverlayView(
    points: List<MapPoint>,
    lang: Language,
    onSelectPoint: (MapPoint) -> Unit,
    onOpenChat: (MapPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedArPoint by remember(points) { mutableStateOf(points.firstOrNull()) }
    var userHeadingDegrees by remember { mutableFloatStateOf(45f) } // Initial azimuth degree (0° - 360°)
    var isCameraBound by remember { mutableStateOf(false) }
    var arDistanceMode by remember { mutableStateOf("NEARBY") } // "NEARBY", "ALL"

    // Pulse animation for AR target reticle
    val pulseAnim = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        pulseAnim.animateTo(
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // CameraX initialization
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A))
            .testTag("station_ar_overlay_view")
    ) {
        // 1. Camera Preview Layer
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview
                        )
                        isCameraBound = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Simulated AR HUD Feed Background fallback if camera permission or preview hardware is unavailable
        if (!isCameraBound) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0D1B2A),
                                Color(0xFF1B263B),
                                Color(0xFF415A77)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "AR Camera Feed",
                        tint = Color(0xFF00E676).copy(alpha = 0.6f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (lang == Language.ZH) "📷 AR 实景相机透视视角已激活" else "📷 AR Camera View Active",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. AR Grid & Horizon Guideline Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, _, _ ->
                        userHeadingDegrees = (userHeadingDegrees - pan.x * 0.15f + 360) % 360
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // Center Horizon Line
            drawLine(
                color = Color(0xFF00E676).copy(alpha = 0.35f),
                start = Offset(0f, h * 0.52f),
                end = Offset(w, h * 0.52f),
                strokeWidth = 1.5.dp.toPx()
            )

            // Center AR Crosshair Target Reticle
            val center = Offset(w / 2, h * 0.48f)
            val radius = 28.dp.toPx() * pulseAnim.value
            drawCircle(
                color = Color(0xFF00E676).copy(alpha = 0.5f),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF00E676),
                radius = 4.dp.toPx(),
                center = center
            )
        }

        // 3. Top AR Compass & Degree Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Compass",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (lang == Language.ZH) "AR 360° 实景导览" else "AR 360° Vision",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${userHeadingDegrees.roundToInt()}° ${getBearingDirectionName(userHeadingDegrees)}",
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Panning Control Slider (Simulates phone rotation in 360° space)
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.width(140.dp)
                    ) {
                        Text(
                            text = if (lang == Language.ZH) "↔ 滑动或旋转手机" else "↔ Pan / Rotate",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                        Slider(
                            value = userHeadingDegrees,
                            onValueChange = { userHeadingDegrees = it },
                            valueRange = 0f..360f,
                            modifier = Modifier.height(20.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00E676),
                                activeTrackColor = Color(0xFF00E676)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // AR Detected Points Counter Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E7D32).copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (lang == Language.ZH) "🟢 周边已捕获 ${points.size} 个实景站点/投递柜" else "🟢 ${points.size} Centers Detected in AR",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable {
                            Toast.makeText(
                                context,
                                if (lang == Language.ZH) "🔄 AR实景空间方位与GPS距离已校准！" else "🔄 AR spatial orientation recalibrated!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Calibrate",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lang == Language.ZH) "校准" else "Calibrate",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // 4. Floating AR Overlay Cards Layer (Positioned according to relative azimuth to current heading)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, bottom = 140.dp)
        ) {
            points.forEachIndexed { index, pt ->
                // Calculate virtual relative bearing angle (-180° to +180°) relative to camera user heading
                val targetBearing = (index * 75f + 30f) % 360f
                var relativeAngle = targetBearing - userHeadingDegrees
                while (relativeAngle > 180f) relativeAngle -= 360f
                while (relativeAngle < -180f) relativeAngle += 360f

                // Map relative angle (-90° to +90° visible field of view) to horizontal offset percentage
                val visible = relativeAngle in -80f..80f
                val xPercent = (relativeAngle / 80f).coerceIn(-0.85f, 0.85f)
                val isSelected = selectedArPoint?.id == pt.id

                if (visible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Offset floating card based on angle and index height stagger
                        val xOffset = (xPercent * 160).dp
                        val yOffset = ((index % 3) * 65 - 60).dp

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(xOffset.roundToPx(), yOffset.roundToPx()) }
                        ) {
                            ArFloatingMarkerCard(
                                point = pt,
                                relativeAngle = relativeAngle,
                                isSelected = isSelected,
                                lang = lang,
                                onClick = {
                                    selectedArPoint = pt
                                    onSelectPoint(pt)
                                }
                            )
                        }
                    }
                }
            }
        }

        // 5. Bottom Selected Station Detail & Navigation Action Dock
        selectedArPoint?.let { pt ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .testTag("ar_selected_point_card"),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.95f),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (pt.type) {
                                            MapPointType.DONATION_DROP_OFF -> Color(0xFFE91E63)
                                            MapPointType.PUBLIC_RECYCLING_BIN -> Color(0xFF2E7D32)
                                            MapPointType.RECYCLING_STATION -> Color(0xFF0288D1)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (pt.type) {
                                        MapPointType.DONATION_DROP_OFF -> "❤️"
                                        MapPointType.PUBLIC_RECYCLING_BIN -> "🗑️"
                                        MapPointType.RECYCLING_STATION -> "🏢"
                                    },
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = pt.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pt.verifiedBadge,
                                        fontSize = 11.sp,
                                        color = Color(0xFF00E676),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "📍 ${pt.distanceKm} km",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Contact or Chat button
                        IconButton(
                            onClick = { onOpenChat(pt) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Contact",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "📍 ${pt.address}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    if (lang == Language.ZH) "🗺️ 已启动 AR 实景导航至: ${pt.name} (预计步行 ${(pt.distanceKm * 12).roundToInt()} 分钟)" else "🗺️ Navigating to ${pt.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("ar_navigate_btn"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "Navigate",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lang == Language.ZH) "AR 路线导航 (${(pt.distanceKm * 1000).roundToInt()}m)" else "AR Route Nav",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArFloatingMarkerCard(
    point: MapPoint,
    relativeAngle: Float,
    isSelected: Boolean,
    lang: Language,
    onClick: () -> Unit
) {
    val directionArrow = when {
        relativeAngle < -20f -> "↖"
        relativeAngle > 20f -> "↗"
        else -> "⬆"
    }

    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .testTag("ar_marker_card_${point.id}"),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF00E676) else Color.Black.copy(alpha = 0.8f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color.White) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f)),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = directionArrow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.Black else Color(0xFF00E676)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = point.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.Black else Color.White,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color.Black.copy(alpha = 0.15f) else Color(0xFF00E676).copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${(point.distanceKm * 1000).roundToInt()}m",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else Color(0xFF00E676)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "⭐ ${point.rating}",
                    fontSize = 10.sp,
                    color = if (isSelected) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun getBearingDirectionName(deg: Float): String {
    return when ((deg % 360).toInt()) {
        in 0..22 -> "北 (N)"
        in 23..67 -> "东北 (NE)"
        in 68..112 -> "东 (E)"
        in 113..157 -> "东南 (SE)"
        in 158..202 -> "南 (S)"
        in 203..247 -> "西南 (SW)"
        in 248..292 -> "西 (W)"
        in 293..337 -> "西北 (NW)"
        else -> "北 (N)"
    }
}
