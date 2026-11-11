package com.policydesk.app.ui.detail

import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.data.profile.AdvisorProfileManager
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.ui.addpolicy.formFieldColors
import com.policydesk.app.ui.components.AppleAlertDialog
import com.policydesk.app.ui.components.AppleButton
import com.policydesk.app.ui.components.AppleSheetGrabber
import com.policydesk.app.ui.components.AppleSprings
import com.policydesk.app.ui.components.BindBackToTop
import com.policydesk.app.ui.components.DaysRemainingBadge
import com.policydesk.app.ui.components.HsrpNumberPlate
import com.policydesk.app.ui.components.LiquidGlassActionDock
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.components.LiquidGlassGroupedSection
import com.policydesk.app.ui.components.LocalScrollToTopDispatcher
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkSeparator
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosSeparator
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch

/**
 * WCAG AA (>= 4.5:1) semantic text & badge colors matching the 60fps.design palette.
 */
internal object AccessibleStatusColors {
    fun blue(isDark: Boolean): Color = if (isDark) Color(0xFF4CA6FF) else Color(0xFF0056B8)
    fun green(isDark: Boolean): Color = if (isDark) Color(0xFF34D375) else Color(0xFF146C34)
    fun orange(isDark: Boolean): Color = if (isDark) Color(0xFFFBBF24) else Color(0xFF9A4104)
    fun deepOrange(isDark: Boolean): Color = if (isDark) Color(0xFFFB923C) else Color(0xFFB33909)
    fun red(isDark: Boolean): Color = if (isDark) Color(0xFFFF6B63) else Color(0xFFBE1B1B)
}

/**
 * Observes Android system animator scale on resume so reduce-motion preferences take effect live.
 */
@Composable
internal fun rememberAnimatorsEnabled(): Boolean {
    val lifecycleOwner = LocalLifecycleOwner.current
    var enabled by remember {
        mutableStateOf(runCatching { ValueAnimator.areAnimatorsEnabled() }.getOrDefault(true))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = runCatching { ValueAnimator.areAnimatorsEnabled() }.getOrDefault(true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return enabled
}

/**
 * Overlaid refractive Liquid Glass top bar shared across flow screens so scrolling content
 * passes underneath and fades in a scroll-reactive hairline separator.
 */
@Composable
internal fun OverlaidGlassTopBar(
    title: String,
    isScrolled: Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    onDoubleTap: (() -> Unit)? = null,
    animatorsEnabled: Boolean = rememberAnimatorsEnabled(),
    actions: @Composable RowScope.() -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val scrollDispatcher = LocalScrollToTopDispatcher.current
    val effectiveDoubleTap = onDoubleTap ?: scrollDispatcher
    var lastTopTapTime by remember { mutableStateOf(0L) }

    val primaryText = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val secondaryText = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = AccessibleStatusColors.blue(isDark)
    val separatorColor = if (isDark) IosDarkSeparator else IosSeparator

    val hairlineAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        animationSpec = if (animatorsEnabled) tween(180) else snap(),
        label = "overlaid_topbar_hairline"
    )
    val bgTopAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 0.84f else 0.72f,
        animationSpec = if (animatorsEnabled) tween(180) else snap(),
        label = "overlaid_topbar_bg"
    )

    val glassTop = (if (isDark) Color(0xFF1C1C20) else Color(0xFFF8F9FC)).copy(alpha = bgTopAlpha)
    val glassBot = (if (isDark) Color(0xFF121216) else Color(0xFFF2F2F7)).copy(alpha = bgTopAlpha * 0.92f)
    val hazeMod = runCatching {
        Modifier.hazeEffect(
            state = hazeState,
            style = HazeStyle(
                backgroundColor = glassTop,
                blurRadius = 22.dp,
                tints = listOf(HazeTint(glassTop)),
                noiseFactor = 0.04f
            )
        )
    }.getOrElse { Modifier }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(hazeMod)
            .background(Brush.verticalGradient(listOf(glassTop, glassBot)))
            .drawBehind {
                if (hairlineAlpha > 0.01f) {
                    drawLine(
                        color = separatorColor.copy(alpha = 0.55f * hairlineAlpha),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 0.75.dp.toPx()
                    )
                }
            }
            .pointerInput(effectiveDoubleTap) {
                detectTapGestures(
                    onDoubleTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        effectiveDoubleTap.invoke()
                    }
                )
            }
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (onNavigateBack != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x28787880) else Color(0x18787880))
                            .clickable(role = Role.Button) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNavigateBack()
                            }
                            .semantics { contentDescription = "Back" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppleIcons.ChevronLeft,
                            contentDescription = null,
                            tint = accentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column(
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        val now = System.currentTimeMillis()
                        if (now - lastTopTapTime < 450L) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            effectiveDoubleTap.invoke()
                            lastTopTapTime = 0L
                        } else {
                            lastTopTapTime = now
                        }
                    }
                ) {
                    Text(
                        text = title,
                        fontSize = if (subtitle == null) 21.sp else 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        letterSpacing = (-0.4).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = secondaryText,
                            letterSpacing = 0.1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                content = actions
            )
        }
    }
}

