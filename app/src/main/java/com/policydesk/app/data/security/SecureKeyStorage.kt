package com.policydesk.app.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

enum class AiProvider(val displayName: String, val defaultModel: String) {
    CLOUDFLARE_AI("Cloudflare Workers AI", "@cf/meta/llama-3.2-11b-vision-instruct"),
    GEMINI("Google Gemini", "gemini-3.5-flash-lite"),
    OPENAI("OpenAI", "gpt-4o-mini"),
    CLAUDE("Anthropic Claude", "claude-3-5-haiku-20241022")
}

data class AiConfig(
    val provider: AiProvider = AiProvider.CLOUDFLARE_AI,
    val apiKey: String = "",
    val model: String = AiProvider.CLOUDFLARE_AI.defaultModel,
    val customEndpoint: String = ""
) {
    val isConfigured: Boolean get() = provider == AiProvider.CLOUDFLARE_AI || apiKey.isNotBlank()
}

class SecureKeyStorage(context: Context) {

    private val prefs = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "policydesk_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Exception) {
        // shortcut: Clear and reinitialize if Android Keystore hardware keys were invalidated by OS update.
        context.deleteSharedPreferences("policydesk_secure_prefs")
        val fallbackMasterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "policydesk_secure_prefs",
            fallbackMasterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getAiConfig(): AiConfig {
        val providerStr = prefs.getString("ai_provider", AiProvider.CLOUDFLARE_AI.name) ?: AiProvider.CLOUDFLARE_AI.name
        val provider = try { AiProvider.valueOf(providerStr) } catch (_: Exception) { AiProvider.CLOUDFLARE_AI }
        val apiKey = prefs.getString("ai_api_key", "") ?: ""
        var model = prefs.getString("ai_model", provider.defaultModel) ?: provider.defaultModel
        val customEndpoint = prefs.getString("ai_custom_endpoint", "") ?: ""
        // Auto-migrate retired Gemini models to current gemini-3.5-flash-lite
        if (provider == AiProvider.GEMINI && (model.isBlank() || model.startsWith("gemini-1.") || model.startsWith("gemini-2."))) {
            model = "gemini-3.5-flash-lite"
        }
        return AiConfig(provider, apiKey, model, customEndpoint)
    }

    fun saveAiConfig(config: AiConfig) {
        val cleanModel = config.model.trim().removePrefix("models/").ifBlank { config.provider.defaultModel }
        prefs.edit()
            .putString("ai_provider", config.provider.name)
            .putString("ai_api_key", config.apiKey.trim())
            .putString("ai_model", cleanModel)
            .putString("ai_custom_endpoint", config.customEndpoint.trim())
            .apply()
    }

    fun removeAiKey() {
        prefs.edit()
            .remove("ai_api_key")
            .apply()
    }
}
