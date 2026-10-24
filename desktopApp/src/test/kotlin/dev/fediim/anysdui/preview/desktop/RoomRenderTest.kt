package dev.fediim.anysdui.preview.desktop

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import dev.fediim.anysdui.preview.PreviewApp
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.EncodedImageFormat
import org.junit.Assume.assumeTrue

/**
 * T12: renders the room for a pushed flow off screen, after connecting to a demo server with a
 * share link, and writes a PNG. It runs only when `PREVIEW_DEMO_LINK` holds a share link of the
 * server this build is configured for (`preview.serverOrigin`); otherwise it is skipped.
 *
 * `PREVIEW_DEMO_PNG` is the output path (default `build/desktop-room.png`) and
 * `PREVIEW_DEMO_WAIT_MS` the time allowed for the connection and the images (default 10000). The
 * link is read from the environment only and never printed.
 */
class RoomRenderTest {
    @Test
    fun theRoomOfAPushedFlowRendersOffScreen() {
        val link = System.getenv("PREVIEW_DEMO_LINK")
        assumeTrue("PREVIEW_DEMO_LINK is not set", !link.isNullOrBlank())
        val output = File(System.getenv("PREVIEW_DEMO_PNG") ?: "build/desktop-room.png")
        val waitMs = System.getenv("PREVIEW_DEMO_WAIT_MS")?.toLongOrNull() ?: 10_000L

        val scene =
            ImageComposeScene(width = 480, height = 860, density = Density(2f)) {
                PreviewApp(initialLink = link)
            }
        try {
            val start = System.nanoTime()
            var image = scene.render(0)
            while ((System.nanoTime() - start) / 1_000_000 < waitMs) {
                Thread.sleep(100)
                image = scene.render(System.nanoTime() - start)
            }
            output.absoluteFile.parentFile.mkdirs()
            val png = image.encodeToData(EncodedImageFormat.PNG)!!.bytes
            output.writeBytes(png)

            val bitmap = Bitmap.makeFromImage(image)
            val colors = HashSet<Int>()
            for (x in 0 until bitmap.width step 7) for (y in 0 until bitmap.height step 7) {
                colors.add(bitmap.getColor(x, y))
            }
            assertTrue(
                colors.size > 8,
                "the room drew more than a blank page (${colors.size} colors)",
            )
        } finally {
            scene.close()
        }
    }
}
