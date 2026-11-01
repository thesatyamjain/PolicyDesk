package com.policydesk.app.ui.importer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.policydesk.app.ui.components.AppleButton
import com.policydesk.app.ui.components.AppleSecondaryButton
import com.policydesk.app.ui.theme.AppleIcons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.policydesk.app.data.importer.SpreadsheetParser
import com.policydesk.app.ui.addpolicy.formFieldColors
import androidx.compose.foundation.isSystemInDarkTheme
import com.policydesk.app.ui.components.LiquidGlassCard
import com.policydesk.app.ui.detail.DetailRow
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkLabelTertiary
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.SapphireBlue
import com.policydesk.app.ui.theme.StatusActiveGreen
import com.policydesk.app.ui.theme.StatusActiveGreenBg
import com.policydesk.app.ui.theme.StatusExpiringAmber
import com.policydesk.app.ui.theme.StatusExpiringAmberBg
import com.policydesk.app.ui.theme.StatusOverdueRed
import com.policydesk.app.ui.theme.StatusOverdueRedBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onNavigateBack: () -> Unit,
    onImportFinished: () -> Unit,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment ?: "spreadsheet.csv"
            viewModel.onFileSelected(context, uri, name)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            com.policydesk.app.ui.components.LiquidGlassTopBar(
                title = "Bulk Spreadsheet Import",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.step) {
                ImportStep.SELECT_FILE -> SelectFileStep(
                    onPickFile = {
                        filePickerLauncher.launch(
                            arrayOf(
                                "text/csv",
                                "text/comma-separated-values",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/vnd.ms-excel",
                                "*/*"
                            )
                        )
                    },
                    onDownloadTemplate = {
                        val csv = SpreadsheetParser.generateCsvTemplate()
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_SUBJECT, "PolicyDesk_Template.csv")
                            putExtra(Intent.EXTRA_TEXT, csv)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share or Save Template"))
                    },
                    errorMessage = state.errorMessage
                )

                ImportStep.MAP_COLUMNS -> MapColumnsStep(
                    fileName = state.fileName,
                    headers = state.headers,
                    columnMapping = state.columnMapping,
                    onRemap = viewModel::onColumnRemapped,
                    onProceed = viewModel::proceedToPreview
                )

                ImportStep.PREVIEW -> PreviewStep(
                    state = state,
                    onSetStrategy = viewModel::setDuplicateStrategy,
                    onExecuteImport = viewModel::executeImport
                )

                ImportStep.IMPORTING -> ImportingStep(progress = state.importProgress)

                ImportStep.SUMMARY -> SummaryStep(
                    summary = state.summary,
                    onUndo = viewModel::undoLastImport,
                    onDownloadFailedCsv = {
                        val csv = viewModel.exportFailedRowsCsv()
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_SUBJECT, "PolicyDesk_Failed_Rows.csv")
                            putExtra(Intent.EXTRA_TEXT, csv)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Save Failed Rows CSV"))
                    },
                    onDone = onImportFinished
                )
            }
        }
    }
}

