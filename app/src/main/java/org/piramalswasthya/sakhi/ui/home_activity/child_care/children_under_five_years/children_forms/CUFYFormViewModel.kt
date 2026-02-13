package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years.children_forms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.ConditionalLogic
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FieldValidation
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.model.BottleItem
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.CUFYFormRepository
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CUFYFormViewModel @Inject constructor(
    private val repository: CUFYFormRepository,
) : ViewModel() {

    sealed class SaveFormState {
        object Idle : SaveFormState()
        object Loading : SaveFormState()
        object Success : SaveFormState()
        data class Error(val message: String) : SaveFormState()
    }

    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema
    private val _infant = MutableStateFlow<CUFYFormResponseJsonEntity?>(null)
    val infant: StateFlow<CUFYFormResponseJsonEntity?> = _infant
    var previousVisitDate: Date? = null
    private val _syncedVisitList = MutableStateFlow<List<CUFYFormResponseJsonEntity>>(emptyList())

    var visitDay: String = ""
    private var isViewMode: Boolean = false

    private val _saveFormState = MutableLiveData<SaveFormState>(SaveFormState.Idle)
    val saveFormState: LiveData<SaveFormState> = _saveFormState


    private val _isBenDead = MutableStateFlow(false)
    val isBenDead: StateFlow<Boolean> = _isBenDead

    private var existingRecordId: Int = 0

    private val _bottleList = MutableLiveData<List<BottleItem>>()
    val bottleList: LiveData<List<BottleItem>> = _bottleList

    fun loadBottleData(benId: Long, formId: String) {
        viewModelScope.launch {
            val list = repository.getBottleList(benId, formId)
            _bottleList.postValue(list)
        }
    }

    fun setRecordId(recordId: Int) {
        existingRecordId = recordId
    }

    fun loadSyncedVisitList(benId: Long) {
        viewModelScope.launch {
            val list = repository.getSyncedVisitsByRchId(benId)
            _syncedVisitList.value = list

        }
    }

    fun loadFormSchemaFromJson(
        benId: Long,
        formId: String,
        visitDay: String,
        isViewMode: Boolean,
        formDataJson: String
    ) {
        this.visitDay = visitDay
        this.isViewMode = isViewMode

        viewModelScope.launch {
            val cachedSchemaEntity = repository.getSavedSchema(formId)
            val cachedSchema: FormSchemaDto? = cachedSchemaEntity?.let {
                FormSchemaDto.fromJson(it.schemaJson)
            }
            val localSchema = cachedSchema ?: repository.getFormSchema(formId)
            if (localSchema == null) return@launch

            val savedValues = try {
                val root = JSONObject(formDataJson)
                val fieldsJson = root.optJSONObject("fields") ?: JSONObject()
                fieldsJson.keys().asSequence().associateWith { fieldsJson.opt(it) }
            } catch (e: Exception) {
                emptyMap<String, Any?>()
            }

            val allFields = localSchema.sections.flatMap { it.fields }

            localSchema.sections.forEach { section ->
                section.fields.forEach { field ->
                    field.value = when (formId) {

                        FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID -> {
                            when (field.type) {
                                "number" -> {
                                    val rawValue = savedValues[field.fieldId]
                                    when (rawValue) {
                                        is Number -> rawValue
                                        is String -> rawValue.toDoubleOrNull()
                                        else -> null
                                    } ?: field.defaultValue
                                }
                                "date" -> savedValues[field.fieldId]?.toString() ?: ""
                                else -> savedValues[field.fieldId] ?: field.defaultValue
                            }
                        }

                        FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID -> {
                            when (field.type) {
                                "image" -> {
                                    val uriValue = savedValues[field.fieldId]?.toString()
                                    if (!uriValue.isNullOrEmpty() && uriValue.startsWith("content://")) {
                                        uriValue
                                    } else ""
                                }
                                "date" -> savedValues[field.fieldId]?.toString() ?: ""
                                "number" -> (savedValues[field.fieldId] as? Number)
                                    ?: (savedValues[field.fieldId]?.toString()?.toDoubleOrNull())
                                    ?: field.defaultValue
                                else -> savedValues[field.fieldId] ?: field.defaultValue
                            }
                        }

                        FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID -> {
                            val value = savedValues[field.fieldId] ?: field.defaultValue
                            when (field.type) {
                                "image", "view_image" -> {
                                    val uriValue = value?.toString() ?: ""
                                    if (uriValue.startsWith("content://")) uriValue else ""
                                }
                                else -> value
                            }
                        }

                        else -> savedValues[field.fieldId] ?: field.defaultValue
                    }


                    field.isEditable = when (field.fieldId) {
                        "visit_day", "due_date" -> false
                        else -> !isViewMode
                    }
                }
            }


            localSchema.sections.forEach { section ->
                section.fields.forEach { field ->
                    field.visible = evaluateFieldVisibility(field, allFields)
                }
            }

            _schema.value = localSchema
        }
    }


    fun loadFormSchema(
        benId: Long,
        formId: String,
        visitDay: String,
        viewMode: Boolean
    ) {
        this.visitDay = visitDay
        this.isViewMode = viewMode

        viewModelScope.launch {
            val cachedSchemaEntity = repository.getSavedSchema(formId)
            val cachedSchema: FormSchemaDto? = cachedSchemaEntity?.let {
                FormSchemaDto.fromJson(it.schemaJson)
            }
            val localSchemaToRender = cachedSchema ?: repository.getFormSchema(formId)?.also {
            }

            if (localSchemaToRender == null) {
                return@launch
            }


            val savedJson = repository.loadFormResponseJson(benId, visitDay)
            val savedFieldValues = if (!savedJson.isNullOrBlank()) {
                try {
                    val root = JSONObject(savedJson)
                    val fieldsJson = root.optJSONObject("fields") ?: JSONObject()
                    fieldsJson.keys().asSequence().associateWith { fieldsJson.opt(it) }
                } catch (e: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }

            val allFields = localSchemaToRender.sections.flatMap { it.fields }
            localSchemaToRender.sections.forEach { section ->
                section.fields.forEach { field ->
                    field.value = when (field.fieldId) {
                        "visit_day" -> visitDay
                        else -> savedFieldValues[field.fieldId] ?: field.default
                    }

                    field.isEditable = when (field.fieldId) {
                        "visit_day", "due_date" -> false
                        else -> !viewMode
                    }
                }
            }

            localSchemaToRender.sections.forEach { section ->
                section.fields.forEach { field ->
                    field.visible = evaluateFieldVisibility(field, allFields)
                }
            }
            _schema.value = localSchemaToRender
        }
    }

    private fun evaluateFieldVisibility(
        field: FormFieldDto,
        allFields: List<FormFieldDto>
    ): Boolean {
        val cond = field.conditional
        return if (cond != null && !cond.dependsOn.isNullOrBlank()) {
            val dependsOnField = allFields.find { it.fieldId == cond.dependsOn }
            val dependsOnValue = dependsOnField?.value?.toString()
            dependsOnValue.equals(cond.expectedValue, ignoreCase = true)
        } else true
    }

    fun updateFieldValue(fieldId: String, value: Any?) {
        val currentSchema = _schema.value ?: return
        val allFields = currentSchema.sections.flatMap { it.fields }

        allFields.find { it.fieldId == fieldId }?.value = value

        allFields.forEach { field ->
            field.visible = evaluateFieldVisibility(field, allFields)
        }

        _schema.value = currentSchema.copy()
    }

suspend fun saveFormResponses(benId: Long, hhId: Long, recordId: Int = 0) {

    val currentSchema = _schema.value ?: run {
        return
    }

    val formId = currentSchema.formId
    val version = currentSchema.version

    _saveFormState.postValue(SaveFormState.Loading)

    try {
        val fieldMap = currentSchema.sections
            .flatMap { it.fields }
            .filter { it.visible && it.value != null }
            .associate { it.fieldId to it.value }

        val rawVisitDate = fieldMap["visit_date"]?.toString() ?: "N/A"
        val visitDate = try {

            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = inputFormat.parse(rawVisitDate)
            if (parsedDate != null) outputFormat.format(parsedDate) else rawVisitDate
        } catch (e: Exception) {
            Timber.tag("CUFYFormVM").e(e, " Date format conversion failed for: $rawVisitDate")
            rawVisitDate
        }
        val fieldsJson = JSONObject()
        fieldMap.forEach { (fieldId, value) ->
            if (fieldId == "follow_up_visit_date") {

                val followUpArray = JSONArray()
                if (value != null && value.toString().isNotBlank()) {
                    val dates = if (value.toString().contains(",")) {
                        value.toString().split(",").map { it.trim() }
                    } else {
                        listOf(value.toString())
                    }
                    dates.forEach { date ->
                        if (date.isNotBlank()) {
                            followUpArray.put(date)
                        }
                    }
                }
                fieldsJson.put(fieldId, followUpArray)
            } else {

                when (value) {
                    is Number -> fieldsJson.put(fieldId, value)
                    is String -> fieldsJson.put(fieldId, value)
                    is Boolean -> fieldsJson.put(fieldId, value)
                    is JSONArray->fieldsJson.put(fieldId,value)
                    else -> fieldsJson.put(fieldId, value?.toString() ?: "")
                }
            }
        }

        val wrappedJson = JSONObject().apply {
            put("formId", formId)
            put("beneficiaryId", benId)
            put("houseHoldId", hhId)
            put("visitDate", visitDate)
            put("fields", fieldsJson)
        }

        val currentTime = System.currentTimeMillis()

        val entity = CUFYFormResponseJsonEntity(
            id = if (recordId > 0) recordId else 0,
            benId = benId,
            hhId = hhId,
            visitDate = visitDate,
            formId = formId,
            version = version,
            formDataJson = wrappedJson.toString(),
            isSynced = false,
            syncedAt = null,
            createdAt = currentTime,
            updatedAt = currentTime
        )

        val insertedId = repository.insertFormResponse(entity)

        loadSyncedVisitList(benId)

        _saveFormState.postValue(SaveFormState.Success)

    } catch (e: Exception) {
        _saveFormState.postValue(SaveFormState.Error(e.localizedMessage ?: "Failed to save form"))
    }
}

    fun getVisibleFields(): List<FormField> {
        return _schema.value?.sections?.flatMap { section ->
            section.fields.filter { it.visible }.map { field ->
                FormField(
                    fieldId = field.fieldId,
                    label = field.label,
                    type = field.type,
                    defaultValue = field.defaultValue,
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
                        if (!it.dependsOn.isNullOrBlank() && !it.expectedValue.isNullOrBlank()) {
                            ConditionalLogic(
                                dependsOn = it.dependsOn,
                                expectedValue = it.expectedValue
                            )
                        } else null
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
                if (!dateStr.isNullOrBlank()) {
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr)
                } else null
            } catch (e: Exception) {
                null
            }
        }
        return if (alreadyFilledDates.contains(today)) {
            Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time
        } else {
            today
        }
    }

    fun getMinVisitDate(): Date? {
        return previousVisitDate?.let {
            Calendar.getInstance().apply {
                time = it
                add(Calendar.DATE, 1)
            }.time
        }
    }

    suspend fun getFormsDataByFormID(formId: String, benId: Long): List<CUFYFormResponseJsonEntity> {
        return repository.getSavedDataByFormId(formId, benId)
    }
}
