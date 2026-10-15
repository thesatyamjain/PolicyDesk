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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.components.LiquidGlassFloatingButton
import com.policydesk.app.ui.components.LiquidGlassSearchBar
import com.policydesk.app.ui.components.LiquidGlassTopBar
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.components.liquidGlass
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemOrange
import com.policydesk.app.ui.theme.AppleSystemDeepOrange
import com.policydesk.app.ui.theme.AppleSystemRed
import com.policydesk.app.ui.theme.CardBackground
import com.policydesk.app.ui.theme.GlassBorderBottom
import com.policydesk.app.ui.theme.GlassBorderTop
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkSeparator
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.IosSeparator
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

    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    if (showAddChooserSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddChooserSheet = false },
            sheetState = sheetState,
            containerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
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
                    color = labelPrimary,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "Three ways in, one verified record out:",
                    fontSize = 13.sp,
                    color = labelSecondary
                )

                AddOptionCard(
                    icon = AppleIcons.SquareAndPencil,
                    iconBg = AppleSystemBlue.copy(alpha = 0.12f),
                    iconTint = AppleSystemBlue,
                    title = "Fill Form (Manual Entry)",
                    subtitle = "Fill vehicle, policy & customer details manually",
                    onClick = {
                        showAddChooserSheet = false
                        onNavigateToAddPolicy()
                    }
                )

                AddOptionCard(
                    icon = AppleIcons.TableCells,
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
                    icon = AppleIcons.DocText,
                    iconBg = AppleSystemRed.copy(alpha = 0.12f),
                    iconTint = AppleSystemRed,
                    title = "Import from PDF (AI Extraction)",
                    subtitle = "Auto-extract policy details with your API key",
                    onClick = {
                        showAddChooserSheet = false
                        onNavigateToPdf()
                    }
                )

                AddOptionCard(
                    icon = Icons.Default.CloudDone,
                    iconBg = AppleSystemBlue.copy(alpha = 0.12f),
                    iconTint = AppleSystemBlue,
                    title = "Restore from Google Drive",
                    subtitle = "Restore all vehicles & policies from your Drive backup",
                    onClick = {
                        showAddChooserSheet = false
                        onNavigateToSettings()
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
                    val isDark = isSystemInDarkTheme()
                    val haptic = LocalHapticFeedback.current
                    val actionTint = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToCustomers()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = AppleIcons.Person2,
                            contentDescription = "Clients & Households",
                            tint = actionTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToMonthView()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = AppleIcons.Calendar,
                            contentDescription = "Month Timeline",
                            tint = actionTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToReports()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = AppleIcons.ChartBar,
                            contentDescription = "Reports & Analytics",
                            tint = actionTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToSettings()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = AppleIcons.GearShape,
                            contentDescription = "Settings",
                            tint = actionTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            LiquidGlassFloatingButton(
                text = "Add Customer",
                icon = AppleIcons.Plus,
                onClick = { showAddChooserSheet = true }
            )
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
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
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(ExpiryFilter.values()) { filter ->
                    val isSelected = uiState.activeFilter == filter
                    val pillShape = RoundedCornerShape(12.dp)
                    val haptic = LocalHapticFeedback.current
                    val isDark = isSystemInDarkTheme()

                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(
                                color = if (isSelected) {
                                    if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                                } else {
                                    if (isDark) Color(0xFF2C2C2E) else Color.White
                                },
                                shape = pillShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) {
                                    Color.Transparent
                                } else {
                                    if (isDark) Color(0x38FFFFFF) else Color(0x24000000)
                                },
                                shape = pillShape
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.onFilterSelected(filter)
                            }
                            .padding(horizontal = 13.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = filter.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else (if (isDark) IosDarkLabelPrimary else IosLabelPrimary)
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(1.dp)
                            .height(18.dp)
                            .background(
                                if (isSystemInDarkTheme()) IosDarkSeparator else IosSeparator,
                                RoundedCornerShape(1.dp)
                            )
                    )
                }

                items(SectorFilter.values()) { sector ->
                    val isSelected = uiState.sectorFilter == sector
                    val pillShape = RoundedCornerShape(12.dp)
                    val haptic = LocalHapticFeedback.current
                    val isDark = isSystemInDarkTheme()

                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(
                                color = if (isSelected) {
                                    (if (isDark) AppleDarkSystemBlue else AppleSystemBlue).copy(alpha = 0.16f)
                                } else {
                                    if (isDark) Color(0xFF2C2C2E) else Color.White
                                },
                                shape = pillShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) {
                                    if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                                } else {
                                    if (isDark) Color(0x38FFFFFF) else Color(0x24000000)
                                },
                                shape = pillShape
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.onSectorFilterSelected(sector)
                            }
                            .padding(horizontal = 13.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = sector.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                            } else {
                                if (isDark) IosDarkLabelSecondary else IosLabelSecondary
                            }
                        )
                    }
                }
            }

            // Vehicle Expiry List
            if (uiState.filteredList.isEmpty()) {
                EmptyStateView(
                    hasQuery = uiState.searchQuery.isNotEmpty(),
                    onAddCustomerClicked = { showAddChooserSheet = true },
                    onNavigateToSettings = onNavigateToSettings
                )
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
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

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
                    .border(0.5.dp, if (isDark) Color(0x33FFFFFF) else Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = labelPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 12.sp, color = labelSecondary)
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
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .liquidGlass(shape = cardShape, elevation = 3.dp, isDark = isDark)
            .clip(cardShape)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) IosDarkLabelSecondary else IosLabelSecondary,
            letterSpacing = 0.1.sp
        )
    }
}

