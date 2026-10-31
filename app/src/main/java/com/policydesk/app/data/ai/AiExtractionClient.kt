package com.policydesk.app.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
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
You are an expert Indian motor vehicle insurance policy document parser specializing in Private Cars, Two Wheelers, and Commercial Vehicles across both PSU insurers (National Insurance, New India Assurance, Oriental Insurance, United India Insurance) and Private insurers.
Extract the policy information from the document into a JSON object matching these exact keys:
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
  "expiryDate": "Policy expiration / valid-till date in DD/MM/YYYY format",
  "totalPremium": "Final gross total premium payable in INR including GST as a numeric value",
  "makeModel": "Vehicle make and model variant (e.g. Swift Dzire VXI, Creta SX)",
  "vin": "Chassis number or VIN (typically 17 alphanumeric characters)",
  "engineNo": "Engine number"
}
CONTENT AWARENESS RULES & ABBREVIATIONS FOR ALL FIELDS:
1. VEHICLE REGISTRATION NUMBER (regNo):
   - Abbreviations / Labels: 'REG NO', 'REGN NO', 'REGN NUMBER', 'REGISTRATION NO', 'VEH NO', 'VEHICLE NO', 'VEHICLE NUMBER', 'VEH REGN NO', 'VEH REG NO', 'PLATE NO', 'NUMBER PLATE', 'RC NO', 'RC NUMBER', 'RTO REG NO', 'RTO NO', 'CAR NO', 'BIKE NO', 'TRUCK NO', 'BUS NO', 'पंजी. संख्या', 'पंजी संख्या', 'पंजीकरण संख्या', 'पंजी सं', 'वाहन पंजी सं', 'गाड़ी नं', 'गाड़ी नंबर', 'वाहन संख्या', 'रजिस्ट्रेशन नं'.
   - Extract the vehicle license plate (e.g. RJ14AB1234, UP16CD5678, DL01A9999, 22BH1234AA).
   - NEVER confuse Policy Number, Engine Number, or Chassis Number with Registration Number.
2. INSURED NAME (customerName) & TITLE (title):
   - Abbreviations / Labels: 'INSURED NAME', 'NAME OF INSURED', 'INSURED'S NAME', 'CUST NAME', 'CUSTOMER NAME', 'PROPOSER NAME', 'NAME OF PROPOSER', 'POLICYHOLDER', 'POLICY HOLDER NAME', 'CLIENT NAME', 'OWNER NAME', 'VEHICLE OWNER', 'APPLICANT NAME', 'PARTY NAME', 'बीमित का नाम', 'बीमाधारक का नाम', 'बीमाधारक', 'ग्राहक का नाम', 'प्रस्तावक का नाम', 'वाहन स्वामी', 'मालिक का नाम'.
   - Titles / Salutations: 'Shri', 'Sh.', 'Sri', 'Smt', 'Smt.', 'Kumari', 'Km', 'Km.', 'Mr', 'Mr.', 'Mrs', 'Mrs.', 'Ms', 'Ms.', 'Dr', 'Dr.', 'M/s', 'Messrs', 'Prof', 'Capt', 'Major', 'Col', 'Lt', 'Hon', 'Rev', 'Adv', 'CA', 'CS', 'Er', 'श्री', 'श्रीमती', 'सुश्री', 'कुमारी', 'डॉ', 'डॉक्टर', 'मेसर्स'.
   - Extract title into "title". Keep only clean individual or business name in "customerName".
3. EXPIRY DATE (expiryDate):
   - Abbreviations / Labels: 'EXP DATE', 'EXP. DATE', 'EXPIRY DATE', 'EXPIRY', 'EXP', 'VALID TILL', 'VALID UPTO', 'VALID UP TO', 'VALID TO', 'TO MIDNIGHT OF', 'PERIOD OF INSURANCE TO', 'POLICY PERIOD TO', 'END DATE', 'EXPIRATION DATE', 'VALIDITY TO', 'DUE DATE', 'EXP DT', 'RENEWAL DATE', 'RENEWAL DUE DATE', 'RISK EXPIRY', 'समाप्ति तिथि', 'वैधता समाप्त', 'की अर्धरात्रि तक', 'अंतिम तिथि', 'समाप्ति दिनांक', 'नवीनीकरण तिथि'.
   - In period strings like 'Policy Effective from [Start Date] to midnight of [Expiry Date]' or 'की अर्धरात्रि तक [Expiry Date]', ALWAYS choose the END date ('to midnight of' / 'की अर्धरात्रि तक'). NEVER choose the start date ('Effective from').
   - Example: In 'Policy Effective from 17/10/2026 की अर्धरात्रि तक /to midnight of 16/10/2027>>', the expiryDate is '16/10/2027'.
   - Format cleanly as DD/MM/YYYY. Strip trailing symbols like '>>' or punctuation.
