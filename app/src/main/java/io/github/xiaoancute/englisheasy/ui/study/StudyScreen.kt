package io.github.xiaoancute.englisheasy.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xiaoancute.englisheasy.data.learning.LearningDashboard
import io.github.xiaoancute.englisheasy.data.learning.TodayStudyTask
import io.github.xiaoancute.englisheasy.data.review.ReviewGrade
import io.github.xiaoancute.englisheasy.data.vocabulary.VocabularyPack
import io.github.xiaoancute.englisheasy.ui.components.CompactInfoRow
import io.github.xiaoancute.englisheasy.ui.components.ConceptCardView
import io.github.xiaoancute.englisheasy.ui.components.EnglishEasySpacing
import io.github.xiaoancute.englisheasy.ui.components.QuietSurface
import io.github.xiaoancute.englisheasy.ui.components.SectionHeader
import io.github.xiaoancute.englisheasy.ui.components.StatePanel
import io.github.xiaoancute.englisheasy.ui.components.quietTextButtonColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    onWordClick: (String) -> Unit,
    onStudyTaskWordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StudyViewModel = hiltViewModel(),
) {
    val dashboard by viewModel.dashboard.collectAsState()
    val dueCards by viewModel.dueCards.collectAsState()
    val vocabularyPacks by viewModel.vocabularyPacks.collectAsState()
    val skippedWords by viewModel.skippedWords.collectAsState()
    val weakWords by viewModel.weakWords.collectAsState()
    val todayTask by viewModel.todayTask.collectAsState()
    val current = dueCards.firstOrNull()
    var revealedWord by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(current?.entity?.word) {
        revealedWord = null
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("学习") },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("今日") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("词库") },
                )
            }

            if (selectedTab == 0) {
                TodaySection(
                    task = todayTask,
                    dashboard = dashboard,
                    weakWords = weakWords,
                    currentReview = current,
                    revealed = current != null && revealedWord == current.entity.word,
                    onReveal = { current?.let { revealedWord = it.entity.word } },
                    onReview = { grade ->
                        current?.let {
                            viewModel.review(it.entity, grade)
                            scope.launch { snackbarHostState.showSnackbar("复习已记录") }
                        }
                    },
                    onStartWord = { word ->
                        onStudyTaskWordClick(word)
                    },
                    onWeakWordClick = onWordClick,
                    onSkipWord = { word ->
                        viewModel.skipWord(word)
                        scope.launch { snackbarHostState.showSnackbar("$word 已跳过，可在词库恢复") }
                    },
                    onOpenPacks = { selectedTab = 1 },
                )
            } else {
                VocabularySection(
                    packs = vocabularyPacks,
                    dashboard = dashboard,
                    skippedWords = skippedWords,
                    onPackSelected = viewModel::selectPack,
                    onRestoreSkippedWord = { word ->
                        viewModel.restoreSkippedWord(word)
                        scope.launch { snackbarHostState.showSnackbar("$word 已恢复") }
                    },
                )
            }
        }
    }
}

@Composable
private fun TodaySection(
    task: TodayStudyTask,
    dashboard: LearningDashboard,
    weakWords: List<String>,
    currentReview: StudyCard?,
    revealed: Boolean,
    onReveal: () -> Unit,
    onReview: (ReviewGrade) -> Unit,
    onStartWord: (String) -> Unit,
    onWeakWordClick: (String) -> Unit,
    onSkipWord: (String) -> Unit,
    onOpenPacks: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = EnglishEasySpacing.PageHorizontal,
                vertical = EnglishEasySpacing.PageVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(EnglishEasySpacing.SectionGap),
    ) {
        item {
            when (task) {
                is TodayStudyTask.Review -> {
                    if (currentReview == null) {
                        PlainStateCard(
                            title = "暂无到期复习",
                            body = "现在可以学一个新词，或者去词库切换范围。",
                            actionText = "去词库",
                            onAction = onOpenPacks,
                        )
                    } else {
                        StudyCardContent(
                            studyCard = currentReview,
                            remainingCount = task.dueReviewCount,
                            revealed = revealed,
                            onReveal = onReveal,
                            onReview = onReview,
                        )
                    }
                }

                is TodayStudyTask.NewWord -> NewWordTaskCard(
                    word = task.word,
                    remainingCount = task.remainingCount,
                    onStart = { onStartWord(task.word) },
                    onSkip = { onSkipWord(task.word) },
                )

                TodayStudyTask.ChoosePack -> PlainStateCard(
                    title = "选择学习范围",
                    body = "先选一个词库，系统会按这个范围安排新词和复习。",
                    actionText = "去词库",
                    onAction = onOpenPacks,
                )

                TodayStudyTask.Done -> PlainStateCard(
                    title = "今日完成",
                    body = "当前词库没有可安排的新词，也没有到期复习。",
                    actionText = "查看词库",
                    onAction = onOpenPacks,
                )
            }
        }

        item {
            LearningOverviewCard(
                dashboard = dashboard,
                task = task,
                onOpenPacks = onOpenPacks,
            )
        }

        if (weakWords.isNotEmpty()) {
            item {
                WeakWordsCard(
                    words = weakWords,
                    onWordClick = onWeakWordClick,
                )
            }
        }
    }
}

