package org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration

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
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.optionItems
import org.piramalswasthya.sakhi.repositories.dynamicRepo.EyeSurgeryFormRepository
import org.piramalswasthya.sakhi.work.dynamicWoker.EyeSurgeryFormSyncWorker
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EyeSurgeryFormViewModel @Inject constructor(
    private val repository: EyeSurgeryFormRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema
    private val _infant = MutableStateFlow<EyeSurgeryFormResponseJsonEntity?>(null)
    val infant: StateFlow<EyeSurgeryFormResponseJsonEntity?> = _infant
    var previousVisitDate: Date? = null
    private val _syncedVisitList = MutableStateFlow<List<EyeSurgeryFormResponseJsonEntity>>(emptyList())

    var visitDay: String = ""
    private var isViewMode: Boolean = false
    private val _isBenDead = MutableStateFlow(false)
    val isBenDead: StateFlow<Boolean> = _isBenDead

    private val _benIdList = MutableLiveData<List<Long>>()
    val benIdList: LiveData<List<Long>> = _benIdList

    fun loadAllBenIds() {
        viewModelScope.launch {
            val ids = repository.getAllBenIds()
            _benIdList.postValue(ids)
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
        loadSavedData: Boolean = true,
        eyeSide: String = ""
    ) {
        this.isViewMode = viewMode
        loadSyncedVisitList(benId)

        viewModelScope.launch {
            val cachedSchemaEntity = repository.getSavedSchema(formId)
            val localSchemaToRender = cachedSchemaEntity?.let {
                FormSchemaDto.fromJson(it.schemaJson)
            } ?: repository.getFormSchema(formId) ?: return@launch

            val savedFieldValues = if (loadSavedData) {
                val savedJson = repository.loadFormResponseJson(benId, formId)
                if (!savedJson.isNullOrBlank()) {
                    try {
                        val root = JSONObject(savedJson)
                        val fieldsJson = root.optJSONObject("fields") ?: JSONObject()
                        fieldsJson.keys().asSequence().associateWith { fieldsJson.opt(it) }
                    } catch (e: Exception) { emptyMap() }
                } else emptyMap()
            } else {
                emptyMap()
            }


            val allFields = localSchemaToRender.sections.flatMap { it.fields.orEmpty() }

            allFields.forEach {
                Log.d("EyeVM", "fieldId=${it.fieldId} type=${it.type} options=${it.options}")
            }

            localSchemaToRender.sections.orEmpty().forEach { section ->
                section.fields.orEmpty().forEach { field ->
                    field.value = when {
                        field.fieldId == "visit_day" -> visitDay

                        field.fieldId == "eye_affected" && eyeSide.isNotEmpty() && !loadSavedData -> {
                            val matchedOption = field.options?.find {
                                it.equals(eyeSide, ignoreCase = true)
                            } ?:
                            eyeSide
                            matchedOption
                        }

                        else -> {
                            if (loadSavedData) {
                                savedFieldValues[field.fieldId] ?: field.default
                            } else {
                                null
                            }
                        }
                    }

                    field.isEditable = when {
                        field.fieldId == "visit_day" || field.fieldId == "due_date" -> false
                        field.fieldId == "eye_affected" && eyeSide.isNotEmpty() && !loadSavedData -> false
                        else -> !viewMode
                    }

                    if (field.fieldId == "eye_affected") {
                        Log.d("EyeVM", "eye_affected: value=${field.value} isEditable=${field.isEditable} options=${field.options}")
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

    fun loadFormSchemaFromJson(
        benId: Long,
        formId: String,
        isViewMode: Boolean,
        formDataJson: String
    ) {
        this.isViewMode = isViewMode
        loadSyncedVisitList(benId)

        viewModelScope.launch {
            val cachedSchemaEntity = repository.getSavedSchema(formId)
            val cachedSchema: FormSchemaDto? = cachedSchemaEntity?.let {
                FormSchemaDto.fromJson(it.schemaJson)
            }
            val localSchema = cachedSchema ?: repository.getFormSchema(formId) ?: return@launch

            val savedValues = try {
                val root = JSONObject(formDataJson)
                val fieldsJson = root.optJSONObject("fields") ?: JSONObject()
                fieldsJson.keys().asSequence().associateWith { fieldsJson.opt(it) }
            } catch (e: Exception) {
                emptyMap<String, Any?>()
            }

            val allFields = localSchema.sections.flatMap { it.fields.orEmpty() }

            localSchema.sections.orEmpty().forEach { section ->
                section.fields.orEmpty().forEach { field ->
                    field.value = savedValues[field.fieldId] ?: field.default
                    field.isEditable = when (field.fieldId) {
                        "visit_day", "due_date" -> false
                        "eye_affected" -> false
                        else -> !isViewMode
                    }
                }
            }

            localSchema.sections.orEmpty().forEach { section ->
                section.fields.orEmpty().forEach { field ->
                    field.visible = evaluateFieldVisibility(field, allFields)
                }
            }

            _schema.value = localSchema
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

    private fun toMonthKey(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return ""
        val inputs = listOf("dd-MM-yyyy", "yyyy-MM-dd")
        val out = SimpleDateFormat("yyyy-MM", Locale.ENGLISH)
        for (fmt in inputs) {
            try {
                val d = SimpleDateFormat(fmt, Locale.ENGLISH).parse(dateStr)
                if (d != null) return out.format(d)
            } catch (_: Exception) {}
        }
        return try {
            if (Regex("\\d{2}-\\d{2}-\\d{4}").matches(dateStr)) {
                val yyyy = dateStr.substring(6, 10)
                val mm = dateStr.substring(3, 5)
                "$yyyy-$mm"
            } else ""
        } catch (_: Exception) { "" }
    }

    suspend fun saveFormResponses(
        benId: Long,
        hhId: Long,
        eyeSide: String = "",
        recordId: Int = 0
    ): Boolean {
        return try {
            val currentSchema = _schema.value ?: return false
            val formId = currentSchema.formId
            val version = currentSchema.version

            val fieldMap = currentSchema.sections.orEmpty()
                .flatMap { it.fields.orEmpty() }
                .filter { it.visible && it.value != null }
                .associate { it.fieldId to it.value }

            val visitDate = fieldMap["visit_date"]?.toString() ?: "N/A"
            val visitMonth = toMonthKey(visitDate)

            val wrappedJson = JSONObject().apply {
                put("formId", formId)
                put("beneficiaryId", benId)
                put("houseHoldId", hhId)
                put("visitDate", visitDate)
                put("eyeSide", eyeSide)
                put("fields", JSONObject(fieldMap))
            }

            val entity = EyeSurgeryFormResponseJsonEntity(
                id = if (recordId > 0) recordId else 0,
                benId = benId,
                hhId = hhId,
                visitDate = visitDate,
                visitMonth = visitMonth,
                eyeSide = eyeSide.uppercase(),
                formId = formId,
                version = version,
                formDataJson = wrappedJson.toString(),
                isSynced = false,
                syncedAt = null
            )
            val referredTo = fieldMap["referred_to"]?.toString()
            val referralReason = fieldMap["symptoms_observed"]?.toString()
            if (!referredTo.isNullOrBlank() && !referralReason.isNullOrBlank()) {
                repository.saveReferral(benId, referredTo,referralReason)
            }
            repository.upsertByEye(entity)
            EyeSurgeryFormSyncWorker.enqueue(context)
            true
        } catch (e: Exception) {
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
                    options = field.optionItems(),
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
                    value = field.value,
                    isEditable = field.isEditable

                )
            }
        } ?: emptyList()
    }

    fun getMaxVisitDate(): Date {
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = todayCal.time
        val todayMonthKey = SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(today)

        val alreadyInThisMonth = _syncedVisitList.value.any {
            it.visitMonth == todayMonthKey
        }
        return if (alreadyInThisMonth) {
            todayCal.apply { add(Calendar.DATE, -1) }.time
        } else today
    }

    fun getMinVisitDate(): Date? {
        return previousVisitDate?.let {
            Calendar.getInstance().apply {
                time = it
                add(Calendar.DATE, 1)
            }.time
        }
    }
}