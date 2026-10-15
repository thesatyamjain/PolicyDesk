package com.policydesk.app.domain.model

object RegistrationNormalizer {
    private val STRIP_REGEX = Regex("[\\s\\-\\.]")

    // Standard Indian format: State code (2) + District/RTO (1-2) + Series (0-3) + Number (4)
    // E.g., MH12AB1234, DL01A9999, KA051234
    private val STANDARD_PATTERN = Regex("^[A-Z]{2}[0-9]{1,2}[A-Z]{0,3}[0-9]{4}$")

    // Bharat Series (BH): Year (2) + BH + Number (4) + Series (1-2)
    // E.g., 22BH1234AA
    private val BH_PATTERN = Regex("^[0-9]{2}BH[0-9]{4}[A-Z]{1,2}$")

    // Vintage / Commercial / Defense / Diplomatic / Old formats
    private val EXTENDED_PATTERN = Regex("^[A-Z0-9]{6,12}$")

    fun normalize(raw: String): String {
        return raw.trim().uppercase().replace(STRIP_REGEX, "")
    }

    /**
     * Checks if the normalized string matches standard Indian registration patterns.
     * Returns true if valid, false if unusual (which triggers a warning but does not block).
     */
    fun isStandardFormat(normalized: String): Boolean {
        if (normalized.length < 6 || normalized.length > 12) return false
        return STANDARD_PATTERN.matches(normalized) || BH_PATTERN.matches(normalized)
    }

    fun isAcceptableFormat(normalized: String): Boolean {
        return EXTENDED_PATTERN.matches(normalized)
    }

    fun formatForDisplay(normalized: String): String {
        val clean = normalize(normalized)
        // Standard pattern formatting: "MH 12 AB 1234"
        if (STANDARD_PATTERN.matches(clean)) {
            val state = clean.substring(0, 2)
            var idx = 2
            while (idx < clean.length && clean[idx].isDigit()) idx++
            val rto = clean.substring(2, idx)
            val numStart = clean.length - 4
            val series = clean.substring(idx, numStart)
            val number = clean.substring(numStart)
            return if (series.isNotEmpty()) "$state $rto $series $number" else "$state $rto $number"
        }
        // BH series: "22 BH 1234 AA"
        if (BH_PATTERN.matches(clean)) {
            val year = clean.substring(0, 2)
            val bh = clean.substring(2, 4)
            val number = clean.substring(4, 8)
            val series = clean.substring(8)
            return "$year $bh $number $series"
        }
        return clean
    }
}

object FieldValidators {
    // 10 digits starting with 6, 7, 8, 9 with optional +91 prefix
    private val MOBILE_REGEX = Regex("^(?:\\+91)?[6-9][0-9]{9}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidName(name: String): Boolean = name.trim().length >= 2

    fun isValidMobile(mobile: String): Boolean {
        val clean = mobile.trim().replace(" ", "").replace("-", "")
        return MOBILE_REGEX.matches(clean)
    }

    fun normalizeMobile(mobile: String): String {
        val clean = mobile.trim().replace(" ", "").replace("-", "")
        return if (clean.startsWith("+91")) clean.substring(3) else clean
    }

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return true
        return EMAIL_REGEX.matches(email.trim())
    }

    fun isValidAddress(address: String): Boolean = address.trim().isNotEmpty()

    fun isValidPolicyNumber(policyNumber: String): Boolean {
        val clean = policyNumber.trim()
        return clean.isNotEmpty() && clean.all { it.isLetterOrDigit() || it == '/' || it == '-' }
    }

    fun isVinStandardLength(vin: String?): Boolean {
        if (vin.isNullOrBlank()) return true
        return vin.trim().length == 17
    }
}
