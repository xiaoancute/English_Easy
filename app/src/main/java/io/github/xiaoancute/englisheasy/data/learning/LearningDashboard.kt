package io.github.xiaoancute.englisheasy.data.learning

import io.github.xiaoancute.englisheasy.data.vocabulary.VocabularyPack
import kotlin.math.roundToInt

data class LearningDashboard(
    val selectedPackLabel: String?,
    val learnedCount: Int,
    val totalCount: Int,
    val availableCount: Int,
    val skippedCount: Int,
    val dueReviewCount: Int,
    val todayWordCount: Int,
) {
    val hasSelectedPack: Boolean = selectedPackLabel != null
    val progressFraction: Float = if (totalCount <= 0) {
        0f
    } else {
        (learnedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    }
    val progressPercent: Int = (progressFraction * 100).roundToInt()

    companion object {
        fun from(
            selectedPack: VocabularyPack?,
            availableWords: List<String>,
            skippedWords: List<String>,
            dueReviewCount: Int,
            todayWordCount: Int,
        ): LearningDashboard {
            return LearningDashboard(
                selectedPackLabel = selectedPack?.let { "${it.stage.label}词库" },
                learnedCount = selectedPack?.learnedCount ?: 0,
                totalCount = selectedPack?.totalCount ?: 0,
                availableCount = if (selectedPack == null) {
                    0
                } else {
                    availableWords.normalizedDistinctCount()
                },
                skippedCount = if (selectedPack == null) {
                    0
                } else {
                    skippedWords.normalizedDistinctCount()
                },
                dueReviewCount = dueReviewCount,
                todayWordCount = todayWordCount,
            )
        }

        private fun List<String>.normalizedDistinctCount(): Int {
            return asSequence()
                .map { it.trim().lowercase().replace(Regex("""\s+"""), " ") }
                .filter { it.isNotBlank() }
                .distinct()
                .count()
        }
    }
}
