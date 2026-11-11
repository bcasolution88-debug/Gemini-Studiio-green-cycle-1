package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn

import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SafetyCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ForumPost
import com.example.data.model.Language
import com.example.ui.components.BilingualHeader
import com.example.ui.components.ZeroWasteTutorialsSection
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import com.example.util.ShareUtils

data class DismantleVideoTutorial(
    val id: String,
    val titleZh: String,
    val titleEn: String,
    val authorNameZh: String,
    val authorNameEn: String,
    val isAiGenerated: Boolean,
    val duration: String,
    val categoryTagZh: String,
    val categoryTagEn: String,
    val stepsZh: List<String>,
    val stepsEn: List<String>,
    val safetyWarningZh: String,
    val safetyWarningEn: String,
    val gradientColors: List<Color>
)

val sampleDismantleTutorials = listOf(
    DismantleVideoTutorial(
        id = "vid_1",
        titleZh = "🤖 Gemini AI 拆解教学: 废旧笔记本电脑电池与铜管安全拆卸",
        titleEn = "🤖 Gemini AI Guide: Safe Laptop Battery & Copper Pipe Dismantling",
        authorNameZh = "Gemini AI Vision Bot",
        authorNameEn = "Gemini AI Vision Bot",
        isAiGenerated = true,
        duration = "01:45",
        categoryTagZh = "电子废弃物",
        categoryTagEn = "E-Waste",
        stepsZh = listOf(
            "彻底关机并卸下笔记本底壳所有微型螺丝",
            "使用防静电绝缘撬棒优先拔掉锂电池供电排线",
            "依次拆卸导热纯铜管与风扇部件，分别归类回收"
        ),
        stepsEn = listOf(
            "Power off & remove bottom chassis micro-screws",
            "Disconnect lithium battery power pin using ESD pry tool",
            "Detach copper heat pipes and cooling fan for sorting"
        ),
        safetyWarningZh = "⚠️ 严禁金属撬棒刺穿锂电池包，拆解过程建议佩戴防绝缘防割手套。",
        safetyWarningEn = "⚠️ Do NOT puncture battery cells with metal tools. Wear ESD gloves.",
        gradientColors = listOf(Color(0xFF1E88E5), Color(0xFF1565C0))
    ),
    DismantleVideoTutorial(
        id = "vid_2",
        titleZh = "👨‍🔧 社区达人实拍: 旧木质沙发海绵包垫与螺钉高效拆解分选",
        titleEn = "👨‍🔧 Community Video: Wooden Sofa & Screw Separation Tutorial",
        authorNameZh = "环保老王 (社区拆解达人)",
        authorNameEn = "Master Wang (Recycle Pro)",
        isAiGenerated = false,
        duration = "02:15",
        categoryTagZh = "家居用品",
        categoryTagEn = "Furniture",
        stepsZh = listOf(
            "拉开皮革与布艺包垫拉链，完整剥离高密度海绵",
            "使用电螺丝批与拔钉钳快速拆卸合页与防锈螺丝",
            "将橡木实木板材切割归拢打包，方便二次制作加工"
        ),
        stepsEn = listOf(
            "Unzip cushion covers and separate high-density foam",
            "Unscrew hinges and pull out iron nails with pliers",
            "Bundle solid oak wood for carpentry upcycling"
        ),
        safetyWarningZh = "⚠️ 注意防范防锈铁钉划伤，建议配备厚手套与护目镜。",
        safetyWarningEn = "⚠️ Beware of sharp iron nails. Wear heavy-duty gloves & goggles.",
        gradientColors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
    ),
    DismantleVideoTutorial(
        id = "vid_3",
        titleZh = "⚡ Gemini AI 拆解指南: 废旧冰箱/空调压缩机无害化铜线提取",
        titleEn = "⚡ Gemini AI Guide: Refrigerator Compressor & Copper Extraction",
        authorNameZh = "Gemini AI Studio",
        authorNameEn = "Gemini AI Studio",
        isAiGenerated = true,
        duration = "03:00",
        categoryTagZh = "家电拆解",
        categoryTagEn = "Appliances",
        stepsZh = listOf(
            "配合环保回收站抽氟设备完全抽取环保制冷剂",
            "切割电机外壳，使用拔线钳完整剥除高纯度纯铜漆包线圈",
            "铝合金散热片与钢铁外壳分道地磅称重"
        ),
        stepsEn = listOf(
            "Evacuate refrigerant gas via certified station tools",
            "Cut motor casing & extract high-purity copper coils",
            "Separate aluminum fins and steel chassis for weighing"
        ),
        safetyWarningZh = "⚠️ 制冷剂必须由持证站点抽氟，切勿私自割破冷凝管道泄漏。",
        safetyWarningEn = "⚠️ Refrigerants must be collected at certified stations to prevent leakage.",
        gradientColors = listOf(Color(0xFFE65100), Color(0xFFBF360C))
    ),
    DismantleVideoTutorial(
        id = "vid_4",
        titleZh = "✂️ DIY 社区短视频: 旧牛仔裤与闲置旧衣改造成时尚环保提袋",
        titleEn = "✂️ DIY Video: Upcycling Denim & Textiles into Eco Tote Bags",
        authorNameZh = "绿碳手作娘",
        authorNameEn = "Eco Handmade Girl",
        isAiGenerated = false,
        duration = "01:30",
        categoryTagZh = "纺织缝纫",
        categoryTagEn = "Textiles",
        stepsZh = listOf(
            "沿旧牛仔裤裤腿侧缝进行平整裁切",
            "使用包缝机或手工缝合底部，并缝入加固布艺手提带",
            "烫印绿碳减碳徽章，制作专属爱心环保购物袋"
        ),
        stepsEn = listOf(
            "Cut denim pants leg neatly along the side seam",
            "Stitch the bottom hem securely and attach cloth handles",
            "Iron on the green eco badge to finish custom tote"
        ),
        safetyWarningZh = "⚠️ 使用裁布剪刀与缝纫针时请注意手指安全。",
        safetyWarningEn = "⚠️ Keep fingers safe when using fabric shears and needles.",
        gradientColors = listOf(Color(0xFF8E24AA), Color(0xFF4A148C))
    )
)

