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
import org.piramalswasthya.sakhi.model.dynamicEntity.FilariaMDA.FilariaMDAFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.filariaaMdaCampaign.FilariaMDACampaignFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.model.dynamicModel.MDACampaignItem
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

@Singleton
class FilariaMdaCampaignRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("gsonAmritApi") private val amritApiService: AmritApiService,
    private val pref : PreferenceDao,
    private val db: InAppDb
) {
    private val formSchemaDao = db.formSchemaDao()
    private val jsonResponseDao = db.formResponseFilariaMDACampaignJsonDao()

    suspend fun getFormSchema(formId: String): FormSchemaDto? = withContext(Dispatchers.IO) {
        var result: FormSchemaDto? = null

        try {
            val response = amritApiService.fetchFormSchema(formId,"en")

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
            language = pref.getCurrentLanguage().symbol,
            version = schema.version,
            schemaJson = schema.toJson()
        )
        formSchemaDao.insertOrUpdate(entity)
    }

    suspend fun getSavedSchema(formId: String) = formSchemaDao.getSchema(formId)

    suspend fun getSyncedVisitsByRchId(): List<FilariaMDACampaignFormResponseJsonEntity> =
        jsonResponseDao.getCampaignSyncedVisitsByRchId()

    suspend fun getAllFormVisits(formName: String, request: HBNCVisitRequest): Response<HBNCVisitListResponse> {
        return amritApiService.getAllEyeSurgeryFormVisits(formName, request)
    }

    suspend fun getBottleList(): List<MDACampaignItem> {
        val jsonList = jsonResponseDao.getCampaignFormJsonList()

        val result = mutableListOf<MDACampaignItem>()

        jsonList.forEach { formJson ->
            val root = JSONObject(formJson)
            val fields = root.optJSONObject("fields")
            val startDate = fields?.optString("start_date", "-") ?: "-"
            val endDate = fields?.optString("end_date", "-") ?: "-"
            val noofFamiles = fields?.optString("no_of_families", "-") ?: "-"
            val noofIndividuals = fields?.optString("no_of_individuals", "-") ?: "-"

            result.add(
                MDACampaignItem(
                    srNo = 0,
                    startDate = startDate,
                    endDate = endDate,
                    noOfIndividuals = noofIndividuals,
                    noOffamilies = noofFamiles
                )
            )
        }

        return result.mapIndexed { index, item ->
            item.copy(srNo = index + 1)
        }
    }

    private fun toYearKey(dateStr: String?): String {
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

    suspend fun saveDownloadedVisitList(list: List<HBNCVisitResponse>, formId: String) {
        try {
            if (list.isEmpty()) return

            val entityList = list.mapNotNull { item ->
                try {
                    if (item.fields == null) return@mapNotNull null

                    val visitDate = item.visitDate ?: "-"
                    val visitYear = toYearKey(visitDate)
                    val hhId = item.houseHoldId

                    // Convert all fields to JSON
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
                        put("houseHoldId", hhId)
                        put("visitDate", visitDate)
                        put("fields", fieldsJson)
                    }

                    // Build entity
                    FilariaMDACampaignFormResponseJsonEntity(
                        visitDate = visitDate,
                        visitYear = visitYear,
                        formId = formId,
                        version = 1,
                        formDataJson = fullJson.toString(),
                        isSynced = true
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (entityList.isNotEmpty()) {
                insertAllByMonth(entityList)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @WorkerThread
    private suspend fun insertAllByMonth(list: List<FilariaMDACampaignFormResponseJsonEntity>) {
        list.forEach { jsonResponseDao.upsertByYear(it) }
    }

    suspend fun insertFormResponse(entity: FilariaMDACampaignFormResponseJsonEntity): Boolean {
        return jsonResponseDao.insertOncePerYear(entity)
    }


    suspend fun loadFormResponseJson(hhId: Long, formId: String): String? =
        jsonResponseDao.getCampaignLatestForBenForm( formId)?.formDataJson

    suspend fun getUnsyncedForms(formName: String): List<FilariaMDACampaignFormResponseJsonEntity> =
        jsonResponseDao.getUnsyncedCampaignForms(formName)

    suspend fun syncFormToServer(userName: String, formName: String, form: FilariaMDAFormResponseJsonEntity): Boolean {
        return try {
            val request = FormSubmitRequestMapper.fromEntity(form, userName) ?: return false
            val response = amritApiService.submitEyeSurgeryForm(formName, listOf(request))
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun markFormAsSynced(id: Int) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        jsonResponseDao.markCampaignAsSynced(id, timestamp)
    }
}
