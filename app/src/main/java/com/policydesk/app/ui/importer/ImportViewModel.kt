package com.policydesk.app.ui.importer

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.data.importer.ImportPreviewResult
import com.policydesk.app.data.importer.ParsedRow
import com.policydesk.app.data.importer.SpreadsheetParser
import com.policydesk.app.domain.model.Customer
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.domain.model.InsurerCatalog
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicySource
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.RegistrationNormalizer
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
import java.util.UUID
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

                // Intelligently detect header row index (skipping report titles/metadata rows)
                val headerIndex = findHeaderRowIndex(table)
                val headerRow = table[headerIndex]
                val allDataRows = table.drop(headerIndex + 1)

                // Filter out blank rows and summary/trailer rows (e.g. "Total", "Grand Total")
                val dataRows = allDataRows.filter { row ->
                    if (row.all { it.isBlank() }) return@filter false
                    val firstCell = row.firstOrNull()?.trim()?.lowercase() ?: ""
                    val isSummaryRow = firstCell.startsWith("total") ||
                            firstCell.startsWith("grand total") ||
                            firstCell.startsWith("sub total") ||
                            firstCell.startsWith("count") ||
                            firstCell.startsWith("summary")
                    !isSummaryRow
                }

                val autoMapping = autoMapHeaders(headerRow, dataRows)

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

    private fun findHeaderRowIndex(table: List<List<String>>): Int {
        val maxScan = minOf(table.size, 15)
        var bestIndex = 0
        var maxMatches = -1

        val keyTokens = listOf(
            "reg", "veh", "vahan", "policy", "pol", "insured", "name", "customer",
            "chassis", "vin", "engine", "mobile", "phone", "contact", "expiry", "exp",
            "due", "valid", "premium", "prem", "tp", "od", "make", "model", "insurer", "company"
        )

        for (i in 0 until maxScan) {
            val row = table[i]
            if (row.size < 2) continue
            var score = 0
            for (cell in row) {
                val clean = cell.lowercase().replace(" ", "").replace("_", "").replace("-", "")
                if (keyTokens.any { clean.contains(it) }) {
                    score++
                }
            }
            if (score > maxMatches && score >= 2) {
                maxMatches = score
                bestIndex = i
            }
        }
        return bestIndex
    }

    companion object {
        fun autoMapHeaders(headers: List<String>, sampleRows: List<List<String>> = emptyList()): Map<Int, String> {
        val mapping = mutableMapOf<Int, String>()
        val mappedFields = mutableSetOf<String>()

        for ((index, rawHeader) in headers.withIndex()) {
            val h = rawHeader.lowercase()
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "")
                .replace(".", "")
                .replace("/", "")

            val isExcludedFromPremium = h.contains("office") || h.contains("branch") || h.contains("code") ||
                    h.contains("bocode") || h.contains("docode") || h.contains("devoff") || h.contains("agent") ||
                    h.contains("broker") || h.contains("pos") || h.contains("idv") || h.contains("suminsured") ||
                    h.contains("claim") || h.contains("tax") || h.contains("gst") || h.contains("sno") ||
                    h.contains("srno") || h.contains("serial") || h.contains("year") || h.contains("pincode") ||
                    h.contains("capacity") || h.contains("seating") || h.contains("cc") || h.contains("gvw")

            val target = when {
                h.contains("reg") || h.contains("vehicleno") || h.contains("vehiclenumber") ||
                        h.contains("vehical") || h.contains("vahan") || h.contains("vahanno") ||
                        h.contains("vehno") || h.contains("vehnum") || h.contains("rcno") ||
                        h.contains("rcnum") || h == "rc" || h == "veh" || h.contains("carno") ||
                        h.contains("bikeno") || h.contains("truckno") || h.contains("busno") ||
                        h.contains("rtono") || h.contains("registration") || h.contains("regmark") ||
                        h.contains("registrationmark") || h.contains("gaddi") -> "regNo"

                h.contains("customername") || h.contains("clientname") || h.contains("insuredname") ||
                        h.contains("proposername") || h.contains("partyname") || h.contains("ownername") ||
                        h.contains("custname") || h.contains("policyholder") ||
                        h == "name" || h.contains("customer") || h.contains("client") ||
                        h.contains("insured") || h.contains("proposer") || h.contains("party") ||
                        h.contains("owner") || h.contains("holder") -> "name"

                h.contains("title") || h.contains("salutation") || h.contains("prefix") ||
                        h.contains("mrms") -> "title"

                h.contains("mobile") || h.contains("phone") || h.contains("contact") ||
                        h.contains("cell") || h.contains("whatsapp") || h.contains("tel") ||
                        h == "mob" || h.contains("contactno") || h.contains("phoneno") ||
                        h.contains("custmobile") || h.contains("mobilenumber") ||
                        h.contains("contactnumber") -> "mobile"

                h.contains("email") || h.contains("mail") || h.contains("emailid") -> "email"

                h.contains("address") || h.contains("residence") || h.contains("location") ||
                        h.contains("city") || h.contains("addr") || h.contains("state") -> "address"

                h.contains("sector") || h.contains("companytype") || h.contains("category") -> "sector"

                h.contains("insurer") || h.contains("insurancecompany") || h.contains("underwriter") ||
                        h.contains("carrier") || h.contains("company") || h.contains("companyname") ||
                        h.contains("insuranceco") || h.contains("insurance") -> "insurer"

                h.contains("policyno") || h.contains("policynumber") || h.contains("policy") ||
                        h.contains("polno") || h.contains("covernoteno") || h.contains("cnno") ||
                        h.contains("certificateno") || h.contains("covernote") || h.contains("certno") ||
                        h.contains("polnum") -> "policyNumber"

                h.contains("expiry") || h.contains("expdate") || h.contains("validtill") ||
                        h.contains("validto") || h.contains("validupto") || h.contains("enddate") ||
                        h.contains("duedate") || h.contains("renewaldate") || h.contains("periodto") ||
                        h.contains("riskexpiry") || h == "exp" -> "expiryDate"

                !isExcludedFromPremium && (
                        h.contains("tppremium") || h.contains("thirdpartypremium") || h.contains("tpprem") ||
                                h == "tp" || h == "thirdparty"
                ) -> "tpPremium"

                !isExcludedFromPremium && (
                        h.contains("odpremium") || h.contains("owndamagepremium") || h.contains("odprem") ||
                                h == "od" || h == "owndamage"
                ) -> "odPremium"

                !isExcludedFromPremium && (
                        h.contains("premium") || h.contains("netpremium") || h.contains("grosspremium") ||
                                h.contains("totalpremium") || h.contains("finalpremium") || h.contains("totalprem") ||
                                h.contains("grossprem") || h.contains("netprem") ||
                                h.contains("totalamount") || h.contains("netamount") || h.contains("grossamount") ||
                                h.contains("finalamount") || h.contains("premiumamount") ||
                                h.contains("gwp") || h.contains("nwp") || h == "prem"
                ) -> "totalPremium"

                h.contains("makemodel") || (h.contains("make") && h.contains("model")) ||
                        h.contains("variant") || h.contains("description") -> "makeModel"

                h.contains("make") || h.contains("mfg") || h.contains("manufacturer") || h.contains("oem") -> "make"

                h.contains("model") || h.contains("submodel") -> "model"

                h.contains("vin") || h.contains("chassis") || h.contains("chassisno") ||
                        h.contains("chassisnumber") || h.contains("frameno") || h.contains("framenumber") ||
                        h.contains("vinnumber") || h.contains("chassisnum") -> "vin"

                h.contains("engineno") || h.contains("enginenumber") || h.contains("engno") ||
                        h.contains("enginenum") || h.contains("engine") || h.contains("motorno") -> "engineNo"

                h.contains("notes") || h.contains("note") || h.contains("remark") ||
                        h.contains("remarks") || h.contains("comments") -> "notes"

                h.contains("saledate") || h.contains("startdate") || h.contains("issuedate") ||
                        h.contains("inception") || h.contains("bookingdate") || h.contains("periodfrom") ||
                        h.contains("booked") -> "saleDate"

                else -> null
            }

            if (target != null && target !in mappedFields) {
                mapping[index] = target
                mappedFields.add(target)
            }
        }

        // Fallback: Content-based heuristic auto-detection from first few sample rows
        if (sampleRows.isNotEmpty()) {
            val unmappedIndices = headers.indices.filter { it !in mapping }
            for (colIdx in unmappedIndices) {
                val rawH = headers.getOrNull(colIdx)?.lowercase() ?: ""
                val h = rawH.replace(" ", "").replace("_", "").replace("-", "")
                val isExcluded = h.contains("office") || h.contains("branch") || h.contains("code") ||
                        h.contains("bocode") || h.contains("docode") || h.contains("devoff") ||
                        h.contains("idv") || h.contains("sum") || h.contains("sno") || h.contains("sr") ||
                        h.contains("no") || h.contains("num") || h.contains("year") || h.contains("date") ||
                        h.contains("pin") || h.contains("tax") || h.contains("gst") || h.contains("cc") ||
                        h.contains("seat") || h.contains("cap") || h.contains("phone") || h.contains("mob")

                val samples = sampleRows.take(5).mapNotNull { row ->
                    if (colIdx < row.size) row[colIdx].trim().ifBlank { null } else null
                }
                if (samples.isEmpty()) continue

                val target = when {
                    "regNo" !in mappedFields && samples.any { RegistrationNormalizer.isStandardFormat(RegistrationNormalizer.normalize(it)) } -> "regNo"
                    "mobile" !in mappedFields && samples.any { FieldValidators.isValidMobile(it) } -> "mobile"
                    "email" !in mappedFields && samples.any { it.contains("@") && FieldValidators.isValidEmail(it) } -> "email"
                    "vin" !in mappedFields && samples.any { it.length == 17 && it.all { c -> c.isLetterOrDigit() } } -> "vin"
                    "expiryDate" !in mappedFields && samples.any { SpreadsheetParser.parseDateToEpochMs(it) != null } -> "expiryDate"
                    "totalPremium" !in mappedFields && !isExcluded &&
                            (h.contains("prem") || h.contains("cost") || h.contains("amount") || h.contains("pay") || samples.any { it.contains("₹") || it.contains("Rs", ignoreCase = true) }) &&
                            samples.any { SpreadsheetParser.parsePremium(it)?.let { p -> p > 0 } == true } -> "totalPremium"
                    "insurer" !in mappedFields && samples.any { InsurerCatalog.matchInsurer(it) != null } -> "insurer"
                    else -> null
                }

                if (target != null && target !in mappedFields) {
                    mapping[colIdx] = target
                    mappedFields.add(target)
                }
            }
        }

        return mapping
    }
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
                            val existing = repository.getVehicleWithPoliciesDirect(row.regNo)
                            val currentPolicy = existing?.currentPolicy
                            val existingPolicyId = currentPolicy?.id ?: UUID.randomUUID().toString()

                            val customer = Customer(
                                title = row.title ?: existing?.vehicle?.customer?.title,
                                name = row.customerName.ifBlank { existing?.vehicle?.customer?.name ?: "" },
                                mobile = row.mobile.ifBlank { existing?.vehicle?.customer?.mobile ?: "" },
                                email = row.email ?: existing?.vehicle?.customer?.email,
                                address = row.address.ifBlank { existing?.vehicle?.customer?.address ?: "" }
                            )
                            val vehicle = Vehicle(
                                regNo = row.regNo,
                                customer = customer,
                                makeModel = row.makeModel ?: existing?.vehicle?.makeModel,
                                vin = row.vin ?: existing?.vehicle?.vin,
                                engineNo = row.engineNo ?: existing?.vehicle?.engineNo,
                                notes = row.notes ?: existing?.vehicle?.notes,
                                updatedAt = now
                            )
                            val policy = Policy(
                                id = existingPolicyId,
                                vehicleRegNo = row.regNo,
                                companySector = row.sector ?: currentPolicy?.companySector ?: com.policydesk.app.domain.model.Sector.PRIVATE,
                                insurer = row.insurer.ifBlank { currentPolicy?.insurer ?: "" },
                                policyNumber = row.policyNumber.ifBlank { currentPolicy?.policyNumber ?: "" },
                                expiryDate = row.expiryDateEpochMs ?: currentPolicy?.expiryDate ?: now,
                                totalPremium = row.totalPremium ?: currentPolicy?.totalPremium ?: 0.0,
                                source = PolicySource.SPREADSHEET,
                                saleDate = row.saleDateEpochMs ?: now,
                                countsAsSale = row.saleDateEpochMs != null
                            )
                            repository.saveVehicleAndPolicy(vehicle, policy)

                            // Clean up any extra unrenewed duplicate policies from previous imports
                            val extraPolicyIds = existing?.policies
                                ?.filter { it.id != existingPolicyId && it.renewedOn == null && it.closeReason.isNullOrBlank() }
                                ?.map { it.id } ?: emptyList()
                            if (extraPolicyIds.isNotEmpty()) {
                                repository.rollbackImport(emptyList(), extraPolicyIds)
                            }

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
