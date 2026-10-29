package org.piramalswasthya.sakhi.repositories.dynamicRepo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.dynamicEntity.GCReferralFollowUpEntity
import org.piramalswasthya.sakhi.model.dynamicEntity.GCReferralFollowUpRequest
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import org.piramalswasthya.sakhi.utils.HelperUtil
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** General Check-up referral follow-up (general_check_follow_up): save, push and pull, as [TBReferralFollowUpRepository]. */
@Singleton
class GCReferralFollowUpRepository @Inject constructor(
    @Named("gsonAmritApi") private val api: AmritApiService,
    private val preferences: PreferenceDao,
    db: InAppDb
) {
    private val dao = db.gcReferralFollowUpDao()
    private val gson = Gson()

    suspend fun getVisits(benId: Long): List<GCReferralFollowUpEntity> = withContext(Dispatchers.IO) {
        dao.getByBeneficiary(benId)
    }

    suspend fun save(entity: GCReferralFollowUpEntity) = withContext(Dispatchers.IO) {
        val existing = dao.getByBeneficiaryAndDate(entity.benId, entity.followUpDate)
        val stored = entity.copy(id = existing?.id ?: entity.id, isSynced = false)
        dao.insertOrUpdate(stored)
    }

    /** Sends every unsynced follow-up in one saveAll call; all are marked synced only when it succeeds. */
    suspend fun pushPending(): Boolean = withContext(Dispatchers.IO) {
        val pending = dao.getUnsynced()
        if (pending.isEmpty()) return@withContext true
        try {
            val response = api.saveGCReferralFollowUp(
                pending.map { entity ->
                    GCReferralFollowUpRequest(
                        beneficiaryId = entity.benId,
                        houseHoldId = entity.houseHoldId,
                        visitDate = entity.followUpDate,
                        fields = gson.fromJson(entity.fieldsJson, object : TypeToken<Map<String, Any?>>() {}.type)
                    )
                }
            )
            val responseText = response.body()?.string()
            val statusCode = responseText?.let {
                try { JSONObject(it).optInt("statusCode", 200) } catch (_: Exception) { 200 }
            } ?: 200
            if (!response.isSuccessful || statusCode != 200) {
                Timber.e("General Check-up follow-up push failed: HTTP ${response.code()} $responseText")
                return@withContext false
            }
            val now = System.currentTimeMillis()
            pending.forEach { dao.markSynced(it.id, now) }
            true
        } catch (e: Exception) {
            Timber.e(e, "General Check-up follow-up push failed")
            false
        }
    }

    suspend fun pullFromServer(): Boolean = withContext(Dispatchers.IO) {
        try {
            val user = preferences.getLoggedInUser() ?: return@withContext false
            val response = api.getGCReferralFollowUps(
                GetDataPaginatedRequest(
                    ashaId = user.userId,
                    pageNo = 0,
                    fromDate = HelperUtil.getCurrentDate(Konstants.defaultTimeStamp),
                    toDate = HelperUtil.getCurrentDate()
                )
            )
            if (!response.isSuccessful) return@withContext false
            val raw = response.body()?.string() ?: return@withContext false
            // getAll returns a bare [...] of records; { data: [...] } and { data: { data: [...] } } are accepted too.
            val data = when (val root = JSONTokener(raw).nextValue()) {
                is JSONArray -> root
                is JSONObject -> when (val inner = root.opt("data")) {
                    is JSONArray -> inner
                    is JSONObject -> inner.optJSONArray("data")
                    is String -> JSONArray(inner)
                    else -> null
                }
                else -> null
            } ?: return@withContext false
            val type = object : TypeToken<List<GCReferralFollowUpRequest>>() {}.type
            val serverRows: List<GCReferralFollowUpRequest> = gson.fromJson(data.toString(), type)
            serverRows.forEach { row ->
                val fields = row.fields.orEmpty()
                val followUpDate = fields[FIELD_FOLLOW_UP_DATE]?.toString().orEmpty()
                if (row.beneficiaryId <= 0 || followUpDate.isBlank()) return@forEach
                val existing = dao.getByBeneficiaryAndDate(row.beneficiaryId, followUpDate)
                if (existing?.isSynced == false) return@forEach
                dao.insertOrUpdate(
                    GCReferralFollowUpEntity(
                        id = existing?.id ?: 0,
                        benId = row.beneficiaryId,
                        houseHoldId = row.houseHoldId,
                        // Not part of the server record; the referral itself carries the referral date.
                        referredOnDate = existing?.referredOnDate.orEmpty(),
                        followUpDate = followUpDate,
                        followUpStatus = fields[FIELD_STATUS]?.toString(),
                        fieldsJson = gson.toJson(fields),
                        isSynced = true
                    )
                )
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "General Check-up follow-up pull failed")
            false
        }
    }

    companion object {
        /** Field IDs of the general_check_follow_up form. */
        const val FIELD_FOLLOW_UP_DATE = "follow_up_date"
        const val FIELD_STATUS = "followup_status"
    }
}
