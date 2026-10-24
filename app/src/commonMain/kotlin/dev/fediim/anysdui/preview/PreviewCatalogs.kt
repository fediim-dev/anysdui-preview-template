package dev.fediim.anysdui.preview

import dev.fediim.anysdui.compose.basic.anysdui.generated.ComposeBasicRenderer
import dev.fediim.anysdui.compose.charts.anysdui.generated.ComposeChartsRenderer
import dev.fediim.anysdui.compose.cupertino.anysdui.generated.ComposeCupertinoRenderer
import dev.fediim.anysdui.compose.foundation.anysdui.generated.ComposeFoundationRenderer
import dev.fediim.anysdui.compose.material.anysdui.generated.ComposeMaterialRenderer
import dev.fediim.anysdui.compose.material3.anysdui.generated.ComposeMaterial3Renderer
import dev.fediim.anysdui.runtime.CatalogRegistration
import dev.fediim.anysdui.runtime.CatalogRenderer
import dev.fediim.anysdui.runtime.CatalogValidator

/**
 * Every catalog this app renders and validates, in precedence order: the company's design system
 * first, then foundation, material3, material, charts, cupertino and basic last (ten of basic's
 * names also exist in foundation, and a colliding name goes to the first catalog).
 *
 * [Invariant] Left out on purpose: 3D (the desktop build needs JDK 22 and iOS needs a native
 * bridge) and the unpublished MCP catalog; a room that needs 3D is refused by the server.
 * [Invariant] All renderers are compiled in and referenced as plain objects; nothing is loaded at
 * run time (C5). @throws IllegalArgumentException if a company renderer's catalog id differs from
 * its JSON's.
 */
fun previewRegistrations(): List<CatalogRegistration<CatalogRenderer>> =
    previewRegistrations(PreviewConfig.companyRenderers, PreviewCatalogJson.company)

/** [previewRegistrations] with the company's renderers and their catalog JSON given. */
internal fun previewRegistrations(
    companyRenderers: List<CatalogRenderer>,
    companyCatalogJson: List<String>,
): List<CatalogRegistration<CatalogRenderer>> {
    require(companyRenderers.size == companyCatalogJson.size) {
        "expected ${companyRenderers.size} company catalogs, got ${companyCatalogJson.size}"
    }
    val builtIns =
        listOf(
            ComposeFoundationRenderer,
            ComposeMaterial3Renderer,
            ComposeMaterialRenderer,
            ComposeChartsRenderer,
            ComposeCupertinoRenderer,
            ComposeBasicRenderer,
        )
    check(builtIns.size == PreviewCatalogJson.builtIn.size)
    return (companyRenderers + builtIns).zip(companyCatalogJson + PreviewCatalogJson.builtIn) {
        renderer,
        json ->
        CatalogRegistration(renderer, CatalogValidator.fromJsonString(json))
    }
}
