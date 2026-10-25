package dev.fediim.anysdui.preview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ShareLinkTest {
    private val origin = "https://srv.example.com"
    private val viewId = "abcdefghijklmnopqrstuv"
    private val sig = "A".repeat(43)
    private val query = "exp=1893456000&sig=$sig"

    private fun link(url: String, server: String = origin) = shareLinkOf(url, server)

    @Test
    fun aValidLinkIsAcceptedAndOnlyTheSocketPathSurvives() {
        val result = assertNotNull(link("https://srv.example.com/view/$viewId?$query"))
        assertEquals(
            "/a2ui?scenario=playground&viewId=$viewId&exp=1893456000&sig=$sig",
            result.socketPath,
        )
    }

    @Test
    fun anotherOriginIsRefused() {
        for (url in
            listOf(
                "https://other.example.com/view/$viewId?$query",
                "https://srv.example.com.evil.com/view/$viewId?$query",
                "https://srv.example.com@evil.com/view/$viewId?$query",
                "https://evil.com@srv.example.com/view/$viewId?$query",
                "https://srv.example.com:8443/view/$viewId?$query",
                "http://srv.example.com/view/$viewId?$query",
                "https://srv.example.com%2eevil.com/view/$viewId?$query",
                "https://srv.example.com\\@evil.com/view/$viewId?$query",
                "https://srv.example.comé/view/$viewId?$query",
                "myapp://srv.example.com/view/$viewId?$query",
                "javascript:alert(1)",
                "/view/$viewId?$query",
                "",
            )) {
            assertNull(link(url), url)
        }
    }

    @Test
    fun httpIsAcceptedOnlyForTheConfiguredLoopbackOrigin() {
        val loopback = "http://127.0.0.1:8081"
        assertNotNull(link("http://127.0.0.1:8081/view/$viewId?$query", loopback))
        assertNull(link("http://127.0.0.1:8082/view/$viewId?$query", loopback))
        assertNull(link("http://10.0.2.2:8081/view/$viewId?$query", loopback))
        assertNull(link("https://127.0.0.1:8081/view/$viewId?$query", loopback))
    }

    @Test
    fun anExplicitDefaultPortEqualsTheConfiguredOrigin() {
        assertNotNull(link("https://srv.example.com:443/view/$viewId?$query"))
        assertNotNull(link("https://SRV.example.com/view/$viewId?$query"))
        assertNotNull(
            link("https://srv.example.com/view/$viewId?$query", "https://srv.example.com:443")
        )
    }

    @Test
    fun aBadPathOrQueryIsRefused() {
        val base = "https://srv.example.com"
        for (url in
            listOf(
                "$base/view/short?$query",
                "$base/view/$viewId/extra?$query",
                "$base/view/$viewId/?$query",
                "$base/other/$viewId?$query",
                "$base/view/$viewId",
                "$base/view/$viewId?exp=1893456000",
                "$base/view/$viewId?sig=$sig",
                "$base/view/$viewId?exp=0123&sig=$sig",
                "$base/view/$viewId?exp=-5&sig=$sig",
                "$base/view/$viewId?exp=1893456000&sig=${"A".repeat(42)}",
                "$base/view/$viewId?exp=1893456000&sig=${"A".repeat(43)}!",
                "$base/view/$viewId?exp=1&exp=2&sig=$sig",
                "$base/view/$viewId?exp=1&sig=$sig&sig=$sig",
            )) {
            assertNull(link(url), url)
        }
    }

    @Test
    fun theResultNeverCarriesTheLinksHostOrAPairingCode() {
        val result =
            assertNotNull(
                link("https://srv.example.com/view/$viewId?$query&pairingCode=SECRETCODE#frag")
            )
        assertFalse("srv.example.com" in result.socketPath)
        assertFalse("SECRETCODE" in result.socketPath)
        assertFalse("pairingCode" in result.socketPath)
        assertFalse("frag" in result.socketPath)
    }

    @Test
    fun toStringHidesTheSignature() {
        val result = assertNotNull(link("https://srv.example.com/view/$viewId?$query"))
        assertEquals("PreviewLink", result.toString())
        assertFalse(sig in result.toString())
    }

    @Test
    fun aMalformedServerOriginAcceptsNothing() {
        assertNull(
            link("https://srv.example.com/view/$viewId?$query", "https://srv.example.com/path")
        )
        assertNull(link("https://srv.example.com/view/$viewId?$query", "not an origin"))
    }

    @Test
    fun anInvalidPairingCodeOpensNoSocket() {
        assertNull(pairingSocketPath(""))
        assertNull(pairingSocketPath("short"))
        assertNull(pairingSocketPath("has space and more"))
        assertNull(pairingSocketPath("a".repeat(65)))
        assertNull(pairingSocketPath("code&scenario=other1"))
        assertEquals(
            "/a2ui?scenario=playground&pairingCode=abcd-1234_EF",
            pairingSocketPath("abcd-1234_EF"),
        )
    }

    @Test
    fun aRefusedCodeJoinNamesTheReasonAndOnlyALinkSaysItIsNoLongerValid() {
        val linkPath = "/a2ui?scenario=playground&viewId=$viewId&exp=1893456000&sig=$sig"
        val codePath = assertNotNull(pairingSocketPath("abcd-1234_EF"))
        assertEquals("This link is no longer valid", roomEndedText(linkPath, "Link not valid"))
        assertEquals(
            "Could not join the room: Catalog ID mismatch",
            roomEndedText(codePath, "Catalog ID mismatch"),
        )
        assertEquals("The room closed the connection.", roomEndedText(codePath, null))
    }
}
