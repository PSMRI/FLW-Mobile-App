package org.piramalswasthya.sakhi.model

import org.junit.Assert.*
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class MaternalHealthCachesTest {

    // =====================================================
    // PregnantWomanRegistrationCache Tests
    // =====================================================

    @Test fun `PregnantWomanRegistrationCache can be created`() {
        val cache = PregnantWomanRegistrationCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertNotNull(cache)
    }

    @Test fun `PregnantWomanRegistrationCache default id is 0`() {
        val cache = PregnantWomanRegistrationCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(0L, cache.id)
    }

    @Test fun `PregnantWomanRegistrationCache default mcpCardNumber is 0`() {
        val cache = PregnantWomanRegistrationCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(0L, cache.mcpCardNumber)
    }

    @Test fun `PregnantWomanRegistrationCache copy works`() {
        val cache = PregnantWomanRegistrationCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        val copy = cache.copy(mcpCardNumber = 12345L, rchId = 67890L)
        assertEquals(12345L, copy.mcpCardNumber)
        assertEquals(67890L, copy.rchId)
    }

    @Test fun `PregnantWomanRegistrationCache same key fields match`() {
        val a = PregnantWomanRegistrationCache(id = 1, benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(1L, a.id)
        assertEquals(1L, a.benId)
        assertEquals(SyncState.UNSYNCED, a.syncState)
    }

    @Test fun `PregnantWomanRegistrationCache inequality`() {
        val a = PregnantWomanRegistrationCache(benId = 1L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        val b = PregnantWomanRegistrationCache(benId = 2L, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertNotEquals(a, b)
    }

    // =====================================================
    // PNCVisitCache Tests
    // =====================================================

    @Test fun `PNCVisitCache can be created`() {
        val cache = PNCVisitCache(benId = 1L, pncPeriod = 1, isActive = true, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertNotNull(cache)
    }

    @Test fun `PNCVisitCache has correct pncPeriod`() {
        val cache = PNCVisitCache(benId = 1L, pncPeriod = 3, isActive = true, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(3, cache.pncPeriod)
    }

    @Test fun `PNCVisitCache isActive is true`() {
        val cache = PNCVisitCache(benId = 1L, pncPeriod = 1, isActive = true, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertTrue(cache.isActive)
    }

    @Test fun `PNCVisitCache default ifaTabsGiven is 0`() {
        val cache = PNCVisitCache(benId = 1L, pncPeriod = 1, isActive = true, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(0, cache.ifaTabsGiven)
    }

    @Test fun `PNCVisitCache copy works`() {
        val cache = PNCVisitCache(benId = 1L, pncPeriod = 1, isActive = true, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        val copy = cache.copy(isActive = false, ifaTabsGiven = 30)
        assertFalse(copy.isActive)
        assertEquals(30, copy.ifaTabsGiven)
    }

    @Test fun `PNCVisitCache same key fields match`() {
        val a = PNCVisitCache(id = 1, benId = 1L, pncPeriod = 1, isActive = true, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(1L, a.id)
        assertEquals(1L, a.benId)
        assertEquals(1, a.pncPeriod)
    }

    // =====================================================
    // DeliveryOutcomeCache Tests
    // =====================================================

    @Test fun `DeliveryOutcomeCache can be created`() {
        val cache = DeliveryOutcomeCache(benId = 1L, isActive = true, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertNotNull(cache)
    }

    @Test fun `DeliveryOutcomeCache default dateOfDelivery is null`() {
        val cache = DeliveryOutcomeCache(benId = 1L, isActive = true, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertNull(cache.dateOfDelivery)
    }

    @Test fun `DeliveryOutcomeCache default placeOfDelivery is null`() {
        val cache = DeliveryOutcomeCache(benId = 1L, isActive = true, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertNull(cache.placeOfDelivery)
    }

    @Test fun `DeliveryOutcomeCache copy works`() {
        val cache = DeliveryOutcomeCache(benId = 1L, isActive = true, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        val copy = cache.copy(placeOfDelivery = "Hospital", typeOfDelivery = "Normal")
        assertEquals("Hospital", copy.placeOfDelivery)
        assertEquals("Normal", copy.typeOfDelivery)
    }

    @Test fun `DeliveryOutcomeCache same key fields match`() {
        val a = DeliveryOutcomeCache(id = 1, benId = 1L, isActive = true, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(1L, a.id)
        assertEquals(1L, a.benId)
        assertTrue(a.isActive)
    }

    // =====================================================
    // ChildRegCache Tests
    // =====================================================

    @Test fun `ChildRegCache can be created`() {
        val cache = ChildRegCache(motherBenId = 1L, syncState = SyncState.UNSYNCED)
        assertNotNull(cache)
    }

    @Test fun `ChildRegCache default babyName is null`() {
        val cache = ChildRegCache(motherBenId = 1L, syncState = SyncState.UNSYNCED)
        assertNull(cache.babyName)
    }

    @Test fun `ChildRegCache default gender is null`() {
        val cache = ChildRegCache(motherBenId = 1L, syncState = SyncState.UNSYNCED)
        assertNull(cache.gender)
    }

    @Test fun `ChildRegCache copy works`() {
        val cache = ChildRegCache(motherBenId = 1L, syncState = SyncState.UNSYNCED)
        val copy = cache.copy(babyName = "Baby", gender = "Male")
        assertEquals("Baby", copy.babyName)
        assertEquals("Male", copy.gender)
    }

    @Test fun `ChildRegCache same key fields match`() {
        val a = ChildRegCache(id = 1, motherBenId = 1L, syncState = SyncState.UNSYNCED)
        assertEquals(1, a.id)
        assertEquals(1L, a.motherBenId)
        assertEquals(SyncState.UNSYNCED, a.syncState)
    }

    // =====================================================
    // InfantRegCache Tests
    // =====================================================

    @Test fun `InfantRegCache can be created`() {
        val cache = InfantRegCache(motherBenId = 1L, isActive = true, babyIndex = 1, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertNotNull(cache)
    }

    @Test fun `InfantRegCache default childBenId is 0`() {
        val cache = InfantRegCache(motherBenId = 1L, isActive = true, babyIndex = 1, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(0L, cache.childBenId)
    }

    @Test fun `InfantRegCache has correct babyIndex`() {
        val cache = InfantRegCache(motherBenId = 1L, isActive = true, babyIndex = 2, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(2, cache.babyIndex)
    }

    @Test fun `InfantRegCache copy works`() {
        val cache = InfantRegCache(motherBenId = 1L, isActive = true, babyIndex = 1, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        val copy = cache.copy(babyName = "Infant", childBenId = 42L)
        assertEquals("Infant", copy.babyName)
        assertEquals(42L, copy.childBenId)
    }

    @Test fun `InfantRegCache same key fields match`() {
        val a = InfantRegCache(id = 1, motherBenId = 1L, isActive = true, babyIndex = 1, createdBy = "test", updatedBy = "test", syncState = SyncState.UNSYNCED)
        assertEquals(1L, a.id)
        assertEquals(1L, a.motherBenId)
        assertEquals(1, a.babyIndex)
    }
}
