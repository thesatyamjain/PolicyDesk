package com.policydesk.app.ui.customers

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.data.profile.AdvisorProfileManager
import com.policydesk.app.domain.model.CustomerGroup
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.ui.components.AppleSecondaryButton
import com.policydesk.app.ui.components.AppleSheetGrabber
import com.policydesk.app.ui.components.AppleSprings
import com.policydesk.app.ui.components.BindBackToTop
import com.policydesk.app.ui.components.DaysRemainingBadge
import com.policydesk.app.ui.components.HsrpNumberPlate
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.components.LiquidGlassSearchBar
import com.policydesk.app.ui.components.LiquidGlassTopBar
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.navigation.EmptyStateHaloBadge
import com.policydesk.app.ui.navigation.FloatingGlassHeader
import com.policydesk.app.ui.navigation.LocalReduceMotion
import com.policydesk.app.ui.navigation.rememberAnimatedCount
import com.policydesk.app.ui.navigation.rememberIsListScrolled
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleDarkSystemGreen
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * Design Read: High-density Apple Liquid Glass Client Hub with HSRP vehicle fleets and tactile 1-tap dialer/messaging actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToVehicleDetail: (String) -> Unit,
    viewModel: CustomersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    val screenHazeState = remember { HazeState() }
    val listState = rememberLazyListState()
    val isScrolled by rememberIsListScrolled(listState)
    BindBackToTop(listState)

    var whatsAppPickerCustomer by remember { mutableStateOf<CustomerGroup?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    whatsAppPickerCustomer?.let { targetCustomer ->
        val sortedFleet = remember(targetCustomer) {
            targetCustomer.vehicles.sortedBy { it.currentPolicy?.expiryDate ?: Long.MAX_VALUE }
        }
        ModalBottomSheet(
            onDismissRequest = { whatsAppPickerCustomer = null },
            sheetState = sheetState,
            containerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            dragHandle = { AppleSheetGrabber() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select Vehicle for WhatsApp",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelPrimary,
                    letterSpacing = (-0.4).sp
                )
                val displayName = if (targetCustomer.title.isNullOrBlank()) {
                    targetCustomer.name
                } else {
                    "${targetCustomer.title} ${targetCustomer.name}"
                }
                Text(
                    text = "$displayName • ${targetCustomer.mobile}",
                    fontSize = 13.sp,
                    color = labelSecondary,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )

                Spacer(modifier = Modifier.height(2.dp))

                sortedFleet.forEach { item ->
                    val policy = item.currentPolicy
                    val daysLeft = policy?.computeDaysRemaining() ?: 0L
                    val status = policy?.computeCurrentStatus() ?: com.policydesk.app.domain.model.PolicyStatus.ACTIVE

                    LiquidGlassCard(
                        shape = RoundedCornerShape(16.dp),
                        elevation = 2.dp,
                        onClick = {
                            whatsAppPickerCustomer = null
                            launchCustomerWhatsApp(context, targetCustomer, item)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                HsrpNumberPlate(
                                    regNo = item.vehicle.regNo,
                                    makeModel = item.vehicle.makeModel,
                                    style = item.vehicle.plateStyle
                                )
                                val expLabel = policy?.expiryDate?.let { "Exp ${UiFormatters.formatDate(it)}" } ?: "No policy"
                                Text(
                                    text = "${item.vehicle.makeModel ?: "Vehicle"} • $expLabel",
                                    fontSize = 11.5.sp,
                                    color = labelSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (policy != null) {
                                    DaysRemainingBadge(daysRemaining = daysLeft, status = status)
                                    Text(
                                        text = UiFormatters.formatCurrency(policy.totalPremium),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentBlue,
                                        style = TextStyle(fontFeatureSettings = "tnum")
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            FloatingGlassHeader(
                hazeState = screenHazeState,
                isScrolled = isScrolled
            ) {
                LiquidGlassTopBar(
                    title = "Clients & Households",
                    subtitle = "Customer Portfolio Directory",
                    onNavigateBack = onNavigateBack
                )
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        val animatedTotalClients = rememberAnimatedCount(uiState.totalCustomers)
        val animatedMultiVehicle = rememberAnimatedCount(uiState.multiVehicleCount)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = screenHazeState),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = 140.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Liquid Glass Search Input
            item(key = "customers_search") {
                LiquidGlassSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    placeholder = "Search client name, mobile, or vehicle..."
                )
            }

            // Telemetry Bento Overview (Total Clients & Multi-Vehicle Households)
            item(key = "customers_kpi_bento") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 2.dp
                    ) {
                        Text(
                            text = "TOTAL CLIENTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$animatedTotalClients",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = labelPrimary,
                            letterSpacing = (-0.4).sp,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                        Text(
                            text = if (uiState.totalCustomers == 1) "Active contact" else "Active contacts",
                            fontSize = 11.sp,
                            color = labelSecondary
                        )
                    }

                    LiquidGlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 2.dp
                    ) {
                        Text(
                            text = "MULTI-VEHICLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentBlue,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$animatedMultiVehicle",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = accentBlue,
                            letterSpacing = (-0.4).sp,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                        Text(
                            text = if (uiState.multiVehicleCount == 1) "Household" else "Households",
                            fontSize = 11.sp,
                            color = labelSecondary
                        )
                    }
                }
            }

            // Customer List or Empty State
            if (uiState.customers.isEmpty()) {
                item(key = "customers_empty_state") {
                    val hasSearch = uiState.searchQuery.isNotBlank()
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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
                                icon = AppleIcons.Person2,
                                tint = accentBlue
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (hasSearch) "No clients match your search" else "No clients registered yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = labelPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (hasSearch) {
                                    "No client records match \"${uiState.searchQuery.trim()}\"."
                                } else {
                                    "Add policies or import a sheet to group multi-vehicle households automatically."
                                },
                                fontSize = 12.5.sp,
                                color = labelSecondary,
                                textAlign = TextAlign.Center
                            )
                            if (hasSearch) {
                                Spacer(modifier = Modifier.height(14.dp))
                                AppleSecondaryButton(
                                    text = "Clear Search",
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.onSearchQueryChanged("")
                                    },
                                    icon = AppleIcons.Clear,
                                    height = 38.dp,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                items(
                    items = uiState.customers,
                    key = { it.mobile.ifBlank { it.name } },
                    contentType = { "customer_card" }
                ) { customer ->
                    val isExpanded = uiState.expandedMobiles.contains(customer.mobile)
                    CustomerCard(
                        customer = customer,
                        onNavigateToVehicle = onNavigateToVehicleDetail,
                        onCall = {
                            if (customer.mobile.any { it.isDigit() }) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.mobile}"))
                                context.startActivity(intent)
                            }
                        },
                        onWhatsApp = {
                            if (customer.vehicles.size > 1) {
                                whatsAppPickerCustomer = customer
                            } else {
                                launchCustomerWhatsApp(context, customer, customer.vehicles.firstOrNull())
                            }
                        },
                        isExpanded = isExpanded,
                        onToggleExpanded = { viewModel.toggleCustomerExpanded(customer.mobile) }
                    )
                }
            }
        }
    }
}

private fun launchCustomerWhatsApp(
    context: Context,
    customer: CustomerGroup,
    targetVehicle: VehicleWithPolicies?
) {
    val clean = customer.mobile.filter { it.isDigit() }
    if (clean.isEmpty()) return
    val formatted = if (clean.length == 10) "91$clean" else clean
    val policy = targetVehicle?.currentPolicy
    val msg = AdvisorProfileManager.formatRenewalMessage(
        context = context,
        customerName = customer.name,
        regNo = targetVehicle?.vehicle?.regNo ?: "",
        insurer = policy?.insurer ?: "",
        expiryDate = policy?.expiryDate?.let { UiFormatters.formatDate(it) } ?: "",
        premium = policy?.totalPremium?.let { UiFormatters.formatCurrency(it) } ?: ""
    )
    val url = "https://api.whatsapp.com/send?phone=$formatted&text=${Uri.encode(msg)}"
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

@Composable
fun CustomerCard(
    customer: CustomerGroup,
    onNavigateToVehicle: (String) -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    isExpanded: Boolean? = null,
    onToggleExpanded: (() -> Unit)? = null
) {
    var localExpanded by remember { mutableStateOf(false) }
    val expanded = isExpanded ?: localExpanded
    val toggleExpanded: () -> Unit = onToggleExpanded ?: { localExpanded = !localExpanded }

    val isDark = isSystemInDarkTheme()
    val reduceMotion = LocalReduceMotion.current
    val haptic = LocalHapticFeedback.current
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
    val accentGreen = if (isDark) AppleDarkSystemGreen else AppleSystemGreen
    val hasPhoneDigits = customer.mobile.any { it.isDigit() }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = if (reduceMotion) snap() else AppleSprings.Snappy,
        label = "fleet_chevron_rotation"
    )

    LiquidGlassCard(
        shape = RoundedCornerShape(18.dp),
        elevation = 3.dp
    ) {
        // Client Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x330A84FF) else Color(0x1F007AFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppleIcons.Person2,
                        contentDescription = null,
                        tint = accentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    val displayName = if (customer.title.isNullOrBlank()) customer.name else "${customer.title} ${customer.name}"
                    Text(
                        text = displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelPrimary,
                        letterSpacing = (-0.3).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "${customer.mobile} • Client for ${UiFormatters.formatTenure(customer.clientSinceDate)}",
                        fontSize = 12.sp,
                        color = labelSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
            }

            // Fleet Count Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (customer.vehicles.size > 1) {
                            if (isDark) Color(0x330A84FF) else Color(0x1F007AFF)
                        } else {
                            if (isDark) Color(0x332C2C2E) else Color(0x1A000000)
                        }
                    )
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${customer.vehicles.size} ${if (customer.vehicles.size == 1) "Vehicle" else "Vehicles"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (customer.vehicles.size > 1) accentBlue else labelSecondary,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }

        if (customer.address.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = customer.address,
                fontSize = 11.sp,
                color = labelSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons & Expand Vehicle Fleet Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hasPhoneDigits) {
                    // Call Action
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0x330A84FF) else Color(0x1F007AFF))
                            .semantics { role = Role.Button }
                            .clickable(onClickLabel = "Call ${customer.name}") {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onCall()
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppleIcons.Phone,
                                contentDescription = "Call ${customer.name}",
                                tint = accentBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Call",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentBlue
                            )
                        }
                    }

                    // WhatsApp Action (launches directly for single vehicle, opens picker for multi-vehicle)
                    val waTargetDesc = if (customer.vehicles.size > 1) {
                        "Choose vehicle to WhatsApp ${customer.name}"
                    } else {
                        "WhatsApp ${customer.name} for ${customer.vehicles.firstOrNull()?.vehicle?.regNo ?: "policy"}"
                    }
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0x3330D158) else Color(0x1F34C759))
                            .semantics { role = Role.Button }
                            .clickable(onClickLabel = waTargetDesc) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onWhatsApp()
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppleIcons.WhatsApp,
                                contentDescription = waTargetDesc,
                                tint = accentGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WhatsApp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentGreen
                            )
                        }
                    }
                }
            }

            // Expand Fleet Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .semantics {
                        role = Role.Button
                        stateDescription = if (expanded) "Expanded" else "Collapsed"
                    }
                    .clickable(
                        onClickLabel = if (expanded) "Hide fleet vehicles" else "View fleet vehicles"
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        toggleExpanded()
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Hide Fleet" else "View Fleet",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentBlue
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = AppleIcons.ChevronDown,
                    contentDescription = null,
                    tint = accentBlue,
                    modifier = Modifier
                        .size(15.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }
        }

        // Expanded Fleet Vehicles
        AnimatedVisibility(
            visible = expanded,
            enter = if (reduceMotion) EnterTransition.None else (
                expandVertically(
                    animationSpec = spring(
                        dampingRatio = 1.0f,
                        stiffness = 320f,
                        visibilityThreshold = IntSize.VisibilityThreshold
                    )
                ) + fadeIn(animationSpec = tween(220))
            ),
            exit = if (reduceMotion) ExitTransition.None else (
                shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = 1.0f,
                        stiffness = 340f,
                        visibilityThreshold = IntSize.VisibilityThreshold
                    )
                ) + fadeOut(animationSpec = tween(160))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                customer.vehicles.forEach { item ->
                    val vehicle = item.vehicle
                    val policy = item.currentPolicy
                    val daysLeft = policy?.computeDaysRemaining() ?: 0L
                    val policyStatus = policy?.computeCurrentStatus() ?: com.policydesk.app.domain.model.PolicyStatus.ACTIVE

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x332C2C2E) else Color(0x66FFFFFF))
                            .border(
                                0.5.dp,
                                if (isDark) Color(0x33FFFFFF) else Color(0x22000000),
                                RoundedCornerShape(12.dp)
                            )
                            .semantics { role = Role.Button }
                            .clickable(onClickLabel = "Open ${vehicle.regNo} details") {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNavigateToVehicle(vehicle.regNo)
                            }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            HsrpNumberPlate(
                                regNo = vehicle.regNo,
                                makeModel = vehicle.makeModel,
                                style = vehicle.plateStyle
                            )
                            Text(
                                text = "${vehicle.makeModel ?: "Vehicle"} • ${policy?.insurer ?: "No Policy"}",
                                fontSize = 11.sp,
                                color = labelSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (policy != null) {
                            DaysRemainingBadge(daysRemaining = daysLeft, status = policyStatus)
                        }
                    }
                }
            }
        }
    }
}
