package io.github.xiaoancute.englisheasy.ui.history

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xiaoancute.englisheasy.data.local.ConceptCardEntity
import io.github.xiaoancute.englisheasy.data.prompt.CURRENT_PROMPT_VERSION
import io.github.xiaoancute.englisheasy.ui.components.EnglishEasySpacing
import io.github.xiaoancute.englisheasy.ui.components.StatePanel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onWordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val history by viewModel.history.collectAsState()

    SavedCardsScreen(
        title = "查询历史",
        emptyText = "暂无历史记录",
        cards = history,
        onWordClick = onWordClick,
        onDelete = viewModel::delete,
        onFavoriteChange = viewModel::setFavorite,
        exportText = viewModel::exportText,
        onClearAll = viewModel::clearAll,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onWordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val favorites by viewModel.favorites.collectAsState()

    SavedCardsScreen(
        title = "收藏",
        emptyText = "暂无收藏",
        cards = favorites,
        onWordClick = onWordClick,
        onDelete = viewModel::delete,
        onFavoriteChange = viewModel::setFavorite,
        exportText = viewModel::exportText,
        onClearAll = null,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedCardsScreen(
    title: String,
    emptyText: String,
    cards: List<ConceptCardEntity>,
    onWordClick: (String) -> Unit,
    onDelete: (String) -> Unit,
    onFavoriteChange: (String, Boolean) -> Unit,
    exportText: (ConceptCardEntity) -> String?,
    onClearAll: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    if (cards.isNotEmpty() && onClearAll != null) {
                        IconButton(onClick = onClearAll) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "清空历史")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (cards.isEmpty()) {
            val emptyBody = if (title == "查询历史") {
                "查过的词会按时间出现在这里。"
            } else {
                "收藏的概念卡会出现在这里，方便回看和复制。"
            }
            EmptyState(
                title = emptyText,
                body = emptyBody,
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(
                        horizontal = EnglishEasySpacing.PageHorizontal,
                        vertical = EnglishEasySpacing.PageVertical,
                    ),
                verticalArrangement = Arrangement.spacedBy(EnglishEasySpacing.ItemGap),
            ) {
                items(cards, key = { it.word }) { entity ->
                    HistoryItem(
                        entity = entity,
                        onClick = { onWordClick(entity.word) },
                        onDelete = { onDelete(entity.word) },
                        onFavoriteChange = { onFavoriteChange(entity.word, it) },
                        onCopy = {
                            exportText(entity)?.let { text ->
                                copyText(context, entity.word, text)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = EnglishEasySpacing.PageHorizontal),
        contentAlignment = Alignment.Center,
    ) {
        StatePanel(title = title, body = body)
    }
}

@Composable
private fun HistoryItem(
    entity: ConceptCardEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
    onCopy: () -> Unit,
) {
    val hasBadges = entity.userNote.isNotBlank() ||
        entity.userExample.isNotBlank() ||
        entity.promptVersion < CURRENT_PROMPT_VERSION

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(EnglishEasySpacing.CardRadius),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(EnglishEasySpacing.ItemGap),
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
                        text = entity.word,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = formatTimestamp(entity.queriedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (hasBadges) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (entity.userNote.isNotBlank()) {
                                InlineBadge(
                                    icon = Icons.Default.EditNote,
                                    text = "有笔记",
                                    contentDescription = "有笔记",
                                )
                            }
                            if (entity.userExample.isNotBlank()) {
                                InlineBadge(
                                    icon = Icons.Default.EditNote,
                                    text = "有例句",
                                    contentDescription = "有例句",
                                )
                            }
                            if (entity.promptVersion < CURRENT_PROMPT_VERSION) {
                                InlineBadge(
                                    icon = Icons.Default.Update,
                                    text = "可更新",
                                    contentDescription = "概念可更新",
                                )
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onFavoriteChange(!entity.isFavorite) }) {
                        Icon(
                            imageVector = if (entity.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (entity.isFavorite) "取消收藏" else "收藏",
                            tint = if (entity.isFavorite) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "复制",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    contentDescription: String,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

private fun copyText(
    context: android.content.Context,
    word: String,
    text: String,
) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(
        ClipData.newPlainText("英易概念卡：$word", text)
    )
    Toast.makeText(context, "已复制概念卡", Toast.LENGTH_SHORT).show()
}

private fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
