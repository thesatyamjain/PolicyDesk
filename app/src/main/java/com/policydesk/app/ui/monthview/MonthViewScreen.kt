package com.policydesk.app.ui.monthview

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.ui.components.AppleSecondaryButton
import com.policydesk.app.ui.components.AppleSprings
import com.policydesk.app.ui.components.BindBackToTop
import com.policydesk.app.ui.components.CupertinoTopBarAction
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.components.LiquidGlassSegmentedControl
import com.policydesk.app.ui.components.LiquidGlassTopBar
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.dashboard.VehicleExpiryCard
import com.policydesk.app.ui.navigation.EmptyStateHaloBadge
import com.policydesk.app.ui.navigation.FloatingGlassHeader
import com.policydesk.app.ui.navigation.LocalReduceMotion
import com.policydesk.app.ui.navigation.aaBlueFill
import com.policydesk.app.ui.navigation.rememberAnimatedCount
import com.policydesk.app.ui.navigation.rememberIsListScrolled
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleDarkSystemGreen
import com.policydesk.app.ui.theme.AppleDarkSystemRed
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemRed
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkSeparator
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class MonthDisplayMode(val label: String) {
    TIMELINE("Timeline List"),
    GRID("Calendar Grid")
}

@Immutable
private data class CalendarDayCellModel(
    val dayNumber: Int,
    val policyCount: Int,
    val hasUrgent: Boolean,
    val isToday: Boolean
)

private val DAY_HEADER_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

private fun LazyItemScope.modeItemModifier(reduceMotion: Boolean): Modifier {
    return if (reduceMotion) {
        Modifier
    } else {
        Modifier.animateItem(
            fadeInSpec = tween<Float>(durationMillis = 220),
            fadeOutSpec = tween<Float>(durationMillis = 160),
            placementSpec = spring<IntOffset>(dampingRatio = 1.0f, stiffness = 340f)
        )
    }
}