@Composable
fun VehicleDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRenew: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    viewModel: VehicleDetailViewModel = hiltViewModel()
) {
    val vehicleWithPolicies by viewModel.vehicleData.collectAsStateWithLifecycle()
    val siblingVehicles by viewModel.siblingVehicles.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val animatorsEnabled = rememberAnimatorsEnabled()

    var showMenu by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditPremiumDialog by remember { mutableStateOf(false) }
    var showWhatsAppSheet by remember { mutableStateOf(false) }
    var editPremiumValue by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is DetailUiEvent.VehicleClosed -> {
                    Toast.makeText(context, "Vehicle closed", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is DetailUiEvent.VehicleDeleted -> {
                    Toast.makeText(context, "Vehicle record deleted", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is DetailUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = AccessibleStatusColors.blue(isDark)
    val accentRed = AccessibleStatusColors.red(isDark)
    val localHaze = remember { HazeState() }
    val scrollState = rememberScrollState()
    BindBackToTop(scrollState)

    val isScrolled by remember { derivedStateOf { scrollState.value > 4 } }
    var topBarHeightPx by remember { mutableIntStateOf(0) }
    val topBarHeightDp = with(LocalDensity.current) { topBarHeightPx.toDp() }

    if (showCloseDialog) {
        val closeReasons = listOf(
            "Vehicle sold",
            "Moved to another advisor / insurer",
            "Customer declined renewal",
            "Other reason"
        )
        var selectedReason by remember { mutableStateOf(closeReasons.first()) }

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
                        val isSelected = selectedReason == reason
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) accentBlue.copy(alpha = 0.14f) else Color.Transparent)
                                .clickable(role = Role.RadioButton) { selectedReason = reason }
                                .semantics { selected = isSelected }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = reason,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) accentBlue else labelPrimary
                            )
                        }
                    }
                }
            }
        )
    }

    if (showEditPremiumDialog) {
        val parsedAmount = editPremiumValue.toDoubleOrNull()
        val isAmountValid = parsedAmount != null && parsedAmount > 0.0

        AppleAlertDialog(
            onDismissRequest = { showEditPremiumDialog = false },
            title = "Edit Policy Premium",
            message = "Update the annual policy premium for ${vehicleWithPolicies?.vehicle?.regNo ?: "vehicle"}:",
            confirmText = "Save",
            onConfirm = {
                if (isAmountValid) {
                    viewModel.updateCurrentPolicyPremium(parsedAmount!!)
                    showEditPremiumDialog = false
                } else {
                    Toast.makeText(context, "Enter a positive premium amount", Toast.LENGTH_SHORT).show()
                }
            },
            dismissText = "Cancel",
            onDismiss = { showEditPremiumDialog = false },
            isDestructive = false,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = editPremiumValue,
                        onValueChange = { input ->
                            editPremiumValue = filterDecimalInput(input)
                        },
                        label = { Text("Annual Premium (₹)") },
                        isError = editPremiumValue.isNotBlank() && !isAmountValid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isAmountValid) {
                        Text(
                            text = "Enter an amount greater than 0",
                            fontSize = 11.sp,
                            color = accentRed
                        )
                    }
                }
            }
        )
    }

    if (showDeleteDialog) {
        AppleAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "Delete Vehicle Record?",
            message = "Are you sure you want to delete ${vehicleWithPolicies?.vehicle?.regNo ?: "this vehicle"} and its policies? This record will be archived.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteVehicle()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showWhatsAppSheet) {
        val currentItem = vehicleWithPolicies
        if (currentItem != null) {
            val allHousehold = listOf(currentItem) + siblingVehicles
            HouseholdWhatsAppSheet(
                items = allHousehold,
                context = context,
                onDismiss = { showWhatsAppSheet = false }
            )
        }
    }

    val currentItem = vehicleWithPolicies
    val currentPolicy = currentItem?.currentPolicy
    val cleanNumber = currentItem?.vehicle?.customer?.mobile?.trim().orEmpty()
    val hasPhone = cleanNumber.any { it.isDigit() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable detail layer marked as hazeSource so both top bar and bottom dock refract scrolling cards
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = localHaze)
        ) {
            if (currentItem == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topBarHeightDp),
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
                        Text("Loading vehicle details...", fontSize = 13.sp, color = labelSecondary)
                    }
                }
            } else {
                val vehicle = currentItem.vehicle
                val policy = currentItem.currentPolicy
                val daysRemaining = policy?.computeDaysRemaining() ?: 0
                val status = policy?.computeCurrentStatus() ?: PolicyStatus.ACTIVE

                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(topBarHeightDp + 10.dp))

                        // HERO CARD: HSRP PLATE, STATUS, MAKE/MODEL, ANNUAL PREMIUM HIGHLIGHT & COUNTDOWN
                        LiquidGlassCard(
                            shape = RoundedCornerShape(20.dp),
                            elevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HsrpNumberPlate(
                                    regNo = vehicle.regNo,
                                    makeModel = vehicle.makeModel,
                                    style = vehicle.plateStyle,
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .padding(end = 8.dp)
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
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = labelPrimary
                                    )
                                }
                            }

                            if (policy != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                val highlightShape = RoundedCornerShape(12.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(highlightShape)
                                        .background(accentBlue.copy(alpha = if (isDark) 0.18f else 0.10f), highlightShape)
                                        .border(
                                            0.75.dp,
                                            accentBlue.copy(alpha = if (isDark) 0.34f else 0.22f),
                                            highlightShape
                                        )
                                        .clickable(role = Role.Button) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            val currentPrem = policy.totalPremium
                                            editPremiumValue = if (currentPrem % 1.0 == 0.0) {
                                                currentPrem.toLong().toString()
                                            } else {
                                                currentPrem.toString()
                                            }
                                            showEditPremiumDialog = true
                                        }
                                        .semantics {
                                            contentDescription = "Annual Premium ${UiFormatters.formatCurrency(policy.totalPremium)}. Tap to edit."
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Annual Premium",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = accentBlue
                                        )
                                        Text(
                                            text = "Tap to adjust",
                                            fontSize = 10.5.sp,
                                            color = accentBlue.copy(alpha = 0.78f)
                                        )
                                    }
                                    Text(
                                        text = UiFormatters.formatCurrency(policy.totalPremium),
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentBlue,
                                        style = TextStyle(fontFeatureSettings = "tnum")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            CountdownLiveBanner(
                                daysRemaining = daysRemaining,
                                status = status,
                                expiryDate = policy?.expiryDate
                            )
                        }

                        // CUSTOMER INFORMATION GROUP
                        LiquidGlassGroupedSection(title = "Policyholder") {
                            val fullTitleName = listOfNotNull(vehicle.customer.title, vehicle.customer.name)
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                            DetailRow(label = "Client Name", value = fullTitleName)
                            DetailRow(label = "Mobile Number", value = vehicle.customer.mobile)
                            vehicle.customer.email?.let { email ->
                                if (email.isNotBlank()) {
                                    DetailRow(label = "Email Address", value = email)
                                }
                            }
                            if (vehicle.customer.address.isNotBlank()) {
                                DetailRow(label = "Residential Address", value = vehicle.customer.address)
                            }
                            DetailRow(
                                label = "Client Relationship",
                                value = "Since ${UiFormatters.formatMonthYear(currentItem.earliestDate)} (${UiFormatters.formatTenure(currentItem.earliestDate)})"
                            )
                            DetailRow(
                                label = "Policy Track Record",
                                value = "${currentItem.policies.size} ${if (currentItem.policies.size == 1) "Policy" else "Policies"} (${currentItem.policyHistory.size} ${if (currentItem.policyHistory.size == 1) "Renewal" else "Renewals"})"
                            )
                        }

                        // MULTI-VEHICLE HOUSEHOLD SWITCHER
                        if (siblingVehicles.isNotEmpty()) {
                            LiquidGlassGroupedSection(
                                title = "Other Vehicles in Household (${siblingVehicles.size})"
                            ) {
                                siblingVehicles.forEachIndexed { idx, sibling ->
                                    val sibPolicy = sibling.currentPolicy
                                    val sibDays = sibPolicy?.computeDaysRemaining() ?: 0L
                                    val sibStatus = sibPolicy?.computeCurrentStatus() ?: PolicyStatus.ACTIVE

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable(role = Role.Button) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                viewModel.selectVehicle(sibling.vehicle.regNo)
                                                coroutineScope.launch {
                                                    if (animatorsEnabled) scrollState.animateScrollTo(0) else scrollState.scrollTo(0)
                                                }
                                            }
                                            .padding(vertical = 8.dp, horizontal = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            HsrpNumberPlate(
                                                regNo = sibling.vehicle.regNo,
                                                makeModel = sibling.vehicle.makeModel,
                                                style = sibling.vehicle.plateStyle
                                            )
                                            sibling.vehicle.makeModel?.takeIf { it.isNotBlank() }?.let { model ->
                                                Text(
                                                    text = model,
                                                    fontSize = 12.sp,
                                                    color = labelSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            DaysRemainingBadge(daysRemaining = sibDays, status = sibStatus)
                                            Icon(
                                                imageVector = AppleIcons.ChevronRight,
                                                contentDescription = "Switch to ${sibling.vehicle.regNo}",
                                                tint = labelSecondary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    if (idx < siblingVehicles.lastIndex) {
                                        HorizontalDivider(
                                            color = if (isDark) IosDarkSeparator else IosSeparator,
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }

                        // ACTIVE POLICY SCHEDULE
                        if (policy != null) {
                            LiquidGlassGroupedSection(title = "Active Policy Schedule") {
                                DetailRow(label = "Insurer Company", value = policy.insurer)
                                DetailRow(label = "Company Sector", value = policy.companySector.displayName)
                                DetailRow(label = "Policy Number", value = policy.policyNumber)
                                DetailRow(label = "Expiry Date", value = UiFormatters.formatDate(policy.expiryDate))
                                DetailRow(
                                    label = "Total Premium Cost",
                                    value = UiFormatters.formatCurrency(policy.totalPremium),
                                    onClick = {
                                        val currentPrem = policy.totalPremium
                                        editPremiumValue = if (currentPrem % 1.0 == 0.0) currentPrem.toLong().toString() else currentPrem.toString()
                                        showEditPremiumDialog = true
                                    }
                                )
                                if (policy.commissionAmount != null || policy.commissionPercentage != null) {
                                    val commAmt = policy.commissionAmount?.let { UiFormatters.formatCurrency(it) } ?: "—"
                                    val commPct = policy.commissionPercentage?.let { pct ->
                                        val formatted = if (pct % 1.0 == 0.0) pct.toLong().toString() else String.format(java.util.Locale.US, "%.1f", pct)
                                        " ($formatted%)"
                                    } ?: ""
                                    DetailRow(label = "Advisor Commission", value = "$commAmt$commPct")
                                }
                                val statusText = when {
                                    status == PolicyStatus.CLOSED -> "Closed"
                                    status == PolicyStatus.RENEWED -> "Renewed"
                                    daysRemaining < 0 -> "Overdue (${-daysRemaining}d)"
                                    daysRemaining == 0L -> "Expires Today"
                                    daysRemaining <= 30 -> "Expiring Soon (${daysRemaining}d left)"
                                    else -> status.displayName
                                }
                                DetailRow(label = "Policy Status", value = statusText)
                            }
                        }

                        // VEHICLE SPECIFICATIONS & NOTES (render only when content exists)
                        val hasSpecs = !vehicle.vin.isNullOrBlank() ||
                            !vehicle.engineNo.isNullOrBlank() ||
                            !vehicle.notes.isNullOrBlank()
                        if (hasSpecs) {
                            LiquidGlassGroupedSection(title = "Specifications & Notes") {
                                vehicle.vin?.takeIf { it.isNotBlank() }?.let { vin ->
                                    DetailRow(label = "Chassis Number (VIN)", value = vin)
                                }
                                vehicle.engineNo?.takeIf { it.isNotBlank() }?.let { engine ->
                                    DetailRow(label = "Engine Number", value = engine)
                                }
                                vehicle.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                                    DetailRow(label = "Client Preferences & Notes", value = notes)
                                }
                            }
                        }

                        // POLICY HISTORY TIMELINE
                        if (currentItem.policyHistory.isNotEmpty()) {
                            LiquidGlassGroupedSection(title = "Policy History Timeline") {
                                currentItem.policyHistory.forEach { historyPolicy ->
                                    PolicyHistoryItem(policy = historyPolicy)
                                }
                            }
                        }

                        Spacer(
                            modifier = Modifier
                                .height(112.dp)
                                .navigationBarsPadding()
                        )
                    }
                }
            }
        }

        // OVERLAID LIQUID GLASS TOP BAR (refracts scrolled detail cards + scroll-reactive hairline)
        OverlaidGlassTopBar(
            title = "Vehicle Details",
            subtitle = currentPolicy?.let { "${it.insurer} • ${it.companySector.displayName}" },
            isScrolled = isScrolled,
            hazeState = localHaze,
            onNavigateBack = onNavigateBack,
            animatorsEnabled = animatorsEnabled,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { topBarHeightPx = it.height }
        ) {
            IconButton(
                onClick = {
                    currentItem?.vehicle?.regNo?.let(onNavigateToEdit)
                },
                enabled = currentItem != null
            ) {
                Icon(
                    imageVector = AppleIcons.SquareAndPencil,
                    contentDescription = "Edit All Details",
                    tint = accentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    enabled = currentItem != null
                ) {
                    Icon(
                        imageVector = AppleIcons.Ellipsis,
                        contentDescription = "More Options",
                        tint = labelPrimary
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (hasPhone) {
                        DropdownMenuItem(
                            text = { Text("Send SMS", color = labelPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = AppleIcons.Message,
                                    contentDescription = null,
                                    tint = accentBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                launchContactIntent(
                                    context = context,
                                    intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$cleanNumber")),
                                    fallbackMessage = "SMS app unavailable"
                                )
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Close without renewal", color = accentRed) },
                        leadingIcon = {
                            Icon(
                                imageVector = AppleIcons.Clear,
                                contentDescription = null,
                                tint = accentRed,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            showCloseDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Vehicle", color = accentRed) },
                        leadingIcon = {
                            Icon(
                                imageVector = AppleIcons.Trash,
                                contentDescription = null,
                                tint = accentRed,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        // STICKY BOTTOM LIQUID GLASS ACTION DOCK (Call, WhatsApp, Renew)
        AnimatedVisibility(
            visible = currentItem != null,
            enter = if (animatorsEnabled) {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
                ) + fadeIn()
            } else EnterTransition.None,
            exit = if (animatorsEnabled) {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
                ) + fadeOut()
            } else ExitTransition.None,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            currentItem?.let { item ->
                val curPolicy = item.currentPolicy
                VehicleDetailActionDock(
                    customerName = item.vehicle.customer.name,
                    mobile = item.vehicle.customer.mobile,
                    regNo = item.vehicle.regNo,
                    insurer = curPolicy?.insurer ?: "",
                    expiryDate = curPolicy?.expiryDate?.let { UiFormatters.formatDate(it) } ?: "",
                    premium = curPolicy?.totalPremium?.let { UiFormatters.formatCurrency(it) } ?: "",
                    hasSiblingVehicles = siblingVehicles.isNotEmpty(),
                    context = context,
                    hazeState = localHaze,
                    onOpenWhatsAppComposer = { showWhatsAppSheet = true },
                    onRenew = { onNavigateToRenew(item.vehicle.regNo) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HouseholdWhatsAppSheet(
    items: List<VehicleWithPolicies>,
    context: Context,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val accentBlue = AccessibleStatusColors.blue(isDark)
    val accentGreen = AccessibleStatusColors.green(isDark)

    var selectedRegNo by remember(items) { mutableStateOf(items.first().vehicle.regNo) }
    val selectedItem = items.firstOrNull { it.vehicle.regNo == selectedRegNo } ?: items.first()
    val buildTemplate = { item: VehicleWithPolicies ->
        val p = item.currentPolicy
        AdvisorProfileManager.formatRenewalMessage(
            context = context,
            customerName = item.vehicle.customer.name,
            regNo = item.vehicle.regNo,
            insurer = p?.insurer ?: "",
            expiryDate = p?.expiryDate?.let { UiFormatters.formatDate(it) } ?: "",
            premium = p?.totalPremium?.let { UiFormatters.formatCurrency(it) } ?: ""
        )
    }
    var messageText by remember(selectedRegNo) { mutableStateOf(buildTemplate(selectedItem)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7),
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        dragHandle = { AppleSheetGrabber() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WhatsApp Renewal Notice",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelPrimary
                    )
                    Text(
                        text = "${selectedItem.vehicle.customer.name} • ${selectedItem.vehicle.customer.mobile}",
                        fontSize = 12.5.sp,
                        color = labelSecondary,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x33787880) else Color(0x1A787880))
                        .clickable(role = Role.Button, onClick = onDismiss)
                        .semantics { contentDescription = "Close WhatsApp notice composer" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppleIcons.Clear,
                        contentDescription = null,
                        tint = labelSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = "SELECT VEHICLE IN HOUSEHOLD",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = labelSecondary,
                letterSpacing = 0.4.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { item ->
                    val isSelected = item.vehicle.regNo == selectedRegNo
                    val pillShape = RoundedCornerShape(12.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(pillShape)
                            .background(
                                if (isSelected) accentBlue.copy(alpha = if (isDark) 0.22f else 0.11f)
                                else if (isDark) Color(0xFF2C2C2E) else Color.White,
                                pillShape
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.75.dp,
                                color = if (isSelected) accentBlue else (if (isDark) Color(0x33FFFFFF) else Color(0x1E000000)),
                                shape = pillShape
                            )
                            .clickable(role = Role.RadioButton) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedRegNo = item.vehicle.regNo
                            }
                            .semantics { selected = isSelected }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            HsrpNumberPlate(
                                regNo = item.vehicle.regNo,
                                makeModel = item.vehicle.makeModel,
                                style = item.vehicle.plateStyle
                            )
                            item.vehicle.makeModel?.takeIf { it.isNotBlank() }?.let { model ->
                                Text(
                                    text = model,
                                    fontSize = 11.5.sp,
                                    color = labelSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        item.currentPolicy?.let { p ->
                            DaysRemainingBadge(
                                daysRemaining = p.computeDaysRemaining(),
                                status = p.computeCurrentStatus()
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("Renewal Message") },
                minLines = 4,
                maxLines = 7,
                shape = RoundedCornerShape(12.dp),
                colors = formFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            AppleButton(
                text = "Open in WhatsApp",
                icon = AppleIcons.WhatsApp,
                containerColor = accentGreen,
                onClick = {
                    launchWhatsAppMessage(
                        context = context,
                        mobile = selectedItem.vehicle.customer.mobile,
                        message = messageText
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
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
    val green = AccessibleStatusColors.green(isDark)
    val red = AccessibleStatusColors.red(isDark)
    val deepOrange = AccessibleStatusColors.deepOrange(isDark)
    val orange = AccessibleStatusColors.orange(isDark)

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
            "Policy Active: ${UiFormatters.formatCompoundDurationDetailed(daysRemaining)} remaining (Expires $expiryStr)"
        )
    }

    val bannerShape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(bannerShape)
            .background(accentColor.copy(alpha = if (isDark) 0.18f else 0.10f), bannerShape)
            .border(0.75.dp, accentColor.copy(alpha = 0.28f), bannerShape)
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
                fontWeight = FontWeight.SemiBold,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
        }
    }
}

@Composable
private fun VehicleDetailActionDock(
    customerName: String,
    mobile: String,
    regNo: String,
    insurer: String,
    expiryDate: String,
    premium: String,
    hasSiblingVehicles: Boolean,
    context: Context,
    hazeState: HazeState,
    onOpenWhatsAppComposer: () -> Unit,
    onRenew: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val accentBlue = AccessibleStatusColors.blue(isDark)
    val accentGreen = AccessibleStatusColors.green(isDark)
    val cleanNumber = mobile.trim()
    val hasPhone = cleanNumber.any { it.isDigit() }

    LiquidGlassActionDock(hazeState = hazeState) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockActionPillButton(
                label = "Call",
                icon = AppleIcons.Phone,
                tintColor = accentBlue,
                enabled = hasPhone,
                modifier = Modifier.weight(0.95f),
                onClick = {
                    launchContactIntent(
                        context = context,
                        intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")),
                        fallbackMessage = "Dialer app unavailable"
                    )
                }
            )
            DockActionPillButton(
                label = "WhatsApp",
                icon = AppleIcons.WhatsApp,
                tintColor = accentGreen,
                enabled = hasPhone,
                modifier = Modifier.weight(1.15f),
                onClick = {
                    if (hasSiblingVehicles) {
                        onOpenWhatsAppComposer()
                    } else {
                        val msg = AdvisorProfileManager.formatRenewalMessage(
                            context = context,
                            customerName = customerName,
                            regNo = regNo,
                            insurer = insurer,
                            expiryDate = expiryDate,
                            premium = premium
                        )
                        launchWhatsAppMessage(
                            context = context,
                            mobile = cleanNumber,
                            message = msg
                        )
                    }
                }
            )
            AppleButton(
                text = "Renew",
                icon = AppleIcons.History,
                onClick = onRenew,
                height = 46.dp,
                shape = RoundedCornerShape(23.dp),
                horizontalPadding = 12.dp,
                fontSize = 14.sp,
                modifier = Modifier.weight(1.2f)
            )
        }
    }
}

@Composable
private fun DockActionPillButton(
    label: String,
    icon: ImageVector,
    tintColor: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val animatorsEnabled = rememberAnimatorsEnabled()
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = if (animatorsEnabled) AppleSprings.CardPress else snap(),
        label = "dock_pill_spring"
    )
    val effectiveTint = if (enabled) tintColor else tintColor.copy(alpha = 0.38f)
    val pillShape = RoundedCornerShape(23.dp)

    Box(
        modifier = modifier
            .height(46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(pillShape)
            .background(
                effectiveTint.copy(
                    alpha = when {
                        !enabled -> 0.06f
                        isDark -> 0.22f
                        else -> 0.12f
                    }
                ),
                pillShape
            )
            .border(
                0.75.dp,
                effectiveTint.copy(alpha = if (enabled) 0.30f else 0.12f),
                pillShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = effectiveTint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = effectiveTint,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun launchWhatsAppMessage(
    context: Context,
    mobile: String,
    message: String
) {
    val digits = mobile.filter { it.isDigit() }
    val formattedMobile = if (digits.length == 10) "91$digits" else digits
    val url = "https://wa.me/$formattedMobile?text=${Uri.encode(message)}"
    launchContactIntent(
        context = context,
        intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)),
        fallbackMessage = "WhatsApp is not installed"
    )
}

private fun launchContactIntent(
    context: Context,
    intent: Intent,
    fallbackMessage: String
) {
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, fallbackMessage, Toast.LENGTH_SHORT).show()
    }
}

private fun filterDecimalInput(input: String): String = buildString {
    var hasDot = false
    for (ch in input) {
        if (ch.isDigit()) {
            append(ch)
        } else if (ch == '.' && !hasDot) {
            append(ch)
            hasDot = true
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    canCopy: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val canCopyValue = canCopy && onClick == null && value.isNotBlank() && value != "N/A" && value != "None"

    val baseModifier = when {
        onClick != null -> {
            modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
        }
        canCopyValue -> {
            modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
                }
        }
        else -> modifier
    }
    Row(
        modifier = baseModifier
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
            textAlign = TextAlign.End,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}

@Composable
fun PolicyHistoryItem(policy: Policy) {
    val isDark = isSystemInDarkTheme()
    val green = AccessibleStatusColors.green(isDark)
    val accentBlue = AccessibleStatusColors.blue(isDark)
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    accentBlue.copy(alpha = if (isDark) 0.20f else 0.12f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AppleIcons.History,
                contentDescription = null,
                tint = accentBlue,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${policy.insurer} • ${policy.policyNumber}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) IosDarkLabelPrimary else IosLabelPrimary,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Text(
                    text = UiFormatters.formatCurrency(policy.totalPremium),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) IosDarkLabelPrimary else IosLabelPrimary,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expired: ${UiFormatters.formatDate(policy.expiryDate)}",
                    fontSize = 12.sp,
                    color = labelSecondary,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
                if (policy.renewedOn != null) {
                    Text(
                        text = "Renewed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = green
                    )
                }
            }
        }
    }
}
