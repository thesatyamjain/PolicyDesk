package com.policydesk.app.ui.addpolicy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.domain.model.Customer
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.domain.model.InsurerCatalog
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicySource
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.SaleType
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.domain.model.Vehicle
import com.policydesk.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class AddPolicyFormState(
    // Personal Details
    val title: String = "",
    val name: String = "",
    val mobile: String = "",
    val email: String = "",
    val address: String = "",

    // Policy Information
    val sector: Sector? = null,
    val insurer: String = "",
    val otherInsurerName: String = "",
    val policyNumber: String = "",
    val expiryDateEpochMs: Long? = null,
    val totalPremium: String = "",

    // Vehicle Specifications
    val regNo: String = "",
    val makeModel: String = "",
    val vin: String = "",
    val engineNo: String = "",

    // Notes
    val notes: String = "",

    // Validation Warnings & Errors
    val regNoDuplicate: Boolean = false,
    val regNoUnusualWarning: Boolean = false,
    val vinWarning: Boolean = false,
    val pastExpiryWarning: Boolean = false,

    // Dialog state for duplicate registration
    val showDuplicateDialog: Boolean = false,
    val showSectorChangeConfirmDialog: Boolean = false,
    val pendingSectorChange: Sector? = null,

    val isSaving: Boolean = false
) {
    val isFormValid: Boolean
        get() {
            val validName = FieldValidators.isValidName(name)
            val validMobile = FieldValidators.isValidMobile(mobile)
            val validAddress = FieldValidators.isValidAddress(address)
            val validSector = sector != null
            val effectiveInsurer = if (insurer == "Other") otherInsurerName.trim() else insurer.trim()
            val validInsurer = effectiveInsurer.isNotEmpty()
            val validPolicyNo = FieldValidators.isValidPolicyNumber(policyNumber)
            val validExpiry = expiryDateEpochMs != null
            val premiumNum = totalPremium.toDoubleOrNull()
            val validPremium = premiumNum != null && premiumNum > 0.0
            val validRegNo = RegistrationNormalizer.normalize(regNo).isNotEmpty()
            val validEmail = email.isBlank() || FieldValidators.isValidEmail(email)

            return validName && validMobile && validAddress && validSector &&
                    validInsurer && validPolicyNo && validExpiry && validPremium &&
                    validRegNo && validEmail
        }
}

sealed interface AddPolicyUiEvent {
    data class SaveSuccess(val regNo: String, val addAnother: Boolean) : AddPolicyUiEvent
    data class OpenExisting(val regNo: String) : AddPolicyUiEvent
    data class ShowToast(val message: String) : AddPolicyUiEvent
}

