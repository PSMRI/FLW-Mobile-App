package org.piramalswasthya.sakhi.ui.home_activity.incentives

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FLW-1177 — Mitanin claim/resubmit window.
 *
 * Two halves:
 *  - `mitanin_*` pin the new pilot rule (claim any day *within* the month the work belongs to).
 *  - `legacy_*` pin the pre-existing behaviour for saksham / sakshamStag / sakshamUat
 *    (Utprerona) / niramay / xushrukha, which FLW-1177 must not touch. Those expectations were
 *    taken from the shipped rule (last month until the 12th, older months always) and will fail
 *    if the Mitanin branch ever leaks into the shared path.
 *
 * Month indices are 0-based: 0 = January, 8 = September, 11 = December.
 */
class IncentiveClaimWindowTest {

    private fun mitanin(
        selectedMonthIndex: Int,
        selectedYear: Int,
        currentMonthIndex: Int,
        currentYear: Int,
        currentDayOfMonth: Int
    ) = IncentiveClaimWindow.isClaimAllowed(
        isMitaninVariant = true,
        selectedMonthIndex = selectedMonthIndex,
        selectedYear = selectedYear,
        currentMonthIndex = currentMonthIndex,
        currentYear = currentYear,
        currentDayOfMonth = currentDayOfMonth
    )

    private fun legacy(
        selectedMonthIndex: Int,
        selectedYear: Int,
        currentMonthIndex: Int,
        currentYear: Int,
        currentDayOfMonth: Int
    ) = IncentiveClaimWindow.isClaimAllowed(
        isMitaninVariant = false,
        selectedMonthIndex = selectedMonthIndex,
        selectedYear = selectedYear,
        currentMonthIndex = currentMonthIndex,
        currentYear = currentYear,
        currentDayOfMonth = currentDayOfMonth
    )

    // ─────────────────────────────────────────────────────────────
    // Mitanin — FLW-1177 pilot rule.
    // The claim targets the PREVIOUS month (August is claimed during September).
    // FLW-1177 removed only the days 1-3 / 1-5 cut-off, not the target month.
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `mitanin allows last month on the first day`() {
        assertTrue(mitanin(7, 2026, 8, 2026, 1))
    }

    /** Was the old cut-off boundary: day 3 with no rejection, day 5 with one. */
    @Test
    fun `mitanin allows last month past the old three and five day cut-offs`() {
        assertTrue(mitanin(7, 2026, 8, 2026, 4))
        assertTrue(mitanin(7, 2026, 8, 2026, 6))
        assertTrue(mitanin(7, 2026, 8, 2026, 15))
    }

    @Test
    fun `mitanin allows last month on the final day of the current month`() {
        assertTrue(mitanin(7, 2026, 8, 2026, 30))
    }

    /**
     * The whole point of the ticket: a rejected activity from last month stays correctable on
     * every day of the current month, so Reclaim is reachable all month.
     */
    @Test
    fun `mitanin day of month never affects last month`() {
        for (day in 1..31) {
            assertTrue("day $day should be claimable", mitanin(7, 2026, 8, 2026, day))
        }
    }

    /** The current month is still in progress — it is claimed next month, not now. */
    @Test
    fun `mitanin blocks the current month`() {
        for (day in 1..31) {
            assertFalse("day $day should not be claimable", mitanin(8, 2026, 8, 2026, day))
        }
    }

    @Test
    fun `mitanin blocks months older than last month`() {
        assertFalse(mitanin(6, 2026, 8, 2026, 10))
        assertFalse(mitanin(0, 2026, 8, 2026, 10))
        assertFalse(mitanin(5, 2025, 8, 2026, 10))
    }

    @Test
    fun `mitanin blocks future months`() {
        assertFalse(mitanin(11, 2026, 8, 2026, 10))
        assertFalse(mitanin(0, 2027, 8, 2026, 10))
    }

    @Test
    fun `mitanin allows december from january across the year boundary`() {
        assertTrue(mitanin(11, 2026, 0, 2027, 1))
        assertTrue(mitanin(11, 2026, 0, 2027, 13))
        assertTrue(mitanin(11, 2026, 0, 2027, 31))
    }

    @Test
    fun `mitanin blocks january while january is running`() {
        assertFalse(mitanin(0, 2027, 0, 2027, 1))
        assertFalse(mitanin(0, 2027, 0, 2027, 31))
    }

    @Test
    fun `mitanin blocks the same month index in a different year`() {
        assertFalse(mitanin(7, 2025, 8, 2026, 10))
        assertFalse(mitanin(7, 2027, 8, 2026, 10))
    }

    // ─────────────────────────────────────────────────────────────
    // Every other flavor — must stay exactly as before FLW-1177
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `legacy allows last month up to and including the twelfth`() {
        assertTrue(legacy(7, 2026, 8, 2026, 1))
        assertTrue(legacy(7, 2026, 8, 2026, 12))
    }

    @Test
    fun `legacy blocks last month from the thirteenth`() {
        assertFalse(legacy(7, 2026, 8, 2026, 13))
        assertFalse(legacy(7, 2026, 8, 2026, 28))
    }

    @Test
    fun `legacy allows older months on any day`() {
        assertTrue(legacy(0, 2026, 8, 2026, 13))
        assertTrue(legacy(5, 2025, 8, 2026, 28))
    }

    @Test
    fun `legacy blocks the current month`() {
        for (day in 1..31) {
            assertFalse("day $day should not be claimable", legacy(8, 2026, 8, 2026, day))
        }
    }

    @Test
    fun `legacy blocks future months`() {
        assertFalse(legacy(11, 2026, 8, 2026, 10))
        assertFalse(legacy(0, 2027, 8, 2026, 10))
    }

    @Test
    fun `legacy treats december as last month when january is running`() {
        assertTrue(legacy(11, 2026, 0, 2027, 12))
        assertFalse(legacy(11, 2026, 0, 2027, 13))
    }

    @Test
    fun `legacy allows months before last december across the year boundary`() {
        // November 2026 seen from January 2027 is an older month, not last month.
        assertTrue(legacy(10, 2026, 0, 2027, 13))
    }

    // ─────────────────────────────────────────────────────────────
    // The behaviour FLW-1177 deliberately diverges on
    // ─────────────────────────────────────────────────────────────

    /**
     * Both branches target last month; they differ only in how long it stays open. Legacy shuts
     * at the 12th, Mitanin stays open for the whole month so a rejection can still be corrected.
     */
    @Test
    fun `after the twelfth only mitanin still allows last month`() {
        assertTrue(mitanin(7, 2026, 8, 2026, 13))
        assertFalse(legacy(7, 2026, 8, 2026, 13))
    }

    @Test
    fun `up to the twelfth both branches allow last month`() {
        assertTrue(mitanin(7, 2026, 8, 2026, 2))
        assertTrue(legacy(7, 2026, 8, 2026, 2))
    }

    /** Neither branch ever offers the month that is still running. */
    @Test
    fun `no branch allows the current month`() {
        assertFalse(mitanin(8, 2026, 8, 2026, 15))
        assertFalse(legacy(8, 2026, 8, 2026, 15))
    }

    /** Older months stay a legacy-only allowance; Mitanin closes them at month end. */
    @Test
    fun `older months remain legacy only`() {
        assertFalse(mitanin(5, 2026, 8, 2026, 20))
        assertTrue(legacy(5, 2026, 8, 2026, 20))
    }
}