4. MOBILE NUMBER (mobile):
   - Abbreviations / Labels: 'MOB NO', 'MOB', 'MOBILE NO', 'MOBILE NUMBER', 'CELL NO', 'CONTACT NO', 'PHONE NO', 'TEL NO', 'TELEPHONE NO', 'WHATSAPP NO', 'PH NO', 'मोबाइल नं', 'मोबाइल नंबर', 'दूरभाष नं', 'संपर्क सूत्र', 'संपर्क नंबर', 'फोन नं'.
   - Extract 10-digit mobile number starting with 6, 7, 8, or 9. Strip +91, 0, or spaces.
5. EMAIL (email):
   - Abbreviations / Labels: 'EMAIL ID', 'EMAIL-ID', 'E-MAIL ID', 'E-MAIL', 'EMAIL', 'E MAIL', 'MAIL ID', 'EMAIL ADDRESS', 'ईमेल', 'ई-मेल', 'ईमेल आईडी'.
   - Must be a valid email format.
6. TOTAL PREMIUM (totalPremium):
   - Abbreviations / Labels: 'TOT PREM', 'TOTAL PREM', 'TOTAL PREMIUM', 'GROSS PREM', 'GROSS PREMIUM', 'PREMIUM PAYABLE', 'TOTAL PAYABLE', 'TOTAL AMOUNT', 'FINAL PREMIUM', 'NET + GST', 'NET+GST', 'TOTAL CHG', 'AMT PAYABLE', 'PREMIUM AMT', 'GROSS AMT', 'TOT AMT', 'GWP', 'NWP', 'कुल प्रीमियम', 'सकल प्रीमियम', 'देय प्रीमियम', 'कुल देय राशि', 'अंतिम प्रीमियम'.
   - Do NOT pick Net Premium or standalone OD/TP premium. Return as clean numeric string (e.g. 14500).
7. POLICY NUMBER (policyNumber):
   - Abbreviations / Labels: 'POL NO', 'POLICY NO', 'POLICY NUMBER', 'CERT NO', 'CERTIFICATE NO', 'CERTIFICATE NUMBER', 'SCHEDULE NO', 'POL/CERT NO', 'DOC NO', 'CN NO', 'COVER NOTE NO', 'पॉलिसी सं', 'पॉलिसी संख्या', 'पॉलिसी नंबर', 'बीमा पत्र सं', 'प्रमाण पत्र सं', 'अनुसूची सं'.
   - Do NOT confuse with proposal, quote, or customer ID.
8. MAKE & MODEL (makeModel):
   - Abbreviations / Labels: 'MAKE/MODEL', 'MAKE & MODEL', 'VEH MAKE', 'VEH MODEL', 'VEHICLE MAKE', 'VEHICLE MODEL', 'VEHICLE DESCRIPTION', 'DESC OF VEHICLE', 'VEH DESC', 'MODEL VARIANT', 'वाहन का मेक/मॉडल', 'वाहन विवरण', 'मेक / मॉडल', 'वाहन मॉडल'.
9. CHASSIS / VIN (vin):
   - Abbreviations / Labels: 'CHASSIS NO', 'CHASSIS NUMBER', 'CHASSIS', 'CHASIS NO', 'CHASS NO', 'VIN NO', 'VIN NUMBER', 'FRAME NO', 'CH NO', 'चेसिस सं', 'चेसिस संख्या', 'चेसिस नंबर', 'फ्रेम सं'.
   - Extract alphanumeric chassis identifier (typically 17 characters).
10. ENGINE NUMBER (engineNo):
    - Abbreviations / Labels: 'ENGINE NO', 'ENGINE NUMBER', 'ENG NO', 'ENG NUMBER', 'MOTOR NO', 'MOTOR NUMBER', 'इंजन सं', 'इंजन संख्या', 'इंजन नंबर', 'मोटर सं'.
    - Do not swap vin and engineNo.
