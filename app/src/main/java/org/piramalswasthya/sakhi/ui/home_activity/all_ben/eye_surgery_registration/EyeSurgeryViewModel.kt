package org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.ConditionalLogic
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FieldValidation
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EyeSurgeryViewModel @Inject constructor(
    private val repository: FormRepository,
    private val benRepo: BenRepo
) : ViewModel() {

    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema

    private val _infant = MutableStateFlow<FormResponseJsonEntityHBYC?>(null)
    val infant: StateFlow<FormResponseJsonEntityHBYC?> = _infant

    private val _syncedVisitList = MutableStateFlow<List<FormResponseJsonEntityHBYC>>(emptyList())
    private var benId: Long = 0L
    private var hhId: Long = 0L
    private var isViewMode: Boolean = false

    private val _isBenDead = MutableStateFlow(false)
    val isBenDead: StateFlow<Boolean> = _isBenDead

    var previousVisitDate: Date? = null


    fun loadSyncedVisitList(benId: Long, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _syncedVisitList.value = repository.getSyncedVisitsByRchIdHBYC(benId)
            onComplete?.invoke()
        }
    }

    fun loadFormSchema(
        benId: Long,
        formId: String,
        viewMode: Boolean,
    ) {
        this.isViewMode = viewMode

        loadSyncedVisitList(benId) {
            viewModelScope.launch {
                val cachedSchemaEntity = repository.getSavedSchema(formId)
                val cachedSchema: FormSchemaDto? = cachedSchemaEntity?.let { FormSchemaDto.fromJson(it.schemaJson) }

                val localSchemaToRender = cachedSchema ?: repository.getFormSchema(formId) ?: return@launch

                val allFields = localSchemaToRender.sections.flatMap { it.fields.orEmpty() }

                localSchemaToRender.sections.orEmpty().forEach { section ->
                    section.fields.orEmpty().forEach { field ->
                        field.visible = evaluateFieldVisibility(field, allFields)
                    }
                }

                val submittedMonths = _syncedVisitList.value.map { it.visitDay }.toSet()
                localSchemaToRender.sections.orEmpty().forEach { section ->
                    section.fields.orEmpty().forEach { field ->
                        if (field.fieldId == "visit_day" && field.options != null) {
                            field.options = field.options!!.filter {!submittedMonths.contains(it) }.toMutableList()
                        }
                    }
                }

                _schema.value = localSchemaToRender
            }
        }
    }

    private fun evaluateFieldVisibility(field: FormFieldDto, allFields: List<FormFieldDto>): Boolean {
        val cond = field.conditional
        return if (cond != null && !cond.dependsOn.isNullOrBlank()) {
            val dependsOnField = allFields.find { it.fieldId == cond.dependsOn }
            val dependsOnValue = dependsOnField?.value?.toString() ?: dependsOnField?.defaultValue?.toString()
            dependsOnValue.equals(cond.expectedValue, ignoreCase = true)
        } else true
    }

    fun updateFieldValue(fieldId: String, value: Any?) {
        val currentSchema = _schema.value ?: return
        val allFields = currentSchema.sections.flatMap { it.fields }
        allFields.find { it.fieldId == fieldId }?.value = value
        allFields.forEach { field -> field.visible = evaluateFieldVisibility(field, allFields) }
        _schema.value = currentSchema.copy()
    }

    suspend fun saveFormResponses(benId: Long, hhId: Long) {
        val currentSchema = _schema.value ?: return
        val formId = currentSchema.formId
        val version = currentSchema.version
        val fieldMap = currentSchema.sections.orEmpty()
            .flatMap { it.fields.orEmpty() }
            .filter { it.visible && it.value != null }
            .associate { it.fieldId to it.value }

        val visitDate = fieldMap["visit_date"]?.toString() ?: "N/A"

        val wrappedJson = JSONObject().apply {
            put("formId", formId)
            put("beneficiaryId", benId)
            put("houseHoldId", hhId)
            put("visitDate", visitDate)
            put("fields", JSONObject(fieldMap))
        }

        val entity = FormResponseJsonEntityHBYC(
            benId = benId,
            hhId = hhId,
            visitDay = "",
            visitDate = visitDate,
            formId = formId,
            version = version,
            formDataJson = wrappedJson.toString(),
            isSynced = false,
            syncedAt = null
        )

        repository.insertFormResponseHBYC(entity)
        loadSyncedVisitList(benId)
    }

    fun getVisibleFields(): List<FormField> {
        return _schema.value?.sections?.flatMap { section ->
            section.fields.filter { it.visible }.map { field ->
                FormField(
                    fieldId = field.fieldId,
                    label = field.label,
                    type = field.type,
                    options = field.options,
                    isRequired = field.required,
                    placeholder = field.placeholder,
                    validation = field.validation?.let {
                        FieldValidation(
                            min = it.min,
                            max = it.max,
                            maxLength = it.maxLength,
                            regex = it.regex,
                            errorMessage = it.errorMessage,
                            decimalPlaces = it.decimalPlaces,
                            maxSizeMB = it.maxSizeMB,
                            afterField = it.afterField,
                            beforeField = it.beforeField
                        )
                    },
                    visible = field.visible,
                    conditional = field.conditional?.let {
                        if (!it.dependsOn.isNullOrBlank() && !it.expectedValue.isNullOrBlank())
                            ConditionalLogic(dependsOn = it.dependsOn, expectedValue = it.expectedValue)
                        else null
                    },
                    value = field.value
                )
            }
        } ?: emptyList()
    }


    fun getMaxVisitDate(): Date {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val alreadyFilledDates = _syncedVisitList.value.mapNotNull {
            try {
                val json = JSONObject(it.formDataJson)
                val fields = json.optJSONObject("fields")
                val dateStr = fields?.optString("visit_date")
                if (!dateStr.isNullOrBlank()) SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr) else null
            } catch (e: Exception) { null }
        }
        return if (alreadyFilledDates.contains(today)) Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time else today
    }

    fun getMinVisitDate(): Date? {
        return previousVisitDate?.let { prev ->
            Calendar.getInstance().apply {
                time = prev
                add(Calendar.DATE, 1)
            }.time
        }
    }
}