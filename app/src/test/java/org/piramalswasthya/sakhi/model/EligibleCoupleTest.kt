package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class EligibleCoupleTest {

    // =====================================================
    // EligibleCoupleRegCache Tests
    // =====================================================

    @Test fun `EligibleCoupleRegCache creation`() {
        val cache = EligibleCoupleRegCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        assertEquals(1L, cache.benId)
    }

    @Test fun `EligibleCoupleRegCache default id is 0`() {
        val cache = EligibleCoupleRegCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        assertEquals(0, cache.id)
    }

    @Test fun `EligibleCoupleRegCache default syncState`() {
        val cache = EligibleCoupleRegCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    @Test fun `EligibleCoupleRegCache copy`() {
        val cache = EligibleCoupleRegCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        val copy = cache.copy(benId = 99L)
        assertEquals(99L, copy.benId)
    }

    @Test fun `EligibleCoupleRegCache equals`() {
        val a = EligibleCoupleRegCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        val b = EligibleCoupleRegCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        assertEquals(a, b)
    }

    @Test fun `EligibleCoupleRegCache not equals`() {
        val a = EligibleCoupleRegCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        val b = EligibleCoupleRegCache(benId = 2L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        assertNotEquals(a, b)
    }

    // =====================================================
    // EligibleCoupleTrackingCache Tests
    // =====================================================

    @Test fun `EligibleCoupleTrackingCache creation`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        assertEquals(1L, cache.benId)
    }

    @Test fun `EligibleCoupleTrackingCache default id is 0`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        assertEquals(0, cache.id)
    }

    @Test fun `EligibleCoupleTrackingCache default syncState`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    @Test fun `EligibleCoupleTrackingCache copy`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        val copy = cache.copy(benId = 50L)
        assertEquals(50L, copy.benId)
    }

    @Test fun `EligibleCoupleTrackingCache equals`() {
        val a = EligibleCoupleTrackingCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        val b = EligibleCoupleTrackingCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        assertEquals(a, b)
    }

    @Test fun `EligibleCoupleTrackingCache not equals`() {
        val a = EligibleCoupleTrackingCache(benId = 1L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        val b = EligibleCoupleTrackingCache(benId = 2L, createdBy = "u", updatedBy = "u", syncState = SyncState.UNSYNCED)
        assertNotEquals(a, b)
    }

    // =====================================================
    // AdolescentHealthCache Tests
    // =====================================================

    @Test fun `AdolescentHealthCache creation`() {
        val cache = AdolescentHealthCache(benId = 1L)
        assertEquals(1L, cache.benId)
    }

    @Test fun `AdolescentHealthCache default syncState`() {
        val cache = AdolescentHealthCache(benId = 1L)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    @Test fun `AdolescentHealthCache copy`() {
        val cache = AdolescentHealthCache(benId = 1L)
        val copy = cache.copy(benId = 42L)
        assertEquals(42L, copy.benId)
    }

    @Test fun `AdolescentHealthCache equals`() {
        val a = AdolescentHealthCache(benId = 1L)
        val b = AdolescentHealthCache(benId = 1L)
        assertEquals(a, b)
    }

    // =====================================================
    // TBSuspectedCache Tests
    // =====================================================

    @Test fun `TBSuspectedCache creation`() {
        val cache = TBSuspectedCache(benId = 1L)
        assertEquals(1L, cache.benId)
    }

    @Test fun `TBSuspectedCache default syncState`() {
        val cache = TBSuspectedCache(benId = 1L)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    @Test fun `TBSuspectedCache copy`() {
        val cache = TBSuspectedCache(benId = 1L)
        val copy = cache.copy(benId = 33L)
        assertEquals(33L, copy.benId)
    }

    // =====================================================
    // TBConfirmedTreatmentCache Tests
    // =====================================================

    @Test fun `TBConfirmedTreatmentCache creation`() {
        val cache = TBConfirmedTreatmentCache(benId = 1L)
        assertEquals(1L, cache.benId)
    }

    @Test fun `TBConfirmedTreatmentCache default syncState`() {
        val cache = TBConfirmedTreatmentCache(benId = 1L)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    @Test fun `TBConfirmedTreatmentCache copy`() {
        val cache = TBConfirmedTreatmentCache(benId = 1L)
        val copy = cache.copy(benId = 77L)
        assertEquals(77L, copy.benId)
    }

    // =====================================================
    // FPOTCache Tests
    // =====================================================

    @Test fun `FPOTCache creation`() {
        val cache = FPOTCache(benId = 1L, hhId = 2L)
        assertEquals(1L, cache.benId)
        assertEquals(2L, cache.hhId)
    }

    @Test fun `FPOTCache copy`() {
        val cache = FPOTCache(benId = 1L, hhId = 2L)
        val copy = cache.copy(benId = 10L)
        assertEquals(10L, copy.benId)
    }

    // =====================================================
    // PMJAYCache Tests
    // =====================================================

    @Test fun `PMJAYCache creation`() {
        val cache = PMJAYCache(benId = 1L, hhId = 2L)
        assertEquals(1L, cache.benId)
        assertEquals(2L, cache.hhId)
    }

    @Test fun `PMJAYCache copy`() {
        val cache = PMJAYCache(benId = 1L, hhId = 2L)
        val copy = cache.copy(benId = 10L)
        assertEquals(10L, copy.benId)
    }

    // =====================================================
    // ReferalCache Tests
    // =====================================================

    @Test fun `ReferalCache creation`() {
        val cache = ReferalCache(benId = 1L, syncState = SyncState.UNSYNCED)
        assertEquals(1L, cache.benId)
    }

    @Test fun `ReferalCache default syncState`() {
        val cache = ReferalCache(benId = 1L, syncState = SyncState.UNSYNCED)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    @Test fun `ReferalCache copy`() {
        val cache = ReferalCache(benId = 1L, syncState = SyncState.UNSYNCED)
        val copy = cache.copy(benId = 88L)
        assertEquals(88L, copy.benId)
    }
}
