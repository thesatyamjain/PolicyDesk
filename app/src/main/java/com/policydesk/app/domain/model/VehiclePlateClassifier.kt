package com.policydesk.app.domain.model

enum class NumberPlateStyle(
    val displayName: String,
    val description: String
) {
    PRIVATE_WHITE(
        displayName = "Private (White Plate)",
        description = "Private cars & two-wheelers (Petrol, Diesel, CNG)"
    ),
    COMMERCIAL_YELLOW(
        displayName = "Commercial (Yellow Plate)",
        description = "Commercial taxis, cabs, trucks, buses & transport"
    ),
    EV_GREEN(
        displayName = "Electric (Green Plate)",
        description = "Private electric vehicles & battery scooters"
    ),
    COMMERCIAL_EV(
        displayName = "Commercial EV (Green & Yellow)",
        description = "Commercial electric cabs, e-rickshaws & delivery fleets"
    )
}

object VehiclePlateClassifier {

    fun detectStyle(makeModel: String?, notes: String? = null, isCommercialHint: Boolean = false): NumberPlateStyle {
        val rawText = listOfNotNull(makeModel, notes).joinToString(" ").uppercase().trim()
        if (rawText.isEmpty()) {
            return if (isCommercialHint) NumberPlateStyle.COMMERCIAL_YELLOW else NumberPlateStyle.PRIVATE_WHITE
        }

        // Normalize non-alphanumeric characters to space and pad with spaces for robust whole-word matching
        val normalized = " " + rawText.replace(Regex("[^A-Z0-9]"), " ").replace(Regex("\\s+"), " ") + " "

        val isEv = isElectric(normalized)
        val isComm = isCommercialHint || isCommercialVehicle(normalized)

        return when {
            isEv && isComm -> NumberPlateStyle.COMMERCIAL_EV
            isEv -> NumberPlateStyle.EV_GREEN
            isComm -> NumberPlateStyle.COMMERCIAL_YELLOW
            else -> NumberPlateStyle.PRIVATE_WHITE
        }
    }

    private fun isElectric(text: String): Boolean {
        if (text.isBlank()) return false
        val evKeywords = listOf(
            " EV ", " ELECTRIC ", " BATTERY ",
            " NEXON EV ", " TIAGO EV ", " TIGOR EV ", " PUNCH EV ", " CURVV EV ", " HARRIER EV ",
            " ZS EV ", " COMET ", " WINDSOR ", " XUV400 ", " IONIQ ", " KONA ", " BYD ", " ATTO ",
            " SEAL ", " E6 ", " EV6 ", " EV9 ", " IX1 ", " IX3 ", " E TRON ", " TAYCAN ", " E C3 ",
            " OLA ", " S1 PRO ", " S1 AIR ", " S1 X ", " ATHER ", " 450X ", " 450S ", " RIZTA ",
            " CHETAK ", " IQUBE ", " VIDA ", " ULTRAVIOLETTE ", " OKINAWA ", " AMPERE ", " PURE EV ",
            " REVOLT ", " E RICKSHAW ", " ERICKSHAW ", " TREO ", " APE E "
        )
        return evKeywords.any { text.contains(it) }
    }

    private fun isCommercialVehicle(text: String): Boolean {
        if (text.isBlank()) return false
        val commKeywords = listOf(
            " COMMERCIAL ", " TAXI ", " CAB ", " TOUR S ", " TOUR M ", " TOUR V ", " TOUR H1 ",
            " TRUCK ", " TIPPER ", " TRAILER ", " LORRY ", " CONTAINER ",
            " PICKUP ", " PICK UP ", " MAXI TRUCK ", " BOLERO MAXI ", " BOLERO PICKUP ", " BOLERO CAMPER ",
            " TATA ACE ", " CHOTA HATHI ", " SUPER ACE ", " INTRA ", " YODHA ", " DOST ", " BADA DOST ",
            " EICHER ", " ASHOK LEYLAND ", " BHARATBENZ ", " FURIO ", " BLAZO ",
            " BUS ", " MINIBUS ", " SCHOOL BUS ", " STAFF BUS ", " TRAVELLER ", " FORCE TRAVELLER ",
            " AUTO RICKSHAW ", " AUTORICKSHAW ", " RICKSHAW ", " E RICKSHAW ", " ERICKSHAW ",
            " AUTO ", " TEMPO ", " TUK TUK ", " VIKRAM ", " LOADER ", " CARRIER ",
            " TRACTOR ", " JCB ", " CRANE ", " EXCAVATOR ", " AMBULANCE ", " GOODS CARRIER ", " PUBLIC CARRIER "
        )
        return commKeywords.any { text.contains(it) }
    }
}
