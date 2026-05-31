package io.github.xiaoancute.englisheasy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room 实体：缓存 ConceptCard。
 *
 * - word 主键（小写 + 压缩空格归一化）
 * - promptVersion：用于判断缓存是否过期（与 CURRENT_PROMPT_VERSION 比对）
 * - cardJson：完整 ConceptCard 序列化为 JSON 字符串存储
 * - queriedAt：查询时间戳（毫秒），用于历史页排序
 * - isFavorite：用户是否已收藏
 * - userNote：用户写给自己的理解笔记
 * - reviewDueAt / reviewStrength / reviewCount / lastReviewedAt：轻量复习队列状态
 */
@Entity(tableName = "concept_cards")
data class ConceptCardEntity(
    @PrimaryKey
    val word: String,
    val promptVersion: Int,
    val cardJson: String,
    val queriedAt: Long,
    val isFavorite: Boolean = false,
    val userNote: String = "",
    val reviewDueAt: Long = queriedAt,
    val reviewStrength: Int = 0,
    val reviewCount: Int = 0,
    val lastReviewedAt: Long = 0L,
) {
    fun toCard(json: Json): ConceptCard = json.decodeFromString(cardJson)

    companion object {
        fun fromCard(
            card: ConceptCard,
            json: Json,
            isFavorite: Boolean = false,
            userNote: String = "",
            reviewDueAt: Long = System.currentTimeMillis(),
            reviewStrength: Int = 0,
            reviewCount: Int = 0,
            lastReviewedAt: Long = 0L,
        ): ConceptCardEntity {
            val now = System.currentTimeMillis()
            return ConceptCardEntity(
                word = card.word.lowercase().trim().replace(Regex("""\s+"""), " "),
                promptVersion = card.promptVersion,
                cardJson = json.encodeToString(card),
                queriedAt = now,
                isFavorite = isFavorite,
                userNote = userNote,
                reviewDueAt = reviewDueAt,
                reviewStrength = reviewStrength,
                reviewCount = reviewCount,
                lastReviewedAt = lastReviewedAt,
            )
        }
    }
}
