package com.policydesk.app.ui.customers

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.domain.model.CustomerGroup
import com.policydesk.app.ui.components.DaysRemainingBadge
import com.policydesk.app.ui.components.HsrpNumberPlate
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.components.LiquidGlassSearchBar
import com.policydesk.app.ui.components.LiquidGlassTopBar
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleDarkSystemGreen
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkLabelTertiary
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary

/**
 * Design Read: High-density Apple Liquid Glass Client Hub with HSRP vehicle fleets and tactile 1-tap dialer/messaging actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit,
    viewModel: CustomersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    Scaffold(
        topBar = {
            LiquidGlassTopBar(
                title = "Clients & Households",
                subtitle = "Customer Portfolio Directory",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Liquid Glass Search Input
            LiquidGlassSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                placeholder = "Search client name, mobile, or vehicle...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Telemetry Bento Overview (Total Clients & Multi-Vehicle Households)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
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
                        text = "${uiState.totalCustomers}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = labelPrimary,
                        letterSpacing = (-0.4).sp
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
                        color = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${uiState.multiVehicleCount} households",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                        letterSpacing = (-0.4).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Customer List
            if (uiState.customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0x332C2C2E) else Color(0x1F007AFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AppleIcons.Person2,
                                contentDescription = null,
                                tint = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotEmpty()) "No clients match your search" else "No clients registered yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add policies or import a sheet to populate clients.",
                            fontSize = 12.sp,
                            color = labelSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.customers, key = { it.mobile }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onNavigateToVehicle = onNavigateToVehicleDetail,
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.mobile}"))
                                context.startActivity(intent)
                            },
                            onWhatsApp = {
                                val clean = customer.mobile.filter { it.isDigit() }
                                val formatted = if (clean.length == 10) "91$clean" else clean
                                val url = "https://api.whatsapp.com/send?phone=$formatted&text=Hello%20${Uri.encode(customer.name)}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCard(
    customer: CustomerGroup,
    onNavigateToVehicle: (String) -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

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
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color(0x330A84FF) else Color(0x1F007AFF)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppleIcons.Person2,
                        contentDescription = null,
                        tint = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
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
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = customer.mobile,
                        fontSize = 12.sp,
                        color = labelSecondary
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
                    color = if (customer.vehicles.size > 1) {
                        if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                    } else {
                        labelSecondary
                    }
                )
            }
        }

        if (customer.address.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = customer.address,
                fontSize = 11.sp,
                color = labelSecondary,
                maxLines = 1
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
                // Call Action
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0x330A84FF) else Color(0x1F007AFF))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCall()
                        }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppleIcons.Phone,
                            contentDescription = null,
                            tint = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Call",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                        )
                    }
                }

                // WhatsApp Action
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0x3330D158) else Color(0x1F34C759))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onWhatsApp()
                        }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppleIcons.Message,
                            contentDescription = null,
                            tint = if (isDark) AppleDarkSystemGreen else AppleSystemGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "WhatsApp",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) AppleDarkSystemGreen else AppleSystemGreen
                        )
                    }
                }
            }

            // Expand Fleet Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        expanded = !expanded
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Hide Fleet" else "View Fleet",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isDark) AppleDarkSystemBlue else AppleSystemBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Expanded Fleet Vehicles
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                customer.vehicles.forEach { item ->
                    val vehicle = item.vehicle
                    val policy = item.currentPolicy
                    val daysLeft = policy?.computeDaysRemaining() ?: 0

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
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNavigateToVehicle(vehicle.regNo)
                            }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            HsrpNumberPlate(
                                regNo = vehicle.regNo
                            )
                            Text(
                                text = "${vehicle.makeModel ?: "Vehicle"} • ${policy?.insurer ?: "No Policy"}",
                                fontSize = 11.sp,
                                color = labelSecondary
                            )
                        }

                        if (policy != null) {
                            DaysRemainingBadge(daysRemaining = daysLeft, status = policy.status)
                        }
                    }
                }
            }
        }
    }
}
