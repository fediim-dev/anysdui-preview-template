package dev.fediim.anysdui.preview

import dev.fediim.anysdui.protocol.CreateSurface
import dev.fediim.anysdui.runtime.SurfaceHost
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

/**
 * A frame as the server sends it for a flow: the Figma plugin's golden flow (anysdui-kmp
 * tools/figma-plugin/test/golden/flow, frame n108_1) after the push route rewrote its image
 * reference to a root-relative `/playground/images/<key>/<sha>` path and stamped each node with the
 * first catalog that declares its component (the server's rule when the room default does not). It
 * holds the expanded composite (`n108_51` with `n108_51__img`).
 */
class FlowRenderTest {
    private val roomCatalogId = "urn:anysdui:sample-designsystem:1.0.0"

    private fun recordedFrame(): List<JsonObject> {
        val relative = "src/jvmTest/resources/recorded-frame.json"
        val file = listOf(File(relative), File("app/$relative")).first { it.exists() }
        return (Json.parseToJsonElement(file.readText()) as JsonArray).map { it.jsonObject }
    }

    @Test
    fun aFlowFrameWithAnExpandedCompositeBuildsWithBuiltInsOnly() {
        val registrations = previewRegistrations(emptyList(), emptyList())
        val host = SurfaceHost(registrations)
        val components = recordedFrame()
        assertTrue(
            components.any { it["id"].toString().contains("__") },
            "has an expanded composite",
        )

        host.apply(
            CreateSurface(
                surfaceId = "surf_playground",
                // The room catalog the server sends as the surface default; a built-ins-only client
                // does not
                // hold it, and every node carries its own catalog (not a live capture, L25).
                catalogId = roomCatalogId,
                sendDataModel = true,
                components = components,
                dataModel = buildJsonObject {},
            )
        )

        val surface = assertNotNull(host.surfaces["surf_playground"])
        assertEquals(components.size, surface.components.size)
        assertTrue(host.emittedErrors.isEmpty(), "no validation error: ${host.emittedErrors}")
    }
}
