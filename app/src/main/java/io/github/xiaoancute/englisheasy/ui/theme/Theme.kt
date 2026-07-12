package io.github.xiaoancute.englisheasy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

/**
 * 英易主题：
 * - useDynamicColor = true 且 Android 12+：跟随系统壁纸取色
 * - 否则：用 seedColor 生成完整 M3 配色方案（含 surfaceContainer 家族）
 */
@Composable
fun EnglishEasyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = false,
    seedColor: Color = Color(0xFF1E5A8E), // 默认靖蓝
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> rememberDynamicColorScheme(
            seedColor = seedColor,
            isDark = darkTheme,
            isAmoled = false,
            // Expressive：更饱满的 container 色阶，接近 Pixel 词典/翻译的「活」质感
            style = PaletteStyle.Expressive,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = EnglishEasyShapes,
        typography = EnglishEasyTypography,
        content = content,
    )
}
