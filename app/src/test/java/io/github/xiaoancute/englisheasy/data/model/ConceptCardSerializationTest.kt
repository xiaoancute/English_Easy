package io.github.xiaoancute.englisheasy.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class ConceptCardSerializationTest {

    @Test
    fun missingEntryTypeDefaultsToWordForLegacyCards() {
        val card = Json.decodeFromString<ConceptCard>(
            """
            {
              "word": "spring",
              "coreConcept": {
                "picture": "一种充满张力的、突然爆发的动态",
                "anchorWord": "burst"
              },
              "chineseApproximation": "中文里没有一个词同时覆盖 spring 的所有用法。",
              "scenarios": [],
              "misconceptions": [],
              "branches": null,
              "promptVersion": 3
            }
            """.trimIndent()
        )

        assertEquals(EntryType.WORD, card.entryType)
        assertTrue(card.toShareText().contains("类型：单词"))
    }
}
