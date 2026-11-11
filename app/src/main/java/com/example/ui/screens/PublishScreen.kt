package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryRepository
import com.example.data.model.Language
import com.example.ui.components.BilingualHeader
import com.example.ui.components.DonationSuccessDialog
import com.example.ui.components.QuickScanCameraDialog
import com.example.ui.components.RecognizedScanResult
import com.example.ui.components.MediaUploadGrid
import com.example.util.GeminiAnalysisResult
import com.example.util.GeminiItemAnalyzer
import com.example.util.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PublishScreen(
    lang: Language,
    onPublishSuccess: (
        title: String,
        majorCatId: String,
        subCatId: String,
        condition: String,
        description: String,
        expectedPrice: Double,
        isDonation: Boolean,
        donorTarget: String?,
        images: List<String>,
        videoUrl: String?,
        location: String,
        contactPhone: String,
        contactWechat: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var currentStep by remember { mutableStateOf(1) } // 1: Photos & Info, 2: AI Assessment, 3: Address & Contact, 4: Review

    var title by remember { mutableStateOf("") }
    var selectedMajorCategory by remember { mutableStateOf(CategoryRepository.majorCategories.first()) }
    var selectedSubCategory by remember { mutableStateOf(CategoryRepository.majorCategories.first().subCategories.first()) }
    var condition by remember { mutableStateOf("90新") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("0") }
    var isDonation by remember { mutableStateOf(false) }
    var donorTarget by remember { mutableStateOf("大凉山偏远山区小学") }
    var location by remember { mutableStateOf("北京市朝阳区三里屯街道") }
    var contactPhone by remember { mutableStateOf("13800138001") }
    var contactWechat by remember { mutableStateOf("green_user_88") }
    var pickupDate by remember { mutableStateOf("2026-08-05 (14:00 - 16:00)") }

    val images = remember { mutableStateListOf("placeholder_img1", "placeholder_img2", "placeholder_img3") }
    var videoUrl by remember { mutableStateOf<String?>("sample_short_video.mp4") }

    // Image Capture & Camera Permissions State
    var cameraPermissionGranted by remember { mutableStateOf(true) }
    var permissionToast by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    var isAnalyzingAi by remember { mutableStateOf(false) }
    var aiResult by remember { mutableStateOf<GeminiAnalysisResult?>(null) }
    var showDonationCelebrationDialog by remember { mutableStateOf(false) }
    var showQuickScanDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    if (showQuickScanDialog) {
        QuickScanCameraDialog(
            lang = lang,
            onDismiss = { showQuickScanDialog = false },
            onApplyScanResult = { res ->
                title = res.title
                condition = res.condition
                description = res.description
                val matchedCat = CategoryRepository.majorCategories.find { cat -> cat.id == res.categoryId } ?: selectedMajorCategory
                selectedMajorCategory = matchedCat
                if (matchedCat.subCategories.isNotEmpty()) {
                    selectedSubCategory = matchedCat.subCategories.first()
                }
                if (images.isEmpty()) {
                    images.add("https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=500")
                }
            }
        )
    }

    if (showDonationCelebrationDialog) {
        DonationSuccessDialog(
            itemTitle = title.ifBlank { "爱心闲置物品" },
            donorTarget = donorTarget,
            lang = lang,
            onDismiss = {
                showDonationCelebrationDialog = false
                val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
                onPublishSuccess(
                    title,
                    selectedMajorCategory.id,
                    selectedSubCategory.id,
                    condition,
                    if (description.isBlank()) "无特殊说明，等待管理员评估上门。" else description,
                    parsedPrice,
                    isDonation,
                    donorTarget,
                    images.toList(),
                    videoUrl,
                    location,
                    contactPhone,
                    contactWechat
                )
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BilingualHeader(
            title = if (lang == Language.ZH) "新建回收申请 (多步骤表单)" else "New Recycling Request Form"
        )

        // Multi-Step Progress Indicator Header (Requirement #1)
        MultiStepHeader(
            currentStep = currentStep,
            lang = lang,
            onStepClick = { step -> if (step < currentStep) currentStep = step }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentStep) {
                1 -> {
                    // STEP 1: Photos & Item Basic Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == Language.ZH) "第 1 步：废品照片与描述" else "Step 1: Item Photos & Description",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Button(
                            onClick = { showQuickScanDialog = true },
                            modifier = Modifier.testTag("open_quick_scan_camera_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera Scan", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (lang == Language.ZH) "📷 相机一键快扫描" else "📷 AI Quick Scan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    // Camera & Gallery Image Capture Permissions Card (Requirement #1)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("camera_permission_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Permission",
                                        tint = if (cameraPermissionGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (lang == Language.ZH) "相机与存储权限 (Image Capture Permission)" else "Camera & Storage Permissions",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (cameraPermissionGranted) {
                                    Text(
                                        text = if (lang == Language.ZH) "已授权 ✓" else "Granted ✓",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (lang == Language.ZH)
                                    "为准确识别旧物材质与评估回收价格，需要获取相机拍摄与本地相册读取权限 (android.permission.CAMERA & READ_MEDIA_IMAGES)。"
                                else
                                    "Camera & media permissions are required to capture waste item photos for AI valuation.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        cameraPermissionGranted = true
                                        permissionToast = if (lang == Language.ZH) "已开启相机拍照权限，请拍摄实物" else "Camera permission authorized"
                                        if (images.size < 9) {
                                            images.add("camera_captured_photo_${images.size + 1}")
                                        }
                                    },
                                    modifier = Modifier.weight(1f).testTag("request_camera_permission_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (lang == Language.ZH) "📷 拍摄照片" else "📷 Take Photo", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        cameraPermissionGranted = true
                                        permissionToast = if (lang == Language.ZH) "相册访问已授权" else "Gallery access granted"
                                        if (images.size < 9) {
                                            images.add("gallery_photo_${images.size + 1}")
                                        }
                                    },
                                    modifier = Modifier.weight(1f).testTag("request_gallery_permission_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (lang == Language.ZH) "🖼️ 相册选择" else "🖼️ Gallery Pick", fontSize = 11.sp)
                                }
                            }

                            permissionToast?.let { msg ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = msg, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }

                    // Media Upload Grid (3-9 photos + short video)
                    MediaUploadGrid(
                        selectedImages = images,
                        selectedVideo = videoUrl,
                        onAddImage = {
                            if (images.size < 9) {
                                images.add("placeholder_img_${images.size + 1}")
                            }
                        },
                        onRemoveImage = { idx ->
                            if (images.size > 0) images.removeAt(idx)
                        },
                        onAddVideo = {
                            videoUrl = "sample_video_${System.currentTimeMillis()}.mp4"
                        },
                        onRemoveVideo = {
                            videoUrl = null
                        },
                        lang = lang
                    )

                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (lang == Language.ZH) "物品名称/标题 (例: 办公闲置木质大书桌)" else "Title (e.g. Wooden Office Desk)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("publish_title_input")
                    )

                    // Condition Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (lang == Language.ZH) "新旧成色:" else "Condition:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        listOf("全新", "95新", "90新", "80新", "需维修/报废").forEach { condOption ->
                            val isSelected = condition == condOption
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { condition = condOption }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = condOption,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(if (lang == Language.ZH) "详细描述 (材质、尺寸、是否有损坏及搬运说明)" else "Detailed Description") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("publish_desc_input")
                    )

                    // Step 1 -> Step 2 Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (title.isBlank()) title = "待回收旧物"
                            currentStep = 2
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("publish_step1_next_btn"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(if (lang == Language.ZH) "下一步：Gemini AI 智能识图与分类 ->" else "Next: AI Assessment ->", fontWeight = FontWeight.Bold)
                    }
                }

                2 -> {
                    // STEP 2: Gemini AI Assessment & Category Selection
                    Text(
                        text = if (lang == Language.ZH) "第 2 步：Gemini API 智能识别与价值评估" else "Step 2: Gemini Vision AI Valuation",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Gemini API Photo Analysis Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_ai_analysis_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Gemini AI",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (lang == Language.ZH) "Gemini 3.5 Flash 智能分类建议" else "Gemini 3.5 Flash Vision AI",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            isAnalyzingAi = true
                                            val sampleBmp = ImageUtils.createSampleWatermarkedBitmap(800, 600, title.ifBlank { "待评估旧物照片" })
                                            aiResult = GeminiItemAnalyzer.analyzeRecycleItem(sampleBmp, title + " " + description, lang)
                                            isAnalyzingAi = false
                                        }
                                    },
                                    enabled = !isAnalyzingAi,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("run_gemini_ai_analysis_btn")
                                ) {
                                    if (isAnalyzingAi) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                    } else {
                                        Text(
                                            text = if (lang == Language.ZH) "⚡ 智能分析照片" else "Analyze Image",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            aiResult?.let { res ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "💡 AI 智能识别标题: ${res.suggestedTitle}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "🏷️ AI 推荐分类: ${res.suggestedMajorCategory.name(lang)} > ${res.suggestedSubCategory.name(lang)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "♻️ 环保建议: ${res.reuseValueAnalysis}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            title = res.suggestedTitle
                                            selectedMajorCategory = res.suggestedMajorCategory
                                            selectedSubCategory = res.suggestedSubCategory
                                            condition = res.detectedCondition
                                            priceText = if (res.isRecommendedDonation) "0" else res.recommendedPrice.toInt().toString()
                                            isDonation = res.isRecommendedDonation
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = if (lang == Language.ZH) "✨ 采用 AI 推荐分类与估价" else "Apply AI Suggestions",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Category Selection & Pricing
                    Text(
                        text = if (lang == Language.ZH) "选择所属类目与预估价格:" else "Category & Pricing:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedMajorCategory.name(lang),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (lang == Language.ZH) "大类" else "Category") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = selectedSubCategory.name(lang),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (lang == Language.ZH) "二级细分" else "Sub-Category") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text(if (lang == Language.ZH) "期望回收价格 (元, 0为无偿捐赠/待评估)" else "Expected Price (0 for Donation)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Donation Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isDonation,
                            onCheckedChange = { isDonation = it },
                            modifier = Modifier.testTag("publish_donation_checkbox")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = if (lang == Language.ZH) "设为公益捐赠 (勾选赠予需要帮助的人群)" else "Mark as Charity Donation",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (lang == Language.ZH) "捐赠成功可获得电子证书与双倍绿碳减排积分" else "Earn eco points & digital certificate upon donation",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isDonation) {
                        OutlinedTextField(
                            value = donorTarget,
                            onValueChange = { donorTarget = it },
                            label = { Text(if (lang == Language.ZH) "指定捐赠目标 (如: 大凉山偏远山区小学)" else "Donation Target School/Region") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Step 2 Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(if (lang == Language.ZH) "<- 上一步" else "<- Back")
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentStep = 3
                            },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("publish_step2_next_btn"),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(if (lang == Language.ZH) "下一步：预约地址 ->" else "Next: Address ->", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                3 -> {
                    // STEP 3: Pickup Location & Contact Info
                    Text(
                        text = if (lang == Language.ZH) "第 3 步：预约上门回收地址与联系方式" else "Step 3: Pickup Address & Contact",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text(if (lang == Language.ZH) "详细上门地址 (省/市/区/街道/门牌号)" else "Collection Address") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pickupDate,
                        onValueChange = { pickupDate = it },
                        label = { Text(if (lang == Language.ZH) "期望上门回收时间段" else "Preferred Pickup Time") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text(if (lang == Language.ZH) "联系电话" else "Phone") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = contactWechat,
                            onValueChange = { contactWechat = it },
                            label = { Text(if (lang == Language.ZH) "微信号 (选填)" else "WeChat") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Step 3 Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStep = 2 },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(if (lang == Language.ZH) "<- 上一步" else "<- Back")
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentStep = 4
                            },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("publish_step3_next_btn"),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(if (lang == Language.ZH) "下一步：确认核对 ->" else "Next: Review ->", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                4 -> {
                    // STEP 4: Review & Final Submission
                    Text(
                        text = if (lang == Language.ZH) "第 4 步：核对发布信息并确认提交" else "Step 4: Review & Submit Request",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📦 物品名称: $title", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("🏷️ 所属类目: ${selectedMajorCategory.name(lang)} > ${selectedSubCategory.name(lang)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text("✨ 新旧程度: $condition", fontSize = 12.sp)
                            Text("💰 预计估价/类型: ${if (isDonation) "❤️ 公益捐赠 ($donorTarget)" else "￥$priceText 元"}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDonation) Color(0xFFE91E63) else MaterialTheme.colorScheme.primary)
                            Text("🖼️ 附带照片: ${images.size} 张照片 ${if (videoUrl != null) "+ 1 个短视频" else ""}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("📍 预约地址: $location", fontSize = 12.sp)
                            Text("🕒 上门时间: $pickupDate", fontSize = 12.sp)
                            Text("📞 联系电话: $contactPhone", fontSize = 12.sp)
                            Text("🌱 预计获得绿碳积分: +50 ~ +120 Points", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStep = 3 },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text(if (lang == Language.ZH) "<- 修改信息" else "Edit Details")
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (title.isBlank()) title = "待回收旧物处理"
                                if (isDonation) {
                                    showDonationCelebrationDialog = true
                                } else {
                                    val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
                                    onPublishSuccess(
                                        title,
                                        selectedMajorCategory.id,
                                        selectedSubCategory.id,
                                        condition,
                                        if (description.isBlank()) "无特殊说明，等待管理员评估上门。" else description,
                                        parsedPrice,
                                        isDonation,
                                        donorTarget,
                                        images.toList(),
                                        videoUrl,
                                        location,
                                        contactPhone,
                                        contactWechat
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .testTag("publish_submit_btn"),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Submit")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (lang == Language.ZH) "🚀 确认提交申请" else "Submit Request",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MultiStepHeader(
    currentStep: Int,
    lang: Language,
    onStepClick: (Int) -> Unit
) {
    val steps = listOf(
        Pair(1, if (lang == Language.ZH) "1. 照片" else "1. Media"),
        Pair(2, if (lang == Language.ZH) "2. AI评估" else "2. AI Assessment"),
        Pair(3, if (lang == Language.ZH) "3. 预约" else "3. Schedule"),
        Pair(4, if (lang == Language.ZH) "4. 确认" else "4. Review")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEach { (stepNum, label) ->
            val isActive = currentStep == stepNum
            val isPassed = currentStep > stepNum

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onStepClick(stepNum) }
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isActive -> MaterialTheme.colorScheme.primary
                                isPassed -> Color(0xFF2E7D32)
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPassed) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text(
                            text = "$stepNum",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (stepNum < 4) {
                Text(text = ">", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

