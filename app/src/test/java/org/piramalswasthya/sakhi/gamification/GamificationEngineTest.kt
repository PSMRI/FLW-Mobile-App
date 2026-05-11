package org.piramalswasthya.sakhi.gamification

import org.junit.Assert.assertEquals
import org.junit.Test

class GamificationEngineStreakTest {

    @Test
    fun `daysBetween same date returns 0`() =
        assertEquals(0L, GamificationEngine.daysBetween("2026-05-09", "2026-05-09"))

    @Test
    fun `daysBetween consecutive days returns 1`() =
        assertEquals(1L, GamificationEngine.daysBetween("2026-05-08", "2026-05-09"))

    @Test
    fun `daysBetween month boundary works`() =
        assertEquals(1L, GamificationEngine.daysBetween("2026-04-30", "2026-05-01"))

    @Test
    fun `computeLevel at 0 points is level 1`() {
        val thresholds = GamificationEngine.LEVEL_THRESHOLDS
        val level = thresholds.indexOfLast { 0 >= it }.coerceAtLeast(0) + 1
        assertEquals(1, level)
    }

    @Test
    fun `computeLevel at 100 points is level 2`() {
        val thresholds = GamificationEngine.LEVEL_THRESHOLDS
        val level = thresholds.indexOfLast { 100 >= it }.coerceAtLeast(0) + 1
        assertEquals(2, level)
    }
}
