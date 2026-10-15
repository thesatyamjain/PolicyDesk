package com.policydesk.app.ui.addpolicy

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import com.policydesk.app.ui.components.AppleAlertDialog
import com.policydesk.app.ui.components.AppleButton
import com.policydesk.app.ui.components.AppleSecondaryButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.domain.model.InsurerCatalog
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.theme.CardBackground
import com.policydesk.app.ui.theme.Navy700
import com.policydesk.app.ui.theme.Navy800
import com.policydesk.app.ui.theme.Navy900
import com.policydesk.app.ui.theme.SapphireBlue
import com.policydesk.app.ui.theme.Slate100
import com.policydesk.app.ui.theme.Slate200
import com.policydesk.app.ui.theme.Slate400
import com.policydesk.app.ui.theme.Slate50
import com.policydesk.app.ui.theme.Slate600
import com.policydesk.app.ui.theme.StatusExpiringAmber
import com.policydesk.app.ui.theme.StatusExpiringAmberBg
import com.policydesk.app.ui.theme.StatusOverdueRed
import com.policydesk.app.ui.theme.StatusOverdueRedBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPolicyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit,
    viewModel: AddPolicyViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val suggestions by viewModel.makeModelSuggestions.collectAsStateWithLifecycle()
    val customInsurers by viewModel.customInsurers.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AddPolicyUiEvent.SaveSuccess -> {
                    Toast.makeText(context, "Policy saved successfully", Toast.LENGTH_SHORT).show()
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
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onExpiryDateSelected(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Duplicate Registration Dialog
    if (state.showDuplicateDialog) {
        AppleAlertDialog(
            onDismissRequest = viewModel::dismissDuplicateDialog,
            title = "Vehicle Already Exists",
            message = "A record with registration ${RegistrationNormalizer.formatForDisplay(state.regNo)} already exists in PolicyDesk. You can view the existing record or update it with new policy details.",
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

    Scaffold(
        topBar = {
            com.policydesk.app.ui.components.LiquidGlassTopBar(
                title = "Add New Policy",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 1: VEHICLE SPECIFICATIONS
            FormSectionCard(title = "Vehicle Specifications") {
                // Registration Number
                OutlinedTextField(
                    value = state.regNo,
                    onValueChange = viewModel::onRegNoChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                viewModel.onRegNoBlur()
                            }
                        },
                    label = { Text("Vehicle Registration No *") },
                    placeholder = { Text("e.g. MH 12 AB 1234 or 22 BH 1234 AA") },
                    singleLine = true,
                    isError = state.hasAttemptedSave && RegistrationNormalizer.normalize(state.regNo).isEmpty(),
                    supportingText = if (state.hasAttemptedSave && RegistrationNormalizer.normalize(state.regNo).isEmpty()) {
                        { Text("Registration number is required", color = StatusOverdueRed) }
                    } else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )

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
                    suggestions = suggestions
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // VIN / Chassis Number
                    OutlinedTextField(
                        value = state.vin,
                        onValueChange = viewModel::onVinChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("Chassis No (VIN)") },
                        placeholder = { Text("17 characters") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )

                    // Engine Number
                    OutlinedTextField(
                        value = state.engineNo,
                        onValueChange = viewModel::onEngineNoChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("Engine Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )
                }

                if (state.vinWarning) {
                    WarningBanner(text = "Chassis number is typically 17 characters.")
                }
            }

            // SECTION 2: POLICY INFORMATION
            FormSectionCard(title = "Policy Information") {
                // Sector Selection
                val isDark = isSystemInDarkTheme()
                Text(
                    text = "Company Sector *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
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
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Enter Company Name *") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )
                }

                // Policy Number
                OutlinedTextField(
                    value = state.policyNumber,
                    onValueChange = viewModel::onPolicyNumberChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Policy Number *") },
                    singleLine = true,
                    isError = state.hasAttemptedSave && !FieldValidators.isValidPolicyNumber(state.policyNumber),
                    supportingText = if (state.hasAttemptedSave && !FieldValidators.isValidPolicyNumber(state.policyNumber)) {
                        { Text("Valid policy number is required", color = StatusOverdueRed) }
                    } else null,
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )

                // Expiry Date & Total Premium Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Expiry Date Field (tap anywhere to pick)
                    Box(modifier = Modifier.weight(1.2f)) {
                        OutlinedTextField(
                            value = state.expiryDateEpochMs?.let { UiFormatters.formatDate(it) } ?: "",
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            isError = state.hasAttemptedSave && state.expiryDateEpochMs == null,
                            supportingText = if (state.hasAttemptedSave && state.expiryDateEpochMs == null) {
                                { Text("Tap to select date", color = StatusOverdueRed) }
                            } else null,
                            label = { Text("Expiry Date *") },
                            placeholder = { Text("Tap to select") },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        imageVector = AppleIcons.Calendar,
                                        contentDescription = "Select Date",
                                        tint = SapphireBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = formFieldColors()
                        )
                        // Clickable overlay capturing taps on the whole field
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showDatePicker = true }
                        )
                    }

                    // Total Premium
                    OutlinedTextField(
                        value = state.totalPremium,
                        onValueChange = viewModel::onTotalPremiumChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("Total Premium *") },
                        placeholder = { Text("₹ INR") },
                        singleLine = true,
                        isError = state.hasAttemptedSave && (state.totalPremium.toDoubleOrNull() ?: 0.0) <= 0.0,
                        supportingText = if (state.hasAttemptedSave && (state.totalPremium.toDoubleOrNull() ?: 0.0) <= 0.0) {
                            { Text("Amount > 0 required", color = StatusOverdueRed) }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
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
                    // Title Dropdown (Bharatiya titles)
                    TitleDropdown(
                        selectedTitle = state.title,
                        onTitleSelected = viewModel::onTitleChanged,
                        modifier = Modifier.width(112.dp)
                    )

                    // Customer Name
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("Customer Name *") },
                        singleLine = true,
                        isError = state.hasAttemptedSave && !FieldValidators.isValidName(state.name),
                        supportingText = if (state.hasAttemptedSave && !FieldValidators.isValidName(state.name)) {
                            { Text("Customer name required (min 2 chars)", color = StatusOverdueRed) }
                        } else null,
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Mobile Number
                    OutlinedTextField(
                        value = state.mobile,
                        onValueChange = viewModel::onMobileChanged,
                        modifier = Modifier.weight(1.2f),
                        label = { Text("Mobile Number *") },
                        placeholder = { Text("10 digits (6-9)") },
                        singleLine = true,
                        isError = state.hasAttemptedSave && !FieldValidators.isValidMobile(state.mobile),
                        supportingText = if (state.hasAttemptedSave && !FieldValidators.isValidMobile(state.mobile)) {
                            { Text("Valid 10-digit mobile required", color = StatusOverdueRed) }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )

                    // Email Address
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChanged,
                        modifier = Modifier.weight(1.3f),
                        label = { Text("Email Address") },
                        singleLine = true,
                        isError = state.hasAttemptedSave && state.email.isNotBlank() && !FieldValidators.isValidEmail(state.email),
                        supportingText = if (state.hasAttemptedSave && state.email.isNotBlank() && !FieldValidators.isValidEmail(state.email)) {
                            { Text("Invalid email format", color = StatusOverdueRed) }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )
                }

                // Residential Address
                OutlinedTextField(
                    value = state.address,
                    onValueChange = viewModel::onAddressChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Residential Address (Optional)") },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )
            }

            // SECTION 4: ADDITIONAL NOTES
            FormSectionCard(title = "Additional Notes") {
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotesChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Client Preferences & Notes") },
                    placeholder = { Text("Preferred call time, language, add-ons...") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            AppleButton(
                text = if (state.isSaving) "Saving..." else "Save Policy",
                onClick = { viewModel.savePolicy(addAnother = false) },
                enabled = !state.isSaving,
                isLoading = state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            AppleSecondaryButton(
                text = "Save and Add Another",
                onClick = { viewModel.savePolicy(addAnother = true) },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
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
    com.policydesk.app.ui.components.LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = 2.dp
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            letterSpacing = (-0.2).sp
        )
        Spacer(modifier = Modifier.height(10.dp))
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
    val selectedBg = if (isDark) AppleDarkSystemBlue.copy(alpha = 0.16f) else AppleSystemBlue.copy(alpha = 0.10f)
    val unselectedBg = if (isDark) Color(0xFF2C2C2E) else Color.White
    val selectedBorder = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
    val unselectedBorder = if (isDark) Color(0x38FFFFFF) else Color(0x26000000)
    val selectedText = if (isDark) AppleDarkSystemBlue else AppleSystemBlue
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
            .clickable {
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
                    tint = selectedText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) selectedText else unselectedText
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
                .menuAnchor(),
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
        val menuBg = if (isDark) IosDarkSecondaryGrouped else CardBackground
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
                .menuAnchor(),
            label = { Text("Title", maxLines = 1, softWrap = false) },
            placeholder = { Text("Shri") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(10.dp),
            colors = formFieldColors()
        )

        val isDark = isSystemInDarkTheme()
        val menuBg = if (isDark) IosDarkSecondaryGrouped else CardBackground
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
    suggestions: List<String>
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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Vehicle Make & Model") },
            placeholder = { Text("e.g. Maruti Swift Dzire, Hyundai Creta") },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = formFieldColors()
        )

        if (filtered.isNotEmpty()) {
            val isDark = isSystemInDarkTheme()
            val menuBg = if (isDark) IosDarkSecondaryGrouped else CardBackground
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
            tint = StatusExpiringAmber,
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
