package org.piramalswasthya.sakhi.database.room.dao

import androidx.lifecycle.LiveData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.UwinCache

class UwinDaoTest {

    private val stored = mutableListOf<UwinCache>()
    private var clearCallCount = 0
    private var resetSyncingCallCount = 0

    private val dao: UwinDao = object : UwinDao {

        override suspend fun insert(session: UwinCache) {
            stored.add(session)
        }

        override suspend fun update(session: UwinCache) {
            val index = stored.indexOfFirst { it.id == session.id }
            if (index >= 0) stored[index] = session
        }

        override suspend fun getAllSessions(): List<UwinCache> = stored.toList()

        override suspend fun getUwinById(id: Int): UwinCache? = stored.find { it.id == id }

        override suspend fun getUnsyncedSessions(synced: SyncState): List<UwinCache> =
            stored.filter { it.syncState != synced }

        override suspend fun updateSyncState(id: Int, syncState: SyncState) {
            stored.find { it.id == id }?.syncState = syncState
        }

        override fun getAllUwinRecords(): LiveData<List<UwinCache>> {
            throw UnsupportedOperationException("not needed for this test")
        }

        override suspend fun clearAll() {
            clearCallCount++
            stored.clear()
        }

        override suspend fun resetSyncingToUnsynced() {
            resetSyncingCallCount++
            stored.filter { it.syncState == SyncState.SYNCING }
                .forEach { it.syncState = SyncState.UNSYNCED }
        }
    }

    private fun session(id: Int, syncState: SyncState = SyncState.UNSYNCED) = UwinCache(
        id = id,
        sessionDate = 1_700_000_000_000L,
        place = "Village Center",
        participantsCount = 12,
        createdBy = "asha1",
        createdDate = 1_700_000_100_000L,
        updatedBy = "asha1",
        updatedDate = 1_700_000_100_000L,
        syncState = syncState
    )

    @Test
    fun `replaceAll clears the table before inserting the new list`() = runTest {
        stored.add(session(1))

        dao.replaceAll(listOf(session(2), session(3)))

        assertEquals(1, clearCallCount)
        assertEquals(listOf(2, 3), stored.map { it.id })
    }

    @Test
    fun `replaceAll leaves an empty table when given an empty list`() = runTest {
        stored.add(session(1))

        dao.replaceAll(emptyList())

        assertEquals(1, clearCallCount)
        assertTrue(stored.isEmpty())
    }

    @Test
    fun `the dao contract keeps the other queries reachable from the default method`() = runTest {
        dao.replaceAll(listOf(session(1, SyncState.SYNCING), session(2, SyncState.SYNCED)))

        assertEquals(2, dao.getAllSessions().size)
        assertEquals(session(1, SyncState.SYNCING), dao.getUwinById(1))
        assertEquals(1, dao.getUnsyncedSessions(SyncState.SYNCED).size)

        dao.updateSyncState(1, SyncState.SYNCED)
        assertEquals(SyncState.SYNCED, dao.getUwinById(1)?.syncState)

        dao.resetSyncingToUnsynced()
        assertEquals(1, resetSyncingCallCount)
    }
}