@HiltViewModel
class AddPolicyViewModel @Inject constructor(
    private val repository: VehicleRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(AddPolicyFormState())
    val formState: StateFlow<AddPolicyFormState> = _formState

    private val _uiEvent = MutableSharedFlow<AddPolicyUiEvent>()
    val uiEvent: SharedFlow<AddPolicyUiEvent> = _uiEvent.asSharedFlow()

    val makeModelSuggestions: StateFlow<List<String>> = repository.getDistinctMakeModels()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val customInsurers: StateFlow<List<String>> = _formState
        .map { it.sector }
        .distinctUntilChanged()
        .flatMapLatest { sector ->
            if (sector != null) repository.getCustomInsurers(sector)
            else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onTitleChanged(title: String) {
        _formState.update { it.copy(title = title) }
    }

    fun onNameChanged(name: String) {
        _formState.update { it.copy(name = name) }
    }

    fun onMobileChanged(mobile: String) {
        _formState.update { it.copy(mobile = mobile) }
    }

    fun onEmailChanged(email: String) {
        _formState.update { it.copy(email = email) }
    }

    fun onAddressChanged(address: String) {
        _formState.update { it.copy(address = address) }
    }

    fun onSectorSelected(newSector: Sector) {
        val current = _formState.value
        if (current.sector != null && current.sector != newSector && current.insurer.isNotEmpty()) {
            _formState.update {
                it.copy(
                    showSectorChangeConfirmDialog = true,
                    pendingSectorChange = newSector
                )
            }
        } else {
            _formState.update {
                it.copy(
                    sector = newSector,
                    insurer = "",
                    otherInsurerName = ""
                )
            }
        }
    }

    fun confirmSectorChange() {
        val pending = _formState.value.pendingSectorChange ?: return
        _formState.update {
            it.copy(
                sector = pending,
                insurer = "",
                otherInsurerName = "",
                showSectorChangeConfirmDialog = false,
                pendingSectorChange = null
            )
        }
    }

    fun dismissSectorChangeDialog() {
        _formState.update {
            it.copy(
                showSectorChangeConfirmDialog = false,
                pendingSectorChange = null
            )
        }
    }

    fun onInsurerSelected(insurer: String) {
        _formState.update {
            it.copy(
                insurer = insurer,
                otherInsurerName = if (insurer != "Other") "" else it.otherInsurerName
            )
        }
    }

    fun onOtherInsurerNameChanged(name: String) {
        _formState.update { it.copy(otherInsurerName = name) }
    }

    fun onPolicyNumberChanged(policyNo: String) {
        _formState.update { it.copy(policyNumber = policyNo.trim().uppercase()) }
    }

    fun onExpiryDateSelected(epochMillis: Long) {
        val today = LocalDate.now()
        val selectedDate = java.time.Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val isPast = selectedDate.isBefore(today)

        _formState.update {
            it.copy(
                expiryDateEpochMs = epochMillis,
                pastExpiryWarning = isPast
            )
        }
    }

    fun onTotalPremiumChanged(premium: String) {
        // Allow only valid decimal numbers
        val filtered = premium.filter { it.isDigit() || it == '.' }
        _formState.update { it.copy(totalPremium = filtered) }
    }

    fun onRegNoChanged(raw: String) {
        val normalized = RegistrationNormalizer.normalize(raw)
        val isUnusual = normalized.isNotEmpty() && !RegistrationNormalizer.isStandardFormat(normalized)
        _formState.update {
            it.copy(
                regNo = raw.uppercase(),
                regNoUnusualWarning = isUnusual,
                regNoDuplicate = false
            )
        }
    }

    fun onRegNoBlur() {
        val normalized = RegistrationNormalizer.normalize(_formState.value.regNo)
        if (normalized.isEmpty()) return

        viewModelScope.launch {
            val exists = repository.exists(normalized)
            if (exists) {
                _formState.update {
                    it.copy(
                        regNoDuplicate = true,
                        showDuplicateDialog = true
                    )
                }
            }
        }
    }

    fun onMakeModelChanged(makeModel: String) {
        _formState.update { it.copy(makeModel = makeModel) }
    }

    fun onVinChanged(vin: String) {
        val clean = vin.uppercase().trim()
        val warning = clean.isNotEmpty() && clean.length != 17
        _formState.update {
            it.copy(
                vin = clean,
                vinWarning = warning
            )
        }
    }

    fun onEngineNoChanged(engineNo: String) {
        _formState.update { it.copy(engineNo = engineNo.uppercase().trim()) }
    }

    fun onNotesChanged(notes: String) {
        _formState.update { it.copy(notes = notes) }
    }

    fun dismissDuplicateDialog() {
        _formState.update { it.copy(showDuplicateDialog = false) }
    }

    fun onOpenExistingDuplicate() {
        val normalized = RegistrationNormalizer.normalize(_formState.value.regNo)
        _formState.update { it.copy(showDuplicateDialog = false) }
        viewModelScope.launch {
            _uiEvent.emit(AddPolicyUiEvent.OpenExisting(normalized))
        }
    }

    fun savePolicy(addAnother: Boolean = false) {
        val state = _formState.value
        if (!state.isFormValid) return

        val normalizedReg = RegistrationNormalizer.normalize(state.regNo)
        val finalInsurer = if (state.insurer == "Other") state.otherInsurerName.trim() else state.insurer.trim()

        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val customer = Customer(
                title = state.title.ifBlank { null },
                name = state.name.trim(),
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
                notes = state.notes.trim().ifBlank { null }
            )

            val policy = Policy(
                vehicleRegNo = normalizedReg,
                companySector = state.sector!!,
                insurer = finalInsurer,
                policyNumber = state.policyNumber.trim(),
                expiryDate = state.expiryDateEpochMs!!,
                totalPremium = state.totalPremium.toDoubleOrNull() ?: 0.0,
                source = PolicySource.FORM,
                saleType = SaleType.NEW_BUSINESS,
                countsAsSale = true
            )

            repository.saveVehicleAndPolicy(vehicle, policy)

            _formState.update { it.copy(isSaving = false) }

            if (addAnother) {
                // Reset form for next entry
                _formState.value = AddPolicyFormState()
                _uiEvent.emit(AddPolicyUiEvent.SaveSuccess(normalizedReg, addAnother = true))
            } else {
                _uiEvent.emit(AddPolicyUiEvent.SaveSuccess(normalizedReg, addAnother = false))
            }
        }
    }
}
