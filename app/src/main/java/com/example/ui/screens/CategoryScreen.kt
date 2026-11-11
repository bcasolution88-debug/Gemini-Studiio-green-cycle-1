package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.CategoryRepository
import com.example.data.model.Language
import com.example.data.model.MajorCategory
import com.example.data.model.SubCategory
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import com.example.ui.components.AiRecyclingAssistantDialog
import com.example.ui.components.MaterialsEncyclopediaDialog
import com.example.ui.components.BilingualHeader
import com.example.ui.components.CategoryTreeView
import com.example.ui.components.DisposalGuidelinesDialog
import com.example.ui.components.SearchHistoryView
import com.example.util.SearchHistoryManager

@Composable
fun CategoryScreen(
    lang: Language,
    onSubCategoryClick: (MajorCategory, SubCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var historyList by remember(lang) { mutableStateOf(SearchHistoryManager.getSearchHistory(context, lang)) }
    var selectedMajorCategory by remember { mutableStateOf(CategoryRepository.majorCategories.first()) }
    var selectedSubCategory by remember { mutableStateOf<SubCategory?>(null) }
    var isTreeViewMode by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var activeDisposalGuidelineSubCategory by remember { mutableStateOf<Pair<MajorCategory, SubCategory>?>(null) }
    var showAiAssistantDialog by remember { mutableStateOf(false) }
    var showMaterialsEncyclopediaDialog by remember { mutableStateOf(false) }

    // Find all subcategories matching search query across all major categories
    val matchedSuggestions = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val results = mutableListOf<Pair<MajorCategory, SubCategory>>()
            CategoryRepository.majorCategories.forEach { major ->
                major.subCategories.forEach { sub ->
                    if (sub.nameZh.contains(searchQuery, ignoreCase = true) ||
                        sub.nameEn.contains(searchQuery, ignoreCase = true) ||
                        major.nameZh.contains(searchQuery, ignoreCase = true) ||
                        major.nameEn.contains(searchQuery, ignoreCase = true) ||
                        (sub.wasteMarketRate?.contains(searchQuery, ignoreCase = true) == true)
                    ) {
                        results.add(Pair(major, sub))
                    }
                }
            }
            results
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BilingualHeader(
            title = if (lang == Language.ZH) "全品类废品与旧物大纲" else "Categories Directory"
        )

        // Gemini AI Assistant & Materials Encyclopedia Quick Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // AI Recycling Assistant Button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showAiAssistantDialog = true }
                    .testTag("ai_recycling_assistant_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Recycling Assistant",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (lang == Language.ZH) "🤖 AI智能问答" else "🤖 AI Assistant",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (lang == Language.ZH) "查物品能否回收/怎么扔" else "Ask how to dispose",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Encyclopedia of Materials Button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showMaterialsEncyclopediaDialog = true }
                    .testTag("materials_encyclopedia_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Encyclopedia of Materials",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (lang == Language.ZH) "📚 材料拆解百科" else "📚 Encyclopedia",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = if (lang == Language.ZH) "塑料/电池/旧衣加工流程" else "Materials & Recycling",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Real-Time Category & Material Search Bar with Disposal Suggestions
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = if (lang == Language.ZH) "🔍 搜索物品或材质 (如: PET瓶/纸箱/铜/家电) 查指南" else "Search waste category or material guidelines...",
                    fontSize = 11.sp
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("category_search_input")
        )

        // Search History & Quick Recommendations (When Search Input is Empty)
        if (searchQuery.isBlank()) {
            SearchHistoryView(
                historyList = historyList,
                lang = lang,
                onQuerySelect = { query ->
                    searchQuery = query
                    historyList = SearchHistoryManager.addSearchQuery(context, query, lang)
                },
                onQueryRemove = { query ->
                    historyList = SearchHistoryManager.removeSearchQuery(context, query, lang)
                },
                onClearAllHistory = {
                    historyList = SearchHistoryManager.clearSearchHistory(context)
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Real-Time Suggestions Panel Overlay when user types
        if (searchQuery.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("category_suggestions_panel"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (lang == Language.ZH) "💡 实时材质匹配与处置建议 (${matchedSuggestions.size})" else "Matched Disposal Guidelines (${matchedSuggestions.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (matchedSuggestions.isEmpty()) {
                        Text(
                            text = if (lang == Language.ZH) "未找到匹配项，可直接在发布页选择通用旧物回收。" else "No matching category guidelines found.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(140.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(matchedSuggestions) { (major, sub) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable {
                                            selectedMajorCategory = major
                                            selectedSubCategory = sub
                                            activeDisposalGuidelineSubCategory = Pair(major, sub)
                                            historyList = SearchHistoryManager.addSearchQuery(context, sub.name(lang), lang)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("suggestion_item_${sub.id}"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Recycling, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(sub.name(lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("[${major.name(lang)}]", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (sub.wasteMarketRate != null) {
                                            Text(sub.wasteMarketRate, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Icon(Icons.Default.Info, contentDescription = "Guidelines", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // View Mode Switcher Header: TreeView vs Split Grid View
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                // TreeView Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isTreeViewMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isTreeViewMode = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("toggle_tree_view")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = "Tree View",
                            tint = if (isTreeViewMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lang == Language.ZH) "树状层级 TreeView" else "Hierarchy TreeView",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTreeViewMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Grid View Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (!isTreeViewMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isTreeViewMode = false }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("toggle_grid_view")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Grid View",
                            tint = if (!isTreeViewMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lang == Language.ZH) "侧栏网格 Split View" else "Split Grid View",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isTreeViewMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Category Counter Indicator
            Text(
                text = "${CategoryRepository.majorCategories.size} 大类 / ${CategoryRepository.majorCategories.sumOf { it.subCategories.size }} 子类",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isTreeViewMode) {
            // Reusable TreeView Component
            CategoryTreeView(
                categories = CategoryRepository.majorCategories,
                lang = lang,
                selectedMajorId = selectedMajorCategory.id,
                selectedSubId = selectedSubCategory?.id,
                onCategorySelected = { major, sub ->
                    selectedMajorCategory = major
                    selectedSubCategory = sub
                    if (sub != null) {
                        activeDisposalGuidelineSubCategory = Pair(major, sub)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Major Category Sidebar
                LazyColumn(
                    modifier = Modifier
                        .width(110.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    items(CategoryRepository.majorCategories) { major ->
                        val isSelected = major.id == selectedMajorCategory.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMajorCategory = major }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                                )
                                .padding(vertical = 16.dp, horizontal = 8.dp)
                                .testTag("major_cat_${major.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = major.name(lang),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Right Subcategories Grid & Information
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    // Category Banner Description
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedMajorCategory.name(lang),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedMajorCategory.description(lang),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            if (selectedMajorCategory.requiresSpecialLogistics) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = Color(0xFFED6C02),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (lang == Language.ZH) "标注: 超大型物品，安排特殊重卡与拆卸专员" else "Tag: Over-sized freight required",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFED6C02)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (lang == Language.ZH) "二级细分品类 (点击查看处置指南)" else "Subcategories (Click for guidelines)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(selectedMajorCategory.subCategories) { sub ->
                            Card(
                                modifier = Modifier
                                    .clickable {
                                        activeDisposalGuidelineSubCategory = Pair(selectedMajorCategory, sub)
                                    }
                                    .testTag("sub_cat_${sub.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Chair,
                                            contentDescription = sub.name(lang),
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = sub.name(lang),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (sub.isWasteRecycleRateAvailable && sub.wasteMarketRate != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = sub.wasteMarketRate,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
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

        // Disposal Guidelines Modal Dialog
        activeDisposalGuidelineSubCategory?.let { (major, sub) ->
            DisposalGuidelinesDialog(
                majorCategory = major,
                subCategory = sub,
                lang = lang,
                onDismiss = { activeDisposalGuidelineSubCategory = null },
                onPublishCategoryRequest = { selectedMajor, selectedSub ->
                    onSubCategoryClick(selectedMajor, selectedSub)
                }
            )
        }

        if (showAiAssistantDialog) {
            AiRecyclingAssistantDialog(
                lang = lang,
                onDismiss = { showAiAssistantDialog = false }
            )
        }

        if (showMaterialsEncyclopediaDialog) {
            MaterialsEncyclopediaDialog(
                lang = lang,
                onDismiss = { showMaterialsEncyclopediaDialog = false }
            )
        }
    }
}