@Composable
fun MonthViewScreen(
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToVehicleDetail: (String) -> Unit,
    viewModel: MonthViewModel = hiltViewModel()
) {
    val selection by viewModel.selection.collectAsStateWithLifecycle()
    val data by viewModel.monthData.collectAsStateWithLifecycle()

    var displayMode by remember { mutableStateOf(MonthDisplayMode.TIMELINE) }
    val isDark = isSystemInDarkTheme()
    val reduceMotion = LocalReduceMotion.current
    val haptic = LocalHapticFeedback.current
    val actionTint = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val greenAccent = if (isDark) AppleDarkSystemGreen else AppleSystemGreen

    val screenHazeState = remember { HazeState() }
    val listState = rememberLazyListState()
    val isScrolled by rememberIsListScrolled(listState)
    BindBackToTop(listState)

    val firstDayOfMonth = remember(selection.year, selection.month) {
        LocalDate.of(selection.year, selection.month, 1)
    }
    val daysInMonth = firstDayOfMonth.lengthOfMonth()
    val safeSelectedDay = selection.selectedDay.coerceIn(1, daysInMonth)

    val today = remember { LocalDate.now() }
    val calendarCells = remember(selection.year, selection.month, data.dateGroupedPolicies, today) {
        val todayEpochDay = today.toEpochDay()
        (1..daysInMonth).map { day ->
            val policies = data.dateGroupedPolicies[day].orEmpty()
            val hasUrgent = policies.any { (it.currentPolicy?.computeDaysRemaining(todayEpochDay) ?: 0L) <= 7L }
            CalendarDayCellModel(
                dayNumber = day,
                policyCount = policies.size,
                hasUrgent = hasUrgent,
                isToday = (today.year == selection.year && today.monthValue == selection.month && today.dayOfMonth == day)
            )
        }
    }

    val sortedDayGroups = remember(data.dateGroupedPolicies) {
        data.dateGroupedPolicies.entries.map { (day, list) ->
            day to list.sortedWith(
                compareBy<VehicleWithPolicies> { it.currentPolicy?.expiryDate ?: Long.MAX_VALUE }
                    .thenBy { it.vehicle.regNo }
            )
        }
    }

    val animatedTotalPolicies = rememberAnimatedCount(data.totalPolicies)

    Scaffold(
        topBar = {
            FloatingGlassHeader(
                hazeState = screenHazeState,
                isScrolled = isScrolled
            ) {
                LiquidGlassTopBar(
                    title = "Month Timeline",
                    subtitle = "Renewal & Expiry Schedule",
                    onNavigateBack = onNavigateBack,
                    actions = {
                        CupertinoTopBarAction(
                            text = "Today",
                            onClick = viewModel::onToday,
                            isProminent = true,
                            tint = actionTint
                        )
                    }
                )
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = screenHazeState),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = 140.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Month Picker Bar
            item(key = "month_picker") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x33545458) else Color.White.copy(alpha = 0.8f))
                            .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color.White, CircleShape)
                            .semantics { role = Role.Button }
                            .clickable(onClickLabel = "Previous Month") {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.onPreviousMonth()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppleIcons.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = actionTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = UiFormatters.formatMonthYear(selection.year, selection.month),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelPrimary,
                        letterSpacing = (-0.3).sp
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x33545458) else Color.White.copy(alpha = 0.8f))
                            .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color.White, CircleShape)
                            .semantics { role = Role.Button }
                            .clickable(onClickLabel = "Next Month") {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.onNextMonth()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppleIcons.ChevronRight,
                            contentDescription = "Next Month",
                            tint = actionTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Summary Liquid Glass Card
            item(key = "month_summary") {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (data.totalPolicies == 1) "Policy" else "Policies",
                                fontSize = 11.sp,
                                color = labelSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$animatedTotalPolicies",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = labelPrimary,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(if (isDark) IosDarkSeparator else Color(0x26000000))
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Premium", fontSize = 11.sp, color = labelSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = UiFormatters.formatCurrency(data.totalPremium),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = actionTint,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                        if (data.totalCommission > 0.0) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(28.dp)
                                    .background(if (isDark) IosDarkSeparator else Color(0x26000000))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Est. Commission", fontSize = 11.sp, color = labelSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = UiFormatters.formatCurrency(data.totalCommission),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = greenAccent,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                            }
                        }
                    }
                }
            }

            // Apple Liquid Glass Segmented Control
            item(key = "month_mode_seg") {
                LiquidGlassSegmentedControl(
                    items = MonthDisplayMode.values().toList(),
                    selectedItem = displayMode,
                    onItemSelected = { displayMode = it },
                    labelProvider = { it.label },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            // Content: Calendar Grid or Timeline List (smooth item animation on switch)
            if (displayMode == MonthDisplayMode.GRID) {
                item(
                    key = "calendar_grid_${selection.year}_${selection.month}",
                    contentType = "calendar_grid"
                ) {
                    Box(modifier = modeItemModifier(reduceMotion).padding(horizontal = 16.dp)) {
                        StaticCalendarGridCard(
                            year = selection.year,
                            month = selection.month,
                            dayCells = calendarCells,
                            selectedDay = safeSelectedDay,
                            onDaySelected = { day ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.onDaySelected(day)
                            }
                        )
                    }
                }

                val selectedList = data.dateGroupedPolicies[safeSelectedDay]
                    ?.sortedWith(
                        compareBy<VehicleWithPolicies> { it.currentPolicy?.expiryDate ?: Long.MAX_VALUE }
                            .thenBy { it.vehicle.regNo }
                    )
                    .orEmpty()
                val selectedDateLabel = LocalDate.of(selection.year, selection.month, safeSelectedDay)
                    .format(DAY_HEADER_FORMATTER)

                item(key = "cal_day_header", contentType = "day_header") {
                    Row(
                        modifier = modeItemModifier(reduceMotion)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedDateLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelPrimary,
                            letterSpacing = (-0.2).sp,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                        Text(
                            text = "${selectedList.size} ${if (selectedList.size == 1) "policy" else "policies"} expiring",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = labelSecondary,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                }

                if (selectedList.isEmpty()) {
                    item(key = "cal_day_empty", contentType = "empty_day") {
                        LiquidGlassCard(
                            modifier = modeItemModifier(reduceMotion)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = 1.dp
                        ) {
                            Text(
                                text = "No policies expiring on $selectedDateLabel.",
                                fontSize = 13.sp,
                                color = labelSecondary,
                                style = TextStyle(fontFeatureSettings = "tnum")
                            )
                        }
                    }
                } else {
                    items(
                        items = selectedList,
                        key = { "grid_${selection.year}_${selection.month}_${safeSelectedDay}_${it.vehicle.regNo}" },
                        contentType = { "month_vehicle_item" }
                    ) { vehicle ->
                        Box(modifier = modeItemModifier(reduceMotion).padding(horizontal = 16.dp)) {
                            VehicleExpiryCard(
                                item = vehicle,
                                onClick = { onNavigateToVehicleDetail(vehicle.vehicle.regNo) }
                            )
                        }
                    }
                }
            } else {
                if (sortedDayGroups.isEmpty()) {
                    item(key = "timeline_empty", contentType = "month_empty") {
                        LiquidGlassCard(
                            modifier = modeItemModifier(reduceMotion)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(20.dp),
                            elevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                EmptyStateHaloBadge(
                                    icon = AppleIcons.Calendar,
                                    tint = actionTint
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "No policies expire this month",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = labelPrimary,
                                    letterSpacing = (-0.2).sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Use the navigation arrows above to explore other months.",
                                    fontSize = 13.sp,
                                    color = labelSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                AppleSecondaryButton(
                                    text = "Jump to Current Month",
                                    onClick = viewModel::onToday,
                                    icon = AppleIcons.Calendar,
                                    height = 42.dp
                                )
                            }
                        }
                    }
                } else {
                    sortedDayGroups.forEach { (day, vehicles) ->
                        val localDate = LocalDate.of(selection.year, selection.month, day.coerceIn(1, daysInMonth))
                        val dateStr = localDate.format(DAY_HEADER_FORMATTER)

                        item(
                            key = "tl_header_${selection.year}_${selection.month}_$day",
                            contentType = "day_header"
                        ) {
                            Row(
                                modifier = modeItemModifier(reduceMotion)
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateStr,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = labelPrimary,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                                Text(
                                    text = "${vehicles.size} ${if (vehicles.size == 1) "policy" else "policies"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = labelSecondary,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                            }
                        }

                        items(
                            items = vehicles,
                            key = { "tl_${selection.year}_${selection.month}_${day}_${it.vehicle.regNo}" },
                            contentType = { "month_vehicle_item" }
                        ) { vehicle ->
                            Box(modifier = modeItemModifier(reduceMotion).padding(horizontal = 16.dp)) {
                                VehicleExpiryCard(
                                    item = vehicle,
                                    onClick = { onNavigateToVehicleDetail(vehicle.vehicle.regNo) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaticCalendarGridCard(
    year: Int,
    month: Int,
    dayCells: List<CalendarDayCellModel>,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit
) {
    val firstDayOfMonth = remember(year, month) { LocalDate.of(year, month, 1) }
    val daysInMonth = firstDayOfMonth.lengthOfMonth()
    val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value - 1
    val totalCells = dayOfWeekOffset + daysInMonth
    val rows = (totalCells + 6) / 7
    val daysOfWeek = remember { listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") }
    val monthName = remember(year, month) {
        firstDayOfMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }
    }
    val isDark = isSystemInDarkTheme()
    val reduceMotion = LocalReduceMotion.current
    val dayHeaderColor = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = dayHeaderColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = 2.dp
        ) {
            for (r in 0 until rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        val dayNumber = cellIndex - dayOfWeekOffset + 1

                        if (dayNumber in 1..daysInMonth) {
                            val cellModel = dayCells[dayNumber - 1]
                            CalendarDayCell(
                                info = cellModel,
                                isSelected = selectedDay == dayNumber,
                                isDark = isDark,
                                reduceMotion = reduceMotion,
                                monthName = monthName,
                                year = year,
                                onClick = { onDaySelected(dayNumber) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f).height(44.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    info: CalendarDayCellModel,
    isSelected: Boolean,
    isDark: Boolean,
    reduceMotion: Boolean,
    monthName: String,
    year: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
    val urgentRed = if (isDark) AppleDarkSystemRed else AppleSystemRed
    val normalGreen = if (isDark) AppleDarkSystemGreen else AppleSystemGreen
    val dayShape = remember { RoundedCornerShape(10.dp) }

    val targetBg = when {
        isSelected -> aaBlueFill(isDark)
        info.policyCount > 0 -> activeBlue.copy(alpha = if (isDark) 0.16f else 0.10f)
        else -> Color.Transparent
    }
    val cellBg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = if (reduceMotion) snap() else tween(durationMillis = 160),
        label = "cal_day_bg"
    )
    val textColor = if (isSelected) {
        Color.White
    } else if (isDark) {
        IosDarkLabelPrimary
    } else {
        IosLabelPrimary
    }
    val dotColor = when {
        isSelected -> Color.White
        info.hasUrgent -> urgentRed
        else -> normalGreen
    }
    val policyWord = if (info.policyCount == 1) "policy" else "policies"
    val cellDesc = "${info.dayNumber} $monthName $year, ${info.policyCount} $policyWord expiring"

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(dayShape)
            .background(cellBg, dayShape)
            .then(
                if (info.isToday && !isSelected) {
                    Modifier.border(1.dp, activeBlue.copy(alpha = 0.65f), dayShape)
                } else Modifier
            )
            .semantics {
                role = Role.Button
                selected = isSelected
                contentDescription = cellDesc
            }
            .clickable(onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = info.dayNumber.toString(),
                fontSize = 13.sp,
                fontWeight = if (info.policyCount > 0 || isSelected || info.isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (info.policyCount > 0) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(dotColor, CircleShape)
                )
            } else {
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}

@Composable
fun CalendarGridView(
    year: Int,
    month: Int,
    dateGroupedPolicies: Map<Int, List<VehicleWithPolicies>>,
    selectedDay: Int?,
    onDaySelected: (Int) -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit
) {
    val firstDayOfMonth = remember(year, month) { LocalDate.of(year, month, 1) }
    val daysInMonth = firstDayOfMonth.lengthOfMonth()
    val safeDay = (selectedDay ?: 1).coerceIn(1, daysInMonth)
    val today = remember { LocalDate.now() }
    val dayCells = remember(year, month, dateGroupedPolicies, today) {
        val todayEpochDay = today.toEpochDay()
        (1..daysInMonth).map { day ->
            val policies = dateGroupedPolicies[day].orEmpty()
            CalendarDayCellModel(
                dayNumber = day,
                policyCount = policies.size,
                hasUrgent = policies.any { (it.currentPolicy?.computeDaysRemaining(todayEpochDay) ?: 0L) <= 7L },
                isToday = (today.year == year && today.monthValue == month && today.dayOfMonth == day)
            )
        }
    }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        StaticCalendarGridCard(
            year = year,
            month = month,
            dayCells = dayCells,
            selectedDay = safeDay,
            onDaySelected = onDaySelected
        )
    }
}

@Composable
fun TimelineListView(
    year: Int,
    month: Int,
    dateGroupedPolicies: Map<Int, List<VehicleWithPolicies>>,
    onNavigateToVehicleDetail: (String) -> Unit,
    onJumpToCurrentMonth: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val actionTint = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    if (dateGroupedPolicies.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EmptyStateHaloBadge(icon = AppleIcons.Calendar, tint = actionTint)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No policies expire this month", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = labelPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Use the navigation arrows to explore other months.", fontSize = 13.sp, color = labelSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                AppleSecondaryButton(
                    text = "Jump to Current Month",
                    onClick = onJumpToCurrentMonth,
                    icon = AppleIcons.Calendar,
                    height = 42.dp
                )
            }
        }
    } else {
        val listState = rememberLazyListState()
        BindBackToTop(listState)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            dateGroupedPolicies.forEach { (day, vehicles) ->
                item(key = "header_$day") {
                    val localDate = LocalDate.of(year, month, day)
                    val dateStr = localDate.format(DAY_HEADER_FORMATTER)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = dateStr, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = labelPrimary)
                        Text(text = "${vehicles.size} ${if (vehicles.size == 1) "policy" else "policies"}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = labelSecondary)
                    }
                }
                items(items = vehicles, key = { it.vehicle.regNo }) { vehicle ->
                    VehicleExpiryCard(item = vehicle, onClick = { onNavigateToVehicleDetail(vehicle.vehicle.regNo) })
                }
            }
        }
    }
}
