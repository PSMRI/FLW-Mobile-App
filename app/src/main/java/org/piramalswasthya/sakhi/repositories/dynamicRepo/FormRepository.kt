package com.example.hbncschemademo.ui.repo

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
import org.piramalswasthya.sakhi.network.AbhaApiService
import org.piramalswasthya.sakhi.network.AmritApiService
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response
import javax.inject.Named

@Singleton
class FormRepository @Inject constructor(
    @ApplicationContext private val context: Context,
//    private val amritApiService: AmritApiService,
    @Named("gsonAmritApi") private val amritApiService: AmritApiService,
    private val db: InAppDb
) {
    private val infantDao = db.infantDao()
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
            Log.e("FormRepository", "Exception in schema API: ${e.message}")
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
            Log.e("FormRepository", "Asset load failed: ${e.message}")
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

//    suspend fun insertInfant(infant: InfantEntity) = infantDao.insertInfant(infant)

    suspend fun getSyncedVisitsByRchId(benId: Long): List<FormResponseJsonEntity> =
        jsonResponseDao.getSyncedVisitsByRchId(benId)

    suspend fun getAllHbncVisits(request: HBNCVisitRequest): Response<HBNCVisitListResponse> {
        return amritApiService.getAllHbncVisits(request)
    }

    suspend fun saveDownloadedVisitList(list: List<HBNCVisitResponse>) {
        for ((index, item) in list.withIndex()) {
            try {
                val visitDay = item.fields["visit_day"]?.asString?.trim() ?: ""
                val visitDate = item.visitDate ?: "-"
                val benId = item.beneficiaryId
                val hhId = item.houseHoldId

                if (visitDay.isBlank()) {
                    Log.w("SkipVisit", "⚠️ Skipping entry at index $index: visitDay or rchId is missing.")
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
                    put("formId", "hbnc_form_001")
                    put("beneficiaryId", benId)
                    put("houseHoldId", hhId)
                    put("visitDate", visitDate)
                    put("fields", fieldsJson)
                }

                val entity = FormResponseJsonEntity(
                    benId = benId,
                    hhId = hhId,
                    visitDay = visitDay,
                    formId = "hbnc_form_001",
                    version = 1,
                    formDataJson = fullJson.toString(),
                    isSynced = true
                )

                insertOrUpdateFormResponse(entity)
                Log.d("DownSync", "✅ Saved visit [$index]: $visitDay | RCH: $benId")

            } catch (e: Exception) {
                Log.e("DownSync", "❌ Error saving visit at index $index: ${e.message}", e)
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

    suspend fun loadFormResponseJson(benId:Long, visitDay: String): String? =
        jsonResponseDao.getFormResponse(benId, visitDay)?.formDataJson

    suspend fun getUnsyncedForms(): List<FormResponseJsonEntity> =
        jsonResponseDao.getUnsyncedForms()

    suspend fun syncFormToServer(form: FormResponseJsonEntity): Boolean {
        return try {
            val request = FormSubmitRequestMapper.fromEntity(form) ?: return false
            val response = amritApiService.submitForm(listOf(request))
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("SyncAPI", "Sync failed: ${e.message}", e)
            false
        }
    }

    suspend fun markFormAsSynced(id: Int) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        jsonResponseDao.markAsSynced(id, timestamp)
    }
}
