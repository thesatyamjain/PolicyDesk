package com.policydesk.app.ui.reports

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.domain.model.ReportPeriod
import com.policydesk.app.domain.model.ReportsAggregate
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.ui.components.DaysRemainingBadge
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.dashboard.VehicleExpiryCard
import com.policydesk.app.ui.detail.DetailRow
import com.policydesk.app.ui.theme.CardBackground
import com.policydesk.app.ui.theme.Navy700
import com.policydesk.app.ui.theme.Navy800
import com.policydesk.app.ui.theme.Navy900
import com.policydesk.app.ui.theme.SapphireBlue
import com.policydesk.app.ui.theme.SapphireBlueLight
import com.policydesk.app.ui.theme.Slate100
import com.policydesk.app.ui.theme.Slate200
import com.policydesk.app.ui.theme.Slate400
import com.policydesk.app.ui.theme.Slate50
import com.policydesk.app.ui.theme.Slate600
import com.policydesk.app.ui.theme.StatusActiveGreen
import com.policydesk.app.ui.theme.StatusActiveGreenBg
import com.policydesk.app.ui.theme.StatusExpiringAmber
import com.policydesk.app.ui.theme.StatusExpiringAmberBg
import com.policydesk.app.ui.theme.StatusOverdueRed
import com.policydesk.app.ui.theme.StatusOverdueRedBg
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit,
    onNavigateToRenew: (String) -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val overdueFilter by viewModel.overdueFilter.collectAsStateWithLifecycle()
    val aggregate by viewModel.aggregateData.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val now = LocalDate.now()

    Scaffold(
        topBar = {
            com.policydesk.app.ui.components.LiquidGlassTopBar(
                title = "Reports & Analytics",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = com.policydesk.app.ui.theme.IosSystemBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Apple Liquid Glass Segmented Control for Tabs
            com.policydesk.app.ui.components.LiquidGlassSegmentedControl(
                items = ReportsTab.values().toList(),
                selectedItem = selectedTab,
                onItemSelected = { viewModel.onTabSelected(it) },
                labelProvider = { it.title },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Period Selector Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val thisMonth = ReportPeriod.Month(now.year, now.monthValue)
                val thisQuarter = ReportPeriod.Quarter(now.year, (now.monthValue - 1) / 3 + 1)
                val currentFY = ReportPeriod.FinancialYear(if (now.monthValue >= 4) now.year else now.year - 1)

                val periods = listOf(thisMonth, thisQuarter, currentFY)

                items(periods) { period ->
                    val isSelected = selectedPeriod.label == period.label
                    val pillShape = RoundedCornerShape(10.dp)
                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(
                                color = if (isSelected) com.policydesk.app.ui.theme.AppleSystemBlue else Color.White.copy(alpha = 0.72f),
                                shape = pillShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) com.policydesk.app.ui.theme.AppleSystemBlue else Color.White.copy(alpha = 0.5f),
                                shape = pillShape
                            )
                            .clickable { viewModel.onPeriodSelected(period) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = period.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else com.policydesk.app.ui.theme.IosLabelPrimary
                        )
                    }
                }
            }

            if (aggregate == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading report data...", color = Slate600)
                }
            } else {
                when (selectedTab) {
                    ReportsTab.SALES -> SalesTabContent(
                        aggregate = aggregate!!,
                        onNavigateToVehicleDetail = onNavigateToVehicleDetail
                    )
                    ReportsTab.MISSED_RENEWALS -> MissedRenewalsTabContent(
                        aggregate = aggregate!!,
                        activeFilter = overdueFilter,
                        onFilterSelected = viewModel::onOverdueFilterSelected,
                        onNavigateToVehicleDetail = onNavigateToVehicleDetail,
                        onNavigateToRenew = onNavigateToRenew,
                        onCloseVehicle = viewModel::closeVehicle
                    )
                    ReportsTab.ANALYTICS -> AnalyticsTabContent(aggregate = aggregate!!)
                }
            }
        }
    }
}

