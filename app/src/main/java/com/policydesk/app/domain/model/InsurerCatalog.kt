package com.policydesk.app.domain.model

object InsurerCatalog {
    val TITLES = listOf("Shri", "Smt.", "Kumari", "Sushri")

    val PSU_INSURERS = listOf(
        "New India Assurance",
        "United India Insurance",
        "National Insurance",
        "Oriental Insurance",
        "Other"
    )

    val PRIVATE_INSURERS = listOf(
        "HDFC ERGO General Insurance",
        "ICICI Lombard General Insurance",
        "Tata AIG General Insurance",
        "Bajaj Allianz General Insurance",
        "SBI General Insurance",
        "Go Digit General Insurance",
        "Reliance General Insurance",
        "Kotak Mahindra General Insurance",
        "Cholamandalam MS General Insurance",
        "Star Health and Allied Insurance",
        "Magma HDI General Insurance",
        "Future Generali India Insurance",
        "Royal Sundaram General Insurance",
        "Liberty General Insurance",
        "Other"
    )

    fun getInsurersForSector(sector: Sector, customInsurers: List<String> = emptyList()): List<String> {
        val base = when (sector) {
            Sector.PSU -> PSU_INSURERS
            Sector.PRIVATE -> PRIVATE_INSURERS
        }
        val customClean = customInsurers.filter { it.isNotBlank() && it !in base && it != "Other" }
        return if (customClean.isEmpty()) base else (base.filter { it != "Other" } + customClean + "Other")
    }

    /**
     * Finds sector and canonical insurer name matching an extracted text or spreadsheet string.
     */
    fun matchInsurer(rawName: String): Pair<Sector, String>? {
        val clean = rawName.trim().lowercase()
            .replace("co. ltd.", "")
            .replace("company limited", "")
            .replace("co ltd", "")
            .replace("ltd", "")
            .replace("insurance", "")
            .replace("general", "")
            .trim()

        for (psu in PSU_INSURERS.filter { it != "Other" }) {
            val key = psu.lowercase().replace("insurance", "").trim()
            if (clean.contains(key) || key.contains(clean)) {
                return Pair(Sector.PSU, psu)
            }
        }

        for (pvt in PRIVATE_INSURERS.filter { it != "Other" }) {
            val key = pvt.lowercase().replace("general insurance", "").replace("insurance", "").trim()
            if (clean.contains(key) || key.contains(clean)) {
                return Pair(Sector.PRIVATE, pvt)
            }
        }

        return null
    }
}
