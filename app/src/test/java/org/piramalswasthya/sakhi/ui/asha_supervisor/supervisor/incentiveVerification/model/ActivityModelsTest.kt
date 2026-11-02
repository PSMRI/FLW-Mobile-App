package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

    private fun buildActivityDetail(id: String = "a1") = ActivityDetail(
        id = id,
        name = "Immunization",
        amount = 100,
        claimCount = 2,
        activityDate = "2026-01-01",
        isDefaultActivity = true,
        groupName = "Group A",
        activityDec = "desc",
        submittedOn = "2026-01-02",
        status = ActivityStatus.PENDING
    )

    @Test
    fun `ActivityDetail exposes all constructor fields`() {
        val detail = buildActivityDetail()

        assertEquals("a1", detail.id)
        assertEquals("Immunization", detail.name)
        assertEquals(100, detail.amount)
        assertEquals(2, detail.claimCount)
        assertEquals("2026-01-01", detail.activityDate)
        assertTrue(detail.isDefaultActivity)
        assertEquals("Group A", detail.groupName)
        assertEquals("desc", detail.activityDec)
        assertEquals("2026-01-02", detail.submittedOn)
        assertEquals(ActivityStatus.PENDING, detail.status)
    }

    @Test
    fun `ActivityDetail equals hashCode toString and copy behave per data class contract`() {
        val first = buildActivityDetail()
        val same = buildActivityDetail()
        val different = buildActivityDetail(id = "a2")

        assertEquals(first, same)
        assertEquals(first.hashCode(), same.hashCode())
        assertNotEquals(first, different)
        assertTrue(first.toString().contains("Immunization"))

        val copied = first.copy(status = ActivityStatus.VERIFIED)
        assertEquals(ActivityStatus.VERIFIED, copied.status)
        assertEquals(first.id, copied.id)
    }

    @Test
    fun `ActivityDetail component functions destructure in declared order`() {
        val detail = buildActivityDetail()

        val (id, name, amount, claimCount, activityDate, isDefault, groupName, activityDec, submittedOn, status) = detail

        assertEquals(detail.id, id)
        assertEquals(detail.name, name)
        assertEquals(detail.amount, amount)
        assertEquals(detail.claimCount, claimCount)
        assertEquals(detail.activityDate, activityDate)
        assertEquals(detail.isDefaultActivity, isDefault)
        assertEquals(detail.groupName, groupName)
        assertEquals(detail.activityDec, activityDec)
        assertEquals(detail.submittedOn, submittedOn)
        assertEquals(detail.status, status)
    }

    @Test
    fun `ActivityStatus contains PENDING VERIFIED and REJECTED values`() {
        val values = ActivityStatus.values().toList()

        assertEquals(3, values.size)
        assertTrue(values.contains(ActivityStatus.PENDING))
        assertTrue(values.contains(ActivityStatus.VERIFIED))
        assertTrue(values.contains(ActivityStatus.REJECTED))
        assertEquals(ActivityStatus.REJECTED, ActivityStatus.valueOf("REJECTED"))
    }

    @Test
    fun `WorkerDetailInfo exposes worker fields and activities list`() {
        val info = WorkerDetailInfo(
            workerName = "Asha One",
            ashaId = "EMP1",
            serviceCenter = "Center A",
            month = "January",
            supervisorId = "SUP1",
            activities = listOf(buildActivityDetail())
        )

        assertEquals("Asha One", info.workerName)
        assertEquals("EMP1", info.ashaId)
        assertEquals("Center A", info.serviceCenter)
        assertEquals("January", info.month)
        assertEquals("SUP1", info.supervisorId)
        assertEquals(1, info.activities.size)

        val copied = info.copy(workerName = "Asha Two")
        assertEquals("Asha Two", copied.workerName)
        assertEquals(info, info.copy())
    }
}
