package org.piramalswasthya.sakhi.ui.home_activity.infant.hbnc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.ConditionalLogic
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FieldValidation
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicModel.VisitCard
import org.piramalswasthya.sakhi.repositories.BenRepo
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HBNCFormViewModel @Inject constructor(
    private val repository: FormRepository,
    private val benRepo: BenRepo
) : ViewModel() {

    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema

    private val _infant = MutableStateFlow<FormResponseJsonEntity?>(null)
    val infant: StateFlow<FormResponseJsonEntity?> = _infant
    var lastVisitDay: String? = null
    var previousVisitDate: Date? = null
    private val _syncedVisitList = MutableStateFlow<List<FormResponseJsonEntity>>(emptyList())
    val syncedVisitList: StateFlow<List<FormResponseJsonEntity>> = _syncedVisitList
    private val visitOrder = listOf("1st Day", "3rd Day", "7th Day", "14th Day", "21st Day", "28th Day", "42nd Day")
    private var benId: Long = 0L
    private var hhId: Long = 0L
    var visitDay: String = ""
    private var isViewMode: Boolean = false


    private val _isBenDead = MutableStateFlow(false)
    val isBenDead: StateFlow<Boolean> = _isBenDead

    fun loadSyncedVisitList(benId: Long) {
        viewModelScope.launch {
            val list = repository.getSyncedVisitsByRchId(benId)
            _syncedVisitList.value = list

        }
    }
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
            val cachedSchemaEntity = repository.getSavedSchema(formId)
            val cachedSchema: FormSchemaDto? = cachedSchemaEntity?.let {
                FormSchemaDto.fromJson(it.schemaJson)
            }
            val localSchemaToRender = cachedSchema ?: repository.getFormSchema(formId)?.also {
            }

            if (localSchemaToRender == null) {
                return@launch
            }

            launch {
                val updatedSchema = repository.getFormSchema(formId)
                if (updatedSchema != null && (cachedSchemaEntity?.version ?: 0) < updatedSchema.version) {
                }
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

            val allFields = localSchemaToRender.sections.flatMap { it.fields.orEmpty() }
            localSchemaToRender.sections.orEmpty().forEach { section ->
                section.fields.orEmpty().forEach { field ->
                    field.value = when (field.fieldId) {
                        "visit_day" -> visitDay
                        "due_date" -> calculateDueDate(dob, visitDay)?.let { formatDate(it) } ?: ""
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
    companion object {
        private const val OTHER_PLACE_OF_DEATH_ID = 8
        private const val DEFAULT_DEATH_ID = -1
    }
suspend fun saveFormResponses(benId: Long, hhId: Long) {
    val currentSchema = _schema.value ?: return
    val formId = currentSchema.formId
    val version = currentSchema.version
    val beneficiaryId = benId

    val fieldMap = currentSchema.sections.orEmpty()
        .flatMap { it.fields.orEmpty() }
        .filter { it.visible && it.value != null }
        .associate { it.fieldId to it.value }

    val visitDate = fieldMap["visit_date"]?.toString() ?: "N/A"
    val isBabyAlive = fieldMap["is_baby_alive"]?.toString().orEmpty()
    if (isBabyAlive.equals("No", ignoreCase = true)) {
        val reasonOfDeath = fieldMap["reason_for_death"]?.toString().orEmpty()
        val placeOfDeath = fieldMap["place_of_death"]?.toString().orEmpty()
        val otherPlaceOfDeath = fieldMap["other_place_of_death"]?.toString().orEmpty()
        val dateOfDeath = fieldMap["date_of_death"]?.toString().orEmpty()

        try {
            benRepo.getBenFromId(benId)?.let { ben ->
                ben.apply {
                    isDeath = true
                    isDeathValue = "Death"
                    this.dateOfDeath = dateOfDeath
                    this.reasonOfDeath = reasonOfDeath
                    reasonOfDeathId = -1
                    this.placeOfDeath = placeOfDeath
                    placeOfDeathId = if (!otherPlaceOfDeath.isNullOrBlank()) OTHER_PLACE_OF_DEATH_ID else DEFAULT_DEATH_ID
                    this.otherPlaceOfDeath = otherPlaceOfDeath
                    if (this.processed != "N") this.processed = "U"
                    syncState = SyncState.UNSYNCED
                }
                benRepo.updateRecord(ben)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


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

    fun getVisitCardList(benId: Long): List<VisitCard> {
        val currentBenId = benId
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
            val visitDate: String = visit?.formDataJson?.let { JSONObject(it).optString("visitDate", null) } ?: "-"
            val isBabyDeath = visit?.formDataJson?.let {
                val root = JSONObject(it)
                val fieldsJson = root.optJSONObject("fields") ?: JSONObject()
                val isAliveValue = fieldsJson.optString("is_baby_alive", "Yes")
                isAliveValue.equals("No", ignoreCase = true)


            } ?: false
                VisitCard(
                    visitDay = day,
                    visitDate = visitDate,
                    isCompleted = isCompleted,
                    isEditable = isEditable,
                    isBabyDeath =isBabyDeath
                )
            }
    }
    fun formatDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }
    private suspend fun getLastVisit(benId: Long): FormResponseJsonEntity? {
                val visits = repository.getSyncedVisitsByRchId(benId)
                return visits
                    .filter { it.visitDay in visitOrder }
                    .maxByOrNull { visitOrder.indexOf(it.visitDay) }
            }

    suspend fun getLastVisitDay(benId: Long): String? {
        return getLastVisit(benId)?.visitDay
    }
    suspend fun getLastVisitDate(benId: Long): Date? {

        val visits = repository.getSyncedVisitsByRchId(benId)
        val lastVisit = visits
            .filter { it.visitDay in visitOrder }
            .maxByOrNull { visitOrder.indexOf(it.visitDay) }

        return getLastVisit(benId)?.formDataJson?.let {
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
            lastVisitDay = getLastVisitDay(benId)

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
                add(Calendar.DATE, 1)
            }.time
        }
    }

    fun checkIfBenDead(benId: Long) {
        viewModelScope.launch {
            try {
                val dead = benRepo.isBenDead(benId)
                _isBenDead.value = dead
            } catch (e: Exception) {
                _isBenDead.value = false
            }
        }
    }
}
