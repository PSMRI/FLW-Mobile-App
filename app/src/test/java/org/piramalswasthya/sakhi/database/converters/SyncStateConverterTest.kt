package org.piramalswasthya.sakhi.database.converters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class SyncStateConverterTest {

    @Test
    fun `toInt maps UNSYNCED to zero`() {
        assertEquals(0, SyncStateConverter.toInt(SyncState.UNSYNCED))
    }

    @Test
    fun `toInt maps SYNCING to one`() {
        assertEquals(1, SyncStateConverter.toInt(SyncState.SYNCING))
    }

    @Test
    fun `toInt maps SYNCED to two`() {
        assertEquals(2, SyncStateConverter.toInt(SyncState.SYNCED))
    }

    @Test
    fun `fromInt maps zero to UNSYNCED`() {
        assertEquals(SyncState.UNSYNCED, SyncStateConverter.fromInt(0))
    }

    @Test
    fun `fromInt maps one to SYNCING`() {
        assertEquals(SyncState.SYNCING, SyncStateConverter.fromInt(1))
    }

    @Test
    fun `fromInt maps two to SYNCED`() {
        assertEquals(SyncState.SYNCED, SyncStateConverter.fromInt(2))
    }

    @Test(expected = ArrayIndexOutOfBoundsException::class)
    fun `fromInt throws for an out of range ordinal`() {
        SyncStateConverter.fromInt(9)
    }

    @Test
    fun `toSyncState parses each enum name`() {
        assertEquals(SyncState.UNSYNCED, SyncStateConverter.toSyncState("UNSYNCED"))
        assertEquals(SyncState.SYNCING, SyncStateConverter.toSyncState("SYNCING"))
        assertEquals(SyncState.SYNCED, SyncStateConverter.toSyncState("SYNCED"))
    }

    @Test
    fun `toSyncState returns null for null input`() {
        assertNull(SyncStateConverter.toSyncState(null))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toSyncState throws for an unknown name`() {
        SyncStateConverter.toSyncState("NOT_A_STATE")
    }

    @Test
    fun `fromSyncState returns the enum name`() {
        assertEquals("UNSYNCED", SyncStateConverter.fromSyncState(SyncState.UNSYNCED))
        assertEquals("SYNCING", SyncStateConverter.fromSyncState(SyncState.SYNCING))
        assertEquals("SYNCED", SyncStateConverter.fromSyncState(SyncState.SYNCED))
    }

    @Test
    fun `fromSyncState returns null for null input`() {
        assertNull(SyncStateConverter.fromSyncState(null))
    }

    @Test
    fun `int round trip preserves every state`() {
        SyncState.values().forEach {
            assertEquals(it, SyncStateConverter.fromInt(SyncStateConverter.toInt(it)))
        }
    }

    @Test
    fun `string round trip preserves every state`() {
        SyncState.values().forEach {
            assertEquals(it, SyncStateConverter.toSyncState(SyncStateConverter.fromSyncState(it)))
        }
    }

    @Test
    fun `ordinal order of SyncState is never changed`() {
        assertEquals(0, SyncState.UNSYNCED.ordinal)
        assertEquals(1, SyncState.SYNCING.ordinal)
        assertEquals(2, SyncState.SYNCED.ordinal)
        assertEquals(3, SyncState.values().size)
    }
}
