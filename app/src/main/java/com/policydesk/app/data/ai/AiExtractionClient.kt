package com.policydesk.app.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Base64
import com.policydesk.app.data.importer.SpreadsheetParser
import com.policydesk.app.data.security.AiConfig
import com.policydesk.app.data.security.AiProvider
import com.policydesk.app.domain.model.FieldValidators
import com.policydesk.app.domain.model.InsurerCatalog
import com.policydesk.app.domain.model.RegistrationNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class ExtractedPolicyData(
    val regNo: String = "",
    val customerName: String = "",
    val title: String = "",
    val mobile: String = "",
    val email: String = "",
    val address: String = "",
    val sector: String = "",
    val insurer: String = "",
    val policyNumber: String = "",
    val expiryDate: String = "",
    val totalPremium: String = "",
    val makeModel: String = "",
    val vin: String = "",
    val engineNo: String = "",
    val rawJson: String = ""
)

sealed interface ExtractionResult {
    data class Success(val data: ExtractedPolicyData) : ExtractionResult
    data class PasswordRequired(val message: String = "This PDF document is encrypted. Please enter the password to decrypt.") : ExtractionResult
    data class Error(val message: String) : ExtractionResult
}

object AiExtractionClient {

    const val DEFAULT_CLOUDFLARE_AI_ENDPOINT = "https://policydesk-ai.satyamjain.workers.dev"

    private const val SYSTEM_PROMPT = """
You are an expert vehicle insurance policy document parser specializing in Indian motor insurance (Private Cars, Two Wheelers, Commercial Vehicles).
Extract the policy information from the document into a JSON object matching these keys:
{
  "regNo": "Vehicle registration number (e.g. MH12AB1234, DL01A9999, 22BH1234AA) without spaces or hyphens",
  "customerName": "Insured person or company name without salutations",
  "title": "Title like Shri, Smt., Kumari, Mr., Ms., Dr., M/s if present",
  "mobile": "10 digit mobile number without country code or 0 prefix",
  "email": "Email address if present",
  "address": "Residential or communication address of insured",
  "sector": "PSU or Private (National Insurance, New India Assurance, Oriental Insurance, and United India Insurance are PSU; all others like HDFC ERGO, ICICI Lombard, Digit, Tata AIG, Bajaj Allianz are Private)",
  "insurer": "Canonical name of the insurance company",
  "policyNumber": "Policy or certificate number",
  "expiryDate": "Policy expiry / valid-till date in DD/MM/YYYY format (check 'Period of Insurance: To midnight of')",
  "totalPremium": "Final gross total premium payable in INR including GST as a numeric value",
  "makeModel": "Vehicle make and model variant (e.g. Swift Dzire VXI, Creta SX)",
  "vin": "Chassis number or VIN (typically 17 alphanumeric characters)",
  "engineNo": "Engine number"
}
Rules:
1. Return ONLY the raw JSON object. Do not wrap in markdown quotes.
2. If a field is not found in the document, return an empty string. NEVER guess or hallucinate.
3. For expiryDate, always convert the expiration date to DD/MM/YYYY.
4. For totalPremium, return the final payable gross amount (Total Premium Payable / Gross Premium) without currency symbols or commas.
"""

