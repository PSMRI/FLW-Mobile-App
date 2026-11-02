package org.piramalswasthya.sakhi.model

import org.piramalswasthya.sakhi.database.room.SyncState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BenWithHRNPTListDomainTest {

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
        val domain = BenWithHRNPTListDomain(
            ben = buildBen(),
            savedTrackings = emptyList()
        )

        assertNotNull(domain)
        assertEquals(null, domain.allSynced)
    }
}
