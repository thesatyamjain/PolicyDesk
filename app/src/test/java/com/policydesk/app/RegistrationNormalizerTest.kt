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
        assertEquals("22 BH 1234 AA", RegistrationNormalizer.formatForDisplay("22BH1234AA"))
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
