package com.policydesk.app.data.importer

import android.content.Context
import android.net.Uri
import com.policydesk.app.domain.model.Customer
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.domain.model.InsurerCatalog
import com.policydesk.app.domain.model.Policy
import com.policydesk.app.domain.model.PolicySource
import com.policydesk.app.domain.model.PolicyStatus
import com.policydesk.app.domain.model.RegistrationNormalizer
import com.policydesk.app.domain.model.SaleType
import com.policydesk.app.domain.model.Sector
import com.policydesk.app.domain.model.Vehicle
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ParsedRow(
    val rowNumber: Int,
    val regNo: String,
    val customerName: String,
    val title: String?,
    val mobile: String,
    val email: String?,
    val address: String,
    val sector: Sector?,
    val insurer: String,
    val policyNumber: String,
    val expiryDateEpochMs: Long?,
    val totalPremium: Double?,
    val makeModel: String?,
    val vin: String?,
    val engineNo: String?,
    val notes: String?,
    val saleDateEpochMs: Long?,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    var isDuplicate: Boolean = false
) {
    val isValid: Boolean get() = errors.isEmpty()
}

data class ImportPreviewResult(
    val totalRows: Int,
    val validRows: List<ParsedRow>,
    val warningRows: List<ParsedRow>,
    val errorRows: List<ParsedRow>,
    val duplicateRows: List<ParsedRow>
)

object SpreadsheetParser {

    val TEMPLATE_CSV_HEADER = listOf(
        "Registration Number",
        "Customer Name",
        "Customer Title",
        "Mobile Number",
        "Email Address",
        "Residential Address",
        "Company Sector",
        "Insurer Name",
        "Policy Number",
        "Expiry Date",
        "Total Premium",
        "Vehicle Make Model",
        "Chassis Number",
        "Engine Number",
        "Notes",
        "Sale Date"
    )

    val TEMPLATE_SAMPLE_ROW = listOf(
        "MH12AB1234",
        "Rajesh Kumar",
        "Shri",
        "9876543210",
        "rajesh@example.com",
        "Flat 402, Shivam Apts, Pune",
        "Private",
        "HDFC ERGO General Insurance",
        "POL/2026/89412",
        "28/11/2026",
        "14500",
        "Maruti Swift Dzire",
        "MA3EJA62S00123456",
        "K12M1234567",
        "Preferred renewal before Diwali",
        "28/11/2025"
    )

    fun generateCsvTemplate(): String {
        val sb = StringBuilder()
        sb.append(TEMPLATE_CSV_HEADER.joinToString(",")).append("\n")
        sb.append(TEMPLATE_SAMPLE_ROW.joinToString(",") { "\"$it\"" }).append("\n")
        return sb.toString()
    }

    fun parseSpreadsheet(context: Context, uri: Uri): List<List<String>> {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: ""
        val fileName = uri.lastPathSegment?.lowercase() ?: ""

        val isXlsx = fileName.endsWith(".xlsx") || mimeType.contains("spreadsheet") || mimeType.contains("excel")

        contentResolver.openInputStream(uri)?.use { stream ->
            return if (isXlsx) {
                parseXlsx(stream)
            } else {
                parseCsv(stream)
            }
        }
        return emptyList()
    }

