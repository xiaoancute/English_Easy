package io.github.xiaoancute.englisheasy.data.model

fun ExpressionRescueCard.toShareText(): String = buildString {
    appendLine("# 表达救援")
    appendLine()
    appendLine(intent)
    appendLine()
    appendLine("## 三档说法")
    appendLine()
    options.forEach { option ->
        appendLine("### ${option.level}")
        appendLine()
        appendLine(option.english)
        appendLine()
        appendLine(option.whyItWorks)
        appendLine()
        appendLine("适合：${option.whenToUse}")
        appendLine()
    }
    if (reusableExpressions.isNotEmpty()) {
        appendLine("## 可复用表达")
        appendLine()
        reusableExpressions.forEach { item ->
            appendLine("- ${item.expression}：${item.meaning}")
            appendLine("  ${item.example}")
        }
        appendLine()
    }
    appendLine("## 换个场景练")
    appendLine()
    appendLine(practicePrompt)
    appendLine()
    appendLine("## 记忆提示")
    appendLine()
    appendLine(memoryCue)
}
