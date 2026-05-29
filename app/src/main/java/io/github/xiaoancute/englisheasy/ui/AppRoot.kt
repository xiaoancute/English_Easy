package io.github.xiaoancute.englisheasy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.xiaoancute.englisheasy.ui.home.HomeScreen
import io.github.xiaoancute.englisheasy.ui.settings.SettingsScreen

/**
 * 顶层 Composable —— 在 Home 和 Settings 之间切换。
 * MVP 阶段两屏切换，不引入 Navigation Compose 依赖。
 */
@Composable
fun AppRoot() {
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
    } else {
        HomeScreen(onOpenSettings = { showSettings = true })
    }
}
