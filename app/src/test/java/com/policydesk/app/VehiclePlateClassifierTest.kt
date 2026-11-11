package com.policydesk.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.policydesk.app.domain.model.NumberPlateStyle
import com.policydesk.app.domain.model.VehiclePlateClassifier
import com.policydesk.app.ui.components.hsrpPlateColors
import com.policydesk.app.ui.theme.AppleDarkSystemBlue
import com.policydesk.app.ui.theme.AppleDarkSystemBlueFill
import com.policydesk.app.ui.theme.AppleDarkSystemDeepOrange
import com.policydesk.app.ui.theme.AppleDarkSystemDeepOrangeBg
import com.policydesk.app.ui.theme.AppleDarkSystemGreen
import com.policydesk.app.ui.theme.AppleDarkSystemGreenBg
import com.policydesk.app.ui.theme.AppleDarkSystemGreenFill
import com.policydesk.app.ui.theme.AppleDarkSystemOrange
import com.policydesk.app.ui.theme.AppleDarkSystemOrangeBg
import com.policydesk.app.ui.theme.AppleDarkSystemPurple
import com.policydesk.app.ui.theme.AppleDarkSystemRed
import com.policydesk.app.ui.theme.AppleDarkSystemRedBg
import com.policydesk.app.ui.theme.AppleDarkSystemRedFill
import com.policydesk.app.ui.theme.AppleSystemBlue
import com.policydesk.app.ui.theme.AppleSystemBlueFill
import com.policydesk.app.ui.theme.AppleSystemDeepOrange
import com.policydesk.app.ui.theme.AppleSystemDeepOrangeBg
import com.policydesk.app.ui.theme.AppleSystemGreen
import com.policydesk.app.ui.theme.AppleSystemGreenBg
import com.policydesk.app.ui.theme.AppleSystemGreenFill
import com.policydesk.app.ui.theme.AppleSystemOrange
import com.policydesk.app.ui.theme.AppleSystemOrangeBg
import com.policydesk.app.ui.theme.AppleSystemPurple
import com.policydesk.app.ui.theme.AppleSystemRed
import com.policydesk.app.ui.theme.AppleSystemRedBg
import com.policydesk.app.ui.theme.AppleSystemRedFill
import com.policydesk.app.ui.theme.IosDarkLabelPrimary
import com.policydesk.app.ui.theme.IosDarkLabelSecondary
import com.policydesk.app.ui.theme.IosDarkLabelTertiary
import com.policydesk.app.ui.theme.IosDarkSecondaryGrouped
import com.policydesk.app.ui.theme.IosDarkTertiaryGrouped
import com.policydesk.app.ui.theme.IosLabelPrimary
import com.policydesk.app.ui.theme.IosLabelSecondary
import com.policydesk.app.ui.theme.IosLabelTertiary
import com.policydesk.app.ui.theme.IosSecondaryGrouped
import com.policydesk.app.ui.theme.IosSystemBackground
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    private fun contrastRatio(foreground: Color, background: Color): Float {
        val fgLum = foreground.luminance()
        val bgLum = background.luminance()
        val lighter = maxOf(fgLum, bgLum)
        val darker = minOf(fgLum, bgLum)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private fun assertWcagAa(label: String, foreground: Color, background: Color) {
        val ratio = contrastRatio(foreground, background)
        assertTrue("$label contrast $ratio < 4.5", ratio >= 4.5f)
    }

    @Test
    fun testAllFourHsrpPlateColorPalettesMeetWcagAa() {
        val evGreen = hsrpPlateColors(NumberPlateStyle.EV_GREEN)
        assertEquals(Color(0xFF0F6830), evGreen.bg)
        assertEquals(Color(0xFFFFFFFF), evGreen.text)

        val commEv = hsrpPlateColors(NumberPlateStyle.COMMERCIAL_EV)
        assertEquals(Color(0xFF0F5E2C), commEv.bg)
        assertEquals(Color(0xFFFFD60A), commEv.text)

        NumberPlateStyle.values().forEach { style ->
            val palette = hsrpPlateColors(style)
            assertWcagAa("HSRP $style text", palette.text, palette.bg)
            assertWcagAa("HSRP $style placeholder", palette.placeholder, palette.bg)
        }
    }

    @Test
    fun testWcagAaContrastForLabelsBadgesAndAccentFills() {
        val darkGlassLightestStop = Color(0xFF26262B)
        val darkDockPillTop = Color(0xFF38383C)

        // Light & dark label hierarchies
        assertWcagAa("IosLabelPrimary on canvas", IosLabelPrimary, IosSystemBackground)
        assertWcagAa("IosLabelSecondary on canvas", IosLabelSecondary, IosSystemBackground)
        assertWcagAa("IosLabelTertiary on canvas", IosLabelTertiary, IosSystemBackground)
        assertWcagAa("IosDarkLabelPrimary on card", IosDarkLabelPrimary, IosDarkSecondaryGrouped)
        assertWcagAa("IosDarkLabelSecondary on tertiary", IosDarkLabelSecondary, IosDarkTertiaryGrouped)
        assertWcagAa("IosDarkLabelTertiary on tertiary", IosDarkLabelTertiary, IosDarkTertiaryGrouped)

        // Accent fills with white foreground
        assertWcagAa("White on AppleSystemBlueFill", Color.White, AppleSystemBlueFill)
        assertWcagAa("White on AppleDarkSystemBlueFill", Color.White, AppleDarkSystemBlueFill)
        assertWcagAa("White on AppleSystemGreenFill", Color.White, AppleSystemGreenFill)
        assertWcagAa("White on AppleDarkSystemGreenFill", Color.White, AppleDarkSystemGreenFill)
        assertWcagAa("White on AppleSystemRedFill", Color.White, AppleSystemRedFill)
        assertWcagAa("White on AppleDarkSystemRedFill", Color.White, AppleDarkSystemRedFill)

        // Status badge foreground over tinted badge backgrounds on light & dark surfaces
        assertWcagAa("Light green badge", AppleSystemGreen, AppleSystemGreenBg.compositeOver(IosSystemBackground))
        assertWcagAa("Light orange badge", AppleSystemOrange, AppleSystemOrangeBg.compositeOver(IosSystemBackground))
        assertWcagAa("Light deepOrange badge", AppleSystemDeepOrange, AppleSystemDeepOrangeBg.compositeOver(IosSystemBackground))
        assertWcagAa("Light red badge", AppleSystemRed, AppleSystemRedBg.compositeOver(IosSystemBackground))

        assertWcagAa("Dark green badge", AppleDarkSystemGreen, AppleDarkSystemGreenBg.compositeOver(darkGlassLightestStop))
        assertWcagAa("Dark orange badge", AppleDarkSystemOrange, AppleDarkSystemOrangeBg.compositeOver(darkGlassLightestStop))
        assertWcagAa("Dark deepOrange badge", AppleDarkSystemDeepOrange, AppleDarkSystemDeepOrangeBg.compositeOver(darkGlassLightestStop))
        assertWcagAa("Dark red badge", AppleDarkSystemRed, AppleDarkSystemRedBg.compositeOver(darkGlassLightestStop))

        // Dock active tab accents on light & dark gliding pills
        assertWcagAa("Light dock blue", AppleSystemBlue, IosSecondaryGrouped)
        assertWcagAa("Light dock purple", AppleSystemPurple, IosSecondaryGrouped)
        assertWcagAa("Light dock orange", AppleSystemOrange, IosSecondaryGrouped)
        assertWcagAa("Light dock green", AppleSystemGreen, IosSecondaryGrouped)
        assertWcagAa("Dark dock blue", AppleDarkSystemBlue, darkDockPillTop)
        assertWcagAa("Dark dock purple", AppleDarkSystemPurple, darkDockPillTop)
        assertWcagAa("Dark dock orange", AppleDarkSystemOrange, darkDockPillTop)
        assertWcagAa("Dark dock green", AppleDarkSystemGreen, darkDockPillTop)
    }
}
