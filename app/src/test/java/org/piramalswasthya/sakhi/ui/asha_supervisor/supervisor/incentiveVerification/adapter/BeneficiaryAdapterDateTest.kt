package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * BRD 164692128 §19.4: a meeting/register claim shows the date it happened, so the reviewer is
 * checking something real. `startDate` comes back in more than one shape, and a blanked date would
 * defeat the point — hence the fallbacks.
 */
class BeneficiaryAdapterDateTest {

    @Test
    fun `parses a plain yyyy-MM-dd date`() {
        assertEquals("06 Jul 2026", BeneficiaryAdapter.formatClaimDate("2026-07-06"))
    }

    @Test
    fun `parses a full ISO timestamp with offset`() {
        assertEquals(
            "06 Jul 2026",
            BeneficiaryAdapter.formatClaimDate("2026-07-06T10:15:30.000+05:30")
        )
    }

    @Test
    fun `parses an ISO timestamp without a zone`() {
        assertEquals("06 Jul 2026", BeneficiaryAdapter.formatClaimDate("2026-07-06T10:15:30"))
    }

    @Test
    fun `parses an ISO timestamp with millis but no zone`() {
        assertEquals("06 Jul 2026", BeneficiaryAdapter.formatClaimDate("2026-07-06T10:15:30.123"))
    }

    @Test
    fun `null and blank produce no date rather than a placeholder`() {
        assertEquals("", BeneficiaryAdapter.formatClaimDate(null))
        assertEquals("", BeneficiaryAdapter.formatClaimDate(""))
        assertEquals("", BeneficiaryAdapter.formatClaimDate("   "))
    }

    @Test
    fun `an unrecognised format falls back to the raw value`() {
        // Better to show the reviewer what the server sent than to hide the date entirely.
        assertEquals("06/07/2026", BeneficiaryAdapter.formatClaimDate("06/07/2026"))
    }
}
