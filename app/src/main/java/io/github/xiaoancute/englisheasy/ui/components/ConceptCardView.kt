package io.github.xiaoancute.englisheasy.ui.components

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.xiaoancute.englisheasy.data.model.Branch
import io.github.xiaoancute.englisheasy.data.model.BranchType
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.model.EntryType
import java.util.Locale

@Composable
fun ConceptCardView(
    card: ConceptCard,
    isFavorite: Boolean = false,
    onFavoriteChange: ((Boolean) -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onCopyClick: (() -> Unit)? = null,
    onRefreshClick: (() -> Unit)? = null,
    userNote: String = "",
    onNoteChange: ((String) -> Unit)? = null,
    scrollable: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val ttsReady = remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context) { status ->
            ttsReady.value = status == TextToSpeech.SUCCESS
        }
    }

    LaunchedEffect(ttsReady.value) {
        if (ttsReady.value) {
            tts.language = Locale.US
        }
    }

    DisposableEffect(tts) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val contentModifier = if (scrollable) {
        modifier
            .fillMaxWidth(1f)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    } else {
        modifier
            .fillMaxWidth(1f)
            .padding(16.dp)
    }

    Column(
        modifier = contentModifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        WordHeader(
            word = card.word,
            entryType = card.entryType,
            isFavorite = isFavorite,
            onFavoriteChange = onFavoriteChange,
            onShareClick = onShareClick,
            onCopyClick = onCopyClick,
            onRefreshClick = onRefreshClick,
        )

        if (card.branches != null) {
            BranchesSection(
                branches = card.branches,
                onSpeak = { text -> tts.speakExample(text, ttsReady.value) },
            )
        } else {
            SingleCardBody(
                card = card,
                onSpeak = { text -> tts.speakExample(text, ttsReady.value) },
            )
        }

        if (onNoteChange != null) {
            UserNoteSection(
                note = userNote,
                onNoteChange = onNoteChange,
            )
        }
    }
}

@Composable
private fun WordHeader(
    word: String,
    entryType: EntryType,
    isFavorite: Boolean,
    onFavoriteChange: ((Boolean) -> Unit)?,
    onShareClick: (() -> Unit)?,
    onCopyClick: (() -> Unit)?,
    onRefreshClick: (() -> Unit)?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            if (onShareClick != null) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "分享",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (onCopyClick != null) {
                IconButton(onClick = onCopyClick) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (onRefreshClick != null) {
                IconButton(onClick = onRefreshClick) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "重新生成",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (onFavoriteChange != null) {
                IconButton(onClick = { onFavoriteChange(!isFavorite) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (isFavorite) "取消收藏" else "收藏",
                        tint = if (isFavorite) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                text = when (entryType) {
                    EntryType.WORD -> "单词"
                    EntryType.FIXED_PHRASE -> "固定短语"
                    EntryType.FREE_COMBINATION -> "普通词组"
                },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun SingleCardBody(
    card: ConceptCard,
    onSpeak: (String) -> Unit,
) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = sc.englishExample,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { onSpeak(sc.englishExample) }) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "朗读例句",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
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
private fun BranchesSection(
    branches: List<Branch>,
    onSpeak: (String) -> Unit,
) {
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
                SingleCardBody(
                    card = branch.card,
                    onSpeak = onSpeak,
                )
            }
        }
    }
}

@Composable
private fun UserNoteSection(
    note: String,
    onNoteChange: (String) -> Unit,
) {
    Section(title = "我的理解") {
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(1f),
            minLines = 3,
            maxLines = 6,
            placeholder = { Text("写下你自己的理解") },
        )
    }
}

private fun TextToSpeech.speakExample(
    text: String,
    isReady: Boolean,
) {
    if (!isReady) return
    speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
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
