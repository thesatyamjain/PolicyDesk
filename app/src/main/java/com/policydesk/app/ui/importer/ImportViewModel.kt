package com.policydesk.app.ui.importer

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.data.importer.ImportPreviewResult
import com.policydesk.app.data.importer.ParsedRow
import com.policydesk.app.data.importer.SpreadsheetParser
import com.policydesk.app.domain.model.Customer
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicySource
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.SaleType
import com.policydesk.app.domain.model.Vehicle
import com.policydesk.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class ImportStep {
    SELECT_FILE,
    MAP_COLUMNS,
    PREVIEW,
    IMPORTING,
    SUMMARY
}

enum class DuplicateStrategy(val title: String, val description: String) {
    UPDATE("Update existing record", "Overwrite existing vehicle and policy with imported data"),
    RENEWAL_IF_LATER("Renew if expiry is later", "Treat duplicate as renewal if imported expiry is after current policy"),
    SKIP("Skip duplicates", "Keep existing records untouched")
}

data class ImportSummaryData(
    val addedCount: Int = 0,
    val updatedCount: Int = 0,
    val renewedCount: Int = 0,
    val skippedCount: Int = 0,
    val failedCount: Int = 0,
    val failedRows: List<ParsedRow> = emptyList(),
    val importedRegNos: List<String> = emptyList(),
    val importedPolicyIds: List<String> = emptyList(),
    val canUndo: Boolean = true
)

