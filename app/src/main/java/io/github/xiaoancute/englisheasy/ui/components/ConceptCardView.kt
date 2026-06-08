package io.github.xiaoancute.englisheasy.ui.components

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.xiaoancute.englisheasy.data.model.Branch
import io.github.xiaoancute.englisheasy.data.model.BranchType
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.model.CoreConcept
import io.github.xiaoancute.englisheasy.data.model.EntryType
import io.github.xiaoancute.englisheasy.data.model.Misconception
import io.github.xiaoancute.englisheasy.data.model.Scenario
import io.github.xiaoancute.englisheasy.data.model.label
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
    userExample: String = "",
    onExampleChange: ((String) -> Unit)? = null,
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

    // 换词时整卡淡入 + 轻微上移
    val enterAnim = remember(card.word) { Animatable(0f) }
    LaunchedEffect(card.word) {
        enterAnim.animateTo(1f, animationSpec = tween(durationMillis = 300))
    }

    val contentModifier = if (scrollable) {
        modifier
            .fillMaxWidth(1f)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    } else {
        modifier
            .fillMaxWidth(1f)
            .padding(vertical = 8.dp)
    }

    Column(
        modifier = contentModifier.graphicsLayer {
            alpha = enterAnim.value
            translationY = (1f - enterAnim.value) * 24f
        },
        verticalArrangement = Arrangement.spacedBy(EnglishEasySpacing.SectionGap),
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

        if (onExampleChange != null) {
            UserExampleSection(
                example = userExample,
                onExampleChange = onExampleChange,
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
    QuietSurface(contentPadding = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            )
            EntryTypePill(entryType)
        }

        HeaderActions(
            isFavorite = isFavorite,
            onFavoriteChange = onFavoriteChange,
            onShareClick = onShareClick,
            onCopyClick = onCopyClick,
            onRefreshClick = onRefreshClick,
        )
    }
}

@Composable
private fun EntryTypePill(entryType: EntryType) {
    Surface(
        shape = RoundedCornerShape(EnglishEasySpacing.Radius),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
    ) {
        Text(
            text = entryType.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HeaderActions(
    isFavorite: Boolean,
    onFavoriteChange: ((Boolean) -> Unit)?,
    onShareClick: (() -> Unit)?,
    onCopyClick: (() -> Unit)?,
    onRefreshClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(1f),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
}

@Composable
private fun SingleCardBody(
    card: ConceptCard,
    onSpeak: (String) -> Unit,
) {
    card.coreConcept?.let { core ->
        CoreConceptBlock(core)
    }

    card.chineseApproximation?.let { approx ->
        Section(title = "中文逼近") {
            Text(
                text = approx,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            )
        }
    }

    card.scenarios?.takeIf { it.isNotEmpty() }?.let { scenarios ->
        Section(title = "典型场景") {
            scenarios.forEach { sc -> ScenarioItem(sc, onSpeak) }
        }
    }

    card.misconceptions?.takeIf { it.isNotEmpty() }?.let { miscons ->
        Section(title = "错误直觉 · 过渡拐杖") {
            miscons.forEach { mc -> MisconceptionItem(mc) }
        }
    }
}

/** 核心概念：作为学习笔记的主段落，用轻量底色突出，但不做大装饰卡。 */
@Composable
private fun CoreConceptBlock(core: CoreConcept) {
    QuietSurface(tonal = true, contentPadding = 16.dp) {
        SectionHeader(title = "核心概念")
        Text(
            text = core.picture,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "锚词",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp),
            )
            AnchorChip(core.anchorWord)
        }
    }
}

@Composable
private fun AnchorChip(word: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(
            text = word,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
        )
    }
}

/** 单个场景：例句 + 朗读，画面解释另起一行，保持轻量行块。 */
@Composable
private fun ScenarioItem(sc: Scenario, onSpeak: (String) -> Unit) {
    QuietSurface(contentPadding = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = sc.englishExample,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onSpeak(sc.englishExample) }) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "朗读例句",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Text(
            text = sc.pictureExplanation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 错误直觉：用「误 / 对」色标 + 淡色底，替代裸 emoji，层级更清楚。 */
@Composable
private fun MisconceptionItem(mc: Misconception) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TaggedLine(
            tag = "误",
            text = mc.wrong,
            tagBg = MaterialTheme.colorScheme.errorContainer,
            tagFg = MaterialTheme.colorScheme.onErrorContainer,
        )
        TaggedLine(
            tag = "对",
            text = mc.correct,
            tagBg = MaterialTheme.colorScheme.primaryContainer,
            tagFg = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun TaggedLine(
    tag: String,
    text: String,
    tagBg: androidx.compose.ui.graphics.Color,
    tagFg: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(shape = RoundedCornerShape(6.dp), color = tagBg) {
            Text(
                text = tag,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = tagFg,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BranchesSection(
    branches: List<Branch>,
    onSpeak: (String) -> Unit,
) {
    SectionHeader(
        title = when (branches.firstOrNull()?.type) {
            BranchType.HOMONYM -> "本词其实是两个独立的词"
            BranchType.SEMANTIC_CLUSTER -> "本词分化成多个语义簇"
            null -> "多义分支"
        },
    )
    branches.forEachIndexed { idx, branch ->
        QuietSurface(tonal = true, contentPadding = 12.dp) {
            SectionHeader(
                title = "分支 ${idx + 1}",
                subtitle = branch.relationNote?.let { "共同祖源：$it" },
            )
            SingleCardBody(
                card = branch.card,
                onSpeak = onSpeak,
            )
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
            placeholder = { Text("把核心画面用自己的话写成一句") },
        )
    }
}

@Composable
private fun UserExampleSection(
    example: String,
    onExampleChange: (String) -> Unit,
) {
    Section(title = "我的输出") {
        Text(
            text = "写一句你真的可能会用的话，不要只改写上面的例句。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = example,
            onValueChange = onExampleChange,
            modifier = Modifier.fillMaxWidth(1f),
            minLines = 2,
            maxLines = 4,
            placeholder = { Text("I would use this word when...") },
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = title)
        content()
    }
}
