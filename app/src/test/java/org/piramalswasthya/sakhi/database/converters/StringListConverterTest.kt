package org.piramalswasthya.sakhi.database.converters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StringListConverterTest {

    private val converter = StringListConverter()

    @Test
    fun `fromList returns null for a null list`() {
        assertNull(converter.fromList(null))
    }

    @Test
    fun `fromList joins entries with the triple pipe separator`() {
        assertEquals("a|||b|||c", converter.fromList(listOf("a", "b", "c")))
    }

    @Test
    fun `fromList of a single entry returns that entry`() {
        assertEquals("only", converter.fromList(listOf("only")))
    }

    @Test
    fun `fromList of an empty list returns an empty string`() {
        assertEquals("", converter.fromList(emptyList()))
    }

    @Test
    fun `fromList keeps blank entries`() {
        assertEquals("a|||", converter.fromList(listOf("a", "")))
    }

    @Test
    fun `toList returns null for null input`() {
        assertNull(converter.toList(null))
    }

    @Test
    fun `toList returns null for empty input`() {
        assertNull(converter.toList(""))
    }

    @Test
    fun `toList returns null for blank input`() {
        assertNull(converter.toList("   "))
    }

    @Test
    fun `toList splits on the triple pipe separator`() {
        assertEquals(listOf("a", "b", "c"), converter.toList("a|||b|||c"))
    }

    @Test
    fun `toList of a value without separators returns a single element list`() {
        assertEquals(listOf("only"), converter.toList("only"))
    }

    @Test
    fun `round trip preserves the original list`() {
        val original = listOf("alpha", "beta", "gamma")

        assertEquals(original, converter.toList(converter.fromList(original)))
    }
}
