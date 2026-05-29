package io.github.xiaoancute.englisheasy.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 概念卡 schema —— 与当前 System Prompt 输出一致。
 *
 * 三种形态：
 *  - 单核心多义：[branches] 为 null，[coreConcept]/[chineseApproximation]/[scenarios]/[misconceptions] 全填
 *  - 同形异义 / 语义簇：[branches] 非空，顶层 4 个内容字段为 null
 */
@Serializable
data class ConceptCard(
    val word: String,
    val entryType: EntryType = EntryType.WORD,
    val coreConcept: CoreConcept? = null,
    val chineseApproximation: String? = null,
    val scenarios: List<Scenario>? = null,
    val misconceptions: List<Misconception>? = null,
    val branches: List<Branch>? = null,
    val promptVersion: Int,
)

@Serializable
enum class EntryType {
    @SerialName("WORD") WORD,
    @SerialName("FIXED_PHRASE") FIXED_PHRASE,
    @SerialName("FREE_COMBINATION") FREE_COMBINATION,
}

@Serializable
data class CoreConcept(
    val picture: String,
    val anchorWord: String,
)

@Serializable
data class Scenario(
    val englishExample: String,
    val pictureExplanation: String,
)

@Serializable
data class Misconception(
    val wrong: String,
    val correct: String,
)

@Serializable
data class Branch(
    val type: BranchType,
    val card: ConceptCard,
    val relationNote: String? = null,
)

@Serializable
enum class BranchType {
    @SerialName("HOMONYM") HOMONYM,
    @SerialName("SEMANTIC_CLUSTER") SEMANTIC_CLUSTER,
}
