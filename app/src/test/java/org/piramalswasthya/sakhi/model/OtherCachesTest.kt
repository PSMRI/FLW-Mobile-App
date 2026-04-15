package org.piramalswasthya.sakhi.model

import org.junit.Assert.*
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class OtherCachesTest {

    // =====================================================
    // CDRCache Tests
    // =====================================================

    @Test fun `CDRCache can be created`() {
        val cache = CDRCache(benId = 1L, processed = "N", syncState = SyncState.UNSYNCED)
        assertNotNull(cache)
    }

    @Test fun `CDRCache default id is 0`() {
        val cache = CDRCache(benId = 1L, processed = "N", syncState = SyncState.UNSYNCED)
        assertEquals(0, cache.id)
    }

    @Test fun `CDRCache default cdr1File is null`() {
        val cache = CDRCache(benId = 1L, processed = "N", syncState = SyncState.UNSYNCED)
        assertNull(cache.cdr1File)
    }

    @Test fun `CDRCache default motherName is null`() {
        val cache = CDRCache(benId = 1L, processed = "N", syncState = SyncState.UNSYNCED)
        assertNull(cache.motherName)
    }

    @Test fun `CDRCache copy works`() {
        val cache = CDRCache(benId = 1L, processed = "N", syncState = SyncState.UNSYNCED)
        val copy = cache.copy(motherName = "Test Mother", cdr1File = "/path/to/file")
        assertEquals("Test Mother", copy.motherName)
        assertEquals("/path/to/file", copy.cdr1File)
    }

    @Test fun `CDRCache same key fields match`() {
        val a = CDRCache(id = 1, benId = 1L, processed = "N", syncState = SyncState.UNSYNCED)
        assertEquals(1, a.id)
        assertEquals(1L, a.benId)
        assertEquals("N", a.processed)
    }

    // =====================================================
    // MDSRCache Tests
    // =====================================================

    @Test fun `MDSRCache can be created`() {
        val cache = MDSRCache(benId = 1L, processed = "N", syncState = SyncState.UNSYNCED)
        assertNotNull(cache)
    }

    @Test fun `MDSRCache default mdsr1File is null`() {
        val cache = MDSRCache(benId = 1L, processed = "N", syncState = SyncState.UNSYNCED)
        assertNull(cache.mdsr1File)
    }

    @Test fun `MDSRCache default address is null`() {
        val cache = MDSRCache(benId = 1L, processed = "N", syncState = SyncState.UNSYNCED)
        assertNull(cache.address)
    }

    @Test fun `MDSRCache copy works`() {
        val cache = MDSRCache(benId = 1L, processed = "N", syncState = SyncState.UNSYNCED)
        val copy = cache.copy(address = "Test Address")
        assertEquals("Test Address", copy.address)
    }

    @Test fun `MDSRCache same key fields match`() {
        val a = MDSRCache(id = 1, benId = 1L, processed = "N", syncState = SyncState.UNSYNCED)
        assertEquals(1, a.id)
        assertEquals(1L, a.benId)
        assertEquals(SyncState.UNSYNCED, a.syncState)
    }

    // =====================================================
    // FPOTCache Tests
    // =====================================================

    @Test fun `FPOTCache can be created`() {
        val cache = FPOTCache(benId = 1L, hhId = 100L)
        assertNotNull(cache)
    }

    @Test fun `FPOTCache default spouseName is null`() {
        val cache = FPOTCache(benId = 1L, hhId = 100L)
        assertNull(cache.spouseName)
    }

    @Test fun `FPOTCache default category is null`() {
        val cache = FPOTCache(benId = 1L, hhId = 100L)
        assertNull(cache.category)
    }

    @Test fun `FPOTCache copy works`() {
        val cache = FPOTCache(benId = 1L, hhId = 100L)
        val copy = cache.copy(spouseName = "Spouse", category = "Cat1")
        assertEquals("Spouse", copy.spouseName)
        assertEquals("Cat1", copy.category)
    }

    @Test fun `FPOTCache same key fields match`() {
        val a = FPOTCache(id = 1, benId = 1L, hhId = 100L)
        assertEquals(1, a.id)
        assertEquals(1L, a.benId)
        assertEquals(100L, a.hhId)
    }

    // =====================================================
    // PMJAYCache Tests
    // =====================================================

    @Test fun `PMJAYCache can be created`() {
        val cache = PMJAYCache(benId = 1L, hhId = 100L)
        assertNotNull(cache)
    }

    @Test fun `PMJAYCache default registeredHospital is null`() {
        val cache = PMJAYCache(benId = 1L, hhId = 100L)
        assertNull(cache.registeredHospital)
    }

    @Test fun `PMJAYCache default contactNumber is 0`() {
        val cache = PMJAYCache(benId = 1L, hhId = 100L)
        assertEquals(0L, cache.contactNumber)
    }

    @Test fun `PMJAYCache copy works`() {
        val cache = PMJAYCache(benId = 1L, hhId = 100L)
        val copy = cache.copy(registeredHospital = "Hospital A", contactNumber = 9876543210L)
        assertEquals("Hospital A", copy.registeredHospital)
        assertEquals(9876543210L, copy.contactNumber)
    }

    @Test fun `PMJAYCache same key fields match`() {
        val a = PMJAYCache(id = 1, benId = 1L, hhId = 100L)
        assertEquals(1, a.id)
        assertEquals(1L, a.benId)
        assertEquals(100L, a.hhId)
    }

    // =====================================================
    // HBYCCache Tests
    // =====================================================

    @Test fun `HBYCCache can be created`() {
        val cache = HBYCCache(benId = 1L, hhId = 100L, processed = "N", syncState = SyncState.UNSYNCED)
        assertNotNull(cache)
    }

    @Test fun `HBYCCache default month is null`() {
        val cache = HBYCCache(benId = 1L, hhId = 100L, processed = "N", syncState = SyncState.UNSYNCED)
        assertNull(cache.month)
    }

    @Test fun `HBYCCache default subcenterName is null`() {
        val cache = HBYCCache(benId = 1L, hhId = 100L, processed = "N", syncState = SyncState.UNSYNCED)
        assertNull(cache.subcenterName)
    }

    @Test fun `HBYCCache copy works`() {
        val cache = HBYCCache(benId = 1L, hhId = 100L, processed = "N", syncState = SyncState.UNSYNCED)
        val copy = cache.copy(month = "January", year = "2025")
        assertEquals("January", copy.month)
        assertEquals("2025", copy.year)
    }

    @Test fun `HBYCCache same key fields match`() {
        val a = HBYCCache(id = 1, benId = 1L, hhId = 100L, processed = "N", syncState = SyncState.UNSYNCED)
        assertEquals(1, a.id)
        assertEquals(1L, a.benId)
        assertEquals(100L, a.hhId)
    }

    // =====================================================
    // AdolescentHealthCache Tests
    // =====================================================

    @Test fun `AdolescentHealthCache can be created`() {
        val cache = AdolescentHealthCache()
        assertNotNull(cache)
    }

    @Test fun `AdolescentHealthCache default benId is null`() {
        val cache = AdolescentHealthCache()
        assertNull(cache.benId)
    }

    @Test fun `AdolescentHealthCache default healthStatus is null`() {
        val cache = AdolescentHealthCache()
        assertNull(cache.healthStatus)
    }

    @Test fun `AdolescentHealthCache default ifaTabletDistributed is null`() {
        val cache = AdolescentHealthCache()
        assertNull(cache.ifaTabletDistributed)
    }

    @Test fun `AdolescentHealthCache copy works`() {
        val cache = AdolescentHealthCache(benId = 1L)
        val copy = cache.copy(healthStatus = "Good", ifaTabletDistributed = true, quantityOfIfaTablets = 30)
        assertEquals("Good", copy.healthStatus)
        assertEquals(true, copy.ifaTabletDistributed)
        assertEquals(30, copy.quantityOfIfaTablets)
    }

    @Test fun `AdolescentHealthCache with custom values`() {
        val cache = AdolescentHealthCache(userID = 42, benId = 100L, healthStatus = "Normal")
        assertEquals(42, cache.userID)
        assertEquals(100L, cache.benId)
        assertEquals("Normal", cache.healthStatus)
    }

    // =====================================================
    // UploadResponse Tests
    // =====================================================

    @Test fun `UploadResponse can be created`() {
        val response = UploadResponse(data = null, statusCode = 200, errorMessage = "", status = "OK")
        assertNotNull(response)
    }

    @Test fun `UploadResponse has correct statusCode`() {
        val response = UploadResponse(data = null, statusCode = 200, errorMessage = "", status = "OK")
        assertEquals(200, response.statusCode)
        assertEquals("OK", response.status)
    }

    @Test fun `UploadResponse with data`() {
        val data = UploadData(response = "test response")
        val response = UploadResponse(data = data, statusCode = 200, errorMessage = "", status = "OK")
        assertNotNull(response.data)
        assertEquals("test response", response.data?.response)
    }

    @Test fun `UploadResponse equality`() {
        val a = UploadResponse(data = null, statusCode = 200, errorMessage = "", status = "OK")
        val b = UploadResponse(data = null, statusCode = 200, errorMessage = "", status = "OK")
        assertEquals(a, b)
    }

    @Test fun `UploadResponse inequality`() {
        val a = UploadResponse(data = null, statusCode = 200, errorMessage = "", status = "OK")
        val b = UploadResponse(data = null, statusCode = 500, errorMessage = "Error", status = "FAIL")
        assertNotEquals(a, b)
    }

    @Test fun `UploadData can be created`() {
        val data = UploadData(response = "success")
        assertEquals("success", data.response)
    }

    @Test fun `UploadData equality`() {
        val a = UploadData(response = "ok")
        val b = UploadData(response = "ok")
        assertEquals(a, b)
    }

    // =====================================================
    // SyncLogEntry Tests
    // =====================================================

    @Test fun `SyncLogEntry can be created`() {
        val entry = SyncLogEntry(id = 1L, timestamp = 123456L, level = LogLevel.INFO, tag = "Sync", message = "Done")
        assertNotNull(entry)
    }

    @Test fun `SyncLogEntry equality`() {
        val a = SyncLogEntry(id = 1L, timestamp = 123456L, level = LogLevel.INFO, tag = "Sync", message = "Done")
        val b = SyncLogEntry(id = 1L, timestamp = 123456L, level = LogLevel.INFO, tag = "Sync", message = "Done")
        assertEquals(a, b)
    }

    @Test fun `SyncLogEntry copy works`() {
        val entry = SyncLogEntry(id = 1L, timestamp = 123456L, level = LogLevel.INFO, tag = "Sync", message = "Done")
        val copy = entry.copy(level = LogLevel.ERROR, message = "Failed")
        assertEquals(LogLevel.ERROR, copy.level)
        assertEquals("Failed", copy.message)
    }

    @Test fun `LogLevel enum values exist`() {
        assertNotNull(LogLevel.DEBUG)
        assertNotNull(LogLevel.INFO)
        assertNotNull(LogLevel.WARN)
        assertNotNull(LogLevel.ERROR)
    }

    @Test fun `LogLevel has 4 values`() {
        assertEquals(4, LogLevel.values().size)
    }
}
