package com.policydesk.app.ui.dashboard

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.ui.components.DaysRemainingBadge
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.components.LiquidGlassFloatingButton
import com.policydesk.app.ui.components.LiquidGlassSearchBar
import com.policydesk.app.ui.components.LiquidGlassTopBar
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.components.liquidGlass
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemOrange
import com.policydesk.app.ui.theme.AppleSystemDeepOrange
import com.policydesk.app.ui.theme.AppleSystemRed
import com.policydesk.app.ui.theme.CardBackground
import com.policydesk.app.ui.theme.GlassBorderBottom
import com.policydesk.app.ui.theme.GlassBorderTop
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.IosSystemBackground
import com.policydesk.app.ui.theme.Navy800
import com.policydesk.app.ui.theme.Navy900
import com.policydesk.app.ui.theme.Slate100
import com.policydesk.app.ui.theme.Slate200
import com.policydesk.app.ui.theme.Slate400
import com.policydesk.app.ui.theme.Slate50
import com.policydesk.app.ui.theme.Slate600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddPolicy: () -> Unit,
    onNavigateToMonthView: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToImport: () -> Unit,
    onNavigateToPdf: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddChooserSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showAddChooserSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddChooserSheet = false },
            sheetState = sheetState,
            containerColor = Color.White.copy(alpha = 0.96f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Add Customer Policy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosLabelPrimary,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "Three ways in, one verified record out:",
                    fontSize = 13.sp,
                    color = IosLabelSecondary
                )

                AddOptionCard(
                    icon = Icons.Default.Edit,
                    iconBg = AppleSystemBlue.copy(alpha = 0.12f),
                    iconTint = AppleSystemBlue,
                    title = "Manual Form Entry",
                    subtitle = "Fill vehicle, policy & customer details manually",
                    onClick = {
                        showAddChooserSheet = false
                        onNavigateToAddPolicy()
                    }
                )

                AddOptionCard(
                    icon = Icons.Default.TableChart,
                    iconBg = AppleSystemGreen.copy(alpha = 0.12f),
                    iconTint = AppleSystemGreen,
                    title = "Spreadsheet Import (.xlsx / .csv)",
                    subtitle = "Migrate your existing Excel spreadsheet in bulk",
                    onClick = {
                        showAddChooserSheet = false
                        onNavigateToImport()
                    }
                )

                AddOptionCard(
                    icon = Icons.Default.PictureAsPdf,
                    iconBg = AppleSystemRed.copy(alpha = 0.12f),
                    iconTint = AppleSystemRed,
                    title = "Import from PDF (AI Extraction)",
                    subtitle = "Auto-extract policy details with your API key",
                    onClick = {
                        showAddChooserSheet = false
                        onNavigateToPdf()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            LiquidGlassTopBar(
                title = "PolicyDesk",
                subtitle = "Vehicle Insurance Advisor",
                actions = {
                    IconButton(onClick = onNavigateToCustomers) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Clients & Households",
                            tint = Navy800
                        )
                    }
                    IconButton(onClick = onNavigateToMonthView) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Month Timeline",
                            tint = Navy800
                        )
                    }
                    IconButton(onClick = onNavigateToReports) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Reports & Analytics",
                            tint = Navy800
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Navy800
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            LiquidGlassFloatingButton(
                text = "Add Customer",
                icon = Icons.Default.Add,
                onClick = { showAddChooserSheet = true }
            )
        },
        containerColor = IosSystemBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Metrics Summary Liquid Glass Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassMetricCard(
                    title = "Overdue",
                    count = uiState.summary.overdueCount,
                    accentColor = AppleSystemRed,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onFilterSelected(ExpiryFilter.OVERDUE) }
                )
                LiquidGlassMetricCard(
                    title = "Next 7d",
                    count = uiState.summary.next7DaysCount,
                    accentColor = AppleSystemDeepOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onFilterSelected(ExpiryFilter.NEXT_7_DAYS) }
                )
                LiquidGlassMetricCard(
                    title = "Next 30d",
                    count = uiState.summary.next30DaysCount,
                    accentColor = AppleSystemOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onFilterSelected(ExpiryFilter.NEXT_30_DAYS) }
                )
                LiquidGlassMetricCard(
                    title = "Active",
                    count = uiState.summary.totalActiveCount,
                    accentColor = AppleSystemGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onFilterSelected(ExpiryFilter.ALL) }
                )
            }

            // Quick Business Cards: Sales This Month & Missed Renewals
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    elevation = 2.dp,
                    onClick = onNavigateToReports
                ) {
                    Text("Sales Reports", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = IosLabelSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("View Sales & Mix →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppleSystemBlue)
                }

                LiquidGlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    elevation = 2.dp,
                    onClick = onNavigateToReports
                ) {
                    Text("Missed Renewals", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = IosLabelSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${uiState.summary.overdueCount} Overdue →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppleSystemRed)
                }
            }

            // Apple Liquid Glass Search Bar
            LiquidGlassSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                placeholder = "Search registration, customer, phone...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Expiry & Sector Filter Glass Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(ExpiryFilter.values()) { filter ->
                    val isSelected = uiState.activeFilter == filter
                    val pillShape = RoundedCornerShape(10.dp)

                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(
                                color = if (isSelected) AppleSystemBlue else Color.White.copy(alpha = 0.72f),
                                shape = pillShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AppleSystemBlue else Color.White.copy(alpha = 0.5f),
                                shape = pillShape
                            )
                            .clickable { viewModel.onFilterSelected(filter) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else IosLabelPrimary
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(Color(0x33000000))
                    )
                }

                items(SectorFilter.values()) { sector ->
                    val isSelected = uiState.sectorFilter == sector
                    val pillShape = RoundedCornerShape(10.dp)

                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(
                                color = if (isSelected) AppleSystemBlue.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.72f),
                                shape = pillShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AppleSystemBlue else Color.White.copy(alpha = 0.5f),
                                shape = pillShape
                            )
                            .clickable { viewModel.onSectorFilterSelected(sector) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = sector.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) AppleSystemBlue else IosLabelSecondary
                        )
                    }
                }
            }

            // Vehicle Expiry List
            if (uiState.filteredList.isEmpty()) {
                EmptyStateView(hasQuery = uiState.searchQuery.isNotEmpty())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.filteredList,
                        key = { it.vehicle.regNo }
                    ) { item ->
                        VehicleExpiryCard(
                            item = item,
                            onClick = { onNavigateToVehicleDetail(item.vehicle.regNo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddOptionCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconBg, RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = IosLabelPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 12.sp, color = IosLabelSecondary)
            }
        }
    }
}

