package dev.fediim.anysdui.designsystem

import dev.fediim.anysdui.designsystem.anysdui.generated.*
import dev.fediim.anysdui.designsystem.components.DsButtonVariant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MappersTest {

    @Test
    fun testEnumDecoding() {
        assertEquals(DsButtonVariant.Primary, "primary".toDsButtonVariant())
        assertEquals(DsButtonVariant.Secondary, "secondary".toDsButtonVariant())
        assertEquals(DsButtonVariant.Ghost, "ghost".toDsButtonVariant())

        val ex =
            assertFailsWith<RuntimeException> {
                "invalid".toDsButtonVariant()
            }
        assertTrue(ex.message!!.contains("invalid"))
        assertTrue(ex.message!!.contains("DsButtonVariant"))
    }

    @Test
    fun testHexColorParsing() {
        val color = parseHexColor("#FF0000")
        assertEquals(1f, color.red)
        assertEquals(0f, color.green)
        assertEquals(0f, color.blue)
    }
}
