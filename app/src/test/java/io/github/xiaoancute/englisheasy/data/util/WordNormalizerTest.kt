package io.github.xiaoancute.englisheasy.data.util

import kotlin.test.Test
import kotlin.test.assertEquals

class WordNormalizerTest {

    @Test
    fun trimsLowercasesAndCollapsesWhitespace() {
        assertEquals("run out of", WordNormalizer.normalize("  Run   OUT  of  "))
        assertEquals("spring", WordNormalizer.normalize("Spring"))
        assertEquals("", WordNormalizer.normalize("   "))
    }
}
