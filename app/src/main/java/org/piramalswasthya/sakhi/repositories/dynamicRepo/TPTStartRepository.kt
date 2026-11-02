package org.piramalswasthya.sakhi.repositories.dynamicRepo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto
import org.piramalswasthya.sakhi.model.dynamicEntity.TPTFollowUpApiRecord
import org.piramalswasthya.sakhi.model.dynamicEntity.TPTFollowUpApiRequest
import org.piramalswasthya.sakhi.model.dynamicEntity.TPTFollowUpEntity
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TPTStartRepository @Inject constructor(
    db: InAppDb,
    private val schemaRepository: NCDFollowUpFormRepository,
    @Named("gsonAmritApi") private val api: AmritApiService,
    private val preferences: PreferenceDao
) {
    private val dao = db.tptFollowUpDao()
    private val benDao = db.benDao
    private val gson = Gson()

    fun observeFollowUps(benId: Long): Flow<List<TPTFollowUpEntity>> =
        dao.observeByBeneficiary(benId)

    fun observeAllFollowUps(): Flow<List<TPTFollowUpEntity>> = dao.observeAll()

    suspend fun getFollowUps(benId: Long): List<TPTFollowUpEntity> = withContext(Dispatchers.IO) {
        dao.getByBeneficiary(benId)
    }

    suspend fun getFormSchema(formId: String): FormSchemaDto? = withContext(Dispatchers.IO) {
        schemaRepository.getSavedSchema(formId)?.let { FormSchemaDto.fromJson(it.schemaJson) }
            ?: schemaRepository.getFormSchema(formId)
    }

    suspend fun saveLocally(response: TPTFollowUpEntity) = withContext(Dispatchers.IO) {
        dao.upsert(response.copy(isSynced = false))
    }

    suspend fun pushPending(): Boolean = withContext(Dispatchers.IO) {
        val userId = preferences.getLoggedInUser()?.userId ?: return@withContext false
        var success = true
        dao.getUnsynced().forEach { entity ->
            try {
                val fields = fieldsForApi(entity.fieldsJson)
                val response = api.saveTPTFollowUp(
                    TPTFollowUpApiRequest(entity.benId, userId, fields)
                )
                val body = response.body()?.string()
                val statusCode = body?.let {
                    runCatching { JSONObject(it).optInt("statusCode", 200) }.getOrDefault(200)
                } ?: 200
                if (response.isSuccessful && statusCode == 200) {
                    dao.markSynced(entity.id, System.currentTimeMillis())
                } else success = false
            } catch (_: Exception) {
                success = false
            }
        }
        success
    }

    suspend fun pullFromServer(): Boolean = withContext(Dispatchers.IO) {
        val user = preferences.getLoggedInUser() ?: return@withContext false
        try {
            val response = api.getTPTFollowUps(
                GetDataPaginatedRequest(
                    ashaId = user.userId,
                    pageNo = 0,
                    fromDate = HelperUtil.getCurrentDate(Konstants.defaultTimeStamp),
                    toDate = HelperUtil.getCurrentDate()
                )
            )
            if (!response.isSuccessful) return@withContext false
            val raw = response.body()?.string() ?: return@withContext false
            // The API wraps the row array as { data: { data: [...] } }.
            // Also accept a direct array for compatibility with older responses.
            val responseData = JSONObject(raw).opt("data")
            val data = when (responseData) {
                is JSONObject -> responseData.opt("data")
                else -> responseData
            }
            val rows = when (data) {
                is JSONArray -> data
                is String -> runCatching { JSONArray(data) }.getOrNull()
                else -> null
            } ?: return@withContext false
            val recordType = object : TypeToken<List<TPTFollowUpApiRecord>>() {}.type
            val records: List<TPTFollowUpApiRecord> = gson.fromJson(rows.toString(), recordType)
            records.forEach { record -> saveServerRecord(record) }
            true
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun saveServerRecord(record: TPTFollowUpApiRecord) {
        if (record.benId <= 0 || record.fields.isEmpty()) return
        val uiFields = record.fields.mapValues { (key, value) ->
            if (key in DATE_FIELD_IDS) value?.toString()?.let(::toUiDate).orEmpty()
            else value?.toString().orEmpty()
        }
        val followUpNo = Regex("Month-(\\d+)", RegexOption.IGNORE_CASE)
            .find(uiFields["monthly_follow_up"].orEmpty())?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
        val existing = dao.getByFollowUpNo(record.benId, followUpNo)
        if (existing?.isSynced == false) return
        val householdId = existing?.houseHoldId ?: benDao.getBenById(record.benId)?.hhId ?: 0L
        dao.upsert(
            TPTFollowUpEntity(
                id = existing?.id ?: 0,
                benId = record.benId,
                houseHoldId = householdId,
                visitNo = existing?.visitNo ?: 1,
                followUpNo = followUpNo,
                treatmentType = uiFields["regimen_type"],
                treatmentStartDate = uiFields["treatment_start_date"],
                followUpDate = uiFields["follow_up_date"],
                fieldsJson = gson.toJson(uiFields),
                isSynced = true
            )
        )
    }

    private fun fieldsForApi(fieldsJson: String): Map<String, String> {
        val values: Map<String, Any?> = gson.fromJson(
            fieldsJson,
            object : TypeToken<Map<String, Any?>>() {}.type
        )
        return values.mapValues { (key, value) ->
            val text = value?.toString().orEmpty()
            if (key in DATE_FIELD_IDS && text.isNotBlank()) toApiDate(text) else text
        }
    }

    private fun toApiDate(value: String): String = parseDate(value)?.let {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(it)
    } ?: value

    private fun toUiDate(value: String): String = parseDate(value)?.let {
        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(it)
    } ?: value

    private fun parseDate(value: String) = listOf(
        "dd-MM-yyyy", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd"
    ).firstNotNullOfOrNull { pattern ->
        runCatching { SimpleDateFormat(pattern, Locale.ENGLISH).apply { isLenient = false }.parse(value) }
            .getOrNull()
    }

    private companion object {
        val DATE_FIELD_IDS = setOf(
            "treatment_start_date", "expected_treatment_completion_date", "follow_up_date",
            "actual_completion_date", "date_of_death"
        )
    }
}
