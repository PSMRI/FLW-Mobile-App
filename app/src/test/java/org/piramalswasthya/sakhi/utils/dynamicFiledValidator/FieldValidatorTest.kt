package org.piramalswasthya.sakhi.utils.dynamicFiledValidator

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.R
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

    // =====================================================
    // Required Field Collection Value Tests
    // =====================================================

    @Test fun `required field with empty collection value fails`() {
        val field = makeField(required = true, value = emptyList<String>())
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
    }

    @Test fun `required field with non-empty collection value passes`() {
        val field = makeField(required = true, value = listOf("a", "b"))
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    // =====================================================
    // Unknown Type With Validation Rules
    // =====================================================

    @Test fun `unknown type with validation rules still passes`() {
        val validation = FieldValidation(min = 0f, max = 10f)
        val field = makeField(type = "custom_widget", value = "42", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    // =====================================================
    // Date Validation - Invalid Format
    // =====================================================

    @Test fun `date field with unparseable value fails`() {
        val validation = FieldValidation()
        val field = makeField(type = "date", value = "not-a-date", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("invalid"))
    }

    // =====================================================
    // Date Validation - minDate/maxDate = "dob"
    // =====================================================

    @Test fun `date field with minDate dob and value before dob fails`() {
        val validation = FieldValidation(minDate = "dob")
        val field = makeField(type = "date", value = "05-04-2026", validation = validation)
        val result = FieldValidator.validate(field, "10-04-2026")
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("before"))
    }

    @Test fun `date field with minDate dob and value after dob passes`() {
        val validation = FieldValidation(minDate = "dob")
        val field = makeField(type = "date", value = "10-04-2026", validation = validation)
        val result = FieldValidator.validate(field, "05-04-2026")
        assertTrue(result.isValid)
    }

    @Test fun `date field with minDate dob and null dob passes`() {
        val validation = FieldValidation(minDate = "dob")
        val field = makeField(type = "date", value = "01-01-2026", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `date field with maxDate dob and value after dob fails`() {
        val validation = FieldValidation(maxDate = "dob")
        val field = makeField(type = "date", value = "10-04-2026", validation = validation)
        val result = FieldValidator.validate(field, "05-04-2026")
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("after"))
    }

    @Test fun `date field with maxDate dob and value before dob passes`() {
        val validation = FieldValidation(maxDate = "dob")
        val field = makeField(type = "date", value = "05-04-2026", validation = validation)
        val result = FieldValidator.validate(field, "10-04-2026")
        assertTrue(result.isValid)
    }

    @Test fun `date field with maxDate dob and null dob passes`() {
        val validation = FieldValidation(maxDate = "dob")
        val field = makeField(type = "date", value = "01-01-2026", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    // =====================================================
    // Date Validation - minDate/maxDate = "today"
    // =====================================================

    @Test fun `date field with minDate today and value before today fails`() {
        val validation = FieldValidation(minDate = "today")
        val field = makeField(type = "date", value = "10-04-2026", validation = validation)
        val result = FieldValidator.validate(field, null, "15-04-2026")
        assertFalse(result.isValid)
    }

    @Test fun `date field with minDate today and value on today passes`() {
        val validation = FieldValidation(minDate = "today")
        val field = makeField(type = "date", value = "15-04-2026", validation = validation)
        val result = FieldValidator.validate(field, null, "15-04-2026")
        assertTrue(result.isValid)
    }

    @Test fun `date field with maxDate today and value after today fails`() {
        val validation = FieldValidation(maxDate = "today")
        val field = makeField(type = "date", value = "15-04-2026", validation = validation)
        val result = FieldValidator.validate(field, null, "10-04-2026")
        assertFalse(result.isValid)
    }

    @Test fun `date field with maxDate today and value on today passes`() {
        val validation = FieldValidation(maxDate = "today")
        val field = makeField(type = "date", value = "10-04-2026", validation = validation)
        val result = FieldValidator.validate(field, null, "10-04-2026")
        assertTrue(result.isValid)
    }

    // =====================================================
    // Date Validation - Explicit minDate/maxDate Strings
    // =====================================================

    @Test fun `date field with explicit minDate string and value before it fails`() {
        val validation = FieldValidation(minDate = "01-01-2026")
        val field = makeField(type = "date", value = "01-12-2025", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
    }

    @Test fun `date field with explicit minDate string and value after it passes`() {
        val validation = FieldValidation(minDate = "01-01-2026")
        val field = makeField(type = "date", value = "02-01-2026", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `date field with explicit maxDate string and value after it fails`() {
        val validation = FieldValidation(maxDate = "31-12-2025")
        val field = makeField(type = "date", value = "01-01-2026", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
    }

    @Test fun `date field with explicit maxDate string and value before it passes`() {
        val validation = FieldValidation(maxDate = "31-12-2025")
        val field = makeField(type = "date", value = "30-12-2025", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    @Test fun `date field with validation but no min or max date passes`() {
        val validation = FieldValidation()
        val field = makeField(type = "date", value = "01-01-2026", validation = validation)
        val result = FieldValidator.validate(field, null)
        assertTrue(result.isValid)
    }

    // =====================================================
    // Date Validation - Custom Field Messages (due_date)
    // =====================================================

    @Test fun `due date field before minDate uses custom message`() {
        val validation = FieldValidation(minDate = "01-01-2026")
        val field = makeField(type = "date", value = "01-12-2025", validation = validation).copy(fieldId = "due_date")
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("Date of Delivery"))
    }

    @Test fun `due date field after maxDate uses custom message`() {
        val validation = FieldValidation(maxDate = "31-12-2025")
        val field = makeField(type = "date", value = "01-01-2026", validation = validation).copy(fieldId = "due_date")
        val result = FieldValidator.validate(field, null)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("Date of Delivery"))
    }

    // =====================================================
    // Context-Backed Error Message Tests
    // =====================================================

    @Test fun `required field with context uses context message`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.field_is_required, "Name") } returns "Name is mandatory"
        val field = makeField(required = true, value = null, label = "Name")
        val result = FieldValidator.validate(field, null, context = ctx)
        assertFalse(result.isValid)
        assertEquals("Name is mandatory", result.errorMessage)
    }

    @Test fun `text field invalid regex with context uses context message`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.field_is_invalid, "Code") } returns "Code format wrong"
        val validation = FieldValidation(regex = "^[0-9]+$")
        val field = makeField(type = "text", value = "abc", label = "Code", validation = validation)
        val result = FieldValidator.validate(field, null, context = ctx)
        assertFalse(result.isValid)
        assertEquals("Code format wrong", result.errorMessage)
    }

    @Test fun `number field non-numeric with context uses context message`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.field_must_be_number, "Weight") } returns "Weight invalid number"
        val validation = FieldValidation(min = 0f, max = 100f)
        val field = makeField(type = "number", value = "xyz", label = "Weight", validation = validation)
        val result = FieldValidator.validate(field, null, context = ctx)
        assertFalse(result.isValid)
        assertEquals("Weight invalid number", result.errorMessage)
    }

    @Test fun `number field below min with context uses context message`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.field_min_value, "Age", "18.0") } returns "Age too low"
        val validation = FieldValidation(min = 18f, max = 100f)
        val field = makeField(type = "number", value = "10", label = "Age", validation = validation)
        val result = FieldValidator.validate(field, null, context = ctx)
        assertFalse(result.isValid)
        assertEquals("Age too low", result.errorMessage)
    }

    @Test fun `number field above max with context uses context message`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.field_max_value, "Score", "100.0") } returns "Score too high"
        val validation = FieldValidation(min = 0f, max = 100f)
        val field = makeField(type = "number", value = "150", label = "Score", validation = validation)
        val result = FieldValidator.validate(field, null, context = ctx)
        assertFalse(result.isValid)
        assertEquals("Score too high", result.errorMessage)
    }

    @Test fun `date field invalid with context uses context message`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.field_date_invalid, "DOB") } returns "DOB bad date"
        val validation = FieldValidation()
        val field = makeField(type = "date", value = "not-a-date", label = "DOB", validation = validation)
        val result = FieldValidator.validate(field, null, context = ctx)
        assertFalse(result.isValid)
        assertEquals("DOB bad date", result.errorMessage)
    }

    @Test fun `date field minDate before with context uses context message`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.field_date_before, "Visit Date", "01-01-2026") } returns "Visit Date too early"
        val validation = FieldValidation(minDate = "01-01-2026")
        val field = makeField(type = "date", value = "01-12-2025", label = "Visit Date", validation = validation)
        val result = FieldValidator.validate(field, null, context = ctx)
        assertFalse(result.isValid)
        assertEquals("Visit Date too early", result.errorMessage)
    }

    @Test fun `date field maxDate after with context uses context message`() {
        val ctx = mockk<Context>(relaxed = true)
        every { ctx.getString(R.string.field_date_after, "Visit Date", "31-12-2025") } returns "Visit Date too late"
        val validation = FieldValidation(maxDate = "31-12-2025")
        val field = makeField(type = "date", value = "01-01-2026", label = "Visit Date", validation = validation)
        val result = FieldValidator.validate(field, null, context = ctx)
        assertFalse(result.isValid)
        assertEquals("Visit Date too late", result.errorMessage)
    }
}
