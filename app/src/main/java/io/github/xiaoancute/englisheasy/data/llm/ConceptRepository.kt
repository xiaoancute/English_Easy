package io.github.xiaoancute.englisheasy.data.llm

import io.github.xiaoancute.englisheasy.data.local.ConceptCardDao
import io.github.xiaoancute.englisheasy.data.local.ConceptCardEntity
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.model.SentenceCard
import io.github.xiaoancute.englisheasy.data.prompt.CURRENT_PROMPT_VERSION
import io.github.xiaoancute.englisheasy.data.prompt.SYSTEM_PROMPT_V3
import io.github.xiaoancute.englisheasy.data.settings.ProviderConfig
import io.github.xiaoancute.englisheasy.data.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 编排 LLM 调用流程 + Room 缓存：
 *  1. 查本地缓存（word 主键，promptVersion 匹配 CURRENT_PROMPT_VERSION）
 *  2. 缓存命中且版本匹配 → 直接返回
 *  3. 缓存未命中或版本过期 → 调用 LLM
 *  4. LLM 响应成功 → 存入缓存并返回
 *  5. 失败时自动重试一次（把错误信息回灌给 LLM）
 */
@Singleton
class ConceptRepository @Inject constructor(
    private val api: OpenAiCompatibleApi,
    private val settings: SettingsRepository,
    private val dao: ConceptCardDao,
    private val json: Json,
) {
    fun observeFavorite(word: String): Flow<Boolean> {
        return dao.observeFavorite(normalizeEntry(word)).map { it == true }
    }

    fun observeNote(word: String): Flow<String> {
        return dao.observeNote(normalizeEntry(word)).map { it.orEmpty() }
    }

    fun observeExample(word: String): Flow<String> {
        return dao.observeExample(normalizeEntry(word)).map { it.orEmpty() }
    }

    fun observeSourceSentence(word: String): Flow<String> {
        return dao.observeSourceSentence(normalizeEntry(word)).map { it.orEmpty() }
    }

    suspend fun lookup(
        word: String,
        contextSentence: String = "",
        forceRefresh: Boolean = false,
    ): Result<ConceptCard> = runCatching {
        val normalized = normalizeEntry(word)
        val normalizedContext = contextSentence.trim()
        require(normalized.isNotEmpty()) { "词或短语不能为空" }

        // 1. 查缓存
        val cached = dao.get(normalized)
        if (
            normalizedContext.isBlank() &&
            !forceRefresh &&
            cached != null &&
            cached.promptVersion == CURRENT_PROMPT_VERSION
        ) {
            return@runCatching cached.toCard(json)
        }

        // 2. 缓存未命中或版本过期 → 调用 LLM
        val cfg = settings.load()
        require(cfg.isUsable) { "请先在设置里填入 API Key、Base URL、模型名" }
        val card = lookupWithJsonRetry(
            word = normalized,
            contextSentence = normalizedContext,
            cfg = cfg,
        )

        // 3. 存入缓存
        dao.insert(
            ConceptCardEntity.fromCard(
                card = card,
                json = json,
                isFavorite = cached?.isFavorite == true,
                sourceSentence = normalizedContext.ifBlank { cached?.sourceSentence.orEmpty() },
                userNote = cached?.userNote.orEmpty(),
                userExample = cached?.userExample.orEmpty(),
                reviewDueAt = cached?.reviewDueAt ?: System.currentTimeMillis(),
                reviewStrength = cached?.reviewStrength ?: 0,
                reviewCount = cached?.reviewCount ?: 0,
                lastReviewedAt = cached?.lastReviewedAt ?: 0L,
            )
        )
        card
    }.recoverCatching {
        throw it.toUserFacingException()
    }

    suspend fun setFavorite(word: String, isFavorite: Boolean) {
        val normalized = normalizeEntry(word)
        require(normalized.isNotEmpty()) { "词或短语不能为空" }
        dao.setFavorite(normalized, isFavorite)
    }

    suspend fun setNote(word: String, note: String) {
        val normalized = normalizeEntry(word)
        require(normalized.isNotEmpty()) { "词或短语不能为空" }
        dao.setNote(normalized, note)
    }

    suspend fun setExample(word: String, example: String) {
        val normalized = normalizeEntry(word)
        require(normalized.isNotEmpty()) { "词或短语不能为空" }
        dao.setExample(normalized, example)
    }

    suspend fun reviewExample(
        word: String,
        userExample: String,
        contextSentence: String = "",
    ): Result<ExampleFeedback> = runCatching {
        val normalized = normalizeEntry(word)
        val trimmedExample = userExample.trim()
        require(normalized.isNotEmpty()) { "词或短语不能为空" }
        require(trimmedExample.isNotEmpty()) { "请先写一句自己的英文例句" }

        val cfg = settings.load()
        require(cfg.isUsable) { "请先在设置里填入 API Key、Base URL、模型名" }

        val response = api.chat(
            fullUrl = "${cfg.baseUrl.trimEnd('/')}/chat/completions",
            authorization = "Bearer ${cfg.apiKey}",
            request = ChatRequest(
                model = cfg.model,
                messages = listOf(
                    ChatMessage(role = "system", content = EXAMPLE_FEEDBACK_SYSTEM_PROMPT),
                    ChatMessage(
                        role = "user",
                        content = buildExampleFeedbackUserMessage(
                            word = normalized,
                            userExample = trimmedExample,
                            contextSentence = contextSentence,
                        ),
                    ),
                ),
                temperature = 0.2,
            ),
        )

        val raw = response.choices.firstOrNull()?.message?.content
            ?: throw LlmResponseFormatException("LLM 响应里没有 choices/message")
        val cleanJson = extractJson(raw) ?: throw LlmResponseFormatException("响应里找不到 JSON 主体")
        try {
            json.decodeFromString<ExampleFeedback>(cleanJson)
        } catch (error: SerializationException) {
            throw LlmResponseFormatException("JSON 解析失败：${error.message}", error)
        }
    }.recoverCatching {
        throw it.toUserFacingException()
    }

    suspend fun analyzeSentence(sentence: String): Result<SentenceCard> = runCatching {
        val trimmed = sentence.trim()
        require(trimmed.isNotEmpty()) { "句子不能为空" }

        val cfg = settings.load()
        require(cfg.isUsable) { "请先在设置里填入 API Key、Base URL、模型名" }

        analyzeSentenceWithJsonRetry(
            sentence = trimmed,
            cfg = cfg,
        )
    }.recoverCatching {
        throw it.toUserFacingException()
    }

    private suspend fun performLookup(
        word: String,
        contextSentence: String,
        cfg: ProviderConfig,
        retryHint: String?,
    ): ConceptCard {
        val response = api.chat(
            fullUrl = "${cfg.baseUrl.trimEnd('/')}/chat/completions",
            authorization = "Bearer ${cfg.apiKey}",
            request = ChatRequest(
                model = cfg.model,
                messages = listOf(
                    ChatMessage(role = "system", content = SYSTEM_PROMPT_V3),
                    ChatMessage(
                        role = "user",
                        content = buildLookupUserMessage(
                            word = word,
                            contextSentence = contextSentence,
                            retryHint = retryHint,
                        ),
                    ),
                ),
            ),
        )

        val raw = response.choices.firstOrNull()?.message?.content
            ?: throw LlmResponseFormatException("LLM 响应里没有 choices/message")
        val cleanJson = extractJson(raw) ?: throw LlmResponseFormatException("响应里找不到 JSON 主体")
        return try {
            json.decodeFromString<ConceptCard>(cleanJson)
        } catch (error: SerializationException) {
            throw LlmResponseFormatException("JSON 解析失败：${error.message}", error)
        }
    }

    private suspend fun lookupWithJsonRetry(
        word: String,
        contextSentence: String,
        cfg: ProviderConfig,
    ): ConceptCard {
        return try {
            performLookup(word, contextSentence, cfg, retryHint = null)
        } catch (firstError: LlmResponseFormatException) {
            performLookup(word, contextSentence, cfg, retryHint = firstError.message)
        } catch (error: Throwable) {
            throw error
        }
    }

    private suspend fun performSentenceAnalysis(
        sentence: String,
        cfg: ProviderConfig,
        retryHint: String?,
    ): SentenceCard {
        val response = api.chat(
            fullUrl = "${cfg.baseUrl.trimEnd('/')}/chat/completions",
            authorization = "Bearer ${cfg.apiKey}",
            request = ChatRequest(
                model = cfg.model,
                messages = listOf(
                    ChatMessage(role = "system", content = SENTENCE_BREAKDOWN_SYSTEM_PROMPT),
                    ChatMessage(
                        role = "user",
                        content = buildSentenceBreakdownUserMessage(
                            sentence = sentence,
                            retryHint = retryHint,
                        ),
                    ),
                ),
                temperature = 0.3,
            ),
        )

        val raw = response.choices.firstOrNull()?.message?.content
            ?: throw LlmResponseFormatException("LLM 响应里没有 choices/message")
        val cleanJson = extractJson(raw) ?: throw LlmResponseFormatException("响应里找不到 JSON 主体")
        return try {
            json.decodeFromString<SentenceCard>(cleanJson)
        } catch (error: SerializationException) {
            throw LlmResponseFormatException("JSON 解析失败：${error.message}", error)
        }
    }

    private suspend fun analyzeSentenceWithJsonRetry(
        sentence: String,
        cfg: ProviderConfig,
    ): SentenceCard {
        return try {
            performSentenceAnalysis(sentence, cfg, retryHint = null)
        } catch (firstError: LlmResponseFormatException) {
            performSentenceAnalysis(sentence, cfg, retryHint = firstError.message)
        } catch (error: Throwable) {
            throw error
        }
    }

    /** 容错：从可能包含 Markdown 包裹的响应里提取 { ... } 主体。 */
    private fun extractJson(raw: String): String? {
        val match = Regex("""\{[\s\S]*\}""").find(raw) ?: return null
        return match.value
    }

    private fun normalizeEntry(value: String): String {
        return value.trim().lowercase().replace(Regex("""\s+"""), " ")
    }

    private class LlmResponseFormatException(
        message: String,
        cause: Throwable? = null,
    ) : IllegalStateException(message, cause)

    private fun Throwable.toUserFacingException(): Throwable {
        val message = when (this) {
            is HttpException -> when (code()) {
                401 -> "API Key 无效（HTTP 401），请检查设置里的 API Key"
                403 -> "权限不足或模型不可用（HTTP 403），请检查模型名和账号权限"
                404 -> "接口或模型不存在（HTTP 404），请检查 Base URL 和模型名"
                408 -> "请求超时（HTTP 408），请稍后重试"
                429 -> "请求频率或额度受限（HTTP 429），请稍后重试"
                in 500..599 -> "模型服务暂时异常（HTTP ${code()}），请稍后重试"
                else -> "请求失败（HTTP ${code()}），请检查设置或稍后重试"
            }

            is IOException -> "网络连接失败，请检查网络连接或 Base URL 是否正确"
            is LlmResponseFormatException -> "解析失败，LLM 响应格式异常，请重新生成"
            else -> this.message ?: "未知错误"
        }

        return IllegalStateException(message, this)
    }
}