@Composable
fun ForumScreen(
    posts: List<ForumPost>,
    lang: Language,
    onPostClick: (ForumPost) -> Unit,
    onPublishClick: () -> Unit,
    onLikeClick: (String) -> Unit = {},
    onHeartClick: (String) -> Unit = {},
    onHelpfulClick: (String) -> Unit = {},
    onAddComment: (ForumPost, String) -> Unit = { _, _ -> },
    sustainabilityEvents: List<com.example.data.model.SustainabilityEvent> = emptyList(),
    onJoinEvent: (String) -> Unit = {},
    onViewEventMap: (Double, Double) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var selectedTag by remember { mutableStateOf("全部") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSortTab by remember { mutableStateOf("MOST_HELPFUL") } // "MOST_HELPFUL", "RECENT", "TRENDING"
    var selectedTutorial by remember { mutableStateOf<DismantleVideoTutorial?>(null) }
    var selectedPostForDetail by remember { mutableStateOf<ForumPost?>(null) }

    val tags = if (lang == Language.ZH) {
        listOf("全部", "🌱 线下环保活动", "待回收", "视频展示", "经验分享", "废品价目", "站点推荐")
    } else {
        listOf("All", "🌱 Local Events", "Pending Recycle", "Video Guide", "Experience", "Price List", "Stations")
    }


    val filteredPosts = posts.filter { post ->
        val matchesTag = selectedTag == "全部" || selectedTag == "All" || post.getTags().contains(selectedTag) || post.title.contains(selectedTag)
        val matchesSearch = searchQuery.isBlank() ||
                post.title.contains(searchQuery, ignoreCase = true) ||
                post.content.contains(searchQuery, ignoreCase = true) ||
                post.authorName.contains(searchQuery, ignoreCase = true)
        matchesTag && matchesSearch
    }

    val sortedPosts = remember(filteredPosts, selectedSortTab) {
        filteredPosts.sortedWith { p1, p2 ->
            when (selectedSortTab) {
                "MOST_HELPFUL" -> p2.helpfulCount.compareTo(p1.helpfulCount)
                "RECENT" -> p2.timestamp.compareTo(p1.timestamp)
                "TRENDING" -> {
                    val score1 = p1.likesCount + p1.heartCount * 2 + p1.commentsCount * 3 + p1.helpfulCount * 2
                    val score2 = p2.likesCount + p2.heartCount * 2 + p2.commentsCount * 3 + p2.helpfulCount * 2
                    score2.compareTo(score1)
                }
                else -> 0
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            BilingualHeader(
                title = if (lang == Language.ZH) "待回收交流社区论坛" else "Recycle Community Forum"
            )

            // Requirement 1: Short Video Tutorials Carousel Component (AI-generated & Community Uploaded)
            DismantleVideoCarousel(
                tutorials = sampleDismantleTutorials,
                lang = lang,
                onTutorialClick = { tutorial ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedTutorial = tutorial
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Search Text Field Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (lang == Language.ZH) "🔍 搜索论坛帖子标题或技巧经验..." else "Search posts or recycling tips...",
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("forum_search_input")
            )

            // Tags Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                items(tags) { tag ->
                    val isSelected = tag == selectedTag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedTag = tag }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Content Discovery Sorting Filter Bar (Requirement 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("forum_sort_bar"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("MOST_HELPFUL", if (lang == Language.ZH) "⭐ 最有用" else "⭐ Most Helpful", "forum_sort_most_helpful"),
                    Triple("RECENT", if (lang == Language.ZH) "🕒 最新发布" else "🕒 Recent", "forum_sort_recent"),
                    Triple("TRENDING", if (lang == Language.ZH) "🔥 热门讨论" else "🔥 Trending", "forum_sort_trending")
                ).forEach { (sortKey, label, testTag) ->
                    val isSelected = selectedSortTab == sortKey
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedSortTab = sortKey
                            }
                            .testTag(testTag),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Specialized Zero Waste Tutorials Section (Admin Pinned)
                item {
                    ZeroWasteTutorialsSection(lang = lang)
                }

                // Sustainability Events Feed
                if (sustainabilityEvents.isNotEmpty() && (selectedTag.contains("全部") || selectedTag.contains("All") || selectedTag.contains("活动") || selectedTag.contains("Events"))) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SafetyCheck,
                                        contentDescription = "Events",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (lang == Language.ZH) "🌱 社区环保活动 & 捡跑工作坊" else "🌱 Sustainability Events & Workshops",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = if (lang == Language.ZH) "附近 ${sustainabilityEvents.size} 个活动" else "${sustainabilityEvents.size} nearby",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            sustainabilityEvents.forEach { ev ->
                                SustainabilityEventCard(
                                    lang = lang,
                                    event = ev,
                                    onJoinClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onJoinEvent(ev.id)
                                    },
                                    onMapClick = {
                                        onViewEventMap(ev.latitude, ev.longitude)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                if (filteredPosts.isEmpty()) {

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .testTag("forum_search_empty_state"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (lang == Language.ZH) "🔍 未找到与 \"$searchQuery\" 相关的交流动态" else "No posts found for \"$searchQuery\"",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (lang == Language.ZH) "尝试更换搜索词或选择其他标签" else "Try different keywords or tags",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                } else {
                    items(sortedPosts) { post ->
                        ForumPostCard(
                            post = post,
                            lang = lang,
                            onClick = {
                                selectedPostForDetail = post
                                onPostClick(post)
                            },
                            onLikeClick = { onLikeClick(post.id) },
                            onHeartClick = { onHeartClick(post.id) },
                            onHelpfulClick = { onHelpfulClick(post.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = onPublishClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .testTag("forum_publish_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Post Forum")
        }

        // Forum Post Detail & Comments Modal Dialog
        selectedPostForDetail?.let { activePost ->
            val livePost = posts.find { it.id == activePost.id } ?: activePost
            ForumPostDetailDialog(
                post = livePost,
                lang = lang,
                onDismiss = { selectedPostForDetail = null },
                onLikeClick = { onLikeClick(livePost.id) },
                onHeartClick = { onHeartClick(livePost.id) },
                onHelpfulClick = { onHelpfulClick(livePost.id) },
                onAddComment = { commentText ->
                    onAddComment(livePost, commentText)
                }
            )
        }

        // Dismantle Video Player Modal Dialog
        selectedTutorial?.let { tutorial ->
            DismantleVideoPlayerDialog(
                tutorial = tutorial,
                lang = lang,
                onDismiss = { selectedTutorial = null }
            )
        }
    }
}

@Composable
fun DismantleVideoCarousel(
    tutorials: List<DismantleVideoTutorial>,
    lang: Language,
    onTutorialClick: (DismantleVideoTutorial) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video Tutorials",
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (lang == Language.ZH) "🎥 物品回收拆解短视频教程" else "🎥 Recycling Dismantling Video Guides",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = if (lang == Language.ZH) "AI生成 & 社区达人" else "AI & Community",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE65100)
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("video_tutorials_carousel")
        ) {
            items(tutorials) { tutorial ->
                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .clickable { onTutorialClick(tutorial) }
                        .testTag("dismantle_video_card_${tutorial.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column {
                        // Video Thumbnail Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .background(Brush.linearGradient(tutorial.gradientColors))
                                .padding(8.dp)
                        ) {
                            // Top author tag
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (tutorial.isAiGenerated) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI",
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Gemini AI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User",
                                        tint = Color(0xFF81C784),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (lang == Language.ZH) "社区达人" else "Community",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Center Play Icon Button
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .align(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Duration badge bottom right
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Text(
                                    text = tutorial.duration,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Video Title & Category Footer
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (lang == Language.ZH) tutorial.titleZh else tutorial.titleEn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (lang == Language.ZH) tutorial.categoryTagZh else tutorial.categoryTagEn,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.primary,
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
}

@Composable
fun DismantleVideoPlayerDialog(
    tutorial: DismantleVideoTutorial,
    lang: Language,
    onDismiss: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0.45f) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().testTag("video_player_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == Language.ZH) tutorial.titleZh else tutorial.titleEn,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Simulated Video Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(tutorial.gradientColors))
                        .clickable { isPlaying = !isPlaying },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isPlaying) (if (lang == Language.ZH) "▶️ 正在播放拆解短视频教程" else "▶️ Playing Video...") else (if (lang == Language.ZH) "⏸️ 已暂停" else "⏸️ Paused"),
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Bottom Player Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { if (isPlaying) 0.65f else 0.45f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF00E676),
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (tutorial.isAiGenerated) "🤖 Gemini AI Vision 智能推导" else "👨‍🔧 社区达人实拍",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${tutorial.duration} / HD 1080P",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Key Dismantling Steps List
                Text(
                    text = if (lang == Language.ZH) "📋 核心拆解分选步骤:" else "📋 Key Dismantling Steps:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                val steps = if (lang == Language.ZH) tutorial.stepsZh else tutorial.stepsEn
                steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(step, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Safety Warning Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == Language.ZH) tutorial.safetyWarningZh else tutorial.safetyWarningEn,
                            fontSize = 11.sp,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val context = LocalContext.current
                OutlinedButton(
                    onClick = {
                        val title = if (lang == Language.ZH) tutorial.titleZh else tutorial.titleEn
                        val author = if (lang == Language.ZH) tutorial.authorNameZh else tutorial.authorNameEn
                        val steps = if (lang == Language.ZH) tutorial.stepsZh else tutorial.stepsEn
                        ShareUtils.shareRecyclingTip(context, title, author, steps, lang)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("share_tutorial_btn_${tutorial.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share Tip", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (lang == Language.ZH) "原生社交分享此拆解妙招 / Share Tip" else "Share Recycling Tip", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (lang == Language.ZH) "我已掌握拆解技巧 (+15绿碳积分)" else "Mastered Technique (+15 Points)")
                }
            }
        }
    }
}

@Composable
fun ForumPostCard(
    post: ForumPost,
    lang: Language,
    onClick: () -> Unit,
    onLikeClick: () -> Unit = {},
    onHeartClick: () -> Unit = {},
    onHelpfulClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("forum_card_${post.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Author info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = post.authorName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (lang == Language.ZH) "社区活跃回收用户" else "Active Recycler",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = post.content,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )

            if (post.videoUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video Tag",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (lang == Language.ZH) "附带实拍短视频" else "Contains Video",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reaction Bar: Like, Heart, Helpful, Comment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Like Button (👍)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (post.isLiked) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { onLikeClick() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("reaction_like_btn_${post.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                            contentDescription = "Like",
                            tint = if (post.isLiked) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.likesCount}",
                            fontSize = 11.sp,
                            fontWeight = if (post.isLiked) FontWeight.Bold else FontWeight.Normal,
                            color = if (post.isLiked) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    // Heart Button (❤️)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (post.isHearted) Color.Red.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { onHeartClick() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("reaction_heart_btn_${post.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (post.isHearted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Heart",
                            tint = if (post.isHearted) Color.Red else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.heartCount}",
                            fontSize = 11.sp,
                            fontWeight = if (post.isHearted) FontWeight.Bold else FontWeight.Normal,
                            color = if (post.isHearted) Color.Red else Color.Gray
                        )
                    }

                    // Helpful Button (💡)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (post.isHelpful) Color(0xFFE65100).copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { onHelpfulClick() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("reaction_helpful_btn_${post.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Helpful",
                            tint = if (post.isHelpful) Color(0xFFE65100) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lang == Language.ZH) "实用 ${post.helpfulCount}" else "Helpful ${post.helpfulCount}",
                            fontSize = 11.sp,
                            fontWeight = if (post.isHelpful) FontWeight.Bold else FontWeight.Normal,
                            color = if (post.isHelpful) Color(0xFFE65100) else Color.Gray
                        )
                    }
                }

                // Comment Count & Share Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.clickable { onClick() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.commentsCount}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            ShareUtils.shareForumPost(
                                context = context,
                                title = post.title,
                                authorName = post.authorName,
                                content = post.content,
                                lang = lang
                            )
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("forum_share_btn_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ForumPostDetailDialog(
    post: ForumPost,
    lang: Language,
    onDismiss: () -> Unit,
    onLikeClick: () -> Unit,
    onHeartClick: () -> Unit,
    onHelpfulClick: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var commentInput by remember { mutableStateOf("") }
    val sampleComments = remember {
        mutableListOf(
            "非常有参考价值！今天试了按图拆解确实很快，感谢分享！",
            "建议大家上门称重前提前把PET和铝罐分好，站点师傅非常效率！",
            "请问朝阳站点的联系电话是多少呢？也想预约大件家门回收。"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("forum_post_detail_dialog")
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
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(post.authorName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(if (lang == Language.ZH) "社区达人" else "Community Member", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(post.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Text(post.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Reactions Row in Dialog
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Like Button (👍)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (post.isLiked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { onLikeClick() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("reaction_like_btn_${post.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (post.isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                                contentDescription = "Like",
                                tint = if (post.isLiked) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("赞 ${post.likesCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Heart Button (❤️)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (post.isHearted) Color.Red.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { onHeartClick() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("reaction_heart_btn_${post.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (post.isHearted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Heart",
                                tint = if (post.isHearted) Color.Red else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("爱心 ${post.heartCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Helpful Button (💡)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (post.isHelpful) Color(0xFFE65100).copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { onHelpfulClick() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("reaction_helpful_btn_${post.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Helpful",
                                tint = if (post.isHelpful) Color(0xFFE65100) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("实用 ${post.helpfulCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Share Button (📢)
                        val context = LocalContext.current
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .clickable {
                                    ShareUtils.shareForumPost(
                                        context = context,
                                        title = post.title,
                                        authorName = post.authorName,
                                        content = post.content,
                                        lang = lang
                                    )
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("reaction_share_btn_${post.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("分享", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("💬 社区交流评论区 (${post.commentsCount + sampleComments.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                // Comments List
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    sampleComments.take(3).forEach { c ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                            Text(c, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input & Send button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        placeholder = { Text(if (lang == Language.ZH) "发表您的交流想法..." else "Write a reply...", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("forum_comment_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (commentInput.isNotBlank()) {
                                sampleComments.add(0, commentInput)
                                onAddComment(commentInput)
                                commentInput = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("forum_comment_send_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SustainabilityEventCard(
    lang: Language,
    event: com.example.data.model.SustainabilityEvent,
    onJoinClick: () -> Unit,
    onMapClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sustainability_event_card_${event.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "📍 ${event.distanceKm} km · ${event.locationName(lang)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = event.dateText(lang),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.title(lang),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = event.description(lang),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == Language.ZH) "主办: ${event.organizer(lang)} (${event.currentAttendees}/${event.maxAttendees}人)"
                           else "By: ${event.organizer(lang)} (${event.currentAttendees}/${event.maxAttendees})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onMapClick,
                        modifier = Modifier.size(32.dp).testTag("event_map_btn_${event.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Map Proximity",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Button(
                        onClick = onJoinClick,
                        modifier = Modifier.height(32.dp).testTag("join_event_btn_${event.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = if (event.isJoined) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                 else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = if (event.isJoined) (if (lang == Language.ZH) "✅ 已报名" else "✅ Joined")
                                   else (if (lang == Language.ZH) "🙋 立即报名" else "🙋 Join Event"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (event.isJoined) MaterialTheme.colorScheme.onSecondaryContainer else Color.White
                        )
                    }
                }
            }
        }
    }
}

