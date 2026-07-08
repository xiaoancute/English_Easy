package io.github.xiaoancute.englisheasy.data.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SentenceCardSerializationTest {

    @Test
    fun parsesSentenceCardJson() {
        val card = Json.decodeFromString<SentenceCard>(
            """
            {
              "sentence": "I'm not really in a position to make that call.",
              "overallMeaning": "说话者不方便或没有权限做这个决定。",
              "whyItFeelsHard": "position 不是位置，而是在说处境和权限。",
              "keyChunks": [
                {
                  "expression": "in a position to",
                  "roleInSentence": "说明是否具备做某事的条件",
                  "naturalMeaning": "有条件、有权限或方便去做某事",
                  "conceptHint": "站在一个能做这件事的位置上"
                }
              ],
              "hiddenTone": "语气比较委婉，不是直接拒绝。",
              "reusablePattern": "I'm not in a position to + 动词",
              "chineseTrap": "容易把 position 只理解成物理位置。",
              "simpleParaphrase": "I can't make that decision.",
              "suggestedLookups": ["in a position to", "make that call"],
              "promptVersion": 3
            }
            """.trimIndent(),
        )

        assertEquals("in a position to", card.keyChunks.single().expression)
        assertTrue(card.chineseTrap.contains("position"))
    }

    @Test
    fun shareTextIncludesCoreSections() {
        val card = SentenceCard(
            sentence = "This is out of my hands.",
            overallMeaning = "这事已经不是我能控制的了。",
            whyItFeelsHard = "out of my hands 不是手的位置，而是控制权离开了我。",
            keyChunks = listOf(
                SentenceChunk(
                    expression = "out of my hands",
                    roleInSentence = "说明控制权不在自己这里",
                    naturalMeaning = "不归我控制",
                    conceptHint = "事情从手里滑出去了",
                )
            ),
            hiddenTone = "带一点无奈和撇清责任。",
            reusablePattern = "This is out of my hands.",
            chineseTrap = "不要逐词想成手外面。",
            simpleParaphrase = "I can't control this anymore.",
            suggestedLookups = listOf("out of my hands"),
            promptVersion = 3,
        )

        val text = card.toShareText()

        assertTrue(text.contains("## 关键表达"))
        assertTrue(text.contains("out of my hands"))
        assertTrue(text.contains("## 简化改写"))
        assertTrue(text.contains("I can't control this anymore."))
    }
}
