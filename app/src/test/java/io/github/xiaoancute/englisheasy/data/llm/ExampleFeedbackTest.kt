package io.github.xiaoancute.englisheasy.data.llm

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExampleFeedbackTest {

    @Test
    fun lookupMessageIncludesContextSentenceWhenPresent() {
        val message = buildLookupUserMessage(
            word = "run out of",
            contextSentence = "I ran out of time before the exam.",
            retryHint = null,
        )

        assertTrue(message.contains("查询词或短语：run out of"))
        assertTrue(message.contains("上下文句子：I ran out of time before the exam."))
        assertTrue(message.contains("优先解释目标词在这个句子里的用法"))
    }

    @Test
    fun lookupMessageStaysCompactWithoutContext() {
        val message = buildLookupUserMessage(
            word = "spring",
            contextSentence = "",
            retryHint = null,
        )

        assertEquals("spring", message)
    }

    @Test
    fun exampleFeedbackMessageIncludesTargetExampleAndContext() {
        val message = buildExampleFeedbackUserMessage(
            word = "available",
            userExample = "I am available after class.",
            contextSentence = "Can we meet today?",
        )

        assertTrue(message.contains("目标词或短语：available"))
        assertTrue(message.contains("用户例句：I am available after class."))
        assertTrue(message.contains("查词上下文：Can we meet today?"))
    }

    @Test
    fun parsesExampleFeedbackJson() {
        val feedback = Json.decodeFromString<ExampleFeedback>(
            """
            {
              "verdict": "自然",
              "improvedExample": "I am available after class.",
              "reason": "available 用来表示某个时间能被安排或能参与。"
            }
            """.trimIndent(),
        )

        assertEquals("自然", feedback.verdict)
        assertEquals("I am available after class.", feedback.improvedExample)
        assertTrue(feedback.reason.contains("某个时间"))
    }
}
