/**
 * Thrown when a preview Gradle property is invalid; the message names the property, never its
 * value.
 */
class PreviewConfigException(message: String) : RuntimeException(message)

/**
 * The preview app's Gradle properties (I1), parsed and checked.
 *
 * [Invariant] Built only by [parse]; every field passed its pattern. [Invariant] An error names the
 * property and the rule, never the value, since a value may be a path or an account detail.
 *
 * @property previewHost The host of [serverOrigin] without its port, for the Android App Link and
 *   the Apple associated domain.
 * @property coordinates `group:artifact:version` entries or Gradle project paths, in order.
 * @property renderers Fully qualified names of generated renderer objects, one per catalog.
 * @property catalogs Catalog JSON paths relative to the root project, one per renderer.
 */
data class PreviewProperties(
    val serverOrigin: String,
    val previewHost: String,
    val applicationId: String,
    val appleTeamId: String,
    val maxComponents: Int?,
    val maxPayloadBytes: Int?,
    val maxFrameBytes: Long?,
    val coordinates: List<String>,
    val renderers: List<String>,
    val catalogs: List<String>,
) {
    companion object {
        private val HTTPS_ORIGIN = Regex("^https://([a-z0-9.-]+)(:[0-9]{1,5})?$")
        private val LOOPBACK_ORIGIN = Regex("^http://(localhost|127\\.0\\.0\\.1)(:[0-9]{1,5})?$")
        private val APPLICATION_ID = Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+$")
        private val TEAM_ID = Regex("^[A-Z0-9]{10}$")
        private val MAVEN_COORDINATE = Regex("^[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+:[A-Za-z0-9_.+-]+$")
        private val PROJECT_PATH = Regex("^(:[a-zA-Z0-9_-]+)+$")
        private val QUALIFIED_IDENTIFIER =
            Regex("^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)+$")

        /**
         * Reads the properties through [read] (null or blank means unset) and checks them;
         * [fileExists] answers for a catalog path relative to the root project.
         *
         * @throws PreviewConfigException on the first property that is missing or invalid.
         */
        fun parse(read: (String) -> String?, fileExists: (String) -> Boolean): PreviewProperties {
            fun value(name: String): String? = read(name)?.trim()?.takeIf { it.isNotEmpty() }

            fun required(name: String): String =
                value(name) ?: throw PreviewConfigException("Gradle property '$name' is not set.")

            fun invalid(name: String, rule: String): Nothing =
                throw PreviewConfigException("Gradle property '$name' is invalid: $rule.")

            fun list(name: String): List<String> =
                value(name)
                    ?.split(',')
                    ?.map { it.trim() }
                    .orEmpty()
                    .also { entries ->
                        if (entries.any { it.isEmpty() }) invalid(name, "it has an empty entry")
                    }

            val origin = required("preview.serverOrigin")
            val originMatch =
                HTTPS_ORIGIN.matchEntire(origin) ?: LOOPBACK_ORIGIN.matchEntire(origin)
            val host = originMatch?.groupValues?.get(1)
            val port =
                originMatch?.groupValues?.get(2)?.drop(1)?.takeIf { it.isNotEmpty() }?.toInt()
            val hostShapeOk =
                host != null &&
                    !host.startsWith('.') &&
                    !host.startsWith('-') &&
                    !host.endsWith('.') &&
                    !host.endsWith('-') &&
                    ".." !in host
            if (host == null || !hostShapeOk || (port != null && port !in 1..65535)) {
                invalid(
                    "preview.serverOrigin",
                    "expected https://host[:port], or http://localhost or http://127.0.0.1 for development",
                )
            }

            val applicationId = required("preview.applicationId")
            if (!APPLICATION_ID.matches(applicationId)) {
                invalid(
                    "preview.applicationId",
                    "expected a Java package name such as com.example.app",
                )
            }
            val teamId = required("preview.appleTeamId")
            if (!TEAM_ID.matches(teamId)) {
                invalid("preview.appleTeamId", "expected 10 upper-case letters or digits")
            }

            fun positive(name: String): Long? =
                value(name)?.let { text ->
                    text.toLongOrNull()?.takeIf { it > 0 && it <= Int.MAX_VALUE }
                        ?: invalid(name, "expected a positive whole number")
                }

            val coordinates = list("designSystem.coordinates")
            for (entry in coordinates) {
                if (!MAVEN_COORDINATE.matches(entry) && !PROJECT_PATH.matches(entry)) {
                    invalid(
                        "designSystem.coordinates",
                        "an entry is neither group:artifact:version nor a project path",
                    )
                }
            }
            val renderers = list("designSystem.renderers")
            for (entry in renderers) {
                if (!QUALIFIED_IDENTIFIER.matches(entry)) {
                    invalid(
                        "designSystem.renderers",
                        "an entry is not a fully qualified Kotlin name",
                    )
                }
            }
            val catalogs = list("designSystem.catalogs")
            if (catalogs.size != renderers.size) {
                invalid(
                    "designSystem.catalogs",
                    "it needs as many entries as designSystem.renderers",
                )
            }
            for (entry in catalogs) {
                if (!fileExists(entry))
                    invalid("designSystem.catalogs", "an entry is not an existing file")
            }

            return PreviewProperties(
                serverOrigin = origin,
                previewHost = host,
                applicationId = applicationId,
                appleTeamId = teamId,
                maxComponents = positive("preview.maxComponents")?.toInt(),
                maxPayloadBytes = positive("preview.maxPayloadBytes")?.toInt(),
                maxFrameBytes = positive("preview.maxFrameBytes"),
                coordinates = coordinates,
                renderers = renderers,
                catalogs = catalogs,
            )
        }
    }
}
