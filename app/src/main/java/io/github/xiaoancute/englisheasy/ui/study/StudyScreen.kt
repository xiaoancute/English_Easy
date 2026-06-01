package io.github.xiaoancute.englisheasy.ui.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
                    text = { Text("复习") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("词库") },
                )
            }

            if (selectedTab == 0) {
                if (current == null) {
                    EmptyStudyState()
                } else {
                    StudyCardContent(
                        studyCard = current,
                        remainingCount = dueCards.size,
                        revealed = revealedWord == current.entity.word,
                        onReveal = { revealedWord = current.entity.word },
                        onReview = { grade -> viewModel.review(current.entity, grade) },
                    )
                }
            } else {
                VocabularySection(
                    packs = vocabularyPacks,
                    todayWords = todayWords,
                    skippedWords = skippedWords,
                    selectedWords = selectedWords,
                    onPackSelected = viewModel::selectPack,
                    onStartWord = { word ->
                        viewModel.startLearning(word)
                        onWordClick(word)
                    },
                    onSkipWord = viewModel::skipWord,
                    onRestoreSkippedWord = viewModel::restoreSkippedWord,
                )
            }
        }
    }
}

@Composable
private fun EmptyStudyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "暂无到期复习",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
    }
}

@Composable
private fun VocabularySection(
    packs: List<VocabularyPack>,
    todayWords: List<String>,
    skippedWords: List<String>,
    selectedWords: List<String>,
    onPackSelected: (VocabularyPack) -> Unit,
    onStartWord: (String) -> Unit,
    onSkipWord: (String) -> Unit,
    onRestoreSkippedWord: (String) -> Unit,
) {
    val unavailableWords = (todayWords + skippedWords).toSet()
    val remainingWords = selectedWords.filterNot { it in unavailableWords }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(packs, key = { it.stage.name }) { pack ->
            VocabularyPackItem(
                pack = pack,
                onClick = { onPackSelected(pack) },
            )
        }

        if (todayWords.isNotEmpty()) {
            item {
                Text(
                    text = "今日学习 · ${todayWords.size} 个",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(todayWords, key = { "today:$it" }) { word ->
                TodayWordItem(
                    word = word,
                    onStart = { onStartWord(word) },
                    onSkip = { onSkipWord(word) },
                )
            }
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

        if (remainingWords.isNotEmpty()) {
            item {
                Text(
                    text = "未学词",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(remainingWords, key = { "word:$it" }) { word ->
                OutlinedButton(
                    onClick = { onStartWord(word) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(word)
                }
            }
        }
    }
}

@Composable
private fun TodayWordItem(
    word: String,
    onStart: () -> Unit,
    onSkip: () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = word,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                WordStatusLabel(text = "待学习")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onStart) {
                    Text("开始学习")
                }
                OutlinedButton(onClick = onSkip) {
                    Text("跳过")
                }
            }
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
                Text(
                    text = "${pack.stage.label}词库",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${pack.learnedCount} / ${pack.totalCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = onClick) {
                Text("查看")
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
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                modifier = Modifier.weight(1f),
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
