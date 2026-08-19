package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class BenWithCbacDomainsTest {

    private fun ben() = BenBasicDomain(
        benId = 1L,
        hhId = 1L,
        reproductiveStatusId = 0,
        regDate = "01-01-2024",
        benName = "Test",
        gender = "Female",
        dob = 0L,
        relToHeadId = 1,
        mobileNo = "9999999999",
        familyHeadName = "Test",
        syncState = SyncState.SYNCED,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    private fun cbacCache(state: SyncState) = CbacCache(
        benId = 1L,
        ashaId = 1,
        syncState = state
    )

    @Test
    fun `BenWithCbacDomain computes allSynced when omitted and records synced`() {
        val domain = BenWithCbacDomain(ben(), listOf(cbacCache(SyncState.SYNCED)))
        assertEquals(SyncState.SYNCED, domain.allSynced)
    }

    @Test
    fun `BenWithCbacDomain computes allSynced as null when records empty`() {
        val domain = BenWithCbacDomain(ben(), emptyList())
        assertNotNull(domain)
        assertEquals(null, domain.allSynced)
    }

    @Test
    fun `BenWithCbacReferDomain computes allSynced when omitted and records unsynced`() {
        val referral = ReferalCache(benId = 1L, syncState = SyncState.SYNCED)
        val domain = BenWithCbacReferDomain(ben(), listOf(cbacCache(SyncState.UNSYNCED)), referral)
        assertEquals(SyncState.UNSYNCED, domain.allSynced)
    }

    @Test
    fun `BenWithCbacDomain computes allSynced as unsynced when a record is unsynced`() {
        val records = listOf(cbacCache(SyncState.SYNCED), cbacCache(SyncState.UNSYNCED))
        val domain = BenWithCbacDomain(ben(), records)
        assertEquals(SyncState.UNSYNCED, domain.allSynced)
        assertEquals(ben(), domain.ben)
        assertEquals(records, domain.savedCbacRecords)
    }
}
