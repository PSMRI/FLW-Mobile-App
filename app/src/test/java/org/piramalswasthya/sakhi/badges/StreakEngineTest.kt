package org.piramalswasthya.sakhi.badges

import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.badges.domain.BadgeDates
import org.piramalswasthya.sakhi.badges.domain.StreakEngine
import org.piramalswasthya.sakhi.model.BadgeStreakFreezeCache

class StreakEngineTest {

    private val engine = StreakEngine()
    private val now = 1_766_000_000_000L // fixed instant

    private fun compute(
        completed: Set<String>,
        grace: Int = 0,
        freezes: List<BadgeStreakFreezeCache> = emptyList()
    ) = engine.compute(
        completedPeriods = completed,
        graceTokens = grace,
        freezes = freezes,
        badgeId = "steady_syncer",
        periodKeyAt = { off -> BadgeDates.weekKeyAt(off, now) },
        periodIntervalAt = { off -> BadgeDates.weekIntervalAt(off, now) }
    )

    private fun weeks(vararg offsets: Int) =
        offsets.map { BadgeDates.weekKeyAt(it, now) }.toSet()

    @Test
    fun `unbroken run counts every week`() {
        assertEquals(4L, compute(weeks(0, -1, -2, -3)).length)
    }

    @Test
    fun `no history means zero`() {
        assertEquals(0L, compute(emptySet()).length)
    }

    @Test
    fun `current incomplete week does not break streak`() {
        assertEquals(3L, compute(weeks(-1, -2, -3)).length)
    }

    @Test
    fun `gap without grace breaks streak`() {
        assertEquals(1L, compute(weeks(0, -2, -3)).length)
    }

    @Test
    fun `grace token bridges one gap and is consumed`() {
        val result = compute(weeks(0, -2, -3), grace = 1)
        assertEquals(3L, result.length)
        assertEquals(0, result.graceRemaining)
    }

    @Test
    fun `freeze window bridges gap without consuming grace`() {
        val gap = BadgeDates.weekIntervalAt(-1, now)
        val result = compute(
            weeks(0, -2, -3), grace = 1,
            freezes = listOf(BadgeStreakFreezeCache(badgeId = "", startDate = gap.first, endDate = gap.last))
        )
        assertEquals(3L, result.length)
        assertEquals(1, result.graceRemaining)
    }

    @Test
    fun `freeze for another badge does not apply`() {
        val gap = BadgeDates.weekIntervalAt(-1, now)
        val result = compute(
            weeks(0, -2, -3),
            freezes = listOf(BadgeStreakFreezeCache(badgeId = "timely_reporter", startDate = gap.first, endDate = gap.last))
        )
        assertEquals(1L, result.length)
    }

    @Test
    fun `second gap breaks streak once grace is exhausted`() {
        // -2 bridged by grace, -4 breaks: -5 and -6 must not count
        val result = compute(weeks(0, -1, -3, -5, -6), grace = 1)
        assertEquals(3L, result.length)
        assertEquals(0, result.graceRemaining)
    }

    @Test
    fun `period keys are stable and iso formatted`() {
        assertEquals(BadgeDates.weekKey(now), BadgeDates.weekKeyAt(0, now))
        assert(Regex("""\d{4}-W\d{2}""").matches(BadgeDates.weekKey(now)))
        assert(Regex("""\d{4}-\d{2}""").matches(BadgeDates.monthKey(now)))
        assert(Regex("""\d{4}-Q[1-4]""").matches(BadgeDates.quarterKey(now)))
    }
}
