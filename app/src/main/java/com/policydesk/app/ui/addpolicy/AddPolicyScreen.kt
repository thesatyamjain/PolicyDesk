package com.policydesk.app.ui.addpolicy

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.domain.model.InsurerCatalog
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.domain.model.VehiclePlateClassifier
import com.policydesk.app.ui.components.AppleAlertDialog
import com.policydesk.app.ui.components.AppleButton
import com.policydesk.app.ui.components.AppleSecondaryButton
import com.policydesk.app.ui.components.BindBackToTop
import com.policydesk.app.ui.components.CupertinoTopBarAction
import com.policydesk.app.ui.components.ExistingClientRenewalBanner
import com.policydesk.app.ui.components.HsrpNumberPlate
import com.policydesk.app.ui.components.HsrpRegistrationTextField
import com.policydesk.app.ui.components.LiquidGlassActionDock
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.components.VehicleCategoryPicker
import com.policydesk.app.ui.detail.AccessibleStatusColors
import com.policydesk.app.ui.detail.OverlaidGlassTopBar
import com.policydesk.app.ui.detail.rememberAnimatorsEnabled
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleIcons
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkLabelTertiary
import com.policydesk.app.ui.theme.IosDarkSecondaryGrouped
import com.policydesk.app.ui.theme.IosDarkTertiaryGrouped
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.StatusExpiringAmber
import com.policydesk.app.ui.theme.StatusExpiringAmberBg
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPolicyScreen(
    editRegNo: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit,
    onNavigateToScan: (() -> Unit)? = null,
    viewModel: AddPolicyViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val suggestions by viewModel.makeModelSuggestions.collectAsStateWithLifecycle()
    val customInsurers by viewModel.customInsurers.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val animatorsEnabled = rememberAnimatorsEnabled()

    var showDatePicker by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Sequential focus chain across inputs (skips chips/dropdowns cleanly)
    val makeModelFocus = remember { FocusRequester() }
    val vinFocus = remember { FocusRequester() }
    val engineFocus = remember { FocusRequester() }
    val otherInsurerFocus = remember { FocusRequester() }
    val policyNoFocus = remember { FocusRequester() }
    val premiumFocus = remember { FocusRequester() }
    val commPctFocus = remember { FocusRequester() }
    val commAmtFocus = remember { FocusRequester() }
    val nameFocus = remember { FocusRequester() }
    val mobileFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    val addressFocus = remember { FocusRequester() }
    val notesFocus = remember { FocusRequester() }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onContactPicked(context, uri)
        }
    }

    LaunchedEffect(editRegNo) {
        if (!editRegNo.isNullOrBlank()) {
            viewModel.loadForEditing(editRegNo)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AddPolicyUiEvent.SaveSuccess -> {
                    val msg = if (state.isEditMode) "Record updated successfully" else "Policy saved successfully"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (!event.addAnother) {
                        onNavigateToVehicleDetail(event.regNo)
                    }
                }
                is AddPolicyUiEvent.OpenExisting -> {
                    onNavigateToVehicleDetail(event.regNo)
                }
                is AddPolicyUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.expiryDateEpochMs ?: System.currentTimeMillis()
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

    // Duplicate Registration / Policy Dialog
    if (state.showDuplicateDialog) {
        val formattedReg = RegistrationNormalizer.formatForDisplay(state.regNo)
        val duplicateTitle = if (state.isEditMode) "Registration Already Exists" else "Duplicate Policy Detected"
        val duplicateMessage = if (state.isEditMode) {
            "Another record with registration $formattedReg already exists in PolicyDesk. Open the existing record instead of overwriting it."
        } else {
            "Registration $formattedReg already has policy ${state.policyNumber.trim()} recorded. Open the existing vehicle record or enter a new renewal policy number."
        }
        AppleAlertDialog(
            onDismissRequest = viewModel::dismissDuplicateDialog,
            title = duplicateTitle,
            message = duplicateMessage,
            confirmText = "Open Existing",
            onConfirm = viewModel::onOpenExistingDuplicate,
            dismissText = "Cancel",
            onDismiss = viewModel::dismissDuplicateDialog
        )
    }

    // Sector Change Confirmation Dialog
    if (state.showSectorChangeConfirmDialog) {
        AppleAlertDialog(
            onDismissRequest = viewModel::dismissSectorChangeDialog,
            title = "Change Company Sector?",
            message = "Changing the sector will reset your currently selected company. Are you sure you want to proceed?",
            confirmText = "Change Sector",
            onConfirm = viewModel::confirmSectorChange,
            dismissText = "Keep Current",
            onDismiss = viewModel::dismissSectorChangeDialog
        )
    }

    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val labelTertiary = if (isDark) IosDarkLabelTertiary else IosLabelTertiary
    val accentBlue = AccessibleStatusColors.blue(isDark)
    val accentGreen = AccessibleStatusColors.green(isDark)
    val accentRed = AccessibleStatusColors.red(isDark)

    val localHaze = remember { HazeState() }
    val scrollState = rememberScrollState()
    BindBackToTop(scrollState)
    val isScrolled by remember { derivedStateOf { scrollState.value > 4 } }
    var topBarHeightPx by remember { mutableIntStateOf(0) }
    val topBarHeightDp = with(LocalDensity.current) { topBarHeightPx.toDp() }

    val missingFields = if (state.hasAttemptedSave && !state.isFormValid) {
        buildList {
            if (RegistrationNormalizer.normalize(state.regNo).isEmpty()) add("Registration No.")
            if (state.regNoDuplicate) add("Duplicate Record")
            if (state.sector == null) add("Sector")
            val effectiveInsurer = if (state.insurer == "Other") state.otherInsurerName.trim() else state.insurer.trim()
            if (effectiveInsurer.isEmpty()) add("Insurer")
            if (!FieldValidators.isValidPolicyNumber(state.policyNumber)) add("Policy No.")
            if (state.expiryDateEpochMs == null) add("Expiry Date")
            if ((state.totalPremium.toDoubleOrNull() ?: 0.0) <= 0.0) add("Premium")
            if (!FieldValidators.isValidName(state.name)) add("Customer Name")
            if (!FieldValidators.isValidMobile(state.mobile)) add("Mobile No.")
            if (state.email.isNotBlank() && !FieldValidators.isValidEmail(state.email)) add("Valid Email")
        }
    } else emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = localHaze)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(topBarHeightDp + 10.dp))

            // Quick Scan Banner: Auto-Fill with AI Scanner
            if (!state.isEditMode && onNavigateToScan != null) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 2.dp,
                    onClick = onNavigateToScan
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(accentBlue.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AppleIcons.Camera,
                                contentDescription = null,
                                tint = accentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Fill with AI Scanner",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = labelPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Snap policy schedule, pick photo, or upload PDF",
                                fontSize = 12.sp,
                                color = labelSecondary,
                                lineHeight = 16.sp
                            )
                        }
                        Icon(
                            imageVector = AppleIcons.ChevronRight,
                            contentDescription = null,
                            tint = labelTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // SECTION 1: VEHICLE SPECIFICATIONS
            FormSectionCard(title = "Vehicle Specifications") {
                val detectedStyle = VehiclePlateClassifier.detectStyle(state.makeModel)
                val effectiveStyle = state.selectedPlateStyle ?: detectedStyle
                val previewPlateShape = RoundedCornerShape(12.dp)

                // Live HSRP Plate Preview strip synchronized across all 4 RTO styles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(previewPlateShape)
                        .background(if (isDark) Color(0x28545458) else Color(0x10767680), previewPlateShape)
                        .border(
                            0.75.dp,
                            if (isDark) Color(0x24FFFFFF) else Color(0x16000000),
                            previewPlateShape
                        )
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                    ) {
                        Text(
                            text = "LIVE HSRP PREVIEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = labelSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = effectiveStyle.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HsrpNumberPlate(
                        regNo = state.regNo.ifBlank { "DL01AB1234" },
                        makeModel = state.makeModel,
                        style = effectiveStyle
                    )
                }

                val isRegEmptyError = state.hasAttemptedSave && RegistrationNormalizer.normalize(state.regNo).isEmpty()
                val regErrorText = when {
                    state.regNoDuplicate && state.isEditMode ->
                        "Registration already belongs to another vehicle"
                    state.regNoDuplicate ->
                        "Policy ${state.policyNumber.trim()} already exists on this vehicle"
                    isRegEmptyError ->
                        "Registration number is required"
                    else -> null
                }

                HsrpRegistrationTextField(
                    value = state.regNo,
                    onValueChange = viewModel::onRegNoChanged,
                    plateStyle = effectiveStyle,
                    isError = state.regNoDuplicate || isRegEmptyError,
                    errorMessage = regErrorText,
                    onFocusLost = viewModel::onRegNoBlur,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { makeModelFocus.requestFocus() }
                    )
                )

                // Interactive Vehicle Category Selector (Auto, Private, Commercial, Electric EV, Comm EV)
                VehicleCategoryPicker(
                    selectedStyle = state.selectedPlateStyle,
                    detectedStyle = detectedStyle,
                    onStyleSelected = viewModel::onPlateStyleSelected,
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )

                val existingClient = state.existingClientInfo
                if (state.isRenewalDetected && existingClient != null && !state.isEditMode) {
                    ExistingClientRenewalBanner(existingInfo = existingClient)
                    AppleSecondaryButton(
                        text = "Open Existing Record (${RegistrationNormalizer.formatForDisplay(state.regNo)})",
                        icon = AppleIcons.ArrowUpRightSquare,
                        tintColor = accentBlue,
                        height = 40.dp,
                        onClick = viewModel::onOpenExistingDuplicate,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Registration Warnings
                if (state.regNoUnusualWarning) {
                    WarningBanner(
                        text = "Unusual registration format. You can still save if correct."
                    )
                }

                // Make & Model (with autocomplete suggestions)
                MakeModelAutocomplete(
                    value = state.makeModel,
                    onValueChange = viewModel::onMakeModelChanged,
                    suggestions = suggestions,
                    modifier = Modifier.focusRequester(makeModelFocus),
                    onNext = { vinFocus.requestFocus() }
                )

                // VIN / Chassis Number
                OutlinedTextField(
                    value = state.vin,
                    onValueChange = viewModel::onVinChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(vinFocus),
                    label = { Text("Chassis No (VIN)") },
                    placeholder = { Text("17 characters (e.g. MA3FBA41SE0123456)") },
                    singleLine = true,
                    textStyle = TextStyle(fontFeatureSettings = "tnum"),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { engineFocus.requestFocus() }
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )

                // Engine Number
                OutlinedTextField(
                    value = state.engineNo,
                    onValueChange = viewModel::onEngineNoChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(engineFocus),
                    label = { Text("Engine Number") },
                    placeholder = { Text("e.g. K12MN1234567") },
                    singleLine = true,
                    textStyle = TextStyle(fontFeatureSettings = "tnum"),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            if (state.insurer == "Other") {
                                otherInsurerFocus.requestFocus()
                            } else {
                                policyNoFocus.requestFocus()
                            }
                        }
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )

                if (state.vinWarning) {
                    WarningBanner(text = "Chassis number is typically 17 characters.")
                }
            }

            // SECTION 2: POLICY INFORMATION
            FormSectionCard(title = "Policy Information") {
                Text(
                    text = "Company Sector *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = labelPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SectorOptionPill(
                        label = "PSU Company",
                        selected = state.sector == Sector.PSU,
                        onClick = { viewModel.onSectorSelected(Sector.PSU) },
                        modifier = Modifier.weight(1f)
                    )
                    SectorOptionPill(
                        label = "Private Company",
                        selected = state.sector == Sector.PRIVATE,
                        onClick = { viewModel.onSectorSelected(Sector.PRIVATE) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Insurer Dropdown
                InsurerDropdown(
                    sector = state.sector,
                    selectedInsurer = state.insurer,
                    customInsurers = customInsurers,
                    onInsurerSelected = viewModel::onInsurerSelected
                )

                // If "Other" selected, show free text input
                if (state.insurer == "Other") {
                    OutlinedTextField(
                        value = state.otherInsurerName,
                        onValueChange = viewModel::onOtherInsurerNameChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(otherInsurerFocus),
                        label = { Text("Enter Company Name *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { policyNoFocus.requestFocus() }
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )
                }

                // Policy Number
                val invalidPolicyNo = state.hasAttemptedSave && !FieldValidators.isValidPolicyNumber(state.policyNumber)
                val isDuplicatePolicyNo = !state.isEditMode && state.regNoDuplicate
                OutlinedTextField(
                    value = state.policyNumber,
                    onValueChange = viewModel::onPolicyNumberChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(policyNoFocus),
                    label = { Text("Policy Number *") },
                    placeholder = { Text("e.g. 1234/56789012/00/000") },
                    singleLine = true,
                    textStyle = TextStyle(fontFeatureSettings = "tnum"),
                    isError = invalidPolicyNo || isDuplicatePolicyNo,
                    supportingText = when {
                        isDuplicatePolicyNo -> {
                            { Text("Already recorded on this vehicle — enter a renewal number", color = accentRed) }
                        }
                        invalidPolicyNo -> {
                            { Text("Valid policy number is required", color = accentRed) }
                        }
                        else -> null
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            if (state.expiryDateEpochMs == null) {
                                showDatePicker = true
                            } else {
                                premiumFocus.requestFocus()
                            }
                        }
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )

                // Expiry Date Field (tap anywhere to pick)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.expiryDateEpochMs?.let { UiFormatters.formatDate(it) } ?: "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        textStyle = TextStyle(fontFeatureSettings = "tnum"),
                        isError = state.hasAttemptedSave && state.expiryDateEpochMs == null,
                        supportingText = if (state.hasAttemptedSave && state.expiryDateEpochMs == null) {
                            { Text("Tap to select date", color = accentRed) }
                        } else null,
                        label = { Text("Policy Expiry Date *") },
                        placeholder = { Text("Tap to pick policy expiration date") },
                        trailingIcon = {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showDatePicker = true
                            }) {
                                Icon(
                                    imageVector = AppleIcons.Calendar,
                                    contentDescription = "Select Date",
                                    tint = accentBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(role = Role.Button) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showDatePicker = true
                            }
                            .semantics {
                                contentDescription = "Policy Expiry Date: ${state.expiryDateEpochMs?.let { UiFormatters.formatDate(it) } ?: "Not selected"}"
                            }
                    )
                }

                // Total Premium
                OutlinedTextField(
                    value = state.totalPremium,
                    onValueChange = viewModel::onTotalPremiumChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(premiumFocus),
                    label = { Text("Total Premium *") },
                    placeholder = { Text("e.g. 14500") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = accentBlue) },
                    singleLine = true,
                    textStyle = TextStyle(fontFeatureSettings = "tnum"),
                    isError = state.hasAttemptedSave && (state.totalPremium.toDoubleOrNull() ?: 0.0) <= 0.0,
                    supportingText = if (state.hasAttemptedSave && (state.totalPremium.toDoubleOrNull() ?: 0.0) <= 0.0) {
                        { Text("Amount > 0 required", color = accentRed) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { commPctFocus.requestFocus() }
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )

                // Advisor Commission (Commission % & Auto-calculated Amount)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = state.commissionPercentage,
                        onValueChange = viewModel::onCommissionPercentageChanged,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(commPctFocus),
                        label = { Text("Commission %") },
                        placeholder = { Text("e.g. 15") },
                        suffix = { Text("%", fontWeight = FontWeight.SemiBold, color = labelSecondary) },
                        singleLine = true,
                        textStyle = TextStyle(fontFeatureSettings = "tnum"),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { commAmtFocus.requestFocus() }
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )

                    OutlinedTextField(
                        value = state.commissionAmount,
                        onValueChange = viewModel::onCommissionAmountChanged,
                        modifier = Modifier
                            .weight(1.2f)
                            .focusRequester(commAmtFocus),
                        label = { Text("Advisor Comm.") },
                        placeholder = { Text("Auto-calc") },
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = accentGreen) },
                        singleLine = true,
                        textStyle = TextStyle(fontFeatureSettings = "tnum"),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { nameFocus.requestFocus() }
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )
                }

                val premVal = state.totalPremium.toDoubleOrNull()
                if (premVal != null && premVal > 0.0) {
                    val netVal = premVal / 1.18
                    Text(
                        text = "Calculated on Net ₹${String.format(java.util.Locale.US, "%,.0f", netVal)} (18% GST excluded)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = labelSecondary,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                        modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                    )
                }

                if (state.pastExpiryWarning) {
                    WarningBanner(
                        text = "This date is in the past. Saving will mark the policy as Overdue."
                    )
                }
            }

            // SECTION 3: PERSONAL DETAILS
            FormSectionCard(title = "Personal Details") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TitleDropdown(
                        selectedTitle = state.title,
                        onTitleSelected = viewModel::onTitleChanged,
                        modifier = Modifier.width(96.dp)
                    )

                    OutlinedTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChanged,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(nameFocus)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    viewModel.onNameBlur()
                                }
                            },
                        label = { Text("Customer Name *") },
                        placeholder = { Text("Full Name") },
                        trailingIcon = {
                            IconButton(onClick = { contactPickerLauncher.launch(null) }) {
                                Icon(
                                    imageVector = AppleIcons.Person,
                                    contentDescription = "Pick Contact",
                                    tint = accentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        singleLine = true,
                        isError = state.hasAttemptedSave && !FieldValidators.isValidName(state.name),
                        supportingText = if (state.hasAttemptedSave && !FieldValidators.isValidName(state.name)) {
                            { Text("Customer name required (min 2 chars)", color = accentRed) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { mobileFocus.requestFocus() }
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )
                }

                // Mobile Number
                OutlinedTextField(
                    value = state.mobile,
                    onValueChange = viewModel::onMobileChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(mobileFocus),
                    label = { Text("Mobile Number *") },
                    placeholder = { Text("10-digit mobile number") },
                    leadingIcon = {
                        Icon(
                            imageVector = AppleIcons.Phone,
                            contentDescription = null,
                            tint = accentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { contactPickerLauncher.launch(null) }) {
                            Icon(
                                imageVector = AppleIcons.Person,
                                contentDescription = "Pick Contact",
                                tint = accentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(fontFeatureSettings = "tnum"),
                    isError = state.hasAttemptedSave && !FieldValidators.isValidMobile(state.mobile),
                    supportingText = if (state.hasAttemptedSave && !FieldValidators.isValidMobile(state.mobile)) {
                        { Text("Valid 10-digit mobile required", color = accentRed) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { emailFocus.requestFocus() }
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )

                // Email Address
                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(emailFocus),
                    label = { Text("Email Address") },
                    placeholder = { Text("e.g. client@example.com (optional)") },
                    singleLine = true,
                    isError = state.hasAttemptedSave && state.email.isNotBlank() && !FieldValidators.isValidEmail(state.email),
                    supportingText = if (state.hasAttemptedSave && state.email.isNotBlank() && !FieldValidators.isValidEmail(state.email)) {
                        { Text("Invalid email format", color = accentRed) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { addressFocus.requestFocus() }
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )

                // Residential Address
                OutlinedTextField(
                    value = state.address,
                    onValueChange = viewModel::onAddressChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(addressFocus),
                    label = { Text("Residential Address (Optional)") },
                    placeholder = { Text("House/Flat no., street, city, pincode") },
                    minLines = 2,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { notesFocus.requestFocus() }
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )
            }

            // SECTION 4: ADDITIONAL NOTES
            FormSectionCard(title = "Additional Notes") {
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotesChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(notesFocus),
                    label = { Text("Client Preferences & Notes") },
                    placeholder = { Text("Preferred call time, language, add-ons...") },
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )
            }

            Spacer(
                modifier = Modifier
                    .height(104.dp)
                    .navigationBarsPadding()
            )
        }

        // Overlaid Refractive Glass Top Bar
        OverlaidGlassTopBar(
            title = if (state.isEditMode) "Edit Record" else "Add New Policy",
            subtitle = if (state.isEditMode) "Modify Vehicle, Policy & Customer Details" else "Live HSRP Plate Preview",
            isScrolled = isScrolled,
            hazeState = localHaze,
            onNavigateBack = onNavigateBack,
            animatorsEnabled = animatorsEnabled,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { topBarHeightPx = it.height }
        )

        FormFloatingDock(
            isEditMode = state.isEditMode,
            isSaving = state.isSaving,
            hasAttemptedSave = state.hasAttemptedSave,
            isFormValid = state.isFormValid,
            missingFields = missingFields,
            hazeState = localHaze,
            onSave = { viewModel.savePolicy(addAnother = false) },
            onSaveAndAddAnother = { viewModel.savePolicy(addAnother = true) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FormFloatingDock(
    isEditMode: Boolean,
    isSaving: Boolean,
    hasAttemptedSave: Boolean,
    isFormValid: Boolean,
    missingFields: List<String>,
    hazeState: HazeState,
    onSave: () -> Unit,
    onSaveAndAddAnother: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val accentRed = AccessibleStatusColors.red(isDark)
    val showMissingBanner = hasAttemptedSave && !isFormValid && missingFields.isNotEmpty()

    LiquidGlassActionDock(
        modifier = modifier,
        hazeState = hazeState,
        imeAware = true,
        topBanner = if (showMissingBanner) {
            {
                val bannerShape = RoundedCornerShape(14.dp)
                val bannerBg = if (isDark) Color(0xE63B1214) else Color(0xF5FFF1F0)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(bannerShape)
                        .background(bannerBg, bannerShape)
                        .border(
                            width = 1.dp,
                            color = accentRed.copy(alpha = 0.45f),
                            shape = bannerShape
                        )
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = AppleIcons.Warning,
                        contentDescription = null,
                        tint = accentRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Required: ${missingFields.joinToString(", ")}",
                        fontSize = 12.sp,
                        color = accentRed,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else null
    ) {
        if (isEditMode) {
            AppleButton(
                text = if (isSaving) "Saving..." else "Save Changes",
                onClick = onSave,
                enabled = !isSaving,
                isLoading = isSaving,
                height = 48.dp,
                shape = RoundedCornerShape(24.dp),
                horizontalPadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppleSecondaryButton(
                    text = "Save & Add",
                    onClick = onSaveAndAddAnother,
                    enabled = !isSaving,
                    height = 48.dp,
                    shape = RoundedCornerShape(24.dp),
                    horizontalPadding = 12.dp,
                    fontSize = 13.5.sp,
                    modifier = Modifier.weight(1f)
                )
                AppleButton(
                    text = if (isSaving) "Saving..." else "Save Policy",
                    onClick = onSave,
                    enabled = !isSaving,
                    isLoading = isSaving,
                    height = 48.dp,
                    shape = RoundedCornerShape(24.dp),
                    horizontalPadding = 12.dp,
                    fontSize = 14.5.sp,
                    modifier = Modifier.weight(1.25f)
                )
            }
        }
    }
}

@Composable
fun FormSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val titleColor = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = 2.dp
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
            letterSpacing = (-0.2).sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SectorOptionPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val accentBlue = AccessibleStatusColors.blue(isDark)
    val selectedBg = accentBlue.copy(alpha = if (isDark) 0.18f else 0.11f)
    val unselectedBg = if (isDark) Color(0xFF2C2C2E) else Color.White
    val selectedBorder = accentBlue
    val unselectedBorder = if (isDark) Color(0x38FFFFFF) else Color(0x26000000)
    val unselectedText = if (isDark) IosDarkLabelPrimary else IosLabelPrimary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) selectedBorder else unselectedBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .background(if (selected) selectedBg else unselectedBg)
            .clickable(role = Role.RadioButton) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(vertical = 12.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected) {
                Icon(
                    imageVector = AppleIcons.Checkmark,
                    contentDescription = null,
                    tint = accentBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) accentBlue else unselectedText
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsurerDropdown(
    sector: Sector?,
    selectedInsurer: String,
    customInsurers: List<String> = emptyList(),
    onInsurerSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val insurers = sector?.let { InsurerCatalog.getInsurersForSector(it, customInsurers) } ?: emptyList()
    val isEnabled = sector != null

    ExposedDropdownMenuBox(
        expanded = expanded && isEnabled,
        onExpandedChange = { if (isEnabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedInsurer,
            onValueChange = {},
            readOnly = true,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = isEnabled),
            label = {
                val label = when (sector) {
                    Sector.PSU -> "PSU Company *"
                    Sector.PRIVATE -> "Private Company *"
                    null -> "Company (Select Sector First) *"
                }
                Text(label)
            },
            placeholder = { Text(if (isEnabled) "Choose Company" else "Select sector above first") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(10.dp),
            colors = formFieldColors()
        )

        val isDark = isSystemInDarkTheme()
        val menuBg = if (isDark) IosDarkSecondaryGrouped else Color.White
        val itemTextColor = if (isDark) IosDarkLabelPrimary else IosLabelPrimary

        ExposedDropdownMenu(
            expanded = expanded && isEnabled,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(menuBg)
        ) {
            insurers.forEach { company ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = company,
                            fontSize = 13.sp,
                            fontWeight = if (company == selectedInsurer) FontWeight.Bold else FontWeight.Normal,
                            color = itemTextColor
                        )
                    },
                    onClick = {
                        onInsurerSelected(company)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleDropdown(
    selectedTitle: String,
    onTitleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedTitle,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
            label = { Text("Title", maxLines = 1, softWrap = false) },
            placeholder = { Text("Shri") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(10.dp),
            colors = formFieldColors()
        )

        val isDark = isSystemInDarkTheme()
        val menuBg = if (isDark) IosDarkSecondaryGrouped else Color.White
        val itemTextColor = if (isDark) IosDarkLabelPrimary else IosLabelPrimary

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(menuBg)
        ) {
            InsurerCatalog.TITLES.forEach { title ->
                DropdownMenuItem(
                    text = { Text(title, fontSize = 13.sp, color = itemTextColor) },
                    onClick = {
                        onTitleSelected(title)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeModelAutocomplete(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = suggestions.filter {
        it.contains(value, ignoreCase = true) && !it.equals(value, ignoreCase = true)
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filtered.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            modifier = modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
            label = { Text("Vehicle Make & Model") },
            placeholder = { Text("e.g. Maruti Swift Dzire, Tata Nexon EV") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    expanded = false
                    onNext?.invoke()
                }
            ),
            shape = RoundedCornerShape(10.dp),
            colors = formFieldColors()
        )

        if (filtered.isNotEmpty()) {
            val isDark = isSystemInDarkTheme()
            val menuBg = if (isDark) IosDarkSecondaryGrouped else Color.White
            val itemTextColor = if (isDark) IosDarkLabelPrimary else IosLabelPrimary

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(menuBg)
            ) {
                filtered.take(5).forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item, fontSize = 13.sp, color = itemTextColor) },
                        onClick = {
                            onValueChange(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WarningBanner(text: String) {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) Color(0x33CA8A04) else StatusExpiringAmberBg
    val border = if (isDark) Color(0x66CA8A04) else StatusExpiringAmber.copy(alpha = 0.4f)
    val textColor = if (isDark) Color(0xFFFEF08A) else IosLabelPrimary
    val iconTint = AccessibleStatusColors.orange(isDark)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(8.dp))
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = AppleIcons.Warning,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = textColor,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun formFieldColors(): TextFieldColors {
    val isDark = isSystemInDarkTheme()
    val container = if (isDark) IosDarkTertiaryGrouped else Color.White
    val borderUnfocused = if (isDark) Color(0x38FFFFFF) else Color(0x2E000000)
    val textPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val textSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary
    val textTertiary = if (isDark) IosDarkLabelTertiary else IosLabelTertiary
    val accent = if (isDark) AppleDarkSystemBlue else AppleSystemBlue

    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = container,
        unfocusedContainerColor = container,
        disabledContainerColor = if (isDark) Color(0xFF1E1E20) else Color(0xFFF2F2F7),
        focusedTextColor = textPrimary,
        unfocusedTextColor = textPrimary,
        disabledTextColor = textSecondary,
        focusedBorderColor = accent,
        unfocusedBorderColor = borderUnfocused,
        disabledBorderColor = if (isDark) Color(0x1FFFFFFF) else Color(0x18000000),
        focusedLabelColor = accent,
        unfocusedLabelColor = textSecondary,
        disabledLabelColor = textTertiary,
        focusedPlaceholderColor = textTertiary,
        unfocusedPlaceholderColor = textTertiary,
        disabledPlaceholderColor = textTertiary.copy(alpha = 0.6f)
    )
}

@Composable
fun formDisabledFieldColors(): TextFieldColors = formFieldColors()
