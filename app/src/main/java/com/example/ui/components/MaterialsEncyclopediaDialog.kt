package com.example.ui.components

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Language

data class MaterialEncyclopediaEntry(
    val id: String,
    val nameZh: String,
    val nameEn: String,
    val symbolCode: String, // e.g. "#1 PET", "E-Waste", "Textiles"
    val icon: ImageVector,
    val themeColor: Color,
    val processingWorkflowZh: List<String>,
    val processingWorkflowEn: List<String>,
    val commonExamplesZh: String,
    val commonExamplesEn: String,
    val recyclingAccuracyTipsZh: String,
    val recyclingAccuracyTipsEn: String,
    val contaminationWarningZh: String,
    val contaminationWarningEn: String
)

object MaterialEncyclopediaData {
    val entries = listOf(
        MaterialEncyclopediaEntry(
            id = "pet_plastic",
            nameZh = "PET 聚酯塑料 (#1)",
            nameEn = "PET Plastics (#1)",
            symbolCode = "PET ♻️1",
            icon = Icons.Default.Recycling,
            themeColor = Color(0xFF1976D2),
            processingWorkflowZh = listOf(
                "1. 智能光谱分选: 利用红外光电离选机将PET瓶与PP/HDPE区分开。",
                "2. 剥离与破碎: 高速撕碎机切割成0.5-1.0厘米薄片，清洗沉浮分离瓶盖。",
                "3. 高温碱洗脱胶: 除去油污与贴纸残胶，经气流提纯达到食品级粒料标准。"
            ),
            processingWorkflowEn = listOf(
                "1. NIR Optical Sorting: Near-infrared sensors separate PET from PP/HDPE.",
                "2. Shredding & Flotation: Shredded into flakes; sink-float tank removes caps.",
                "3. Hot Caustic Washing: Removes adhesive and residues to achieve bottle-to-bottle food grade."
            ),
            commonExamplesZh = "矿泉水瓶、碳酸饮料瓶、洗发水瓶、透明食品包装盒",
            commonExamplesEn = "Water bottles, soda bottles, clear food containers",
            recyclingAccuracyTipsZh = "倒空液体后用力压扁并关紧瓶盖，可显著节省储存与托运容积！",
            recyclingAccuracyTipsEn = "Empty liquid residue, crush flat, and re-tighten the cap to optimize freight volume.",
            contaminationWarningZh = "沾染大量食用油或油漆的瓶体不可直接混入，需先用温水冲冼。",
            contaminationWarningEn = "Heavy oil or paint contamination requires washing before sorting."
        ),
        MaterialEncyclopediaEntry(
            id = "textiles",
            nameZh = "旧衣与旧纺织品",
            nameEn = "Textiles & Garments",
            symbolCode = "TEX 👕",
            icon = Icons.Default.ShoppingBag,
            themeColor = Color(0xFFE91E63),
            processingWorkflowZh = listOf(
                "1. 纤维成分检测: 红外光谱鉴别纯棉、涤纶、羊毛及混纺比例。",
                "2. 开松与开纤: 金属扣拉链物理剔除后，进入高速针板开松成初级棉纤维。",
                "3. 隔音与绝热熔喷: 用于汽车隔音棉、建筑保温板或再生成环保冲锋衣线材。"
            ),
            processingWorkflowEn = listOf(
                "1. Fiber Analysis: NIR spectroscopy identifies cotton, polyester, wool & blends.",
                "2. De-buttoning & Shredding: Metal zips removed; shredded into raw fluff.",
                "3. Upcycling & Thermal Bonding: Downcycled into insulation pads or spun into recycled yarn."
            ),
            commonExamplesZh = "棉衣、牛仔裤、床单被套、毛绒玩具、窗帘布艺",
            commonExamplesEn = "Shirts, denim jeans, bedsheets, plush toys, curtains",
            recyclingAccuracyTipsZh = "保持干爽无霉变，请勿与湿垃圾混放；鞋子需双双捆扎在一起。",
            recyclingAccuracyTipsEn = "Keep dry; tie pairs of shoes together securely.",
            contaminationWarningZh = "湿透发霉或受剧毒化学品污染的破布属于干垃圾，无法纤维化再生。",
            contaminationWarningEn = "Mildewed or toxic-chemically stained fabrics cannot be recycled."
        ),
        MaterialEncyclopediaEntry(
            id = "lithium_batteries",
            nameZh = "锂电池与电子废弃物",
            nameEn = "Batteries & E-Waste",
            symbolCode = "BAT 🔋",
            icon = Icons.Default.BatteryChargingFull,
            themeColor = Color(0xFFD32F2F),
            processingWorkflowZh = listOf(
                "1. 惰性气体带电破碎: 在氮气保护下进行无火花撕碎，收集电解液气化相。",
                "2. 黑色极粉热解萃取: 分选得出富含钴、锂、镍、锰的高纯黑粉料（Black Mass）。",
                "3. 湿法冶金提纯: 酸浸提炼高纯电池级碳酸锂，循环用于新动力电池生产。"
            ),
            processingWorkflowEn = listOf(
                "1. Nitrogen Shredding: Shredded under nitrogen blanket to prevent fires.",
                "2. Black Mass Recovery: Separates high-value Cobalt, Lithium, Nickel & Manganese powder.",
                "3. Hydrometallurgy: Acid leaching yields battery-grade Lithium Carbonate."
            ),
            commonExamplesZh = "手机电池、手持充电宝、电动牙刷、笔记本电脑锂电池、电动车蓄电池",
            commonExamplesEn = "Phone batteries, power banks, electric toothbrushes, laptop batteries",
            recyclingAccuracyTipsZh = "用透明绝缘胶布贴住暴露接线端子，防止运输中剧烈碰撞短路起火！",
            recyclingAccuracyTipsEn = "Tape battery terminals with clear electrical tape to eliminate short-circuit risks.",
            contaminationWarningZh = "严禁敲击、刺穿或丢入普通垃圾桶，必须投放至专用的危险有害箱或预约回收。",
            contaminationWarningEn = "NEVER crush, puncture or toss into general waste bin!"
        ),
        MaterialEncyclopediaEntry(
            id = "precious_metals",
            nameZh = "废金属（铜、铝、钢铁）",
            nameEn = "Scrap Metals (Copper/Alum)",
            symbolCode = "MET ⚙️",
            icon = Icons.Default.PrecisionManufacturing,
            themeColor = Color(0xFFED6C02),
            processingWorkflowZh = listOf(
                "1. 涡电流与磁选 separation: 永磁滚筒吸附钢铁，涡电流分选机剥离铝合金与铜线。",
                "2. 高温熔炼提纯: 铝罐熔炼节省95%原铝电解能耗；废铜经电解精炼达到99.99%高纯铜。",
                "3. 连续铸造轧制: 铸造成标准化铝锭、钢坯及铜线杆，重新进入高端制造业。"
            ),
            processingWorkflowEn = listOf(
                "1. Eddy Current & Magnetic Separation: Separates ferrous iron from non-ferrous alum/copper.",
                "2. Smelting: Recycling alum saves 95% energy vs virgin mining; copper electro-refined.",
                "3. Continuous Casting: Ingot/billet casting for industrial manufacturing."
            ),
            commonExamplesZh = "易拉罐、不锈钢厨具、电线废铜、铝合金门窗、铁锅家具",
            commonExamplesEn = "Beverage cans, stainless pots, copper cables, aluminum frames",
            recyclingAccuracyTipsZh = "易拉罐尽量踩扁；电线无需自行剥皮，回收站有自动剥线机提高回收价值。",
            recyclingAccuracyTipsEn = "Crush cans flat. No need to strip wire coats manually; automated machinery handles it.",
            contaminationWarningZh = "含铅、水银等有毒金属气罐（如喷漆罐）需先完全泄压穿孔后方可投递。",
            contaminationWarningEn = "Aerosol cans containing toxic paint require complete pressure release before recycling."
        ),
        MaterialEncyclopediaEntry(
            id = "paper_cardboard",
            nameZh = "废纸板与纸张",
            nameEn = "Paper & Cardboard",
            symbolCode = "PAP 📦",
            icon = Icons.Default.Book,
            themeColor = Color(0xFF388E3C),
            processingWorkflowZh = listOf(
                "1. 碎浆与除渣: 废纸投入高浓水碎浆机，离心脱除塑胶封胶与钉子。",
                "2. 浮选脱墨: 注入微气泡带走油墨颗粒，漂白提高纸浆白度。",
                "3. 抄造造纸: 抄纸网压榨脱水烘干，重新压制成高强度箱板纸与包装纸。"
            ),
            processingWorkflowEn = listOf(
                "1. Hydropulping: Disintegrated with water; centrifugal cleaners remove plastic tape & staples.",
                "2. Flotation De-inking: Microbubbles bind to ink particles to lift them away.",
                "3. Paper Machine Rolling: Pressed, dried, and rolled into sturdy containerboard corrugated paper."
            ),
            commonExamplesZh = "瓦楞纸箱、快递盒、书籍报纸、办公A4纸、牛奶纸盒(利乐包)",
            commonExamplesEn = "Corrugated boxes, express packages, books, office paper, Tetra Pak cartons",
            recyclingAccuracyTipsZh = "拆开箱子撕掉封箱胶带并打平打包，利乐包需剪开洗净烫平。",
            recyclingAccuracyTipsEn = "Flatten cardboard boxes, remove plastic tape, and rinse Tetra Pak cartons.",
            contaminationWarningZh = "吸饱油污的披萨盒、卫生间用纸及复写纸不可用于再生纸浆生产。",
            contaminationWarningEn = "Greasy pizza boxes and used tissue papers degrade pulp and must be disposed as organic waste."
        )
    )
}

