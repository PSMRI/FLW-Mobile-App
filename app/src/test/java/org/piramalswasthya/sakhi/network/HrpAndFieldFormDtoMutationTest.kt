package org.piramalswasthya.sakhi.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class HrpAndFieldFormDtoMutationTest {

    @Test
    fun `HRPPregnantTrackDTO anemia and hypertension properties round trip through setters`() {
        val dto = HRPPregnantTrackDTO(benId = 101L, visitDate = null, visit = null)
        dto.id = 9
        dto.visitDate = "2023-01-05"
        dto.rdPmsa = "Yes"
        dto.rdDengue = "No"
        dto.rdFilaria = "Yes"
        dto.severeAnemia = "Yes"
        dto.hemoglobinTest = "8.5"
        dto.ifaGiven = "Yes"
        dto.ifaQuantity = 30
        dto.pregInducedHypertension = "Yes"
        dto.systolic = 150
        dto.diastolic = 95

        assertEquals(9, dto.id)
        assertEquals("2023-01-05", dto.visitDate)
        assertEquals("Yes", dto.rdPmsa)
        assertEquals("No", dto.rdDengue)
        assertEquals("Yes", dto.rdFilaria)
        assertEquals("Yes", dto.severeAnemia)
        assertEquals("8.5", dto.hemoglobinTest)
        assertEquals("Yes", dto.ifaGiven)
        assertEquals(30, dto.ifaQuantity)
        assertEquals("Yes", dto.pregInducedHypertension)
        assertEquals(150, dto.systolic)
        assertEquals(95, dto.diastolic)

        val cache = dto.toCache()
        assertEquals("Yes", cache.rdPmsa)
        assertEquals("No", cache.rdDengue)
        assertEquals("8.5", cache.hemoglobinTest)
        assertEquals(30, cache.ifaQuantity)
        assertEquals(150, cache.systolic)
        assertEquals(95, cache.diastolic)
        assertNotNull(cache.visitDate)
    }

    @Test
    fun `HRPPregnantTrackDTO glucose and obstetric properties round trip through setters`() {
        val dto = HRPPregnantTrackDTO(benId = 102L, visitDate = "2023-02-01", visit = "1")
        dto.gestDiabetesMellitus = "Yes"
        dto.bloodGlucoseTest = "Done"
        dto.fbg = 110
        dto.rbg = 180
        dto.ppbg = 160
        dto.fastingOgtt = 95
        dto.after2hrsOgtt = 140
        dto.hypothyrodism = "No"
        dto.polyhydromnios = "Yes"
        dto.oligohydromnios = "No"
        dto.antepartumHem = "Yes"
        dto.malPresentation = "Breech"
        dto.hivsyph = "Negative"
        dto.visit = "2"

        assertEquals("Yes", dto.gestDiabetesMellitus)
        assertEquals("Done", dto.bloodGlucoseTest)
        assertEquals(110, dto.fbg)
        assertEquals(180, dto.rbg)
        assertEquals(160, dto.ppbg)
        assertEquals(95, dto.fastingOgtt)
        assertEquals(140, dto.after2hrsOgtt)
        assertEquals("No", dto.hypothyrodism)
        assertEquals("Yes", dto.polyhydromnios)
        assertEquals("No", dto.oligohydromnios)
        assertEquals("Yes", dto.antepartumHem)
        assertEquals("Breech", dto.malPresentation)
        assertEquals("Negative", dto.hivsyph)
        assertEquals("2", dto.visit)

        val cache = dto.toCache()
        assertEquals("Yes", cache.gestDiabetesMellitus)
        assertEquals(110, cache.fbg)
        assertEquals(180, cache.rbg)
        assertEquals(160, cache.ppbg)
        assertEquals(95, cache.fastingOgtt)
        assertEquals(140, cache.after2hrsOgtt)
        assertEquals("Breech", cache.malPresentation)
        assertEquals("Negative", cache.hivsyph)
        assertEquals("2", cache.visit)
    }

    @Test
    fun `HRPPregnantTrackDTO numeric properties accept null after being set`() {
        val dto = HRPPregnantTrackDTO(benId = 103L, visitDate = "2023-02-01", visit = "1")
        dto.ifaQuantity = 10
        dto.ifaQuantity = null
        dto.fbg = null
        dto.rbg = null
        dto.ppbg = null
        dto.fastingOgtt = null
        dto.after2hrsOgtt = null
        dto.visitDate = null

        assertNull(dto.ifaQuantity)
        assertNull(dto.fbg)
        assertNull(dto.rbg)
        assertNull(dto.ppbg)
        assertNull(dto.fastingOgtt)
        assertNull(dto.after2hrsOgtt)

        val cache = dto.toCache()
        assertEquals(0L, cache.visitDate)
        assertNull(cache.fbg)
    }

    @Test
    fun `HRPPregnantAssessDTO risk properties round trip through setters`() {
        val dto = HRPPregnantAssessDTO(benId = 111L, lmpDate = null, edd = null, visitDate = null)
        dto.id = 4
        dto.noOfDeliveries = "3"
        dto.timeLessThan18m = "Yes"
        dto.heightShort = "Yes"
        dto.age = "17"
        dto.rhNegative = "Yes"
        dto.homeDelivery = "No"
        dto.badObstetric = "Yes"
        dto.multiplePregnancy = "No"
        dto.lmpDate = "2023-01-01"
        dto.edd = "2023-10-08"
        dto.isHighRisk = true
        dto.visitDate = "2023-03-01"

        assertEquals(4, dto.id)
        assertEquals("3", dto.noOfDeliveries)
        assertEquals("Yes", dto.timeLessThan18m)
        assertEquals("Yes", dto.heightShort)
        assertEquals("17", dto.age)
        assertEquals("Yes", dto.rhNegative)
        assertEquals("No", dto.homeDelivery)
        assertEquals("Yes", dto.badObstetric)
        assertEquals("No", dto.multiplePregnancy)
        assertEquals("2023-01-01", dto.lmpDate)
        assertEquals("2023-10-08", dto.edd)
        assertTrue(dto.isHighRisk)
        assertEquals("2023-03-01", dto.visitDate)

        val cache = dto.toCache()
        assertEquals("3", cache.noOfDeliveries)
        assertEquals("17", cache.age)
        assertEquals("Yes", cache.rhNegative)
        assertEquals("No", cache.homeDelivery)
        assertTrue(cache.isHighRisk)
        assertTrue(cache.lmpDate > 0L)
        assertTrue(cache.edd > 0L)
        assertTrue(cache.visitDate > 0L)
    }

    @Test
    fun `HRPNonPregnantTrackDTO vitals properties round trip through setters`() {
        val dto = HRPNonPregnantTrackDTO(benId = 121L, visitDate = null, lmp = null)
        dto.id = 6
        dto.visitDate = "2023-04-01"
        dto.anemia = "Yes"
        dto.hypertension = "No"
        dto.systolic = 130
        dto.diastolic = 85
        dto.diabetes = "Yes"
        dto.bloodGlucoseTest = "Done"
        dto.fbg = 100
        dto.rbg = 170
        dto.ppbg = 150
        dto.severeAnemia = "No"

        assertEquals(6, dto.id)
        assertEquals("2023-04-01", dto.visitDate)
        assertEquals("Yes", dto.anemia)
        assertEquals("No", dto.hypertension)
        assertEquals(130, dto.systolic)
        assertEquals(85, dto.diastolic)
        assertEquals("Yes", dto.diabetes)
        assertEquals("Done", dto.bloodGlucoseTest)
        assertEquals(100, dto.fbg)
        assertEquals(170, dto.rbg)
        assertEquals(150, dto.ppbg)
        assertEquals("No", dto.severeAnemia)
        assertNull(dto.hemoglobinTest)
        assertNull(dto.ifaGiven)
        assertNull(dto.ifaQuantity)

        val cache = dto.toCache()
        assertEquals("Yes", cache.anemia)
        assertEquals(130, cache.systolic)
        assertEquals(85, cache.diastolic)
        assertEquals(100, cache.fbg)
        assertEquals(170, cache.rbg)
        assertEquals(150, cache.ppbg)
        assertNotNull(cache.visitDate)
    }

    @Test
    fun `HRPNonPregnantTrackDTO family planning properties round trip through setters`() {
        val dto = HRPNonPregnantTrackDTO(
            benId = 122L,
            visitDate = "2023-04-01",
            lmp = "2023-03-01",
            hemoglobinTest = "9.0",
            ifaGiven = "Yes",
            ifaQuantity = 20
        )
        dto.fp = "IUCD"
        dto.lmp = "2023-03-15"
        dto.missedPeriod = "Yes"
        dto.isPregnant = "No"

        assertEquals("IUCD", dto.fp)
        assertEquals("2023-03-15", dto.lmp)
        assertEquals("Yes", dto.missedPeriod)
        assertEquals("No", dto.isPregnant)
        assertEquals("9.0", dto.hemoglobinTest)
        assertEquals("Yes", dto.ifaGiven)
        assertEquals(20, dto.ifaQuantity)

        val cache = dto.toCache()
        assertEquals("IUCD", cache.fp)
        assertEquals("Yes", cache.missedPeriod)
        assertEquals("No", cache.isPregnant)
        assertEquals("9.0", cache.hemoglobinTest)
        assertEquals("Yes", cache.ifaGiven)
        assertEquals(20, cache.ifaQuantity)
        assertNotNull(cache.lmp)
    }

    @Test
    fun `HRPNonPregnantAssessDTO risk properties round trip through setters`() {
        val dto = HRPNonPregnantAssessDTO(benId = 131L, visitDate = null)
        dto.noOfDeliveries = "4"
        dto.timeLessThan18m = "Yes"
        dto.heightShort = "No"
        dto.age = "35"
        dto.misCarriage = "Yes"
        dto.homeDelivery = "Yes"
        dto.medicalIssues = "Diabetes"
        dto.pastCSection = "Yes"
        dto.isHighRisk = true
        dto.visitDate = "2023-05-05"

        assertEquals("4", dto.noOfDeliveries)
        assertEquals("Yes", dto.timeLessThan18m)
        assertEquals("No", dto.heightShort)
        assertEquals("35", dto.age)
        assertEquals("Yes", dto.misCarriage)
        assertEquals("Yes", dto.homeDelivery)
        assertEquals("Diabetes", dto.medicalIssues)
        assertEquals("Yes", dto.pastCSection)
        assertTrue(dto.isHighRisk)
        assertEquals("2023-05-05", dto.visitDate)

        val cache = dto.toCache()
        assertEquals("4", cache.noOfDeliveries)
        assertEquals("35", cache.age)
        assertEquals("Yes", cache.misCarriage)
        assertEquals("Diabetes", cache.medicalIssues)
        assertEquals("Yes", cache.pastCSection)
        assertTrue(cache.isHighRisk)
        assertTrue(cache.visitDate > 0L)
    }

    @Test
    fun `HRPMicroBirthPlanDTO facility properties round trip through setters`() {
        val dto = HRPMicroBirthPlanDTO(benId = 141L)
        assertEquals(0, dto.id)
        dto.nearestSc = "SC One"
        dto.bloodGroup = "O+"
        dto.contactNumber1 = "9990001111"
        dto.contactNumber2 = "9990002222"
        dto.scHosp = "SC Hospital"
        dto.usg = "Done"
        dto.block = "Block A"
        dto.nearestPhc = "PHC One"
        dto.nearestFru = "FRU One"

        assertEquals("SC One", dto.nearestSc)
        assertEquals("O+", dto.bloodGroup)
        assertEquals("9990001111", dto.contactNumber1)
        assertEquals("9990002222", dto.contactNumber2)
        assertEquals("SC Hospital", dto.scHosp)
        assertEquals("Done", dto.usg)
        assertEquals("Block A", dto.block)
        assertEquals("PHC One", dto.nearestPhc)
        assertEquals("FRU One", dto.nearestFru)

        val cache = dto.toCache()
        assertEquals("SC One", cache.nearestSc)
        assertEquals("O+", cache.bloodGroup)
        assertEquals("9990001111", cache.contactNumber1)
        assertEquals("SC Hospital", cache.scHosp)
        assertEquals("Block A", cache.block)
        assertEquals("FRU One", cache.nearestFru)
    }

    @Test
    fun `HRPMicroBirthPlanDTO support network properties round trip through setters`() {
        val dto = HRPMicroBirthPlanDTO(benId = 142L)
        dto.bloodDonors1 = "Donor A"
        dto.bloodDonors2 = "Donor B"
        dto.birthCompanion = "Sister"
        dto.careTaker = "Mother"
        dto.communityMember = "Member X"
        dto.communityMemberContact = "9998887777"
        dto.modeOfTransportation = "Ambulance"

        assertEquals("Donor A", dto.bloodDonors1)
        assertEquals("Donor B", dto.bloodDonors2)
        assertEquals("Sister", dto.birthCompanion)
        assertEquals("Mother", dto.careTaker)
        assertEquals("Member X", dto.communityMember)
        assertEquals("9998887777", dto.communityMemberContact)
        assertEquals("Ambulance", dto.modeOfTransportation)

        val cache = dto.toCache()
        assertEquals("Donor B", cache.bloodDonors2)
        assertEquals("Mother", cache.careTaker)
        assertEquals("9998887777", cache.communityMemberContact)
        assertEquals("Ambulance", cache.modeOfTransportation)
        assertEquals("P", cache.processed)
    }

    @Test
    fun `VHNDDTO meeting and education properties round trip through setters`() {
        val dto = VHNDDTO(vhndDate = "2023-06-01")
        dto.vhndDate = "2023-06-02"
        dto.place = "Anganwadi"
        dto.noOfBeneficiariesAttended = 25
        dto.Image1 = "img-a"
        dto.Image2 = "img-b"
        dto.vhndPlaceId = 3
        dto.pregnantWomenAnc = "5"
        dto.lactatingMothersPnc = "4"
        dto.childrenImmunization = "10"
        dto.knowledgeBalancedDiet = "Yes"
        dto.careDuringPregnancy = "Yes"
        dto.importanceBreastfeeding = "Yes"
        dto.complementaryFeeding = "No"
        dto.hygieneSanitation = "Yes"
        dto.familyPlanningHealthcare = "Yes"
        dto.selectAllEducation = true

        assertEquals("2023-06-02", dto.vhndDate)
        assertEquals("Anganwadi", dto.place)
        assertEquals(25, dto.noOfBeneficiariesAttended)
        assertEquals("img-a", dto.Image1)
        assertEquals("img-b", dto.Image2)
        assertEquals(3, dto.vhndPlaceId)
        assertEquals("5", dto.pregnantWomenAnc)
        assertEquals("4", dto.lactatingMothersPnc)
        assertEquals("10", dto.childrenImmunization)
        assertEquals("Yes", dto.knowledgeBalancedDiet)
        assertEquals("Yes", dto.careDuringPregnancy)
        assertEquals("Yes", dto.importanceBreastfeeding)
        assertEquals("No", dto.complementaryFeeding)
        assertEquals("Yes", dto.hygieneSanitation)
        assertEquals("Yes", dto.familyPlanningHealthcare)
        assertEquals(true, dto.selectAllEducation)

        val cache = dto.toCache()
        assertEquals("Anganwadi", cache.place)
        assertEquals(25, cache.noOfBeneficiariesAttended)
        assertEquals("img-a", cache.image1)
        assertEquals("img-b", cache.image2)
        assertEquals(3, cache.vhndPlaceId)
        assertEquals("5", cache.pregnantWomenAnc)
        assertEquals("No", cache.complementaryFeeding)
        assertEquals("Yes", cache.familyPlanningHealthcare)
    }

    @Test
    fun `VHNCDTO attendance properties round trip through setters`() {
        val dto = VHNCDTO(vhncDate = "2023-06-10")
        dto.vhncDate = "2023-06-11"
        dto.place = "Panchayat Bhawan"
        dto.noOfBeneficiariesAttended = 18
        dto.Image1 = "vhnc-a"
        dto.Image2 = "vhnc-b"
        dto.villageName = "Rampur"
        dto.anm = 2
        dto.aww = 3
        dto.noOfPragnentWoment = 6
        dto.noOfLactingMother = 7
        dto.noOfCommittee = 8
        dto.followupPrevius = true

        assertEquals("2023-06-11", dto.vhncDate)
        assertEquals("Panchayat Bhawan", dto.place)
        assertEquals(18, dto.noOfBeneficiariesAttended)
        assertEquals("vhnc-a", dto.Image1)
        assertEquals("vhnc-b", dto.Image2)
        assertEquals("Rampur", dto.villageName)
        assertEquals(2, dto.anm)
        assertEquals(3, dto.aww)
        assertEquals(6, dto.noOfPragnentWoment)
        assertEquals(7, dto.noOfLactingMother)
        assertEquals(8, dto.noOfCommittee)
        assertEquals(true, dto.followupPrevius)

        val cache = dto.toCache()
        assertEquals("Panchayat Bhawan", cache.place)
        assertEquals(18, cache.noOfBeneficiariesAttended)
        assertEquals("vhnc-a", cache.image1)
        assertEquals("Rampur", cache.villageName)
        assertEquals(2, cache.anm)
        assertEquals(3, cache.aww)
        assertEquals(6, cache.noOfPragnentWoment)
        assertEquals(7, cache.noOfLactingMother)
        assertEquals(8, cache.noOfCommittee)
        assertEquals(true, cache.followupPrevius)
    }

    @Test
    fun `PHCReviewDTO meeting properties round trip through setters`() {
        val dto = PHCReviewDTO(phcReviewDate = "2023-07-01")
        dto.placeId = 4
        dto.phcReviewDate = "2023-07-02"
        dto.place = "PHC Hall"
        dto.noOfBeneficiariesAttended = 12
        dto.Image1 = "phc-a"
        dto.Image2 = "phc-b"
        dto.villageName = "Sitapur"
        dto.mitaninHistory = "history text"
        dto.mitaninActivityCheckList = "checklist text"

        assertEquals(4, dto.placeId)
        assertEquals("2023-07-02", dto.phcReviewDate)
        assertEquals("PHC Hall", dto.place)
        assertEquals(12, dto.noOfBeneficiariesAttended)
        assertEquals("phc-a", dto.Image1)
        assertEquals("phc-b", dto.Image2)
        assertEquals("Sitapur", dto.villageName)
        assertEquals("history text", dto.mitaninHistory)
        assertEquals("checklist text", dto.mitaninActivityCheckList)

        val cache = dto.toCache()
        assertEquals(4, cache.placeId)
        assertEquals("PHC Hall", cache.place)
        assertEquals("phc-a", cache.image1)
        assertEquals("phc-b", cache.image2)
        assertEquals("Sitapur", cache.villageName)
        assertEquals("history text", cache.mitaninHistory)
        assertEquals("checklist text", cache.mitaninActivityCheckList)
    }

    @Test
    fun `AHDDTO properties round trip through setters`() {
        val dto = AHDDTO(mobilizedForAHD = "Yes")
        dto.mobilizedForAHD = "No"
        dto.ahdPlace = "School"
        dto.ahdDate = "2023-08-01"
        dto.image1 = "ahd-a"
        dto.image2 = "ahd-b"

        assertEquals("No", dto.mobilizedForAHD)
        assertEquals("School", dto.ahdPlace)
        assertEquals("2023-08-01", dto.ahdDate)
        assertEquals("ahd-a", dto.image1)
        assertEquals("ahd-b", dto.image2)

        val cache = dto.toCache()
        assertEquals("No", cache.mobilizedForAHD)
        assertEquals("School", cache.ahdPlace)
        assertEquals("2023-08-01", cache.ahdDate)
        assertEquals("ahd-a", cache.image1)
        assertEquals("ahd-b", cache.image2)
    }

    @Test
    fun `DewormingDTO properties round trip through setters`() {
        val dto = DewormingDTO()
        dto.id = 15
        dto.dewormingDone = "Yes"
        dto.dewormingDate = "2023-09-01"
        dto.dewormingLocation = "School"
        dto.ageGroup = 2
        dto.image1 = "dw-a"
        dto.image2 = "dw-b"
        dto.regDate = "2023-09-02"

        assertEquals(15, dto.id)
        assertEquals("Yes", dto.dewormingDone)
        assertEquals("2023-09-01", dto.dewormingDate)
        assertEquals("School", dto.dewormingLocation)
        assertEquals(2, dto.ageGroup)
        assertEquals("dw-a", dto.image1)
        assertEquals("dw-b", dto.image2)
        assertEquals("2023-09-02", dto.regDate)

        val cache = dto.toCache()
        assertEquals(15, cache.id)
        assertEquals("Yes", cache.dewormingDone)
        assertEquals("School", cache.dewormingLocation)
        assertEquals(2, cache.ageGroup)
        assertEquals("dw-b", cache.image2)
        assertEquals("2023-09-02", cache.regDate)
    }

    @Test
    fun `AdolscentHealthDTO identity and distribution properties round trip through setters`() {
        val dto = AdolscentHealthDTO(benId = 151L, visitDate = "2023-10-01")
        dto.id = 3
        dto.userID = 44
        dto.benId = 152L
        dto.visitDate = "2023-10-02"
        dto.healthStatus = "Good"
        dto.ifaTabletDistributed = true
        dto.quantityOfIfaTablets = 12
        dto.menstrualHygieneAwarenessGiven = true
        dto.sanitaryNapkinDistributed = true
        dto.isSanitaryNapkinUsed = true
        dto.noOfPacketsDistributed = 2
        dto.place = "School"
        dto.distributionDate = "2023-10-03"

        assertEquals(3, dto.id)
        assertEquals(44, dto.userID)
        assertEquals(152L, dto.benId)
        assertEquals("2023-10-02", dto.visitDate)
        assertEquals("Good", dto.healthStatus)
        assertEquals(true, dto.ifaTabletDistributed)
        assertEquals(12, dto.quantityOfIfaTablets)
        assertEquals(true, dto.menstrualHygieneAwarenessGiven)
        assertEquals(true, dto.sanitaryNapkinDistributed)
        assertEquals(true, dto.isSanitaryNapkinUsed)
        assertEquals(2, dto.noOfPacketsDistributed)
        assertEquals("School", dto.place)
        assertEquals("2023-10-03", dto.distributionDate)

        val cache = dto.toCache()
        assertEquals(152L, cache.benId)
        assertEquals("Good", cache.healthStatus)
        assertEquals(true, cache.ifaTabletDistributed)
        assertEquals(12, cache.quantityOfIfaTablets)
        assertEquals(2, cache.noOfPacketsDistributed)
        assertEquals("School", cache.place)
        assertTrue(cache.distributionDate > 0L)
    }

    @Test
    fun `AdolscentHealthDTO counseling and referral properties round trip through setters`() {
        val dto = AdolscentHealthDTO(benId = 153L, visitDate = "2023-11-01")
        dto.referredToHealthFacility = "PHC"
        dto.counselingProvided = true
        dto.counselingType = "Nutrition"
        dto.followUpDate = "2023-11-15"
        dto.referralStatus = "Completed"

        assertEquals("PHC", dto.referredToHealthFacility)
        assertEquals(true, dto.counselingProvided)
        assertEquals("Nutrition", dto.counselingType)
        assertEquals("2023-11-15", dto.followUpDate)
        assertEquals("Completed", dto.referralStatus)

        val cache = dto.toCache()
        assertEquals("PHC", cache.referredToHealthFacility)
        assertEquals(true, cache.counselingProvided)
        assertEquals("Nutrition", cache.counselingType)
        assertTrue(cache.followUpDate > 0L)
        assertEquals("Completed", cache.referralStatus)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    private fun tbSuspected() = TBSuspectedDTO(
        id = 2L,
        benId = 161L,
        visitDate = "2023-12-01",
        isSputumCollected = true,
        sputumSubmittedAt = "DMC",
        nikshayId = "NIK-1",
        sputumTestResult = "Positive",
        referred = true,
        followUps = "2",
        visitLabel = "Visit -1"
    )

    @Test
    fun `TBSuspectedDTO immutable referral properties are readable`() {
        val dto = tbSuspected()
        assertEquals(true, dto.referred)
        assertEquals("2", dto.followUps)
    }

    @Test
    fun `TBSuspectedDTO diagnostic properties round trip through setters`() {
        val dto = tbSuspected()
        dto.visitLabel = "Visit -2"
        dto.typeOfTBCase = "Pulmonary"
        dto.reasonForSuspicion = "Persistent cough"
        dto.hasSymptoms = true
        dto.isChestXRayDone = true
        dto.chestXRayResult = "Abnormal"
        dto.referralFacility = "DMC Alpha"
        dto.isTBConfirmed = true
        dto.isDRTBConfirmed = false
        dto.isConfirmed = true

        assertEquals("Visit -2", dto.visitLabel)
        assertEquals("Pulmonary", dto.typeOfTBCase)
        assertEquals("Persistent cough", dto.reasonForSuspicion)
        assertEquals(true, dto.hasSymptoms)
        assertEquals(true, dto.isChestXRayDone)
        assertEquals("Abnormal", dto.chestXRayResult)
        assertEquals("DMC Alpha", dto.referralFacility)
        assertEquals(true, dto.isTBConfirmed)
        assertEquals(false, dto.isDRTBConfirmed)
        assertTrue(dto.isConfirmed)

        val cache = dto.toCache()
        assertEquals("Visit -2", cache.visitLabel)
        assertEquals("Pulmonary", cache.typeOfTBCase)
        assertEquals("Persistent cough", cache.reasonForSuspicion)
        assertTrue(cache.hasSymptoms)
        assertEquals(true, cache.isChestXRayDone)
        assertEquals("Abnormal", cache.chestXRayResult)
        assertEquals("DMC Alpha", cache.referralFacility)
        assertEquals(true, cache.isTBConfirmed)
        assertEquals(false, cache.isDRTBConfirmed)
        assertTrue(cache.isConfirmed)
    }

    @Test
    fun `TBSuspectedDTO null hasSymptoms defaults to false in cache`() {
        val dto = tbSuspected()
        dto.hasSymptoms = true
        dto.hasSymptoms = null
        dto.isChestXRayDone = null
        dto.chestXRayResult = null
        dto.referralFacility = null
        dto.isTBConfirmed = null
        dto.isDRTBConfirmed = null

        assertNull(dto.hasSymptoms)
        assertNull(dto.isChestXRayDone)
        assertNull(dto.chestXRayResult)
        assertNull(dto.referralFacility)

        val cache = dto.toCache()
        assertFalse(cache.hasSymptoms)
        assertNull(cache.isChestXRayDone)
        assertNull(cache.isTBConfirmed)
        assertNull(cache.isDRTBConfirmed)
    }

    @Test
    fun `NCDReferalDTO mutable type and sync state round trip through setters`() {
        val dto = NCDReferalDTO(
            benId = 171L,
            referredToInstituteID = 5,
            refrredToAdditionalServiceList = listOf("Lab", "Radiology"),
            referredToInstituteName = "District Hospital",
            referralReason = "High BP",
            revisitDate = "2023-12-20",
            vanID = 2,
            parkingPlaceID = 3,
            beneficiaryRegID = 900L,
            benVisitID = 901L,
            visitCode = 902L,
            providerServiceMapID = 4,
            createdBy = "asha9",
            type = "NCD"
        )
        dto.type = "TB"
        dto.syncState = SyncState.UNSYNCED

        assertEquals("TB", dto.type)
        assertEquals(SyncState.UNSYNCED, dto.syncState)
        assertEquals(false, dto.isSpecialist)

        val cache = dto.toCache()
        assertEquals("TB", cache.type)
        assertEquals(5, cache.referredToInstituteID)
        assertEquals(listOf("Lab", "Radiology"), cache.refrredToAdditionalServiceList)
        assertEquals("District Hospital", cache.referredToInstituteName)
        assertEquals("High BP", cache.referralReason)
        assertEquals(2, cache.vanID)
        assertEquals(3, cache.parkingPlaceID)
        assertEquals(900L, cache.beneficiaryRegID)
        assertEquals(901L, cache.benVisitID)
        assertEquals(902L, cache.visitCode)
        assertEquals(4, cache.providerServiceMapID)
        assertEquals("asha9", cache.createdBy)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }
}
