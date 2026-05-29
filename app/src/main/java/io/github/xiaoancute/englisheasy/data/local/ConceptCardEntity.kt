package io.github.xiaoancute.englisheasy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room 实体：缓存 ConceptCard。
 *
 * - word 主键（小写归一化）
 * - promptVersion：用于判断缓存是否过期（与 CURRENT_PROMPT_VERSION 比对）
 * - cardJson：完整 ConceptCard 序列化为 JSON 字符串存储
 * - queriedAt：查询时间戳（毫秒），用于历史页排序
 */
@Entity(tableName = "concept_cards")
data class ConceptCardEntity(
    @PrimaryKey
    val word: String,
    val promptVersion: Int,
    val cardJson: String,
    val queriedAt: Long,
) {
    fun toCard(json: Json): ConceptCard = json.decodeFromString(cardJson)

    companion object {
        fun fromCard(card: ConceptCard, json: Json): ConceptCardEntity {
            return ConceptCardEntity(
                word = card.word.lowercase().trim(),
                promptVersion = card.promptVersion,
                cardJson = json.encodeToString(card),
                queriedAt = System.currentTimeMillis(),
            )
        }
    }
}
