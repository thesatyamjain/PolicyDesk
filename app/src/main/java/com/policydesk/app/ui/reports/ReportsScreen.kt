package com.policydesk.app.ui.reports

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.data.profile.AdvisorProfileManager
import com.policydesk.app.domain.model.MonthlyTrendItem
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.ReportPeriod
import com.policydesk.app.domain.model.ReportsAggregate
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.ui.components.AppleButton
import com.policydesk.app.ui.components.AppleSecondaryButton
import com.policydesk.app.ui.components.AppleSprings
import com.policydesk.app.ui.components.BindBackToTop
import com.policydesk.app.ui.components.DaysRemainingBadge
import com.policydesk.app.ui.components.HsrpNumberPlate
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.components.LiquidGlassSegmentedControl
import com.policydesk.app.ui.components.LiquidGlassTopBar
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.dashboard.VehicleExpiryCard
import com.policydesk.app.ui.detail.DetailRow
import com.policydesk.app.ui.navigation.EmptyStateHaloBadge
import com.policydesk.app.ui.navigation.FloatingGlassHeader
import com.policydesk.app.ui.navigation.LocalReduceMotion
import com.policydesk.app.ui.navigation.rememberIsListScrolled
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleDarkSystemGreen
import com.policydesk.app.ui.theme.AppleDarkSystemPurple
import com.policydesk.app.ui.theme.AppleDarkSystemRed
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemPurple
import com.policydesk.app.ui.theme.AppleSystemRed
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkLabelTertiary
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.StatusOverdueRed
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToVehicleDetail: (String) -> Unit,
    onNavigateToRenew: (String) -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val overdueFilter by viewModel.overdueFilter.collectAsStateWithLifecycle()
    val aggregate by viewModel.aggregateData.collectAsStateWithLifecycle()
    val urgentRisk by viewModel.urgentRiskSummary.collectAsStateWithLifecycle()

    val isDark = isSystemInDarkTheme()
    val reduceMotion = LocalReduceMotion.current
    val haptic = LocalHapticFeedback.current
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    val now = remember { LocalDate.now() }
    val periods = remember(now) {
        val thisMonth = ReportPeriod.Month(now.year, now.monthValue)
        val thisQuarter = ReportPeriod.Quarter(now.year, (now.monthValue - 1) / 3 + 1)
        val currentFY = ReportPeriod.FinancialYear(if (now.monthValue >= 4) now.year else now.year - 1)
        listOf(thisMonth, thisQuarter, currentFY)
    }

    val screenHazeState = remember { HazeState() }
    val salesListState = rememberLazyListState()
    val missedListState = rememberLazyListState()
    val analyticsListState = rememberLazyListState()

    val activeListState = when (selectedTab) {
        ReportsTab.SALES -> salesListState
        ReportsTab.MISSED_RENEWALS -> missedListState
        ReportsTab.ANALYTICS -> analyticsListState
    }
    val isScrolled by rememberIsListScrolled(activeListState)
    BindBackToTop(activeListState)

    Scaffold(
        topBar = {
            FloatingGlassHeader(
                hazeState = screenHazeState,
                isScrolled = isScrolled
            ) {
                LiquidGlassTopBar(
                    title = "Reports & Analytics",
                    subtitle = "Performance & Revenue Insights",
                    onNavigateBack = onNavigateBack
                )

                // Pinned Segmented Tabs above scrolling body
                LiquidGlassSegmentedControl(
                    items = ReportsTab.values().toList(),
                    selectedItem = selectedTab,
                    onItemSelected = { viewModel.onTabSelected(it) },
                    labelProvider = { it.title },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                // Pinned Period Selector Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(periods, key = { it.label }) { period ->
                        val isSelected = selectedPeriod == period
                        val pillShape = RoundedCornerShape(10.dp)
                        val unselectedBg = if (isDark) Color(0x33545458) else Color.White.copy(alpha = 0.78f)
                        val unselectedBorder = if (isDark) Color(0x33FFFFFF) else Color(0x22000000)
                        val unselectedText = if (isDark) IosDarkLabelPrimary else IosLabelPrimary

                        Box(
                            modifier = Modifier
                                .clip(pillShape)
                                .background(
                                    color = if (isSelected) accentBlue else unselectedBg,
                                    shape = pillShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) accentBlue else unselectedBorder,
                                    shape = pillShape
                                )
                                .semantics {
                                    role = Role.Button
                                    selected = isSelected
                                }
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.onPeriodSelected(period)
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = formatReportPeriodChipLabel(period),
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else unselectedText,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        val topInset = paddingValues.calculateTopPadding() + 10.dp

        if (aggregate == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topInset),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp,
                        color = accentBlue
                    )
                    Text("Loading report data...", color = labelSecondary, fontSize = 14.sp)
                }
            }
        } else {
            val currentAggregate = aggregate!!
            val isPortfolioEmpty = currentAggregate.analytics.bookSizeVehicles == 0 &&
                currentAggregate.sales.totalCount == 0 &&
                currentAggregate.missed.totalCount == 0 &&
                urgentRisk.overdueCount == 0 &&
                urgentRisk.dueSoonCount == 0

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (reduceMotion) {
                        ContentTransform(
                            targetContentEnter = androidx.compose.animation.EnterTransition.None,
                            initialContentExit = androidx.compose.animation.ExitTransition.None
                        )
                    } else {
                        fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = screenHazeState),
                label = "reports_tab_content"
            ) { tab ->
                if (isPortfolioEmpty) {
                    ReportsEmptyPortfolioState(
                        topInset = topInset,
                        listState = activeListState
                    )
                } else {
                    when (tab) {
                        ReportsTab.SALES -> SalesTabContent(
                            aggregate = currentAggregate,
                            onNavigateToVehicleDetail = onNavigateToVehicleDetail,
                            listState = salesListState,
                            topPadding = topInset
                        )
                        ReportsTab.MISSED_RENEWALS -> MissedRenewalsTabContent(
                            aggregate = currentAggregate,
                            activeFilter = overdueFilter,
                            onFilterSelected = viewModel::onOverdueFilterSelected,
                            onNavigateToVehicleDetail = onNavigateToVehicleDetail,
                            onNavigateToRenew = onNavigateToRenew,
                            onCloseVehicle = viewModel::closeVehicle,
                            urgentRisk = urgentRisk,
                            listState = missedListState,
                            topPadding = topInset
                        )
                        ReportsTab.ANALYTICS -> AnalyticsTabContent(
                            aggregate = currentAggregate,
                            listState = analyticsListState,
                            topPadding = topInset
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsEmptyPortfolioState(
    topInset: androidx.compose.ui.unit.Dp,
    listState: LazyListState
) {
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topInset, bottom = 140.dp)
    ) {
        item(key = "reports_empty_portfolio") {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EmptyStateHaloBadge(
                        icon = AppleIcons.ChartBar,
                        tint = accentBlue
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No analytics data yet",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add customer policies or import a spreadsheet to view sales trends, book value, and renewal risk.",
                        fontSize = 13.sp,
                        color = labelSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SalesTabContent(
    aggregate: ReportsAggregate,
    onNavigateToVehicleDetail: (String) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    topPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    val sales = aggregate.sales
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
    val accentGreen = if (isDark) AppleDarkSystemGreen else AppleSystemGreen
    val periodLabel = formatReportPeriodChipLabel(aggregate.period)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topPadding, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Cards Row
        item(key = "sales_kpi_row") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassCard(modifier = Modifier.weight(1f)) {
                    Text("Total Premium Sold", fontSize = 11.sp, color = labelSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = UiFormatters.formatCurrency(sales.totalPremium),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentBlue,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }

                LiquidGlassCard(modifier = Modifier.weight(1f)) {
                    Text("Commission Earned", fontSize = 11.sp, color = labelSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = UiFormatters.formatCurrency(sales.totalCommission),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentGreen,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
            }
        }

        // Sales Performance & Split
        item(key = "sales_performance") {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Sales Performance ($periodLabel)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                DetailRow(
                    label = "Total Policies Sold",
                    value = "${sales.totalCount} ${if (sales.totalCount == 1) "policy" else "policies"}"
                )
                DetailRow(
                    label = "New Business",
                    value = "${sales.newBusinessCount} ${if (sales.newBusinessCount == 1) "policy" else "policies"}"
                )
                DetailRow(
                    label = "Renewals",
                    value = "${sales.renewalCount} ${if (sales.renewalCount == 1) "policy" else "policies"}"
                )
                if (sales.totalPremium > 0.0 && sales.totalCommission > 0.0) {
                    val netPremium = sales.totalPremium / 1.18
                    val avgCommPct = (sales.totalCommission / netPremium) * 100.0
                    DetailRow(
                        label = "Average Commission Rate",
                        value = "${String.format(Locale.US, "%.1f", avgCommPct)}% (Net)"
                    )
                }
            }
        }

        // Companies & Insurer Breakdown
        if (sales.insurerBreakdown.isNotEmpty() || sales.splitByInsurer.isNotEmpty()) {
            item(key = "sales_companies_breakdown") {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Companies Breakdown", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = labelPrimary)
                            val companyCount = sales.insurerBreakdown.size.takeIf { it > 0 } ?: sales.splitByInsurer.size
                            Text(
                                text = "$companyCount ${if (companyCount == 1) "Company" else "Companies"}",
                                fontSize = 11.sp,
                                color = labelSecondary,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }

                        if (sales.insurerBreakdown.isNotEmpty()) {
                            sales.insurerBreakdown.forEachIndexed { index, item ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.insurer,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = labelPrimary,
                                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = UiFormatters.formatCurrency(item.totalPremium),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentBlue,
                                            style = TextStyle(fontFeatureSettings = "tnum")
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val policyCountStr = "${item.policyCount} ${if (item.policyCount == 1) "policy" else "policies"}"
                                        val shareStr = "${String.format(Locale.US, "%.1f", item.sharePercent)}% share"
                                        Text(
                                            text = "$policyCountStr • $shareStr",
                                            fontSize = 11.sp,
                                            color = labelSecondary,
                                            style = TextStyle(fontFeatureSettings = "tnum")
                                        )

                                        if (item.totalCommission > 0.0) {
                                            val netPrem = item.totalPremium / 1.18
                                            val effPct = if (netPrem > 0.0) (item.totalCommission / netPrem) * 100.0 else 0.0
                                            val commStr = UiFormatters.formatCurrency(item.totalCommission)
                                            val pctStr = String.format(Locale.US, "%.1f", effPct)
                                            Text(
                                                text = "Comm: $commStr ($pctStr%)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = accentGreen,
                                                style = TextStyle(fontFeatureSettings = "tnum")
                                            )
                                        }
                                    }

                                    // Subtle visual share track
                                    val trackProgress = (item.sharePercent / 100.0).toFloat().coerceIn(0.02f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(trackProgress)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(accentBlue)
                                        )
                                    }
                                }

                                if (index < sales.insurerBreakdown.size - 1) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                            }
                        } else {
                            sales.splitByInsurer.forEach { (insurer, prem) ->
                                DetailRow(label = insurer, value = UiFormatters.formatCurrency(prem))
                            }
                        }
                    }
                }
            }
        }

        // Canonical 12-Month Sales Trend (Scrubbable + Accessible Tap Selection)
        if (sales.monthlyTrends.isNotEmpty()) {
            item(key = "monthly_sales_trend") {
                MonthlySalesTrendCard(trends = sales.monthlyTrends)
            }
        }

        // Policies Sold List Header
        item(key = "sold_policies_header") {
            Text(
                text = "Policies Sold in $periodLabel (${sales.soldPolicies.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = labelPrimary,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
        }

        if (sales.soldPolicies.isEmpty()) {
            item(key = "sold_policies_empty") {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 1.dp
                ) {
                    Text(
                        text = "No sales recorded in $periodLabel.",
                        color = labelSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(
                items = sales.soldPolicies,
                key = { "sold_${it.vehicle.regNo}_${it.currentPolicy?.id ?: ""}" }
            ) { item ->
                VehicleExpiryCard(
                    item = item,
                    onClick = { onNavigateToVehicleDetail(item.vehicle.regNo) }
                )
            }
        }
    }
}

@Composable
fun MissedRenewalsTabContent(
    aggregate: ReportsAggregate,
    activeFilter: OverdueFilter,
    onFilterSelected: (OverdueFilter) -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit,
    onNavigateToRenew: (String) -> Unit,
    onCloseVehicle: (String, String) -> Unit,
    urgentRisk: UrgentRiskSummary = UrgentRiskSummary(
        overdueCount = aggregate.missed.totalCount,
        overduePolicies = aggregate.missed.allMissed,
        overdueRiskPremium = aggregate.missed.premiumAtRisk
    ),
    listState: LazyListState = rememberLazyListState(),
    topPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    val missed = aggregate.missed
    val performance = aggregate.performance
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
    val redAccent = if (isDark) AppleDarkSystemRed else StatusOverdueRed
    val periodLabel = formatReportPeriodChipLabel(aggregate.period)

    val displayedList = when (activeFilter) {
        OverdueFilter.ALL -> missed.allMissed
        OverdueFilter.DUE_SOON -> urgentRisk.dueSoonPolicies
        OverdueFilter.UP_TO_30 -> missed.upTo30Days
        OverdueFilter.FROM_31_TO_90 -> missed.from31To90Days
        OverdueFilter.OVER_90 -> missed.over90Days
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topPadding, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Cards: Premium at Risk & Distinct Overdue / Due <=7d Counts (no double-counting)
        item(key = "missed_kpi_row") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassCard(modifier = Modifier.weight(1f)) {
                    Text("Premium at Risk", fontSize = 11.sp, color = labelSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = UiFormatters.formatCurrency(missed.premiumAtRisk),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = redAccent,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                    Text(
                        text = "Overdue policies",
                        fontSize = 10.5.sp,
                        color = labelSecondary
                    )
                }

                LiquidGlassCard(modifier = Modifier.weight(1f)) {
                    Text("Overdue / Due ≤ 7d", fontSize = 11.sp, color = labelSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${urgentRisk.overdueCount} / ${urgentRisk.dueSoonCount}",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = redAccent,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                    Text(
                        text = "Distinct counts",
                        fontSize = 10.5.sp,
                        color = labelSecondary
                    )
                }
            }
        }

        // Renewal Performance Card
        item(key = "missed_renewal_rate") {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Renewal Rate ($periodLabel)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelPrimary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", performance.renewalRatePercent)}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentBlue,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (performance.renewalRatePercent / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = accentBlue,
                    trackColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
                )
                Spacer(modifier = Modifier.height(10.dp))
                DetailRow(label = "Policies Due", value = "${performance.policiesDue}")
                DetailRow(label = "Renewed On Time", value = "${performance.renewedOnTime}")
                DetailRow(label = "Renewed Late", value = "${performance.renewedLate}")
                DetailRow(label = "Still Missed", value = "${performance.missed}")
            }
        }

        // Age & Urgent Filter Chips
        item(key = "missed_filter_chips") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(OverdueFilter.values()) { filter ->
                    val isSelected = activeFilter == filter
                    val unselectedBg = if (isDark) Color(0x33545458) else Color(0xFFE5E5EA)
                    val unselectedText = if (isDark) IosDarkLabelPrimary else IosLabelPrimary

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) accentBlue else unselectedBg)
                            .semantics {
                                role = Role.Button
                                selected = isSelected
                            }
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onFilterSelected(filter)
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = filter.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else unselectedText,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                }
            }
        }

        // Missed / Urgent Policies List
        if (displayedList.isEmpty()) {
            item(key = "missed_empty_bucket") {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 1.dp
                ) {
                    Text(
                        text = if (activeFilter == OverdueFilter.DUE_SOON) {
                            "No policies expiring within 7 days."
                        } else {
                            "No overdue policies in ${activeFilter.label}."
                        },
                        color = labelSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(
                items = displayedList,
                key = { "missed_${activeFilter.name}_${it.vehicle.regNo}" }
            ) { item ->
                MissedRenewalCard(
                    item = item,
                    onNavigateToDetail = { onNavigateToVehicleDetail(item.vehicle.regNo) },
                    onRenew = { onNavigateToRenew(item.vehicle.regNo) },
                    onCall = {
                        if (item.vehicle.customer.mobile.any { it.isDigit() }) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.vehicle.customer.mobile}"))
                            context.startActivity(intent)
                        }
                    },
                    onWhatsApp = {
                        launchMissedWhatsApp(context, item)
                    }
                )
            }
        }
    }
}