data class ImportUiState(
    val step: ImportStep = ImportStep.SELECT_FILE,
    val fileName: String = "",
    val rawRows: List<List<String>> = emptyList(),
    val headers: List<String> = emptyList(),
    val columnMapping: Map<Int, String> = emptyMap(),
    val previewResult: ImportPreviewResult? = null,
    val duplicateStrategy: DuplicateStrategy = DuplicateStrategy.UPDATE,
    val importProgress: Float = 0f,
    val summary: ImportSummaryData? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val repository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    fun onFileSelected(context: Context, uri: Uri, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val table = SpreadsheetParser.parseSpreadsheet(context, uri)
                if (table.isEmpty()) {
                    _uiState.update { it.copy(errorMessage = "File is empty or could not be read.") }
                    return@launch
                }

                val headerRow = table.first()
                val dataRows = table.drop(1)

                val autoMapping = autoMapHeaders(headerRow)

                _uiState.update {
                    it.copy(
                        fileName = fileName,
                        headers = headerRow,
                        rawRows = dataRows,
                        columnMapping = autoMapping,
                        step = ImportStep.MAP_COLUMNS,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to parse file: ${e.message}") }
            }
        }
    }

    private fun autoMapHeaders(headers: List<String>): Map<Int, String> {
        val mapping = mutableMapOf<Int, String>()
        for ((index, rawHeader) in headers.withIndex()) {
            val h = rawHeader.lowercase().replace(" ", "").replace("_", "")
            when {
                h.contains("reg") || h.contains("vehicleno") -> mapping[index] = "regNo"
                h.contains("name") || h.contains("customer") || h.contains("client") -> mapping[index] = "name"
                h.contains("title") || h.contains("salutation") -> mapping[index] = "title"
                h.contains("mobile") || h.contains("phone") || h.contains("contact") -> mapping[index] = "mobile"
                h.contains("email") -> mapping[index] = "email"
                h.contains("address") -> mapping[index] = "address"
                h.contains("sector") -> mapping[index] = "sector"
                h.contains("insurer") || h.contains("company") -> mapping[index] = "insurer"
                h.contains("policy") || h.contains("policyno") -> mapping[index] = "policyNumber"
                h.contains("expiry") || h.contains("duedate") -> mapping[index] = "expiryDate"
                h.contains("premium") || h.contains("amount") || h.contains("cost") -> mapping[index] = "totalPremium"
                h.contains("make") || h.contains("model") -> mapping[index] = "makeModel"
                h.contains("vin") || h.contains("chassis") -> mapping[index] = "vin"
                h.contains("engine") -> mapping[index] = "engineNo"
                h.contains("note") || h.contains("remark") -> mapping[index] = "notes"
                h.contains("saledate") || h.contains("booked") -> mapping[index] = "saleDate"
            }
        }
        return mapping
    }

    fun onColumnRemapped(columnIndex: Int, targetFieldKey: String) {
        _uiState.update { state ->
            val updated = state.columnMapping.toMutableMap()
            if (targetFieldKey.isEmpty()) {
                updated.remove(columnIndex)
            } else {
                updated[columnIndex] = targetFieldKey
            }
            state.copy(columnMapping = updated)
        }
    }

    fun proceedToPreview() {
        val state = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            val allVehicles = repository.getAllVehicles().first()
            val existingRegs = allVehicles.map { it.vehicle.regNo }.toSet()

            val preview = SpreadsheetParser.validateAndMapRows(
                rawRows = state.rawRows,
                columnMapping = state.columnMapping,
                existingRegNumbers = existingRegs
            )

            _uiState.update {
                it.copy(
                    previewResult = preview,
                    step = ImportStep.PREVIEW
                )
            }
        }
    }

    fun setDuplicateStrategy(strategy: DuplicateStrategy) {
        _uiState.update { it.copy(duplicateStrategy = strategy) }
    }

    fun executeImport() {
        val state = _uiState.value
        val preview = state.previewResult ?: return

        _uiState.update { it.copy(step = ImportStep.IMPORTING, importProgress = 0f) }

        viewModelScope.launch(Dispatchers.IO) {
            var added = 0
            var updated = 0
            var renewed = 0
            var skipped = 0
            val failed = mutableListOf<ParsedRow>()
            failed.addAll(preview.errorRows)

            val addedRegNos = mutableListOf<String>()
            val addedPolicyIds = mutableListOf<String>()

            val nonDuplicates = mutableListOf<ParsedRow>().apply {
                addAll(preview.validRows)
                addAll(preview.warningRows)
            }

            val total = nonDuplicates.size + if (state.duplicateStrategy != DuplicateStrategy.SKIP) preview.duplicateRows.size else 0
            var processed = 0
            val now = System.currentTimeMillis()

            // 1. Process non-duplicate valid and warning rows
            for (row in nonDuplicates) {
                try {
                    val customer = Customer(
                        title = row.title,
                        name = row.customerName,
                        mobile = row.mobile,
                        email = row.email,
                        address = row.address
                    )

                    val vehicle = Vehicle(
                        regNo = row.regNo,
                        customer = customer,
                        makeModel = row.makeModel,
                        vin = row.vin,
                        engineNo = row.engineNo,
                        notes = row.notes,
                        updatedAt = now
                    )

                    val isSale = row.saleDateEpochMs != null
                    val policy = Policy(
                        vehicleRegNo = row.regNo,
                        companySector = row.sector ?: com.policydesk.app.domain.model.Sector.PRIVATE,
                        insurer = row.insurer,
                        policyNumber = row.policyNumber,
                        expiryDate = row.expiryDateEpochMs ?: now,
                        totalPremium = row.totalPremium ?: 0.0,
                        source = PolicySource.SPREADSHEET,
                        saleDate = row.saleDateEpochMs ?: now,
                        countsAsSale = isSale
                    )

                    repository.saveVehicleAndPolicy(vehicle, policy)
                    added++
                    addedRegNos.add(row.regNo)
                    addedPolicyIds.add(policy.id)
                } catch (e: Exception) {
                    failed.add(row)
                }
                processed++
                if (total > 0) {
                    _uiState.update { it.copy(importProgress = processed.toFloat() / total) }
                }
            }

            // 2. Process duplicate rows according to chosen strategy
            when (state.duplicateStrategy) {
                DuplicateStrategy.SKIP -> {
                    skipped += preview.duplicateRows.size
                }
                DuplicateStrategy.UPDATE -> {
                    for (row in preview.duplicateRows) {
                        try {
                            val customer = Customer(
                                title = row.title,
                                name = row.customerName,
                                mobile = row.mobile,
                                email = row.email,
                                address = row.address
                            )
                            val vehicle = Vehicle(
                                regNo = row.regNo,
                                customer = customer,
                                makeModel = row.makeModel,
                                vin = row.vin,
                                engineNo = row.engineNo,
                                notes = row.notes,
                                updatedAt = now
                            )
                            val policy = Policy(
                                vehicleRegNo = row.regNo,
                                companySector = row.sector ?: com.policydesk.app.domain.model.Sector.PRIVATE,
                                insurer = row.insurer,
                                policyNumber = row.policyNumber,
                                expiryDate = row.expiryDateEpochMs ?: now,
                                totalPremium = row.totalPremium ?: 0.0,
                                source = PolicySource.SPREADSHEET,
                                saleDate = row.saleDateEpochMs ?: now,
                                countsAsSale = row.saleDateEpochMs != null
                            )
                            repository.saveVehicleAndPolicy(vehicle, policy)
                            updated++
                            addedPolicyIds.add(policy.id)
                        } catch (e: Exception) {
                            failed.add(row)
                        }
                        processed++
                        if (total > 0) {
                            _uiState.update { it.copy(importProgress = processed.toFloat() / total) }
                        }
                    }
                }
                DuplicateStrategy.RENEWAL_IF_LATER -> {
                    for (row in preview.duplicateRows) {
                        try {
                            val existing = repository.getVehicleWithPoliciesDirect(row.regNo)
                            val currentPolicy = existing?.currentPolicy
                            val currentExpiry = currentPolicy?.expiryDate ?: 0L
                            val importedExpiry = row.expiryDateEpochMs ?: 0L

                            if (currentPolicy != null && importedExpiry > currentExpiry) {
                                // Mark previous policy as renewed
                                repository.markPolicyRenewed(currentPolicy.id, now)

                                val customer = Customer(
                                    title = row.title ?: existing.vehicle.customer.title,
                                    name = row.customerName.ifBlank { existing.vehicle.customer.name },
                                    mobile = row.mobile.ifBlank { existing.vehicle.customer.mobile },
                                    email = row.email ?: existing.vehicle.customer.email,
                                    address = row.address.ifBlank { existing.vehicle.customer.address }
                                )
                                val vehicle = Vehicle(
                                    regNo = row.regNo,
                                    customer = customer,
                                    makeModel = row.makeModel ?: existing.vehicle.makeModel,
                                    vin = row.vin ?: existing.vehicle.vin,
                                    engineNo = row.engineNo ?: existing.vehicle.engineNo,
                                    notes = row.notes ?: existing.vehicle.notes,
                                    updatedAt = now
                                )
                                val renewalPolicy = Policy(
                                    vehicleRegNo = row.regNo,
                                    companySector = row.sector ?: currentPolicy.companySector,
                                    insurer = row.insurer.ifBlank { currentPolicy.insurer },
                                    policyNumber = row.policyNumber.ifBlank { currentPolicy.policyNumber },
                                    expiryDate = importedExpiry,
                                    totalPremium = row.totalPremium ?: currentPolicy.totalPremium,
                                    source = PolicySource.SPREADSHEET,
                                    saleType = SaleType.RENEWAL,
                                    saleDate = row.saleDateEpochMs ?: now,
                                    countsAsSale = true
                                )
                                repository.saveVehicleAndPolicy(vehicle, renewalPolicy)
                                renewed++
                                addedPolicyIds.add(renewalPolicy.id)
                            } else {
                                skipped++
                            }
                        } catch (e: Exception) {
                            failed.add(row)
                        }
                        processed++
                        if (total > 0) {
                            _uiState.update { it.copy(importProgress = processed.toFloat() / total) }
                        }
                    }
                }
            }

            val summary = ImportSummaryData(
                addedCount = added,
                updatedCount = updated,
                renewedCount = renewed,
                skippedCount = skipped,
                failedCount = failed.size,
                failedRows = failed,
                importedRegNos = addedRegNos,
                importedPolicyIds = addedPolicyIds,
                canUndo = true
            )

            _uiState.update {
                it.copy(
                    step = ImportStep.SUMMARY,
                    summary = summary
                )
            }
        }
    }

    fun undoLastImport() {
        val summary = _uiState.value.summary ?: return
        if (!summary.canUndo) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.rollbackImport(summary.importedRegNos, summary.importedPolicyIds)
            _uiState.update { it.copy(summary = summary.copy(canUndo = false)) }
            _uiEvent.emit("Last import successfully undone (${summary.importedPolicyIds.size} records reverted)")
        }
    }

    fun exportFailedRowsCsv(): String {
        val failed = _uiState.value.summary?.failedRows ?: emptyList()
        val sb = StringBuilder()
        sb.append("Row,Registration,Customer,Mobile,Errors\n")
        for (f in failed) {
            val errs = f.errors.joinToString("; ").replace("\"", "\"\"")
            sb.append("${f.rowNumber},\"${f.regNo}\",\"${f.customerName}\",\"${f.mobile}\",\"$errs\"\n")
        }
        return sb.toString()
    }

    fun reset() {
        _uiState.value = ImportUiState()
    }
}
