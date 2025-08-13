package org.piramalswasthya.sakhi.utils.dynamicFiledValidator

import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import java.text.SimpleDateFormat
import java.util.*

data class FieldValidationConfig(
    val fieldId: String,
    val customMessages: Map<String, String> = emptyMap()
)

object FieldValidator {

    data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null)

    private val fieldConfigs = mapOf(
        "due_date" to FieldValidationConfig(
            fieldId = "due_date",
            customMessages = mapOf(
                "minDate" to "cannot be before Date of Delivery",
                "maxDate" to "cannot be after today"
            )
        )
    )

    fun validate(field: FormField, dob: String?, todayStr: String = getToday()): ValidationResult {
        if (field.isRequired && (field.value == null || field.value.toString().isBlank())) {
            return ValidationResult(false, "${field.label} is required")
        }

        val valueStr = field.value.toString().trim()
        val rules = field.validation ?: return ValidationResult(true)

        return when (field.type) {
            "text" -> {
                val regex = rules.regex
                if (regex != null && !regex.toRegex().matches(valueStr)) {
                    ValidationResult(false, rules.errorMessage ?: "${field.label} is invalid")
                } else {
                    ValidationResult(true)
                }
            }

            "number" -> {
                val num = valueStr.toFloatOrNull()
                    ?: return ValidationResult(false, "${field.label} must be a number")

                val min = rules.min
                val max = rules.max

                if (min != null && num < min)
                    return ValidationResult(false, "${field.label} should be at least $min")

                if (max != null && num > max)
                    return ValidationResult(false, "${field.label} should be at most $max")

                ValidationResult(true)
            }

            "date" -> {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val valueDate = runCatching { sdf.parse(valueStr) }.getOrNull()
                    ?: return ValidationResult(false, "${field.label} date is invalid")

                val minDate = when (rules.minDate) {
                    "dob" -> dob?.let { runCatching { sdf.parse(it) }.getOrNull() }
                    "today" -> runCatching { sdf.parse(todayStr) }.getOrNull()
                    else -> rules.minDate?.let { runCatching { sdf.parse(it) }.getOrNull() }
                }

                val maxDate = when (rules.maxDate) {
                    "dob" -> dob?.let { runCatching { sdf.parse(it) }.getOrNull() }
                    "today" -> runCatching { sdf.parse(todayStr) }.getOrNull()
                    else -> rules.maxDate?.let { runCatching { sdf.parse(it) }.getOrNull() }
                }

                if (minDate != null && valueDate.before(minDate)) {
                    val customMessage = fieldConfigs[field.fieldId]?.customMessages?.get("minDate")
                    return ValidationResult(
                        false,
                        customMessage?.let { "${field.label} $it" }
                            ?: "${field.label} cannot be before ${rules.minDate}"

                    )
                }

                if (maxDate != null && valueDate.after(maxDate)) {
                    val customMessage = fieldConfigs[field.fieldId]?.customMessages?.get("minDate")
                    return ValidationResult(
                        false,
                        customMessage?.let { "${field.label} $it" }
                            ?: "${field.label} cannot be after ${rules.maxDate}"
                    )
                }

                ValidationResult(true)
            }

            else -> ValidationResult(true)
        }
    }

    private fun getToday(): String {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    }
}
