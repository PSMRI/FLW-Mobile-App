package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormElementTest {

    @Test fun `FormElement creation with minimal params`() {
        val fe = FormElement(id = 1, inputType = InputType.EDIT_TEXT, required = true, title = "Name")
        assertEquals(1, fe.id)
        assertEquals(InputType.EDIT_TEXT, fe.inputType)
        assertEquals("Name", fe.title)
        assertTrue(fe.required)
    }


    @Test fun `FormElement with entries`() {
        val entries = arrayOf("Option1", "Option2", "Option3")
        val fe = FormElement(id = 1, inputType = InputType.DROPDOWN, required = true, title = "Select", entries = entries)
        assertEquals(3, fe.entries!!.size)
        assertEquals("Option1", fe.entries!![0])
    }

    @Test fun `FormElement value can be set`() {
        val fe = FormElement(id = 1, inputType = InputType.EDIT_TEXT, required = true, title = "Name")
        fe.value = "John"
        assertEquals("John", fe.value)
    }



    @Test fun `FormElement with errorText`() {
        val fe = FormElement(id = 1, inputType = InputType.EDIT_TEXT, required = true, title = "Name", errorText = "Required field")
        assertEquals("Required field", fe.errorText)
    }

    @Test fun `FormElement with hasDependants`() {
        val fe = FormElement(id = 1, inputType = InputType.DROPDOWN, required = true, title = "Type", hasDependants = true)
        assertTrue(fe.hasDependants)
    }

    @Test fun `FormElement with EDIT_TEXT`() { assertEquals(InputType.EDIT_TEXT, FormElement(1, InputType.EDIT_TEXT, false, "T").inputType) }
    @Test fun `FormElement with DROPDOWN`() { assertEquals(InputType.DROPDOWN, FormElement(1, InputType.DROPDOWN, false, "T").inputType) }
    @Test fun `FormElement with RADIO`() { assertEquals(InputType.RADIO, FormElement(1, InputType.RADIO, false, "T").inputType) }
    @Test fun `FormElement with DATE_PICKER`() { assertEquals(InputType.DATE_PICKER, FormElement(1, InputType.DATE_PICKER, false, "T").inputType) }
    @Test fun `FormElement with TEXT_VIEW`() { assertEquals(InputType.TEXT_VIEW, FormElement(1, InputType.TEXT_VIEW, false, "T").inputType) }
    @Test fun `FormElement with IMAGE_VIEW`() { assertEquals(InputType.IMAGE_VIEW, FormElement(1, InputType.IMAGE_VIEW, false, "T").inputType) }
    @Test fun `FormElement with CHECKBOXES`() { assertEquals(InputType.CHECKBOXES, FormElement(1, InputType.CHECKBOXES, false, "T").inputType) }
    @Test fun `FormElement with TIME_PICKER`() { assertEquals(InputType.TIME_PICKER, FormElement(1, InputType.TIME_PICKER, false, "T").inputType) }
    @Test fun `FormElement with HEADLINE`() { assertEquals(InputType.HEADLINE, FormElement(1, InputType.HEADLINE, false, "T").inputType) }
    @Test fun `FormElement with AGE_PICKER`() { assertEquals(InputType.AGE_PICKER, FormElement(1, InputType.AGE_PICKER, false, "T").inputType) }
    @Test fun `FormElement with FILE_UPLOAD`() { assertEquals(InputType.FILE_UPLOAD, FormElement(1, InputType.FILE_UPLOAD, false, "T").inputType) }
    @Test fun `FormElement with MULTIFILE_UPLOAD`() { assertEquals(InputType.MULTIFILE_UPLOAD, FormElement(1, InputType.MULTIFILE_UPLOAD, false, "T").inputType) }
    @Test fun `FormElement with BUTTON`() { assertEquals(InputType.BUTTON, FormElement(1, InputType.BUTTON, false, "T").inputType) }
    @Test fun `FormElement with NUMBER_PICKER`() { assertEquals(InputType.NUMBER_PICKER, FormElement(1, InputType.NUMBER_PICKER, false, "T").inputType) }

    @Test fun `FormElement with empty title`() { assertEquals("", FormElement(1, InputType.TEXT_VIEW, false, "").title) }
    @Test fun `FormElement with empty entries array`() { assertEquals(0, FormElement(1, InputType.DROPDOWN, false, "T", entries = emptyArray()).entries!!.size) }
    @Test fun `FormElement with null entries`() { assertNull(FormElement(1, InputType.EDIT_TEXT, false, "T").entries) }
    @Test fun `FormElement etMaxLength`() { assertEquals(50, FormElement(1, InputType.EDIT_TEXT, false, "T", etMaxLength = 50).etMaxLength) }
    @Test fun `FormElement required true`() { assertTrue(FormElement(1, InputType.EDIT_TEXT, true, "T").required) }
    @Test fun `FormElement required false`() { assertFalse(FormElement(1, InputType.EDIT_TEXT, false, "T").required) }
}
