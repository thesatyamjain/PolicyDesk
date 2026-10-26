package com.policydesk.app.ui.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import com.policydesk.app.ui.components.AppleAlertDialog
import com.policydesk.app.ui.components.AppleButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.ui.components.DaysRemainingBadge
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.components.LiquidGlassGroupedSection
import com.policydesk.app.ui.components.LiquidGlassTopBar
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.components.liquidGlass
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemOrange
import com.policydesk.app.ui.theme.AppleSystemDeepOrange
import com.policydesk.app.ui.theme.AppleSystemRed
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.components.HsrpNumberPlate
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.IosSystemBackground

@Composable
fun VehicleDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRenew: (String) -> Unit,
    viewModel: VehicleDetailViewModel = hiltViewModel()
) {
    val vehicleWithPolicies by viewModel.vehicleData.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showMenu by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is DetailUiEvent.VehicleClosed -> {
                    Toast.makeText(context, "Vehicle closed", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is DetailUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showCloseDialog) {
        val closeReasons = listOf(
            "Vehicle sold",
            "Moved to another advisor / insurer",
            "Customer declined renewal",
            "Other reason"
        )
        var selectedReason by remember { mutableStateOf(closeReasons.first()) }

        val isDark = isSystemInDarkTheme()
        val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
        val accentBlue = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

        AppleAlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = "Close Vehicle Policy",
            message = "Closing removes this vehicle from active expiry dashboards. Please choose a reason:",
            confirmText = "Close Vehicle",
            onConfirm = {
                viewModel.closeVehicle(selectedReason)
                showCloseDialog = false
            },
            dismissText = "Cancel",
            onDismiss = { showCloseDialog = false },
            isDestructive = true,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    closeReasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedReason == reason) accentBlue.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { selectedReason = reason }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = reason,
                                fontSize = 13.sp,
                                fontWeight = if (selectedReason == reason) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedReason == reason) accentBlue else labelPrimary
                            )
                        }
                    }
                }
            }
        )
    }

    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    Scaffold(
        topBar = {
            LiquidGlassTopBar(
                title = "Vehicle Details",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = labelPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Close without renewal", color = AppleSystemRed) },
                            onClick = {
                                showMenu = false
                                showCloseDialog = true
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            vehicleWithPolicies?.let { item ->
                SurfaceActionBar(
                    onRenew = { onNavigateToRenew(item.vehicle.regNo) }
                )
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        val data = vehicleWithPolicies
        if (data == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading vehicle details...", color = labelSecondary)
            }
        } else {
            val vehicle = data.vehicle
            val policy = data.currentPolicy
            val daysRemaining = policy?.computeDaysRemaining() ?: 0
            val status = policy?.computeCurrentStatus() ?: PolicyStatus.ACTIVE

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // VEHICLE NUMBER PLATE HEADER & COUNTDOWN (LIQUID GLASS)
                LiquidGlassCard(
                    shape = RoundedCornerShape(20.dp),
                    elevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Authentic Indian HSRP Number Plate
                        HsrpNumberPlate(
                            regNo = vehicle.regNo
                        )

                        DaysRemainingBadge(
                            daysRemaining = daysRemaining,
                            status = status
                        )
                    }

                    vehicle.makeModel?.let { make ->
                        if (make.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = make,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = labelSecondary
                            )
                        }
                    }

                    // Live Countdown Banner
                    Spacer(modifier = Modifier.height(12.dp))
                    CountdownLiveBanner(
                        daysRemaining = daysRemaining,
                        status = status,
                        expiryDate = policy?.expiryDate
                    )
                }

                // QUICK CONTACT ACTIONS
                CustomerContactBar(
                    customerName = vehicle.customer.name,
                    mobile = vehicle.customer.mobile,
                    regNo = vehicle.regNo,
                    context = context
                )

                // CUSTOMER INFORMATION GROUP
                LiquidGlassGroupedSection(title = "Customer Details") {
                    val fullTitleName = listOfNotNull(vehicle.customer.title, vehicle.customer.name).joinToString(" ")
                    DetailRow(label = "Customer Name", value = fullTitleName)
                    DetailRow(label = "Mobile Number", value = vehicle.customer.mobile)
                    vehicle.customer.email?.let { email ->
                        if (email.isNotBlank()) {
                            DetailRow(label = "Email Address", value = email)
                        }
                    }
                    DetailRow(label = "Residential Address", value = vehicle.customer.address)
                }

                // CURRENT POLICY DETAILS GROUP
                if (policy != null) {
                    LiquidGlassGroupedSection(title = "Current Policy Details") {
                        DetailRow(label = "Insurer Company", value = policy.insurer)
                        DetailRow(label = "Company Sector", value = policy.companySector.displayName)
                        DetailRow(label = "Policy Number", value = policy.policyNumber)
                        DetailRow(label = "Expiry Date", value = UiFormatters.formatDate(policy.expiryDate))
                        DetailRow(label = "Total Premium Cost", value = UiFormatters.formatCurrency(policy.totalPremium))
                        DetailRow(label = "Policy Status", value = status.displayName)
                    }
                }

                // VEHICLE SPECIFICATIONS & NOTES
                LiquidGlassGroupedSection(title = "Specifications & Notes") {
                    vehicle.vin?.let { vin ->
                        if (vin.isNotBlank()) {
                            DetailRow(label = "Chassis Number (VIN)", value = vin)
                        }
                    }
                    vehicle.engineNo?.let { engine ->
                        if (engine.isNotBlank()) {
                            DetailRow(label = "Engine Number", value = engine)
                        }
                    }
                    vehicle.notes?.let { notes ->
                        if (notes.isNotBlank()) {
                            DetailRow(label = "Client Preferences & Notes", value = notes)
                        }
                    }
                }

                // POLICY HISTORY TIMELINE
                if (data.policyHistory.isNotEmpty()) {
                    LiquidGlassGroupedSection(title = "Policy History Timeline") {
                        data.policyHistory.forEach { historyPolicy ->
                            PolicyHistoryItem(policy = historyPolicy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun CountdownLiveBanner(
    daysRemaining: Long,
    status: PolicyStatus,
    expiryDate: Long?
) {
    val expiryStr = expiryDate?.let { UiFormatters.formatDate(it) } ?: "Unknown"
    val isDark = isSystemInDarkTheme()
    val secondaryLabel = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val green = if (isDark) com.policydesk.app.ui.theme.AppleDarkSystemGreen else AppleSystemGreen
    val red = if (isDark) com.policydesk.app.ui.theme.AppleDarkSystemRed else AppleSystemRed
    val deepOrange = if (isDark) com.policydesk.app.ui.theme.AppleDarkSystemDeepOrange else AppleSystemDeepOrange
    val orange = if (isDark) com.policydesk.app.ui.theme.AppleDarkSystemOrange else AppleSystemOrange

    val (accentColor, text) = when {
        status == PolicyStatus.CLOSED -> Pair(secondaryLabel, "Vehicle is Closed")
        status == PolicyStatus.RENEWED -> Pair(green, "Policy is Renewed")
        daysRemaining < 0 -> Pair(
            red,
            "Expired on $expiryStr (Overdue by ${-daysRemaining} days)"
        )
        daysRemaining == 0L -> Pair(
            red,
            "Policy expires TODAY ($expiryStr)"
        )
        daysRemaining <= 7 -> Pair(
            deepOrange,
            "Urgent: Expires in $daysRemaining days on $expiryStr"
        )
        daysRemaining <= 30 -> Pair(
            orange,
            "Expiring Soon: $daysRemaining days left (Expires $expiryStr)"
        )
        else -> Pair(
            green,
            "Policy Active: $daysRemaining days remaining (Expires $expiryStr)"
        )
    }

    val bannerShape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(bannerShape)
            .background(accentColor.copy(alpha = 0.12f), bannerShape)
            .border(0.75.dp, accentColor.copy(alpha = 0.30f), bannerShape)
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = AppleIcons.ShieldCheck,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CustomerContactBar(
    customerName: String,
    mobile: String,
    regNo: String,
    context: android.content.Context
) {
    LiquidGlassCard(
        shape = RoundedCornerShape(18.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ContactActionButton(
                icon = AppleIcons.Phone,
                label = "Call",
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobile"))
                    context.startActivity(intent)
                }
            )
            ContactActionButton(
                icon = AppleIcons.Message,
                label = "WhatsApp",
                onClick = {
                    val formattedMobile = if (mobile.startsWith("91")) mobile else "91$mobile"
                    val msg = Uri.encode("Hello $customerName, regarding your vehicle policy for ${RegistrationNormalizer.formatForDisplay(regNo)}:")
                    val url = "https://wa.me/$formattedMobile?text=$msg"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            )
            ContactActionButton(
                icon = AppleIcons.Message,
                label = "SMS",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$mobile"))
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun ContactActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(AppleSystemBlue.copy(alpha = 0.12f), CircleShape)
                .border(0.5.dp, AppleSystemBlue.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AppleSystemBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        val isDark = isSystemInDarkTheme()
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (isDark) IosDarkLabelSecondary else IosLabelSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) IosDarkLabelPrimary else IosLabelPrimary,
            modifier = Modifier.weight(1.3f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
fun PolicyHistoryItem(policy: Policy) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = AppleIcons.History,
            contentDescription = null,
            tint = if (isDark) IosDarkLabelSecondary else IosLabelSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${policy.insurer} • ${policy.policyNumber}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
            )
            Text(
                text = "Expired: ${UiFormatters.formatDate(policy.expiryDate)} • Premium: ${UiFormatters.formatCurrency(policy.totalPremium)}",
                fontSize = 12.sp,
                color = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
            )
        }
    }
}

@Composable
fun SurfaceActionBar(
    onRenew: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                elevation = 8.dp,
                fillAlphaTop = 0.95f,
                fillAlphaBottom = 0.88f
            )
            .padding(16.dp)
    ) {
        AppleButton(
            text = "Renew Policy",
            onClick = onRenew,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
