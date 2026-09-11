package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.room.dao.BadgeDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.badges.domain.BadgeDefinitions
import org.piramalswasthya.sakhi.badges.domain.BadgeKind
import org.piramalswasthya.sakhi.model.BadgeConfigCache
import org.piramalswasthya.sakhi.model.BadgeEarnedCache
import org.piramalswasthya.sakhi.model.BadgeStreakFreezeCache
import org.piramalswasthya.sakhi.network.BadgeApiService
import org.piramalswasthya.sakhi.network.BadgeEarnedDTO
import org.piramalswasthya.sakhi.network.BadgeEarnedPush
import timber.log.Timber

/**
 * Badge synchronization (LLD §2.2 / §4.2): pulls central config, freeze
 * windows and previously earned milestones; pushes newly earned milestones
 * (badgeId, level, earnedAt only — no beneficiary data).
 *
 * Every step degrades independently: with the server down or endpoints not
 * yet deployed, the module keeps functioning on last-known or compiled
 * defaults (LLD §5.2), so this worker always succeeds.
 */
@HiltWorker
class BadgeSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val api: BadgeApiService,
    private val badgeDao: BadgeDao,
    private val pref: PreferenceDao
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "BadgeSyncWorker"
    }

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()

        try {
            api.getConfig().body()?.config?.takeIf { it.isNotEmpty() }?.let { config ->
                badgeDao.upsertConfig(config.map { (k, v) -> BadgeConfigCache(k, v, now) })
            }
        } catch (e: Exception) {
            Timber.d("Badges: config pull skipped (${e.message})")
        }

        try {
            api.getFreezes().body()?.freezes?.let { freezes ->
                badgeDao.replaceFreezes(freezes.map {
                    BadgeStreakFreezeCache(
                        badgeId = it.badgeId ?: "",
                        startDate = it.startDate,
                        endDate = it.endDate
                    )
                })
            }
        } catch (e: Exception) {
            Timber.d("Badges: freeze pull skipped (${e.message})")
        }

        val userId = try {
            pref.getLoggedInUser()?.userId
        } catch (e: Exception) {
            null
        } ?: return Result.success()

        // Restore milestones on reinstall (insert-IGNORE keeps local awards intact).
        //
        // Only badges whose awards carry no caseRef are restored from the server. The push
        // deliberately omits caseRef, because for per-case badges it is a beneficiary id and
        // that never leaves the device (LLD §4) — so a restored row would come back with an
        // empty caseRef and miss the local uniqueness key (userId, badgeId, level, caseRef).
        // Quarterly awards for two different quarters would collapse into one row on the way
        // out and duplicate against the locally derived ones on the way back in.
        //
        // Nothing is lost by skipping them: quarterly and per-case awards are rederived from
        // the ASHA's own re-synced records by BadgeFactsReader, which is how reinstall
        // restore works with no backend at all. Carrying a stable non-PII award key through
        // the API would let the server hold them too, and needs the field on both sides.
        try {
            api.getEarned().body()?.earned?.let { restored ->
                val restorable = restored.filter { dto ->
                    when (BadgeDefinitions.byId(dto.badgeId)?.kind) {
                        BadgeKind.STREAK_WEEKLY,
                        BadgeKind.STREAK_MONTHLY,
                        BadgeKind.CUMULATIVE -> true
                        // QUARTERLY and PER_CASE carry a caseRef the payload cannot express.
                        else -> false
                    }
                }
                badgeDao.insertEarned(restorable.map {
                    BadgeEarnedCache(
                        userId = userId, badgeId = it.badgeId, level = it.level,
                        earnedAt = it.earnedAt, synced = true
                    )
                })
            }
        } catch (e: Exception) {
            Timber.d("Badges: earned restore skipped (${e.message})")
        }

        try {
            val unsynced = badgeDao.getUnsyncedEarned(userId)
            if (unsynced.isNotEmpty()) {
                val response = api.postEarned(
                    BadgeEarnedPush(
                        userId = userId,
                        badges = unsynced.map {
                            // caseRef deliberately omitted: beneficiary-level
                            // data never leaves the device (LLD §4)
                            BadgeEarnedDTO(it.badgeId, it.level, it.earnedAt)
                        }
                    )
                )
                if (response.isSuccessful) badgeDao.markEarnedSynced(unsynced.map { it.id })
            }
        } catch (e: Exception) {
            Timber.d("Badges: earned push skipped (${e.message})")
        }

        return Result.success()
    }
}
