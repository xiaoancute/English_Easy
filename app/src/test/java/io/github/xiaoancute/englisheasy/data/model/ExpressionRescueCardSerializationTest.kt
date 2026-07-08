package io.github.xiaoancute.englisheasy.data.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpressionRescueCardSerializationTest {

    @Test
    fun parsesExpressionRescueCardJson() {
        val card = Json.decodeFromString<ExpressionRescueCard>(
            """
            {
              "intent": "这事我现在没法决定，不是我不愿意，是我没权限。",
              "options": [
                {
                  "level": "保命版",
                  "english": "I can't decide this right now.",
                  "whyItWorks": "先把核心意思说清楚。",
                  "whenToUse": "临时开口、压力比较大时"
                },
                {
                  "level": "自然版",
                  "english": "I'm not really in a position to make that call.",
                  "whyItWorks": "in a position to 表示有没有条件或权限。",
                  "whenToUse": "工作或正式一点的对话"
                },
                {
                  "level": "成熟版",
                  "english": "That's not something I can decide on my own right now.",
                  "whyItWorks": "把责任范围说清楚，同时不显得生硬。",
                  "whenToUse": "需要委婉说明权限边界时"
                }
              ],
              "reusableExpressions": [
                {
                  "expression": "be in a position to",
                  "meaning": "有条件或权限做某事",
                  "example": "I'm not in a position to answer that."
                }
              ],
              "practicePrompt": "换成你想说：这件事我需要先问一下别人。",
              "memoryCue": "不是站在哪里，而是有没有站在能决定的位置上。",
              "promptVersion": 3
            }
            """.trimIndent(),
        )

        assertEquals("自然版", card.options[1].level)
        assertEquals("be in a position to", card.reusableExpressions.single().expression)
    }

    @Test
    fun shareTextIncludesExpressionOptionsAndPracticePrompt() {
        val card = ExpressionRescueCard(
            intent = "我想礼貌地说我今天不太方便。",
            options = listOf(
                ExpressionOption(
                    level = "保命版",
                    english = "I can't make it today.",
                    whyItWorks = "直接表达今天去不了。",
                    whenToUse = "熟人之间",
                )
            ),
            reusableExpressions = listOf(
                ReusableExpression(
                    expression = "make it",
                    meaning = "成功到场或参加",
                    example = "Sorry, I can't make it tonight.",
                )
            ),
            practicePrompt = "换成你想说：我这周不太方便。",
            memoryCue = "make it 是能不能到场，不是制造它。",
            promptVersion = 3,
        )

        val text = card.toShareText()

        assertTrue(text.contains("## 三档说法"))
        assertTrue(text.contains("I can't make it today."))
        assertTrue(text.contains("## 可复用表达"))
        assertTrue(text.contains("make it"))
        assertTrue(text.contains("## 换个场景练"))
    }
}
