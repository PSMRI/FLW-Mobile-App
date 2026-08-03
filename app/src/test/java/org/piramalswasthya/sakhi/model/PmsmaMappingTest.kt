package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Tests for the pure mapper functions in PMSMA.kt:
 *  - PMSMACache.asPostModel()
 *  - PmsmaPost.toPmsmaCache()
 */
class PmsmaMappingTest {

    // ---------------------------------------------------------------
    // PMSMACache.asPostModel()
    // ---------------------------------------------------------------

    @Test
    fun `PMSMACache asPostModel maps core fields`() {
        val cache = PMSMACache(
            id = 3L,
            benId = 88L,
            visitNumber = 2,
            isActive = true,
            haveMCPCard = true,
            husbandName = "Ravi",
            numANC = 4,
            weight = 60,
            systolicBloodPressure = "120",
            bloodPressure = "80",
            twinPregnancy = false,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED
        )

        val post = cache.asPostModel()

        assertEquals(3L, post.id)
        assertEquals(88L, post.benId)
        assertEquals(2, post.visitNumber)
        assertTrue(post.isActive)
        assertTrue(post.haveMCPCard)
        assertEquals("Ravi", post.husbandName)
        assertEquals(4, post.numANC)
        assertEquals(60, post.weight)
        assertEquals(120, post.systolicBloodPressure)
        assertEquals(80, post.diastolicBloodPressure)
        assertFalse(post.twinPregnancy)
    }

    @Test
    fun `PMSMACache asPostModel maps null blood pressures`() {
        val cache = PMSMACache(
            benId = 1L,
            visitNumber = 1,
            isActive = true,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED
        )

        val post = cache.asPostModel()

        assertNull(post.systolicBloodPressure)
        assertNull(post.diastolicBloodPressure)
    }

    // ---------------------------------------------------------------
    // PmsmaPost.toPmsmaCache()
    // ---------------------------------------------------------------

    @Test
    fun `PmsmaPost toPmsmaCache maps core fields and sets synced`() {
        val post = PmsmaPost(
            id = 4L,
            benId = 99L,
            visitNumber = 1,
            isActive = true,
            rchNumber = "RCH-9",
            haveMCPCard = true,
            husbandName = "Mohan",
            numANC = 2,
            weight = 55,
            systolicBloodPressure = 118,
            diastolicBloodPressure = 76,
            createdBy = "c",
            updatedBy = "u"
        )

        val cache = post.toPmsmaCache()

        assertEquals(4L, cache.id)
        assertEquals(99L, cache.benId)
        assertEquals(1, cache.visitNumber)
        assertTrue(cache.isActive)
        assertEquals("RCH-9", cache.mctsNumberOrRchNumber)
        assertTrue(cache.haveMCPCard)
        assertEquals("Mohan", cache.husbandName)
        assertEquals(2, cache.numANC)
        assertEquals(55, cache.weight)
        assertEquals("118", cache.systolicBloodPressure)
        assertEquals("76", cache.bloodPressure)
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }
}
