package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tpt.start

import androidx.lifecycle.SavedStateHandle
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
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.OptionItemParser
import org.piramalswasthya.sakhi.model.dynamicEntity.TPTFollowUpEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.optionItems
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.TPTStartRepository
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TPTStartViewModel @Inject constructor(
    private val repository: TPTStartRepository,
    private val benRepo: BenRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val args = TPTStartFragmentArgs.fromSavedStateHandle(savedStateHandle)
    val benId: Long = args.benId
    val householdId: Long = args.hhId
    val referralFollowUpDate: String = args.referralFollowUpDate

    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema
    private val _history = MutableStateFlow<List<TPTFollowUpEntity>>(emptyList())
    val history: StateFlow<List<TPTFollowUpEntity>> = _history
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val formId = FormConstants.TB_TPT_FOLLOW_UP
    val currentFollowUpNo: Int
        get() = (_history.value.maxOfOrNull { visit ->
            maxOf(visit.followUpNo, monthlyFollowUpNo(visit.fieldsJson))
        } ?: 0) + 1
    val currentVisitNo: Int
        get() = _history.value.maxOfOrNull { it.visitNo } ?: 1
    val isTreatmentCompleted: Boolean
        get() = _history.value.any { isTreatmentCompletedVisit(it, _schema.value) }

    fun loadForm() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _history.value = repository.getFollowUps(benId)
                val loadedSchema = repository.getFormSchema(formId)
                    ?: throw IllegalStateException("TPT form schema is unavailable")
                _schema.value = configureSchema(loadedSchema)
            } catch (exception: Exception) {
                _error.value = exception.message ?: "Unable to load TPT form"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun configureSchema(schema: FormSchemaDto): FormSchemaDto {
        val completedVisit = _history.value
            .filter { isTreatmentCompletedVisit(it, schema) }
            .maxWithOrNull(compareBy<TPTFollowUpEntity> { it.visitNo }.thenBy { it.followUpNo }.thenBy { it.updatedAt })
        val completedVisitFields = completedVisit?.let { parseFields(it.fieldsJson) }
        val mainVisit = _history.value.firstOrNull { !it.treatmentStartDate.isNullOrBlank() }
        val savedFields = mainVisit?.let { parseFields(it.fieldsJson) }
        val treatmentStartMinDate = referralFollowUpDate.takeIf { it.isNotBlank() && !it.equals("null", true) }
            ?.let { parseDate(it)?.let(::formatUiDate) }
        val today = formatUiDate(Date())
        val configuredSections = schema.sections.map { section ->
            section.copy(fields = section.fields.map { field ->
                val treatmentTypeField = isTreatmentTypeField(field.fieldId, field.label)
                val treatmentDateField = isTreatmentStartDateField(field.fieldId, field.label)
                val completionDateField = isCompletionDateField(field.fieldId, field.label)
                val followUpDateField = isFollowUpDateField(field.fieldId, field.label)
                val monthlyField = isMonthlyFollowUpField(field.fieldId, field.label)
                val validation = if (treatmentDateField && mainVisit == null) {
                    (field.validation ?: org.piramalswasthya.sakhi.model.dynamicEntity.FieldValidationDto()).copy(
                        minDate = treatmentStartMinDate ?: field.validation?.minDate,
                        maxDate = today
                    )
                } else field.validation
                when {
                    completedVisit != null -> field.copy(
                        value = savedFieldValue(completedVisitFields, field.fieldId)
                            ?: field.value ?: field.defaultValue ?: field.default,
                        isEditable = false,
                        validation = validation
                    )
                    treatmentTypeField && mainVisit != null -> field.copy(
                        value = mainVisit.treatmentType ?: savedFields?.opt(field.fieldId),
                        isEditable = false
                    )
                    treatmentDateField && mainVisit != null -> field.copy(
                        value = mainVisit.treatmentStartDate ?: savedFields?.opt(field.fieldId),
                        isEditable = false
                    )
                    completionDateField -> field.copy(
                        value = calculateCompletionDate(treatmentTypeValue(schema), treatmentStartDateValue(schema)),
                        isEditable = false
                    )
                    followUpDateField && currentFollowUpNo > 0 -> field.copy(
                        value = null,
                        validation = validation,
                        isEditable = true
                    )
                    monthlyField && currentFollowUpNo > 0 -> field.copy(
                        value = "Month-${currentFollowUpNo.coerceAtMost(6)}",
                        isEditable = false
                    )
                    else -> field.copy(
                        value = field.value ?: field.defaultValue ?: field.default,
                        validation = validation,
                        visible = true
                    )
                }
            })
        }
        val configuredSchema = schema.copy(sections = configuredSections)
        if (completedVisit != null) return refreshConditions(configuredSchema)
        val completionDate = calculateCompletionDate(
            treatmentTypeValue(configuredSchema),
            treatmentStartDateValue(configuredSchema)
        )
        val withCompletionDate = configuredSchema.copy(sections = configuredSchema.sections.map { section ->
            section.copy(fields = section.fields.map { field ->
                if (isCompletionDateField(field.fieldId, field.label)) {
                    field.copy(value = completionDate, isEditable = false)
                } else field
            })
        })
        return refreshConditions(withCompletionDate)
    }

    fun updateFieldValue(fieldId: String, value: Any?) {
        if (isTreatmentCompleted) return
        val current = _schema.value ?: return
        val updated = current.copy(sections = current.sections.map { section ->
            section.copy(fields = section.fields.map { field ->
                if (field.fieldId == fieldId) field.copy(value = value, errorMessage = null) else field
            })
        })
        val completionDate = calculateCompletionDate(treatmentTypeValue(updated), treatmentStartDateValue(updated))
        val withCompletionDate = updated.copy(sections = updated.sections.map { section ->
            section.copy(fields = section.fields.map { field ->
                if (isCompletionDateField(field.fieldId, field.label)) {
                    field.copy(value = completionDate, isEditable = false)
                } else field
            })
        })
        _schema.value = refreshConditions(withCompletionDate)
    }

    private fun refreshConditions(schema: FormSchemaDto): FormSchemaDto {
        val values = schema.sections.flatMap { it.fields }.associateBy { normalizeFieldText(it.fieldId) }
        return schema.copy(sections = schema.sections.map { section ->
            section.copy(fields = section.fields.map { field ->
                val condition = field.conditional
                if (condition == null || condition.dependsOn.isNullOrBlank()) field.copy(visible = true)
                else {
                    val dependency = values[normalizeFieldText(condition.dependsOn)]
                    val actual = dependency?.value?.toString()?.trim().orEmpty()
                    val expected = condition.expectedValue?.trim().orEmpty()
                    val visible = actual.isNotBlank() && (expected.isBlank() || actual.equals(expected, true))
                    field.copy(visible = visible, value = if (visible) field.value else null, errorMessage = null)
                }
            })
        })
    }

    fun getVisibleFields(): List<FormField> = _schema.value?.sections?.flatMap { section ->
        section.fields.filter { it.visible }.map { field ->
            val baseValidation = field.validation
            val priorFollowUp = _history.value.filter { monthlyFollowUpNo(it.fieldsJson) > 0 || it.followUpNo > 0 }
                .maxByOrNull { maxOf(it.followUpNo, monthlyFollowUpNo(it.fieldsJson)) }
            val minFollowUpDate = priorFollowUp?.followUpDate
                ?: _history.value.firstOrNull { !it.treatmentStartDate.isNullOrBlank() }?.treatmentStartDate
                ?: treatmentStartDateValue(_schema.value)
            val tptDateValidation = when {
                isTreatmentStartDateField(field.fieldId, field.label) && _history.value.none { !it.treatmentStartDate.isNullOrBlank() } ->
                    baseValidation?.copy(
                        minDate = referralFollowUpDate.takeIf { it.isNotBlank() && !it.equals("null", true) }
                            ?.let { parseDate(it)?.let(::formatUiDate) }
                            ?: baseValidation.minDate,
                        maxDate = formatUiDate(Date())
                    )
                isFollowUpDateField(field.fieldId, field.label) -> baseValidation?.copy(
                    minDate = minFollowUpDate?.let { value ->
                        parseDate(value)?.let { minimum ->
                            val hasPreviousFollowUp = priorFollowUp != null
                            formatUiDate(if (hasPreviousFollowUp) addDays(minimum, 1) else minimum)
                        } ?: value
                    } ?: baseValidation.minDate,
                    maxDate = formatUiDate(Date()),
                    errorMessage = "Only one TPT follow-up is allowed per month"
                )
                isActualCompletionDateField(field.fieldId, field.label) -> baseValidation?.copy(
                    minDate = minFollowUpDate?.let { value ->
                        parseDate(value)?.let { formatUiDate(addDays(it, 1)) } ?: value
                    } ?: baseValidation.minDate,
                    maxDate = formatUiDate(Date())
                )
                else -> baseValidation
            }
            FormField(
                fieldId = field.fieldId,
                label = field.label,
                type = field.type,
                options = field.optionItems(),
                isRequired = field.required,
                placeholder = field.placeholder,
                validation = tptDateValidation?.let {
                    FieldValidation(
                        min = it.min,
                        max = it.max,
                        minDate = it.minDate,
                        maxDate = it.maxDate,
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
                conditional = field.conditional
                    ?.takeIf { !it.dependsOn.isNullOrBlank() && !it.expectedValue.isNullOrBlank() }
                    ?.let { ConditionalLogic(it.dependsOn.orEmpty(), it.expectedValue.orEmpty()) },
                value = field.value,
                isEditable = field.isEditable,
                errorMessage = field.errorMessage
            )
        }
    }.orEmpty()

    suspend fun saveFormResponses(updatedFields: List<FormField>) {
        if (isTreatmentCompleted) return
        val schema = _schema.value ?: return
        val values = updatedFields.associate { it.fieldId to it.value }
        val updatedSchema = refreshConditions(schema.copy(sections = schema.sections.map { section ->
            section.copy(fields = section.fields.map { field ->
                if (values.containsKey(field.fieldId)) field.copy(value = values[field.fieldId]) else field
            })
        }))
        val fieldsObject = JSONObject()
        updatedSchema.sections.flatMap { it.fields }.forEach { field ->
            fieldsObject.put(
                field.fieldId,
                if (field.visible) field.value?.toString().orEmpty() else ""
            )
        }
        val mainVisit = _history.value.firstOrNull { !it.treatmentStartDate.isNullOrBlank() }
        val regimen = updatedSchema.sections.flatMap { it.fields }
            .firstOrNull { isTreatmentTypeField(it.fieldId, it.label) }
            ?.value?.toString() ?: mainVisit?.treatmentType
        val treatmentDate = updatedSchema.sections.flatMap { it.fields }
            .firstOrNull { isTreatmentStartDateField(it.fieldId, it.label) }
            ?.value?.toString() ?: mainVisit?.treatmentStartDate
        val followUpDate = updatedSchema.sections.flatMap { it.fields }
            .firstOrNull { isFollowUpDateField(it.fieldId, it.label) }
            ?.value?.toString()

        repository.saveLocally(
            TPTFollowUpEntity(
                benId = benId,
                houseHoldId = householdId,
                visitNo = currentVisitNo,
                followUpNo = currentFollowUpNo,
                treatmentType = regimen,
                treatmentStartDate = treatmentDate,
                followUpDate = followUpDate,
                formId = formId,
                version = schema.version,
                fieldsJson = fieldsObject.toString()
            )
        )
        val schemaFields = updatedSchema.sections.flatMap { it.fields }
        val outcomeField = schemaFields.firstOrNull { isTreatmentOutcomeField(it.fieldId, it.label) }
        val outcomeValue = outcomeField?.value?.toString().orEmpty()
        val outcomeLabel = OptionItemParser.parse(outcomeField?.options)
            ?.firstOrNull { it.value.equals(outcomeValue, ignoreCase = true) }
            ?.label.orEmpty()
        if (listOf(outcomeValue, outcomeLabel).any(::isDeathOutcome)) {
            benRepo.getBenFromId(benId)?.let { beneficiary ->
                beneficiary.isDeath = true
                beneficiary.isDeathValue = "Death"
                beneficiary.dateOfDeath = schemaFields.firstOrNull {
                    isDateOfDeathField(it.fieldId, it.label)
                }?.value?.toString()?.takeIf { it.isNotBlank() }
                beneficiary.reasonOfDeath = schemaFields.firstOrNull {
                    isReasonOfDeathField(it.fieldId, it.label)
                }?.value?.toString()?.takeIf { it.isNotBlank() }
                beneficiary.placeOfDeath = schemaFields.firstOrNull {
                    isPlaceOfDeathField(it.fieldId, it.label)
                }?.value?.toString()?.takeIf { it.isNotBlank() }
                if (beneficiary.processed != "N") beneficiary.processed = "U"
                beneficiary.syncState = SyncState.UNSYNCED
                benRepo.updateRecord(beneficiary)
            }
        }
        _history.value = repository.getFollowUps(benId)
        _schema.value = configureSchema(updatedSchema)
    }

    fun followUpSchedule(): List<String> {
        return _history.value
            .filter { maxOf(it.followUpNo, monthlyFollowUpNo(it.fieldsJson)) > 0 }
            .sortedBy { maxOf(it.followUpNo, monthlyFollowUpNo(it.fieldsJson)) }
            .mapNotNull { visit ->
                val date = visit.followUpDate?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                "Follow-up ${maxOf(visit.followUpNo, monthlyFollowUpNo(visit.fieldsJson))} | $date (Completed)"
            }
    }

    fun isFollowUpLimitReached(): Boolean {
        // The form supports monthly follow-ups from Month-1 through Month-6.
        // Keep the submit action available for the next monthly visit even when
        // the treatment course duration is shorter, so completion/outcome can be recorded.
        return currentFollowUpNo > 6
    }

    fun validateTreatmentStartDate(): String? {
        if (_history.value.any { !it.treatmentStartDate.isNullOrBlank() }) return null
        val startDate = parseDate(treatmentStartDateValue(_schema.value))
            ?: return "Enter a valid treatment start date"
        val minimumStartDate = referralFollowUpDate.takeIf { it.isNotBlank() && !it.equals("null", true) }
            ?.let(::parseDate)
        if (minimumStartDate != null && startDate.before(minimumStartDate)) {
            return "Treatment start date must be on or after the referral follow-up date"
        }
        if (startDate.after(Date())) return "Treatment start date cannot be after today"
        return null
    }

    fun validateFollowUpDates(): String? {
        val start = parseDate(treatmentStartDateValue(_schema.value))
            ?: _history.value.firstOrNull { !it.treatmentStartDate.isNullOrBlank() }?.treatmentStartDate?.let(::parseDate)
        val followUp = _schema.value?.sections?.flatMap { it.fields }
            ?.firstOrNull { isFollowUpDateField(it.fieldId, it.label) }?.value?.toString()?.let(::parseDate)
            ?: return "Enter a valid follow-up date"
        if (followUp.after(Date())) return "Follow-up date cannot be after today"
        val previousDates = _history.value
            .filter { it.followUpNo > 0 || monthlyFollowUpNo(it.fieldsJson) > 0 }
            .mapNotNull { it.followUpDate?.let(::parseDate) }
        val previous = previousDates.maxByOrNull { it.time } ?: start
        if (previous != null && (if (previousDates.isEmpty()) followUp.before(previous) else !followUp.after(previous))) {
            return "Follow-up date must be after the previous visit"
        }
        val cal = Calendar.getInstance().apply { time = followUp }
        if (previousDates.any { date ->
                Calendar.getInstance().apply { time = date }.let {
                    it.get(Calendar.YEAR) == cal.get(Calendar.YEAR) && it.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                }
            }) return "Only one TPT follow-up is allowed per month"
        val actualCompletion = _schema.value?.sections?.flatMap { it.fields }
            ?.firstOrNull { isActualCompletionDateField(it.fieldId, it.label) }?.value?.toString()?.let(::parseDate)
        if (actualCompletion != null && (actualCompletion.after(Date()) || !actualCompletion.after(followUp))) {
            return "Actual treatment completion date must be after the follow-up date and cannot be a future date"
        }
        return null
    }

    private fun treatmentTypeValue(schema: FormSchemaDto?): Any? = schema?.sections
        ?.flatMap { it.fields }?.firstOrNull { isTreatmentTypeField(it.fieldId, it.label) }?.value

    private fun treatmentStartDateValue(schema: FormSchemaDto?): String? = schema?.sections
        ?.flatMap { it.fields }
        ?.firstOrNull { isTreatmentStartDateField(it.fieldId, it.label) }
        ?.value?.toString()

    private fun normalizeFieldText(value: String) = value.lowercase(Locale.ENGLISH).filter(Char::isLetterOrDigit)

    private fun isTreatmentCompletedVisit(visit: TPTFollowUpEntity, schema: FormSchemaDto?): Boolean {
        val savedFields = parseFields(visit.fieldsJson) ?: return false
        val completionField = schema?.sections?.flatMap { it.fields }
            ?.firstOrNull { isTreatmentCompletedField(it.fieldId, it.label) }
        val value = if (completionField != null) {
            savedFieldValue(savedFields, completionField.fieldId)
        } else {
            val key = savedFields.keys().asSequence()
                .firstOrNull { isTreatmentCompletedField(it, it) }
            key?.let { savedFieldValue(savedFields, it) }
        }
        return normalizeFieldText(value?.toString().orEmpty()) in setOf("yes", "true", "1")
    }

    private fun savedFieldValue(fields: JSONObject?, fieldId: String): Any? =
        fields?.opt(fieldId)?.takeUnless { it == JSONObject.NULL }

    private fun isTreatmentCompletedField(fieldId: String, label: String): Boolean {
        val id = normalizeFieldText(fieldId)
        val normalizedLabel = normalizeFieldText(label)
        return id.contains("treatmentcompleted") || normalizedLabel.contains("treatmentcompleted")
    }

    private fun isTreatmentOutcomeField(fieldId: String, label: String): Boolean {
        val normalizedId = normalizeFieldText(fieldId)
        val normalizedLabel = normalizeFieldText(label)
        return normalizedId.contains("tptoutcome") ||
                normalizedId.contains("treatmentoutcome") ||
                normalizedLabel.contains("tptoutcome") ||
                normalizedLabel.contains("treatmentoutcome")
    }

    private fun isDateOfDeathField(fieldId: String, label: String): Boolean {
        val id = normalizeFieldText(fieldId)
        val normalizedLabel = normalizeFieldText(label)
        return id.contains("dateofdeath") || id.contains("deathdate") ||
                normalizedLabel.contains("dateofdeath") || normalizedLabel.contains("deathdate")
    }

    private fun isReasonOfDeathField(fieldId: String, label: String): Boolean {
        val id = normalizeFieldText(fieldId)
        val normalizedLabel = normalizeFieldText(label)
        return id.contains("reasonfordeath") || id.contains("deathreason") ||
                normalizedLabel.contains("reasonfordeath") || normalizedLabel.contains("deathreason")
    }

    private fun isPlaceOfDeathField(fieldId: String, label: String): Boolean {
        val id = normalizeFieldText(fieldId)
        val normalizedLabel = normalizeFieldText(label)
        return id.contains("placeofdeath") || id.contains("deathplace") ||
                normalizedLabel.contains("placeofdeath") || normalizedLabel.contains("deathplace")
    }

    private fun isDeathOutcome(value: String): Boolean {
        val normalized = normalizeFieldText(value)
        return normalized == "death" || normalized == "deceased" || normalized == "died"
    }

    private fun isTreatmentTypeField(fieldId: String, label: String = ""): Boolean {
        val combined = "${normalizeFieldText(fieldId)} ${normalizeFieldText(label)}"
        return combined.contains("regimen") || combined.contains("treatmenttype")
    }

    private fun isTreatmentStartDateField(fieldId: String, label: String = "") =
        normalizeFieldText(fieldId).contains("treatmentstartdate") ||
                normalizeFieldText(label).contains("treatmentstartdate")

    private fun isCompletionDateField(fieldId: String, label: String = "") =
        normalizeFieldText(fieldId).contains("expectedtreatmentcompletiondate") ||
                normalizeFieldText(label).contains("expectedtreatmentcompletiondate")

    private fun isFollowUpDateField(fieldId: String, label: String = "") =
        normalizeFieldText(fieldId).contains("followupdate") ||
                normalizeFieldText(label).contains("followupdate")

    private fun isActualCompletionDateField(fieldId: String, label: String = "") =
        normalizeFieldText(fieldId).contains("actualcompletiondate") ||
                normalizeFieldText(label).contains("actualtreatmentcompletiondate")

    private fun isMonthlyFollowUpField(fieldId: String, label: String = "") =
        normalizeFieldText(fieldId).contains("monthlyfollowup") ||
                normalizeFieldText(label).contains("monthlyfollowup")

    private fun durationMonths(value: Any?): Int? {
        val regimen = value?.toString()?.trim()?.uppercase(Locale.ENGLISH) ?: return null
        return when {
            regimen.contains("6H") -> 6
            regimen.contains("3RH") -> 3
            regimen.contains("3HP") -> 3
            regimen.contains("6LFX") -> 6
            regimen.contains("4R") -> 4
            regimen.contains("1HP") -> 1
            regimen == "1" -> 6
            regimen == "2" -> 3
            regimen == "3" -> 3
            regimen == "4" -> 6
            regimen == "5" -> 4
            regimen == "6" -> 1
            else -> null
        }
    }

    private fun calculateCompletionDate(regimen: Any?, treatmentDate: String?): String? {
        val duration = durationMonths(regimen) ?: return null
        val date = parseDate(treatmentDate) ?: return null
        return formatUiDate(addMonths(date, duration))
    }

    private fun addMonths(date: Date, months: Int): Date {
        val calendar = Calendar.getInstance().apply { time = date }
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MONTH, months)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)))
        return calendar.time
    }

    private fun addDays(date: Date, days: Int): Date = Calendar.getInstance().apply {
        time = date
        add(Calendar.DAY_OF_MONTH, days)
    }.time

    private fun parseDate(value: String?): Date? {
        if (value.isNullOrBlank()) return null
        return listOf("dd-MM-yyyy", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss")
            .firstNotNullOfOrNull { pattern ->
                try { SimpleDateFormat(pattern, Locale.ENGLISH).apply { isLenient = false }.parse(value) }
                catch (_: Exception) { null }
            }
    }

    private fun formatUiDate(value: Date): String =
        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(value)

    private fun formatUiDate(value: Long): String = formatUiDate(Date(value))

    private fun parseFields(json: String): JSONObject? = try {
        val root = JSONObject(json)
        root.optJSONObject("fields") ?: root
    } catch (_: Exception) {
        null
    }

    private fun monthlyFollowUpNo(fieldsJson: String): Int =
        Regex("Month-(\\d+)", RegexOption.IGNORE_CASE)
            .find(parseFields(fieldsJson)?.optString("monthly_follow_up").orEmpty())
            ?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
}
