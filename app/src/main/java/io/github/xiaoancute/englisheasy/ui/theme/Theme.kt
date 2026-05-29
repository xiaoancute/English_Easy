package io.github.xiaoancute.englisheasy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 温润纸感主题：深墨绿主色 + 暖米白背景 + 赭石强调。
 * 不使用 dynamicColor（系统取色会盖掉品牌色），light/dark 成对手工调校。
 */

private val LightColors = lightColorScheme(
    primary = Color(0xFF2D4A3E),            // 深墨绿
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCDE8D8),
    onPrimaryContainer = Color(0xFF0A2018),
    secondary = Color(0xFFB8702D),          // 赭石橙（强调）
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF5DFC8),
    onSecondaryContainer = Color(0xFF3A1F08),
    tertiary = Color(0xFF6B7A52),           // 橄榄（收藏星等点缀）
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F1E8),          // 暖米白
    onBackground = Color(0xFF2A2620),        // 暖黑
    surface = Color(0xFFFDFBF6),            // 卡片底（略暖白）
    onSurface = Color(0xFF2A2620),
    surfaceVariant = Color(0xFFEAE4D6),      // 容器/section 暖灰
    onSurfaceVariant = Color(0xFF4E4A40),
    outline = Color(0xFF9C9685),
    outlineVariant = Color(0xFFD8D2C4),
    error = Color(0xFF9B2C2C),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF6DAD6),
    onErrorContainer = Color(0xFF410E0B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA0CFB5),            // 浅墨绿
    onPrimary = Color(0xFF0F2A1E),
    primaryContainer = Color(0xFF1F4435),
    onPrimaryContainer = Color(0xFFBCEBCF),
    secondary = Color(0xFFE0A86B),          // 暖橙
    onSecondary = Color(0xFF3A1F08),
    secondaryContainer = Color(0xFF5A3D1E),
    onSecondaryContainer = Color(0xFFF5DFC8),
    tertiary = Color(0xFFBFC79E),
    onTertiary = Color(0xFF2A3015),
    background = Color(0xFF1A1814),          // 暖墨
    onBackground = Color(0xFFE8E2D5),
    surface = Color(0xFF221F1A),
    onSurface = Color(0xFFE8E2D5),
    surfaceVariant = Color(0xFF3A352D),
    onSurfaceVariant = Color(0xFFCFC8B8),
    outline = Color(0xFF8C8676),
    outlineVariant = Color(0xFF4A453C),
    error = Color(0xFFE59A9A),
    onError = Color(0xFF5A1A1A),
    errorContainer = Color(0xFF7A2A2A),
    onErrorContainer = Color(0xFFF6DAD6),
)

@Composable
fun EnglishEasyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = EnglishEasyTypography,
        content = content,
    )
}
