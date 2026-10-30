package com.policydesk.app

import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.SaleType
import com.policydesk.app.domain.model.Sector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class RegistrationNormalizerTest {

    @Test
    fun normalize_strips_spaces_hyphens_and_dots_and_uppercases() {
        assertEquals("MH12AB1234", RegistrationNormalizer.normalize("mh 12-ab 1234"))
        assertEquals("DL01CA9999", RegistrationNormalizer.normalize("dl-01.ca 9999"))
        assertEquals("22BH1234AA", RegistrationNormalizer.normalize("22 bh-1234.aa"))
        assertEquals("MH12AB1234", RegistrationNormalizer.normalize("MH-12/AB_1234"))
        assertEquals("KA05MB4321", RegistrationNormalizer.normalize("ka   05  -- mb .. 4321"))
    }

    @Test
    fun extractRegistrationNumber_handles_all_abbreviations_and_psu_labels() {
        assertEquals("RJ14AB1234", RegistrationNormalizer.extractRegistrationNumber("पंजी. संख्या / Regn. Number: RJ14AB1234"))
        assertEquals("UP16CD5678", RegistrationNormalizer.extractRegistrationNumber("विवरण / Vehicle Details Regn No UP 16 CD 5678"))
        assertEquals("22BH1234AA", RegistrationNormalizer.extractRegistrationNumber("पंजीकरण संख्या 22 BH 1234 AA"))
        assertEquals("DL01A9999", RegistrationNormalizer.extractRegistrationNumber("Regn. Number DL-01-A-9999"))
        assertEquals("MH12AB1234", RegistrationNormalizer.extractRegistrationNumber("MH12AB1234"))
        assertEquals("MH12DE4321", RegistrationNormalizer.extractRegistrationNumber("वाहन पंजी सं: MH-12-DE-4321"))
        assertEquals("DL04C1111", RegistrationNormalizer.extractRegistrationNumber("गाड़ी नं: DL 04 C 1111"))
        assertEquals("HR26DK8888", RegistrationNormalizer.extractRegistrationNumber("गाड़ी नंबर - HR 26 DK 8888"))
        assertEquals("KA01MJ9090", RegistrationNormalizer.extractRegistrationNumber("RC NO: KA 01 MJ 9090"))
        assertEquals("GJ01AA1234", RegistrationNormalizer.extractRegistrationNumber("Plate No. GJ 01 AA 1234"))
        assertEquals("TN09BB5555", RegistrationNormalizer.extractRegistrationNumber("Veh Regn No: TN 09 BB 5555"))
        assertEquals("WB02AC7777", RegistrationNormalizer.extractRegistrationNumber("रजिस्ट्रेशन नं: WB 02 AC 7777"))
    }

    @Test
    fun standard_format_validation() {
        assertTrue(RegistrationNormalizer.isStandardFormat("MH12AB1234"))
        assertTrue(RegistrationNormalizer.isStandardFormat("DL1A1234"))
        assertTrue(RegistrationNormalizer.isStandardFormat("22BH1234AA"))
        assertFalse(RegistrationNormalizer.isStandardFormat("INVALID12345678"))
    }

    @Test
    fun repairOcrMisreads_fixes_common_ocr_confusion() {
        assertEquals("MH12AB1230", RegistrationNormalizer.repairOcrMisreads("MH12AB123O"))
        assertEquals("DL01CA9991", RegistrationNormalizer.repairOcrMisreads("DL01CA999I"))
        assertEquals("KA05MB4325", RegistrationNormalizer.repairOcrMisreads("KA05MB432S"))
        assertEquals("MH12AB1234", RegistrationNormalizer.repairOcrMisreads("MH12AB1234"))
    }

    @Test
    fun formatForDisplay_formats_cleanly() {
        assertEquals("MH 12 AB 1234", RegistrationNormalizer.formatForDisplay("MH12AB1234"))
        assertEquals("MH 77 HH 7777", RegistrationNormalizer.formatForDisplay("MH77HH7777"))
        assertEquals("22 BH 1234 AA", RegistrationNormalizer.formatForDisplay("22BH1234AA"))
        assertEquals("DL 1 A 9999", RegistrationNormalizer.formatForDisplay("DL1A9999"))
        assertEquals("KA 05 1234", RegistrationNormalizer.formatForDisplay("KA051234"))
        assertEquals("MH 77", RegistrationNormalizer.formatForDisplay("MH77"))
        assertEquals("MH 77 H", RegistrationNormalizer.formatForDisplay("MH77H"))
    }

    @Test
    fun fieldValidators_mobile_and_name_and_policy() {
        assertTrue(FieldValidators.isValidMobile("9876543210"))
        assertTrue(FieldValidators.isValidMobile("+919876543210"))
        assertFalse(FieldValidators.isValidMobile("1234567890")) // doesn't start with 6-9
        assertFalse(FieldValidators.isValidMobile("98765"))      // too short

        assertEquals("9876543210", FieldValidators.normalizeMobile("+919876543210"))
        assertEquals("9876543210", FieldValidators.normalizeMobile("9876543210"))
        assertEquals("9876543210", FieldValidators.normalizeMobile("+91-98765-43210"))
        assertEquals("9876543210", FieldValidators.normalizeMobile("09876543210"))
        assertEquals("9876543210", FieldValidators.normalizeMobile("9876543210.0"))
        assertTrue(FieldValidators.isValidMobile("9876543210.0"))
        assertTrue(FieldValidators.isValidMobile("+91-98765-43210"))

        assertTrue(FieldValidators.isValidName("Rajesh Sharma"))
        assertFalse(FieldValidators.isValidName("R"))

        assertTrue(FieldValidators.isValidPolicyNumber("POL/2026/001-A"))
        assertTrue(FieldValidators.isValidPolicyNumber("1001.2024.1"))
        assertTrue(FieldValidators.isValidPolicyNumber("OG_23_1001"))
        assertTrue(FieldValidators.isValidPolicyNumber("POL 12345"))
        assertFalse(FieldValidators.isValidPolicyNumber("POL@#$123"))

        assertTrue(FieldValidators.isVinStandardLength("1HGCR2F83HA000000"))
        assertFalse(FieldValidators.isVinStandardLength("SHORTVIN123"))
    }

    @Test
    fun content_awareness_field_sanitizers_for_all_fields() {
        // Customer Name & Title with Hindi and English honorifics & abbreviations
        val (name1, title1) = FieldValidators.extractCustomerNameAndTitle("Shri Rajesh Kumar")
        assertEquals("Rajesh Kumar", name1)
        assertEquals("Shri", title1)

        val (name2, title2) = FieldValidators.extractCustomerNameAndTitle("बीमित का नाम: Mr. Ramesh Patel")
        assertEquals("Ramesh Patel", name2)
        assertEquals("Mr", title2)

        val (name3, title3) = FieldValidators.extractCustomerNameAndTitle("M/s Apex Logistics PVT LTD")
        assertEquals("Apex Logistics PVT LTD", name3)
        assertEquals("M/s", title3)

        val (name4, title4) = FieldValidators.extractCustomerNameAndTitle("श्री सुरेश कुमार शर्मा")
        assertEquals("सुरेश कुमार शर्मा", name4)
        assertEquals("Shri", title4)

        val (name5, title5) = FieldValidators.extractCustomerNameAndTitle("बीमाधारक का नाम: श्रीमती अनीता देवी")
        assertEquals("अनीता देवी", name5)
        assertEquals("Smt.", title5)

        val (name6, title6) = FieldValidators.extractCustomerNameAndTitle("Cust Name: Dr. Vikram Sarabhai")
        assertEquals("Vikram Sarabhai", name6)
        assertEquals("Dr", title6)

        val (name7, title7) = FieldValidators.extractCustomerNameAndTitle("वाहन स्वामी: Adv. Ashok Singhal")
        assertEquals("Ashok Singhal", name7)
        assertEquals("Adv", title7)

        val (name8, title8) = FieldValidators.extractCustomerNameAndTitle("Proposer Name: Capt. Amarjeet Singh")
        assertEquals("Amarjeet Singh", name8)
        assertEquals("Capt", title8)

        // Email
        assertEquals("rajesh@gmail.com", FieldValidators.extractCleanEmail("Email ID: rajesh@gmail.com;"))
        assertEquals("contact@company.co.in", FieldValidators.extractCleanEmail("ई-मेल: contact@company.co.in"))

        // Policy Number
        assertEquals("POL/2026/001-A", FieldValidators.extractCleanPolicyNumber("Policy No: POL/2026/001-A."))
        assertEquals("1001.2024.1", FieldValidators.extractCleanPolicyNumber("पॉलिसी संख्या: 1001.2024.1>>"))
        assertEquals("CN-998877", FieldValidators.extractCleanPolicyNumber("कवर नोट सं: CN-998877"))
        assertEquals("SCH/4567/89", FieldValidators.extractCleanPolicyNumber("अनुसूची सं: SCH/4567/89"))

        // Address
        assertEquals("Flat 402, Shanti Nagar, Pune 411001", FieldValidators.extractCleanAddress("Address: Flat 402,\nShanti Nagar,\tPune 411001"))
        assertEquals("ग्राम व पोस्ट रामपुर, जिला मेरठ 250001", FieldValidators.extractCleanAddress("पत्राचार का पता: ग्राम व पोस्ट रामपुर, जिला मेरठ 250001"))

        // VIN & Engine
        assertEquals("MA3EJA62S00123456", FieldValidators.extractCleanVin("Chassis No: MA3EJA62S00123456"))
        assertEquals("ME4JC3833E8012345", FieldValidators.extractCleanVin("चेसिस सं: ME4JC3833E8012345"))
        assertEquals("FR778899", FieldValidators.extractCleanVin("फ्रेम सं: FR-778899"))
        assertEquals("K12M1234567", FieldValidators.extractCleanEngineNo("इंजन संख्या: K12M-1234567"))
        assertEquals("ENG9988776", FieldValidators.extractCleanEngineNo("मोटर सं: ENG9988776"))

        // Make Model
        assertEquals("Maruti Suzuki Swift Dzire VXI", FieldValidators.extractCleanMakeModel("Make / Model: Maruti Suzuki Swift Dzire VXI"))
        assertEquals("Hyundai Creta SX Petrol", FieldValidators.extractCleanMakeModel("वाहन विवरण: Hyundai Creta SX Petrol"))
    }

    @Test
    fun insurer_catalog_matches_all_irdai_acronyms_and_sectors() {
        val testCases = mapOf(
            "nic" to Pair(Sector.PSU, "National Insurance"),
            "nicl" to Pair(Sector.PSU, "National Insurance"),
            "nia" to Pair(Sector.PSU, "New India Assurance"),
            "niacl" to Pair(Sector.PSU, "New India Assurance"),
            "oic" to Pair(Sector.PSU, "Oriental Insurance"),
            "oicl" to Pair(Sector.PSU, "Oriental Insurance"),
            "uiic" to Pair(Sector.PSU, "United India Insurance"),
            "uiicl" to Pair(Sector.PSU, "United India Insurance"),
            "bagic" to Pair(Sector.PRIVATE, "Bajaj Allianz General Insurance"),
            "ilgic" to Pair(Sector.PRIVATE, "ICICI Lombard General Insurance"),
            "bhartiaxa" to Pair(Sector.PRIVATE, "ICICI Lombard General Insurance"),
            "itgi" to Pair(Sector.PRIVATE, "IFFCO Tokio General Insurance"),
            "tagic" to Pair(Sector.PRIVATE, "Tata AIG General Insurance"),
            "godigit" to Pair(Sector.PRIVATE, "Go Digit General Insurance"),
            "rgicl" to Pair(Sector.PRIVATE, "Reliance General Insurance"),
            "rsgi" to Pair(Sector.PRIVATE, "Royal Sundaram General Insurance"),
            "sbig" to Pair(Sector.PRIVATE, "SBI General Insurance"),
            "sgic" to Pair(Sector.PRIVATE, "Shriram General Insurance"),
            "usgic" to Pair(Sector.PRIVATE, "Universal Sompo General Insurance"),
            "fgii" to Pair(Sector.PRIVATE, "Future Generali India Insurance"),
            "cholams" to Pair(Sector.PRIVATE, "Cholamandalam MS General Insurance")
        )

        for ((acronym, expected) in testCases) {
            val matched = com.policydesk.app.domain.model.InsurerCatalog.matchInsurer(acronym)
            assertEquals("Failed matching $acronym", expected, matched)
        }
    }

    @Test
    fun policy_countdown_and_status_computation() {
        val today = LocalDate.of(2026, 10, 14)
        val todayEpochDay = today.toEpochDay()
        val zone = ZoneId.systemDefault()

        // 1. Expired 5 days ago -> OVERDUE
        val expiredPolicy = Policy(
            vehicleRegNo = "MH12AB1234",
            companySector = Sector.PSU,
            insurer = "National Insurance",
            policyNumber = "POL1",
            expiryDate = today.minusDays(5).atStartOfDay(zone).toInstant().toEpochMilli(),
            totalPremium = 10000.0
        )
        assertEquals(-5L, expiredPolicy.computeDaysRemaining(todayEpochDay))
        assertEquals(PolicyStatus.OVERDUE, expiredPolicy.computeCurrentStatus(todayEpochDay))

        // 2. Expiring in 10 days -> EXPIRING_SOON
        val expiringSoonPolicy = Policy(
            vehicleRegNo = "MH12AB1234",
            companySector = Sector.PRIVATE,
            insurer = "HDFC ERGO",
            policyNumber = "POL2",
            expiryDate = today.plusDays(10).atStartOfDay(zone).toInstant().toEpochMilli(),
            totalPremium = 12000.0
        )
        assertEquals(10L, expiringSoonPolicy.computeDaysRemaining(todayEpochDay))
        assertEquals(PolicyStatus.EXPIRING_SOON, expiringSoonPolicy.computeCurrentStatus(todayEpochDay))

        // 3. Expiring in 90 days -> ACTIVE
        val activePolicy = Policy(
            vehicleRegNo = "MH12AB1234",
            companySector = Sector.PRIVATE,
            insurer = "ICICI Lombard",
            policyNumber = "POL3",
            expiryDate = today.plusDays(90).atStartOfDay(zone).toInstant().toEpochMilli(),
            totalPremium = 15000.0
        )
        assertEquals(90L, activePolicy.computeDaysRemaining(todayEpochDay))
        assertEquals(PolicyStatus.ACTIVE, activePolicy.computeCurrentStatus(todayEpochDay))

        // 4. Closed with reason -> CLOSED
        val closedPolicy = activePolicy.copy(closeReason = "Vehicle sold")
        assertEquals(PolicyStatus.CLOSED, closedPolicy.computeCurrentStatus(todayEpochDay))

        // 5. Renewed on timestamp -> RENEWED
        val renewedPolicy = activePolicy.copy(renewedOn = System.currentTimeMillis())
        assertEquals(PolicyStatus.RENEWED, renewedPolicy.computeCurrentStatus(todayEpochDay))
    }
}
