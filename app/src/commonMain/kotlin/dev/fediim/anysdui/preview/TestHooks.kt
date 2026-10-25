package dev.fediim.anysdui.preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.fediim.anysdui.runtime.SurfaceHost
import dev.fediim.anysdui.runtime.WebSocketConnectionState

/**
 * Marks what only tests may use: the room screen's state, readable from another module. Opting in
 * is an error-level requirement, so app code cannot use it by accident.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Test hook: not part of the app")
@Retention(AnnotationRetention.BINARY)
annotation class PreviewTestHook

/**
 * What the room screen is doing, for a test to read: the socket's state, the text shown once the
 * room has ended (null while it has not), and the ids of the nodes of the room's surface.
 */
@PreviewTestHook
class RoomProbe {
    internal var connection: WebSocketConnectionState by
        mutableStateOf(WebSocketConnectionState.Disconnected)

    /** Whether the socket is connected (the `helloAck` handshake succeeded). */
    val connected: Boolean
        get() = connection == WebSocketConnectionState.Connected

    var ended: String? by mutableStateOf(null)
        internal set

    internal var host: SurfaceHost? = null

    /** The ids of the components of the room's surface, empty before the first surface. */
    fun nodeIds(): Set<String> =
        host?.surfaces?.get("surf_playground")?.components?.keys?.toSet().orEmpty()
}
