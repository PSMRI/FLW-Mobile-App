package org.piramalswasthya.sakhi.repositories.dynamicRepo

import android.content.Context
import androidx.annotation.WorkerThread
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.helpers.dynamicMapper.FormSubmitRequestMapper
import org.piramalswasthya.sakhi.model.BottleItem
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity
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
import javax.inject.Named

//@Singleton
//class EyeSurgeryFormRepository @Inject constructor(
//    @ApplicationContext private val context: Context,
//    @Named("gsonAmritApi") private val amritApiService: AmritApiService,
//    private val db: InAppDb
//) {
//    private val formSchemaDao = db.formSchemaDao()
//    private val jsonResponseDao = db.formResponseJsonDaoEyeSurgery()
//
//    suspend fun getFormSchema(formId: String): FormSchemaDto? = withContext(Dispatchers.IO) {
//        var result: FormSchemaDto? = null
//
//        try {
//            val response = amritApiService.fetchFormSchema(formId)
//
//            if (response.isSuccessful) {
//                val apiResponse = response.body()
//                if (apiResponse?.success == true) {
//                    val apiSchema = apiResponse.data
//                    if (apiSchema != null) {
//                        val localSchema = getSavedSchema(apiSchema.formId)
//                        if (localSchema == null || localSchema.version < apiSchema.version) {
//                            saveFormSchemaToDb(apiSchema)
//                        }
//                        result = apiSchema
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            // ignored — fallback below will handle
//        }
//
//        if (result == null) {
//            val dbSchema = formSchemaDao.getSchema(formId)
//            result = dbSchema?.let { FormSchemaDto.fromJson(it.schemaJson) }
//                ?: loadSchemaFromAssets()
//        }
//        result
//    }
//
//    private fun loadSchemaFromAssets(): FormSchemaDto? {
//        return try {
//            val json = context.assets.open("hbnc_form_1stday.json")
//                .bufferedReader().use { it.readText() }
//            FormSchemaDto.fromJson(json)
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    suspend fun saveFormSchemaToDb(schema: FormSchemaDto) {
//        val entity = FormSchemaEntity(
//            formId = schema.formId,
//            formName = schema.formName,
//            version = schema.version,
//            schemaJson = schema.toJson()
//        )
//        formSchemaDao.insertOrUpdate(entity)
//    }
//
//    suspend fun getSavedSchema(formId: String) = formSchemaDao.getSchema(formId)
//
//    suspend fun getSyncedVisitsByRchId(benId: Long): List<EyeSurgeryFormResponseJsonEntity> =
//        jsonResponseDao.getSyncedVisitsByRchId(benId)
//
//    suspend fun getAllFormVisits(formName: String, request: HBNCVisitRequest): Response<HBNCVisitListResponse> {
//        return amritApiService.getAllEyeSurgeryFormVisits(formName, request)
//    }
//
//    suspend fun getAllBenIds(): List<Long> {
//        return jsonResponseDao.getAllUniqueBenIds()
//    }
//
//    suspend fun getBottleList(benId: Long, formId: String): List<BottleItem> {
//        val jsonList = jsonResponseDao.getFormJsonList(benId, formId)
//
//        val result = mutableListOf<BottleItem>()
//
//        jsonList.forEachIndexed { index, formJson ->
//            try {
//                val root = JSONObject(formJson)
//                val fields = root.optJSONObject("fields")
//                val date = fields?.optString("visit_date", "-") ?: "-"
//                val count = fields?.optString("ifa_quantity", "-") ?: "-"
//
//                result.add(
//                    BottleItem(
//                        srNo = index + 1,
//                        bottleNumber = count.toString(),
//                        dateOfProvision = date
//                    )
//                )
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        return result
//    }
//
//suspend fun saveDownloadedVisitList(list: List<HBNCVisitResponse>, formId: String) {
//    try {
//        if (list.isEmpty()) return
//
//        val entityList = list.mapNotNull { item ->
//            try {
//                if (item.fields == null) return@mapNotNull null
//
//                val visitDate = item.visitDate ?: "-"
//                val benId = item.beneficiaryId
//                val hhId = item.houseHoldId
//
//                // Convert all fields to JSON
//                val fieldsJson = JSONObject()
//                item.fields.entrySet().forEach { (key, jsonElement) ->
//                    val value = when {
//                        jsonElement.isJsonNull -> JSONObject.NULL
//                        jsonElement.isJsonPrimitive -> {
//                            val prim = jsonElement.asJsonPrimitive
//                            when {
//                                prim.isBoolean -> prim.asBoolean
//                                prim.isNumber -> prim.asNumber
//                                prim.isString -> prim.asString
//                                else -> prim.asString
//                            }
//                        }
//                        else -> jsonElement.toString()
//                    }
//                    fieldsJson.put(key, value)
//                }
//
//                // Wrap the form payload
//                val fullJson = JSONObject().apply {
//                    put("formId", formId)
//                    put("beneficiaryId", benId)
//                    put("houseHoldId", hhId)
//                    put("visitDate", visitDate)
//                    put("fields", fieldsJson)
//                }
//
//                // Build entity
//                EyeSurgeryFormResponseJsonEntity(
//                    benId = benId,
//                    hhId = hhId,
//                    visitDate = visitDate,
//                    formId = formId,
//                    version = 1,
//                    formDataJson = fullJson.toString(),
//                    isSynced = true
//                )
//            } catch (e: Exception) {
//                e.printStackTrace()
//                null
//            }
//        }
//
//        if (entityList.isNotEmpty()) {
//            jsonResponseDao.insertAll(entityList)
//        }
//
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//}
//
//
//    suspend fun insertOrUpdateFormResponse(entity: EyeSurgeryFormResponseJsonEntity) {
//        val existing = jsonResponseDao.getFormResponse(entity.benId,entity.visitDate)
//        val updated = existing?.let { entity.copy(id = it.id) } ?: entity
//        jsonResponseDao.insertFormResponse(updated)
//    }
//
//    suspend fun insertFormResponse(entity: EyeSurgeryFormResponseJsonEntity) =
//        jsonResponseDao.insertFormResponse(entity)
//
//    suspend fun loadFormResponseJson(benId: Long): String? =
//        jsonResponseDao.getFormResponse(benId,"")?.formDataJson
//
//    suspend fun getUnsyncedForms(formName: String): List<EyeSurgeryFormResponseJsonEntity> =
//        jsonResponseDao.getUnsyncedForms(formName)
//
//    suspend fun syncFormToServer(userName: String,formName: String, form: EyeSurgeryFormResponseJsonEntity): Boolean {
//        return try {
//            val request = FormSubmitRequestMapper.fromEntity(form,userName) ?: return false
//            val response = amritApiService.submitEyeSurgeryForm(formName,listOf(request))
//            response.isSuccessful
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    suspend fun markFormAsSynced(id: Int) {
//        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
//        jsonResponseDao.markAsSynced(id, timestamp)
//    }
//}


@Singleton
class EyeSurgeryFormRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("gsonAmritApi") private val amritApiService: AmritApiService,
    private val db: InAppDb
) {
    private val formSchemaDao = db.formSchemaDao()
    private val jsonResponseDao = db.formResponseJsonDaoEyeSurgery()

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

    suspend fun getSyncedVisitsByRchId(benId: Long): List<EyeSurgeryFormResponseJsonEntity> =
        jsonResponseDao.getSyncedVisitsByRchId(benId)

    suspend fun getAllFormVisits(formName: String, request: HBNCVisitRequest): Response<HBNCVisitListResponse> {
        return amritApiService.getAllEyeSurgeryFormVisits(formName, request)
    }

    suspend fun getAllBenIds(): List<Long> {
        return jsonResponseDao.getAllUniqueBenIds()
    }

    suspend fun getBottleList(benId: Long, formId: String): List<BottleItem> {
        val jsonList = jsonResponseDao.getFormJsonList(benId, formId)

        val result = mutableListOf<BottleItem>()

        jsonList.forEachIndexed { index, formJson ->
            try {
                val root = JSONObject(formJson)
                val fields = root.optJSONObject("fields")
                val date = fields?.optString("visit_date", "-") ?: "-"
                val count = fields?.optString("ifa_quantity", "-") ?: "-"

                result.add(
                    BottleItem(
                        srNo = index + 1,
                        bottleNumber = count.toString(),
                        dateOfProvision = date
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
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

    suspend fun saveDownloadedVisitList(list: List<HBNCVisitResponse>, formId: String) {
        try {
            if (list.isEmpty()) return

            val entityList = list.mapNotNull { item ->
                try {
                    if (item.fields == null) return@mapNotNull null

                    val visitDate = item.visitDate ?: "-"
                    val visitMonth = toMonthKey(visitDate)
                    val benId = item.beneficiaryId
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

                    // Wrap the form payload
                    val fullJson = JSONObject().apply {
                        put("formId", formId)
                        put("beneficiaryId", benId)
                        put("houseHoldId", hhId)
                        put("visitDate", visitDate)
                        put("fields", fieldsJson)
                    }

                    // Build entity
                    EyeSurgeryFormResponseJsonEntity(
                        benId = benId,
                        hhId = hhId,
                        visitDate = visitDate,
                        visitMonth = visitMonth,
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
                // Use REPLACE; unique index will collapse duplicates within same month
                insertAllByMonth(entityList)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @WorkerThread
    private suspend fun insertAllByMonth(list: List<EyeSurgeryFormResponseJsonEntity>) {
        // Ensure month upsert semantics for bulk
        list.forEach { jsonResponseDao.upsertByMonth(it) }
    }

    suspend fun insertOrUpdateFormResponse(entity: EyeSurgeryFormResponseJsonEntity) {
        val updated = entity // month-based upsert will handle id mapping
        jsonResponseDao.upsertByMonth(updated)
    }

    suspend fun insertFormResponse(entity: EyeSurgeryFormResponseJsonEntity) =
        jsonResponseDao.upsertByMonth(entity)

    suspend fun loadFormResponseJson(benId: Long, formId: String): String? =
        jsonResponseDao.getLatestForBenForm(benId, formId)?.formDataJson

    suspend fun getUnsyncedForms(formName: String): List<EyeSurgeryFormResponseJsonEntity> =
        jsonResponseDao.getUnsyncedForms(formName)

    suspend fun syncFormToServer(userName: String, formName: String, form: EyeSurgeryFormResponseJsonEntity): Boolean {
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
        jsonResponseDao.markAsSynced(id, timestamp)
    }
}
