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
