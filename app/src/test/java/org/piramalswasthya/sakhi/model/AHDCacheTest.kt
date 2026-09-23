package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Unit tests for [AHDCache] mapping helpers: toDTO() and toAHDCache().
 */
class AHDCacheTest {

    private fun cache() = AHDCache(
        id = 7,
        mobilizedForAHD = "Yes",
        ahdPlace = "PHC-1",
        ahdDate = "01-01-2024",
        image1 = "img1",
        image2 = "img2",
        syncState = SyncState.SYNCED
    )

    @Test
    fun `toDTO copies core fields`() {
        val dto = cache().toDTO()
        assertEquals(7, dto.id)
        assertEquals("Yes", dto.mobilizedForAHD)
        assertEquals("PHC-1", dto.ahdPlace)
        assertEquals("01-01-2024", dto.ahdDate)
    }

    @Test
    fun `toDTO carries both images`() {
        val dto = cache().toDTO()
        assertEquals("img1", dto.image1)
        assertEquals("img2", dto.image2)
    }

    @Test
    fun `toDTO handles null optional fields`() {
        val dto = AHDCache().toDTO()
        assertNull(dto.mobilizedForAHD)
        assertNull(dto.ahdPlace)
        assertEquals(0, dto.id)
    }

    @Test
    fun `toAHDCache copies fields but resets syncState to default`() {
        val copy = cache().toAHDCache()
        assertEquals(7, copy.id)
        assertEquals("Yes", copy.mobilizedForAHD)
        assertEquals("img2", copy.image2)
        // toAHDCache does not carry syncState, so it falls back to the default.
        assertEquals(SyncState.UNSYNCED, copy.syncState)
    }
}
