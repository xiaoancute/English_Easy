package io.github.xiaoancute.englisheasy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.xiaoancute.englisheasy.data.model.ExpressionOption
import io.github.xiaoancute.englisheasy.data.model.ExpressionRescueCard
import io.github.xiaoancute.englisheasy.data.model.ReusableExpression

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpressionRescueCardView(
    card: ExpressionRescueCard,
    onShareClick: (() -> Unit)? = null,
    onCopyClick: (() -> Unit)? = null,
    onRefreshClick: (() -> Unit)? = null,
    onLookupExpression: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(EnglishEasySpacing.SectionGap),
    ) {
        ExpressionHeader(
            intent = card.intent,
            onShareClick = onShareClick,
            onCopyClick = onCopyClick,
            onRefreshClick = onRefreshClick,
        )

        Section(title = "三档说法") {
            card.options.forEach { option ->
                ExpressionOptionItem(option)
            }
        }

        if (card.reusableExpressions.isNotEmpty()) {
            Section(title = "可复用表达") {
                card.reusableExpressions.forEach { item ->
                    ReusableExpressionItem(
                        item = item,
                        onLookupExpression = onLookupExpression,
                    )
                }
            }
        }

        InfoSection(title = "练习", body = card.practicePrompt)
        InfoSection(title = "提示", body = card.memoryCue)

        if (card.reusableExpressions.isNotEmpty()) {
            Section(title = "查词") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    card.reusableExpressions.forEach { item ->
                        AssistChip(
                            onClick = { onLookupExpression(item.expression) },
                            label = { Text(item.expression) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                            border = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressionHeader(
    intent: String,
    onShareClick: (() -> Unit)?,
    onCopyClick: (() -> Unit)?,
    onRefreshClick: (() -> Unit)?,
) {
    SurfaceCard(tone = SurfaceTone.Hero) {
        Text(
            text = intent,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onShareClick != null) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "分享",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            if (onCopyClick != null) {
                IconButton(onClick = onCopyClick) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            if (onRefreshClick != null) {
                IconButton(onClick = onRefreshClick) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "重新生成",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpressionOptionItem(option: ExpressionOption) {
    SurfaceCard(contentPadding = 16.dp) {
        Text(
            text = option.level,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = option.english,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = option.whyItWorks,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = option.whenToUse,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReusableExpressionItem(
    item: ReusableExpression,
    onLookupExpression: (String) -> Unit,
) {
    SurfaceCard(tone = SurfaceTone.Tonal, contentPadding = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = item.expression,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = item.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = { onLookupExpression(item.expression) },
                label = { Text("查") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                border = null,
            )
        }
        Text(
            text = item.example,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun InfoSection(title: String, body: String) {
    Section(title = title) {
        SurfaceCard(tone = SurfaceTone.Tonal, contentPadding = 16.dp) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = title)
        content()
    }
}
