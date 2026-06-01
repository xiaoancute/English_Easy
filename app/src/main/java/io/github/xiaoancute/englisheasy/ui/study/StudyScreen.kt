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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
    val dueCards by viewModel.dueCards.collectAsState()
    val vocabularyPacks by viewModel.vocabularyPacks.collectAsState()
    val selectedWords by viewModel.selectedWords.collectAsState()
    val todayWords by viewModel.todayWords.collectAsState()
    val skippedWords by viewModel.skippedWords.collectAsState()
    val selectedPackLabel by viewModel.selectedPackLabel.collectAsState()
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
                    currentReview = current,
                    dueReviewCount = dueCards.size,
                    todayWordCount = todayWords.size,
                    selectedPackLabel = selectedPackLabel,
                    revealed = current != null && revealedWord == current.entity.word,
                    onReveal = { current?.let { revealedWord = it.entity.word } },
                    onReview = { grade -> current?.let { viewModel.review(it.entity, grade) } },
                    onStartWord = { word ->
                        viewModel.startLearning(word)
                        onWordClick(word)
                    },
                    onSkipWord = viewModel::skipWord,
                    onOpenPacks = { selectedTab = 1 },
                )
            } else {
                VocabularySection(
                    packs = vocabularyPacks,
                    skippedWords = skippedWords,
                    selectedWords = selectedWords,
                    selectedPackLabel = selectedPackLabel,
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
    currentReview: StudyCard?,
    dueReviewCount: Int,
    todayWordCount: Int,
    selectedPackLabel: String?,
    revealed: Boolean,
    onReveal: () -> Unit,
    onReview: (ReviewGrade) -> Unit,
    onStartWord: (String) -> Unit,
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
            TodaySummary(
                dueReviewCount = dueReviewCount,
                todayWordCount = todayWordCount,
                selectedPackLabel = selectedPackLabel,
            )
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
                    title = "选择词库",
                    body = "先选一个范围，今日任务会从这个词库里取词。",
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
private fun TodaySummary(
    dueReviewCount: Int,
    todayWordCount: Int,
    selectedPackLabel: String?,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "今日任务",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SummaryMetric(
                    label = "到期复习",
                    value = dueReviewCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                SummaryMetric(
                    label = "新词",
                    value = todayWordCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = selectedPackLabel ?: "未选择词库",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
    skippedWords: List<String>,
    selectedWords: List<String>,
    selectedPackLabel: String?,
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
                text = "词库管理",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        items(packs, key = { it.stage.name }) { pack ->
            VocabularyPackItem(
                pack = pack,
                selected = "${pack.stage.label}词库" == selectedPackLabel,
                onClick = { onPackSelected(pack) },
            )
        }

        item {
            SelectedPackSummary(
                selectedPackLabel = selectedPackLabel,
                availableCount = selectedWords.size,
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
    selectedPackLabel: String?,
    availableCount: Int,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "当前范围",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = selectedPackLabel ?: "未选择词库",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "可安排 $availableCount 个",
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${pack.stage.label}词库",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (selected) {
                        WordStatusLabel(text = "当前")
                    }
                }
                Text(
                    text = "${pack.learnedCount} / ${pack.totalCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = onClick) {
                Text(if (selected) "已选" else "选择")
            }
        }
    }
}

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
