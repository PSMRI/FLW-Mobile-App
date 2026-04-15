package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class SyncStatusTest {

    // =====================================================
    // SyncStatusCache Tests
    // =====================================================

    @Test fun `SyncStatusCache creation`() {
        val cache = SyncStatusCache(1, "BEN", SyncState.SYNCED, 10)
        assertEquals(1, cache.id)
        assertEquals("BEN", cache.name)
        assertEquals(SyncState.SYNCED, cache.syncState)
        assertEquals(10, cache.count)
    }

    @Test fun `SyncStatusCache copy changes field`() {
        val cache = SyncStatusCache(1, "BEN", SyncState.SYNCED, 10)
        val copy = cache.copy(count = 20)
        assertEquals(20, copy.count)
        assertEquals("BEN", copy.name)
    }

    @Test fun `SyncStatusCache equals for same data`() {
        val a = SyncStatusCache(1, "BEN", SyncState.SYNCED, 10)
        val b = SyncStatusCache(1, "BEN", SyncState.SYNCED, 10)
        assertEquals(a, b)
    }

    @Test fun `SyncStatusCache not equals for different data`() {
        val a = SyncStatusCache(1, "BEN", SyncState.SYNCED, 10)
        val b = SyncStatusCache(2, "HH", SyncState.UNSYNCED, 5)
        assertNotEquals(a, b)
    }

    // =====================================================
    // SyncStatusDomain Tests
    // =====================================================

    @Test fun `SyncStatusDomain creation with computed totalCount`() {
        val domain = SyncStatusDomain("BEN", 10, 5, 2)
        assertEquals(17, domain.totalCount)
    }

    @Test fun `SyncStatusDomain totalCount is zero for zeros`() {
        val domain = SyncStatusDomain("BEN", 0, 0, 0)
        assertEquals(0, domain.totalCount)
    }

    @Test fun `SyncStatusDomain copy preserves totalCount computation`() {
        val domain = SyncStatusDomain("BEN", 10, 5, 2)
        val copy = domain.copy(synced = 20)
        assertEquals(17, copy.totalCount)
    }

    // =====================================================
    // asDomainModel() Extension Tests
    // =====================================================

    @Test fun `asDomainModel returns empty for empty list`() {
        val result = emptyList<SyncStatusCache>().asDomainModel(emptyArray(), emptyArray())
        assertEquals(0, result.size)
    }

    @Test fun `asDomainModel groups by name`() {
        val list = listOf(
            SyncStatusCache(1, "BEN", SyncState.SYNCED, 10),
            SyncStatusCache(2, "BEN", SyncState.UNSYNCED, 5),
            SyncStatusCache(3, "HH", SyncState.SYNCED, 3)
        )
        val result = list.asDomainModel(arrayOf("Beneficiary", "Household"), arrayOf("BEN", "HH"))
        assertEquals(2, result.size)
    }

    @Test fun `asDomainModel maps local names`() {
        val list = listOf(SyncStatusCache(1, "BEN", SyncState.SYNCED, 10))
        val result = list.asDomainModel(arrayOf("Beneficiary"), arrayOf("BEN"))
        assertEquals("Beneficiary", result[0].name)
    }

    @Test fun `asDomainModel uses original name when not in english list`() {
        val list = listOf(SyncStatusCache(1, "UNKNOWN", SyncState.SYNCED, 10))
        val result = list.asDomainModel(arrayOf("Beneficiary"), arrayOf("BEN"))
        assertEquals("UNKNOWN", result[0].name)
    }

    @Test fun `asDomainModel sets synced count correctly`() {
        val list = listOf(
            SyncStatusCache(1, "BEN", SyncState.SYNCED, 10),
            SyncStatusCache(2, "BEN", SyncState.UNSYNCED, 5)
        )
        val result = list.asDomainModel(emptyArray(), emptyArray())
        assertEquals(10, result[0].synced)
        assertEquals(5, result[0].notSynced)
    }
}
