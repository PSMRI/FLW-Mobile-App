package org.piramalswasthya.sakhi.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Covers the pure DTO -> Cache `toCache()` mappers declared in JsonAdapters.kt.
 * All date helpers used by these mappers (getLongFromDate / getLongFromDateMultipleSupport /
 * convertIsoDateToMillis) are null-safe and swallow parse errors, so simple inputs are safe.
 */
class DtoToCacheMappingTest {

    // ---------------- HRPPregnantTrackDTO ----------------
    @Test fun `HRPPregnantTrackDTO toCache maps benId and visit`() {
        val cache = HRPPregnantTrackDTO(benId = 11L, visitDate = "01-01-2024", visit = "v1").toCache()
        assertEquals(11L, cache.benId)
        assertEquals("v1", cache.visit)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test fun `HRPPregnantTrackDTO toCache passes through clinical fields`() {
        val cache = HRPPregnantTrackDTO(
            benId = 1L, visitDate = null, visit = "v",
            systolic = 120, diastolic = 80, ifaQuantity = 30
        ).toCache()
        assertEquals(120, cache.systolic)
        assertEquals(80, cache.diastolic)
        assertEquals(30, cache.ifaQuantity)
    }

    // ---------------- HRPPregnantAssessDTO ----------------
    @Test fun `HRPPregnantAssessDTO toCache maps benId and isHighRisk`() {
        val cache = HRPPregnantAssessDTO(
            benId = 22L, lmpDate = null, edd = null, visitDate = null, isHighRisk = true
        ).toCache()
        assertEquals(22L, cache.benId)
        assertEquals(true, cache.isHighRisk)
    }

    @Test fun `HRPPregnantAssessDTO toCache defaults isHighRisk false`() {
        val cache = HRPPregnantAssessDTO(benId = 1L, lmpDate = null, edd = null, visitDate = null).toCache()
        assertFalse(cache.isHighRisk)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    // ---------------- HRPMicroBirthPlanDTO ----------------
    @Test fun `HRPMicroBirthPlanDTO toCache maps benId and sets processed`() {
        val cache = HRPMicroBirthPlanDTO(benId = 33L, bloodGroup = "O+").toCache()
        assertEquals(33L, cache.benId)
        assertEquals("O+", cache.bloodGroup)
        assertEquals("P", cache.processed)
    }

    // ---------------- HRPNonPregnantTrackDTO ----------------
    @Test fun `HRPNonPregnantTrackDTO toCache maps benId and passthrough`() {
        val cache = HRPNonPregnantTrackDTO(
            benId = 44L, visitDate = null, lmp = null, anemia = "yes", isPregnant = "no"
        ).toCache()
        assertEquals(44L, cache.benId)
        assertEquals("yes", cache.anemia)
        assertEquals("no", cache.isPregnant)
    }

    // ---------------- HRPNonPregnantAssessDTO ----------------
    @Test fun `HRPNonPregnantAssessDTO toCache maps benId and isHighRisk`() {
        val cache = HRPNonPregnantAssessDTO(
            benId = 55L, visitDate = null, isHighRisk = true, pastCSection = "yes"
        ).toCache()
        assertEquals(55L, cache.benId)
        assertEquals(true, cache.isHighRisk)
        assertEquals("yes", cache.pastCSection)
    }

    // ---------------- VHNDDTO ----------------
    @Test fun `VHNDDTO toCache maps date and place`() {
        val cache = VHNDDTO(vhndDate = "01-01-2024", place = "Center", noOfBeneficiariesAttended = 5).toCache()
        assertEquals("01-01-2024", cache.vhndDate)
        assertEquals("Center", cache.place)
        assertEquals(5, cache.noOfBeneficiariesAttended)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    // ---------------- VHNCDTO ----------------
    @Test fun `VHNCDTO toCache maps date and counts`() {
        val cache = VHNCDTO(vhncDate = "02-02-2024", villageName = "Vill", noOfCommittee = 3).toCache()
        assertEquals("02-02-2024", cache.vhncDate)
        assertEquals("Vill", cache.villageName)
        assertEquals(3, cache.noOfCommittee)
    }

    // ---------------- PHCReviewDTO ----------------
    @Test fun `PHCReviewDTO toCache maps date and details`() {
        val cache = PHCReviewDTO(
            phcReviewDate = "03-03-2024", villageName = "V", mitaninHistory = "h", placeId = 7
        ).toCache()
        assertEquals("03-03-2024", cache.phcReviewDate)
        assertEquals("V", cache.villageName)
        assertEquals("h", cache.mitaninHistory)
        assertEquals(7, cache.placeId)
    }

    // ---------------- AHDDTO ----------------
    @Test fun `AHDDTO toCache maps mobilizedForAHD and place`() {
        val cache = AHDDTO(mobilizedForAHD = "Yes", ahdPlace = "P1").toCache()
        assertEquals("Yes", cache.mobilizedForAHD)
        assertEquals("P1", cache.ahdPlace)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    // ---------------- DewormingDTO ----------------
    @Test fun `DewormingDTO toCache maps id and fields`() {
        val cache = DewormingDTO(id = 9, dewormingDone = "Yes", ageGroup = 2).toCache()
        assertEquals(9, cache.id)
        assertEquals("Yes", cache.dewormingDone)
        assertEquals(2, cache.ageGroup)
    }

    // ---------------- TBScreeningDTO ----------------
    @Test fun `TBScreeningDTO toCache maps benId and symptoms`() {
        val cache = TBScreeningDTO(
            id = 1L, benId = 66L, visitDate = null,
            coughMoreThan2Weeks = true, bloodInSputum = false, feverMoreThan2Weeks = true,
            lossOfWeight = false, nightSweats = true, historyOfTb = false,
            takingAntiTBDrugs = false, familySufferingFromTB = true
        ).toCache()
        assertEquals(66L, cache.benId)
        assertEquals(true, cache.coughMoreThan2Weeks)
        assertEquals(true, cache.familySufferingFromTB)
    }

    // ---------------- AdolscentHealthDTO ----------------
    @Test fun `AdolscentHealthDTO toCache maps benId and healthStatus`() {
        val cache = AdolscentHealthDTO(
            benId = 77L, visitDate = "01-01-2024", healthStatus = "Good", quantityOfIfaTablets = 4
        ).toCache()
        assertEquals(77L, cache.benId)
        assertEquals("Good", cache.healthStatus)
        assertEquals(4, cache.quantityOfIfaTablets)
    }

    // ---------------- TBSuspectedDTO ----------------
    @Test fun `TBSuspectedDTO toCache maps benId and defaults`() {
        val cache = TBSuspectedDTO(
            id = 1L, benId = 88L, visitDate = null, isSputumCollected = true,
            sputumSubmittedAt = "Lab", nikshayId = "N1", sputumTestResult = "Neg",
            referred = false, followUps = null, visitLabel = "Visit-1"
        ).toCache()
        assertEquals(88L, cache.benId)
        assertEquals("Lab", cache.sputumSubmittedAt)
        assertEquals("Visit-1", cache.visitLabel)
        assertFalse(cache.hasSymptoms)
    }

    @Test fun `TBSuspectedDTO toCache maps hasSymptoms when true`() {
        val cache = TBSuspectedDTO(
            id = 1L, benId = 1L, visitDate = null, isSputumCollected = null,
            sputumSubmittedAt = null, nikshayId = null, sputumTestResult = null,
            referred = null, followUps = null, visitLabel = null, hasSymptoms = true
        ).toCache()
        assertEquals(true, cache.hasSymptoms)
    }

    // ---------------- TBConfirmedTreatmentDTO ----------------
    @Test fun `TBConfirmedTreatmentDTO toCache maps benId and default reasonForDeath`() {
        val cache = TBConfirmedTreatmentDTO(
            id = 1L, benId = 99L, regimenType = "R1",
            treatmentStartDate = null, expectedTreatmentCompletionDate = null, followUpDate = null,
            monthlyFollowUpDone = null, adherenceToMedicines = null, anyDiscomfort = false,
            treatmentCompleted = false, actualTreatmentCompletionDate = null, treatmentOutcome = null,
            dateOfDeath = null, placeOfDeath = null, reasonForDeath = null, reasonForNotCompleting = null
        ).toCache()
        assertEquals(99L, cache.benId)
        assertEquals("R1", cache.regimenType)
        assertEquals("Tuberculosis", cache.reasonForDeath)
    }

    // ---------------- AESScreeningDTO ----------------
    @Test fun `AESScreeningDTO toCache maps benId and household`() {
        val cache = AESScreeningDTO(
            benId = 100L, visitDate = "01-01-2024", houseHoldDetailsId = 200L,
            dateOfDeath = "01-01-2024", createdDate = "01-01-2024", aesJeCaseStatus = "Active"
        ).toCache()
        assertEquals(100L, cache.benId)
        assertEquals(200L, cache.houseHoldDetailsId)
        assertEquals("Active", cache.aesJeCaseStatus)
    }

    // ---------------- NCDReferalDTO ----------------
    @Test fun `NCDReferalDTO toCache maps benId and reason`() {
        val cache = NCDReferalDTO(
            benId = 101L, referredToInstituteID = 3, refrredToAdditionalServiceList = null,
            referredToInstituteName = "Inst", referralReason = "Reason", revisitDate = "01-01-2024",
            vanID = null, parkingPlaceID = null, beneficiaryRegID = null, benVisitID = null,
            visitCode = null, providerServiceMapID = null, createdBy = null, type = "T"
        ).toCache()
        assertEquals(101L, cache.benId)
        assertEquals("Reason", cache.referralReason)
        assertEquals("Inst", cache.referredToInstituteName)
        assertFalse(cache.isSpecialist!!)
    }

    // ---------------- FilariaScreeningDTO ----------------
    @Test fun `FilariaScreeningDTO toCache maps benId and household`() {
        val cache = FilariaScreeningDTO(
            benId = 102L, mdaHomeVisitDate = "01-01-2024", houseHoldDetailsId = 300L,
            createdDate = "2024-01-01T00:00:00.000+05:30", sufferingFromFilariasis = true
        ).toCache()
        assertEquals(102L, cache.benId)
        assertEquals(300L, cache.houseHoldDetailsId)
        assertEquals(true, cache.sufferingFromFilariasis)
    }

    // ---------------- ScreeningRoundDTO ----------------
    @Test fun `ScreeningRoundDTO toCache maps rounds and householdId`() {
        val cache = ScreeningRoundDTO(date = "01-01-2024", rounds = 2, householdId = 400L).toCache()
        assertEquals(2, cache.rounds)
        assertEquals(400L, cache.householdId)
    }

    // ---------------- KALAZARScreeningDTO ----------------
    @Test fun `KALAZARScreeningDTO toCache maps benId and status`() {
        val cache = KALAZARScreeningDTO(
            benId = 103L, visitDate = "01-01-2024", houseHoldDetailsId = 500L,
            beneficiaryStatus = "Alive", dateOfDeath = "01-01-2024", placeOfDeath = "x",
            otherPlaceOfDeath = "x", reasonForDeath = "x", otherReasonForDeath = "x",
            rapidDiagnosticTest = "Neg", dateOfRdt = "01-01-2024", referToName = "Dr",
            otherReferredFacility = "F", createdDate = "01-01-2024", createdBy = "creator",
            kalaAzarCaseStatus = "Suspected"
        ).toCache()
        assertEquals(103L, cache.benId)
        assertEquals(500L, cache.houseHoldDetailsId)
        assertEquals("Suspected", cache.kalaAzarCaseStatus)
        assertNotNull(cache.rapidDiagnosticTest)
    }
}
