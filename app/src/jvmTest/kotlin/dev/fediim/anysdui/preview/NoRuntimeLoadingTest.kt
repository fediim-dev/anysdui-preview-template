package dev.fediim.anysdui.preview

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/** C5: renderers and catalogs are compiled in; nothing loads code at run time. */
class NoRuntimeLoadingTest {
    private val forbidden =
        listOf(
                "Class.forName",
                "ServiceLoader",
                "ClassLoader",
                "loadLibrary",
                "dlopen",
            )
            .map { Regex(Regex.escape(it)) } + Regex("\\beval\\s*\\(")

    // Gradle runs this module's tests from app/; an IDE may run them from the repository root.
    private fun appDir(): File =
        listOf(File("."), File("app")).first { File(it, "src/commonMain").isDirectory }

    @Test
    fun noMainSourceLoadsCodeAtRunTime() {
        val app = appDir()
        val roots =
            (app.resolve("src").listFiles().orEmpty().filter { it.name.endsWith("Main") } +
                app.resolve("build/generated/preview")) +
                File(app.parentFile, "androidApp/src/main") +
                File(app.parentFile, "desktopApp/src/main") +
                File(app.parentFile, "webApp/src") +
                File(app.parentFile, "designsystems/sample/src/commonMain")
        val sources =
            roots
                .filter { it.exists() }
                .flatMap {
                    it.walkTopDown().filter { f -> f.isFile && f.extension == "kt" }.toList()
                }
        assertTrue(sources.size >= 8, "found ${sources.size} sources")
        for (file in sources) {
            val text = file.readText()
            for (pattern in forbidden) {
                assertTrue(!pattern.containsMatchIn(text), "${file.path} uses ${pattern.pattern}")
            }
        }
    }
}
