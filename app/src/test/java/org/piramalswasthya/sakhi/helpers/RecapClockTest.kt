package org.piramalswasthya.sakhi.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.RecapStatus
import org.piramalswasthya.sakhi.ui.home_activity.home.MonthlyRecapStripState
import org.piramalswasthya.sakhi.ui.home_activity.home.stripStateFor
import java.util.Calendar

/**
 * Pure JVM tests for the Monthly Recap month-window resolver. No emulator
 * clock changes — fixed calendars are passed in directly.
 */
class RecapClockTest {

    private fun cal(year: Int, month0: Int, day: Int, hour: Int = 0, minute: Int = 0): Calendar =
        Calendar.getInstance().apply {
            clear()
            set(year, month0, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }

    @Test
    fun `mid-month resolves the immediately previous month`() {
        val window = previousMonthWindow(cal(2025, Calendar.MARCH, 15, 14, 23))
        assertEquals(2025, window.year)
        assertEquals(2, window.month)
        assertEquals(202502, window.yearMonth)
        assertEquals(cal(2025, Calendar.FEBRUARY, 1).timeInMillis, window.startMillis)
        assertEquals(cal(2025, Calendar.MARCH, 1).timeInMillis, window.endMillisExclusive)
    }

    @Test
    fun `january rolls over to previous december`() {
        val window = previousMonthWindow(cal(2025, Calendar.JANUARY, 10))
        assertEquals(2024, window.year)
        assertEquals(12, window.month)
        assertEquals(202412, window.yearMonth)
        assertEquals(cal(2024, Calendar.DECEMBER, 1).timeInMillis, window.startMillis)
        assertEquals(cal(2025, Calendar.JANUARY, 1).timeInMillis, window.endMillisExclusive)
    }

    @Test
    fun `leap-year february window spans 29 days`() {
        val window = previousMonthWindow(cal(2024, Calendar.MARCH, 5))
        assertEquals(202402, window.yearMonth)
        val days = (window.endMillisExclusive - window.startMillis) / (24L * 60 * 60 * 1000)
        assertEquals(29, days)
    }

    @Test
    fun `window boundaries are start-inclusive and end-exclusive`() {
        // Exactly the first instant of July: June is the fully completed month,
        // and the end boundary equals "now" without including it.
        val firstInstantOfJuly = cal(2026, Calendar.JULY, 1)
        val window = previousMonthWindow(firstInstantOfJuly)
        assertEquals(202606, window.yearMonth)
        assertEquals(cal(2026, Calendar.JUNE, 1).timeInMillis, window.startMillis)
        assertEquals(firstInstantOfJuly.timeInMillis, window.endMillisExclusive)
        assertTrue(window.startMillis < window.endMillisExclusive)
    }

    @Test
    fun `yearMonth key is numeric and comparable across year boundary`() {
        val december = previousMonthWindow(cal(2025, Calendar.JANUARY, 2)).yearMonth   // 202412
        val january = previousMonthWindow(cal(2025, Calendar.FEBRUARY, 2)).yearMonth   // 202501
        assertTrue(december < january)
    }

    // --- seven-day active window (days 1..7 open; 8th onward closed) ---

    @Test
    fun `first day opens the window and targets previous month`() {
        val now = cal(2026, Calendar.AUGUST, 1, 0, 0)
        assertTrue(isMonthlyRecapWindowOpen(now))
        assertEquals(202607, previousMonthWindow(now).yearMonth) // July 2026
    }

    @Test
    fun `seventh day just before midnight remains open`() {
        val now = cal(2026, Calendar.AUGUST, 7, 23, 59)
        assertTrue(isMonthlyRecapWindowOpen(now))
        assertEquals(202607, previousMonthWindow(now).yearMonth)
    }

    @Test
    fun `eighth day at midnight closes the window`() {
        assertFalse(isMonthlyRecapWindowOpen(cal(2026, Calendar.AUGUST, 8, 0, 0)))
    }

    @Test
    fun `later month date remains closed`() {
        assertFalse(isMonthlyRecapWindowOpen(cal(2026, Calendar.AUGUST, 20)))
    }

    @Test
    fun `first open on the fifth day is still within the fixed window`() {
        // The window is fixed to the calendar month, not to first-open time.
        assertTrue(isMonthlyRecapWindowOpen(cal(2026, Calendar.AUGUST, 5, 9, 15)))
    }

    @Test
    fun `january rollover opens window and targets previous december`() {
        val now = cal(2027, Calendar.JANUARY, 1, 0, 0)
        assertTrue(isMonthlyRecapWindowOpen(now))
        assertEquals(202612, previousMonthWindow(now).yearMonth) // December 2026
    }

    @Test
    fun `month transition retargets the recap month`() {
        // 1 Sep 2026 -> target August 2026 (not July); each month is independent.
        val now = cal(2026, Calendar.SEPTEMBER, 1, 0, 0)
        assertTrue(isMonthlyRecapWindowOpen(now))
        assertEquals(202608, previousMonthWindow(now).yearMonth)
    }

    @Test
    fun `active window days value is seven`() {
        assertEquals(7, ACTIVE_WINDOW_DAYS)
    }

    // --- combined: window + gates + snapshot status -> final strip state ---

    /** Mirrors production wiring: window feeds the activeWindowOpen gate. */
    private fun stripStateAt(now: Calendar, status: RecapStatus?): MonthlyRecapStripState {
        val available = MonthlyRecapGates(
            emergencySwitchOn = true,
            moduleEnabled = true,
            userEligible = true,
            activeWindowOpen = isMonthlyRecapWindowOpen(now),
            configFresh = true,
        ).available()
        return stripStateFor(available, status)
    }

    @Test
    fun `within window snapshot status drives READY RESUME REPLAY`() {
        val inside = cal(2026, Calendar.AUGUST, 3)
        assertEquals(MonthlyRecapStripState.READY, stripStateAt(inside, null))
        assertEquals(MonthlyRecapStripState.READY, stripStateAt(inside, RecapStatus.NOT_STARTED))
        assertEquals(MonthlyRecapStripState.RESUME, stripStateAt(inside, RecapStatus.IN_PROGRESS))
        assertEquals(MonthlyRecapStripState.REPLAY, stripStateAt(inside, RecapStatus.COMPLETED))
    }

    @Test
    fun `outside window is HIDDEN regardless of snapshot status`() {
        val outside = cal(2026, Calendar.AUGUST, 8, 0, 0)
        assertEquals(MonthlyRecapStripState.HIDDEN, stripStateAt(outside, null))
        assertEquals(MonthlyRecapStripState.HIDDEN, stripStateAt(outside, RecapStatus.IN_PROGRESS))
        assertEquals(MonthlyRecapStripState.HIDDEN, stripStateAt(outside, RecapStatus.COMPLETED))
    }

    @Test
    fun `failed or unknown gate is HIDDEN even during days 1 to 7`() {
        val inside = cal(2026, Calendar.AUGUST, 3)
        val unknownGate = MonthlyRecapGates(
            emergencySwitchOn = null, // unknown
            moduleEnabled = true,
            userEligible = true,
            activeWindowOpen = isMonthlyRecapWindowOpen(inside), // true
            configFresh = true,
        ).available()
        assertEquals(MonthlyRecapStripState.HIDDEN, stripStateFor(unknownGate, RecapStatus.COMPLETED))
    }
}
