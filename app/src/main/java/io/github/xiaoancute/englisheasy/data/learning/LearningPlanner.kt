package io.github.xiaoancute.englisheasy.data.learning

import io.github.xiaoancute.englisheasy.data.util.WordNormalizer

sealed interface TodayStudyTask {
    data class Review(val dueReviewCount: Int) : TodayStudyTask
    data class NewWord(val word: String, val remainingCount: Int) : TodayStudyTask
    data object ChoosePack : TodayStudyTask
    data object Done : TodayStudyTask
}

object LearningPlanner {
    fun todayWords(
        words: List<String>,
        blockedWords: Set<String>,
        limit: Int = 10,
    ): List<String> {
        if (limit <= 0) return emptyList()

        val blocked = blockedWords.map(WordNormalizer::normalize).toSet()
        val seen = mutableSetOf<String>()
        return words.asSequence()
            .map(WordNormalizer::normalize)
            .filter { it.isNotBlank() }
            .filter { it !in blocked }
            .filter { seen.add(it) }
            .take(limit)
            .toList()
    }

    fun todayTask(
        dueReviewCount: Int,
        todayWords: List<String>,
        hasSelectedPack: Boolean,
    ): TodayStudyTask {
        if (dueReviewCount > 0) {
            return TodayStudyTask.Review(dueReviewCount = dueReviewCount)
        }

        val normalizedTodayWords = normalizeWords(todayWords)
        val nextWord = normalizedTodayWords.firstOrNull()
        if (nextWord != null) {
            return TodayStudyTask.NewWord(
                word = nextWord,
                remainingCount = normalizedTodayWords.size,
            )
        }

        return if (hasSelectedPack) {
            TodayStudyTask.Done
        } else {
            TodayStudyTask.ChoosePack
        }
    }

    private fun normalizeWords(words: List<String>): List<String> {
        val seen = mutableSetOf<String>()
        return words.asSequence()
            .map(WordNormalizer::normalize)
            .filter { it.isNotBlank() }
            .filter { seen.add(it) }
            .toList()
    }
}
