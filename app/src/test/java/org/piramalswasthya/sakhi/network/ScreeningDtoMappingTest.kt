package org.piramalswasthya.sakhi.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class ScreeningDtoMappingTest {

    // =====================================================
    // LeprosyScreeningDTO.toCache()
    // =====================================================

    private fun leprosyDto() = LeprosyScreeningDTO(
        benId = 11L,
        homeVisitDate = "2023-01-15",
        leprosyStatusDate = "2023-01-16",
        dateOfDeath = "null",
        houseHoldDetailsId = 500L,
        createdBy = "creator",
        createdDate = "2023-01-10",
        modifiedBy = "modifier",
        lastModDate = "2023-01-11"
    )

    @Test fun `leprosy toCache maps ids`() {
        val cache = leprosyDto().toCache()
        assertEquals(11L, cache.benId)
        assertEquals(500L, cache.houseHoldDetailsId)
    }

    @Test fun `leprosy toCache sets synced state`() {
        assertEquals(SyncState.SYNCED, leprosyDto().toCache().syncState)
    }

    @Test fun `leprosy toCache maps created and modified by`() {
        val cache = leprosyDto().toCache()
        assertEquals("creator", cache.createdBy)
        assertEquals("modifier", cache.modifiedBy)
    }

    @Test fun `leprosy toCache parses valid date to positive long`() {
        assertTrue(leprosyDto().toCache().homeVisitDate > 0L)
    }

    @Test fun `leprosy toCache parses null date literal to zero`() {
        assertEquals(0L, leprosyDto().toCache().dateOfDeath)
    }

    @Test fun `leprosy toCache defaults for visit and confirmation`() {
        val cache = leprosyDto().toCache()
        assertEquals(1, cache.currentVisitNumber)
        assertEquals(false, cache.isConfirmed)
    }

    @Test fun `leprosy toCache passes through symptom fields`() {
        val cache = leprosyDto().copy(
            leprosyStatus = "Positive",
            typeOfLeprosy = "MB",
            weaknessFeet = "Yes",
            weaknessFeetId = 2
        ).toCache()
        assertEquals("Positive", cache.leprosyStatus)
        assertEquals("MB", cache.typeOfLeprosy)
        assertEquals("Yes", cache.weaknessFeet)
        assertEquals(2, cache.weaknessFeetId)
    }

    @Test fun `leprosy toCache defaults optional referral, disease and symptom id fields when not supplied`() {
        val cache = leprosyDto().toCache()
        assertEquals(0, cache.referredTo)
        assertEquals(null, cache.referToName)
        assertEquals(null, cache.otherReferredTo)
        assertEquals(null, cache.typeOfLeprosy)
        assertEquals(null, cache.remarks)
        assertEquals(null, cache.beneficiaryStatus)
        assertEquals(null, cache.placeOfDeath)
        assertEquals(null, cache.otherPlaceOfDeath)
        assertEquals(null, cache.reasonForDeath)
        assertEquals(null, cache.otherReasonForDeath)
        assertEquals(0, cache.diseaseTypeID)
        assertEquals(0, cache.beneficiaryStatusId)
        assertEquals(null, cache.leprosySymptoms)
        assertEquals(1, cache.leprosySymptomsPosition)
        assertEquals(0, cache.lerosyStatusPosition)
        assertEquals("Visit -1", cache.visitLabel)
        assertEquals(1, cache.visitNumber)
        assertEquals("Screening", cache.leprosyState)
        assertEquals(0, cache.totalFollowUpMonthsRequired)
        assertEquals(null, cache.mdtBlisterPackRecived)
        assertEquals(null, cache.treatmentStatus)
        assertTrue(cache.treatmentStartDate > 0L)
        assertTrue(cache.treatmentEndDate > 0L)
    }

    @Test fun `leprosy toCache defaults all clinical symptom fields and ids when not supplied`() {
        val cache = leprosyDto().toCache()
        assertEquals(null, cache.recurrentUlceration)
        assertEquals(1, cache.recurrentUlcerationId)
        assertEquals(null, cache.recurrentTingling)
        assertEquals(1, cache.recurrentTinglingId)
        assertEquals(null, cache.hypopigmentedPatch)
        assertEquals(1, cache.hypopigmentedPatchId)
        assertEquals(null, cache.thickenedSkin)
        assertEquals(1, cache.thickenedSkinId)
        assertEquals(null, cache.skinNodules)
        assertEquals(1, cache.skinNodulesId)
        assertEquals(null, cache.skinPatchDiscoloration)
        assertEquals(1, cache.skinPatchDiscolorationId)
        assertEquals(null, cache.recurrentNumbness)
        assertEquals(1, cache.recurrentNumbnessId)
        assertEquals(null, cache.clawingFingers)
        assertEquals(1, cache.clawingFingersId)
        assertEquals(null, cache.tinglingNumbnessExtremities)
        assertEquals(1, cache.tinglingNumbnessExtremitiesId)
        assertEquals(null, cache.inabilityCloseEyelid)
        assertEquals(1, cache.inabilityCloseEyelidId)
        assertEquals(null, cache.difficultyHoldingObjects)
        assertEquals(1, cache.difficultyHoldingObjectsId)
        assertEquals(null, cache.weaknessFeet)
        assertEquals(1, cache.weaknessFeetId)
    }

    private fun leprosyDtoAllFieldsPopulated() = LeprosyScreeningDTO(
        id = 77,
        benId = 111L,
        homeVisitDate = "2023-05-01",
        leprosyStatusDate = "2023-05-02",
        dateOfDeath = "2023-05-03",
        houseHoldDetailsId = 900L,
        leprosyStatus = "Positive",
        referredTo = 5,
        referToName = "PHC Center",
        otherReferredTo = "Other Facility",
        typeOfLeprosy = "MB",
        remarks = "Some remarks",
        beneficiaryStatus = "Alive",
        placeOfDeath = "Home",
        otherPlaceOfDeath = "N/A",
        reasonForDeath = "Unknown",
        otherReasonForDeath = "None",
        diseaseTypeID = 3,
        beneficiaryStatusId = 1,
        leprosySymptoms = "Patch",
        leprosySymptomsPosition = 2,
        lerosyStatusPosition = 4,
        currentVisitNumber = 2,
        visitLabel = "Visit -2",
        visitNumber = 2,
        isConfirmed = true,
        leprosyState = "Confirmed",
        treatmentStartDate = "2023-05-04",
        totalFollowUpMonthsRequired = 6,
        treatmentEndDate = "2023-11-04",
        mdtBlisterPackRecived = "Yes",
        treatmentStatus = "Ongoing",
        createdBy = "creator2",
        createdDate = "2023-05-05",
        modifiedBy = "modifier2",
        lastModDate = "2023-05-06",
        recurrentUlceration = "Yes",
        recurrentUlcerationId = 2,
        recurrentTingling = "Yes",
        recurrentTinglingId = 2,
        hypopigmentedPatch = "Yes",
        hypopigmentedPatchId = 2,
        thickenedSkin = "Yes",
        thickenedSkinId = 2,
        skinNodules = "Yes",
        skinNodulesId = 2,
        skinPatchDiscoloration = "Yes",
        skinPatchDiscolorationId = 2,
        recurrentNumbness = "Yes",
        recurrentNumbnessId = 2,
        clawingFingers = "Yes",
        clawingFingersId = 2,
        tinglingNumbnessExtremities = "Yes",
        tinglingNumbnessExtremitiesId = 2,
        inabilityCloseEyelid = "Yes",
        inabilityCloseEyelidId = 2,
        difficultyHoldingObjects = "Yes",
        difficultyHoldingObjectsId = 2,
        weaknessFeet = "Yes",
        weaknessFeetId = 2
    )

    @Test fun `leprosy fully populated dto maps referral, disease and beneficiary fields via toCache`() {
        val cache = leprosyDtoAllFieldsPopulated().toCache()
        assertEquals(5, cache.referredTo)
        assertEquals("PHC Center", cache.referToName)
        assertEquals("Other Facility", cache.otherReferredTo)
        assertEquals("MB", cache.typeOfLeprosy)
        assertEquals("Some remarks", cache.remarks)
        assertEquals("Alive", cache.beneficiaryStatus)
        assertEquals("Home", cache.placeOfDeath)
        assertEquals("N/A", cache.otherPlaceOfDeath)
        assertEquals("Unknown", cache.reasonForDeath)
        assertEquals("None", cache.otherReasonForDeath)
        assertEquals(3, cache.diseaseTypeID)
        assertEquals(1, cache.beneficiaryStatusId)
    }

    @Test fun `leprosy fully populated dto maps symptom position, visit and treatment metadata via toCache`() {
        val cache = leprosyDtoAllFieldsPopulated().toCache()
        assertEquals("Patch", cache.leprosySymptoms)
        assertEquals(2, cache.leprosySymptomsPosition)
        assertEquals(4, cache.lerosyStatusPosition)
        assertEquals(2, cache.currentVisitNumber)
        assertEquals("Visit -2", cache.visitLabel)
        assertEquals(2, cache.visitNumber)
        assertEquals(true, cache.isConfirmed)
        assertEquals("Confirmed", cache.leprosyState)
        assertTrue(cache.treatmentStartDate > 0L)
        assertEquals(6, cache.totalFollowUpMonthsRequired)
        assertTrue(cache.treatmentEndDate > 0L)
        assertEquals("Yes", cache.mdtBlisterPackRecived)
        assertEquals("Ongoing", cache.treatmentStatus)
    }

    @Test fun `leprosy fully populated dto maps all clinical symptom fields and ids via toCache`() {
        val cache = leprosyDtoAllFieldsPopulated().toCache()
        assertEquals("Yes", cache.recurrentUlceration)
        assertEquals(2, cache.recurrentUlcerationId)
        assertEquals("Yes", cache.recurrentTingling)
        assertEquals(2, cache.recurrentTinglingId)
        assertEquals("Yes", cache.hypopigmentedPatch)
        assertEquals(2, cache.hypopigmentedPatchId)
        assertEquals("Yes", cache.thickenedSkin)
        assertEquals(2, cache.thickenedSkinId)
        assertEquals("Yes", cache.skinNodules)
        assertEquals(2, cache.skinNodulesId)
        assertEquals("Yes", cache.skinPatchDiscoloration)
        assertEquals(2, cache.skinPatchDiscolorationId)
        assertEquals("Yes", cache.recurrentNumbness)
        assertEquals(2, cache.recurrentNumbnessId)
        assertEquals("Yes", cache.clawingFingers)
        assertEquals(2, cache.clawingFingersId)
        assertEquals("Yes", cache.tinglingNumbnessExtremities)
        assertEquals(2, cache.tinglingNumbnessExtremitiesId)
        assertEquals("Yes", cache.inabilityCloseEyelid)
        assertEquals(2, cache.inabilityCloseEyelidId)
        assertEquals("Yes", cache.difficultyHoldingObjects)
        assertEquals(2, cache.difficultyHoldingObjectsId)
        assertEquals("Yes", cache.weaknessFeet)
        assertEquals(2, cache.weaknessFeetId)
    }

    @Test fun `leprosy toCache passes through explicit null overrides for nullable referral and symptom id fields`() {
        val cache = leprosyDtoAllFieldsPopulated().copy(
            referredTo = null,
            diseaseTypeID = null,
            beneficiaryStatusId = null,
            leprosySymptomsPosition = null,
            lerosyStatusPosition = null,
            recurrentUlcerationId = null,
            weaknessFeetId = null
        ).toCache()
        assertEquals(null, cache.referredTo)
        assertEquals(null, cache.diseaseTypeID)
        assertEquals(null, cache.beneficiaryStatusId)
        assertEquals(null, cache.leprosySymptomsPosition)
        assertEquals(null, cache.lerosyStatusPosition)
        assertEquals(null, cache.recurrentUlcerationId)
        assertEquals(null, cache.weaknessFeetId)
    }

    @Test fun `leprosy toCache maps alternate leprosyState and treatment status values`() {
        val cache = leprosyDtoAllFieldsPopulated().copy(
            leprosyState = "Cured",
            treatmentStatus = "Completed",
            isConfirmed = false
        ).toCache()
        assertEquals("Cured", cache.leprosyState)
        assertEquals("Completed", cache.treatmentStatus)
        assertEquals(false, cache.isConfirmed)
    }

    @Test fun `leprosy toCache parses null date literal for treatment start and end dates`() {
        val cache = leprosyDtoAllFieldsPopulated().copy(
            treatmentStartDate = "null",
            treatmentEndDate = "null"
        ).toCache()
        assertEquals(0L, cache.treatmentStartDate)
        assertEquals(0L, cache.treatmentEndDate)
    }

    @Test fun `leprosy dto constructed with only a subset of optional fields supplied directly leaves the rest defaulted`() {
        val dto = LeprosyScreeningDTO(
            benId = 66L,
            homeVisitDate = "2023-07-01",
            leprosyStatusDate = "2023-07-02",
            dateOfDeath = "null",
            houseHoldDetailsId = 800L,
            createdBy = "creator4",
            createdDate = "2023-07-03",
            modifiedBy = "modifier4",
            lastModDate = "2023-07-04",
            remarks = "supplied remark",
            beneficiaryStatus = "Alive"
        )
        assertEquals("supplied remark", dto.remarks)
        assertEquals("Alive", dto.beneficiaryStatus)
        assertEquals(null, dto.typeOfLeprosy)
        assertEquals(1, dto.weaknessFeetId)
        val cache = dto.toCache()
        assertEquals("supplied remark", cache.remarks)
        assertEquals("Alive", cache.beneficiaryStatus)
    }

    @Test fun `LeprosyScreeningDTO differing only in the last constructor field is unequal after all other fields compare equal`() {
        val base = leprosyDtoAllFieldsPopulated()
        val changed = base.copy(weaknessFeetId = (base.weaknessFeetId ?: 0) + 1)
        assertNotEquals(base, changed)
        assertEquals(base.benId, changed.benId)
        assertEquals(base.weaknessFeet, changed.weaknessFeet)
        assertEquals(base.leprosyState, changed.leprosyState)
    }

    // =====================================================
    // MalariaScreeningDTO.toCache()
    // =====================================================

    private fun malariaDto() = MalariaScreeningDTO(
        benId = 22L,
        visitId = 7L,
        caseDate = "2023-02-01",
        houseHoldDetailsId = 600L,
        screeningDate = "2023-02-02",
        beneficiaryStatus = "Alive",
        dateOfDeath = "null",
        placeOfDeath = "",
        otherPlaceOfDeath = "",
        reasonForDeath = "",
        otherReasonForDeath = "",
        rapidDiagnosticTest = "Negative",
        dateOfRdt = "2023-02-03",
        slideTestName = "Test",
        slideTestPf = "No",
        slideTestPv = "No",
        dateOfSlideTest = "2023-02-04",
        dateOfVisitBySupervisor = "null",
        followUpDate = "2023-02-10",
        caseStatus = "Open"
    )

    @Test fun `malaria toCache maps ids`() {
        val cache = malariaDto().toCache()
        assertEquals(22L, cache.benId)
        assertEquals(7L, cache.visitId)
        assertEquals(600L, cache.houseHoldDetailsId)
    }

    @Test fun `malaria toCache sets synced state`() {
        assertEquals(SyncState.SYNCED, malariaDto().toCache().syncState)
    }

    @Test fun `malaria toCache maps status fields`() {
        val cache = malariaDto().toCache()
        assertEquals("Alive", cache.beneficiaryStatus)
        assertEquals("Open", cache.caseStatus)
    }

    @Test fun `malaria toCache parses screening date to positive long`() {
        assertTrue(malariaDto().toCache().screeningDate > 0L)
    }

    @Test fun `malaria toCache parses null date literal to zero`() {
        assertEquals(0L, malariaDto().toCache().dateOfDeath)
    }

    @Test fun `malaria toCache maps symptom booleans`() {
        val cache = malariaDto().copy(headache = true, vomiting = true).toCache()
        assertEquals(true, cache.headache)
        assertEquals(true, cache.vomiting)
    }

    private fun malariaDtoMinimal() = MalariaScreeningDTO(
        benId = 44L,
        visitId = 3L,
        caseDate = "2023-04-01",
        houseHoldDetailsId = 610L,
        screeningDate = "2023-04-02",
        beneficiaryStatus = "Alive",
        dateOfDeath = "null",
        placeOfDeath = "",
        otherPlaceOfDeath = "",
        reasonForDeath = "",
        otherReasonForDeath = "",
        rapidDiagnosticTest = "Negative",
        dateOfRdt = "2023-04-03",
        slideTestName = "Test",
        slideTestPf = "No",
        slideTestPv = "No",
        dateOfSlideTest = "2023-04-04",
        dateOfVisitBySupervisor = "null",
        followUpDate = "2023-04-10"
    )

    @Test fun `malaria toCache defaults optional referral, disease and symptom fields when not supplied`() {
        val cache = malariaDtoMinimal().toCache()
        assertEquals("", cache.caseStatus)
        assertEquals(0, cache.referredTo)
        assertEquals("null", cache.referToName)
        assertEquals(null, cache.otherReferredFacility)
        assertEquals(null, cache.remarks)
        assertEquals(0, cache.diseaseTypeID)
        assertEquals(0, cache.beneficiaryStatusId)
        assertEquals(false, cache.feverMoreThanTwoWeeks)
        assertEquals(false, cache.fluLikeIllness)
        assertEquals(false, cache.shakingChills)
        assertEquals(false, cache.headache)
        assertEquals(false, cache.muscleAches)
        assertEquals(false, cache.tiredness)
        assertEquals(false, cache.nausea)
        assertEquals(false, cache.vomiting)
        assertEquals(false, cache.diarrhea)
        assertEquals("", cache.createdBy)
        assertEquals(0, cache.malariaTestType)
        assertEquals(0, cache.malariaSlideTestType)
    }

    private fun malariaDtoAllFieldsPopulated() = MalariaScreeningDTO(
        id = 55,
        benId = 222L,
        visitId = 8L,
        caseDate = "2023-06-01",
        houseHoldDetailsId = 650L,
        screeningDate = "2023-06-02",
        beneficiaryStatus = "Alive",
        beneficiaryStatusId = 1,
        dateOfDeath = "2023-06-03",
        placeOfDeath = "Hospital",
        otherPlaceOfDeath = "N/A",
        reasonForDeath = "Unknown",
        otherReasonForDeath = "None",
        rapidDiagnosticTest = "Positive",
        dateOfRdt = "2023-06-04",
        slideTestName = "SlideX",
        slideTestPf = "Yes",
        slideTestPv = "Yes",
        dateOfSlideTest = "2023-06-05",
        dateOfVisitBySupervisor = "2023-06-06",
        caseStatus = "Closed",
        referredTo = 4,
        referToName = "District Hospital",
        otherReferredFacility = "Other Facility",
        remarks = "Follow up needed",
        diseaseTypeID = 2,
        followUpDate = "2023-06-10",
        feverMoreThanTwoWeeks = true,
        fluLikeIllness = true,
        shakingChills = true,
        headache = true,
        muscleAches = true,
        tiredness = true,
        nausea = true,
        vomiting = true,
        diarrhea = true,
        createdBy = "creator3",
        malariaTestType = 1,
        malariaSlideTestType = 1
    )

    @Test fun `malaria fully populated dto maps referral, disease and beneficiary fields via toCache`() {
        val cache = malariaDtoAllFieldsPopulated().toCache()
        assertEquals("Closed", cache.caseStatus)
        assertEquals(4, cache.referredTo)
        assertEquals("District Hospital", cache.referToName)
        assertEquals("Other Facility", cache.otherReferredFacility)
        assertEquals("Follow up needed", cache.remarks)
        assertEquals(2, cache.diseaseTypeID)
        assertEquals(1, cache.beneficiaryStatusId)
        assertEquals("creator3", cache.createdBy)
        assertEquals(1, cache.malariaTestType)
        assertEquals(1, cache.malariaSlideTestType)
    }

    @Test fun `malaria fully populated dto maps all symptom booleans via toCache`() {
        val cache = malariaDtoAllFieldsPopulated().toCache()
        assertEquals(true, cache.feverMoreThanTwoWeeks)
        assertEquals(true, cache.fluLikeIllness)
        assertEquals(true, cache.shakingChills)
        assertEquals(true, cache.headache)
        assertEquals(true, cache.muscleAches)
        assertEquals(true, cache.tiredness)
        assertEquals(true, cache.nausea)
        assertEquals(true, cache.vomiting)
        assertEquals(true, cache.diarrhea)
    }

    @Test fun `malaria fully populated dto maps place and reason of death via toCache`() {
        val cache = malariaDtoAllFieldsPopulated().toCache()
        assertEquals("Hospital", cache.placeOfDeath)
        assertEquals("N/A", cache.otherPlaceOfDeath)
        assertEquals("Unknown", cache.reasonForDeath)
        assertEquals("None", cache.otherReasonForDeath)
        assertTrue(cache.dateOfVisitBySupervisor > 0L)
    }

    @Test fun `malaria toCache passes through explicit null overrides for nullable symptom and referral fields`() {
        val cache = malariaDtoAllFieldsPopulated().copy(
            referredTo = null,
            diseaseTypeID = null,
            feverMoreThanTwoWeeks = null,
            fluLikeIllness = null,
            createdBy = null,
            malariaTestType = null,
            malariaSlideTestType = null
        ).toCache()
        assertEquals(null, cache.referredTo)
        assertEquals(null, cache.diseaseTypeID)
        assertEquals(null, cache.feverMoreThanTwoWeeks)
        assertEquals(null, cache.fluLikeIllness)
        assertEquals(null, cache.createdBy)
        assertEquals(null, cache.malariaTestType)
        assertEquals(null, cache.malariaSlideTestType)
    }

    @Test fun `malaria dto constructed with only a subset of optional fields supplied directly leaves the rest defaulted`() {
        val dto = MalariaScreeningDTO(
            benId = 88L,
            visitId = 9L,
            caseDate = "2023-08-01",
            houseHoldDetailsId = 900L,
            screeningDate = "2023-08-02",
            beneficiaryStatus = "Alive",
            dateOfDeath = "null",
            placeOfDeath = "",
            otherPlaceOfDeath = "",
            reasonForDeath = "",
            otherReasonForDeath = "",
            rapidDiagnosticTest = "Negative",
            dateOfRdt = "2023-08-03",
            slideTestName = "Test",
            slideTestPf = "No",
            slideTestPv = "No",
            dateOfSlideTest = "2023-08-04",
            dateOfVisitBySupervisor = "null",
            followUpDate = "2023-08-10",
            remarks = "supplied remark",
            diseaseTypeID = 7
        )
        assertEquals("supplied remark", dto.remarks)
        assertEquals(7, dto.diseaseTypeID)
        assertEquals("", dto.caseStatus)
        assertEquals(false, dto.headache)
        val cache = dto.toCache()
        assertEquals("supplied remark", cache.remarks)
        assertEquals(7, cache.diseaseTypeID)
    }

    @Test fun `malaria toCache maps alternate case status and boolean symptom combination`() {
        val cache = malariaDtoAllFieldsPopulated().copy(
            caseStatus = "Reopened",
            feverMoreThanTwoWeeks = false,
            shakingChills = false,
            muscleAches = false
        ).toCache()
        assertEquals("Reopened", cache.caseStatus)
        assertEquals(false, cache.feverMoreThanTwoWeeks)
        assertEquals(false, cache.shakingChills)
        assertEquals(false, cache.muscleAches)
    }

    @Test fun `MalariaScreeningDTO differing only in the last constructor field is unequal after all other fields compare equal`() {
        val base = malariaDtoAllFieldsPopulated()
        val changed = base.copy(malariaSlideTestType = (base.malariaSlideTestType ?: 0) + 1)
        assertNotEquals(base, changed)
        assertEquals(base.benId, changed.benId)
        assertEquals(base.diarrhea, changed.diarrhea)
        assertEquals(base.caseStatus, changed.caseStatus)
    }

    // =====================================================
    // MalariaConfirmedDTO.toCache()
    // =====================================================

    private fun confirmedDto() = MalariaConfirmedDTO(
        diseaseId = 9,
        benId = 33L,
        houseHoldDetailsId = 700L,
        dateOfDiagnosis = "2023-03-01",
        treatmentStartDate = "2023-03-02",
        treatmentCompletionDate = "2023-03-20",
        treatmentGiven = "ACT",
        referralDate = "null",
        day = "3"
    )

    @Test fun `confirmed toCache maps ids`() {
        val cache = confirmedDto().toCache()
        assertEquals(33L, cache.benId)
        assertEquals(700L, cache.houseHoldDetailsId)
        assertEquals(9, cache.diseaseId)
    }

    @Test fun `confirmed toCache maps treatment and day`() {
        val cache = confirmedDto().toCache()
        assertEquals("ACT", cache.treatmentGiven)
        assertEquals("3", cache.day)
    }

    @Test fun `confirmed toCache sets synced state`() {
        assertEquals(SyncState.SYNCED, confirmedDto().toCache().syncState)
    }

    @Test fun `confirmed toCache parses diagnosis date to positive long`() {
        assertTrue(confirmedDto().toCache().dateOfDiagnosis > 0L)
    }

    // =====================================================
    // Generated data-class members (equals / hashCode / toString / copy)
    // =====================================================

    @Test fun `LeprosyScreeningDTO generated members behave consistently`() {
        val dto = leprosyDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("LeprosyScreeningDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals("other"))
        assertNotEquals(dto, dto.copy(benId = 12L))
        assertEquals(11L, dto.benId)
        assertEquals(500L, dto.houseHoldDetailsId)
        assertEquals("2023-01-15", dto.homeVisitDate)
        assertEquals("2023-01-16", dto.leprosyStatusDate)
        assertEquals("null", dto.dateOfDeath)
        assertEquals("creator", dto.createdBy)
        assertEquals("2023-01-10", dto.createdDate)
        assertEquals("modifier", dto.modifiedBy)
        assertEquals("2023-01-11", dto.lastModDate)
    }

    @Test fun `LeprosyScreeningDTO copy overrides only named fields`() {
        val changed = leprosyDto().copy(leprosyStatus = "Negative", typeOfLeprosy = "PB")
        assertEquals("Negative", changed.leprosyStatus)
        assertEquals("PB", changed.typeOfLeprosy)
        assertEquals(11L, changed.benId)
        assertEquals("creator", changed.createdBy)
        assertNotEquals(leprosyDto(), changed)
    }

    @Test fun `LeprosyScreeningDTO fully populated instance generated members behave consistently`() {
        val dto = leprosyDtoAllFieldsPopulated()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertNotEquals(leprosyDto(), dto)
        assertEquals(77, dto.id)
        assertEquals("Positive", dto.leprosyStatus)
        assertEquals(true, dto.isConfirmed)
        assertEquals("Confirmed", dto.leprosyState)
    }

    @Test fun `LeprosyScreeningDTO copy overrides clinical symptom fields independently`() {
        val base = leprosyDtoAllFieldsPopulated()
        val changed = base.copy(
            clawingFingers = "No",
            clawingFingersId = 1,
            weaknessFeet = "No",
            weaknessFeetId = 1
        )
        assertEquals("No", changed.clawingFingers)
        assertEquals(1, changed.clawingFingersId)
        assertEquals("No", changed.weaknessFeet)
        assertEquals(1, changed.weaknessFeetId)
        assertEquals("Yes", changed.recurrentUlceration)
        assertEquals(base.benId, changed.benId)
        assertNotEquals(base, changed)
    }

    @Test fun `MalariaScreeningDTO generated members behave consistently`() {
        val dto = malariaDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("MalariaScreeningDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals(7))
        assertNotEquals(dto, dto.copy(benId = 23L))
        assertEquals(22L, dto.benId)
        assertEquals(7L, dto.visitId)
        assertEquals(600L, dto.houseHoldDetailsId)
        assertEquals("Alive", dto.beneficiaryStatus)
        assertEquals("Negative", dto.rapidDiagnosticTest)
        assertEquals("Test", dto.slideTestName)
        assertEquals("No", dto.slideTestPf)
        assertEquals("No", dto.slideTestPv)
        assertEquals("Open", dto.caseStatus)
    }

    @Test fun `MalariaScreeningDTO toCache maps slide test and follow up details`() {
        val cache = malariaDto().toCache()
        assertEquals("Negative", cache.rapidDiagnosticTest)
        assertEquals("Test", cache.slideTestName)
        assertEquals("No", cache.slideTestPf)
        assertEquals("No", cache.slideTestPv)
        assertTrue(cache.dateOfRdt > 0L)
        assertTrue(cache.dateOfSlideTest > 0L)
        assertTrue(cache.followUpDate > 0L)
        assertEquals(0L, cache.dateOfVisitBySupervisor)
    }

    @Test fun `MalariaScreeningDTO fully populated instance generated members behave consistently`() {
        val dto = malariaDtoAllFieldsPopulated()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertNotEquals(malariaDto(), dto)
        assertEquals(55, dto.id)
        assertEquals("Closed", dto.caseStatus)
        assertEquals(true, dto.diarrhea)
    }

    @Test fun `MalariaScreeningDTO copy overrides symptom and disease fields independently`() {
        val base = malariaDtoAllFieldsPopulated()
        val changed = base.copy(diseaseTypeID = 9, diarrhea = false, nausea = false)
        assertEquals(9, changed.diseaseTypeID)
        assertEquals(false, changed.diarrhea)
        assertEquals(false, changed.nausea)
        assertEquals(true, changed.fluLikeIllness)
        assertEquals(base.benId, changed.benId)
        assertNotEquals(base, changed)
    }

    @Test fun `MalariaConfirmedDTO generated members behave consistently`() {
        val dto = confirmedDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("MalariaConfirmedDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals("x"))
        assertNotEquals(dto, dto.copy(benId = 34L))
        assertEquals(9, dto.diseaseId)
        assertEquals(33L, dto.benId)
        assertEquals(700L, dto.houseHoldDetailsId)
        assertEquals("ACT", dto.treatmentGiven)
        assertEquals("3", dto.day)
    }
}
