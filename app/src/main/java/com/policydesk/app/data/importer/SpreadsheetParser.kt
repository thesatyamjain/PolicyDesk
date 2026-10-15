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
            } else if (name == "xl/worksheets/sheet1.xml") {
                sheet1Bytes = zip.readBytes()
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

    private fun parseSheetXml(stream: InputStream, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var eventType = parser.eventType
        var currentRow = mutableListOf<String>()
        var cellType: String? = null
        var inV = false
        var cellValue = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name == "row") {
                        currentRow = mutableListOf()
                    } else if (name == "c") {
                        cellType = parser.getAttributeValue(null, "t")
                    } else if (name == "v") {
                        inV = true
                        cellValue = ""
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inV) cellValue += parser.text
                }
                XmlPullParser.END_TAG -> {
                    if (name == "v") {
                        inV = false
                        val finalVal = if (cellType == "s") {
                            val idx = cellValue.toIntOrNull() ?: -1
                            if (idx in sharedStrings.indices) sharedStrings[idx] else cellValue
                        } else {
                            cellValue
                        }
                        currentRow.add(finalVal)
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

        // 1. Try Excel numeric serial date (e.g. 45000)
        val num = clean.toDoubleOrNull()
        if (num != null && num > 20000 && num < 70000) {
            // Excel epoch is 1899-12-30
            val days = num.toLong() - 25569
            return days * 86400000L
        }

        // 2. Try common formats
        val patterns = listOf(
            "dd/MM/yyyy", "d/M/yyyy", "dd-MM-yyyy", "d-M-yyyy",
            "yyyy-MM-dd", "dd.MM.yyyy", "yyyy/MM/dd"
        )
        for (pattern in patterns) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern)
                val date = LocalDate.parse(clean, formatter)
                return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {}
        }
        return null
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
            val makeModel = data["makeModel"]?.ifBlank { null }
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

            if (!FieldValidators.isValidMobile(mobile)) {
                errors.add("Invalid 10-digit mobile number ($mobile)")
            }

            if (address.isBlank()) {
                errors.add("Missing Residential Address")
            }

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

            val premium = premiumRaw.replace(",", "").toDoubleOrNull()
            if (premium == null || premium <= 0.0) {
                errors.add("Total Premium must be a valid number greater than 0")
            }

            val saleDateMs = if (saleDateRaw.isNotBlank()) parseDateToEpochMs(saleDateRaw) else null

            val isDuplicate = existingRegNumbers.contains(normalizedReg)

            val parsedRow = ParsedRow(
                rowNumber = rowNum,
                regNo = normalizedReg,
                customerName = name,
                title = title,
                mobile = FieldValidators.normalizeMobile(mobile),
                email = email,
                address = address,
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
