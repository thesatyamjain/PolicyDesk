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

    // Pattern to locate registration numbers inside text or labels
    private val EMBEDDED_STANDARD_REGEX = Regex("""\b([A-Z]{2})\s*[-_.]?\s*([0-9]{1,2})\s*[-_.]?\s*([A-Z]{0,3})\s*[-_.]?\s*([0-9]{4})\b""", RegexOption.IGNORE_CASE)
    private val EMBEDDED_BH_REGEX = Regex("""\b([0-9]{2})\s*[-_.]?\s*BH\s*[-_.]?\s*([0-9]{4})\s*[-_.]?\s*([A-Z]{1,2})\b""", RegexOption.IGNORE_CASE)

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
     * Extracts a clean vehicle registration number from noisy text, table headers,
     * or labels (e.g., "पंजी. संख्या / Regn. Number: RJ14AB1234").
     */
    fun extractRegistrationNumber(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""

        val bhMatch = EMBEDDED_BH_REGEX.find(trimmed)
        if (bhMatch != null) {
            return normalize(bhMatch.value)
        }

        val stdMatch = EMBEDDED_STANDARD_REGEX.find(trimmed)
        if (stdMatch != null) {
            return normalize(stdMatch.value)
        }

        // Strip known Hindi and English label prefixes if present
        val labelCleaned = trimmed
            .replace(Regex("""^(?i)(पंजीकरण\s*संख्या|पंजीकरण\s*सं\.?|पंजी\.?\s*संख्या|पंजी\s*सं\.?|वाहन\s*पंजीकरण\s*संख्या|वाहन\s*पंजी\.?\s*सं\.?|वाहन\s*संख्या|वाहन\s*नं\.?|वाहन\s*क्रमांक|गाड़ी\s*नं\.?|गाड़ी\s*नंबर|गाड़ी\s*नं\.?|गाड़ी\s*नंबर|रजिस्ट्रेशन\s*नं\.?|रजिस्ट्रेशन\s*नंबर|vehicle\s*regn?\.?\s*no\.?|vehicle\s*registration\s*no\.?|vehicle\s*number|vehicle\s*no\.?|veh\s*regn?\.?\s*no\.?|veh\s*no\.?|registration\s*no\.?|registration\s*number|regn?\.?\s*no\.?|regn?\.?\s*number|plate\s*no\.?|number\s*plate|rc\s*no\.?|rc\s*number|rto\s*reg\s*no\.?|rto\s*no\.?|car\s*no\.?|bike\s*no\.?|truck\s*no\.?|bus\s*no\.?)\s*[:/-]?\s*"""), "")
            .trim()

        return normalize(labelCleaned)
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

    /**
     * Content-aware extractor for Insured/Customer Name and Salutation/Title.
     * Separates titles (Shri, Smt, Kumari, Mr, Mrs, Ms, Dr, M/s, etc.) and strips bilingual labels.
     */
    fun extractCustomerNameAndTitle(rawName: String, rawTitle: String = ""): Pair<String, String> {
        var clean = rawName.trim()
            .replace(Regex("""^(?i)(बीमित\s*का\s*नाम|बीमाधारक\s*का\s*नाम|बीमाधारक|ग्राहक\s*का\s*नाम|प्रस्तावक\s*का\s*नाम|प्रस्तावक|वाहन\s*स्वामी|स्वामी\s*का\s*नाम|मालिक\s*का\s*नाम|पार्टी\s*का\s*नाम|name\s*of\s*insured|insured(?:'s)?\s*name|customer\s*name|cust\s*name|proposer\s*name|name\s*of\s*proposer|policyholder\s*name|policy\s*holder\s*name|policyholder|client\s*name|owner\s*name|party\s*name|applicant\s*name|name)\s*[:/-]?\s*"""), "")
            .trim()

        var title = rawTitle.trim()

        val salutations = listOf(
            "Shri", "Smt", "Kumari", "Km", "Mr", "Mrs", "Ms", "Dr", "M/s", "Messrs",
            "Prof", "Capt", "Major", "Maj", "Col", "Lt", "Hon", "Rev", "Adv", "CA", "CS", "Er"
        )
        val hindiSalutations = mapOf(
            "श्री" to "Shri",
            "श्रीमती" to "Smt.",
            "सुश्री" to "Kumari",
            "कुमारी" to "Kumari",
            "डॉक्टर" to "Dr.",
            "डॉ" to "Dr.",
            "मेसर्स" to "M/s"
        )

        for ((hSal, mapped) in hindiSalutations) {
            val prefixRegex = Regex("""^${Regex.escape(hSal)}\.?\s+""")
            if (prefixRegex.containsMatchIn(clean)) {
                if (title.isBlank()) title = mapped
                clean = clean.replace(prefixRegex, "").trim()
                break
            }
        }

        if (title.isBlank()) {
            for (sal in salutations) {
                val prefixRegex = Regex("""^(?i)${Regex.escape(sal)}\.?\s+""")
                if (prefixRegex.containsMatchIn(clean)) {
                    title = if (sal.equals("m/s", ignoreCase = true)) "M/s" else sal.replaceFirstChar { it.uppercase() }
                    clean = clean.replace(prefixRegex, "").trim()
                    break
                }
            }
        }

        // Title case formatting for customer name
        val formattedName = clean.split(Regex("\\s+")).filter { it.isNotBlank() }.joinToString(" ") { word ->
            if (word.all { it.isUpperCase() } && word.length <= 4) word // Preserves business acronyms (e.g. PVT, LTD, LLP)
            else word.lowercase().replaceFirstChar { it.uppercase() }
        }

        return Pair(formattedName, title)
    }

    fun extractCleanEmail(rawEmail: String): String {
        val trimmed = rawEmail.trim()
        val match = EMAIL_REGEX.find(trimmed)
            ?: Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""").find(trimmed)
        return match?.value?.lowercase() ?: ""
    }

    fun extractCleanPolicyNumber(rawPolicy: String): String {
        return rawPolicy.trim()
            .replace(Regex("""^(?i)(पॉलिसी\s*संख्या|पॉलिसी\s*सं\.?|पॉलिसी\s*नंबर|बीमा\s*पत्र\s*सं\.?|बीमा\s*पत्र\s*संख्या|प्रमाण\s*पत्र\s*सं\.?|कवर\s*नोट\s*सं\.?|अनुसूची\s*सं\.?|अनुसूची\s*संख्या|policy\s*no\.?|policy\s*number|pol\s*no\.?|pol\s*number|pol/cert\s*no\.?|certificate\s*no\.?|cert\s*no\.?|schedule\s*no\.?|cover\s*note\s*no\.?|cn\s*no\.?|doc\s*no\.?)\s*[:/-]?\s*"""), "")
            .trim()
            .trimEnd('.', ',', ';', '>')
    }

    fun extractCleanAddress(rawAddress: String): String {
        return rawAddress.trim()
            .replace(Regex("""^(?i)(पता|पत्राचार\s*का\s*पता|स्थायी\s*पता|बीमित\s*का\s*पता|ग्राहक\s*का\s*पता|निवास\s*का\s*पता|comm\.?\s*address|communication\s*address|res\.?\s*address|residential\s*address|corr\.?\s*address|correspondence\s*address|perm\.?\s*address|permanent\s*address|insured\s*address|customer\s*address|postal\s*address|address|addr)\s*[:/-]?\s*"""), "")
            .replace(Regex("""[\r\n\t]+"""), " ")
            .replace(Regex("""\s{2,}"""), " ")
            .trim()
    }

    fun extractCleanVin(rawVin: String): String {
        return rawVin.trim()
            .replace(Regex("""^(?i)(चेसिस\s*संख्या|चेसिस\s*सं\.?|चेसिस\s*नंबर|चेसिस|फ्रेम\s*सं\.?|फ्रेम\s*संख्या|chassis\s*no\.?|chassis\s*number|chassis|chasis\s*no\.?|chasis|chass\s*no\.?|vin\s*no\.?|vin\s*number|vin|frame\s*no\.?|frame\s*number|ch\s*no\.?)\s*[:/-]?\s*"""), "")
            .filter { it.isLetterOrDigit() }
            .uppercase()
    }

    fun extractCleanEngineNo(rawEngine: String): String {
        return rawEngine.trim()
            .replace(Regex("""^(?i)(इंजन\s*संख्या|इंजन\s*सं\.?|इंजन\s*नंबर|इंजन|मोटर\s*सं\.?|मोटर\s*संख्या|मोटर\s*नंबर|engine\s*no\.?|engine\s*number|eng\s*no\.?|eng\s*number|engine|motor\s*no\.?|motor\s*number|motor)\s*[:/-]?\s*"""), "")
            .filter { it.isLetterOrDigit() }
            .uppercase()
    }

    fun extractCleanMakeModel(rawMakeModel: String): String {
        return rawMakeModel.trim()
            .replace(Regex("""^(?i)(वाहन\s*का\s*मेक/?मॉडल|वाहन\s*विवरण|वाहन\s*मॉडल|मेक/?मॉडल|वाहन\s*का\s*प्रकार|make\s*/?\s*model|make\s*&\s*model|vehicle\s*make|vehicle\s*model|veh\s*make|veh\s*model|vehicle\s*description|desc\s*of\s*vehicle|veh\s*desc|model\s*variant|variant|description|model)\s*[:/-]?\s*"""), "")
            .replace(Regex("""\s{2,}"""), " ")
            .trim()
    }

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
