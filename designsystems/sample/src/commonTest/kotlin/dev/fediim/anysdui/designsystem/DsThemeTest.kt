package dev.fediim.anysdui.designsystem

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers

class DsThemeTest {

    private class UnitApplier : Applier<Unit> {
        override val current: Unit = Unit

        override fun down(node: Unit) {}

        override fun up() {}

        override fun insertTopDown(index: Int, instance: Unit) {}

        override fun insertBottomUp(index: Int, instance: Unit) {}

        override fun remove(index: Int, count: Int) {}

        override fun move(from: Int, to: Int, count: Int) {}

        override fun clear() {}

        override fun onEndChanges() {}
    }

    /**
     * Runs the initial composition of [block] against a headless [UnitApplier] and disposes it.
     *
     * [Invariant] Synchronous and coroutine-free: `setContent` performs the initial composition on
     * the calling thread, which is all these token-resolution assertions observe. [Anti-Pattern] Do
     * not launch `Recomposer.runRecomposeAndApplyChanges` here — it requires a
     * `MonotonicFrameClock` in context and serves only re-composition, which no test here triggers.
     */
    private fun composeOnce(block: @Composable () -> Unit) {
        val recomposer = Recomposer(Dispatchers.Unconfined)
        val composition =
            Composition(
                applier = UnitApplier(),
                parent = recomposer,
            )
        try {
            composition.setContent(block)
        } finally {
            composition.dispose()
            recomposer.cancel()
        }
    }

    @Test
    fun testDefaultFallbackTokensWithoutExplicitProvider() {
        var observedColors: DsColors? = null
        var observedTypography: DsTypography? = null

        composeOnce {
            observedColors = DsTheme.colors
            observedTypography = DsTheme.typography
        }

        assertNotNull(observedColors, "DsTheme.colors should safely resolve default colors")
        val typography =
            assertNotNull(
                observedTypography,
                "DsTheme.typography should safely resolve default typography",
            )
        assertEquals(24.sp, typography.title.fontSize)
    }

    @Test
    fun testExplicitProviderOverridesDefaults() {
        var observedTitleSize: androidx.compose.ui.unit.TextUnit? = null

        composeOnce {
            DsTheme(dark = true) {
                observedTitleSize = DsTheme.typography.title.fontSize
            }
        }

        assertEquals(24.sp, observedTitleSize)
    }

    /**
     * Resolves the palette an explicit [DsTheme] mode selects, through the public accessors only.
     */
    private fun paletteFor(dark: Boolean): DsColors {
        var observed: DsColors? = null
        composeOnce {
            DsTheme(dark = dark) { observed = DsTheme.colors }
        }
        return assertNotNull(observed, "DsTheme(dark = $dark) should resolve a palette")
    }

    @Test
    fun testDarkModeResolvesTheDarkPalette() {
        val dark = paletteFor(dark = true)

        assertEquals(Color(0xFF141218), dark.surface, "dark mode must resolve DarkColors.surface")
        assertEquals(
            Color(0xFFE6E1E5),
            dark.onSurface,
            "dark mode must resolve DarkColors.onSurface",
        )
        assertEquals(Color(0xFFD0BCFF), dark.primary, "dark mode must resolve DarkColors.primary")
        assertEquals(Color(0xFF492532), dark.onAccent, "dark mode must resolve DarkColors.onAccent")
    }

    @Test
    fun testLightModeResolvesTheLightPalette() {
        val light = paletteFor(dark = false)

        assertEquals(
            Color(0xFFFEF7FF),
            light.surface,
            "light mode must resolve LightColors.surface",
        )
        assertEquals(
            Color(0xFF1D1B20),
            light.onSurface,
            "light mode must resolve LightColors.onSurface",
        )
        assertEquals(
            Color(0xFF6750A4),
            light.primary,
            "light mode must resolve LightColors.primary",
        )
        assertEquals(
            Color(0xFFFFFFFF),
            light.onAccent,
            "light mode must resolve LightColors.onAccent",
        )
    }

    @Test
    fun testResolvedModeIsPublishedToComposables() {
        var observedDark: Boolean? = null
        var observedLight: Boolean? = null
        var observedOutsideProvider: Boolean? = null

        composeOnce {
            DsTheme(dark = true) { observedDark = DsTheme.isDark }
            DsTheme(dark = false) { observedLight = DsTheme.isDark }
            observedOutsideProvider = DsTheme.isDark
        }

        assertEquals(true, observedDark, "DsTheme.isDark must report the resolved dark mode")
        assertEquals(false, observedLight, "DsTheme.isDark must report the resolved light mode")
        assertEquals(false, observedOutsideProvider, "DsTheme.isDark must fall back to light")
    }

