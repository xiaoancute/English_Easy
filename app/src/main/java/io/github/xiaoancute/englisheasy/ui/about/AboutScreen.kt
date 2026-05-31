package io.github.xiaoancute.englisheasy.ui.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.xiaoancute.englisheasy.BuildConfig
import io.github.xiaoancute.englisheasy.data.prompt.CURRENT_PROMPT_VERSION

private const val GITHUB_URL = "https://github.com/xiaoancute/English_Easy"
private const val INSPIRATION_VIDEO_URL = "https://www.bilibili.com/video/BV1s6oLB8ESh/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("关于") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 标题
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "英易 English Easy",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "版本 ${BuildConfig.VERSION_NAME}（概念引擎 v$CURRENT_PROMPT_VERSION）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = "把英文词在母语者大脑里的样子，重新呈现给你 —— 不是翻译，是概念还原。",
                style = MaterialTheme.typography.bodyMedium,
            )

            InfoCard(title = "版本信息") {
                BulletText("App ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                BulletText("概念引擎 v$CURRENT_PROMPT_VERSION")
                BulletText("License: MIT")
            }

            InfoCard(title = "隐私说明") {
                BulletText("你的 API Key 用 Android Keystore 加密后只存在本机，不会上传任何服务器。")
                BulletText("查词请求直接发往你自己配置的 LLM 端点，本应用没有任何中间服务器。")
                BulletText("历史、收藏、笔记都只保存在本机数据库，卸载即清除。")
            }

            InfoCard(title = "开源") {
                LinkText(text = "GitHub：$GITHUB_URL") { uriHandler.openUri(GITHUB_URL) }
                BulletText("基于 MIT 协议开源，欢迎提 Issue 与 PR。")
            }

            InfoCard(title = "致谢") {
                BulletText("理念受 B 站 up 主「苹果香蕉--」的语言学习视频启发。")
                LinkText(text = "观看启发视频") { uriHandler.openUri(INSPIRATION_VIDEO_URL) }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            content()
        }
    }
}

@Composable
private fun BulletText(text: String) {
    Text(
        text = "· $text",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
    )
}

@Composable
private fun LinkText(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
    )
}
