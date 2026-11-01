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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.ui.addpolicy.FormSectionCard
import com.policydesk.app.ui.addpolicy.SectorOptionPill
import com.policydesk.app.ui.addpolicy.formFieldColors
import com.policydesk.app.ui.components.HsrpNumberPlate
import com.policydesk.app.ui.components.RegistrationPlateVisualTransformation
import androidx.compose.foundation.isSystemInDarkTheme
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
                title = "PDF Policy Review",
                subtitle = if (state.queue.size > 1) "Document ${state.queueIndex + 1} of ${state.queue.size}" else null,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (state.queue.size > 1 && state.pdfUri != null) {
                        androidx.compose.material3.TextButton(onClick = viewModel::skipCurrentPdf) {
                            Text(
                                text = if (state.queueIndex + 1 < state.queue.size) "Skip" else "Finish",
                                color = com.policydesk.app.ui.theme.AppleSystemBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                            text = "Select insurer PDF documents to automatically extract vehicle schedule, customer info, and policy dates.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = labelSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        AppleButton(
                            text = "Select Policy PDF(s)",
                            onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                            icon = AppleIcons.Upload,
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
                                text = "Another PDF",
                                onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                                modifier = Modifier.weight(1f)
                            )
                            AppleSecondaryButton(
                                text = "AI Settings",
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

                // Section 1: Vehicle Details
                FormSectionCard(title = "Vehicle Information") {
                    OutlinedTextField(
                        value = state.regNo,
                        onValueChange = { viewModel.onFieldChanged("regNo", it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Registration No *") },
                        singleLine = true,
                        visualTransformation = RegistrationPlateVisualTransformation,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
                    )

                    if (state.regNo.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HsrpNumberPlate(
                                regNo = state.regNo
                            )
                            if (RegistrationNormalizer.isStandardFormat(state.regNo)) {
                                Text(
                                    text = "Standard Indian Plate",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    color = StatusActiveGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

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
                }

                // Section 3: Customer Details
                FormSectionCard(title = "Customer Details") {
                    OutlinedTextField(
                        value = state.customerName,
                        onValueChange = { viewModel.onFieldChanged("customerName", it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Customer Name *") },
                        singleLine = true,
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
