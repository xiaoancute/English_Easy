package io.github.xiaoancute.englisheasy.data.llm

import io.github.xiaoancute.englisheasy.data.local.ConceptCardDao
import io.github.xiaoancute.englisheasy.data.local.ConceptCardEntity
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.model.ExpressionRescueCard
import io.github.xiaoancute.englisheasy.data.model.SentenceCard
import io.github.xiaoancute.englisheasy.data.model.validateStructure
import io.github.xiaoancute.englisheasy.data.prompt.CURRENT_PROMPT_VERSION
import io.github.xiaoancute.englisheasy.data.prompt.SYSTEM_PROMPT_V3
import io.github.xiaoancute.englisheasy.data.settings.ProviderConfig
import io.github.xiaoancute.englisheasy.data.settings.SettingsRepository
import io.github.xiaoancute.englisheasy.data.util.WordNormalizer
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
 *  2. 无上下文且缓存命中且版本匹配 → 直接返回
 *  3. 否则调用 LLM；解析后校验卡片结构
 *  4. 写入缓存：带上下文时不覆盖已有通用卡，只更新来源句子等元数据
 *  5. 格式失败时自动重试一次（把错误信息回灌给 LLM）
 */
@Singleton
class ConceptRepository @Inject constructor(
    private val api: OpenAiCompatibleApi,
    private val settings: SettingsRepository,
    private val dao: ConceptCardDao,
    private val json: Json,
) {
    fun observeFavorite(word: String): Flow<Boolean> {
        return dao.observeFavorite(WordNormalizer.normalize(word)).map { it == true }
    }

    fun observeNote(word: String): Flow<String> {
        return dao.observeNote(WordNormalizer.normalize(word)).map { it.orEmpty() }
    }

    fun observeExample(word: String): Flow<String> {
        return dao.observeExample(WordNormalizer.normalize(word)).map { it.orEmpty() }
    }

    fun observeSourceSentence(word: String): Flow<String> {
        return dao.observeSourceSentence(WordNormalizer.normalize(word)).map { it.orEmpty() }
    }

    suspend fun lookup(
        word: String,
        contextSentence: String = "",
        forceRefresh: Boolean = false,
    ): Result<ConceptCard> = runCatching {
        val normalized = WordNormalizer.normalize(word)
        val normalizedContext = contextSentence.trim()
        require(normalized.isNotEmpty()) { "词或短语不能为空" }

        // 1. 无上下文时优先走通用缓存
        val cached = dao.get(normalized)
        val hasFreshGeneralCache = cached != null &&
            cached.promptVersion == CURRENT_PROMPT_VERSION
        if (
            normalizedContext.isBlank() &&
            !forceRefresh &&
            hasFreshGeneralCache
        ) {
            return@runCatching cached!!.toCard(json)
        }

        // 2. 缓存未命中、强制刷新、或带上下文 → 调用 LLM
        val cfg = settings.load()
        require(cfg.isUsable) { "请先在设置里填入 API Key、Base URL、模型名" }
        // 强制主键与 prompt 版本：LLM 常改写 word / 乱填 promptVersion，不能信
        val card = lookupWithJsonRetry(
            word = normalized,
            contextSentence = normalizedContext,
            cfg = cfg,
        ).copy(
            word = normalized,
            promptVersion = CURRENT_PROMPT_VERSION,
        )

        // 3. 带上下文且已有通用卡：保留通用 cardJson，只更新来源句子等元数据
        val preserveGeneralCard = normalizedContext.isNotBlank() &&
            !forceRefresh &&
            hasFreshGeneralCache
        val cardToStore = if (preserveGeneralCard) {
            cached!!.toCard(json).copy(
                word = normalized,
                promptVersion = CURRENT_PROMPT_VERSION,
            )
        } else {
            card
        }

        dao.insert(
            ConceptCardEntity.fromCard(
                card = cardToStore,
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
        // 本次会话仍返回 LLM 结果（带上下文时展示语境版）
        card
    }.recoverCatching {
        throw it.toUserFacingException()
    }

    suspend fun setFavorite(word: String, isFavorite: Boolean) {
        val normalized = WordNormalizer.normalize(word)
        require(normalized.isNotEmpty()) { "词或短语不能为空" }
        val updated = dao.setFavorite(normalized, isFavorite)
        // UPDATE 0 行时 UI 会假成功：没有缓存行就无法收藏
        require(updated > 0) { "还没有这张概念卡，请先查询成功后再收藏" }
    }

    suspend fun setNote(word: String, note: String) {
        val normalized = WordNormalizer.normalize(word)
        require(normalized.isNotEmpty()) { "词或短语不能为空" }
        dao.setNote(normalized, note)
    }

    suspend fun setExample(word: String, example: String) {
        val normalized = WordNormalizer.normalize(word)
        require(normalized.isNotEmpty()) { "词或短语不能为空" }
        dao.setExample(normalized, example)
    }

    suspend fun reviewExample(
        word: String,
        userExample: String,
        contextSentence: String = "",
    ): Result<ExampleFeedback> = runCatching {
        val normalized = WordNormalizer.normalize(word)
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

    suspend fun rescueExpression(intent: String): Result<ExpressionRescueCard> = runCatching {
        val trimmed = intent.trim()
        require(trimmed.isNotEmpty()) { "想表达的内容不能为空" }

        val cfg = settings.load()
        require(cfg.isUsable) { "请先在设置里填入 API Key、Base URL、模型名" }

        rescueExpressionWithJsonRetry(
            intent = trimmed,
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
            json.decodeFromString<ConceptCard>(cleanJson).also { it.ensureValid() }
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

    private suspend fun performExpressionRescue(
        intent: String,
        cfg: ProviderConfig,
        retryHint: String?,
    ): ExpressionRescueCard {
        val response = api.chat(
            fullUrl = "${cfg.baseUrl.trimEnd('/')}/chat/completions",
            authorization = "Bearer ${cfg.apiKey}",
            request = ChatRequest(
                model = cfg.model,
                messages = listOf(
                    ChatMessage(role = "system", content = EXPRESSION_RESCUE_SYSTEM_PROMPT),
                    ChatMessage(
                        role = "user",
                        content = buildExpressionRescueUserMessage(
                            intent = intent,
                            retryHint = retryHint,
                        ),
                    ),
                ),
                temperature = 0.35,
            ),
        )

        val raw = response.choices.firstOrNull()?.message?.content
            ?: throw LlmResponseFormatException("LLM 响应里没有 choices/message")
        val cleanJson = extractJson(raw) ?: throw LlmResponseFormatException("响应里找不到 JSON 主体")
        return try {
            json.decodeFromString<ExpressionRescueCard>(cleanJson)
        } catch (error: SerializationException) {
            throw LlmResponseFormatException("JSON 解析失败：${error.message}", error)
        }
    }

    private suspend fun rescueExpressionWithJsonRetry(
        intent: String,
        cfg: ProviderConfig,
    ): ExpressionRescueCard {
        return try {
            performExpressionRescue(intent, cfg, retryHint = null)
        } catch (firstError: LlmResponseFormatException) {
            performExpressionRescue(intent, cfg, retryHint = firstError.message)
        } catch (error: Throwable) {
            throw error
        }
    }

    /** 容错：从可能包含 Markdown 包裹的响应里提取 { ... } 主体。 */
    private fun extractJson(raw: String): String? {
        val match = Regex("""\{[\s\S]*\}""").find(raw) ?: return null
        return match.value
    }

    private fun ConceptCard.ensureValid() {
        try {
            validateStructure()
        } catch (error: IllegalArgumentException) {
            throw LlmResponseFormatException(
                "卡片结构不完整：${error.message}",
                error,
            )
        }
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
