package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormInputOldTest {

    private fun input() = FormInputOld(
        inputType = InputType.EDIT_TEXT,
        title = "Title",
        required = true
    )

    @Test fun `defaults are null or expected`() {
        val i = input()
        assertNull(i.subtitle)
        assertNull(i.entries)
        assertNull(i.regex)
        assertNull(i.errorText)
        assertNull(i.max)
        assertNull(i.min)
        assertNull(i.imageFile)
        assertEquals(false, i.allCaps)
        assertEquals(50, i.etMaxLength)
    }

    @Test fun `value flow starts null and can be updated`() {
        val i = input()
        assertNull(i.value.value)
        i.value.value = "hello"
        assertEquals("hello", i.value.value)
    }

    @Test fun `copy toString and equality on constructor fields`() {
        val i = input()
        val same = i.copy()
        assertEquals(i, same)
        assertEquals(i.hashCode(), same.hashCode())
        assertNotEquals(i, i.copy(title = "Other"))
        assertTrue(i.toString().contains("FormInputOld"))
    }

    @Test fun `copy and equality with all fields set`() {
        val i = FormInputOld(
            inputType = InputType.EDIT_TEXT,
            title = "Title",
            subtitle = "Subtitle",
            entries = arrayOf("a", "b"),
            required = true,
            regex = "\\d+",
            allCaps = true,
            etInputType = 1,
            isMobileNumber = true,
            etMaxLength = 10,
            errorText = "err",
            max = 100L,
            min = 1L,
            minDecimal = 0.5,
            maxDecimal = 99.5,
            orientation = 1,
            imageFile = java.io.File("test.txt"),
            iconDrawableRes = 123
        )
        val same = i.copy()
        assertEquals(i, same)
        assertEquals(i.hashCode(), same.hashCode())

        assertNotEquals(i, i.copy(inputType = InputType.RADIO))
        assertNotEquals(i, i.copy(subtitle = "Other"))
        assertNotEquals(i, i.copy(required = false))
        assertNotEquals(i, i.copy(regex = "Other"))
        assertNotEquals(i, i.copy(allCaps = false))
        assertNotEquals(i, i.copy(etInputType = 999))
        assertNotEquals(i, i.copy(isMobileNumber = false))
        assertNotEquals(i, i.copy(etMaxLength = 999))
        assertNotEquals(i, i.copy(errorText = "Other"))
        assertNotEquals(i, i.copy(max = 999L))
        assertNotEquals(i, i.copy(min = 999L))
        assertNotEquals(i, i.copy(minDecimal = 9.9))
        assertNotEquals(i, i.copy(maxDecimal = 9.9))
        assertNotEquals(i, i.copy(orientation = 999))
        assertNotEquals(i, i.copy(imageFile = java.io.File("other.txt")))
        assertNotEquals(i, i.copy(iconDrawableRes = 999))
    }

    @Test fun `entries and min max fields can be set`() {
        val i = input().copy(
            entries = arrayOf("a", "b"),
            max = 100L,
            min = 1L,
            minDecimal = 0.5,
            maxDecimal = 99.5,
            errorText = "err"
        )
        assertEquals(2, i.entries?.size)
        assertEquals(100L, i.max)
        assertEquals(1L, i.min)
        assertEquals(0.5, i.minDecimal)
        assertEquals(99.5, i.maxDecimal)
        assertEquals("err", i.errorText)
        assertNotNull(i)
    }
}
