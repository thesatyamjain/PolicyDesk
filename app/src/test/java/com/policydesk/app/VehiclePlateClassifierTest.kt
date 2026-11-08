package com.policydesk.app

import com.policydesk.app.domain.model.NumberPlateStyle
import com.policydesk.app.domain.model.VehiclePlateClassifier
import org.junit.Assert.assertEquals
import org.junit.Test

class VehiclePlateClassifierTest {

    @Test
    fun testPrivateVehiclePlateDetection() {
        assertEquals(NumberPlateStyle.PRIVATE_WHITE, VehiclePlateClassifier.detectStyle("Maruti Suzuki Swift VXI"))
        assertEquals(NumberPlateStyle.PRIVATE_WHITE, VehiclePlateClassifier.detectStyle("Hyundai Creta SX"))
        assertEquals(NumberPlateStyle.PRIVATE_WHITE, VehiclePlateClassifier.detectStyle("Honda Activa 6G"))
        assertEquals(NumberPlateStyle.PRIVATE_WHITE, VehiclePlateClassifier.detectStyle("Hero Splendor Plus"))
        assertEquals(NumberPlateStyle.PRIVATE_WHITE, VehiclePlateClassifier.detectStyle(null))
        assertEquals(NumberPlateStyle.PRIVATE_WHITE, VehiclePlateClassifier.detectStyle(""))
    }

    @Test
    fun testCommercialVehiclePlateDetection() {
        assertEquals(NumberPlateStyle.COMMERCIAL_YELLOW, VehiclePlateClassifier.detectStyle("Tata Ace Gold"))
        assertEquals(NumberPlateStyle.COMMERCIAL_YELLOW, VehiclePlateClassifier.detectStyle("Bolero Maxi Truck Plus"))
        assertEquals(NumberPlateStyle.COMMERCIAL_YELLOW, VehiclePlateClassifier.detectStyle("Maruti Dzire Tour S (Taxi)"))
        assertEquals(NumberPlateStyle.COMMERCIAL_YELLOW, VehiclePlateClassifier.detectStyle("Eicher Pro 2049 Truck"))
        assertEquals(NumberPlateStyle.COMMERCIAL_YELLOW, VehiclePlateClassifier.detectStyle("Force Traveller 17 Seater School Bus"))
        assertEquals(NumberPlateStyle.COMMERCIAL_YELLOW, VehiclePlateClassifier.detectStyle("Auto Rickshaw LPG"))
        assertEquals(NumberPlateStyle.COMMERCIAL_YELLOW, VehiclePlateClassifier.detectStyle("Mahindra Bolero Camper Pickup"))
    }

    @Test
    fun testPrivateElectricVehiclePlateDetection() {
        assertEquals(NumberPlateStyle.EV_GREEN, VehiclePlateClassifier.detectStyle("Tata Nexon EV Fearless"))
        assertEquals(NumberPlateStyle.EV_GREEN, VehiclePlateClassifier.detectStyle("Tata Tiago EV"))
        assertEquals(NumberPlateStyle.EV_GREEN, VehiclePlateClassifier.detectStyle("Tata Punch EV"))
        assertEquals(NumberPlateStyle.EV_GREEN, VehiclePlateClassifier.detectStyle("MG ZS EV"))
        assertEquals(NumberPlateStyle.EV_GREEN, VehiclePlateClassifier.detectStyle("Ola S1 Pro"))
        assertEquals(NumberPlateStyle.EV_GREEN, VehiclePlateClassifier.detectStyle("Ather 450X Gen 3"))
        assertEquals(NumberPlateStyle.EV_GREEN, VehiclePlateClassifier.detectStyle("Bajaj Chetak Electric"))
        assertEquals(NumberPlateStyle.EV_GREEN, VehiclePlateClassifier.detectStyle("TVS iQube Electric"))
        assertEquals(NumberPlateStyle.EV_GREEN, VehiclePlateClassifier.detectStyle("BYD Atto 3"))
        assertEquals(NumberPlateStyle.EV_GREEN, VehiclePlateClassifier.detectStyle("MG Windsor EV"))
    }

    @Test
    fun testCommercialElectricVehiclePlateDetection() {
        assertEquals(NumberPlateStyle.COMMERCIAL_EV, VehiclePlateClassifier.detectStyle("Mayuri E-Rickshaw Battery"))
        assertEquals(NumberPlateStyle.COMMERCIAL_EV, VehiclePlateClassifier.detectStyle("Tata Ace EV Goods Carrier"))
        assertEquals(NumberPlateStyle.COMMERCIAL_EV, VehiclePlateClassifier.detectStyle("Mahindra Treo Electric Auto"))
        assertEquals(NumberPlateStyle.COMMERCIAL_EV, VehiclePlateClassifier.detectStyle("Tata Tigor EV Commercial Taxi"))
    }

    @Test
    fun testVehiclePlateStyleEntityMapping() {
        val customer = com.policydesk.app.domain.model.Customer(
            name = "Ramesh Kumar",
            mobile = "9876543210",
            address = "Mumbai"
        )
        val vehicle = com.policydesk.app.domain.model.Vehicle(
            regNo = "MH01AB1234",
            customer = customer,
            makeModel = "Maruti Suzuki Swift", // By default would be PRIVATE_WHITE
            plateStyle = NumberPlateStyle.COMMERCIAL_YELLOW // Manually chosen as commercial taxi
        )

        val entity = com.policydesk.app.data.local.entity.VehicleEntity.fromDomain(vehicle)
        assertEquals("COMMERCIAL_YELLOW", entity.plateStyle)

        val restored = entity.toDomain()
        assertEquals(NumberPlateStyle.COMMERCIAL_YELLOW, restored.plateStyle)
    }

    @Test
    fun testVehiclePlateStyleNullDefaultsToAutoDetection() {
        val customer = com.policydesk.app.domain.model.Customer(
            name = "Suresh Patel",
            mobile = "9876543211",
            address = "Pune"
        )
        val vehicle = com.policydesk.app.domain.model.Vehicle(
            regNo = "MH12EV9999",
            customer = customer,
            makeModel = "Tata Nexon EV",
            plateStyle = null
        )

        val entity = com.policydesk.app.data.local.entity.VehicleEntity.fromDomain(vehicle)
        assertEquals(null, entity.plateStyle)

        val restored = entity.toDomain()
        assertEquals(null, restored.plateStyle)
        // Resolves automatically via detector when null
        val detected = restored.plateStyle ?: VehiclePlateClassifier.detectStyle(restored.makeModel)
        assertEquals(NumberPlateStyle.EV_GREEN, detected)
    }
}
