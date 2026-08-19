package org.piramalswasthya.sakhi.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.ReferralRequest

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

    @Test fun `LeprosyFollowUpDTO toCache maps lastModDate to positive long`() {
        assertTrue(leprosyFollowUpDto().toCache().lastModDate > 0L)
    }

    @Test fun `LeprosyFollowUpDTO constructor applies default values for optional fields`() {
        val dto = leprosyFollowUpDto()
        assertNull(dto.treatmentStatus)
        assertNull(dto.mdtBlisterPackReceived)
        assertNull(dto.remarks)
        assertNull(dto.leprosySymptoms)
        assertNull(dto.typeOfLeprosy)
        assertNull(dto.referToName)
        assertNull(dto.mdtBlisterPackRecived)
        assertEquals(1, dto.leprosySymptomsPosition)
        assertEquals("Visit -1", dto.visitLabel)
        assertEquals("", dto.leprosyStatus)
        assertEquals(0, dto.referredTo)
    }

    @Test fun `LeprosyFollowUpDTO toCache passes through null optional fields as null`() {
        val cache = leprosyFollowUpDto().toCache()
        assertNull(cache.treatmentStatus)
        assertNull(cache.mdtBlisterPackReceived)
        assertNull(cache.remarks)
        assertNull(cache.leprosySymptoms)
        assertNull(cache.typeOfLeprosy)
        assertNull(cache.referToName)
        assertNull(cache.mdtBlisterPackRecived)
    }

    @Test fun `LeprosyFollowUpDTO toCache passes through default numeric and label fields`() {
        val cache = leprosyFollowUpDto().toCache()
        assertEquals(1, cache.leprosySymptomsPosition)
        assertEquals("Visit -1", cache.visitLabel)
        assertEquals("", cache.leprosyStatus)
        assertEquals(0, cache.referredTo)
    }

    @Test fun `LeprosyFollowUpDTO toCache tolerates null referredTo leprosyStatus and leprosySymptomsPosition`() {
        val cache = leprosyFollowUpDto().copy(
            referredTo = null,
            leprosyStatus = null,
            leprosySymptomsPosition = null
        ).toCache()
        assertNull(cache.referredTo)
        assertNull(cache.leprosyStatus)
        assertNull(cache.leprosySymptomsPosition)
    }

    @Test fun `LeprosyFollowUpDTO toCache maps followUpDate and homeVisitDate to positive longs`() {
        val cache = leprosyFollowUpDto().copy(
            followUpDate = "2023-03-01",
            homeVisitDate = "2023-03-02"
        ).toCache()
        assertTrue(cache.followUpDate > 0L)
        assertTrue(cache.homeVisitDate > 0L)
    }

    @Test fun `LeprosyFollowUpDTO toCache maps treatmentStartDate treatmentEndDate and treatmentCompleteDate to positive longs`() {
        val cache = leprosyFollowUpDto().copy(
            treatmentStartDate = "2023-03-03",
            treatmentEndDate = "2023-03-04",
            treatmentCompleteDate = "2023-03-05"
        ).toCache()
        assertTrue(cache.treatmentStartDate > 0L)
        assertTrue(cache.treatmentEndDate > 0L)
        assertTrue(cache.treatmentCompleteDate > 0L)
    }

    @Test fun `LeprosyFollowUpDTO toCache passes through mdt remarks and symptoms fields`() {
        val cache = leprosyFollowUpDto().copy(
            mdtBlisterPackReceived = "Y",
            mdtBlisterPackRecived = "N",
            remarks = "all good",
            leprosySymptoms = "numbness"
        ).toCache()
        assertEquals("Y", cache.mdtBlisterPackReceived)
        assertEquals("N", cache.mdtBlisterPackRecived)
        assertEquals("all good", cache.remarks)
        assertEquals("numbness", cache.leprosySymptoms)
    }

    @Test fun `LeprosyFollowUpDTO copy equals hashCode and toString`() {
        val dto = leprosyFollowUpDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("LeprosyFollowUpDTO"))
        assertTrue(dto == dto)
        assertTrue(!dto.equals(null))
        assertTrue(!dto.equals("other"))
    }

    @Test fun `LeprosyFollowUpDTO copy with changed fields is not equal`() {
        val dto = leprosyFollowUpDto()
        val changed = dto.copy(visitNumber = 9, leprosyStatus = "Negative")
        assertEquals(9, changed.visitNumber)
        assertEquals("Negative", changed.leprosyStatus)
        assertTrue(dto != changed)
    }

    private fun leprosyFollowUpDtoFull() = LeprosyFollowUpDTO(
        benId = 155L,
        visitNumber = 5,
        followUpDate = "2023-09-01",
        treatmentStatus = "Ongoing",
        mdtBlisterPackReceived = "Yes",
        treatmentCompleteDate = "2023-09-02",
        remarks = "all clear",
        homeVisitDate = "2023-09-03",
        leprosySymptoms = "patch",
        typeOfLeprosy = "MB",
        leprosySymptomsPosition = 3,
        visitLabel = "Visit -5",
        leprosyStatus = "Positive",
        referredTo = 6,
        referToName = "District Hospital",
        treatmentEndDate = "2023-09-04",
        mdtBlisterPackRecived = "No",
        createdBy = "creator5",
        createdDate = "2023-09-05",
        modifiedBy = "modifier5",
        lastModDate = "2023-09-06",
        treatmentStartDate = "2023-09-07"
    )

    @Test fun `LeprosyFollowUpDTO fully populated dto maps every field via toCache`() {
        val cache = leprosyFollowUpDtoFull().toCache()
        assertEquals(155L, cache.benId)
        assertEquals(5, cache.visitNumber)
        assertEquals("Ongoing", cache.treatmentStatus)
        assertEquals("Yes", cache.mdtBlisterPackReceived)
        assertEquals("all clear", cache.remarks)
        assertEquals("patch", cache.leprosySymptoms)
        assertEquals("MB", cache.typeOfLeprosy)
        assertEquals(3, cache.leprosySymptomsPosition)
        assertEquals("Visit -5", cache.visitLabel)
        assertEquals("Positive", cache.leprosyStatus)
        assertEquals(6, cache.referredTo)
        assertEquals("District Hospital", cache.referToName)
        assertEquals("No", cache.mdtBlisterPackRecived)
        assertEquals("creator5", cache.createdBy)
        assertEquals("modifier5", cache.modifiedBy)
    }

    @Test fun `LeprosyFollowUpDTO differing only in the last constructor field is unequal after all other fields compare equal`() {
        val base = leprosyFollowUpDtoFull()
        val changed = base.copy(treatmentStartDate = "2023-09-08")
        assertNotEquals(base, changed)
        assertEquals(base.benId, changed.benId)
        assertEquals(base.visitNumber, changed.visitNumber)
        assertEquals(base.leprosyStatus, changed.leprosyStatus)
        assertNotEquals(base.hashCode(), changed.hashCode())
    }

    @Test fun `LeprosyFollowUpDTO copy overrides visitLabel independently`() {
        val changed = leprosyFollowUpDtoFull().copy(visitLabel = "Visit -6")
        assertEquals("Visit -6", changed.visitLabel)
        assertEquals(155L, changed.benId)
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

    // ---------------- AddHealthIdRecord ----------------

    private fun abhaProfileSample() = ABHAProfile(
        firstName = "Asha",
        lastName = "Devi",
        dob = "1990-05-12",
        gender = "F",
        mobile = "9999999999",
        email = "asha@example.com",
        ABHANumber = "11-2222-3333-4444",
        abhaStatus = "ACTIVE"
    )

    private fun addHealthIdRecord() = AddHealthIdRecord(
        healthId = "asha@abdm",
        healthIdNumber = "11-2222-3333-4444",
        providerServiceMapId = 7,
        createdBy = "creator",
        message = "created",
        txnId = "txn-1",
        ABHAProfile = abhaProfileSample(),
        isNew = true
    )

    @Test fun `AddHealthIdRecord exposes constructor values`() {
        val dto = addHealthIdRecord()
        assertEquals("asha@abdm", dto.healthId)
        assertEquals("11-2222-3333-4444", dto.healthIdNumber)
        assertEquals(7, dto.providerServiceMapId)
        assertEquals("creator", dto.createdBy)
        assertEquals("created", dto.message)
        assertEquals("txn-1", dto.txnId)
        assertEquals("Asha", dto.ABHAProfile?.firstName)
        assertEquals(true, dto.isNew)
    }

    @Test fun `AddHealthIdRecord tolerates all null optional fields`() {
        val dto = AddHealthIdRecord(
            healthId = null,
            healthIdNumber = null,
            providerServiceMapId = null,
            createdBy = null,
            message = null,
            txnId = null,
            ABHAProfile = null,
            isNew = null
        )
        assertNull(dto.healthId)
        assertNull(dto.healthIdNumber)
        assertNull(dto.providerServiceMapId)
        assertNull(dto.createdBy)
        assertNull(dto.message)
        assertNull(dto.txnId)
        assertNull(dto.ABHAProfile)
        assertNull(dto.isNew)
    }

    @Test fun `AddHealthIdRecord isNew false is distinct from null and true`() {
        val whenTrue = addHealthIdRecord()
        val whenFalse = addHealthIdRecord().copy(isNew = false)
        val whenNull = addHealthIdRecord().copy(isNew = null)
        assertEquals(true, whenTrue.isNew)
        assertEquals(false, whenFalse.isNew)
        assertNull(whenNull.isNew)
        assertNotEquals(whenTrue, whenFalse)
        assertNotEquals(whenFalse, whenNull)
    }

    @Test fun `AddHealthIdRecord var fields are mutable`() {
        val dto = addHealthIdRecord()
        dto.providerServiceMapId = 42
        dto.createdBy = "other creator"
        dto.message = "updated"
        dto.txnId = "txn-2"
        dto.isNew = false
        dto.ABHAProfile = null
        assertEquals(42, dto.providerServiceMapId)
        assertEquals("other creator", dto.createdBy)
        assertEquals("updated", dto.message)
        assertEquals("txn-2", dto.txnId)
        assertEquals(false, dto.isNew)
        assertNull(dto.ABHAProfile)
    }

    @Test fun `AddHealthIdRecord copy equals hashCode and toString`() {
        val dto = addHealthIdRecord()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("AddHealthIdRecord"))
        assertTrue(dto == dto)
        assertTrue(!dto.equals(null))
        assertTrue(!dto.equals("other"))
    }

    @Test fun `AddHealthIdRecord differing only in the last constructor field is unequal after all other fields compare equal`() {
        val base = addHealthIdRecord()
        val changed = base.copy(isNew = false)
        assertNotEquals(base, changed)
        assertEquals(base.healthId, changed.healthId)
        assertEquals(base.txnId, changed.txnId)
        assertEquals(base.ABHAProfile, changed.ABHAProfile)
    }

    // ---------------- LoginVerifyOtpResponse ----------------

    private fun accountSample(verified: Boolean = true) = Accounts(
        ABHANumber = "11-2222-3333-4444",
        preferredAbhaAddress = "asha@abdm",
        name = "Asha Devi",
        status = "ACTIVE",
        profilePhoto = "photo",
        mobileVerified = verified
    )

    private fun loginVerifyOtpResponse() = LoginVerifyOtpResponse(
        txnId = "txn-1",
        authResult = "success",
        message = "OTP verified",
        token = "tok",
        expiresIn = 3600L,
        refreshToken = "rtok",
        refreshExpiresIn = 7200L,
        accounts = listOf(accountSample())
    )

    @Test fun `LoginVerifyOtpResponse exposes constructor values`() {
        val dto = loginVerifyOtpResponse()
        assertEquals("txn-1", dto.txnId)
        assertEquals("success", dto.authResult)
        assertEquals("OTP verified", dto.message)
        assertEquals("tok", dto.token)
        assertEquals(3600L, dto.expiresIn)
        assertEquals("rtok", dto.refreshToken)
        assertEquals(7200L, dto.refreshExpiresIn)
        assertEquals(1, dto.accounts.size)
        assertEquals("Asha Devi", dto.accounts[0].name)
        assertTrue(dto.accounts[0].mobileVerified)
    }

    @Test fun `LoginVerifyOtpResponse tolerates an empty accounts list`() {
        val dto = loginVerifyOtpResponse().copy(accounts = emptyList())
        assertTrue(dto.accounts.isEmpty())
    }

    @Test fun `LoginVerifyOtpResponse holds multiple accounts with differing verification status`() {
        val dto = loginVerifyOtpResponse().copy(
            accounts = listOf(accountSample(verified = true), accountSample(verified = false))
        )
        assertEquals(2, dto.accounts.size)
        assertTrue(dto.accounts[0].mobileVerified)
        assertFalse(dto.accounts[1].mobileVerified)
    }

    @Test fun `LoginVerifyOtpResponse copy equals hashCode and toString`() {
        val dto = loginVerifyOtpResponse()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("LoginVerifyOtpResponse"))
        assertTrue(dto == dto)
        assertTrue(!dto.equals(null))
        assertTrue(!dto.equals("other"))
    }

    @Test fun `LoginVerifyOtpResponse differing only in the last constructor field is unequal after all other fields compare equal`() {
        val base = loginVerifyOtpResponse()
        val changed = base.copy(accounts = listOf(accountSample(verified = false)))
        assertNotEquals(base, changed)
        assertEquals(base.txnId, changed.txnId)
        assertEquals(base.token, changed.token)
        assertEquals(base.expiresIn, changed.expiresIn)
    }

    @Test fun `TmcAuthUserRequest constructor uses defaults when authKey and doLogout omitted`() {
        val request = TmcAuthUserRequest(
            userName = "asha_user",
            password = "secret"
        )
        assertEquals("asha_user", request.userName)
        assertEquals("secret", request.password)
        assertEquals("", request.authKey)
        assertTrue(request.doLogout)
    }

    @Test fun `ReferralRequest exposes constructor value`() {
        val ncdReferal = NCDReferalDTO(
            benId = 1L,
            referredToInstituteID = null,
            refrredToAdditionalServiceList = null,
            referredToInstituteName = null,
            referralReason = null,
            revisitDate = "2023-01-01",
            vanID = null,
            parkingPlaceID = null,
            beneficiaryRegID = null,
            benVisitID = null,
            visitCode = null,
            providerServiceMapID = null,
            createdBy = null,
            type = null
        )
        val request = ReferralRequest(refer = ncdReferal)
        assertEquals(ncdReferal, request.refer)
    }

    @Test fun `AdolescentHealthRequestDTO exposes constructor values`() {
        val dto = AdolescentHealthRequestDTO(userId = 5, adolescentHealths = emptyList())
        assertEquals(5, dto.userId)
        assertTrue(dto.adolescentHealths.isEmpty())
    }

    @Test fun `FilariaScreeningRequestDTO exposes constructor values`() {
        val dto = FilariaScreeningRequestDTO(userId = 6, filariaLists = emptyList())
        assertEquals(6, dto.userId)
    }

    @Test fun `GetCBACRequest exposes constructor value`() {
        val dto = GetCBACRequest(createdBy = "creator")
        assertEquals("creator", dto.createdBy)
    }

    @Test fun `KalaAzarScreeningRequestDTO exposes constructor values`() {
        val dto = KalaAzarScreeningRequestDTO(userId = 7, kalaAzarLists = emptyList())
        assertEquals(7, dto.userId)
    }

    @Test fun `LeprosyScreeningRequestDTO exposes constructor values`() {
        val dto = LeprosyScreeningRequestDTO(userId = 8, leprosyLists = emptyList())
        assertEquals(8, dto.userId)
    }

    @Test fun `MalariaConfirmedRequestDTO exposes constructor values`() {
        val dto = MalariaConfirmedRequestDTO(userId = 9, malariaFollowListUp = emptyList())
        assertEquals(9, dto.userId)
    }

    @Test fun `MalariaScreeningRequestDTO exposes constructor values`() {
        val dto = MalariaScreeningRequestDTO(userId = 10, malariaLists = emptyList())
        assertEquals(10, dto.userId)
    }

    @Test fun `TBConfirmedRequestDTO exposes constructor values`() {
        val dto = TBConfirmedRequestDTO(userId = 11, tbConfirmedList = emptyList())
        assertEquals(11, dto.userId)
    }

    @Test fun `TBScreeningRequestDTO exposes constructor values`() {
        val dto = TBScreeningRequestDTO(userId = 12, tbScreeningList = emptyList())
        assertEquals(12, dto.userId)
    }

    @Test fun `TBSuspectedRequestDTO exposes constructor values`() {
        val dto = TBSuspectedRequestDTO(userId = 13, tbSuspectedList = emptyList())
        assertEquals(13, dto.userId)
    }

    @Test fun `sendOtpRequest exposes constructor value`() {
        val dto = sendOtpRequest(mobNo = "9999999999")
        assertEquals("9999999999", dto.mobNo)
    }

    @Test fun `AESScreeningRequestDTO exposes constructor values`() {
        val dto = AESScreeningRequestDTO(userId = 14, aesJeLists = emptyList())
        assertEquals(14, dto.userId)
        assertTrue(dto.aesJeLists.isEmpty())
    }

    @Test fun `GetBenHealthIdRequest exposes constructor values`() {
        val dto = GetBenHealthIdRequest(beneficiaryRegID = 100L, beneficiaryID = 200L)
        assertEquals(100L, dto.beneficiaryRegID)
        assertEquals(200L, dto.beneficiaryID)
    }

    @Test fun `GetVHNDRequest exposes constructor values`() {
        val dto = GetVHNDRequest(formType = "VHND", userId = 15)
        assertEquals("VHND", dto.formType)
        assertEquals(15, dto.userId)
    }

    @Test fun `UserDataDTO exposes constructor values`() {
        val dto = UserDataDTO(userId = 16, entries = listOf("x"))
        assertEquals(16, dto.userId)
        assertEquals(listOf("x"), dto.entries)
    }

    @Test fun `UserDetailsByAyushmanCardNoRequest exposes constructor values`() {
        val dto = UserDetailsByAyushmanCardNoRequest(cardNo = "card-1", houseHoldId = "hh-1")
        assertEquals("card-1", dto.cardNo)
        assertEquals("hh-1", dto.houseHoldId)
    }

    @Test fun `ValidateOtpRequest exposes constructor values`() {
        val dto = ValidateOtpRequest(otp = 123456, mobNo = "9999999999")
        assertEquals(123456, dto.otp)
        assertEquals("9999999999", dto.mobNo)
    }

    @Test fun `IRSScreeningRequestDTO exposes constructor value`() {
        val dto = IRSScreeningRequestDTO(rounds = emptyList())
        assertTrue(dto.rounds.isEmpty())
    }

    @Test fun `TmcUserDetailsRequest exposes constructor value`() {
        val dto = TmcUserDetailsRequest(userID = 17)
        assertEquals(17, dto.userID)
    }

    @Test fun `AbhaTokenRequest constructor uses default grantType when omitted`() {
        val request = AbhaTokenRequest(
            clientId = "id",
            clientSecret = "secret"
        )
        assertEquals("client_credentials", request.grantType)
        assertEquals("id", request.clientId)
        assertEquals("secret", request.clientSecret)
    }

    @Test fun `Accounts exposes constructor values and generated members`() {
        val account = accountSample()
        assertEquals("11-2222-3333-4444", account.ABHANumber)
        assertEquals("asha@abdm", account.preferredAbhaAddress)
        assertEquals("Asha Devi", account.name)
        assertEquals("ACTIVE", account.status)
        assertEquals("photo", account.profilePhoto)
        assertTrue(account.mobileVerified)
        val same = account.copy()
        assertEquals(account, same)
        assertEquals(account.hashCode(), same.hashCode())
        assertNotEquals(account, account.copy(mobileVerified = false))
    }
}
