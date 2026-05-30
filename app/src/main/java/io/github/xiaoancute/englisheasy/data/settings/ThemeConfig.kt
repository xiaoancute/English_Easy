package io.github.xiaoancute.englisheasy.data.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * 主题配置：用户自选主题色 + 是否跟随系统。
 */
data class ThemeConfig(
    val themeColor: Int,           // ARGB int
    val useDynamicColor: Boolean,  // true = 跟随系统壁纸取色
) {
    companion object {
        val DEFAULT = ThemeConfig(
            themeColor = PresetColors.BLUE.argb,
            useDynamicColor = false,
        )
    }
}

/**
 * 预设主题色（8 色）。
 */
enum class PresetColors(val label: String, val color: Color) {
    BLUE("靖蓝", Color(0xFF1E5A8E)),
    GREEN("青绿", Color(0xFF2D7A5F)),
    PURPLE("紫罗兰", Color(0xFF6B4C9A)),
    ORANGE("橙红", Color(0xFFD97706)),
    PINK("粉红", Color(0xFFDB2777)),
    TEAL("青蓝", Color(0xFF0891B2)),
    INDIGO("靛蓝", Color(0xFF4F46E5)),
    GRAY("中性灰", Color(0xFF6B7280));

    val argb: Int get() = color.toArgb()
}
