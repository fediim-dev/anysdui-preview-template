package dev.fediim.anysdui.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import dev.fediim.anysdui.annotations.ResType
import dev.fediim.anysdui.annotations.SduiChildren
import dev.fediim.anysdui.annotations.SduiComponent
import dev.fediim.anysdui.annotations.SduiDefault
import dev.fediim.anysdui.annotations.SduiExample
import dev.fediim.anysdui.annotations.SduiRes
import dev.fediim.anysdui.designsystem.DsTheme
import dev.fediim.anysdui.runtime.LocalBoxScope
import dev.fediim.anysdui.runtime.LocalColumnScope
import dev.fediim.anysdui.runtime.LocalRowScope

/**
 * Standard card component for the AnySDUI Design System.
 *
 * Provides a bounded content container styled with the surface theme tokens.
 *
 * [Invariant] An omitted or unresolvable `containerColor` resolves to `DsTheme.colors.surface` and
 * an omitted or unresolvable `contentColor` to `DsTheme.colors.onSurface`, so a payload that sets
 * neither renders exactly as it did before either parameter existed.
 *
 * [Anti-Pattern] Do not colour a card with a `background` modifier op — the card's own surface is
 * drawn over it, leaving colour visible only in the corners its shape cuts away.
 *
 * @param modifier Layout modifiers applied to the card container.
 * @param containerColor Background color of the card container, defaulting to theme surface color.
 * @param contentColor Preferred content and text color within the card, defaulting to theme
 *   onSurface color.
 * @param content Composable children displayed in a vertical column inside the card.
 */
@SduiExample(
    name = "ds-profile",
    json =
        """[
  {"version": "v1.0", "createSurface": {"surfaceId": "profile"}},
  {"version": "v1.0", "updateComponents": {"surfaceId": "profile", "components": [
    {"id": "root", "component": "DsCard", "content": ["profile_column"]},
    {"id": "profile_column", "component": "DsColumn", "spacing": 12, "modifier": [{"type": "padding", "all": 16}], "content": ["profile_header", "profile_bio", "profile_name_field", "profile_actions"]},
    {"id": "profile_header", "component": "DsRow", "spacing": 8, "verticalAlignment": "centerVertically", "content": ["profile_icon", "profile_title"]},
    {"id": "profile_icon", "component": "DsIcon", "icon": {"type": "icon", "value": "user"}, "tint": {"type": "color", "value": "primary"}},
    {"id": "profile_title", "component": "DsText", "text": {"@path": "/profile/name"}, "style": {"type": "typography", "value": "title"}},
    {"id": "profile_bio", "component": "DsText", "text": {"@path": "/profile/bio"}, "style": {"type": "typography", "value": "body"}, "color": {"type": "color", "value": "onSurface"}},
    {"id": "profile_name_field", "component": "DsTextField", "label": "Display name", "value": {"@path": "/profile/draftName"}, "placeholder": "Your name"},
    {"id": "profile_actions", "component": "DsRow", "spacing": 8, "horizontalArrangement": "end", "modifier": [{"type": "fillMaxWidth"}], "content": ["discard_button", "save_button"]},
    {"id": "discard_button", "component": "DsButton", "variant": "ghost", "onClick": {"event": {"name": "discard_profile"}}, "content": ["discard_label"]},
    {"id": "discard_label", "component": "DsText", "text": "Discard", "style": {"type": "typography", "value": "button"}},
    {"id": "save_button", "component": "DsButton", "variant": "primary", "onClick": {"event": {"name": "save_profile", "context": {"name": {"@path": "/profile/draftName"}}}}, "content": ["save_label"]},
    {"id": "save_label", "component": "DsText", "text": "Save", "style": {"type": "typography", "value": "button"}}
  ]}},
  {"version": "v1.0", "updateDataModel": {"surfaceId": "profile", "path": "/profile", "value": {"name": "Ada Lovelace", "draftName": "Ada Lovelace", "bio": "Writes about engines and numbers."}}}
]""",
)
@SduiComponent
@Composable
fun DsCard(
    modifier: Modifier = Modifier,
    @SduiRes(ResType.COLOR)
    @SduiDefault("DsTheme.colors.surface")
    containerColor: Color = DsTheme.colors.surface,
    @SduiRes(ResType.COLOR)
    @SduiDefault("DsTheme.colors.onSurface")
    contentColor: Color = DsTheme.colors.onSurface,
    @SduiChildren content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor = containerColor.takeOrElse { DsTheme.colors.surface },
                contentColor = contentColor.takeOrElse { DsTheme.colors.onSurface },
            ),
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
