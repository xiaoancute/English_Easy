package io.github.xiaoancute.englisheasy.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExpressionRescueCard(
    val intent: String,
    val options: List<ExpressionOption>,
    val reusableExpressions: List<ReusableExpression>,
    val practicePrompt: String,
    val memoryCue: String,
    val promptVersion: Int,
)

@Serializable
data class ExpressionOption(
    val level: String,
    val english: String,
    val whyItWorks: String,
    val whenToUse: String,
)

@Serializable
data class ReusableExpression(
    val expression: String,
    val meaning: String,
    val example: String,
)
