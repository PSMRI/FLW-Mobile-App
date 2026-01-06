package org.piramalswasthya.sakhi.repositories.dynamicRepo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.dynamicEntity.*
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.utils.HelperUtil
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class NCDFollowUpFormRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("gsonAmritApi") private val amritApiService: AmritApiService,
    private val pref: PreferenceDao,
    private val db: InAppDb
) {

    private val jsonResponseDao = db.NCDReferalFormResponseJsonDao()
    private val formSchemaDao = db.formSchemaDao()

    /* ---------------- SCHEMA ---------------- */
    suspend fun getSavedSchema(formId: String): FormSchemaEntity? = formSchemaDao.getSchema(formId)

    suspend fun getFormSchema(formId: String): FormSchemaDto? = withContext(Dispatchers.IO) {
        getSavedSchema(formId)?.let { return@withContext FormSchemaDto.fromJson(it.schemaJson) }
        try {
            val response = amritApiService.fetchFormSchema(formId, pref.getCurrentLanguage().symbol)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { apiSchema ->
                    val local = formSchemaDao.getSchema(apiSchema.formId)
                    if (local == null || local.version < apiSchema.version) saveFormSchemaToDb(apiSchema)
                    return@withContext apiSchema
                }
            }
        } catch (_: Exception) {}
        null
    }

    private suspend fun saveFormSchemaToDb(schema: FormSchemaDto) {
        formSchemaDao.insertOrUpdate(
            FormSchemaEntity(
                formId = schema.formId,
                formName = schema.formName,
                language = pref.getCurrentLanguage().symbol,
                version = schema.version,
                schemaJson = schema.toJson()
            )
        )
    }

    /* ---------------- DOWNSYNC ---------------- */
    suspend fun fetchFormsFromServer(
        formId: String,
        userName: String
    ): List<NCDReferalFormResponseJsonEntity> {
        return try {

            val user = pref.getLoggedInUser()
                ?: throw IllegalStateException("No user logged in")

            val hbncRequest = HBNCVisitRequest(
                fromDate = HelperUtil.getCurrentDate(Konstants.defaultTimeStamp),
                toDate = HelperUtil.getCurrentDate(),
                pageNo = 0,
                ashaId = user.userId,
                userName = user.userName
            )

            val response = amritApiService.getAllFormNCDFollowUp(hbncRequest)

            if (response.isSuccessful) {
                // ✅ Server response wrapper
                val responseBody = response.body()
                val bodyList: List<FormNCDFollowUpSubmitRequest> = responseBody?.data ?: emptyList()

                bodyList.map {
                    NCDReferalFormResponseJsonEntity(
                        id = 0,
                        benId = it.benId,
                        hhId = it.hhId,
                        visitNo = it.visitNo,
                        followUpNo = it.followUpNo,
                        treatmentStartDate = it.treatmentStartDate,
                        followUpDate = it.followUpDate,
                        diagnosisCodes = it.diagnosisCodes,
                        formId = it.formId,
                        version = it.version,
                        formDataJson = it.formDataJson,
                        isSynced = true
                    )
                }
            } else emptyList()
        } catch (e: IOException) {
            Timber.w(e, "fetchFormsFromServer failed")
            throw e
        }
    }


    suspend fun saveDownloadedForms(forms: List<NCDReferalFormResponseJsonEntity>) {
        forms.forEach { entity ->
            val existing = jsonResponseDao.getFormResponse(entity.benId, entity.visitNo, entity.followUpNo)
            if (existing == null || existing.updatedAt < entity.updatedAt) {
                jsonResponseDao.insertFormResponse(entity)
                Timber.d("📥 Saved form id=${entity.id} benId=${entity.benId}")
            }
        }
    }

    /* ---------------- LOCAL SAVE ---------------- */
    suspend fun saveVisitOrFollowUp(
        benId: Long,
        hhId: Long,
        visitNo: Int,
        followUpNo: Int,
        treatmentStartDate: String,
        followUpDate: String? = null,
        diagnosisList: List<String>,
        formId: String,
        formJson: String,
        version: Int = 1
    ) {
        val entity = NCDReferalFormResponseJsonEntity(
            benId = benId,
            hhId = hhId,
            visitNo = visitNo,
            followUpNo = followUpNo,
            treatmentStartDate = treatmentStartDate,
            followUpDate = followUpDate,
            diagnosisCodes = diagnosisList.joinToString(","),
            formId = formId,
            version = version,
            formDataJson = formJson,
            isSynced = false
        )
        insertOrUpdateFormResponse(entity)
    }

    private suspend fun insertOrUpdateFormResponse(entity: NCDReferalFormResponseJsonEntity) {
        val existing = jsonResponseDao.getFormResponse(entity.benId, entity.visitNo, entity.followUpNo)
        val updated = existing?.let { mergeFollowUp(existing, entity) } ?: entity
        jsonResponseDao.insertFormResponse(updated)
    }

    private fun mergeFollowUp(
        existing: NCDReferalFormResponseJsonEntity,
        newEntity: NCDReferalFormResponseJsonEntity
    ): NCDReferalFormResponseJsonEntity {
        return try {
            val existingJson = JSONObject(existing.formDataJson)
            val newJson = JSONObject(newEntity.formDataJson)

            val mergedFields = JSONObject()
            newJson.optJSONObject("fields")?.let { newFields ->
                newFields.keys().forEach { key -> mergedFields.put(key, newFields.get(key)) }
            }

            val mergedJson = JSONObject().apply {
                put("formId", newJson.optString("formId"))
                put("beneficiaryId", newJson.optLong("beneficiaryId"))
                put("houseHoldId", newJson.optLong("houseHoldId"))
                put("visitNo", newJson.optInt("visitNo"))
                put("followUpNo", newJson.optInt("followUpNo"))
                put("fields", mergedFields)
            }

            newEntity.copy(formDataJson = mergedJson.toString(), updatedAt = System.currentTimeMillis())
        } catch (e: Exception) {
            Timber.e(e, "mergeFollowUp failed")
            newEntity.copy(updatedAt = System.currentTimeMillis())
        }
    }

    /* ---------------- HISTORY ---------------- */
    suspend fun getAllVisitsByBeneficiary(benId: Long, formId: String): List<NCDReferalFormResponseJsonEntity> =
        jsonResponseDao.getAllVisitsByBeneficiary(benId, formId)

    /* ---------------- SYNC ---------------- */
    suspend fun getUnsyncedForms(formId: String): List<NCDReferalFormResponseJsonEntity> =
        jsonResponseDao.getUnsyncedForms(formId)

    suspend fun markFormAsSynced(id: Int) {
        jsonResponseDao.markAsSynced(id, System.currentTimeMillis())
    }

    suspend fun syncFormToServer(
        userName: String,
        formName: String,
        request: FormNCDFollowUpSubmitRequest
    ): Boolean {
        return try {
            val response = amritApiService.submitNCDFollowUp( listOf(request))
            response.isSuccessful
        } catch (e: Exception) {
            Timber.e(e, "syncFormToServer failed")
            false
        }
    }
}
