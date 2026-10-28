package com.policydesk.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.domain.model.DashboardSummary
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class ExpiryFilter(val label: String) {
    ALL("All Active"),
    OVERDUE("Overdue"),
    NEXT_7_DAYS("Next 7 Days"),
    NEXT_30_DAYS("Next 30 Days")
}

enum class SectorFilter(val label: String) {
    ALL("All Sectors"),
    PSU("PSU Insurers"),
    PRIVATE("Private Insurers")
}

data class DashboardUiState(
    val summary: DashboardSummary = DashboardSummary(),
    val searchQuery: String = "",
    val activeFilter: ExpiryFilter = ExpiryFilter.ALL,
    val sectorFilter: SectorFilter = SectorFilter.ALL,
    val filteredList: List<VehicleWithPolicies> = emptyList(),
    val isLoading: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedRegNos: Set<String> = emptySet()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: VehicleRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(ExpiryFilter.ALL)
    private val _sectorFilter = MutableStateFlow(SectorFilter.ALL)
    private val _isSelectionMode = MutableStateFlow(false)
    private val _selectedRegNos = MutableStateFlow<Set<String>>(emptySet())

    private val _filterState = combine(
        repository.getDashboardSummary(),
        _searchQuery,
        _activeFilter,
        _sectorFilter
    ) { summary, query, filter, sector ->
        val today = LocalDate.now().toEpochDay()
        val queryClean = RegistrationNormalizer.normalize(query)

        val list = summary.soonestExpiring.filter { item ->
            // Search filter
            val regMatch = queryClean.isNotEmpty() && item.vehicle.regNo.contains(queryClean)
            val nameMatch = item.vehicle.customer.name.contains(query.trim(), ignoreCase = true)
            val mobileMatch = item.vehicle.customer.mobile.contains(query.trim())
            val policyMatch = item.currentPolicy?.policyNumber?.contains(query.trim(), ignoreCase = true) == true
            val matchesSearch = query.isBlank() || regMatch || nameMatch || mobileMatch || policyMatch

            if (!matchesSearch) return@filter false

            val policy = item.currentPolicy ?: return@filter false
            val daysLeft = policy.computeDaysRemaining(today)

            // Category filter
            val matchesExpiry = when (filter) {
                ExpiryFilter.ALL -> true
                ExpiryFilter.OVERDUE -> daysLeft < 0
                ExpiryFilter.NEXT_7_DAYS -> daysLeft in 0..7
                ExpiryFilter.NEXT_30_DAYS -> daysLeft in 0..30
            }

            if (!matchesExpiry) return@filter false

            // Sector filter (PRD 5.5 P1)
            when (sector) {
                SectorFilter.ALL -> true
                SectorFilter.PSU -> policy.companySector == com.policydesk.app.domain.model.Sector.PSU
                SectorFilter.PRIVATE -> policy.companySector == com.policydesk.app.domain.model.Sector.PRIVATE
            }
        }

        DashboardUiState(
            summary = summary,
            searchQuery = query,
            activeFilter = filter,
            sectorFilter = sector,
            filteredList = list,
            isLoading = false
        )
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        _filterState,
        _isSelectionMode,
        _selectedRegNos
    ) { baseState, isSelectionMode, selectedRegNos ->
        baseState.copy(
            isSelectionMode = isSelectionMode,
            selectedRegNos = selectedRegNos
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: ExpiryFilter) {
        _activeFilter.value = filter
    }

    fun onSectorFilterSelected(sector: SectorFilter) {
        _sectorFilter.value = sector
    }

    fun enterSelectionMode(initialRegNo: String? = null) {
        _isSelectionMode.value = true
        _selectedRegNos.value = if (initialRegNo != null) setOf(initialRegNo) else emptySet()
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedRegNos.value = emptySet()
    }

    fun toggleSelect(regNo: String) {
        if (!_isSelectionMode.value) {
            _isSelectionMode.value = true
            _selectedRegNos.value = setOf(regNo)
            return
        }
        val current = _selectedRegNos.value.toMutableSet()
        if (current.contains(regNo)) {
            current.remove(regNo)
        } else {
            current.add(regNo)
        }
        _selectedRegNos.value = current
    }

    fun selectAll(visibleRegNos: List<String>) {
        _isSelectionMode.value = true
        _selectedRegNos.value = visibleRegNos.toSet()
    }

    fun deselectAll() {
        _selectedRegNos.value = emptySet()
    }

    fun toggleSelectAll(visibleRegNos: List<String>) {
        val current = _selectedRegNos.value
        val allSelected = visibleRegNos.isNotEmpty() && current.containsAll(visibleRegNos)
        if (allSelected) {
            deselectAll()
        } else {
            selectAll(visibleRegNos)
        }
    }

    fun deleteSelected(onComplete: () -> Unit = {}) {
        val targets = _selectedRegNos.value.toList()
        if (targets.isEmpty()) return
        viewModelScope.launch {
            repository.softDeleteVehicles(targets)
            exitSelectionMode()
            onComplete()
        }
    }
}
