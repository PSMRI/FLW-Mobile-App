package org.piramalswasthya.sakhi.badges

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.badges.domain.BadgeDates
import org.piramalswasthya.sakhi.badges.domain.BadgeDefinitions
import org.piramalswasthya.sakhi.badges.domain.BadgeFactsReader
import org.piramalswasthya.sakhi.badges.domain.BadgeIds
import org.piramalswasthya.sakhi.badges.domain.StreakEngine
import org.piramalswasthya.sakhi.badges.domain.TaskCompletionBus
import org.piramalswasthya.sakhi.database.room.dao.BadgeDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BadgeEarnedCache
import org.piramalswasthya.sakhi.model.BadgeSyncLogCache
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Demo staging for presentations — DEBUG builds only (long-press "My Impact").
 * Stages Steady Syncer exactly one sync short of its next tier and seeds a
 * first-tier history for the other badges so the shelf looks lived-in.
 * Touches only BADGE_* tables; never fabricates health records.
 */
@Singleton
class BadgeDemoSeeder @Inject constructor(
    private val badgeDao: BadgeDao,
    private val facts: BadgeFactsReader,
    private val streakEngine: StreakEngine,
    private val pref: PreferenceDao,
    private val bus: TaskCompletionBus
) {

    suspend fun stage(): String = withContext(Dispatchers.IO) {
        val userId = pref.getLoggedInUser()?.userId ?: return@withContext "Not logged in"
        val now = System.currentTimeMillis()
        val def = BadgeDefinitions.byId(BadgeIds.STEADY_SYNCER)!!
        val config = badgeDao.getConfig().associate { it.key to it.value }
        val milestones = BadgeDefinitions.effectiveMilestones(def, config)
        val thisWeek = BadgeDates.weekKey(now)
        val keyAt = { off: Int -> BadgeDates.weekKeyAt(off, now) }

        // best tier reachable from history, ignoring the current (not yet synced) week
        val completed = (badgeDao.getAllSyncWeeks().toSet() + facts.activityWeeks()) - thisWeek
        val best = maxOf(
            streakEngine.compute(
                completed, 0, emptyList(), def.id, keyAt,
                periodIntervalAt = { BadgeDates.weekIntervalAt(it, now) }
            ).length,
            streakEngine.longestRun(completed, keyAt)
        )
        val target = milestones.firstOrNull { it > best }
            ?: return@withContext "Steady Syncer is already at its top tier"

        // fill weeks -1..-(target-1): one more sync this week reaches the target
        for (off in 1 until target.toInt()) {
            val at = BadgeDates.weekIntervalAt(-off, now).first + TimeUnit.DAYS.toMillis(2)
            badgeDao.insertSyncLog(BadgeSyncLogCache(keyAt(-off), at))
        }
        badgeDao.deleteSyncWeek(thisWeek)

        // lived-in shelf: first tier of each cumulative/streak badge, this quarter's re-earnables
        val quarter = BadgeDates.quarterKey(now)
        badgeDao.insertEarned(
            listOf(
                BadgeIds.TIMELY_REPORTER, BadgeIds.MATERNAL_JOURNEY,
                BadgeIds.CHILD_FULLY_PROTECTED, BadgeIds.DIGITAL_IDENTITY
            ).map { BadgeEarnedCache(userId = userId, badgeId = it, level = 1, earnedAt = now) } +
                    listOf(BadgeIds.COMPLETE_WORKER, BadgeIds.COMMUNITY_VOICE).map {
                        BadgeEarnedCache(
                            userId = userId, badgeId = it, level = 1,
                            caseRef = quarter, earnedAt = now
                        )
                    }
        )
        bus.publish()
        "Steady Syncer at ${target - 1} weeks — one sync unlocks tier ${milestones.indexOf(target) + 1}"
    }
}
