package dev.fediim.anysdui.preview

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * An ended room stays ended: after the server refuses or closes the socket, the transport makes no
 * second connection (its own reconnect loop is stopped).
 */
@OptIn(PreviewTestHook::class)
class EndedRoomTest {
    @Test
    fun afterTheServerClosesTheSocketNoSecondConnectionIsMade() {
        val attempts = AtomicInteger()
        val server = ServerSocket(0, 50, java.net.InetAddress.getByName("127.0.0.1"))
        val acceptor =
            thread(isDaemon = true) {
                while (!server.isClosed) {
                    try {
                        server.accept().use { socket ->
                            attempts.incrementAndGet()
                            socket.getInputStream().read(ByteArray(4096))
                            socket
                                .getOutputStream()
                                .write(
                                    "HTTP/1.1 400 Bad Request\r\nConnection: close\r\nContent-Length: 0\r\n\r\n"
                                        .toByteArray()
                                )
                        }
                    } catch (e: java.io.IOException) {
                        // The server socket was closed, or a client left early.
                    }
                }
            }
        val probe = RoomProbe()
        val scene =
            ImageComposeScene(width = 480, height = 860, density = Density(1f)) {
                RoomScreen(
                    socketPath = assertNotNull(pairingSocketPath("abcd-1234_EF")),
                    onLeave = {},
                    probe = probe,
                    serverOrigin = "http://127.0.0.1:${server.localPort}",
                )
            }
        try {
            val start = System.nanoTime()

            fun renderFor(ms: Long, until: () -> Boolean = { false }) {
                val from = System.nanoTime()
                while ((System.nanoTime() - from) / 1_000_000 < ms && !until()) {
                    Thread.sleep(50)
                    scene.render(System.nanoTime() - start)
                }
            }
            renderFor(10_000) { probe.ended != null }
            assertNotNull(probe.ended, "the room ended")
            val atEnd = attempts.get()
            assertTrue(atEnd >= 1)
            // The transport's first reconnect would come after one second.
            renderFor(3_500)
            assertEquals(atEnd, attempts.get(), "no connection after the room ended")
        } finally {
            scene.close()
            server.close()
            acceptor.join(2_000)
        }
    }
}
