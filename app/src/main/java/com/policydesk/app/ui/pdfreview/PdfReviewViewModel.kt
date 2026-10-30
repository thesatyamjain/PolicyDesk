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
    val makeModel: String = "",
    val vin: String = "",
    val engineNo: String = "",

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

                    _uiState.update {
                        it.copy(
                            isExtracting = false,
                            isPasswordProtected = false,
                            extractedData = data,
                            regNo = RegistrationNormalizer.normalize(data.regNo),
                            customerName = data.customerName,
                            title = data.title,
                            mobile = FieldValidators.normalizeMobile(data.mobile),
                            email = data.email,
                            address = data.address,
                            sector = sector ?: Sector.PRIVATE,
                            insurer = finalInsurer,
                            policyNumber = data.policyNumber,
                            expiryDate = data.expiryDate,
                            totalPremium = data.totalPremium,
                            makeModel = data.makeModel,
                            vin = data.vin,
                            engineNo = data.engineNo
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

    fun retryCurrentPdf() {
        val uri = _uiState.value.pdfUri ?: return
        val password = _uiState.value.passwordInput.ifBlank { null }
        loadPdf(uri, password)
    }

    fun onFieldChanged(fieldKey: String, value: String) {
        _uiState.update {
            when (fieldKey) {
                "regNo" -> it.copy(regNo = RegistrationNormalizer.normalize(value))
                "customerName" -> it.copy(customerName = value)
                "mobile" -> it.copy(mobile = FieldValidators.normalizeMobile(value))
                "email" -> it.copy(email = value)
                "address" -> it.copy(address = value)
                "policyNumber" -> it.copy(policyNumber = value.uppercase())
                "expiryDate" -> it.copy(expiryDate = value)
                "totalPremium" -> it.copy(totalPremium = value.filter { c -> c.isDigit() || c == '.' })
                "makeModel" -> it.copy(makeModel = value)
                "vin" -> it.copy(vin = value.uppercase())
                "engineNo" -> it.copy(engineNo = value.uppercase())
                else -> it
            }
        }
    }

    fun onSectorSelected(sector: Sector) {
        _uiState.update { it.copy(sector = sector) }
    }

    fun onInsurerSelected(insurer: String) {
        _uiState.update { it.copy(insurer = insurer) }
    }

    fun savePolicy() {
        val state = _uiState.value
        if (!state.isValid) return

        val normalizedReg = RegistrationNormalizer.normalize(state.regNo)
        val expiryMs = SpreadsheetParser.parseDateToEpochMs(state.expiryDate) ?: return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val customer = Customer(
                title = state.title.ifBlank { null },
                name = state.customerName.trim(),
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
                updatedAt = now
            )

            val policy = Policy(
                vehicleRegNo = normalizedReg,
                companySector = state.sector ?: Sector.PRIVATE,
                insurer = state.insurer.trim(),
                policyNumber = state.policyNumber.trim(),
                expiryDate = expiryMs,
                totalPremium = state.totalPremium.toDoubleOrNull() ?: 0.0,
                source = PolicySource.PDF,
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
