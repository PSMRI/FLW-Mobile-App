package org.piramalswasthya.sakhi.badges.domain

import org.piramalswasthya.sakhi.R

/**
 * Badge catalog (LLD §1.1 / §3.1). Rules are data, not conditionals:
 * adding or tuning a badge is a change here (or a server config override),
 * not an evaluator rewrite.
 */
enum class BadgeKind {
    /** Consecutive ISO weeks (streak protection applies). */
    STREAK_WEEKLY,

    /** Consecutive calendar months (streak protection applies). */
    STREAK_MONTHLY,

    /** Threshold met within the current quarter / rolling window; re-earned every quarter. */
    QUARTERLY,

    /** Lifetime distinct-count milestones. */
    CUMULATIVE,

    /** One recognition per beneficiary case. */
    PER_CASE
}

data class BadgeDefinition(
    val id: String,
    val kind: BadgeKind,
    /** Milestone thresholds; for QUARTERLY/PER_CASE a single threshold. */
    val milestones: List<Long>,
    val defaultGraceTokens: Int = 0,
    val enabledByDefault: Boolean = true,
    val titleRes: Int,
    val descRes: Int,
    /** Fallback / locked icon (tier 1 artwork or placeholder). */
    val iconRes: Int,
    /**
     * Per-level artwork, index 0..3: milestone tiers for streak/cumulative
     * badges, Q1..Q4 for quarterly badges. Empty → iconRes for every level.
     */
    val tierIcons: List<Int> = emptyList()
)

object BadgeIds {
    const val STEADY_SYNCER = "steady_syncer"
    const val TIMELY_REPORTER = "timely_reporter"
    const val COMPLETE_WORKER = "complete_worker"
    const val COMMUNITY_VOICE = "community_voice"
    const val MATERNAL_JOURNEY = "maternal_journey"
    const val CHILD_FULLY_PROTECTED = "child_fully_protected"
    const val DIGITAL_IDENTITY = "digital_identity"
    const val VULNERABLE_BABY = "vulnerable_baby"
    const val CRITICAL_REFERRAL = "critical_referral"
}

/** BADGE_CONFIG keys. Server values override the compiled defaults below. */
object BadgeConfigKeys {
    const val FEATURE_ENABLED = "feature_enabled"
    const val COPY_VERSION = "copy_version"
    fun milestones(badgeId: String) = "milestones.$badgeId"
    fun enabled(badgeId: String) = "enabled.$badgeId"
    fun grace(badgeId: String) = "grace.$badgeId"
}

object BadgeDefinitions {

