package dev.fediim.anysdui.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.fediim.anysdui.annotations.ResType
import dev.fediim.anysdui.annotations.SduiComponent
import dev.fediim.anysdui.annotations.SduiDefault
import dev.fediim.anysdui.annotations.SduiRes
import dev.fediim.anysdui.designsystem.DsTheme

/**
 * Standard icon container for the AnySDUI Design System.
 *
 * Renders an [ImageVector] at the specified dp size and applies the semantic tint.
 *
 * @param icon Vector graphic icon asset to render from the design system icon tokens.
 * @param tint Color tint applied to the icon vector, defaulting to theme onSurface color.
 * @param size Dimensions in dp for width and height of the icon.
 * @param modifier Layout modifiers applied to the icon.
 */
@SduiComponent
@Composable
fun DsIcon(
    @SduiRes(ResType.ICON) icon: ImageVector,
    @SduiRes(ResType.COLOR)
    @SduiDefault("DsTheme.colors.onSurface")
    tint: Color = DsTheme.colors.onSurface,
    @SduiDefault("24") size: Int = 24,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size.dp),
    )
}
