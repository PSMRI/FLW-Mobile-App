package org.piramalswasthya.sakhi.utils.dynamicFiledValidator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FieldValidation
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField

class FieldValidatorTest {

    private fun makeField(
        type: String = "text",
        label: String = "Test",
        value: Any? = null,
        required: Boolean = false,
        validation: FieldValidation? = null
    ) = FormField(
        fieldId = "test_field",
        label = label,
        type = type,
        value = value,
        isRequired = required,
        validation = validation
    )

    // =====================================================
    // Required Field Tests
    // =====================================================

    @Test fun `required field with null value fails`() {
        val field = makeField(required = true, value = null)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("required"))
    }

    @Test fun `required field with blank value fails`() {
        val field = makeField(required = true, value = "  ")
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
    }

    @Test fun `required field with value passes`() {
        val field = makeField(required = true, value = "John")
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `optional field with null value passes`() {
        val field = makeField(required = false, value = null)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    // =====================================================
    // Number Validation Tests
    // =====================================================

    @Test fun `number field with valid number passes`() {
        val validation = FieldValidation(min = 0f, max = 100f)
        val field = makeField(type = "number", value = "50", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `number field below min fails`() {
        val validation = FieldValidation(min = 10f, max = 100f)
        val field = makeField(type = "number", value = "5", label = "Age", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("at least"))
    }

    @Test fun `number field above max fails`() {
        val validation = FieldValidation(min = 0f, max = 100f)
        val field = makeField(type = "number", value = "150", label = "Score", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("at most"))
    }

    @Test fun `number field at min boundary passes`() {
        val validation = FieldValidation(min = 10f, max = 100f)
        val field = makeField(type = "number", value = "10", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `number field at max boundary passes`() {
        val validation = FieldValidation(min = 0f, max = 100f)
        val field = makeField(type = "number", value = "100", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `number field with non-numeric value fails`() {
        val validation = FieldValidation(min = 0f, max = 100f)
        val field = makeField(type = "number", value = "abc", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("number"))
    }

    @Test fun `number field with decimal passes`() {
        val validation = FieldValidation(min = 0f, max = 100f)
        val field = makeField(type = "number", value = "50.5", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `number field with null min passes for any value`() {
        val validation = FieldValidation(max = 100f)
        val field = makeField(type = "number", value = "-999", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `number field with null max passes for any value`() {
        val validation = FieldValidation(min = 0f)
        val field = makeField(type = "number", value = "999999", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    // =====================================================
    // Text Validation Tests
    // =====================================================

    @Test fun `text field with valid regex passes`() {
        val validation = FieldValidation(regex = "^[A-Z]+$")
        val field = makeField(type = "text", value = "HELLO", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `text field with invalid regex fails`() {
        val validation = FieldValidation(regex = "^[A-Z]+$", errorMessage = "Must be uppercase")
        val field = makeField(type = "text", value = "hello", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
        assertEquals("Must be uppercase", result.errorMessage)
    }

    @Test fun `text field with no regex passes`() {
        val validation = FieldValidation()
        val field = makeField(type = "text", value = "anything", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    // =====================================================
    // Date Validation Tests
    // =====================================================

    @Test fun `date field with valid date passes`() {
        val field = makeField(type = "date", value = "14-04-2026")
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }


    @Test fun `date field with today max validation`() {
        val validation = FieldValidation(maxDate = "today")
        val field = makeField(type = "date", value = "14-04-2026", validation = validation)
        val result = FieldValidator.validate(field, null, "14-04-2026")
        assertTrue(result.isValid)
    }

    // =====================================================
    // Unknown Type Tests
    // =====================================================

    @Test fun `unknown type always passes`() {
        val field = makeField(type = "custom_widget", value = "anything")
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    // =====================================================
    // ValidationResult Tests
    // =====================================================

    @Test fun `ValidationResult creation valid`() {
        val result = FieldValidator.ValidationResult(true)
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test fun `ValidationResult creation invalid with message`() {
        val result = FieldValidator.ValidationResult(false, "Error occurred")
        assertFalse(result.isValid)
        assertEquals("Error occurred", result.errorMessage)
    }

    @Test fun `ValidationResult equals`() {
        val a = FieldValidator.ValidationResult(true)
        val b = FieldValidator.ValidationResult(true)
        assertEquals(a, b)
    }

    // =====================================================
    // FieldValidationConfig Tests
    // =====================================================

    @Test fun `FieldValidationConfig creation`() {
        val config = FieldValidationConfig("test_field")
        assertEquals("test_field", config.fieldId)
        assertTrue(config.customMessages.isEmpty())
    }

    @Test fun `FieldValidationConfig with custom messages`() {
        val messages = mapOf("minDate" to "Too early", "maxDate" to "Too late")
        val config = FieldValidationConfig("due_date", messages)
        assertEquals("Too early", config.customMessages["minDate"])
        assertEquals("Too late", config.customMessages["maxDate"])
    }

    // =====================================================
    // No Validation Rules Tests
    // =====================================================

    @Test fun `field without validation rules passes`() {
        val field = makeField(type = "text", value = "anything", validation = null)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `number field without validation rules passes`() {
        val field = makeField(type = "number", value = "42")
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    // =====================================================
    // Extended Number Edge Cases
    // =====================================================

    @Test fun `number field with zero passes when min is 0`() {
        val validation = FieldValidation(min = 0f, max = 100f)
        val field = makeField(type = "number", value = "0", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `number field with negative passes when min allows`() {
        val validation = FieldValidation(min = -100f, max = 100f)
        val field = makeField(type = "number", value = "-50", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    // number field with null value has multiple valid behaviors - skipped

    @Test fun `number field just below min fails`() {
        val validation = FieldValidation(min = 10f, max = 100f)
        val field = makeField(type = "number", value = "9.99", label = "Score", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
    }

    @Test fun `number field just above max fails`() {
        val validation = FieldValidation(min = 0f, max = 100f)
        val field = makeField(type = "number", value = "100.01", label = "Score", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
    }

    // =====================================================
    // Extended Text/Regex Edge Cases
    // =====================================================

    @Test fun `text field with null regex passes any value`() {
        val validation = FieldValidation(regex = null)
        val field = makeField(type = "text", value = "any", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `text field with phone regex passes valid phone`() {
        val validation = FieldValidation(regex = "^[0-9]{10}$", errorMessage = "Invalid phone")
        val field = makeField(type = "text", value = "9876543210", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `text field with phone regex fails short number`() {
        val validation = FieldValidation(regex = "^[0-9]{10}$", errorMessage = "Invalid phone")
        val field = makeField(type = "text", value = "12345", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
        assertEquals("Invalid phone", result.errorMessage)
    }

    @Test fun `text field with email regex passes valid email`() {
        val validation = FieldValidation(regex = "^[^@]+@[^@]+\\.[^@]+$", errorMessage = "Invalid email")
        val field = makeField(type = "text", value = "test@example.com", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `text field with email regex fails invalid email`() {
        val validation = FieldValidation(regex = "^[^@]+@[^@]+\\.[^@]+$", errorMessage = "Invalid email")
        val field = makeField(type = "text", value = "not-an-email", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
    }

    // =====================================================
    // Extended Required Field Edge Cases
    // =====================================================

    @Test fun `required field with empty string fails`() {
        val field = makeField(required = true, value = "")
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
    }

    @Test fun `required field with single char passes`() {
        val field = makeField(required = true, value = "A")
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `required field with numeric value passes`() {
        val field = makeField(required = true, value = "123")
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `required field with whitespace only fails`() {
        val field = makeField(required = true, value = "\t\n  ")
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
    }

    // =====================================================
    // Extended ValidationResult Tests
    // =====================================================

    @Test fun `ValidationResult inequality`() {
        val a = FieldValidator.ValidationResult(true)
        val b = FieldValidator.ValidationResult(false, "Error")
        assertNotEquals(a, b)
    }

    @Test fun `ValidationResult with null message`() {
        val result = FieldValidator.ValidationResult(true, null)
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test fun `ValidationResult with empty message`() {
        val result = FieldValidator.ValidationResult(false, "")
        assertFalse(result.isValid)
        assertEquals("", result.errorMessage)
    }

    // =====================================================
    // Multiple Validations Combined
    // =====================================================

    @Test fun `required number with min max all valid`() {
        val validation = FieldValidation(min = 1f, max = 150f)
        val field = makeField(type = "number", value = "75", required = true, validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `required number with value below min fails`() {
        val validation = FieldValidation(min = 18f, max = 100f)
        val field = makeField(type = "number", value = "10", required = true, label = "Age", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
    }

    @Test fun `required text with regex both valid`() {
        val validation = FieldValidation(regex = "^[A-Za-z]+$")
        val field = makeField(type = "text", value = "Hello", required = true, validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `required text with regex fails on required`() {
        val validation = FieldValidation(regex = "^[A-Za-z]+$")
        val field = makeField(type = "text", value = "", required = true, validation = validation)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
    }
}
