package com.policydesk.app.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.domain.model.CustomerGroup
import com.policydesk.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CustomersUiState(
    val customers: List<CustomerGroup> = emptyList(),
    val searchQuery: String = "",
    val totalCustomers: Int = 0,
    val multiVehicleCount: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val repository: VehicleRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<CustomersUiState> = combine(
        repository.getCustomerGroups(),
        _searchQuery
    ) { groups, query ->
        val cleanQuery = query.trim().lowercase()
        val filtered = if (cleanQuery.isEmpty()) {
            groups
        } else {
            groups.filter { group ->
                group.name.lowercase().contains(cleanQuery) ||
                        group.mobile.contains(cleanQuery) ||
                        group.vehicles.any { it.vehicle.regNo.lowercase().contains(cleanQuery) }
            }
        }

        CustomersUiState(
            customers = filtered,
            searchQuery = query,
            totalCustomers = groups.size,
            multiVehicleCount = groups.count { it.vehicles.size > 1 },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CustomersUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
