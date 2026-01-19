package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.filaria_mda

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
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
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FilariaMdaCampaignJsonDao
import org.piramalswasthya.sakhi.model.BottleItem
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.filariaaMdaCampaign.FilariaMDACampaignFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicModel.MDACampaignItem
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FilariaMdaCampaignRepository
import org.piramalswasthya.sakhi.work.dynamicWoker.FilariaMDAFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.FilariaMdaCampaignPushWorker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.collections.orEmpty

@HiltViewModel
class FilariaMdaFormCampaignViewModel @Inject constructor(
    private val repository: FilariaMdaCampaignRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    var dao: FilariaMdaCampaignJsonDao

    ) : ViewModel() {

    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema
    private val _infant = MutableStateFlow<FilariaMDACampaignFormResponseJsonEntity?>(null)
    val infant: StateFlow<FilariaMDACampaignFormResponseJsonEntity?> = _infant
    var previousVisitDate: Date? = null
    private val _syncedVisitList = MutableStateFlow<List<FilariaMDACampaignFormResponseJsonEntity>>(emptyList())

    var visitDay: String = ""
    var isViewMode: Boolean = false
    private val _isBenDead = MutableStateFlow(false)
    val isBenDead: StateFlow<Boolean> = _isBenDead

    private val _bottleList = MutableLiveData<List<MDACampaignItem>>()
    val bottleList: LiveData<List<MDACampaignItem>> = _bottleList
    private val _showToastLiveData = MutableLiveData<String>()
    val showToastLiveData: LiveData<String> = _showToastLiveData

    var yearDate = FilariaMdaCampaignFormFragmentArgs.fromSavedStateHandle(savedStateHandle).date
    var wasDuplicate = false
        private set



    fun getCurrentYear(): String {
        return SimpleDateFormat("yyyy", Locale.getDefault())
            .format(Date())
    }
    private val _isCampaignAlreadyAdded = MutableLiveData(false)
    val isCampaignAlreadyAdded: LiveData<Boolean> = _isCampaignAlreadyAdded

    fun checkCurrentYearCampaign(formId: String) {
        viewModelScope.launch {
            val currentYear = getCurrentYear()
            val existing = dao.getCampaignByBenFormYear(formId, currentYear)
            _isCampaignAlreadyAdded.postValue(existing != null)
        }
    }


    fun loadBottleData() {
        viewModelScope.launch {
            val list = repository.getBottleList()
            _bottleList.postValue(list)
        }
    }

    fun loadSyncedVisitList() {
        viewModelScope.launch {
            val list = repository.getSyncedVisitsByRchId()
            _syncedVisitList.value = list
        }
    }

    fun loadFormSchema(
        formId: String,
        viewMode: Boolean,
    ) {
        this.isViewMode = viewMode
        loadSyncedVisitList()

        viewModelScope.launch {
            val cachedSchemaEntity = repository.getSavedSchema(formId)
            val cachedSchema: FormSchemaDto? = cachedSchemaEntity?.let {
                FormSchemaDto.fromJson(it.schemaJson)
            }

            val localSchemaToRender = cachedSchema ?: repository.getFormSchema(formId) ?: return@launch

            val savedJson = repository.loadFormResponseJson(yearDate)
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
            localSchemaToRender.sections.forEach { section ->
                section.fields.forEach { field ->
                    field.value = when (field.fieldId) {
                        "visit_day" -> visitDay
                        else -> savedFieldValues[field.fieldId] ?: field.default
                    }

                    field.isEditable = when (field.fieldId) {
                        "visit_day", "due_date" -> false
                        else -> !viewMode
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
        val out = SimpleDateFormat("yyyy", Locale.getDefault())
        for (fmt in inputs) {
            try {
                val d = SimpleDateFormat(fmt, Locale.getDefault()).parse(dateStr)
                if (d != null) return out.format(d)
            } catch (_: Exception) {}
        }
        return try {
            if (Regex("\\d{2}-\\d{2}-\\d{4}").matches(dateStr)) {
                dateStr.substring(6, 10)
            } else ""
        } catch (_: Exception) { "" }
    }

    suspend fun saveFormResponses(benId: Long, hhId: Long): Boolean {
        return try {
            val currentSchema = _schema.value ?: return false
            val formId = currentSchema.formId
            val version = currentSchema.version
            val beneficiaryId = benId

            val fieldMap = currentSchema.sections
                .flatMap { it.fields }
                .filter { it.visible && it.value != null }
                .associate { it.fieldId to it.value }

            val visitDate = fieldMap["start_date"]?.toString() ?: "N/A"
            val visitMonth = toMonthKey(visitDate)

            val wrappedJson = JSONObject().apply {
                put("formId", formId)
                put("beneficiaryId", beneficiaryId)
                put("houseHoldId", hhId)
                put("visitDate", visitDate)
                put("fields", JSONObject(fieldMap))
            }

            val entity = FilariaMDACampaignFormResponseJsonEntity(
                visitDate = visitDate,
                visitYear = visitMonth,
                formId = formId,
                version = version,
                formDataJson = wrappedJson.toString(),
                isSynced = false,
                syncedAt = null
            )

            val inserted = repository.insertFormResponse(entity)

            if (!inserted) {
                wasDuplicate = true
                _showToastLiveData.postValue("You have already submitted this form for this Year")
                return false
            }

            wasDuplicate = false

            loadSyncedVisitList()
            FilariaMdaCampaignPushWorker.enqueue(context)

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

    fun getStartDateMin(): Date {
        val cal = Calendar.getInstance()
        cal.time = Date()

        cal.add(Calendar.MONTH, -1)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return cal.time
    }

    fun getStartDateMax(): Date {
        val cal = Calendar.getInstance()
        cal.time = Date()

        cal.add(Calendar.MONTH, 2)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)

        return cal.time
    }
}
