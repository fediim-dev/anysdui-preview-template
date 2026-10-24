import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PreviewPropertiesTest {
    private val valid =
        mapOf(
            "preview.serverOrigin" to "https://srv.example.com:8443",
            "preview.applicationId" to "com.example.preview",
            "preview.appleTeamId" to "ABCDE12345",
        )

    private fun parse(
        overrides: Map<String, String> = emptyMap(),
        existing: Set<String> = emptySet(),
    ) = PreviewProperties.parse({ (valid + overrides)[it] }, { it in existing })

    private fun assertRefused(property: String, overrides: Map<String, String>, secret: String) {
        val error = assertFailsWith<PreviewConfigException>(property) { parse(overrides) }
        assertTrue(property in error.message.orEmpty(), "names $property: ${error.message}")
        assertFalse(secret in error.message.orEmpty(), "never the value: ${error.message}")
    }

    @Test
    fun validPropertiesParseAndPreviewHostHasNoPort() {
        val parsed = parse()
        assertEquals("srv.example.com", parsed.previewHost)
        assertEquals("https://srv.example.com:8443", parsed.serverOrigin)
        assertEquals(
            "srv.example.com",
            parse(mapOf("preview.serverOrigin" to "https://srv.example.com")).previewHost,
        )
        assertNull(parsed.maxComponents)
        assertTrue(parsed.coordinates.isEmpty())
    }

    @Test
    fun loopbackMayUseHttpButNothingElseMay() {
        assertEquals(
            "localhost",
            parse(mapOf("preview.serverOrigin" to "http://localhost:8081")).previewHost,
        )
        assertEquals(
            "127.0.0.1",
            parse(mapOf("preview.serverOrigin" to "http://127.0.0.1:8081")).previewHost,
        )
        for (origin in
            listOf(
                "http://10.0.2.2:8081",
                "http://srv.example.com",
                "https://srv.example.com/path",
                "https://user@srv.example.com",
                "https://srv.example.com:99999",
                "https://srv.example.com:",
                "https://UPPER.example.com",
                "ftp://srv.example.com",
                "srv.example.com",
            )) {
            assertRefused("preview.serverOrigin", mapOf("preview.serverOrigin" to origin), origin)
        }
    }

    @Test
    fun applicationIdAndTeamIdAreChecked() {
        for (id in
            listOf("single", "1com.example", "com..example", "com.exa mple", "com.example-app")) {
            assertRefused("preview.applicationId", mapOf("preview.applicationId" to id), id)
        }
        for (team in listOf("abcde12345", "ABCDE1234", "ABCDE123456", "ABCDE-2345")) {
            assertRefused("preview.appleTeamId", mapOf("preview.appleTeamId" to team), team)
        }
    }

    @Test
    fun aRequiredPropertyThatIsMissingIsNamed() {
        val error =
            assertFailsWith<PreviewConfigException> {
                PreviewProperties.parse(
                    { if (it == "preview.serverOrigin") null else valid[it] },
                    { false },
                )
            }
        assertTrue("preview.serverOrigin" in error.message.orEmpty())
    }

    @Test
    fun capsMustBePositiveWholeNumbers() {
        assertEquals(5, parse(mapOf("preview.maxComponents" to "5")).maxComponents)
        assertEquals(7L, parse(mapOf("preview.maxFrameBytes" to "7")).maxFrameBytes)
        for (bad in listOf("0", "-1", "abc", "1.5")) {
            assertRefused("preview.maxComponents", mapOf("preview.maxComponents" to bad), bad)
        }
    }

    @Test
    fun designSystemListsAreChecked() {
        val files = setOf("designsystems/sample/catalog.json")
        val ok =
            mapOf(
                "designSystem.coordinates" to ":designsystems:sample, com.acme:ds:1.0.0",
                "designSystem.renderers" to
                    "dev.fediim.anysdui.designsystem.anysdui.generated.SampleDesignsystemRenderer",
                "designSystem.catalogs" to "designsystems/sample/catalog.json",
            )
        val parsed = parse(ok, files)
        assertEquals(listOf(":designsystems:sample", "com.acme:ds:1.0.0"), parsed.coordinates)

        assertRefused(
            "designSystem.coordinates",
            ok + ("designSystem.coordinates" to "not a coordinate"),
            "not a coordinate",
        )
        assertRefused("designSystem.coordinates", ok + ("designSystem.coordinates" to "a:b"), "a:b")
        assertRefused(
            "designSystem.renderers",
            ok + ("designSystem.renderers" to "1bad.Name"),
            "1bad.Name",
        )
        assertRefused(
            "designSystem.renderers",
            ok + ("designSystem.renderers" to "OnlyOne"),
            "OnlyOne",
        )
        val unequal =
            ok + ("designSystem.catalogs" to "designsystems/sample/catalog.json,other.json")
        assertRefused("designSystem.catalogs", unequal, "other.json")
        val error = assertFailsWith<PreviewConfigException> { parse(ok, emptySet()) }
        assertTrue("designSystem.catalogs" in error.message.orEmpty())
        assertFalse("catalog.json" in error.message.orEmpty())
    }
}
