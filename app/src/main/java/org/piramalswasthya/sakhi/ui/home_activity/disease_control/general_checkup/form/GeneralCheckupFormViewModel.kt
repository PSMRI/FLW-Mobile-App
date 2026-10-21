package org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.form

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.ConditionalLogic
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FieldValidation
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.CheckupPriority
import org.piramalswasthya.sakhi.model.GeneralCheckupCache
import org.piramalswasthya.sakhi.model.HealthProblem
import org.piramalswasthya.sakhi.model.HealthProblemOwnerModule
import org.piramalswasthya.sakhi.model.HealthProblemType
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.OptionItem
import org.piramalswasthya.sakhi.model.dynamicEntity.optionItems
import org.piramalswasthya.sakhi.model.formDataToJson
import org.piramalswasthya.sakhi.model.monthKey
import org.piramalswasthya.sakhi.repositories.GeneralCheckupRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.form.GeneralCheckupFormSchema.Companion.FIELD_DATE
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.form.GeneralCheckupFormSchema.Companion.FIELD_OTHER
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.form.GeneralCheckupFormSchema.Companion.FIELD_TYPE
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.form.GeneralCheckupFormSchema.Companion.OTHER_MAX_LENGTH
import org.piramalswasthya.sakhi.utils.dynamicFiledValidator.FieldValidator
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * Add Check-up form state (BRD FR3–FR6, prototype v0.5 §2.6). The fields come from the server-driven
 * `general_check_up` schema ([GeneralCheckupFormSchema]) and are drawn by FormRendererAdapter; the
 * history, banners, routing, duplicate check and refer flow stay native.
 */
