package com.policydesk.app.ui.navigation

import android.animation.ValueAnimator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.policydesk.app.ui.addpolicy.AddPolicyScreen
import com.policydesk.app.ui.components.AppleSprings
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
import com.policydesk.app.ui.theme.IosDarkSeparator
import com.policydesk.app.ui.theme.IosSeparator
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

internal val LocalReduceMotion = compositionLocalOf { false }

@Composable
internal fun rememberReduceMotion(): Boolean {
    val lifecycleOwner = LocalLifecycleOwner.current
    var reduceMotion by remember {
        mutableStateOf(
            runCatching { !ValueAnimator.areAnimatorsEnabled() }.getOrDefault(false)
        )
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                reduceMotion = runCatching { !ValueAnimator.areAnimatorsEnabled() }.getOrDefault(false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return reduceMotion
}

@Composable
internal fun rememberIsListScrolled(listState: LazyListState): State<Boolean> {
    return remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
}

@Composable
internal fun rememberAnimatedCount(target: Int): Int {
    val reduceMotion = LocalReduceMotion.current
    val animated by animateIntAsState(
        targetValue = target,
        animationSpec = if (reduceMotion) snap() else tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "kpi_counter"
    )
    return animated
}

/**
 * Shared floating glass header overlay for top-level tabs:
 * - Applies Haze backdrop blur over scrolled content underneath
 * - Reveals a scroll-reactive hairline divider when scrolled > 0
 */
@Composable
internal fun FloatingGlassHeader(
    hazeState: HazeState,
    isScrolled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val reduceMotion = LocalReduceMotion.current
    val hairlineAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        animationSpec = if (reduceMotion) snap() else tween(durationMillis = 180),
        label = "floating_header_hairline"
    )
    val separatorColor = if (isDark) IosDarkSeparator else IosSeparator
    val tintColor = (if (isDark) Color(0xFF141418) else Color(0xFFF8F9FC)).copy(alpha = 0.78f)
    val hazeModifier = remember(hazeState, isDark) {
        try {
            Modifier.hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = tintColor,
                    blurRadius = 24.dp,
                    tints = listOf(HazeTint(tintColor)),
                    noiseFactor = 0.04f
                )
            )
        } catch (_: Throwable) {
            Modifier.background(tintColor)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(hazeModifier)
    ) {
        content()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.75.dp)
                .graphicsLayer { alpha = hairlineAlpha }
                .background(separatorColor)
        )
    }
}

