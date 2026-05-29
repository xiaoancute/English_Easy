package io.github.xiaoancute.englisheasy.data.settings

/**
 * LLM Provider 配置。BYOK 模式，所有字段都由用户在设置页填入。
 *
 * 默认指向 OpenAI；用户可改成任意 OpenAI 兼容端点
 *（DeepSeek、Moonshot、智谱、Groq、本地 Ollama 等都行）。
 */
data class ProviderConfig(
    val apiKey: String,
    val baseUrl: String,
    val model: String,
) {
    val isUsable: Boolean
        get() = apiKey.isNotBlank() && baseUrl.isNotBlank() && model.isNotBlank()

    companion object {
        val DEFAULT = ProviderConfig(
            apiKey = "",
            baseUrl = "https://api.openai.com/v1/",
            model = "gpt-4o-mini",
        )
    }
}
