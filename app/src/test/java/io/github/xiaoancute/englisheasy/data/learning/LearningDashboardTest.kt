package io.github.xiaoancute.englisheasy.data.learning

import io.github.xiaoancute.englisheasy.data.vocabulary.VocabularyEntry
import io.github.xiaoancute.englisheasy.data.vocabulary.VocabularyPack
import io.github.xiaoancute.englisheasy.data.vocabulary.VocabularyStage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LearningDashboardTest {

    @Test
    fun emptyDashboardWhenNoPackIsSelected() {
        val dashboard = LearningDashboard.from(
            selectedPack = null,
            availableWords = listOf("spring"),
            skippedWords = listOf("run"),
            dueReviewCount = 2,
            todayWordCount = 3,
        )

        assertFalse(dashboard.hasSelectedPack)
        assertEquals(null, dashboard.selectedPackLabel)
        assertEquals(0, dashboard.learnedCount)
        assertEquals(0, dashboard.totalCount)
        assertEquals(0, dashboard.availableCount)
        assertEquals(0, dashboard.skippedCount)
        assertEquals(2, dashboard.dueReviewCount)
        assertEquals(3, dashboard.todayWordCount)
        assertEquals(0, dashboard.progressPercent)
        assertEquals(0f, dashboard.progressFraction)
    }

    @Test
    fun buildsSelectedPackProgress() {
        val dashboard = LearningDashboard.from(
            selectedPack = pack(
                learnedCount = 2,
                words = listOf("spring", "run", "frame", "charge", "draw"),
            ),
            availableWords = listOf("frame", "charge", "draw"),
            skippedWords = listOf("draw"),
            dueReviewCount = 1,
            todayWordCount = 3,
        )

        assertTrue(dashboard.hasSelectedPack)
        assertEquals("高中词库", dashboard.selectedPackLabel)
        assertEquals(2, dashboard.learnedCount)
        assertEquals(5, dashboard.totalCount)
        assertEquals(3, dashboard.availableCount)
        assertEquals(1, dashboard.skippedCount)
        assertEquals(1, dashboard.dueReviewCount)
        assertEquals(3, dashboard.todayWordCount)
        assertEquals(40, dashboard.progressPercent)
        assertEquals(0.4f, dashboard.progressFraction)
    }

    @Test
    fun normalizesAvailableAndSkippedCounts() {
        val dashboard = LearningDashboard.from(
            selectedPack = pack(
                learnedCount = 1,
                words = listOf("take a break", "break the ice", "red apple"),
            ),
            availableWords = listOf(" Take   A   Break ", "take a break", "red apple"),
            skippedWords = listOf(" BREAK   THE ICE ", "break the ice"),
            dueReviewCount = 0,
            todayWordCount = 2,
        )

        assertEquals(2, dashboard.availableCount)
        assertEquals(1, dashboard.skippedCount)
        assertEquals(33, dashboard.progressPercent)
    }

    @Test
    fun progressIsClampedWhenLearnedCountExceedsTotal() {
        val dashboard = LearningDashboard.from(
            selectedPack = pack(
                learnedCount = 5,
                words = listOf("spring", "run"),
            ),
            availableWords = emptyList(),
            skippedWords = emptyList(),
            dueReviewCount = 0,
            todayWordCount = 0,
        )

        assertEquals(100, dashboard.progressPercent)
        assertEquals(1f, dashboard.progressFraction)
    }

    private fun pack(
        learnedCount: Int,
        words: List<String>,
    ): VocabularyPack {
        return VocabularyPack(
            stage = VocabularyStage.SENIOR,
            entries = words.map { word ->
                VocabularyEntry(
                    word = word,
                    stage = VocabularyStage.SENIOR,
                    source = "test",
                )
            },
            learnedCount = learnedCount,
        )
    }
}
