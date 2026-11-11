package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.Language
import com.example.data.model.RecycleStation
import com.example.ui.components.BilingualHeader
import com.example.ui.components.StationAdminChatDialog
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

enum class MapPointType {
    RECYCLING_STATION,
    DONATION_DROP_OFF,
    PUBLIC_RECYCLING_BIN
}

enum class MapScreenMode {
    MAP,
    LIST,
    AR
}

data class MapPoint(
    val id: String,
    val name: String,
    val type: MapPointType,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val distanceKm: Double,
    val rating: Double,
    val verifiedBadge: String,
    val hours: String,
    val acceptedItems: List<String>,
    val phone: String?,
    val stationRef: RecycleStation? = null
)

@Composable
fun StationMapScreen(
    stations: List<RecycleStation>,
    lang: Language,
    onStationClick: (RecycleStation) -> Unit,
    onRegisterStationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mapScreenMode by remember { mutableStateOf(MapScreenMode.MAP) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPointFilter by remember { mutableStateOf<MapPointType?>(null) } // null = All
    var selectedSpecialty by remember { mutableStateOf("All") }
    var isSidebarOpen by remember { mutableStateOf(false) }
    var isLocationPermissionGranted by remember { mutableStateOf(false) }
    var locationToastMessage by remember { mutableStateOf<String?>(null) }
    var activeChatPoint by remember { mutableStateOf<MapPoint?>(null) }

    if (activeChatPoint != null) {
        val pt = activeChatPoint!!
        StationAdminChatDialog(
            stationName = pt.name,
            stationPhone = pt.phone,
            lang = lang,
            onDismiss = { activeChatPoint = null }
        )
    }

    // Aggregate map points: Commercial Stations + Community-Verified Donation Drop-off Points + Public Recycling Bins
    val allMapPoints = remember(stations) {
        val points = mutableListOf<MapPoint>()

        // 1. Commercial Recycling Stations
        stations.forEach { sta ->
            points.add(
                MapPoint(
                    id = "sta_${sta.id}",
                    name = sta.name,
                    type = MapPointType.RECYCLING_STATION,
                    latitude = sta.latitude,
                    longitude = sta.longitude,
                    address = sta.address,
                    distanceKm = sta.distanceKm.toDouble(),
                    rating = sta.rating.toDouble(),
                    verifiedBadge = if (lang == Language.ZH) "⭐ 官方备案站点" else "⭐ Certified Station",
                    hours = sta.businessHours,
                    acceptedItems = sta.getServiceCategories(),
                    phone = sta.phone,
                    stationRef = sta
                )
            )
        }

        // 2. Community-Verified Donation Drop-off Locations (Requirement #2)
        points.add(
            MapPoint(
                id = "donation_1",
                name = if (lang == Language.ZH) "❤️ 三里屯社区爱心捐赠接力站" else "Sanlitun Community Charity Drop-off",
                type = MapPointType.DONATION_DROP_OFF,
                latitude = 39.9150,
                longitude = 116.4850,
                address = "北京市朝阳区三里屯社区服务中心东侧",
                distanceKm = 0.8,
                rating = 4.9,
                verifiedBadge = if (lang == Language.ZH) "✅ 社区认证捐赠点" else "✅ Verified Drop-off Point",
                hours = "08:30 - 20:00 (全天有人值守)",
                acceptedItems = listOf("冬衣羽绒服", "教辅图书", "童鞋文具", "小型家电"),
                phone = "010-85123990"
            )
        )
        points.add(
            MapPoint(
                id = "donation_2",
                name = if (lang == Language.ZH) "❤️ 望京绿色共享公益集散仓" else "Wangjing Green Charity Warehouse",
                type = MapPointType.DONATION_DROP_OFF,
                latitude = 39.9980,
                longitude = 116.4700,
                address = "北京市朝阳区望京SOHO塔2底商106室",
                distanceKm = 2.4,
                rating = 4.8,
                verifiedBadge = if (lang == Language.ZH) "✅ 志愿者先锋点" else "✅ Volunteer Hub",
                hours = "09:00 - 21:00",
                acceptedItems = listOf("二手家具", "儿童玩具", "各类闲置书籍"),
                phone = "010-64789012"
            )
        )

        // 3. Public Recycling Bins (Requirement #2)
        points.add(
            MapPoint(
                id = "bin_1",
                name = if (lang == Language.ZH) "🗑️ 24H智能分类回收柜 (朝阳门分柜)" else "24H Smart Recycling Bin (Chaoyangmen)",
                type = MapPointType.PUBLIC_RECYCLING_BIN,
                latitude = 39.9200,
                longitude = 116.4380,
                address = "朝阳门外大街16号公共绿地广场",
                distanceKm = 1.1,
                rating = 4.7,
                verifiedBadge = if (lang == Language.ZH) "⚡ 24H扫码投递" else "⚡ 24H Smart Bin",
                hours = "24小时自助开放",
                acceptedItems = listOf("塑料瓶", "废纸箱", "易拉罐", "玻璃瓶"),
                phone = null
            )
        )
        points.add(
            MapPoint(
                id = "bin_2",
                name = if (lang == Language.ZH) "🗑️ 便民旧衣与织物智能投放箱 (CBD中心区)" else "Textile & Clothing Bin (CBD)",
                type = MapPointType.PUBLIC_RECYCLING_BIN,
                latitude = 39.9050,
                longitude = 116.4600,
                address = "国贸大厦B座后侧社区公共投递区",
                distanceKm = 1.6,
                rating = 4.6,
                verifiedBadge = if (lang == Language.ZH) "⚡ 公益积分投递箱" else "⚡ Eco Credit Bin",
                hours = "24小时自助开放",
                acceptedItems = listOf("废旧纺织品", "鞋帽", "废旧电池"),
                phone = null
            )
        )

        points
    }

    // Selected point state
    var selectedPoint by remember(allMapPoints) { mutableStateOf(allMapPoints.firstOrNull()) }

    // Filter points by search query, point type filter, and specialty
    val filteredPoints = remember(allMapPoints, searchQuery, selectedPointFilter, selectedSpecialty) {
        allMapPoints.filter { pt ->
            val matchesType = selectedPointFilter == null || pt.type == selectedPointFilter
            val matchesQuery = searchQuery.isBlank() ||
                pt.name.contains(searchQuery, ignoreCase = true) ||
                pt.address.contains(searchQuery, ignoreCase = true) ||
                pt.acceptedItems.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesSpecialty = when (selectedSpecialty) {
                "Electronics" -> pt.acceptedItems.any { it.contains("电子") || it.contains("家电") || it.contains("数码") }
                "Furniture" -> pt.acceptedItems.any { it.contains("家具") || it.contains("木") }
                "Glass/Plastic" -> pt.acceptedItems.any { it.contains("塑料") || it.contains("纸") || it.contains("玻璃") }
                "Metal" -> pt.acceptedItems.any { it.contains("金属") || it.contains("电池") || it.contains("罐") }
                else -> true
            }

            matchesType && matchesQuery && matchesSpecialty
        }
    }

    val defaultLatLng = remember(selectedPoint, filteredPoints) {
        val pt = selectedPoint ?: filteredPoints.firstOrNull()
        LatLng(pt?.latitude ?: 39.9080, pt?.longitude ?: 116.4800)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 12.5f)
    }

    LaunchedEffect(selectedPoint) {
        selectedPoint?.let { pt ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(pt.latitude, pt.longitude),
                13.5f
            )
        }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            BilingualHeader(
                title = if (lang == Language.ZH) "社区捐赠点与回收地图" else "Recycling & Donation Map"
            )

            // Top Search Bar & Geolocation Permission Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = if (lang == Language.ZH) "搜索捐赠点/回收箱/站点名称..." else "Search drop-offs, bins or stations...",
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("map_station_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    // Location Button
                    Button(
                        onClick = {
                            isLocationPermissionGranted = true
                            locationToastMessage = if (lang == Language.ZH) "📍 定位成功！附近社区捐赠点与回收箱已激活" else "📍 Location updated!"
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(39.9150, 116.4850), 13.5f)
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("request_location_permission_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLocationPermissionGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Location",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isLocationPermissionGranted) {
                                    if (lang == Language.ZH) "已定位" else "Located"
                                } else {
                                    if (lang == Language.ZH) "📍 定位" else "📍 Locate"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (locationToastMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = locationToastMessage!!,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Map Category Marker Type Filter Chips (Requirement #2)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        MapFilterChip(
                            label = if (lang == Language.ZH) "全部 (${allMapPoints.size})" else "All (${allMapPoints.size})",
                            isSelected = selectedPointFilter == null,
                            onClick = { selectedPointFilter = null }
                        )
                    }
                    item {
                        MapFilterChip(
                            label = if (lang == Language.ZH) "❤️ 社区认证捐赠点" else "❤️ Donation Drop-offs",
                            isSelected = selectedPointFilter == MapPointType.DONATION_DROP_OFF,
                            onClick = { selectedPointFilter = MapPointType.DONATION_DROP_OFF }
                        )
                    }
                    item {
                        MapFilterChip(
                            label = if (lang == Language.ZH) "🗑️ 24H公共回收箱" else "🗑️ Public Bins",
                            isSelected = selectedPointFilter == MapPointType.PUBLIC_RECYCLING_BIN,
                            onClick = { selectedPointFilter = MapPointType.PUBLIC_RECYCLING_BIN }
                        )
                    }
                    item {
                        MapFilterChip(
                            label = if (lang == Language.ZH) "🏢 备案回收站" else "🏢 Recycling Stations",
                            isSelected = selectedPointFilter == MapPointType.RECYCLING_STATION,
                            onClick = { selectedPointFilter = MapPointType.RECYCLING_STATION }
                        )
                    }
                }
            }

            // View Switcher & Sidebar Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (mapScreenMode == MapScreenMode.MAP) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { mapScreenMode = MapScreenMode.MAP }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("toggle_map_view")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Map",
                                    tint = if (mapScreenMode == MapScreenMode.MAP) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lang == Language.ZH) "地图" else "Map",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mapScreenMode == MapScreenMode.MAP) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (mapScreenMode == MapScreenMode.LIST) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { mapScreenMode = MapScreenMode.LIST }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("toggle_list_view")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = "List",
                                    tint = if (mapScreenMode == MapScreenMode.LIST) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lang == Language.ZH) "列表" else "List",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mapScreenMode == MapScreenMode.LIST) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (mapScreenMode == MapScreenMode.AR) Color(0xFF00E676) else Color.Transparent)
                                .clickable { mapScreenMode = MapScreenMode.AR }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("toggle_ar_view")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "📷",
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lang == Language.ZH) "AR实景" else "AR View",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mapScreenMode == MapScreenMode.AR) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { onRegisterStationClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("station_register_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AddBusiness,
                            contentDescription = "Register",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lang == Language.ZH) "站点/投递点申报" else "Register Location",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Main Map/List/AR Container
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (mapScreenMode == MapScreenMode.MAP) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    ) {
                        // Google Maps View
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(isMyLocationEnabled = false),
                            uiSettings = MapUiSettings(zoomControlsEnabled = true)
                        ) {
                            filteredPoints.forEach { pt ->
                                val markerHue = when (pt.type) {
                                    MapPointType.DONATION_DROP_OFF -> BitmapDescriptorFactory.HUE_ROSE
                                    MapPointType.PUBLIC_RECYCLING_BIN -> BitmapDescriptorFactory.HUE_GREEN
                                    MapPointType.RECYCLING_STATION -> BitmapDescriptorFactory.HUE_AZURE
                                }

                                Marker(
                                    state = MarkerState(position = LatLng(pt.latitude, pt.longitude)),
                                    title = pt.name,
                                    snippet = "${pt.verifiedBadge} | ${pt.hours}",
                                    icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                                    onClick = {
                                        selectedPoint = pt
                                        false
                                    }
                                )
                            }
                        }

                        // Map Legend Banner Overlay (Requirement #2)
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                                .testTag("station_map_material_legend"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE91E63)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("❤️ 捐赠点", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE91E63))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF2E7D32)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🗑️ 公共箱", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF0288D1)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🏢 回收站", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                                }
                            }
                        }

                        // Floating Selected Point Detail Card (Requirement #2)
                        selectedPoint?.let { pt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(12.dp)
                                    .testTag("selected_map_point_card"),
                                shape = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = pt.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when (pt.type) {
                                                        MapPointType.DONATION_DROP_OFF -> Color(0xFFE91E63).copy(alpha = 0.15f)
                                                        MapPointType.PUBLIC_RECYCLING_BIN -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                                        MapPointType.RECYCLING_STATION -> MaterialTheme.colorScheme.primaryContainer
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = pt.verifiedBadge,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (pt.type) {
                                                    MapPointType.DONATION_DROP_OFF -> Color(0xFFE91E63)
                                                    MapPointType.PUBLIC_RECYCLING_BIN -> Color(0xFF2E7D32)
                                                    MapPointType.RECYCLING_STATION -> MaterialTheme.colorScheme.primary
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "📍 地址: ${pt.address} (${pt.distanceKm} km)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = "🕒 开放时间: ${pt.hours}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "📦 支持品类: ${pt.acceptedItems.joinToString(", ")}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (pt.stationRef != null) {
                                                    onStationClick(pt.stationRef)
                                                }
                                            },
                                            modifier = Modifier.weight(1.2f),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(if (lang == Language.ZH) "投递/报价" else "Details", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = { activeChatPoint = pt },
                                            modifier = Modifier
                                                .weight(1.1f)
                                                .testTag("chat_with_station_admin_btn"),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                        ) {
                                            Text(if (lang == Language.ZH) "💬 咨询站长" else "💬 Chat Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { /* Navigate */ },
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Icon(Icons.Default.Navigation, contentDescription = "Nav", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (mapScreenMode == MapScreenMode.LIST) {
                    // List View for all map points
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredPoints) { pt ->
                            MapPointCard(
                                mapPoint = pt,
                                lang = lang,
                                onClick = {
                                    if (pt.stationRef != null) onStationClick(pt.stationRef)
                                },
                                onChatClick = { activeChatPoint = pt }
                            )
                        }
                    }
                } else {
                    // AR View overlay using CameraX & Compass spatial overlay
                    com.example.ui.components.StationArOverlayView(
                        points = filteredPoints,
                        lang = lang,
                        onSelectPoint = { pt ->
                            selectedPoint = pt
                        },
                        onOpenChat = { pt ->
                            activeChatPoint = pt
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MapFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MapPointCard(
    mapPoint: MapPoint,
    lang: Language,
    onClick: () -> Unit,
    onChatClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("map_point_card_${mapPoint.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (mapPoint.type) {
                            MapPointType.DONATION_DROP_OFF -> Icons.Default.Favorite
                            MapPointType.PUBLIC_RECYCLING_BIN -> Icons.Default.Delete
                            MapPointType.RECYCLING_STATION -> Icons.Default.Store
                        },
                        contentDescription = null,
                        tint = when (mapPoint.type) {
                            MapPointType.DONATION_DROP_OFF -> Color(0xFFE91E63)
                            MapPointType.PUBLIC_RECYCLING_BIN -> Color(0xFF2E7D32)
                            MapPointType.RECYCLING_STATION -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = mapPoint.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = mapPoint.verifiedBadge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (mapPoint.type) {
                        MapPointType.DONATION_DROP_OFF -> Color(0xFFE91E63)
                        MapPointType.PUBLIC_RECYCLING_BIN -> Color(0xFF2E7D32)
                        MapPointType.RECYCLING_STATION -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "地址: ${mapPoint.address}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "时间: ${mapPoint.hours} | 距离: ${mapPoint.distanceKm} km",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "接收品类: ${mapPoint.acceptedItems.joinToString(" · ")}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (lang == Language.ZH) "查看详情" else "Details", fontSize = 11.sp)
                }

                if (onChatClick != null) {
                    Button(
                        onClick = onChatClick,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("list_chat_admin_btn_${mapPoint.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text(if (lang == Language.ZH) "💬 咨询站长" else "💬 Chat Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

