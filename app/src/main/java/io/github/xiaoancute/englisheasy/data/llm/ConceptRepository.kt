package io.github.xiaoancute.englisheasy.data.llm

import io.github.xiaoancute.englisheasy.data.local.ConceptCardDao
import io.github.xiaoancute.englisheasy.data.local.ConceptCardEntity
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.prompt.CURRENT_PROMPT_VERSION
import io.github.xiaoancute.englisheasy.data.prompt.SYSTEM_PROMPT_V3
import io.github.xiaoancute.englisheasy.data.settings.ProviderConfig
import io.github.xiaoancute.englisheasy.data.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
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

    suspend fun lookup(word: String, forceRefresh: Boolean = false): Result<ConceptCard> = runCatching {
        val normalized = normalizeEntry(word)
        require(normalized.isNotEmpty()) { "单词不能为空" }

        // 1. 查缓存
        val cached = dao.get(normalized)
        if (!forceRefresh && cached != null && cached.promptVersion == CURRENT_PROMPT_VERSION) {
            return@runCatching cached.toCard(json)
        }

        // 2. 缓存未命中或版本过期 → 调用 LLM
        val cfg = settings.load()
        require(cfg.isUsable) { "请先在设置里填入 API Key、Base URL、模型名" }
        val card = performLookup(normalized, cfg, retryHint = null)

        // 3. 存入缓存
        dao.insert(
            ConceptCardEntity.fromCard(
                card = card,
                json = json,
                isFavorite = cached?.isFavorite == true,
                userNote = cached?.userNote.orEmpty(),
            )
        )
        card
    }.recoverCatching { firstError ->
        // 4. 失败重试一次
        val normalized = normalizeEntry(word)
        val cfg = settings.load()
        require(cfg.isUsable) { "请先在设置里填入 API Key、Base URL、模型名" }
        val cached = dao.get(normalized)
        val card = performLookup(normalized, cfg, retryHint = firstError.message)
        dao.insert(
            ConceptCardEntity.fromCard(
                card = card,
                json = json,
                isFavorite = cached?.isFavorite == true,
                userNote = cached?.userNote.orEmpty(),
            )
        )
        card
    }

    suspend fun setFavorite(word: String, isFavorite: Boolean) {
        val normalized = normalizeEntry(word)
        require(normalized.isNotEmpty()) { "单词不能为空" }
        dao.setFavorite(normalized, isFavorite)
    }

    suspend fun setNote(word: String, note: String) {
        val normalized = normalizeEntry(word)
        require(normalized.isNotEmpty()) { "单词不能为空" }
        dao.setNote(normalized, note)
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
                    ChatMessage(role = "system", content = SYSTEM_PROMPT_V3),
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

    private fun normalizeEntry(value: String): String {
        return value.trim().lowercase().replace(Regex("""\s+"""), " ")
    }
}
