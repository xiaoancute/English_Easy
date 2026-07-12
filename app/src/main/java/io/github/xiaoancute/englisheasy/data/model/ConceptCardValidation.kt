package io.github.xiaoancute.englisheasy.data.model

/**
 * 校验 LLM 反序列化后的概念卡是否具备可展示的最小结构。
 * 失败时抛 [IllegalArgumentException]，由调用方转成可重试的格式错误。
 */
fun ConceptCard.validateStructure() {
    require(word.isNotBlank()) { "word 为空" }

    val branchList = branches
    if (!branchList.isNullOrEmpty()) {
        branchList.forEachIndexed { index, branch ->
            try {
                branch.card.validateStructure()
            } catch (error: IllegalArgumentException) {
                throw IllegalArgumentException("branches[$index] 无效：${error.message}", error)
            }
        }
        return
    }

    val core = coreConcept
    require(core != null) { "缺少 coreConcept（且无 branches）" }
    require(core.picture.isNotBlank()) { "coreConcept.picture 为空" }
    require(core.anchorWord.isNotBlank()) { "coreConcept.anchorWord 为空" }
}
