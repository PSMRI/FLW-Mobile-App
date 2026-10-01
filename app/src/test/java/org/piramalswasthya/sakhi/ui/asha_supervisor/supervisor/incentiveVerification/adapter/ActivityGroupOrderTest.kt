package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter

import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.ClaimedIncentiveUI

/**
 * FLW-1171: the monthly honorarium comes back pre-ticked (`isApproved`) and must read as the
 * default the reviewer is accepting — so it leads its group, and its group leads the screen,
 * with the unticked claims below. Everything else keeps the order the payload sent.
 */
class ActivityGroupOrderTest {

    private fun record(
        id: Int,
        group: String,
        desc: String,
        isApproved: Boolean = false
    ) = ClaimedIncentiveUI(
        activityId = id,
        incentiveId = id,
        activityDec = desc,
        groupName = group,
        amount = 100,
        claimCount = 1,
        isDefaultActivity = isApproved,
        totalAmount = 100,
        isDefault = isApproved,
        approvalStatus = 102,
        isApproved = isApproved
    )

    @Test
    fun `pre-ticked record leads its group`() {
        val groups = listOf(
            record(1, "Administrative and Fixed Payments", "Filling 5 prescribed types"),
            record(2, "Administrative and Fixed Payments", "Filling prescribed information"),
            record(52, "Administrative and Fixed Payments", "Monthly honorarium", isApproved = true)
        ).toActivityGroups()

        assertEquals(
            listOf("Monthly honorarium", "Filling 5 prescribed types", "Filling prescribed information"),
            groups.single().activities.map { it.activityDec }
        )
    }

    @Test
    fun `group holding the pre-ticked record leads the screen`() {
        val groups = listOf(
            record(1, "Maternal Health", "ANC checkup"),
            record(2, "Child Health", "Immunization"),
            record(52, "Administrative and Fixed Payments", "Monthly honorarium", isApproved = true)
        ).toActivityGroups()

        assertEquals("Administrative and Fixed Payments", groups.first().groupName)
        // The remaining groups keep the order the payload sent them in.
        assertEquals(listOf("Maternal Health", "Child Health"), groups.drop(1).map { it.groupName })
    }

    @Test
    fun `no pre-ticked record leaves the payload order untouched`() {
        val groups = listOf(
            record(1, "Maternal Health", "ANC checkup"),
            record(2, "Maternal Health", "PNC visit"),
            record(3, "Child Health", "Immunization")
        ).toActivityGroups()

        assertEquals(listOf("Maternal Health", "Child Health"), groups.map { it.groupName })
        assertEquals(
            listOf("ANC checkup", "PNC visit"),
            groups.first().activities.map { it.activityDec }
        )
    }

    @Test
    fun `group totals stay correct after reordering`() {
        val groups = listOf(
            record(1, "Administrative and Fixed Payments", "Filling 5 prescribed types"),
            record(52, "Administrative and Fixed Payments", "Monthly honorarium", isApproved = true)
        ).toActivityGroups()

        assertEquals(200, groups.single().totalAmount)
        assertEquals(2, groups.single().totalClaims)
    }
}