@Composable
fun MaterialsEncyclopediaDialog(
    lang: Language,
    onDismiss: () -> Unit
) {
    var selectedEntry by remember { mutableStateOf(MaterialEncyclopediaData.entries.first()) }
    var filterQuery by remember { mutableStateOf("") }

    val filteredEntries = remember(filterQuery) {
        if (filterQuery.isBlank()) MaterialEncyclopediaData.entries
        else {
            MaterialEncyclopediaData.entries.filter {
                it.nameZh.contains(filterQuery, ignoreCase = true) ||
                it.nameEn.contains(filterQuery, ignoreCase = true) ||
                it.symbolCode.contains(filterQuery, ignoreCase = true) ||
                it.commonExamplesZh.contains(filterQuery, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("materials_encyclopedia_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (lang == Language.ZH) "📚 材料拆解与回收百科全书" else "Encyclopedia of Recycling Materials",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (lang == Language.ZH) "工业级加工流程 • 投放精准度指南" else "Industrial Workflows & Accuracy Guidelines",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_encyclopedia_dialog")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Filter search bar
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = {
                        Text(
                            if (lang == Language.ZH) "🔍 搜索材质名称/标志/示例 (如: PET, 锂电池, 铜)..." else "Search material name or code...",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("encyclopedia_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Left Side: Material Categories List
                    LazyColumn(
                        modifier = Modifier
                            .width(130.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        items(filteredEntries) { entry ->
                            val isSelected = entry.id == selectedEntry.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedEntry = entry }
                                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                                    .padding(vertical = 12.dp, horizontal = 10.dp)
                                    .testTag("encyclopedia_item_${entry.id}"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = entry.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) entry.themeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = entry.symbolCode,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) entry.themeColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (lang == Language.ZH) entry.nameZh else entry.nameEn,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Right Side: Material Detail Deep-Dive
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Title & Badge Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = selectedEntry.themeColor.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = selectedEntry.icon,
                                            contentDescription = null,
                                            tint = selectedEntry.themeColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (lang == Language.ZH) selectedEntry.nameZh else selectedEntry.nameEn,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = selectedEntry.themeColor
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(selectedEntry.themeColor)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = selectedEntry.symbolCode,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (lang == Language.ZH) "常见实例: ${selectedEntry.commonExamplesZh}" else "Examples: ${selectedEntry.commonExamplesEn}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Industrial Workflow Section
                        Text(
                            text = if (lang == Language.ZH) "⚙️ 工业级无害化拆解与再生全流程" else "⚙️ Industrial Processing Workflow",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val workflowList = if (lang == Language.ZH) selectedEntry.processingWorkflowZh else selectedEntry.processingWorkflowEn
                        workflowList.forEach { step ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = step,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Recycling Accuracy Tips
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (lang == Language.ZH) "🎯 用户投递精准度提升技巧" else "🎯 Accuracy Tips",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (lang == Language.ZH) selectedEntry.recyclingAccuracyTipsZh else selectedEntry.recyclingAccuracyTipsEn,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Contamination Warning
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (lang == Language.ZH) "⚠️ 污染风险与禁投提醒" else "⚠️ Contamination Warning",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (lang == Language.ZH) selectedEntry.contaminationWarningZh else selectedEntry.contaminationWarningEn,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB71C1C)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
