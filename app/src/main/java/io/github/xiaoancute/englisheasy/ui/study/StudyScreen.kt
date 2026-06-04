package io.github.xiaoancute.englisheasy.ui.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xiaoancute.englisheasy.data.learning.LearningDashboard
import io.github.xiaoancute.englisheasy.data.learning.TodayStudyTask
import io.github.xiaoancute.englisheasy.data.review.ReviewGrade
import io.github.xiaoancute.englisheasy.data.vocabulary.VocabularyPack
import io.github.xiaoancute.englisheasy.ui.components.ConceptCardView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    onWordClick: (String) -> Unit,
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
                    onReview = { grade -> current?.let { viewModel.review(it.entity, grade) } },
                    onStartWord = { word ->
                        viewModel.startLearning(word)
                        onWordClick(word)
                    },
                    onWeakWordClick = onWordClick,
                    onSkipWord = viewModel::skipWord,
                    onOpenPacks = { selectedTab = 1 },
                )
            } else {
                VocabularySection(
                    packs = vocabularyPacks,
                    dashboard = dashboard,
                    skippedWords = skippedWords,
                    onPackSelected = viewModel::selectPack,
                    onRestoreSkippedWord = viewModel::restoreSkippedWord,
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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
    }
}

@Composable
private fun LearningOverviewCard(
    dashboard: LearningDashboard,
    task: TodayStudyTask,
    onOpenPacks: () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "今日概览",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = dashboard.selectedPackLabel ?: "未选择词库",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                OutlinedButton(onClick = onOpenPacks) {
                    Text(if (dashboard.hasSelectedPack) "切换" else "选择")
                }
            }

            if (dashboard.hasSelectedPack) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "学习进度",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${dashboard.learnedCount} / ${dashboard.totalCount} · ${dashboard.progressPercent}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { dashboard.progressFraction },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Text(
                    text = "先选一个词库，系统会按这个范围安排新词和复习。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = "下一步：${taskLabel(task)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SummaryMetric(
                    label = "到期复习",
                    value = dashboard.dueReviewCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                SummaryMetric(
                    label = "今日新词",
                    value = dashboard.todayWordCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SummaryMetric(
                    label = "可安排",
                    value = dashboard.availableCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                SummaryMetric(
                    label = "已跳过",
                    value = dashboard.skippedCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun WeakWordsCard(
    words: List<String>,
    onWordClick: (String) -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "薄弱词",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${words.size} 个需要多看几眼",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            words.take(WEAK_WORD_PREVIEW_LIMIT).forEach { word ->
                WeakWordRow(
                    word = word,
                    onClick = { onWordClick(word) },
                )
            }
        }
    }
}

@Composable
private fun WeakWordRow(
    word: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TextButton(onClick = onClick) {
                Text("查看")
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WordStatusLabel(text = "今日新词 · $remainingCount 个")
            Text(
                text = word,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
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
}

@Composable
private fun PlainStateCard(
    title: String,
    body: String,
    actionText: String,
    onAction: () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onAction) {
                Text(actionText)
            }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = "学习范围",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
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
                Text(
                    text = "已跳过 · ${skippedWords.size} 个",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
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
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "当前学习范围",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = dashboard.selectedPackLabel ?: "未选择词库",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (dashboard.hasSelectedPack) {
                Text(
                    text = "已学 ${dashboard.learnedCount} / ${dashboard.totalCount} · ${dashboard.progressPercent}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LinearProgressIndicator(
                    progress = { dashboard.progressFraction },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Text(
                text = "今日可安排 ${dashboard.availableCount} 个",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SkippedWordItem(
    word: String,
    onRestore: () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
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
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${pack.stage.label}词库",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (selected) {
                            WordStatusLabel(text = "当前")
                        }
                    }
                    Text(
                        text = "${pack.learnedCount} / ${pack.totalCount} · ${packProgressPercent(pack)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(onClick = onClick) {
                    Text(if (selected) "已选" else "选择")
                }
            }

            LinearProgressIndicator(
                progress = { packProgressFraction(pack) },
                modifier = Modifier.fillMaxWidth(),
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "$remainingCount 张到期",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (revealed && studyCard.card != null) {
            ConceptCardView(
                card = studyCard.card,
                isFavorite = studyCard.entity.isFavorite,
                userNote = studyCard.entity.userNote,
                scrollable = false,
            )
        } else {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = studyCard.entity.word,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    OutlinedButton(onClick = onReveal) {
                        Text("显示概念卡")
                    }
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
