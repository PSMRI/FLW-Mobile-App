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
import com.google.gson.Gson
import org.piramalswasthya.sakhi.database.shared_preferences.ReferralStatusManager
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.repositories.NcdReferalRepo

@HiltViewModel
class AntenatalCounsellingViewModel @Inject constructor(
    private val repository: FormRepository,
    private val benRepo: BenRepo,
    private val infantRegRepo: InfantRegRepo,
    private var referalRepo: NcdReferalRepo,
    private val referralStatusManager: ReferralStatusManager
) : ViewModel() {

    enum class ReferralType {
        MATERNAL,
        NCD,
        TB,
        LEPROSY,
        GERIATRIC,
        COPD,
        DEPRESSION,
        HRP
    }

    enum class State {
        IDLE, LOADING, SUCCESS, FAIL
    }

    private var _visitCount = MutableStateFlow(0)
    val visitCount: StateFlow<Int> = _visitCount
    private val _state = MutableLiveData(State.IDLE)
    val state: LiveData<State>
        get() = _state
    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema

    private val _syncedVisitList = MutableStateFlow<List<ANCFormResponseJsonEntity>>(emptyList())
    val syncedVisitList: StateFlow<List<ANCFormResponseJsonEntity>> = _syncedVisitList

    private val _lastVisitData = MutableStateFlow<ANCFormResponseJsonEntity?>(null)
    val lastVisitData: StateFlow<ANCFormResponseJsonEntity?> = _lastVisitData

    private val _referralList = MutableLiveData<MutableList<ReferalCache>>(mutableListOf())
    val referralList: LiveData<MutableList<ReferalCache>> = _referralList

    private val _completedReferrals = MutableLiveData<MutableSet<ReferralType>>(mutableSetOf())
    val completedReferrals: LiveData<MutableSet<ReferralType>> = _completedReferrals

    private val _showReferralDialog = MutableLiveData(false)
    val showReferralDialog: LiveData<Boolean> = _showReferralDialog

    var referralCache: ReferalCache? = null

    fun isReferralAlreadyDone(type: ReferralType): Boolean {
        val inMemory = _completedReferrals.value?.contains(type) == true
        val persistent = referralStatusManager.isReferred(benId, type.name)
        return inMemory || persistent
    }

    fun markReferralCompleted(type: ReferralType) {
        val set = _completedReferrals.value ?: mutableSetOf()
        set.add(type)
        _completedReferrals.value = set
        referralStatusManager.markAsReferred(benId, type.name)
    }

    fun addReferral(referral: ReferalCache) {
        val list = _referralList.value ?: mutableListOf()
        val alreadyExists = list.any {
            it.referralReason == referral.referralReason
        }

        if (!alreadyExists) {
            list.add(referral)
            _referralList.value = list
            referralCache = referral
            referralStatusManager.markAsReferred(benId, ReferralType.MATERNAL.name)
        }
    }

    private fun loadExistingReferralStatus() {
        viewModelScope.launch {
            if (referralStatusManager.isReferred(benId, ReferralType.MATERNAL.name)) {
                markReferralCompleted(ReferralType.MATERNAL)
            }
        }
    }

    /*fun loadVisitCount(benId: Long) {
        viewModelScope.launch {
            val visits = repository.getSyncedVisitsByRchIdANC(benId)
            _visitCount.value = visits.size
        }
    }*/
    fun checkForReferralTriggers(formData: Map<String, Any?>): Boolean {
        val dangerSignQuestions = listOf(
            "swelling", "high_bp", "convulsions", "anemia", "reduced_fetal_movement",
            "age_risk", "child_gap", "short_height", "pre_preg_weight", "bleeding",
            "miscarriage_history", "four_plus_delivery", "first_delivery", "twin_pregnancy",
            "c_section_history", "pre_existing_disease", "fever_malaria", "jaundice",
            "sickle_cell", "prolonged_labor", "malpresentation"
        )

        return dangerSignQuestions.any { questionId ->
            formData[questionId]?.toString()?.equals("Yes", ignoreCase = true) == true
        }
    }

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


    init{
        loadExistingReferralStatus()
    }

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
        lang: String,
        visitNumber: Int
    ) {
        this.visitDay = visitDay
        this.isViewMode = viewMode
        this.benId = benId

        viewModelScope.launch {
            initializeVisitData(benId, formId, lang, visitDay, viewMode, visitNumber)
        }
    }

    private suspend fun initializeVisitData(
        benId: Long,
        formId: String,
        lang: String,
        visitDay: String,
        viewMode: Boolean,
        visitNumber: Int
    ) {
        loadVisitCount(benId)
        val schema = loadSchema(formId, lang) ?: return
        val savedFieldValues = loadSavedFieldValues(benId, visitDay)

        if (!viewMode) {
            loadLastVisitData(benId)
        }

        processSchemaFields(schema, savedFieldValues, visitDay, viewMode, visitNumber)
        _schema.value = schema
    }

    private suspend fun loadVisitCount(benId: Long) {
        val visits = repository.getSyncedVisitsByRchIdANC(benId)
        _visitCount.value = visits.size
    }

    private suspend fun loadSchema(formId: String, lang: String): FormSchemaDto? {
        val cachedSchemaEntity = repository.getSavedSchema(formId)
        return cachedSchemaEntity?.let { FormSchemaDto.fromJson(it.schemaJson) }
            ?: repository.getFormSchema(formId, lang)
    }

    private suspend fun loadSavedFieldValues(benId: Long, visitDay: String): Map<String, Any?> {
        val savedJson = repository.loadFormResponseJsonANC(benId, visitDay)
        return if (!savedJson.isNullOrBlank()) {
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
    }

    private fun processSchemaFields(
        schema: FormSchemaDto,
        savedFieldValues: Map<String, Any?>,
        visitDay: String,
        viewMode: Boolean,
        visitNumber: Int
    ) {
        val allFields = schema.sections.flatMap { it.fields }

        schema.sections.forEach { section ->
            section.fields.forEach { field ->
                setFieldValue(field, savedFieldValues, visitDay, viewMode, visitNumber)
                setFieldEditability(field, viewMode)
                setLastVisitDataForField(field, viewMode)
            }
        }

        updateFieldVisibility(schema, allFields)
    }

    private fun setFieldValue(
        field: FormFieldDto,
        savedFieldValues: Map<String, Any?>,
        visitDay: String,
        viewMode: Boolean,
        visitNumber: Int
    ) {
        field.value = when (field.fieldId) {
            "visit_day" -> visitDay
            "visit_number" -> getVisitNumberValue(field, viewMode, visitNumber)
            "age_risk" -> getAgeRiskValue()
            else -> savedFieldValues[field.fieldId] ?: field.default
        }
    }

    private fun getVisitNumberValue(field: FormFieldDto, viewMode: Boolean, visitNumber: Int): String {
        return if (!viewMode) {
            "Visit-${_visitCount.value + 1}"
        } else {
            "Visit-$visitNumber"
        }
    }

    private fun getAgeRiskValue(): String {
        return if (motherAge < 18 || motherAge > 35) "Yes" else "No"
    }

    private fun setFieldEditability(field: FormFieldDto, viewMode: Boolean) {
        field.isEditable = when (field.fieldId) {
            "visit_day", "due_date", "age_risk", "visit_number" -> false
            else -> !viewMode
        }
    }

    private fun setLastVisitDataForField(field: FormFieldDto, viewMode: Boolean) {
        if (viewMode || _lastVisitData.value == null) return

        val lastVisit = _lastVisitData.value ?: return
        processLastVisitData(field, lastVisit, viewMode)
    }

    private fun processLastVisitData(field: FormFieldDto, lastVisit: ANCFormResponseJsonEntity, viewMode: Boolean) {
        try {
            val formDataJson = lastVisit.formDataJson ?: return
            if (formDataJson.isBlank()) return

            val jsonObject = JSONObject(formDataJson)
            val fieldsJson = jsonObject.optJSONObject("fields") ?: JSONObject()

            when (field.fieldId) {
                in oneTimeQuestions -> handleOneTimeQuestion(field, fieldsJson, viewMode)
                in editableOneTimeQuestions -> handleEditableOneTimeQuestion(field, fieldsJson, viewMode)
                else -> {
                    if (field.fieldId != "age_risk") {
                        field.isEditable = !viewMode
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error processing last visit data for field ${field.fieldId}")
            if (field.fieldId != "age_risk") {
                field.isEditable = !viewMode
            }
        }
    }

    private fun handleOneTimeQuestion(field: FormFieldDto, fieldsJson: JSONObject, viewMode: Boolean) {
        val lastValue = fieldsJson.optString(field.fieldId)
        if (lastValue.isNotEmpty()) {
            field.value = lastValue
            field.isEditable = false
        } else {
            field.isEditable = !viewMode
        }
    }

    private fun handleEditableOneTimeQuestion(field: FormFieldDto, fieldsJson: JSONObject, viewMode: Boolean) {
        val lastValue = fieldsJson.optString(field.fieldId)
        if (lastValue.isNotEmpty()) {
            field.value = lastValue
            field.isEditable = true
        } else {
            field.isEditable = !viewMode
        }
    }

    private fun updateFieldVisibility(schema: FormSchemaDto, allFields: List<FormFieldDto>) {
        schema.sections.forEach { section ->
            section.fields.forEach { field ->
                field.visible = evaluateFieldVisibility(field, allFields)
            }
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
            _state.postValue ( State.LOADING)

            repository.insertFormResponseANC(entity)
            referralList.value?.forEach {
                referalRepo.saveReferedNCD(it)
            }

            if (referralCache != null) {
                markReferralCompleted(ReferralType.MATERNAL)
            }

            _state.postValue(State.SUCCESS)
        } catch (e: Exception) {
            e.printStackTrace()
            _state.postValue( State.FAIL)
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