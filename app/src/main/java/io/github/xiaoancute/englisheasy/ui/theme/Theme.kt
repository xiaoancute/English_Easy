package io.github.xiaoancute.englisheasy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * 英易主题：支持用户自选主题色 + 跟随系统动态取色。
 * - useDynamicColor = true：Android 12+ 跟随壁纸取色
 * - useDynamicColor = false：使用 seedColor 生成配色方案
 */

@Composable
fun EnglishEasyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = false,
    seedColor: Color = Color(0xFF1E5A8E),  // 默认靖蓝
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = seedColor,
            background = Color(0xFF111318),
            surface = Color(0xFF181A1D),
            surfaceVariant = Color(0xFF22262A),
            outlineVariant = Color(0xFF343A40),
        )
        else -> lightColorScheme(
            primary = seedColor,
            background = Color(0xFFFAFAF9),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF3F5F6),
            outlineVariant = Color(0xFFE0E4E7),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EnglishEasyTypography,
        content = content,
    )
}
