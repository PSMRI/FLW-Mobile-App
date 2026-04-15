package org.piramalswasthya.sakhi.model

import org.junit.Assert.*
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class DiseaseControlCachesTest {

    // =====================================================
    // AESScreeningCache Tests
    // =====================================================

    @Test fun `AESScreeningCache can be created with required params`() {
        val cache = AESScreeningCache(benId = 1L, houseHoldDetailsId = 100L)
        assertNotNull(cache)
        assertEquals(1L, cache.benId)
        assertEquals(100L, cache.houseHoldDetailsId)
    }

    @Test fun `AESScreeningCache default id is 0`() {
        val cache = AESScreeningCache(benId = 1L, houseHoldDetailsId = 100L)
        assertEquals(0, cache.id)
    }

    @Test fun `AESScreeningCache default beneficiaryStatus is null`() {
        val cache = AESScreeningCache(benId = 1L, houseHoldDetailsId = 100L)
        assertNull(cache.beneficiaryStatus)
    }

    @Test fun `AESScreeningCache copy works`() {
        val cache = AESScreeningCache(benId = 1L, houseHoldDetailsId = 100L)
        val copy = cache.copy(beneficiaryStatus = "Active")
        assertEquals("Active", copy.beneficiaryStatus)
        assertEquals(1L, copy.benId)
    }

    @Test fun `AESScreeningCache same key fields match`() {
        val ts = 1000L
        val a = AESScreeningCache(id = 1, benId = 1L, houseHoldDetailsId = 100L, visitDate = ts)
        val b = AESScreeningCache(id = 1, benId = 1L, houseHoldDetailsId = 100L, visitDate = ts)
        assertEquals(a.id, b.id)
        assertEquals(a.benId, b.benId)
        assertEquals(a.houseHoldDetailsId, b.houseHoldDetailsId)
    }

    @Test fun `AESScreeningCache inequality on different benId`() {
        val a = AESScreeningCache(benId = 1L, houseHoldDetailsId = 100L)
        val b = AESScreeningCache(benId = 2L, houseHoldDetailsId = 100L)
        assertNotEquals(a, b)
    }

    // =====================================================
    // FilariaScreeningCache Tests
    // =====================================================

    @Test fun `FilariaScreeningCache can be created`() {
        val cache = FilariaScreeningCache(benId = 1L, houseHoldDetailsId = 100L)
        assertNotNull(cache)
    }

    @Test fun `FilariaScreeningCache default sufferingFromFilariasis is false`() {
        val cache = FilariaScreeningCache(benId = 1L, houseHoldDetailsId = 100L)
        assertEquals(false, cache.sufferingFromFilariasis)
    }

    @Test fun `FilariaScreeningCache copy works`() {
        val cache = FilariaScreeningCache(benId = 1L, houseHoldDetailsId = 100L)
        val copy = cache.copy(sufferingFromFilariasis = true)
        assertEquals(true, copy.sufferingFromFilariasis)
    }

    @Test fun `FilariaScreeningCache same key fields match`() {
        val a = FilariaScreeningCache(id = 1, benId = 1L, houseHoldDetailsId = 100L)
        val b = FilariaScreeningCache(id = 1, benId = 1L, houseHoldDetailsId = 100L)
        assertEquals(a.id, b.id)
        assertEquals(a.benId, b.benId)
    }

    // =====================================================
    // KalaAzarScreeningCache Tests
    // =====================================================

    @Test fun `KalaAzarScreeningCache can be created`() {
        val cache = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 100L)
        assertNotNull(cache)
    }

    @Test fun `KalaAzarScreeningCache default beneficiaryStatusId is 0`() {
        val cache = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 100L)
        assertEquals(0, cache.beneficiaryStatusId)
    }

    @Test fun `KalaAzarScreeningCache copy works`() {
        val cache = KalaAzarScreeningCache(benId = 1L, houseHoldDetailsId = 100L)
        val copy = cache.copy(beneficiaryStatus = "Confirmed")
        assertEquals("Confirmed", copy.beneficiaryStatus)
    }

    @Test fun `KalaAzarScreeningCache same key fields match`() {
        val a = KalaAzarScreeningCache(id = 1, benId = 1L, houseHoldDetailsId = 100L)
        val b = KalaAzarScreeningCache(id = 1, benId = 1L, houseHoldDetailsId = 100L)
        assertEquals(a.id, b.id)
        assertEquals(a.benId, b.benId)
    }

    // =====================================================
    // LeprosyScreeningCache Tests
    // =====================================================

    @Test fun `LeprosyScreeningCache can be created`() {
        val cache = LeprosyScreeningCache(benId = 1L, houseHoldDetailsId = 100L, createdBy = "test", modifiedBy = "test")
        assertNotNull(cache)
    }

    @Test fun `LeprosyScreeningCache default id is 0`() {
        val cache = LeprosyScreeningCache(benId = 1L, houseHoldDetailsId = 100L, createdBy = "test", modifiedBy = "test")
        assertEquals(0, cache.id)
    }

    @Test fun `LeprosyScreeningCache copy works`() {
        val cache = LeprosyScreeningCache(benId = 1L, houseHoldDetailsId = 100L, createdBy = "test", modifiedBy = "test")
        val copy = cache.copy(benId = 2L)
        assertEquals(2L, copy.benId)
    }

    // =====================================================
    // MalariaScreeningCache Tests
    // =====================================================

    @Test fun `MalariaScreeningCache can be created`() {
        val cache = MalariaScreeningCache(benId = 1L, visitId = 10L, houseHoldDetailsId = 100L)
        assertNotNull(cache)
    }

    @Test fun `MalariaScreeningCache has correct benId`() {
        val cache = MalariaScreeningCache(benId = 42L, visitId = 10L, houseHoldDetailsId = 100L)
        assertEquals(42L, cache.benId)
    }

    @Test fun `MalariaScreeningCache copy works`() {
        val cache = MalariaScreeningCache(benId = 1L, visitId = 10L, houseHoldDetailsId = 100L)
        val copy = cache.copy(visitId = 20L)
        assertEquals(20L, copy.visitId)
    }

    @Test fun `MalariaScreeningCache same key fields match`() {
        val a = MalariaScreeningCache(id = 1, benId = 1L, visitId = 10L, houseHoldDetailsId = 100L)
        val b = MalariaScreeningCache(id = 1, benId = 1L, visitId = 10L, houseHoldDetailsId = 100L)
        assertEquals(a.id, b.id)
        assertEquals(a.benId, b.benId)
        assertEquals(a.visitId, b.visitId)
    }

    // =====================================================
    // MalariaConfirmedCasesCache Tests
    // =====================================================

    @Test fun `MalariaConfirmedCasesCache can be created`() {
        val cache = MalariaConfirmedCasesCache(benId = 1L, houseHoldDetailsId = 100L)
        assertNotNull(cache)
    }

    @Test fun `MalariaConfirmedCasesCache default diseaseId is 0`() {
        val cache = MalariaConfirmedCasesCache(benId = 1L, houseHoldDetailsId = 100L)
        assertEquals(0, cache.diseaseId)
    }

    @Test fun `MalariaConfirmedCasesCache copy works`() {
        val cache = MalariaConfirmedCasesCache(benId = 1L, houseHoldDetailsId = 100L)
        val copy = cache.copy(diseaseId = 5)
        assertEquals(5, copy.diseaseId)
    }

    // =====================================================
    // TBScreeningCache Tests
    // =====================================================

    @Test fun `TBScreeningCache can be created`() {
        val cache = TBScreeningCache(benId = 1L)
        assertNotNull(cache)
    }

    @Test fun `TBScreeningCache default coughMoreThan2Weeks is null`() {
        val cache = TBScreeningCache(benId = 1L)
        assertNull(cache.coughMoreThan2Weeks)
    }

    @Test fun `TBScreeningCache copy works`() {
        val cache = TBScreeningCache(benId = 1L)
        val copy = cache.copy(coughMoreThan2Weeks = true, bloodInSputum = false)
        assertEquals(true, copy.coughMoreThan2Weeks)
        assertEquals(false, copy.bloodInSputum)
    }

    @Test fun `TBScreeningCache same key fields match`() {
        val a = TBScreeningCache(id = 1, benId = 1L)
        val b = TBScreeningCache(id = 1, benId = 1L)
        assertEquals(a.id, b.id)
        assertEquals(a.benId, b.benId)
    }

    // =====================================================
    // TBSuspectedCache Tests
    // =====================================================

    @Test fun `TBSuspectedCache can be created`() {
        val cache = TBSuspectedCache(benId = 1L)
        assertNotNull(cache)
    }

    @Test fun `TBSuspectedCache default hasSymptoms is false`() {
        val cache = TBSuspectedCache(benId = 1L)
        assertFalse(cache.hasSymptoms)
    }

    @Test fun `TBSuspectedCache copy works`() {
        val cache = TBSuspectedCache(benId = 1L)
        val copy = cache.copy(typeOfTBCase = "Pulmonary")
        assertEquals("Pulmonary", copy.typeOfTBCase)
    }

    // =====================================================
    // TBConfirmedTreatmentCache Tests
    // =====================================================

    @Test fun `TBConfirmedTreatmentCache can be created`() {
        val cache = TBConfirmedTreatmentCache(benId = 1L)
        assertNotNull(cache)
    }

    @Test fun `TBConfirmedTreatmentCache default regimenType is null`() {
        val cache = TBConfirmedTreatmentCache(benId = 1L)
        assertNull(cache.regimenType)
    }

    @Test fun `TBConfirmedTreatmentCache copy works`() {
        val cache = TBConfirmedTreatmentCache(benId = 1L)
        val copy = cache.copy(regimenType = "DOTS")
        assertEquals("DOTS", copy.regimenType)
    }

    // =====================================================
    // IRSRoundScreening Tests
    // =====================================================

    @Test fun `IRSRoundScreening can be created with defaults`() {
        val cache = IRSRoundScreening()
        assertNotNull(cache)
        assertEquals(0, cache.id)
        assertEquals(0, cache.rounds)
        assertEquals(0L, cache.householdId)
    }

    @Test fun `IRSRoundScreening with custom values`() {
        val cache = IRSRoundScreening(rounds = 3, householdId = 42L)
        assertEquals(3, cache.rounds)
        assertEquals(42L, cache.householdId)
    }

    @Test fun `IRSRoundScreening copy works`() {
        val cache = IRSRoundScreening(rounds = 1)
        val copy = cache.copy(rounds = 2)
        assertEquals(2, copy.rounds)
    }

    @Test fun `IRSRoundScreening same key fields`() {
        val ts = 1000L
        val a = IRSRoundScreening(id = 1, rounds = 2, householdId = 3L, date = ts)
        val b = IRSRoundScreening(id = 1, rounds = 2, householdId = 3L, date = ts)
        assertEquals(a, b)
    }
}
