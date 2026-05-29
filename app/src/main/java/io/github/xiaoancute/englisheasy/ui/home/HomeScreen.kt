package io.github.xiaoancute.englisheasy.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.model.toShareText
import io.github.xiaoancute.englisheasy.ui.components.ConceptCardView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    initialWord: String? = null,
    onWordConsumed: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var input by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // 从历史页跳转过来时自动查询
    if (initialWord != null && input != initialWord) {
        input = initialWord
        viewModel.lookup(initialWord)
        onWordConsumed()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("英易 English Easy") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("输入单词或短语，例：spring / break the ice") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(1f),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.lookup(input)
                        keyboard?.hide()
                    },
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.lookup(input)
                        keyboard?.hide()
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "查询")
                    }
                },
            )

            ResultArea(
                state = state,
                onFavoriteChange = viewModel::setFavorite,
                onNoteChange = viewModel::setNote,
                onRefresh = viewModel::refreshCurrent,
                onShare = { card, note -> shareCard(context, card, note) },
                onCopy = { card, note -> copyCard(context, card, note) },
            )
        }
    }
}

@Composable
private fun ResultArea(
    state: HomeUiState,
    onFavoriteChange: (Boolean) -> Unit,
    onNoteChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onShare: (ConceptCard, String) -> Unit,
    onCopy: (ConceptCard, String) -> Unit,
) {
    when (state) {
        HomeUiState.Idle -> IdleHint()
        HomeUiState.Loading -> LoadingIndicator()
        is HomeUiState.Success -> ConceptCardView(
            card = state.card,
            isFavorite = state.isFavorite,
            onFavoriteChange = onFavoriteChange,
            onShareClick = { onShare(state.card, state.userNote) },
            onCopyClick = { onCopy(state.card, state.userNote) },
            onRefreshClick = onRefresh,
            userNote = state.userNote,
            onNoteChange = onNoteChange,
        )
        is HomeUiState.Error -> ErrorView(message = state.message)
    }
}

private fun shareCard(
    context: android.content.Context,
    card: ConceptCard,
    userNote: String,
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "英易概念卡：${card.word}")
        putExtra(Intent.EXTRA_TEXT, card.toShareText(userNote))
    }
    context.startActivity(
        Intent.createChooser(sendIntent, "分享概念卡")
    )
}

private fun copyCard(
    context: android.content.Context,
    card: ConceptCard,
    userNote: String,
) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(
        ClipData.newPlainText("英易概念卡：${card.word}", card.toShareText(userNote))
    )
    Toast.makeText(context, "已复制概念卡", Toast.LENGTH_SHORT).show()
}

@Composable
private fun IdleHint() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "把英文词在母语者大脑里的样子，重新呈现给你",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
    }
}

@Composable
private fun LoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = "正在还原概念……",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.7f),
            )
        }
    }
}

@Composable
private fun ErrorView(message: String) {
    val (title, hint) = when {
        message.contains("请先在设置里填入") -> "未配置 API" to "点击右上角设置按钮，填入你的 API Key"
        message.contains("401") || message.contains("Unauthorized") -> "API Key 无效" to "请检查设置里的 API Key 是否正确"
        message.contains("timeout") || message.contains("Unable to resolve host") -> "网络连接失败" to "请检查网络连接或 Base URL 是否正确"
        message.contains("JSON") || message.contains("响应") -> "解析失败" to "LLM 响应格式异常，请稍后重试"
        else -> "出错了" to message
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}
