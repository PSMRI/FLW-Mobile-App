package org.piramalswasthya.sakhi.badges.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.dao.BadgeDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BadgeEarnedCache
import org.piramalswasthya.sakhi.model.BadgeStateCache
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core engine (LLD §3, §5.1). Recompute, never increment: every run
 * recalculates badge progress from current local facts and overwrites only
 * the mutable BADGE_STATE. Awarded milestones go to the append-only
 * BADGE_EARNED log ("once earned, never revoked") whose unique constraint
 * makes double-runs and reinstalls idempotent.
 */
@Singleton
class BadgeEvaluator @Inject constructor(
    private val badgeDao: BadgeDao,
    private val facts: BadgeFactsReader,
    private val streakEngine: StreakEngine,
    private val pref: PreferenceDao
) {

    private val mutex = Mutex()

    suspend fun evaluateAll() = withContext(Dispatchers.IO) {
        mutex.withLock {
            val userId = try {
                pref.getLoggedInUser()?.userId
            } catch (e: Exception) {
                null
            } ?: return@withLock

            val config = try {
                badgeDao.getConfig().associate { it.key to it.value }
            } catch (e: Exception) {
                emptyMap()
            }
            // Remote kill-switch (LLD §5.2): evaluation skipped without crashes.
            if (!BadgeDefinitions.isFeatureEnabled(config)) return@withLock

            val freezes = try {
                badgeDao.getFreezes()
            } catch (e: Exception) {
                emptyList()
            }
            val now = System.currentTimeMillis()
            val states = mutableListOf<BadgeStateCache>()
            val earned = mutableListOf<BadgeEarnedCache>()

            for (def in BadgeDefinitions.ALL) {
                if (!BadgeDefinitions.isEnabled(def, config)) continue
                try {
                    evaluate(def, config, freezes, userId, now, states, earned)
                } catch (e: Exception) {
                    // One badge family failing must not stop the others (LLD §5.2)
                    Timber.w(e, "Badges: evaluation failed for ${def.id}")
                }
            }

            badgeDao.upsertStates(states)
            if (earned.isNotEmpty()) badgeDao.insertEarned(earned)
            Timber.d("Badges: evaluated ${states.size} badges, ${earned.size} candidate awards")
        }
    }

    private suspend fun evaluate(
        def: BadgeDefinition,
        config: Map<String, String>,
        freezes: List<org.piramalswasthya.sakhi.model.BadgeStreakFreezeCache>,
        userId: Int,
        now: Long,
        states: MutableList<BadgeStateCache>,
        earned: MutableList<BadgeEarnedCache>
    ) {
        val milestones = BadgeDefinitions.effectiveMilestones(def, config)

        when (def.kind) {
            BadgeKind.STREAK_WEEKLY, BadgeKind.STREAK_MONTHLY -> {
                val weekly = def.kind == BadgeKind.STREAK_WEEKLY
                val completed =
                    if (weekly) badgeDao.getAllSyncWeeks().toSet()
                    else facts.onTimeIncentiveMonths()
                val streak = streakEngine.compute(
                    completedPeriods = completed,
                    graceTokens = BadgeDefinitions.effectiveGrace(def, config),
                    freezes = freezes,
                    badgeId = def.id,
                    periodKeyAt = { off ->
                        if (weekly) BadgeDates.weekKeyAt(off, now) else BadgeDates.monthKeyAt(off, now)
                    },
                    periodIntervalAt = { off ->
                        if (weekly) BadgeDates.weekIntervalAt(off, now)
                        else BadgeDates.monthIntervalAt(off, now)
                    }
                )
                val level = milestones.count { streak.length >= it }
                states += BadgeStateCache(
                    badgeId = def.id,
                    currentLevel = level,
                    progress = streak.length,
                    nextTarget = milestones.firstOrNull { streak.length < it } ?: milestones.last(),
                    streakCount = streak.length,
                    graceRemaining = streak.graceRemaining,
                    lastEvaluatedAt = now
                )
                for (lvl in 1..level) {
                    earned += BadgeEarnedCache(
                        userId = userId, badgeId = def.id, level = lvl, earnedAt = now
                    )
                }
            }

            BadgeKind.QUARTERLY -> {
                val since = when (def.id) {
                    // rolling 90-day window (LLD §3.1)
                    BadgeIds.COMPLETE_WORKER -> now - TimeUnit.DAYS.toMillis(90)
                    else -> BadgeDates.quarterStart(now)
                }
                val measure = when (def.id) {
                    BadgeIds.COMPLETE_WORKER -> facts.activeDomainsSince(since)
                    else -> facts.meetingTypesSince(since)
                }
                val threshold = milestones.first()
                val met = measure >= threshold
                states += BadgeStateCache(
                    badgeId = def.id,
                    currentLevel = if (met) 1 else 0,
                    progress = measure,
                    nextTarget = threshold,
                    lastEvaluatedAt = now
                )
                if (met) {
                    // re-earned every quarter: caseRef keys the quarter
                    earned += BadgeEarnedCache(
                        userId = userId, badgeId = def.id, level = 1,
                        caseRef = BadgeDates.quarterKey(now), earnedAt = now
                    )
                }
            }

            BadgeKind.CUMULATIVE -> {
                val count = when (def.id) {
                    BadgeIds.MATERNAL_JOURNEY -> facts.completedMaternalJourneys().size
                    BadgeIds.CHILD_FULLY_PROTECTED -> facts.fullyImmunizedChildren().size
                    BadgeIds.DIGITAL_IDENTITY -> facts.abhaGeneratedBens().size
                    else -> 0
                }.toLong()
                val level = milestones.count { count >= it }
                states += BadgeStateCache(
                    badgeId = def.id,
                    currentLevel = level,
                    progress = count,
                    nextTarget = milestones.firstOrNull { count < it } ?: milestones.last(),
                    lastEvaluatedAt = now
                )
                for (lvl in 1..level) {
                    earned += BadgeEarnedCache(
                        userId = userId, badgeId = def.id, level = lvl, earnedAt = now
                    )
                }
            }

            BadgeKind.PER_CASE -> {
                val cases = when (def.id) {
                    BadgeIds.VULNERABLE_BABY -> facts.vulnerableBabiesCaredFor()
                    BadgeIds.CRITICAL_REFERRAL -> facts.nrcReferredChildren()
                    else -> emptyList()
                }
                states += BadgeStateCache(
                    badgeId = def.id,
                    currentLevel = if (cases.isEmpty()) 0 else 1,
                    progress = cases.size.toLong(),
                    nextTarget = milestones.first(),
                    lastEvaluatedAt = now
                )
                for (case in cases) {
                    // one recognition per beneficiary; caseRef never leaves the device
                    earned += BadgeEarnedCache(
                        userId = userId, badgeId = def.id, level = 1,
                        caseRef = case, earnedAt = now
                    )
                }
            }
        }
    }
}
