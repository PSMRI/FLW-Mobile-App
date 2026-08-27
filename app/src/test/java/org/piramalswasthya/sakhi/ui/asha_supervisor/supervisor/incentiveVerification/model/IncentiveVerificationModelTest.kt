package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.ClaimedIncentiveUI

/**
 * Plain data-class model coverage for the incentive verification screens. None of these
 * carry any mapping logic; they were only ever exercised indirectly through mocks in
 * [org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.WorkerDetailViewModelTest],
 * so their generated equals()/hashCode()/copy()/toString() were never invoked directly.
 */
class IncentiveVerificationModelTest {

    private fun activity() = Activity(
        approvalStatus = 1,
        reason = "reason",
        otherReason = "other",
        claimedDate = "2024-01-01",
        approvalDate = "2024-01-02",
        verifiedByUserName = "verifier",
        role = "ASHA",
        isClaimed = true
    )

    @Test fun `Activity copy and equality`() {
        val a = activity()
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(reason = "different"))
        assertTrue(a.toString().contains("Activity"))
    }

    private fun ashaWorkerResponse() = AshaWorkerResponse(
        approvalStatus = 1,
        facilityId = 10,
        gender = "F",
        facilityType = "PHC",
        rejected = 0,
        pending = 1,
        mobile = "9999999999",
        verified = 2,
        fullName = "Asha Devi",
        employeeId = "EMP1",
        userId = 5,
        totalAmount = 500,
        facilityName = "Facility",
        activities = listOf(activity())
    )

    @Test fun `AshaWorkerResponse copy and equality`() {
        val a = ashaWorkerResponse()
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(userId = 6))
        assertTrue(a.toString().contains("AshaWorkerResponse"))
    }

    @Test fun `AshaWorkerResponse holds field values`() {
        val a = ashaWorkerResponse()
        assertEquals(5, a.userId)
        assertEquals("Asha Devi", a.fullName)
        assertEquals(1, a.activities?.size)
    }

    private fun approvalStatusSummary() = ApprovalStatusSummary(rejected = 1, pending = 2, verified = 3)

    @Test fun `ApprovalStatusSummary defaults and copy`() {
        val default = ApprovalStatusSummary()
        assertEquals(0, default.rejected)
        assertEquals(0, default.pending)
        assertEquals(0, default.verified)

        val a = approvalStatusSummary()
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(rejected = 99))
        assertTrue(a.toString().contains("ApprovalStatusSummary"))
    }

    private fun ashaListResponse() = AshaListResponse(
        approvalStatus = approvalStatusSummary(),
        data = listOf(ashaWorkerResponse()),
        statusCode = 200
    )

    @Test fun `AshaListResponse copy and equality`() {
        val a = ashaListResponse()
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(statusCode = 500))
        assertTrue(a.toString().contains("AshaListResponse"))
        assertEquals(200, a.statusCode)
        assertEquals(1, a.data?.size)
    }

    private fun ashaWorker() = AshaWorker(
        id = "1",
        name = "Asha Devi",
        ashaId = "ASHA1",
        approvalDate = "2024-01-01",
        OtherReason = "other",
        reason = "reason",
        serviceCenter = "PHC",
        verifiedByUserName = "verifier",
        role = "ASHA",
        amount = 100,
        pending = 1,
        verified = 2,
        rejected = 0,
        status = VerificationStatus.VERIFIED
    )

    @Test fun `AshaWorker copy and equality`() {
        val a = ashaWorker()
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(status = VerificationStatus.PENDING))
        assertTrue(a.toString().contains("AshaWorker"))
    }

    @Test fun `VerificationStatus has expected enum values`() {
        assertEquals(
            listOf("VERIFIED", "PENDING", "REJECTED", "OVERDUE", "APPROVED", "UNCLAIMED", "ALL"),
            VerificationStatus.values().map { it.name }
        )
    }

    private fun monthlyDetail() = MonthlyDetail(
        month = "January",
        year = 2024,
        serviceCenter = "PHC",
        verifiedCount = 5,
        pendingCount = 2,
        rejectedCount = 1
    )

    @Test fun `MonthlyDetail copy and equality`() {
        val a = monthlyDetail()
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(year = 2025))
        assertTrue(a.toString().contains("MonthlyDetail"))
    }

    private fun supervisor() = Supervisor(name = "Sup Name", id = "SUP1")

    @Test fun `Supervisor copy and equality`() {
        val a = supervisor()
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(name = "Other"))
        assertTrue(a.toString().contains("Supervisor"))
    }

    private fun claimedIncentiveUI() = ClaimedIncentiveUI(
        activityId = 1,
        activityDec = "desc",
        groupName = "group",
        amount = 100,
        claimCount = 2,
        isDefaultActivity = true,
        totalAmount = 200,
        isDefault = false,
        approvalStatus = 1
    )

    @Test fun `ClaimedIncentiveUI copy and equality`() {
        val a = claimedIncentiveUI()
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(amount = 999))
        assertTrue(a.toString().contains("ClaimedIncentiveUI"))
        assertEquals(1, a.activityId)
        assertEquals(200, a.totalAmount)
    }
}
