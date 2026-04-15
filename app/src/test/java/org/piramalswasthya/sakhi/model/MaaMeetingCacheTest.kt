package org.piramalswasthya.sakhi.model

import org.junit.Assert.*
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class MaaMeetingCacheTest {

    // =====================================================
    // MaaMeetingEntity Tests
    // =====================================================

    @Test fun `MaaMeetingEntity can be created`() {
        val entity = MaaMeetingEntity(meetingDate = null, place = null, participants = null, ashaId = 1)
        assertNotNull(entity)
    }

    @Test fun `MaaMeetingEntity default id is 0`() {
        val entity = MaaMeetingEntity(meetingDate = null, place = null, participants = null, ashaId = 1)
        assertEquals(0L, entity.id)
    }

    @Test fun `MaaMeetingEntity ashaId is correct`() {
        val entity = MaaMeetingEntity(meetingDate = null, place = null, participants = null, ashaId = 42)
        assertEquals(42, entity.ashaId)
    }

    @Test fun `MaaMeetingEntity default meetingDate when set to null`() {
        val entity = MaaMeetingEntity(meetingDate = null, place = null, participants = null, ashaId = 1)
        assertNull(entity.meetingDate)
    }

    @Test fun `MaaMeetingEntity default place is null`() {
        val entity = MaaMeetingEntity(meetingDate = null, place = null, participants = null, ashaId = 1)
        assertNull(entity.place)
    }

    @Test fun `MaaMeetingEntity copy works`() {
        val entity = MaaMeetingEntity(meetingDate = null, place = null, participants = null, ashaId = 1)
        val copy = entity.copy(place = "Village Hall", ashaId = 5)
        assertEquals("Village Hall", copy.place)
        assertEquals(5, copy.ashaId)
    }

    @Test fun `MaaMeetingEntity same fields`() {
        val ts = 1000L
        val a = MaaMeetingEntity(id = 1, meetingDate = null, place = null, participants = null, ashaId = 1, createdAt = ts, updatedAt = ts)
        val b = MaaMeetingEntity(id = 1, meetingDate = null, place = null, participants = null, ashaId = 1, createdAt = ts, updatedAt = ts)
        assertEquals(a, b)
    }

    @Test fun `MaaMeetingEntity inequality`() {
        val a = MaaMeetingEntity(meetingDate = null, place = null, participants = null, ashaId = 1)
        val b = MaaMeetingEntity(meetingDate = null, place = null, participants = null, ashaId = 2)
        assertNotEquals(a, b)
    }

    @Test fun `MaaMeetingEntity with synced state`() {
        val entity = MaaMeetingEntity(meetingDate = null, place = null, participants = null, ashaId = 1, syncState = SyncState.SYNCED)
        assertEquals(SyncState.SYNCED, entity.syncState)
    }

    @Test fun `MaaMeetingEntity syncState update via copy`() {
        val entity = MaaMeetingEntity(meetingDate = null, place = null, participants = null, ashaId = 1)
        val updated = entity.copy(syncState = SyncState.SYNCED)
        assertEquals(SyncState.SYNCED, updated.syncState)
    }
}
