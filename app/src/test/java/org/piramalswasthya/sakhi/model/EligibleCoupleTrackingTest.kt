package org.piramalswasthya.sakhi.model

import org.junit.Assert.*
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class EligibleCoupleTrackingTest {

    // =====================================================
    // EligibleCoupleTrackingCache Tests
    // =====================================================

    @Test fun `EligibleCoupleTrackingCache can be created`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertNotNull(cache)
    }

    @Test fun `EligibleCoupleTrackingCache default id is 0`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(0, cache.id)
    }

    @Test fun `EligibleCoupleTrackingCache default visitDate is 0`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(0L, cache.visitDate)
    }

    @Test fun `EligibleCoupleTrackingCache copy works`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        val copy = cache.copy(benId = 2L)
        assertEquals(2L, copy.benId)
    }

    @Test fun `EligibleCoupleTrackingCache same key fields match`() {
        val a = EligibleCoupleTrackingCache(id = 1, benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(1, a.id)
        assertEquals(1L, a.benId)
        assertEquals("test", a.createdBy)
    }

    @Test fun `EligibleCoupleTrackingCache inequality`() {
        val a = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        val b = EligibleCoupleTrackingCache(benId = 2L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertNotEquals(a, b)
    }

    @Test fun `EligibleCoupleTrackingCache with SYNCED state`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.SYNCED)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test fun `EligibleCoupleTrackingCache copy with updated syncState`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        val synced = cache.copy(syncState = SyncState.SYNCED)
        assertEquals(SyncState.SYNCED, synced.syncState)
    }

    @Test fun `EligibleCoupleTrackingCache with all defaults checked`() {
        val cache = EligibleCoupleTrackingCache(benId = 1L, createdBy = "c", updatedBy = "u", syncState = SyncState.UNSYNCED)
        assertEquals("c", cache.createdBy)
        assertEquals("u", cache.updatedBy)
    }

    @Test fun `EligibleCoupleTrackingCache different createdBy not equal`() {
        val a = EligibleCoupleTrackingCache(benId = 1L, createdBy = "a", updatedBy = "test", syncState = SyncState.UNSYNCED)
        val b = EligibleCoupleTrackingCache(benId = 1L, createdBy = "b", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertNotEquals(a, b)
    }
}
