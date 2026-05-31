package io.github.xiaoancute.englisheasy.ui.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xiaoancute.englisheasy.data.review.ReviewGrade
import io.github.xiaoancute.englisheasy.ui.components.ConceptCardView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    modifier: Modifier = Modifier,
    viewModel: StudyViewModel = hiltViewModel(),
) {
    val dueCards by viewModel.dueCards.collectAsState()
    val current = dueCards.firstOrNull()
    var revealedWord by remember { mutableStateOf<String?>(null) }

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
        if (current == null) {
            EmptyStudyState(modifier = Modifier.padding(innerPadding))
        } else {
            StudyCardContent(
                studyCard = current,
                remainingCount = dueCards.size,
                revealed = revealedWord == current.entity.word,
                onReveal = { revealedWord = current.entity.word },
                onReview = { grade -> viewModel.review(current.entity, grade) },
                modifier = Modifier.padding(innerPadding),
            )
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
