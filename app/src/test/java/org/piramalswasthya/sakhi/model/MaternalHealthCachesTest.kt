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

    @Test fun `ChildRegCache equality and hashCode with all fields set`() {
        val a = ChildRegCache(
            id = 2, motherBenId = 1L, babyName = "Baby", infantTerm = "Full", corticosteroidGiven = "Yes",
            gender = "Male", babyCriedAtBirth = true, resuscitation = false, referred = "No",
            hadBirthDefect = "No", birthDefect = null, otherDefect = null, weight = 3.2,
            breastFeedingStarted = true, opv0Dose = 1000L, bcgDose = 2000L, hepBDose = 3000L,
            vitkDose = 4000L, syncState = SyncState.SYNCED
        )
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(babyName = "Other"))
        assertTrue(a.toString().contains("ChildRegCache"))

        assertNotEquals(a, a.copy(id = 999))
        assertNotEquals(a, a.copy(motherBenId = 999L))
        assertNotEquals(a, a.copy(infantTerm = "Other"))
        assertNotEquals(a, a.copy(corticosteroidGiven = "Other"))
        assertNotEquals(a, a.copy(gender = "Other"))
        assertNotEquals(a, a.copy(babyCriedAtBirth = false))
        assertNotEquals(a, a.copy(resuscitation = true))
        assertNotEquals(a, a.copy(referred = "Other"))
        assertNotEquals(a, a.copy(hadBirthDefect = "Other"))
        assertNotEquals(a, a.copy(birthDefect = "Other"))
        assertNotEquals(a, a.copy(otherDefect = "Other"))
        assertNotEquals(a, a.copy(weight = 9.9))
        assertNotEquals(a, a.copy(breastFeedingStarted = false))
        assertNotEquals(a, a.copy(opv0Dose = 9999L))
        assertNotEquals(a, a.copy(bcgDose = 9999L))
        assertNotEquals(a, a.copy(hepBDose = 9999L))
        assertNotEquals(a, a.copy(vitkDose = 9999L))
        assertNotEquals(a, a.copy(syncState = SyncState.UNSYNCED))
    }

    @Test fun `ChildRegCache getters and setters mutate and read back`() {
        val cache = ChildRegCache(motherBenId = 1L, syncState = SyncState.UNSYNCED)

        cache.babyName = "Baby"
        assertEquals("Baby", cache.babyName)
        cache.infantTerm = "Full"
        assertEquals("Full", cache.infantTerm)
        cache.corticosteroidGiven = "Yes"
        assertEquals("Yes", cache.corticosteroidGiven)
        cache.gender = "Male"
        assertEquals("Male", cache.gender)
        cache.babyCriedAtBirth = true
        assertEquals(true, cache.babyCriedAtBirth)
        cache.resuscitation = false
        assertEquals(false, cache.resuscitation)
        cache.referred = "No"
        assertEquals("No", cache.referred)
        cache.hadBirthDefect = "No"
        assertEquals("No", cache.hadBirthDefect)
        cache.birthDefect = "None"
        assertEquals("None", cache.birthDefect)
        cache.otherDefect = "None"
        assertEquals("None", cache.otherDefect)
        cache.weight = 3.5
        assertEquals(3.5, cache.weight)
        cache.breastFeedingStarted = true
        assertEquals(true, cache.breastFeedingStarted)
        cache.opv0Dose = 1111L
        assertEquals(1111L, cache.opv0Dose)
        cache.bcgDose = 2222L
        assertEquals(2222L, cache.bcgDose)
        cache.hepBDose = 3333L
        assertEquals(3333L, cache.hepBDose)
        cache.vitkDose = 4444L
        assertEquals(4444L, cache.vitkDose)
        cache.syncState = SyncState.SYNCED
        assertEquals(SyncState.SYNCED, cache.syncState)
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

    @Test fun `InfantRegCache equality and hashCode with all fields set`() {
        val a = InfantRegCache(
            id = 2, childBenId = 3L, motherBenId = 1L, isActive = true, babyName = "Infant",
            babyIndex = 1, infantTerm = "Full", corticosteroidGiven = "Yes", gender = Gender.MALE,
            babyCriedAtBirth = true, resuscitation = false, referred = "No", hadBirthDefect = "No",
            birthDefect = null, isSNCU = "Yes", deliveryDischargeSummary1 = "d1",
            deliveryDischargeSummary2 = "d2", deliveryDischargeSummary3 = "d3",
            deliveryDischargeSummary4 = "d4", otherDefect = null, weight = 3.2,
            breastFeedingStarted = true, opv0Dose = 1000L, bcgDose = 2000L, hepBDose = 3000L,
            vitkDose = 4000L, processed = "N", createdBy = "test", createdDate = 5000L,
            updatedBy = "test", updatedDate = 6000L, syncState = SyncState.SYNCED
        )
        val b = a.copy()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertTrue(a.toString().contains("InfantRegCache"))

        assertNotEquals(a, a.copy(id = 999))
        assertNotEquals(a, a.copy(childBenId = 999L))
        assertNotEquals(a, a.copy(motherBenId = 999L))
        assertNotEquals(a, a.copy(isActive = false))
        assertNotEquals(a, a.copy(babyName = "Other"))
        assertNotEquals(a, a.copy(babyIndex = 999))
        assertNotEquals(a, a.copy(infantTerm = "Other"))
        assertNotEquals(a, a.copy(corticosteroidGiven = "Other"))
        assertNotEquals(a, a.copy(gender = Gender.FEMALE))
        assertNotEquals(a, a.copy(babyCriedAtBirth = false))
        assertNotEquals(a, a.copy(resuscitation = true))
        assertNotEquals(a, a.copy(referred = "Other"))
        assertNotEquals(a, a.copy(hadBirthDefect = "Other"))
        assertNotEquals(a, a.copy(birthDefect = "Other"))
        assertNotEquals(a, a.copy(isSNCU = "No"))
        assertNotEquals(a, a.copy(deliveryDischargeSummary1 = "Other"))
        assertNotEquals(a, a.copy(deliveryDischargeSummary2 = "Other"))
        assertNotEquals(a, a.copy(deliveryDischargeSummary3 = "Other"))
        assertNotEquals(a, a.copy(deliveryDischargeSummary4 = "Other"))
        assertNotEquals(a, a.copy(otherDefect = "Other"))
        assertNotEquals(a, a.copy(weight = 9.9))
        assertNotEquals(a, a.copy(breastFeedingStarted = false))
        assertNotEquals(a, a.copy(opv0Dose = 9999L))
        assertNotEquals(a, a.copy(bcgDose = 9999L))
        assertNotEquals(a, a.copy(hepBDose = 9999L))
        assertNotEquals(a, a.copy(vitkDose = 9999L))
        assertNotEquals(a, a.copy(processed = "Y"))
        assertNotEquals(a, a.copy(createdBy = "Other"))
        assertNotEquals(a, a.copy(createdDate = 9999L))
        assertNotEquals(a, a.copy(updatedBy = "Other"))
        assertNotEquals(a, a.copy(updatedDate = 9999L))
        assertNotEquals(a, a.copy(syncState = SyncState.UNSYNCED))
    }
}
