package org.piramalswasthya.sakhi.repositories.dynamicRepo

import android.content.Context
import androidx.annotation.WorkerThread
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.dynamicMapper.FormSubmitRequestMapper
import org.piramalswasthya.sakhi.model.BottleItem
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.mosquitonetEntity.MosquitoNetFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class MosquitoNetFormRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("gsonAmritApi") private val amritApiService: AmritApiService,
    private val pref : PreferenceDao,
    private val db: InAppDb
) {
    private val formSchemaDao = db.formSchemaDao()
    private val jsonResponseDao = db.formResponseMosquitoNetJsonDao()

    suspend fun getFormSchema(formId: String): FormSchemaDto? = withContext(Dispatchers.IO) {
        var result: FormSchemaDto? = null
        try {
            val response = amritApiService.fetchFormSchema(formId, pref.getCurrentLanguage().symbol)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val apiSchema = apiResponse.data
                    if (apiSchema != null) {
                        val local = getSavedSchema(apiSchema.formId)
                        if (local == null || local.version < apiSchema.version) {
                            saveFormSchemaToDb(apiSchema)
                        }
                        result = apiSchema
                    }
                }
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }

        if (result == null) {
            val dbSchema = formSchemaDao.getSchema(formId)
            result = dbSchema?.let { FormSchemaDto.fromJson(it.schemaJson) } ?: loadSchemaFromAssets()
        }
        result
    }

    private fun loadSchemaFromAssets(): FormSchemaDto? {
        return try {
            val json = context.assets.open("hbnc_form_1stday.json").bufferedReader().use { it.readText() }
            FormSchemaDto.fromJson(json)
        } catch (_: Exception) { null }
    }

    suspend fun saveFormSchemaToDb(schema: FormSchemaDto) {
        val entity = FormSchemaEntity(
            formId = schema.formId,
            formName = schema.formName,
            language = pref.getCurrentLanguage().symbol,
            version = schema.version,
            schemaJson = schema.toJson()
        )
        formSchemaDao.insertOrUpdate(entity)
    }

    suspend fun getSavedSchema(formId: String) = formSchemaDao.getSchema(formId)


    suspend fun getAllFormVisits(
        formName: String,
        request: HBNCVisitRequest
    ): Response<HBNCVisitListResponse> =
        amritApiService.getAllDiseaseMosquitoFormVisits(formName, request)
    suspend fun getAllByHhId(hhId: Long): List<MosquitoNetFormResponseJsonEntity> =
        jsonResponseDao.getAllByHhId(hhId)


    suspend fun getBottleList(hhId: Long): List<BottleItem> {
        val jsonList = jsonResponseDao.getFormJsonList(hhId)

        val result = mutableListOf<BottleItem>()

        jsonList.forEach { formJson ->
            val root = JSONObject(formJson)
            val fields = root.optJSONObject("fields")
            val date = fields?.optString("visit_date", "-") ?: "-"
            val count = fields?.optString("is_net_distributed", "-") ?: "-"

            result.add(
                BottleItem(
                    srNo = 0,
                    bottleNumber = count,
                    dateOfProvision = date
                )
            )
        }

        return result.mapIndexed { index, item ->
            item.copy(srNo = index + 1)
        }
    }

    suspend fun loadFormResponseJson(hhId: Long, formId: String): String? =
        jsonResponseDao.getLatestForHhForm(hhId, formId)?.formDataJson

    suspend fun getUnsyncedForms(formId: String): List<MosquitoNetFormResponseJsonEntity> =
        jsonResponseDao.getUnsyncedForms(formId)

    suspend fun saveDownloadedVisitList(list: List<HBNCVisitResponse>, formId: String) {
        if (list.isEmpty()) return
        val entityList = list.mapNotNull { item ->
            try {
                val visitDate = item.visitDate ?: return@mapNotNull null
                val hhId = item.houseHoldId
                val id = item.id

                val fieldsJson = JSONObject()
                item.fields?.entrySet()?.forEach { (key, jsonElement) ->
                    val value = when {
                        jsonElement.isJsonNull -> JSONObject.NULL
                        jsonElement.isJsonPrimitive -> {
                            val p = jsonElement.asJsonPrimitive
                            when {
                                p.isBoolean -> p.asBoolean
                                p.isNumber -> p.asNumber
                                p.isString -> p.asString
                                else -> p.asString
                            }
                        }
                        else -> jsonElement.toString()
                    }
                    fieldsJson.put(key, value)
                }

                val fullJson = JSONObject().apply {
                    put("id", id)
                    put("formId", formId)
                    put("houseHoldId", hhId)
                    put("visitDate", visitDate)
                    put("beneficiaryId", item.beneficiaryId)
                    put("fields", fieldsJson)
                }

                MosquitoNetFormResponseJsonEntity(
                    id = id,
                    hhId = hhId,
                    formId = formId,
                    visitDate = visitDate,
                    formDataJson = fullJson.toString(),
                    isSynced = true,
                    version = 1
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        if (entityList.isNotEmpty()) {
            insertAllWithLimit(entityList)
        }
    }

    @WorkerThread
    private suspend fun insertAllWithLimit(list: List<MosquitoNetFormResponseJsonEntity>) {
        list.forEach { jsonResponseDao.insertWithLimit(it) }
    }

    suspend fun insertFormResponse(entity: MosquitoNetFormResponseJsonEntity): Boolean {
       return jsonResponseDao.insertWithLimit(entity)
    }

    suspend fun syncFormToServer(
        userName: String,
        formName: String,
        form: MosquitoNetFormResponseJsonEntity
    ): Boolean {
        return try {
            val request = FormSubmitRequestMapper.fromEntity(form, userName) ?: return false
            val response = amritApiService.submitDiseaseMosquitoForm(formName, listOf(request))
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    suspend fun markFormAsSynced(id: Int) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        jsonResponseDao.markAsSynced(id, timestamp)
    }
}
