package com.policydesk.app.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.domain.model.CustomerGroup
import com.policydesk.app.domain.model.RegistrationNormalizer
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
    val isLoading: Boolean = false,
    val expandedMobiles: Set<String> = emptySet(),
    val hasAnyCustomers: Boolean = false
)

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val repository: VehicleRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _expandedMobiles = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<CustomersUiState> = combine(
        repository.getCustomerGroups(),
        _searchQuery,
        _expandedMobiles
    ) { groups, query, expanded ->
        val cleanQuery = query.trim().lowercase()
        val normalizedQuery = RegistrationNormalizer.normalize(query).lowercase()
        val filtered = if (cleanQuery.isEmpty()) {
            groups
        } else {
            groups.filter { group ->
                group.name.lowercase().contains(cleanQuery) ||
                        group.mobile.contains(cleanQuery) ||
                        group.vehicles.any {
                            it.vehicle.regNo.lowercase().contains(cleanQuery) ||
                                (normalizedQuery.isNotEmpty() && it.vehicle.regNo.lowercase().contains(normalizedQuery))
                        }
            }
        }

        CustomersUiState(
            customers = filtered,
            searchQuery = query,
            totalCustomers = groups.size,
            multiVehicleCount = groups.count { it.vehicles.size > 1 },
            isLoading = false,
            expandedMobiles = expanded,
            hasAnyCustomers = groups.isNotEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CustomersUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleCustomerExpanded(mobile: String) {
        val current = _expandedMobiles.value.toMutableSet()
        if (!current.add(mobile)) {
            current.remove(mobile)
        }
        _expandedMobiles.value = current
    }
}
