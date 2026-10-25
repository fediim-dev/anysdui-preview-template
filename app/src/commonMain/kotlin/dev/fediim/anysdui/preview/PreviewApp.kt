package dev.fediim.anysdui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.fediim.anysdui.compose.basic.ImageSourcePolicy
import dev.fediim.anysdui.compose.basic.LocalImageSourcePolicy
import dev.fediim.anysdui.runtime.SduiSurface
import dev.fediim.anysdui.runtime.SurfaceHost
import dev.fediim.anysdui.runtime.SurfaceHostLimits
import dev.fediim.anysdui.runtime.WebSocketConnectionState
import dev.fediim.anysdui.runtime.WebSocketTransport

private const val LINK_REFUSED = "This link does not open here."
private const val CODE_REFUSED = "That is not a pairing code."

/**
 * The preview app: a join screen (pairing code or a pasted share link), then the room.
 *
 * [initialLink] is a share link the platform launched the app with, or null; a link that arrives
 * later goes through [openLink]. A link opens only if [shareLinkOf] accepts it for the configured
 * origin (RFC-0012 §11).
 *
 * [Invariant] The pairing code and the link live in composition state only: never saved across
 * process death or rotation, never stored, never logged. [Invariant] The socket always opens to the
 * configured origin.
 */
@Composable
fun PreviewApp(initialLink: String?) {
    var socketPath by remember {
        mutableStateOf(
            initialLink?.let { shareLinkOf(it, PreviewConfig.SERVER_ORIGIN)?.socketPath }
        )
    }
    var notice by remember {
        mutableStateOf(if (initialLink != null && socketPath == null) LINK_REFUSED else null)
    }
    val waiting = PreviewLinkInbox.waiting
    LaunchedEffect(waiting) {
        if (waiting != null) {
            PreviewLinkInbox.waiting = null
            val link = shareLinkOf(waiting, PreviewConfig.SERVER_ORIGIN)
            socketPath = link?.socketPath
            notice = if (link == null) LINK_REFUSED else null
        }
    }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                val path = socketPath
                if (path == null) {
                    JoinScreen(
                        notice = notice,
                        onCode = { code ->
                            socketPath = pairingSocketPath(code.trim())
                            notice = if (socketPath == null) CODE_REFUSED else null
                        },
                        onLink = { text ->
                            socketPath =
                                shareLinkOf(text.trim(), PreviewConfig.SERVER_ORIGIN)?.socketPath
                            notice = if (socketPath == null) LINK_REFUSED else null
                        },
                    )
                } else {
                    RoomScreen(path, onLeave = { socketPath = null })
                }
            }
        }
    }
}

