package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.mosquito_net

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
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.mosquitonetEntity.MosquitoNetFormResponseJsonEntity
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MosquitoNetFormViewModel @Inject constructor(
    private val repository: FormRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema
    private val _infant = MutableStateFlow<MosquitoNetFormResponseJsonEntity?>(null)
    val infant: StateFlow<MosquitoNetFormResponseJsonEntity?> = _infant
    var previousVisitDate: Date? = null
    private val _syncedVisitList = MutableStateFlow<List<FormResponseJsonEntity>>(emptyList())

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
        viewMode: Boolean,
    ) {
        this.isViewMode = viewMode
        loadSyncedVisitList(benId)

        viewModelScope.launch {
            val cachedSchemaEntity = repository.getSavedSchema(formId)
            val cachedSchema: FormSchemaDto? = cachedSchemaEntity?.let {
                FormSchemaDto.fromJson(it.schemaJson)
            }

            val localSchemaToRender = cachedSchema ?: repository.getFormSchema(formId) ?: return@launch

            val savedJson = repository.loadFormResponseJson(benId, formId)
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
        val out = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        for (fmt in inputs) {
            try {
                val d = SimpleDateFormat(fmt, Locale.getDefault()).parse(dateStr)
                if (d != null) return out.format(d)
            } catch (_: Exception) {}
        }
        return try {
            if (Regex("\\d{2}-\\d{2}-\\d{4}").matches(dateStr)) {
                val yyyy = dateStr.substring(6, 10)
                val mm = dateStr.substring(3, 5)
                "$yyyy-$mm"
            } else ""
        } catch (_: Exception) { "" }
    }

    suspend fun saveFormResponses(benId: Long, hhId: Long) : Boolean {
        return try{
            val currentSchema = _schema.value ?: return false
            val formId = currentSchema.formId
            val version = currentSchema.version
            val beneficiaryId = benId

            val fieldMap = currentSchema.sections.orEmpty()
                .flatMap { it.fields.orEmpty() }
                .filter { it.visible && it.value != null }
                .associate { it.fieldId to it.value }

            val visitDate = fieldMap["visit_date"]?.toString() ?: "N/A"
            val visitMonth = toMonthKey(visitDate)

            val wrappedJson = JSONObject().apply {
                put("formId", formId)
                put("beneficiaryId", beneficiaryId)
                put("houseHoldId", hhId)
                put("visitDate", visitDate)
                put("fields", JSONObject(fieldMap))
            }

            val entity = MosquitoNetFormResponseJsonEntity(
                benId = benId,
                hhId = hhId,
                visitDate = visitDate,
                visitDay = "",
                formId = formId,
                version = version,
                formDataJson = wrappedJson.toString(),
                isSynced = false,
                createdAt = System.currentTimeMillis(),
                syncedAt = null
            )

//            repository.insertFormResponse(entity)

            loadSyncedVisitList(benId)
//            EyeSurgeryFormSyncWorker.enqueue(context)

            true
        }catch (e: Exception){
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

    fun getMaxVisitDate(): Date {
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = todayCal.time
        val todayMonthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(today)

        val alreadyInThisMonth = _syncedVisitList.value.any {
            it.visitDay == todayMonthKey
        }
        return if (alreadyInThisMonth) {
            todayCal.apply { add(Calendar.DATE, -1) }.time
        } else today
    }

    fun getMinVisitDate(): Date? {
        return previousVisitDate?.let {
            Calendar.getInstance().apply {
                time = it
                add(Calendar.DATE, 1)
            }.time
        }
    }
}
