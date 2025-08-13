package org.piramalswasthya.sakhi.configuration.dynamicDataSet

data class FormField(
    val fieldId: String,
    val label: String,
    val type: String,
    val isRequired: Boolean,
    val options: List<String>? = null,
    var value: Any? = null,
    var visible: Boolean = true,
    val conditional: ConditionalLogic? = null,
    var errorMessage: String? = null,
    // ✅ New field for input hint
    val placeholder: String? = null,

    // ✅ Replacing Map with structured validation object
    val validation: FieldValidation? = null,
    val isEditable: Boolean = true
)

data class ConditionalLogic(
    val dependsOn: String,
    val expectedValue: String
)

// ✅ New class for validation rules
data class FieldValidation(
    // For number fields
    val min: Float? = null,
    val max: Float? = null,
    // ✅ For date fields
    val minDate: String? = null,  // e.g. "dob", "today", or "01-01-2024"
    val maxDate: String? = null,

    // For text fields
    val maxLength: Int? = null,
    val regex: String? = null,
    val errorMessage: String? = null,

    // For number/text formatting
    val decimalPlaces: Int? = null,

    // For image fields
    val maxSizeMB: Int? = null,

    // For date/text cross-field logic
    val afterField: String? = null,
    val beforeField: String? = null
)


