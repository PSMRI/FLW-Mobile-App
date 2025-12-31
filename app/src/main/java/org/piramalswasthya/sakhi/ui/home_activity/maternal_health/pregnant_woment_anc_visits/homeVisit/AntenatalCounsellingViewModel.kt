package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.homeVisit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC
import org.piramalswasthya.sakhi.model.dynamicModel.VisitCard
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.InfantRegRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AntenatalCounsellingViewModel @Inject constructor(
    private val repository: FormRepository,
    private val benRepo: BenRepo,
    private val infantRegRepo: InfantRegRepo,
) : ViewModel() {

    enum class State {
        IDLE, LOADING, SUCCESS, FAIL
    }

    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state
    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema

    private val _syncedVisitList = MutableStateFlow<List<ANCFormResponseJsonEntity>>(emptyList())
    val syncedVisitList: StateFlow<List<ANCFormResponseJsonEntity>> = _syncedVisitList

    private val _lastVisitData = MutableStateFlow<ANCFormResponseJsonEntity?>(null)
    val lastVisitData: StateFlow<ANCFormResponseJsonEntity?> = _lastVisitData
    private val oneTimeQuestions = listOf(
        "child_gap", "short_height", "pre_preg_weight", "miscarriage_history",
        "four_plus_delivery", "first_delivery", "c_section_history", "pre_existing_disease",
        "prolonged_labor"
    )

    private val editableOneTimeQuestions = listOf("twin_pregnancy", "sickle_cell")

    val visitOrder = listOf("1 Months", "2 Months", "3 Months", "4 Months", "5 Months", "6 Months", "7 Months", "8 Months", "9 Months")
    private var benId: Long = 0L
    private var hhId: Long = 0L
    var visitMonth: String = ""

    var visitDay: String = ""
    private var isViewMode: Boolean = false

    private val _isBenDead = MutableStateFlow(false)
    val isBenDead: StateFlow<Boolean> = _isBenDead

    private val _isSNCU = MutableStateFlow(false)
    val isSNCU: StateFlow<Boolean> = _isSNCU

    private var motherAge: Int = 0

    fun setMotherAge(age: Int) {
        motherAge = age
    }

    suspend fun loadLastVisitData(benId: Long) {
        try {
            val visits = repository.getSyncedVisitsByRchIdANC(benId)
            _lastVisitData.value = visits.maxByOrNull {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(it.visitDate)?.time ?: 0L
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading last visit data")
        }
    }

    fun getLastVisitData(): ANCFormResponseJsonEntity? {
        return _lastVisitData.value
    }

    fun fetchSNCUStatus(benId: Long) {
        viewModelScope.launch {
            val infantRecord = infantRegRepo.getInfantReg(benId, 1)
            _isSNCU.value = infantRecord?.isSNCU.equals("Yes", ignoreCase = true)
        }
    }

    var previousVisitDate: Date? = null
    var lastVisitDay: String? = null

    fun loadSyncedVisitList(benId: Long, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _syncedVisitList.value = repository.getSyncedVisitsByRchIdANC(benId)
            onComplete?.invoke()
        }
    }

    fun loadFormSchema(
        benId: Long,
        formId: String,
        visitDay: String,
        viewMode: Boolean,
        lang: String
    ) {
        this.visitDay = visitDay
        this.isViewMode = viewMode
        this.benId = benId

        viewModelScope.launch {
            val cachedSchemaEntity = repository.getSavedSchema(formId)
            val cachedSchema: FormSchemaDto? = cachedSchemaEntity?.let {
                FormSchemaDto.fromJson(it.schemaJson)
            }
            val localSchemaToRender = cachedSchema ?: repository.getFormSchema(formId,lang)?.also {
            }

            if (localSchemaToRender == null) {
                return@launch
            }

            val savedJson = repository.loadFormResponseJsonANC(benId,visitDay)
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

            // Load last visit data for one-time questions
            if (!viewMode) {
                loadLastVisitData(benId)
            }

            val allFields = localSchemaToRender.sections.flatMap { it.fields }
            localSchemaToRender.sections.forEach { section ->
                section.fields.forEach { field ->
                    field.value = when (field.fieldId) {
                        "visit_day" -> visitDay
                        else -> savedFieldValues[field.fieldId] ?: field.default
                    }

                    if (!viewMode && _lastVisitData.value != null) {
                        val lastVisit = _lastVisitData.value
                        try {
                            val formDataJson = lastVisit?.formDataJson
                            if (!formDataJson.isNullOrBlank()) {
                                val jsonObject = JSONObject(formDataJson)
                                val fieldsJson = jsonObject.optJSONObject("fields") ?: JSONObject()

                                when (field.fieldId) {
                                    "age_risk" -> {
                                        val isAgeRisk = motherAge < 18 || motherAge > 35
                                        field.value = if (isAgeRisk) "Yes" else "No"
                                        field.isEditable = false
                                    }
                                    in oneTimeQuestions -> {
                                        val lastValue = fieldsJson.optString(field.fieldId)
                                        if (lastValue.isNotEmpty()) {
                                            field.value = lastValue
                                            field.isEditable = false
                                        } else {
                                            field.isEditable = !viewMode
                                        }
                                    }
                                    in editableOneTimeQuestions -> {
                                        val lastValue = fieldsJson.optString(field.fieldId)
                                        if (lastValue.isNotEmpty()) {
                                            field.value = lastValue
                                            field.isEditable = true // Keep editable
                                        } else {
                                            field.isEditable = !viewMode
                                        }
                                    }
                                    else -> {
                                        field.isEditable = !viewMode
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error processing last visit data for field ${field.fieldId}")
                            field.isEditable = !viewMode
                        }
                    } else {
                        field.isEditable = when (field.fieldId) {
                            "visit_day", "due_date", "age_risk" -> false
                            else -> !viewMode
                        }

                        if (field.fieldId == "age_risk" && viewMode) {
                            val isAgeRisk = motherAge < 18 || motherAge > 35
                            field.value = if (isAgeRisk) "Yes" else "No"
                        }
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

    suspend fun getLastVisitDates(benId: Long): String
    {val visits = repository.getLastVisitForBenANC(benId)
        return  visits?.visitDate.toString()
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

    suspend fun saveFormResponses(benId: Long) {
        val currentSchema = _schema.value ?: return
        val formId = currentSchema.formId
        val version = currentSchema.version
        val fieldMap = currentSchema.sections.orEmpty()
            .flatMap { it.fields.orEmpty() }
            .filter { it.visible && it.value != null }
            .associate { it.fieldId to it.value }

        val visitDate = fieldMap["home_visit_date"]?.toString() ?: "N/A"

        val wrappedJson = JSONObject().apply {
            put("formId", formId)
            put("beneficiaryId", benId)
            put("visitDate", visitDate)
            put("fields", JSONObject(fieldMap))
        }

        val entity = ANCFormResponseJsonEntity(
            benId = benId,
            visitDay = visitMonth,
            visitDate = visitDate,
            formId = formId,
            version = version,
            formDataJson = wrappedJson.toString(),
            isSynced = false,
            syncedAt = null
        )

        try {
            _state.value = State.LOADING

            repository.insertFormResponseANC(entity)

            _state.value = State.SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = State.FAIL
        }
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

    fun getVisitCardList(benId: Long, dobMillis: Long): List<VisitCard> {
        val relevantVisits = _syncedVisitList.value.filter { it.benId == benId }
        val completed = relevantVisits.map { it.visitDay }.toSet()
        val babyAgeMonths = getBabyAgeMonths(dobMillis)
        val visitMonthMapping = mapOf(
            "3 Months" to 3,
            "6 Months" to 6,
            "9 Months" to 9,
            "12 Months" to 12,
            "15 Months" to 15
        )

        val allVisits = listOf("3 Months", "6 Months", "9 Months", "12 Months", "15 Months")
        val eligibleMonths = visitOrder.filter { month ->
            val monthValue = visitMonthMapping[month] ?: 0
            val isEligible = monthValue <= babyAgeMonths
            isEligible
        }

        var nextEditableFound = false

        val finalList = eligibleMonths.map { month ->
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

            VisitCard(
                visitDay = month,
                visitDate = visitDate,
                isCompleted = isCompleted,
                isEditable = isEditable,
                isBabyDeath = isBabyDeath
            )
        }.toMutableList()

        val futureMonths = allVisits.filterNot { eligibleMonths.contains(it) }

        futureMonths.forEach { month ->
            finalList.add(
                VisitCard(
                    visitDay = month,
                    visitDate = "-",
                    isCompleted = false,
                    isEditable = false,
                    isBabyDeath = false
                )
            )
        }

        return finalList
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