package com.policydesk.app.ui.monthview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.policydesk.app.ui.components.BindBackToTop
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import com.policydesk.app.ui.theme.AppleIcons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.components.LiquidGlassSegmentedControl
import com.policydesk.app.ui.components.LiquidGlassTopBar
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.dashboard.VehicleExpiryCard
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemRed
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkLabelTertiary
import com.policydesk.app.ui.theme.IosDarkSeparator
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.IosSeparator
import com.policydesk.app.ui.theme.IosSystemBackground
import java.time.LocalDate

enum class MonthDisplayMode(val label: String) {
    TIMELINE("Timeline List"),
    GRID("Calendar Grid")
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
    var selectedCalendarDay by remember { mutableStateOf<Int?>(null) }
    val isDark = isSystemInDarkTheme()
    val actionTint = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    Scaffold(
        topBar = {
            LiquidGlassTopBar(
                title = "Month Timeline",
                subtitle = "Renewal & Expiry Schedule",
                onNavigateBack = onNavigateBack,
                actions = {
                    TextButton(
                        onClick = viewModel::onToday,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "Today",
                            fontWeight = FontWeight.SemiBold,
                            color = actionTint,
                            fontSize = 14.sp
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        val isDark = isSystemInDarkTheme()
        val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
        val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // Month Picker Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        selectedCalendarDay = null
                        viewModel.onPreviousMonth()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x33545458) else Color.White.copy(alpha = 0.8f))
                        .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Previous Month",
                        tint = AppleSystemBlue,
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

                IconButton(
                    onClick = {
                        selectedCalendarDay = null
                        viewModel.onNextMonth()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x33545458) else Color.White.copy(alpha = 0.8f))
                        .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Next Month",
                        tint = AppleSystemBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Summary Liquid Glass Card
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Policies Expiring", fontSize = 12.sp, color = labelSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${data.totalPolicies}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = labelPrimary)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(if (isDark) IosDarkSeparator else Color(0x26000000)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Premium", fontSize = 12.sp, color = labelSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(UiFormatters.formatCurrency(data.totalPremium), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppleSystemBlue)
                    }
                }
            }

            // Apple Liquid Glass Segmented Control
            LiquidGlassSegmentedControl(
                items = MonthDisplayMode.values().toList(),
                selectedItem = displayMode,
                onItemSelected = { displayMode = it },
                labelProvider = { it.label },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Content: Grid View or Timeline View
            if (displayMode == MonthDisplayMode.GRID) {
                CalendarGridView(
                    year = selection.year,
                    month = selection.month,
                    dateGroupedPolicies = data.dateGroupedPolicies,
                    selectedDay = selectedCalendarDay,
                    onDaySelected = { selectedCalendarDay = it },
                    onNavigateToVehicleDetail = onNavigateToVehicleDetail
                )
            } else {
                TimelineListView(
                    year = selection.year,
                    month = selection.month,
                    dateGroupedPolicies = data.dateGroupedPolicies,
                    onNavigateToVehicleDetail = onNavigateToVehicleDetail
                )
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
    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val daysInMonth = firstDayOfMonth.lengthOfMonth()
    val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value - 1
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val isDark = isSystemInDarkTheme()
    val dayHeaderColor = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Day of Week Headers
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
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

        // Calendar Day Cells in Liquid Glass Container
        val totalCells = dayOfWeekOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = 2.dp
        ) {
            for (r in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        val dayNumber = cellIndex - dayOfWeekOffset + 1

                        if (dayNumber in 1..daysInMonth) {
                            val policiesOnDay = dateGroupedPolicies[dayNumber] ?: emptyList()
                            val isSelected = selectedDay == dayNumber
                            val dayShape = RoundedCornerShape(10.dp)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(dayShape)
                                    .background(
                                        if (isSelected) AppleSystemBlue
                                        else if (policiesOnDay.isNotEmpty()) AppleSystemBlue.copy(alpha = 0.08f)
                                        else Color.Transparent,
                                        dayShape
                                    )
                                    .clickable { onDaySelected(dayNumber) }
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val isDark = isSystemInDarkTheme()
                                    Text(
                                        text = dayNumber.toString(),
                                        fontSize = 13.sp,
                                        fontWeight = if (policiesOnDay.isNotEmpty() || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else (if (isDark) IosDarkLabelPrimary else IosLabelPrimary)
                                    )

                                    if (policiesOnDay.isNotEmpty()) {
                                        val hasUrgent = policiesOnDay.any { (it.currentPolicy?.computeDaysRemaining() ?: 0) <= 7 }
                                        val dotColor = if (isSelected) Color.White else if (hasUrgent) AppleSystemRed else AppleSystemGreen
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .background(dotColor, CircleShape)
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f).height(44.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Day's Policies List
        if (selectedDay != null) {
            val list = dateGroupedPolicies[selectedDay] ?: emptyList()
            val isDark = isSystemInDarkTheme()
            val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
            val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

            Text(
                text = "$selectedDay ${LocalDate.of(year, month, selectedDay).month.name.lowercase().replaceFirstChar { it.uppercase() }} (${list.size} expiring)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = labelPrimary,
                letterSpacing = (-0.2).sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (list.isEmpty()) {
                Text("No policies expiring on this date.", fontSize = 13.sp, color = labelSecondary)
            } else {
                val listState = rememberLazyListState()
                BindBackToTop(listState)
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(
                        items = list,
                        key = { it.vehicle.regNo },
                        contentType = { "month_vehicle_item" }
                    ) { vehicle ->
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

@Composable
fun TimelineListView(
    year: Int,
    month: Int,
    dateGroupedPolicies: Map<Int, List<VehicleWithPolicies>>,
    onNavigateToVehicleDetail: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    if (dateGroupedPolicies.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(if (isDark) Color(0x33545458) else Color.White.copy(alpha = 0.8f), CircleShape)
                        .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = AppleIcons.Calendar, contentDescription = null, tint = if (isDark) Color(0xFF8E8E93) else IosLabelTertiary, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("No policies expire this month", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = labelPrimary, letterSpacing = (-0.2).sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Use the navigation arrows to explore other months.", fontSize = 13.sp, color = labelSecondary)
            }
        }
    } else {
        val listState = rememberLazyListState()
        BindBackToTop(listState)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            dateGroupedPolicies.forEach { (day, vehicles) ->
                item(key = "header_$day") {
                    val localDate = LocalDate.of(year, month, day)
                    val dateStr = "${day} ${localDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} $year"
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = dateStr, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = labelPrimary)
                        Text(text = "${vehicles.size} ${if (vehicles.size == 1) "policy" else "policies"}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = labelSecondary)
                    }
                }

                items(
                    items = vehicles,
                    key = { it.vehicle.regNo },
                    contentType = { "month_vehicle_item" }
                ) { vehicle ->
                    VehicleExpiryCard(
                        item = vehicle,
                        onClick = { onNavigateToVehicleDetail(vehicle.vehicle.regNo) }
                    )
                }
            }
        }
    }
}
