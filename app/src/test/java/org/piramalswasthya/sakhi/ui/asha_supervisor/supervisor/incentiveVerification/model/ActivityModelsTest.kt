package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ActivityModelsTest {

    @Test
    fun `RejectionReason constructor uses default isSelected when omitted`() {
        val reason = RejectionReason(
            id = "1",
            reason = "Incomplete documents"
        )

        assertNotNull(reason)
        assertEquals(false, reason.isSelected)
    }

    @Test
    fun `RejectionReason exposes id and reason and isSelected is mutable`() {
        val reason = RejectionReason(
            id = "2",
            reason = "Wrong amount",
            isSelected = true
        )

        assertEquals("2", reason.id)
        assertEquals("Wrong amount", reason.reason)
        reason.isSelected = false
        assertEquals(false, reason.isSelected)
    }

    @Test
    fun `ApprovalStatusSummary exposes rejected and pending counts`() {
        val summary = ApprovalStatusSummary(
            rejected = 3,
            pending = 5,
            verified = 2
        )

        assertEquals(3, summary.rejected)
        assertEquals(5, summary.pending)
        assertEquals(2, summary.verified)
    }
}
