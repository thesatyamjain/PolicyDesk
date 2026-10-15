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
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.ui.addpolicy.FormSectionCard
import com.policydesk.app.ui.addpolicy.SectorOptionPill
import com.policydesk.app.ui.addpolicy.formFieldColors
import com.policydesk.app.ui.theme.CardBackground
import com.policydesk.app.ui.theme.Navy900
import com.policydesk.app.ui.theme.SapphireBlue
import com.policydesk.app.ui.theme.Slate200
import com.policydesk.app.ui.theme.Slate50
import com.policydesk.app.ui.theme.Slate600
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
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (state.pdfUri == null) {
            // Pick PDF Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(StatusOverdueRedBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppleIcons.DocText,
                        contentDescription = null,
                        tint = StatusOverdueRed,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Import Policy PDF",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pick one or multiple insurer PDFs from storage or share directly to PolicyDesk from WhatsApp or Gmail.",
                    fontSize = 13.sp,
                    color = Slate600,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                AppleButton(
                    text = "Select Policy PDF(s)",
                    onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                    icon = AppleIcons.Upload,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else if (state.isExtracting) {
            // Extracting in progress
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = SapphireBlue, strokeWidth = 3.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Reading policy document...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Navy900
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Extracting customer, vehicle & expiry dates with your AI key",
                    fontSize = 12.sp,
                    color = Slate600
                )
            }
        } else if (state.extractionError != null) {
            // Error Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = AppleIcons.Warning,
                    contentDescription = null,
                    tint = StatusOverdueRed,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Extraction Issue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.extractionError ?: "Unknown error",
                    fontSize = 13.sp,
                    color = Slate600,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                AppleButton(
                    text = "Try Another PDF",
                    onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                AppleSecondaryButton(
                    text = "Open AI Settings",
                    onClick = onNavigateToSettings,
                    modifier = Modifier.fillMaxWidth()
                )
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
                        .background(StatusActiveGreenBg, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppleIcons.Checkmark,
                            contentDescription = null,
                            tint = StatusActiveGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Values extracted from PDF. Review carefully before saving.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusActiveGreen
                        )
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
                        shape = RoundedCornerShape(10.dp),
                        colors = formFieldColors()
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
