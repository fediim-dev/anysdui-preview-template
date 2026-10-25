package dev.fediim.anysdui.preview

import dev.fediim.anysdui.compose.basic.anysdui.generated.ComposeBasicRenderer
import dev.fediim.anysdui.compose.charts.anysdui.generated.ComposeChartsRenderer
import dev.fediim.anysdui.compose.cupertino.anysdui.generated.ComposeCupertinoRenderer
import dev.fediim.anysdui.compose.foundation.anysdui.generated.ComposeFoundationRenderer
import dev.fediim.anysdui.compose.material.anysdui.generated.ComposeMaterialRenderer
import dev.fediim.anysdui.compose.material3.anysdui.generated.ComposeMaterial3Renderer
import dev.fediim.anysdui.runtime.CatalogRegistration
import dev.fediim.anysdui.runtime.SurfaceHost
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PreviewCatalogsTest {
    private val builtInIds =
        listOf(
                ComposeFoundationRenderer,
                ComposeMaterial3Renderer,
                ComposeMaterialRenderer,
                ComposeChartsRenderer,
                ComposeCupertinoRenderer,
                ComposeBasicRenderer,
            )
            .map { it.catalogId }

    private fun catalogJson(name: String): String {
        val relative = "catalogs/compose-$name-catalog.json"
        return listOf(File(relative), File("app/$relative")).first { it.exists() }.readText()
    }

    private fun assertPrecedenceHolds(registrations: List<CatalogRegistration<*>>) {
        @Suppress("UNCHECKED_CAST")
        val host =
            SurfaceHost(
                registrations
                    as List<CatalogRegistration<dev.fediim.anysdui.runtime.CatalogRenderer>>
            )
        CatalogRegistration.checkPrecedence(assertNotNull(host.registeredRenderer), host.validator)
    }

    @Test
    fun withoutACompanyDesignSystemTheSixBuiltInsComeInOrderWithBasicLast() {
        val registrations = previewRegistrations(emptyList(), emptyList())
        assertEquals(builtInIds, registrations.map { it.catalogId })
        assertEquals(6, registrations.size)
        assertEquals(ComposeBasicRenderer.catalogId, registrations.last().catalogId)
        assertPrecedenceHolds(registrations)
    }

    @Test
    fun theBuildsCompanyRenderersComeFirst() {
        val registrations = previewRegistrations()
        val company = PreviewConfig.companyRenderers.map { it.catalogId }
        assertEquals(company + builtInIds, registrations.map { it.catalogId })
        assertPrecedenceHolds(registrations)
        // Built with -PdesignSystem.coordinates=:designsystems:sample (see the README).
        if (company.isNotEmpty())
            assertTrue(company.first().startsWith("urn:anysdui:sample-designsystem"))
    }

    @Test
    fun aCompanyRendererWhoseCatalogIdDiffersFromItsJsonThrows() {
        assertFailsWith<IllegalArgumentException> {
            previewRegistrations(listOf(ComposeBasicRenderer), listOf(catalogJson("foundation")))
        }
        assertFailsWith<IllegalArgumentException> {
            previewRegistrations(listOf(ComposeBasicRenderer), emptyList())
        }
    }
}
