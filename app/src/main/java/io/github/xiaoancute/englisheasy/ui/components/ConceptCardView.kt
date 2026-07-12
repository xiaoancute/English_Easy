package io.github.xiaoancute.englisheasy.ui.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.xiaoancute.englisheasy.data.llm.ExampleFeedback
import io.github.xiaoancute.englisheasy.data.model.Branch
import io.github.xiaoancute.englisheasy.data.model.BranchType
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.model.CoreConcept
import io.github.xiaoancute.englisheasy.data.model.EntryType
import io.github.xiaoancute.englisheasy.data.model.Misconception
import io.github.xiaoancute.englisheasy.data.model.Scenario
import io.github.xiaoancute.englisheasy.data.model.label
import io.github.xiaoancute.englisheasy.data.pronunciation.PronunciationAudio
import kotlinx.coroutines.launch

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
    sourceSentence: String = "",
    userExample: String = "",
    onExampleChange: ((String) -> Unit)? = null,
    onReviewExample: (() -> Unit)? = null,
    isReviewingExample: Boolean = false,
    exampleFeedback: ExampleFeedback? = null,
    exampleFeedbackError: String? = null,
    scrollable: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val player = remember { MediaPlayer() }
    DisposableEffect(player) {
        onDispose {
            player.release()
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
            coreConcept = if (card.branches == null) card.coreConcept else null,
            isFavorite = isFavorite,
            onFavoriteChange = onFavoriteChange,
            onShareClick = onShareClick,
            onCopyClick = onCopyClick,
            onRefreshClick = onRefreshClick,
            onPronounceClick = {
                scope.launch {
                    val audioUrl = PronunciationAudio.findUrl(card.word)
                    if (audioUrl == null) {
                        Toast.makeText(context, "暂无真人发音", Toast.LENGTH_SHORT).show()
                    } else {
                        player.playPronunciation(audioUrl) {
                            Toast.makeText(context, "发音播放失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
        )

        if (sourceSentence.isNotBlank()) {
            SourceSentenceSection(sourceSentence)
        }

        if (card.branches != null) {
            BranchesSection(
                branches = card.branches,
            )
        } else {
            SingleCardBody(
                card = card,
                showCoreConcept = false,
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
                onReviewExample = onReviewExample,
                isReviewingExample = isReviewingExample,
                feedback = exampleFeedback,
                feedbackError = exampleFeedbackError,
            )
        }
    }
}

@Composable
private fun SourceSentenceSection(sourceSentence: String) {
    Section(title = "来源句子") {
        SurfaceCard(tone = SurfaceTone.Tonal, contentPadding = 16.dp) {
            Text(
                text = sourceSentence,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun WordHeader(
    word: String,
    entryType: EntryType,
    coreConcept: CoreConcept?,
    isFavorite: Boolean,
    onFavoriteChange: ((Boolean) -> Unit)?,
    onShareClick: (() -> Unit)?,
    onCopyClick: (() -> Unit)?,
    onRefreshClick: (() -> Unit)?,
    onPronounceClick: () -> Unit,
) {
    SurfaceCard(tone = SurfaceTone.Hero) {
        Row(
            modifier = Modifier.fillMaxWidth(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            )
            EntryTypePill(entryType)
        }

        coreConcept?.let { core ->
            Text(
                text = core.picture,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "锚词",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                    modifier = Modifier.padding(end = 8.dp),
                )
                HeroAnchorChip(core.anchorWord)
            }
        }

        HeaderActions(
            isFavorite = isFavorite,
            onFavoriteChange = onFavoriteChange,
            onShareClick = onShareClick,
            onCopyClick = onCopyClick,
            onRefreshClick = onRefreshClick,
            onPronounceClick = onPronounceClick,
        )
    }
}

@Composable
private fun HeroAnchorChip(word: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.52f),
    ) {
        Text(
            text = word,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun EntryTypePill(entryType: EntryType) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
    ) {
        Text(
            text = entryType.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
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
    onPronounceClick: () -> Unit,
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
        IconButton(onClick = onPronounceClick) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "播放真人发音",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        if (onFavoriteChange != null) {
            IconButton(onClick = { onFavoriteChange(!isFavorite) }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) "取消收藏" else "收藏",
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
                )
            }
        }
    }
}

@Composable
private fun SingleCardBody(
    card: ConceptCard,
    showCoreConcept: Boolean = true,
) {
    if (showCoreConcept) {
        card.coreConcept?.let { core ->
            CoreConceptBlock(core)
        }
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
            scenarios.forEach { sc -> ScenarioItem(sc) }
        }
    }

    card.misconceptions?.takeIf { it.isNotEmpty() }?.let { miscons ->
        // 默认折叠，避免先植入错误印象；看完核心概念后再展开
        var expanded by remember(card.word) { mutableStateOf(false) }
        Section(title = "错误直觉 · 过渡拐杖") {
            if (expanded) {
                miscons.forEach { mc -> MisconceptionItem(mc) }
                TextButton(onClick = { expanded = false }) {
                    Text("收起")
                }
            } else {
                SurfaceCard(
                    tone = SurfaceTone.Tonal,
                    contentPadding = 14.dp,
                    modifier = Modifier.clickable { expanded = true },
                ) {
                    Text(
                        text = "点击展开（建议先看完核心概念与场景）",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** 核心概念：作为学习笔记的主段落，用轻量底色突出，但不做大装饰卡。 */
@Composable
private fun CoreConceptBlock(core: CoreConcept) {
    SurfaceCard(tone = SurfaceTone.Tonal) {
        SectionLabel("核心概念")
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
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
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

/** 单个场景：例句 + 画面解释，保持轻量行块。 */
@Composable
private fun ScenarioItem(sc: Scenario) {
    SurfaceCard(contentPadding = 16.dp) {
        Text(
            text = sc.englishExample,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
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
        Surface(shape = RoundedCornerShape(8.dp), color = tagBg) {
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
) {
    SectionHeader(
        title = when (branches.firstOrNull()?.type) {
            BranchType.HOMONYM -> "本词其实是两个独立的词"
            BranchType.SEMANTIC_CLUSTER -> "本词分化成多个语义簇"
            null -> "多义分支"
        },
    )
    branches.forEachIndexed { idx, branch ->
        SurfaceCard(tone = SurfaceTone.Tonal) {
            SectionHeader(
                title = "分支 ${idx + 1}",
                subtitle = branch.relationNote?.let { "共同祖源：$it" },
            )
            SingleCardBody(
                card = branch.card,
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
            placeholder = { Text("笔记") },
            shape = RoundedCornerShape(16.dp),
        )
    }
}

@Composable
private fun UserExampleSection(
    example: String,
    onExampleChange: (String) -> Unit,
    onReviewExample: (() -> Unit)?,
    isReviewingExample: Boolean,
    feedback: ExampleFeedback?,
    feedbackError: String?,
) {
    Section(title = "我的输出") {
        OutlinedTextField(
            value = example,
            onValueChange = onExampleChange,
            modifier = Modifier.fillMaxWidth(1f),
            minLines = 2,
            maxLines = 4,
            placeholder = { Text("Your sentence") },
            shape = RoundedCornerShape(16.dp),
        )
        if (onReviewExample != null) {
            Button(
                onClick = onReviewExample,
                enabled = !isReviewingExample,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(EnglishEasySpacing.PillRadius),
            ) {
                if (isReviewingExample) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("检查例句")
                }
            }
        }
        feedback?.let { ExampleFeedbackBlock(it) }
        feedbackError?.let { ExampleFeedbackError(it) }
    }
}

@Composable
private fun ExampleFeedbackBlock(feedback: ExampleFeedback) {
    SurfaceCard(tone = SurfaceTone.Tonal, contentPadding = 16.dp) {
        Text(
            text = feedback.verdict,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = feedback.improvedExample,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = feedback.reason,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExampleFeedbackError(message: String) {
    SurfaceCard(tone = SurfaceTone.Tonal, contentPadding = 16.dp) {
        Text(
            text = "检查失败",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun MediaPlayer.playPronunciation(
    audioUrl: String,
    onError: () -> Unit,
) {
    runCatching {
        reset()
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build(),
        )
        setDataSource(audioUrl)
        setOnPreparedListener { it.start() }
        setOnErrorListener { _, _, _ ->
            onError()
            true
        }
        prepareAsync()
    }.onFailure {
        onError()
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = title)
        content()
    }
}
