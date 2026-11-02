package org.piramalswasthya.sakhi.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
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

    @Test fun `HRPPregnantTrackDTO toCache maps all remaining optional lab and risk fields`() {
        val cache = HRPPregnantTrackDTO(
            id = 5, benId = 11L, visitDate = "01-01-2024",
            rdPmsa = "Neg", rdDengue = "Neg", rdFilaria = "Neg",
            severeAnemia = "No", hemoglobinTest = "Done", ifaGiven = "Yes", ifaQuantity = 30,
            pregInducedHypertension = "No", systolic = 120, diastolic = 80,
            gestDiabetesMellitus = "No", bloodGlucoseTest = "Done", fbg = 90, rbg = 110, ppbg = 130,
            fastingOgtt = 95, after2hrsOgtt = 140, hypothyrodism = "No", polyhydromnios = "No",
            oligohydromnios = "No", antepartumHem = "No", malPresentation = "No", hivsyph = "Neg",
            visit = "v1"
        ).toCache()
        assertEquals("Neg", cache.rdPmsa)
        assertEquals("Neg", cache.rdDengue)
        assertEquals("Neg", cache.rdFilaria)
        assertEquals("No", cache.severeAnemia)
        assertEquals("Done", cache.hemoglobinTest)
        assertEquals("Yes", cache.ifaGiven)
        assertEquals("No", cache.pregInducedHypertension)
        assertEquals("No", cache.gestDiabetesMellitus)
        assertEquals("Done", cache.bloodGlucoseTest)
        assertEquals(90, cache.fbg)
        assertEquals(110, cache.rbg)
        assertEquals(130, cache.ppbg)
        assertEquals(95, cache.fastingOgtt)
        assertEquals(140, cache.after2hrsOgtt)
        assertEquals("No", cache.hypothyrodism)
        assertEquals("No", cache.polyhydromnios)
        assertEquals("No", cache.oligohydromnios)
        assertEquals("No", cache.antepartumHem)
        assertEquals("No", cache.malPresentation)
        assertEquals("Neg", cache.hivsyph)
    }

    @Test fun `HRPPregnantTrackDTO generated members consistent when fully populated`() {
        val dto = HRPPregnantTrackDTO(
            id = 5, benId = 11L, visitDate = "01-01-2024",
            rdPmsa = "Neg", rdDengue = "Neg", rdFilaria = "Neg",
            severeAnemia = "No", hemoglobinTest = "Done", ifaGiven = "Yes", ifaQuantity = 30,
            pregInducedHypertension = "No", systolic = 120, diastolic = 80,
            gestDiabetesMellitus = "No", bloodGlucoseTest = "Done", fbg = 90, rbg = 110, ppbg = 130,
            fastingOgtt = 95, after2hrsOgtt = 140, hypothyrodism = "No", polyhydromnios = "No",
            oligohydromnios = "No", antepartumHem = "No", malPresentation = "No", hivsyph = "Neg",
            visit = "v1"
        )
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertNotEquals(dto, dto.copy(hivsyph = "Pos"))
        assertEquals("Neg", dto.hivsyph)
        assertEquals(5, dto.id)
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

    @Test fun `HRPMicroBirthPlanDTO toCache maps all remaining contact and facility fields`() {
        val cache = HRPMicroBirthPlanDTO(
            id = 2, benId = 33L, nearestSc = "SC1", bloodGroup = "O+",
            contactNumber1 = "999", contactNumber2 = "888", scHosp = "Hosp",
            usg = "Done", block = "Block1", nearestPhc = "PHC1", nearestFru = "FRU1",
            bloodDonors1 = "D1", bloodDonors2 = "D2", birthCompanion = "Comp",
            careTaker = "Care", communityMember = "CM", communityMemberContact = "777",
            modeOfTransportation = "Ambulance"
        ).toCache()
        assertEquals(0, cache.id)
        assertEquals("SC1", cache.nearestSc)
        assertEquals("999", cache.contactNumber1)
        assertEquals("888", cache.contactNumber2)
        assertEquals("Hosp", cache.scHosp)
        assertEquals("Done", cache.usg)
        assertEquals("Block1", cache.block)
        assertEquals("PHC1", cache.nearestPhc)
        assertEquals("FRU1", cache.nearestFru)
        assertEquals("D1", cache.bloodDonors1)
        assertEquals("D2", cache.bloodDonors2)
        assertEquals("Comp", cache.birthCompanion)
        assertEquals("Care", cache.careTaker)
        assertEquals("CM", cache.communityMember)
        assertEquals("777", cache.communityMemberContact)
        assertEquals("Ambulance", cache.modeOfTransportation)
    }

    @Test fun `HRPMicroBirthPlanDTO generated members consistent when fully populated`() {
        val dto = HRPMicroBirthPlanDTO(
            id = 2, benId = 33L, nearestSc = "SC1", bloodGroup = "O+",
            contactNumber1 = "999", contactNumber2 = "888", scHosp = "Hosp",
            usg = "Done", block = "Block1", nearestPhc = "PHC1", nearestFru = "FRU1",
            bloodDonors1 = "D1", bloodDonors2 = "D2", birthCompanion = "Comp",
            careTaker = "Care", communityMember = "CM", communityMemberContact = "777",
            modeOfTransportation = "Ambulance"
        )
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertNotEquals(dto, dto.copy(modeOfTransportation = "Bike"))
        assertEquals("Ambulance", dto.modeOfTransportation)
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

    @Test fun `HRPNonPregnantTrackDTO toCache maps all remaining vitals and lab fields`() {
        val cache = HRPNonPregnantTrackDTO(
            id = 3, benId = 44L, visitDate = "01-01-2024",
            anemia = "No", hypertension = "No", systolic = 118, diastolic = 76,
            diabetes = "No", bloodGlucoseTest = "Done", fbg = 88, rbg = 100, ppbg = 120,
            severeAnemia = "No", hemoglobinTest = "Done", ifaGiven = "Yes", ifaQuantity = 30,
            fp = "Yes", lmp = "01-01-2024", missedPeriod = "No", isPregnant = "No"
        ).toCache()
        assertEquals("No", cache.hypertension)
        assertEquals(118, cache.systolic)
        assertEquals(76, cache.diastolic)
        assertEquals("No", cache.diabetes)
        assertEquals("Done", cache.bloodGlucoseTest)
        assertEquals(88, cache.fbg)
        assertEquals(100, cache.rbg)
        assertEquals(120, cache.ppbg)
        assertEquals("No", cache.severeAnemia)
        assertEquals("Done", cache.hemoglobinTest)
        assertEquals("Yes", cache.ifaGiven)
        assertEquals(30, cache.ifaQuantity)
        assertEquals("Yes", cache.fp)
        assertEquals("No", cache.missedPeriod)
    }

    @Test fun `HRPNonPregnantTrackDTO generated members consistent when fully populated`() {
        val dto = HRPNonPregnantTrackDTO(
            id = 3, benId = 44L, visitDate = "01-01-2024",
            anemia = "No", hypertension = "No", systolic = 118, diastolic = 76,
            diabetes = "No", bloodGlucoseTest = "Done", fbg = 88, rbg = 100, ppbg = 120,
            severeAnemia = "No", hemoglobinTest = "Done", ifaGiven = "Yes", ifaQuantity = 30,
            fp = "Yes", lmp = "01-01-2024", missedPeriod = "No", isPregnant = "No"
        )
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertNotEquals(dto, dto.copy(missedPeriod = "Yes"))
        assertEquals("No", dto.missedPeriod)
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

    @Test fun `VHNDDTO toCache maps all remaining images and education fields`() {
        val cache = VHNDDTO(
            id = 1, vhndDate = "01-01-2024", place = "Center", noOfBeneficiariesAttended = 5,
            Image1 = "img1.png", Image2 = "img2.png", vhndPlaceId = 9,
            pregnantWomenAnc = "Yes", lactatingMothersPnc = "Yes", childrenImmunization = "Yes",
            knowledgeBalancedDiet = "Yes", careDuringPregnancy = "Yes",
            importanceBreastfeeding = "Yes", complementaryFeeding = "Yes",
            hygieneSanitation = "Yes", familyPlanningHealthcare = "Yes", selectAllEducation = true
        ).toCache()
        assertEquals("img1.png", cache.image1)
        assertEquals("img2.png", cache.image2)
        assertEquals(9, cache.vhndPlaceId)
        assertEquals("Yes", cache.pregnantWomenAnc)
        assertEquals("Yes", cache.lactatingMothersPnc)
        assertEquals("Yes", cache.childrenImmunization)
        assertEquals("Yes", cache.knowledgeBalancedDiet)
        assertEquals("Yes", cache.careDuringPregnancy)
        assertEquals("Yes", cache.importanceBreastfeeding)
        assertEquals("Yes", cache.complementaryFeeding)
        assertEquals("Yes", cache.hygieneSanitation)
        assertEquals("Yes", cache.familyPlanningHealthcare)
    }

    @Test(expected = NullPointerException::class)
    fun `VHNDDTO toCache throws when vhndDate is null`() {
        VHNDDTO(vhndDate = null).toCache()
    }

    private fun vhndDto() = VHNDDTO(vhndDate = "01-01-2024", place = "Center", noOfBeneficiariesAttended = 5)

    @Test fun `VHNDDTO generated members behave consistently`() {
        val dto = vhndDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("VHNDDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals("x"))
        assertNotEquals(dto, dto.copy(place = "Other"))
        assertEquals("01-01-2024", dto.vhndDate)
        assertEquals("Center", dto.place)
        assertEquals(5, dto.noOfBeneficiariesAttended)
        assertEquals(false, dto.selectAllEducation)
    }

    @Test fun `VHNDDTO generated members consistent when fully populated`() {
        val dto = VHNDDTO(
            id = 1, vhndDate = "01-01-2024", place = "Center", noOfBeneficiariesAttended = 5,
            Image1 = "img1.png", Image2 = "img2.png", vhndPlaceId = 9,
            pregnantWomenAnc = "Yes", lactatingMothersPnc = "Yes", childrenImmunization = "Yes",
            knowledgeBalancedDiet = "Yes", careDuringPregnancy = "Yes",
            importanceBreastfeeding = "Yes", complementaryFeeding = "Yes",
            hygieneSanitation = "Yes", familyPlanningHealthcare = "Yes", selectAllEducation = true
        )
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertNotEquals(dto, dto.copy(selectAllEducation = false))
        assertEquals(true, dto.selectAllEducation)
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

    @Test fun `AdolscentHealthDTO toCache maps all remaining counseling and referral fields`() {
        val cache = AdolscentHealthDTO(
            id = 1, userID = 2, benId = 77L, visitDate = "01-01-2024", healthStatus = "Good",
            ifaTabletDistributed = true, quantityOfIfaTablets = 4,
            menstrualHygieneAwarenessGiven = true, sanitaryNapkinDistributed = true,
            isSanitaryNapkinUsed = true, noOfPacketsDistributed = 6, place = "Center",
            distributionDate = "01-01-2024", referredToHealthFacility = "Yes",
            counselingProvided = true, counselingType = "Group", followUpDate = "02-01-2024",
            referralStatus = "Done"
        ).toCache()
        assertEquals(true, cache.ifaTabletDistributed)
        assertEquals(true, cache.menstrualHygieneAwarenessGiven)
        assertEquals(true, cache.sanitaryNapkinDistributed)
        assertEquals(6, cache.noOfPacketsDistributed)
        assertEquals("Center", cache.place)
        assertEquals("Yes", cache.referredToHealthFacility)
        assertEquals(true, cache.counselingProvided)
        assertEquals("Group", cache.counselingType)
        assertEquals("Done", cache.referralStatus)
    }

    private fun adolscentHealthDto() = AdolscentHealthDTO(
        benId = 77L, visitDate = "01-01-2024", healthStatus = "Good", quantityOfIfaTablets = 4
    )

    @Test fun `AdolscentHealthDTO generated members behave consistently`() {
        val dto = adolscentHealthDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("AdolscentHealthDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals("x"))
        assertNotEquals(dto, dto.copy(healthStatus = "Poor"))
        assertEquals(77L, dto.benId)
        assertEquals("01-01-2024", dto.visitDate)
        assertEquals(4, dto.quantityOfIfaTablets)
    }

    @Test fun `AdolscentHealthDTO generated members consistent when fully populated`() {
        val dto = AdolscentHealthDTO(
            id = 1, userID = 2, benId = 77L, visitDate = "01-01-2024", healthStatus = "Good",
            ifaTabletDistributed = true, quantityOfIfaTablets = 4,
            menstrualHygieneAwarenessGiven = true, sanitaryNapkinDistributed = true,
            isSanitaryNapkinUsed = true, noOfPacketsDistributed = 6, place = "Center",
            distributionDate = "01-01-2024", referredToHealthFacility = "Yes",
            counselingProvided = true, counselingType = "Group", followUpDate = "02-01-2024",
            referralStatus = "Done"
        )
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertNotEquals(dto, dto.copy(isSanitaryNapkinUsed = false))
        assertEquals(1, dto.id)
        assertEquals(2, dto.userID)
        assertEquals(true, dto.isSanitaryNapkinUsed)
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

    // =====================================================
    // Generated data-class members (equals / hashCode / toString / copy)
    // =====================================================

    private fun hrpPregnantTrackDto() =
        HRPPregnantTrackDTO(benId = 11L, visitDate = "01-01-2024", visit = "v1")

    @Test fun `HRPPregnantTrackDTO generated members behave consistently`() {
        val dto = hrpPregnantTrackDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("HRPPregnantTrackDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals("other"))
        assertNotEquals(dto, dto.copy(benId = 12L))
        assertEquals(11L, dto.benId)
        assertEquals("v1", dto.visit)
        assertEquals("01-01-2024", dto.visitDate)
    }

    @Test fun `HRPPregnantTrackDTO copy overrides selected fields only`() {
        val changed = hrpPregnantTrackDto().copy(visit = "v2")
        assertEquals("v2", changed.visit)
        assertEquals(11L, changed.benId)
        assertEquals("01-01-2024", changed.visitDate)
    }

    private fun hrpMicroBirthPlanDto() = HRPMicroBirthPlanDTO(benId = 33L, bloodGroup = "O+")

    @Test fun `HRPMicroBirthPlanDTO generated members behave consistently`() {
        val dto = hrpMicroBirthPlanDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("HRPMicroBirthPlanDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals(0))
        assertNotEquals(dto, dto.copy(bloodGroup = "A+"))
        assertEquals(33L, dto.benId)
        assertEquals("O+", dto.bloodGroup)
    }

    private fun hrpNonPregnantTrackDto() = HRPNonPregnantTrackDTO(
        benId = 44L, visitDate = null, lmp = null, anemia = "yes", isPregnant = "no"
    )

    @Test fun `HRPNonPregnantTrackDTO generated members behave consistently`() {
        val dto = hrpNonPregnantTrackDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("HRPNonPregnantTrackDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals("x"))
        assertNotEquals(dto, dto.copy(anemia = "no"))
        assertEquals(44L, dto.benId)
        assertEquals("yes", dto.anemia)
        assertEquals("no", dto.isPregnant)
    }

    private fun tbScreeningDto() = TBScreeningDTO(
        id = 1L, benId = 66L, visitDate = null,
        coughMoreThan2Weeks = true, bloodInSputum = false, feverMoreThan2Weeks = true,
        lossOfWeight = false, nightSweats = true, historyOfTb = false,
        takingAntiTBDrugs = false, familySufferingFromTB = true
    )

    @Test fun `TBScreeningDTO generated members behave consistently`() {
        val dto = tbScreeningDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("TBScreeningDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals("x"))
        assertNotEquals(dto, dto.copy(benId = 67L))
        assertEquals(1L, dto.id)
        assertEquals(66L, dto.benId)
        assertEquals(true, dto.coughMoreThan2Weeks)
        assertEquals(false, dto.bloodInSputum)
        assertEquals(true, dto.feverMoreThan2Weeks)
        assertEquals(false, dto.lossOfWeight)
        assertEquals(true, dto.nightSweats)
        assertEquals(false, dto.historyOfTb)
        assertEquals(false, dto.takingAntiTBDrugs)
        assertEquals(true, dto.familySufferingFromTB)
    }

    @Test fun `TBScreeningDTO toCache maps remaining symptom flags`() {
        val cache = tbScreeningDto().copy(
            bloodInSputum = true, lossOfWeight = true, historyOfTb = true, takingAntiTBDrugs = true
        ).toCache()
        assertEquals(true, cache.bloodInSputum)
        assertEquals(true, cache.lossOfWeight)
        assertEquals(true, cache.historyOfTb)
        assertEquals(true, cache.takingAntiTBDrugs)
        assertEquals(true, cache.nightSweats)
    }

    @Test fun `TBScreeningDTO toCache maps extended risk assessment fields`() {
        val cache = tbScreeningDto().copy(
            riseOfFever = true, lossOfAppetite = true, age = true, diabetic = true,
            tobaccoUser = true, bmi = true, contactWithTBPatient = true,
            historyOfTBInLastFiveYrs = true, sympotomatic = "Yes", asymptomatic = "No",
            recommandateTest = "CBNAAT"
        ).toCache()
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

    @Test fun `TBScreeningDTO generated members consistent when fully populated`() {
        val dto = tbScreeningDto().copy(
            riseOfFever = true, lossOfAppetite = true, age = true, diabetic = true,
            tobaccoUser = true, bmi = true, contactWithTBPatient = true,
            historyOfTBInLastFiveYrs = true, sympotomatic = "Yes", asymptomatic = "No",
            recommandateTest = "CBNAAT"
        )
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertNotEquals(dto, dto.copy(recommandateTest = "Other"))
        assertEquals("Yes", dto.sympotomatic)
        assertEquals("No", dto.asymptomatic)
        assertEquals("CBNAAT", dto.recommandateTest)
    }

    private fun aesScreeningDto() = AESScreeningDTO(
        benId = 100L, visitDate = "01-01-2024", houseHoldDetailsId = 200L,
        dateOfDeath = "01-01-2024", createdDate = "01-01-2024", aesJeCaseStatus = "Active"
    )

    @Test fun `AESScreeningDTO generated members behave consistently`() {
        val dto = aesScreeningDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("AESScreeningDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals("x"))
        assertNotEquals(dto, dto.copy(benId = 101L))
        assertEquals(100L, dto.benId)
        assertEquals(200L, dto.houseHoldDetailsId)
        assertEquals("Active", dto.aesJeCaseStatus)
        assertEquals("01-01-2024", dto.visitDate)
        assertEquals("01-01-2024", dto.dateOfDeath)
        assertEquals("01-01-2024", dto.createdDate)
    }

    @Test fun `AESScreeningDTO toCache maps all remaining death and referral fields`() {
        val cache = aesScreeningDto().copy(
            beneficiaryStatus = "Alive", beneficiaryStatusId = 2, placeOfDeath = "Home",
            otherPlaceOfDeath = "Other", reasonForDeath = "Illness", otherReasonForDeath = "Other",
            referredTo = 5, referToName = "Dr Smith", otherReferredFacility = "Facility1",
            diseaseTypeID = 3, createdBy = "creator", followUpPoint = 2
        ).toCache()
        assertEquals("Alive", cache.beneficiaryStatus)
        assertEquals(2, cache.beneficiaryStatusId)
        assertEquals("Home", cache.placeOfDeath)
        assertEquals("Other", cache.otherPlaceOfDeath)
        assertEquals("Illness", cache.reasonForDeath)
        assertEquals("Other", cache.otherReasonForDeath)
        assertEquals(5, cache.referredTo)
        assertEquals("Dr Smith", cache.referToName)
        assertEquals("Facility1", cache.otherReferredFacility)
        assertEquals(3, cache.diseaseTypeID)
        assertEquals("creator", cache.createdBy)
        assertEquals(2, cache.followUpPoint)
    }

    @Test fun `AESScreeningDTO toCache maps referToName to string literal null when unset`() {
        val cache = aesScreeningDto().copy(referToName = null).toCache()
        assertEquals("null", cache.referToName)
    }

    @Test fun `AESScreeningDTO generated members consistent when fully populated`() {
        val dto = aesScreeningDto().copy(
            beneficiaryStatus = "Alive", beneficiaryStatusId = 2, placeOfDeath = "Home",
            otherPlaceOfDeath = "Other", reasonForDeath = "Illness", otherReasonForDeath = "Other",
            referredTo = 5, referToName = "Dr Smith", otherReferredFacility = "Facility1",
            diseaseTypeID = 3, createdBy = "creator", followUpPoint = 2
        )
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertNotEquals(dto, dto.copy(followUpPoint = 9))
        assertEquals("Alive", dto.beneficiaryStatus)
        assertEquals("Dr Smith", dto.referToName)
    }

    private fun kalazarScreeningDto() = KALAZARScreeningDTO(
        benId = 103L, visitDate = "01-01-2024", houseHoldDetailsId = 500L,
        beneficiaryStatus = "Alive", dateOfDeath = "01-01-2024", placeOfDeath = "x",
        otherPlaceOfDeath = "x", reasonForDeath = "x", otherReasonForDeath = "x",
        rapidDiagnosticTest = "Neg", dateOfRdt = "01-01-2024", referToName = "Dr",
        otherReferredFacility = "F", createdDate = "01-01-2024", createdBy = "creator",
        kalaAzarCaseStatus = "Suspected"
    )

    @Test fun `KALAZARScreeningDTO generated members behave consistently`() {
        val dto = kalazarScreeningDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("KALAZARScreeningDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals("x"))
        assertNotEquals(dto, dto.copy(benId = 104L))
        assertEquals(103L, dto.benId)
        assertEquals(500L, dto.houseHoldDetailsId)
        assertEquals("Alive", dto.beneficiaryStatus)
        assertEquals("Neg", dto.rapidDiagnosticTest)
        assertEquals("Dr", dto.referToName)
        assertEquals("F", dto.otherReferredFacility)
        assertEquals("creator", dto.createdBy)
        assertEquals("Suspected", dto.kalaAzarCaseStatus)
    }

    @Test fun `KALAZARScreeningDTO toCache maps death and referral details`() {
        val cache = kalazarScreeningDto().toCache()
        assertEquals("x", cache.placeOfDeath)
        assertEquals("x", cache.otherPlaceOfDeath)
        assertEquals("x", cache.reasonForDeath)
        assertEquals("x", cache.otherReasonForDeath)
        assertEquals("Dr", cache.referToName)
        assertEquals("F", cache.otherReferredFacility)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test fun `KALAZARScreeningDTO toCache maps optional identifier fields`() {
        val cache = kalazarScreeningDto().copy(
            id = 4, beneficiaryStatusId = 2, referredTo = 6, diseaseTypeID = 3, followUpPoint = 1
        ).toCache()
        assertEquals(2, cache.beneficiaryStatusId)
        assertEquals(6, cache.referredTo)
        assertEquals(3, cache.diseaseTypeID)
        assertEquals(1, cache.followUpPoint)
    }

    @Test fun `KALAZARScreeningDTO generated members consistent when identifiers differ`() {
        val dto = kalazarScreeningDto().copy(
            id = 4, beneficiaryStatusId = 2, referredTo = 6, diseaseTypeID = 3, followUpPoint = 1
        )
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertNotEquals(dto, dto.copy(followUpPoint = 9))
        assertEquals(4, dto.id)
        assertEquals(2, dto.beneficiaryStatusId)
        assertEquals(6, dto.referredTo)
        assertEquals(3, dto.diseaseTypeID)
    }

    // =====================================================
    // Additional branch coverage (nullable / boolean / equals-position combinations)
    // =====================================================

    // ---------------- HRPPregnantTrackDTO ----------------
    @Test fun `HRPPregnantTrackDTO toCache maps mixed optional fields with partial nulls`() {
        val cache = HRPPregnantTrackDTO(
            benId = 12L, visitDate = "05-05-2024", rdPmsa = "Pos", rdDengue = null,
            severeAnemia = null, hemoglobinTest = "Done", gestDiabetesMellitus = null,
            fbg = 100, hypothyrodism = null, visit = "v3"
        ).toCache()
        assertEquals("Pos", cache.rdPmsa)
        assertEquals(null, cache.rdDengue)
        assertEquals("Done", cache.hemoglobinTest)
        assertEquals(null, cache.severeAnemia)
        assertEquals(100, cache.fbg)
        assertEquals(null, cache.gestDiabetesMellitus)
    }

    @Test fun `HRPPregnantTrackDTO toCache handles null visit with non-null visitDate`() {
        val cache = HRPPregnantTrackDTO(benId = 13L, visitDate = "06-06-2024", visit = null).toCache()
        assertEquals(13L, cache.benId)
        assertEquals(null, cache.visit)
    }

    @Test fun `HRPPregnantTrackDTO generated members consistent when middle field differs`() {
        val dto = HRPPregnantTrackDTO(
            id = 5, benId = 11L, visitDate = "01-01-2024",
            rdPmsa = "Neg", rdDengue = "Neg", rdFilaria = "Neg",
            severeAnemia = "No", hemoglobinTest = "Done", ifaGiven = "Yes", ifaQuantity = 30,
            pregInducedHypertension = "No", systolic = 120, diastolic = 80,
            gestDiabetesMellitus = "No", bloodGlucoseTest = "Done", fbg = 90, rbg = 110, ppbg = 130,
            fastingOgtt = 95, after2hrsOgtt = 140, hypothyrodism = "No", polyhydromnios = "No",
            oligohydromnios = "No", antepartumHem = "No", malPresentation = "No", hivsyph = "Neg",
            visit = "v1"
        )
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertNotEquals(dto, dto.copy(gestDiabetesMellitus = "Yes"))
        assertNotEquals(dto, dto.copy(fbg = 999))
    }

    @Test fun `HRPPregnantTrackDTO copy overrides multiple fields independently`() {
        val dto = hrpPregnantTrackDto().copy(rdFilaria = "Pos", malPresentation = "Yes")
        assertEquals("Pos", dto.rdFilaria)
        assertEquals("Yes", dto.malPresentation)
        assertEquals(11L, dto.benId)
    }

    // ---------------- KALAZARScreeningDTO ----------------
    @Test fun `KALAZARScreeningDTO toCache maps null kalaAzarCaseStatus`() {
        val cache = kalazarScreeningDto().copy(kalaAzarCaseStatus = null).toCache()
        assertEquals(null, cache.kalaAzarCaseStatus)
    }

    @Test fun `KALAZARScreeningDTO generated members consistent when early field differs`() {
        val dto = kalazarScreeningDto()
        assertNotEquals(dto, dto.copy(visitDate = "09-09-2024"))
    }

    @Test fun `KALAZARScreeningDTO generated members consistent when middle field differs`() {
        val dto = kalazarScreeningDto()
        assertNotEquals(dto, dto.copy(rapidDiagnosticTest = "Pos"))
        assertNotEquals(dto, dto.copy(dateOfRdt = "10-10-2024"))
    }

    @Test fun `KALAZARScreeningDTO toCache maps default optional identifiers when unset`() {
        val dto = KALAZARScreeningDTO(
            benId = 200L, visitDate = "01-01-2024", houseHoldDetailsId = 900L,
            beneficiaryStatus = "Alive", dateOfDeath = "01-01-2024", placeOfDeath = "x",
            otherPlaceOfDeath = "x", reasonForDeath = "x", otherReasonForDeath = "x",
            rapidDiagnosticTest = "Neg", dateOfRdt = "01-01-2024", referToName = "Dr",
            otherReferredFacility = "F", createdDate = "01-01-2024", createdBy = "creator"
        )
        val cache = dto.toCache()
        assertEquals(0, cache.beneficiaryStatusId)
        assertEquals(0, cache.referredTo)
        assertEquals(0, cache.diseaseTypeID)
        assertEquals(0, cache.followUpPoint)
        assertEquals("", cache.kalaAzarCaseStatus)
    }

    // ---------------- HRPMicroBirthPlanDTO ----------------
    @Test fun `HRPMicroBirthPlanDTO toCache always resets id to zero and processed to P`() {
        val cache = HRPMicroBirthPlanDTO(id = 9, benId = 33L, bloodGroup = "AB+").toCache()
        assertEquals(0, cache.id)
        assertEquals("P", cache.processed)
        assertEquals("AB+", cache.bloodGroup)
    }

    @Test fun `HRPMicroBirthPlanDTO toCache maps mixed partial optional fields`() {
        val cache = HRPMicroBirthPlanDTO(
            benId = 40L, nearestSc = "SC2", bloodGroup = null, contactNumber1 = "111",
            scHosp = null, block = "B2", bloodDonors1 = null, careTaker = "Care2"
        ).toCache()
        assertEquals("SC2", cache.nearestSc)
        assertEquals(null, cache.bloodGroup)
        assertEquals("111", cache.contactNumber1)
        assertEquals(null, cache.scHosp)
        assertEquals("B2", cache.block)
        assertEquals("Care2", cache.careTaker)
    }

    @Test fun `HRPMicroBirthPlanDTO generated members consistent when middle field differs`() {
        val dto = hrpMicroBirthPlanDto()
        assertNotEquals(dto, dto.copy(nearestPhc = "PHC2"))
        assertNotEquals(dto, dto.copy(careTaker = "Other"))
    }

    // ---------------- AESScreeningDTO ----------------
    @Test fun `AESScreeningDTO toCache maps benId with default optional fields unset`() {
        val dto = AESScreeningDTO(
            benId = 150L, visitDate = "01-01-2024", houseHoldDetailsId = 250L,
            dateOfDeath = "01-01-2024", createdDate = "01-01-2024"
        )
        val cache = dto.toCache()
        assertEquals(150L, cache.benId)
        assertEquals("", cache.aesJeCaseStatus)
        assertEquals(0, cache.beneficiaryStatusId)
        assertEquals(1, cache.followUpPoint)
        assertEquals(null, cache.beneficiaryStatus)
    }

    @Test fun `AESScreeningDTO generated members consistent when middle field differs`() {
        val dto = aesScreeningDto()
        assertNotEquals(dto, dto.copy(dateOfDeath = "02-02-2024"))
        assertNotEquals(dto, dto.copy(houseHoldDetailsId = 999L))
    }

    // ---------------- HRPNonPregnantTrackDTO ----------------
    @Test fun `HRPNonPregnantTrackDTO toCache handles visitDate set and lmp null`() {
        val cache = HRPNonPregnantTrackDTO(
            benId = 45L, visitDate = "07-07-2024", lmp = null, anemia = "No", isPregnant = "No"
        ).toCache()
        assertEquals(45L, cache.benId)
        assertEquals(0L, cache.lmp)
    }

    @Test fun `HRPNonPregnantTrackDTO toCache handles visitDate null and lmp set`() {
        val cache = HRPNonPregnantTrackDTO(
            benId = 46L, visitDate = null, lmp = "08-08-2024", anemia = "No", isPregnant = "No"
        ).toCache()
        assertEquals(0L, cache.visitDate)
        assertNotNull(cache.lmp)
    }

    @Test fun `HRPNonPregnantTrackDTO generated members consistent when middle field differs`() {
        val dto = hrpNonPregnantTrackDto()
        assertNotEquals(dto, dto.copy(diabetes = "Yes"))
        assertNotEquals(dto, dto.copy(bloodGlucoseTest = "Done"))
    }

    // ---------------- TBScreeningDTO ----------------
    @Test fun `TBScreeningDTO generated members consistent when middle field differs`() {
        val dto = tbScreeningDto()
        assertNotEquals(dto, dto.copy(nightSweats = false))
        assertNotEquals(dto, dto.copy(historyOfTb = true))
    }

    @Test fun `TBScreeningDTO toCache maps default extended fields as null`() {
        val cache = tbScreeningDto().toCache()
        assertEquals(null, cache.riseOfFever)
        assertEquals(null, cache.sympotomatic)
        assertEquals(66L, cache.benId)
    }

    // ---------------- AdolscentHealthDTO ----------------
    @Test fun `AdolscentHealthDTO toCache does not propagate isSanitaryNapkinUsed to cache`() {
        val cache = adolscentHealthDto().copy(isSanitaryNapkinUsed = true).toCache()
        assertEquals(null, cache.isSanitaryNapkinUsed)
    }

    @Test fun `AdolscentHealthDTO generated members consistent when middle field differs`() {
        val dto = adolscentHealthDto()
        assertNotEquals(dto, dto.copy(place = "Other Place"))
        assertNotEquals(dto, dto.copy(noOfPacketsDistributed = 99))
    }

    @Test fun `AdolscentHealthDTO defaults id and userID to null when unset`() {
        val dto = adolscentHealthDto()
        assertEquals(null, dto.id)
        assertEquals(null, dto.userID)
    }

    // ---------------- FilariaScreeningDTO ----------------
    @Test(expected = NullPointerException::class)
    fun `FilariaScreeningDTO toCache throws when sufferingFromFilariasis is null`() {
        FilariaScreeningDTO(
            benId = 110L, mdaHomeVisitDate = "01-01-2024", houseHoldDetailsId = 310L,
            createdDate = "2024-01-01T00:00:00.000+05:30", sufferingFromFilariasis = null
        ).toCache()
    }

    @Test fun `FilariaScreeningDTO toCache maps null diseaseTypeID directly without exception`() {
        val cache = FilariaScreeningDTO(
            benId = 111L, mdaHomeVisitDate = "01-01-2024", houseHoldDetailsId = 311L,
            createdDate = "2024-01-01T00:00:00.000+05:30", sufferingFromFilariasis = true,
            diseaseTypeID = null
        ).toCache()
        assertEquals(null, cache.diseaseTypeID)
    }

    @Test fun `FilariaScreeningDTO toCache does not propagate filariasisCaseCount to cache`() {
        val cache = FilariaScreeningDTO(
            benId = 112L, mdaHomeVisitDate = "01-01-2024", houseHoldDetailsId = 312L,
            createdDate = "2024-01-01T00:00:00.000+05:30", sufferingFromFilariasis = false,
            filariasisCaseCount = "5"
        ).toCache()
        assertEquals(null, cache.filariasisCaseCount)
        assertEquals(false, cache.sufferingFromFilariasis)
    }

    @Test fun `FilariaScreeningDTO toCache converts unset optional fields to string literal null`() {
        val cache = FilariaScreeningDTO(
            benId = 113L, mdaHomeVisitDate = "01-01-2024", houseHoldDetailsId = 313L,
            createdDate = "2024-01-01T00:00:00.000+05:30", sufferingFromFilariasis = true,
            doseStatus = null, affectedBodyPart = null, otherDoseStatusDetails = null
        ).toCache()
        assertEquals("null", cache.doseStatus)
        assertEquals("null", cache.affectedBodyPart)
        assertEquals("null", cache.otherDoseStatusDetails)
        assertEquals("", cache.medicineSideEffect)
        assertEquals("", cache.otherSideEffectDetails)
        assertEquals("", cache.createdBy)
    }

    @Test fun `FilariaScreeningDTO toCache maps all optional fields when populated`() {
        val cache = FilariaScreeningDTO(
            benId = 114L, mdaHomeVisitDate = "01-01-2024", houseHoldDetailsId = 314L,
            createdDate = "2024-01-01T00:00:00.000+05:30", sufferingFromFilariasis = false,
            doseStatus = "Complete", affectedBodyPart = "Leg", otherDoseStatusDetails = "Details",
            medicineSideEffect = "Nausea", otherSideEffectDetails = "None", createdBy = "creator",
            diseaseTypeID = 2
        ).toCache()
        assertEquals("Complete", cache.doseStatus)
        assertEquals("Leg", cache.affectedBodyPart)
        assertEquals("Details", cache.otherDoseStatusDetails)
        assertEquals("Nausea", cache.medicineSideEffect)
        assertEquals("None", cache.otherSideEffectDetails)
        assertEquals("creator", cache.createdBy)
        assertEquals(2, cache.diseaseTypeID)
        assertEquals(false, cache.sufferingFromFilariasis)
    }

    // ---------------- HRPPregnantAssessDTO ----------------
    @Test fun `HRPPregnantAssessDTO toCache maps all remaining risk factor fields`() {
        val cache = HRPPregnantAssessDTO(
            id = 3, benId = 22L, noOfDeliveries = "Yes", timeLessThan18m = "No",
            heightShort = "Yes", age = "No", rhNegative = "Yes", homeDelivery = "No",
            badObstetric = "Yes", multiplePregnancy = "No",
            lmpDate = "01-01-2024", edd = "01-08-2024", isHighRisk = true, visitDate = "01-02-2024"
        ).toCache()
        assertEquals(22L, cache.benId)
        assertEquals("Yes", cache.noOfDeliveries)
        assertEquals("No", cache.timeLessThan18m)
        assertEquals("Yes", cache.heightShort)
        assertEquals("No", cache.age)
        assertEquals("Yes", cache.rhNegative)
        assertEquals("No", cache.homeDelivery)
        assertEquals("Yes", cache.badObstetric)
        assertEquals("No", cache.multiplePregnancy)
        assertNotNull(cache.lmpDate)
        assertNotNull(cache.edd)
        assertNotNull(cache.visitDate)
        assertEquals(true, cache.isHighRisk)
    }

    private fun hrpPregnantAssessDto() = HRPPregnantAssessDTO(
        benId = 22L, lmpDate = null, edd = null, visitDate = null, isHighRisk = true
    )

    @Test fun `HRPPregnantAssessDTO generated members behave consistently`() {
        val dto = hrpPregnantAssessDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("HRPPregnantAssessDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals("x"))
        assertNotEquals(dto, dto.copy(benId = 23L))
        assertEquals(22L, dto.benId)
        assertEquals(true, dto.isHighRisk)
    }

    @Test fun `HRPPregnantAssessDTO generated members consistent when fully populated`() {
        val dto = HRPPregnantAssessDTO(
            id = 3, benId = 22L, noOfDeliveries = "Yes", timeLessThan18m = "No",
            heightShort = "Yes", age = "No", rhNegative = "Yes", homeDelivery = "No",
            badObstetric = "Yes", multiplePregnancy = "No",
            lmpDate = "01-01-2024", edd = "01-08-2024", isHighRisk = true, visitDate = "01-02-2024"
        )
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertNotEquals(dto, dto.copy(badObstetric = "No"))
        assertNotEquals(dto, dto.copy(rhNegative = "No"))
        assertEquals("Yes", dto.badObstetric)
        assertEquals(3, dto.id)
    }
}
