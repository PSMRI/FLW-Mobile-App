package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Unit tests for [UwinCache.asDomainModel] (Cache -> UwinNetwork).
 */
class UwinCacheTest {

    private fun cache() = UwinCache(
        id = 3,
        sessionDate = 1_700_000_000_000L,
        place = "Center",
        participantsCount = 12,
        uploadedFiles1 = "f1",
        uploadedFiles2 = "f2",
        processed = "N",
        createdBy = "asha",
        createdDate = 1_700_000_000_000L,
        updatedBy = "asha",
        updatedDate = 1_700_000_000_000L,
        syncState = SyncState.UNSYNCED
    )

    @Test
    fun `asDomainModel maps core fields`() {
        val net = cache().asDomainModel()
        assertEquals(3, net.id)
        assertEquals("Center", net.place)
        assertEquals(12, net.participantsCount)
        assertEquals(1_700_000_000_000L, net.sessionDate)
    }

    @Test
    fun `asDomainModel carries uploaded files and creator`() {
        val net = cache().asDomainModel()
        assertEquals("f1", net.uploadedFiles1)
        assertEquals("f2", net.uploadedFiles2)
        assertEquals("asha", net.createdBy)
    }

    @Test
    fun `asDomainModel formats non-zero dates to non-null strings`() {
        val net = cache().asDomainModel()
        assertNotNull(net.createdDate)
        assertNotNull(net.updatedDate)
    }
}
