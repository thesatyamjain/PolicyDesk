package com.policydesk.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.policydesk.app.ui.addpolicy.AddPolicyScreen
import com.policydesk.app.ui.components.DockTab
import com.policydesk.app.ui.components.LiquidGlassDock
import com.policydesk.app.ui.components.LocalHazeState
import com.policydesk.app.ui.components.LocalScrollToTopDispatcher
import com.policydesk.app.ui.components.LocalScrollToTopTrigger
import com.policydesk.app.ui.customers.CustomersScreen
import com.policydesk.app.ui.dashboard.DashboardScreen
import com.policydesk.app.ui.detail.VehicleDetailScreen
import com.policydesk.app.ui.importer.ImportScreen
import com.policydesk.app.ui.monthview.MonthViewScreen
import com.policydesk.app.ui.pdfreview.PdfReviewScreen
import com.policydesk.app.ui.renew.RenewPolicyScreen
import com.policydesk.app.ui.reports.ReportsScreen
import com.policydesk.app.ui.settings.SettingsScreen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object MonthView : Screen("month_view")
    object Reports : Screen("reports")
    object AddPolicy : Screen("add_policy?editRegNo={editRegNo}") {
        fun createRoute(editRegNo: String? = null) = if (editRegNo != null) "add_policy?editRegNo=$editRegNo" else "add_policy"
    }
    object ImportSpreadsheet : Screen("import_spreadsheet")
    object PdfReview : Screen("pdf_review")
    object Settings : Screen("settings")
    object Customers : Screen("customers")
    object VehicleDetail : Screen("vehicle_detail/{regNo}") {
        fun createRoute(regNo: String) = "vehicle_detail/$regNo"
    }
    object RenewPolicy : Screen("renew_policy/{regNo}") {
        fun createRoute(regNo: String) = "renew_policy/$regNo"
    }
}

