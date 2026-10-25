package dev.fediim.anysdui.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import dev.fediim.anysdui.annotations.ResType
import dev.fediim.anysdui.annotations.SduiComponent
import dev.fediim.anysdui.annotations.SduiDefault
import dev.fediim.anysdui.annotations.SduiRes
import dev.fediim.anysdui.designsystem.DsTheme

/**
 * Standard text component for the AnySDUI Design System.
 *
 * Renders read-only text styled using the system's typography and color tokens.
 *
 * @param text The text content to display.
 * @param style Typography style token to apply.
 * @param color Color token to render the text with.
 * @param modifier Layout and styling modifiers to apply to the text.
 */
@SduiComponent
@Composable
fun DsText(
    text: String,
    @SduiRes(ResType.TYPOGRAPHY)
    @SduiDefault("DsTheme.typography.body")
    style: TextStyle = DsTheme.typography.body,
    @SduiRes(ResType.COLOR)
    @SduiDefault("DsTheme.colors.onSurface")
    color: Color = DsTheme.colors.onSurface,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier,
    )
}
