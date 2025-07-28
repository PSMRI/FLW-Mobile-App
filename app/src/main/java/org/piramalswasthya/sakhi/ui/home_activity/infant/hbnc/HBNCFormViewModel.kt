package org.piramalswasthya.sakhi.ui.home_activity.infant.hbnc

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
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
    // Holds the delivery/birth date of the baby
    var lastVisitDay: String? = null

    // Holds the last submitted visit date (null if 1st visit)
    var previousVisitDate: Date? = null

    private val _syncedVisitList = MutableStateFlow<List<FormResponseJsonEntity>>(emptyList())
    val syncedVisitList: StateFlow<List<FormResponseJsonEntity>> = _syncedVisitList
    private val visitOrder = listOf("1st Day", "3rd Day", "7th Day", "14th Day", "21st Day", "28th Day", "42nd Day")

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


//    fun loadFormSchema(benId: Long, formId: String, visitDay: String, viewMode: Boolean,dob:Long) {
//
//        this.visitDay = visitDay
//        this.isViewMode = viewMode
//
//        viewModelScope.launch {
//            val apiSchema = repository.getFormSchema(formId)
//            if (apiSchema == null) {
//                return@launch
//            }
//            val savedJson = repository.loadFormResponseJson(benId, visitDay)
//
//            val savedFieldValues: Map<String, Any?> = if (!savedJson.isNullOrBlank()) {
//                try {
//                    val root = JSONObject(savedJson)
//                    val fieldsJson = root.optJSONObject("fields") ?: JSONObject()
//                    fieldsJson.keys().asSequence().associateWith { fieldsJson.opt(it) }
//                } catch (e: Exception) {
//                    emptyMap()
//                }
//            } else {
//                emptyMap()
//            }
//
//
//            val allFields = apiSchema.sections.flatMap { it.fields.orEmpty() }
//
//            apiSchema.sections.orEmpty().forEach { section ->
//                section.fields.orEmpty().forEach { field ->
////                    field.value = when (field.fieldId) {
////                        "visit_day" -> visitDay
////                        else -> savedFieldValues[field.fieldId] ?: field.defaultValue
////                    }
////                    field.isEditable = field.fieldId != "visit_day" && !isViewMode
//
//
//
//
//                    field.value = when (field.fieldId) {
//                        "visit_day" -> visitDay
//                        "due_date" -> calculateDueDate(dob, visitDay)?.let { formatDate(it) } ?: ""
//                        else -> savedFieldValues[field.fieldId] ?: field.defaultValue
//                    }
//
//                    field.isEditable = when (field.fieldId) {
//                        "visit_day" -> false
//                        "due_date" -> false // Non-editable due date
//                        else -> !isViewMode
//                    }
//
//                }
//            }
//
//            apiSchema.sections.orEmpty().forEach { section ->
//                section.fields.orEmpty().forEach { field ->
//                    field.visible = evaluateFieldVisibility(field, allFields)
//                }
//            }
//
//            _schema.value = apiSchema
//        }
//    }
fun loadFormSchema(
    benId: Long,
    formId: String,
    visitDay: String,
    viewMode: Boolean,
    dob: Long
) {
    this.visitDay = visitDay
    this.isViewMode = viewMode

    viewModelScope.launch {
        val apiSchema = repository.getFormSchema(formId) ?: return@launch
        val savedJson = repository.loadFormResponseJson(benId, visitDay)

        val savedFieldValues: Map<String, Any?> = if (!savedJson.isNullOrBlank()) {
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

        val allFields = apiSchema.sections.flatMap { it.fields.orEmpty() }

        apiSchema.sections.orEmpty().forEach { section ->
            section.fields.orEmpty().forEach { field ->

                // Value set karna
                field.value = when (field.fieldId) {
                    "visit_day" -> visitDay
                    "due_date" -> calculateDueDate(dob, visitDay)?.let { formatDate(it) } ?: ""
                    else -> savedFieldValues[field.fieldId] ?: field.defaultValue
                }

                // Editable flag set karna
                field.isEditable = when (field.fieldId) {
                    "visit_day", "due_date" -> false // Always non-editable
                    else -> !viewMode // Baaki fields only editable in edit mode
                }
            }
        }

        // Conditional visibility
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

    fun saveFormResponses(benId: Long, hhId: Long) {
        viewModelScope.launch {


            val currentSchema = _schema.value ?: return@launch
            val formId = currentSchema.formId
            val version = currentSchema.version
            val beneficiaryId = benId ?: 0
//            val visitDate = calculateDueDate(_infant.value?.visitDay ?: "", visitDay) ?: "N/A"
//            val dob = dob
//            if (dob.isNullOrEmpty() || visitDay.isNullOrEmpty()) {
//                Log.e("saveFormResponses", "DOB or VisitDay is null or empty.")
//                return@launch
//            }

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

            val entity = FormResponseJsonEntity(
                benId = benId,
                hhId = hhId,
                visitDay = visitDay,
                visitDate = visitDate,
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


    fun calculateDueDate(dobMillis: Long, visitDay: String): Long? {
        return try {
            val calendar = Calendar.getInstance().apply {
                time = Date(dobMillis)
            }
            Log.v("DEBUG", "Raw DOB millis: $dobMillis → ${formatDate(dobMillis)}")
            Log.v("DEBUG", "Received DOB millis from source: $dobMillis → ${formatDate(dobMillis)}")

            val daysToAdd = when (visitDay.trim()) {
                "1st Day" -> 0
                "3rd Day" -> 2
                "7th Day" -> 6
                "14th Day" -> 13
                "21st Day" -> 20
                "28th Day" -> 27
                "42nd Day" -> 41
                else -> return null
            }

            calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
            calendar.timeInMillis
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
                                dependsOn = it.dependsOn!!,
                                expectedValue = it.expectedValue!!
                            )
                        } else null
                    },
                    value = field.value
                )
            }
        } ?: emptyList()
    }

    fun getVisitCardList(dob:Long): List<VisitCard> {
//        val visitOrder = listOf("1st Day", "3rd Day", "7th Day", "14th Day", "21st Day", "28th Day", "42nd Day")

        val currentBenId = infant.value?.benId
//        val dob = infant.value?.visitDay ?: "-"

        val relevantVisits = _syncedVisitList.value.filter { it.benId == currentBenId }

        val completed = relevantVisits.map { it.visitDay }.toSet()

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

            val visit = relevantVisits.find { it.visitDay == day }
            val visitDate: String = visit?.formDataJson
                ?.let { JSONObject(it).optString("visit_date", null) }
                ?: "-"
//                val visitDate = visit?.let {
//                    try {
//                        val json = JSONObject(it.formDataJson)
//                        json.optString("visit_date", "-")
//                    } catch (e: Exception) {
//                        "-"
//                    }
//                } ?: calculateDueDate(dob, day) ?: "-"

                VisitCard(
                    visitDay = day,
                    visitDate = visitDate,
                    isCompleted = isCompleted,
                    isEditable = isEditable
                )
            }
    }
    fun formatDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }
    suspend fun getLastVisitDay(benId: Long): String? {
        val visits = repository.getSyncedVisitsByRchId(benId)

        // Filter only valid visitDays and find max index
        val lastVisit = visits
            .filter { it.visitDay in visitOrder }
            .maxByOrNull { visitOrder.indexOf(it.visitDay) }

        return lastVisit?.visitDay
    }
    suspend fun getLastVisitDate(benId: Long): Date? {

        val visits = repository.getSyncedVisitsByRchId(benId)
        val lastVisit = visits
            .filter { it.visitDay in visitOrder }
            .maxByOrNull { visitOrder.indexOf(it.visitDay) }

        return lastVisit?.formDataJson?.let {
            try {
                val json = JSONObject(it)
                val fields = json.optJSONObject("fields")
                val dateStr = fields?.optString("visit_date")
                if (!dateStr.isNullOrBlank()) {
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }


    fun loadVisitDates(benId: Long) {
        viewModelScope.launch {
            previousVisitDate = getLastVisitDate(benId)
            lastVisitDay = getLastVisitDay(benId)  // if needed
            Log.d("HBNCDatess", "benid: $benId visitDateVievmodel: $lastVisitDay, previousVisitDate: $previousVisitDate")

        }
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
                add(Calendar.DATE, 1) // 1 din baad se allow
            }.time
        }
    }

}
