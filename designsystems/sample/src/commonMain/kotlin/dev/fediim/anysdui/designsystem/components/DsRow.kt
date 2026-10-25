package dev.fediim.anysdui.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.fediim.anysdui.annotations.SduiChildren
import dev.fediim.anysdui.annotations.SduiComponent
import dev.fediim.anysdui.annotations.SduiDefault
import dev.fediim.anysdui.annotations.SduiHorizontalArrangement
import dev.fediim.anysdui.annotations.SduiVerticalAlignment
import dev.fediim.anysdui.runtime.LocalBoxScope
import dev.fediim.anysdui.runtime.LocalColumnScope
import dev.fediim.anysdui.runtime.LocalRowScope

private fun SduiVerticalAlignment.toVerticalAlignment(): Alignment.Vertical =
    when (this) {
        SduiVerticalAlignment.Top -> Alignment.Top
        SduiVerticalAlignment.CenterVertically -> Alignment.CenterVertically
        SduiVerticalAlignment.Bottom -> Alignment.Bottom
    }

private fun resolveHorizontalArrangement(
    arrangement: SduiHorizontalArrangement,
    spacing: Int,
): Arrangement.Horizontal {
    return if (spacing > 0) {
        val alignment =
            when (arrangement) {
                SduiHorizontalArrangement.Start -> Alignment.Start
                SduiHorizontalArrangement.Center -> Alignment.CenterHorizontally
                SduiHorizontalArrangement.End -> Alignment.End
                else -> Alignment.Start
            }
        Arrangement.spacedBy(spacing.dp, alignment)
    } else {
        when (arrangement) {
            SduiHorizontalArrangement.Start -> Arrangement.Start
            SduiHorizontalArrangement.Center -> Arrangement.Center
            SduiHorizontalArrangement.End -> Arrangement.End
            SduiHorizontalArrangement.SpaceBetween -> Arrangement.SpaceBetween
            SduiHorizontalArrangement.SpaceAround -> Arrangement.SpaceAround
            SduiHorizontalArrangement.SpaceEvenly -> Arrangement.SpaceEvenly
        }
    }
}

/**
 * Standard horizontal layout row for the AnySDUI Design System.
 *
 * Positions children horizontally and applies spacing between them.
 *
 * @param spacing Spacing in dp between adjacent children.
 * @param horizontalArrangement Horizontal arrangement of children.
 * @param verticalAlignment Vertical alignment of children within the row.
 * @param modifier Layout and styling modifiers to apply to the row.
 * @param content Composable slot for the row's children.
 */
@SduiComponent
@Composable
fun DsRow(
    @SduiDefault("8") spacing: Int = 8,
    @SduiDefault("SduiHorizontalArrangement.Start")
    horizontalArrangement: SduiHorizontalArrangement = SduiHorizontalArrangement.Start,
    @SduiDefault("SduiVerticalAlignment.Top")
    verticalAlignment: SduiVerticalAlignment = SduiVerticalAlignment.Top,
    modifier: Modifier = Modifier,
    @SduiChildren content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = resolveHorizontalArrangement(horizontalArrangement, spacing),
        verticalAlignment = verticalAlignment.toVerticalAlignment(),
        content = {
            CompositionLocalProvider(
                LocalRowScope provides this,
                LocalColumnScope provides null,
                LocalBoxScope provides null,
            ) {
                content()
            }
        },
    )
}