private fun launchMissedWhatsApp(context: Context, item: VehicleWithPolicies) {
    val cleanMobile = item.vehicle.customer.mobile.filter { it.isDigit() }
    if (cleanMobile.isEmpty()) return
    val formattedMobile = if (cleanMobile.length == 10) "91$cleanMobile" else cleanMobile
    val p = item.currentPolicy
    val msg = AdvisorProfileManager.formatRenewalMessage(
        context = context,
        customerName = item.vehicle.customer.name,
        regNo = item.vehicle.regNo,
        insurer = p?.insurer ?: "",
        expiryDate = p?.expiryDate?.let { UiFormatters.formatDate(it) } ?: "Overdue",
        premium = p?.totalPremium?.let { UiFormatters.formatCurrency(it) } ?: ""
    )
    val url = "https://api.whatsapp.com/send?phone=$formattedMobile&text=${Uri.encode(msg)}"
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

@Composable
fun MissedRenewalCard(
    item: VehicleWithPolicies,
    onNavigateToDetail: () -> Unit,
    onRenew: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    val policy = item.currentPolicy
    val daysRemaining = policy?.computeDaysRemaining() ?: 0L
    val status = policy?.computeCurrentStatus() ?: PolicyStatus.OVERDUE

    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val redAccent = if (isDark) AppleDarkSystemRed else StatusOverdueRed
    val blueAccent = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
    val greenAccent = if (isDark) AppleDarkSystemGreen else AppleSystemGreen
    val hasPhoneDigits = item.vehicle.customer.mobile.any { it.isDigit() }

    // Non-nested interactive structure: top Column opens detail; bottom Row has sibling Call/WA/Renew buttons
    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .semantics { role = Role.Button }
                .clickable(onClickLabel = "Open details for ${item.vehicle.regNo}") {
                    onNavigateToDetail()
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HsrpNumberPlate(
                    regNo = item.vehicle.regNo,
                    makeModel = item.vehicle.makeModel,
                    style = item.vehicle.plateStyle,
                    modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                )
                DaysRemainingBadge(
                    daysRemaining = daysRemaining,
                    status = status
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.vehicle.customer.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = labelPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${policy?.insurer ?: "Insurer"} • Exp ${policy?.expiryDate?.let { UiFormatters.formatDate(it) } ?: "N/A"}",
                fontSize = 11.5.sp,
                color = labelSecondary,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
            if (policy != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Premium at risk: ${UiFormatters.formatCurrency(policy.totalPremium)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = redAccent,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons Row (siblings, not nested inside card clickable)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (hasPhoneDigits) {
                AppleSecondaryButton(
                    text = "Call",
                    onClick = onCall,
                    icon = AppleIcons.Phone,
                    tintColor = blueAccent,
                    height = 36.dp,
                    modifier = Modifier.weight(1f)
                )

                AppleSecondaryButton(
                    text = "WhatsApp",
                    onClick = onWhatsApp,
                    icon = AppleIcons.WhatsApp,
                    tintColor = greenAccent,
                    height = 36.dp,
                    modifier = Modifier.weight(1.2f)
                )
            }

            AppleButton(
                text = "Renew",
                onClick = onRenew,
                height = 36.dp,
                modifier = Modifier.weight(1.1f)
            )
        }
    }
}

@Composable
fun AnalyticsTabContent(
    aggregate: ReportsAggregate,
    listState: LazyListState = rememberLazyListState(),
    topPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    val analytics = aggregate.analytics
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
    val periodLabel = formatReportPeriodChipLabel(aggregate.period)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topPadding, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Book Size Card
        item(key = "analytics_book_size") {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Active Book Size", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = labelPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Vehicles", fontSize = 11.sp, color = labelSecondary)
                        Text(
                            text = "${analytics.bookSizeVehicles}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelPrimary,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Book Value", fontSize = 11.sp, color = labelSecondary)
                        Text(
                            text = UiFormatters.formatCurrency(analytics.bookSizePremium),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentBlue,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                }
            }
        }

        // Renewal Pipeline
        item(key = "analytics_pipeline") {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Upcoming Renewal Pipeline", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = labelPrimary)
                    DetailRow(
                        label = "Next 3 Months",
                        value = "${analytics.renewalPipeline3Months.first} ${if (analytics.renewalPipeline3Months.first == 1) "policy" else "policies"} (${UiFormatters.formatCurrency(analytics.renewalPipeline3Months.second)})"
                    )
                    DetailRow(
                        label = "Next 6 Months",
                        value = "${analytics.renewalPipeline6Months.first} ${if (analytics.renewalPipeline6Months.first == 1) "policy" else "policies"} (${UiFormatters.formatCurrency(analytics.renewalPipeline6Months.second)})"
                    )
                    DetailRow(
                        label = "Next 12 Months",
                        value = "${analytics.renewalPipeline12Months.first} ${if (analytics.renewalPipeline12Months.first == 1) "policy" else "policies"} (${UiFormatters.formatCurrency(analytics.renewalPipeline12Months.second)})"
                    )
                }
            }
        }

        // Renewal Performance Summary
        item(key = "analytics_retention") {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Retention Metrics ($periodLabel)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = labelPrimary)
                    DetailRow(
                        label = "Overall Renewal Rate",
                        value = "${String.format(Locale.US, "%.1f", analytics.renewalRatePercent)}%"
                    )
                    DetailRow(
                        label = "On-Time Renewal Rate",
                        value = "${String.format(Locale.US, "%.1f", analytics.onTimeRatePercent)}%"
                    )
                    DetailRow(
                        label = "Premium at Risk (Overdue)",
                        value = UiFormatters.formatCurrency(analytics.premiumAtRisk)
                    )
                    DetailRow(
                        label = "Period Commission Earned",
                        value = UiFormatters.formatCurrency(analytics.commissionEarned)
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlySalesTrendCard(trends: List<MonthlyTrendItem>) {
    val maxPremium = trends.maxOfOrNull { it.totalPremium }?.coerceAtLeast(1000.0) ?: 1000.0
    val isDark = isSystemInDarkTheme()
    val reduceMotion = LocalReduceMotion.current
    val haptic = LocalHapticFeedback.current
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val labelTertiary = if (isDark) IosDarkLabelTertiary else IosLabelTertiary
    val newBizColor = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
    val renewalColor = if (isDark) AppleDarkSystemPurple else AppleSystemPurple
    val barTrackColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)

    var selectedIndex by remember(trends.size) { mutableIntStateOf((trends.size - 1).coerceAtLeast(0)) }
    var isScrubbing by remember { mutableStateOf(false) }
    var chartWidthPx by remember { mutableIntStateOf(0) }
    var calloutWidthPx by remember { mutableIntStateOf(0) }
    var hasPositionedCallout by remember { mutableStateOf(false) }

    val safeSelectedIndex = selectedIndex.coerceIn(0, (trends.size - 1).coerceAtLeast(0))
    val activeTrend = trends.getOrNull(safeSelectedIndex)

    val calloutXAnim = remember { Animatable(0f) }

    val entryProgress = remember(trends) { Animatable(if (reduceMotion) 1f else 0f) }
    LaunchedEffect(trends, reduceMotion) {
        if (reduceMotion) {
            entryProgress.snapTo(1f)
        } else {
            entryProgress.snapTo(0f)
            entryProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
            )
        }
    }

    LaunchedEffect(safeSelectedIndex, chartWidthPx, calloutWidthPx, isScrubbing, reduceMotion) {
        if (chartWidthPx > 0 && calloutWidthPx > 0 && trends.isNotEmpty()) {
            val rawCenterPx = ((safeSelectedIndex + 0.5f) / trends.size.toFloat()) * chartWidthPx
            val maxLeftPx = (chartWidthPx - calloutWidthPx).coerceAtLeast(0).toFloat()
            val targetLeftPx = (rawCenterPx - calloutWidthPx / 2f).coerceIn(0f, maxLeftPx)

            if (!hasPositionedCallout || isScrubbing || reduceMotion) {
                calloutXAnim.snapTo(targetLeftPx)
                hasPositionedCallout = true
            } else {
                calloutXAnim.animateTo(
                    targetValue = targetLeftPx,
                    animationSpec = AppleSprings.Smooth // critically damped: zero overshoot past bounds
                )
            }
        }
    }

    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "12-Month Sales Trend",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelPrimary
                )
                Text(
                    text = "Tap or drag horizontally across bars",
                    fontSize = 11.sp,
                    color = labelSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(newBizColor, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontSize = 11.sp, color = labelSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(renewalColor, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ren", fontSize = 11.sp, color = labelSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Clamped Floating Callout above chart bars
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
        ) {
            if (activeTrend != null) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = calloutXAnim.value.roundToInt(), y = 0) }
                        .onSizeChanged { calloutWidthPx = it.width }
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0xE62C2C2E) else Color(0xF5FFFFFF))
                        .border(
                            width = 0.75.dp,
                            color = if (isDark) Color(0x40FFFFFF) else Color(0x26000000),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${activeTrend.monthLabel}: New ${UiFormatters.formatCurrency(activeTrend.newBusinessPremium)} • Ren ${UiFormatters.formatCurrency(activeTrend.renewalPremium)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Canonical 12-Month Bars Row (supports pointer horizontal drag scrub + tap bar selection)
        val totalBarHeight = 96.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { chartWidthPx = it.width }
                .pointerInput(trends.size, chartWidthPx) {
                    if (trends.isEmpty() || chartWidthPx <= 0) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isScrubbing = true
                            val idx = ((offset.x / chartWidthPx.toFloat()) * trends.size)
                                .toInt()
                                .coerceIn(0, trends.lastIndex)
                            if (idx != selectedIndex) {
                                selectedIndex = idx
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val idx = ((change.position.x / chartWidthPx.toFloat()) * trends.size)
                                .toInt()
                                .coerceIn(0, trends.lastIndex)
                            if (idx != selectedIndex) {
                                selectedIndex = idx
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        },
                        onDragEnd = { isScrubbing = false },
                        onDragCancel = { isScrubbing = false }
                    )
                },
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            trends.forEachIndexed { index, item ->
                val isSelected = index == safeSelectedIndex
                val colAlpha by animateFloatAsState(
                    targetValue = if (isScrubbing && !isSelected) 0.42f else 1f,
                    animationSpec = if (reduceMotion) snap() else tween(140),
                    label = "chart_col_alpha"
                )
                val totalRatio = ((item.totalPremium / maxPremium).toFloat().coerceIn(0f, 1f)) * entryProgress.value
                val nbPart = item.newBusinessPremium.toFloat().coerceAtLeast(0f)
                val renPart = item.renewalPremium.toFloat().coerceAtLeast(0f)
                val barDesc = "${item.monthLabel}: New ${UiFormatters.formatCurrency(item.newBusinessPremium)}, Renewal ${UiFormatters.formatCurrency(item.renewalPremium)}, Total ${UiFormatters.formatCurrency(item.totalPremium)}"
                val shortMonth = item.monthLabel.substringBefore(" ").take(3)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .alpha(colAlpha)
                        .clip(RoundedCornerShape(6.dp))
                        .semantics {
                            role = Role.Button
                            selected = isSelected
                            contentDescription = barDesc
                        }
                        .clickable {
                            if (selectedIndex != index) {
                                selectedIndex = index
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(totalBarHeight)
                            .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                            .background(barTrackColor)
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        width = 1.5.dp,
                                        color = newBizColor,
                                        shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 3.dp, bottomEnd = 3.dp)
                                    )
                                } else Modifier
                            ),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (totalRatio > 0f && (nbPart + renPart) > 0f) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(totalBarHeight * totalRatio.coerceAtLeast(0.05f))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            ) {
                                if (renPart > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(renPart)
                                            .background(renewalColor)
                                    )
                                }
                                if (nbPart > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(nbPart)
                                            .background(newBizColor)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = shortMonth,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) newBizColor else if (item.totalPremium > 0) labelSecondary else labelTertiary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

