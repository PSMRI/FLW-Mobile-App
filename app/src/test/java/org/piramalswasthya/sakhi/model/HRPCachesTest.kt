package org.piramalswasthya.sakhi.model

import org.junit.Assert.*
import org.junit.Test

class HRPCachesTest {

    // =====================================================
    // HRPMicroBirthPlanCache Tests
    // =====================================================

    @Test fun `HRPMicroBirthPlanCache can be created`() {
        val cache = HRPMicroBirthPlanCache(benId = 1L)
        assertNotNull(cache)
    }

    @Test fun `HRPMicroBirthPlanCache default id is 0`() {
        val cache = HRPMicroBirthPlanCache(benId = 1L)
        assertEquals(0, cache.id)
    }

    @Test fun `HRPMicroBirthPlanCache default nearestSc is null`() {
        val cache = HRPMicroBirthPlanCache(benId = 1L)
        assertNull(cache.nearestSc)
    }

    @Test fun `HRPMicroBirthPlanCache default bloodGroup is null`() {
        val cache = HRPMicroBirthPlanCache(benId = 1L)
        assertNull(cache.bloodGroup)
    }

    @Test fun `HRPMicroBirthPlanCache copy works`() {
        val cache = HRPMicroBirthPlanCache(benId = 1L)
        val copy = cache.copy(bloodGroup = "O+", nearestSc = "SC1")
        assertEquals("O+", copy.bloodGroup)
        assertEquals("SC1", copy.nearestSc)
    }

    @Test fun `HRPMicroBirthPlanCache equality`() {
        val a = HRPMicroBirthPlanCache(id = 1, benId = 1L)
        val b = HRPMicroBirthPlanCache(id = 1, benId = 1L)
        assertEquals(a, b)
    }

    @Test fun `HRPMicroBirthPlanCache with contact numbers`() {
        val cache = HRPMicroBirthPlanCache(benId = 1L, contactNumber1 = "9876543210", contactNumber2 = "9876543211")
        assertEquals("9876543210", cache.contactNumber1)
        assertEquals("9876543211", cache.contactNumber2)
    }

    // =====================================================
    // HRPNonPregnantAssessCache Tests
    // =====================================================

    @Test fun `HRPNonPregnantAssessCache can be created`() {
        val cache = HRPNonPregnantAssessCache(benId = 1L)
        assertNotNull(cache)
    }

    @Test fun `HRPNonPregnantAssessCache default noOfDeliveries is null`() {
        val cache = HRPNonPregnantAssessCache(benId = 1L)
        assertNull(cache.noOfDeliveries)
    }

    @Test fun `HRPNonPregnantAssessCache copy works`() {
        val cache = HRPNonPregnantAssessCache(benId = 1L)
        val copy = cache.copy(noOfDeliveries = "3", heightShort = "Yes")
        assertEquals("3", copy.noOfDeliveries)
        assertEquals("Yes", copy.heightShort)
    }

    @Test fun `HRPNonPregnantAssessCache same key fields match`() {
        val a = HRPNonPregnantAssessCache(id = 1, benId = 1L)
        val b = HRPNonPregnantAssessCache(id = 1, benId = 1L)
        assertEquals(a.id, b.id)
        assertEquals(a.benId, b.benId)
    }

    @Test fun `HRPNonPregnantAssessCache with all fields`() {
        val cache = HRPNonPregnantAssessCache(
            benId = 1L, noOfDeliveries = "2", timeLessThan18m = "No",
            heightShort = "No", age = "25", misCarriage = "No"
        )
        assertEquals("2", cache.noOfDeliveries)
        assertEquals("No", cache.timeLessThan18m)
        assertEquals("25", cache.age)
    }

    // =====================================================
    // HRPNonPregnantTrackCache Tests
    // =====================================================

    @Test fun `HRPNonPregnantTrackCache can be created`() {
        val cache = HRPNonPregnantTrackCache(benId = 1L)
        assertNotNull(cache)
    }

    @Test fun `HRPNonPregnantTrackCache default anemia is null`() {
        val cache = HRPNonPregnantTrackCache(benId = 1L)
        assertNull(cache.anemia)
    }

    @Test fun `HRPNonPregnantTrackCache copy works`() {
        val cache = HRPNonPregnantTrackCache(benId = 1L)
        val copy = cache.copy(anemia = "Mild", systolic = 120, diastolic = 80)
        assertEquals("Mild", copy.anemia)
        assertEquals(120, copy.systolic)
        assertEquals(80, copy.diastolic)
    }

    @Test fun `HRPNonPregnantTrackCache equality`() {
        val a = HRPNonPregnantTrackCache(id = 1, benId = 1L)
        val b = HRPNonPregnantTrackCache(id = 1, benId = 1L)
        assertEquals(a, b)
    }

    // =====================================================
    // HRPPregnantAssessCache Tests
    // =====================================================

    @Test fun `HRPPregnantAssessCache can be created`() {
        val cache = HRPPregnantAssessCache(benId = 1L)
        assertNotNull(cache)
    }

    @Test fun `HRPPregnantAssessCache default id is 0`() {
        val cache = HRPPregnantAssessCache(benId = 1L)
        assertEquals(0, cache.id)
    }

    @Test fun `HRPPregnantAssessCache default rhNegative is null`() {
        val cache = HRPPregnantAssessCache(benId = 1L)
        assertNull(cache.rhNegative)
    }

    @Test fun `HRPPregnantAssessCache copy works`() {
        val cache = HRPPregnantAssessCache(benId = 1L)
        val copy = cache.copy(noOfDeliveries = "1", rhNegative = "No")
        assertEquals("1", copy.noOfDeliveries)
        assertEquals("No", copy.rhNegative)
    }

    @Test fun `HRPPregnantAssessCache same benId and id`() {
        val a = HRPPregnantAssessCache(id = 1, benId = 1L)
        val b = HRPPregnantAssessCache(id = 1, benId = 1L)
        assertEquals(a.id, b.id)
        assertEquals(a.benId, b.benId)
    }

    // =====================================================
    // HRPPregnantTrackCache Tests
    // =====================================================

    @Test fun `HRPPregnantTrackCache can be created`() {
        val cache = HRPPregnantTrackCache(benId = 1L)
        assertNotNull(cache)
    }

    @Test fun `HRPPregnantTrackCache default visitDate is null`() {
        val cache = HRPPregnantTrackCache(benId = 1L)
        assertNull(cache.visitDate)
    }

    @Test fun `HRPPregnantTrackCache default severeAnemia is null`() {
        val cache = HRPPregnantTrackCache(benId = 1L)
        assertNull(cache.severeAnemia)
    }

    @Test fun `HRPPregnantTrackCache copy works`() {
        val cache = HRPPregnantTrackCache(benId = 1L)
        val copy = cache.copy(rdPmsa = "Yes", rdDengue = "No", rdFilaria = "No")
        assertEquals("Yes", copy.rdPmsa)
        assertEquals("No", copy.rdDengue)
        assertEquals("No", copy.rdFilaria)
    }

    @Test fun `HRPPregnantTrackCache equality`() {
        val a = HRPPregnantTrackCache(id = 1, benId = 1L)
        val b = HRPPregnantTrackCache(id = 1, benId = 1L)
        assertEquals(a, b)
    }

    @Test fun `HRPPregnantTrackCache inequality on different benId`() {
        val a = HRPPregnantTrackCache(benId = 1L)
        val b = HRPPregnantTrackCache(benId = 2L)
        assertNotEquals(a, b)
    }
}
