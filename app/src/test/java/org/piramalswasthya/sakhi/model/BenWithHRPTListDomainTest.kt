package org.piramalswasthya.sakhi.model

import org.piramalswasthya.sakhi.database.room.SyncState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BenWithHRPTListDomainTest {

    private fun buildBen(): BenBasicDomain {
        return BenBasicDomain(
            benId = 1L,
            hhId = 1L,
            reproductiveStatusId = 1,
            regDate = "17-03-2026",
            benName = "Test Ben",
            gender = "F",
            dob = 0L,
            relToHeadId = 1,
            mobileNo = "9999999999",
            familyHeadName = "Head",
            abhaId = null,
            syncState = SyncState.SYNCED,
            isConsent = true,
            isSpouseAdded = false,
            isChildrenAdded = false,
            isMarried = false,
            isDeathValue = null
        )
    }

    @Test
    fun `constructor computes allSynced default when omitted`() {
        val domain = BenWithHRPTListDomain(
            ben = buildBen(),
            lmpString = "2026-01-01",
            eddString = "2026-10-01",
            weeksOfPregnancy = "10",
            savedTrackings = emptyList()
        )

        assertNotNull(domain)
        assertEquals(null, domain.allSynced)
    }
}
