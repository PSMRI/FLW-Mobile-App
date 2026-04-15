package org.piramalswasthya.sakhi.model

import org.junit.Assert.*
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class UWINAndCampaignTest {

    // =====================================================
    // ORSCampaignCache Extended Tests
    // =====================================================

    @Test fun `ORSCampaignCache with json data`() {
        val cache = ORSCampaignCache(formDataJson = "{\"date\":\"2025-01-01\"}")
        assertEquals("{\"date\":\"2025-01-01\"}", cache.formDataJson)
    }

    @Test fun `ORSCampaignCache syncState transitions`() {
        val cache = ORSCampaignCache(syncState = SyncState.UNSYNCED)
        val synced = cache.copy(syncState = SyncState.SYNCED)
        assertEquals(SyncState.SYNCED, synced.syncState)
    }

    @Test fun `ORSCampaignCache id auto increments from 0`() {
        val cache = ORSCampaignCache()
        assertEquals(0, cache.id)
    }

    // =====================================================
    // PulsePolioCampaignCache Extended Tests
    // =====================================================

    @Test fun `PulsePolioCampaignCache with json data`() {
        val cache = PulsePolioCampaignCache(formDataJson = "{\"children\":5}")
        assertEquals("{\"children\":5}", cache.formDataJson)
    }

    @Test fun `PulsePolioCampaignCache syncState transitions`() {
        val cache = PulsePolioCampaignCache(syncState = SyncState.UNSYNCED)
        val synced = cache.copy(syncState = SyncState.SYNCED)
        assertEquals(SyncState.SYNCED, synced.syncState)
    }

    // =====================================================
    // SyncState Enum Tests
    // =====================================================

    @Test fun `SyncState UNSYNCED exists`() { assertNotNull(SyncState.UNSYNCED) }
    @Test fun `SyncState SYNCED exists`() { assertNotNull(SyncState.SYNCED) }
    @Test fun `SyncState SYNCING exists`() { assertNotNull(SyncState.SYNCING) }
    @Test fun `SyncState values are not empty`() { assertTrue(SyncState.values().isNotEmpty()) }
    @Test fun `SyncState valueOf UNSYNCED`() { assertEquals(SyncState.UNSYNCED, SyncState.valueOf("UNSYNCED")) }
    @Test fun `SyncState valueOf SYNCED`() { assertEquals(SyncState.SYNCED, SyncState.valueOf("SYNCED")) }
    @Test fun `SyncState valueOf SYNCING`() { assertEquals(SyncState.SYNCING, SyncState.valueOf("SYNCING")) }
    @Test fun `SyncState UNSYNCED name`() { assertEquals("UNSYNCED", SyncState.UNSYNCED.name) }
    @Test fun `SyncState SYNCED name`() { assertEquals("SYNCED", SyncState.SYNCED.name) }
    @Test fun `SyncState SYNCING name`() { assertEquals("SYNCING", SyncState.SYNCING.name) }
    @Test fun `SyncState has at least 3 values`() { assertTrue(SyncState.values().size >= 3) }
    @Test fun `SyncState all names are uppercase`() { SyncState.values().forEach { assertEquals(it.name, it.name.uppercase()) } }
    @Test fun `SyncState all names are unique`() { assertEquals(SyncState.values().size, SyncState.values().map { it.name }.distinct().size) }

    // =====================================================
    // AHDCache Extended Tests
    // =====================================================

    @Test fun `AHDCache with all fields set`() {
        val cache = AHDCache(mobilizedForAHD = "Yes", ahdPlace = "School", ahdDate = "2025-04-01", syncState = SyncState.SYNCED)
        assertEquals("Yes", cache.mobilizedForAHD)
        assertEquals("School", cache.ahdPlace)
        assertEquals("2025-04-01", cache.ahdDate)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test fun `AHDCache syncState transition`() {
        val cache = AHDCache(syncState = SyncState.UNSYNCED)
        val syncing = cache.copy(syncState = SyncState.SYNCING)
        assertEquals(SyncState.SYNCING, syncing.syncState)
    }

    // =====================================================
    // DewormingCache Extended Tests
    // =====================================================

    @Test fun `DewormingCache with all fields set`() {
        val cache = DewormingCache(dewormingDone = "Yes", dewormingDate = "2025-04-01", dewormingLocation = "PHC", ageGroup = 10)
        assertEquals("Yes", cache.dewormingDone)
        assertEquals("2025-04-01", cache.dewormingDate)
        assertEquals("PHC", cache.dewormingLocation)
        assertEquals(10, cache.ageGroup)
    }

    @Test fun `DewormingCache syncState transition`() {
        val cache = DewormingCache(syncState = SyncState.UNSYNCED)
        val synced = cache.copy(syncState = SyncState.SYNCED)
        assertEquals(SyncState.SYNCED, synced.syncState)
    }

    // =====================================================
    // IRSRoundScreening Extended Tests
    // =====================================================

    @Test fun `IRSRoundScreening toDTO works`() {
        val screening = IRSRoundScreening(rounds = 2, householdId = 42L)
        val dto = screening.toDTO()
        assertNotNull(dto)
    }

    @Test fun `IRSRoundScreening multiple rounds`() {
        val s1 = IRSRoundScreening(rounds = 1, householdId = 1L)
        val s2 = IRSRoundScreening(rounds = 2, householdId = 1L)
        val s3 = IRSRoundScreening(rounds = 3, householdId = 1L)
        assertEquals(1, s1.rounds)
        assertEquals(2, s2.rounds)
        assertEquals(3, s3.rounds)
    }

    @Test fun `IRSRoundScreening different households are not equal`() {
        val a = IRSRoundScreening(rounds = 1, householdId = 1L)
        val b = IRSRoundScreening(rounds = 1, householdId = 2L)
        assertNotEquals(a, b)
    }

    // =====================================================
    // TBScreeningCache Extended Tests
    // =====================================================

    @Test fun `TBScreeningCache with symptoms set`() {
        val cache = TBScreeningCache(benId = 1L, coughMoreThan2Weeks = true, bloodInSputum = true)
        assertTrue(cache.coughMoreThan2Weeks!!)
        assertTrue(cache.bloodInSputum!!)
    }

    @Test fun `TBScreeningCache all defaults null`() {
        val cache = TBScreeningCache(benId = 1L)
        assertNull(cache.coughMoreThan2Weeks)
        assertNull(cache.bloodInSputum)
        assertNull(cache.feverMoreThan2Weeks)
        assertNull(cache.lossOfWeight)
    }

    // =====================================================
    // TBSuspectedCache Extended Tests
    // =====================================================

    @Test fun `TBSuspectedCache with type set`() {
        val cache = TBSuspectedCache(benId = 1L, typeOfTBCase = "Pulmonary", hasSymptoms = true)
        assertEquals("Pulmonary", cache.typeOfTBCase)
        assertTrue(cache.hasSymptoms)
    }

    // =====================================================
    // HRPMicroBirthPlanCache Extended Tests
    // =====================================================

    @Test fun `HRPMicroBirthPlanCache with full data`() {
        val cache = HRPMicroBirthPlanCache(
            benId = 1L, nearestSc = "SC1", bloodGroup = "A+",
            contactNumber1 = "9999999999", contactNumber2 = "8888888888"
        )
        assertEquals("SC1", cache.nearestSc)
        assertEquals("A+", cache.bloodGroup)
        assertEquals("9999999999", cache.contactNumber1)
    }
}
