package io.github.xiaoancute.englisheasy.data.learning

import kotlin.test.Test
import kotlin.test.assertEquals

class LearningPlannerTest {

    @Test
    fun picksFirstUnlearnedWordsForToday() {
        val result = LearningPlanner.todayWords(
            words = listOf(" Apple ", "School", "charge", "draw", "hold"),
            blockedWords = setOf("apple", "draw"),
            limit = 2,
        )

        assertEquals(listOf("school", "charge"), result)
    }

    @Test
    fun doesNotDuplicateWordsAfterNormalizingSpaces() {
        val result = LearningPlanner.todayWords(
            words = listOf("take   a   break", "take a break", "break the ice"),
            blockedWords = emptySet(),
            limit = 10,
        )

        assertEquals(listOf("take a break", "break the ice"), result)
    }

    @Test
    fun skipsBlockedWordsAfterNormalizingCaseAndSpaces() {
        val result = LearningPlanner.todayWords(
            words = listOf("Take   A   Break", "break the ice", "red apple"),
            blockedWords = setOf(" take a break ", "BREAK  THE   ICE"),
            limit = 10,
        )

        assertEquals(listOf("red apple"), result)
    }

    @Test
    fun todayTaskPrioritizesDueReviewsOverNewWords() {
        val result = LearningPlanner.todayTask(
            dueReviewCount = 3,
            todayWords = listOf("spring", "run"),
            hasSelectedPack = true,
        )

        assertEquals(TodayStudyTask.Review(dueReviewCount = 3), result)
    }

    @Test
    fun todayTaskPicksOneNewWordWhenNoReviewIsDue() {
        val result = LearningPlanner.todayTask(
            dueReviewCount = 0,
            todayWords = listOf(" Spring ", "run"),
            hasSelectedPack = true,
        )

        assertEquals(TodayStudyTask.NewWord(word = "spring", remainingCount = 2), result)
    }

    @Test
    fun todayTaskAsksForPackWhenNoPackIsSelected() {
        val result = LearningPlanner.todayTask(
            dueReviewCount = 0,
            todayWords = emptyList(),
            hasSelectedPack = false,
        )

        assertEquals(TodayStudyTask.ChoosePack, result)
    }

    @Test
    fun todayTaskIsDoneWhenPackHasNoAvailableWords() {
        val result = LearningPlanner.todayTask(
            dueReviewCount = 0,
            todayWords = emptyList(),
            hasSelectedPack = true,
        )

        assertEquals(TodayStudyTask.Done, result)
    }
}
