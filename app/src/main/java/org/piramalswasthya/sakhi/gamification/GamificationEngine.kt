package org.piramalswasthya.sakhi.gamification

import org.piramalswasthya.sakhi.model.GamificationBadge
import org.piramalswasthya.sakhi.model.GamificationProfile
import org.piramalswasthya.sakhi.model.PointsTransaction
import org.piramalswasthya.sakhi.repositories.GamificationRepo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core gamification engine.
 *
 * Pure business logic — no Android framework dependencies except the
 * injected repository. Fully unit-testable with a fake repository.
 *
 * ── Design decisions ──────────────────────────────────────────────────────
 *
 * WHY streaks over leaderboards:
 *   Leaderboards create pressure to record duplicate or false health data —
 *   a documented problem in community health worker digitisation programs.
 *   Streaks are personal and internally motivating; they don't require
 *   comparing against peers and work fully offline.
 *
 * WHY points are health-outcome weighted (not volume-weighted):
 *   HRP identification (35 pts) > Delivery outcome (30 pts) > ANC (20 pts)
 *   > Registration (10–15 pts) > Login (5 pts).
 *   This aligns the reward signal with actual health impact, not just
 *   form-filling speed.
 *
 * WHY java.util.Calendar instead of java.time.LocalDate:
 *   Min SDK is 25. java.time requires API 26+. Calendar works on all
 *   supported devices without desugaring configuration changes.
 * ──────────────────────────────────────────────────────────────────────────
 */
