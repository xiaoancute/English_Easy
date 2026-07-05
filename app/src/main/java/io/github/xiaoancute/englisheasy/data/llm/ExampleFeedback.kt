package io.github.xiaoancute.englisheasy.data.llm

import kotlinx.serialization.Serializable

@Serializable
data class ExampleFeedback(
    val verdict: String,
    val improvedExample: String,
    val reason: String,
)

internal val EXAMPLE_FEEDBACK_SYSTEM_PROMPT = """
# 英易例句反馈器

你是「英易」的例句反馈器。用户已经看过一个英文词或短语的概念卡，现在写了一句自己的英文例句。

你的任务不是写作文批改长评，而是判断这句例句是否自然、是否真的用到了目标词的核心用法，并给一个更自然的改写。

## 输出格式（严格 JSON，禁止 Markdown）

```json
{
  "verdict": "自然 | 基本可用 | 不自然",
  "improvedExample": "一条更自然的英文例句。如果用户原句已经自然，可以原样返回。",
  "reason": "一句中文理由，说明目标词为什么这样用，或用户原句哪里卡住。"
}
```

## 规则

- 反馈必须短，三行以内能读完。
- 不要逐词翻译用户例句。
- 不要泛泛讲语法，只解释影响自然度或目标词用法的点。
- 如果用户例句没有真正使用目标词或短语，判为“不自然”，并在改写里补上目标词或短语。
""".trimIndent()

internal fun buildLookupUserMessage(
    word: String,
    contextSentence: String,
    retryHint: String?,
): String {
    val trimmedWord = word.trim()
    val trimmedContext = contextSentence.trim()
    val baseMessage = if (trimmedContext.isBlank()) {
        trimmedWord
    } else {
        """
        查询词或短语：$trimmedWord
        上下文句子：$trimmedContext

        请优先解释目标词在这个句子里的用法，再给出能迁移到其他场景的核心画面。
        """.trimIndent()
    }

    return if (retryHint == null) {
        baseMessage
    } else {
        """
        上次响应解析失败：$retryHint
        请只输出合法 JSON，不要任何 Markdown 包裹或前后说明。

        $baseMessage
        """.trimIndent()
    }
}

internal fun buildExampleFeedbackUserMessage(
    word: String,
    userExample: String,
    contextSentence: String,
): String {
    val trimmedContext = contextSentence.trim()
    return buildString {
        appendLine("目标词或短语：${word.trim()}")
        if (trimmedContext.isNotBlank()) {
            appendLine("查词上下文：$trimmedContext")
        }
        appendLine("用户例句：${userExample.trim()}")
    }.trim()
}
