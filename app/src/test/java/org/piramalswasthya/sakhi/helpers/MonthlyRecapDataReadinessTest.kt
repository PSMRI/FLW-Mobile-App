package org.piramalswasthya.sakhi.helpers

import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.model.RecapFreezeBlocker

/**
 * Every branch of the freeze decision. The rule is fail-closed: anything other
 * than a fully trustworthy local database must NOT be frozen, because every
 * failure mode in the sync pipeline loses rows and none can add them — so an
 * ungated freeze can only ever under-credit the ASHA's work.
 */
class MonthlyRecapDataReadinessTest {

    /** 1 June 2026 00:00 UTC — the recap window start used throughout. */
    private val windowStart = 1_748_736_000_000L
    private val dayMillis = 86_400_000L

    private fun blocker(
        fullPull: Boolean = true,
        localDataSince: Long = windowStart - 30 * dayMillis,
        hasWork: Boolean = true,
    ) = recapFreezeBlocker(
        readiness = RecapDataReadiness(fullPull, localDataSince),
        windowStartMillis = windowStart,
        hasCountableWork = hasWork,
    )

    @Test
    fun `trusted device with work freezes`() {
        assertEquals(RecapFreezeBlocker.NONE, blocker())
    }

    @Test
    fun `unfinished full pull blocks`() {
        assertEquals(RecapFreezeBlocker.FULL_PULL_INCOMPLETE, blocker(fullPull = false))
    }

    @Test
    fun `unfinished full pull outranks every other reason`() {
        // The most fundamental cause is reported, so diagnostics name the real problem.
        assertEquals(
            RecapFreezeBlocker.FULL_PULL_INCOMPLETE,
            blocker(fullPull = false, localDataSince = windowStart + dayMillis, hasWork = false),
        )
    }

    @Test
    fun `install newer than the month blocks - the re-install case`() {
        assertEquals(
            RecapFreezeBlocker.DEVICE_MISSED_PART_OF_MONTH,
            blocker(localDataSince = windowStart + dayMillis),
        )
    }

    @Test
    fun `install one millisecond into the month blocks`() {
        assertEquals(
            RecapFreezeBlocker.DEVICE_MISSED_PART_OF_MONTH,
            blocker(localDataSince = windowStart + 1),
        )
    }

    @Test
    fun `install exactly at the window start is trusted`() {
        // Boundary: present for the whole month, so nothing could have been missed.
        assertEquals(RecapFreezeBlocker.NONE, blocker(localDataSince = windowStart))
    }

    @Test
    fun `unknown install marker fails closed rather than reading as very old`() {
        assertEquals(RecapFreezeBlocker.DEVICE_MISSED_PART_OF_MONTH, blocker(localDataSince = 0L))
        assertEquals(RecapFreezeBlocker.DEVICE_MISSED_PART_OF_MONTH, blocker(localDataSince = -1L))
    }

    @Test
    fun `trusted device with no countable work reports the zero-month rule`() {
        assertEquals(RecapFreezeBlocker.NO_COUNTABLE_WORK, blocker(hasWork = false))
    }

    @Test
    fun `readiness is checked before countable work`() {
        // A brand-new install with zero work must report the readiness problem, not
        // the zero-month rule — otherwise the diagnostics would mislead an investigation.
        assertEquals(
            RecapFreezeBlocker.DEVICE_MISSED_PART_OF_MONTH,
            blocker(localDataSince = windowStart + dayMillis, hasWork = false),
        )
    }

    @Test
    fun `only NONE permits a freeze`() {
        // Guards against a future reason being added and accidentally treated as ready.
        val blocking = RecapFreezeBlocker.entries.filter { it != RecapFreezeBlocker.NONE }
        assertEquals(3, blocking.size)
    }
}
