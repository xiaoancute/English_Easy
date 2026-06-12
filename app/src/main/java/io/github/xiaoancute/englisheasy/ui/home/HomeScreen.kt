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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.model.toShareText
import io.github.xiaoancute.englisheasy.ui.components.ConceptCardView
import io.github.xiaoancute.englisheasy.ui.components.EnglishEasySpacing
import io.github.xiaoancute.englisheasy.ui.components.SectionHeader
import io.github.xiaoancute.englisheasy.ui.components.StatePanel
import io.github.xiaoancute.englisheasy.ui.components.SurfaceCard
import io.github.xiaoancute.englisheasy.ui.components.SurfaceTone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    initialWord: String? = null,
    markLearningOnSuccess: Boolean = false,
    onWordConsumed: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val isConfigured by viewModel.isConfigured.collectAsState()
    var input by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingStudyLookupWord by remember { mutableStateOf<String?>(null) }

    fun performLookup(query: String = input) {
        val normalized = query.trim()
        if (normalized.isBlank()) return
        input = normalized
        viewModel.lookup(normalized)
        keyboard?.hide()
    }

    // 从其他页跳转过来时自动查询
    LaunchedEffect(initialWord, markLearningOnSuccess) {
        if (initialWord != null) {
            input = initialWord
            pendingStudyLookupWord = if (markLearningOnSuccess) initialWord else null
            viewModel.lookup(
                word = initialWord,
                markLearningOnSuccess = markLearningOnSuccess,
            )
            onWordConsumed()
        }
    }

    LaunchedEffect(state, pendingStudyLookupWord) {
        val word = pendingStudyLookupWord ?: return@LaunchedEffect
        when (val currentState = state) {
            is HomeUiState.Success -> {
                snackbarHostState.showSnackbar("已加入学习中：${currentState.card.word}")
                pendingStudyLookupWord = null
            }
            is HomeUiState.Error -> {
                snackbarHostState.showSnackbar("查询失败，未加入学习进度：$word")
                pendingStudyLookupWord = null
            }
            HomeUiState.Idle, HomeUiState.Loading -> Unit
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("英易 English Easy") },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(
                    horizontal = EnglishEasySpacing.PageHorizontal,
                    vertical = EnglishEasySpacing.PageVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(EnglishEasySpacing.SectionGap),
        ) {
            LookupPanel(
                input = input,
                onInputChange = { input = it },
                onLookup = { performLookup() },
            )

            ResultArea(
                state = state,
                isConfigured = isConfigured,
                onExampleLookup = { performLookup(it) },
                onFavoriteChange = viewModel::setFavorite,
                onNoteChange = viewModel::setNote,
                onExampleChange = viewModel::setExample,
                onRefresh = viewModel::refreshCurrent,
                onShare = { card, note, example -> shareCard(context, card, note, example) },
                onCopy = { card, note, example -> copyCard(context, card, note, example) },
                onRetry = { performLookup() },
            )
        }
    }
}

@Composable
private fun LookupPanel(
    input: String,
    onInputChange: (String) -> Unit,
    onLookup: () -> Unit,
) {
    SurfaceCard(tone = SurfaceTone.Tonal) {
        SectionHeader(
            title = "概念还原",
            subtitle = "先看核心画面，再写自己的例句",
        )

        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            placeholder = { Text("spring / break the ice") },
            singleLine = true,
            shape = RoundedCornerShape(EnglishEasySpacing.PillRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(onSearch = { onLookup() }),
        )
        Button(
            onClick = onLookup,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(EnglishEasySpacing.PillRadius),
        ) {
            Text("还原概念")
        }
    }
}

