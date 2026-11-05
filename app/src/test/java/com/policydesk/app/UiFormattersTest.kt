package com.policydesk.app

import com.policydesk.app.ui.components.UiFormatters
import org.junit.Assert.assertEquals
import org.junit.Test

class UiFormattersTest {

    @Test
    fun testCompoundDurationDetailed() {
        // <= 30 days
        assertEquals("15 days", UiFormatters.formatCompoundDurationDetailed(15))
        assertEquals("1 day", UiFormatters.formatCompoundDurationDetailed(1))

        // Compound Months + Days
        // 31 days = 1 month, 1 day
        assertEquals("1 month, 1 day", UiFormatters.formatCompoundDurationDetailed(31))
        // 35 days = 1 month, 5 days
        assertEquals("1 month, 5 days", UiFormatters.formatCompoundDurationDetailed(35))
        // 60 days = 2 months
        assertEquals("2 months", UiFormatters.formatCompoundDurationDetailed(60))
        // 118 days = 3 months, 28 days
        assertEquals("3 months, 28 days", UiFormatters.formatCompoundDurationDetailed(118))
        // 120 days = 4 months
        assertEquals("4 months", UiFormatters.formatCompoundDurationDetailed(120))

        // Years
        // 365 days = 1 year
        assertEquals("1 year", UiFormatters.formatCompoundDurationDetailed(365))
        // 395 days = 1 year, 1 month
        assertEquals("1 year, 1 month", UiFormatters.formatCompoundDurationDetailed(395))
        // 425 days = 1 year, 2 months
        assertEquals("1 year, 2 months", UiFormatters.formatCompoundDurationDetailed(425))
    }

    @Test
    fun testCompoundDurationCompact() {
        // <= 30 days
        assertEquals("15d", UiFormatters.formatCompoundDurationCompact(15))

        // Compact months + days for badges
        // 31 days -> 1m 1d
        assertEquals("1m 1d", UiFormatters.formatCompoundDurationCompact(31))
        // 35 days -> 1m 5d
        assertEquals("1m 5d", UiFormatters.formatCompoundDurationCompact(35))
        // 60 days -> 2m
        assertEquals("2m", UiFormatters.formatCompoundDurationCompact(60))
        // 118 days -> 3m 28d
        assertEquals("3m 28d", UiFormatters.formatCompoundDurationCompact(118))
        // 120 days -> 4m
        assertEquals("4m", UiFormatters.formatCompoundDurationCompact(120))

        // Compact years
        assertEquals("1y", UiFormatters.formatCompoundDurationCompact(365))
        assertEquals("1y 1m", UiFormatters.formatCompoundDurationCompact(395))
        assertEquals("1y 2m", UiFormatters.formatCompoundDurationCompact(425))
    }

    @Test
    fun testCurrencyFormatting() {
        assertEquals("₹0", UiFormatters.formatCurrency(0.0))
        assertEquals("₹500", UiFormatters.formatCurrency(500.0))
        assertEquals("₹1,250", UiFormatters.formatCurrency(1250.0))
        assertEquals("₹24,500", UiFormatters.formatCurrency(24500.0))
        assertEquals("₹1,25,000", UiFormatters.formatCurrency(125000.0))
    }
}
