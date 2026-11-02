package org.piramalswasthya.sakhi.configuration

import android.text.Spanned
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class DecimalDigitsInputFilterTest {

    private val dest = mockk<Spanned>(relaxed = true)

    @Test
    fun `empty source is returned unchanged`() {
        val result = DecimalDigitsInputFilter.filter("", 0, 0, dest, 0, 0)
        assertEquals("", result)
    }

    @Test
    fun `single digit before and after decimal point matches`() {
        val result = DecimalDigitsInputFilter.filter("1.5", 0, 3, dest, 0, 0)
        assertEquals("1.5", result)
    }

    @Test
    fun `two digits before decimal point matches`() {
        val result = DecimalDigitsInputFilter.filter("12.3", 0, 4, dest, 0, 0)
        assertEquals("12.3", result)
    }

    @Test
    fun `three digits before decimal point matches`() {
        val result = DecimalDigitsInputFilter.filter("123.4", 0, 5, dest, 0, 0)
        assertEquals("123.4", result)
    }

    @Test
    fun `four digits before decimal point is rejected`() {
        val result = DecimalDigitsInputFilter.filter("1234.5", 0, 6, dest, 0, 0)
        assertEquals("", result)
    }

    @Test
    fun `two digits after decimal point is rejected`() {
        val result = DecimalDigitsInputFilter.filter("1.55", 0, 4, dest, 0, 0)
        assertEquals("", result)
    }

    @Test
    fun `missing decimal point is rejected`() {
        val result = DecimalDigitsInputFilter.filter("15", 0, 2, dest, 0, 0)
        assertEquals("", result)
    }

    @Test
    fun `non numeric source is rejected`() {
        val result = DecimalDigitsInputFilter.filter("abc", 0, 3, dest, 0, 0)
        assertEquals("", result)
    }
}
