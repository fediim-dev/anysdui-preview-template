package dev.fediim.anysdui.preview.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.fediim.anysdui.preview.PreviewApp

/**
 * The desktop entry. Desktop has no https link association, so a share link is the first launch
 * argument or is pasted into the join screen; it is never logged.
 */
fun main(args: Array<String>) = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AnySDUI Preview",
        state = rememberWindowState(width = 480.dp, height = 860.dp),
    ) {
        PreviewApp(initialLink = args.firstOrNull())
    }
}
