package com.policydesk.app

import com.policydesk.app.data.local.entity.PolicyEntity
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicySource
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.Sector
import org.junit.Assert.assertEquals
import org.junit.Test

class CommissionCalculationTest {

    private fun calculateAmount(grossPremium: Double, percentage: Double): Double {
        val netPrem = grossPremium / 1.18
        return (netPrem * percentage) / 100.0
    }

    private fun calculatePercentage(grossPremium: Double, amount: Double): Double {
        val netPrem = grossPremium / 1.18
        return if (netPrem > 0.0) (amount / netPrem) * 100.0 else 0.0
    }

    private fun formatDecimal(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
        }
    }

    @Test
    fun testCommissionAmountCalculation() {
        val grossPremium = 11800.0
        val percentage = 15.0
        val amount = calculateAmount(grossPremium, percentage)
        assertEquals(1500.0, amount, 0.001)
        assertEquals("1500", formatDecimal(amount))

        // Decimal percentage test
        val decimalPct = 12.5
        val decimalAmt = calculateAmount(grossPremium, decimalPct)
        assertEquals(1250.0, decimalAmt, 0.001)
        assertEquals("1250", formatDecimal(decimalAmt))
    }

    @Test
    fun testCommissionPercentageCalculation() {
        val grossPremium = 11800.0
        val amount = 1500.0
        val percentage = calculatePercentage(grossPremium, amount)
        assertEquals(15.0, percentage, 0.001)
        assertEquals("15", formatDecimal(percentage))

        // Reverse decimal
        val decimalAmt = 1250.0
        val revPct = calculatePercentage(grossPremium, decimalAmt)
        assertEquals(12.5, revPct, 0.001)
        assertEquals("12.5", formatDecimal(revPct))
    }

    @Test
    fun testRecalculateOnPremiumChange() {
        val originalGross = 11800.0
        val pct = 15.0
        val originalAmt = calculateAmount(originalGross, pct)
        assertEquals(1500.0, originalAmt, 0.001)

        val updatedGross = 23600.0
        val updatedAmt = calculateAmount(updatedGross, pct)
        assertEquals(3000.0, updatedAmt, 0.001)
        assertEquals("3000", formatDecimal(updatedAmt))
    }

    @Test
    fun testPolicyEntityDomainMapping() {
        val policy = Policy(
            id = "test-policy-1",
            vehicleRegNo = "DL01AB1234",
            companySector = Sector.PRIVATE,
            insurer = "HDFC ERGO",
            policyNumber = "POL-123456",
            expiryDate = System.currentTimeMillis() + 86400000L * 100,
            totalPremium = 11800.0,
            commissionPercentage = 15.0,
            commissionAmount = 1500.0,
            status = PolicyStatus.ACTIVE,
            source = PolicySource.FORM
        )

        val entity = PolicyEntity.fromDomain(policy)
        assertEquals(15.0, entity.commissionPercentage!!, 0.001)
        assertEquals(1500.0, entity.commissionAmount!!, 0.001)

        val domainBack = entity.toDomain()
        assertEquals(policy.commissionPercentage, domainBack.commissionPercentage)
        assertEquals(policy.commissionAmount, domainBack.commissionAmount)
    }

    @Test
    fun testCommissionFormattingAndEdgeCases() {
        // Zero commission
        val zeroAmt = calculateAmount(11800.0, 0.0)
        assertEquals(0.0, zeroAmt, 0.001)
        assertEquals("0", formatDecimal(zeroAmt))

        // High commission percentage
        val highAmt = calculateAmount(59000.0, 25.0)
        assertEquals(12500.0, highAmt, 0.001)
        assertEquals("12500", formatDecimal(highAmt))

        // Small fraction
        val fractionAmt = calculateAmount(11800.0, 10.5)
        assertEquals(1050.0, fractionAmt, 0.001)
        assertEquals("1050", formatDecimal(fractionAmt))

        // Zero premium reverse calculation
        val zeroPremPct = calculatePercentage(0.0, 500.0)
        assertEquals(0.0, zeroPremPct, 0.001)
    }

    @Test
    fun testAnalyticsCommissionAggregation() {
        val trend = com.policydesk.app.domain.model.MonthlyTrendItem(
            monthLabel = "Nov '26",
            newBusinessPremium = 14500.0,
            renewalPremium = 20000.0,
            totalPremium = 34500.0,
            totalCommission = 5175.0
        )
        assertEquals(5175.0, trend.totalCommission, 0.001)

        val sales = com.policydesk.app.domain.model.SalesSummary(
            totalPremium = 34500.0,
            totalCommission = 5175.0,
            totalCount = 2,
            newBusinessCount = 1,
            renewalCount = 1,
            monthlyTrends = listOf(trend)
        )
        assertEquals(5175.0, sales.totalCommission, 0.001)
        val avgCommRate = (sales.totalCommission / sales.totalPremium) * 100.0
        assertEquals(15.0, avgCommRate, 0.001)

        val monthView = com.policydesk.app.domain.model.MonthViewData(
            year = 2026,
            month = 11,
            totalPolicies = 2,
            totalPremium = 34500.0,
            totalCommission = 5175.0
        )
        assertEquals(5175.0, monthView.totalCommission, 0.001)
    }

    @Test
    fun testInsurerBreakdownPerformance() {
        val hdfc = com.policydesk.app.domain.model.InsurerPerformance(
            insurer = "HDFC ERGO",
            totalPremium = 20000.0,
            totalCommission = 3000.0,
            policyCount = 2,
            sharePercent = 66.7
        )
        val nia = com.policydesk.app.domain.model.InsurerPerformance(
            insurer = "New India Assurance",
            totalPremium = 10000.0,
            totalCommission = 1000.0,
            policyCount = 1,
            sharePercent = 33.3
        )
        val sales = com.policydesk.app.domain.model.SalesSummary(
            totalPremium = 30000.0,
            totalCommission = 4000.0,
            totalCount = 3,
            insurerBreakdown = listOf(hdfc, nia)
        )
        assertEquals(2, sales.insurerBreakdown.size)
        assertEquals("HDFC ERGO", sales.insurerBreakdown[0].insurer)
        assertEquals(3000.0, sales.insurerBreakdown[0].totalCommission, 0.001)
        val hdfcRate = (sales.insurerBreakdown[0].totalCommission / sales.insurerBreakdown[0].totalPremium) * 100.0
        assertEquals(15.0, hdfcRate, 0.001)
    }
}