@Composable
private fun ResultArea(
    state: HomeUiState,
    isConfigured: Boolean,
    onExampleLookup: (String) -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
    onNoteChange: (String) -> Unit,
    onExampleChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onShare: (ConceptCard, String, String) -> Unit,
    onCopy: (ConceptCard, String, String) -> Unit,
    onRetry: () -> Unit,
) {
    when (state) {
        HomeUiState.Idle -> if (isConfigured) {
            IdleHint(onExampleLookup = onExampleLookup)
        } else {
            SetupGuide()
        }
        HomeUiState.Loading -> LoadingIndicator()
        is HomeUiState.Success -> ConceptCardView(
            card = state.card,
            isFavorite = state.isFavorite,
            onFavoriteChange = onFavoriteChange,
            onShareClick = { onShare(state.card, state.userNote, state.userExample) },
            onCopyClick = { onCopy(state.card, state.userNote, state.userExample) },
            onRefreshClick = onRefresh,
            userNote = state.userNote,
            onNoteChange = onNoteChange,
            userExample = state.userExample,
            onExampleChange = onExampleChange,
        )
        is HomeUiState.Error -> ErrorView(message = state.message, onRetry = onRetry)
    }
}

private fun shareCard(
    context: android.content.Context,
    card: ConceptCard,
    userNote: String,
    userExample: String,
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "英易概念卡：${card.word}")
        putExtra(Intent.EXTRA_TEXT, card.toShareText(userNote, userExample))
    }
    context.startActivity(
        Intent.createChooser(sendIntent, "分享概念卡")
    )
}

private fun copyCard(
    context: android.content.Context,
    card: ConceptCard,
    userNote: String,
    userExample: String,
) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(
        ClipData.newPlainText("英易概念卡：${card.word}", card.toShareText(userNote, userExample))
    )
    Toast.makeText(context, "已复制概念卡", Toast.LENGTH_SHORT).show()
}

@Composable
private fun IdleHint(
    onExampleLookup: (String) -> Unit,
) {
    SurfaceCard(tone = SurfaceTone.Tonal) {
        SectionHeader(
            title = "从容易误解的词开始",
            subtitle = "查完重点看核心概念和典型场景，最后写一句自己的英文。",
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ExampleChip(text = "spring", onClick = onExampleLookup)
            ExampleChip(text = "break the ice", onClick = onExampleLookup)
            ExampleChip(text = "worth", onClick = onExampleLookup)
        }
    }
}

@Composable
private fun ExampleChip(
    text: String,
    onClick: (String) -> Unit,
) {
    AssistChip(
        onClick = { onClick(text) },
        label = { Text(text) },
        shape = RoundedCornerShape(EnglishEasySpacing.PillRadius),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        border = null,
    )
}

@Composable
private fun SetupGuide() {
    StatePanel(
        title = "需要先配置 AI 服务",
        body = "在底部「设置」里填入 Base URL、模型和 API Key 后即可查词。查完可以保存理解和自己的例句。",
    )
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
                text = "正在还原核心画面……",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.7f),
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
) {
    val (title, hint) = when {
        message.contains("请先在设置里填入") -> "未配置 API" to "点击底部「设置」tab，填入你的 API Key。"
        message.contains("401") || message.contains("Unauthorized") -> "API Key 无效" to "请检查设置里的 API Key 是否正确。"
        message.contains("403") || message.contains("权限不足") -> "权限不足" to message
        message.contains("404") || message.contains("不存在") -> "配置有误" to message
        message.contains("429") || message.contains("额度受限") -> "请求受限" to message
        message.contains("timeout") ||
            message.contains("Unable to resolve host") ||
            message.contains("网络连接失败") ||
            message.contains("请求超时") -> "网络连接失败" to "请检查网络连接或 Base URL 是否正确。"
        message.contains("JSON") || message.contains("响应") || message.contains("解析失败") -> "解析失败" to "LLM 响应格式异常，请稍后重试。"
        else -> "出错了" to message
    }

    StatePanel(
        title = title,
        body = hint,
        action = {
            TextButton(onClick = onRetry) {
                Text("重试")
            }
        },
    )
}
