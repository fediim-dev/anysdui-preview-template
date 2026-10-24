package dev.fediim.anysdui.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.fediim.anysdui.annotations.SduiChildren
import dev.fediim.anysdui.annotations.SduiComponent
import dev.fediim.anysdui.annotations.SduiDefault
import dev.fediim.anysdui.annotations.SduiUserGesture
import dev.fediim.anysdui.designsystem.DsTheme

/** Button variants supported by the AnySDUI Design System. */
enum class DsButtonVariant {
    /** Filled button using the primary theme color. */
    Primary,

    /** Filled button using the accent theme color. */
    Secondary,

    /** Transparent button showing text and/or icon in primary color. */
    Ghost,
}

/**
 * Standard button component for the AnySDUI Design System.
 *
 * Exposes options for variants, enabled state, custom click actions, and content slot.
 *
 * @param onClick Action callback triggered when the button is clicked.
 * @param variant Visual style variant determining background and content colors.
 * @param enabled Whether the button responds to user interaction.
 * @param loading Whether the button is in a loading state, suppressing click interactions.
 * @param modifier Layout modifiers applied to the button container.
 * @param content Composable content rendered inside the button row.
 */
@SduiComponent
@Composable
fun DsButton(
    @SduiUserGesture onClick: () -> Unit,
    @SduiDefault("DsButtonVariant.Primary") variant: DsButtonVariant = DsButtonVariant.Primary,
    @SduiDefault("true") enabled: Boolean = true,
    @SduiDefault("false") loading: Boolean = false,
    modifier: Modifier = Modifier,
    @SduiChildren content: @Composable RowScope.() -> Unit,
) {
    val (containerColor, contentColor) = dsButtonColors(variant, DsTheme.colors)

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor =
                    if (variant == DsButtonVariant.Ghost) Color.Transparent else Color.LightGray,
                disabledContentColor = Color.DarkGray,
            ),
        content = content,
    )
}

/**
 * Resolves the container and content color pair for a [DsButtonVariant] given [colors].
 *
 * [Invariant] Returns container color as first, content color as second.
 */
internal fun dsButtonColors(
    variant: DsButtonVariant,
    colors: dev.fediim.anysdui.designsystem.DsColors,
): Pair<Color, Color> {
    val container =
        when (variant) {
            DsButtonVariant.Primary -> colors.primary
            DsButtonVariant.Secondary -> colors.accent
            DsButtonVariant.Ghost -> Color.Transparent
        }
    val content =
        when (variant) {
            DsButtonVariant.Primary -> colors.onPrimary
            DsButtonVariant.Secondary -> colors.onAccent
            DsButtonVariant.Ghost -> colors.primary
        }
    return container to content
}
