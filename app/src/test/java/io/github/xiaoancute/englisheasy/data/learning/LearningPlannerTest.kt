package io.github.xiaoancute.englisheasy.data.learning

import kotlin.test.Test
import kotlin.test.assertEquals

class LearningPlannerTest {

    @Test
    fun picksFirstUnlearnedWordsForToday() {
        val result = LearningPlanner.todayWords(
            words = listOf(" Apple ", "School", "charge", "draw", "hold"),
            learnedWords = setOf("apple", "draw"),
            limit = 2,
        )

        assertEquals(listOf("school", "charge"), result)
    }

    @Test
    fun doesNotDuplicateWordsAfterNormalizingSpaces() {
        val result = LearningPlanner.todayWords(
            words = listOf("take   a   break", "take a break", "break the ice"),
            learnedWords = emptySet(),
            limit = 10,
        )

        assertEquals(listOf("take a break", "break the ice"), result)
    }
}
