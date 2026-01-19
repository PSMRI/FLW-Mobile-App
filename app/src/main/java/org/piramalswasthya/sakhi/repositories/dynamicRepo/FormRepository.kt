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
import org.piramalswasthya.sakhi.model.dynamicEntity.anc.ANCFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.ANC_FORM_ID
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.HBNC_FORM_ID
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.HBYC_FORM_ID
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.LF_MDA_CAMPAIGN
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

    private val jsonResponseDaoANC = db.formResponseJsonDaoANC()

    val ALL_FORM_IDS = listOf(
        FormConstants.HBNC_FORM_ID,
        FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID,
        FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID,
        FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID,
        FormConstants.IFA_DISTRIBUTION_FORM_ID,
        FormConstants.EYE_SURGERY_FORM_ID,
        FormConstants.HBYC_FORM_ID,
        FormConstants.MOSQUITO_NET_FORM_ID,
        FormConstants.MDA_DISTRIBUTION_FORM_ID,
        FormConstants.CDTF_001,
        FormConstants.ANC_FORM_ID,
        FormConstants.ORS_CAMPAIGN_FORM_ID,
        FormConstants.PULSE_POLIO_CAMPAIGN_FORM_ID,
        LF_MDA_CAMPAIGN
    )


    suspend fun downloadAllFormsSchemas(lang: String) = withContext(Dispatchers.IO) {
        ALL_FORM_IDS.forEach { formId ->
            try {
                val response = amritApiService.fetchFormSchema(formId, lang)

                if (response.isSuccessful) {
                    val apiSchema = response.body()?.data ?: return@forEach

                    val local = getSavedSchema(formId)

                    if (local == null ||
                        local.version < apiSchema.version ||
                        local.language != lang
                    ) {
                        saveFormSchemaToDb(apiSchema, lang)
                        Log.d("FORM_SYNC", "Updated schema → $formId")
                    } else {
                        Log.d("FORM_SYNC", "Already latest → $formId")
                    }

                } else {
                    Log.e("FORM_SYNC", "Server error → $formId")
                }

            } catch (e: Exception) {
                Log.e("FORM_SYNC", "Exception → $formId", e)
            }
        }
    }



    suspend fun getFormSchema(formId: String, lang: String): FormSchemaDto? = withContext(Dispatchers.IO) {
        try {
            val response = amritApiService.fetchFormSchema(formId, lang)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                val apiSchema = apiResponse?.data

                apiSchema?.let {
                    val localSchema = getSavedSchema(it.formId)
                    if (localSchema == null || localSchema.version < it.version || localSchema.language != lang) {
                        saveFormSchemaToDb(it,lang)
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


    suspend fun saveFormSchemaToDb(schema: FormSchemaDto, lang: String) {
        val entity = FormSchemaEntity(
            formId = schema.formId,
            formName = schema.formName,
            language = lang,
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

    suspend fun getAllAncVisits(request: HBNCVisitRequest): Response<HBNCVisitListResponse> {
        Log.d("anc_visit", "getAllAncVisits: called api")
        return amritApiService.getAllAncVisits(request)}


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

    suspend fun saveDownloadedVisitListANC(list: List<HBNCVisitResponse>) {
        list.forEachIndexed { index, item ->
            try {
                val fields = item.fields ?: return@forEachIndexed

                val benId = item.beneficiaryId
                val visitDate = item.visitDate

                val fieldsJson = JSONObject()
                fields.entrySet().forEach { (key, jsonElement) ->
                    val value = if (jsonElement.isJsonNull) JSONObject.NULL else jsonElement.asString
                    fieldsJson.put(key, value)
                }

                val visitDay = "Visit-${index + 1}"

                val fullJson = JSONObject().apply {
                    put("formId", ANC_FORM_ID)
                    put("beneficiaryId", benId)
                    put("visitDate", visitDate)
                    put("fields", fieldsJson)
                }

                val entity = ANCFormResponseJsonEntity(
                    benId = benId,
                    visitDay = visitDay,
                    visitDate = visitDate,
                    formId = ANC_FORM_ID,
                    version = 1,
                    formDataJson = fullJson.toString(),
                    isSynced = true
                )

                insertFormResponseANC(entity)

            } catch (e: Exception) {
                Timber.e(e, "Failed to save ANC visit ")
            }
        }
    }


    /*suspend fun saveDownloadedVisitListANC(list: List<HBNCVisitResponse>) {
        for (item in list) {
            try {
                if (item.fields == null) continue
                val visitDay = item.fields["visit_day"]?.asString ?: continue
                val visitDate = item.visitDate ?: "-"
                val benId = item.beneficiaryId

                val fieldsJson = JSONObject()
                item.fields.entrySet().forEach { (key, jsonElement) ->
                    val value = if (jsonElement.isJsonNull) JSONObject.NULL else jsonElement.asString
                    fieldsJson.put(key, value)
                }

                val fullJson = JSONObject().apply {
                    put("formId", HBYC_FORM_ID)
                    put("beneficiaryId", benId)
                    put("visitDate", visitDate)
                    put("fields", fieldsJson)
                }

                val entity = ANCFormResponseJsonEntity(
                    benId = benId,
                    visitDay = visitDay,
                    visitDate = visitDate,
                    formId = ANC_FORM_ID,
                    version = 1,
                    formDataJson = fullJson.toString(),
                    isSynced = true
                )
                insertFormResponseANC(entity)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save ANC visit")
            }
        }
    }*/

    suspend fun getInfantByRchIdANC(benId: Long) = jsonResponseDaoANC.getSyncedVisitsByRchId(benId)
    suspend fun getSyncedVisitsByRchIdANC(benId: Long): List<ANCFormResponseJsonEntity> =
        jsonResponseDaoANC.getSyncedVisitsByRchId(benId)

    suspend fun insertOrUpdateFormResponseANC(entity: ANCFormResponseJsonEntity) {
        val existing = jsonResponseDaoANC.getFormResponse(entity.benId, entity.visitDate)
        val updated = existing?.let { entity.copy(id = it.id) } ?: entity
        jsonResponseDaoANC.insertFormResponse(updated)
    }

    suspend fun insertFormResponseANC(entity: ANCFormResponseJsonEntity) =
        jsonResponseDaoANC.insertFormResponse(entity)

    suspend fun loadFormResponseJsonANC(benId: Long, visitDate: String): String? =
        jsonResponseDaoANC.getFormResponse(benId, visitDate)?.formDataJson

    suspend fun getUnsyncedFormsANC(): List<ANCFormResponseJsonEntity> =
        jsonResponseDaoANC.getUnsyncedForms()

    suspend fun syncFormToServerANC(form: ANCFormResponseJsonEntity): Boolean {
        return try {
            val request = FormSubmitRequestMapper.formEntity(form,preferenceDao.getLoggedInUser()!!.userName) ?: return false
            val response = amritApiService.submitFromANC(listOf(request))
            response.isSuccessful
        } catch (e: Exception) { false }
    }

    suspend fun markFormAsSyncedANC(id: Int) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        jsonResponseDaoANC.markAsSynced(id, timestamp)
    }

     suspend fun getLastVisitForBenANC(benId: Long): ANCFormResponseJsonEntity? {
        return try {
            val visits = jsonResponseDaoANC.getVisitsForBen(benId)
            visits.maxByOrNull {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(it.visitDate)?.time ?: 0L
            }
        } catch (e: Exception) {
            null
        }
    }


}
