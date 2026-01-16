package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.ors_campaign

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.ConditionalLogic
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FieldValidation
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ORSCampaignCache
import org.piramalswasthya.sakhi.model.dynamicEntity.FormFieldDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.repositories.VLFRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import org.piramalswasthya.sakhi.work.dynamicWoker.ORSCampaignPushWorker
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ORSCampaignViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val formRepository: FormRepository,
    private val vlfRepo: VLFRepo,
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    private val _schema = MutableStateFlow<FormSchemaDto?>(null)
    val schema: StateFlow<FormSchemaDto?> = _schema

    private val recordId = ORSCampaignFormFragmentArgs.fromSavedStateHandle(savedStateHandle).id
    private var isViewMode = false
    private var currentCache: ORSCampaignCache? = null

    val formId = FormConstants.ORS_CAMPAIGN_FORM_ID

    init {
        viewModelScope.launch {
            if (recordId > 0) {
                currentCache = vlfRepo.getORSCampaign(recordId)
                isViewMode = true
            } else {
                isViewMode = false
            }
            loadFormSchema()
        }
    }

    private suspend fun loadFormSchema() {
        val cachedSchemaEntity = formRepository.getSavedSchema(formId)
        val cachedSchema: FormSchemaDto? = cachedSchemaEntity?.let {
            FormSchemaDto.fromJson(it.schemaJson)
        }

        val localSchemaToRender = cachedSchema ?: formRepository.getFormSchema(
            formId,
            preferenceDao.getCurrentLanguage().symbol
        ) ?: return

        val savedFieldValues = if (currentCache != null && !currentCache!!.formDataJson.isNullOrEmpty()) {
            try {
                val savedJson = JSONObject(currentCache!!.formDataJson)
                val fieldsJson = savedJson.optJSONObject("fields") ?: JSONObject()
                fieldsJson.keys().asSequence().associateWith { 
                    fieldsJson.opt(it).toString() 
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading saved form data")
                emptyMap()
            }
        } else {
            emptyMap()
        }

        val allFields = localSchemaToRender.sections.flatMap { it.fields.orEmpty() }
        
        localSchemaToRender.sections.forEach { section ->
            section.fields.forEach { field ->
                field.value = savedFieldValues[field.fieldId] ?: field.defaultValue ?: field.default
                field.isEditable = !isViewMode
            }
        }

        localSchemaToRender.sections.forEach { section ->
            section.fields.forEach { field ->
                field.visible = evaluateFieldVisibility(field, allFields)
            }
        }

        _schema.value = localSchemaToRender
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

    fun updateFieldValue(fieldId: String, value: Any?) {
        val currentSchema = _schema.value ?: return
        val allFields = currentSchema.sections.flatMap { it.fields.orEmpty() }
        
        allFields.find { it.fieldId == fieldId }?.value = value
        
        allFields.forEach { field ->
            field.visible = evaluateFieldVisibility(field, allFields)
        }
        
        _schema.value = currentSchema.copy()
    }

    fun saveForm() {
        viewModelScope.launch {
            try {
                val currentSchema = _schema.value ?: return@launch
                val allFields = currentSchema.sections.flatMap { it.fields.orEmpty() }
                    .filter { it.visible }
                
                val fieldsMap = allFields.associate { it.fieldId to it.value }
                val formDataJson = Gson().toJson(mapOf("fields" to fieldsMap))

                val cache = currentCache?.copy(
                    formDataJson = formDataJson,
                    syncState = SyncState.UNSYNCED
                ) ?: ORSCampaignCache(
                    id = 0,
                    formDataJson = formDataJson,
                    syncState = SyncState.UNSYNCED
                )

                if (recordId > 0) {
                    val savedCache = cache.copy(id = recordId)
                    vlfRepo.saveORSCampaign(savedCache)
                } else {
                    vlfRepo.saveORSCampaign(cache)
                }
                
                ORSCampaignPushWorker.enqueue(context)
            } catch (e: Exception) {
                Timber.e(e, "Error saving ORS Campaign form")
            }
        }
    }
}
