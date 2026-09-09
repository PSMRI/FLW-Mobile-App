package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FLW-1171 / BRD §20.3: the Monthly Honorarium (S.No 52) arrives `isApproved` and is ticked by
 * default on the Mitanin Trainer and ANM screens, with every other claimed incentive unticked.
 * Pre-ticked is only a default — the reviewer can untick it and reject it — so this covers what
 * the screen seeds once, and nothing more.
 */
class DefaultSelectionTest {

    private fun record(incentiveId: Int, desc: String, isApproved: Boolean) = ClaimedIncentiveUI(
        activityId = incentiveId,
        incentiveId = incentiveId,
        activityDec = desc,
        groupName = "Administrative and Fixed Payments",
        amount = 100,
        claimCount = 1,
        isDefaultActivity = false,
        totalAmount = 100,
        isDefault = true,
        approvalStatus = 102,
        isApproved = isApproved
    )

    /** Mirrors the 10-Sep-2026 claimedIncentiveByUser payload. */
    private val records = listOf(
        record(99516, "Filling 5 prescribed types of information in the Mitanin register", false),
        record(99517, "Filling prescribed information in the Mitanin register", false),
        record(99515, "Monthly honorarium", true)
    )

    @Test
    fun `only the pre-approved row is ticked by default`() {
        assertEquals(listOf(99515), records.defaultSelectedIncentiveIds())
    }

    @Test
    fun `the other claimed incentives are left unticked`() {
        val seeded = records.defaultSelectedIncentiveIds()
        assertTrue(99516 !in seeded)
        assertTrue(99517 !in seeded)
    }

    @Test
    fun `keys on incentiveId, which is what the verify payload sends`() {
        // One activity can yield several claimed rows in a month, so activityId would be ambiguous.
        val duplicated = listOf(
            record(99515, "Monthly honorarium", true),
            record(99999, "Monthly honorarium", true)
        )
        assertEquals(listOf(99515, 99999), duplicated.defaultSelectedIncentiveIds())
    }

    @Test
    fun `nothing is ticked when the payload has no pre-approved row`() {
        assertTrue(records.filter { !it.isApproved }.defaultSelectedIncentiveIds().isEmpty())
    }

    @Test
    fun `an empty payload ticks nothing`() {
        assertTrue(emptyList<ClaimedIncentiveUI>().defaultSelectedIncentiveIds().isEmpty())
    }
}
