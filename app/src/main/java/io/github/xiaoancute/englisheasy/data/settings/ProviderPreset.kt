package io.github.xiaoancute.englisheasy.data.settings

enum class ProviderPreset(
    val label: String,
    val baseUrl: String,
    val defaultModel: String,
    val apiKeyPlaceholder: String = "",
) {
    OpenAi(
        label = "OpenAI",
        baseUrl = "https://api.openai.com/v1/",
        defaultModel = "gpt-5.4-mini",
    ),
    DeepSeek(
        label = "DeepSeek",
        baseUrl = "https://api.deepseek.com/",
        defaultModel = "deepseek-v4-flash",
    ),
    Moonshot(
        label = "Moonshot",
        baseUrl = "https://api.moonshot.ai/v1/",
        defaultModel = "kimi-k2.6",
    ),
    Zhipu(
        label = "智谱",
        baseUrl = "https://open.bigmodel.cn/api/paas/v4/",
        defaultModel = "glm-5.2",
    ),
    Groq(
        label = "Groq",
        baseUrl = "https://api.groq.com/openai/v1/",
        defaultModel = "llama-3.3-70b-versatile",
    ),
    Ollama(
        label = "Ollama",
        baseUrl = "http://localhost:11434/v1/",
        defaultModel = "gpt-oss:20b",
        apiKeyPlaceholder = "ollama",
    ),
    Custom(
        label = "自定义",
        baseUrl = "",
        defaultModel = "",
    );

    fun applyTo(current: ProviderConfig): ProviderConfig {
        if (this == Custom) return current
        return current.copy(
            apiKey = current.apiKey.ifBlank { apiKeyPlaceholder },
            baseUrl = baseUrl,
            model = defaultModel,
        )
    }

    companion object {
        fun match(config: ProviderConfig): ProviderPreset {
            val normalizedBaseUrl = normalizeBaseUrl(config.baseUrl)
            return entries.firstOrNull { preset ->
                preset != Custom && normalizeBaseUrl(preset.baseUrl) == normalizedBaseUrl
            } ?: Custom
        }

        private fun normalizeBaseUrl(value: String): String {
            return value.trim().trimEnd('/') + "/"
        }
    }
}