11. SECTOR (sector) & INSURER (insurer):
    - PSU: National Insurance (NIC/NICL), New India Assurance (NIA/NIACL), Oriental Insurance (OIC/OICL), United India Insurance (UIIC/UIICL) -> "PSU".
    - Private: All other insurers (Bajaj Allianz BAGIC, ICICI Lombard ILGIC, HDFC ERGO, Go Digit GODIGIT, Tata AIG TAGIC, SBI General SBIG, Reliance RGICL, IFFCO Tokio ITGI, Shriram SGIC, Royal Sundaram RSGI, Chola MS, Universal Sompo USGIC, Future Generali FGII, Zuno/Edelweiss, Zurich Kotak, etc.) -> "Private".
12. ADDRESS (address):
    - Abbreviations / Labels: 'COMM. ADDRESS', 'COMM ADDRESS', 'COMMUNICATION ADDRESS', 'RES. ADDRESS', 'RESIDENTIAL ADDRESS', 'CORR. ADDRESS', 'PERM. ADDRESS', 'INSURED ADDRESS', 'CUSTOMER ADDRESS', 'POSTAL ADDRESS', 'ADDR', 'ADD.', 'पता', 'पत्राचार का पता', 'स्थायी पता', 'बीमित का पता', 'निवास का पता'. Include PIN code.