@Composable
fun VehicleExpiryCard(
    item: VehicleWithPolicies,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
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
                    .background(if (isDark) Color(0xFF332E00) else Color(0xFFFEF9C3).copy(alpha = 0.60f), RoundedCornerShape(8.dp))
                    .border(0.75.dp, if (isDark) Color(0xFFEAB308) else Color(0xFFCA8A04).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    text = RegistrationNormalizer.formatForDisplay(item.vehicle.regNo),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = if (isDark) Color(0xFFFEF08A) else IosLabelPrimary
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
                    color = labelPrimary,
                    letterSpacing = (-0.2).sp
                )
                item.vehicle.makeModel?.let { make ->
                    if (make.isNotBlank()) {
                        Text(
                            text = make,
                            fontSize = 13.sp,
                            color = labelSecondary
                        )
                    }
                }
            }

            if (policy != null) {
                Text(
                    text = UiFormatters.formatCurrency(policy.totalPremium),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
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
                color = labelSecondary
            )

            if (policy != null) {
                Text(
                    text = "Exp: ${UiFormatters.formatDate(policy.expiryDate)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (daysRemaining <= 7) AppleSystemRed else labelSecondary
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    hasQuery: Boolean,
    onAddCustomerClicked: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

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
                    .background(if (isDark) Color(0x33545458) else Color.White.copy(alpha = 0.75f), CircleShape)
                    .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color.White.copy(alpha = 0.9f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppleIcons.Car,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF8E8E93) else IosLabelTertiary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasQuery) "No matching policies found" else "No vehicles added yet",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = labelPrimary,
                letterSpacing = (-0.2).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (hasQuery) "Try searching for a different registration, customer or phone number" else "Tap below to add via Form, Spreadsheet, or PDF.",
                fontSize = 13.sp,
                color = labelSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (!hasQuery) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddCustomerClicked,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) AppleDarkSystemBlue else AppleSystemBlue)
                ) {
                    Icon(imageVector = AppleIcons.Plus, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Customer Policy", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onNavigateToSettings,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) AppleDarkSystemBlue else AppleSystemBlue)
                ) {
                    Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore from Google Drive", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
