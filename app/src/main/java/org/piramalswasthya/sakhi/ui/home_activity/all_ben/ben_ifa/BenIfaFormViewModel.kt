package org.piramalswasthya.sakhi.ui.home_activity.all_ben.ben_ifa

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.ConditionalLogic
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FieldValidation
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.model.BottleItem
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.ben_ifa.BenIfaFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.dynamicRepo.BenIfaFormRepository
import org.piramalswasthya.sakhi.work.dynamicWoker.BenIfaFormSyncWorker
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BenIfaFormViewModel @Inject constructor(
    private val repository: BenIfaFormRepository,
    @ApplicationContext private val context: Context
) : ViewModel()  {

    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema
    private val _infant = MutableStateFlow<BenIfaFormResponseJsonEntity?>(null)
    val infant: StateFlow<BenIfaFormResponseJsonEntity?> = _infant
    var previousVisitDate: Date? = null
    private val _syncedVisitList = MutableStateFlow<List<BenIfaFormResponseJsonEntity>>(emptyList())

    var visitDay: String = ""
    private var isViewMode: Boolean = false
    private val _isBenDead = MutableStateFlow(false)
    val isBenDead: StateFlow<Boolean> = _isBenDead

    private val _benIdList = MutableLiveData<List<Long>>()
    val benIdList: LiveData<List<Long>> = _benIdList

    private val _bottleList = MutableLiveData<List<BottleItem>>()
    val bottleList: LiveData<List<BottleItem>> = _bottleList
    private val _canAddNewVisit = MutableLiveData<Boolean>()
    val canAddNewVisit: LiveData<Boolean> get() = _canAddNewVisit

    fun loadBottleData(benId: Long, formId: String) {
        viewModelScope.launch {
            val list = repository.getBottleList(benId, formId)
            _bottleList.postValue(list)
        }
    }

    fun checkIfCanAdd(benId: Long) {
        viewModelScope.launch {
            val canAdd = repository.canAddNewVisit(benId)
            _canAddNewVisit.postValue(canAdd)
        }
    }

    fun loadSyncedVisitList(benId: Long) {
        viewModelScope.launch {
            val list = repository.getSyncedVisitsByRchId(benId)
            _syncedVisitList.value = list

        }
    }
    fun loadFormSchema(
        benId: Long,
        formId: String,
        viewMode: Boolean,
        visitDate: String
    ) {
        this.isViewMode = viewMode
        loadSyncedVisitList(benId)

        viewModelScope.launch {
            val cachedSchemaEntity = repository.getSavedSchema(formId)
            val cachedSchema: FormSchemaDto? = cachedSchemaEntity?.let {
                FormSchemaDto.fromJson(it.schemaJson)
            }

            val localSchemaToRender = cachedSchema ?: repository.getFormSchema(formId) ?: return@launch

            val savedJson = repository.loadFormResponseJson(benId,visitDate)
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

            val allFields = localSchemaToRender.sections.flatMap { it.fields.orEmpty() }
            localSchemaToRender.sections.orEmpty().forEach { section ->
                section.fields.orEmpty().forEach { field ->
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

            localSchemaToRender.sections.orEmpty().forEach { section ->
                section.fields.orEmpty().forEach { field ->
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

    suspend fun saveFormResponses(benId: Long, hhId: Long) : Boolean {
        return try{
            val currentSchema = _schema.value ?: return false
            val formId = currentSchema.formId
            val version = currentSchema.version
            val beneficiaryId = benId

            val fieldMap = currentSchema.sections.orEmpty()
                .flatMap { it.fields.orEmpty() }
                .filter { it.visible && it.value != null }
                .associate { it.fieldId to it.value }

            val visitDate = fieldMap["visit_date"]?.toString() ?: "N/A"

            val wrappedJson = JSONObject().apply {
                put("formId", formId)
                put("beneficiaryId", beneficiaryId)
                put("houseHoldId", hhId)
                put("visitDate", visitDate)
                put("fields", JSONObject(fieldMap))
            }

            val entity = BenIfaFormResponseJsonEntity(
                benId = benId,
                hhId = hhId,
                visitDate = visitDate,
                formId = formId,
                version = version,
                formDataJson = wrappedJson.toString(),
                isSynced = false,
                syncedAt = null
            )

            repository.insertFormResponse(entity)
            loadSyncedVisitList(benId)
            BenIfaFormSyncWorker.enqueue(context)


            true
        }catch (e: Exception){
            e.printStackTrace()
            false
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

    fun getMinVisitDate(): Date {
        return Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }.time
    }
}