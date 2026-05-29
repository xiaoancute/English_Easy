package io.github.xiaoancute.englisheasy.data.llm

import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.prompt.SYSTEM_PROMPT_V2
import io.github.xiaoancute.englisheasy.data.settings.ProviderConfig
import io.github.xiaoancute.englisheasy.data.settings.SettingsRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 编排 LLM 调用流程：
 *  1. 拉取用户配置（API key / base URL / model）
 *  2. 发送 system + user 消息
 *  3. 从响应里提取 JSON（容错正则）
 *  4. 反序列化为 ConceptCard
 *  5. 失败时自动重试一次（把错误信息回灌给 LLM）
 */
@Singleton
class ConceptRepository @Inject constructor(
    private val api: OpenAiCompatibleApi,
    private val settings: SettingsRepository,
    private val json: Json,
) {
    suspend fun lookup(word: String): Result<ConceptCard> = runCatching {
        val cfg = settings.load()
        require(cfg.isUsable) { "请先在设置里填入 API Key、Base URL、模型名" }
        performLookup(word.trim(), cfg, retryHint = null)
    }.recoverCatching { firstError ->
        val cfg = settings.load()
        require(cfg.isUsable) { "请先在设置里填入 API Key、Base URL、模型名" }
        performLookup(word.trim(), cfg, retryHint = firstError.message)
    }

    private suspend fun performLookup(
        word: String,
        cfg: ProviderConfig,
        retryHint: String?,
    ): ConceptCard {
        val userMessage = if (retryHint != null) {
            "上次响应解析失败：$retryHint\n请只输出合法 JSON，不要任何 Markdown 包裹或前后说明。\n\n查询单词：$word"
        } else {
            word
        }

        val response = api.chat(
            fullUrl = "${cfg.baseUrl.trimEnd('/')}/chat/completions",
            authorization = "Bearer ${cfg.apiKey}",
            request = ChatRequest(
                model = cfg.model,
                messages = listOf(
                    ChatMessage(role = "system", content = SYSTEM_PROMPT_V2),
                    ChatMessage(role = "user", content = userMessage),
                ),
            ),
        )

        val raw = response.choices.firstOrNull()?.message?.content
            ?: error("LLM 响应里没有 choices/message")
        val cleanJson = extractJson(raw) ?: error("响应里找不到 JSON 主体")
        return json.decodeFromString<ConceptCard>(cleanJson)
    }

    /** 容错：从可能包含 Markdown 包裹的响应里提取 { ... } 主体。 */
    private fun extractJson(raw: String): String? {
        val match = Regex("""\{[\s\S]*\}""").find(raw) ?: return null
        return match.value
    }
}
