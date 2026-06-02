package io.github.xiaoancute.englisheasy.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xiaoancute.englisheasy.data.settings.PresetColors
import io.github.xiaoancute.englisheasy.data.settings.ProviderConfig
import io.github.xiaoancute.englisheasy.data.settings.ThemeConfig
import io.github.xiaoancute.englisheasy.ui.about.AboutScreen

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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AiServiceSection(
                apiKey = apiKey,
                baseUrl = baseUrl,
                model = model,
                keyVisible = keyVisible,
                onApiKeyChange = { apiKey = it },
                onBaseUrlChange = { baseUrl = it },
                onModelChange = { model = it },
                onToggleKeyVisible = { keyVisible = !keyVisible },
                onSave = {
                    viewModel.save(
                        ProviderConfig(
                            apiKey = apiKey.trim(),
                            baseUrl = baseUrl.trim(),
                            model = model.trim(),
                        ),
                    )
                },
            )

            ThemeSection(
                selectedColor = selectedColor,
                useDynamicColor = useDynamicColor,
                onDynamicColorChange = { enabled ->
                    useDynamicColor = enabled
                    viewModel.saveTheme(
                        ThemeConfig(
                            themeColor = selectedColor,
                            useDynamicColor = enabled,
                        ),
                    )
                },
                onColorSelected = { color ->
                    selectedColor = color
                    viewModel.saveTheme(
                        ThemeConfig(
                            themeColor = color,
                            useDynamicColor = useDynamicColor,
                        ),
                    )
                },
            )

            AboutSection(onOpenAbout = { showAbout = true })
        }
    }
}

@Composable
private fun AiServiceSection(
    apiKey: String,
    baseUrl: String,
    model: String,
    keyVisible: Boolean,
    onApiKeyChange: (String) -> Unit,
    onBaseUrlChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onToggleKeyVisible: () -> Unit,
    onSave: () -> Unit,
) {
    val configured = apiKey.isNotBlank() && baseUrl.isNotBlank() && model.isNotBlank()

    SettingsCard(
        title = "AI 服务",
        subtitle = "使用你自己的 OpenAI 兼容 API，Key 只保存在本机。",
        trailing = {
            StatusPill(
                text = if (configured) "已配置" else "未配置",
                positive = configured,
            )
        },
    ) {
        OutlinedTextField(
            value = baseUrl,
            onValueChange = onBaseUrlChange,
            label = { Text("Base URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("例：https://api.openai.com/v1/ 或 https://api.deepseek.com/v1/") },
        )

        OutlinedTextField(
            value = model,
            onValueChange = onModelChange,
            label = { Text("模型名") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("例：gpt-4o-mini / deepseek-chat / moonshot-v1-8k") },
        )

        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChange,
            label = { Text("API Key") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleKeyVisible) {
                    Icon(
                        imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (keyVisible) "隐藏" else "显示",
                    )
                }
            },
        )

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("保存 AI 配置")
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
    SettingsCard(
        title = "外观",
        subtitle = "控制主题色和系统动态取色。",
    ) {
        SettingsRow(
            title = "跟随系统取色",
            body = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                "使用系统壁纸生成的颜色"
            } else {
                "Android 12 及以上可用"
            },
            trailing = {
                Switch(
                    checked = useDynamicColor,
                    onCheckedChange = onDynamicColorChange,
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                )
            },
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "主题色",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
}

@Composable
private fun AboutSection(onOpenAbout: () -> Unit) {
    SettingsCard(title = "关于") {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenAbout),
            color = Color.Transparent,
            shape = MaterialTheme.shapes.medium,
        ) {
            SettingsRow(
                title = "关于英易 · 隐私说明",
                body = "查看版本说明、BYOK 隐私边界和开源信息。",
                trailing = {
                    TextButton(onClick = onOpenAbout) {
                        Text("查看")
                    }
                },
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (trailing != null) {
                    Box(modifier = Modifier.padding(start = 12.dp)) {
                        trailing()
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    body: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        trailing()
    }
}

@Composable
private fun StatusPill(
    text: String,
    positive: Boolean,
) {
    Surface(
        color = if (positive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (positive) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
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
            .size(34.dp)
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
