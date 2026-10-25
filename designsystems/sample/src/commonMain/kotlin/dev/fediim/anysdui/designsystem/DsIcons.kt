package dev.fediim.anysdui.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import dev.fediim.anysdui.annotations.ResType
import dev.fediim.anysdui.annotations.SduiTheme

/**
 * Design system icon tokens mapping onto A2UI IconRef tokens.
 *
 * Programmatically constructs simple vector drawings to remain lightweight and target-independent.
 */
@SduiTheme(ResType.ICON)
object DsIcons {
    /** Checkmark icon. */
    val Check: ImageVector =
        ImageVector.Builder(
                name = "Check",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
            .path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 3f,
            ) {
                moveTo(4f, 12f)
                lineTo(9f, 17f)
                lineTo(20f, 6f)
            }
            .build()

    /** Close/cancel cross icon. */
    val Close: ImageVector =
        ImageVector.Builder(
                name = "Close",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
            .path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 3f,
            ) {
                moveTo(5f, 5f)
                lineTo(19f, 19f)
                moveTo(5f, 19f)
                lineTo(19f, 5f)
            }
            .build()

    /** User profile icon. */
    val User: ImageVector =
        ImageVector.Builder(
                name = "User",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
            .path(fill = SolidColor(Color.Black)) {
                // Head circle-like square
                moveTo(12f, 4f)
                lineTo(16f, 8f)
                lineTo(12f, 12f)
                lineTo(8f, 8f)
                close()

                // Torso/shoulders
                moveTo(4f, 20f)
                lineTo(20f, 20f)
                lineTo(17f, 15f)
                lineTo(7f, 15f)
                close()
            }
            .build()

    /** Star rating/favorite icon. */
    val Star: ImageVector =
        ImageVector.Builder(
                name = "Star",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
            .path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                lineTo(15f, 9f)
                lineTo(22f, 9f)
                lineTo(17f, 14f)
                lineTo(19f, 21f)
                lineTo(12f, 17f)
                lineTo(5f, 21f)
                lineTo(7f, 14f)
                lineTo(2f, 9f)
                lineTo(9f, 9f)
                close()
            }
            .build()

    /**
     * Search magnifying glass icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Search: ImageVector = Icons.Filled.Search

    /**
     * Gear settings icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Settings: ImageVector = Icons.Filled.Settings

    /**
     * Home house icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Home: ImageVector = Icons.Filled.Home

    /**
     * Favorite heart icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Favorite: ImageVector = Icons.Filled.Favorite

    /**
     * Hamburger navigation menu icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Menu: ImageVector = Icons.Filled.Menu

    /**
     * Plus add icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Add: ImageVector = Icons.Filled.Add

    /**
     * Warning triangle icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Warning: ImageVector = Icons.Filled.Warning

    /**
     * Information circle icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Info: ImageVector = Icons.Filled.Info

    /**
     * Email envelope icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Email: ImageVector = Icons.Filled.Email

    /**
     * Share network icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Share: ImageVector = Icons.Filled.Share

    /**
     * Delete trash can icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Delete: ImageVector = Icons.Filled.Delete

    /**
     * Edit pencil icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Edit: ImageVector = Icons.Filled.Edit

    /**
     * Notifications bell icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Notifications: ImageVector = Icons.Filled.Notifications

    /**
     * Refresh circular arrows icon from Material Icons.
     *
     * [Invariant] Standard 24x24 Material vector viewport.
     */
    val Refresh: ImageVector = Icons.Filled.Refresh
}