    val ALL: List<BadgeDefinition> = listOf(
        BadgeDefinition(
            id = BadgeIds.STEADY_SYNCER,
            kind = BadgeKind.STREAK_WEEKLY,
            milestones = listOf(4, 8, 16, 26),
            defaultGraceTokens = 1, // 1-week grace period (LLD §1.1)
            titleRes = R.string.badge_steady_syncer_title,
            descRes = R.string.badge_steady_syncer_desc,
            iconRes = R.drawable.badge_steady_syncer_t1,
            tierIcons = listOf(
                R.drawable.badge_steady_syncer_t1,
                R.drawable.badge_steady_syncer_t2,
                R.drawable.badge_steady_syncer_t3,
                R.drawable.badge_steady_syncer_t4
            )
        ),
        BadgeDefinition(
            id = BadgeIds.TIMELY_REPORTER,
            kind = BadgeKind.STREAK_MONTHLY,
            milestones = listOf(2, 4, 6, 12),
            titleRes = R.string.badge_timely_reporter_title,
            descRes = R.string.badge_timely_reporter_desc,
            iconRes = R.drawable.badge_timely_reporter_t1,
            tierIcons = listOf(
                R.drawable.badge_timely_reporter_t1,
                R.drawable.badge_timely_reporter_t2,
                R.drawable.badge_timely_reporter_t3,
                R.drawable.badge_timely_reporter_t4
            )
        ),
        BadgeDefinition(
            id = BadgeIds.COMPLETE_WORKER,
            kind = BadgeKind.QUARTERLY,
            milestones = listOf(4), // active in 4+ health domains, rolling 90 days
            titleRes = R.string.badge_complete_worker_title,
            descRes = R.string.badge_complete_worker_desc,
            iconRes = R.drawable.badge_complete_worker_t1,
            tierIcons = listOf(
                R.drawable.badge_complete_worker_t1,
                R.drawable.badge_complete_worker_t2,
                R.drawable.badge_complete_worker_t3,
                R.drawable.badge_complete_worker_t4
            )
        ),
        BadgeDefinition(
            id = BadgeIds.COMMUNITY_VOICE,
            kind = BadgeKind.QUARTERLY,
            milestones = listOf(4), // 4 meeting types (MAA, NDD, AHD, U-WIN)
            titleRes = R.string.badge_community_voice_title,
            descRes = R.string.badge_community_voice_desc,
            iconRes = R.drawable.badge_community_voice_t1,
            tierIcons = listOf(
                R.drawable.badge_community_voice_t1,
                R.drawable.badge_community_voice_t2,
                R.drawable.badge_community_voice_t3,
                R.drawable.badge_community_voice_t4
            )
        ),
        BadgeDefinition(
            id = BadgeIds.MATERNAL_JOURNEY,
            kind = BadgeKind.CUMULATIVE,
            milestones = listOf(5, 15, 30, 50),
            titleRes = R.string.badge_maternal_journey_title,
            descRes = R.string.badge_maternal_journey_desc,
            iconRes = R.drawable.badge_maternal_journey_t1,
            tierIcons = listOf(
                R.drawable.badge_maternal_journey_t1,
                R.drawable.badge_maternal_journey_t2,
                R.drawable.badge_maternal_journey_t3,
                R.drawable.badge_maternal_journey_t4
            )
        ),
        BadgeDefinition(
            id = BadgeIds.CHILD_FULLY_PROTECTED,
            kind = BadgeKind.CUMULATIVE,
            milestones = listOf(10, 25, 60, 100),
            titleRes = R.string.badge_child_fully_protected_title,
            descRes = R.string.badge_child_fully_protected_desc,
            iconRes = R.drawable.badge_child_fully_protected_t1,
            tierIcons = listOf(
                R.drawable.badge_child_fully_protected_t1,
                R.drawable.badge_child_fully_protected_t2,
                R.drawable.badge_child_fully_protected_t3,
                R.drawable.badge_child_fully_protected_t4
            )
        ),
        BadgeDefinition(
            id = BadgeIds.DIGITAL_IDENTITY,
            kind = BadgeKind.CUMULATIVE,
            milestones = listOf(25, 75, 150, 300),
            titleRes = R.string.badge_digital_identity_title,
            descRes = R.string.badge_digital_identity_desc,
            iconRes = R.drawable.badge_digital_identity_t1,
            tierIcons = listOf(
                R.drawable.badge_digital_identity_t1,
                R.drawable.badge_digital_identity_t2,
                R.drawable.badge_digital_identity_t3,
                R.drawable.badge_digital_identity_t4
            )
        ),
        BadgeDefinition(
            id = BadgeIds.VULNERABLE_BABY,
            kind = BadgeKind.PER_CASE,
            milestones = listOf(1),
            titleRes = R.string.badge_vulnerable_baby_title,
            descRes = R.string.badge_vulnerable_baby_desc,
            iconRes = R.drawable.badge_vulnerable_baby
        ),
        BadgeDefinition(
            id = BadgeIds.CRITICAL_REFERRAL,
            kind = BadgeKind.PER_CASE,
            milestones = listOf(1),
            // LLD §1.3: temporarily deferred until the referral signal gains
            // data-model support. Rule is implemented; enable via server config.
            enabledByDefault = false,
            titleRes = R.string.badge_critical_referral_title,
            descRes = R.string.badge_critical_referral_desc,
            iconRes = R.drawable.badge_critical_referral
        )
    )

    fun byId(id: String): BadgeDefinition? = ALL.firstOrNull { it.id == id }

    /** Effective milestones for a badge: server override or compiled default. */
    fun effectiveMilestones(def: BadgeDefinition, config: Map<String, String>): List<Long> =
        config[BadgeConfigKeys.milestones(def.id)]
            ?.split(",")
            ?.mapNotNull { it.trim().toLongOrNull() }
            ?.takeIf { it.isNotEmpty() }
            ?: def.milestones

    fun effectiveGrace(def: BadgeDefinition, config: Map<String, String>): Int =
        config[BadgeConfigKeys.grace(def.id)]?.toIntOrNull() ?: def.defaultGraceTokens

    fun isEnabled(def: BadgeDefinition, config: Map<String, String>): Boolean =
        config[BadgeConfigKeys.enabled(def.id)]?.toBooleanStrictOrNull() ?: def.enabledByDefault

    fun isFeatureEnabled(config: Map<String, String>): Boolean =
        config[BadgeConfigKeys.FEATURE_ENABLED]?.toBooleanStrictOrNull() ?: true

    /**
     * Artwork for the badge's current state: (icon res, earned-look).
     * Streak/cumulative badges show the highest earned tier (tier 1 dimmed
     * while locked); quarterly badges show the current calendar quarter's
     * artwork, dimmed until earned this quarter.
     */
    fun displayIcon(
        def: BadgeDefinition,
        state: org.piramalswasthya.sakhi.model.BadgeStateCache?
    ): Pair<Int, Boolean> {
        val level = state?.currentLevel ?: 0
        return when (def.kind) {
            BadgeKind.QUARTERLY -> {
                val quarter = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) / 3
                (def.tierIcons.getOrNull(quarter) ?: def.iconRes) to (level > 0)
            }

            BadgeKind.PER_CASE -> def.iconRes to ((state?.progress ?: 0L) > 0L)

            else -> (def.tierIcons.getOrNull((level - 1).coerceAtLeast(0))
                ?: def.iconRes) to (level > 0)
        }
    }
}