@Composable
private fun LearningOverviewCard(
    dashboard: LearningDashboard,
    task: TodayStudyTask,
    onOpenPacks: () -> Unit,
) {
    QuietSurface(tonal = true) {
        SectionHeader(
            title = "今日概览",
            subtitle = dashboard.selectedPackLabel ?: "未选择词库",
            trailing = {
                OutlinedButton(onClick = onOpenPacks) {
                    Text(if (dashboard.hasSelectedPack) "切换" else "选择")
                }
            }
        )

        if (dashboard.hasSelectedPack) {
            CompactInfoRow(
                label = "学习进度",
                value = "${dashboard.learnedCount} / ${dashboard.totalCount} · ${dashboard.progressPercent}%",
            )
            QuietProgressBar(progress = dashboard.progressFraction)
        } else {
            Text(
                text = "先选一个词库，系统会按这个范围安排新词和复习。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        CompactInfoRow(label = "下一步", value = taskLabel(task))
        CompactInfoRow(label = "到期复习", value = dashboard.dueReviewCount.toString())
        CompactInfoRow(label = "今日新词", value = dashboard.todayWordCount.toString())
        CompactInfoRow(label = "可安排", value = dashboard.availableCount.toString())
        CompactInfoRow(label = "已跳过", value = dashboard.skippedCount.toString())
    }
}

@Composable
private fun WeakWordsCard(
    words: List<String>,
    onWordClick: (String) -> Unit,
) {
    QuietSurface(tonal = true, contentPadding = 12.dp) {
        SectionHeader(
            title = "薄弱词",
            subtitle = "${words.size} 个需要多看几眼",
        )

        words.take(WEAK_WORD_PREVIEW_LIMIT).forEach { word ->
            WeakWordRow(
                word = word,
                onClick = { onWordClick(word) },
            )
        }
    }
}

@Composable
private fun WeakWordRow(
    word: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = word,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        TextButton(
            onClick = onClick,
            colors = quietTextButtonColors(),
        ) {
            Text("查看")
        }
    }
}

@Composable
private fun NewWordTaskCard(
    word: String,
    remainingCount: Int,
    onStart: () -> Unit,
    onSkip: () -> Unit,
) {
    QuietSurface(contentPadding = 18.dp) {
        WordStatusLabel(text = "今日新词 · $remainingCount 个")
        Text(
            text = word,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onStart,
                modifier = Modifier.weight(1f),
            ) {
                Text("学习这个词")
            }
            OutlinedButton(onClick = onSkip) {
                Text("跳过")
            }
        }
    }
}

@Composable
private fun PlainStateCard(
    title: String,
    body: String,
    actionText: String,
    onAction: () -> Unit,
) {
    StatePanel(
        title = title,
        body = body,
    ) {
        OutlinedButton(onClick = onAction) {
            Text(actionText)
        }
    }
}

