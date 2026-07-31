package org.piramalswasthya.sakhi.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Extra coverage for JsonAdapters.kt mappers/helpers not exercised by
 * DtoToCacheMappingTest or ScreeningDtoMappingTest:
 *  - ABHAGeneratedDTO.toCache()
 *  - LeprosyFollowUpDTO.toCache()
 *  - the package-level date helpers getLongFromDate / getLongFromDateMultipleSupport / convertIsoDateToMillis
 */
class JsonAdaptersTest {

    // ---------------- ABHAGeneratedDTO.toCache() ----------------

    private fun abhaDto() = ABHAGeneratedDTO(
        beneficiaryID = 10L,
        beneficiaryRegID = 20L,
        benName = "Asha",
        createdBy = "creator",
        message = "created",
        txnId = "txn-1",
        providerServiceMapId = 7
    )

    @Test fun `ABHAGeneratedDTO toCache maps identifiers and names`() {
        val cache = abhaDto().toCache()
        assertEquals(10L, cache.beneficiaryID)
        assertEquals(20L, cache.beneficiaryRegID)
        assertEquals("Asha", cache.benName)
        assertEquals("txn-1", cache.txnId)
        assertEquals("creator", cache.createdBy)
        assertEquals(7, cache.providerServiceMapId)
    }

    @Test fun `ABHAGeneratedDTO toCache carries abha flags and optional surname`() {
        val cache = abhaDto().copy(
            benSurname = "Devi",
            healthId = "hid",
            healthIdNumber = "hidn",
            isNewAbha = true
        ).toCache()
        assertEquals("Devi", cache.benSurname)
        assertEquals("hid", cache.healthId)
        assertEquals("hidn", cache.healthIdNumber)
        assertTrue(cache.isNewAbha)
    }

    @Test fun `ABHAGeneratedDTO toCache defaults isNewAbha false`() {
        assertEquals(false, abhaDto().toCache().isNewAbha)
    }

    // ---------------- LeprosyFollowUpDTO.toCache() ----------------

    private fun leprosyFollowUpDto() = LeprosyFollowUpDTO(
        benId = 55L,
        visitNumber = 3,
        createdBy = "creator",
        createdDate = "2023-01-10",
        modifiedBy = "modifier",
        lastModDate = "2023-01-11"
    )

    @Test fun `LeprosyFollowUpDTO toCache maps ids and visit`() {
        val cache = leprosyFollowUpDto().toCache()
        assertEquals(55L, cache.benId)
        assertEquals(3, cache.visitNumber)
    }

    @Test fun `LeprosyFollowUpDTO toCache sets synced state and audit fields`() {
        val cache = leprosyFollowUpDto().toCache()
        assertEquals(SyncState.SYNCED, cache.syncState)
        assertEquals("creator", cache.createdBy)
        assertEquals("modifier", cache.modifiedBy)
    }

    @Test fun `LeprosyFollowUpDTO toCache passes through clinical fields`() {
        val cache = leprosyFollowUpDto().copy(
            treatmentStatus = "Ongoing",
            leprosyStatus = "Positive",
            typeOfLeprosy = "MB",
            referredTo = 2,
            referToName = "PHC"
        ).toCache()
        assertEquals("Ongoing", cache.treatmentStatus)
        assertEquals("Positive", cache.leprosyStatus)
        assertEquals("MB", cache.typeOfLeprosy)
        assertEquals(2, cache.referredTo)
        assertEquals("PHC", cache.referToName)
    }

    @Test fun `LeprosyFollowUpDTO toCache parses valid date to positive long`() {
        assertTrue(leprosyFollowUpDto().toCache().createdDate > 0L)
    }

    // ---------------- getLongFromDate ----------------

    @Test fun `getLongFromDate returns zero for null`() {
        assertEquals(0L, getLongFromDate(null))
    }

    @Test fun `getLongFromDate returns zero for blank`() {
        assertEquals(0L, getLongFromDate("   "))
    }

    @Test fun `getLongFromDate returns zero for literal null string`() {
        assertEquals(0L, getLongFromDate("null"))
        assertEquals(0L, getLongFromDate("NULL"))
    }

    @Test fun `getLongFromDate parses yyyy-MM-dd to positive long`() {
        assertTrue(getLongFromDate("2023-06-15") > 0L)
    }

