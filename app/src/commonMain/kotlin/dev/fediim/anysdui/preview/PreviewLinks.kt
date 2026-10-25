package dev.fediim.anysdui.preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Where a link that reaches the running app (an Android `onNewIntent`, an iOS `onOpenURL`) waits
 * for [PreviewApp] to take it.
 *
 * [Invariant] Held in memory only, until taken; never saved or logged.
 */
internal object PreviewLinkInbox {
    var waiting: String? by mutableStateOf(null)
}

/** Hands [url] to the running app; the app opens it only if [shareLinkOf] accepts it. */
fun openLink(url: String) {
    PreviewLinkInbox.waiting = url
}
