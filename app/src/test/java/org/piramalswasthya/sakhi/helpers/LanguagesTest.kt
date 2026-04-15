package org.piramalswasthya.sakhi.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguagesTest {

    // =====================================================
    // Enum Values Tests
    // =====================================================

    @Test
    fun `english symbol is en`() {
        assertEquals("en", Languages.ENGLISH.symbol)
    }

    @Test
    fun `assamese symbol is as`() {
        assertEquals("as", Languages.ASSAMESE.symbol)
    }

    @Test
    fun `hindi symbol is hi`() {
        assertEquals("hi", Languages.HINDI.symbol)
    }

    @Test
    fun `total language count is 3`() {
        assertEquals(3, Languages.values().size)
    }

    @Test
    fun `valueOf returns correct enum`() {
        assertEquals(Languages.ENGLISH, Languages.valueOf("ENGLISH"))
        assertEquals(Languages.HINDI, Languages.valueOf("HINDI"))
        assertEquals(Languages.ASSAMESE, Languages.valueOf("ASSAMESE"))
    }

    @Test
    fun `all symbols are unique`() {
        val symbols = Languages.values().map { it.symbol }
        assertEquals(symbols.size, symbols.distinct().size)
    }

    @Test
    fun `all symbols are two letter codes`() {
        Languages.values().forEach {
            assertEquals(2, it.symbol.length)
        }
    }

    @Test fun `ENGLISH name is ENGLISH`() { assertEquals("ENGLISH", Languages.ENGLISH.name) }
    @Test fun `HINDI name is HINDI`() { assertEquals("HINDI", Languages.HINDI.name) }
    @Test fun `ASSAMESE name is ASSAMESE`() { assertEquals("ASSAMESE", Languages.ASSAMESE.name) }
    @Test fun `ENGLISH ordinal is 0`() { assertEquals(0, Languages.ENGLISH.ordinal) }
    @Test fun `symbols are lowercase`() { Languages.values().forEach { assertEquals(it.symbol, it.symbol.lowercase()) } }
    @Test fun `ENGLISH symbol is not empty`() { assertTrue(Languages.ENGLISH.symbol.isNotEmpty()) }
    @Test fun `HINDI symbol is not empty`() { assertTrue(Languages.HINDI.symbol.isNotEmpty()) }
    @Test fun `ASSAMESE symbol is not empty`() { assertTrue(Languages.ASSAMESE.symbol.isNotEmpty()) }
    @Test fun `all names are uppercase`() { Languages.values().forEach { assertEquals(it.name, it.name.uppercase()) } }
    @Test fun `all names are not blank`() { Languages.values().forEach { assertTrue(it.name.isNotBlank()) } }
}