@Composable
fun SelectFileStep(
    onPickFile: () -> Unit,
    onDownloadTemplate: () -> Unit,
    errorMessage: String?
) {
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(SapphireBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AppleIcons.Upload,
                contentDescription = null,
                tint = SapphireBlue,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Import Existing Spreadsheet",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = labelPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Migrate your records in a single session. Supports Excel (.xlsx) and CSV files up to 5,000 rows.",
            fontSize = 13.sp,
            color = labelSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        AppleButton(
            text = "Select .XLSX or .CSV File",
            onClick = onPickFile,
            icon = AppleIcons.Upload,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        AppleSecondaryButton(
            text = "Download Standard Template",
            onClick = onDownloadTemplate,
            icon = AppleIcons.Download,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = AppleIcons.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = errorMessage,
                        color = if (isDark) Color(0xFFFFB4AB) else MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MapColumnsStep(
    fileName: String,
    headers: List<String>,
    columnMapping: Map<Int, String>,
    onRemap: (Int, String) -> Unit,
    onProceed: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    val schemaFields = listOf(
        "" to "-- Ignore Column --",
        "regNo" to "Vehicle Registration No *",
        "name" to "Customer Name *",
        "title" to "Title (Shri/Smt/Kumari)",
        "mobile" to "Mobile Number *",
        "email" to "Email Address",
        "address" to "Residential Address *",
        "sector" to "Company Sector (PSU/Private) *",
        "insurer" to "Insurer Company *",
        "policyNumber" to "Policy Number *",
        "expiryDate" to "Policy Expiry Date *",
        "totalPremium" to "Total Premium Cost *",
        "tpPremium" to "TP Premium",
        "odPremium" to "OD Premium",
        "makeModel" to "Vehicle Make & Model",
        "make" to "Vehicle Make",
        "model" to "Vehicle Model",
        "vin" to "Chassis Number (VIN)",
        "engineNo" to "Engine Number",
        "notes" to "Notes & Preferences",
        "saleDate" to "Sale Date"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Review Column Mapping",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = labelPrimary
        )
        Text(
            text = "File: $fileName (${headers.size} columns detected)",
            fontSize = 12.sp,
            color = labelSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(headers.indices.toList()) { colIdx ->
                val headerName = headers[colIdx]
                val currentMapping = columnMapping[colIdx] ?: ""

                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Header: \"$headerName\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = labelPrimary
                            )
                        }

                        Column(modifier = Modifier.weight(1.3f)) {
                            ColumnMappingDropdown(
                                selectedKey = currentMapping,
                                options = schemaFields,
                                onSelect = { onRemap(colIdx, it) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AppleButton(
            text = "Preview Validation",
            onClick = onProceed,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnMappingDropdown(
    selectedKey: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayLabel = options.find { it.first == selectedKey }?.second ?: "-- Ignore --"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = displayLabel,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(8.dp),
            colors = formFieldColors()
        )

        val isDark = isSystemInDarkTheme()
        val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
        val menuBg = if (isDark) Color(0xFF1C1C1E) else Color.White

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(menuBg)
        ) {
            options.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label, fontSize = 12.sp, color = labelPrimary) },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PreviewStep(
    state: ImportUiState,
    onSetStrategy: (DuplicateStrategy) -> Unit,
    onExecuteImport: () -> Unit
) {
    val preview = state.previewResult ?: return
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Validation Preview", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = labelPrimary)
        Text("Total ${preview.totalRows} rows ready to process", fontSize = 12.sp, color = labelSecondary)

        Spacer(modifier = Modifier.height(12.dp))

        // Stat Pills Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatPill("Valid: ${preview.validRows.size}", StatusActiveGreen, StatusActiveGreenBg, Modifier.weight(1f))
            StatPill("Warnings: ${preview.warningRows.size}", StatusExpiringAmber, StatusExpiringAmberBg, Modifier.weight(1f))
            StatPill("Errors: ${preview.errorRows.size}", StatusOverdueRed, StatusOverdueRedBg, Modifier.weight(1f))
            StatPill(
                "Duplicate: ${preview.duplicateRows.size}",
                if (isDark) AppleDarkSystemBlue else SapphireBlue,
                if (isDark) Color(0x330A84FF) else Color(0x1F007AFF),
                Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Duplicate Handling Option
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            elevation = 2.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Duplicate Registration Handling", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = labelPrimary)
                DuplicateStrategy.values().forEach { strategy ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSetStrategy(strategy) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.duplicateStrategy == strategy,
                            onClick = { onSetStrategy(strategy) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(strategy.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = labelPrimary)
                            Text(strategy.description, fontSize = 11.sp, color = labelSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Error & Problem Rows List
        Text("Problem Details (${preview.errorRows.size + preview.warningRows.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = labelPrimary)
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(preview.errorRows) { row ->
                RowErrorCard(row = row, isError = true)
            }
            items(preview.warningRows) { row ->
                RowErrorCard(row = row, isError = false)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val countToImport = preview.validRows.size + preview.warningRows.size +
                if (state.duplicateStrategy != DuplicateStrategy.SKIP) preview.duplicateRows.size else 0
        AppleButton(
            text = "Import $countToImport Records",
            onClick = onExecuteImport,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StatPill(label: String, textColor: Color, bgColor: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RowErrorCard(row: com.policydesk.app.data.importer.ParsedRow, isError: Boolean) {
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isError) StatusOverdueRedBg else StatusExpiringAmberBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(if (isError) StatusOverdueRed.copy(alpha = 0.3f) else StatusExpiringAmber.copy(alpha = 0.3f)))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "Row ${row.rowNumber}: ${if (row.regNo.isNotBlank()) row.regNo else "Missing Reg No"} (${row.customerName})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isError) StatusOverdueRed else labelPrimary
            )
            val issues = if (isError) row.errors else row.warnings
            issues.forEach { issue ->
                Text(text = "• $issue", fontSize = 11.sp, color = labelSecondary)
            }
        }
    }
}

@Composable
fun ImportingStep(progress: Float) {
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary
    val labelSecondary = if (isDark) IosDarkLabelSecondary else IosLabelSecondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Importing Records...", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = labelPrimary)
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = SapphireBlue,
            trackColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("${(progress * 100).toInt()}% completed", fontSize = 13.sp, color = labelSecondary)
    }
}

@Composable
fun SummaryStep(
    summary: ImportSummaryData?,
    onUndo: () -> Unit,
    onDownloadFailedCsv: () -> Unit,
    onDone: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val labelPrimary = if (isDark) IosDarkLabelPrimary else IosLabelPrimary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(StatusActiveGreenBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AppleIcons.CheckCircle,
                contentDescription = null,
                tint = StatusActiveGreen,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Import Complete!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = labelPrimary)
        Spacer(modifier = Modifier.height(18.dp))

        if (summary != null) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = 2.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(label = "New Records Added", value = "${summary.addedCount}")
                    DetailRow(label = "Existing Records Updated", value = "${summary.updatedCount}")
                    if (summary.renewedCount > 0) {
                        DetailRow(label = "Renewals Processed", value = "${summary.renewedCount}")
                    }
                    DetailRow(label = "Duplicates Skipped", value = "${summary.skippedCount}")
                    DetailRow(label = "Failed Rows", value = "${summary.failedCount}")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (summary != null && summary.failedCount > 0) {
            AppleSecondaryButton(
                text = "Download Failed Rows (CSV)",
                onClick = onDownloadFailedCsv,
                icon = AppleIcons.Download,
                tintColor = StatusOverdueRed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (summary != null && summary.canUndo && (summary.addedCount > 0 || summary.updatedCount > 0 || summary.renewedCount > 0)) {
            AppleSecondaryButton(
                text = "Undo Last Import",
                onClick = onUndo,
                tintColor = StatusOverdueRed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        AppleButton(
            text = "Go to Dashboard",
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}
