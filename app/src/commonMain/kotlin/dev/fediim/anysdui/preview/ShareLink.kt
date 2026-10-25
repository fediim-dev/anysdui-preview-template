package dev.fediim.anysdui.preview

private val VIEW_PATH = Regex("^/view/([A-Za-z0-9_-]{22})$")
private val EXP_PATTERN = Regex("^[1-9][0-9]{0,11}$")
private val SIG_PATTERN = Regex("^[A-Za-z0-9_-]{43}$")

/** The pairing code a user types; the same shape the server issues. */
internal val PAIRING_CODE_PATTERN = Regex("^[a-zA-Z0-9_-]{8,64}$")

/**
 * A share link the app may open: the WebSocket path of its room, without a host.
 *
 * [Invariant] [toString] shows none of it, as the signature is a bearer value. [Invariant] Built
 * only by [shareLinkOf], so the path holds a validated `viewId`, `exp` and `sig` and nothing from
 * the link's host or other query keys.
 */
class PreviewLink internal constructor(internal val socketPath: String) {
    override fun toString(): String = "PreviewLink"
}

/**
 * The room a share link [url] opens (RFC-0012 §8, §11), or null when the app must not open it.
 *
 * The link must be `http(s)` with a normalised origin (default port filled in) exactly equal to
 * [serverOrigin]'s, a path of exactly `/view/<22 base64url characters>`, and a query with exactly
 * one `exp` (1 to 12 digits, no sign, no leading zero) and one 43-character base64url `sig`; other
 * keys, such as a `pairingCode`, and any fragment are ignored. [Invariant] The result carries the
 * `/a2ui?scenario=playground&viewId=…&exp=…&sig=…` path only; the socket is opened to the
 * configured origin, never to a host the link names.
 */
fun shareLinkOf(url: String, serverOrigin: String): PreviewLink? {
    val configured = httpOriginOf(serverOrigin)?.takeIf { it.rest.isEmpty() } ?: return null
    val link = httpOriginOf(url) ?: return null
    if (link.normalized != configured.normalized) return null
    val withoutFragment = link.rest.substringBefore('#')
    val viewId =
        VIEW_PATH.matchEntire(withoutFragment.substringBefore('?'))?.groupValues?.get(1)
            ?: return null
    val query = if ('?' in withoutFragment) withoutFragment.substringAfter('?') else ""
    val pairs =
        query
            .split('&')
            .filter { it.isNotEmpty() }
            .map { it.substringBefore('=') to it.substringAfter('=', "") }
    val exp = pairs.filter { it.first == "exp" }.singleOrNull()?.second ?: return null
    val sig = pairs.filter { it.first == "sig" }.singleOrNull()?.second ?: return null
    if (!EXP_PATTERN.matches(exp) || !SIG_PATTERN.matches(sig)) return null
    return PreviewLink("/a2ui?scenario=playground&viewId=$viewId&exp=$exp&sig=$sig")
}

/** The socket path of the room [code] pairs with, or null when [code] is not a pairing code. */
internal fun pairingSocketPath(code: String): String? =
    if (PAIRING_CODE_PATTERN.matches(code)) "/a2ui?scenario=playground&pairingCode=$code" else null
