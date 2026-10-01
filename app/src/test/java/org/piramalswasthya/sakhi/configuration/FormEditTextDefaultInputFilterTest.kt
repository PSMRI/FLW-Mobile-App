package org.piramalswasthya.sakhi.configuration

import android.text.Spanned
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormEditTextDefaultInputFilterTest {

    private val dest = mockk<Spanned>(relaxed = true)

    @Test
    fun `uppercase letters are accepted`() {
        val result = FormEditTextDefaultInputFilter.filter("ABC", 0, 3, dest, 0, 0)
        assertNull(result)
    }

    @Test
    fun `uppercase letters with spaces are accepted`() {
        val result = FormEditTextDefaultInputFilter.filter("ABC DEF", 0, 7, dest, 0, 0)
        assertNull(result)
    }

    @Test
    fun `lowercase letters are rejected`() {
        val result = FormEditTextDefaultInputFilter.filter("abc", 0, 3, dest, 0, 0)
        assertEquals("", result)
    }

    @Test
    fun `digits are rejected`() {
        val result = FormEditTextDefaultInputFilter.filter("123", 0, 3, dest, 0, 0)
        assertEquals("", result)
    }

    @Test
    fun `mixed case is rejected`() {
        val result = FormEditTextDefaultInputFilter.filter("AbC", 0, 3, dest, 0, 0)
        assertEquals("", result)
    }

    @Test
    fun `empty source is rejected`() {
        val result = FormEditTextDefaultInputFilter.filter("", 0, 0, dest, 0, 0)
        assertEquals("", result)
    }
}
