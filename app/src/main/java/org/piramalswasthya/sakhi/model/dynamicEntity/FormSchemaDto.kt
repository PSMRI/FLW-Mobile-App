package org.piramalswasthya.sakhi.model.dynamicEntity

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class FormSchemaDto(
    @SerializedName("formId")
    val formId: String,

    @SerializedName("formName")
    val formName: String,

    @SerializedName("version")
    val version: Int = 1,

    @SerializedName("sections")
    val sections: List<FormSectionDto> = emptyList()
) {
    companion object {
        fun fromJson(json: String): FormSchemaDto = Gson().fromJson(json, FormSchemaDto::class.java)
    }

    fun toJson(): String = Gson().toJson(this)
}

data class FormSectionDto(
    @SerializedName("sectionId")
    val sectionId: String = "",

    @SerializedName("sectionTitle")
    val sectionTitle: String = "",

    @SerializedName("fields")
    val fields: List<FormFieldDto> = emptyList()
)

data class FormFieldDto(
    @SerializedName("fieldId")
    val fieldId: String = "",

    @SerializedName("label")
    val label: String = "",

    @SerializedName("type")
    val type: String = "",

    @SerializedName("options")
    val options: List<String>? = null,

    @SerializedName("isRequired")
    val required: Boolean = false,

    @SerializedName("conditional")
    val conditional: ConditionalLogic? = null,

    @SerializedName("validation")
    val validation: FieldValidationDto? = null,

    @SerializedName("placeholder")
    val placeholder: String? = null,

    @SerializedName("defaultValue")
    val defaultValue: String? = null,

    @SerializedName("default")
    val default: Any? = null,

    @SerializedName("value")
    var value: Any? = null,

    @Transient var visible: Boolean = true,
    @Transient var errorMessage: String? = null,
    @Transient var isEditable: Boolean = true
)

data class ConditionalLogic(
    @SerializedName("dependsOn")
    val dependsOn: String? = null,

    @SerializedName("expectedValue")
    val expectedValue: String? = null
)

data class FieldValidationDto(
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
    val beforeField: String? = null,
    val incremental: Boolean? = null
)

data class OptionItem(
    val label: String = "",
    val value: Any? = null
)
