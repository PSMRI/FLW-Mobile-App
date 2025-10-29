package org.piramalswasthya.sakhi.repositories.dynamicRepo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.helpers.dynamicMapper.FormSubmitRequestMapper
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response
import timber.log.Timber
import javax.inject.Named

@Singleton
class CUFYFormRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("gsonAmritApi") private val amritApiService: AmritApiService,
    private val db: InAppDb
) {
    private val formSchemaDao = db.formSchemaDao()
    private val jsonResponseDao = db.CUFYFormResponseJsonDao()

    suspend fun getFormSchema(formId: String): FormSchemaDto? = withContext(Dispatchers.IO) {
        var result: FormSchemaDto? = null

        try {
            val response = amritApiService.fetchFormSchema(formId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val apiSchema = apiResponse.data
                    if (apiSchema != null) {
                        val localSchema = getSavedSchema(apiSchema.formId)
                        if (localSchema == null || localSchema.version < apiSchema.version) {
                            saveFormSchemaToDb(apiSchema)
                        }
                        result = apiSchema
                    }
                }
            }
        } catch (e: Exception) {
            // ignored — fallback below will handle
        }

        if (result == null) {
            val dbSchema = formSchemaDao.getSchema(formId)
            result = dbSchema?.let { FormSchemaDto.fromJson(it.schemaJson) }
                ?: loadSchemaFromAssets()
        }
        result
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

    suspend fun getSyncedVisitsByRchId(benId: Long): List<CUFYFormResponseJsonEntity> =
        jsonResponseDao.getSyncedVisitsByRchId(benId)

    suspend fun getAllFormVisits(formName: String, request: HBNCVisitRequest): Response<HBNCVisitListResponse> {
        return amritApiService.getAllFormVisits(formName, request)
    }

    suspend fun saveDownloadedVisitList(list: List<HBNCVisitResponse>, formId: String) {
        for ((index, item) in list.withIndex()) {
            try {
                if (item.fields == null) {
                    continue
                }

                val visitDate = item.visitDate ?: "-"
                val benId = item.beneficiaryId
                val hhId = item.houseHoldId

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
                    put("formId", formId)
                    put("beneficiaryId", benId)
                    put("houseHoldId", hhId)
                    put("visitDate", visitDate)
                    put("fields", fieldsJson)
                }

                val entity = CUFYFormResponseJsonEntity(
                    benId = benId,
                    hhId = hhId,
                    visitDate = visitDate,
                    formId = formId,
                    version = 1,
                    formDataJson = fullJson.toString(),
                    isSynced = true
                )

                insertOrUpdateFormResponse(entity)

            } catch (e: Exception) {
                Timber.tag("CUFYFormRepository").e(e, "Failed to save visit at index " + index)
            }
        }
    }


    suspend fun insertOrUpdateFormResponse(entity: CUFYFormResponseJsonEntity) {
        val existing = jsonResponseDao.getFormResponse(entity.benId, entity.visitDate)
        val updated = existing?.let { entity.copy(id = it.id) } ?: entity
        jsonResponseDao.insertFormResponse(updated)
    }

    suspend fun insertFormResponse(entity: CUFYFormResponseJsonEntity) =
        jsonResponseDao.insertFormResponse(entity)

    suspend fun loadFormResponseJson(benId: Long, visitDay: String): String? =
        jsonResponseDao.getFormResponse(benId, visitDay)?.formDataJson

    suspend fun getUnsyncedForms(formId: String): List<CUFYFormResponseJsonEntity> =
        jsonResponseDao.getUnsyncedForms(formId)



    suspend fun syncFormToServer(userName: String,formName: String, form: CUFYFormResponseJsonEntity): Boolean {
        return try {
            val request = FormSubmitRequestMapper.fromEntity(form,userName) ?: return false
            val response = amritApiService.submitChildCareForm(formName,listOf(request))
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
