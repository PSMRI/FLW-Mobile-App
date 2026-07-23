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

    // =====================================================
    // asNetworkModel Mapping Tests (merged from EligibleCoupleTrackingMappingTest)
    // =====================================================

    private fun cache() = EligibleCoupleTrackingCache(
        benId = 42L,
        createdBy = "creator",
        updatedBy = "modifier",
        syncState = SyncState.UNSYNCED
    )

    @Test fun `asNetworkModel maps benId`() {
        assertEquals(42L, cache().asNetworkModel().benId)
    }

    @Test fun `asNetworkModel maps createdBy and updatedBy`() {
        val net = cache().asNetworkModel()
        assertEquals("creator", net.createdBy)
        assertEquals("modifier", net.updatedBy)
    }

    @Test fun `asNetworkModel default isActive is true`() {
        assertEquals(true, cache().asNetworkModel().isActive)
    }

    @Test fun `asNetworkModel lmp_date equals benId by design`() {
        // asNetworkModel sets lmp_date = benId
        assertEquals(42L, cache().asNetworkModel().lmp_date)
    }

    @Test fun `asNetworkModel formats zero lmpDate to date string`() {
        val net = cache().asNetworkModel()
        assertNotNull(net.lmpDate)
    }

    @Test fun `asNetworkModel formats visitDate as datetime`() {
        val net = cache().asNetworkModel()
        assertTrue(net.visitDate.contains("T"))
    }

    @Test fun `asNetworkModel null dateOfAntraInjection stays null`() {
        val net = cache().copy(dateOfAntraInjection = null).asNetworkModel()
        assertNull(net.dateOfAntraInjection)
    }

    @Test fun `asNetworkModel parses dateOfAntraInjection when present`() {
        val net = cache().copy(dateOfAntraInjection = "01-01-2023").asNetworkModel()
        assertNotNull(net.dateOfAntraInjection)
        assertTrue(net.dateOfAntraInjection!!.contains("T"))
    }

    @Test fun `asNetworkModel passes through pregnancy fields`() {
        val net = cache().copy(
            isPregnant = "Yes",
            pregnancyTestResult = "Positive",
            methodOfContraception = "Antra"
        ).asNetworkModel()
        assertEquals("Yes", net.isPregnant)
        assertEquals("Positive", net.pregnancyTestResult)
        assertEquals("Antra", net.methodOfContraception)
    }

    private fun cache2() = EligibleCoupleTrackingCache(
        benId = 55L,
        createdBy = "creator",
        updatedBy = "modifier",
        syncState = SyncState.UNSYNCED
    )

    @Test fun `not-pregnant negative result`() {
        val net = cache2().copy(
            isPregnant = "No",
            pregnancyTestResult = "Negative",
            methodOfContraception = null
        ).asNetworkModel()
        assertEquals("No", net.isPregnant)
        assertEquals("Negative", net.pregnancyTestResult)
        assertNull(net.methodOfContraception)
    }

    @Test fun `inactive flag maps through`() {
        val net = cache2().copy(isActive = false).asNetworkModel()
        assertEquals(false, net.isActive)
    }

    @Test fun `alternate antra date format parses`() {
        val net = cache2().copy(dateOfAntraInjection = "15-08-2022").asNetworkModel()
        assertEquals(true, net.dateOfAntraInjection?.contains("T"))
    }

    @Test fun `condom contraception passes through`() {
        val net = cache2().copy(
            isPregnant = "No",
            methodOfContraception = "Condom"
        ).asNetworkModel()
        assertEquals("Condom", net.methodOfContraception)
    }
}
