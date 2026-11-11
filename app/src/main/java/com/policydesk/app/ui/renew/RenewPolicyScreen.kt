package com.policydesk.app.ui.renew

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.ui.addpolicy.FormSectionCard
import com.policydesk.app.ui.addpolicy.WarningBanner
import com.policydesk.app.ui.addpolicy.formDisabledFieldColors
import com.policydesk.app.ui.addpolicy.formFieldColors
import com.policydesk.app.ui.components.AppleSprings
import com.policydesk.app.ui.components.BindBackToTop
import com.policydesk.app.ui.components.CupertinoTopBarAction
import com.policydesk.app.ui.components.HsrpNumberPlate
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.detail.AccessibleStatusColors
import com.policydesk.app.ui.detail.DetailRow
import com.policydesk.app.ui.detail.OverlaidGlassTopBar
import com.policydesk.app.ui.detail.rememberAnimatorsEnabled
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenewPolicyScreen(
    onNavigateBack: () -> Unit,
    onRenewalComplete: (String) -> Unit,
    viewModel: RenewPolicyViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val animatorsEnabled = rememberAnimatorsEnabled()

    var showDatePicker by remember { mutableStateOf(false) }
    var hasExited by remember { mutableStateOf(false) }
    val currentOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val currentOnRenewalComplete by rememberUpdatedState(onRenewalComplete)

    DisposableEffect(Unit) {
        onDispose {
            hasExited = true
        }
    }

    fun handleSafeBack() {
        if (hasExited || state.isSaving) return
        hasExited = true
        currentOnNavigateBack()
    }

    BackHandler(enabled = !hasExited) {
        if (!state.isSaving) {
            handleSafeBack()
        }
    }

    // Auto-pop after a brief Undo window; cancelled automatically if user taps Undo, Back, or leaves screen.
    val activeRenewalToken = state.renewalToken
    LaunchedEffect(activeRenewalToken, state.isRenewed) {
        if (state.isRenewed && activeRenewalToken != 0L) {
            delay(1800L)
            if (!hasExited) {
                hasExited = true
                val targetReg = state.currentVehicle?.vehicle?.regNo ?: ""
                currentOnRenewalComplete(targetReg)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is RenewUiEvent.RenewSuccess -> Unit // Handled by inline Undo pill + draw-in checkmark
                is RenewUiEvent.RenewalReverted -> {
                    Toast.makeText(context, "Renewal reverted", Toast.LENGTH_SHORT).show()
                }
                is RenewUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.newExpiryEpochMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                CupertinoTopBarAction(
                    text = "Done",
                    isProminent = true,
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onExpiryDateSelected(it)
                        }
                        showDatePicker = false
                    }
                )
            },
            dismissButton = {
                CupertinoTopBarAction(
                    text = "Cancel",
                    onClick = { showDatePicker = false }
                )
            }
        ) {
            DatePicker(state = datePickerState)
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

    val premiumFocusRequester = remember { FocusRequester() }
    val notesFocusRequester = remember { FocusRequester() }

    val item = state.currentVehicle
    val subtitle = item?.let {
        "${RegistrationNormalizer.formatForDisplay(it.vehicle.regNo)} • ${it.vehicle.customer.name}"
    } ?: "Update coverage & start countdown"

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = localHaze)
        ) {
            if (item == null) {
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
                        Text("Loading renewal form...", color = labelSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                val vehicle = item.vehicle
                val prevPolicy = item.currentPolicy
                val inputsEnabled = !state.isSaving && !state.isRenewed

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(topBarHeightDp + 10.dp))

                    // Vehicle Header Card
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        elevation = 2.dp
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HsrpNumberPlate(
                                    regNo = vehicle.regNo,
                                    makeModel = vehicle.makeModel,
                                    style = vehicle.plateStyle
                                )
                                Text(
                                    text = vehicle.customer.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = labelPrimary,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (prevPolicy != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                DetailRow(label = "Current Insurer", value = prevPolicy.insurer)
                                DetailRow(label = "Expiring / Expired On", value = UiFormatters.formatDate(prevPolicy.expiryDate))
                            }
                        }
                    }

                    // New Policy Details
                    FormSectionCard(title = "New Policy Information") {
                        // New Policy Number
                        OutlinedTextField(
                            value = state.newPolicyNumber,
                            onValueChange = viewModel::onPolicyNumberChanged,
                            enabled = inputsEnabled,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("New Policy Number *") },
                            placeholder = { Text("Enter renewal policy number") },
                            isError = state.newPolicyNumber.isNotEmpty() && !state.isPolicyNumberValid,
                            supportingText = if (state.newPolicyNumber.isNotEmpty() && !state.isPolicyNumberValid) {
                                { Text("Use letters, numbers, '/', '-', or '.'", color = accentRed) }
                            } else null,
                            singleLine = true,
                            textStyle = TextStyle(fontFeatureSettings = "tnum"),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    if (!state.isExpiryValid) {
                                        showDatePicker = true
                                    } else {
                                        premiumFocusRequester.requestFocus()
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(10.dp),
                            colors = formFieldColors()
                        )

                        // New Expiry Date
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = inputsEnabled, role = Role.Button) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showDatePicker = true
                                }
                                .semantics {
                                    contentDescription = "New Policy Expiry Date: ${state.newExpiryEpochMs?.let { UiFormatters.formatDate(it) } ?: "Not selected"}. Tap to change."
                                }
                        ) {
                            OutlinedTextField(
                                value = state.newExpiryEpochMs?.let { UiFormatters.formatDate(it) } ?: "",
                                onValueChange = {},
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                isError = state.newExpiryEpochMs != null && !state.isExpiryValid,
                                supportingText = if (state.newExpiryEpochMs != null && !state.isExpiryValid) {
                                    { Text("Select a future renewal expiry date", color = accentRed) }
                                } else null,
                                label = { Text("New Policy Expiry Date *") },
                                placeholder = { Text("Pick renewal expiry date") },
                                textStyle = TextStyle(fontFeatureSettings = "tnum"),
                                trailingIcon = {
                                    Icon(
                                        imageVector = AppleIcons.Calendar,
                                        contentDescription = null,
                                        tint = accentBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = formDisabledFieldColors()
                            )
                        }

                        if (state.earlierExpiryWarning) {
                            WarningBanner(
                                text = "Warning: The new expiry date is earlier than or equal to the previous expiry date."
                            )
                        }

                        // New Total Premium
                        OutlinedTextField(
                            value = state.newPremium,
                            onValueChange = viewModel::onPremiumChanged,
                            enabled = inputsEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(premiumFocusRequester),
                            label = { Text("New Total Premium Cost (₹) *") },
                            placeholder = { Text("e.g. 14500") },
                            isError = state.newPremium.isNotEmpty() && !state.isPremiumValid,
                            supportingText = if (state.newPremium.isNotEmpty() && !state.isPremiumValid) {
                                { Text("Enter a positive premium amount", color = accentRed) }
                            } else null,
                            singleLine = true,
                            textStyle = TextStyle(fontFeatureSettings = "tnum"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { notesFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(10.dp),
                            colors = formFieldColors()
                        )

                        // Renewal Notes
                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = viewModel::onNotesChanged,
                            enabled = inputsEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(notesFocusRequester),
                            label = { Text("Renewal Notes") },
                            placeholder = { Text("Add-ons included, NCB discount, etc.") },
                            minLines = 2,
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            shape = RoundedCornerShape(10.dp),
                            colors = formFieldColors()
                        )
                    }

                    // Inline Validation Guidance (single source of truth matching slider disabled state)
                    if (!state.isValid && !state.isSaving && !state.isRenewed) {
                        val bannerShape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = accentRed.copy(alpha = if (isDark) 0.16f else 0.09f),
                                    shape = bannerShape
                                )
                                .border(
                                    1.dp,
                                    accentRed.copy(alpha = 0.34f),
                                    bannerShape
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = AppleIcons.Warning,
                                    contentDescription = null,
                                    tint = accentRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Required to renew: ${state.validationErrors.joinToString(", ")}",
                                    fontSize = 12.sp,
                                    color = accentRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Bounded 60fps Swipe-to-Renew Slider + Accessible 1-Tap Confirm
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SwipeToRenewSlider(
                            enabled = state.isValid && !state.isSaving,
                            isSaving = state.isSaving,
                            isCompleted = state.isRenewed,
                            resetKey = state.renewalToken,
                            invalidGuidance = state.validationErrors.joinToString(", "),
                            animatorsEnabled = animatorsEnabled,
                            onConfirmed = {
                                focusManager.clearFocus()
                                viewModel.saveRenewal()
                            }
                        )

                        val canConfirm = state.isValid && !state.isSaving && !state.isRenewed
                        Text(
                            text = if (state.isRenewed) "Renewal confirmed" else "Or confirm renewal with one tap",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (canConfirm) accentBlue else labelSecondary.copy(alpha = 0.55f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(
                                    enabled = canConfirm,
                                    role = Role.Button
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    focusManager.clearFocus()
                                    viewModel.saveRenewal()
                                }
                                .semantics {
                                    contentDescription = "Confirm policy renewal with one tap"
                                    if (!canConfirm) disabled()
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .height(84.dp)
                            .navigationBarsPadding()
                    )
                }
            }
        }

        // Overlaid Refractive Glass Top Bar
        OverlaidGlassTopBar(
            title = "Renew Policy",
            subtitle = subtitle,
            isScrolled = isScrolled,
            hazeState = localHaze,
            onNavigateBack = ::handleSafeBack,
            animatorsEnabled = animatorsEnabled,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { topBarHeightPx = it.height }
        )

        // Floating Undo Confirmation Pill (cancels auto-pop cleanly on Undo, Done, or Back)
        AnimatedVisibility(
            visible = state.isRenewed && state.renewalToken != 0L,
            enter = if (animatorsEnabled) {
                slideInVertically(initialOffsetY = { it }) + fadeIn()
            } else EnterTransition.None,
            exit = if (animatorsEnabled) {
                slideOutVertically(targetOffsetY = { it }) + fadeOut()
            } else ExitTransition.None,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            val pillBg = if (isDark) Color(0xFF2C2C2E) else Color(0xFF1C1C1E)
            val undoBg = if (isDark) Color(0xFF0066D6) else Color(0xFF0062CC)
            Row(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .shadow(14.dp, RoundedCornerShape(18.dp), spotColor = Color(0x40000000))
                    .clip(RoundedCornerShape(18.dp))
                    .background(pillBg)
                    .border(0.75.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Policy renewed — countdown updated",
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(undoBg)
                            .clickable(role = Role.Button) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.undoRenewal()
                            }
                            .padding(horizontal = 11.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Undo",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable(role = Role.Button) {
                                if (!hasExited) {
                                    hasExited = true
                                    val targetReg = state.currentVehicle?.vehicle?.regNo ?: ""
                                    currentOnRenewalComplete(targetReg)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Done",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeToRenewSlider(
    enabled: Boolean,
    isSaving: Boolean,
    isCompleted: Boolean,
    resetKey: Long,
    invalidGuidance: String,
    animatorsEnabled: Boolean,
    onConfirmed: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val trackHeight = 58.dp
    val thumbSize = 50.dp
    val trackPadding = 4.dp
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val paddingPx = with(density) { trackPadding.toPx() }

    val dragAnimatable = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val checkProgress by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = if (isCompleted && animatorsEnabled) {
            tween(
                durationMillis = 420,
                delayMillis = 80,
                easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
            )
        } else {
            snap()
        },
        label = "swipe_check_draw"
    )

    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val accentBlue = AccessibleStatusColors.blue(isDark)
    val fillBlue = if (isDark) Color(0xFF0066D6) else Color(0xFF0062CC)
    val fillGreen = Color(0xFF15803D)
    val trackBg = if (isDark) Color(0x38545458) else Color(0x24767680)
    val trackBorder = if (isDark) Color(0x33FFFFFF) else Color(0x26000000)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(trackHeight)
            .alpha(if (enabled || isCompleted) 1f else 0.48f)
            .clip(CircleShape)
            .background(trackBg, CircleShape)
            .border(1.dp, trackBorder, CircleShape)
            .focusable(enabled = enabled && !isCompleted)
            .onKeyEvent { keyEvent ->
                if (enabled && !isCompleted && keyEvent.type == KeyEventType.KeyUp &&
                    (keyEvent.key == Key.Enter || keyEvent.key == Key.Spacebar || keyEvent.key == Key.DirectionRight)
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirmed()
                    true
                } else {
                    false
                }
            }
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "Slide to Renew and Activate policy"
                stateDescription = when {
                    isCompleted -> "Renewed and countdown active"
                    isSaving -> "Saving renewal"
                    !enabled -> "Disabled. Missing: $invalidGuidance"
                    else -> "Ready. Slide right or double tap to confirm renewal"
                }
                if (!enabled && !isCompleted) {
                    disabled()
                } else if (enabled && !isCompleted) {
                    onClick(label = "Confirm renewal") {
                        onConfirmed()
                        true
                    }
                }
            }
    ) {
        val trackWidthPx = with(density) { maxWidth.toPx() }
        val maxTravelPx = (trackWidthPx - thumbSizePx - paddingPx * 2f).coerceAtLeast(0f)

        // Snap thumb to right when completed, or smoothly back to 0 when reset (Undo / invalidation)
        LaunchedEffect(isCompleted, resetKey, enabled, maxTravelPx) {
            if (isDragging) return@LaunchedEffect
            val target = if (isCompleted) maxTravelPx else 0f
            if (animatorsEnabled) {
                dragAnimatable.animateTo(target, AppleSprings.Smooth)
            } else {
                dragAnimatable.snapTo(target)
            }
        }

        val clampedOffsetPx = dragAnimatable.value.coerceIn(0f, maxTravelPx)
        val progress = if (maxTravelPx > 0f) (clampedOffsetPx / maxTravelPx).coerceIn(0f, 1f) else 0f

        // Strictly bounded progress fill inside CircleShape track (zero clipping or left-edge overshoot)
        if (trackWidthPx > 0f) {
            val fillVisibleWidthPx = (paddingPx * 2f + thumbSizePx + clampedOffsetPx)
                .coerceIn(0f, trackWidthPx)
            val fillTranslationX = (fillVisibleWidthPx - trackWidthPx).coerceIn(-trackWidthPx, 0f)
            val fillBrush = if (isCompleted) {
                Brush.horizontalGradient(listOf(fillGreen, fillGreen))
            } else {
                Brush.horizontalGradient(listOf(fillBlue, fillGreen))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = fillTranslationX
                        alpha = if (isCompleted || clampedOffsetPx > 1f) 1f else 0.92f
                    }
                    .clip(CircleShape)
                    .background(fillBrush)
            )
        }

        // Track Label
        val labelText = when {
            isCompleted -> "Renewed & Countdown Active"
            isSaving -> "Activating Renewal..."
            !enabled -> "Complete required fields above"
            else -> "Slide to Renew & Activate"
        }
        val labelAlpha = if (isCompleted) 1f else (1f - progress * 1.15f).coerceIn(0.15f, 1f)
        val labelColor = if (isCompleted || progress > 0.55f) Color.White else labelPrimary

        Text(
            text = labelText,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.1).sp,
            color = labelColor.copy(alpha = labelAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 64.dp)
        )

        // Bounded Draggable Thumb
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = trackPadding)
                .size(thumbSize)
                .graphicsLayer {
                    translationX = clampedOffsetPx
                }
                .shadow(6.dp, CircleShape, spotColor = Color(0x38000000))
                .clip(CircleShape)
                .background(Color.White, CircleShape)
                .pointerInput(enabled, isCompleted, maxTravelPx) {
                    if (!enabled || isCompleted || maxTravelPx <= 0f) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val next = (dragAnimatable.value + dragAmount).coerceIn(0f, maxTravelPx)
                            scope.launch {
                                dragAnimatable.snapTo(next)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            val current = dragAnimatable.value.coerceIn(0f, maxTravelPx)
                            if (current >= maxTravelPx * 0.82f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    if (animatorsEnabled) {
                                        dragAnimatable.animateTo(maxTravelPx, AppleSprings.Smooth)
                                    } else {
                                        dragAnimatable.snapTo(maxTravelPx)
                                    }
                                }
                                onConfirmed()
                            } else {
                                scope.launch {
                                    if (animatorsEnabled) {
                                        dragAnimatable.animateTo(0f, AppleSprings.Smooth)
                                    } else {
                                        dragAnimatable.snapTo(0f)
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            scope.launch {
                                if (animatorsEnabled) {
                                    dragAnimatable.animateTo(0f, AppleSprings.Smooth)
                                } else {
                                    dragAnimatable.snapTo(0f)
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                isSaving -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = fillBlue
                    )
                }
                isCompleted -> {
                    AnimatedDrawCheckmark(
                        progress = checkProgress,
                        color = fillGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                else -> {
                    // Crisp vector right-arrow matching the 60fps.design swipe thumb
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val w = size.width
                        val h = size.height
                        val arrowPath = Path().apply {
                            moveTo(w * 0.18f, h * 0.50f)
                            lineTo(w * 0.80f, h * 0.50f)
                            moveTo(w * 0.54f, h * 0.24f)
                            lineTo(w * 0.82f, h * 0.50f)
                            lineTo(w * 0.54f, h * 0.76f)
                        }
                        drawPath(
                            path = arrowPath,
                            color = if (enabled) accentBlue else accentBlue.copy(alpha = 0.5f),
                            style = Stroke(
                                width = 2.4.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedDrawCheckmark(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val pathMeasure = remember { PathMeasure() }
    val fullPath = remember { Path() }
    val partialPath = remember { Path() }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        fullPath.reset()
        fullPath.moveTo(w * 0.20f, h * 0.54f)
        fullPath.lineTo(w * 0.42f, h * 0.75f)
        fullPath.lineTo(w * 0.82f, h * 0.28f)

        pathMeasure.setPath(fullPath, false)
        partialPath.reset()
        pathMeasure.getSegment(
            startDistance = 0f,
            stopDistance = pathMeasure.length * progress.coerceIn(0f, 1f),
            destination = partialPath,
            startWithMoveTo = true
        )

        drawPath(
            path = partialPath,
            color = color,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
