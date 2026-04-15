package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class ScreeningCacheTest {

    // =====================================================
    // AESScreeningCache Extended Tests
    // =====================================================

    @Test fun `AESScreeningCache default id is 0`() {
        val cache = AESScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertEquals(0, cache.id)
    }

    @Test fun `AESScreeningCache beneficiaryStatus default is empty`() {
        val cache = AESScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertNull(cache.beneficiaryStatus)
    }

    @Test fun `AESScreeningCache copy changes benId`() {
        val cache = AESScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        val copy = cache.copy(benId = 99L)
        assertEquals(99L, copy.benId)
    }

    @Test fun `AESScreeningCache key fields match for same data`() {
        val a = AESScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertEquals(1L, a.benId)
        assertEquals(2L, a.houseHoldDetailsId)
    }

    @Test fun `AESScreeningCache not equals for different benId`() {
        val a = AESScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        val b = AESScreeningCache(benId = 3L, houseHoldDetailsId = 2L)
        assertNotEquals(a.benId, b.benId)
    }

    @Test fun `AESScreeningCache same benId have same key`() {
        val a = AESScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        val b = AESScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertEquals(a.benId, b.benId)
        assertEquals(a.houseHoldDetailsId, b.houseHoldDetailsId)
    }

    // =====================================================
    // FilariaScreeningCache Extended Tests
    // =====================================================

    @Test fun `FilariaScreeningCache default id is 0`() {
        val cache = FilariaScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertEquals(0, cache.id)
    }

    @Test fun `FilariaScreeningCache copy`() {
        val cache = FilariaScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        val copy = cache.copy(benId = 50L)
        assertEquals(50L, copy.benId)
    }

    @Test fun `FilariaScreeningCache same key fields`() {
        val a = FilariaScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertEquals(1L, a.benId)
        assertEquals(2L, a.houseHoldDetailsId)
    }

    @Test fun `FilariaScreeningCache not equals`() {
        val a = FilariaScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        val b = FilariaScreeningCache(benId = 2L, houseHoldDetailsId = 2L)
        assertNotEquals(a, b)
    }

    // =====================================================
    // KalaAzarScreeningCache Extended Tests
    // =====================================================

    @Test fun `KalaAzarScreeningCache default id is 0`() {
        val cache = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertEquals(0, cache.id)
    }


    @Test fun `KalaAzarScreeningCache copy`() {
        val cache = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        val copy = cache.copy(benId = 77L)
        assertEquals(77L, copy.benId)
    }

    @Test fun `KalaAzarScreeningCache same key fields`() {
        val a = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertEquals(1L, a.benId)
        assertEquals(2L, a.houseHoldDetailsId)
    }

    // =====================================================
    // MalariaScreeningCache Extended Tests
    // =====================================================

    @Test fun `MalariaScreeningCache default id is 0`() {
        val cache = MalariaScreeningCache(benId = 1L, houseHoldDetailsId = 2L, visitId = 1)
        assertEquals(0, cache.id)
    }

    @Test fun `MalariaScreeningCache visitId`() {
        val cache = MalariaScreeningCache(benId = 1L, houseHoldDetailsId = 2L, visitId = 5)
        assertEquals(5L, cache.visitId)
    }

    @Test fun `MalariaScreeningCache copy visitId`() {
        val cache = MalariaScreeningCache(benId = 1L, houseHoldDetailsId = 2L, visitId = 1)
        val copy = cache.copy(visitId = 10)
        assertEquals(10L, copy.visitId)
    }

    @Test fun `MalariaScreeningCache default syncState`() {
        val cache = MalariaScreeningCache(benId = 1L, houseHoldDetailsId = 2L, visitId = 1)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    @Test fun `MalariaScreeningCache same key fields`() {
        val a = MalariaScreeningCache(benId = 1L, houseHoldDetailsId = 2L, visitId = 1)
        assertEquals(1L, a.benId)
        assertEquals(1L, a.visitId)
    }

    // =====================================================
    // IRSRoundScreening Extended Tests
    // =====================================================


    @Test fun `IRSRoundScreening copy`() {
        val cache = IRSRoundScreening(id = 0, householdId = 1L)
        val copy = cache.copy(householdId = 99L)
        assertEquals(99L, copy.householdId)
    }

    @Test fun `IRSRoundScreening same key fields`() {
        val a = IRSRoundScreening(id = 0, householdId = 1L)
        assertEquals(0, a.id)
        assertEquals(1L, a.householdId)
    }

    // =====================================================
    // LeprosyScreeningCache Tests
    // =====================================================

    @Test fun `LeprosyScreeningCache creation`() {
        val cache = LeprosyScreeningCache(benId = 1L, houseHoldDetailsId = 2L, createdBy = "user", modifiedBy = "user")
        assertEquals(1L, cache.benId)
    }


    @Test fun `LeprosyScreeningCache copy`() {
        val cache = LeprosyScreeningCache(benId = 1L, houseHoldDetailsId = 2L, createdBy = "u", modifiedBy = "u")
        val copy = cache.copy(benId = 55L)
        assertEquals(55L, copy.benId)
    }

    // =====================================================
    // TBScreeningCache Tests
    // =====================================================

    @Test fun `TBScreeningCache creation`() {
        val cache = TBScreeningCache(benId = 1L)
        assertEquals(1L, cache.benId)
    }

    @Test fun `TBScreeningCache default syncState`() {
        val cache = TBScreeningCache(benId = 1L)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    @Test fun `TBScreeningCache copy`() {
        val cache = TBScreeningCache(benId = 1L)
        val copy = cache.copy(benId = 33L)
        assertEquals(33L, copy.benId)
    }

    @Test fun `TBScreeningCache same key fields`() {
        val a = TBScreeningCache(benId = 1L)
        assertEquals(1L, a.benId)
        assertEquals(0, a.id)
    }

    // =====================================================
    // HRPNonPregnantAssessCache Tests
    // =====================================================

    @Test fun `HRPNonPregnantAssessCache creation`() {
        val cache = HRPNonPregnantAssessCache(benId = 1L)
        assertEquals(1L, cache.benId)
    }

    @Test fun `HRPNonPregnantAssessCache default syncState`() {
        val cache = HRPNonPregnantAssessCache(benId = 1L)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    // =====================================================
    // HRPPregnantAssessCache Tests
    // =====================================================

    @Test fun `HRPPregnantAssessCache creation`() {
        val cache = HRPPregnantAssessCache(benId = 1L)
        assertEquals(1L, cache.benId)
    }

    @Test fun `HRPPregnantAssessCache default syncState`() {
        val cache = HRPPregnantAssessCache(benId = 1L)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    // =====================================================
    // HRPMicroBirthPlanCache Tests
    // =====================================================

    @Test fun `HRPMicroBirthPlanCache creation`() {
        val cache = HRPMicroBirthPlanCache(benId = 1L)
        assertEquals(1L, cache.benId)
    }

    @Test fun `HRPMicroBirthPlanCache default syncState`() {
        val cache = HRPMicroBirthPlanCache(benId = 1L)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    // =====================================================
    // HRPNonPregnantTrackCache Tests
    // =====================================================

    @Test fun `HRPNonPregnantTrackCache creation`() {
        val cache = HRPNonPregnantTrackCache(benId = 1L)
        assertEquals(1L, cache.benId)
    }

    @Test fun `HRPNonPregnantTrackCache default syncState`() {
        val cache = HRPNonPregnantTrackCache(benId = 1L)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    // =====================================================
    // HRPPregnantTrackCache Tests
    // =====================================================

    @Test fun `HRPPregnantTrackCache creation`() {
        val cache = HRPPregnantTrackCache(benId = 1L)
        assertEquals(1L, cache.benId)
    }

    @Test fun `HRPPregnantTrackCache default syncState`() {
        val cache = HRPPregnantTrackCache(benId = 1L)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }
}
