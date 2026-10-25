package dev.fediim.anysdui.designsystem

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.graphics.Color
import dev.fediim.anysdui.designsystem.anysdui.generated.SampleDesignsystemRenderer
import dev.fediim.anysdui.runtime.ResourceResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers

/**
 * Proves the generated renderer resolves this catalog's colour tokens (issue #221).
 *
 * Before #221 no production `ResourceResolver` existed at all, so every token in the catalog's
 * `ColorRef` enum resolved to nothing and a `background` modifier op naming one rendered unstyled.
 */
class ResourceResolutionTest {

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

    /** [Invariant] Synchronous and coroutine-free, matching the harness in [DsThemeTest]. */
    private fun composeOnce(block: @Composable () -> Unit) {
        val recomposer = Recomposer(Dispatchers.Unconfined)
        val composition = Composition(applier = UnitApplier(), parent = recomposer)
        try {
            composition.setContent(block)
        } finally {
            composition.dispose()
            recomposer.cancel()
        }
    }

    /**
     * The seven tokens the catalog's `ColorRef` enum declares, paired with their theme accessor.
     */
    private val declaredTokens =
        listOf("accent", "error", "onAccent", "onPrimary", "onSurface", "primary", "surface")

    private fun expected(token: String, colors: DsColors): Color =
        when (token) {
            "accent" -> colors.accent
            "error" -> colors.error
            "onAccent" -> colors.onAccent
            "onPrimary" -> colors.onPrimary
            "onSurface" -> colors.onSurface
            "primary" -> colors.primary
            "surface" -> colors.surface
            else -> error("unlisted token '$token' — keep this test in step with the catalog enum")
        }

    @Test
    fun testTheGeneratedRendererIsAResourceResolver() {
        assertIs<ResourceResolver>(
            SampleDesignsystemRenderer,
            "a catalog declaring colour tokens must be able to resolve them",
        )
    }

    @Test
    fun testEveryDeclaredColourTokenResolvesToItsThemeValue() {
        val resolved = mutableMapOf<String, Color>()
        var themeColors: DsColors? = null

        composeOnce {
            DsTheme(dark = false) {
                themeColors = DsTheme.colors
                declaredTokens.forEach { token ->
                    resolved[token] =
                        (SampleDesignsystemRenderer as ResourceResolver).resolveColor(token)
                }
            }
        }

        val colors = themeColors
        assertTrue(colors != null, "theme did not compose")
        declaredTokens.forEach { token ->
            assertEquals(
                expected(token, colors),
                resolved[token],
                "token '$token' must resolve to its theme value",
            )
            assertTrue(
                resolved[token] != Color.Unspecified,
                "token '$token' must not resolve to Unspecified",
            )
        }
    }

    @Test
    fun testTokensTrackTheActiveThemeRatherThanACapturedValue() {
        var lightPrimary: Color? = null
        var darkPrimary: Color? = null
        var lightThemePrimary: Color? = null
        var darkThemePrimary: Color? = null

        composeOnce {
            DsTheme(dark = false) {
                lightThemePrimary = DsTheme.colors.primary
                lightPrimary =
                    (SampleDesignsystemRenderer as ResourceResolver).resolveColor("primary")
            }
        }
        composeOnce {
            DsTheme(dark = true) {
                darkThemePrimary = DsTheme.colors.primary
                darkPrimary =
                    (SampleDesignsystemRenderer as ResourceResolver).resolveColor("primary")
            }
        }

        assertEquals(lightThemePrimary, lightPrimary)
        assertEquals(darkThemePrimary, darkPrimary)
        assertTrue(
            lightPrimary != darkPrimary,
            "the light and dark palettes differ, so a resolver reading the active theme must differ too",
        )
    }

    @Test
    fun testUnknownTokenDegradesToUnspecified() {
        var resolved: Color? = null
        composeOnce {
            DsTheme(dark = false) {
                resolved =
                    (SampleDesignsystemRenderer as ResourceResolver).resolveColor("noSuchToken")
            }
        }
        assertEquals(Color.Unspecified, resolved, "an unknown token must degrade, never throw")
    }

    @Test
    fun testHexLiteralsStillResolveThroughTheSamePath() {
        var resolved: Color? = null
        composeOnce {
            DsTheme(dark = false) {
                resolved =
                    (SampleDesignsystemRenderer as ResourceResolver).resolveColor("#FF6750A4")
            }
        }
        assertEquals(Color(0xFF6750A4), resolved)
    }
}
