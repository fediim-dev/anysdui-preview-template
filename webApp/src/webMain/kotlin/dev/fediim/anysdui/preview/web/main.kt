package dev.fediim.anysdui.preview.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.fediim.anysdui.preview.PreviewApp
import kotlinx.browser.document
import kotlinx.browser.window

/**
 * The web entry, served from the server's own origin (the image route sends no CORS). A page at a
 * `/view/…` path is a share link and goes to [PreviewApp]; any other URL shows the join screen.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val spinner = document.getElementById("app-loading-spinner")
    spinner?.parentElement?.removeChild(spinner)

    val location = window.location
    val link = location.href.takeIf { location.pathname.startsWith("/view/") }
    ComposeViewport { PreviewApp(initialLink = link) }
}