@Composable
internal fun EmptyStateHaloBadge(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val reduceMotion = LocalReduceMotion.current
    val pulseScale = if (reduceMotion) {
        1f
    } else {
        val transition = rememberInfiniteTransition(label = "empty_halo_transition")
        val animated by transition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "empty_halo_scale"
        )
        animated
    }

    Box(
        modifier = modifier.size(76.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(74.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            tint.copy(alpha = if (isDark) 0.24f else 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (isDark) Color(0x33545458) else Color.White.copy(alpha = 0.84f),
                    CircleShape
                )
                .border(
                    width = 1.dp,
                    color = if (isDark) Color(0x33FFFFFF) else Color.White.copy(alpha = 0.92f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

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
    object Onboarding : Screen("onboarding")
}

@Composable
fun PolicyDeskNavHost(
    initialPdfUri: android.net.Uri? = null,
    initialRegNo: String? = null,
    otaManager: com.policydesk.app.data.ota.OtaManager? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = androidx.compose.ui.platform.LocalContext.current
    val isOnboardingDone = remember { com.policydesk.app.data.onboarding.OnboardingManager.isOnboardingCompleted(context) }

    val startDestination = when {
        initialPdfUri != null -> Screen.PdfReview.route
        !initialRegNo.isNullOrBlank() -> Screen.VehicleDetail.createRoute(initialRegNo)
        !isOnboardingDone -> Screen.Onboarding.route
        else -> Screen.Dashboard.route
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val emptyOtaState = remember { kotlinx.coroutines.flow.MutableStateFlow(com.policydesk.app.data.ota.OtaState.Idle) }
    val otaState by (otaManager?.otaState ?: emptyOtaState).collectAsState()
    val isUpdateAvailable = otaState is com.policydesk.app.data.ota.OtaState.UpdateAvailable || otaState is com.policydesk.app.data.ota.OtaState.ReadyToInstall

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
    val reduceMotion = rememberReduceMotion()
    var isSelectionActive by remember { mutableStateOf(false) }
    var scrollToTopTrigger by remember { mutableStateOf(0L) }
    val onScrollToTop = remember { { scrollToTopTrigger = System.currentTimeMillis() } }
    val scrollToTab: (Int) -> Unit = remember(pagerState, coroutineScope, reduceMotion) {
        { targetPage ->
            coroutineScope.launch {
                val clamped = targetPage.coerceIn(0, 3)
                if (reduceMotion) {
                    pagerState.scrollToPage(clamped)
                } else {
                    pagerState.animateScrollToPage(clamped, animationSpec = AppleSprings.Smooth)
                }
            }
        }
    }

    val pagerPosition = if (isTopLevelRoute) {
        pagerState.currentPage + pagerState.currentPageOffsetFraction
    } else null

    var dockScrubPosition by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(Unit) {
        snapshotFlow { dockScrubPosition }
            .collectLatest { scrubPos ->
                if (scrubPos != null) {
                    val clamped = scrubPos.coerceIn(0f, 3f)
                    val page = clamped.roundToInt().coerceIn(0, 3)
                    val offset = (clamped - page).coerceIn(-0.5f, 0.5f)
                    pagerState.scrollToPage(page, offset)
                }
            }
    }

    LaunchedEffect(pagerState.currentPage, isTopLevelRoute) {
        if (!isTopLevelRoute || pagerState.currentPage != 0) {
            isSelectionActive = false
        }
    }

    BackHandler(enabled = isTopLevelRoute && pagerState.currentPage != 0) {
        scrollToTab(0)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .drop(1)
            .collectLatest {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
    }

    LaunchedEffect(initialPdfUri) {
        if (initialPdfUri != null && currentRoute != Screen.PdfReview.route) {
            navController.navigate(Screen.PdfReview.route) {
                popUpTo(Screen.Dashboard.route) { saveState = true }
                launchSingleTop = true
            }
        }
    }

    val navSlideSpec = remember(reduceMotion) {
        spring<IntOffset>(
            dampingRatio = 1.0f,
            stiffness = 340f,
            visibilityThreshold = IntOffset.VisibilityThreshold
        )
    }

    CompositionLocalProvider(
        LocalHazeState provides hazeState,
        LocalScrollToTopTrigger provides scrollToTopTrigger,
        LocalScrollToTopDispatcher provides onScrollToTop,
        LocalReduceMotion provides reduceMotion
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                enterTransition = {
                    if (reduceMotion) {
                        EnterTransition.None
                    } else if (initialState.destination.route == null) {
                        fadeIn(animationSpec = tween(150))
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> (fullWidth * 0.34f).toInt() },
                            animationSpec = navSlideSpec
                        ) + fadeIn(animationSpec = AppleSprings.Smooth)
                    }
                },
                exitTransition = {
                    if (reduceMotion) {
                        ExitTransition.None
                    } else {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -(fullWidth * 0.16f).toInt() },
                            animationSpec = navSlideSpec
                        ) + scaleOut(
                            targetScale = 0.96f,
                            animationSpec = AppleSprings.Smooth
                        ) + fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                    }
                },
                popEnterTransition = {
                    if (reduceMotion) {
                        EnterTransition.None
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> -(fullWidth * 0.16f).toInt() },
                            animationSpec = navSlideSpec
                        ) + scaleIn(
                            initialScale = 0.96f,
                            animationSpec = AppleSprings.Smooth
                        ) + fadeIn(animationSpec = AppleSprings.Smooth)
                    }
                },
                popExitTransition = {
                    if (reduceMotion) {
                        ExitTransition.None
                    } else {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> (fullWidth * 0.34f).toInt() },
                            animationSpec = navSlideSpec
                        ) + fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                    }
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
                                scrollToTab(2)
                            },
                            onNavigateToReports = {
                                scrollToTab(3)
                            },
                            onNavigateToCustomers = {
                                scrollToTab(1)
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
                            onSelectionModeChange = { isSelectionActive = it },
                            isUpdateAvailable = isUpdateAvailable,
                            isTabActive = isTopLevelRoute && pagerState.currentPage == 0
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
                    onNavigateToScan = {
                        navController.navigate(Screen.PdfReview.route) {
                            popUpTo(Screen.AddPolicy.route) { inclusive = true }
                        }
                    },
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

            composable(Screen.Onboarding.route) {
                com.policydesk.app.ui.onboarding.OnboardingScreen(
                    onFinish = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOnboarding = { navController.navigate(Screen.Onboarding.route) }
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

        // Ambient Bottom Scrim for Floating Dock
        // Gracefully fades scrolling content into the background behind the floating dock
        AnimatedVisibility(
            visible = isTopLevelTab && !isSelectionActive,
            enter = if (reduceMotion) EnterTransition.None else fadeIn(animationSpec = tween(200)),
            exit = if (reduceMotion) ExitTransition.None else fadeOut(animationSpec = tween(150)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val isDark = isSystemInDarkTheme()
            val scrimColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                scrimColor.copy(alpha = 0.45f),
                                scrimColor.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
        }

        // True Persistent Floating Liquid Glass Dock (Suspended over content, active across all primary tabs)
        AnimatedVisibility(
            visible = isTopLevelTab && !isSelectionActive,
            enter = if (reduceMotion) EnterTransition.None else (
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
                ) + fadeIn(animationSpec = tween(200))
            ),
            exit = if (reduceMotion) ExitTransition.None else (
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
                ) + fadeOut(animationSpec = tween(150))
            ),
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
                            scrollToTab(targetIndex)
                        } else if (selectedTab == DockTab.SETTINGS) {
                            navController.navigate(Screen.Settings.route)
                        }
                    },
                    onScrollToTop = onScrollToTop,
                    hazeState = hazeState,
                    pagerPosition = pagerPosition,
                    onScrubProgress = { pos -> dockScrubPosition = pos },
                    onScrubFinished = { targetIndex ->
                        dockScrubPosition = null
                        scrollToTab(targetIndex)
                    }
                )
            }
        }

        if (otaManager != null) {
            com.policydesk.app.ui.components.OtaUpdateDialog(otaManager = otaManager)
        }
    }
}
}
