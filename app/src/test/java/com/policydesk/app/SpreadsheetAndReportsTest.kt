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
        assertEquals(expected, SpreadsheetParser.parseDateToEpochMs("28-Nov-2026"))
        assertEquals(expected, SpreadsheetParser.parseDateToEpochMs("28 Nov 2026"))
        assertEquals(expected, SpreadsheetParser.parseDateToEpochMs("28/11/2026 23:59:59"))
    }

    @Test
    fun parsePremium_cleans_currency_and_symbols() {
        assertEquals(14500.0, SpreadsheetParser.parsePremium("14500") ?: 0.0, 0.001)
        assertEquals(14500.0, SpreadsheetParser.parsePremium("14,500") ?: 0.0, 0.001)
        assertEquals(14500.0, SpreadsheetParser.parsePremium("₹ 14,500.00/-") ?: 0.0, 0.001)
        assertEquals(14500.0, SpreadsheetParser.parsePremium("Rs. 14,500") ?: 0.0, 0.001)
        assertEquals(14500.0, SpreadsheetParser.parsePremium("INR 14500/-") ?: 0.0, 0.001)
        assertEquals(14500.0, SpreadsheetParser.parsePremium("₹\u00A014,500.00") ?: 0.0, 0.001)
        assertEquals(14500.0, SpreadsheetParser.parsePremium("Rs.\u202F14500/=") ?: 0.0, 0.001)
    }

    @Test
    fun formatCurrency_formats_indian_numbering_system() {
        assertEquals("₹0", com.policydesk.app.ui.components.UiFormatters.formatCurrency(0.0))
        assertEquals("₹500", com.policydesk.app.ui.components.UiFormatters.formatCurrency(500.0))
        assertEquals("₹15,000", com.policydesk.app.ui.components.UiFormatters.formatCurrency(15000.0))
        assertEquals("₹1,50,000", com.policydesk.app.ui.components.UiFormatters.formatCurrency(150000.0))
        assertEquals("₹12,34,567", com.policydesk.app.ui.components.UiFormatters.formatCurrency(1234567.0))
        assertEquals("-₹15,000", com.policydesk.app.ui.components.UiFormatters.formatCurrency(-15000.0))
    }

    @Test
    fun autoMapHeaders_does_not_map_office_code_to_premium() {
        val headers = listOf("SR NO", "OFFICE CODE", "REGISTRATION NO", "INSURED NAME", "MOBILE", "NET PREMIUM")
        val sampleRows = listOf(
            listOf("1", "190000", "UP94AT2306", "NABAV ENTERPRISES", "9876543210", "45000"),
            listOf("2", "190000", "UP84H3613", "SHAHAJAD KHAN", "9876543211", "18000")
        )
        val mapping = com.policydesk.app.ui.importer.ImportViewModel.autoMapHeaders(headers, sampleRows)

        // OFFICE CODE (index 1) must never be mapped to totalPremium
        assertTrue(mapping[1] != "totalPremium")
        // NET PREMIUM (index 5) must be mapped to totalPremium
        assertEquals("totalPremium", mapping[5])
        // REGISTRATION NO (index 2) must be mapped to regNo
        assertEquals("regNo", mapping[2])
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
    fun insurerCatalog_contains_all_major_psu_and_private_and_matches_aliases() {
        val psuList = com.policydesk.app.domain.model.InsurerCatalog.PSU_INSURERS
        assertTrue(psuList.contains("National Insurance"))
        assertTrue(psuList.contains("New India Assurance"))
        assertTrue(psuList.contains("Oriental Insurance"))
        assertTrue(psuList.contains("United India Insurance"))
        assertEquals("Other", psuList.last())

        val pvtList = com.policydesk.app.domain.model.InsurerCatalog.PRIVATE_INSURERS
        assertTrue(pvtList.contains("Acko General Insurance"))
        assertTrue(pvtList.contains("Bajaj Allianz General Insurance"))
        assertTrue(pvtList.contains("Cholamandalam MS General Insurance"))
        assertTrue(pvtList.contains("Go Digit General Insurance"))
        assertTrue(pvtList.contains("HDFC ERGO General Insurance"))
        assertTrue(pvtList.contains("ICICI Lombard General Insurance"))
        assertTrue(pvtList.contains("IFFCO Tokio General Insurance"))
        assertTrue(pvtList.contains("Reliance General Insurance"))
        assertTrue(pvtList.contains("SBI General Insurance"))
        assertTrue(pvtList.contains("Tata AIG General Insurance"))
        assertTrue(pvtList.contains("Universal Sompo General Insurance"))
        assertTrue(pvtList.contains("Zuno General Insurance"))
        assertTrue(pvtList.contains("Zurich Kotak General Insurance"))
        assertEquals("Other", pvtList.last())

        // Test alias & abbreviation matching
        assertEquals(Pair(Sector.PSU, "National Insurance"), com.policydesk.app.domain.model.InsurerCatalog.matchInsurer("NIC"))
        assertEquals(Pair(Sector.PSU, "New India Assurance"), com.policydesk.app.domain.model.InsurerCatalog.matchInsurer("The New India Assurance Co. Ltd."))
        assertEquals(Pair(Sector.PSU, "United India Insurance"), com.policydesk.app.domain.model.InsurerCatalog.matchInsurer("UIIC"))
        assertEquals(Pair(Sector.PSU, "Oriental Insurance"), com.policydesk.app.domain.model.InsurerCatalog.matchInsurer("OICL"))
        assertEquals(Pair(Sector.PRIVATE, "Go Digit General Insurance"), com.policydesk.app.domain.model.InsurerCatalog.matchInsurer("Digit"))
        assertEquals(Pair(Sector.PRIVATE, "ICICI Lombard General Insurance"), com.policydesk.app.domain.model.InsurerCatalog.matchInsurer("Bharti AXA General Insurance"))
        assertEquals(Pair(Sector.PRIVATE, "Zuno General Insurance"), com.policydesk.app.domain.model.InsurerCatalog.matchInsurer("Edelweiss General Insurance"))
        assertEquals(Pair(Sector.PRIVATE, "Zurich Kotak General Insurance"), com.policydesk.app.domain.model.InsurerCatalog.matchInsurer("Zurich Kotak"))
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

    @Test
    fun validateAndMapRows_supports_custom_broker_headers_with_separate_make_model_and_tp_premium() {
        val rawRows = listOf(
            listOf(
                "POL/2026/7890",       // Policy Number
                "Rohan Sharma",        // Insured Name
                "15/10/2026",          // Policy Expiry Date
                "MA3EJA62S00123456",   // Chassis Number
                "K12M1234567",         // Engine Number
                "MH 12 AB 1234",       // Registration Number
                "₹ 3,450/-",           // TP Premium
                "HDFC ERGO",           // Company Name
                "+91 98765-43210",     // Mobile Number
                "Maruti Suzuki",       // Vehicle Make
                "Swift Dzire VXI"      // Vehicle Model
            )
        )

        val columnMapping = mapOf(
            0 to "policyNumber",
            1 to "name",
            2 to "expiryDate",
            3 to "vin",
            4 to "engineNo",
            5 to "regNo",
            6 to "tpPremium",
            7 to "insurer",
            8 to "mobile",
            9 to "make",
            10 to "model"
        )

        val result = SpreadsheetParser.validateAndMapRows(rawRows, columnMapping, emptySet())
        assertEquals(1, result.validRows.size)
        val row = result.validRows[0]
        assertEquals("POL/2026/7890", row.policyNumber)
        assertEquals("Rohan Sharma", row.customerName)
        assertEquals("MH12AB1234", row.regNo)
        assertEquals(3450.0, row.totalPremium ?: 0.0, 0.001)
        assertEquals("9876543210", row.mobile)
        assertEquals("Maruti Suzuki Swift Dzire VXI", row.makeModel)
        assertEquals("HDFC ERGO General Insurance", row.insurer)
        assertEquals(Sector.PRIVATE, row.sector)
    }
}
