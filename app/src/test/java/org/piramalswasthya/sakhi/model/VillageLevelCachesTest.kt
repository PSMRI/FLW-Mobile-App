package org.piramalswasthya.sakhi.model

import org.junit.Assert.*
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class VillageLevelCachesTest {

    // =====================================================
    // AHDCache Tests
    // =====================================================

    @Test fun `AHDCache can be created with defaults`() {
        val cache = AHDCache()
        assertNotNull(cache)
        assertEquals(0, cache.id)
    }

    @Test fun `AHDCache default syncState is UNSYNCED`() {
        val cache = AHDCache()
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    @Test fun `AHDCache default fields are null`() {
        val cache = AHDCache()
        assertNull(cache.mobilizedForAHD)
        assertNull(cache.ahdPlace)
        assertNull(cache.ahdDate)
        assertNull(cache.image1)
        assertNull(cache.image2)
    }

    @Test fun `AHDCache copy works`() {
        val cache = AHDCache()
        val copy = cache.copy(mobilizedForAHD = "Yes", ahdPlace = "Village Hall")
        assertEquals("Yes", copy.mobilizedForAHD)
        assertEquals("Village Hall", copy.ahdPlace)
    }

    @Test fun `AHDCache equality`() {
        val a = AHDCache(id = 1)
        val b = AHDCache(id = 1)
        assertEquals(a, b)
    }

    @Test fun `AHDCache with synced state`() {
        val cache = AHDCache(syncState = SyncState.SYNCED)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    // =====================================================
    // DewormingCache Tests
    // =====================================================

    @Test fun `DewormingCache can be created with defaults`() {
        val cache = DewormingCache()
        assertNotNull(cache)
    }

    @Test fun `DewormingCache default syncState is UNSYNCED`() {
        val cache = DewormingCache()
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    @Test fun `DewormingCache default fields are null`() {
        val cache = DewormingCache()
        assertNull(cache.dewormingDone)
        assertNull(cache.dewormingDate)
        assertNull(cache.dewormingLocation)
        assertNull(cache.ageGroup)
    }

    @Test fun `DewormingCache copy works`() {
        val cache = DewormingCache()
        val copy = cache.copy(dewormingDone = "Yes", ageGroup = 5)
        assertEquals("Yes", copy.dewormingDone)
        assertEquals(5, copy.ageGroup)
    }

    @Test fun `DewormingCache equality`() {
        val a = DewormingCache(id = 1)
        val b = DewormingCache(id = 1)
        assertEquals(a, b)
    }

    // =====================================================
    // VHNCCache Tests
    // =====================================================

    @Test fun `VHNCCache can be created`() {
        val cache = VHNCCache(id = 0, vhncDate = "2025-01-01")
        assertNotNull(cache)
    }

    @Test fun `VHNCCache has correct date`() {
        val cache = VHNCCache(id = 0, vhncDate = "2025-03-15")
        assertEquals("2025-03-15", cache.vhncDate)
    }

    @Test fun `VHNCCache default optional fields are null`() {
        val cache = VHNCCache(id = 0, vhncDate = "2025-01-01")
        assertNull(cache.place)
        assertNull(cache.noOfBeneficiariesAttended)
        assertNull(cache.image1)
    }

    @Test fun `VHNCCache copy works`() {
        val cache = VHNCCache(id = 0, vhncDate = "2025-01-01")
        val copy = cache.copy(place = "PHC", noOfBeneficiariesAttended = 50)
        assertEquals("PHC", copy.place)
        assertEquals(50, copy.noOfBeneficiariesAttended)
    }

    @Test fun `VHNCCache equality`() {
        val a = VHNCCache(id = 1, vhncDate = "2025-01-01")
        val b = VHNCCache(id = 1, vhncDate = "2025-01-01")
        assertEquals(a, b)
    }

    // =====================================================
    // VHNDCache Tests
    // =====================================================

    @Test fun `VHNDCache can be created`() {
        val cache = VHNDCache(id = 0, vhndDate = "2025-01-01")
        assertNotNull(cache)
    }

    @Test fun `VHNDCache has correct date`() {
        val cache = VHNDCache(id = 0, vhndDate = "2025-04-01")
        assertEquals("2025-04-01", cache.vhndDate)
    }

    @Test fun `VHNDCache default place is null`() {
        val cache = VHNDCache(id = 0, vhndDate = "2025-01-01")
        assertNull(cache.place)
    }

    @Test fun `VHNDCache copy works`() {
        val cache = VHNDCache(id = 0, vhndDate = "2025-01-01")
        val copy = cache.copy(place = "AWC", noOfBeneficiariesAttended = 30)
        assertEquals("AWC", copy.place)
        assertEquals(30, copy.noOfBeneficiariesAttended)
    }

    @Test fun `VHNDCache equality`() {
        val a = VHNDCache(id = 1, vhndDate = "2025-01-01")
        val b = VHNDCache(id = 1, vhndDate = "2025-01-01")
        assertEquals(a, b)
    }

    // =====================================================
    // PHCReviewMeetingCache Tests
    // =====================================================

    @Test fun `PHCReviewMeetingCache can be created`() {
        val cache = PHCReviewMeetingCache(id = 0, phcReviewDate = "2025-01-15")
        assertNotNull(cache)
    }

    @Test fun `PHCReviewMeetingCache has correct date`() {
        val cache = PHCReviewMeetingCache(id = 0, phcReviewDate = "2025-02-20")
        assertEquals("2025-02-20", cache.phcReviewDate)
    }

    @Test fun `PHCReviewMeetingCache default placeId is 0`() {
        val cache = PHCReviewMeetingCache(id = 0, phcReviewDate = "2025-01-15")
        assertEquals(0, cache.placeId)
    }

    @Test fun `PHCReviewMeetingCache copy works`() {
        val cache = PHCReviewMeetingCache(id = 0, phcReviewDate = "2025-01-15")
        val copy = cache.copy(place = "PHC", villageName = "TestVillage")
        assertEquals("PHC", copy.place)
        assertEquals("TestVillage", copy.villageName)
    }

    @Test fun `PHCReviewMeetingCache equality`() {
        val a = PHCReviewMeetingCache(id = 1, phcReviewDate = "2025-01-15")
        val b = PHCReviewMeetingCache(id = 1, phcReviewDate = "2025-01-15")
        assertEquals(a, b)
    }

    // =====================================================
    // ORSCampaignCache Tests
    // =====================================================

    @Test fun `ORSCampaignCache can be created with defaults`() {
        val cache = ORSCampaignCache()
        assertNotNull(cache)
    }

    @Test fun `ORSCampaignCache default syncState is UNSYNCED`() {
        val cache = ORSCampaignCache()
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    @Test fun `ORSCampaignCache default formDataJson is null`() {
        val cache = ORSCampaignCache()
        assertNull(cache.formDataJson)
    }

    @Test fun `ORSCampaignCache copy works`() {
        val cache = ORSCampaignCache()
        val copy = cache.copy(formDataJson = "{\"key\":\"value\"}")
        assertEquals("{\"key\":\"value\"}", copy.formDataJson)
    }

    // =====================================================
    // PulsePolioCampaignCache Tests
    // =====================================================

    @Test fun `PulsePolioCampaignCache can be created with defaults`() {
        val cache = PulsePolioCampaignCache()
        assertNotNull(cache)
    }

    @Test fun `PulsePolioCampaignCache default syncState is UNSYNCED`() {
        val cache = PulsePolioCampaignCache()
        assertEquals(SyncState.UNSYNCED, cache.syncState)
    }

    @Test fun `PulsePolioCampaignCache default formDataJson is null`() {
        val cache = PulsePolioCampaignCache()
        assertNull(cache.formDataJson)
    }

    @Test fun `PulsePolioCampaignCache copy works`() {
        val cache = PulsePolioCampaignCache()
        val copy = cache.copy(formDataJson = "{}")
        assertEquals("{}", copy.formDataJson)
    }

    @Test fun `PulsePolioCampaignCache equality`() {
        val a = PulsePolioCampaignCache(id = 1)
        val b = PulsePolioCampaignCache(id = 1)
        assertEquals(a, b)
    }

    // =====================================================
    // SaasBahuSammelanCache Tests
    // =====================================================

    @Test fun `SaasBahuSammelanCache can be created`() {
        val cache = SaasBahuSammelanCache(ashaId = 1)
        assertNotNull(cache)
    }

    @Test fun `SaasBahuSammelanCache default place is null`() {
        val cache = SaasBahuSammelanCache(ashaId = 1)
        assertNull(cache.place)
    }

    @Test fun `SaasBahuSammelanCache default participants is 0`() {
        val cache = SaasBahuSammelanCache(ashaId = 1)
        assertEquals(0, cache.participants)
    }

    @Test fun `SaasBahuSammelanCache copy works`() {
        val cache = SaasBahuSammelanCache(ashaId = 1)
        val copy = cache.copy(place = "Hall", participants = 25)
        assertEquals("Hall", copy.place)
        assertEquals(25, copy.participants)
    }

    @Test fun `SaasBahuSammelanCache equality`() {
        val a = SaasBahuSammelanCache(id = 1, ashaId = 1)
        val b = SaasBahuSammelanCache(id = 1, ashaId = 1)
        assertEquals(a, b)
    }
}
