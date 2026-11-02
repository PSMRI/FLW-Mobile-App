package org.piramalswasthya.sakhi.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import java.util.TimeZone

class ScreeningDtoMutationTest {

    private fun leprosy() = LeprosyScreeningDTO(
        benId = 71L,
        homeVisitDate = "2023-03-01",
        leprosyStatusDate = "2023-03-02",
        dateOfDeath = "null",
        houseHoldDetailsId = 900L,
        createdBy = "creator",
        createdDate = "2023-03-03",
        modifiedBy = "modifier",
        lastModDate = "2023-03-04"
    )

    @Test
    fun `LeprosyScreeningDTO referral and death properties round trip through setters`() {
        val dto = leprosy()
        dto.leprosyStatus = "Suspected"
        dto.referredTo = 12
        dto.referToName = "PHC Rampur"
        dto.otherReferredTo = "District Hospital"
        dto.typeOfLeprosy = "PB"
        dto.remarks = "needs follow up"
        dto.beneficiaryStatus = "Dead"
        dto.placeOfDeath = "Home"
        dto.otherPlaceOfDeath = "In transit"
        dto.reasonForDeath = "Leprosy"
        dto.otherReasonForDeath = "Complications"
        dto.diseaseTypeID = 4
        dto.beneficiaryStatusId = 3

        assertEquals("Suspected", dto.leprosyStatus)
        assertEquals(12, dto.referredTo)
        assertEquals("PHC Rampur", dto.referToName)
        assertEquals("District Hospital", dto.otherReferredTo)
        assertEquals("PB", dto.typeOfLeprosy)
        assertEquals("needs follow up", dto.remarks)
        assertEquals("Dead", dto.beneficiaryStatus)
        assertEquals("Home", dto.placeOfDeath)
        assertEquals("In transit", dto.otherPlaceOfDeath)
        assertEquals("Leprosy", dto.reasonForDeath)
        assertEquals("Complications", dto.otherReasonForDeath)
        assertEquals(4, dto.diseaseTypeID)
        assertEquals(3, dto.beneficiaryStatusId)

        val cache = dto.toCache()
        assertEquals("Suspected", cache.leprosyStatus)
        assertEquals(12, cache.referredTo)
        assertEquals("PHC Rampur", cache.referToName)
        assertEquals("District Hospital", cache.otherReferredTo)
        assertEquals("Dead", cache.beneficiaryStatus)
        assertEquals(4, cache.diseaseTypeID)
        assertEquals(3, cache.beneficiaryStatusId)
    }

    @Test
    fun `LeprosyScreeningDTO visit and treatment properties round trip through setters`() {
        val dto = leprosy()
        dto.leprosySymptoms = "Patch on arm"
        dto.leprosySymptomsPosition = 5
        dto.lerosyStatusPosition = 6
        dto.currentVisitNumber = 3
        dto.visitLabel = "Visit -3"
        dto.visitNumber = 3
        dto.isConfirmed = true
        dto.leprosyState = "Treatment"
        dto.treatmentStartDate = "2023-04-01"
        dto.totalFollowUpMonthsRequired = 12
        dto.treatmentEndDate = "2024-04-01"
        dto.mdtBlisterPackRecived = "Yes"
        dto.treatmentStatus = "Ongoing"

        assertEquals("Patch on arm", dto.leprosySymptoms)
        assertEquals(5, dto.leprosySymptomsPosition)
        assertEquals(6, dto.lerosyStatusPosition)
        assertEquals(3, dto.currentVisitNumber)
        assertEquals("Visit -3", dto.visitLabel)
        assertEquals(3, dto.visitNumber)
        assertTrue(dto.isConfirmed)
        assertEquals("Treatment", dto.leprosyState)
        assertEquals("2023-04-01", dto.treatmentStartDate)
        assertEquals(12, dto.totalFollowUpMonthsRequired)
        assertEquals("2024-04-01", dto.treatmentEndDate)
        assertEquals("Yes", dto.mdtBlisterPackRecived)
        assertEquals("Ongoing", dto.treatmentStatus)

        val cache = dto.toCache()
        assertEquals(3, cache.currentVisitNumber)
        assertEquals("Visit -3", cache.visitLabel)
        assertTrue(cache.isConfirmed)
        assertEquals("Treatment", cache.leprosyState)
        assertEquals(12, cache.totalFollowUpMonthsRequired)
        assertTrue(cache.treatmentStartDate > 0L)
        assertTrue(cache.treatmentEndDate > 0L)
    }