@Singleton
class GamificationEngine @Inject constructor(
    private val repo: GamificationRepo
) {

    companion object {
        private val DATE_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        /** Points table. Weights reflect health-outcome importance. */
        private val POINTS_MAP = mapOf(
            "HOUSEHOLD_REGISTERED"      to 10,
            "BENEFICIARY_REGISTERED"    to 15,
            "ANC_VISIT_COMPLETED"       to 20,
            "DELIVERY_OUTCOME_RECORDED" to 30,
            "PNC_VISIT_COMPLETED"       to 20,
            "IMMUNIZATION_RECORDED"     to 25,
            "HRP_CASE_IDENTIFIED"       to 35,  // highest — saves maternal lives
            "NCD_SCREENING_COMPLETED"   to 20,
            "CBAC_FORM_FILLED"          to 15,
            "DAILY_LOGIN"               to 5
        )

        /**
         * XP thresholds for each level (index = level number).
         * Level 1 starts at 0 XP.
         */
        private val LEVEL_THRESHOLDS = listOf(
            0,    // Level 1
            100,  // Level 2
            300,  // Level 3
            600,  // Level 4
            1000, // Level 5
            1500, // Level 6
            2200, // Level 7
            3000  // Level 8
        )

        private fun today(): String = DATE_FMT.format(Date())

        /**
         * Returns the difference in calendar days between two ISO date strings.
         * Uses Calendar to stay compatible with min SDK 25.
         */
        fun daysBetween(from: String, to: String): Long {
            val fromCal = Calendar.getInstance().apply { time = DATE_FMT.parse(from)!! }
            val toCal   = Calendar.getInstance().apply { time = DATE_FMT.parse(to)!! }
            val fromMs  = fromCal.timeInMillis
            val toMs    = toCal.timeInMillis
            return (toMs - fromMs) / (1000 * 60 * 60 * 24)
        }
    }

    /**
     * Main entry point. Call after any successful health-worker action save.
     *
     * @param userId  The ASHA worker's userId (matches User.userId: Int)
     * @param event   The gamification event that occurred
     */
    suspend fun process(userId: Int, event: GamificationEvent) {
        val points = POINTS_MAP[event.eventType] ?: return
        val today  = today()

        // 1. Load or create profile
        val existing = repo.getProfile(userId)
            ?: GamificationProfile(userId = userId, createdAt = System.currentTimeMillis())

        // 2. Deduplicate daily login — only award once per calendar day
        if (event is GamificationEvent.DailyLogin &&
            existing.lastActivityDate == today) return

        // 3. Update streak
        val streakUpdated = updateStreak(existing, today)

        // 4. Award points + compute new level
        val newTotal = streakUpdated.totalPoints + points
        val newLevel = computeLevel(newTotal)
        val leveledUp = newLevel > streakUpdated.level

        val finalProfile = streakUpdated.copy(
            totalPoints      = newTotal,
            level            = newLevel,
            lastActivityDate = today,
            updatedAt        = System.currentTimeMillis(),
            syncState        = 0   // mark dirty for sync
        )

        // 5. Persist — profile first, then transaction
        repo.upsertProfile(finalProfile)
        repo.insertTransaction(
            PointsTransaction(
                userId    = userId,
                points    = points,
                eventType = event.eventType,
                eventRefId = event.refId,
                reason    = friendlyReason(event.eventType),
                syncState = 0
            )
        )

        // 6. Evaluate badges
        evaluateBadges(userId, event, finalProfile, leveledUp)
    }

    // ── Streak logic ──────────────────────────────────────────────────────────

    /**
     * Updates streak counters based on [today] relative to [profile.lastActivityDate].
     *
     * - Same day  → no change (idempotent)
     * - Next day  → streak + 1
     * - Any gap   → streak resets to 1
     */
    fun updateStreak(profile: GamificationProfile, today: String): GamificationProfile {
        val last = profile.lastActivityDate
            ?: return profile.copy(currentStreakDays = 1, longestStreakDays = 1)

        return when (daysBetween(last, today)) {
            0L -> profile                        // same day, no streak change
            1L -> {                              // consecutive day
                val newStreak = profile.currentStreakDays + 1
                profile.copy(
                    currentStreakDays = newStreak,
                    longestStreakDays = maxOf(profile.longestStreakDays, newStreak)
                )
            }
            else -> profile.copy(currentStreakDays = 1)   // streak broken
        }
    }

    // ── Level computation ─────────────────────────────────────────────────────

    fun computeLevel(totalPoints: Int): Int =
        LEVEL_THRESHOLDS.indexOfLast { totalPoints >= it }.coerceAtLeast(0) + 1

    // ── Badge evaluation ──────────────────────────────────────────────────────

    private suspend fun evaluateBadges(
        userId: Int,
        event: GamificationEvent,
        profile: GamificationProfile,
        leveledUp: Boolean
    ) {
        // First-time activity badges
        if (event is GamificationEvent.HouseholdRegistered)
            awardBadgeIfNew(userId, BadgeCatalog.FIRST_HOUSEHOLD)

        if (event is GamificationEvent.BeneficiaryRegistered)
            awardBadgeIfNew(userId, BadgeCatalog.FIRST_BENEFICIARY)

        if (event is GamificationEvent.AncVisitCompleted)
            awardBadgeIfNew(userId, BadgeCatalog.ANC_HERO)

        if (event is GamificationEvent.ImmunizationRecorded)
            awardBadgeIfNew(userId, BadgeCatalog.IMMUNIZATION_GUARDIAN)

        if (event is GamificationEvent.HrpCaseIdentified)
            awardBadgeIfNew(userId, BadgeCatalog.HRP_IDENTIFIER)

        if (event is GamificationEvent.NcdScreeningCompleted)
            awardBadgeIfNew(userId, BadgeCatalog.NCD_CHAMPION)

        // Streak milestone badges
        when (profile.currentStreakDays) {
            3    -> awardBadgeIfNew(userId, BadgeCatalog.STREAK_3)
            7    -> awardBadgeIfNew(userId, BadgeCatalog.STREAK_7)
            30   -> awardBadgeIfNew(userId, BadgeCatalog.STREAK_30)
        }

        // Level-up badges
        if (leveledUp) {
            when (profile.level) {
                2 -> awardBadgeIfNew(userId, BadgeCatalog.LEVEL_2)
                5 -> awardBadgeIfNew(userId, BadgeCatalog.LEVEL_5)
            }
        }
    }

    private suspend fun awardBadgeIfNew(userId: Int, def: BadgeCatalog.BadgeDef) {
        if (!repo.hasBadge(userId, def.type)) {
            repo.insertBadge(
                GamificationBadge(
                    userId      = userId,
                    badgeType   = def.type,
                    badgeNameEn = def.nameEn,
                    badgeNameHi = def.nameHi,
                    badgeNameAs = def.nameAs,
                    syncState   = 0
                )
            )
        }
    }

    private fun friendlyReason(eventType: String) = when (eventType) {
        "HOUSEHOLD_REGISTERED"      -> "Household registration"
        "BENEFICIARY_REGISTERED"    -> "Beneficiary registration"
        "ANC_VISIT_COMPLETED"       -> "ANC visit completed"
        "DELIVERY_OUTCOME_RECORDED" -> "Delivery outcome recorded"
        "PNC_VISIT_COMPLETED"       -> "PNC visit completed"
        "IMMUNIZATION_RECORDED"     -> "Immunization recorded"
        "HRP_CASE_IDENTIFIED"       -> "High-risk pregnancy identified"
        "NCD_SCREENING_COMPLETED"   -> "NCD screening completed"
        "CBAC_FORM_FILLED"          -> "CBAC form filled"
        "DAILY_LOGIN"               -> "Daily login bonus"
        else                        -> eventType
    }
}
