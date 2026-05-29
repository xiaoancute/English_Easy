package io.github.xiaoancute.englisheasy.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.xiaoancute.englisheasy.ui.compare.CompareScreen
import io.github.xiaoancute.englisheasy.ui.history.FavoritesScreen
import io.github.xiaoancute.englisheasy.ui.history.HistoryScreen
import io.github.xiaoancute.englisheasy.ui.home.HomeScreen
import io.github.xiaoancute.englisheasy.ui.settings.SettingsScreen
import io.github.xiaoancute.englisheasy.ui.stats.StatsScreen

/**
 * 顶层 Composable —— 查词 / 历史 / 收藏 / 统计 / 对比 + 设置入口。
 */
@Composable
fun AppRoot() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var pendingWordFromHistory by remember { mutableStateOf<String?>(null) }

    // 历史页点击词条 → 跳转到 Home 并触发查询
    if (pendingWordFromHistory != null) {
        selectedTab = 0
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavItem.entries.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                onOpenSettings = { selectedTab = 5 },
                modifier = Modifier.padding(innerPadding),
                initialWord = pendingWordFromHistory,
                onWordConsumed = { pendingWordFromHistory = null },
            )
            1 -> HistoryScreen(
                onWordClick = { word ->
                    pendingWordFromHistory = word
                },
                modifier = Modifier.padding(innerPadding),
            )
            2 -> FavoritesScreen(
                onWordClick = { word ->
                    pendingWordFromHistory = word
                },
                modifier = Modifier.padding(innerPadding),
            )
            3 -> StatsScreen(
                modifier = Modifier.padding(innerPadding),
            )
            4 -> CompareScreen(
                modifier = Modifier.padding(innerPadding),
            )
            5 -> SettingsScreen(
                onBack = { selectedTab = 0 },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

private enum class BottomNavItem(val label: String, val icon: ImageVector) {
    HOME("查词", Icons.Default.Home),
    HISTORY("历史", Icons.Default.History),
    FAVORITES("收藏", Icons.Default.Star),
    STATS("统计", Icons.Default.BarChart),
    COMPARE("对比", Icons.Default.CompareArrows),
}
