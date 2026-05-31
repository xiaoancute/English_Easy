package io.github.xiaoancute.englisheasy.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xiaoancute.englisheasy.data.settings.PresetColors
import io.github.xiaoancute.englisheasy.data.settings.ProviderConfig
import io.github.xiaoancute.englisheasy.ui.about.AboutScreen
import io.github.xiaoancute.englisheasy.data.settings.ThemeConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val saved by viewModel.config.collectAsState()
    val savedTheme by viewModel.themeConfig.collectAsState()

    var apiKey by remember { mutableStateOf(saved.apiKey) }
    var baseUrl by remember { mutableStateOf(saved.baseUrl) }
    var model by remember { mutableStateOf(saved.model) }
    var keyVisible by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(savedTheme.themeColor) }
    var useDynamicColor by remember { mutableStateOf(savedTheme.useDynamicColor) }

    if (showAbout) {
        AboutScreen(onBack = { showAbout = false }, modifier = modifier)
        return
    }

    // saved 异步加载完成后，回填到本地表单
    LaunchedEffect(saved) {
        apiKey = saved.apiKey
        baseUrl = saved.baseUrl
        model = saved.model
    }

    LaunchedEffect(savedTheme) {
        selectedColor = savedTheme.themeColor
        useDynamicColor = savedTheme.useDynamicColor
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("设置") },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "LLM Provider 配置（BYOK）",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "本应用直接调用你自己的 LLM API。所有 key 仅保存在设备本地。\n" +
                        "兼容任意 OpenAI 风格端点：OpenAI / DeepSeek / Moonshot / 智谱 / Groq / 本地 Ollama 等。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Base URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(1f),
                supportingText = {
                    Text(
                        "OpenAI: https://api.openai.com/v1/\n" +
                                "DeepSeek: https://api.deepseek.com/v1/\n" +
                                "Moonshot: https://api.moonshot.cn/v1/"
                    )
                },
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("模型名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(1f),
                supportingText = { Text("例：gpt-4o-mini / deepseek-chat / moonshot-v1-8k") },
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(
                            imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (keyVisible) "隐藏" else "显示",
                        )
                    }
                },
            )

            Button(
                onClick = {
                    viewModel.save(
                        ProviderConfig(
                            apiKey = apiKey.trim(),
                            baseUrl = baseUrl.trim(),
                            model = model.trim(),
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(1f),
            ) {
                Text("保存")
            }

            ThemeSection(
                selectedColor = selectedColor,
                useDynamicColor = useDynamicColor,
                onDynamicColorChange = { enabled ->
                    useDynamicColor = enabled
                    viewModel.saveTheme(
                        ThemeConfig(
                            themeColor = selectedColor,
                            useDynamicColor = enabled,
                        )
                    )
                },
                onColorSelected = { color ->
                    selectedColor = color
                    viewModel.saveTheme(
                        ThemeConfig(
                            themeColor = color,
                            useDynamicColor = useDynamicColor,
                        )
                    )
                },
            )

            TextButton(
                onClick = { showAbout = true },
                modifier = Modifier.fillMaxWidth(1f),
            ) {
                Text("关于英易 · 隐私说明")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeSection(
    selectedColor: Int,
    useDynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    onColorSelected: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "外观",
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            ) {
                Text(
                    text = "跟随系统取色",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        "使用系统壁纸生成的颜色"
                    } else {
                        "Android 12 及以上可用"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                )
            }
            Switch(
                checked = useDynamicColor,
                onCheckedChange = onDynamicColorChange,
                enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
            )
        }
        Text(
            text = "主题色",
            style = MaterialTheme.typography.bodyMedium,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PresetColors.entries.forEach { preset ->
                ColorSwatch(
                    color = preset.color,
                    selected = selectedColor == preset.argb,
                    label = preset.label,
                    onClick = { onColorSelected(preset.argb) },
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = 2.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = CircleShape,
            )
            .clickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