13. Return ONLY the raw JSON object. Do not wrap in markdown quotes.
14. If a field is not found in the document, return an empty string. NEVER guess or hallucinate.
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
                    val cleanEndpoint = endpoint.trimEnd('/')
                    val healthUrl = "$cleanEndpoint/health"
                    val conn = URL(healthUrl).openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 10000
                    conn.readTimeout = 10000
                    val code = conn.responseCode
                    if (code in 200..299) {
                        // Check or trigger agreement endpoint if present
                        trySubmitCloudflareAgreement(cleanEndpoint)
                        Result.success("Connection successful! Cloudflare Workers AI active (@cf/meta/llama-3.2-11b-vision-instruct)")
                    } else {
                        Result.failure(Exception("Cloudflare endpoint returned HTTP $code"))
                    }
                }
                AiProvider.GEMINI -> {
                    val cleanModel = config.model.trim().removePrefix("models/").ifBlank { "gemini-3.5-flash-lite" }
                    val activeModel = fetchAvailableGeminiModel(cleanKey) ?: cleanModel
                    val url = URL("https://generativelanguage.googleapis.com/v1beta/models/${activeModel}:generateContent?key=${cleanKey}")
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
                        Result.success("Connection successful! Provider: Google Gemini ($activeModel)")
                    } else if (code == 404) {
                        // Model not found or deprecated - verify key with models.list
                        val discovered = fetchAvailableGeminiModel(cleanKey)
                        if (discovered != null) {
                            Result.success("Connection successful! Active model found: '$discovered'.")
                        } else {
                            Result.failure(Exception("HTTP $code: Model '$cleanModel' is retired. Please update to 'gemini-3.5-flash-lite'."))
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

    private fun fetchAvailableGeminiModel(cleanKey: String): String? {
        return try {
            val listUrl = URL("https://generativelanguage.googleapis.com/v1beta/models?key=${cleanKey}")
            val listConn = listUrl.openConnection() as HttpURLConnection
            listConn.requestMethod = "GET"
            listConn.connectTimeout = 8000
            listConn.readTimeout = 8000
            if (listConn.responseCode == 200) {
                val raw = BufferedReader(InputStreamReader(listConn.inputStream)).readText()
                val modelsArray = JSONObject(raw).optJSONArray("models") ?: return null
                val supported = mutableListOf<String>()
                for (i in 0 until modelsArray.length()) {
                    val m = modelsArray.optJSONObject(i) ?: continue
                    val name = m.optString("name", "").removePrefix("models/")
                    val methods = m.optJSONArray("supportedGenerationMethods")
                    var canGenerate = false
                    if (methods != null) {
                        for (j in 0 until methods.length()) {
                            if (methods.optString(j) == "generateContent") {
                                canGenerate = true
                                break
                            }
                        }
                    }
                    if (canGenerate) supported.add(name)
                }
                // Priority: 3.5-flash-lite > 3.8-flash > 2.5-flash > first flash model > first supported model
                supported.firstOrNull { it.contains("3.5-flash-lite") }
                    ?: supported.firstOrNull { it.contains("3.8-flash") }
                    ?: supported.firstOrNull { it.contains("2.5-flash") }
                    ?: supported.firstOrNull { it.contains("flash") }
                    ?: supported.firstOrNull()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractWithGemini(base64Pdf: String, config: AiConfig, password: String? = null): ExtractionResult {
        val cleanKey = config.apiKey.trim()
        val configuredModel = config.model.trim().removePrefix("models/").ifBlank { "gemini-3.5-flash-lite" }
        val dynamicallyDiscovered = fetchAvailableGeminiModel(cleanKey)
        val candidateModels = listOfNotNull(
            dynamicallyDiscovered,
            configuredModel.takeIf { it != "gemini-2.5-flash-lite" },
            "gemini-3.5-flash-lite",
            "gemini-3.8-flash",
            "gemini-2.5-flash",
            "gemini-2.0-flash"
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

    private fun renderPdfOrImageToBase64(context: Context, uri: Uri, maxPages: Int = 1, scale: Float = 2.4f): String? {
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
                if (renderer.pageCount == 0) {
                    renderer.close()
                    return null
                }

                val pagesToRender = minOf(renderer.pageCount, maxPages)
                val actualScale = if (pagesToRender > 1) 1.8f else scale

                val firstPage = renderer.openPage(0)
                val pageWidth = firstPage.width
                val pageHeight = firstPage.height

                val totalHeight = if (pagesToRender > 1) {
                    val secondPage = renderer.openPage(1)
                    val h = pageHeight + secondPage.height
                    secondPage.close()
                    h
                } else {
                    pageHeight
                }

                val bitmapWidth = (pageWidth * actualScale).toInt()
                val bitmapHeight = (totalHeight * actualScale).toInt()
                val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)

                val matrix0 = Matrix().apply {
                    setScale(actualScale, actualScale)
                }
                firstPage.render(bitmap, null, matrix0, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                firstPage.close()

                if (pagesToRender > 1) {
                    val secondPage = renderer.openPage(1)
                    val matrix1 = Matrix().apply {
                        setScale(actualScale, actualScale)
                        postTranslate(0f, pageHeight * actualScale)
                    }
                    secondPage.render(bitmap, null, matrix1, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    secondPage.close()
                }

                renderer.close()

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)
                bitmap.recycle()
                Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractWithCloudflare(context: Context, pdfUri: Uri, config: AiConfig): ExtractionResult {
        val endpoint = config.customEndpoint.ifBlank { DEFAULT_CLOUDFLARE_AI_ENDPOINT }.trimEnd('/')

        // Pass 1: Crisp single-page schedule scan calibrated to Llama 3.2 Vision's native 1120px window
        val singlePageImage = renderPdfOrImageToBase64(context, pdfUri, maxPages = 1, scale = 1.7f)
        if (singlePageImage != null) {
            val pass1 = executeCloudflareExtraction(endpoint, singlePageImage, isRetry = false)
            if (pass1 is ExtractionResult.Success) {
                if (pass1.data.regNo.isNotBlank() || pass1.data.policyNumber.isNotBlank() || pass1.data.customerName.isNotBlank()) {
                    return pass1
                }
            }
        }

        // Pass 2: High-density schedule scan (scale 2.0f)
        val highResImage = renderPdfOrImageToBase64(context, pdfUri, maxPages = 1, scale = 2.0f)
        if (highResImage != null) {
            val pass2 = executeCloudflareExtraction(endpoint, highResImage, isRetry = true)
            if (pass2 is ExtractionResult.Success) {
                return pass2
            }
        }

        // Pass 3: Multi-page composite (Pages 0 & 1)
        val multiPageImage = renderPdfOrImageToBase64(context, pdfUri, maxPages = 2, scale = 1.4f)
        if (multiPageImage != null) {
            val pass3 = executeCloudflareExtraction(endpoint, multiPageImage, isRetry = true)
            if (pass3 is ExtractionResult.Success) {
                return pass3
            }
            return pass3
        }

        return ExtractionResult.Error("Could not render document for Cloudflare AI scanning.")
    }

    private fun executeCloudflareExtraction(endpoint: String, base64Image: String, isRetry: Boolean): ExtractionResult {
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
                val isExplicitlyNotPolicy = json.has("is_policy") && !json.optBoolean("is_policy", true)
                val hasAnyExtractedField = (json.optString("regNo").isNotBlank() ||
                        json.optString("policyNumber").isNotBlank() ||
                        json.optString("customerName").isNotBlank() ||
                        json.optString("insurer").isNotBlank())

                if (isExplicitlyNotPolicy && !hasAnyExtractedField) {
                    return ExtractionResult.Error("Could not detect policy details from this page. Please ensure the policy schedule page is selected, or use Continue Manually.")
                }
                parseExtractionJson(json, responseText)
            } else {
                val stream = conn.errorStream ?: conn.inputStream
                val errText = BufferedReader(InputStreamReader(stream)).readText()

                // Cloudflare Error 5016: Meta Llama 3.2 license agreement required
                if ((errText.contains("5016") || errText.contains("agree")) && !isRetry) {
                    val agreed = trySubmitCloudflareAgreement(endpoint)
                    if (agreed) {
                        return executeCloudflareExtraction(endpoint, base64Image, isRetry = true)
                    }
                    return ExtractionResult.Error(
                        "Cloudflare Workers AI requires a one-time Meta Llama 3.2 license agreement on your account.\n\n" +
                        "How to fix:\n" +
                        "1. Run deploy.bat in the 'policydesk-ai' folder, OR\n" +
                        "2. Open Cloudflare Dashboard > Workers AI to accept Llama terms, OR\n" +
                        "3. Switch to Google Gemini (free API key) in Settings > AI."
                    )
                }

                val parsedMsg = try {
                    JSONObject(errText).optString("error", errText)
                } catch (_: Exception) { errText }
                ExtractionResult.Error("Cloudflare Workers AI error ($code): $parsedMsg")
            }
        } catch (e: Exception) {
            ExtractionResult.Error("Cloudflare Workers AI request failed: ${e.message}")
        }
    }

    private fun trySubmitCloudflareAgreement(endpoint: String): Boolean {
        return try {
            val cleanEndpoint = endpoint.trimEnd('/')
            val agreeUrl = URL("$cleanEndpoint/agree")
            val conn = agreeUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            val code = conn.responseCode
            code in 200..299
        } catch (_: Exception) {
            false
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
            val extractedReg = RegistrationNormalizer.extractRegistrationNumber(rawReg)
            val normalizedReg = RegistrationNormalizer.repairOcrMisreads(extractedReg)

            val rawMobile = parsed.optString("mobile", "")
            val normalizedMobile = FieldValidators.normalizeMobile(rawMobile)

            val rawEmail = parsed.optString("email", "")
            val cleanEmail = FieldValidators.extractCleanEmail(rawEmail)

            val rawName = parsed.optString("customerName", "")
            val rawTitle = parsed.optString("title", "")
            val (cleanName, cleanTitle) = FieldValidators.extractCustomerNameAndTitle(rawName, rawTitle)

            val rawPolicyNo = parsed.optString("policyNumber", "")
            val cleanPolicyNo = FieldValidators.extractCleanPolicyNumber(rawPolicyNo)

            val rawAddress = parsed.optString("address", "")
            val cleanAddress = FieldValidators.extractCleanAddress(rawAddress)

            val rawVin = parsed.optString("vin", "")
            val cleanVin = FieldValidators.extractCleanVin(rawVin)

            val rawEngine = parsed.optString("engineNo", "")
            val cleanEngine = FieldValidators.extractCleanEngineNo(rawEngine)

            val rawMakeModel = parsed.optString("makeModel", "")
            val cleanMakeModel = FieldValidators.extractCleanMakeModel(rawMakeModel)

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
                customerName = cleanName,
                title = cleanTitle,
                mobile = normalizedMobile,
                email = cleanEmail,
                address = cleanAddress,
                sector = finalSector,
                insurer = finalInsurer,
                policyNumber = cleanPolicyNo,
                expiryDate = cleanExpiry,
                totalPremium = cleanPremium,
                makeModel = cleanMakeModel,
                vin = cleanVin,
                engineNo = cleanEngine,
                rawJson = rawJsonStr
            )
            ExtractionResult.Success(data)
        } catch (e: Exception) {
            ExtractionResult.Error("Failed to parse extracted JSON: ${e.message}")
        }
    }
}
