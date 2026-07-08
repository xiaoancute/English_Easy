package io.github.xiaoancute.englisheasy.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SentenceCard(
    val sentence: String,
    val overallMeaning: String,
    val whyItFeelsHard: String,
    val keyChunks: List<SentenceChunk>,
    val hiddenTone: String,
    val reusablePattern: String,
    val chineseTrap: String,
    val simpleParaphrase: String,
    val suggestedLookups: List<String> = emptyList(),
    val promptVersion: Int,
)

@Serializable
data class SentenceChunk(
    val expression: String,
    val roleInSentence: String,
    val naturalMeaning: String,
    val conceptHint: String,
)
