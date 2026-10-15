package com.policydesk.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.domain.model.ReportPeriod
import com.policydesk.app.domain.model.ReportsAggregate
import com.policydesk.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class ReportsTab(val title: String) {
    SALES("Sales"),
    MISSED_RENEWALS("Missed Renewals"),
    ANALYTICS("Analytics")
}

enum class OverdueFilter(val label: String) {
    ALL("All Overdue"),
    UP_TO_30("≤ 30 Days"),
    FROM_31_TO_90("31–90 Days"),
    OVER_90("> 90 Days")
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: VehicleRepository
) : ViewModel() {

    private val now = LocalDate.now()
    private val _selectedPeriod = MutableStateFlow<ReportPeriod>(ReportPeriod.Month(now.year, now.monthValue))
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _selectedTab = MutableStateFlow(ReportsTab.SALES)
    val selectedTab: StateFlow<ReportsTab> = _selectedTab.asStateFlow()

    private val _overdueFilter = MutableStateFlow(OverdueFilter.ALL)
    val overdueFilter: StateFlow<OverdueFilter> = _overdueFilter.asStateFlow()

    val aggregateData: StateFlow<ReportsAggregate?> = _selectedPeriod.flatMapLatest { period ->
        repository.getReportsAggregate(period)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun onTabSelected(tab: ReportsTab) {
        _selectedTab.value = tab
    }

    fun onPeriodSelected(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    fun onOverdueFilterSelected(filter: OverdueFilter) {
        _overdueFilter.value = filter
    }

    fun closeVehicle(regNo: String, reason: String) {
        viewModelScope.launch {
            repository.closeVehicle(regNo, reason)
        }
    }
}
