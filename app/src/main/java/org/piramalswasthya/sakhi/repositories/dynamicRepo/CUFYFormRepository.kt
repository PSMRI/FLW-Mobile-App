package org.piramalswasthya.sakhi.repositories.dynamicRepo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.helpers.dynamicMapper.FormSubmitRequestMapper
import org.piramalswasthya.sakhi.model.BottleItem
import org.piramalswasthya.sakhi.model.dynamicEntity.CUFYFormResponseJsonEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaEntity
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitListResponse
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitResponse
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
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

    suspend fun getBottleList(benId: Long, formId: String): List<BottleItem> {
        val jsonList = jsonResponseDao.getFormJsonList(benId, formId)

        val result = mutableListOf<BottleItem>()

        jsonList.forEachIndexed { index, formJson ->
            try {
                val root = JSONObject(formJson)
                val fields = root.optJSONObject("fields")
                val date = fields?.optString("ifa_provision_date", "-") ?: "-"
                val count = fields?.optString("ifa_bottle_count", "-") ?: "-"

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


   /* suspend fun insertOrUpdateFormResponse(entity: CUFYFormResponseJsonEntity) {
        val existing = jsonResponseDao.getFormResponse(entity.benId, entity.visitDate)
        val updated = existing?.let { entity.copy(id = it.id) } ?: entity
        jsonResponseDao.insertFormResponse(updated)
    }*/

    suspend fun insertOrUpdateFormResponse(entity: CUFYFormResponseJsonEntity) {
        Timber.tag("CUFYFormRepository").d("🔄 insertOrUpdateFormResponse: START - entity.id=${entity.id}, benId=${entity.benId}, visitDate=${entity.visitDate}")

        val existing = jsonResponseDao.getFormResponse(entity.benId, entity.visitDate)
        Timber.tag("CUFYFormRepository").d("🔍 insertOrUpdateFormResponse: Found existing record - ${if (existing != null) "ID=${existing.id}" else "null"}")

        val updated = existing?.let {
            val result = entity.copy(id = it.id)
            Timber.tag("CUFYFormRepository").d("📝 insertOrUpdateFormResponse: Updating existing record ID=${it.id}")
            result
        } ?: run {
            Timber.tag("CUFYFormRepository").d("🆕 insertOrUpdateFormResponse: Creating new record")
            entity
        }

        jsonResponseDao.insertFormResponse(updated)
        Timber.tag("CUFYFormRepository").d("✅ insertOrUpdateFormResponse: COMPLETED - Final entity ID=${updated.id}")
    }

   /* suspend fun insertFormResponse(entity: CUFYFormResponseJsonEntity) {
        if (entity.id > 0) {

            val existingRecord = jsonResponseDao.getFormResponseById(entity.id)
            if (existingRecord != null) {
                val updatedEntity = entity.copy(createdAt = existingRecord.createdAt)
                jsonResponseDao.updateFormResponse(updatedEntity)
                return
            }
        }

        jsonResponseDao.insertFormResponse(entity)
    }*/


    suspend fun insertFormResponse(entity: CUFYFormResponseJsonEntity) {
        Timber.tag("CUFYFormRepository").d("🔄 insertFormResponse: START - entity.id=${entity.id}, benId=${entity.benId}, visitDate=${entity.visitDate}")

        if (entity.id > 0) {
            Timber.tag("CUFYFormRepository").d("🔍 insertFormResponse: Checking for existing record with ID=${entity.id}")
            val existingRecord = jsonResponseDao.getFormResponseById(entity.id)

            if (existingRecord != null) {
                Timber.tag("CUFYFormRepository").d("📝 insertFormResponse: Found existing record, UPDATING ID=${entity.id}")
                val updatedEntity = entity.copy(createdAt = existingRecord.createdAt)
                val updateResult = jsonResponseDao.updateFormResponse(updatedEntity)
                Timber.tag("CUFYFormRepository").d("✅ insertFormResponse: Update completed, rows affected=$updateResult")
                return
            } else {
                Timber.tag("CUFYFormRepository").w("⚠️ insertFormResponse: No existing record found for ID=${entity.id}, will INSERT as new")
            }
        } else {
            Timber.tag("CUFYFormRepository").d("🆕 insertFormResponse: ID=0, INSERTING as new record")
        }

        Timber.tag("CUFYFormRepository").d("💽 insertFormResponse: Calling DAO insertFormResponse")
        jsonResponseDao.insertFormResponse(entity)
        Timber.tag("CUFYFormRepository").d("✅ insertFormResponse: COMPLETED")
    }

    suspend fun loadFormResponseJson(benId: Long, visitDay: String): String? =
        jsonResponseDao.getFormResponse(benId, visitDay)?.formDataJson

    suspend fun getUnsyncedForms(formId: String): List<CUFYFormResponseJsonEntity> =
        jsonResponseDao.getUnsyncedForms(formId)

    suspend fun getSavedDataByFormId(formId: String, benId: Long  ): List<CUFYFormResponseJsonEntity> =
        jsonResponseDao.getFormsDataByFormID(formId, benId)



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


    suspend fun getCurrentSamStatus(benId: Long): String {
        Timber.tag("CUFYFormRepository").d("🔍 getCurrentSamStatus: benId=$benId")


        val samForms = jsonResponseDao.getFormsDataByFormID(
            FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID,
            benId
        ).sortedByDescending { it.visitDate }

        Timber.tag("CUFYFormRepository").d("📋 getCurrentSamStatus: Found ${samForms.size} SAM forms")

        if (samForms.isEmpty()) {
            return "Check SAM"
        }

        val latestForm = samForms.first()
        Timber.tag("CUFYFormRepository").d("📄 getCurrentSamStatus: Latest form data: ${latestForm.formDataJson}")

        return try {
            val json = JSONObject(latestForm.formDataJson)
            val fields = json.optJSONObject("fields") ?: JSONObject()

            val isReferredToNRC = fields.optString("is_child_referred_nrc") == "Yes"
            val isAdmittedToNRC = fields.optString("is_child_admitted_nrc") == "Yes"
            val isDischargedFromNRC = fields.optString("is_child_discharged_nrc") == "Yes"
            val samStatus = fields.optString("sam_status")

            Timber.tag("CUFYFormRepository").d("🔍 getCurrentSamStatus: isReferred=$isReferredToNRC, isAdmitted=$isAdmittedToNRC, isDischarged=$isDischargedFromNRC, samStatus=$samStatus")


            when {
                isDischargedFromNRC -> {
                    if (samStatus == "Improved") "Check SAM" else "Follow up SAM"
                }
                isAdmittedToNRC -> "NRC Admitted"
                isReferredToNRC -> "Referred to NRC"
                else -> "Check SAM"
            }
        } catch (e: Exception) {
            Timber.tag("CUFYFormRepository").e(e, "❌ getCurrentSamStatus: Error parsing form data")
            "Check SAM"
        }
    }
}