@Composable
fun PolicyDeskNavHost(
    initialPdfUri: android.net.Uri? = null,
    initialRegNo: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = when {
        initialPdfUri != null -> Screen.PdfReview.route
        !initialRegNo.isNullOrBlank() -> Screen.VehicleDetail.createRoute(initialRegNo)
        else -> Screen.Dashboard.route
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val isTopLevelRoute = currentRoute == null || currentRoute == Screen.Dashboard.route
    val currentTab = if (isTopLevelRoute) {
        when (pagerState.currentPage) {
            0 -> DockTab.DASHBOARD
            1 -> DockTab.CUSTOMERS
            2 -> DockTab.TIMELINE
            3 -> DockTab.REPORTS
            else -> DockTab.DASHBOARD
        }
    } else null

    val isTopLevelTab = currentTab != null
    val hazeState = remember { HazeState() }
    var isSelectionActive by remember { mutableStateOf(false) }
    var scrollToTopTrigger by remember { mutableStateOf(0L) }
    val onScrollToTop = remember { { scrollToTopTrigger = System.currentTimeMillis() } }

    val pagerPosition = if (isTopLevelRoute) {
        pagerState.currentPage + pagerState.currentPageOffsetFraction
    } else null

    BackHandler(enabled = isTopLevelRoute && pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    LaunchedEffect(initialPdfUri) {
        if (initialPdfUri != null && currentRoute != Screen.PdfReview.route) {
            navController.navigate(Screen.PdfReview.route) {
                popUpTo(Screen.Dashboard.route) { saveState = true }
                launchSingleTop = true
            }
        }
    }

    CompositionLocalProvider(
        LocalHazeState provides hazeState,
        LocalScrollToTopTrigger provides scrollToTopTrigger,
        LocalScrollToTopDispatcher provides onScrollToTop
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                enterTransition = {
                    if (initialState.destination.route == null) {
                        fadeIn(animationSpec = tween(150))
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> (fullWidth * 0.32f).toInt() },
                            animationSpec = spring(dampingRatio = 0.86f, stiffness = 420f)
                        ) + fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing))
                    }
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -(fullWidth * 0.18f).toInt() },
                        animationSpec = spring(dampingRatio = 0.86f, stiffness = 420f)
                    ) + fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -(fullWidth * 0.18f).toInt() },
                        animationSpec = spring(dampingRatio = 0.86f, stiffness = 420f)
                    ) + fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> (fullWidth * 0.32f).toInt() },
                        animationSpec = spring(dampingRatio = 0.86f, stiffness = 420f)
                    ) + fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                }
            ) {
            composable(Screen.Dashboard.route) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = !isSelectionActive
                ) { page ->
                    when (page) {
                        0 -> DashboardScreen(
                            onNavigateToAddPolicy = {
                                navController.navigate(Screen.AddPolicy.route)
                            },
                            onNavigateToMonthView = {
                                coroutineScope.launch { pagerState.animateScrollToPage(2) }
                            },
                            onNavigateToReports = {
                                coroutineScope.launch { pagerState.animateScrollToPage(3) }
                            },
                            onNavigateToCustomers = {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            onNavigateToImport = {
                                navController.navigate(Screen.ImportSpreadsheet.route)
                            },
                            onNavigateToPdf = {
                                navController.navigate(Screen.PdfReview.route)
                            },
                            onNavigateToSettings = {
                                navController.navigate(Screen.Settings.route)
                            },
                            onNavigateToVehicleDetail = { regNo ->
                                navController.navigate(Screen.VehicleDetail.createRoute(regNo))
                            },
                            onSelectionModeChange = { isSelectionActive = it }
                        )
                        1 -> CustomersScreen(
                            onNavigateBack = null,
                            onNavigateToVehicleDetail = { regNo ->
                                navController.navigate(Screen.VehicleDetail.createRoute(regNo))
                            }
                        )
                        2 -> MonthViewScreen(
                            onNavigateBack = null,
                            onNavigateToVehicleDetail = { regNo ->
                                navController.navigate(Screen.VehicleDetail.createRoute(regNo))
                            }
                        )
                        3 -> ReportsScreen(
                            onNavigateBack = null,
                            onNavigateToVehicleDetail = { regNo ->
                                navController.navigate(Screen.VehicleDetail.createRoute(regNo))
                            },
                            onNavigateToRenew = { regNo ->
                                navController.navigate(Screen.RenewPolicy.createRoute(regNo))
                            }
                        )
                    }
                }
            }

            composable(
                route = Screen.AddPolicy.route,
                arguments = listOf(
                    navArgument("editRegNo") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val editRegNo = backStackEntry.arguments?.getString("editRegNo")
                AddPolicyScreen(
                    editRegNo = editRegNo,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToVehicleDetail = { regNo ->
                        navController.navigate(Screen.VehicleDetail.createRoute(regNo)) {
                            popUpTo(Screen.Dashboard.route)
                        }
                    }
                )
            }

            composable(Screen.ImportSpreadsheet.route) {
                ImportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onImportFinished = {
                        navController.popBackStack(Screen.Dashboard.route, false)
                    }
                )
            }

            composable(Screen.PdfReview.route) {
                PdfReviewScreen(
                    initialUri = initialPdfUri,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToVehicleDetail = { regNo ->
                        navController.navigate(Screen.VehicleDetail.createRoute(regNo)) {
                            popUpTo(Screen.Dashboard.route)
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.VehicleDetail.route,
                arguments = listOf(navArgument("regNo") { type = NavType.StringType })
            ) {
                VehicleDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRenew = { regNo ->
                        navController.navigate(Screen.RenewPolicy.createRoute(regNo))
                    },
                    onNavigateToEdit = { regNo ->
                        navController.navigate(Screen.AddPolicy.createRoute(regNo))
                    }
                )
            }

            composable(
                route = Screen.RenewPolicy.route,
                arguments = listOf(navArgument("regNo") { type = NavType.StringType })
            ) {
                RenewPolicyScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onRenewalComplete = { regNo ->
                        navController.popBackStack()
                    }
                )
            }
        }

        // True Persistent Floating Liquid Glass Dock (Suspended over content, active across all primary tabs)
        AnimatedVisibility(
            visible = isTopLevelTab && !isSelectionActive,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
            ) + fadeOut(animationSpec = tween(150)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            currentTab?.let { tab ->
                LiquidGlassDock(
                    currentTab = tab,
                    onTabSelected = { selectedTab ->
                        val targetIndex = when (selectedTab) {
                            DockTab.DASHBOARD -> 0
                            DockTab.CUSTOMERS -> 1
                            DockTab.TIMELINE -> 2
                            DockTab.REPORTS -> 3
                            DockTab.SETTINGS -> null
                        }
                        if (targetIndex != null) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(targetIndex)
                            }
                        } else if (selectedTab == DockTab.SETTINGS) {
                            navController.navigate(Screen.Settings.route)
                        }
                    },
                    onScrollToTop = onScrollToTop,
                    hazeState = hazeState,
                    pagerPosition = pagerPosition
                )
            }
        }
    }
}
}
