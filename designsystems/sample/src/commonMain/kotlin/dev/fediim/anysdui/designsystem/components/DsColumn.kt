package dev.fediim.anysdui.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.fediim.anysdui.annotations.SduiChildren
import dev.fediim.anysdui.annotations.SduiComponent
import dev.fediim.anysdui.annotations.SduiDefault
import dev.fediim.anysdui.annotations.SduiHorizontalAlignment
import dev.fediim.anysdui.annotations.SduiVerticalArrangement
import dev.fediim.anysdui.runtime.LocalBoxScope
import dev.fediim.anysdui.runtime.LocalColumnScope
import dev.fediim.anysdui.runtime.LocalRowScope

private fun SduiHorizontalAlignment.toHorizontalAlignment(): Alignment.Horizontal =
    when (this) {
        SduiHorizontalAlignment.Start -> Alignment.Start
        SduiHorizontalAlignment.CenterHorizontally -> Alignment.CenterHorizontally
        SduiHorizontalAlignment.End -> Alignment.End
    }

private fun resolveVerticalArrangement(
    arrangement: SduiVerticalArrangement,
    spacing: Int,
): Arrangement.Vertical {
    return if (spacing > 0) {
        val alignment =
            when (arrangement) {
                SduiVerticalArrangement.Top -> Alignment.Top
                SduiVerticalArrangement.Center -> Alignment.CenterVertically
                SduiVerticalArrangement.Bottom -> Alignment.Bottom
                else -> Alignment.Top
            }
        Arrangement.spacedBy(spacing.dp, alignment)
    } else {
        when (arrangement) {
            SduiVerticalArrangement.Top -> Arrangement.Top
            SduiVerticalArrangement.Center -> Arrangement.Center
            SduiVerticalArrangement.Bottom -> Arrangement.Bottom
            SduiVerticalArrangement.SpaceBetween -> Arrangement.SpaceBetween
            SduiVerticalArrangement.SpaceAround -> Arrangement.SpaceAround
            SduiVerticalArrangement.SpaceEvenly -> Arrangement.SpaceEvenly
        }
    }
}

/**
 * Standard vertical layout column for the AnySDUI Design System.
 *
 * Positions children vertically and applies spacing between them.
 *
 * @param spacing Space in dp between adjacent children in the column.
 * @param verticalArrangement Vertical arrangement strategy for positioning children.
 * @param horizontalAlignment Horizontal alignment of children within the column cross-axis.
 * @param modifier Layout modifiers applied to the column.
 * @param content Composable children displayed sequentially from top to bottom.
 */
@SduiComponent
@Composable
fun DsColumn(
    @SduiDefault("8") spacing: Int = 8,
    @SduiDefault("SduiVerticalArrangement.Top")
    verticalArrangement: SduiVerticalArrangement = SduiVerticalArrangement.Top,
    @SduiDefault("SduiHorizontalAlignment.Start")
    horizontalAlignment: SduiHorizontalAlignment = SduiHorizontalAlignment.Start,
    modifier: Modifier = Modifier,
    @SduiChildren content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = resolveVerticalArrangement(verticalArrangement, spacing),
        horizontalAlignment = horizontalAlignment.toHorizontalAlignment(),
        content = {
            CompositionLocalProvider(
                LocalColumnScope provides this,
                LocalRowScope provides null,
                LocalBoxScope provides null,
            ) {
                content()
            }
        },
    )
}
