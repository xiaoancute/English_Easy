package io.github.xiaoancute.englisheasy.data.learning

object WeakWordPolicy {
    const val STRENGTH_THRESHOLD = 2

    fun isWeak(
        reviewCount: Int,
        reviewStrength: Int,
    ): Boolean {
        return reviewCount > 0 && reviewStrength < STRENGTH_THRESHOLD
    }
}
