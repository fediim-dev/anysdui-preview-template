package dev.fediim.anysdui.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import anysdui_kmp.sample_designsystem.generated.resources.Res
import anysdui_kmp.sample_designsystem.generated.resources.material_symbols_outlined
import dev.fediim.anysdui.annotations.ResType
import dev.fediim.anysdui.annotations.SduiComponent
import dev.fediim.anysdui.annotations.SduiDefault
import dev.fediim.anysdui.annotations.SduiExample
import dev.fediim.anysdui.annotations.SduiRes
import org.jetbrains.compose.resources.Font

/**
 * Google Material Symbols glyph drawn by ligature name from the bundled variable font.
 *
 * [Invariant] Draws within a square of [size] dp. An unspecified [tint] means the content colour.
 *
 * @param name Ligature name of the symbol, for example `shopping_cart`.
 * @param size Width and height of the square in dp.
 * @param tint Glyph colour, defaulting to the current content colour.
 * @param modifier Layout modifiers applied to the symbol.
 */
@SduiExample(
    name = "ds-symbol",
    json =
        """[
  {"version": "v1.0", "createSurface": {"surfaceId": "symbol"}},
  {"version": "v1.0", "updateComponents": {"surfaceId": "symbol", "components": [
    {"id": "root", "component": "MaterialSymbol", "name": "shopping_cart", "size": 24}
  ]}}
]""",
)
@SduiComponent
@Composable
fun MaterialSymbol(
    name: String,
    @SduiDefault("24") size: Int = 24,
    @SduiRes(ResType.COLOR)
    @SduiDefault("androidx.compose.ui.graphics.Color.Unspecified")
    tint: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    val font = Font(Res.font.material_symbols_outlined)
    val fontFamily = remember(font) { FontFamily(font) }
    val fontSizeSp = with(LocalDensity.current) { size.dp.toSp() }
    Box(modifier = modifier.size(size.dp), contentAlignment = Alignment.Center) {
        Text(
            text = name,
            fontFamily = fontFamily,
            fontSize = fontSizeSp,
            lineHeight = fontSizeSp,
            color = if (tint == Color.Unspecified) LocalContentColor.current else tint,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}
