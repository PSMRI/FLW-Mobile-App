package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormElementTest {

    @Test
    fun `constructing with only the mandatory params fills every optional param with its default`() {
        val element = FormElement(
            id = 1,
            inputType = InputType.EDIT_TEXT,
            required = true,
            title = "field"
        )

        assertEquals(1, element.id)
        assertEquals(InputType.EDIT_TEXT, element.inputType)
        assertTrue(element.required)
        assertEquals("field", element.title)
        assertNull(element.subtitle)
        assertEquals(-1, element.arrayId)
        assertNull(element.entries)
        assertEquals(false, element.doubleStar)
        assertEquals(false, element.hasDependants)
        assertEquals(false, element.hasAlertError)
        assertNull(element.value)
        assertNull(element.regex)
        assertEquals(false, element.allCaps)
        assertEquals(android.text.InputType.TYPE_CLASS_TEXT, element.etInputType)
        assertEquals(false, element.isMobileNumber)
        assertEquals(50, element.etMaxLength)
        assertEquals(false, element.multiLine)
        assertNull(element.errorText)
        assertNull(element.max)
        assertNull(element.min)
        assertNull(element.minDecimal)
        assertNull(element.maxDecimal)
        assertNull(element.orientation)
        assertEquals(false, element.hasSpeechToText)
        assertEquals(false, element.showDateNumberPicker)
        assertEquals(false, element.showHighRisk)
        assertTrue(element.isEnabled)
        assertTrue(element.headingLine)
        assertEquals(false, element.showYearFirstInDatePicker)
        assertNull(element.backgroundDrawable)
        assertNull(element.iconDrawableRes)
        assertNull(element.minFiles)
        assertNull(element.maxFiles)
        assertNull(element.allowedFormats)
        assertNull(element.maxFileSizeMB)
        assertNull(element.selectedFiles)
        assertEquals(false, element.showDrawable)
        assertNull(element.secondaryTitle)
        assertNull(element.secondaryValue)
    }

    @Test
    fun `constructing with every param overridden keeps the supplied values`() {
        val entries = arrayOf("a", "b")
        val files = mutableListOf("a.jpg")
        val formats = listOf("jpg", "png")
        val element = FormElement(
            id = 2,
            inputType = InputType.DROPDOWN,
            required = false,
            title = "t",
            subtitle = "sub",
            arrayId = 9,
            entries = entries,
            doubleStar = true,
            hasDependants = true,
            hasAlertError = true,
            value = "v",
            regex = "^.*$",
            allCaps = true,
            etInputType = 1,
            isMobileNumber = true,
            etMaxLength = 10,
            multiLine = true,
            errorText = "err",
            max = 5L,
            min = 1L,
            minDecimal = 0.1,
            maxDecimal = 9.9,
            orientation = 2,
            hasSpeechToText = true,
            showDateNumberPicker = true,
            showHighRisk = true,
            isEnabled = false,
            headingLine = false,
            showYearFirstInDatePicker = true,
            backgroundDrawable = 100,
            iconDrawableRes = 200,
            minFiles = 1,
            maxFiles = 3,
            allowedFormats = formats,
            maxFileSizeMB = 5,
            selectedFiles = files,
            showDrawable = true,
            secondaryTitle = "sec",
            secondaryValue = "secval"
        )

        assertEquals("sub", element.subtitle)
        assertEquals(9, element.arrayId)
        assertEquals(entries, element.entries)
        assertTrue(element.doubleStar)
        assertTrue(element.hasDependants)
        assertTrue(element.hasAlertError)
        assertEquals("v", element.value)
        assertEquals("^.*$", element.regex)
        assertEquals(1, element.etInputType)
        assertTrue(element.isMobileNumber)
        assertEquals(10, element.etMaxLength)
        assertTrue(element.multiLine)
        assertEquals("err", element.errorText)
        assertEquals(5L, element.max)
        assertEquals(1L, element.min)
        assertEquals(0.1, element.minDecimal)
        assertEquals(9.9, element.maxDecimal)
        assertEquals(2, element.orientation)
        assertTrue(element.hasSpeechToText)
        assertTrue(element.showDateNumberPicker)
        assertTrue(element.showHighRisk)
        assertEquals(false, element.isEnabled)
        assertEquals(false, element.headingLine)
        assertTrue(element.showYearFirstInDatePicker)
        assertEquals(100, element.backgroundDrawable)
        assertEquals(200, element.iconDrawableRes)
        assertEquals(1, element.minFiles)
        assertEquals(3, element.maxFiles)
        assertEquals(formats, element.allowedFormats)
        assertEquals(5, element.maxFileSizeMB)
        assertEquals(files, element.selectedFiles)
        assertEquals(true, element.showDrawable)
        assertEquals("sec", element.secondaryTitle)
        assertEquals("secval", element.secondaryValue)
    }

    @Test
    fun `copy overrides only the requested fields and equals reflects field equality`() {
        val original = FormElement(id = 1, inputType = InputType.EDIT_TEXT, required = true, title = "a")
        val renamed = original.copy(title = "b")

        assertEquals("b", renamed.title)
        assertEquals(original.id, renamed.id)
        assertTrue(original != renamed)
        assertEquals(original, original.copy())
        assertEquals(original.hashCode(), original.copy().hashCode())
        assertTrue(original.toString().contains("field"))
    }
}
