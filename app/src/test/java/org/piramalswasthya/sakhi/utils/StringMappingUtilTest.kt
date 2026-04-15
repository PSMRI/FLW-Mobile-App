package org.piramalswasthya.sakhi.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StringMappingUtilTest {

    // --- convertDigits ---

    @Test
    fun `hindi digits convert to english`() {
        assertEquals("0123456789", StringMappingUtil.convertDigits("०१२३४५६७८९"))
    }

    @Test
    fun `bengali assamese digits convert to english`() {
        assertEquals("0123456789", StringMappingUtil.convertDigits("০১২৩৪৫৬৭৮৯"))
    }

    @Test
    fun `mixed hindi and english text converts correctly`() {
        assertEquals("Age: 25 years", StringMappingUtil.convertDigits("Age: २५ years"))
    }

    @Test
    fun `already english digits remain unchanged`() {
        assertEquals("12345", StringMappingUtil.convertDigits("12345"))
    }

    @Test
    fun `plain english text remains unchanged`() {
        assertEquals("Hello World", StringMappingUtil.convertDigits("Hello World"))
    }

    @Test
    fun `empty string returns empty`() {
        assertEquals("", StringMappingUtil.convertDigits(""))
    }

    @Test
    fun `null input returns empty`() {
        assertEquals("", StringMappingUtil.convertDigits(null))
    }

    @Test
    fun `mixed hindi digits within sentence`() {
        assertEquals("Phone: 9876543210", StringMappingUtil.convertDigits("Phone: ९८७६५४३२१०"))
    }

    @Test
    fun `single hindi digit converts`() {
        assertEquals("5", StringMappingUtil.convertDigits("५"))
    }

    @Test
    fun `special characters preserved during conversion`() {
        assertEquals("25-03-2026", StringMappingUtil.convertDigits("२५-०३-२०२६"))
    }

    // --- toEnglishDigits (recursive) ---

    @Test
    fun `toEnglishDigits converts string`() {
        assertEquals("25", StringMappingUtil.toEnglishDigits("२५"))
    }

    @Test
    fun `toEnglishDigits converts number`() {
        val result = StringMappingUtil.toEnglishDigits(42)
        assertEquals("42", result)
    }

    @Test
    fun `toEnglishDigits converts list`() {
        val input = listOf("२५", "३०", "hello")
        val expected = listOf("25", "30", "hello")
        assertEquals(expected, StringMappingUtil.toEnglishDigits(input))
    }

    @Test
    fun `toEnglishDigits converts map values`() {
        val input = mapOf("age" to "२५", "name" to "Nikhil")
        val result = StringMappingUtil.toEnglishDigits(input) as Map<*, *>
        assertEquals("25", result["age"])
        assertEquals("Nikhil", result["name"])
    }

    @Test
    fun `toEnglishDigits returns null for null input`() {
        assertNull(StringMappingUtil.toEnglishDigits(null))
    }

    @Test
    fun `toEnglishDigits converts nested list`() {
        val input = listOf(listOf("१", "२"), listOf("३"))
        val expected = listOf(listOf("1", "2"), listOf("3"))
        assertEquals(expected, StringMappingUtil.toEnglishDigits(input))
    }

    @Test
    fun `toEnglishDigits handles boolean passthrough`() {
        assertEquals(true, StringMappingUtil.toEnglishDigits(true))
    }
}
