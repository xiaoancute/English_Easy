package io.github.xiaoancute.englisheasy.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.xiaoancute.englisheasy.ui.history.FavoritesScreen
import io.github.xiaoancute.englisheasy.ui.history.HistoryScreen
import io.github.xiaoancute.englisheasy.ui.home.HomeScreen
import io.github.xiaoancute.englisheasy.ui.settings.SettingsScreen
import io.github.xiaoancute.englisheasy.ui.study.StudyScreen

/**
 * 顶层 Composable —— 查词 / 历史 / 收藏 / 学习 / 设置。
 */
@Composable
fun AppRoot() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var pendingLookup by remember { mutableStateOf<PendingLookup?>(null) }

    // 其他页点击词条 → 跳转到 Home 并触发查询
    LaunchedEffect(pendingLookup) {
        if (pendingLookup != null) {
            selectedTab = 0
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                BottomNavItem.entries.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                initialWord = pendingLookup?.word,
                markLearningOnSuccess = pendingLookup?.source == LookupSource.StudyTask,
                onWordConsumed = { pendingLookup = null },
            )
            1 -> HistoryScreen(
                onWordClick = { word ->
                    pendingLookup = PendingLookup(word, LookupSource.Normal)
                },
                modifier = Modifier.padding(innerPadding),
            )
            2 -> FavoritesScreen(
                onWordClick = { word ->
                    pendingLookup = PendingLookup(word, LookupSource.Normal)
                },
                modifier = Modifier.padding(innerPadding),
            )
            3 -> StudyScreen(
                onWordClick = { word ->
                    pendingLookup = PendingLookup(word, LookupSource.Normal)
                },
                onStudyTaskWordClick = { word ->
                    pendingLookup = PendingLookup(word, LookupSource.StudyTask)
                },
                modifier = Modifier.padding(innerPadding),
            )
            4 -> SettingsScreen(
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

private enum class BottomNavItem(val label: String, val icon: ImageVector) {
    HOME("查词", Icons.Default.Home),
    HISTORY("历史", Icons.Default.History),
    FAVORITES("收藏", Icons.Default.Star),
    STUDY("学习", Icons.Default.School),
    SETTINGS("设置", Icons.Default.Settings),
}

private data class PendingLookup(
    val word: String,
    val source: LookupSource,
)

private enum class LookupSource {
    Normal,
    StudyTask,
}