    @Test
    fun `LeprosyScreeningDTO clinical symptom properties round trip through setters`() {
        val dto = leprosy()
        dto.recurrentUlceration = "Yes"
        dto.recurrentUlcerationId = 2
        dto.recurrentTingling = "No"
        dto.recurrentTinglingId = 3
        dto.hypopigmentedPatch = "Yes"
        dto.hypopigmentedPatchId = 4
        dto.thickenedSkin = "No"
        dto.thickenedSkinId = 5
        dto.skinNodules = "Yes"
        dto.skinNodulesId = 6
        dto.skinPatchDiscoloration = "No"
        dto.skinPatchDiscolorationId = 7
        dto.recurrentNumbness = "Yes"
        dto.recurrentNumbnessId = 8
        dto.clawingFingers = "No"
        dto.clawingFingersId = 9
        dto.tinglingNumbnessExtremities = "Yes"
        dto.tinglingNumbnessExtremitiesId = 10
        dto.inabilityCloseEyelid = "No"
        dto.inabilityCloseEyelidId = 11
        dto.difficultyHoldingObjects = "Yes"
        dto.difficultyHoldingObjectsId = 12
        dto.weaknessFeet = "No"
        dto.weaknessFeetId = 13

        assertEquals("Yes", dto.recurrentUlceration)
        assertEquals(2, dto.recurrentUlcerationId)
        assertEquals("No", dto.recurrentTingling)
        assertEquals(3, dto.recurrentTinglingId)
        assertEquals("Yes", dto.hypopigmentedPatch)
        assertEquals(4, dto.hypopigmentedPatchId)
        assertEquals("No", dto.thickenedSkin)
        assertEquals(5, dto.thickenedSkinId)
        assertEquals("Yes", dto.skinNodules)
        assertEquals(6, dto.skinNodulesId)
        assertEquals("No", dto.skinPatchDiscoloration)
        assertEquals(7, dto.skinPatchDiscolorationId)
        assertEquals("Yes", dto.recurrentNumbness)
        assertEquals(8, dto.recurrentNumbnessId)
        assertEquals("No", dto.clawingFingers)
        assertEquals(9, dto.clawingFingersId)
        assertEquals("Yes", dto.tinglingNumbnessExtremities)
        assertEquals(10, dto.tinglingNumbnessExtremitiesId)
        assertEquals("No", dto.inabilityCloseEyelid)
        assertEquals(11, dto.inabilityCloseEyelidId)
        assertEquals("Yes", dto.difficultyHoldingObjects)
        assertEquals(12, dto.difficultyHoldingObjectsId)
        assertEquals("No", dto.weaknessFeet)
        assertEquals(13, dto.weaknessFeetId)

        val cache = dto.toCache()
        assertEquals("Yes", cache.recurrentUlceration)
        assertEquals(13, cache.weaknessFeetId)
    }

    @Test
    fun `LeprosyScreeningDTO nullable properties accept explicit null after being set`() {
        val dto = leprosy()
        dto.leprosySymptoms = "Patch"
        dto.leprosySymptoms = null
        dto.leprosySymptomsPosition = null
        dto.lerosyStatusPosition = null
        dto.recurrentTingling = null
        dto.recurrentTinglingId = null
        dto.visitNumber = null
        dto.referredTo = null

        assertNull(dto.leprosySymptoms)
        assertNull(dto.leprosySymptomsPosition)
        assertNull(dto.lerosyStatusPosition)
        assertNull(dto.recurrentTingling)
        assertNull(dto.recurrentTinglingId)
        assertNull(dto.visitNumber)
        assertNull(dto.referredTo)

        val cache = dto.toCache()
        assertNull(cache.leprosySymptoms)
        assertNull(cache.referredTo)
    }

    private fun leprosyFollowUp() = LeprosyFollowUpDTO(
        benId = 81L,
        visitNumber = 2,
        createdBy = "creator",
        createdDate = "2023-05-01",
        modifiedBy = "modifier",
        lastModDate = "2023-05-02"
    )

