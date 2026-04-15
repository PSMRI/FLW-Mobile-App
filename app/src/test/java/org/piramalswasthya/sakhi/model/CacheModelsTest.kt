package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class CacheModelsTest {

    // =====================================================
    // AESScreeningCache Tests
    // =====================================================

    @Test fun `AESScreeningCache creation`() {
        val cache = AESScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertEquals(1L, cache.benId)
        assertEquals(2L, cache.houseHoldDetailsId)
    }

    @Test fun `AESScreeningCache default syncState is UNSYNCED`() {
        val cache = AESScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    @Test fun `AESScreeningCache syncState can be changed`() {
        val cache = AESScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        cache.syncState = SyncState.SYNCED
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    // =====================================================
    // FilariaScreeningCache Tests
    // =====================================================

    @Test fun `FilariaScreeningCache creation`() {
        val cache = FilariaScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertEquals(1L, cache.benId)
        assertEquals(2L, cache.houseHoldDetailsId)
    }

    @Test fun `FilariaScreeningCache default syncState`() {
        val cache = FilariaScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    // =====================================================
    // KalaAzarScreeningCache Tests
    // =====================================================

    @Test fun `KalaAzarScreeningCache creation`() {
        val cache = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertEquals(1L, cache.benId)
        assertEquals(2L, cache.houseHoldDetailsId)
    }

    @Test fun `KalaAzarScreeningCache default syncState`() {
        val cache = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 2L)
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    // =====================================================
    // MalariaScreeningCache Tests
    // =====================================================

    @Test fun `MalariaScreeningCache creation`() {
        val cache = MalariaScreeningCache(benId = 1L, houseHoldDetailsId = 2L, visitId = 1)
        assertEquals(1L, cache.benId)
        assertEquals(2L, cache.houseHoldDetailsId)
        assertEquals(1L, cache.visitId)
    }

    // =====================================================
    // IRSRoundScreening Tests
    // =====================================================

    @Test fun `IRSRoundScreening creation`() {
        val cache = IRSRoundScreening(id = 0, householdId = 1L)
        assertEquals(0, cache.id)
        assertEquals(1L, cache.householdId)
    }

    // =====================================================
    // SyncState Enum Tests
    // =====================================================

    @Test fun `SyncState has expected values`() { assertEquals(3, SyncState.values().size) }
    @Test fun `SyncState SYNCED exists`() { assertNotNull(SyncState.SYNCED) }
    @Test fun `SyncState UNSYNCED exists`() { assertNotNull(SyncState.UNSYNCED) }
    @Test fun `SyncState SYNCING exists`() { assertNotNull(SyncState.SYNCING) }
}
