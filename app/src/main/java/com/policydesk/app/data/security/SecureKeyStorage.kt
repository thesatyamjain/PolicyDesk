package com.policydesk.app.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

enum class AiProvider(val displayName: String, val defaultModel: String) {
    GEMINI("Google Gemini", "gemini-2.5-flash"),
    OPENAI("OpenAI", "gpt-4o-mini"),
    CLAUDE("Anthropic Claude", "claude-3-5-haiku-20241022")
}

data class AiConfig(
    val provider: AiProvider = AiProvider.GEMINI,
    val apiKey: String = "",
    val model: String = AiProvider.GEMINI.defaultModel
) {
    val isConfigured: Boolean get() = apiKey.isNotBlank()
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
        val providerStr = prefs.getString("ai_provider", AiProvider.GEMINI.name) ?: AiProvider.GEMINI.name
        val provider = try { AiProvider.valueOf(providerStr) } catch (_: Exception) { AiProvider.GEMINI }
        val apiKey = prefs.getString("ai_api_key", "") ?: ""
        var model = prefs.getString("ai_model", provider.defaultModel) ?: provider.defaultModel
        // Auto-migrate retired Gemini 1.5 models to stable gemini-2.5-flash
        if (provider == AiProvider.GEMINI && (model.isBlank() || model == "gemini-1.5-flash" || model == "models/gemini-1.5-flash")) {
            model = "gemini-2.5-flash"
        }
        return AiConfig(provider, apiKey, model)
    }

    fun saveAiConfig(config: AiConfig) {
        val cleanModel = config.model.trim().removePrefix("models/").ifBlank { config.provider.defaultModel }
        prefs.edit()
            .putString("ai_provider", config.provider.name)
            .putString("ai_api_key", config.apiKey.trim())
            .putString("ai_model", cleanModel)
            .apply()
    }

    fun removeAiKey() {
        prefs.edit()
            .remove("ai_api_key")
            .apply()
    }
}
