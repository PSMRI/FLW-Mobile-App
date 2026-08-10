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

    private fun fpotFullyPopulated() = FPOTCache(
        id = 1,
        benId = 1L,
        hhId = 100L,
        monthlySerialNumber = "M1",
        annualSerialNumber = "A1",
        spouseName = "Spouse",
        category = "APL",
        benAddress = "Address",
        contactNumber = "9999999999",
        educationalQualification = "Graduate",
        numChildren = "2",
        youngestChildAge = "5",
        sterilization = true,
        mrCheckListFilled = true,
        dateOfOperation = 1_600_000_000_000L,
        femaleSterilization = "Yes",
        secondFollowUpExpectedDate = 1_600_100_000_000L,
        followUpActualDate = 1_600_200_000_000L,
        followUpDetails = "Details",
        secondPostFollowUpCounselling = "Counselling",
        thirdFollowUpExpectedDate = 1_600_300_000_000L,
        menstruationStarted = true,
        spermatozoaFoundInSemen = "Yes",
        thirdPostFollowUpCounselling = "Counselling2",
        sterilizationOrVasectomyIssueDate = 1_600_400_000_000L,
        notIssuedReason = "Reason",
        sterilizationOrVasectomyDocSubmitted = "Yes",
        remarks = "Remark",
        createdBy = "creator",
        createdDate = 1_600_500_000_000L,
        processed = "N"
    )

    @Test fun `FPOTCache equals true for identical instances`() {
        assertEquals(fpotFullyPopulated(), fpotFullyPopulated())
    }

    @Test fun `FPOTCache equals false when spouseName differs`() {
        assertNotEquals(fpotFullyPopulated(), fpotFullyPopulated().copy(spouseName = "Other"))
    }

    @Test fun `FPOTCache equals false when sterilization differs`() {
        assertNotEquals(fpotFullyPopulated(), fpotFullyPopulated().copy(sterilization = false))
    }

    @Test fun `FPOTCache equals false when createdDate differs`() {
        assertNotEquals(fpotFullyPopulated(), fpotFullyPopulated().copy(createdDate = 1L))
    }

    @Test fun `FPOTCache equals false against null and different type`() {
        val cache = fpotFullyPopulated()
        assertFalse(cache.equals(null))
        assertFalse(cache.equals("not a cache"))
        assertTrue(cache.equals(cache))
    }

    @Test fun `FPOTCache hashCode equal for equal objects`() {
        assertEquals(fpotFullyPopulated().hashCode(), fpotFullyPopulated().hashCode())
    }

    @Test fun `FPOTCache hashCode does not throw when optional fields are null`() {
        val minimal = FPOTCache(benId = 1L, hhId = 100L)
        assertNotNull(minimal.hashCode())
    }

    @Test fun `FPOTCache toString contains key field values`() {
        val text = fpotFullyPopulated().toString()
        assertTrue(text.contains("FPOTCache"))
        assertTrue(text.contains("Spouse"))
    }

    @Test fun `FPOTCache sterilization true and false`() {
        assertEquals(true, fpotFullyPopulated().copy(sterilization = true).sterilization)
        assertEquals(false, fpotFullyPopulated().copy(sterilization = false).sterilization)
    }

    @Test fun `FPOTCache mrCheckListFilled true and false`() {
        assertEquals(true, fpotFullyPopulated().copy(mrCheckListFilled = true).mrCheckListFilled)
        assertEquals(false, fpotFullyPopulated().copy(mrCheckListFilled = false).mrCheckListFilled)
    }

    @Test fun `FPOTCache menstruationStarted true and false`() {
        assertEquals(true, fpotFullyPopulated().copy(menstruationStarted = true).menstruationStarted)
        assertEquals(false, fpotFullyPopulated().copy(menstruationStarted = false).menstruationStarted)
    }

    // =====================================================
    // FPOTPost Tests
    // =====================================================

    private fun fpotPostFullyPopulated() = FPOTPost(
        abortionPost = "no",
        ageOfYoung = "5",
        annualSerialNumber = "A1",
        beneficiaryAddress = "addr",
        beneficiaryAge = "28",
        beneficiaryName = "Name",
        beneficiaryid = 1L,
        createdBy = "creator",
        createdDate = "2024-01-01",
        educationalQualification = "Grad",
        exceptlaproscopy = "no",
        grade = "A",
        houseoldId = "H1",
        husbandWifeName = "Spouse",
        inTheDateOperation = "2024-01-02",
        intervalMiniLap = "yes",
        latitude = 12.0,
        loginId = 1,
        longitude = 77.0,
        medicalRecord = "rec",
        monthlySerialNumber = "M1",
        phoneNumber = "9999999999",
        postMiniLap = "no",
        postnatalSterlization = "no",
        sterilizationConsent = "yes",
        totalLiveChild = "2",
        traditional = "no",
        updatedBy = "updater",
        updatedDate = 1_600_000_000_000L
    )

    @Test fun `FPOTPost equals true for identical instances`() {
        assertEquals(fpotPostFullyPopulated(), fpotPostFullyPopulated())
    }

    @Test fun `FPOTPost equals false when beneficiaryName differs`() {
        assertNotEquals(fpotPostFullyPopulated(), fpotPostFullyPopulated().copy(beneficiaryName = "Other"))
    }

    @Test fun `FPOTPost hashCode equal for equal objects`() {
        assertEquals(fpotPostFullyPopulated().hashCode(), fpotPostFullyPopulated().hashCode())
    }

    @Test fun `FPOTPost toString contains key field values`() {
        val text = fpotPostFullyPopulated().toString()
        assertTrue(text.contains("FPOTPost"))
        assertTrue(text.contains("Spouse"))
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
