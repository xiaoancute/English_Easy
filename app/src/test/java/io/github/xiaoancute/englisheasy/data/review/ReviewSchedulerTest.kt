package io.github.xiaoancute.englisheasy.data.review

import kotlin.test.Test
import kotlin.test.assertEquals

class ReviewSchedulerTest {

    private val now = 1_700_000_000_000L

    @Test
    fun forgotSchedulesTenMinutesLaterAndDoesNotIncreaseStrength() {
        val result = ReviewScheduler.schedule(
            currentStrength = 2,
            currentReviewCount = 4,
            grade = ReviewGrade.FORGOT,
            now = now,
        )

        assertEquals(1, result.reviewStrength)
        assertEquals(5, result.reviewCount)
        assertEquals(now, result.lastReviewedAt)
        assertEquals(now + 10 * 60 * 1000L, result.reviewDueAt)
    }

    @Test
    fun vagueSchedulesOneDayLaterAndKeepsAtLeastOneStrength() {
        val result = ReviewScheduler.schedule(
            currentStrength = 0,
            currentReviewCount = 1,
            grade = ReviewGrade.VAGUE,
            now = now,
        )

        assertEquals(1, result.reviewStrength)
        assertEquals(2, result.reviewCount)
        assertEquals(now + 24 * 60 * 60 * 1000L, result.reviewDueAt)
    }

    @Test
    fun rememberedUsesIncreasingIntervals() {
        val intervals = (0..5).map { strength ->
            ReviewScheduler.schedule(
                currentStrength = strength,
                currentReviewCount = 0,
                grade = ReviewGrade.REMEMBERED,
                now = now,
            ).reviewDueAt - now
        }

        assertEquals(
            listOf(
                1.days,
                3.days,
                7.days,
                14.days,
                30.days,
                30.days,
            ),
            intervals,
        )
    }

    private val Int.days: Long
        get() = this * 24 * 60 * 60 * 1000L
}
