package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Unit tests for [UwinNetwork.asCacheModel] (Network -> Cache).
 */
class UwinNetworkTest {

    private fun network() = UwinNetwork(
        id = 5,
        sessionDate = 2_000L,
        place = "PHC",
        participantsCount = 8,
        uploadedFiles1 = "u1",
        uploadedFiles2 = "u2",
        createdBy = "creator",
        createdDate = "2024-01-01",
        updatedBy = "updater",
        updatedDate = "2024-01-02"
    )

    @Test
    fun `asCacheModel maps core fields`() {
        val cache = network().asCacheModel()
        assertEquals(5, cache.id)
        assertEquals("PHC", cache.place)
        assertEquals(8, cache.participantsCount)
        assertEquals(2_000L, cache.sessionDate)
    }

    @Test
    fun `asCacheModel forces processed P and synced state`() {
        val cache = network().asCacheModel()
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test
    fun `asCacheModel parses date strings to non-zero millis`() {
        val cache = network().asCacheModel()
        assertTrue(cache.createdDate > 0L)
        assertTrue(cache.updatedDate > 0L)
    }

    @Test
    fun `asCacheModel carries creator and uploaded files`() {
        val cache = network().asCacheModel()
        assertEquals("creator", cache.createdBy)
        assertEquals("u1", cache.uploadedFiles1)
        assertEquals("u2", cache.uploadedFiles2)
    }
}
