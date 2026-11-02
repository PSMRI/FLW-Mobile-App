package org.piramalswasthya.sakhi.model

import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class HRPTrackingWrapperCacheTest {

    private fun resources(): Resources = mockk(relaxed = true) {
        every { getString(any()) } returns "Visited on"
    }

    private fun ben(benId: Long = 1L) = BenBasicCache(
        benId = benId,
        hhId = 1L,
        regDate = 1_600_000_000_000L,
        benName = "Test",
        gender = Gender.FEMALE,
        dob = 1_000_000_000_000L,
        relToHeadId = 1,
        mobileNo = 9999999999L,
        hrpStatus = false,
        syncState = SyncState.SYNCED,
        reproductiveStatusId = 1,
        lastMenstrualPeriod = null,
        isKid = false,
        immunizationStatus = false,
        villageId = 1,
        abhaId = null,
        isNewAbha = false,
        cbacFilled = false,
        cbacSyncState = SyncState.SYNCED,
        cdrFilled = false,
        cdrSyncState = SyncState.SYNCED,
        mdsrFilled = false,
        mdsrSyncState = SyncState.SYNCED,
        pmsmaSyncState = SyncState.SYNCED,
        pmsmaFilled = false,
        hbncFilled = false,
        hbycFilled = false,
        pwrFilled = false,
        pwrSyncState = SyncState.SYNCED,
        doSyncState = SyncState.SYNCED,
        irSyncState = SyncState.SYNCED,
        crSyncState = SyncState.SYNCED,
        ecrFilled = false,
        ectFilled = false,
        tbsnFilled = false,
        tbsnSyncState = SyncState.SYNCED,
        tbspFilled = false,
        tbspSyncState = SyncState.SYNCED,
        hrppaFilled = false,
        hrpnpaFilled = false,
        hrpmbpFilled = false,
        hrptFilled = false,
        hrptrackingDone = false,
        hrnptrackingDone = false,
        hrnptFilled = false,
        hrppaSyncState = SyncState.SYNCED,
        hrpnpaSyncState = SyncState.SYNCED,
        hrpmbpSyncState = SyncState.SYNCED,
        hrptSyncState = SyncState.SYNCED,
        hrnptSyncState = SyncState.SYNCED,
        isDelivered = false,
        pwHrp = false,
        irFilled = false,
        isMdsr = false,
        crFilled = false,
        doFilled = false,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    // =====================================================
    // BenWithHRPTrackingCache.asDomainModel()
    // =====================================================

    @Test fun `BenWithHRPTrackingCache asDomainModel maps ben and empty trackings`() {
        val wrapper = BenWithHRPTrackingCache(
            ben = ben(),
            assessCache = HRPPregnantAssessCache(benId = 1L, lmpDate = 1_500_000_000_000L),
            savedTrackings = emptyList()
        )
        val domain = wrapper.asDomainModel(resources())
        assertEquals(1L, domain.ben.benId)
        assertEquals(0, domain.savedTrackings.size)
        assertNotNull(domain.lmpString)
        assertNotNull(domain.eddString)
    }

    @Test fun `BenWithHRPTrackingCache asDomainModel maps non-empty trackings with filled date`() {
        val wrapper = BenWithHRPTrackingCache(
            ben = ben(),
            assessCache = HRPPregnantAssessCache(benId = 1L, lmpDate = 1_500_000_000_000L),
            savedTrackings = listOf(
                HRPPregnantTrackCache(benId = 1L, visitDate = 1_600_000_000_000L, syncState = SyncState.SYNCED)
            )
        )
        val domain = wrapper.asDomainModel(resources())
        assertEquals(1, domain.savedTrackings.size)
        assertEquals(1L, domain.savedTrackings[0].benId)
        assertEquals(SyncState.SYNCED, domain.savedTrackings[0].syncState)
        assertNotNull(domain.savedTrackings[0].filledOnString)
    }

    @Test fun `BenWithHRPTrackingCache asDomainModel weeksOfPregnancy is NA when beyond term`() {
        val wrapper = BenWithHRPTrackingCache(
            ben = ben(),
            assessCache = HRPPregnantAssessCache(benId = 1L, lmpDate = 0L),
            savedTrackings = emptyList()
        )
        val domain = wrapper.asDomainModel(resources())
        assertEquals("NA", domain.weeksOfPregnancy)
    }

    // =====================================================
    // BenWithHRNPTrackingCache.asDomainModel()
    // =====================================================

    @Test fun `BenWithHRNPTrackingCache asDomainModel maps ben and empty trackings`() {
        val wrapper = BenWithHRNPTrackingCache(
            ben = ben(),
            assessCache = HRPNonPregnantAssessCache(benId = 1L),
            savedTrackings = emptyList()
        )
        val domain = wrapper.asDomainModel(resources())
        assertEquals(1L, domain.ben.benId)
        assertEquals(0, domain.savedTrackings.size)
        assertNull(domain.allSynced)
    }

    @Test fun `BenWithHRNPTrackingCache asDomainModel maps non-empty trackings`() {
        val wrapper = BenWithHRNPTrackingCache(
            ben = ben(),
            assessCache = HRPNonPregnantAssessCache(benId = 1L),
            savedTrackings = listOf(
                HRPNonPregnantTrackCache(benId = 1L, visitDate = 1_600_000_000_000L, syncState = SyncState.SYNCED)
            )
        )
        val domain = wrapper.asDomainModel(resources())
        assertEquals(1, domain.savedTrackings.size)
        assertEquals(SyncState.SYNCED, domain.savedTrackings[0].syncState)
        assertEquals(SyncState.SYNCED, domain.allSynced)
    }

    // =====================================================
    // BenWithHRPACache.asDomainModel()
    // =====================================================

    @Test fun `BenWithHRPACache asDomainModel with null assess and mbp`() {
        val wrapper = BenWithHRPACache(ben = ben(), assess = null, mbp = null)
        val domain = wrapper.asDomainModel()
        assertEquals(1L, domain.ben.benId)
        assertNull(domain.lmpString)
        assertNull(domain.eddString)
        assertNull(domain.weeksOfPregnancy)
        assertNull(domain.mbp)
    }

    @Test fun `BenWithHRPACache asDomainModel computes lmp derived fields when assess present`() {
        val wrapper = BenWithHRPACache(
            ben = ben(),
            assess = HRPPregnantAssessCache(benId = 1L, lmpDate = 1_500_000_000_000L),
            mbp = HRPMicroBirthPlanCache(benId = 1L)
        )
        val domain = wrapper.asDomainModel()
        assertNotNull(domain.lmpString)
        assertNotNull(domain.eddString)
        assertNotNull(domain.mbp)
    }
}
