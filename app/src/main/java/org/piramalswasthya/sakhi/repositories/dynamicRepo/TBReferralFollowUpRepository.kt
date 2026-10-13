package org.piramalswasthya.sakhi.repositories.dynamicRepo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.dynamicEntity.TBReferralFollowUpEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.TBReferralFollowUpRequest
import org.piramalswasthya.sakhi.model.dynamicEntity.BenWithTbReferralFollowUpDomain
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import org.piramalswasthya.sakhi.utils.HelperUtil
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TBReferralFollowUpRepository @Inject constructor(
    @Named("gsonAmritApi") private val api: AmritApiService,
    private val preferences: PreferenceDao,
    db: InAppDb
) {
    private val dao = db.tbReferralFollowUpDao()
    private val gson = Gson()

    val observeRecommendedTpt: Flow<List<BenWithTbReferralFollowUpDomain>> =
        dao.observeBeneficiariesByFollowUpStatus("RECOMMEND_TPT")
            .map { rows -> rows.map { it.asDomainModel() } }

    val tptListCount: Flow<Int> = observeRecommendedTpt.map { it.size }


    suspend fun getVisits(benId: Long): List<TBReferralFollowUpEntity> = withContext(Dispatchers.IO) {
        dao.getByBeneficiary(benId)
    }

    suspend fun save(entity: TBReferralFollowUpEntity) = withContext(Dispatchers.IO) {
        val existing = dao.getByBeneficiaryAndDate(entity.benId, entity.followUpDate)
        val stored = entity.copy(id = existing?.id ?: entity.id, isSynced = false)
        dao.insertOrUpdate(stored)
    }

    suspend fun pushPending(): Boolean = withContext(Dispatchers.IO) {
        var success = true
        dao.getUnsynced().forEach { entity ->
            try {
                val response = api.saveTBReferralFollowUp(
                    TBReferralFollowUpRequest(
                        benId = entity.benId,
                        houseHoldId = entity.houseHoldId,
                        fields = gson.fromJson(entity.fieldsJson, object : TypeToken<Map<String, Any?>>() {}.type)
                    )
                )
                val responseText = response.body()?.string()
                val statusCode = responseText?.let {
                    try { JSONObject(it).optInt("statusCode", 200) } catch (_: Exception) { 200 }
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
        try {
            val user = preferences.getLoggedInUser() ?: return@withContext false
            val response = api.getTBReferralFollowUps(
                GetDataPaginatedRequest(
                    ashaId = user.userId,
                    pageNo = 0,
                    fromDate = HelperUtil.getCurrentDate(Konstants.defaultTimeStamp),
                    toDate = HelperUtil.getCurrentDate()
                )
            )
            if (!response.isSuccessful) return@withContext false
            val raw = response.body()?.string() ?: return@withContext false
            // The API wraps records as { data: { data: [...] } }.
            // Keep accepting the older/direct array response as well.
            val responseData = JSONObject(raw).opt("data") ?: return@withContext false
            val dataValue = when (responseData) {
                is JSONObject -> responseData.opt("data")
                else -> responseData
            } ?: return@withContext false
            val data = when (dataValue) {
                is org.json.JSONArray -> dataValue
                is String -> org.json.JSONArray(dataValue)
                else -> return@withContext false
            }
            val type = object : TypeToken<List<TBReferralFollowUpRequest>>() {}.type
            val serverRows: List<TBReferralFollowUpRequest> = gson.fromJson(data.toString(), type)
            serverRows.forEach { row ->
                val followUpDate = row.fields["follow_up_date"]?.toString().orEmpty()
                if (row.benId <= 0 || followUpDate.isBlank()) return@forEach
                val existing = dao.getByBeneficiaryAndDate(row.benId, followUpDate)
                if (existing?.isSynced == false) return@forEach
                val referredOn = row.fields["referred_on_date"]?.toString().orEmpty()
                val status = row.fields["follow_up_status"]?.toString()
                dao.insertOrUpdate(
                    TBReferralFollowUpEntity(
                        id = existing?.id ?: 0,
                        benId = row.benId,
                        houseHoldId = row.houseHoldId,
                        referredOnDate = referredOn,
                        followUpDate = followUpDate,
                        followUpStatus = status,
                        fieldsJson = gson.toJson(row.fields),
                        isSynced = true
                    )
                )
            }
            true
        } catch (_: Exception) { false }
    }
}
