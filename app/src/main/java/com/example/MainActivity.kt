package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.model.AppStrings
import com.example.data.model.Language
import com.example.data.model.LocalLanguageManager
import com.example.data.model.OrderAgreement
import com.example.data.model.RecycleItem
import com.example.data.model.RecycleStation
import com.example.data.repository.RecycleRepository
import com.example.ui.components.GlobalFloatingButtons
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CategoryScreen
import com.example.ui.screens.DonationScreen
import com.example.ui.screens.ElectronicAgreementScreen
import com.example.ui.screens.ForumScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ItemDetailScreen
import com.example.ui.screens.MessageNotificationScreen
import com.example.ui.screens.OrderListScreen
import com.example.ui.screens.PublishScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StationDetailScreen
import com.example.ui.screens.StationMapScreen
import com.example.ui.screens.StationRegistrationScreen
import com.example.ui.screens.SupplyDemandScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.GreenCycleTheme
import com.example.ui.viewmodel.MainViewModel

enum class ScreenRoute {
    HOME, CATEGORY, FORUM, DONATION, SUPPLY, STATION, PROFILE,
    PUBLISH, ITEM_DETAIL, STATION_DETAIL, STATION_REGISTRATION,
    ORDER_LIST, ELECTRONIC_AGREEMENT, ADMIN_DASHBOARD, MESSAGES
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember { RecycleRepository(database.recycleDao()) }
            val viewModel = remember { MainViewModel(repository) }

            val themeManager = remember { com.example.ui.theme.ThemeManager(context = context) }
            val langManager = remember { com.example.data.model.LanguageManager(context = context) }

