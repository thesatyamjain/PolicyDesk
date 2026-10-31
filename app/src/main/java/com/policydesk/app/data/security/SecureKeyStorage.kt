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

    fun getApiKeyForProvider(provider: AiProvider): String {
        val specificKey = prefs.getString("ai_api_key_${provider.name}", null)
        if (specificKey != null) return specificKey
        // Backward-compatibility migration: check if active provider has old un-prefixed "ai_api_key"
        val activeProviderStr = prefs.getString("ai_provider", AiProvider.CLOUDFLARE_AI.name)
        if (activeProviderStr == provider.name) {
            val legacyKey = prefs.getString("ai_api_key", "") ?: ""
            if (legacyKey.isNotBlank()) {
                prefs.edit().putString("ai_api_key_${provider.name}", legacyKey).apply()
                return legacyKey
            }
        }
        return ""
    }

    fun getModelForProvider(provider: AiProvider): String {
        var model = prefs.getString("ai_model_${provider.name}", null)
        if (model == null) {
            val activeProviderStr = prefs.getString("ai_provider", AiProvider.CLOUDFLARE_AI.name)
            if (activeProviderStr == provider.name) {
                model = prefs.getString("ai_model", provider.defaultModel)
            }
        }
        val finalModel = model ?: provider.defaultModel
        if (provider == AiProvider.GEMINI && (finalModel.isBlank() || finalModel.startsWith("gemini-1.") || finalModel.startsWith("gemini-2."))) {
            return "gemini-3.5-flash-lite"
        }
        return finalModel
    }

    fun getAiConfig(): AiConfig {
        val providerStr = prefs.getString("ai_provider", AiProvider.CLOUDFLARE_AI.name) ?: AiProvider.CLOUDFLARE_AI.name
        val provider = try { AiProvider.valueOf(providerStr) } catch (_: Exception) { AiProvider.CLOUDFLARE_AI }
        val apiKey = getApiKeyForProvider(provider)
        val model = getModelForProvider(provider)
        val customEndpoint = prefs.getString("ai_custom_endpoint", "") ?: ""
        return AiConfig(provider, apiKey, model, customEndpoint)
    }

    fun getAiConfigFor(provider: AiProvider): AiConfig {
        val apiKey = getApiKeyForProvider(provider)
        val model = getModelForProvider(provider)
        val customEndpoint = prefs.getString("ai_custom_endpoint", "") ?: ""
        return AiConfig(provider, apiKey, model, customEndpoint)
    }

    fun saveAiConfig(config: AiConfig) {
        val cleanModel = config.model.trim().removePrefix("models/").ifBlank { config.provider.defaultModel }
        prefs.edit()
            .putString("ai_provider", config.provider.name)
            .putString("ai_api_key_${config.provider.name}", config.apiKey.trim())
            .putString("ai_model_${config.provider.name}", cleanModel)
            .putString("ai_custom_endpoint", config.customEndpoint.trim())
            .putString("ai_api_key", config.apiKey.trim())
            .putString("ai_model", cleanModel)
            .apply()
    }

    fun removeAiKey(provider: AiProvider = getAiConfig().provider) {
        prefs.edit()
            .remove("ai_api_key_${provider.name}")
            .remove("ai_api_key")
            .apply()
    }
}
