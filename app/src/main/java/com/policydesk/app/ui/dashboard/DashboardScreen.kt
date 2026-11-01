package com.policydesk.app.ui.dashboard

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.TextStyle
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.policydesk.app.ui.components.BindBackToTop
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.ui.components.AppleAlertDialog
import com.policydesk.app.ui.components.AppleButton
import com.policydesk.app.ui.components.AppleSecondaryButton
import com.policydesk.app.ui.components.DaysRemainingBadge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.policydesk.app.ui.components.DockTab
import com.policydesk.app.ui.components.HsrpNumberPlate
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.components.LiquidGlassDock
import com.policydesk.app.ui.components.LiquidGlassFloatingButton
import com.policydesk.app.ui.components.LiquidGlassSearchBar
import com.policydesk.app.ui.components.LiquidGlassSelectionBar
import com.policydesk.app.ui.components.LiquidGlassTopBar
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.components.liquidGlass
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleDarkSystemGreen
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
    onSelectionModeChange: (Boolean) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddChooserSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val actionTint = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    val allSelected = uiState.filteredList.isNotEmpty() && uiState.selectedRegNos.size == uiState.filteredList.size

    LaunchedEffect(uiState.isSelectionMode) {
        onSelectionModeChange(uiState.isSelectionMode)
    }

    BackHandler(enabled = uiState.isSelectionMode) {
        viewModel.exitSelectionMode()
    }

    val onShareSelected: () -> Unit = {
        val selectedItems = uiState.filteredList.filter { it.vehicle.regNo in uiState.selectedRegNos }
        if (selectedItems.isNotEmpty()) {
            val shareText = buildString {
                appendLine("PolicyDesk Summary (${selectedItems.size} Policies):")
                appendLine("────────────────────────────────")
                selectedItems.forEachIndexed { i, item ->
                    val p = item.currentPolicy
                    val exp = p?.expiryDate?.let { UiFormatters.formatDate(it) } ?: "N/A"
                    val premium = p?.totalPremium?.let { UiFormatters.formatCurrency(it) } ?: "N/A"
                    appendLine("${i + 1}. ${item.vehicle.regNo} | ${item.vehicle.customer.name}")
                    appendLine("   Insurer: ${p?.insurer ?: "N/A"} | Exp: $exp | Premium: $premium")
                    if (item.vehicle.customer.mobile.isNotBlank()) {
                        appendLine("   Contact: ${item.vehicle.customer.mobile}")
                    }
                    appendLine()
                }
            }
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share ${selectedItems.size} Policies"))
        }
    }

    if (showDeleteConfirmDialog) {
        AppleAlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = "Delete ${uiState.selectedRegNos.size} Policies?",
            message = "Are you sure you want to remove the selected policies? Their vehicle records will be archived.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showDeleteConfirmDialog = false
                viewModel.deleteSelected()
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (uiState.isSelectionMode) {
                    LiquidGlassTopBar(
                        title = if (uiState.selectedRegNos.isEmpty()) "Select Policies" else "${uiState.selectedRegNos.size} Selected",
                        subtitle = "${uiState.filteredList.size} policies total",
                        onNavigateBack = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.exitSelectionMode()
                        },
                        actions = {
                            TextButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.toggleSelectAll(uiState.filteredList.map { it.vehicle.regNo })
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = if (allSelected) "Deselect All" else "Select All",
                                    color = actionTint,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            TextButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.exitSelectionMode()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "Done",
                                    color = actionTint,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                } else {
                    LiquidGlassTopBar(
                        title = "PolicyDesk",
                        subtitle = "Vehicle Insurance Advisor",
                        actions = {
                            if (uiState.filteredList.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.enterSelectionMode()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = "Select",
                                        color = actionTint,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Prominent Apple Glass Add Button (Primary Action)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) AppleDarkSystemBlue else AppleSystemBlue, CircleShape)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showAddChooserSheet = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = AppleIcons.Plus,
                                    contentDescription = "Add Policy",
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Apple Glass Settings Button (Frosted glass secondary navigation control)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDark) Color(0x29FFFFFF) else Color(0x14000000),
                                        CircleShape
                                    )
                                    .border(
                                        width = 0.5.dp,
                                        color = Color.White.copy(alpha = if (isDark) 0.18f else 0.45f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onNavigateToSettings()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = AppleIcons.GearShape,
                                    contentDescription = "Settings",
                                    tint = if (isDark) IosDarkLabelPrimary else IosLabelPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
        val listState = rememberLazyListState()
        BindBackToTop(listState)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding() + 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Executive Bento Hero Pulse Card (Full-Width, Scrollable)
            item {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = 4.dp,
                    onClick = { viewModel.onFilterSelected(ExpiryFilter.ALL) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(AppleSystemGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Portfolio in force",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) AppleDarkSystemGreen else AppleSystemGreen,
                                    letterSpacing = (-0.1).sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${uiState.summary.totalActiveCount}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = labelPrimary,
                                    letterSpacing = (-0.8).sp,
                                    lineHeight = 34.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Active Vehicles",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = labelSecondary,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                        }

                        // Quick Month Timeline Pill Shortcut
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0x330A84FF) else Color(0x1F007AFF))
                                .clickable(onClick = onNavigateToMonthView)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = AppleIcons.Calendar,
                                    contentDescription = null,
                                    tint = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Timeline",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                                )
                            }
                        }
                    }
                }
            }

            // 2. Urgent Action Triage Bento (2 Columns, Scrollable)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Overdue Action Card
                    LiquidGlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        elevation = 3.dp,
                        onClick = { viewModel.onFilterSelected(ExpiryFilter.OVERDUE) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Overdue",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppleSystemRed,
                                letterSpacing = (-0.1).sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(AppleSystemRed, CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.summary.overdueCount}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = AppleSystemRed,
                            letterSpacing = (-0.5).sp,
                            lineHeight = 28.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Needs immediate nudge",
                            fontSize = 11.sp,
                            color = labelSecondary
                        )
                    }

                    // Next 7 Days Card
                    LiquidGlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        elevation = 3.dp,
                        onClick = { viewModel.onFilterSelected(ExpiryFilter.NEXT_7_DAYS) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "This week",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppleSystemDeepOrange,
                                letterSpacing = (-0.1).sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(AppleSystemDeepOrange, CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.summary.next7DaysCount}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = AppleSystemDeepOrange,
                            letterSpacing = (-0.5).sp,
                            lineHeight = 28.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Expiring in 7 days",
                            fontSize = 11.sp,
                            color = labelSecondary
                        )
                    }
                }
            }

            // 3. Next 30 Days Pipeline Banner Strip (Scrollable)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color(0x332C2C2E) else Color(0x66FFFFFF))
                        .border(
                            0.5.dp,
                            if (isDark) Color(0x33FFFFFF) else Color(0x33000000),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { viewModel.onFilterSelected(ExpiryFilter.NEXT_30_DAYS) }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(AppleSystemOrange, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "30-Day Renewal Pipeline",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = labelPrimary
                            )
                        }
                        Text(
                            text = "${uiState.summary.next30DaysCount} policies",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleSystemOrange
                        )
                    }
                }
            }

            // 4. Apple Liquid Glass Search Bar (Scrollable)
            item {
                LiquidGlassSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    placeholder = "Search registration, customer, phone...",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            // 5. Expiry & Sector Filter Glass Pills (Scrollable)
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(ExpiryFilter.values()) { filter ->
                        val isSelected = uiState.activeFilter == filter
                        val pillShape = RoundedCornerShape(12.dp)
                        val haptic = LocalHapticFeedback.current
                        val isDark = isSystemInDarkTheme()

                        val pillScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.04f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
                            label = "expiry_pill_scale"
                        )
                        val pillBg by animateColorAsState(
                            targetValue = if (isSelected) {
                                if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                            } else {
                                if (isDark) Color(0xFF2C2C2E) else Color.White
                            },
                            animationSpec = spring(dampingRatio = 0.8f),
                            label = "expiry_pill_bg"
                        )

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = pillScale
                                    scaleY = pillScale
                                }
                                .clip(pillShape)
                                .background(pillBg, pillShape)
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

                        val pillScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.04f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
                            label = "sector_pill_scale"
                        )
                        val pillBg by animateColorAsState(
                            targetValue = if (isSelected) {
                                (if (isDark) AppleDarkSystemBlue else AppleSystemBlue).copy(alpha = 0.16f)
                            } else {
                                if (isDark) Color(0xFF2C2C2E) else Color.White
                            },
                            animationSpec = spring(dampingRatio = 0.8f),
                            label = "sector_pill_bg"
                        )

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = pillScale
                                    scaleY = pillScale
                                }
                                .clip(pillShape)
                                .background(pillBg, pillShape)
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
            }

            // 6. Vehicle Expiry List
            if (uiState.filteredList.isEmpty()) {
                item {
                    EmptyStateView(
                        hasQuery = uiState.searchQuery.isNotEmpty(),
                        onAddCustomerClicked = { showAddChooserSheet = true },
                        onNavigateToSettings = onNavigateToSettings
                    )
                }
            } else {
                items(
                    items = uiState.filteredList,
                    key = { it.vehicle.regNo },
                    contentType = { "vehicle_card" }
                ) { item ->
                    val isSelected = uiState.selectedRegNos.contains(item.vehicle.regNo)
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        VehicleExpiryCard(
                            item = item,
                            onClick = { onNavigateToVehicleDetail(item.vehicle.regNo) },
                            isSelectionMode = uiState.isSelectionMode,
                            isSelected = isSelected,
                            onToggleSelect = { viewModel.toggleSelect(item.vehicle.regNo) },
                            onLongClick = { viewModel.enterSelectionMode(item.vehicle.regNo) }
                        )
                    }
                }
            }
        }
    }

    // Floating Selection Bar (Slides up when in selection mode)
        AnimatedVisibility(
            visible = uiState.isSelectionMode,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
            ) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            LiquidGlassSelectionBar(
                selectedCount = uiState.selectedRegNos.size,
                totalCount = uiState.filteredList.size,
                allSelected = allSelected,
                onToggleSelectAll = { viewModel.toggleSelectAll(uiState.filteredList.map { it.vehicle.regNo }) },
                onShare = onShareSelected,
                onDelete = { showDeleteConfirmDialog = true },
                onClose = { viewModel.exitSelectionMode() }
            )
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
            letterSpacing = (-0.5).sp,
            style = TextStyle(fontFeatureSettings = "tnum")
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
    onClick: () -> Unit,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val policy = item.currentPolicy
    val daysRemaining = policy?.computeDaysRemaining() ?: 0
    val status = policy?.computeCurrentStatus() ?: com.policydesk.app.domain.model.PolicyStatus.ACTIVE
    val mobile = item.vehicle.customer.mobile

    LiquidGlassCard(
        shape = RoundedCornerShape(18.dp),
        elevation = 3.dp,
        onClick = {
            if (isSelectionMode) onToggleSelect() else onClick()
        },
        onLongClick = {
            if (!isSelectionMode) onLongClick()
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedVisibility(
                    visible = isSelectionMode,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) (if (isDark) AppleDarkSystemBlue else AppleSystemBlue)
                                else Color.Transparent
                            )
                            .border(
                                width = 2.dp,
                                color = if (isSelected) Color.Transparent else (if (isDark) Color(0x66FFFFFF) else Color(0x33000000)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = AppleIcons.Checkmark,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                // Authentic Indian HSRP Number Plate
                HsrpNumberPlate(
                    regNo = item.vehicle.regNo
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
                    fontWeight = FontWeight.Bold,
                    color = labelPrimary,
                    letterSpacing = (-0.3).sp
                )
                item.vehicle.makeModel?.let { make ->
                    if (make.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
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
                    letterSpacing = (-0.3).sp,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = policy?.insurer ?: "No Active Policy",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = labelSecondary
                )
                if (policy != null) {
                    Text(
                        text = "Exp ${UiFormatters.formatDate(policy.expiryDate)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (daysRemaining <= 7) AppleSystemRed else labelSecondary,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
            }

            // Quick Direct Action Shortcuts (WhatsApp & Phone)
            if (mobile.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x3330D158) else Color(0x1F34C759))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val cleanMobile = mobile.filter { it.isDigit() }
                                val phoneWithCountry = if (cleanMobile.length == 10) "91$cleanMobile" else cleanMobile
                                val expText = policy?.expiryDate?.let { UiFormatters.formatDate(it) } ?: ""
                                val premText = policy?.totalPremium?.let { UiFormatters.formatCurrency(it) } ?: ""
                                val message = com.policydesk.app.data.profile.AdvisorProfileManager.formatRenewalMessage(
                                    context = context,
                                    customerName = item.vehicle.customer.name,
                                    regNo = item.vehicle.regNo,
                                    insurer = policy?.insurer ?: "",
                                    expiryDate = expText,
                                    premium = premText
                                )
                                val url = "https://api.whatsapp.com/send?phone=$phoneWithCountry&text=${Uri.encode(message)}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppleIcons.WhatsApp,
                            contentDescription = "WhatsApp reminder",
                            tint = if (isDark) AppleDarkSystemGreen else AppleSystemGreen,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x330A84FF) else Color(0x1F007AFF))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobile"))
                                context.startActivity(intent)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppleIcons.Phone,
                            contentDescription = "Call customer",
                            tint = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
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
                Spacer(modifier = Modifier.height(20.dp))
                AppleButton(
                    text = "Add Customer Policy",
                    onClick = onAddCustomerClicked,
                    icon = AppleIcons.Plus,
                    height = 46.dp,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                AppleSecondaryButton(
                    text = "Restore from Google Drive",
                    onClick = onNavigateToSettings,
                    icon = Icons.Default.CloudDone,
                    height = 42.dp,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    }
}
