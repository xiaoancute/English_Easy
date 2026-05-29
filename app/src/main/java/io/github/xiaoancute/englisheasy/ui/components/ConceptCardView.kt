package io.github.xiaoancute.englisheasy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.xiaoancute.englisheasy.data.model.Branch
import io.github.xiaoancute.englisheasy.data.model.BranchType
import io.github.xiaoancute.englisheasy.data.model.ConceptCard

@Composable
fun ConceptCardView(
    card: ConceptCard,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(1f)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        WordHeader(card.word)

        if (card.branches != null) {
            BranchesSection(card.branches)
        } else {
            SingleCardBody(card)
        }
    }
}

@Composable
private fun WordHeader(word: String) {
    Text(
        text = word,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SingleCardBody(card: ConceptCard) {
    card.coreConcept?.let { core ->
        Section(title = "核心概念") {
            Text(
                text = core.picture,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "锚词：${core.anchorWord}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }

    card.chineseApproximation?.let { approx ->
        Section(title = "中文逼近") {
            Text(
                text = approx,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    card.scenarios?.takeIf { it.isNotEmpty() }?.let { scenarios ->
        Section(title = "典型场景") {
            scenarios.forEach { sc ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = sc.englishExample,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = sc.pictureExplanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    )
                }
            }
        }
    }

    card.misconceptions?.takeIf { it.isNotEmpty() }?.let { miscons ->
        Section(title = "错误直觉（过渡拐杖）") {
            miscons.forEach { mc ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "❌ ${mc.wrong}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = "✅ ${mc.correct}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun BranchesSection(branches: List<Branch>) {
    Text(
        text = when (branches.firstOrNull()?.type) {
            BranchType.HOMONYM -> "本词其实是两个独立的词"
            BranchType.SEMANTIC_CLUSTER -> "本词分化成多个语义簇"
            null -> ""
        },
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.secondary,
    )
    branches.forEachIndexed { idx, branch ->
        Card(
            modifier = Modifier.fillMaxWidth(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "分支 ${idx + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                branch.relationNote?.let { note ->
                    Text(
                        text = "共同祖源：$note",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
                SingleCardBody(branch.card)
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(1f),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier.padding(PaddingValues(top = 4.dp)),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                content()
            }
        }
    }
}
