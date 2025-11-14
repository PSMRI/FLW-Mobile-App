package org.piramalswasthya.sakhi.repositories.dynamicRepo

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.dynamicMapper.FormSubmitRequestMapper
import org.piramalswasthya.sakhi.model.dynamicEntity.FormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.hbyc.FormResponseJsonEntityHBYC
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.HBNC_FORM_ID
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.HBYC_FORM_ID
import retrofit2.Response
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class FormRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceDao: PreferenceDao,
    @Named("gsonAmritApi") private val amritApiService: AmritApiService,
    private val db: InAppDb
) {
    private val formSchemaDao = db.formSchemaDao()
    private val jsonResponseDao = db.formResponseJsonDao()
    private val jsonResponseDaoHBYC = db.formResponseJsonDaoHBYC()


    suspend fun getFormSchema(formId: String): FormSchemaDto? = withContext(Dispatchers.IO) {
        try {
            val response = amritApiService.fetchFormSchema(formId,"en")
            if (response.isSuccessful) {
                val apiResponse = response.body()
                val apiSchema = apiResponse?.data

                apiSchema?.let {
                    val localSchema = getSavedSchema(it.formId)
                    if (localSchema == null || localSchema.version < it.version) {
                        saveFormSchemaToDb(it)
                    }
                    return@withContext it
                }
            } else {
            }
        } catch (e: Exception) {
        }
        val local = formSchemaDao.getSchema(formId)?.let { FormSchemaDto.fromJson(it.schemaJson) }
        return@withContext local
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

            val request = FormSubmitRequestMapper.fromEntity(form,preferenceDao.getLoggedInUser()!!.userName) ?: return false
            val response = amritApiService.submitForm(listOf(request))
            response.isSuccessful
        } catch (e: Exception) { false }
    }

    suspend fun markFormAsSynced(id: Int) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        jsonResponseDao.markAsSynced(id, timestamp)
    }

    suspend fun getAllHbncVisits(request: HBNCVisitRequest): Response<HBNCVisitListResponse> {
        return amritApiService.getAllHbncVisits(request)
    }
 suspend fun getAllHbycVisits(request: HBNCVisitRequest): Response<HBNCVisitListResponse> {
        return amritApiService.getAllHbycVisits(request)
    }

    suspend fun saveDownloadedVisitList(list: List<HBNCVisitResponse>) {
        for (item in list) {
            try {
                if (item.fields == null) continue
                val visitDay = item.fields["visit_day"]?.asString ?: continue
                val visitDate = item.visitDate ?: "-"
                val benId = item.beneficiaryId
                val hhId = item.houseHoldId

                val fieldsJson = JSONObject()
                item.fields.entrySet().forEach { (key, jsonElement) ->
                    val value = if (jsonElement.isJsonNull) JSONObject.NULL else jsonElement.asString
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
                Timber.e(e, "Failed to save HBNC visit")
            }
        }
    }

    suspend fun getInfantByRchIdHBYC(benId: Long) = jsonResponseDaoHBYC.getSyncedVisitsByRchId(benId)
    suspend fun getSyncedVisitsByRchIdHBYC(benId: Long): List<FormResponseJsonEntityHBYC> =
        jsonResponseDaoHBYC.getSyncedVisitsByRchId(benId)

    suspend fun insertOrUpdateFormResponseHBYC(entity: FormResponseJsonEntityHBYC) {
        val existing = jsonResponseDaoHBYC.getFormResponse(entity.benId, entity.visitDay)
        val updated = existing?.let { entity.copy(id = it.id) } ?: entity
        jsonResponseDaoHBYC.insertFormResponse(updated)
    }

    suspend fun insertFormResponseHBYC(entity: FormResponseJsonEntityHBYC) =
        jsonResponseDaoHBYC.insertFormResponse(entity)

    suspend fun loadFormResponseJsonHBYC(benId: Long, visitDay: String): String? =
        jsonResponseDaoHBYC.getFormResponse(benId, visitDay)?.formDataJson

    suspend fun getUnsyncedFormsHBYC(): List<FormResponseJsonEntityHBYC> =
        jsonResponseDaoHBYC.getUnsyncedForms()

    suspend fun syncFormToServerHBYC(form: FormResponseJsonEntityHBYC): Boolean {
        return try {
            val request = FormSubmitRequestMapper.fromEntity(form,preferenceDao.getLoggedInUser()!!.userName) ?: return false
            val response = amritApiService.submitFormhbyc(listOf(request))
            response.isSuccessful
        } catch (e: Exception) { false }
    }

    suspend fun markFormAsSyncedHBYC(id: Int) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        jsonResponseDaoHBYC.markAsSynced(id, timestamp)
    }

    suspend fun saveDownloadedVisitListHBYC(list: List<HBNCVisitResponse>) {
        for (item in list) {
            try {
                if (item.fields == null) continue
                val visitDay = item.fields["visit_day"]?.asString ?: continue
                val visitDate = item.visitDate ?: "-"
                val benId = item.beneficiaryId
                val hhId = item.houseHoldId

                val fieldsJson = JSONObject()
                item.fields.entrySet().forEach { (key, jsonElement) ->
                    val value = if (jsonElement.isJsonNull) JSONObject.NULL else jsonElement.asString
                    fieldsJson.put(key, value)
                }

                val fullJson = JSONObject().apply {
                    put("formId", HBYC_FORM_ID)
                    put("beneficiaryId", benId)
                    put("houseHoldId", hhId)
                    put("visitDate", visitDate)
                    put("fields", fieldsJson)
                }

                val entity = FormResponseJsonEntityHBYC(
                    benId = benId,
                    hhId = hhId,
                    visitDay = visitDay,
                    visitDate = visitDate,
                    formId = HBYC_FORM_ID,
                    version = 1,
                    formDataJson = fullJson.toString(),
                    isSynced = true
                )
                insertOrUpdateFormResponseHBYC(entity)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save HBYC visit")
            }
        }
    }
}
