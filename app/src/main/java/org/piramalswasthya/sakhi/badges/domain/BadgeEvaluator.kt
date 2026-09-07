package org.piramalswasthya.sakhi.badges.domain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.R
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
    private val pref: PreferenceDao,
    private val celebrations: BadgeCelebrations,
    @ApplicationContext private val context: Context
) {

    private val mutex = Mutex()

    @Volatile
    private var lastRunAt = 0L

    suspend fun evaluateAll() = withContext(Dispatchers.IO) {
        mutex.withLock {
            // collapse the burst of triggers at app launch (bus + workers)
            // into one pass — full table scans are costly on low-end devices
            if (System.currentTimeMillis() - lastRunAt < RUN_THROTTLE_MS) return@withLock
            lastRunAt = System.currentTimeMillis()
            val userId = try {
                pref.getLoggedInUser()?.userId
            } catch (e: Exception) {
                null
            }
            if (userId == null) {
                Timber.w("Badges: evaluation skipped — no logged-in user")
                return@withLock
            }

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
            val priorEarned = try {
                badgeDao.getEarned(userId)
            } catch (e: Exception) {
                emptyList()
            }

            for (def in BadgeDefinitions.ALL) {
                if (!BadgeDefinitions.isEnabled(def, config)) continue
                try {
                    evaluate(
                        def, config, freezes, userId, now, states, earned,
                        hasHistory = priorEarned.any { it.badgeId == def.id }
                    )
                } catch (e: Exception) {
                    // One badge family failing must not stop the others (LLD §5.2)
                    Timber.w(e, "Badges: evaluation failed for ${def.id}")
                }
            }

            // never revoked: a state can't show a lower tier than one permanently
            // earned (reinstall backfill, lowered milestones, demo history)
            val maxEarned = priorEarned.groupBy { it.badgeId }.mapValues { e -> e.value.maxOf { it.level } }
            badgeDao.upsertStates(states.map { s ->
                val kind = BadgeDefinitions.byId(s.badgeId)?.kind
                val floor = maxEarned[s.badgeId] ?: 0
                val tiered = kind == BadgeKind.STREAK_WEEKLY || kind == BadgeKind.STREAK_MONTHLY ||
                        kind == BadgeKind.CUMULATIVE
                if (tiered && s.currentLevel < floor) s.copy(currentLevel = floor) else s
            })
            if (earned.isNotEmpty()) {
                // skip celebration on the first-ever evaluation (historical backfill)
                val hadEarnedBefore = priorEarned.isNotEmpty()
                val insertedIds = badgeDao.insertEarned(earned)
                if (hadEarnedBefore) {
                    val newRows = insertedIds.zip(earned)
                        .filter { it.first != -1L }
                        .filter { BadgeDefinitions.byId(it.second.badgeId)?.celebrate == true }
                    // system notification for the first new award (works backgrounded)
                    newRows.firstOrNull()?.let { celebrate(it.second) }
                    // in-app game-style overlay for each new award
                    newRows.forEach { celebrations.publish(it.second.badgeId, it.second.level) }
                }
            }
            Timber.d("Badges: evaluated ${states.size} badges, ${earned.size} candidate awards")
        }
    }

    /** Daytime celebration when a new milestone unlocks (checkAndUnlock). */
    private fun celebrate(newRow: BadgeEarnedCache) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) return
            val def = BadgeDefinitions.byId(newRow.badgeId) ?: return
            val name = pref.getLoggedInUser()?.name ?: ""
            val body = context.getString(
                R.string.badge_unlock_notification, name, context.getString(def.titleRes)
            )
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CELEBRATION_CHANNEL_ID,
                        context.getString(R.string.badge_shelf_title),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
            val intent = context.packageManager
                .getLaunchIntentForPackage(context.packageName)
                ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP }
            val pending = intent?.let {
                PendingIntent.getActivity(
                    context, CELEBRATION_ID, it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            manager.notify(
                CELEBRATION_ID,
                NotificationCompat.Builder(context, CELEBRATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(context.getString(R.string.badge_shelf_title))
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setContentIntent(pending)
                    .setAutoCancel(true)
                    .build()
            )
        } catch (e: Exception) {
            Timber.w(e, "Badges: celebration notification failed")
        }
    }

    companion object {
        private const val CELEBRATION_CHANNEL_ID = "badge_celebration"
        private const val CELEBRATION_ID = 20003
        private const val RUN_THROTTLE_MS = 10_000L

        // ponytail: 2-year quarterly backfill; raise if older history matters
        private const val PAST_QUARTERS_LOOKBACK = 8
    }

    private suspend fun evaluate(
        def: BadgeDefinition,
        config: Map<String, String>,
        freezes: List<org.piramalswasthya.sakhi.model.BadgeStreakFreezeCache>,
        userId: Int,
        now: Long,
        states: MutableList<BadgeStateCache>,
        earned: MutableList<BadgeEarnedCache>,
        hasHistory: Boolean
    ) {
        val milestones = BadgeDefinitions.effectiveMilestones(def, config)

        when (def.kind) {
            BadgeKind.STREAK_WEEKLY, BadgeKind.STREAK_MONTHLY -> {
                val weekly = def.kind == BadgeKind.STREAK_WEEKLY
                // sync log ∪ activity weeks: activity is recomputable from
                // re-synced records, so streak history survives a reinstall
                val completed =
                    if (weekly) badgeDao.getAllSyncWeeks().toSet() + facts.activityWeeks()
                    else facts.onTimeIncentiveMonths()
                val periodKeyAt = { off: Int ->
                    if (weekly) BadgeDates.weekKeyAt(off, now) else BadgeDates.monthKeyAt(off, now)
                }
                val streak = streakEngine.compute(
                    completedPeriods = completed,
                    graceTokens = BadgeDefinitions.effectiveGrace(def, config),
                    freezes = freezes,
                    badgeId = def.id,
                    periodKeyAt = periodKeyAt,
                    periodIntervalAt = { off ->
                        if (weekly) BadgeDates.weekIntervalAt(off, now)
                        else BadgeDates.monthIntervalAt(off, now)
                    }
                )
                // once earned, never revoked: award on the best run ever, so a
                // broken streak or a reinstall keeps previously earned tiers
                val bestRun = maxOf(streak.length, streakEngine.longestRun(completed, periodKeyAt))
                val level = milestones.count { bestRun >= it }
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
                // reinstall restore: while this badge has no earned history,
                // recompute past quarters from re-synced record dates
                // (calendar quarters approximate Complete Worker's rolling 90d)
                if (!hasHistory) {
                    for (q in 1..PAST_QUARTERS_LOOKBACK) {
                        val window = BadgeDates.quarterIntervalAt(-q, now)
                        val past = when (def.id) {
                            BadgeIds.COMPLETE_WORKER ->
                                facts.activeDomainsSince(window.first, window.last + 1)

                            else -> facts.meetingTypesSince(window.first, window.last + 1)
                        }
                        if (past >= threshold) {
                            earned += BadgeEarnedCache(
                                userId = userId, badgeId = def.id, level = 1,
                                caseRef = BadgeDates.quarterKey(window.first), earnedAt = now
                            )
                        }
                    }
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