    @Test fun `getLongFromDate parses long format fallback`() {
        assertTrue(getLongFromDate("Jun 5, 2023 3:30:00 PM") > 0L)
    }

    @Test fun `getLongFromDate returns zero for unparseable`() {
        assertEquals(0L, getLongFromDate("not-a-date"))
    }

    // ---------------- getLongFromDateMultipleSupport ----------------

    @Test fun `getLongFromDateMultipleSupport returns null for null`() {
        assertNull(getLongFromDateMultipleSupport(null))
    }

    @Test fun `getLongFromDateMultipleSupport returns null for blank`() {
        assertNull(getLongFromDateMultipleSupport(""))
    }

    @Test fun `getLongFromDateMultipleSupport returns null for epoch sentinel`() {
        assertNull(getLongFromDateMultipleSupport("1970-01-01"))
    }

    @Test fun `getLongFromDateMultipleSupport parses iso date`() {
        assertNotNull(getLongFromDateMultipleSupport("2023-06-15"))
    }

    @Test fun `getLongFromDateMultipleSupport parses slash date`() {
        assertNotNull(getLongFromDateMultipleSupport("15/06/2023"))
    }

    @Test fun `getLongFromDateMultipleSupport returns null for garbage`() {
        assertNull(getLongFromDateMultipleSupport("garbage"))
    }

    // ---------------- convertIsoDateToMillis ----------------

    @Test fun `convertIsoDateToMillis returns zero for null`() {
        assertEquals(0L, convertIsoDateToMillis(null))
    }

    @Test fun `convertIsoDateToMillis returns zero for empty`() {
        assertEquals(0L, convertIsoDateToMillis(""))
    }

    @Test fun `convertIsoDateToMillis parses valid iso offset date`() {
        assertTrue(convertIsoDateToMillis("2024-01-01T00:00:00.000+05:30") > 0L)
    }

    @Test fun `convertIsoDateToMillis returns zero for garbage`() {
        assertEquals(0L, convertIsoDateToMillis("garbage"))
    }

    // ---------------- CreateAbhaIdResponse ----------------

    private fun createAbhaIdResponse() = CreateAbhaIdResponse(
        token = "tok",
        refreshToken = "rtok",
        healthIdNumber = "11-2222-3333-4444",
        name = "Asha Devi",
        gender = "F",
        yearOfBirth = "1990",
        monthOfBirth = "05",
        dayOfBirth = "12",
        firstName = "Asha",
        healthId = "asha@abdm",
        lastName = "Devi",
        middleName = "K",
        stateCode = "27",
        districtCode = "512",
        stateName = "Maharashtra",
        districtName = "Pune",
        email = "asha@example.com",
        kycPhoto = "kyc",
        profilePhoto = "photo",
        mobile = "9999999999",
        authMethods = arrayOf("AADHAAR_OTP"),
        pincode = "411001",
        tags = mapOf("k" to "v"),
        alreadyExists = "false",
        new = true,
        txnId = "txn-99"
    )

    @Test fun `CreateAbhaIdResponse exposes constructor values`() {
        val dto = createAbhaIdResponse()
        assertEquals("tok", dto.token)
        assertEquals("rtok", dto.refreshToken)
        assertEquals("11-2222-3333-4444", dto.healthIdNumber)
        assertEquals("Asha Devi", dto.name)
        assertEquals("F", dto.gender)
        assertEquals("1990", dto.yearOfBirth)
        assertEquals("05", dto.monthOfBirth)
        assertEquals("12", dto.dayOfBirth)
        assertEquals("Asha", dto.firstName)
        assertEquals("asha@abdm", dto.healthId)
        assertEquals("Devi", dto.lastName)
        assertEquals("K", dto.middleName)
        assertEquals("27", dto.stateCode)
        assertEquals("512", dto.districtCode)
        assertEquals("Maharashtra", dto.stateName)
        assertEquals("Pune", dto.districtName)
        assertEquals("asha@example.com", dto.email)
        assertEquals("kyc", dto.kycPhoto)
        assertEquals("photo", dto.profilePhoto)
        assertEquals("9999999999", dto.mobile)
        assertEquals("411001", dto.pincode)
        assertEquals(mapOf("k" to "v"), dto.tags)
        assertEquals("false", dto.alreadyExists)
        assertTrue(dto.new)
        assertEquals("txn-99", dto.txnId)
        assertEquals("AADHAAR_OTP", dto.authMethods[0])
    }

