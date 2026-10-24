import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Reads the checked-in source files, never build output (RFC-0012 §8, §11). */
class AssociationFilesTest {
    // Gradle runs buildSrc tests from buildSrc/; an IDE may run them from the repository root.
    private fun source(path: String): File =
        listOf(File("../$path"), File(path)).first { it.exists() }

    private fun androidFiles(dir: File): List<File> =
        dir.walkTopDown().filter { it.isFile && it.extension in setOf("xml", "kt") }.toList()

    @Test
    fun mainManifestHasOneAutoVerifiedHttpsFilterOnTheViewPath() {
        val manifest = source("androidApp/src/main/AndroidManifest.xml").readText()
        val filters =
            Regex("<intent-filter[^>]*android:autoVerify=\"true\"[\\s\\S]*?</intent-filter>")
                .findAll(manifest)
                .toList()
        assertEquals(1, filters.size, "one autoVerify filter")
        val filter = filters.single().value
        val schemes =
            Regex("android:scheme=\"([^\"]*)\"").findAll(filter).map { it.groupValues[1] }.toList()
        assertEquals(listOf("https"), schemes)
        assertEquals(
            listOf("\${previewHost}"),
            Regex("android:host=\"([^\"]*)\"").findAll(filter).map { it.groupValues[1] }.toList(),
        )
        assertEquals(
            listOf("/view/"),
            Regex("android:pathPrefix=\"([^\"]*)\"")
                .findAll(filter)
                .map { it.groupValues[1] }
                .toList(),
        )
        assertFalse(
            Regex("android:(path|pathPattern|pathSuffix|pathAdvancedPattern)=")
                .containsMatchIn(filter)
        )
        assertEquals(1, Regex("<data ").findAll(filter).count(), "one data element")
        assertEquals(
            2,
            Regex("<intent-filter").findAll(manifest).count(),
            "the launcher filter and the link filter",
        )
    }

    @Test
    fun cleartextIsOnlyInTheDebugSourceSet() {
        val main = source("androidApp/src/main")
        for (file in androidFiles(main)) {
            val text = file.readText()
            assertFalse("usesCleartextTraffic" in text, file.path)
            assertFalse("networkSecurityConfig" in text, file.path)
            assertFalse("cleartextTrafficPermitted" in text, file.path)
        }
        val debug = source("androidApp/src/debug")
        assertTrue(androidFiles(debug).any { "cleartextTrafficPermitted" in it.readText() })
        assertFalse(
            "<intent-filter" in source("androidApp/src/debug/AndroidManifest.xml").readText()
        )
    }

    @Test
    fun iosEntitlementHoldsOnlyTheAssociatedDomain() {
        val entitlements = source("iosApp/iosApp/iosApp.entitlements").readText()
        val keys =
            Regex("<key>([^<]*)</key>").findAll(entitlements).map { it.groupValues[1] }.toList()
        assertEquals(listOf("com.apple.developer.associated-domains"), keys)
        val strings =
            Regex("<string>([^<]*)</string>")
                .findAll(entitlements)
                .map { it.groupValues[1] }
                .toList()
        assertEquals(listOf("applinks:\$(PREVIEW_HOST)"), strings)
    }

    @Test
    fun infoPlistRegistersNoCustomUrlScheme() {
        assertFalse("CFBundleURLTypes" in source("iosApp/iosApp/Info.plist").readText())
    }
}
