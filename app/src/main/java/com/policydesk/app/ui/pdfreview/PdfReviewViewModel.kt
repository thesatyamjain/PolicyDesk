package com.policydesk.app.ui.pdfreview

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.data.ai.AiExtractionClient
import com.policydesk.app.data.ai.ExtractedPolicyData
import com.policydesk.app.data.ai.ExtractionResult
import com.policydesk.app.data.importer.SpreadsheetParser
import com.policydesk.app.data.security.SecureKeyStorage
import com.policydesk.app.domain.model.Customer
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.domain.model.InsurerCatalog
import com.policydesk.app.domain.model.NumberPlateStyle
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicySource
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.SaleType
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.domain.model.Vehicle
import com.policydesk.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.policydesk.app.domain.model.ExistingClientInfo
import java.util.UUID
import javax.inject.Inject

data class PdfReviewUiState(
    val queue: List<Uri> = emptyList(),
    val queueIndex: Int = 0,
    val pdfUri: Uri? = null,
    val isExtracting: Boolean = false,
    val isPasswordProtected: Boolean = false,
    val passwordInput: String = "",
    val extractionError: String? = null,
    val extractedData: ExtractedPolicyData? = null,

    // Existing client & Renewal detection
    val existingClientInfo: ExistingClientInfo? = null,
    val isRenewalDetected: Boolean = false,

    // Editable form fields
    val regNo: String = "",
    val customerName: String = "",
    val title: String = "",
    val mobile: String = "",
    val email: String = "",
    val address: String = "",
    val sector: Sector? = null,
    val insurer: String = "",
    val policyNumber: String = "",
    val expiryDate: String = "",
    val totalPremium: String = "",
    val commissionPercentage: String = "",
    val commissionAmount: String = "",
    val makeModel: String = "",
    val vin: String = "",
    val engineNo: String = "",
    val selectedPlateStyle: NumberPlateStyle? = null,

    val isSaving: Boolean = false
) {
    val isValid: Boolean
        get() {
            val validReg = RegistrationNormalizer.normalize(regNo).isNotBlank()
            val validName = FieldValidators.isValidName(customerName)
            val validMobile = FieldValidators.isValidMobile(mobile)
            val validSector = sector != null
            val validInsurer = insurer.isNotBlank()
            val validPolicyNo = FieldValidators.isValidPolicyNumber(policyNumber)
            val validExpiry = SpreadsheetParser.parseDateToEpochMs(expiryDate) != null
            val validPremium = totalPremium.toDoubleOrNull()?.let { it > 0 } ?: false
            return validReg && validName && validMobile && validSector &&
                    validInsurer && validPolicyNo && validExpiry && validPremium
        }
}

sealed interface PdfReviewUiEvent {
    data class SaveSuccess(val regNo: String, val isBatchFinished: Boolean = true) : PdfReviewUiEvent
    data object BatchCompleted : PdfReviewUiEvent
    data class ShowToast(val message: String) : PdfReviewUiEvent
}

