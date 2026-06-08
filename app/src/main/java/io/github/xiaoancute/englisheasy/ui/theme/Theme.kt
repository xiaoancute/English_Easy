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
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF20384C),
            onPrimaryContainer = Color(0xFFD8E8F5),
            secondary = Color(0xFFB8C3CB),
            onSecondary = Color(0xFF24313A),
            secondaryContainer = Color(0xFF2A3035),
            onSecondaryContainer = Color(0xFFE2E8ED),
            tertiary = Color(0xFFAFC9B9),
            onTertiary = Color(0xFF21362B),
            tertiaryContainer = Color(0xFF27352D),
            onTertiaryContainer = Color(0xFFDCEBE1),
            background = Color(0xFF111318),
            onBackground = Color(0xFFE4E7EA),
            surface = Color(0xFF181A1D),
            onSurface = Color(0xFFE4E7EA),
            surfaceVariant = Color(0xFF22262A),
            onSurfaceVariant = Color(0xFFBEC6CD),
            outline = Color(0xFF7D8790),
            outlineVariant = Color(0xFF343A40),
        )
        else -> lightColorScheme(
            primary = seedColor,
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE7F0F7),
            onPrimaryContainer = Color(0xFF173A56),
            secondary = Color(0xFF536471),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE8EDF0),
            onSecondaryContainer = Color(0xFF24323A),
            tertiary = Color(0xFF4E6F5A),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFE7F0EA),
            onTertiaryContainer = Color(0xFF223729),
            background = Color(0xFFFAFAF9),
            onBackground = Color(0xFF1F2328),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1F2328),
            surfaceVariant = Color(0xFFF3F5F6),
            onSurfaceVariant = Color(0xFF4F5660),
            outline = Color(0xFF7E868F),
            outlineVariant = Color(0xFFE0E4E7),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EnglishEasyTypography,
        content = content,
    )
}