@HiltViewModel
class GeneralCheckupFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val preferenceDao: PreferenceDao,
    private val benDao: BenDao,
    private val formRepository: FormRepository,
    private val generalCheckupRepo: GeneralCheckupRepo,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    sealed class SaveState {
        object Saved : SaveState()
        data class Duplicate(val existing: GeneralCheckupCache) : SaveState()
        object Failed : SaveState()
    }

    sealed class FormState {
        object Loading : FormState()
        object Ready : FormState()
        /** The schema is neither saved on the device nor reachable. */
        object Unavailable : FormState()
        /** A schema came from the server but has no usable problem list (see the log for why). */
        object Invalid : FormState()
    }

    private val args = GeneralCheckupFormFragmentArgs.fromSavedStateHandle(savedStateHandle)
    val benId = args.benId
    val hhId = args.hhId

    val formId = FormConstants.GENERAL_CHECKUP_FORM_ID

    private val _benName = MutableLiveData<String>()
    val benName: LiveData<String> get() = _benName

    private val _benDetails = MutableLiveData<String>()
    val benDetails: LiveData<String> get() = _benDetails

    private val _formState = MutableLiveData<FormState>(FormState.Loading)
    val formState: LiveData<FormState> get() = _formState

    /** Earlier check-ups, newest first (FR14). */
    private val _history = MutableLiveData<List<GeneralCheckupCache>>(emptyList())
    val history: LiveData<List<GeneralCheckupCache>> get() = _history

    private val _saveState = MutableLiveData<SaveState?>(null)
    val saveState: LiveData<SaveState?> get() = _saveState

    /** Bumped whenever an answer or an error changes, so the fragment redraws the fields and banners. */
    private val _formChanged = MutableLiveData(Unit)
    val formChanged: LiveData<Unit> get() = _formChanged

    val minDate: Long get() = startOfDay(Calendar.getInstance().apply { add(Calendar.MONTH, -1) })
    val maxDate: Long get() = System.currentTimeMillis()

    private var form: GeneralCheckupFormSchema? = null

    /** What is wrong with the server's form, in plain words; set when the state is [FormState.Invalid]. */
    var formIssues: String? = null
        private set

    /** Answers by fieldId: String for date/dropdown/text, Set<String> for a multicheckbox. */
    private val values = mutableMapOf<String, Any?>()

    /** Validation errors by fieldId. */
    private val errors = mutableMapOf<String, String>()

    init {
        values[FIELD_DATE] = uiDateFormat().format(System.currentTimeMillis())
        viewModelScope.launch {
            benDao.getBen(benId)?.let { ben ->
                _benName.value = listOfNotNull(ben.firstName, ben.lastName).joinToString(" ")
                _benDetails.value = "${ben.age} ${ben.ageUnit?.name.orEmpty()} | ${ben.gender?.name.orEmpty()} | ID $benId"
            }
        }
        viewModelScope.launch {
            generalCheckupRepo.getHistory(benId).collectLatest { _history.value = it }
        }
        loadForm()
    }

    /**
     * Saved schema first (downloaded with the other forms on the home screen). If there is none in
     * the user's language, or it cannot be used, the server is asked, so Try again picks up a fix.
     */
    fun loadForm() {
        viewModelScope.launch {
            _formState.value = FormState.Loading
            formIssues = null
            val language = preferenceDao.getCurrentLanguage().symbol
            var schema: FormSchemaDto? = null
            form = try {
                schema = savedSchema(language)
                schema?.let { GeneralCheckupFormSchema.parse(it) } ?: run {
                    schema = formRepository.getFormSchema(formId, language) ?: schema
                    schema?.let { GeneralCheckupFormSchema.parse(it) }
                }
            } catch (e: Exception) {
                Timber.e(e, "General Check-up form schema could not be loaded")
                null
            }
            _formState.value = when {
                form != null -> FormState.Ready
                schema != null -> {
                    formIssues = schema?.let { GeneralCheckupFormSchema.issueReport(it) }
                    Timber.w("General Check-up form (%s) is not usable. %s", language, formIssues)
                    FormState.Invalid
                }
                else -> FormState.Unavailable
            }
            _formChanged.value = Unit
        }
    }

    private suspend fun savedSchema(language: String): FormSchemaDto? =
        formRepository.getSavedSchema(formId)
            ?.takeIf { it.language == language }
            ?.let { runCatching { FormSchemaDto.fromJson(it.schemaJson) }.getOrNull() }

    // ── Answers ────────────────────────────────────────────────────────────────

    fun onFieldChanged(fieldId: String, value: Any?) {
        val normalized: Any? = when (value) {
            is Collection<*> -> value.mapNotNull { it?.toString() }.toCollection(LinkedHashSet())
            null -> null
            else -> value.toString()
        }
        if (fieldId == FIELD_TYPE && normalized != values[FIELD_TYPE]) {
            // A new type starts a fresh problem list (prototype §2.6).
            form?.problemTypes?.forEach { values.remove(it.problemFieldId); errors.remove(it.problemFieldId) }
            values.remove(FIELD_OTHER)
            errors.remove(FIELD_OTHER)
        }
        values[fieldId] = normalized
        errors.remove(fieldId)
        _formChanged.value = Unit
    }

    val selectedProblemType: HealthProblemType?
        get() = form?.problemType(values[FIELD_TYPE] as? String)

    /** Ticked problems of the selected type, in the server's order. */
    private val selectedProblems: List<HealthProblem>
        get() {
            val type = selectedProblemType ?: return emptyList()
            val ticked = (values[type.problemFieldId] as? Set<*>).orEmpty()
            return type.problems.filter { it.problemId in ticked }
        }

    private val otherText: String get() = (values[FIELD_OTHER] as? String).orEmpty().trim()

    val hasSelection get() = selectedProblems.isNotEmpty()
    val isOtherSelected get() = selectedProblems.any { it.needsText }
    val priority
        get() = if (selectedProblems.any { it.emergency }) CheckupPriority.EMERGENCY else CheckupPriority.ROUTINE

    /** First ticked problem owned by another module (FR6), or null. */
    val ownedProblem get() = selectedProblems.firstOrNull { it.ownerModule != null }

    /** Check-up date at local midnight; null if not set or unreadable. */
    val checkupDate: Long?
        get() = (values[FIELD_DATE] as? String)?.let { runCatching { uiDateFormat().parse(it)?.time }.getOrNull() }

    // ── Fields for the renderer ────────────────────────────────────────────────

    /**
     * Visible fields in schema order, with answers and errors applied. [otherRequiredMessage] shows
     * under the Other box as soon as it appears, until she starts typing (FLW-1229 item 4).
     */
    fun visibleFields(
        ownerTitle: (HealthProblemOwnerModule) -> String,
        otherRequiredMessage: String? = null
    ): List<FormField> {
        val form = form ?: return emptyList()
        return form.fields.filter { isVisible(form, it) }.map { field ->
            val problemType = form.typeOfProblemField(field.fieldId)
            FormField(
                fieldId = field.fieldId,
                label = field.label,
                type = field.type,
                defaultValue = field.defaultValue ?: field.default?.toString(),
                isRequired = field.required || field.fieldId == FIELD_OTHER,
                options = problemType?.problems?.map { problem ->
                    OptionItem(
                        label = problem.ownerModule?.let { "${problem.label} (${ownerTitle(it)})" } ?: problem.label,
                        value = problem.problemId,
                        highlight = problem.emergency
                    )
                } ?: field.optionItems(),
                value = values[field.fieldId],
                visible = true,
                placeholder = field.placeholder,
                validation = field.validation?.let {
                    FieldValidation(
                        min = it.min, max = it.max, minDate = it.minDate, maxDate = it.maxDate,
                        maxLength = it.maxLength, regex = it.regex, errorMessage = it.errorMessage,
                        decimalPlaces = it.decimalPlaces, maxSizeMB = it.maxSizeMB,
                        afterField = it.afterField, beforeField = it.beforeField
                    )
                },
                conditional = field.conditional?.let { c ->
                    if (c.dependsOn.isNullOrBlank() || c.expectedValue == null) null
                    else ConditionalLogic(c.dependsOn, c.expectedValue)
                },
                errorMessage = errors[field.fieldId]
                    ?: otherRequiredMessage.takeIf { field.fieldId == FIELD_OTHER && values[FIELD_OTHER] == null }
            )
        }
    }

    private fun isVisible(form: GeneralCheckupFormSchema, field: FormFieldDto): Boolean {
        form.typeOfProblemField(field.fieldId)?.let { return it.typeId == selectedProblemType?.typeId }
        if (field.fieldId == FIELD_OTHER) return isOtherSelected
        // Any other server field: the engine's single-equality condition.
        val condition = field.conditional ?: return true
        val dependsOn = condition.dependsOn?.takeIf { it.isNotBlank() } ?: return true
        val actual = values[dependsOn]?.toString().orEmpty()
        val expected = condition.expectedValue?.trim().orEmpty()
        return actual.isNotBlank() && (expected.isBlank() || actual.equals(expected, ignoreCase = true))
    }

    // ── Validation and save ────────────────────────────────────────────────────

    /**
     * Validates in the prototype's order (date, type, problems, Other) and then any extra server
     * fields. Errors show under the fields; returns the first message, or null when valid.
     */
    fun validate(context: Context, ownerTitle: (HealthProblemOwnerModule) -> String): String? {
        errors.clear()
        val fields = visibleFields(ownerTitle)
        fields.forEach { field ->
            val error = when (field.fieldId) {
                FIELD_DATE -> checkupDate.let { date ->
                    if (date == null || date < minDate || date > maxDate) context.getString(R.string.gc_date_hint) else null
                }
                FIELD_TYPE -> if (selectedProblemType == null) context.getString(R.string.gc_err_type) else null
                FIELD_OTHER -> when {
                    otherText.isBlank() -> context.getString(R.string.gc_other_required)
                    otherText.length > OTHER_MAX_LENGTH -> context.getString(R.string.gc_err_other_length, OTHER_MAX_LENGTH)
                    else -> null
                }
                else -> if (form?.typeOfProblemField(field.fieldId) != null) {
                    if (selectedProblems.isEmpty()) context.getString(R.string.gc_err_problem) else null
                } else {
                    FieldValidator.validate(field, null, context = context).takeUnless { it.isValid }?.errorMessage
                }
            }
            if (error != null) errors[field.fieldId] = error
        }
        _formChanged.value = Unit
        return fields.firstNotNullOfOrNull { errors[it.fieldId] }
    }

    /** Reason for referral sent to the server, always in English: "General Check-up: Fever, Other: …". */
    fun referralReason(): String = REASON_PREFIX + selectedProblems.joinToString(", ") {
        if (it.needsText) "${it.labelEn}: $otherText" else it.labelEn
    }

    private val otherProblemIds: List<String>
        get() = selectedProblemType?.problems.orEmpty().filter { it.needsText }.map { it.problemId }

    /** Dedup check before opening the refer form, on the check-up date (the refer form's default date). */
    suspend fun findDuplicate(): GeneralCheckupCache? = generalCheckupRepo.findDuplicate(
        benId, checkupDate ?: System.currentTimeMillis(),
        selectedProblems.map { it.problemId }, otherProblemIds, otherText
    )

    /** Every answer of a visible field, for formData (multi-select values comma-separated). */
    private fun answers(): Map<String, String> {
        val form = form ?: return emptyMap()
        return form.fields.filter { isVisible(form, it) }.mapNotNull { field ->
            val text = when (val v = values[field.fieldId]) {
                is Collection<*> -> v.joinToString(",")
                null -> null
                else -> v.toString().trim()
            }
            text?.takeIf { it.isNotEmpty() }?.let { field.fieldId to it }
        }.toMap()
    }

    /** Referral returned by the refer form; saved with the check-up in [saveForm] (same as TB screening). */
    var referralCache: ReferalCache? = null
        private set

    fun addReferral(referral: ReferalCache) {
        referralCache = referral
    }

    fun saveForm() {
        referralCache?.let { onReferralSubmitted(it) }
    }

    /** The refer form came back: save the check-up and its referral together (dedup re-checked on the referral date). */
    fun onReferralSubmitted(referral: ReferalCache) {
        val form = form ?: return
        val problemType = selectedProblemType ?: return
        val problems = selectedProblems
        val date = checkupDate ?: return
        viewModelScope.launch {
            try {
                val user = preferenceDao.getLoggedInUser()
                referral.benId = benId
                referral.type = REFERRAL_TYPE
                referral.priority = priority.name
                val checkup = GeneralCheckupCache(
                    checkupId = UUID.randomUUID().toString(),
                    benId = benId,
                    hhId = hhId,
                    ashaId = user?.userId ?: 0,
                    checkupDate = date,
                    problemTypeId = problemType.typeId,
                    problemTypeLabel = problemType.labelEn,
                    problemIds = problems.map { it.problemId },
                    problemLabels = problems.map { it.labelEn },
                    otherText = otherText.takeIf { isOtherSelected && it.isNotEmpty() },
                    priority = priority.name,
                    masterVersion = form.version,
                    referralReason = referral.referralReason ?: referralReason(),
                    referralDate = referral.revisitDate,
                    referralMonth = monthKey(referral.revisitDate),
                    formData = formDataToJson(answers()),
                    createdBy = user?.userName.orEmpty()
                )
                _saveState.value = when (val result =
                    generalCheckupRepo.saveCheckupWithReferral(checkup, referral, otherProblemIds)) {
                    GeneralCheckupRepo.SaveResult.Saved -> {
                        WorkerUtils.triggerAmritPushWorker(appContext)
                        SaveState.Saved
                    }
                    is GeneralCheckupRepo.SaveResult.Duplicate -> SaveState.Duplicate(result.existing)
                }
            } catch (e: Exception) {
                Timber.e(e, "General Check-up save failed")
                _saveState.value = SaveState.Failed
            }
        }
    }

    fun onSaveStateHandled() {
        _saveState.value = null
    }

    /** One history row: date, type and problems. */
    fun historyLine(checkup: GeneralCheckupCache): String {
        val problems = checkup.problemLabels.joinToString(", ") +
                checkup.otherText?.let { " ($it)" }.orEmpty()
        return "${uiDateFormat().format(checkup.checkupDate)} · ${checkup.problemTypeLabel}\n$problems"
    }

    companion object {
        const val REFERRAL_TYPE = "GENERAL_CHECKUP"
        private const val REASON_PREFIX = "General Check-up: "

        /** The form engine's date format. */
        private fun uiDateFormat() = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

        private fun startOfDay(cal: Calendar): Long = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
