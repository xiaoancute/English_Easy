package io.github.xiaoancute.englisheasy.data.vocabulary

import io.github.xiaoancute.englisheasy.data.util.WordNormalizer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class VocabularyEntry(
    val word: String,
    val stage: VocabularyStage,
    val source: String,
    val tags: List<String> = emptyList(),
)

enum class VocabularyStage(val label: String) {
    PRIMARY("小学"),
    JUNIOR("初中"),
    SENIOR("高中"),
    GAOKAO("高考"),
}

data class VocabularyPack(
    val stage: VocabularyStage,
    val entries: List<VocabularyEntry>,
    val learnedCount: Int,
) {
    val totalCount: Int = entries.size
}

object VocabularyCatalog {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val stageOrder = listOf(
        VocabularyStage.PRIMARY,
        VocabularyStage.JUNIOR,
        VocabularyStage.SENIOR,
        VocabularyStage.GAOKAO,
    )

    fun decode(rawJson: String): List<VocabularyEntry> {
        return json.decodeFromString<List<VocabularyEntry>>(rawJson)
            .map { it.copy(word = WordNormalizer.normalize(it.word)) }
            .filter { it.word.isNotBlank() }
            .distinctBy { it.stage to it.word }
    }

    fun groupByStage(
        entries: List<VocabularyEntry>,
        learnedWords: Set<String>,
    ): List<VocabularyPack> {
        val normalizedLearnedWords = learnedWords.map(WordNormalizer::normalize).toSet()
        val grouped = entries.groupBy { it.stage }
        return stageOrder.mapNotNull { stage ->
            val stageEntries = grouped[stage].orEmpty().sortedBy { it.word }
            if (stageEntries.isEmpty()) {
                null
            } else {
                VocabularyPack(
                    stage = stage,
                    entries = stageEntries,
                    learnedCount = stageEntries.count { it.word in normalizedLearnedWords },
                )
            }
        }
    }
}