@Composable
private fun VocabularySection(
    packs: List<VocabularyPack>,
    dashboard: LearningDashboard,
    skippedWords: List<String>,
    onPackSelected: (VocabularyPack) -> Unit,
    onRestoreSkippedWord: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = EnglishEasySpacing.PageHorizontal,
                vertical = EnglishEasySpacing.PageVertical,
            ),
        verticalArrangement = Arrangement.spacedBy(EnglishEasySpacing.ItemGap),
    ) {
        item {
            SectionHeader(
                title = "学习范围",
                subtitle = "选择新词和复习的来源",
            )
        }

        items(packs, key = { it.stage.name }) { pack ->
            VocabularyPackItem(
                pack = pack,
                selected = "${pack.stage.label}词库" == dashboard.selectedPackLabel,
                onClick = { onPackSelected(pack) },
            )
        }

        item {
            SelectedPackSummary(
                dashboard = dashboard,
            )
        }

        if (skippedWords.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "已跳过",
                    subtitle = "${skippedWords.size} 个，可恢复到学习范围",
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(skippedWords, key = { "skipped:$it" }) { word ->
                SkippedWordItem(
                    word = word,
                    onRestore = { onRestoreSkippedWord(word) },
                )
            }
        }
    }
}

@Composable
private fun SelectedPackSummary(
    dashboard: LearningDashboard,
) {
    QuietSurface(tonal = true, contentPadding = 12.dp) {
        SectionHeader(
            title = "当前学习范围",
            subtitle = dashboard.selectedPackLabel ?: "未选择词库",
        )
        if (dashboard.hasSelectedPack) {
            CompactInfoRow(
                label = "已学",
                value = "${dashboard.learnedCount} / ${dashboard.totalCount} · ${dashboard.progressPercent}%",
            )
            QuietProgressBar(progress = dashboard.progressFraction)
        }
        CompactInfoRow(label = "今日可安排", value = "${dashboard.availableCount} 个")
    }
}

@Composable
private fun SkippedWordItem(
    word: String,
    onRestore: () -> Unit,
) {
    QuietSurface(contentPadding = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            OutlinedButton(onClick = onRestore) {
                Text("恢复")
            }
        }
    }
}

@Composable
private fun WordStatusLabel(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun VocabularyPackItem(
    pack: VocabularyPack,
    selected: Boolean,
    onClick: () -> Unit,
) {
    QuietSurface(contentPadding = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${pack.stage.label}词库",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (selected) {
                        WordStatusLabel(text = "当前")
                    }
                }
                CompactInfoRow(
                    label = "进度",
                    value = "${pack.learnedCount} / ${pack.totalCount} · ${packProgressPercent(pack)}%",
                )
            }
            OutlinedButton(onClick = onClick) {
                Text(if (selected) "已选" else "选择")
            }
        }

        QuietProgressBar(progress = packProgressFraction(pack))
    }
}

@Composable
private fun QuietProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val normalized = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (normalized > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(normalized)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

private fun taskLabel(task: TodayStudyTask): String {
    return when (task) {
        is TodayStudyTask.Review -> "复习 ${task.dueReviewCount} 个到期词"
        is TodayStudyTask.NewWord -> "学习新词 · 剩余 ${task.remainingCount} 个"
        TodayStudyTask.ChoosePack -> "先选词库"
        TodayStudyTask.Done -> "今日完成"
    }
}

private fun packProgressPercent(pack: VocabularyPack): Int {
    return (packProgressFraction(pack) * 100).toInt()
}

private fun packProgressFraction(pack: VocabularyPack): Float {
    if (pack.totalCount <= 0) return 0f
    return (pack.learnedCount.toFloat() / pack.totalCount.toFloat()).coerceIn(0f, 1f)
}

private const val WEAK_WORD_PREVIEW_LIMIT = 5

@Composable
private fun StudyCardContent(
    studyCard: StudyCard,
    remainingCount: Int,
    revealed: Boolean,
    onReveal: () -> Unit,
    onReview: (ReviewGrade) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(EnglishEasySpacing.ItemGap),
    ) {
        WordStatusLabel(text = "复习 · $remainingCount 张到期")

        if (revealed && studyCard.card != null) {
            ConceptCardView(
                card = studyCard.card,
                isFavorite = studyCard.entity.isFavorite,
                userNote = studyCard.entity.userNote,
                scrollable = false,
            )
        } else {
            QuietSurface(contentPadding = 18.dp) {
                Text(
                    text = studyCard.entity.word,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                OutlinedButton(onClick = onReveal) {
                    Text("显示概念卡")
                }
            }
        }

        if (revealed) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onReview(ReviewGrade.FORGOT) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("忘了")
                }
                OutlinedButton(
                    onClick = { onReview(ReviewGrade.VAGUE) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("模糊")
                }
                Button(
                    onClick = { onReview(ReviewGrade.REMEMBERED) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("记得")
                }
            }
        }
    }
}