            androidx.compose.runtime.CompositionLocalProvider(
                LocalLanguageManager provides langManager
            ) {
                GreenCycleTheme(themeManager = themeManager) {
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        SplashScreen(
                            lang = langManager.currentLanguage,
                            onSplashComplete = { autoLoggedIn ->
                                showSplash = false
                            }
                        )
                    } else {
                        MainAppContent(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val langManager = LocalLanguageManager.current
    val currentLang = langManager.currentLanguage

    val items by viewModel.items.collectAsState()
    val forumPosts by viewModel.forumPosts.collectAsState()
    val stations by viewModel.stations.collectAsState()
    val orders by viewModel.orders.collectAsState()

    var currentScreen by remember { mutableStateOf(ScreenRoute.HOME) }
    val screenHistory = remember { mutableListOf(ScreenRoute.HOME) }

    fun navigateTo(route: ScreenRoute) {
        if (currentScreen != route) {
            screenHistory.add(route)
            currentScreen = route
        }
    }

    fun navigateBack() {
        if (screenHistory.size > 1) {
            screenHistory.removeAt(screenHistory.lastIndex)
            currentScreen = screenHistory.last()
        }
    }

    val showBottomBar = currentScreen in listOf(
        ScreenRoute.HOME, ScreenRoute.CATEGORY, ScreenRoute.FORUM,
        ScreenRoute.DONATION, ScreenRoute.SUPPLY, ScreenRoute.STATION, ScreenRoute.PROFILE
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    NavigationBarItem(
                        selected = currentScreen == ScreenRoute.HOME,
                        onClick = { navigateTo(ScreenRoute.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text(AppStrings.navHome(currentLang), fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = currentScreen == ScreenRoute.CATEGORY,
                        onClick = { navigateTo(ScreenRoute.CATEGORY) },
                        icon = { Icon(Icons.Default.Category, contentDescription = "Category") },
                        label = { Text(AppStrings.navCategory(currentLang), fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_category")
                    )
                    NavigationBarItem(
                        selected = currentScreen == ScreenRoute.FORUM,
                        onClick = { navigateTo(ScreenRoute.FORUM) },
                        icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = "Forum") },
                        label = { Text(AppStrings.navForum(currentLang), fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_forum")
                    )
                    NavigationBarItem(
                        selected = currentScreen == ScreenRoute.DONATION,
                        onClick = { navigateTo(ScreenRoute.DONATION) },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Donation") },
                        label = { Text(AppStrings.navDonation(currentLang), fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_donation")
                    )
                    NavigationBarItem(
                        selected = currentScreen == ScreenRoute.STATION,
                        onClick = { navigateTo(ScreenRoute.STATION) },
                        icon = { Icon(Icons.Default.Store, contentDescription = "Stations") },
                        label = { Text(AppStrings.navStation(currentLang), fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_station")
                    )
                    NavigationBarItem(
                        selected = currentScreen == ScreenRoute.PROFILE,
                        onClick = { navigateTo(ScreenRoute.PROFILE) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text(AppStrings.navProfile(currentLang), fontSize = 10.sp) },
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ScreenRoute.HOME -> HomeScreen(
                    lang = currentLang,
                    items = items,
                    searchQuery = viewModel.searchQuery,
                    onSearchChange = { viewModel.searchQuery = it },
                    onCategoryClick = { major ->
                        viewModel.selectedCategory = major
                        navigateTo(ScreenRoute.CATEGORY)
                    },
                    onItemClick = { item ->
                        viewModel.selectedItemDetail = item
                        navigateTo(ScreenRoute.ITEM_DETAIL)
                    },
                    onPublishClick = { navigateTo(ScreenRoute.PUBLISH) },
                    onForumClick = { navigateTo(ScreenRoute.FORUM) },
                    onDonationClick = { navigateTo(ScreenRoute.DONATION) },
                    onSupplyClick = { navigateTo(ScreenRoute.SUPPLY) },
                    onMapClick = { navigateTo(ScreenRoute.STATION) },
                    onAdminClick = { navigateTo(ScreenRoute.ADMIN_DASHBOARD) },
                    greenTip = viewModel.weeklyGreenTip,
                    isTipLoading = viewModel.isGreenTipLoading,
                    onRefreshTip = { viewModel.refreshWeeklyGreenTip() }
                )

                ScreenRoute.CATEGORY -> CategoryScreen(
                    lang = currentLang,
                    onSubCategoryClick = { major, sub ->
                        navigateTo(ScreenRoute.HOME)
                    }
                )

                ScreenRoute.FORUM -> ForumScreen(
                    posts = forumPosts,
                    lang = currentLang,
                    onPostClick = { post ->
                        viewModel.selectedPostDetail = post
                    },
                    onPublishClick = { navigateTo(ScreenRoute.PUBLISH) },
                    onLikeClick = { postId -> viewModel.toggleLikePost(postId) },
                    onHeartClick = { postId -> viewModel.toggleHeartPost(postId) },
                    onHelpfulClick = { postId -> viewModel.toggleHelpfulPost(postId) },
                    onAddComment = { post, text -> viewModel.addForumComment(context, post, text) },
                    sustainabilityEvents = viewModel.sustainabilityEvents,
                    onJoinEvent = { eventId -> viewModel.toggleJoinEvent(context, eventId) },
                    onViewEventMap = { _, _ -> navigateTo(ScreenRoute.STATION) }
                )

                ScreenRoute.DONATION -> DonationScreen(
                    drives = viewModel.donationDrives,
                    lang = currentLang,
                    onDriveClick = { drive ->
                        viewModel.selectedDonationDrive = drive
                        navigateTo(ScreenRoute.PUBLISH)
                    },
                    communityWishlists = viewModel.communityWishlists,
                    onMatchWishlist = { wishId -> viewModel.matchWishlistItem(context, wishId) }
                )


                ScreenRoute.SUPPLY -> SupplyDemandScreen(
                    items = viewModel.supplyItems,
                    lang = currentLang
                )

                ScreenRoute.STATION -> StationMapScreen(
                    stations = stations,
                    lang = currentLang,
                    onStationClick = { sta ->
                        viewModel.selectedStationDetail = sta
                        navigateTo(ScreenRoute.STATION_DETAIL)
                    },
                    onRegisterStationClick = { navigateTo(ScreenRoute.STATION_REGISTRATION) }
                )

                ScreenRoute.PROFILE -> UserProfileScreen(
                    lang = currentLang,
                    items = items,
                    onItemClick = { item ->
                        viewModel.selectedItemDetail = item
                        navigateTo(ScreenRoute.ITEM_DETAIL)
                    },
                    onNavigate = { routeId ->
                        when (routeId) {
                            "my_posts" -> navigateTo(ScreenRoute.HOME)
                            "my_orders" -> navigateTo(ScreenRoute.ORDER_LIST)
                            "my_donations" -> navigateTo(ScreenRoute.DONATION)
                            "saved_stations" -> navigateTo(ScreenRoute.STATION)
                            "messages" -> navigateTo(ScreenRoute.MESSAGES)
                            "admin" -> navigateTo(ScreenRoute.ADMIN_DASHBOARD)
                        }
                    }
                )

                ScreenRoute.PUBLISH -> PublishScreen(
                    lang = currentLang,
                    onPublishSuccess = { title, majorId, subId, cond, desc, price, isDon, donTgt, imgs, vid, loc, phone, wx ->
                        viewModel.publishRecycleItem(title, majorId, subId, cond, desc, price, isDon, donTgt, imgs, vid, loc, phone, wx)
                        navigateTo(ScreenRoute.HOME)
                    }
                )

                ScreenRoute.ITEM_DETAIL -> {
                    viewModel.selectedItemDetail?.let { item ->
                        ItemDetailScreen(
                            item = item,
                            lang = currentLang,
                            onViewAgreementClick = {
                                navigateTo(ScreenRoute.ORDER_LIST)
                            },
                            onBackClick = { navigateBack() }
                        )
                    } ?: navigateTo(ScreenRoute.HOME)
                }

                ScreenRoute.STATION_DETAIL -> {
                    viewModel.selectedStationDetail?.let { station ->
                        StationDetailScreen(
                            station = station,
                            lang = currentLang,
                            onBackClick = { navigateBack() }
                        )
                    } ?: navigateTo(ScreenRoute.STATION)
                }

                ScreenRoute.STATION_REGISTRATION -> StationRegistrationScreen(
                    lang = currentLang,
                    onSubmit = { name, addr, phone, wx, hrs, tags, svcs, prices ->
                        viewModel.registerStation(name, addr, phone, wx, hrs, tags, svcs, prices)
                        navigateTo(ScreenRoute.STATION)
                    }
                )

                ScreenRoute.ORDER_LIST -> OrderListScreen(
                    orders = orders,
                    lang = currentLang,
                    onOrderClick = { order ->
                        viewModel.selectedOrderDetail = order
                        navigateTo(ScreenRoute.ELECTRONIC_AGREEMENT)
                    }
                )

                ScreenRoute.ELECTRONIC_AGREEMENT -> {
                    val order = viewModel.selectedOrderDetail ?: orders.firstOrNull()
                    if (order != null) {
                        ElectronicAgreementScreen(
                            order = order,
                            lang = currentLang,
                            onSignComplete = { sig ->
                                viewModel.signAgreement(context, order.orderId, sig)
                            }
                        )
                    } else {
                        navigateTo(ScreenRoute.HOME)
                    }
                }

                ScreenRoute.ADMIN_DASHBOARD -> AdminDashboardScreen(
                    pendingItems = items.filter { it.status == "PENDING" || it.status == "MATCHING" },
                    lang = currentLang,
                    onAssignRecipient = { id, name, phone, wx, station, time, loc, price, eval ->
                        viewModel.adminAssignRecipient(context, id, name, phone, wx, station, time, loc, price, eval)
                        navigateTo(ScreenRoute.ORDER_LIST)
                    }
                )

                ScreenRoute.MESSAGES -> MessageNotificationScreen(
                    lang = currentLang
                )
            }

            // Global Floating Back & Back-To-Top Buttons in Bottom-Left corner (Requirements #32, #33, #34)
            GlobalFloatingButtons(
                canGoBack = screenHistory.size > 1,
                onBackClick = { navigateBack() },
                onScrollToTopClick = {
                    // Smooth back to top or home reset
                    currentScreen = currentScreen
                },
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}
