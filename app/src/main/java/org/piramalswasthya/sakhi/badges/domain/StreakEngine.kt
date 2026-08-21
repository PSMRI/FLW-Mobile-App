package org.piramalswasthya.sakhi.badges.domain

import org.piramalswasthya.sakhi.model.BadgeStreakFreezeCache
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Streak fairness (LLD §3.3). A missed period, checked in order:
 *  1. Freeze window  → period excluded, streak unchanged, no token consumed.
 *  2. Grace token    → one token consumed, streak continues.
 *  3. Streak break   → active streak resets (earned milestones stay in BADGE_EARNED).
 */
@Singleton
class StreakEngine @Inject constructor() {

    data class Streak(val length: Long, val graceRemaining: Int)

    /**
     * Walks periods backwards from the current one. The current, still-running
     * period counts if completed but never breaks the streak or consumes grace.
     *
     * @param periodKeyAt      offset (0 = current, -1 = previous …) → period key
     * @param periodIntervalAt offset → [start, end] millis of that period
     */
    fun compute(
        completedPeriods: Set<String>,
        graceTokens: Int,
        freezes: List<BadgeStreakFreezeCache>,
        badgeId: String,
        periodKeyAt: (Int) -> String,
        periodIntervalAt: (Int) -> LongRange,
        maxLookback: Int = MAX_LOOKBACK
    ): Streak {
        if (completedPeriods.isEmpty()) return Streak(0, graceTokens)

        val applicableFreezes = freezes.filter { it.badgeId.isEmpty() || it.badgeId == badgeId }
        val remaining = completedPeriods.toMutableSet()
        var length = 0L
        var grace = graceTokens

        if (remaining.remove(periodKeyAt(0))) length++

        for (offset in -1 downTo -maxLookback) {
            // nothing older left to bridge to — don't burn grace on the tail
            if (remaining.isEmpty()) break

            when {
                remaining.remove(periodKeyAt(offset)) -> length++

                inFreezeWindow(periodIntervalAt(offset), applicableFreezes) -> Unit

                grace > 0 -> grace--

                else -> return Streak(length, grace)
            }
        }
        return Streak(length, grace)
    }

    private fun inFreezeWindow(period: LongRange, freezes: List<BadgeStreakFreezeCache>): Boolean =
        freezes.any { it.startDate <= period.last && it.endDate >= period.first }

    companion object {
        // ponytail: 10-year lookback bound; raise if multi-decade streaks become real
        private const val MAX_LOOKBACK = 520
    }
}
