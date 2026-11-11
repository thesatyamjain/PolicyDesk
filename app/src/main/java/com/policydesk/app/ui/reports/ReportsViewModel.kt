package com.policydesk.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.policydesk.app.domain.model.ReportPeriod
import com.policydesk.app.domain.model.ReportsAggregate
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

enum class ReportsTab(val title: String) {
    SALES("Sales"),
    MISSED_RENEWALS("Missed"),
    ANALYTICS("Analytics")
}

enum class OverdueFilter(val label: String) {
    ALL("All Overdue"),
    DUE_SOON("Due ≤ 7d"),
    UP_TO_30("≤ 30 Days"),
    FROM_31_TO_90("31–90 Days"),
    OVER_90("> 90 Days")
}

data class UrgentRiskSummary(
    val overdueCount: Int = 0,
    val dueSoonCount: Int = 0,
    val overduePolicies: List<VehicleWithPolicies> = emptyList(),
    val dueSoonPolicies: List<VehicleWithPolicies> = emptyList(),
    val overdueRiskPremium: Double = 0.0,
    val dueSoonRiskPremium: Double = 0.0
)

internal fun computeUrgentRiskSummary(
    vehicles: List<VehicleWithPolicies>,
    todayEpochDay: Long = LocalDate.now().toEpochDay()
): UrgentRiskSummary {
    val overdue = mutableListOf<VehicleWithPolicies>()
    val dueSoon = mutableListOf<VehicleWithPolicies>()
    var overduePrem = 0.0
    var dueSoonPrem = 0.0

    for (item in vehicles) {
        if (item.vehicle.isClosed) continue
        val policy = item.currentPolicy ?: continue
        if (!policy.closeReason.isNullOrBlank() || policy.renewedOn != null) continue

        val daysLeft = policy.computeDaysRemaining(todayEpochDay)
        when {
            daysLeft < 0L -> {
                overdue.add(item)
                overduePrem += policy.totalPremium
            }
            daysLeft in 0L..7L -> {
                dueSoon.add(item)
                dueSoonPrem += policy.totalPremium
            }
        }
    }

    return UrgentRiskSummary(
        overdueCount = overdue.size,
        dueSoonCount = dueSoon.size,
        overduePolicies = overdue.sortedBy { it.currentPolicy?.expiryDate ?: Long.MAX_VALUE },
        dueSoonPolicies = dueSoon.sortedBy { it.currentPolicy?.expiryDate ?: Long.MAX_VALUE },
        overdueRiskPremium = overduePrem,
        dueSoonRiskPremium = dueSoonPrem
    )
}

internal fun formatReportPeriodChipLabel(period: ReportPeriod): String {
    return when (period) {
        is ReportPeriod.Month -> {
            val monthShort = LocalDate.of(period.year, period.month, 1)
                .month
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            "$monthShort ${period.year}"
        }
        is ReportPeriod.Quarter -> {
            // Convert calendar quarter (1=Jan-Mar..4=Oct-Dec) to Indian Financial Year (Apr-Mar)
            val calQuarter = period.quarter.coerceIn(1, 4)
            val fyQuarter = if (calQuarter == 1) 4 else calQuarter - 1
            val fyEndYear = if (calQuarter == 1) period.year else period.year + 1
            "Q$fyQuarter FY${String.format(Locale.US, "%02d", fyEndYear % 100)}"
        }
        is ReportPeriod.FinancialYear -> {
            "FY ${period.startYear}-${String.format(Locale.US, "%02d", (period.startYear + 1) % 100)}"
        }
        is ReportPeriod.Custom -> period.label
    }
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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

    val urgentRiskSummary: StateFlow<UrgentRiskSummary> = repository.getAllVehicles()
        .map { vehicles -> computeUrgentRiskSummary(vehicles) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UrgentRiskSummary()
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
