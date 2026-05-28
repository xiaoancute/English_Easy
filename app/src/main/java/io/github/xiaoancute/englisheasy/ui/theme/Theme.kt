package io.github.xiaoancute.englisheasy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.os.Build

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E3A5F),
    onPrimary = Color.White,
    secondary = Color(0xFF4A6B8A),
    background = Color(0xFFFCFCFE),
    surface = Color(0xFFFCFCFE),
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C5E8),
    onPrimary = Color(0xFF003258),
    secondary = Color(0xFFB8C8DC),
    background = Color(0xFF101418),
    surface = Color(0xFF101418),
    onBackground = Color(0xFFE2E2E5),
    onSurface = Color(0xFFE2E2E5),
)

@Composable
fun EnglishEasyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
