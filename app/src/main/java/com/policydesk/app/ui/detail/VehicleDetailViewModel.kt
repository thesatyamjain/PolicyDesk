package com.policydesk.app.ui.detail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.domain.repository.VehicleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiEvent {
    object VehicleClosed : DetailUiEvent
    object VehicleDeleted : DetailUiEvent
    data class ShowToast(val message: String) : DetailUiEvent
}

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: VehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val activeRegNo = MutableStateFlow(
        RegistrationNormalizer.normalize(checkNotNull(savedStateHandle["regNo"]))
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val vehicleData: StateFlow<VehicleWithPolicies?> = activeRegNo
        .flatMapLatest { reg -> repository.getVehicleWithPolicies(reg) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val siblingVehicles: StateFlow<List<VehicleWithPolicies>> = combine(
        vehicleData,
        repository.getAllVehicles()
    ) { current, all ->
        val currentItem = current ?: return@combine emptyList()
        val normalizedMobile = FieldValidators.normalizeMobile(currentItem.vehicle.customer.mobile)
        if (normalizedMobile.isBlank()) return@combine emptyList()
        all.filter { candidate ->
            candidate.vehicle.regNo != currentItem.vehicle.regNo &&
                FieldValidators.normalizeMobile(candidate.vehicle.customer.mobile) == normalizedMobile
        }.sortedBy { it.currentPolicy?.expiryDate ?: Long.MAX_VALUE }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val _uiEvent = MutableSharedFlow<DetailUiEvent>()
    val uiEvent: SharedFlow<DetailUiEvent> = _uiEvent.asSharedFlow()

    fun selectVehicle(regNo: String) {
        val normalized = RegistrationNormalizer.normalize(regNo)
        if (normalized.isNotEmpty() && normalized != activeRegNo.value) {
            activeRegNo.value = normalized
        }
    }

    fun closeVehicle(reason: String) {
        val reg = activeRegNo.value
        viewModelScope.launch {
            repository.closeVehicle(reg, reason)
            _uiEvent.emit(DetailUiEvent.VehicleClosed)
        }
    }

    fun deleteVehicle() {
        val reg = activeRegNo.value
        viewModelScope.launch {
            repository.softDeleteVehicle(reg)
            _uiEvent.emit(DetailUiEvent.VehicleDeleted)
        }
    }

    fun updateCurrentPolicyPremium(newPremium: Double) {
        if (newPremium <= 0.0 || newPremium.isNaN() || newPremium.isInfinite()) {
            viewModelScope.launch {
                _uiEvent.emit(DetailUiEvent.ShowToast("Enter a positive premium amount"))
            }
            return
        }
        viewModelScope.launch {
            val vehicleWithPolicies = vehicleData.value ?: return@launch
            val policy = vehicleWithPolicies.currentPolicy ?: return@launch
            val newCommissionAmount = policy.commissionPercentage?.let { pct ->
                val netPrem = newPremium / 1.18
                (netPrem * pct) / 100.0
            } ?: policy.commissionAmount
            val updated = policy.copy(
                totalPremium = newPremium,
                commissionAmount = newCommissionAmount
            )
            repository.saveVehicleAndPolicy(vehicleWithPolicies.vehicle, updated)
            _uiEvent.emit(DetailUiEvent.ShowToast("Premium updated successfully"))
        }
    }

    fun attachPolicyDocument(uri: Uri) {
        viewModelScope.launch {
            val vehicleWithPolicies = vehicleData.value ?: return@launch
            val policy = vehicleWithPolicies.currentPolicy ?: return@launch
            val savedId = com.policydesk.app.data.storage.PolicyDocumentManager.savePolicyDocument(
                context = context,
                sourceUri = uri,
                policyId = policy.id
            )
            if (savedId != null) {
                val updated = policy.copy(attachmentId = savedId)
                repository.saveVehicleAndPolicy(vehicleWithPolicies.vehicle, updated)
                _uiEvent.emit(DetailUiEvent.ShowToast("Policy PDF saved & stored in app"))
            } else {
                _uiEvent.emit(DetailUiEvent.ShowToast("Failed to save policy PDF"))
            }
        }
    }

    fun removePolicyDocument() {
        viewModelScope.launch {
            val vehicleWithPolicies = vehicleData.value ?: return@launch
            val policy = vehicleWithPolicies.currentPolicy ?: return@launch
            val attachmentId = policy.attachmentId ?: return@launch
            com.policydesk.app.data.storage.PolicyDocumentManager.deletePolicyDocument(context, attachmentId)
            val updated = policy.copy(attachmentId = null)
            repository.saveVehicleAndPolicy(vehicleWithPolicies.vehicle, updated)
            _uiEvent.emit(DetailUiEvent.ShowToast("Policy PDF removed"))
        }
    }
}
