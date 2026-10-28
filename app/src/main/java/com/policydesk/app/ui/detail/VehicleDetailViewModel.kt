package com.policydesk.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiEvent {
    object VehicleClosed : DetailUiEvent
    data class ShowToast(val message: String) : DetailUiEvent
}

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    private val repository: VehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val regNo: String = RegistrationNormalizer.normalize(checkNotNull(savedStateHandle["regNo"]))

    val vehicleData: StateFlow<VehicleWithPolicies?> = repository.getVehicleWithPolicies(regNo)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _uiEvent = MutableSharedFlow<DetailUiEvent>()
    val uiEvent: SharedFlow<DetailUiEvent> = _uiEvent.asSharedFlow()

    fun closeVehicle(reason: String) {
        viewModelScope.launch {
            repository.closeVehicle(regNo, reason)
            _uiEvent.emit(DetailUiEvent.VehicleClosed)
        }
    }

    fun updateCurrentPolicyPremium(newPremium: Double) {
        viewModelScope.launch {
            val vehicleWithPolicies = vehicleData.value ?: return@launch
            val policy = vehicleWithPolicies.currentPolicy ?: return@launch
            val updated = policy.copy(totalPremium = newPremium)
            repository.saveVehicleAndPolicy(vehicleWithPolicies.vehicle, updated)
            _uiEvent.emit(DetailUiEvent.ShowToast("Premium updated successfully"))
        }
    }
}
