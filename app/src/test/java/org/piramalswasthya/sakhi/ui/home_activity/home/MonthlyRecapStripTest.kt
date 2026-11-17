package org.piramalswasthya.sakhi.ui.home_activity.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.RecapStatus
import java.util.Calendar
import java.util.Locale

/**
 * Pure unit tests for the Monthly Recap strip state mapping and month utility.
 * These cover the privacy-safe presentation flags and year rollover without any
 * Android Context or device-clock dependency.
 */
class MonthlyRecapStripTest {

    // --- fromToken ---

    @Test
    fun `fromToken maps known tokens case-insensitively`() {
        assertEquals(MonthlyRecapStripState.READY, MonthlyRecapStripState.fromToken("ready"))
        assertEquals(MonthlyRecapStripState.READY, MonthlyRecapStripState.fromToken(" Ready "))
        assertEquals(MonthlyRecapStripState.RESUME, MonthlyRecapStripState.fromToken("resume"))
        assertEquals(MonthlyRecapStripState.REPLAY, MonthlyRecapStripState.fromToken("REPLAY"))
    }

    @Test
    fun `fromToken falls back to HIDDEN for unknown or null`() {
        assertEquals(MonthlyRecapStripState.HIDDEN, MonthlyRecapStripState.fromToken("hidden"))
        assertEquals(MonthlyRecapStripState.HIDDEN, MonthlyRecapStripState.fromToken(null))
        assertEquals(MonthlyRecapStripState.HIDDEN, MonthlyRecapStripState.fromToken("garbage"))
    }

    // --- styleFor (state priority / presentation flags) ---

    @Test
    fun `HIDDEN is not visible and does not animate`() {
        val style = styleFor(MonthlyRecapStripState.HIDDEN)
        assertFalse(style.visible)
        assertFalse(style.animateEntrance)
        assertFalse(style.showProgress)
    }

    @Test
    fun `READY shows new emphasis, animates, and has no progress`() {
        val style = styleFor(MonthlyRecapStripState.READY)
        assertTrue(style.visible)
        assertTrue(style.showNewBadge)
        assertFalse(style.showCompletedBadge)
        assertFalse(style.showProgress)
        assertTrue(style.animateEntrance)
    }

    @Test
    fun `RESUME shows generic progress and no new emphasis`() {
        val style = styleFor(MonthlyRecapStripState.RESUME)
        assertTrue(style.visible)
        assertFalse(style.showNewBadge)
        assertFalse(style.showCompletedBadge)
        assertTrue(style.showProgress)
    }

    @Test
    fun `REPLAY shows completed marker, no new emphasis, and stays static`() {
        val style = styleFor(MonthlyRecapStripState.REPLAY)
        assertTrue(style.visible)
        assertFalse(style.showNewBadge)
        assertTrue(style.showCompletedBadge)
        assertFalse(style.showProgress)
        assertFalse(style.animateEntrance)
    }

    // --- stripStateFor (availability + snapshot status -> strip state) ---

    @Test
    fun `unavailable fails closed to HIDDEN regardless of snapshot`() {
        assertEquals(MonthlyRecapStripState.HIDDEN, stripStateFor(false, null))
        assertEquals(MonthlyRecapStripState.HIDDEN, stripStateFor(false, RecapStatus.NOT_STARTED))
        assertEquals(MonthlyRecapStripState.HIDDEN, stripStateFor(false, RecapStatus.IN_PROGRESS))
        assertEquals(MonthlyRecapStripState.HIDDEN, stripStateFor(false, RecapStatus.COMPLETED))
    }

    @Test
    fun `available maps snapshot status to strip state`() {
        assertEquals(MonthlyRecapStripState.READY, stripStateFor(true, null))
        assertEquals(MonthlyRecapStripState.READY, stripStateFor(true, RecapStatus.NOT_STARTED))
        assertEquals(MonthlyRecapStripState.RESUME, stripStateFor(true, RecapStatus.IN_PROGRESS))
        assertEquals(MonthlyRecapStripState.REPLAY, stripStateFor(true, RecapStatus.COMPLETED))
    }

    // --- previousMonthLabel ---

    @Test
    fun `previousMonthLabel returns the prior month within the same year`() {
        val march = Calendar.getInstance().apply { set(2025, Calendar.MARCH, 15) }
        assertEquals("February", MonthlyRecapMonth.previousMonthLabel(Locale.ENGLISH, march))
    }

    @Test
    fun `previousMonthLabel rolls over to December in January`() {
        val january = Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 10) }
        assertEquals("December", MonthlyRecapMonth.previousMonthLabel(Locale.ENGLISH, january))
    }
}
