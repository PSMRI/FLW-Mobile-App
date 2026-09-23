package org.piramalswasthya.sakhi.configuration.dynamicDataSet

import org.junit.Assert.assertEquals
import org.piramalswasthya.sakhi.model.dynamicEntity.OptionItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicFormFieldTest {

    // =====================================================
    // FormField Creation Tests
    // =====================================================

    @Test fun `FormField creation with required params`() {
        val f = FormField(fieldId = "name", label = "Name", type = "text", isRequired = true)
        assertEquals("name", f.fieldId)
        assertEquals("Name", f.label)
        assertEquals("text", f.type)
        assertTrue(f.isRequired)
    }

    @Test fun `FormField default values`() {
        val f = FormField(fieldId = "f", label = "L", type = "t", isRequired = false)
        assertNull(f.defaultValue)
        assertNull(f.options)
        assertNull(f.value)
        assertTrue(f.visible)
        assertNull(f.conditional)
        assertNull(f.errorMessage)
        assertNull(f.placeholder)
        assertNull(f.validation)
        assertTrue(f.isEditable)
    }

    @Test fun `FormField with all fields`() {
        val validation = FieldValidation(min = 0f, max = 100f)
        val conditional = ConditionalLogic("dep", "val")
        val f = FormField("id", "Label", "number", "0", true, listOf(OptionItem("A"), OptionItem("B")), 42, true, conditional, "err", "placeholder", validation, true)
        assertEquals("id", f.fieldId)
        assertEquals("0", f.defaultValue)
        assertEquals(42, f.value)
        assertEquals("err", f.errorMessage)
        assertEquals("placeholder", f.placeholder)
        assertNotNull(f.conditional)
        assertNotNull(f.validation)
    }

    @Test fun `FormField value can be changed`() {
        val f = FormField("f", "L", "text", isRequired = false)
        assertNull(f.value)
        f.value = "Hello"
        assertEquals("Hello", f.value)
    }

    @Test fun `FormField visible can be toggled`() {
        val f = FormField("f", "L", "text", isRequired = false)
        assertTrue(f.visible)
        f.visible = false
        assertFalse(f.visible)
    }

    @Test fun `FormField isEditable can be toggled`() {
        val f = FormField("f", "L", "text", isRequired = false)
        assertTrue(f.isEditable)
        f.isEditable = false
        assertFalse(f.isEditable)
    }

    @Test fun `FormField errorMessage can be set`() {
        val f = FormField("f", "L", "text", isRequired = false)
        f.errorMessage = "Required"
        assertEquals("Required", f.errorMessage)
    }

    @Test fun `FormField with options`() {
        val f = FormField("f", "L", "dropdown", isRequired = false, options = listOf(OptionItem("Yes"), OptionItem("No")))
        assertEquals(2, f.options!!.size)
        assertEquals(OptionItem("Yes"), f.options!![0])
    }

    @Test fun `FormField copy changes fieldId`() {
        val f = FormField("f1", "L", "text", isRequired = false)
        val copy = f.copy(fieldId = "f2")
        assertEquals("f2", copy.fieldId)
    }

    @Test fun `FormField equals for same data`() {
        val a = FormField("f", "L", "text", isRequired = true)
        val b = FormField("f", "L", "text", isRequired = true)
        assertEquals(a, b)
    }

    @Test fun `FormField not equals for different fieldId`() {
        val a = FormField("f1", "L", "text", isRequired = true)
        val b = FormField("f2", "L", "text", isRequired = true)
        assertNotEquals(a, b)
    }

    // =====================================================
    // ConditionalLogic Tests
    // =====================================================

    @Test fun `ConditionalLogic creation`() {
        val cl = ConditionalLogic("gender", "Female")
        assertEquals("gender", cl.dependsOn)
        assertEquals("Female", cl.expectedValue)
    }

    @Test fun `ConditionalLogic copy`() {
        val cl = ConditionalLogic("a", "b")
        val copy = cl.copy(expectedValue = "c")
        assertEquals("c", copy.expectedValue)
        assertEquals("a", copy.dependsOn)
    }

    @Test fun `ConditionalLogic equals`() {
        val a = ConditionalLogic("f", "v")
        val b = ConditionalLogic("f", "v")
        assertEquals(a, b)
    }

    @Test fun `ConditionalLogic not equals`() {
        val a = ConditionalLogic("f1", "v")
        val b = ConditionalLogic("f2", "v")
        assertNotEquals(a, b)
    }

    // =====================================================
    // FieldValidation Tests
    // =====================================================

    @Test fun `FieldValidation creation with defaults`() {
        val v = FieldValidation()
        assertNull(v.min)
        assertNull(v.max)
        assertNull(v.minDate)
        assertNull(v.maxDate)
        assertNull(v.maxLength)
        assertNull(v.regex)
        assertNull(v.errorMessage)
        assertNull(v.decimalPlaces)
        assertNull(v.maxSizeMB)
        assertNull(v.afterField)
        assertNull(v.beforeField)
    }

    @Test fun `FieldValidation with min max`() {
        val v = FieldValidation(min = 0f, max = 150f)
        assertEquals(0f, v.min)
        assertEquals(150f, v.max)
    }

    @Test fun `FieldValidation with date constraints`() {
        val v = FieldValidation(minDate = "01-01-2020", maxDate = "31-12-2026")
        assertEquals("01-01-2020", v.minDate)
        assertEquals("31-12-2026", v.maxDate)
    }

    @Test fun `FieldValidation with regex`() {
        val v = FieldValidation(regex = "^[0-9]{10}$", errorMessage = "Must be 10 digits")
        assertEquals("^[0-9]{10}$", v.regex)
        assertEquals("Must be 10 digits", v.errorMessage)
    }

    @Test fun `FieldValidation with maxLength`() {
        val v = FieldValidation(maxLength = 255)
        assertEquals(255, v.maxLength)
    }

    @Test fun `FieldValidation with decimalPlaces`() {
        val v = FieldValidation(decimalPlaces = 2)
        assertEquals(2, v.decimalPlaces)
    }

    @Test fun `FieldValidation with maxSizeMB`() {
        val v = FieldValidation(maxSizeMB = 5)
        assertEquals(5, v.maxSizeMB)
    }

    @Test fun `FieldValidation with afterField and beforeField`() {
        val v = FieldValidation(afterField = "start_date", beforeField = "end_date")
        assertEquals("start_date", v.afterField)
        assertEquals("end_date", v.beforeField)
    }

    @Test fun `FieldValidation copy`() {
        val v = FieldValidation(min = 0f)
        val copy = v.copy(max = 100f)
        assertEquals(0f, copy.min)
        assertEquals(100f, copy.max)
    }

    @Test fun `FieldValidation equals`() {
        val a = FieldValidation(min = 0f, max = 100f)
        val b = FieldValidation(min = 0f, max = 100f)
        assertEquals(a, b)
    }

    @Test fun `FieldValidation not equals`() {
        val a = FieldValidation(min = 0f)
        val b = FieldValidation(min = 10f)
        assertNotEquals(a, b)
    }

    @Test fun `FieldValidation minDate is mutable`() {
        val v = FieldValidation()
        assertNull(v.minDate)
        v.minDate = "01-01-2026"
        assertEquals("01-01-2026", v.minDate)
    }
}