    @Test
    fun `LeprosyFollowUpDTO date properties round trip through setters`() {
        val dto = leprosyFollowUp()
        dto.followUpDate = "2023-06-01"
        dto.treatmentCompleteDate = "2023-07-01"
        dto.homeVisitDate = "2023-06-15"
        dto.treatmentStartDate = "2023-05-20"
        dto.treatmentEndDate = "2023-08-20"

        assertEquals("2023-06-01", dto.followUpDate)
        assertEquals("2023-07-01", dto.treatmentCompleteDate)
        assertEquals("2023-06-15", dto.homeVisitDate)
        assertEquals("2023-05-20", dto.treatmentStartDate)
        assertEquals("2023-08-20", dto.treatmentEndDate)
        assertEquals("2023-05-01", dto.createdDate)
        assertEquals("2023-05-02", dto.lastModDate)

        val cache = dto.toCache()
        assertTrue(cache.followUpDate > 0L)
        assertTrue(cache.treatmentCompleteDate > 0L)
        assertTrue(cache.homeVisitDate > 0L)
        assertTrue(cache.treatmentStartDate > 0L)
        assertTrue(cache.treatmentEndDate > 0L)
    }

    @Test
    fun `LeprosyFollowUpDTO clinical and referral properties round trip through setters`() {
        val dto = leprosyFollowUp()
        dto.treatmentStatus = "Completed"
        dto.mdtBlisterPackReceived = "Yes"
        dto.mdtBlisterPackRecived = "No"
        dto.remarks = "stable"
        dto.leprosySymptoms = "None"
        dto.typeOfLeprosy = "MB"
        dto.leprosySymptomsPosition = 4
        dto.visitLabel = "Visit -2"
        dto.leprosyStatus = "Confirmed"
        dto.referredTo = 9
        dto.referToName = "CHC"

        assertEquals("Completed", dto.treatmentStatus)
        assertEquals("Yes", dto.mdtBlisterPackReceived)
        assertEquals("No", dto.mdtBlisterPackRecived)
        assertEquals("stable", dto.remarks)
        assertEquals("None", dto.leprosySymptoms)
        assertEquals("MB", dto.typeOfLeprosy)
        assertEquals(4, dto.leprosySymptomsPosition)
        assertEquals("Visit -2", dto.visitLabel)
        assertEquals("Confirmed", dto.leprosyStatus)
        assertEquals(9, dto.referredTo)
        assertEquals("CHC", dto.referToName)

        val cache = dto.toCache()
        assertEquals("Completed", cache.treatmentStatus)
        assertEquals("Yes", cache.mdtBlisterPackReceived)
        assertEquals("No", cache.mdtBlisterPackRecived)
        assertEquals("Confirmed", cache.leprosyStatus)
        assertEquals(9, cache.referredTo)
        assertEquals("CHC", cache.referToName)
    }

