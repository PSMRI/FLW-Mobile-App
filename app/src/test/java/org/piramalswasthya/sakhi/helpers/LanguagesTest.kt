package org.piramalswasthya.sakhi.helpers

import org.junit.Assert.assertEquals
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
}
