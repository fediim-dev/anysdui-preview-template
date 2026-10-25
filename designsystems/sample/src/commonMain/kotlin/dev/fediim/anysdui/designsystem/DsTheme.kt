package dev.fediim.anysdui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.fediim.anysdui.annotations.ResType
import dev.fediim.anysdui.annotations.SduiCatalog
import dev.fediim.anysdui.annotations.SduiTheme

/**
 * Design system color tokens.
 *
 * Exposes a semantic color palette mapping onto A2UI ColorRef tokens.
 */
@Immutable
@SduiTheme(ResType.COLOR)
class DsColors(
    val primary: Color,
    val onPrimary: Color,
    val surface: Color,
    val onSurface: Color,
    val accent: Color,
    val onAccent: Color,
    val error: Color,
)

/**
 * Design system typography tokens.
 *
 * Exposes semantic text styles mapping onto A2UI TypographyRef tokens.
 */
@Immutable
@SduiTheme(ResType.TYPOGRAPHY)
class DsTypography(
    val title: TextStyle,
    val body: TextStyle,
    val caption: TextStyle,
    val button: TextStyle,
)

// Tasteful light color palette
private val LightColors =
    DsColors(
        primary = Color(0xFF6750A4),
        onPrimary = Color(0xFFFFFFFF),
        surface = Color(0xFFFEF7FF),
        onSurface = Color(0xFF1D1B20),
        accent = Color(0xFF7D5260),
        onAccent = Color(0xFFFFFFFF),
        error = Color(0xFFB3261E),
    )

// Tasteful dark color palette
private val DarkColors =
    DsColors(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        surface = Color(0xFF141218),
        onSurface = Color(0xFFE6E1E5),
        accent = Color(0xFFEFB8C8),
        onAccent = Color(0xFF492532),
        error = Color(0xFFF2B8B5),
    )

private val Typography =
    DsTypography(
        title =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
            ),
        body =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
        caption =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Light,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            ),
        button =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
    )

/**
 * CompositionLocal providing active [DsColors] design tokens.
 *
 * Defaults to [LightColors] when evaluated outside an explicit [DsTheme] provider. [Invariant]
 * Always resolves a non-null [DsColors] instance; never throws. [Recipe] Providing custom colors
 *
 * ```kotlin
 * CompositionLocalProvider(LocalDsColors provides customColors) { content() }
 * ```
 */
val LocalDsColors =
    staticCompositionLocalOf<DsColors> {
        LightColors
    }

/**
 * CompositionLocal providing active [DsTypography] semantic text style tokens.
 *
 * Defaults to [Typography] when evaluated outside an explicit [DsTheme] provider. [Invariant]
 * Always resolves a non-null [DsTypography] instance; never throws. [Recipe] Providing custom
 * typography
 *
 * ```kotlin
 * CompositionLocalProvider(LocalDsTypography provides customTypography) { content() }
 * ```
 */
val LocalDsTypography =
    staticCompositionLocalOf<DsTypography> {
        Typography
    }

/**
 * CompositionLocal reporting whether the enclosing [DsTheme] resolved its dark palette.
 *
 * [Invariant] A read-only *output* of the mode [DsTheme] resolved — providing it directly does not
 * change which palette [LocalDsColors] carries, so the two would disagree. [Invariant] Defaults to
 * `false` outside a [DsTheme] provider, matching [LocalDsColors]' light default. [Recipe] Picking a
 * per-mode asset without re-reading the platform
 *
 * ```kotlin
 * val logo = if (DsTheme.isDark) darkLogo else lightLogo
 * ```
 */
val LocalDsDarkMode =
    staticCompositionLocalOf<Boolean> {
        false
    }

/**
 * Entry point to retrieve theme tokens in @Composable functions.
 *
 * [Recipe] Accessing theme properties
 *
 * ```kotlin
 * val primaryColor = DsTheme.colors.primary
 * val titleStyle = DsTheme.typography.title
 * ```
 */
@SduiCatalog(
    catalogId = "urn:anysdui:sample-designsystem:1.0.0",
    title = "Sample Design System",
    description = "A standard Compose Multiplatform design system catalog for Server-Driven UI.",
)
object DsTheme {
    val colors: DsColors
        @Composable get() = LocalDsColors.current

    val typography: DsTypography
        @Composable get() = LocalDsTypography.current

    /** Whether the enclosing [DsTheme] resolved its dark palette; `false` outside one. */
    val isDark: Boolean
        @Composable get() = LocalDsDarkMode.current
}

/**
 * Composable wrapper setting up the AnySDUI Design System theme.
 *
 * Configures the semantic color and typography tokens in the CompositionLocal context, and
 * publishes the resolved mode on [LocalDsDarkMode].
 *
 * @param dark selects the dark palette; follows the platform setting unless a caller overrides it.
 *   [Invariant] The default reads the host OS setting, so a bare `DsTheme { }` follows the device.
 *   Pass the parameter explicitly to pin a palette — a preview, a gallery toggle, or a side-by-side
 *   comparison all need that, which is why the platform read is a default and not the body.
 *   [Anti-Pattern] Do not rely on the default in a headless composition (a bare `Recomposer` with
 *   no platform host). The skiko targets resolve the setting through `LocalSystemTheme`, whose
 *   default loads a native library that a host normally provides ahead of it; without that host the
 *   read throws `ExceptionInInitializerError`. Headless callers pass `dark` explicitly — which also
 *   makes the assertion independent of whatever mode the machine running it happens to be in.
 *   [Recipe] Pinning a palette regardless of the device
 *
 * ```kotlin
 * DsTheme(dark = true) { content() }
 * ```
 */
@Composable
fun DsTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (dark) DarkColors else LightColors
    CompositionLocalProvider(
        LocalDsColors provides colors,
        LocalDsTypography provides Typography,
        LocalDsDarkMode provides dark,
        content = content,
    )
}