@HiltViewModel
class PdfReviewViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: VehicleRepository
) : ViewModel() {

    private val secureStorage = SecureKeyStorage(context)

    private val _uiState = MutableStateFlow(PdfReviewUiState())
    val uiState: StateFlow<PdfReviewUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<PdfReviewUiEvent>()
    val uiEvent: SharedFlow<PdfReviewUiEvent> = _uiEvent.asSharedFlow()

    fun onPdfsSelected(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _uiState.update { it.copy(queue = uris, queueIndex = 0) }
        loadCurrentQueueItem(uris, 0)
    }

    fun onPdfSelected(uri: Uri, password: String? = null) {
        _uiState.update { it.copy(queue = listOf(uri), queueIndex = 0) }
        loadPdf(uri, password)
    }

    private fun loadCurrentQueueItem(queue: List<Uri>, index: Int, password: String? = null) {
        if (index in queue.indices) {
            loadPdf(queue[index], password)
        }
    }

    fun skipCurrentPdf() {
        val state = _uiState.value
        val nextIndex = state.queueIndex + 1
        if (nextIndex < state.queue.size) {
            _uiState.update { it.copy(queueIndex = nextIndex) }
            loadCurrentQueueItem(state.queue, nextIndex)
        } else {
            viewModelScope.launch {
                _uiEvent.emit(PdfReviewUiEvent.BatchCompleted)
            }
        }
    }

    fun switchToManualEntry() {
        _uiState.update { it.copy(extractionError = null, isExtracting = false) }
    }

    private fun loadPdf(uri: Uri, password: String? = null) {
        _uiState.update {
            it.copy(
                pdfUri = uri,
                isExtracting = true,
                extractionError = null,
                isPasswordProtected = false,
                passwordInput = ""
            )
        }
        val config = secureStorage.getAiConfig()

        viewModelScope.launch {
            val result = AiExtractionClient.extractFromPdf(context, uri, config, password)
            when (result) {
                is ExtractionResult.Success -> {
                    val data = result.data

                    // Auto match sector & insurer
                    var sector: Sector? = null
                    var finalInsurer = data.insurer
                    if (data.sector.equals("PSU", ignoreCase = true)) sector = Sector.PSU
                    if (data.sector.equals("Private", ignoreCase = true)) sector = Sector.PRIVATE

                    val matched = InsurerCatalog.matchInsurer(data.insurer)
                    if (matched != null) {
                        if (sector == null) sector = matched.first
                        finalInsurer = matched.second
                    }

                    // Existing client & renewal check
                    val normalizedReg = RegistrationNormalizer.normalize(data.regNo)
                    val existing = if (normalizedReg.isNotBlank()) {
                        repository.getVehicleWithPoliciesDirect(normalizedReg)
                    } else null

                    val prevPolicy = existing?.currentPolicy
                    val existingInfo = if (existing != null) {
                        ExistingClientInfo(
                            clientName = existing.vehicle.customer.name,
                            mobile = existing.vehicle.customer.mobile,
                            previousInsurer = prevPolicy?.insurer,
                            previousPolicyNumber = prevPolicy?.policyNumber,
                            previousExpiryEpochMs = prevPolicy?.expiryDate,
                            previousPolicyId = prevPolicy?.id,
                            totalPoliciesCount = existing.policies.size,
                            clientSinceEpochMs = existing.earliestDate
                        )
                    } else null

                    // Smart auto-fill for customer fields (often masked in PDFs, e.g. 98XXXXXX12)
                    val extractedMobileNorm = FieldValidators.normalizeMobile(data.mobile)
                    val isExtractedMobileMaskedOrInvalid = data.mobile.contains("X", ignoreCase = true) ||
                            data.mobile.contains("*") ||
                            !FieldValidators.isValidMobile(extractedMobileNorm)

                    val resolvedMobile = if (isExtractedMobileMaskedOrInvalid && existing != null && FieldValidators.isValidMobile(existing.vehicle.customer.mobile)) {
                        existing.vehicle.customer.mobile
                    } else {
                        extractedMobileNorm
                    }

                    val resolvedName = FieldValidators.formatNameTitleCase(data.customerName.ifBlank { existing?.vehicle?.customer?.name ?: "" })
                    val resolvedTitle = data.title.ifBlank { existing?.vehicle?.customer?.title ?: "" }
                    val resolvedEmail = data.email.ifBlank { existing?.vehicle?.customer?.email ?: "" }
                    val resolvedAddress = data.address.ifBlank { existing?.vehicle?.customer?.address ?: "" }
                    val resolvedMakeModel = data.makeModel.ifBlank { existing?.vehicle?.makeModel ?: "" }
                    val resolvedVin = data.vin.ifBlank { existing?.vehicle?.vin ?: "" }
                    val resolvedEngineNo = data.engineNo.ifBlank { existing?.vehicle?.engineNo ?: "" }

                    // Advisor commission pre-fill if available from previous policy on this vehicle
                    val prevCommPct = prevPolicy?.commissionPercentage
                    val resolvedCommPct = if (prevCommPct != null && prevCommPct > 0.0) {
                        formatDecimal(prevCommPct)
                    } else ""

                    val premNum = data.totalPremium.toDoubleOrNull()
                    val resolvedCommAmt = if (premNum != null && premNum > 0.0 && prevCommPct != null && prevCommPct > 0.0) {
                        val netPrem = premNum / 1.18
                        formatDecimal((netPrem * prevCommPct) / 100.0)
                    } else ""

                    _uiState.update {
                        it.copy(
                            isExtracting = false,
                            isPasswordProtected = false,
                            extractedData = data,
                            existingClientInfo = existingInfo,
                            isRenewalDetected = existingInfo != null,
                            regNo = normalizedReg,
                            customerName = resolvedName,
                            title = resolvedTitle,
                            mobile = resolvedMobile,
                            email = resolvedEmail,
                            address = resolvedAddress,
                            sector = sector ?: Sector.PRIVATE,
                            insurer = finalInsurer,
                            policyNumber = data.policyNumber,
                            expiryDate = data.expiryDate,
                            totalPremium = data.totalPremium,
                            commissionPercentage = resolvedCommPct,
                            commissionAmount = resolvedCommAmt,
                            makeModel = resolvedMakeModel,
                            vin = resolvedVin,
                            engineNo = resolvedEngineNo
                        )
                    }
                }
                is ExtractionResult.PasswordRequired -> {
                    _uiState.update {
                        it.copy(
                            isExtracting = false,
                            isPasswordProtected = true
                        )
                    }
                }
                is ExtractionResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isExtracting = false,
                            isPasswordProtected = false,
                            extractionError = result.message
                        )
                    }
                }
            }
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(passwordInput = password) }
    }

    fun submitPassword() {
        val uri = _uiState.value.pdfUri ?: return
        val password = _uiState.value.passwordInput
        loadPdf(uri, password)
    }

    fun dismissPasswordDialog() {
        _uiState.update { it.copy(isPasswordProtected = false) }
    }

    private fun filterDecimal(input: String): String {
        return buildString {
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
    }

    private fun formatDecimal(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
        }
    }

    fun retryCurrentPdf() {
        val uri = _uiState.value.pdfUri ?: return
        val password = _uiState.value.passwordInput.ifBlank { null }
        loadPdf(uri, password)
    }

    private fun checkExistingVehicle(rawRegNo: String) {
        val normalized = RegistrationNormalizer.normalize(rawRegNo)
        if (normalized.length < 5) {
            _uiState.update { it.copy(existingClientInfo = null, isRenewalDetected = false) }
            return
        }
        viewModelScope.launch {
            val existing = repository.getVehicleWithPoliciesDirect(normalized)
            if (existing != null) {
                val prevPolicy = existing.currentPolicy
                val info = ExistingClientInfo(
                    clientName = existing.vehicle.customer.name,
                    mobile = existing.vehicle.customer.mobile,
                    previousInsurer = prevPolicy?.insurer,
                    previousPolicyNumber = prevPolicy?.policyNumber,
                    previousExpiryEpochMs = prevPolicy?.expiryDate,
                    previousPolicyId = prevPolicy?.id,
                    totalPoliciesCount = existing.policies.size,
                    clientSinceEpochMs = existing.earliestDate
                )
                _uiState.update { curr ->
                    val isCurrMobileInvalid = curr.mobile.isBlank() ||
                            curr.mobile.contains("X", ignoreCase = true) ||
                            curr.mobile.contains("*") ||
                            !FieldValidators.isValidMobile(curr.mobile)

                    val useMobile = if (isCurrMobileInvalid && FieldValidators.isValidMobile(existing.vehicle.customer.mobile)) {
                        existing.vehicle.customer.mobile
                    } else curr.mobile

                    val useName = curr.customerName.ifBlank { existing.vehicle.customer.name }
                    val useTitle = curr.title.ifBlank { existing.vehicle.customer.title ?: "" }
                    val useAddress = curr.address.ifBlank { existing.vehicle.customer.address }
                    val useEmail = curr.email.ifBlank { existing.vehicle.customer.email ?: "" }
                    val useMakeModel = curr.makeModel.ifBlank { existing.vehicle.makeModel ?: "" }
                    val useVin = curr.vin.ifBlank { existing.vehicle.vin ?: "" }
                    val useEngineNo = curr.engineNo.ifBlank { existing.vehicle.engineNo ?: "" }

                    val useCommPct = if (curr.commissionPercentage.isBlank() && prevPolicy?.commissionPercentage != null && prevPolicy.commissionPercentage > 0.0) {
                        formatDecimal(prevPolicy.commissionPercentage)
                    } else curr.commissionPercentage

                    val prem = curr.totalPremium.toDoubleOrNull()
                    val pctNum = useCommPct.toDoubleOrNull()
                    val useCommAmt = if (curr.commissionAmount.isBlank() && prem != null && prem > 0.0 && pctNum != null && pctNum >= 0.0) {
                        formatDecimal(((prem / 1.18) * pctNum) / 100.0)
                    } else curr.commissionAmount

                    curr.copy(
                        existingClientInfo = info,
                        isRenewalDetected = true,
                        customerName = useName,
                        title = useTitle,
                        mobile = useMobile,
                        address = useAddress,
                        email = useEmail,
                        makeModel = useMakeModel,
                        vin = useVin,
                        engineNo = useEngineNo,
                        selectedPlateStyle = curr.selectedPlateStyle ?: existing.vehicle.plateStyle,
                        commissionPercentage = useCommPct,
                        commissionAmount = useCommAmt
                    )
                }
            } else {
                _uiState.update { it.copy(existingClientInfo = null, isRenewalDetected = false) }
            }
        }
    }

    fun onFieldChanged(fieldKey: String, value: String) {
        if (fieldKey == "regNo") {
            checkExistingVehicle(value)
        }
        _uiState.update { current ->
            when (fieldKey) {
                "regNo" -> current.copy(regNo = RegistrationNormalizer.normalize(value))
                "customerName" -> current.copy(customerName = value)
                "mobile" -> current.copy(mobile = FieldValidators.normalizeMobile(value))
                "email" -> current.copy(email = value)
                "address" -> current.copy(address = value)
                "policyNumber" -> current.copy(policyNumber = value.uppercase())
                "expiryDate" -> current.copy(expiryDate = value)
                "totalPremium" -> {
                    val filtered = filterDecimal(value)
                    val prem = filtered.toDoubleOrNull()
                    val pctNum = current.commissionPercentage.toDoubleOrNull()
                    val calculatedAmount = if (prem != null && prem > 0.0 && pctNum != null && pctNum >= 0.0) {
                        val netPrem = prem / 1.18
                        val amt = (netPrem * pctNum) / 100.0
                        formatDecimal(amt)
                    } else {
                        current.commissionAmount
                    }
                    current.copy(
                        totalPremium = filtered,
                        commissionAmount = calculatedAmount
                    )
                }
                "commissionPercentage" -> {
                    val filtered = filterDecimal(value)
                    val prem = current.totalPremium.toDoubleOrNull()
                    val pctNum = filtered.toDoubleOrNull()
                    val calculatedAmount = if (prem != null && prem > 0.0 && pctNum != null && pctNum >= 0.0) {
                        val netPrem = prem / 1.18
                        val amt = (netPrem * pctNum) / 100.0
                        formatDecimal(amt)
                    } else {
                        if (filtered.isBlank()) "" else current.commissionAmount
                    }
                    current.copy(
                        commissionPercentage = filtered,
                        commissionAmount = calculatedAmount
                    )
                }
                "commissionAmount" -> {
                    val filtered = filterDecimal(value)
                    val prem = current.totalPremium.toDoubleOrNull()
                    val amtNum = filtered.toDoubleOrNull()
                    val calculatedPct = if (prem != null && prem > 0.0 && amtNum != null && amtNum >= 0.0) {
                        val netPrem = prem / 1.18
                        val pct = (amtNum / netPrem) * 100.0
                        formatDecimal(pct)
                    } else {
                        if (filtered.isBlank()) "" else current.commissionPercentage
                    }
                    current.copy(
                        commissionAmount = filtered,
                        commissionPercentage = calculatedPct
                    )
                }
                "makeModel" -> current.copy(makeModel = value)
                "vin" -> current.copy(vin = value.uppercase())
                "engineNo" -> current.copy(engineNo = value.uppercase())
                else -> current
            }
        }
    }

    fun onFieldBlur(fieldKey: String) {
        if (fieldKey == "customerName") {
            val current = _uiState.value.customerName
            if (current.isNotBlank()) {
                val formatted = FieldValidators.formatNameTitleCase(current)
                if (formatted != current) {
                    _uiState.update { it.copy(customerName = formatted) }
                }
            }
        }
    }

    fun onSectorSelected(sector: Sector) {
        _uiState.update { it.copy(sector = sector) }
    }

    fun onInsurerSelected(insurer: String) {
        _uiState.update { it.copy(insurer = insurer) }
    }

    fun onPlateStyleSelected(style: NumberPlateStyle?) {
        _uiState.update { it.copy(selectedPlateStyle = style) }
    }

    fun savePolicy() {
        val state = _uiState.value
        if (!state.isValid) return

        val normalizedReg = RegistrationNormalizer.normalize(state.regNo)
        val expiryMs = SpreadsheetParser.parseDateToEpochMs(state.expiryDate) ?: return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existingInfo = state.existingClientInfo
            val isRenewal = state.isRenewalDetected && existingInfo != null

            // Check if there's an existing policy on this vehicle to mark as renewed
            val isSamePolicyNo = existingInfo?.previousPolicyNumber?.equals(state.policyNumber.trim(), ignoreCase = true) == true
            if (isRenewal && existingInfo?.previousPolicyId != null && !isSamePolicyNo) {
                repository.markPolicyRenewed(existingInfo.previousPolicyId, now)
            }

            val customer = Customer(
                title = state.title.ifBlank { null },
                name = FieldValidators.formatNameTitleCase(state.customerName),
                mobile = FieldValidators.normalizeMobile(state.mobile),
                email = state.email.trim().ifBlank { null },
                address = state.address.trim()
            )

            val vehicle = Vehicle(
                regNo = normalizedReg,
                customer = customer,
                makeModel = state.makeModel.trim().ifBlank { null },
                vin = state.vin.trim().ifBlank { null },
                engineNo = state.engineNo.trim().ifBlank { null },
                plateStyle = state.selectedPlateStyle,
                updatedAt = now
            )

            val policyId = if (isSamePolicyNo && existingInfo?.previousPolicyId != null) {
                existingInfo.previousPolicyId
            } else {
                UUID.randomUUID().toString()
            }

            val policy = Policy(
                id = policyId,
                vehicleRegNo = normalizedReg,
                companySector = state.sector ?: Sector.PRIVATE,
                insurer = state.insurer.trim(),
                policyNumber = state.policyNumber.trim(),
                expiryDate = expiryMs,
                totalPremium = state.totalPremium.toDoubleOrNull() ?: 0.0,
                commissionPercentage = state.commissionPercentage.toDoubleOrNull(),
                commissionAmount = state.commissionAmount.toDoubleOrNull(),
                source = PolicySource.PDF,
                saleType = if (isRenewal && !isSamePolicyNo) SaleType.RENEWAL else SaleType.NEW_BUSINESS,
                saleDate = now,
                countsAsSale = true
            )

            repository.saveVehicleAndPolicy(vehicle, policy)
            _uiState.update { it.copy(isSaving = false) }

            val nextIndex = state.queueIndex + 1
            if (nextIndex < state.queue.size) {
                _uiEvent.emit(PdfReviewUiEvent.ShowToast("Saved $normalizedReg. Loading next document (${nextIndex + 1}/${state.queue.size})..."))
                _uiState.update { it.copy(queueIndex = nextIndex) }
                loadCurrentQueueItem(state.queue, nextIndex)
            } else {
                _uiEvent.emit(PdfReviewUiEvent.SaveSuccess(normalizedReg, isBatchFinished = true))
            }
        }
    }
}
