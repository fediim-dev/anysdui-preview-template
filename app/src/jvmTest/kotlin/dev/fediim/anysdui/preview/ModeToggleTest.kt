package dev.fediim.anysdui.preview

import dev.fediim.anysdui.protocol.Action
import dev.fediim.anysdui.runtime.WebSocketConnectionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ModeToggleTest {
    private fun navigate(
        context: kotlinx.serialization.json.JsonObject,
        name: String = "anysdui.navigate",
    ) =
        Action(
            name = name,
            surfaceId = "surf_playground",
            sourceComponentId = "x",
            timestamp = "2026-01-01T00:00:00Z",
            context = context,
        )

    @Test
    fun theActionIsNavigateWithTheMode() {
        for (mode in listOf("follow", "explore")) {
            val action = navigateModeAction(mode)
            assertEquals("anysdui.navigate", action.name)
            assertEquals(JsonPrimitive(mode), action.context["mode"])
            assertEquals(setOf("mode"), action.context.keys)
        }
    }

    @Test
    fun followAfterConnected() {
        assertEquals("follow", modeAfter(WebSocketConnectionState.Connected, "explore"))
        assertEquals("explore", modeAfter(WebSocketConnectionState.Connecting, "explore"))
        assertEquals("explore", modeAfter(WebSocketConnectionState.Disconnected, "explore"))
    }

    @Test
    fun exploreOnASentTargetFrameId() {
        val toFrame = navigate(buildJsonObject { put("targetFrameId", "n100_1") })
        assertEquals("explore", modeAfterSent(toFrame, "follow"))
        assertEquals("follow", modeAfterSent(navigateModeAction("follow"), "follow"))
        assertEquals(
            "follow",
            modeAfterSent(
                navigate(buildJsonObject { put("targetFrameId", "x") }, "other"),
                "follow",
            ),
        )
    }
}
