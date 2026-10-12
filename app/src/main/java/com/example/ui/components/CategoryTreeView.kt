package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.model.Language
import com.example.data.model.MajorCategory
import com.example.data.model.SubCategory

/**
 * Reusable TreeView component representing hierarchical category lists (Major -> Sub)
 * with expandable/collapsible nodes, indentation, and navigation state callbacks.
 */
@Composable
fun CategoryTreeView(
    categories: List<MajorCategory>,
    lang: Language,
    selectedMajorId: String?,
    selectedSubId: String?,
    onCategorySelected: (MajorCategory, SubCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Keep track of expanded states for major categories
    val expandedStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            // Expand first major category or selected one by default
            categories.forEach { cat ->
                put(cat.id, cat.id == (selectedMajorId ?: categories.firstOrNull()?.id))
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories, key = { it.id }) { major ->
            val isExpanded = expandedStates[major.id] ?: false
            val isMajorSelected = selectedMajorId == major.id && selectedSubId == null

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tree_card_${major.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMajorSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isMajorSelected) 2.dp else 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Major Category Tree Parent Node
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedStates[major.id] = !isExpanded
                                onCategorySelected(major, null)
                            }
                            .padding(12.dp)
                            .testTag("tree_node_major_${major.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Expand/Collapse Chevron Indicator
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Category Folder Icon
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Category Title & Count
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = major.name(lang),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMajorSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )

                                if (major.requiresSpecialLogistics) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = "Logistics",
                                        tint = Color(0xFFED6C02),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Text(
                                text = major.description(lang),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        // Subcategories Badge Count
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${major.subCategories.size}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Hierarchical Children Nodes (Subcategories)
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                .padding(start = 28.dp, end = 12.dp, top = 4.dp, bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            major.subCategories.forEach { sub ->
                                val isSubSelected = selectedMajorId == major.id && selectedSubId == sub.id

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSubSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable {
                                            onCategorySelected(major, sub)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                        .testTag("tree_node_sub_${sub.id}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Indentation Arrow Icon
                                    Icon(
                                        imageVector = Icons.Default.SubdirectoryArrowRight,
                                        contentDescription = null,
                                        tint = if (isSubSelected) Color.White else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = sub.name(lang),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSubSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Scrap market rate tag if available
                                    if (sub.isWasteRecycleRateAvailable && sub.wasteMarketRate != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (isSubSelected) Color.White.copy(alpha = 0.2f)
                                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = sub.wasteMarketRate,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSubSelected) Color.White else MaterialTheme.colorScheme.primary
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
    }
}
