package org.piramalswasthya.sakhi.ui.home_activity.infant.hbyc

import android.util.Log
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
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicModel.VisitCard
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.InfantRegRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HBYCFormViewModel @Inject constructor(
    private val repository: FormRepository,
    private val benRepo: BenRepo,
    private val infantRegRepo: InfantRegRepo,
) : ViewModel() {

    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema

    private val _infant = MutableStateFlow<FormResponseJsonEntityHBYC?>(null)
    val infant: StateFlow<FormResponseJsonEntityHBYC?> = _infant

    private val _syncedVisitList = MutableStateFlow<List<FormResponseJsonEntityHBYC>>(emptyList())
    val syncedVisitList: StateFlow<List<FormResponseJsonEntityHBYC>> = _syncedVisitList

    val visitOrder = listOf("3 Months", "6 Months", "9 Months", "12 Months", "15 Months")
    private var benId: Long = 0L
    private var hhId: Long = 0L
    var visitMonth: String = ""
    private var isViewMode: Boolean = false

    private val _isBenDead = MutableStateFlow(false)
    val isBenDead: StateFlow<Boolean> = _isBenDead


    private val _isSNCU = MutableStateFlow(false)
    val isSNCU: StateFlow<Boolean> = _isSNCU

    fun fetchSNCUStatus(benId: Long) {
        viewModelScope.launch {
            val infantRecord = infantRegRepo.getInfantReg(benId, 1)
            _isSNCU.value = infantRecord?.isSNCU.equals("Yes", ignoreCase = true)
        }
    }

    var previousVisitDate: Date? = null
    var lastVisitDay: String? = null

    companion object {
        private const val OTHER_PLACE_OF_DEATH_ID = 8
        private const val DEFAULT_DEATH_ID = -1
    }

    fun loadSyncedVisitList(benId: Long, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _syncedVisitList.value = repository.getSyncedVisitsByRchIdHBYC(benId)
            onComplete?.invoke()
        }
    }
    fun loadInfant(benId: Long, hhId: Long) {
        this.benId = benId
        this.hhId = hhId
        viewModelScope.launch {
            _infant.value = repository.getInfantByRchIdHBYC(benId).firstOrNull()
        }
    }

    fun loadFormSchema(
        benId: Long,
        formId: String,
        visitMonth: String,
        viewMode: Boolean,
        dob: Long
    ) {
        this.visitMonth = visitMonth
        this.isViewMode = viewMode

        loadSyncedVisitList(benId) {
            viewModelScope.launch {
                val cachedSchemaEntity = repository.getSavedSchema(formId)
                val cachedSchema: FormSchemaDto? = cachedSchemaEntity?.let { FormSchemaDto.fromJson(it.schemaJson) }

                val localSchemaToRender = cachedSchema ?: repository.getFormSchema(formId) ?: return@launch

                val savedJson = repository.loadFormResponseJsonHBYC(benId, visitMonth)
                val savedFieldValues = if (!savedJson.isNullOrBlank()) {
                    try {
                        val root = JSONObject(savedJson)
                        val fieldsJson = root.optJSONObject("fields") ?: JSONObject()
                        fieldsJson.keys().asSequence().associateWith { fieldsJson.opt(it) }
                    } catch (e: Exception) { emptyMap() }
                } else emptyMap()

                val allFields = localSchemaToRender.sections.flatMap { it.fields.orEmpty() }

                localSchemaToRender.sections.orEmpty().forEach { section ->
                    section.fields.orEmpty().forEach { field ->
                        field.value = when (field.fieldId) {
                            "visit_day" -> visitMonth
                            "due_date" -> calculateDueDate(dob, visitMonth)?.let { formatDate(it) } ?: field.defaultValue
                            else -> savedFieldValues[field.fieldId] ?: field.defaultValue
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

                val submittedMonths = _syncedVisitList.value.map { it.visitDay }.toSet()
                localSchemaToRender.sections.orEmpty().forEach { section ->
                    section.fields.orEmpty().forEach { field ->
                        if (field.fieldId == "visit_day" && field.options != null) {
                            field.options = field.options!!.filter { it == visitMonth || !submittedMonths.contains(it) }.toMutableList()
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

    allFields.find { it.fieldId == fieldId }?.apply {
        this.value = value
    }
    allFields.forEach { field ->
        field.visible = evaluateFieldVisibility(field, allFields)
    }

    val babyAliveValue = allFields.find { it.fieldId == "is_baby_alive" }?.value
    val sncuField = allFields.find { it.fieldId == "discharged_from_sncu" }
    if (sncuField != null && babyAliveValue == "Yes" && _isSNCU.value) {
        sncuField.value = "Yes"
    }
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
            } catch (e: Exception) { e.printStackTrace() }
        }

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
            visitDay = visitMonth,
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

    fun calculateDueDate(dobMillis: Long, visitMonth: String): Long? {
        val calendar = Calendar.getInstance().apply { time = Date(dobMillis) }
        val monthsToAdd = when (visitMonth.trim()) {
            "3 Months" -> 3
            "6 Months" -> 6
            "9 Months" -> 9
            "12 Months" -> 12
            "15 Months" -> 15
            else -> return null
        }
        calendar.add(Calendar.MONTH, monthsToAdd)
        return calendar.timeInMillis
    }

    fun formatDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return sdf.format(Date(epochMillis))
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

//    fun getVisitCardList(benId: Long): List<VisitCard> {
//        val relevantVisits = _syncedVisitList.value.filter { it.benId == benId }
//        val completed = relevantVisits.map { it.visitDay }.toSet()
//        return visitOrder.map { month ->
//            val isCompleted = completed.contains(month)
//            val isEditable = when (month) {
//                "3 Months" -> !isCompleted
//                "6 Months" -> !isCompleted && completed.contains("3 Months")
//                "9 Months" -> !isCompleted && completed.contains("6 Months")
//                "12 Months" -> !isCompleted && completed.contains("9 Months")
//                "15 Months" -> !isCompleted && completed.contains("12 Months")
//                else -> false
//            }
//            val visit = relevantVisits.find { it.visitDay == month }
//            val visitDate = visit?.formDataJson?.let { JSONObject(it).optString("visitDate", "-") } ?: "-"
//            val isBabyDeath = visit?.formDataJson?.let {
//                val root = JSONObject(it)
//                val fieldsJson = root.optJSONObject("fields") ?: JSONObject()
//                fieldsJson.optString("is_baby_alive", "Yes").equals("No", ignoreCase = true)
//            } ?: false
//            VisitCard(
//                visitDay = month,
//                visitDate = visitDate,
//                isCompleted = isCompleted,
//                isEditable = isEditable,
//                isBabyDeath = isBabyDeath
//            )
//        }
//    }

    fun getVisitCardList(benId: Long, dobMillis: Long): List<VisitCard> {
        val relevantVisits = _syncedVisitList.value.filter { it.benId == benId }
        val completed = relevantVisits.map { it.visitDay }.toSet()

        // Baby ki age months use new method
        val babyAgeMonths = getBabyAgeMonths(dobMillis)
        Log.d("HBYC", "Baby age in months: $babyAgeMonths")

        val visitMonthMapping = mapOf(
            "3 Months" to 3,
            "6 Months" to 6,
            "9 Months" to 9,
            "12 Months" to 12,
            "15 Months" to 15
        )

        // Eligible months filter based on baby age
        val eligibleMonths = visitOrder.filter { month ->
            val monthValue = visitMonthMapping[month] ?: 0
            val isEligible = monthValue <= babyAgeMonths
            Log.d("HBYC", "Month: $month, Value: $monthValue, Eligible: $isEligible")
            isEligible
        }

        Log.d("HBYC", "Eligible months after filtering: $eligibleMonths")

        // Sequential visit logic: only next pending visit is editable
        var nextEditableFound = false

        return eligibleMonths.map { month ->
            val isCompleted = completed.contains(month)
            val isEditable = if (!nextEditableFound && !isCompleted) {
                nextEditableFound = true
                true
            } else false

            val visit = relevantVisits.find { it.visitDay == month }
            val visitDate = visit?.formDataJson?.let { JSONObject(it).optString("visitDate", "-") } ?: "-"
            val isBabyDeath = visit?.formDataJson?.let {
                val root = JSONObject(it)
                val fieldsJson = root.optJSONObject("fields") ?: JSONObject()
                fieldsJson.optString("is_baby_alive", "Yes").equals("No", ignoreCase = true)
            } ?: false

            Log.d(
                "HBYC",
                "VisitCard -> Month: $month, Completed: $isCompleted, Editable: $isEditable, VisitDate: $visitDate, BabyDeath: $isBabyDeath"
            )

            VisitCard(
                visitDay = month,
                visitDate = visitDate,
                isCompleted = isCompleted,
                isEditable = isEditable,
                isBabyDeath = isBabyDeath
            )
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

    fun checkIfBenDead(benId: Long) {
        viewModelScope.launch {
            try {
                _isBenDead.value = benRepo.isBenDead(benId)
            } catch (e: Exception) {
                _isBenDead.value = false
            }
        }
    }

    private suspend fun getLastVisit(benId: Long): FormResponseJsonEntityHBYC? {
        val visits = repository.getSyncedVisitsByRchIdHBYC(benId)
        return visits.filter { it.visitDay in visitOrder }
            .maxByOrNull { visitOrder.indexOf(it.visitDay) }
    }

    suspend fun getLastVisitDay(benId: Long): String? = getLastVisit(benId)?.visitDay

    suspend fun getLastVisitDate(benId: Long): Date? {
        val lastVisit = getLastVisit(benId)
        return lastVisit?.formDataJson?.let {
            try {
                val json = JSONObject(it)
                val fields = json.optJSONObject("fields")
                val dateStr = fields?.optString("visit_date")
                if (!dateStr.isNullOrBlank()) SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr) else null
            } catch (e: Exception) { null }
        }
    }

    fun loadVisitDates(benId: Long) {
        viewModelScope.launch {
            previousVisitDate = getLastVisitDate(benId)
            lastVisitDay = getLastVisitDay(benId)
        }
    }

    fun getBabyAgeMonths(dobMillis: Long): Int {
        val dob = Calendar.getInstance().apply { timeInMillis = dobMillis }
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        age = age * 12 + today.get(Calendar.MONTH) - dob.get(Calendar.MONTH)
        return age
    }
}
