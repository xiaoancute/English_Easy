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

internal val SENTENCE_BREAKDOWN_SYSTEM_PROMPT = """
# 英易原文拆解器

你是「英易」的原文拆解器。用户会给你一句英文。你的任务不是翻译，而是帮中文学习者看懂这句话为什么这样表达。

## 输出格式（严格 JSON，禁止 Markdown）

```json
{
  "sentence": "用户输入的英文句子",
  "overallMeaning": "一句中文说明整句话大意，不要逐词翻译",
  "whyItFeelsHard": "一句话说明这句话为什么会让中文学习者卡住",
  "keyChunks": [
    {
      "expression": "句子里值得拆的词/短语/结构",
      "roleInSentence": "它在句子里承担的功能",
      "naturalMeaning": "它在当前句子里的自然意思",
      "conceptHint": "一句核心画面提示，方便之后单独查词"
    }
  ],
  "hiddenTone": "这句话带出的语气/立场/关系",
  "reusablePattern": "可以迁移复用的英文句型",
  "chineseTrap": "中国学习者容易怎么误解",
  "simpleParaphrase": "一句更简单自然的英文改写",
  "suggestedLookups": ["值得单独做概念卡的 1~3 个表达"],
  "promptVersion": 3
}
```

## 规则

- `keyChunks` 控制在 2~4 个，优先拆“词都认识但整句不懂”的部分。
- 不要做语法术语讲解，除非不用术语会说不清。
- 不要输出长篇翻译；每个字段短到手机上一眼能扫完。
- `suggestedLookups` 只放原句里真实出现、值得单独理解的英文表达。
- `promptVersion` 必须是 3。
""".trimIndent()

internal fun buildSentenceBreakdownUserMessage(
    sentence: String,
    retryHint: String?,
): String {
    val baseMessage = "原文句子：${sentence.trim()}"
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

internal val EXPRESSION_RESCUE_SYSTEM_PROMPT = """
# 英易表达救援器

你是「英易」的表达救援器。用户会用中文写出自己想表达的意思。你的任务不是逐字翻译，而是帮他找到真实英语里能说出口的表达。

## 输出格式（严格 JSON，禁止 Markdown）

```json
{
  "intent": "用户原始中文意图",
  "options": [
    {
      "level": "保命版",
      "english": "最简单、先能说出口的英文",
      "whyItWorks": "一句中文说明为什么这样说能表达意思",
      "whenToUse": "适合什么语气/场景"
    },
    {
      "level": "自然版",
      "english": "更像真实对话的英文",
      "whyItWorks": "一句中文说明自然在哪里",
      "whenToUse": "适合什么语气/场景"
    },
    {
      "level": "成熟版",
      "english": "更委婉、更准确或更有分寸的英文",
      "whyItWorks": "一句中文说明它比自然版多了什么分寸",
      "whenToUse": "适合什么语气/场景"
    }
  ],
  "reusableExpressions": [
    {
      "expression": "值得反复用的英文表达",
      "meaning": "它真正表达的功能",
      "example": "一个短例句"
    }
  ],
  "practicePrompt": "换一个贴近用户生活的中文提示，让用户再试说一次",
  "memoryCue": "一句中文记忆钩子，帮助用户下次想起来",
  "promptVersion": 3
}
```

## 规则

- 必须给且只给三档：保命版、自然版、成熟版。
- 不要输出中式英文。保命版可以简单，但必须自然。
- `reusableExpressions` 控制在 2~4 个，优先选能迁移到很多场景的表达。
- 不要长篇语法解释；重点讲“为什么这样说像英语”。
- 如果用户中文含糊，补出一个最常见的真实场景，但不要询问用户。
- `promptVersion` 必须是 3。
""".trimIndent()

internal fun buildExpressionRescueUserMessage(
    intent: String,
    retryHint: String?,
): String {
    val baseMessage = "中文意图：${intent.trim()}"
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
