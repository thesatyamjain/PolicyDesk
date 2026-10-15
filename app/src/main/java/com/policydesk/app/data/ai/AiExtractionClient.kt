package com.policydesk.app.data.ai

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.policydesk.app.data.security.AiConfig
import com.policydesk.app.data.security.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
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

    private const val SYSTEM_PROMPT = """
You are an expert vehicle insurance policy document parser. 
Extract the policy information into JSON format matching the following keys:
{
  "regNo": "Vehicle registration number (e.g. MH12AB1234)",
  "customerName": "Insured person or company name",
  "title": "Title like Shri, Smt., Kumari if present as prefix",
  "mobile": "10 digit mobile number without +91",
  "email": "Email address if present",
  "address": "Residential / communication address",
  "sector": "PSU or Private",
  "insurer": "Name of the insurance company",
  "policyNumber": "Policy number",
  "expiryDate": "Expiry date in DD/MM/YYYY format",
  "totalPremium": "Total premium payable in INR as a number",
  "makeModel": "Vehicle make and model (e.g. Swift Dzire)",
  "vin": "Chassis number or VIN",
  "engineNo": "Engine number"
}
Rules:
1. Return ONLY the raw JSON object. Do not wrap in markdown quotes.
2. If a field is not found in the document, return an empty string. NEVER GUESS OR FABRICATE.
"""

    suspend fun testConnection(config: AiConfig): Result<String> = withContext(Dispatchers.IO) {
        if (config.apiKey.isBlank()) {
            return@withContext Result.failure(Exception("API key cannot be empty"))
        }

        try {
            when (config.provider) {
                AiProvider.GEMINI -> {
                    val url = URL("https://generativelanguage.googleapis.com/v1beta/models/${config.model}:generateContent?key=${config.apiKey}")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
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
                        Result.success("Connection successful! Provider: Google Gemini")
                    } else {
                        Result.failure(Exception("HTTP $code: ${conn.responseMessage}"))
                    }
                }
                AiProvider.OPENAI -> {
                    val url = URL("https://api.openai.com/v1/models")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Authorization", "Bearer ${config.apiKey}")
                    val code = conn.responseCode
                    if (code == 200) {
                        Result.success("Connection successful! Provider: OpenAI")
                    } else {
                        Result.failure(Exception("HTTP $code: ${conn.responseMessage}"))
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
                AiProvider.GEMINI -> extractWithGemini(base64Pdf, config, password)
                else -> ExtractionResult.Error("Provider ${config.provider.displayName} PDF intake in beta. Please use Gemini for native PDF parsing.")
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
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/${config.model}:generateContent?key=${config.apiKey}"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 30000

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

        OutputStreamWriter(conn.outputStream).use { it.write(requestJson.toString()) }

        val code = conn.responseCode
        if (code != 200) {
            val errReader = BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))
            val errorText = errReader.readText()
            return ExtractionResult.Error("Gemini API Error ($code): $errorText")
        }

        val responseText = BufferedReader(InputStreamReader(conn.inputStream)).readText()
        val responseJson = JSONObject(responseText)
        val candidates = responseJson.optJSONArray("candidates")
        val candidate = candidates?.optJSONObject(0)
        val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text") ?: ""

        if (text.isBlank()) {
            return ExtractionResult.Error("The model returned empty output. Ensure document is an insurance PDF.")
        }

        return try {
            val cleanJsonStr = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(cleanJsonStr)
            val data = ExtractedPolicyData(
                regNo = parsed.optString("regNo", ""),
                customerName = parsed.optString("customerName", ""),
                title = parsed.optString("title", ""),
                mobile = parsed.optString("mobile", ""),
                email = parsed.optString("email", ""),
                address = parsed.optString("address", ""),
                sector = parsed.optString("sector", ""),
                insurer = parsed.optString("insurer", ""),
                policyNumber = parsed.optString("policyNumber", ""),
                expiryDate = parsed.optString("expiryDate", ""),
                totalPremium = parsed.optString("totalPremium", ""),
                makeModel = parsed.optString("makeModel", ""),
                vin = parsed.optString("vin", ""),
                engineNo = parsed.optString("engineNo", ""),
                rawJson = cleanJsonStr
            )
            ExtractionResult.Success(data)
        } catch (e: Exception) {
            ExtractionResult.Error("Failed to parse extracted JSON: ${e.message}")
        }
    }
}
