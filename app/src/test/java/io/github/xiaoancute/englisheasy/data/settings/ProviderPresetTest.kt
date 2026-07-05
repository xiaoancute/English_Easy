package io.github.xiaoancute.englisheasy.data.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class ProviderPresetTest {

    @Test
    fun appliesOpenAiPresetWithoutReplacingApiKey() {
        val current = ProviderConfig(
            apiKey = "sk-test",
            baseUrl = "https://old.example/v1/",
            model = "old-model",
        )

        val result = ProviderPreset.OpenAi.applyTo(current)

        assertEquals("sk-test", result.apiKey)
        assertEquals("https://api.openai.com/v1/", result.baseUrl)
        assertEquals("gpt-5.4-mini", result.model)
    }

    @Test
    fun ollamaPresetUsesLocalOpenAiCompatibleEndpoint() {
        val result = ProviderPreset.Ollama.applyTo(ProviderConfig.DEFAULT)

        assertEquals("ollama", result.apiKey)
        assertEquals("http://localhost:11434/v1/", result.baseUrl)
        assertEquals("gpt-oss:20b", result.model)
    }

    @Test
    fun moonshotPresetUsesCurrentApiDomain() {
        val result = ProviderPreset.Moonshot.applyTo(ProviderConfig.DEFAULT)

        assertEquals("https://api.moonshot.ai/v1/", result.baseUrl)
        assertEquals("kimi-k2.6", result.model)
    }
}
