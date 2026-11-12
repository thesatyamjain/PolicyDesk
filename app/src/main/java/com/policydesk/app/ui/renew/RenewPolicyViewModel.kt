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
import com.policydesk.app.domain.model.Vehicle
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
    val isSaving: Boolean = false,
    val isRenewed: Boolean = false,
    val renewalToken: Long = 0L
) {
    val isPolicyNumberValid: Boolean
        get() = FieldValidators.isValidPolicyNumber(newPolicyNumber)

    val isExpiryValid: Boolean
        get() = isValidFutureExpiry(newExpiryEpochMs)

    val isPremiumValid: Boolean
        get() {
            val premiumNum = newPremium.toDoubleOrNull()
            return premiumNum != null && !premiumNum.isNaN() && !premiumNum.isInfinite() && premiumNum > 0.0
        }

    val validationErrors: List<String>
        get() = buildList {
            if (newPolicyNumber.isBlank()) {
                add("Policy Number")
            } else if (!isPolicyNumberValid) {
                add("Valid Policy Number")
            }
            if (newExpiryEpochMs == null) {
                add("Expiry Date")
            } else if (!isExpiryValid) {
                add("Future Expiry Date")
            }
            if (!isPremiumValid) {
                add("Positive Premium")
            }
        }

    val isValid: Boolean
        get() = validationErrors.isEmpty()

    companion object {
        fun isValidFutureExpiry(epochMs: Long?): Boolean {
            if (epochMs == null) return false
            val selectedDate = Instant.ofEpochMilli(epochMs)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            return selectedDate.isAfter(LocalDate.now())
        }
    }
}

sealed interface RenewUiEvent {
    data class RenewSuccess(val regNo: String) : RenewUiEvent
    object RenewalReverted : RenewUiEvent
    data class ShowToast(val message: String) : RenewUiEvent
}

private data class RenewalUndoSnapshot(
    val previousVehicle: Vehicle,
    val previousPolicy: Policy,
    val createdPolicyId: String
)

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

    private var undoSnapshot: RenewalUndoSnapshot? = null

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

                val initialPremium = currentPolicy?.totalPremium?.let { p ->
                    if (p % 1.0 == 0.0) p.toLong().toString() else p.toString()
                } ?: ""

                _formState.update {
                    it.copy(
                        currentVehicle = vehicle,
                        newExpiryEpochMs = suggestedExpiry,
                        newPremium = initialPremium,
                        notes = vehicle.vehicle.notes ?: ""
                    )
                }
            }
        }
    }

    fun onPolicyNumberChanged(no: String) {
        if (_formState.value.isRenewed) return
        _formState.update { it.copy(newPolicyNumber = no.uppercase()) }
    }

    fun onExpiryDateSelected(epochMillis: Long) {
        if (_formState.value.isRenewed) return
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
        if (_formState.value.isRenewed) return
        val filtered = buildString {
            var hasDot = false
            for (ch in premium) {
                if (ch.isDigit()) {
                    append(ch)
                } else if (ch == '.' && !hasDot) {
                    append(ch)
                    hasDot = true
                }
            }
        }
        _formState.update { it.copy(newPremium = filtered) }
    }

    fun onNotesChanged(notes: String) {
        if (_formState.value.isRenewed) return
        _formState.update { it.copy(notes = notes) }
    }

    fun saveRenewal() {
        val state = _formState.value
        if (state.isSaving || state.isRenewed || !state.isValid) return
        val vehicleWithPolicies = state.currentVehicle ?: return
        val prevPolicy = vehicleWithPolicies.currentPolicy
        if (prevPolicy == null) {
            viewModelScope.launch {
                _uiEvent.emit(RenewUiEvent.ShowToast("No active policy found to renew"))
            }
            return
        }

        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newPrem = state.newPremium.toDoubleOrNull() ?: 0.0
            val carriedCommPct = prevPolicy.commissionPercentage
            val newCommAmount = carriedCommPct?.let { pct ->
                ((newPrem / 1.18) * pct) / 100.0
            }

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
                totalPremium = newPrem,
                commissionPercentage = carriedCommPct,
                commissionAmount = newCommAmount,
                status = PolicyStatus.ACTIVE,
                source = PolicySource.FORM,
                saleType = SaleType.RENEWAL,
                saleDate = now,
                countsAsSale = true
            )

            val updatedVehicle = vehicleWithPolicies.vehicle.copy(
                notes = state.notes.trim().ifBlank { null },
                updatedAt = now,
                isClosed = false,
                closeReason = null
            )

            undoSnapshot = RenewalUndoSnapshot(
                previousVehicle = vehicleWithPolicies.vehicle,
                previousPolicy = prevPolicy,
                createdPolicyId = newPolicy.id
            )

            // Save both
            repository.saveVehicleAndPolicy(updatedVehicle, updatedPrevPolicy)
            repository.saveVehicleAndPolicy(updatedVehicle, newPolicy)

            val refreshed = repository.getVehicleWithPoliciesDirect(vehicleWithPolicies.vehicle.regNo)

            _formState.update {
                it.copy(
                    currentVehicle = refreshed ?: it.currentVehicle,
                    isSaving = false,
                    isRenewed = true,
                    renewalToken = System.nanoTime()
                )
            }
            _uiEvent.emit(RenewUiEvent.RenewSuccess(vehicleWithPolicies.vehicle.regNo))
        }
    }

    fun undoRenewal() {
        val snap = undoSnapshot ?: return
        undoSnapshot = null
        // Clear token synchronously so any pending UI auto-pop timer cancels immediately
        _formState.update {
            it.copy(
                isRenewed = false,
                renewalToken = 0L,
                isSaving = true
            )
        }
        viewModelScope.launch {
            repository.rollbackImport(emptyList(), listOf(snap.createdPolicyId))
            repository.saveVehicleAndPolicy(snap.previousVehicle, snap.previousPolicy)
            val refreshed = repository.getVehicleWithPoliciesDirect(snap.previousVehicle.regNo)
            _formState.update {
                it.copy(
                    currentVehicle = refreshed ?: it.currentVehicle,
                    isSaving = false,
                    isRenewed = false,
                    renewalToken = 0L
                )
            }
            _uiEvent.emit(RenewUiEvent.RenewalReverted)
        }
    }
}
