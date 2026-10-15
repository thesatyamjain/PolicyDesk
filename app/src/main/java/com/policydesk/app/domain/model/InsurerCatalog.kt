package com.policydesk.app.domain.model

object InsurerCatalog {
    val TITLES = listOf("Shri", "Smt.", "Kumari", "Sushri")

    val PSU_INSURERS = listOf(
        "National Insurance",
        "New India Assurance",
        "Oriental Insurance",
        "United India Insurance",
        "Other"
    )

    val PRIVATE_INSURERS = listOf(
        "Acko General Insurance",
        "Bajaj Allianz General Insurance",
        "Cholamandalam MS General Insurance",
        "Future Generali India Insurance",
        "Go Digit General Insurance",
        "HDFC ERGO General Insurance",
        "ICICI Lombard General Insurance",
        "IFFCO Tokio General Insurance",
        "IndusInd General Insurance",
        "Kotak Mahindra General Insurance",
        "Kshema General Insurance",
        "Liberty General Insurance",
        "Magma HDI General Insurance",
        "Navi General Insurance",
        "Raheja QBE General Insurance",
        "Reliance General Insurance",
        "Royal Sundaram General Insurance",
        "SBI General Insurance",
        "Shriram General Insurance",
        "Tata AIG General Insurance",
        "Universal Sompo General Insurance",
        "Zuno General Insurance",
        "Zurich Kotak General Insurance",
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
            .replace("ltd.", "")
            .replace("ltd", "")
            .replace("insurance", "")
            .replace("general", "")
            .replace("the ", "")
            .trim()

        // 1. Direct PSU matches and well-known abbreviations
        when {
            clean.contains("national") || clean == "nic" || clean == "nicl" ->
                return Pair(Sector.PSU, "National Insurance")
            clean.contains("new india") || clean == "nia" || clean == "niacl" ->
                return Pair(Sector.PSU, "New India Assurance")
            clean.contains("oriental") || clean == "oic" || clean == "oicl" ->
                return Pair(Sector.PSU, "Oriental Insurance")
            clean.contains("united") || clean == "uiic" || clean == "uiicl" ->
                return Pair(Sector.PSU, "United India Insurance")
        }

        // 2. Direct Private matches and common brand handles
        when {
            clean.contains("acko") ->
                return Pair(Sector.PRIVATE, "Acko General Insurance")
            clean.contains("bajaj") || clean.contains("allianz") ->
                return Pair(Sector.PRIVATE, "Bajaj Allianz General Insurance")
            clean.contains("chola") || clean.contains("cholamandalam") ->
                return Pair(Sector.PRIVATE, "Cholamandalam MS General Insurance")
            clean.contains("future") || clean.contains("generali") ->
                return Pair(Sector.PRIVATE, "Future Generali India Insurance")
            clean.contains("digit") ->
                return Pair(Sector.PRIVATE, "Go Digit General Insurance")
            clean.contains("hdfc") || clean.contains("ergo") ->
                return Pair(Sector.PRIVATE, "HDFC ERGO General Insurance")
            clean.contains("icici") || clean.contains("lombard") || clean.contains("bharti axa") ->
                return Pair(Sector.PRIVATE, "ICICI Lombard General Insurance")
            clean.contains("iffco") || clean.contains("tokio") ->
                return Pair(Sector.PRIVATE, "IFFCO Tokio General Insurance")
            clean.contains("indusind") ->
                return Pair(Sector.PRIVATE, "IndusInd General Insurance")
            clean.contains("zurich") ->
                return Pair(Sector.PRIVATE, "Zurich Kotak General Insurance")
            clean.contains("kotak") ->
                return Pair(Sector.PRIVATE, "Kotak Mahindra General Insurance")
            clean.contains("kshema") ->
                return Pair(Sector.PRIVATE, "Kshema General Insurance")
            clean.contains("liberty") ->
                return Pair(Sector.PRIVATE, "Liberty General Insurance")
            clean.contains("magma") ->
                return Pair(Sector.PRIVATE, "Magma HDI General Insurance")
            clean.contains("navi") ->
                return Pair(Sector.PRIVATE, "Navi General Insurance")
            clean.contains("raheja") || clean.contains("qbe") ->
                return Pair(Sector.PRIVATE, "Raheja QBE General Insurance")
            clean.contains("reliance") ->
                return Pair(Sector.PRIVATE, "Reliance General Insurance")
            clean.contains("sundaram") || clean.contains("royal") ->
                return Pair(Sector.PRIVATE, "Royal Sundaram General Insurance")
            clean.contains("sbi") ->
                return Pair(Sector.PRIVATE, "SBI General Insurance")
            clean.contains("shriram") ->
                return Pair(Sector.PRIVATE, "Shriram General Insurance")
            clean.contains("tata") || clean.contains("aig") ->
                return Pair(Sector.PRIVATE, "Tata AIG General Insurance")
            clean.contains("sompo") || clean.contains("universal") ->
                return Pair(Sector.PRIVATE, "Universal Sompo General Insurance")
            clean.contains("zuno") || clean.contains("edelweiss") ->
                return Pair(Sector.PRIVATE, "Zuno General Insurance")
        }

        // 3. Fallback scan against standard lists
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
