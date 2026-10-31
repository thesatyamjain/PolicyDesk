package com.policydesk.app

import com.policydesk.app.data.security.AiProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiModelDiscoveryTest {

    data class GeminiRawModel(val name: String, val methods: List<String>)

    @Test
    fun testGeminiModelsFilteringAndPrioritySorting() {
        val rawModels = listOf(
            GeminiRawModel("models/text-embedding-004", listOf("embedContent")),
            GeminiRawModel("models/imagen-3.0-generate-002", listOf("generateImages")),
            GeminiRawModel("models/gemini-2.5-flash", listOf("generateContent")),
            GeminiRawModel("models/gemini-3.5-flash-lite", listOf("generateContent")),
            GeminiRawModel("models/gemini-3.8-flash", listOf("generateContent")),
            GeminiRawModel("models/gemini-1.5-pro", listOf("generateContent")),
            GeminiRawModel("models/aqa", listOf("generateContent")),
            GeminiRawModel("models/learnlm-1.5-pro-experimental", listOf("generateContent"))
        )

        val candidates = mutableListOf<String>()
        for (m in rawModels) {
            val name = m.name.removePrefix("models/")
            val canGen = m.methods.contains("generateContent")
            if (canGen && (name.contains("flash") || name.contains("pro") || name.startsWith("gemini-"))) {
                if (!name.contains("embedding") && !name.contains("aqa") && !name.contains("imagen") && !name.contains("learnlm")) {
                    candidates.add(name)
                }
            }
        }

        val sorted = candidates.sortedWith(
            compareByDescending<String> { it.contains("3.5-flash-lite") }
                .thenByDescending { it.contains("3.8-flash") }
                .thenByDescending { it.contains("2.5-flash") && !it.contains("lite") }
                .thenByDescending { it.contains("2.0-flash") }
                .thenByDescending { it.contains("flash") }
                .thenByDescending { it.contains("pro") }
        ).distinct()

        assertFalse(sorted.contains("text-embedding-004"))
        assertFalse(sorted.contains("imagen-3.0-generate-002"))
        assertFalse(sorted.contains("aqa"))
        assertFalse(sorted.contains("learnlm-1.5-pro-experimental"))
        assertEquals(4, sorted.size)
        // Verify highest priority is gemini-3.5-flash-lite, followed by 3.8-flash
        assertEquals("gemini-3.5-flash-lite", sorted[0])
        assertEquals("gemini-3.8-flash", sorted[1])
        assertEquals("gemini-2.5-flash", sorted[2])
        assertEquals("gemini-1.5-pro", sorted[3])
    }

    data class OpenAiRawModel(val id: String)

    @Test
    fun testOpenAiModelsFilteringAndPrioritySorting() {
        val rawModels = listOf(
            OpenAiRawModel("text-embedding-3-small"),
            OpenAiRawModel("tts-1"),
            OpenAiRawModel("gpt-4o"),
            OpenAiRawModel("gpt-4o-mini"),
            OpenAiRawModel("gpt-4o-realtime-preview"),
            OpenAiRawModel("o1-mini"),
            OpenAiRawModel("dall-e-3"),
            OpenAiRawModel("whisper-1")
        )

        val candidates = mutableListOf<String>()
        for (m in rawModels) {
            val id = m.id
            if (id.startsWith("gpt-4o") || id.startsWith("o1") || id.startsWith("o3") || id.startsWith("gpt-4-turbo")) {
                if (!id.contains("audio") && !id.contains("realtime")) {
                    candidates.add(id)
                }
            }
        }

        val sorted = candidates.sortedWith(
            compareByDescending<String> { it == "gpt-4o-mini" }
                .thenByDescending { it == "gpt-4o" }
                .thenByDescending { it.startsWith("o3") }
                .thenByDescending { it.startsWith("o1") }
                .thenByDescending { it.startsWith("gpt-4o") }
        ).distinct()

        assertFalse(sorted.contains("text-embedding-3-small"))
        assertFalse(sorted.contains("tts-1"))
        assertFalse(sorted.contains("dall-e-3"))
        assertFalse(sorted.contains("gpt-4o-realtime-preview"))
        assertFalse(sorted.contains("whisper-1"))
        assertEquals(3, sorted.size)
        assertEquals("gpt-4o-mini", sorted[0])
        assertEquals("gpt-4o", sorted[1])
        assertEquals("o1-mini", sorted[2])
    }

    @Test
    fun testProviderDefaultModelsCoverage() {
        assertEquals("gemini-3.5-flash-lite", AiProvider.GEMINI.defaultModel)
        assertEquals("@cf/meta/llama-3.2-11b-vision-instruct", AiProvider.CLOUDFLARE_AI.defaultModel)
        assertEquals("gpt-4o-mini", AiProvider.OPENAI.defaultModel)
        assertEquals("claude-3-5-haiku-20241022", AiProvider.CLAUDE.defaultModel)
    }
}
