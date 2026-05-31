package io.github.xiaoancute.englisheasy.data.review

enum class ReviewGrade {
    FORGOT,
    VAGUE,
    REMEMBERED,
}

data class ReviewSchedule(
    val reviewDueAt: Long,
    val reviewStrength: Int,
    val reviewCount: Int,
    val lastReviewedAt: Long,
)

object ReviewScheduler {
    private const val MINUTE = 60 * 1000L
    private const val DAY = 24 * 60 * MINUTE

    fun schedule(
        currentStrength: Int,
        currentReviewCount: Int,
        grade: ReviewGrade,
        now: Long = System.currentTimeMillis(),
    ): ReviewSchedule {
        val nextStrength = when (grade) {
            ReviewGrade.FORGOT -> (currentStrength - 1).coerceAtLeast(0)
            ReviewGrade.VAGUE -> currentStrength.coerceAtLeast(1)
            ReviewGrade.REMEMBERED -> (currentStrength + 1).coerceAtMost(5)
        }
        val interval = when (grade) {
            ReviewGrade.FORGOT -> 10 * MINUTE
            ReviewGrade.VAGUE -> DAY
            ReviewGrade.REMEMBERED -> rememberedInterval(nextStrength)
        }
        return ReviewSchedule(
            reviewDueAt = now + interval,
            reviewStrength = nextStrength,
            reviewCount = currentReviewCount + 1,
            lastReviewedAt = now,
        )
    }

    private fun rememberedInterval(strength: Int): Long {
        val days = when (strength) {
            1 -> 1
            2 -> 3
            3 -> 7
            4 -> 14
            else -> 30
        }
        return days * DAY
    }
}
