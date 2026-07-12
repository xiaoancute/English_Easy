package io.github.xiaoancute.englisheasy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 间距与形状 token。
 * 参考：Pixel / Material You 的 8dp 节奏 + 词典类 App 的宽松阅读留白。
 */
object EnglishEasySpacing {
    val PageHorizontal = 16.dp
    val PageVertical = 12.dp
    val SectionGap = 16.dp
    val ItemGap = 10.dp
    val SurfacePadding = 16.dp
    val CardRadius = 8.dp
    val HeroRadius = 12.dp
    val PillRadius = 28.dp
}

enum class SurfaceTone { Plain, Tonal, Hero }

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    tone: SurfaceTone = SurfaceTone.Plain,
    contentPadding: Dp = EnglishEasySpacing.SurfacePadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    val container = when (tone) {
        SurfaceTone.Plain -> MaterialTheme.colorScheme.surfaceContainer
        SurfaceTone.Tonal -> MaterialTheme.colorScheme.surfaceContainerHigh
        SurfaceTone.Hero -> MaterialTheme.colorScheme.primaryContainer
    }
    val radius = if (tone == SurfaceTone.Hero) {
        EnglishEasySpacing.HeroRadius
    } else {
        EnglishEasySpacing.CardRadius
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius),
        color = container,
        tonalElevation = if (tone == SurfaceTone.Tonal) 1.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(EnglishEasySpacing.ItemGap),
            content = content,
        )
    }
}

/** 圆形 tonal 操作按钮 —— 参考 Pixel 词典/翻译的圆形控件。 */
@Composable
fun TonalIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** 列表左侧 monogram（Apple 联系人 / 词典列表常见手法）。 */
@Composable
fun LetterAvatar(
    letter: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Surface(
        modifier = modifier.size(44.dp),
        shape = CircleShape,
        color = containerColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = letter.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
        }
    }
}

/** 小写大写 section 标签。 */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = modifier,
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailing != null) {
            Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
        }
    }
}

/** 紧凑空状态，保留页面上下文并把行动放在说明旁边。 */
@Composable
fun EmptyHero(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(48.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        if (action != null) {
            Box(modifier = Modifier.padding(top = 4.dp)) {
                action()
            }
        }
    }
}

@Composable
fun StatePanel(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    SurfaceCard(modifier = modifier, tone = SurfaceTone.Tonal) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (action != null) action()
    }
}

@Composable
fun CompactInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor,
        )
    }
}

@Composable
fun accentTextButtonColors() = ButtonDefaults.textButtonColors(
    contentColor = MaterialTheme.colorScheme.primary,
)
