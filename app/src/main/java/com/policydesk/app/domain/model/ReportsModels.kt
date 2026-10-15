package com.policydesk.app.domain.model

import java.time.LocalDate
import java.time.ZoneId

sealed class ReportPeriod(val label: String) {
    data class Month(val year: Int, val month: Int) : ReportPeriod("${LocalDate.of(year, month, 1).month.name} $year")
    data class Quarter(val year: Int, val quarter: Int) : ReportPeriod("Q$quarter $year")
    data class FinancialYear(val startYear: Int) : ReportPeriod("FY $startYear–${(startYear + 1) % 100}")
    data class Custom(val startMillis: Long, val endMillis: Long) : ReportPeriod("Custom Range")

    fun getStartAndEndMillis(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        return when (this) {
            is Month -> {
                val start = LocalDate.of(year, month, 1).atStartOfDay(zone).toInstant().toEpochMilli()
                val end = LocalDate.of(year, month, 1).plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
                Pair(start, end)
            }
            is Quarter -> {
                val startMonth = (quarter - 1) * 3 + 1
                val start = LocalDate.of(year, startMonth, 1).atStartOfDay(zone).toInstant().toEpochMilli()
                val end = LocalDate.of(year, startMonth, 1).plusMonths(3).atStartOfDay(zone).toInstant().toEpochMilli() - 1
                Pair(start, end)
            }
            is FinancialYear -> {
                // Indian Financial Year: April 1 to March 31
                val start = LocalDate.of(startYear, 4, 1).atStartOfDay(zone).toInstant().toEpochMilli()
                val end = LocalDate.of(startYear + 1, 4, 1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
                Pair(start, end)
            }
            is Custom -> Pair(startMillis, endMillis)
        }
    }
}

data class MonthlyTrendItem(
    val monthLabel: String,
    val newBusinessPremium: Double = 0.0,
    val renewalPremium: Double = 0.0,
    val totalPremium: Double = 0.0
)

data class SalesSummary(
    val totalPremium: Double = 0.0,
    val totalCount: Int = 0,
    val newBusinessCount: Int = 0,
    val renewalCount: Int = 0,
    val splitByInsurer: Map<String, Double> = emptyMap(),
    val splitBySector: Map<Sector, Double> = emptyMap(),
    val soldPolicies: List<VehicleWithPolicies> = emptyList(),
    val monthlyTrends: List<MonthlyTrendItem> = emptyList()
)

data class MissedRenewalsSummary(
    val totalCount: Int = 0,
    val premiumAtRisk: Double = 0.0,
    val upTo30Days: List<VehicleWithPolicies> = emptyList(),
    val from31To90Days: List<VehicleWithPolicies> = emptyList(),
    val over90Days: List<VehicleWithPolicies> = emptyList(),
    val allMissed: List<VehicleWithPolicies> = emptyList()
)

data class RenewalPerformance(
    val policiesDue: Int = 0,
    val renewedOnTime: Int = 0,
    val renewedLate: Int = 0,
    val missed: Int = 0,
    val renewalRatePercent: Double = 0.0,
    val onTimeRatePercent: Double = 0.0
)

data class AnalyticsSummary(
    val premiumSold: Double = 0.0,
    val policiesSold: Int = 0,
    val renewalRatePercent: Double = 0.0,
    val onTimeRatePercent: Double = 0.0,
    val premiumAtRisk: Double = 0.0,
    val renewalPipeline3Months: Pair<Int, Double> = Pair(0, 0.0),
    val renewalPipeline6Months: Pair<Int, Double> = Pair(0, 0.0),
    val renewalPipeline12Months: Pair<Int, Double> = Pair(0, 0.0),
    val bookSizeVehicles: Int = 0,
    val bookSizePremium: Double = 0.0,
    val insurerMixShare: Map<String, Double> = emptyMap()
)

data class ReportsAggregate(
    val period: ReportPeriod,
    val sales: SalesSummary,
    val missed: MissedRenewalsSummary,
    val performance: RenewalPerformance,
    val analytics: AnalyticsSummary
)
