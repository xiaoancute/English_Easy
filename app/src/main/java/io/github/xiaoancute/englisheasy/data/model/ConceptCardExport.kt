package io.github.xiaoancute.englisheasy.data.model

fun ConceptCard.toShareText(userNote: String = ""): String = buildString {
    appendCard(this@toShareText, headingLevel = 1)
    val trimmedNote = userNote.trim()
    if (trimmedNote.isNotEmpty()) {
        appendLine("## 我的理解")
        appendLine()
        appendLine(trimmedNote)
        appendLine()
    }
    appendLine()
    appendLine("---")
    appendLine("由英易 English Easy 生成")
}

private fun StringBuilder.appendCard(card: ConceptCard, headingLevel: Int) {
    val headingPrefix = "#".repeat(headingLevel.coerceAtLeast(1))
    appendLine("$headingPrefix ${card.word}")
    appendLine()
    appendLine("类型：${card.entryType.label}")
    appendLine()

    if (card.branches != null) {
        appendLine("## 分支")
        appendLine()
        card.branches.forEachIndexed { index, branch ->
            appendLine("### 分支 ${index + 1}：${branch.type.label}")
            branch.relationNote?.let { note ->
                appendLine()
                appendLine("共同祖源：$note")
            }
            appendLine()
            appendCard(branch.card, headingLevel = 4)
            appendLine()
        }
        return
    }

    card.coreConcept?.let { core ->
        appendLine("## 核心概念")
        appendLine()
        appendLine(core.picture)
        appendLine()
        appendLine("锚词：${core.anchorWord}")
        appendLine()
    }

    card.chineseApproximation?.let { approx ->
        appendLine("## 中文逼近")
        appendLine()
        appendLine(approx)
        appendLine()
    }

    card.scenarios?.takeIf { it.isNotEmpty() }?.let { scenarios ->
        appendLine("## 典型场景")
        appendLine()
        scenarios.forEach { scenario ->
            appendLine("- ${scenario.englishExample}")
            appendLine("  ${scenario.pictureExplanation}")
        }
        appendLine()
    }

    card.misconceptions?.takeIf { it.isNotEmpty() }?.let { misconceptions ->
        appendLine("## 错误直觉")
        appendLine()
        misconceptions.forEach { misconception ->
            appendLine("- 错误：${misconception.wrong}")
            appendLine("  正确：${misconception.correct}")
        }
        appendLine()
    }
}

private val EntryType.label: String
    get() = when (this) {
        EntryType.WORD -> "单词"
        EntryType.FIXED_PHRASE -> "固定短语"
        EntryType.FREE_COMBINATION -> "普通词组"
    }

private val BranchType.label: String
    get() = when (this) {
        BranchType.HOMONYM -> "同形异义"
        BranchType.SEMANTIC_CLUSTER -> "语义簇"
    }