    private fun parseCsv(inputStream: InputStream): List<List<String>> {
        val result = mutableListOf<List<String>>()
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            val trimmed = line?.trim() ?: continue
            if (trimmed.isEmpty()) continue

            val tokens = mutableListOf<String>()
            val sb = java.lang.StringBuilder()
            var inQuotes = false

            for (i in 0 until trimmed.length) {
                val c = trimmed[i]
                if (c == '\"') {
                    inQuotes = !inQuotes
                } else if (c == ',' && !inQuotes) {
                    tokens.add(sb.toString().trim())
                    sb.setLength(0)
                } else {
                    sb.append(c)
                }
            }
            tokens.add(sb.toString().trim())
            result.add(tokens)
        }
        return result
    }

    private fun parseXlsx(inputStream: InputStream): List<List<String>> {
        val sharedStrings = mutableListOf<String>()
        val rows = mutableListOf<List<String>>()

        val zip = ZipInputStream(inputStream)
        var entry: ZipEntry?

        var sheet1Bytes: ByteArray? = null

        while (zip.nextEntry.also { entry = it } != null) {
            val name = entry?.name ?: ""
            if (name == "xl/sharedStrings.xml") {
                sharedStrings.addAll(parseSharedStrings(zip))
            } else if (name.startsWith("xl/worksheets/sheet") && name.endsWith(".xml")) {
                if (sheet1Bytes == null || name.contains("sheet1.xml")) {
                    sheet1Bytes = zip.readBytes()
                }
            }
            zip.closeEntry()
        }

        if (sheet1Bytes != null) {
            rows.addAll(parseSheetXml(sheet1Bytes.inputStream(), sharedStrings))
        }
        return rows
    }

    private fun parseSharedStrings(stream: InputStream): List<String> {
        val list = mutableListOf<String>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var eventType = parser.eventType
        var inT = false
        val sb = StringBuilder()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name == "t") inT = true
                }
                XmlPullParser.TEXT -> {
                    if (inT) sb.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    if (name == "t") {
                        inT = false
                    } else if (name == "si") {
                        list.add(sb.toString())
                        sb.setLength(0)
                    }
                }
            }
            eventType = parser.next()
        }
        return list
    }

    private fun parseColumnIndex(cellRef: String?): Int {
        if (cellRef.isNullOrEmpty()) return -1
        var col = 0
        var foundLetter = false
        for (i in 0 until cellRef.length) {
            val c = cellRef[i]
            if (c in 'A'..'Z') {
                col = col * 26 + (c - 'A' + 1)
                foundLetter = true
            } else if (c in 'a'..'z') {
                col = col * 26 + (c - 'a' + 1)
                foundLetter = true
            } else {
                break
            }
        }
        return if (foundLetter) col - 1 else -1
    }

    private fun parseSheetXml(stream: InputStream, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var eventType = parser.eventType
        var currentRow = mutableListOf<String>()
        var cellType: String? = null
        var cellRef: String? = null
        var inV = false
        var inT = false
        val cellValue = StringBuilder()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name == "row") {
                        currentRow = mutableListOf()
                    } else if (name == "c") {
                        cellType = parser.getAttributeValue(null, "t")
                        cellRef = parser.getAttributeValue(null, "r")
                        cellValue.setLength(0)
                    } else if (name == "v") {
                        inV = true
                    } else if (name == "t") {
                        inT = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inV || inT) cellValue.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    if (name == "v") {
                        inV = false
                    } else if (name == "t") {
                        inT = false
                    } else if (name == "c") {
                        val rawStr = cellValue.toString().trim()
                        val finalVal = if (cellType == "s") {
                            val idx = rawStr.toIntOrNull() ?: -1
                            if (idx in sharedStrings.indices) sharedStrings[idx] else rawStr
                        } else {
                            rawStr
                        }
                        val colIdx = parseColumnIndex(cellRef)
                        if (colIdx >= 0) {
                            while (currentRow.size < colIdx) {
                                currentRow.add("")
                            }
                            if (currentRow.size == colIdx) {
                                currentRow.add(finalVal)
                            } else {
                                currentRow[colIdx] = finalVal
                            }
                        } else {
                            currentRow.add(finalVal)
                        }
                    } else if (name == "row") {
                        if (currentRow.any { it.isNotBlank() }) {
                            rows.add(currentRow)
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return rows
    }

    fun parseDateToEpochMs(raw: String): Long? {
        val clean = raw.trim()
        if (clean.isEmpty()) return null

        // 1. Try Excel numeric serial date (e.g. 45000 or 45000.5)
        val num = clean.toDoubleOrNull()
        if (num != null && num > 20000 && num < 75000) {
            val days = num.toLong() - 25569
            return days * 86400000L
        }

        val patterns = listOf(
            "dd/MM/yyyy", "d/M/yyyy", "dd-MM-yyyy", "d-M-yyyy",
            "yyyy-MM-dd", "dd.MM.yyyy", "yyyy/MM/dd",
            "dd/MM/yy", "d/M/yy", "dd-MM-yy", "d-M-yy",
            "dd-MMM-yyyy", "d-MMM-yyyy", "dd MMM yyyy", "d MMM yyyy",
            "dd-MMM-yy", "d-MMM-yy", "dd-MMMM-yyyy", "dd MMMM yyyy",
            "MM/dd/yyyy", "M/d/yyyy"
        )

        fun tryParseDateString(str: String): Long? {
            val trimmed = str.trim()
            for (pattern in patterns) {
                try {
                    val formatter = DateTimeFormatter.ofPattern(pattern, java.util.Locale.ENGLISH)
                    val date = LocalDate.parse(trimmed, formatter)
                    return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                } catch (_: Exception) {}
            }
            return null
        }

        // 2. Direct match of clean string
        tryParseDateString(clean)?.let { return it }

        // 3. Regex search for embedded dates (e.g. "to midnight of 16/10/2027>>", "17/10/2026 to 16/10/2027")
        val sanitized = clean.replace(Regex("""[><()\"'\[\]]"""), " ")
        val dateRegex = Regex("""\b(\d{1,2}[-/. ](?:\d{1,2}|[A-Za-z]{3,9})[-/. ]\d{2,4})\b|\b(\d{4}[-/. ]\d{1,2}[-/. ]\d{1,2})\b""")
        val matches = dateRegex.findAll(sanitized).map { it.value.trim() }.toList()

        if (matches.isNotEmpty()) {
            val validEpochs = matches.mapNotNull { tryParseDateString(it) }
            if (validEpochs.isNotEmpty()) {
                // In insurance, if start & expiry dates are both present in period string, expiry is the later date!
                return validEpochs.maxOrNull()
            }
        }

        // 4. Fallback: split on whitespace/T (e.g. "28/11/2026 23:59:59")
        val firstToken = clean.split(Regex("[\\sT]"))[0].trim().trimEnd('>', '<', '.', ',', ')')
        tryParseDateString(firstToken)?.let { return it }

        return null
    }

    fun formatDateForDisplay(epochMs: Long): String {
        return java.time.Instant.ofEpochMilli(epochMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    fun parsePremium(raw: String): Double? {
        if (raw.isBlank()) return null
        val clean = raw
            .replace("₹", "")
            .replace("INR", "", ignoreCase = true)
            .replace("Rs.", "", ignoreCase = true)
            .replace("Rs", "", ignoreCase = true)
            .replace("/-", "")
            .replace("/=", "")
            .replace("/", "")
            .replace(",", "")
            .replace(Regex("[\\s\\u00A0\\u202F\\u2007\\uFEFF]"), "")
            .trim()
        return clean.toDoubleOrNull()
    }

    /**
     * Maps raw table rows into structured ParsedRows with validation
     */
    fun validateAndMapRows(
        rawRows: List<List<String>>,
        columnMapping: Map<Int, String>,
        existingRegNumbers: Set<String>
    ): ImportPreviewResult {
        val validList = mutableListOf<ParsedRow>()
        val warningList = mutableListOf<ParsedRow>()
        val errorList = mutableListOf<ParsedRow>()
        val duplicateList = mutableListOf<ParsedRow>()

        for ((index, row) in rawRows.withIndex()) {
            val rowNum = index + 2 // 1-indexed including header
            val data = mutableMapOf<String, String>()

            for ((colIndex, fieldKey) in columnMapping) {
                if (colIndex < row.size) {
                    data[fieldKey] = row[colIndex].trim()
                }
            }

            val regRaw = data["regNo"] ?: ""
            val name = data["name"] ?: ""
            val title = data["title"]?.ifBlank { null }
            val mobile = data["mobile"] ?: ""
            val email = data["email"]?.ifBlank { null }
            val address = data["address"] ?: ""
            val rawSector = data["sector"] ?: ""
            val rawInsurer = data["insurer"] ?: ""
            val policyNo = data["policyNumber"] ?: ""
            val expiryRaw = data["expiryDate"] ?: ""
            val premiumRaw = data["totalPremium"] ?: ""
            val rawMake = data["make"]?.trim()?.ifBlank { null }
            val rawModel = data["model"]?.trim()?.ifBlank { null }
            val rawMakeModel = data["makeModel"]?.trim()?.ifBlank { null }
            val makeModel = when {
                rawMake != null && rawModel != null -> if (rawModel.contains(rawMake, ignoreCase = true)) rawModel else "$rawMake $rawModel"
                rawMakeModel != null -> rawMakeModel
                rawMake != null -> rawMake
                rawModel != null -> rawModel
                else -> null
            }
            val vin = data["vin"]?.ifBlank { null }
            val engineNo = data["engineNo"]?.ifBlank { null }
            val notes = data["notes"]?.ifBlank { null }
            val saleDateRaw = data["saleDate"] ?: ""

            val errors = mutableListOf<String>()
            val warnings = mutableListOf<String>()

            // Validation
            val normalizedReg = RegistrationNormalizer.normalize(regRaw)
            if (normalizedReg.isEmpty()) {
                errors.add("Missing Registration Number")
            } else if (!RegistrationNormalizer.isStandardFormat(normalizedReg)) {
                warnings.add("Unusual registration format ($normalizedReg)")
            }

            if (!FieldValidators.isValidName(name)) {
                errors.add("Customer name must be at least 2 characters")
            }

            val normalizedMobile = FieldValidators.normalizeMobile(mobile)
            if (!FieldValidators.isValidMobile(normalizedMobile)) {
                errors.add("Invalid 10-digit mobile number ($mobile)")
            }

            val cleanAddress = address.trim()
            val finalAddress = cleanAddress.ifBlank { "Not Specified" }

            // Insurer & Sector matching
            var sector: Sector? = null
            var finalInsurer = rawInsurer

            if (rawSector.equals("PSU", ignoreCase = true)) {
                sector = Sector.PSU
            } else if (rawSector.equals("Private", ignoreCase = true)) {
                sector = Sector.PRIVATE
            }

            if (finalInsurer.isNotBlank()) {
                val matched = InsurerCatalog.matchInsurer(finalInsurer)
                if (matched != null) {
                    if (sector == null) sector = matched.first
                    finalInsurer = matched.second
                } else if (sector == null) {
                    sector = Sector.PRIVATE // Fallback default
                    warnings.add("Insurer '$finalInsurer' not in standard catalog; saved as Private Other")
                }
            } else {
                errors.add("Missing Insurer Name")
            }

            if (sector == null) {
                errors.add("Company sector must be PSU or Private")
            }

            if (!FieldValidators.isValidPolicyNumber(policyNo)) {
                errors.add("Invalid Policy Number (alphanumeric with / and -)")
            }

            val expiryMs = parseDateToEpochMs(expiryRaw)
            if (expiryMs == null) {
                errors.add("Expiry date '$expiryRaw' not recognized (use DD/MM/YYYY)")
            }

            val directPremium = parsePremium(data["totalPremium"] ?: "")
            val tpPremium = parsePremium(data["tpPremium"] ?: "")
            val odPremium = parsePremium(data["odPremium"] ?: "")

            val premium = when {
                directPremium != null && directPremium > 0.0 -> directPremium
                tpPremium != null && odPremium != null && (tpPremium + odPremium) > 0.0 -> (tpPremium + odPremium)
                tpPremium != null && tpPremium > 0.0 -> tpPremium
                odPremium != null && odPremium > 0.0 -> odPremium
                else -> null
            }
            if (premium == null || premium <= 0.0) {
                errors.add("Total Premium / TP Premium must be a valid number greater than 0")
            }

            val saleDateMs = if (saleDateRaw.isNotBlank()) parseDateToEpochMs(saleDateRaw) else null

            val isDuplicate = existingRegNumbers.contains(normalizedReg)

            val parsedRow = ParsedRow(
                rowNumber = rowNum,
                regNo = normalizedReg,
                customerName = name,
                title = title,
                mobile = normalizedMobile,
                email = email,
                address = finalAddress,
                sector = sector,
                insurer = finalInsurer,
                policyNumber = policyNo,
                expiryDateEpochMs = expiryMs,
                totalPremium = premium,
                makeModel = makeModel,
                vin = vin,
                engineNo = engineNo,
                notes = notes,
                saleDateEpochMs = saleDateMs,
                errors = errors,
                warnings = warnings,
                isDuplicate = isDuplicate
            )

            when {
                errors.isNotEmpty() -> errorList.add(parsedRow)
                isDuplicate -> duplicateList.add(parsedRow)
                warnings.isNotEmpty() -> warningList.add(parsedRow)
                else -> validList.add(parsedRow)
            }
        }

        return ImportPreviewResult(
            totalRows = rawRows.size,
            validRows = validList,
            warningRows = warningList,
            errorRows = errorList,
            duplicateRows = duplicateList
        )
    }
}
