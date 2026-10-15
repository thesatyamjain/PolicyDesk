package com.policydesk.app.ui.renew

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicySource
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.SaleType
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class RenewFormState(
    val currentVehicle: VehicleWithPolicies? = null,
    val newPolicyNumber: String = "",
    val newExpiryEpochMs: Long? = null,
    val newPremium: String = "",
    val startDateEpochMs: Long? = null,
    val notes: String = "",

    val earlierExpiryWarning: Boolean = false,
    val isSaving: Boolean = false
) {
    val isValid: Boolean
        get() {
            val validPolicyNo = FieldValidators.isValidPolicyNumber(newPolicyNumber)
            val validExpiry = newExpiryEpochMs != null && newExpiryEpochMs > System.currentTimeMillis()
            val premiumNum = newPremium.toDoubleOrNull()
            val validPremium = premiumNum != null && premiumNum > 0.0
            return validPolicyNo && validExpiry && validPremium
        }
}

sealed interface RenewUiEvent {
    data class RenewSuccess(val regNo: String) : RenewUiEvent
    data class ShowToast(val message: String) : RenewUiEvent
}

@HiltViewModel
class RenewPolicyViewModel @Inject constructor(
    private val repository: VehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val regNo: String = RegistrationNormalizer.normalize(checkNotNull(savedStateHandle["regNo"]))

    private val _formState = MutableStateFlow(RenewFormState())
    val formState: StateFlow<RenewFormState> = _formState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<RenewUiEvent>()
    val uiEvent: SharedFlow<RenewUiEvent> = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            val vehicle = repository.getVehicleWithPoliciesDirect(regNo)
            if (vehicle != null) {
                val currentPolicy = vehicle.currentPolicy
                // Default suggested expiry is 1 year after current expiry (or 1 year from now if already overdue)
                val baseDate = if (currentPolicy != null && currentPolicy.expiryDate > System.currentTimeMillis()) {
                    Instant.ofEpochMilli(currentPolicy.expiryDate).atZone(ZoneId.systemDefault()).toLocalDate()
                } else {
                    LocalDate.now()
                }
                val suggestedExpiry = baseDate.plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                _formState.update {
                    it.copy(
                        currentVehicle = vehicle,
                        newExpiryEpochMs = suggestedExpiry,
                        newPremium = currentPolicy?.totalPremium?.toString() ?: ""
                    )
                }
            }
        }
    }

    fun onPolicyNumberChanged(no: String) {
        _formState.update { it.copy(newPolicyNumber = no.trim().uppercase()) }
    }

    fun onExpiryDateSelected(epochMillis: Long) {
        val currentExpiry = _formState.value.currentVehicle?.currentPolicy?.expiryDate ?: 0L
        val isEarlier = epochMillis <= currentExpiry
        _formState.update {
            it.copy(
                newExpiryEpochMs = epochMillis,
                earlierExpiryWarning = isEarlier
            )
        }
    }

    fun onPremiumChanged(premium: String) {
        val filtered = premium.filter { it.isDigit() || it == '.' }
        _formState.update { it.copy(newPremium = filtered) }
    }

    fun onNotesChanged(notes: String) {
        _formState.update { it.copy(notes = notes) }
    }

    fun saveRenewal() {
        val state = _formState.value
        val vehicleWithPolicies = state.currentVehicle ?: return
        if (!state.isValid) return

        val prevPolicy = vehicleWithPolicies.currentPolicy ?: return

        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val now = System.currentTimeMillis()

            // 1. Mark previous policy as Renewed
            val updatedPrevPolicy = prevPolicy.copy(
                status = PolicyStatus.RENEWED,
                renewedOn = now
            )

            // 2. Create the new current policy
            val newPolicy = Policy(
                vehicleRegNo = vehicleWithPolicies.vehicle.regNo,
                companySector = prevPolicy.companySector,
                insurer = prevPolicy.insurer,
                policyNumber = state.newPolicyNumber.trim(),
                expiryDate = state.newExpiryEpochMs!!,
                startDate = state.startDateEpochMs ?: now,
                totalPremium = state.newPremium.toDoubleOrNull() ?: 0.0,
                status = PolicyStatus.ACTIVE,
                source = PolicySource.FORM,
                saleType = SaleType.RENEWAL,
                saleDate = now,
                countsAsSale = true
            )

            val updatedVehicle = vehicleWithPolicies.vehicle.copy(
                updatedAt = now,
                isClosed = false,
                closeReason = null
            )

            // Save both
            repository.saveVehicleAndPolicy(updatedVehicle, updatedPrevPolicy)
            repository.saveVehicleAndPolicy(updatedVehicle, newPolicy)

            _formState.update { it.copy(isSaving = false) }
            _uiEvent.emit(RenewUiEvent.RenewSuccess(vehicleWithPolicies.vehicle.regNo))
        }
    }
}
