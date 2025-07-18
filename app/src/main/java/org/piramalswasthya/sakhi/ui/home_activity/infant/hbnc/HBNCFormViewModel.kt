package org.piramalswasthya.sakhi.ui.home_activity.infant.hbnc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.hbncschemademo.ui.repo.FormRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.ConditionalLogic
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FieldValidation
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.InfantEntity
import org.piramalswasthya.sakhi.model.dynamicModel.VisitCard
import org.piramalswasthya.sakhi.work.dynamicWoker.FormSyncWorker
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HBNCFormViewModel @Inject constructor(private val repository: FormRepository) : ViewModel() {

    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema

    private val _infant = MutableStateFlow<FormResponseJsonEntity?>(null)
    val infant: StateFlow<FormResponseJsonEntity?> = _infant

    private val _syncedVisitList = MutableStateFlow<List<FormResponseJsonEntity>>(emptyList())
    val syncedVisitList: StateFlow<List<FormResponseJsonEntity>> = _syncedVisitList

    private var rchId: String = ""
    private var benId: Long = 0L
    private var hhId: Long = 0L
    var visitDay: String = ""
    private var isViewMode: Boolean = false

    fun loadSyncedVisitList(benId: Long) {
        viewModelScope.launch {
            val list = repository.getSyncedVisitsByRchId(benId)
            _syncedVisitList.value = list
        }
    }

    fun loadFormSchema(formId: String, visitDay: String, viewMode: Boolean) {
        this.visitDay = visitDay
        this.isViewMode = viewMode

        viewModelScope.launch {
            val apiSchema = repository.getFormSchema(formId)
            if (apiSchema == null) return@launch

            val savedJson = repository.loadFormResponseJson(benId, visitDay)
            val savedFieldValues: Map<String, Any?> = try {
                val root = JSONObject(savedJson ?: "")
                val fieldsJson = root.optJSONObject("fields") ?: JSONObject()
                fieldsJson.keys().asSequence().associateWith { fieldsJson.opt(it) }
            } catch (e: Exception) {
                emptyMap()
            }

            val allFields = apiSchema.sections.flatMap { it.fields.orEmpty() }

            apiSchema.sections.orEmpty().forEach { section ->
                section.fields.orEmpty().forEach { field ->
                    field.value = when (field.fieldId) {
                        "visit_day" -> visitDay
                        else -> savedFieldValues[field.fieldId] ?: field.defaultValue
                    }
                    field.isEditable = field.fieldId != "visit_day" && !isViewMode
                }
            }

            apiSchema.sections.orEmpty().forEach { section ->
                section.fields.orEmpty().forEach { field ->
                    field.visible = evaluateFieldVisibility(field, allFields)
                }
            }

            _schema.value = apiSchema
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

    fun saveFormResponses() {
        viewModelScope.launch {
            val currentSchema = _schema.value ?: return@launch
            val formId = currentSchema.formId
            val version = currentSchema.version
            val beneficiaryId = rchId.toIntOrNull() ?: 0
            val visitDate = calculateDueDate(_infant.value?.visitDay ?: "", visitDay) ?: "2025-07-10"

            val fieldMap = currentSchema.sections.orEmpty()
                .flatMap { it.fields.orEmpty() }
                .filter { it.visible && it.value != null }
                .associate { it.fieldId to it.value }

            val wrappedJson = JSONObject().apply {
                put("formId", formId)
                put("beneficiaryId", beneficiaryId)
                put("visitDate", visitDate)
                put("fields", JSONObject(fieldMap))
            }

            val entity = FormResponseJsonEntity(
                benId = benId,
                hhId = hhId,
                visitDay = visitDay,
                formId = formId,
                version = version,
                formDataJson = wrappedJson.toString(),
                isSynced = false,
                syncedAt = null
            )

            repository.insertFormResponse(entity)
            loadSyncedVisitList(benId)
        }
    }

    fun loadInfant(benId: Long, hhId: Long) {
        this.benId = benId
        this.hhId = hhId
        viewModelScope.launch {
            _infant.value = repository.getInfantByRchId(benId).firstOrNull()
        }
    }

    fun isVisitDayEnabled(day: String): Boolean {
        val history = _syncedVisitList.value.map { it.visitDay }
        return when (day) {
            "1st Day" -> "1st Day" !in history
            "3rd Day" -> "1st Day" in history && "3rd Day" !in history
            "7th Day" -> "3rd Day" in history && "7th Day" !in history
            "14th Day" -> "7th Day" in history && "14th Day" !in history
            "21st Day" -> "7th Day" in history && "21st Day" !in history
            "28th Day" -> "7th Day" in history && "28th Day" !in history
            "42nd Day" -> "7th Day" in history && "28th Day" in history && "42nd Day" !in history
            else -> false
        }
    }

    fun getNextEligibleVisitDay(): String? {
        val visitOrder = listOf("1st Day", "3rd Day", "7th Day", "14th Day", "21st Day", "28th Day", "42nd Day")
        val completed = _syncedVisitList.value.map { it.visitDay }

        return visitOrder.firstOrNull { day ->
            when (day) {
                "1st Day", "3rd Day", "7th Day" ->
                    day !in completed && previousDayCompleted(day, completed)
                "14th Day", "21st Day", "28th Day" ->
                    "7th Day" in completed && day !in completed
                "42nd Day" ->
                    "7th Day" in completed && "28th Day" in completed && day !in completed
                else -> false
            }
        }
    }

    fun calculateDueDate(dob: String, visitDay: String): String? {
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = sdf.parse(dob) ?: return null
            val calendar = Calendar.getInstance().apply { time = date }
            when (visitDay) {
                "1st Day" -> calendar.add(Calendar.DAY_OF_MONTH, 0)
                "3rd Day" -> calendar.add(Calendar.DAY_OF_MONTH, 2)
                "7th Day" -> calendar.add(Calendar.DAY_OF_MONTH, 6)
                "14th Day" -> calendar.add(Calendar.DAY_OF_MONTH, 13)
                "21st Day" -> calendar.add(Calendar.DAY_OF_MONTH, 20)
                "28th Day" -> calendar.add(Calendar.DAY_OF_MONTH, 27)
                "42nd Day" -> calendar.add(Calendar.DAY_OF_MONTH, 41)
            }
            sdf.format(calendar.time)
        } catch (e: Exception) {
            null
        }
    }

    fun syncUnsyncedForms() {
        viewModelScope.launch {
            val unsyncedForms = repository.getUnsyncedForms()
            for (form in unsyncedForms) {
                val success = repository.syncFormToServer(form)
                if (success) {
                    repository.markFormAsSynced(form.id)
                }
            }
        }
    }

    fun createFormSyncRequest(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<FormSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
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

    fun loadVisitHistory(rchId: String) {
        viewModelScope.launch {
            _syncedVisitList.value = repository.getSyncedVisitsByRchId(benId)
        }
    }

    fun getVisitCardList(): List<VisitCard> {
        val visitOrder = listOf("1st Day", "3rd Day", "7th Day", "14th Day", "21st Day", "28th Day", "42nd Day")
        val completed = _syncedVisitList.value.map { it.visitDay }.toSet()
        val dob = infant.value?.visitDay ?: "-"

        return visitOrder.map { day ->
            val isCompleted = completed.contains(day)
            val isEditable = when (day) {
                "1st Day" -> !isCompleted
                "3rd Day" -> !isCompleted && completed.contains("1st Day")
                "7th Day" -> !isCompleted && completed.contains("3rd Day")
                "14th Day", "21st Day", "28th Day" -> !isCompleted && completed.contains("7th Day")
                "42nd Day" -> !isCompleted && completed.contains("28th Day")
                else -> false
            }

            val visit = _syncedVisitList.value.find { it.visitDay == day }
            val visitDate = visit?.let {
                try {
                    val json = JSONObject(it.formDataJson)
                    json.optString("visitDate", "-")
                } catch (e: Exception) {
                    "-"
                }
            } ?: calculateDueDate(dob, day) ?: "-"

            VisitCard(
                visitDay = day,
                visitDate = visitDate,
                isCompleted = isCompleted,
                isEditable = isEditable
            )
        }
    }

    private fun previousDayCompleted(day: String, completed: List<String>): Boolean {
        return when (day) {
            "3rd Day" -> "1st Day" in completed
            "7th Day" -> "3rd Day" in completed
            else -> true
        }
    }
}
