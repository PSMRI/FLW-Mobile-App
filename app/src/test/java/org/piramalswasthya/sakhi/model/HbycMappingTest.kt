package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Tests for the pure mapper function in HBYC.kt:
 *  - HbycPost.toCache(houseoldId)
 *
 * Skipped: HBYCCache.asPostModel(...) (requires User + HouseholdCache with a
 * nested locationRecord graph and BenRegCache).
 */
class HbycMappingTest {

    @Test
    fun `HbycPost toCache maps core fields and sets synced`() {
        val post = HbycPost(
            beneficiaryid = 21L,
            byHeightLenght = 3,
            childVaccinatedByAge = 5,
            childWasIll = 1,
            createdBy = "creator",
            deathCause = "cause",
            delayConstraint = 0,
            ifYesReferHospital = 1,
            ifYesThenHealth = 0,
            ironicFolicAcidSyurp = 2,
            markAgeInMonth = 4,
            month = 6,
            numberOfChildrenWeiingLess = 1,
            ors = 3,
            orsInLastMonth = 0,
            placeOfDeath = 2,
            primaryHealthCenter = "PHC",
            qmAnmWasInformed = 1,
            subCenterName = "SC",
            supperVisionFromBlock = 0,
            supplementStarted = 1,
            supplimentGiven = 1,
            totalNumberChildVillage = 10,
            weightAccordingToChildAge = 1,
            year = "2024"
        )

        val cache = post.toCache(houseoldId = 500L)

        assertEquals(21L, cache.benId)
        assertEquals(500L, cache.hhId)
        assertEquals(3, cache.byHeightLength)
        assertEquals("5", cache.hbycAgeCategory)
        assertEquals(1, cache.wasIll)
        assertEquals("creator", cache.createdBy)
        assertEquals("cause", cache.deathCause)
        assertEquals(1, cache.referred)
        assertEquals(2, cache.ironFolicAcidGiven)
        assertEquals("4 months", cache.deathAge)
        assertEquals("6", cache.month)
        assertEquals(3, cache.orsPacketDelivered)
        assertEquals(2, cache.deathPlace)
        assertEquals("PHC", cache.primaryHealthCenterName)
        assertEquals("SC", cache.subcenterName)
        assertEquals("10", cache.infantPopulation)
        assertEquals("2024", cache.year)
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test
    fun `HbycPost toCache defaults null nullable ints to zero`() {
        val post = HbycPost(
            beneficiaryid = 1L,
            createdBy = "c"
        )

        val cache = post.toCache(houseoldId = 1L)

        assertEquals(0, cache.byHeightLength)
        assertEquals(0, cache.wasIll)
        assertEquals(0, cache.referred)
        assertEquals(0, cache.ironFolicAcidGiven)
        assertEquals(0, cache.orsPacketDelivered)
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }
}