@Composable
fun SalesTabContent(
    aggregate: ReportsAggregate,
    onNavigateToVehicleDetail: (String) -> Unit
) {
    val sales = aggregate.sales

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Total Premium Sold", fontSize = 11.sp, color = Slate600)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = UiFormatters.formatCurrency(sales.totalPremium),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SapphireBlue
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Policies Sold", fontSize = 11.sp, color = Slate600)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = sales.totalCount.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                    }
                }
            }
        }

        // New Business vs Renewal Split
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sale Type Split", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailRow(label = "New Business", value = "${sales.newBusinessCount} policies")
                    DetailRow(label = "Renewals", value = "${sales.renewalCount} policies")
                }
            }
        }

        // Split by Insurer
        if (sales.splitByInsurer.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Premium by Insurer", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
                        sales.splitByInsurer.forEach { (insurer, prem) ->
                            DetailRow(label = insurer, value = UiFormatters.formatCurrency(prem))
                        }
                    }
                }
            }
        }

        // 12-Month Sales Trend (P1)
        if (sales.monthlyTrends.isNotEmpty()) {
            item {
                MonthlySalesTrendCard(trends = sales.monthlyTrends)
            }
        }

        // Policies List
        item {
            Text("Policies Sold in Period (${sales.soldPolicies.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
        }

        if (sales.soldPolicies.isEmpty()) {
            item {
                Text("No sales recorded in this period.", color = Slate600, fontSize = 13.sp)
            }
        } else {
            items(sales.soldPolicies, key = { it.vehicle.regNo }) { item ->
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
    onCloseVehicle: (String, String) -> Unit
) {
    val missed = aggregate.missed
    val performance = aggregate.performance
    val context = LocalContext.current

    val displayedList = when (activeFilter) {
        OverdueFilter.ALL -> missed.allMissed
        OverdueFilter.UP_TO_30 -> missed.upTo30Days
        OverdueFilter.FROM_31_TO_90 -> missed.from31To90Days
        OverdueFilter.OVER_90 -> missed.over90Days
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Cards: Premium at Risk & Overdue Count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Premium at Risk", fontSize = 11.sp, color = Slate600)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = UiFormatters.formatCurrency(missed.premiumAtRisk),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusOverdueRed
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Overdue Policies", fontSize = 11.sp, color = Slate600)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = missed.totalCount.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusOverdueRed
                        )
                    }
                }
            }
        }

        // Renewal Performance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Renewal Rate", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
                        Text(
                            text = "${String.format("%.1f", performance.renewalRatePercent)}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SapphireBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (performance.renewalRatePercent / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SapphireBlue,
                        trackColor = Slate100
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DetailRow(label = "Policies Due", value = "${performance.policiesDue}")
                    DetailRow(label = "Renewed On Time", value = "${performance.renewedOnTime}")
                    DetailRow(label = "Renewed Late", value = "${performance.renewedLate}")
                    DetailRow(label = "Still Missed", value = "${performance.missed}")
                }
            }
        }

        // Age Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(OverdueFilter.values()) { filter ->
                    val isSelected = activeFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Navy900 else Slate100)
                            .clickable { onFilterSelected(filter) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Navy700
                        )
                    }
                }
            }
        }

        // Missed Policies List
        if (displayedList.isEmpty()) {
            item {
                Text("No overdue policies in this category.", color = Slate600, fontSize = 13.sp)
            }
        } else {
            items(displayedList, key = { it.vehicle.regNo }) { item ->
                MissedRenewalCard(
                    item = item,
                    onNavigateToDetail = { onNavigateToVehicleDetail(item.vehicle.regNo) },
                    onRenew = { onNavigateToRenew(item.vehicle.regNo) },
                    onCall = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.vehicle.customer.mobile}"))
                        context.startActivity(intent)
                    },
                    onWhatsApp = {
                        val mobile = item.vehicle.customer.mobile
                        val formattedMobile = if (mobile.startsWith("91")) mobile else "91$mobile"
                        val msg = Uri.encode("Hello ${item.vehicle.customer.name}, your insurance policy for ${item.vehicle.regNo} has expired. Would you like to renew it today?")
                        val url = "https://wa.me/$formattedMobile?text=$msg"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                )
            }
        }
    }
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
    val daysRemaining = policy?.computeDaysRemaining() ?: 0
    val daysOverdue = -daysRemaining

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onNavigateToDetail),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StatusOverdueRed.copy(alpha = 0.3f)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.vehicle.regNo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                Box(
                    modifier = Modifier
                        .background(StatusOverdueRedBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$daysOverdue days overdue",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusOverdueRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.vehicle.customer.name, fontSize = 13.sp, color = Navy800)
            Text(text = "${policy?.insurer} • Expired ${policy?.expiryDate?.let { UiFormatters.formatDate(it) }}", fontSize = 11.sp, color = Slate600)
            if (policy != null) {
                Text(text = "Premium at risk: ${UiFormatters.formatCurrency(policy.totalPremium)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StatusOverdueRed)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onWhatsApp,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 11.sp)
                }

                Button(
                    onClick = onRenew,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("Renew Late", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AnalyticsTabContent(aggregate: ReportsAggregate) {
    val analytics = aggregate.analytics

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Book Size Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Active Book Size", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Active Vehicles", fontSize = 11.sp, color = Slate600)
                            Text(text = "${analytics.bookSizeVehicles}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy900)
                        }
                        Column {
                            Text("Book Value", fontSize = 11.sp, color = Slate600)
                            Text(text = UiFormatters.formatCurrency(analytics.bookSizePremium), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SapphireBlue)
                        }
                    }
                }
            }
        }

        // Renewal Pipeline
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Upcoming Renewal Pipeline", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
                    DetailRow(
                        label = "Next 3 Months",
                        value = "${analytics.renewalPipeline3Months.first} policies (${UiFormatters.formatCurrency(analytics.renewalPipeline3Months.second)})"
                    )
                    DetailRow(
                        label = "Next 6 Months",
                        value = "${analytics.renewalPipeline6Months.first} policies (${UiFormatters.formatCurrency(analytics.renewalPipeline6Months.second)})"
                    )
                    DetailRow(
                        label = "Next 12 Months",
                        value = "${analytics.renewalPipeline12Months.first} policies (${UiFormatters.formatCurrency(analytics.renewalPipeline12Months.second)})"
                    )
                }
            }
        }

        // Renewal Performance Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Retention Metrics", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
                    DetailRow(
                        label = "Overall Renewal Rate",
                        value = "${String.format("%.1f", analytics.renewalRatePercent)}%"
                    )
                    DetailRow(
                        label = "On-Time Renewal Rate",
                        value = "${String.format("%.1f", analytics.onTimeRatePercent)}%"
                    )
                    DetailRow(
                        label = "Premium at Risk (Overdue)",
                        value = UiFormatters.formatCurrency(analytics.premiumAtRisk)
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlySalesTrendCard(trends: List<com.policydesk.app.domain.model.MonthlyTrendItem>) {
    val maxPremium = trends.maxOfOrNull { it.totalPremium }?.coerceAtLeast(1000.0) ?: 1000.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "12-Month Sales Trend",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(SapphireBlue, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", fontSize = 11.sp, color = Slate600)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Navy800, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Renewal", fontSize = 11.sp, color = Slate600)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(trends) { item ->
                    val totalBarHeight = 90.dp
                    val nbRatio = (item.newBusinessPremium / maxPremium).toFloat().coerceIn(0f, 1f)
                    val renRatio = (item.renewalPremium / maxPremium).toFloat().coerceIn(0f, 1f)
                    val totalRatio = (item.totalPremium / maxPremium).toFloat().coerceIn(0f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(46.dp)
                    ) {
                        Text(
                            text = if (item.totalPremium > 0) "₹${(item.totalPremium / 1000).toInt()}k" else "₹0",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.totalPremium > 0) Navy900 else Slate400,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(totalBarHeight)
                                .background(Slate100, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(totalBarHeight * totalRatio)
                            ) {
                                if (nbRatio > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(nbRatio)
                                            .background(SapphireBlue, if (renRatio == 0f) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp) else RoundedCornerShape(0.dp))
                                    )
                                }
                                if (renRatio > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(renRatio)
                                            .background(Navy800)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.monthLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate600,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

