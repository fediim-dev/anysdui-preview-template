package dev.fediim.anysdui.preview

/**
 * An `http(s)` URL's origin, normalised, and what follows its authority.
 *
 * @property scheme `http` or `https`, lower case.
 * @property host Lower case; letters, digits, `.` and `-` only.
 * @property port Explicit, or the scheme's default (443, 80).
 * @property rest The path, query and fragment as written, or `""`.
 */
internal class HttpOrigin(
    val scheme: String,
    val host: String,
    val port: Int,
    val rest: String,
) {
    /** `scheme://host:port`, default port filled in: two URLs of one origin have the same text. */
    val normalized: String
        get() = "$scheme://$host:$port"
}

private val HOST_PATTERN = Regex("^[a-z0-9]([a-z0-9.-]*[a-z0-9])?$")

/**
 * Parses [url] as `http://` or `https://` plus an authority, or returns null.
 *
 * [Invariant] Refuses, rather than normalises: anything outside printable ASCII, a backslash, a
 * userinfo (`@`), a `%` in the authority, an IPv6 literal, an empty or out-of-range port, an empty
 * host, a host with `..` or an edge dot or dash. A host that this check cannot read is not an
 * origin.
 */
internal fun httpOriginOf(url: String): HttpOrigin? {
    if (url.any { it.code !in 0x21..0x7E || it == '\\' }) return null
    val scheme =
        when {
            url.startsWith("https://", ignoreCase = true) -> "https"
            url.startsWith("http://", ignoreCase = true) -> "http"
            else -> return null
        }
    val afterScheme = url.substring(scheme.length + 3)
    val authorityEnd = afterScheme.indexOfFirst { it == '/' || it == '?' || it == '#' }
    val authority = if (authorityEnd < 0) afterScheme else afterScheme.substring(0, authorityEnd)
    val rest = if (authorityEnd < 0) "" else afterScheme.substring(authorityEnd)
    if (authority.isEmpty() || '@' in authority || '%' in authority || '[' in authority) return null
    if (authority.count { it == ':' } > 1) return null
    val host = authority.substringBefore(':').lowercase()
    if (!HOST_PATTERN.matches(host) || ".." in host) return null
    val portText = if (':' in authority) authority.substringAfter(':') else null
    val port =
        when {
            portText == null -> if (scheme == "https") 443 else 80
            portText.isEmpty() || portText.length > 5 || !portText.all { it in '0'..'9' } ->
                return null
            else -> portText.toInt().takeIf { it in 1..65535 } ?: return null
        }
    return HttpOrigin(scheme, host, port, rest)
}
