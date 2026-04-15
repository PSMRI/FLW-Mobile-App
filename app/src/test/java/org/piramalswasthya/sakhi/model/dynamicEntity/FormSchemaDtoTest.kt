package org.piramalswasthya.sakhi.model.dynamicEntity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormSchemaDtoTest {

    // =====================================================
    // FormSchemaDto Tests
    // =====================================================

    @Test fun `FormSchemaDto creation`() {
        val dto = FormSchemaDto(formId = "form_001", formName = "Test Form")
        assertEquals("form_001", dto.formId)
        assertEquals("Test Form", dto.formName)
        assertEquals(1, dto.version)
        assertTrue(dto.sections.isEmpty())
    }

    @Test fun `FormSchemaDto with sections`() {
        val section = FormSectionDto(sectionId = "s1", sectionTitle = "Section 1")
        val dto = FormSchemaDto(formId = "f1", formName = "Form", sections = listOf(section))
        assertEquals(1, dto.sections.size)
        assertEquals("s1", dto.sections[0].sectionId)
    }

    @Test fun `FormSchemaDto toJson returns valid string`() {
        val dto = FormSchemaDto(formId = "f1", formName = "Form")
        val json = dto.toJson()
        assertNotNull(json)
        assertTrue(json.contains("f1"))
        assertTrue(json.contains("Form"))
    }

    @Test fun `FormSchemaDto fromJson parses correctly`() {
        val json = """{"formId":"f1","formName":"Form","version":2,"sections":[]}"""
        val dto = FormSchemaDto.fromJson(json)
        assertEquals("f1", dto.formId)
        assertEquals("Form", dto.formName)
        assertEquals(2, dto.version)
    }

    @Test fun `FormSchemaDto roundtrip toJson and fromJson`() {
        val original = FormSchemaDto(formId = "f1", formName = "Form", version = 3)
        val json = original.toJson()
        val parsed = FormSchemaDto.fromJson(json)
        assertEquals(original.formId, parsed.formId)
        assertEquals(original.formName, parsed.formName)
        assertEquals(original.version, parsed.version)
    }

    @Test fun `FormSchemaDto copy changes formId`() {
        val dto = FormSchemaDto(formId = "f1", formName = "Form")
        val copy = dto.copy(formId = "f2")
        assertEquals("f2", copy.formId)
        assertEquals("Form", copy.formName)
    }

    // =====================================================
    // FormSectionDto Tests
    // =====================================================

    @Test fun `FormSectionDto creation with defaults`() {
        val section = FormSectionDto()
        assertEquals("", section.sectionId)
        assertEquals("", section.sectionTitle)
        assertTrue(section.fields.isEmpty())
    }

    @Test fun `FormSectionDto creation with fields`() {
        val field = FormFieldDto(fieldId = "f1", label = "Name")
        val section = FormSectionDto(sectionId = "s1", sectionTitle = "S1", fields = listOf(field))
        assertEquals(1, section.fields.size)
    }

    @Test fun `FormSectionDto copy`() {
        val section = FormSectionDto(sectionId = "s1", sectionTitle = "Title")
        val copy = section.copy(sectionTitle = "New Title")
        assertEquals("New Title", copy.sectionTitle)
    }

    // =====================================================
    // FormFieldDto Tests
    // =====================================================

    @Test fun `FormFieldDto creation with defaults`() {
        val field = FormFieldDto()
        assertEquals("", field.fieldId)
        assertEquals("", field.label)
        assertEquals("", field.type)
        assertFalse(field.required)
        assertNull(field.options)
        assertNull(field.conditional)
        assertNull(field.validation)
        assertNull(field.value)
        assertTrue(field.visible)
        assertTrue(field.isEditable)
    }

    @Test fun `FormFieldDto creation with values`() {
        val field = FormFieldDto(fieldId = "name", label = "Full Name", type = "text", required = true)
        assertEquals("name", field.fieldId)
        assertEquals("Full Name", field.label)
        assertEquals("text", field.type)
        assertTrue(field.required)
    }

    @Test fun `FormFieldDto value can be set`() {
        val field = FormFieldDto(fieldId = "age")
        field.value = 25
        assertEquals(25, field.value)
    }

    @Test fun `FormFieldDto visible can be toggled`() {
        val field = FormFieldDto()
        assertTrue(field.visible)
        field.visible = false
        assertFalse(field.visible)
    }

    @Test fun `FormFieldDto isEditable can be toggled`() {
        val field = FormFieldDto()
        assertTrue(field.isEditable)
        field.isEditable = false
        assertFalse(field.isEditable)
    }

    @Test fun `FormFieldDto with options`() {
        val field = FormFieldDto(fieldId = "gender", options = listOf("Male", "Female", "Other"))
        assertEquals(3, field.options!!.size)
    }

    @Test fun `FormFieldDto errorMessage can be set`() {
        val field = FormFieldDto()
        assertNull(field.errorMessage)
        field.errorMessage = "Required"
        assertEquals("Required", field.errorMessage)
    }

    @Test fun `FormFieldDto copy`() {
        val field = FormFieldDto(fieldId = "f1", label = "Label")
        val copy = field.copy(label = "New Label")
        assertEquals("New Label", copy.label)
        assertEquals("f1", copy.fieldId)
    }

    @Test fun `FormFieldDto with placeholder`() {
        val field = FormFieldDto(fieldId = "name", placeholder = "Enter name")
        assertEquals("Enter name", field.placeholder)
    }

    @Test fun `FormFieldDto with defaultValue`() {
        val field = FormFieldDto(fieldId = "status", defaultValue = "Active")
        assertEquals("Active", field.defaultValue)
    }

    // =====================================================
    // ConditionalLogic Tests
    // =====================================================

    @Test fun `ConditionalLogic creation with defaults`() {
        val logic = ConditionalLogic()
        assertNull(logic.dependsOn)
        assertNull(logic.expectedValue)
    }

    @Test fun `ConditionalLogic creation with values`() {
        val logic = ConditionalLogic(dependsOn = "gender", expectedValue = "Female")
        assertEquals("gender", logic.dependsOn)
        assertEquals("Female", logic.expectedValue)
    }

    @Test fun `ConditionalLogic equals`() {
        val a = ConditionalLogic("f1", "v1")
        val b = ConditionalLogic("f1", "v1")
        assertEquals(a, b)
    }

    @Test fun `ConditionalLogic copy`() {
        val logic = ConditionalLogic("f1", "v1")
        val copy = logic.copy(expectedValue = "v2")
        assertEquals("v2", copy.expectedValue)
    }

    // =====================================================
    // FieldValidationDto Tests
    // =====================================================

    @Test fun `FieldValidationDto creation with defaults`() {
        val validation = FieldValidationDto()
        assertNull(validation.min)
        assertNull(validation.max)
        assertNull(validation.minDate)
        assertNull(validation.maxDate)
        assertNull(validation.maxLength)
        assertNull(validation.regex)
        assertNull(validation.errorMessage)
    }

    @Test fun `FieldValidationDto with min max`() {
        val validation = FieldValidationDto(min = 0f, max = 100f)
        assertEquals(0f, validation.min)
        assertEquals(100f, validation.max)
    }

    @Test fun `FieldValidationDto with date constraints`() {
        val validation = FieldValidationDto(minDate = "dob", maxDate = "today")
        assertEquals("dob", validation.minDate)
        assertEquals("today", validation.maxDate)
    }

    @Test fun `FieldValidationDto with regex`() {
        val validation = FieldValidationDto(regex = "^[0-9]+$", errorMessage = "Numbers only")
        assertEquals("^[0-9]+$", validation.regex)
        assertEquals("Numbers only", validation.errorMessage)
    }

    @Test fun `FieldValidationDto copy`() {
        val validation = FieldValidationDto(min = 0f)
        val copy = validation.copy(max = 50f)
        assertEquals(0f, copy.min)
        assertEquals(50f, copy.max)
    }

    // =====================================================
    // OptionItem Tests
    // =====================================================

    @Test fun `OptionItem creation with defaults`() {
        val item = OptionItem()
        assertEquals("", item.label)
        assertNull(item.value)
    }

    @Test fun `OptionItem creation with values`() {
        val item = OptionItem(label = "Yes", value = true)
        assertEquals("Yes", item.label)
        assertEquals(true, item.value)
    }

    @Test fun `OptionItem equals`() {
        val a = OptionItem("A", 1)
        val b = OptionItem("A", 1)
        assertEquals(a, b)
    }

    @Test fun `OptionItem copy`() {
        val item = OptionItem("Old", 1)
        val copy = item.copy(label = "New")
        assertEquals("New", copy.label)
    }
}
