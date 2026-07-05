package io.github.xiaoancute.englisheasy.data.model

import io.github.xiaoancute.englisheasy.data.local.ConceptCardEntity
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

    @Test
    fun shareTextIncludesUserExampleWhenPresent() {
        val card = ConceptCard(
            word = "available",
            entryType = EntryType.WORD,
            coreConcept = CoreConcept(
                picture = "此刻能拿到手、能用上的",
                anchorWord = "ready",
            ),
            chineseApproximation = "中文用可用、有空、联系得上去逼近。",
            scenarios = emptyList(),
            misconceptions = emptyList(),
            branches = null,
            promptVersion = 3,
        )

        val text = card.toShareText(
            userNote = "不是能力，是能不能被占用。",
            sourceSentence = "Are you available after class?",
            userExample = "I am available after class.",
        )

        assertTrue(text.contains("## 我的理解"))
        assertTrue(text.contains("不是能力，是能不能被占用。"))
        assertTrue(text.contains("## 来源句子"))
        assertTrue(text.contains("Are you available after class?"))
        assertTrue(text.contains("## 我的例句"))
        assertTrue(text.contains("I am available after class."))
    }

    @Test
    fun conceptCardEntityStoresUserExample() {
        val card = ConceptCard(
            word = "take a break",
            entryType = EntryType.FIXED_PHRASE,
            coreConcept = CoreConcept(
                picture = "主动拿出一小段暂停",
                anchorWord = "stop",
            ),
            chineseApproximation = "中文用休息一下、暂停去逼近。",
            scenarios = emptyList(),
            misconceptions = emptyList(),
            branches = null,
            promptVersion = 3,
        )

        val entity = ConceptCardEntity.fromCard(
            card = card,
            json = Json,
            sourceSentence = "Let's take a break after this page.",
            userExample = "Let's take a break after this page.",
        )

        assertEquals("take a break", entity.word)
        assertEquals("Let's take a break after this page.", entity.sourceSentence)
        assertEquals("Let's take a break after this page.", entity.userExample)
    }

}
