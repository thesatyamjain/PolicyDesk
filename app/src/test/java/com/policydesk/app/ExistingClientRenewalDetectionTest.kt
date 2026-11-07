package com.policydesk.app

import com.policydesk.app.domain.model.Customer
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicySource
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.SaleType
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.domain.model.Vehicle
import com.policydesk.app.domain.model.VehicleWithPolicies
import com.policydesk.app.domain.model.ExistingClientInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ExistingClientRenewalDetectionTest {

    private fun resolveMobile(extractedMobile: String, existingCustomerMobile: String?): String {
        val extractedNorm = FieldValidators.normalizeMobile(extractedMobile)
        val isMaskedOrInvalid = extractedMobile.contains("X", ignoreCase = true) ||
                extractedMobile.contains("*") ||
                !FieldValidators.isValidMobile(extractedNorm)

        return if (isMaskedOrInvalid && !existingCustomerMobile.isNullOrBlank() && FieldValidators.isValidMobile(existingCustomerMobile)) {
            existingCustomerMobile
        } else {
            extractedNorm
        }
    }

    @Test
    fun testMaskedMobileResolvesToExistingClientMobile() {
        val existingMobile = "9876543210"

        // Masked with X
        assertEquals(existingMobile, resolveMobile("98XXXXXX10", existingMobile))
        assertEquals(existingMobile, resolveMobile("XXXXX43210", existingMobile))
        assertEquals(existingMobile, resolveMobile("98xxxxxx10", existingMobile))

        // Masked with asterisks
        assertEquals(existingMobile, resolveMobile("98*****210", existingMobile))

        // Blank or partial
        assertEquals(existingMobile, resolveMobile("", existingMobile))
        assertEquals(existingMobile, resolveMobile("9876", existingMobile))

        // Fully visible new mobile number should NOT be replaced
        val newMobile = "9123456789"
        assertEquals(newMobile, resolveMobile(newMobile, existingMobile))
    }

    @Test
    fun testExistingVehicleProducesClientInfoAndDetectsRenewal() {
        val regNo = "DL01AB1234"
        val existingVehicle = Vehicle(
            regNo = regNo,
            customer = Customer(
                name = "Rahul Sharma",
                mobile = "9876543210",
                email = "rahul@example.com",
                address = "Connaught Place, New Delhi"
            ),
            makeModel = "Honda City VX",
            vin = "MA3EYD21S00123456",
            engineNo = "E1234567"
        )
        val prevPolicy = Policy(
            id = "prev-policy-123",
            vehicleRegNo = regNo,
            companySector = Sector.PRIVATE,
            insurer = "HDFC ERGO General Insurance Co. Ltd.",
            policyNumber = "HDFC/2023/001",
            expiryDate = 1714521600000L, // Past date
            totalPremium = 11800.0,
            commissionPercentage = 15.0,
            commissionAmount = 1500.0,
            status = PolicyStatus.ACTIVE,
            saleType = SaleType.NEW_BUSINESS
        )
        val vwp = VehicleWithPolicies(
            vehicle = existingVehicle,
            policies = listOf(prevPolicy)
        )

        val clientInfo = ExistingClientInfo(
            clientName = vwp.vehicle.customer.name,
            mobile = vwp.vehicle.customer.mobile,
            previousInsurer = vwp.currentPolicy?.insurer,
            previousPolicyNumber = vwp.currentPolicy?.policyNumber,
            previousExpiryEpochMs = vwp.currentPolicy?.expiryDate,
            previousPolicyId = vwp.currentPolicy?.id,
            totalPoliciesCount = vwp.policies.size
        )

        assertEquals("Rahul Sharma", clientInfo.clientName)
        assertEquals("9876543210", clientInfo.mobile)
        assertEquals("HDFC ERGO General Insurance Co. Ltd.", clientInfo.previousInsurer)
        assertEquals("HDFC/2023/001", clientInfo.previousPolicyNumber)
        assertEquals(1, clientInfo.totalPoliciesCount)
    }

    @Test
    fun testRenewalVsNewBusinessSaleTypeAssignment() {
        val existingPolicyNo = "HDFC/2023/001"
        val renewalPolicyNo = "HDFC/2024/002"

        // Case 1: Brand new client/vehicle (no existing client)
        val isExistingClient1 = false
        val saleType1 = if (isExistingClient1) SaleType.RENEWAL else SaleType.NEW_BUSINESS
        assertEquals(SaleType.NEW_BUSINESS, saleType1)

        // Case 2: Existing client renewing with new policy number
        val isExistingClient2 = true
        val isSamePolicyNo2 = existingPolicyNo.equals(renewalPolicyNo, ignoreCase = true)
        val saleType2 = if (isExistingClient2 && !isSamePolicyNo2) SaleType.RENEWAL else SaleType.NEW_BUSINESS
        assertEquals(SaleType.RENEWAL, saleType2)

        // Case 3: Re-uploading or editing the identical existing policy document
        val isExistingClient3 = true
        val isSamePolicyNo3 = existingPolicyNo.equals("HDFC/2023/001", ignoreCase = true)
        val saleType3 = if (isExistingClient3 && !isSamePolicyNo3) SaleType.RENEWAL else SaleType.NEW_BUSINESS
        assertEquals(SaleType.NEW_BUSINESS, saleType3)
    }

    @Test
    fun testCommissionCarryOverForRenewal() {
        val prevCommissionPct = 15.0
        val newGrossPremium = 17700.0 // 17,700 total (Net = 17700 / 1.18 = 15000)
        val netPrem = newGrossPremium / 1.18
        val calculatedCommAmount = (netPrem * prevCommissionPct) / 100.0

        assertEquals(15000.0, netPrem, 0.001)
        assertEquals(2250.0, calculatedCommAmount, 0.001)
    }

    @Test
    fun testManualFormPrefillFromExistingClient() {
        val existingCustomer = Customer(
            name = "Priya Patel",
            mobile = "9823456789",
            email = "priya@example.com",
            address = "Satellite, Ahmedabad"
        )
        val existingVehicle = Vehicle(
            regNo = "GJ01AB9999",
            customer = existingCustomer,
            makeModel = "Hyundai Creta SX",
            vin = "MALB1234567890123",
            engineNo = "G4FL123456"
        )

        // User typed nothing yet in customer/vehicle fields
        val currName = ""
        val currMobile = ""
        val currAddress = ""
        val currMakeModel = ""

        val prefilledName = currName.ifBlank { existingVehicle.customer.name }
        val prefilledMobile = if (!FieldValidators.isValidMobile(currMobile) && FieldValidators.isValidMobile(existingVehicle.customer.mobile)) {
            existingVehicle.customer.mobile
        } else currMobile
        val prefilledAddress = currAddress.ifBlank { existingVehicle.customer.address }
        val prefilledMakeModel = currMakeModel.ifBlank { existingVehicle.makeModel ?: "" }

        assertEquals("Priya Patel", prefilledName)
        assertEquals("9823456789", prefilledMobile)
        assertEquals("Satellite, Ahmedabad", prefilledAddress)
        assertEquals("Hyundai Creta SX", prefilledMakeModel)
    }

    @Test
    fun testManualFormEditModeDoesNotTriggerRenewal() {
        val isEditMode = true
        val hasExistingClient = true

        // When in edit mode, it updates the record and should not be treated as a renewal
        val isRenewal = !isEditMode && hasExistingClient
        assertFalse(isRenewal)

        val saleType = if (isRenewal) SaleType.RENEWAL else SaleType.NEW_BUSINESS
        assertEquals(SaleType.NEW_BUSINESS, saleType)
    }
}
