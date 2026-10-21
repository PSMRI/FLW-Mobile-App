package org.piramalswasthya.sakhi.repositories

import androidx.room.withTransaction
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.GeneralCheckupDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.GeneralCheckupCache
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.checkupFromFormRecord
import org.piramalswasthya.sakhi.model.dynamicModel.HBNCVisitRequest
import org.piramalswasthya.sakhi.model.monthKey
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.utils.HelperUtil
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * General Check-up records (FLW-1229): save with the GC referral, dedup, history and upload.
 * The referral rides the existing NCD_REFER table and referral push/pull (type = GENERAL_CHECKUP);
 * its follow-up uses the existing referral follow-up form (CDTF_001).
 */
@Singleton
class GeneralCheckupRepo @Inject constructor(
    private val database: InAppDb,
    private val dao: GeneralCheckupDao,
    @Named("gsonAmritApi") private val amritApiService: AmritApiService,
    private val preferenceDao: PreferenceDao,
    private val userRepo: UserRepo,
) {

    sealed class SaveResult {
        object Saved : SaveResult()
        data class Duplicate(val existing: GeneralCheckupCache) : SaveResult()
    }

    fun getHistory(benId: Long): Flow<List<GeneralCheckupCache>> = dao.getHistory(benId)

    fun getCheckupCountPerBen(): Flow<Map<Long, Int>> =
        dao.getCheckupCountPerBen().map { rows -> rows.associate { it.benId to it.count } }

    /**
     * BRD §8: the same beneficiary, problem and calendar month (of the referral date) gives one referral.
     * "Other" counts as the same problem only when the typed text matches.
     */
    suspend fun findDuplicate(
        benId: Long,
        referralDate: Long,
        problemIds: Collection<String>,
        otherProblemIds: Collection<String>,
        otherText: String?
    ): GeneralCheckupCache? = withContext(Dispatchers.IO) {
        dao.getCheckupsInMonth(benId, monthKey(referralDate)).firstOrNull { existing ->
            existing.problemIds.any { id ->
                when {
                    id !in problemIds -> false
                    id in otherProblemIds ->
                        existing.otherText?.trim().equals(otherText?.trim(), ignoreCase = true)
                    else -> true
                }
            }
        }
    }

    /** Saves the check-up and its referral together, so neither exists without the other. */
    suspend fun saveCheckupWithReferral(
        checkup: GeneralCheckupCache,
        referral: ReferalCache,
        otherProblemIds: Collection<String>
    ): SaveResult = withContext(Dispatchers.IO) {
        database.withTransaction {
            findDuplicate(
                checkup.benId, checkup.referralDate, checkup.problemIds, otherProblemIds, checkup.otherText
            )?.let { return@withTransaction SaveResult.Duplicate(it) }
            database.referalDao.upsert(referral)
            dao.insertCheckup(checkup)
            SaveResult.Saved
        }
    }

    // ── Sync ─────────────────────────────────────────────────────────────────

    suspend fun pushUnsynced(): Boolean = withContext(Dispatchers.IO) {
        // Rows left in SYNCING by a killed worker go back to UNSYNCED before the push.
        dao.resetCheckupSyncState(SyncState.SYNCING, SyncState.UNSYNCED)
        val pending = dao.getCheckupsBySyncState(SyncState.UNSYNCED)
        if (pending.isEmpty()) return@withContext true
        val ids = pending.map { it.checkupId }
        dao.moveCheckupSyncState(ids, SyncState.UNSYNCED, SyncState.SYNCING)
        val userName = preferenceDao.getLoggedInUser()?.userName.orEmpty()
        val json = callWithAuth {
            amritApiService.saveGeneralCheckups(pending.map { it.asSubmitRequest(it.createdBy.ifBlank { userName }) })
        }
        if (json == null) {
            dao.moveCheckupSyncState(ids, SyncState.SYNCING, SyncState.UNSYNCED)
            return@withContext false
        }
        val data = json.optJSONObject("data")
        val saved = data?.optJSONArray("saved")?.checkupIds()
        if (saved == null) {
            // No per-record result: the whole batch was accepted.
            dao.moveCheckupSyncState(ids, SyncState.SYNCING, SyncState.SYNCED)
            return@withContext true
        }
        // A rejected row (e.g. server-side DUPLICATE, FLW-1228 point 5) is not retried; it stays as local history.
        val rejected = data.optJSONArray("rejected")?.checkupIds().orEmpty()
        if (rejected.isNotEmpty()) Timber.w("General Check-up rejected by server: $rejected")
        val done = (saved + rejected).filter { it in ids }
        dao.moveCheckupSyncState(done, SyncState.SYNCING, SyncState.SYNCED)
        dao.moveCheckupSyncState(ids - done.toSet(), SyncState.SYNCING, SyncState.UNSYNCED)
        done.size == ids.size
    }

    private fun org.json.JSONArray.checkupIds(): List<String> =
        (0 until length()).mapNotNull { optJSONObject(it)?.optString("checkupId")?.takeIf(String::isNotBlank) }

    /**
     * Pulls the ASHA's check-ups (generalCheckUp/getAll) so history, counts and the duplicate check
     * survive a reinstall. Local rows win: a checkupId already on the device is not overwritten.
     */
    suspend fun pullFromServer(): Boolean = withContext(Dispatchers.IO) {
        val user = preferenceDao.getLoggedInUser() ?: return@withContext false
        val json = callWithAuth {
            amritApiService.getAllGeneralCheckups(
                HBNCVisitRequest(
                    fromDate = HelperUtil.getCurrentDate(Konstants.defaultTimeStamp),
                    toDate = HelperUtil.getCurrentDate(),
                    pageNo = 0,
                    ashaId = user.userId,
                    userName = user.userName
                )
            )
        } ?: return@withContext false
        // data is the record list; also accept it wrapped as { data: [...] }.
        val records = (json.optJSONArray("data") ?: json.optJSONObject("data")?.optJSONArray("data"))
            ?.let { JsonParser.parseString(it.toString()).asJsonArray }
            ?.mapNotNull { it.takeIf { e -> e.isJsonObject }?.asJsonObject }
            .orEmpty()
        // A record with missing or unreadable details is skipped, not fatal.
        val pulled = records.mapNotNull { checkupFromFormRecord(it) }
        if (records.size != pulled.size) Timber.w("General Check-up pull skipped ${records.size - pulled.size} unreadable records")
        if (pulled.isNotEmpty()) {
            val knownBens = dao.getExistingBenIds(pulled.map { it.benId }.distinct()).toSet()
            dao.insertCheckupsIfAbsent(pulled.filter { it.benId in knownBens })
        }
        true
    }

    /**
     * Calls a GC endpoint and returns the parsed envelope on statusCode 200, or null on any failure.
     * Refreshes the token once on 401 / 5002.
     */
    private suspend fun callWithAuth(
        retry: Boolean = true,
        call: suspend () -> Response<ResponseBody>
    ): JSONObject? {
        val user = preferenceDao.getLoggedInUser() ?: return null
        val response = call()
        if (!response.isSuccessful) {
            Timber.e("General Check-up API failed: HTTP ${response.code()}")
            return null
        }
        val json = JSONObject(response.body()?.string() ?: return null)
        // Some GC endpoints reply with { "status": "SUCCESS" } and omit statusCode.
        if (!json.has("statusCode") && json.optString("status").equals("SUCCESS", ignoreCase = true)) return json
        return when (json.optInt("statusCode", -1)) {
            200 -> json
            401, 5002 -> if (retry && userRepo.refreshTokenTmc(user.userName, user.password))
                callWithAuth(retry = false, call) else null
            else -> {
                Timber.e("General Check-up API failed: ${json.optString("errorMessage")}")
                null
            }
        }
    }
}
