package io.github.xiaoancute.englisheasy.data.learning

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WeakWordPolicyTest {

    @Test
    fun reviewedLowStrengthWordsAreWeak() {
        assertTrue(
            WeakWordPolicy.isWeak(
                reviewCount = 1,
                reviewStrength = 0,
            )
        )
        assertTrue(
            WeakWordPolicy.isWeak(
                reviewCount = 3,
                reviewStrength = 1,
            )
        )
    }

    @Test
    fun unreviewedOrStableWordsAreNotWeak() {
        assertFalse(
            WeakWordPolicy.isWeak(
                reviewCount = 0,
                reviewStrength = 0,
            )
        )
        assertFalse(
            WeakWordPolicy.isWeak(
                reviewCount = 2,
                reviewStrength = WeakWordPolicy.STRENGTH_THRESHOLD,
            )
        )
    }
}
