package org.piramalswasthya.sakhi.configuration.dynamicDataSet

data class FormField(
    val fieldId: String,
    val label: String,
    val type: String,
    var defaultValue: String? = null,
    val isRequired: Boolean,
    val options: List<String>? = null,
    var value: Any? = null,
    var visible: Boolean = true,
    val conditional: ConditionalLogic? = null,
    var errorMessage: String? = null,
    val placeholder: String? = null,

    val validation: FieldValidation? = null,
    val isEditable: Boolean = true
)

data class ConditionalLogic(
    val dependsOn: String,
    val expectedValue: String
)

data class FieldValidation(
    val min: Float? = null,
    val max: Float? = null,
    val minDate: String? = null,
    val maxDate: String? = null,

    val maxLength: Int? = null,
    val regex: String? = null,
    val errorMessage: String? = null,

    val decimalPlaces: Int? = null,

    val maxSizeMB: Int? = null,

    val afterField: String? = null,
    val beforeField: String? = null
)


