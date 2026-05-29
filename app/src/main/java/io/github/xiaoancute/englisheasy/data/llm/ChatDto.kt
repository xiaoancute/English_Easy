package io.github.xiaoancute.englisheasy.data.llm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OpenAI 兼容的 /v1/chat/completions 请求体。
 * 适用于 OpenAI / DeepSeek / Moonshot / 智谱 / Groq / Ollama 等所有 OpenAI 兼容端点。
 */
@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @SerialName("response_format")
    val responseFormat: ResponseFormat = ResponseFormat("json_object"),
    val temperature: Double = 0.7,
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ResponseFormat(val type: String)

@Serializable
data class ChatResponse(
    val choices: List<Choice>,
)

@Serializable
data class Choice(
    val message: ChatMessage,
)
