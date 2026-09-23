package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The Verify / Reject rule on the supervisor's worker-detail screen.
 *
 * Regression cover for the saksham / utprerona defect: `btnVerify` was shown only on the
 * mitanin flavour while `btnReject` was never flavour-gated, so a PENDING (102) claim offered
 * Reject alone. The rule is expressed once in [isClaimActionable] and consumed by both the
 * worker-detail and beneficiary-detail screens.
 */
class ClaimActionableTest {

    private fun actionableFor(status: VerificationStatus) =
        isClaimActionable(status.name, status.code)

    @Test
    fun `a pending claim is actionable`() {
        // 102 — the status in the reported saksham payload.
        assertTrue(actionableFor(VerificationStatus.PENDING))
        assertTrue(isClaimActionable("PENDING", 102))
    }

    @Test
    fun `an overdue claim stays actionable`() {
        // FLW-1169: the overdue tag marks lateness, it never removes the action.
        assertTrue(actionableFor(VerificationStatus.OVERDUE))
    }

    @Test
    fun `decided claims are not actionable`() {
        assertFalse(actionableFor(VerificationStatus.VERIFIED))
        assertFalse(actionableFor(VerificationStatus.APPROVED))
        assertFalse(actionableFor(VerificationStatus.REJECTED))
    }

    @Test
    fun `either representation of a decided status closes the claim`() {
        // The list passes name and code; one alone must still settle it.
        assertFalse(isClaimActionable("VERIFIED", 0))
        assertFalse(isClaimActionable("", VerificationStatus.REJECTED.code))
        assertFalse(isClaimActionable("APPROVED", VerificationStatus.APPROVED.code))
    }

    @Test
    fun `absent navigation arguments leave the claim actionable`() {
        // Matches the behaviour the action card has always had, and mirrors the list's own
        // default of PENDING for an unknown code (IncentiveVerificationViewModel.mapStatus).
        assertTrue(isClaimActionable("", 0))
    }

    @Test
    fun `an unknown future status is actionable rather than silently locked`() {
        assertTrue(isClaimActionable("SOMETHING_NEW", 999))
    }
}
