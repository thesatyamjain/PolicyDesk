package com.policydesk.app

import com.policydesk.app.data.importer.SpreadsheetParser
import com.policydesk.app.domain.model.Customer
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicySource
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.ReportPeriod
import com.policydesk.app.domain.model.SaleType
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.domain.model.Vehicle
import com.policydesk.app.domain.model.VehicleWithPolicies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class SpreadsheetAndReportsTest {

    @Test
    fun parseDateToEpochMs_supports_multiple_formats() {
        val zone = ZoneId.systemDefault()
        val expected = LocalDate.of(2026, 11, 28).atStartOfDay(zone).toInstant().toEpochMilli()

        assertEquals(expected, SpreadsheetParser.parseDateToEpochMs("28/11/2026"))
        assertEquals(expected, SpreadsheetParser.parseDateToEpochMs("28-11-2026"))
        assertEquals(expected, SpreadsheetParser.parseDateToEpochMs("2026-11-28"))
    }

    @Test
    fun template_csv_generation_is_valid() {
        val template = SpreadsheetParser.generateCsvTemplate()
        assertTrue(template.contains("Registration Number"))
        assertTrue(template.contains("MH12AB1234"))
        assertTrue(template.contains("HDFC ERGO"))
    }

    @Test
    fun validateAndMapRows_categorizes_correctly() {
        val rawRows = listOf(
            // Row 1: Valid
            listOf("MH 12 AB 1234", "Amit Verma", "Shri", "9876543210", "amit@test.com", "Pune", "Private", "HDFC ERGO", "POL/101", "28/11/2026", "15000"),
            // Row 2: Duplicate
            listOf("DL 01 AA 9999", "Rohit Sen", "Shri", "9811122233", "rohit@test.com", "Delhi", "PSU", "New India Assurance", "POL/102", "15/12/2026", "12000"),
            // Row 3: Error (invalid mobile, missing address)
            listOf("KA 05 CD 5555", "Suresh", "", "123", "", "", "Private", "Tata AIG", "POL/103", "10/10/2026", "8000")
        )

        val columnMapping = mapOf(
            0 to "regNo",
            1 to "name",
            2 to "title",
            3 to "mobile",
            4 to "email",
            5 to "address",
            6 to "sector",
            7 to "insurer",
            8 to "policyNumber",
            9 to "expiryDate",
            10 to "totalPremium"
        )

        val existing = setOf("DL01AA9999")

        val result = SpreadsheetParser.validateAndMapRows(rawRows, columnMapping, existing)

        assertEquals(3, result.totalRows)
        assertEquals(1, result.validRows.size)
        assertEquals("MH12AB1234", result.validRows[0].regNo)

        assertEquals(1, result.duplicateRows.size)
        assertEquals("DL01AA9999", result.duplicateRows[0].regNo)

        assertEquals(1, result.errorRows.size)
        assertEquals(4, result.errorRows[0].rowNumber) // index 2 + header row 1 = row 4
    }

    @Test
    fun reportPeriod_calculates_correct_bounds() {
        val monthPeriod = ReportPeriod.Month(2026, 10)
        val (startMs, endMs) = monthPeriod.getStartAndEndMillis()
        val zone = ZoneId.systemDefault()

        val startDate = java.time.Instant.ofEpochMilli(startMs).atZone(zone).toLocalDate()
        assertEquals(2026, startDate.year)
        assertEquals(10, startDate.monthValue)
        assertEquals(1, startDate.dayOfMonth)

        val fy = ReportPeriod.FinancialYear(2026)
        val (fyStart, fyEnd) = fy.getStartAndEndMillis()
        val fyStartDate = java.time.Instant.ofEpochMilli(fyStart).atZone(zone).toLocalDate()
        assertEquals(4, fyStartDate.monthValue) // April
    }

    @Test
    fun duplicateStrategy_and_monthlyTrend_models_behave_as_expected() {
        val trend = com.policydesk.app.domain.model.MonthlyTrendItem(
            monthLabel = "Oct '26",
            newBusinessPremium = 15000.0,
            renewalPremium = 25000.0,
            totalPremium = 40000.0
        )
        assertEquals("Oct '26", trend.monthLabel)
        assertEquals(40000.0, trend.totalPremium, 0.001)

        val summary = com.policydesk.app.domain.model.SalesSummary(
            totalPremium = 40000.0,
            monthlyTrends = listOf(trend)
        )
        assertEquals(1, summary.monthlyTrends.size)
        assertEquals(25000.0, summary.monthlyTrends[0].renewalPremium, 0.001)
    }

    @Test
    fun insurerCatalog_integrates_custom_insurers_before_other() {
        val custom = listOf("Custom Shield Insurance", "HDFC ERGO General Insurance")
        val list = com.policydesk.app.domain.model.InsurerCatalog.getInsurersForSector(
            sector = Sector.PRIVATE,
            customInsurers = custom
        )
        // Must contain "Custom Shield Insurance"
        assertTrue(list.contains("Custom Shield Insurance"))
        // Last element must be "Other"
        assertEquals("Other", list.last())
        // "Custom Shield Insurance" must precede "Other"
        assertEquals(list.size - 2, list.indexOf("Custom Shield Insurance"))
        // No duplicate for existing standard insurer
        assertEquals(1, list.count { it == "HDFC ERGO General Insurance" })
    }

    @Test
    fun customerGrouping_aggregates_multiple_vehicles() {
        val c1 = Customer(name = "Ramesh Kumar", mobile = "9876543210", address = "Mumbai")
        val v1 = Vehicle(regNo = "MH01AB1111", customer = c1)
        val p1 = Policy(vehicleRegNo = "MH01AB1111", companySector = Sector.PRIVATE, insurer = "Digit", policyNumber = "POL1", expiryDate = System.currentTimeMillis() + 86400000L, totalPremium = 10000.0)
        
        val v2 = Vehicle(regNo = "MH01AB2222", customer = c1)
        val p2 = Policy(vehicleRegNo = "MH01AB2222", companySector = Sector.PSU, insurer = "New India", policyNumber = "POL2", expiryDate = System.currentTimeMillis() + 86400000L, totalPremium = 15000.0)

        val group = com.policydesk.app.domain.model.CustomerGroup(
            mobile = "9876543210",
            name = "Ramesh Kumar",
            title = null,
            address = "Mumbai",
            vehicles = listOf(
                VehicleWithPolicies(v1, listOf(p1)),
                VehicleWithPolicies(v2, listOf(p2))
            ),
            totalPolicies = 2,
            totalActivePremium = 25000.0
        )

        assertEquals("9876543210", group.mobile)
        assertEquals(2, group.vehicles.size)
        assertEquals(25000.0, group.totalActivePremium, 0.001)
    }
}