@Composable
fun LiquidGlassMetricCard(
    title: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .liquidGlass(shape = cardShape, elevation = 2.dp)
            .clip(cardShape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = (-0.4).sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = IosLabelSecondary,
            letterSpacing = 0.1.sp
        )
    }
}

@Composable
fun VehicleExpiryCard(
    item: VehicleWithPolicies,
    onClick: () -> Unit
) {
    val policy = item.currentPolicy
    val daysRemaining = policy?.computeDaysRemaining() ?: 0
    val status = policy?.computeCurrentStatus() ?: com.policydesk.app.domain.model.PolicyStatus.ACTIVE

    LiquidGlassCard(
        shape = RoundedCornerShape(18.dp),
        elevation = 3.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vehicle Registration Plate Pill
            Box(
                modifier = Modifier
                    .background(Color(0xFFFEF9C3).copy(alpha = 0.60f), RoundedCornerShape(8.dp))
                    .border(0.75.dp, Color(0xFFCA8A04).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    text = RegistrationNormalizer.formatForDisplay(item.vehicle.regNo),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = IosLabelPrimary
                )
            }

            DaysRemainingBadge(
                daysRemaining = daysRemaining,
                status = status
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.vehicle.customer.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IosLabelPrimary,
                    letterSpacing = (-0.2).sp
                )
                item.vehicle.makeModel?.let { make ->
                    if (make.isNotBlank()) {
                        Text(
                            text = make,
                            fontSize = 13.sp,
                            color = IosLabelSecondary
                        )
                    }
                }
            }

            if (policy != null) {
                Text(
                    text = UiFormatters.formatCurrency(policy.totalPremium),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleSystemBlue,
                    letterSpacing = (-0.2).sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = policy?.insurer ?: "No Active Policy",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = IosLabelSecondary
            )

            if (policy != null) {
                Text(
                    text = "Exp: ${UiFormatters.formatDate(policy.expiryDate)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (daysRemaining <= 7) AppleSystemRed else IosLabelSecondary
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(hasQuery: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White.copy(alpha = 0.75f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = IosLabelTertiary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasQuery) "No matching policies found" else "No vehicles added yet",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = IosLabelPrimary,
                letterSpacing = (-0.2).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (hasQuery) "Try searching for a different registration, customer or phone number" else "Tap '+ Add Customer' below to add via Form, Spreadsheet, or PDF.",
                fontSize = 13.sp,
                color = IosLabelSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
