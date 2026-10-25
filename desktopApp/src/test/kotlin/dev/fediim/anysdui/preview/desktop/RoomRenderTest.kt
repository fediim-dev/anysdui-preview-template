package dev.fediim.anysdui.preview.desktop

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import dev.fediim.anysdui.preview.PreviewApp
import dev.fediim.anysdui.preview.PreviewTestHook
import dev.fediim.anysdui.preview.RoomProbe
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.jetbrains.skia.EncodedImageFormat
import org.junit.Assume.assumeTrue

/**
 * T12: renders the room for a pushed flow off screen, after connecting to a demo server with a
 * share link, and writes a PNG. It runs only when `PREVIEW_DEMO_LINK` holds a share link of the
 * server this build is configured for (`preview.serverOrigin`); otherwise it is skipped.
 *
 * It passes only when the room is connected, has not ended, and holds a node of the pushed flow
 * (`PREVIEW_DEMO_NODE`, default `root`). `PREVIEW_DEMO_PNG` is the output path (default
 * `build/desktop-room.png`) and `PREVIEW_DEMO_WAIT_MS` the time allowed for the connection and the
 * images (default 10000). The link is read from the environment only and never printed.
 */
@OptIn(PreviewTestHook::class)
class RoomRenderTest {
    @Test
    fun theRoomOfAPushedFlowRendersOffScreen() {
        val link = System.getenv("PREVIEW_DEMO_LINK")
        assumeTrue("PREVIEW_DEMO_LINK is not set", !link.isNullOrBlank())
        val output = File(System.getenv("PREVIEW_DEMO_PNG") ?: "build/desktop-room.png")
        val waitMs = System.getenv("PREVIEW_DEMO_WAIT_MS")?.toLongOrNull() ?: 10_000L
        val node = System.getenv("PREVIEW_DEMO_NODE") ?: "root"

        val probe = RoomProbe()
        val scene =
            ImageComposeScene(width = 480, height = 860, density = Density(2f)) {
                PreviewApp(initialLink = link, probe = probe)
            }
        try {
            val start = System.nanoTime()
            var image = scene.render(0)

            fun renderFor(ms: Long, until: () -> Boolean = { false }) {
                val from = System.nanoTime()
                while ((System.nanoTime() - from) / 1_000_000 < ms && !until()) {
                    Thread.sleep(100)
                    image = scene.render(System.nanoTime() - start)
                }
            }
            renderFor(waitMs) { probe.ended != null || node in probe.nodeIds() }
            // Let the images load before the picture is taken.
            if (probe.ended == null) renderFor(waitMs.coerceAtMost(3_000))

            output.absoluteFile.parentFile.mkdirs()
            output.writeBytes(image.encodeToData(EncodedImageFormat.PNG)!!.bytes)

            assertNull(probe.ended, "the room ended: ${probe.ended}")
            assertTrue(probe.connected, "the room is not connected")
            assertTrue(node in probe.nodeIds(), "the room's surface has no node '$node'")
        } finally {
            scene.close()
        }
    }
}
