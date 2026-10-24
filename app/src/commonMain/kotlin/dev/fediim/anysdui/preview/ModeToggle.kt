package dev.fediim.anysdui.preview

import dev.fediim.anysdui.protocol.A2uiMessage
import dev.fediim.anysdui.protocol.Action
import dev.fediim.anysdui.runtime.WebSocketConnectionState
import dev.fediim.anysdui.runtime.getCurrentIsoTimestamp
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * The standard A2UI `action` that switches a client between `"follow"` and `"explore"` (RFC-0012
 * §5). The server consumes it; no component sends it, so the source id is a fixed marker.
 */
internal fun navigateModeAction(mode: String): Action =
    Action(
        name = "anysdui.navigate",
        surfaceId = "surf_playground",
        sourceComponentId = "anysdui.preview.mode",
        timestamp = getCurrentIsoTimestamp(),
        context = buildJsonObject { put("mode", mode) },
    )

/**
 * The toggle's mode after [state]: `"follow"` once Connected, because the server starts every new
 * connection in Follow, including an automatic reconnect; [mode] otherwise.
 */
internal fun modeAfter(state: WebSocketConnectionState, mode: String): String =
    if (state is WebSocketConnectionState.Connected) "follow" else mode

/**
 * The toggle's mode after the app sent [message]: `"explore"` when it is a navigate action to a
 * frame (a tapped frame link), [mode] otherwise.
 */
internal fun modeAfterSent(message: A2uiMessage, mode: String): String =
    if (
        message is Action &&
            message.name == "anysdui.navigate" &&
            "targetFrameId" in message.context
    )
        "explore"
    else mode
