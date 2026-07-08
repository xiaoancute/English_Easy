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
import io.github.xiaoancute.englisheasy.data.model.SentenceCard
import io.github.xiaoancute.englisheasy.data.model.SentenceChunk

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceCardView(
    card: SentenceCard,
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
        SentenceHeader(
            sentence = card.sentence,
            onShareClick = onShareClick,
            onCopyClick = onCopyClick,
            onRefreshClick = onRefreshClick,
        )

        InfoSection(title = "整句意思", body = card.overallMeaning)
        InfoSection(title = "卡点", body = card.whyItFeelsHard)

        if (card.keyChunks.isNotEmpty()) {
            Section(title = "关键表达") {
                card.keyChunks.forEach { chunk ->
                    ChunkItem(chunk = chunk, onLookupExpression = onLookupExpression)
                }
            }
        }

        InfoSection(title = "隐含语气", body = card.hiddenTone)
        InfoSection(title = "可复用句型", body = card.reusablePattern)
        InfoSection(title = "中文误区", body = card.chineseTrap)
        InfoSection(title = "简单改写", body = card.simpleParaphrase)

        if (card.suggestedLookups.isNotEmpty()) {
            Section(title = "查词") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    card.suggestedLookups.forEach { expression ->
                        AssistChip(
                            onClick = { onLookupExpression(expression) },
                            label = { Text(expression) },
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
private fun SentenceHeader(
    sentence: String,
    onShareClick: (() -> Unit)?,
    onCopyClick: (() -> Unit)?,
    onRefreshClick: (() -> Unit)?,
) {
    SurfaceCard(tone = SurfaceTone.Hero) {
        Text(
            text = sentence,
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
private fun ChunkItem(
    chunk: SentenceChunk,
    onLookupExpression: (String) -> Unit,
) {
    SurfaceCard(contentPadding = 16.dp) {
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
                    text = chunk.expression,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = chunk.roleInSentence,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = { onLookupExpression(chunk.expression) },
                label = { Text("查") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                border = null,
            )
        }
        Text(
            text = chunk.naturalMeaning,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = chunk.conceptHint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
