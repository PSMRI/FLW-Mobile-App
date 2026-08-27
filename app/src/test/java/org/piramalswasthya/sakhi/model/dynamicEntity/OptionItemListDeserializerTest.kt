package org.piramalswasthya.sakhi.model.dynamicEntity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OptionItemListDeserializerTest {

    @Test fun `parse returns null for null input`() {
        assertNull(OptionItemParser.parse(null))
    }

    @Test fun `parse returns null for non list input`() {
        assertNull(OptionItemParser.parse("not a list"))
        assertNull(OptionItemParser.parse(42))
    }

    @Test fun `parse handles list of strings`() {
        val result = OptionItemParser.parse(listOf("Yes", "No"))
        assertEquals(2, result?.size)
        assertEquals(OptionItem(label = "Yes", value = "Yes"), result?.get(0))
        assertEquals(OptionItem(label = "No", value = "No"), result?.get(1))
    }

    @Test fun `parse handles list of maps with label and value`() {
        val raw = listOf(
            mapOf("id" to 20, "value" to "Home", "label" to "Home Delivery")
        )
        val result = OptionItemParser.parse(raw)
        assertEquals(1, result?.size)
        assertEquals("Home Delivery", result?.get(0)?.label)
        assertEquals("Home", result?.get(0)?.value)
    }

    @Test fun `parse handles map missing label falls back to value`() {
        val raw = listOf(mapOf("value" to "Hospital"))
        val result = OptionItemParser.parse(raw)
        assertEquals("Hospital", result?.get(0)?.label)
        assertEquals("Hospital", result?.get(0)?.value)
    }

    @Test fun `parse skips map missing value`() {
        val raw = listOf(mapOf("label" to "No value here"))
        val result = OptionItemParser.parse(raw)
        assertNull(result)
    }

    @Test fun `parse handles existing OptionItem instances`() {
        val item = OptionItem(label = "L", value = "V")
        val result = OptionItemParser.parse(listOf(item))
        assertEquals(listOf(item), result)
    }

    @Test fun `parse skips unsupported item types and returns null when result is empty`() {
        val result = OptionItemParser.parse(listOf(1, 2.0, true))
        assertNull(result)
    }

    @Test fun `parse skips unsupported items but keeps supported ones`() {
        val result = OptionItemParser.parse(listOf("Yes", 123, mapOf("value" to "No")))
        assertEquals(2, result?.size)
        assertEquals("Yes", result?.get(0)?.value)
        assertEquals("No", result?.get(1)?.value)
    }

    @Test fun `optionItems extension delegates to parser using field options`() {
        val field = FormFieldDto(fieldId = "gender", options = listOf("Male", "Female"))
        val result = field.optionItems()
        assertEquals(2, result?.size)
        assertEquals("Male", result?.get(0)?.value)
    }

    @Test fun `optionItems extension returns null when field has no options`() {
        val field = FormFieldDto(fieldId = "name")
        assertNull(field.optionItems())
    }
}