    @Test
    fun `LeprosyFollowUpDTO treatment complete date default sentinel round trips through the local offset`() {
        val dto = leprosyFollowUp()
        val cache = dto.toCache()
        val expectedSentinel = -TimeZone.getDefault().getOffset(0L).toLong()
        assertEquals(expectedSentinel, cache.treatmentCompleteDate)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    private fun malaria() = MalariaScreeningDTO(
        benId = 21L,
        visitId = 3L,
        caseDate = "2023-02-01",
        houseHoldDetailsId = 600L,
        screeningDate = "2023-02-02",
        beneficiaryStatus = "Alive",
        dateOfDeath = "null",
        placeOfDeath = "",
        otherPlaceOfDeath = "",
        reasonForDeath = "",
        otherReasonForDeath = "",
        rapidDiagnosticTest = "Positive",
        dateOfRdt = "2023-02-03",
        slideTestName = "Slide A",
        slideTestPf = "Neg",
        slideTestPv = "Pos",
        dateOfSlideTest = "2023-02-04",
        dateOfVisitBySupervisor = "2023-02-05",
        followUpDate = "2023-02-10"
    )

    @Test
    fun `MalariaScreeningDTO immutable death and test properties are readable`() {
        val dto = malaria()
        assertEquals(0, dto.beneficiaryStatusId)
        assertEquals("null", dto.dateOfDeath)
        assertEquals("", dto.placeOfDeath)
        assertEquals("", dto.otherPlaceOfDeath)
        assertEquals("", dto.reasonForDeath)
        assertEquals("", dto.otherReasonForDeath)
        assertEquals("2023-02-03", dto.dateOfRdt)
        assertEquals("2023-02-04", dto.dateOfSlideTest)
        assertEquals("2023-02-05", dto.dateOfVisitBySupervisor)
    }

    @Test
    fun `MalariaScreeningDTO referral properties round trip through setters`() {
        val dto = malaria()
        dto.caseStatus = "Confirmed"
        dto.referredTo = 7
        dto.referToName = "PHC Alpha"
        dto.otherReferredFacility = "Sub Centre"
        dto.remarks = "urgent"
        dto.diseaseTypeID = 2
        dto.followUpDate = "2023-03-11"
        dto.createdBy = "asha1"
        dto.malariaTestType = 5
        dto.malariaSlideTestType = 6

        assertEquals("Confirmed", dto.caseStatus)
        assertEquals(7, dto.referredTo)
        assertEquals("PHC Alpha", dto.referToName)
        assertEquals("Sub Centre", dto.otherReferredFacility)
        assertEquals("urgent", dto.remarks)
        assertEquals(2, dto.diseaseTypeID)
        assertEquals("2023-03-11", dto.followUpDate)
        assertEquals("asha1", dto.createdBy)
        assertEquals(5, dto.malariaTestType)
        assertEquals(6, dto.malariaSlideTestType)

        val cache = dto.toCache()
        assertEquals("Confirmed", cache.caseStatus)
        assertEquals(7, cache.referredTo)
        assertEquals("PHC Alpha", cache.referToName)
        assertEquals("Sub Centre", cache.otherReferredFacility)
        assertEquals("urgent", cache.remarks)
        assertEquals(2, cache.diseaseTypeID)
        assertEquals("asha1", cache.createdBy)
        assertEquals(5, cache.malariaTestType)
        assertEquals(6, cache.malariaSlideTestType)
    }

    @Test
    fun `MalariaScreeningDTO symptom properties round trip through setters`() {
        val dto = malaria()
        dto.feverMoreThanTwoWeeks = true
        dto.fluLikeIllness = true
        dto.shakingChills = true
        dto.headache = true
        dto.muscleAches = true
        dto.tiredness = true
        dto.nausea = true
        dto.vomiting = true
        dto.diarrhea = true

        assertEquals(true, dto.feverMoreThanTwoWeeks)
        assertEquals(true, dto.fluLikeIllness)
        assertEquals(true, dto.shakingChills)
        assertEquals(true, dto.headache)
        assertEquals(true, dto.muscleAches)
        assertEquals(true, dto.tiredness)
        assertEquals(true, dto.nausea)
        assertEquals(true, dto.vomiting)
        assertEquals(true, dto.diarrhea)

        val cache = dto.toCache()
        assertEquals(true, cache.shakingChills)
        assertEquals(true, cache.muscleAches)
        assertEquals(true, cache.tiredness)
        assertEquals(true, cache.vomiting)
    }

    @Test
    fun `MalariaScreeningDTO nullable symptom and referral properties accept null`() {
        val dto = malaria()
        dto.shakingChills = null
        dto.muscleAches = null
        dto.tiredness = null
        dto.vomiting = null
        dto.referToName = null
        dto.otherReferredFacility = null
        dto.createdBy = null
        dto.malariaTestType = null
        dto.malariaSlideTestType = null

        assertNull(dto.shakingChills)
        assertNull(dto.muscleAches)
        assertNull(dto.tiredness)
        assertNull(dto.vomiting)
        assertNull(dto.referToName)
        assertNull(dto.otherReferredFacility)
        assertNull(dto.createdBy)
        assertNull(dto.malariaTestType)
        assertNull(dto.malariaSlideTestType)

        val cache = dto.toCache()
        assertEquals("null", cache.referToName)
        assertNull(cache.otherReferredFacility)
        assertNull(cache.createdBy)
    }

    private fun kalazar() = KALAZARScreeningDTO(
        benId = 31L,
        visitDate = "2023-04-01",
        houseHoldDetailsId = 700L,
        beneficiaryStatus = "Alive",
        dateOfDeath = "null",
        placeOfDeath = "",
        otherPlaceOfDeath = "",
        reasonForDeath = "",
        otherReasonForDeath = "",
        rapidDiagnosticTest = "Negative",
        dateOfRdt = "2023-04-02",
        referToName = "PHC",
        otherReferredFacility = "None",
        createdDate = "2023-04-03",
        createdBy = "asha2"
    )

    @Test
    fun `KALAZARScreeningDTO death properties round trip through setters`() {
        val dto = kalazar()
        dto.visitDate = "2023-05-01"
        dto.beneficiaryStatus = "Dead"
        dto.beneficiaryStatusId = 2
        dto.dateOfDeath = "2023-05-02"
        dto.placeOfDeath = "Hospital"
        dto.otherPlaceOfDeath = "Other place"
        dto.reasonForDeath = "Kala Azar"
        dto.otherReasonForDeath = "Other reason"

        assertEquals("2023-05-01", dto.visitDate)
        assertEquals("Dead", dto.beneficiaryStatus)
        assertEquals(2, dto.beneficiaryStatusId)
        assertEquals("2023-05-02", dto.dateOfDeath)
        assertEquals("Hospital", dto.placeOfDeath)
        assertEquals("Other place", dto.otherPlaceOfDeath)
        assertEquals("Kala Azar", dto.reasonForDeath)
        assertEquals("Other reason", dto.otherReasonForDeath)

        val cache = dto.toCache()
        assertEquals("Dead", cache.beneficiaryStatus)
        assertEquals(2, cache.beneficiaryStatusId)
        assertTrue(cache.dateOfDeath > 0L)
        assertEquals("Hospital", cache.placeOfDeath)
        assertEquals("Other place", cache.otherPlaceOfDeath)
        assertEquals("Kala Azar", cache.reasonForDeath)
        assertEquals("Other reason", cache.otherReasonForDeath)
    }

    @Test
    fun `KALAZARScreeningDTO diagnostic and referral properties round trip through setters`() {
        val dto = kalazar()
        dto.rapidDiagnosticTest = "Positive"
        dto.dateOfRdt = "2023-05-03"
        dto.kalaAzarCaseStatus = "Confirmed"
        dto.referredTo = 14
        dto.referToName = "District Hospital"
        dto.otherReferredFacility = "Medical College"
        dto.diseaseTypeID = 8
        dto.createdDate = "2023-05-04"
        dto.createdBy = "asha3"
        dto.followUpPoint = 4
        dto.syncState = SyncState.UNSYNCED

        assertEquals("Positive", dto.rapidDiagnosticTest)
        assertEquals("2023-05-03", dto.dateOfRdt)
        assertEquals("Confirmed", dto.kalaAzarCaseStatus)
        assertEquals(14, dto.referredTo)
        assertEquals("District Hospital", dto.referToName)
        assertEquals("Medical College", dto.otherReferredFacility)
        assertEquals(8, dto.diseaseTypeID)
        assertEquals("2023-05-04", dto.createdDate)
        assertEquals("asha3", dto.createdBy)
        assertEquals(4, dto.followUpPoint)
        assertEquals(SyncState.UNSYNCED, dto.syncState)

        val cache = dto.toCache()
        assertEquals("Positive", cache.rapidDiagnosticTest)
        assertTrue(cache.dateOfRdt > 0L)
        assertEquals("Confirmed", cache.kalaAzarCaseStatus)
        assertEquals(14, cache.referredTo)
        assertEquals("District Hospital", cache.referToName)
        assertEquals("Medical College", cache.otherReferredFacility)
        assertEquals(8, cache.diseaseTypeID)
        assertEquals("asha3", cache.createdBy)
        assertEquals(4, cache.followUpPoint)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    private fun aes() = AESScreeningDTO(
        benId = 41L,
        visitDate = "2023-06-01",
        houseHoldDetailsId = 800L,
        dateOfDeath = "null",
        createdDate = "2023-06-02"
    )

    @Test
    fun `AESScreeningDTO death properties round trip through setters`() {
        val dto = aes()
        assertEquals(0, dto.id)
        dto.visitDate = "2023-07-01"
        dto.beneficiaryStatus = "Dead"
        dto.beneficiaryStatusId = 5
        dto.dateOfDeath = "2023-07-02"
        dto.placeOfDeath = "Home"
        dto.otherPlaceOfDeath = "Other"
        dto.reasonForDeath = "AES"
        dto.otherReasonForDeath = "Other reason"

        assertEquals("2023-07-01", dto.visitDate)
        assertEquals("Dead", dto.beneficiaryStatus)
        assertEquals(5, dto.beneficiaryStatusId)
        assertEquals("2023-07-02", dto.dateOfDeath)
        assertEquals("Home", dto.placeOfDeath)
        assertEquals("Other", dto.otherPlaceOfDeath)
        assertEquals("AES", dto.reasonForDeath)
        assertEquals("Other reason", dto.otherReasonForDeath)

        val cache = dto.toCache()
        assertEquals("Dead", cache.beneficiaryStatus)
        assertEquals(5, cache.beneficiaryStatusId)
        assertTrue(cache.dateOfDeath > 0L)
        assertEquals("Home", cache.placeOfDeath)
    }

    @Test
    fun `AESScreeningDTO referral and audit properties round trip through setters`() {
        val dto = aes()
        dto.aesJeCaseStatus = "Suspected"
        dto.referredTo = 21
        dto.referToName = "CHC Beta"
        dto.otherReferredFacility = "PHC Gamma"
        dto.diseaseTypeID = 9
        dto.createdDate = "2023-07-05"
        dto.createdBy = "asha4"
        dto.followUpPoint = 3
        dto.syncState = SyncState.UNSYNCED

        assertEquals("Suspected", dto.aesJeCaseStatus)
        assertEquals(21, dto.referredTo)
        assertEquals("CHC Beta", dto.referToName)
        assertEquals("PHC Gamma", dto.otherReferredFacility)
        assertEquals(9, dto.diseaseTypeID)
        assertEquals("2023-07-05", dto.createdDate)
        assertEquals("asha4", dto.createdBy)
        assertEquals(3, dto.followUpPoint)
        assertEquals(SyncState.UNSYNCED, dto.syncState)

        val cache = dto.toCache()
        assertEquals("Suspected", cache.aesJeCaseStatus)
        assertEquals(21, cache.referredTo)
        assertEquals("CHC Beta", cache.referToName)
        assertEquals("PHC Gamma", cache.otherReferredFacility)
        assertEquals(9, cache.diseaseTypeID)
        assertEquals("asha4", cache.createdBy)
        assertEquals(3, cache.followUpPoint)
        assertTrue(cache.createdDate > 0L)
    }

    private fun filaria() = FilariaScreeningDTO(
        benId = 51L,
        mdaHomeVisitDate = "2023-08-01",
        houseHoldDetailsId = 950L,
        createdDate = "2023-08-02T10:15:30+05:30"
    )

    @Test
    fun `FilariaScreeningDTO dose and side effect properties round trip through setters`() {
        val dto = filaria()
        assertEquals(0, dto.id)
        dto.sufferingFromFilariasis = true
        dto.doseStatus = "Taken"
        dto.affectedBodyPart = "Leg"
        dto.otherDoseStatusDetails = "Full dose"
        dto.filariasisCaseCount = "2"
        dto.medicineSideEffect = "Nausea"
        dto.otherSideEffectDetails = "Mild"
        dto.createdBy = "asha5"
        dto.diseaseTypeID = 11
        dto.createdDate = "2023-09-02T10:15:30+05:30"
        dto.syncState = SyncState.UNSYNCED

        assertEquals(true, dto.sufferingFromFilariasis)
        assertEquals("Taken", dto.doseStatus)
        assertEquals("Leg", dto.affectedBodyPart)
        assertEquals("Full dose", dto.otherDoseStatusDetails)
        assertEquals("2", dto.filariasisCaseCount)
        assertEquals("Nausea", dto.medicineSideEffect)
        assertEquals("Mild", dto.otherSideEffectDetails)
        assertEquals("asha5", dto.createdBy)
        assertEquals(11, dto.diseaseTypeID)
        assertEquals("2023-09-02T10:15:30+05:30", dto.createdDate)
        assertEquals(SyncState.UNSYNCED, dto.syncState)

        val cache = dto.toCache()
        assertEquals(true, cache.sufferingFromFilariasis)
        assertEquals("Taken", cache.doseStatus)
        assertEquals("Leg", cache.affectedBodyPart)
        assertEquals("Full dose", cache.otherDoseStatusDetails)
        assertEquals("Nausea", cache.medicineSideEffect)
        assertEquals("Mild", cache.otherSideEffectDetails)
        assertEquals("asha5", cache.createdBy)
        assertEquals(11, cache.diseaseTypeID)
    }

    @Test
    fun `FilariaScreeningDTO null optional properties stringify to literal null in cache`() {
        val dto = filaria()
        dto.doseStatus = null
        dto.affectedBodyPart = null
        dto.otherDoseStatusDetails = null
        dto.medicineSideEffect = null
        dto.otherSideEffectDetails = null
        dto.createdBy = null
        dto.sufferingFromFilariasis = false

        val cache = dto.toCache()
        assertEquals("null", cache.doseStatus)
        assertEquals("null", cache.affectedBodyPart)
        assertEquals("null", cache.otherDoseStatusDetails)
        assertEquals("null", cache.medicineSideEffect)
        assertEquals("null", cache.otherSideEffectDetails)
        assertEquals("null", cache.createdBy)
        assertEquals(false, cache.sufferingFromFilariasis)
    }

    private fun tbScreening() = TBScreeningDTO(
        id = 1L,
        benId = 61L,
        visitDate = "2023-10-01",
        coughMoreThan2Weeks = false,
        bloodInSputum = false,
        feverMoreThan2Weeks = false,
        lossOfWeight = false,
        nightSweats = false,
        historyOfTb = false,
        takingAntiTBDrugs = false,
        familySufferingFromTB = false
    )

    @Test
    fun `TBScreeningDTO primary symptom properties round trip through setters`() {
        val dto = tbScreening()
        dto.coughMoreThan2Weeks = true
        dto.bloodInSputum = true
        dto.feverMoreThan2Weeks = true
        dto.lossOfWeight = true
        dto.nightSweats = true
        dto.historyOfTb = true
        dto.takingAntiTBDrugs = true
        dto.familySufferingFromTB = true

        assertEquals(true, dto.coughMoreThan2Weeks)
        assertEquals(true, dto.bloodInSputum)
        assertEquals(true, dto.feverMoreThan2Weeks)
        assertEquals(true, dto.lossOfWeight)
        assertEquals(true, dto.nightSweats)
        assertEquals(true, dto.historyOfTb)
        assertEquals(true, dto.takingAntiTBDrugs)
        assertEquals(true, dto.familySufferingFromTB)

        val cache = dto.toCache()
        assertEquals(true, cache.coughMoreThan2Weeks)
        assertEquals(true, cache.familySufferingFromTB)
    }

    @Test
    fun `TBScreeningDTO risk assessment properties round trip through setters`() {
        val dto = tbScreening()
        dto.riseOfFever = true
        dto.lossOfAppetite = true
        dto.age = true
        dto.diabetic = true
        dto.tobaccoUser = true
        dto.bmi = true
        dto.contactWithTBPatient = true
        dto.historyOfTBInLastFiveYrs = true
        dto.sympotomatic = "Yes"
        dto.asymptomatic = "No"
        dto.recommandateTest = "CBNAAT"

        assertEquals(true, dto.riseOfFever)
        assertEquals(true, dto.lossOfAppetite)
        assertEquals(true, dto.age)
        assertEquals(true, dto.diabetic)
        assertEquals(true, dto.tobaccoUser)
        assertEquals(true, dto.bmi)
        assertEquals(true, dto.contactWithTBPatient)
        assertEquals(true, dto.historyOfTBInLastFiveYrs)
        assertEquals("Yes", dto.sympotomatic)
        assertEquals("No", dto.asymptomatic)
        assertEquals("CBNAAT", dto.recommandateTest)

        val cache = dto.toCache()
        assertEquals(true, cache.riseOfFever)
        assertEquals(true, cache.lossOfAppetite)
        assertEquals(true, cache.age)
        assertEquals(true, cache.diabetic)
        assertEquals(true, cache.tobaccoUser)
        assertEquals(true, cache.bmi)
        assertEquals(true, cache.contactWithTBPatient)
        assertEquals(true, cache.historyOfTBInLastFiveYrs)
        assertEquals("Yes", cache.sympotomatic)
        assertEquals("No", cache.asymptomatic)
        assertEquals("CBNAAT", cache.recommandateTest)
    }

    @Test
    fun `TBScreeningDTO risk assessment properties accept null after being set`() {
        val dto = tbScreening()
        dto.riseOfFever = true
        dto.riseOfFever = null
        dto.lossOfAppetite = null
        dto.age = null
        dto.diabetic = null
        dto.tobaccoUser = null
        dto.bmi = null
        dto.contactWithTBPatient = null
        dto.historyOfTBInLastFiveYrs = null

        assertNull(dto.riseOfFever)
        assertNull(dto.lossOfAppetite)
        assertNull(dto.age)
        assertNull(dto.diabetic)
        assertNull(dto.tobaccoUser)
        assertNull(dto.bmi)
        assertNull(dto.contactWithTBPatient)
        assertNull(dto.historyOfTBInLastFiveYrs)

        val cache = dto.toCache()
        assertNull(cache.riseOfFever)
        assertNull(cache.historyOfTBInLastFiveYrs)
    }
}
