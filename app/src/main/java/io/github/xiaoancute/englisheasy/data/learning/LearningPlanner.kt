package io.github.xiaoancute.englisheasy.data.learning

object LearningPlanner {
    fun todayWords(
        words: List<String>,
        blockedWords: Set<String>,
        limit: Int = 10,
    ): List<String> {
        if (limit <= 0) return emptyList()

        val blocked = blockedWords.map { normalizeWord(it) }.toSet()
        val seen = mutableSetOf<String>()
        return words.asSequence()
            .map(::normalizeWord)
            .filter { it.isNotBlank() }
            .filter { it !in blocked }
            .filter { seen.add(it) }
            .take(limit)
            .toList()
    }

    private fun normalizeWord(word: String): String {
        return word
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
    }
}
