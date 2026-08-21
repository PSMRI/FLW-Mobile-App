package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.piramalswasthya.sakhi.database.room.dao.BadgeDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
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
                badgeDao.clearFreezes()
                badgeDao.insertFreezes(freezes.map {
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

        // Restore milestones on reinstall (insert-IGNORE keeps local awards intact)
        try {
            api.getEarned().body()?.earned?.let { restored ->
                badgeDao.insertEarned(restored.map {
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
            val unsynced = badgeDao.getUnsyncedEarned()
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
