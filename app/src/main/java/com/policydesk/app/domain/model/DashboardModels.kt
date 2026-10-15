package com.policydesk.app.domain.model

data class DashboardSummary(
    val overdueCount: Int = 0,
    val next7DaysCount: Int = 0,
    val next30DaysCount: Int = 0,
    val totalActiveCount: Int = 0,
    val soonestExpiring: List<VehicleWithPolicies> = emptyList()
)

data class MonthViewData(
    val year: Int,
    val month: Int,
    val totalPolicies: Int = 0,
    val totalPremium: Double = 0.0,
    val dateGroupedPolicies: Map<Int, List<VehicleWithPolicies>> = emptyMap()
)
