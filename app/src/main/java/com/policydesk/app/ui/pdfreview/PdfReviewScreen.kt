package com.policydesk.app.ui.pdfreview

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.policydesk.app.ui.components.AppleAlertDialog
import com.policydesk.app.ui.components.AppleButton
import com.policydesk.app.ui.components.AppleSecondaryButton
import com.policydesk.app.ui.components.CupertinoTopBarAction
import com.policydesk.app.ui.components.LiquidGlassCanvas
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.theme.AppleIcons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.data.importer.SpreadsheetParser
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.ui.addpolicy.FormSectionCard
import com.policydesk.app.ui.addpolicy.SectorOptionPill
import com.policydesk.app.ui.addpolicy.formFieldColors
import com.policydesk.app.ui.components.ExistingClientRenewalBanner
import com.policydesk.app.ui.components.HsrpNumberPlate
import com.policydesk.app.ui.components.HsrpRegistrationTextField
import com.policydesk.app.ui.components.RegistrationPlateVisualTransformation
import com.policydesk.app.ui.components.UiFormatters
import com.policydesk.app.ui.components.VehicleCategoryPicker
import androidx.compose.foundation.isSystemInDarkTheme
import com.policydesk.app.ui.theme.AppleDarkSystemGreen
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.StatusActiveGreen
import com.policydesk.app.ui.theme.StatusActiveGreenBg
import com.policydesk.app.ui.theme.StatusExpiringAmber
import com.policydesk.app.ui.theme.StatusExpiringAmberBg
import com.policydesk.app.ui.theme.StatusOverdueRed
import com.policydesk.app.ui.theme.StatusOverdueRedBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReviewScreen(
    initialUri: Uri? = null,
    onNavigateBack: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: PdfReviewViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(initialUri) {
        if (initialUri != null && state.pdfUri == null) {
            viewModel.onPdfSelected(initialUri)
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.onPdfsSelected(uris)
        }
    }

    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraPhotoUri != null) {
            viewModel.onPdfsSelected(listOf(cameraPhotoUri!!))
        }
    }

    fun launchCamera() {
        try {
            val photosDir = File(context.cacheDir, "photos").apply { mkdirs() }
            val photoFile = File(photosDir, "policy_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraPhotoUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PdfReviewUiEvent.SaveSuccess -> {
                    Toast.makeText(context, "Policy extracted and saved!", Toast.LENGTH_SHORT).show()
                    if (event.isBatchFinished) {
                        onNavigateToVehicleDetail(event.regNo)
                    }
                }
                is PdfReviewUiEvent.BatchCompleted -> {
                    Toast.makeText(context, "Batch review completed!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is PdfReviewUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (state.isPasswordProtected) {
        AppleAlertDialog(
            onDismissRequest = viewModel::dismissPasswordDialog,
            title = "Password-Protected PDF",
            message = "This policy document is encrypted with a password. Enter it to decrypt and extract details.",
            confirmText = "Unlock & Extract",
            onConfirm = viewModel::submitPassword,
            dismissText = "Cancel",
            onDismiss = viewModel::dismissPasswordDialog,
            content = {
                OutlinedTextField(
                    value = state.passwordInput,
                    onValueChange = viewModel::onPasswordChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Document Password") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = formFieldColors()
                )
            }
        )
    }

    LiquidGlassCanvas(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            com.policydesk.app.ui.components.LiquidGlassTopBar(
                title = "AI Policy Scanner",
                subtitle = if (state.queue.size > 1) "Document ${state.queueIndex + 1} of ${state.queue.size}" else "Camera • PDF • Photo",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = ::launchCamera) {
                        Icon(
                            imageVector = AppleIcons.Camera,
                            contentDescription = "Take Photo",
                            tint = com.policydesk.app.ui.theme.AppleSystemBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf", "image/*")) }) {
                        Icon(
                            imageVector = AppleIcons.Upload,
                            contentDescription = "Upload Document or Photo",
                            tint = com.policydesk.app.ui.theme.AppleSystemBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (state.queue.size > 1 && state.pdfUri != null) {
                        CupertinoTopBarAction(
                            text = if (state.queueIndex + 1 < state.queue.size) "Skip" else "Finish",
                            onClick = viewModel::skipCurrentPdf,
                            tint = com.policydesk.app.ui.theme.AppleSystemBlue
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        val isDark = isSystemInDarkTheme()
        val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
        val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

        if (state.pdfUri == null) {
            // Pick PDF Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    if (isDark) Color(0xFF0A84FF).copy(alpha = 0.16f) else Color(0xFF007AFF).copy(alpha = 0.12f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AppleIcons.DocText,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Import Policy Document",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Select insurer PDF documents, photos, or WhatsApp screenshots to automatically extract vehicle schedule, customer info, and policy dates.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = labelSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        AppleButton(
                            text = "Choose PDF or Image",
                            onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf", "image/*")) },
                            icon = AppleIcons.Upload,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        AppleSecondaryButton(
                            text = "Take Photo with Camera",
                            onClick = ::launchCamera,
                            icon = AppleIcons.Camera,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        AppleSecondaryButton(
                            text = "Fill Policy Manually",
                            onClick = viewModel::switchToManualEntry,
                            icon = AppleIcons.SquareAndPencil,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else if (state.isExtracting) {
            // Extracting in progress
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = if (isDark) com.policydesk.app.ui.theme.AppleDarkSystemBlue else com.policydesk.app.ui.theme.AppleSystemBlue,
                                strokeWidth = 3.5.dp,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Analyzing Document",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Reading policy schedule, vehicle registration, and coverage dates...",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = labelSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else if (state.extractionError != null) {
            // Error Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(StatusOverdueRedBg, CircleShape)
                                .border(1.dp, StatusOverdueRed.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AppleIcons.Warning,
                                contentDescription = "Extraction Issue",
                                tint = StatusOverdueRed,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Extraction Issue",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = state.extractionError ?: "Unable to read policy information.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = labelSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Tip: If the PDF is a scanned photocopy or non-standard format, you can continue by entering details manually.",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = labelSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        AppleButton(
                            text = "Retry Extraction",
                            onClick = viewModel::retryCurrentPdf,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        AppleSecondaryButton(
                            text = "Continue Manually",
                            onClick = viewModel::switchToManualEntry,
                            icon = AppleIcons.SquareAndPencil,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppleSecondaryButton(
                                text = "Camera",
                                onClick = ::launchCamera,
                                icon = AppleIcons.Camera,
                                modifier = Modifier.weight(1f)
                            )
                            AppleSecondaryButton(
                                text = "Pick File",
                                onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf", "image/*")) },
                                icon = AppleIcons.Upload,
                                modifier = Modifier.weight(1f)
                            )
                            AppleSecondaryButton(
                                text = "Settings",
                                onClick = onNavigateToSettings,
                                icon = AppleIcons.GearShape,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        } else {
            // Review & Confirm Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StatusActiveGreenBg, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(StatusActiveGreen.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AppleIcons.Checkmark,
                                contentDescription = null,
                                tint = StatusActiveGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Policy Details Extracted",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusActiveGreen
                            )
                            Text(
                                text = "Review and verify extracted fields before saving.",
                                fontSize = 11.sp,
                                color = StatusActiveGreen.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                val existingClient = state.existingClientInfo
                if (state.isRenewalDetected && existingClient != null) {
                    ExistingClientRenewalBanner(existingInfo = existingClient)
                }

                // Section 1: Vehicle Details
                FormSectionCard(title = "Vehicle Information") {
                    // Unified HSRP Registration Number Plate Input Field
                    val detectedStyle = com.policydesk.app.domain.model.VehiclePlateClassifier.detectStyle(state.makeModel)
                    val effectiveStyle = state.selectedPlateStyle ?: detectedStyle

                    HsrpRegistrationTextField(
                        value = state.regNo,
                        onValueChange = { viewModel.onFieldChanged("regNo", it) },
                        plateStyle = effectiveStyle,
                        label = "Registration No *",
                        isError = state.regNo.isBlank() && state.isSaving,
                        errorMessage = "Registration number is required"
                    )

                    // Interactive Vehicle Category Selector (Auto, Private, Commercial, EV, Comm EV)
                    VehicleCategoryPicker(
                        selectedStyle = state.selectedPlateStyle,
                        detectedStyle = detectedStyle,
                        onStyleSelected = viewModel::onPlateStyleSelected,
                        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = state.makeModel,
                        onValueChange = { viewModel.onFieldChanged("makeModel", it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Make & Model") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = state.vin,
                            onValueChange = { viewModel.onFieldChanged("vin", it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Chassis No (VIN)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = formFieldColors()
                        )
                        OutlinedTextField(
                            value = state.engineNo,
                            onValueChange = { viewModel.onFieldChanged("engineNo", it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Engine Number") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = formFieldColors()
                        )
                    }
                }

                // Section 2: Policy Details
                FormSectionCard(title = "Policy Details") {
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

                    OutlinedTextField(
                        value = state.insurer,
                        onValueChange = { viewModel.onFieldChanged("insurer", it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Insurer Name *") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )

                    OutlinedTextField(
                        value = state.policyNumber,
                        onValueChange = { viewModel.onFieldChanged("policyNumber", it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Policy Number *") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = state.expiryDate,
                            onValueChange = { viewModel.onFieldChanged("expiryDate", it) },
                            modifier = Modifier.weight(1.2f),
                            label = { Text("Expiry (DD/MM/YYYY) *") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = formFieldColors()
                        )

                        OutlinedTextField(
                            value = state.totalPremium,
                            onValueChange = { viewModel.onFieldChanged("totalPremium", it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Total Premium *") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = formFieldColors()
                        )
                    }

                    // Advisor Commission (Commission % & Auto-calculated Amount)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = state.commissionPercentage,
                            onValueChange = { viewModel.onFieldChanged("commissionPercentage", it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Commission %") },
                            placeholder = { Text("e.g. 15") },
                            suffix = { Text("%", fontWeight = FontWeight.SemiBold, color = if (isDark) IosDarkLabelSecondary else IosLabelSecondary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = formFieldColors()
                        )

                        OutlinedTextField(
                            value = state.commissionAmount,
                            onValueChange = { viewModel.onFieldChanged("commissionAmount", it) },
                            modifier = Modifier.weight(1.2f),
                            label = { Text("Advisor Comm.") },
                            placeholder = { Text("Auto-calc") },
                            prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = if (isDark) AppleDarkSystemGreen else AppleSystemGreen) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                            color = if (isDark) IosDarkLabelSecondary else IosLabelSecondary,
                            modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                        )
                    }
                }

                // Section 3: Customer Details
                FormSectionCard(title = "Customer Details") {
                    OutlinedTextField(
                        value = state.customerName,
                        onValueChange = { viewModel.onFieldChanged("customerName", it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    viewModel.onFieldBlur("customerName")
                                }
                            },
                        label = { Text("Customer Name *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )

                    OutlinedTextField(
                        value = state.mobile,
                        onValueChange = { viewModel.onFieldChanged("mobile", it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Mobile Number *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )

                    OutlinedTextField(
                        value = state.address,
                        onValueChange = { viewModel.onFieldChanged("address", it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Address *") },
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )
                }

                if (!state.isValid) {
                    val missingList = mutableListOf<String>()
                    if (RegistrationNormalizer.normalize(state.regNo).isBlank()) missingList.add("Registration No")
                    if (!FieldValidators.isValidName(state.customerName)) missingList.add("Customer Name")
                    if (!FieldValidators.isValidMobile(state.mobile)) missingList.add("10-Digit Mobile")
                    if (state.sector == null || state.insurer.isBlank()) missingList.add("Insurer")
                    if (!FieldValidators.isValidPolicyNumber(state.policyNumber)) missingList.add("Policy Number")
                    if (SpreadsheetParser.parseDateToEpochMs(state.expiryDate) == null) missingList.add("Expiry Date")
                    if (state.totalPremium.toDoubleOrNull()?.let { it > 0 } != true) missingList.add("Premium Amount")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0x33CA8A04) else Color(0x1FFF9500))
                            .border(1.dp, if (isDark) Color(0x66CA8A04) else Color(0x33FF9500), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppleIcons.Warning,
                                contentDescription = null,
                                tint = StatusExpiringAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Required to save: " + missingList.joinToString(", "),
                                fontSize = 12.sp,
                                color = if (isDark) Color(0xFFFEF08A) else labelPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                AppleButton(
                    text = when {
                        state.isSaving -> "Saving..."
                        state.queue.size > 1 && state.queueIndex + 1 < state.queue.size ->
                            "Confirm & Next Document (${state.queueIndex + 1}/${state.queue.size})"
                        else -> "Confirm & Save Policy"
                    },
                    onClick = viewModel::savePolicy,
                    enabled = state.isValid && !state.isSaving,
                    isLoading = state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
}