    /**
     * WCAG 2.1 relative luminance of an sRGB colour.
     *
     * [Invariant] Input channels are the sRGB 0..1 values Compose stores; the gamma expansion below
     * is the spec's, not an approximation.
     */
    private fun relativeLuminance(color: Color): Double {
        fun channel(v: Float): Double {
            val c = v.toDouble()
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(color.red) +
            0.7152 * channel(color.green) +
            0.0722 * channel(color.blue)
    }

    /** WCAG 2.1 contrast ratio between two opaque colours, in the range 1.0..21.0. */
    private fun contrastRatio(foreground: Color, background: Color): Double {
        val a = relativeLuminance(foreground)
        val b = relativeLuminance(background)
        val lighter = maxOf(a, b)
        val darker = minOf(a, b)
        return (lighter + 0.05) / (darker + 0.05)
    }

    /**
     * Asserts the AA floor for body text on every `on*`/background pair in one palette.
     *
     * [Invariant] "Pair" means each `onX` token against the `X` it names, plus the two tokens that
     * are only ever drawn on `surface` (`accent`, `error`).
     */
    private fun assertPaletteMeetsAa(palette: DsColors, label: String) {
        val pairs =
            listOf(
                "onSurface/surface" to contrastRatio(palette.onSurface, palette.surface),
                "onPrimary/primary" to contrastRatio(palette.onPrimary, palette.primary),
                "onAccent/accent" to contrastRatio(palette.onAccent, palette.accent),
                "accent/surface" to contrastRatio(palette.accent, palette.surface),
                "error/surface" to contrastRatio(palette.error, palette.surface),
            )
        pairs.forEach { (name, ratio) ->
            assertTrue(
                ratio >= AA_BODY_TEXT_RATIO,
                "$label $name is ${ratio.asRatio()}:1, below the WCAG AA body-text floor of $AA_BODY_TEXT_RATIO:1",
            )
        }
    }

    @Test
    fun testLightPaletteMeetsAaContrast() {
        assertPaletteMeetsAa(paletteFor(dark = false), "light")
    }

    @Test
    fun testDarkPaletteMeetsAaContrast() {
        assertPaletteMeetsAa(paletteFor(dark = true), "dark")
    }

    @Test
    fun testComponentDeclaredColorPairingsMeetAaContrast() {
        listOf(false to "light", true to "dark").forEach { (dark, modeLabel) ->
            val palette = paletteFor(dark = dark)

            // DsButton Primary: onPrimary on primary
            val (primaryContainer, primaryContent) =
                dev.fediim.anysdui.designsystem.components.dsButtonColors(
                    dev.fediim.anysdui.designsystem.components.DsButtonVariant.Primary,
                    palette,
                )
            val primaryRatio = contrastRatio(primaryContent, primaryContainer)
            assertTrue(
                primaryRatio >= AA_BODY_TEXT_RATIO,
                "$modeLabel DsButton Primary contrast is ${primaryRatio.asRatio()}:1, below $AA_BODY_TEXT_RATIO:1",
            )

            // DsButton Secondary: onAccent on accent
            val (secondaryContainer, secondaryContent) =
                dev.fediim.anysdui.designsystem.components.dsButtonColors(
                    dev.fediim.anysdui.designsystem.components.DsButtonVariant.Secondary,
                    palette,
                )
            val secondaryRatio = contrastRatio(secondaryContent, secondaryContainer)
            assertTrue(
                secondaryRatio >= AA_BODY_TEXT_RATIO,
                "$modeLabel DsButton Secondary contrast is ${secondaryRatio.asRatio()}:1, below $AA_BODY_TEXT_RATIO:1",
            )

            // DsButton Ghost: primary on surface (since container is Transparent)
            val (_, ghostContent) =
                dev.fediim.anysdui.designsystem.components.dsButtonColors(
                    dev.fediim.anysdui.designsystem.components.DsButtonVariant.Ghost,
                    palette,
                )
            val ghostRatio = contrastRatio(ghostContent, palette.surface)
            assertTrue(
                ghostRatio >= AA_BODY_TEXT_RATIO,
                "$modeLabel DsButton Ghost contrast on surface is ${ghostRatio.asRatio()}:1, below $AA_BODY_TEXT_RATIO:1",
            )

            // DsCard default: onSurface on surface
            val cardRatio = contrastRatio(palette.onSurface, palette.surface)
            assertTrue(
                cardRatio >= AA_BODY_TEXT_RATIO,
                "$modeLabel DsCard default contrast is ${cardRatio.asRatio()}:1, below $AA_BODY_TEXT_RATIO:1",
            )
        }
    }

    private companion object {
        /**
         * WCAG 2.1 AA minimum contrast ratio for body text. No pair in either palette is exempt.
         */
        const val AA_BODY_TEXT_RATIO = 4.5

        fun Double.asRatio(): String = ((this * 100).roundToInt() / 100.0).toString()
    }
}