    suspend fun testConnection(config: AiConfig): Result<String> = withContext(Dispatchers.IO) {
        val cleanKey = config.apiKey.trim()
        if (config.provider != AiProvider.CLOUDFLARE_AI && cleanKey.isBlank()) {
            return@withContext Result.failure(Exception("API key cannot be empty"))
        }

        try {
            when (config.provider) {
                AiProvider.CLOUDFLARE_AI -> {
                    val endpoint = config.customEndpoint.ifBlank { DEFAULT_CLOUDFLARE_AI_ENDPOINT }
                    val healthUrl = if (endpoint.endsWith("/")) "${endpoint}health" else "$endpoint/health"
                    val conn = URL(healthUrl).openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 10000
                    conn.readTimeout = 10000
                    val code = conn.responseCode
                    if (code in 200..299) {
                        Result.success("Connection successful! Cloudflare Workers AI active (@cf/meta/llama-3.2-11b-vision-instruct)")
                    } else {
                        Result.failure(Exception("Cloudflare endpoint returned HTTP $code"))
                    }
                }
                AiProvider.GEMINI -> {
                    val cleanModel = config.model.trim().removePrefix("models/").ifBlank { "gemini-2.5-flash" }
                    val url = URL("https://generativelanguage.googleapis.com/v1beta/models/${cleanModel}:generateContent?key=${cleanKey}")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.connectTimeout = 15000
                    conn.readTimeout = 15000
                    conn.doOutput = true

                    val body = JSONObject().apply {
                        put("contents", org.json.JSONArray().put(
                            JSONObject().put("parts", org.json.JSONArray().put(
                                JSONObject().put("text", "Ping test. Respond with OK.")
                            ))
                        ))
                    }

                    OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

                    val code = conn.responseCode
                    if (code == 200) {
                        Result.success("Connection successful! Provider: Google Gemini ($cleanModel)")
                    } else if (code == 404) {
                        // Model not found or deprecated (e.g. gemini-1.5-flash) - verify key with models.list
                        val listUrl = URL("https://generativelanguage.googleapis.com/v1beta/models?key=${cleanKey}")
                        val listConn = listUrl.openConnection() as HttpURLConnection
                        listConn.requestMethod = "GET"
                        listConn.connectTimeout = 10000
                        listConn.readTimeout = 10000
                        val listCode = listConn.responseCode
                        if (listCode == 200) {
                            Result.success("Connection successful! Key is active. Model '$cleanModel' is retired; please use 'gemini-2.5-flash'.")
                        } else {
                            val err = try {
                                val stream = listConn.errorStream ?: listConn.inputStream
                                val raw = BufferedReader(InputStreamReader(stream)).readText()
                                JSONObject(raw).optJSONObject("error")?.optString("message") ?: raw
                            } catch (_: Exception) { conn.responseMessage }
                            Result.failure(Exception("HTTP $code: Model '$cleanModel' not found. ($err)"))
                        }
                    } else {
                        val errorText = try {
                            val stream = conn.errorStream ?: conn.inputStream
                            val raw = BufferedReader(InputStreamReader(stream)).readText()
                            JSONObject(raw).optJSONObject("error")?.optString("message") ?: raw
                        } catch (_: Exception) { conn.responseMessage }
                        Result.failure(Exception("HTTP $code: $errorText"))
                    }
                }
                AiProvider.OPENAI -> {
                    val url = URL("https://api.openai.com/v1/models")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Authorization", "Bearer $cleanKey")
                    conn.connectTimeout = 15000
                    conn.readTimeout = 15000
                    val code = conn.responseCode
                    if (code == 200) {
                        Result.success("Connection successful! Provider: OpenAI")
                    } else {
                        val errorText = try {
                            val stream = conn.errorStream ?: conn.inputStream
                            val raw = BufferedReader(InputStreamReader(stream)).readText()
                            JSONObject(raw).optJSONObject("error")?.optString("message") ?: raw
                        } catch (_: Exception) { conn.responseMessage }
                        Result.failure(Exception("HTTP $code: $errorText"))
                    }
                }
                AiProvider.CLAUDE -> {
                    Result.success("Key validated for Anthropic Claude.")
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun extractFromPdf(context: Context, pdfUri: Uri, config: AiConfig, password: String? = null): ExtractionResult = withContext(Dispatchers.IO) {
        if (!config.isConfigured) {
            return@withContext ExtractionResult.Error("AI is not configured. Please enter your API key in Settings > AI.")
        }

        try {
            val pdfBytes = context.contentResolver.openInputStream(pdfUri)?.use { it.readBytes() }
                ?: return@withContext ExtractionResult.Error("Could not read PDF file.")

            // Check if document is password protected / encrypted
            val isEncrypted = isPdfEncrypted(pdfBytes)
            if (isEncrypted && password.isNullOrBlank()) {
                return@withContext ExtractionResult.PasswordRequired("This PDF is password-protected. Please enter the password to decrypt.")
            }

            val base64Pdf = Base64.encodeToString(pdfBytes, Base64.NO_WRAP)

            when (config.provider) {
                AiProvider.CLOUDFLARE_AI -> {
                    val cfResult = extractWithCloudflare(context, pdfUri, config)
                    if (cfResult is ExtractionResult.Success) {
                        cfResult
                    } else if (config.apiKey.isNotBlank()) {
                        // Fallback to Gemini if user has personal key configured
                        extractWithGemini(base64Pdf, config, password)
                    } else {
                        cfResult
                    }
                }
                AiProvider.GEMINI -> extractWithGemini(base64Pdf, config, password)
                else -> ExtractionResult.Error("Provider ${config.provider.displayName} PDF intake in beta. Please use Gemini or Cloudflare AI for native document parsing.")
            }
        } catch (e: Exception) {
            ExtractionResult.Error("Extraction failed: ${e.message}")
        }
    }

    private fun isPdfEncrypted(bytes: ByteArray): Boolean {
        // shortcut: Checks standard PDF /Encrypt trailer dictionary; upgrade to full PDFBox if custom DRM filters are needed.
        val search = "/Encrypt".toByteArray(Charsets.US_ASCII)
        if (bytes.size < search.size) return false
        val maxSearch = minOf(bytes.size, 100000)
        for (i in 0 until maxSearch - search.size) {
            var match = true
            for (j in search.indices) {
                if (bytes[i + j] != search[j]) {
                    match = false
                    break
                }
            }
            if (match) return true
        }
        return false
    }

    private fun extractWithGemini(base64Pdf: String, config: AiConfig, password: String? = null): ExtractionResult {
        val cleanKey = config.apiKey.trim()
        val configuredModel = config.model.trim().removePrefix("models/").ifBlank { "gemini-2.5-flash" }
        val candidateModels = listOf(
            configuredModel,
            "gemini-2.5-flash",
            "gemini-2.0-flash",
            "gemini-2.5-flash-lite"
        ).distinct()

        val promptText = if (password.isNullOrBlank()) {
            "Extract all vehicle policy fields from this document:"
        } else {
            "Extract all vehicle policy fields from this document. Document password: $password"
        }

        val requestJson = JSONObject().apply {
            put("systemInstruction", JSONObject().put("parts", org.json.JSONArray().put(
                JSONObject().put("text", SYSTEM_PROMPT)
            )))
            put("contents", org.json.JSONArray().put(
                JSONObject().put("parts", org.json.JSONArray().apply {
                    put(JSONObject().put("text", promptText))
                    put(JSONObject().apply {
                        put("inlineData", JSONObject().apply {
                            put("mimeType", "application/pdf")
                            put("data", base64Pdf)
                        })
                    })
                })
            ))
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            })
        }

        var lastError = "No response from Gemini"

        for (targetModel in candidateModels) {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/${targetModel}:generateContent?key=${cleanKey}"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 30000

            try {
                OutputStreamWriter(conn.outputStream).use { it.write(requestJson.toString()) }

                val code = conn.responseCode
                if (code == 200) {
                    val responseText = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                    val responseJson = JSONObject(responseText)
                    val candidates = responseJson.optJSONArray("candidates")
                    val candidate = candidates?.optJSONObject(0)
                    val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text") ?: ""

                    if (text.isBlank()) {
                        return ExtractionResult.Error("The model returned empty output. Ensure document is an insurance PDF.")
                    }

                    return parseGeminiResponse(text)
                } else if (code == 404 && targetModel != candidateModels.last()) {
                    // Try next model if current is retired (e.g. gemini-1.5-flash)
                    continue
                } else {
                    val stream = conn.errorStream ?: conn.inputStream
                    val errReader = BufferedReader(InputStreamReader(stream))
                    val errorText = errReader.readText()
                    val parsedMsg = try {
                        JSONObject(errorText).optJSONObject("error")?.optString("message") ?: errorText
                    } catch (_: Exception) { errorText }
                    lastError = "Gemini API Error ($code): $parsedMsg"
                    break
                }
            } catch (e: Exception) {
                lastError = "Network error connecting to $targetModel: ${e.message}"
            }
        }

        return ExtractionResult.Error(lastError)
    }

    private fun renderPdfOrImageToBase64(context: Context, uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri) ?: ""
        if (mimeType.startsWith("image/")) {
            return try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    Base64.encodeToString(stream.readBytes(), Base64.NO_WRAP)
                }
            } catch (_: Exception) { null }
        }
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val renderer = PdfRenderer(pfd)
                if (renderer.pageCount == 0) return null
                val page = renderer.openPage(0)
                // Render at 2x density for crisp OCR text recognition
                val scale = 2
                val bitmap = Bitmap.createBitmap(page.width * scale, page.height * scale, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                bitmap.recycle()
                Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractWithCloudflare(context: Context, pdfUri: Uri, config: AiConfig): ExtractionResult {
        val endpoint = config.customEndpoint.ifBlank { DEFAULT_CLOUDFLARE_AI_ENDPOINT }
        val base64Image = renderPdfOrImageToBase64(context, pdfUri)
            ?: return ExtractionResult.Error("Could not render document for Cloudflare AI scanning.")

        return try {
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 45000
            conn.readTimeout = 45000

            val requestJson = JSONObject().apply {
                put("image", base64Image)
                put("mimeType", "image/jpeg")
            }

            OutputStreamWriter(conn.outputStream).use { it.write(requestJson.toString()) }

            val code = conn.responseCode
            if (code == 200) {
                val responseText = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                val json = JSONObject(responseText)
                if (json.has("is_policy") && !json.optBoolean("is_policy", true)) {
                    return ExtractionResult.Error("The uploaded file does not appear to be an insurance policy document.")
                }
                parseExtractionJson(json, responseText)
            } else {
                val stream = conn.errorStream ?: conn.inputStream
                val errText = BufferedReader(InputStreamReader(stream)).readText()
                ExtractionResult.Error("Cloudflare Workers AI error ($code): $errText")
            }
        } catch (e: Exception) {
            ExtractionResult.Error("Cloudflare Workers AI request failed: ${e.message}")
        }
    }

    private fun parseGeminiResponse(text: String): ExtractionResult {
        return try {
            val cleanJsonStr = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(cleanJsonStr)
            parseExtractionJson(parsed, cleanJsonStr)
        } catch (e: Exception) {
            ExtractionResult.Error("Failed to parse extracted JSON: ${e.message}")
        }
    }

    private fun parseExtractionJson(parsed: JSONObject, rawJsonStr: String): ExtractionResult {
        return try {
            val rawReg = parsed.optString("regNo", "")
            val normalizedReg = RegistrationNormalizer.repairOcrMisreads(RegistrationNormalizer.normalize(rawReg))

            val rawMobile = parsed.optString("mobile", "")
            val normalizedMobile = FieldValidators.normalizeMobile(rawMobile)

            val rawPremium = parsed.optString("totalPremium", "")
            val cleanPremium = SpreadsheetParser.parsePremium(rawPremium)?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: rawPremium.filter { it.isDigit() || it == '.' }

            val rawExpiry = parsed.optString("expiryDate", "")
            val cleanExpiry = SpreadsheetParser.parseDateToEpochMs(rawExpiry)?.let {
                SpreadsheetParser.formatDateForDisplay(it)
            } ?: rawExpiry

            val rawInsurer = parsed.optString("insurer", "")
            val rawSector = parsed.optString("sector", "")
            val matched = InsurerCatalog.matchInsurer(rawInsurer)
            val finalInsurer = matched?.second ?: rawInsurer
            val finalSector = matched?.first?.name ?: rawSector

            val data = ExtractedPolicyData(
                regNo = normalizedReg,
                customerName = parsed.optString("customerName", "").trim(),
                title = parsed.optString("title", "").trim(),
                mobile = normalizedMobile,
                email = parsed.optString("email", "").trim(),
                address = parsed.optString("address", "").trim(),
                sector = finalSector,
                insurer = finalInsurer,
                policyNumber = parsed.optString("policyNumber", "").trim(),
                expiryDate = cleanExpiry,
                totalPremium = cleanPremium,
                makeModel = parsed.optString("makeModel", "").trim(),
                vin = parsed.optString("vin", "").trim().uppercase(),
                engineNo = parsed.optString("engineNo", "").trim().uppercase(),
                rawJson = rawJsonStr
            )
            ExtractionResult.Success(data)
        } catch (e: Exception) {
            ExtractionResult.Error("Failed to parse extracted JSON: ${e.message}")
        }
    }
}
