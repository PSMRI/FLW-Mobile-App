package org.piramalswasthya.sakhi.repositories.dynamicRepo

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.helpers.dynamicMapper.FormSubmitRequestMapper
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.HBNC_FORM_ID
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response
import timber.log.Timber
import javax.inject.Named

@Singleton
class FormRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("gsonAmritApi") private val amritApiService: AmritApiService,
    private val db: InAppDb
) {
    private val formSchemaDao = db.formSchemaDao()
    private val jsonResponseDao = db.formResponseJsonDao()

    suspend fun getFormSchema(formId: String): FormSchemaDto? = withContext(Dispatchers.IO) {
        try {
            val response = amritApiService.fetchFormSchema(formId)
            if (response.isSuccessful) {
                val apiSchema = response.body()
                apiSchema?.let {
                    val localSchema = getSavedSchema(it.formId)
                    if (localSchema == null || localSchema.version < it.version) {
                        saveFormSchemaToDb(it)
                    }
                    return@withContext it
                }
            }
        } catch (e: Exception) {
        }

        formSchemaDao.getSchema(formId)?.let {
            return@withContext FormSchemaDto.fromJson(it.schemaJson)
        } ?: loadSchemaFromAssets()
    }

    private fun loadSchemaFromAssets(): FormSchemaDto? {
        return try {
            val json = context.assets.open("hbnc_form_1stday.json")
                .bufferedReader().use { it.readText() }
            FormSchemaDto.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveFormSchemaToDb(schema: FormSchemaDto) {
        val entity = FormSchemaEntity(
            formId = schema.formId,
            formName = schema.formName,
            version = schema.version,
            schemaJson = schema.toJson()
        )
        formSchemaDao.insertOrUpdate(entity)
    }

    suspend fun getSavedSchema(formId: String) = formSchemaDao.getSchema(formId)

    suspend fun getInfantByRchId(benId: Long) = jsonResponseDao.getSyncedVisitsByRchId(benId)

    suspend fun getSyncedVisitsByRchId(benId: Long): List<FormResponseJsonEntity> =
        jsonResponseDao.getSyncedVisitsByRchId(benId)

    suspend fun getAllHbncVisits(request: HBNCVisitRequest): Response<HBNCVisitListResponse> {
        return amritApiService.getAllHbncVisits(request)
    }

    suspend fun saveDownloadedVisitList(list: List<HBNCVisitResponse>) {
        for ((index, item) in list.withIndex()) {
            try {
                if (item.fields == null) {
                    continue
                }

                val visitDay = item.fields.get("visit_day")?.let {
                    if (it.isJsonPrimitive) it.asString.trim() else ""
                } ?: ""
                val visitDate = item.visitDate ?: "-"
                val benId = item.beneficiaryId
                val hhId = item.houseHoldId

                if (visitDay.isBlank()) {
                    continue
                }

                val fieldsJson = JSONObject()
                item.fields.entrySet().forEach { (key, jsonElement) ->
                    val value = when {
                        jsonElement.isJsonNull -> JSONObject.NULL
                        jsonElement.isJsonPrimitive -> {
                            val prim = jsonElement.asJsonPrimitive
                            when {
                                prim.isBoolean -> prim.asBoolean
                                prim.isNumber -> prim.asNumber
                                prim.isString -> prim.asString
                                else -> prim.asString
                            }
                        }

                        else -> jsonElement.toString()
                    }
                    fieldsJson.put(key, value)
                }

                val fullJson = JSONObject().apply {
                    put("formId", HBNC_FORM_ID)
                    put("beneficiaryId", benId)
                    put("houseHoldId", hhId)
                    put("visitDate", visitDate)
                    put("fields", fieldsJson)
                }

                val entity = FormResponseJsonEntity(
                    benId = benId,
                    hhId = hhId,
                    visitDay = visitDay,
                    visitDate = visitDate,
                    formId = HBNC_FORM_ID,
                    version = 1,
                    formDataJson = fullJson.toString(),
                    isSynced = true
                )

                insertOrUpdateFormResponse(entity)

            } catch (e: Exception) {
                Timber.tag("FormRepository").e(e, "Failed to save visit at index " + index)
            }
        }
    }


    suspend fun insertOrUpdateFormResponse(entity: FormResponseJsonEntity) {
        val existing = jsonResponseDao.getFormResponse(entity.benId, entity.visitDay)
        val updated = existing?.let { entity.copy(id = it.id) } ?: entity
        jsonResponseDao.insertFormResponse(updated)
    }

    suspend fun insertFormResponse(entity: FormResponseJsonEntity) =
        jsonResponseDao.insertFormResponse(entity)

    suspend fun loadFormResponseJson(benId: Long, visitDay: String): String? =
        jsonResponseDao.getFormResponse(benId, visitDay)?.formDataJson

    suspend fun getUnsyncedForms(): List<FormResponseJsonEntity> =
        jsonResponseDao.getUnsyncedForms()


    suspend fun syncFormToServer(form: FormResponseJsonEntity): Boolean {
        return try {
            val request = FormSubmitRequestMapper.fromEntity(form) ?: return false
            val response = amritApiService.submitForm(listOf(request))
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun markFormAsSynced(id: Int) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        jsonResponseDao.markAsSynced(id, timestamp)
    }
}