@Composable
private fun JoinScreen(notice: String?, onCode: (String) -> Unit, onLink: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Join a preview room", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Pairing code") },
            singleLine = true,
        )
        Button(onClick = { onCode(code) }) { Text("Join") }
        Text("or")
        OutlinedTextField(
            value = link,
            onValueChange = { link = it },
            label = { Text("Share link") },
            singleLine = true,
        )
        Button(onClick = { onLink(link) }) { Text("Open link") }
        if (notice != null) Text(notice, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun RoomScreen(socketPath: String, onLeave: () -> Unit) {
    var ended by remember(socketPath) { mutableStateOf<String?>(null) }
    var connection by
        remember(socketPath) {
            mutableStateOf<WebSocketConnectionState>(WebSocketConnectionState.Disconnected)
        }
    var mode by remember(socketPath) { mutableStateOf("follow") }
    val registrations = remember { previewRegistrations() }
    val transport =
        remember(socketPath) {
            var started = false
            previewTransport(
                socketPath = socketPath,
                catalogIds = registrations.map { it.catalogId },
                onMessageSent = { message -> mode = modeAfterSent(message, mode) },
                onStateChanged = { state ->
                    connection = state
                    mode = modeAfter(state, mode)
                    when (state) {
                        is WebSocketConnectionState.Failed ->
                            ended = roomEndedText(socketPath, state.error)
                        WebSocketConnectionState.Disconnected ->
                            if (started) ended = roomEndedText(socketPath, null)
                        else -> started = true
                    }
                },
            )
        }
    DisposableEffect(transport) { onDispose { transport.close() } }
    val host =
        remember(transport) {
            SurfaceHost(
                registrations = registrations,
                transport = transport,
                allowFallbackToFirstComponentAsRoot = true,
                limits = SurfaceHostLimits(maxComponentsPerSurface = PreviewConfig.MAX_COMPONENTS),
            )
        }
    LaunchedEffect(host, transport) {
        transport.incoming.collect { message -> host.apply(message) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onLeave) { Text("Leave") }
            if (ended == null) {
                for ((id, label) in listOf("follow" to "Follow", "explore" to "Explore")) {
                    FilterChip(
                        selected = mode == id,
                        onClick = {
                            mode = id
                            transport.send(navigateModeAction(id))
                        },
                        label = { Text(label) },
                        modifier = Modifier.semantics { contentDescription = "$label mode" },
                    )
                }
                Text(
                    when (connection) {
                        WebSocketConnectionState.Connected -> "Connected"
                        is WebSocketConnectionState.Failed -> "Connection failed"
                        else -> "Connecting"
                    },
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            contentAlignment = if (ended != null) Alignment.Center else Alignment.TopCenter,
        ) {
            val endedText = ended
            if (endedText != null) {
                Text(endedText)
            } else {
                Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    CompositionLocalProvider(LocalImageSourcePolicy provides imageSourcePolicy()) {
                        SduiSurface(host = host)
                    }
                }
            }
        }
    }
}

/**
 * What the room screen says once its socket is refused or closed. A share-link socket says the link
 * is no longer valid, as the server's own viewer does; a pairing-code socket names the server's
 * reason, such as a catalog mismatch, so a wrong build is not mistaken for an expired link.
 */
internal fun roomEndedText(socketPath: String, error: String?): String =
    when {
        "viewId=" in socketPath -> "This link is no longer valid"
        error.isNullOrBlank() -> "The room closed the connection."
        else -> "Could not join the room: $error"
    }

/**
 * Images load from the server origin only, resolving the server's root-relative image paths against
 * it; loopback over `http` only when the configured origin is loopback (development).
 */
private fun imageSourcePolicy(): ImageSourcePolicy {
    val origin = httpOriginOf(PreviewConfig.SERVER_ORIGIN)
    val loopback = origin?.host == "localhost" || origin?.host == "127.0.0.1"
    return ImageSourcePolicy(
        allowedOrigins = setOf(PreviewConfig.SERVER_ORIGIN),
        allowInsecureLoopback = loopback,
        baseOrigin = PreviewConfig.SERVER_ORIGIN,
    )
}

/** The socket to the configured origin for [socketPath], with the configured caps. */
internal fun previewTransport(
    socketPath: String,
    catalogIds: List<String>,
    onMessageSent: (dev.fediim.anysdui.protocol.A2uiMessage) -> Unit,
    onStateChanged: (WebSocketConnectionState) -> Unit,
): WebSocketTransport {
    val origin = checkNotNull(httpOriginOf(PreviewConfig.SERVER_ORIGIN)) { "serverOrigin" }
    return WebSocketTransport(
        host = origin.host,
        port = origin.port,
        path = socketPath,
        supportedCatalogIds = catalogIds,
        isSecure = origin.scheme == "https",
        maxFrameSizeBytes =
            PreviewConfig.MAX_FRAME_BYTES ?: WebSocketTransport.DEFAULT_MAX_FRAME_SIZE_BYTES,
        maxPayloadSizeBytes =
            PreviewConfig.MAX_PAYLOAD_BYTES ?: WebSocketTransport.DEFAULT_MAX_PAYLOAD_SIZE_BYTES,
        onMessageSent = onMessageSent,
        onStateChanged = onStateChanged,
    )
}
