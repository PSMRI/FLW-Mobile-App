package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class MoreModelTests {

    @Test fun `BenHealthIdDetails creation with defaults`() {
        val d = BenHealthIdDetails()
        assertEquals("", d.healthId)
        assertEquals("", d.healthIdNumber)
        assertFalse(d.isNewAbha)
    }
    @Test fun `BenHealthIdDetails creation with values`() {
        val d = BenHealthIdDetails("id123", "num456", true)
        assertEquals("id123", d.healthId)
        assertTrue(d.isNewAbha)
    }
    @Test fun `BenHealthIdDetails copy`() { assertTrue(BenHealthIdDetails("id", "num").copy(isNewAbha = true).isNewAbha) }
    @Test fun `BenHealthIdDetails equals`() { assertEquals(BenHealthIdDetails("id", "num", true), BenHealthIdDetails("id", "num", true)) }
    @Test fun `BenHealthIdDetails not equals`() { assertNotEquals(BenHealthIdDetails("a", "b"), BenHealthIdDetails("c", "d")) }

    @Test fun `HomeVisitDomain creation`() {
        val d = HomeVisitDomain(1, 1L, 1, 1000L, "2026-04-14", "{}", SyncState.SYNCED, true)
        assertEquals(1, d.id)
        assertEquals(1L, d.benId)
        assertTrue(d.isSynced)
    }
    @Test fun `HomeVisitDomain copy`() { assertEquals(3, HomeVisitDomain(1, 1L, 1, 1000L, "d", "{}", SyncState.UNSYNCED, false).copy(visitNumber = 3).visitNumber) }
    @Test fun `HomeVisitDomain equals`() { assertEquals(HomeVisitDomain(1, 1L, 1, 1000L, "d", "{}", SyncState.SYNCED, true), HomeVisitDomain(1, 1L, 1, 1000L, "d", "{}", SyncState.SYNCED, true)) }
    @Test fun `HomeVisitDomain not equals`() { assertNotEquals(HomeVisitDomain(1, 1L, 1, 1000L, "d", "{}", SyncState.SYNCED, true), HomeVisitDomain(2, 1L, 1, 1000L, "d", "{}", SyncState.SYNCED, true)) }

    @Test fun `HomeVisitUiState both true`() { val s = HomeVisitUiState(true, true); assertTrue(s.canAddHomeVisit); assertTrue(s.canViewHomeVisit) }
    @Test fun `HomeVisitUiState both false`() { val s = HomeVisitUiState(false, false); assertFalse(s.canAddHomeVisit); assertFalse(s.canViewHomeVisit) }
    @Test fun `HomeVisitUiState not equals`() { assertNotEquals(HomeVisitUiState(true, true), HomeVisitUiState(false, true)) }

    @Test fun `UploadResponse equals`() { assertEquals(UploadResponse(null, 200, "", "OK"), UploadResponse(null, 200, "", "OK")) }
    @Test fun `UploadResponse not equals`() { assertNotEquals(UploadResponse(null, 200, "", "OK"), UploadResponse(null, 500, "err", "FAIL")) }
    @Test fun `UploadResponse copy`() { assertEquals(500, UploadResponse(null, 200, "", "OK").copy(statusCode = 500).statusCode) }

    @Test fun `FailedWorkerInfo copy`() { assertEquals("New", FailedWorkerInfo("W", "E").copy(error = "New").error) }
    @Test fun `FailedWorkerInfo not equals`() { assertNotEquals(FailedWorkerInfo("W1", "E1"), FailedWorkerInfo("W2", "E2")) }
}
