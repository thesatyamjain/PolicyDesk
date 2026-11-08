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
}
