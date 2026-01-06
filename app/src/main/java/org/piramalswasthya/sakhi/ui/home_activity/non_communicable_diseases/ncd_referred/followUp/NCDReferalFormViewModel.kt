package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.followUp

import android.content.Context
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
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.NCDReferalFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.dynamicRepo.NCDFollowUpFormRepository
import org.piramalswasthya.sakhi.utils.Log
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import org.piramalswasthya.sakhi.work.dynamicWoker.MosquitoNetFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.NCDFollowUpSyncWorker
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NCDReferalFormViewModel @Inject constructor(
    private val repository: NCDFollowUpFormRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema
    private val _visitHistory = MutableStateFlow<List<NCDReferalFormResponseJsonEntity>>(emptyList())
    val visitHistory: StateFlow<List<NCDReferalFormResponseJsonEntity>> = _visitHistory

    var visitNo = 1
    var followUpNo = 0
    var isFollowUpMode = false
    var isViewMode = false
    private var isTreatmentSavedInDb = false
    private val formId = FormConstants.CDTF_001

    private suspend fun loadVisitHistoryInternal(benId: Long) {
        _visitHistory.value = repository.getAllVisitsByBeneficiary(benId, formId)
    }
    private fun resolveNextVisitNumbers() {
        val list = _visitHistory.value
        if (list.isEmpty()) {
            visitNo = 1
            followUpNo = 0
            isFollowUpMode = false
            return
        }

        val last = list.maxWithOrNull(compareBy<NCDReferalFormResponseJsonEntity> { it.visitNo }
            .thenBy { it.followUpNo })!!

        visitNo = last.visitNo
        followUpNo = last.followUpNo + 1
        if (followUpNo > 6) {
            visitNo++
            followUpNo = 0
        }
        isFollowUpMode = followUpNo > 0
    }

    private fun resolveTreatmentSavedFlag() {
        isTreatmentSavedInDb = _visitHistory.value.any { it.followUpNo == 0 && !it.treatmentStartDate.isNullOrBlank() }
    }

    private fun getLastMainVisit(): NCDReferalFormResponseJsonEntity? {
        val lastMain = _visitHistory.value.filter { it.followUpNo == 0 }.maxByOrNull { it.visitNo }
        return lastMain
    }

    private fun getLastFollowUp(): NCDReferalFormResponseJsonEntity? {
        val lastFollow = _visitHistory.value.filter { it.followUpNo >= 1 }.maxByOrNull { it.followUpNo }
        return lastFollow
    }

private fun getNextFollowUpMinDate(): String {

    val lastMain = getLastMainVisit() ?: return ""
    val lastFollowUp = getLastFollowUp()

    val treatmentDate = parseDbDate(lastMain.treatmentStartDate) ?: return ""

    val cal = Calendar.getInstance()
    val todayCal = Calendar.getInstance()

    if (lastFollowUp != null && lastFollowUp.visitNo == visitNo) {
        val lastFollowUpDate = parseDbDate(lastFollowUp.followUpDate) ?: treatmentDate
        cal.time = lastFollowUpDate

    } else {
        cal.time = treatmentDate
    }
    cal.add(Calendar.MONTH, 1)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val isFutureMonth =
        cal.get(Calendar.YEAR) > todayCal.get(Calendar.YEAR) ||
                (cal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                        cal.get(Calendar.MONTH) > todayCal.get(Calendar.MONTH))

    if (isFutureMonth) {

        cal.time = todayCal.time
        cal.set(Calendar.DAY_OF_MONTH, 1)
    }

    val nextDate =
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cal.time)

    return nextDate
}


    private fun parseDbDate(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        return try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr) } catch (_: Exception) { null }
    }

    private fun parseUiDate(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        return try { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr) } catch (_: Exception) { null }
    }



    private fun formatDateForSave(input: String?): String {
        val date = parseUiDate(input) ?: return ""
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
    }

    fun loadFormSchema(benId: Long) {
        viewModelScope.launch {
            loadVisitHistoryInternal(benId)
            resolveNextVisitNumbers()
            resolveTreatmentSavedFlag()

            val schemaDto = repository.getSavedSchema(formId)?.let { FormSchemaDto.fromJson(it.schemaJson) }
                ?: repository.getFormSchema(formId) ?: return@launch

            schemaDto.sections.forEach { section ->
                section.fields.forEach { field ->
                    when (field.fieldId) {
                        "visit_label" -> {
                            field.value = "Visit-$visitNo"
                            field.isEditable = false
                            field.visible = true
                        }
                        "follow_up_no" -> field.value = followUpNo.toString()
                        "follow_up_date" -> {
                            field.visible = isFollowUpMode && isTreatmentSavedInDb
                            field.isEditable = field.visible && !isViewMode
                            if (isFollowUpMode && isTreatmentSavedInDb) {
                                val lastFollowUp = getLastFollowUp()
                                val lastFollowUpStr = lastFollowUp?.followUpDate ?: getLastMainVisit()?.treatmentStartDate ?: "null"
                                val nextFollowUp = getNextFollowUpMinDate()
//                                field.value = nextFollowUp
                                field.value = null
                            }
                        }
                        else -> {
                            field.visible = true
                            field.isEditable = !isViewMode
                        }
                    }
                }
            }

            if (isFollowUpMode) {
                getLastMainVisit()?.let { visit ->
                    val storedFields = JSONObject(visit.formDataJson).optJSONObject("fields") ?: JSONObject()
                    schemaDto.sections.forEach { section ->
                        section.fields.forEach { field ->
                            when (field.fieldId) {
                                "diagnosis" -> {
                                    val diagValue = storedFields.opt("diagnosis")
                                    field.value = when (diagValue) {
                                        is org.json.JSONArray -> (0 until diagValue.length()).map { diagValue.getString(it) }
                                        is String -> diagValue.split(",").map { it.trim() }
                                        else -> emptyList<String>()
                                    }
                                    field.isEditable = false
                                }
                                "treatment_start_date" -> {
                                    field.value = storedFields.optString(field.fieldId)
                                    field.isEditable = false
                                }
                            }
                        }
                    }
                }
            }

            _schema.value = schemaDto
        }
    }


    fun updateFieldValue(fieldId: String, value: Any?) {
        val current = _schema.value ?: return
        current.sections.forEach { section ->
            section.fields.forEach { field ->
                if (field.fieldId == fieldId) field.value = value
            }
        }
        _schema.value = current
    }

    suspend fun saveFormResponses(benId: Long, hhId: Long) {
        val schema = _schema.value ?: return
        resolveNextVisitNumbers()

        val fieldsMap = schema.sections.flatMap { it.fields }.associate { field ->
            val value = if (field.fieldId == "visit_label") "Visit-$visitNo" else field.value
            field.fieldId to value
        }

        val wrappedJson = JSONObject().apply {
            put("visitNo", visitNo)
            put("followUpNo", followUpNo)
            put("fields", JSONObject(fieldsMap))
        }

        val diagnosisList: List<String> = try {
            when (val diag = fieldsMap["diagnosis"]) {
                is Iterable<*> -> diag.filterNotNull().map { it.toString() }
                is String -> diag.split(",").map { it.trim() }
                is Map<*, *> -> diag.values.map { it.toString() }
                else -> emptyList()
            }
        } catch (e: Exception) { emptyList() }

        repository.saveVisitOrFollowUp(
            benId = benId,
            hhId = hhId,
            visitNo = visitNo,
            followUpNo = followUpNo,
            treatmentStartDate = formatDateForSave(fieldsMap["treatment_start_date"]?.toString()),
            followUpDate = if (isFollowUpMode && isTreatmentSavedInDb)
                formatDateForSave(fieldsMap["follow_up_date"]?.toString()) else null,
            diagnosisList = diagnosisList,
            formId = formId,
            formJson = wrappedJson.toString(),
            version = schema.version
        )

        loadVisitHistoryInternal(benId)
        NCDFollowUpSyncWorker.enqueue(context)

    }
    fun getVisibleFields(): List<FormField> {
        return _schema.value?.sections?.flatMap { section ->
            section.fields.filter { it.visible }.map { field ->

                val validation = field.validation?.let {
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
                }

                FormField(
                    fieldId = field.fieldId,
                    label = field.label,
                    type = field.type,
                    options = field.options,
                    isRequired = field.required,
                    placeholder = field.placeholder,
                    validation = validation,
                    visible = field.visible,
                    conditional = field.conditional
                        ?.takeIf { !it.dependsOn.isNullOrBlank() && !it.expectedValue.isNullOrBlank() }
                        ?.let { ConditionalLogic(it.dependsOn.orEmpty(), it.expectedValue.orEmpty()) },
//                    value = if (field.fieldId == "visit_label") field.value ?: "Visit-$visitNo" else field.value,
                    value = if (field.fieldId == "visit_label") {
                        "Visit-$visitNo"
                    } else {
                        field.value
                    },
                    isEditable = field.isEditable,
                    errorMessage = field.errorMessage
                )
            }
        } ?: emptyList()
    }

    private fun parseUiDateStrict(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).apply { isLenient = false }.parse(dateStr)
        } catch (e: Exception) {
            Log.d("FollowUpCheck", "Failed to parse UI date: $dateStr, ${e.message}")
            null
        }
    }
    private fun parseDbDateStrict(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    fun getFollowUpDateErrorFromUI(): Pair<String, Int>? {
        val fields = getVisibleFields()
        val followUpField = fields.firstOrNull { it.fieldId == "follow_up_date" } ?: return null
        val followUpDateStr = followUpField.value as? String
        val followUpDate = parseUiDateStrict(followUpDateStr)
            ?: return "Follow-up date is required or invalid" to 0


        val lastMain = getLastMainVisit() ?: return "Main visit not found" to -1
        val treatmentDate = parseDbDateStrict(lastMain.treatmentStartDate)
            ?: return "Invalid treatment date in DB" to -1
        val currentVisitFollowUps = _visitHistory.value
            .filter { it.visitNo == visitNo && it.followUpNo >= 1 }
            .sortedBy { it.followUpNo }

        if (currentVisitFollowUps.isEmpty()) {
            if (!followUpDate.after(treatmentDate)) {
                return "Follow-up must be after treatment start date (${lastMain.treatmentStartDate})" to 0
            }
        } else {
            val lastFollowUp = currentVisitFollowUps.last()
            val lastFollowCal = Calendar.getInstance().apply { time = parseDbDateStrict(lastFollowUp.followUpDate) ?: treatmentDate }

            val expectedMonth = (lastFollowCal.get(Calendar.MONTH) + 1) % 12
            val expectedYear = if (lastFollowCal.get(Calendar.MONTH) == Calendar.DECEMBER) lastFollowCal.get(Calendar.YEAR) + 1 else lastFollowCal.get(Calendar.YEAR)

            val followCal = Calendar.getInstance().apply { time = followUpDate }
            if (followCal.get(Calendar.MONTH) != expectedMonth || followCal.get(Calendar.YEAR) != expectedYear) {
                return "Follow-up must be in the immediate next month after last follow-up of current visit" to 0
            }
        }

        val today = Calendar.getInstance()
        val followCal = Calendar.getInstance().apply { time = followUpDate }
        val isFutureMonth = followCal.get(Calendar.YEAR) > today.get(Calendar.YEAR) ||
                (followCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        followCal.get(Calendar.MONTH) > today.get(Calendar.MONTH))
        if (isFutureMonth) return "Follow-up cannot be in a future month" to 0

        return null
    }
}