    @Test fun `CreateAbhaIdResponse copy equals hashCode and toString`() {
        val dto = createAbhaIdResponse()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("CreateAbhaIdResponse"))
        assertTrue(dto == dto)
        assertTrue(!dto.equals(null))
        assertTrue(!dto.equals("other"))
    }

    @Test fun `CreateAbhaIdResponse copy with changed fields is not equal`() {
        val dto = createAbhaIdResponse()
        val changed = dto.copy(token = "other", new = false, txnId = "txn-100")
        assertEquals("other", changed.token)
        assertEquals("txn-100", changed.txnId)
        assertEquals(false, changed.new)
        assertTrue(dto != changed)
    }

    @Test fun `CreateAbhaIdResponse tolerates null optional fields`() {
        val dto = createAbhaIdResponse().copy(
            healthId = null,
            email = null,
            kycPhoto = null,
            pincode = null,
            tags = null
        )
        assertNull(dto.healthId)
        assertNull(dto.email)
        assertNull(dto.kycPhoto)
        assertNull(dto.pincode)
        assertNull(dto.tags)
        assertNotNull(dto.toString())
        assertNotNull(dto.hashCode())
    }

    @Test fun `CreateAbhaIdResponse txnId is mutable`() {
        val dto = createAbhaIdResponse()
        dto.txnId = "reassigned"
        assertEquals("reassigned", dto.txnId)
    }

    // ---------------- CreateHIDResponse ----------------

    private fun createHidResponse() = CreateHIDResponse(
        hID = 4242L,
        healthIdNumber = "11-2222-3333-4444",
        name = "Asha Devi",
        gender = "F",
        yearOfBirth = "1990",
        monthOfBirth = "05",
        dayOfBirth = "12",
        firstName = "Asha",
        healthId = "asha@abdm",
        lastName = "Devi",
        middleName = "K",
        stateCode = "27",
        districtCode = "512",
        stateName = "Maharashtra",
        districtName = "Pune",
        email = "asha@example.com",
        kycPhoto = "kyc",
        mobile = "9999999999",
        authMethod = "AADHAAR_OTP",
        authMethods = arrayOf("AADHAAR_OTP", "MOBILE_OTP"),
        deleted = false,
        processed = "Y",
        createdBy = "creator",
        txnId = "txn-1"
    )

    @Test fun `CreateHIDResponse exposes constructor values`() {
        val dto = createHidResponse()
        assertEquals(4242L, dto.hID)
        assertEquals("11-2222-3333-4444", dto.healthIdNumber)
        assertEquals("Asha Devi", dto.name)
        assertEquals("F", dto.gender)
        assertEquals("1990", dto.yearOfBirth)
        assertEquals("05", dto.monthOfBirth)
        assertEquals("12", dto.dayOfBirth)
        assertEquals("Asha", dto.firstName)
        assertEquals("asha@abdm", dto.healthId)
        assertEquals("Devi", dto.lastName)
        assertEquals("K", dto.middleName)
        assertEquals("27", dto.stateCode)
        assertEquals("512", dto.districtCode)
        assertEquals("Maharashtra", dto.stateName)
        assertEquals("Pune", dto.districtName)
        assertEquals("asha@example.com", dto.email)
        assertEquals("kyc", dto.kycPhoto)
        assertEquals("9999999999", dto.mobile)
        assertEquals("AADHAAR_OTP", dto.authMethod)
        assertEquals(2, dto.authMethods?.size)
        assertEquals(false, dto.deleted)
        assertEquals("Y", dto.processed)
        assertEquals("creator", dto.createdBy)
        assertEquals("txn-1", dto.txnId)
    }

    @Test fun `CreateHIDResponse copy equals hashCode and toString`() {
        val dto = createHidResponse()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("CreateHIDResponse"))
        assertTrue(dto == dto)
        assertTrue(!dto.equals(null))
        assertTrue(!dto.equals(42))
    }

    @Test fun `CreateHIDResponse copy with changed fields is not equal`() {
        val dto = createHidResponse()
        val changed = dto.copy(hID = 1L, deleted = true, txnId = "txn-2")
        assertEquals(1L, changed.hID)
        assertTrue(changed.deleted)
        assertEquals("txn-2", changed.txnId)
        assertTrue(dto != changed)
    }

    @Test fun `CreateHIDResponse tolerates all null optional fields`() {
        val dto = CreateHIDResponse(
            hID = 1L,
            healthIdNumber = null,
            name = null,
            gender = null,
            yearOfBirth = null,
            monthOfBirth = null,
            dayOfBirth = null,
            firstName = null,
            healthId = null,
            lastName = null,
            middleName = null,
            stateCode = null,
            districtCode = null,
            stateName = null,
            districtName = null,
            email = null,
            kycPhoto = null,
            mobile = null,
            authMethod = null,
            authMethods = null,
            deleted = true,
            processed = null,
            createdBy = null,
            txnId = null
        )
        assertNull(dto.healthIdNumber)
        assertNull(dto.authMethods)
        assertNull(dto.txnId)
        assertTrue(dto.deleted)
        assertNotNull(dto.toString())
        assertNotNull(dto.hashCode())
        assertEquals(dto, dto.copy())
    }

    // ---------------- CreateHealthIdRequest ----------------

    private fun createHealthIdRequest() = CreateHealthIdRequest(
        otp = "123456",
        txnId = "txn-7",
        address = "Line 1",
        dayOfBirth = "12",
        email = "asha@example.com",
        profilePhoto = "photo",
        password = "secret",
        healthId = "asha@abdm",
        healthIdNumber = "11-2222-3333-4444",
        firstName = "Asha",
        gender = "F",
        lastName = "Devi",
        middleName = "K",
        monthOfBirth = "05",
        name = "Asha Devi",
        pincode = 411001,
        yearOfBirth = "1990",
        providerServiceMapID = 15,
        createdBy = "creator"
    )

    @Test fun `CreateHealthIdRequest exposes constructor values`() {
        val dto = createHealthIdRequest()
        assertEquals("123456", dto.otp)
        assertEquals("txn-7", dto.txnId)
        assertEquals("Line 1", dto.address)
        assertEquals("12", dto.dayOfBirth)
        assertEquals("asha@example.com", dto.email)
        assertEquals("photo", dto.profilePhoto)
        assertEquals("secret", dto.password)
        assertEquals("asha@abdm", dto.healthId)
        assertEquals("11-2222-3333-4444", dto.healthIdNumber)
        assertEquals("Asha", dto.firstName)
        assertEquals("F", dto.gender)
        assertEquals("Devi", dto.lastName)
        assertEquals("K", dto.middleName)
        assertEquals("05", dto.monthOfBirth)
        assertEquals("Asha Devi", dto.name)
        assertEquals(411001, dto.pincode)
        assertEquals("1990", dto.yearOfBirth)
        assertEquals(15, dto.providerServiceMapID)
        assertEquals("creator", dto.createdBy)
    }

    @Test fun `CreateHealthIdRequest copy equals hashCode and toString`() {
        val dto = createHealthIdRequest()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("CreateHealthIdRequest"))
        assertTrue(dto == dto)
        assertTrue(!dto.equals(null))
        assertTrue(!dto.equals("nope"))
    }

    @Test fun `CreateHealthIdRequest copy with changed fields is not equal`() {
        val dto = createHealthIdRequest()
        val changed = dto.copy(otp = "654321", pincode = 400001, createdBy = "other")
        assertEquals("654321", changed.otp)
        assertEquals(400001, changed.pincode)
        assertEquals("other", changed.createdBy)
        assertTrue(dto != changed)
    }

    @Test fun `CreateHealthIdRequest tolerates all null fields`() {
        val dto = CreateHealthIdRequest(
            otp = null,
            txnId = null,
            address = null,
            dayOfBirth = null,
            email = null,
            profilePhoto = null,
            password = null,
            healthId = null,
            healthIdNumber = null,
            firstName = null,
            gender = null,
            lastName = null,
            middleName = null,
            monthOfBirth = null,
            name = null,
            pincode = null,
            yearOfBirth = null,
            providerServiceMapID = null,
            createdBy = null
        )
        assertNull(dto.otp)
        assertNull(dto.pincode)
        assertNull(dto.providerServiceMapID)
        assertNotNull(dto.toString())
        assertNotNull(dto.hashCode())
        assertEquals(dto, dto.copy())
    }
}
