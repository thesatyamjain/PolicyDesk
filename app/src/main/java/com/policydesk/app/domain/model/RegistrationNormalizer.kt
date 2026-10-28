package com.policydesk.app.domain.model

object RegistrationNormalizer {
    // Standard Indian format: State code (2) + District/RTO (1-2) + Series (0-3) + Number (4)
    // E.g., MH12AB1234, DL01A9999, KA051234
    private val STANDARD_PATTERN = Regex("^[A-Z]{2}[0-9]{1,2}[A-Z]{0,3}[0-9]{4}$")

    // Bharat Series (BH): Year (2) + BH + Number (4) + Series (1-2)
    // E.g., 22BH1234AA
    private val BH_PATTERN = Regex("^[0-9]{2}BH[0-9]{4}[A-Z]{1,2}$")

    // Vintage / Commercial / Defense / Diplomatic / Old formats
    private val EXTENDED_PATTERN = Regex("^[A-Z0-9]{6,12}$")

    fun normalize(raw: String): String {
        val sb = StringBuilder(raw.length)
        for (i in 0 until raw.length) {
            val ch = raw[i]
            if (ch in 'a'..'z') {
                sb.append((ch.code - 32).toChar())
            } else if (ch in 'A'..'Z' || ch in '0'..'9') {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Checks if the normalized string matches standard Indian registration patterns.
     * Returns true if valid, false if unusual (which triggers a warning but does not block).
     */
    fun isStandardFormat(normalized: String): Boolean {
        if (normalized.length < 6 || normalized.length > 12) return false
        return STANDARD_PATTERN.matches(normalized) || BH_PATTERN.matches(normalized)
    }

    /**
     * Intelligently repairs common OCR character confusions (e.g. 'O' vs '0', 'I' vs '1')
     * if repairing them yields a valid standard Indian registration number.
     */
    fun repairOcrMisreads(normalized: String): String {
        if (isStandardFormat(normalized)) return normalized
        if (normalized.length !in 8..11) return normalized

        val chars = normalized.toCharArray()
        val numStart = chars.size - 4

        for (i in numStart until chars.size) {
            when (chars[i]) {
                'O' -> chars[i] = '0'
                'I' -> chars[i] = '1'
                'S' -> chars[i] = '5'
                'Z' -> chars[i] = '2'
                'B' -> chars[i] = '8'
            }
        }

        if (chars.size >= 4) {
            if (chars[2] == 'O') chars[2] = '0'
            else if (chars[2] == 'I') chars[2] = '1'
            if (chars[3] == 'O' && chars[2].isDigit()) chars[3] = '0'
            else if (chars[3] == 'I' && chars[2].isDigit()) chars[3] = '1'
        }

        val candidate = String(chars)
        return if (isStandardFormat(candidate)) candidate else normalized
    }

    fun isAcceptableFormat(normalized: String): Boolean {
        return EXTENDED_PATTERN.matches(normalized)
    }

    /**
     * Determines space break indices for Indian vehicle registration formats.
     * Supports both progressive typing (partial strings) and full plates.
     */
    fun getSpaceBreakIndices(raw: String): Set<Int> {
        val clean = normalize(raw)
        if (clean.length < 3) return emptySet()

        val breaks = mutableSetOf<Int>()

        // Bharat (BH) series: e.g. 22BH1234AA
        val isBh = clean.length >= 2 && clean[0].isDigit() && clean[1].isDigit() &&
                (clean.length < 3 || clean[2] == 'B')
        if (isBh) {
            if (clean.length > 2) breaks.add(2)
            if (clean.length > 4) breaks.add(4)
            if (clean.length > 8) breaks.add(8)
            return breaks
        }

        // Standard Indian format: State (2 letters) + RTO (1-2 digits) + Series (0-3 letters) + Number (1-4 digits)
        if (clean.length >= 2 && clean[0].isLetter() && clean[1].isLetter()) {
            var i = 2
            // Must have digits after state code
            if (i < clean.length && clean[i].isDigit()) {
                breaks.add(2) // Break between State and RTO
                while (i < clean.length && clean[i].isDigit() && (i - 2) < 2) {
                    i++
                }
                val rtoEnd = i
                // If there are letters for series
                if (i < clean.length && clean[i].isLetter()) {
                    breaks.add(rtoEnd) // Break between RTO and Series
                    while (i < clean.length && clean[i].isLetter() && (i - rtoEnd) < 3) {
                        i++
                    }
                    val seriesEnd = i
                    if (i < clean.length && clean[i].isDigit()) {
                        breaks.add(seriesEnd) // Break between Series and Number
                    }
                } else if (clean.length - rtoEnd >= 4 && clean.substring(rtoEnd).all { it.isDigit() }) {
                    // No series letters (e.g. KA051234): 4 trailing digits are number
                    val numStart = clean.length - 4
                    if (numStart >= rtoEnd) {
                        breaks.add(numStart)
                    }
                }
            }
        }
        return breaks
    }

    fun formatForDisplay(normalized: String): String {
        val clean = normalize(normalized)
        if (clean.length < 3) return clean
        val breaks = getSpaceBreakIndices(clean)
        if (breaks.isEmpty()) return clean
        val sb = StringBuilder(clean.length + breaks.size)
        for (i in 0 until clean.length) {
            if (i in breaks) sb.append(' ')
            sb.append(clean[i])
        }
        return sb.toString()
    }
}

object FieldValidators {
    // 10 digits starting with 6, 7, 8, 9 with optional +91 prefix
    private val MOBILE_REGEX = Regex("^(?:\\+91)?[6-9][0-9]{9}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidName(name: String): Boolean = name.trim().length >= 2

    fun normalizeMobile(mobile: String): String {
        var clean = mobile.trim()
        val asDouble = clean.toDoubleOrNull()
        if (asDouble != null && asDouble >= 6000000000.0 && asDouble < 100000000000.0) {
            clean = asDouble.toLong().toString()
        }
        val digits = clean.filter { it.isDigit() }
        return when {
            digits.length == 10 -> digits
            digits.length == 12 && digits.startsWith("91") -> digits.substring(2)
            digits.length == 11 && digits.startsWith("0") -> digits.substring(1)
            digits.length > 10 && digits.takeLast(10)[0] in '6'..'9' -> digits.takeLast(10)
            else -> digits
        }
    }

    fun isValidMobile(mobile: String): Boolean {
        val clean = normalizeMobile(mobile)
        return clean.length == 10 && clean[0] in '6'..'9'
    }

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return true
        return EMAIL_REGEX.matches(email.trim())
    }

    fun isValidAddress(address: String): Boolean = true

    fun isValidPolicyNumber(policyNumber: String): Boolean {
        val clean = policyNumber.trim()
        return clean.isNotEmpty() && clean.all { it.isLetterOrDigit() || it == '/' || it == '-' || it == '.' || it == '_' || it == ' ' }
    }

    fun isVinStandardLength(vin: String?): Boolean {
        if (vin.isNullOrBlank()) return true
        return vin.trim().length == 17
    }
}
