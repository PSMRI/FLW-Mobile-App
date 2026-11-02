package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CbacNetworkDtoAccessorTest {

    private fun postNew() = CbacPostNew(
        cbacAge = "40-49",
        cbacAgeScore = 1,
        cbacConsumeGutka = "Yes",
        cbacConsumeGutkaScore = 2,
        cbacAlcohol = "No",
        cbacAlcoholScore = 0,
        cbacWaistMale = "80-90 cm",
        cbacWaistMaleScore = 1,
        cbacWaistFemale = null,
        cbacWaistFemaleScore = null,
        cbacPhysicalActivity = "Yes",
        cbacPhysicalActivityScore = 0,
        cbacFamilyHistoryBpdiabetes = "Yes",
        cbacFamilyHistoryBpdiabetesScore = 2,
        cbacShortnessBreath = "No",
        cbacCough2weeks = "No",
        cbacBloodsputum = "No",
        cbacFever2weeks = "No",
        cbacWeightLoss = "No",
        cbacNightSweats = "No",
        cbacAntiTBDrugs = "No",
        cbacTb = "No",
        cbacTBHistory = "No",
        cbacUlceration = "No",
        cbacRecurrentTingling = "No",
        cbacFitsHistory = "No",
        cbacMouthopeningDifficulty = "No",
        cbacMouthUlcers = "No",
        cbacMouthUlcersGrowth = "No",
        cbacMouthredpatch = "No",
        cbacPainchewing = "No",
        cbacTonechange = "No",
        cbacHypopigmentedpatches = "No",
        cbacThickenedskin = "No",
        cbacNodulesonskin = "No",
        cbacRecurrentNumbness = "No",
        cbacBlurredVision = "No",
        cbacDifficultyreading = "No",
        cbacPainineyes = "No",
        cbacRednessPain = "No",
        cbacDifficultyHearing = "No",
        cbacClawingfingers = "No",
        cbacHandTingling = "No",
        cbacInabilityCloseeyelid = "No",
        cbacDifficultHoldingObjects = "No",
        cbacFeetweakness = "No",
        cbacLumpBreast = "No",
        cbacBloodnippleDischarge = "No",
        cbacBreastsizechange = "No",
        cbacBleedingPeriods = "No",
        cbacBleedingMenopause = "No",
        cbacBleedingIntercourse = "No",
        cbacVaginalDischarge = "No",
        cbacFeelingUnsteady = "No",
        cbacPhysicalDisabilitySuffering = "No",
        cbacNeedhelpEverydayActivities = "No",
        cbacForgetnearones = "No",
        CbacOccupationalExposure = "No",
        CbacLittleInterestPleasure = "No",
        CbacDepressedhopeless = "No",
        CbacCookingOil = "Mustard",
        totalScore = 6,
        CbacOccupationalExposureScore = 0,
        CbacLittleInterestPleasureScore = 0,
        CbacDepressedhopelessScore = 0,
        CbacCookingOilScore = 0,
        CbacFeelingDownScore = 0,
        isRefer = true
    )

    @Test
    fun `CbacPostNew scoring fields are readable`() {
        val post = postNew()
        assertEquals("40-49", post.cbacAge)
        assertEquals(1, post.cbacAgeScore)
        assertEquals("Yes", post.cbacConsumeGutka)
        assertEquals(2, post.cbacConsumeGutkaScore)
        assertEquals("No", post.cbacAlcohol)
        assertEquals(0, post.cbacAlcoholScore)
        assertEquals("80-90 cm", post.cbacWaistMale)
        assertEquals(1, post.cbacWaistMaleScore)
        assertNull(post.cbacWaistFemale)
        assertNull(post.cbacWaistFemaleScore)
        assertEquals("Yes", post.cbacPhysicalActivity)
        assertEquals(0, post.cbacPhysicalActivityScore)
        assertEquals("Yes", post.cbacFamilyHistoryBpdiabetes)
        assertEquals(2, post.cbacFamilyHistoryBpdiabetesScore)
        assertEquals(6, post.totalScore)
        assertTrue(post.isRefer)
    }

    @Test
    fun `CbacPostNew tuberculosis symptom fields are readable`() {
        val post = postNew()
        assertEquals("No", post.cbacShortnessBreath)
        assertEquals("No", post.cbacCough2weeks)
        assertEquals("No", post.cbacBloodsputum)
        assertEquals("No", post.cbacFever2weeks)
        assertEquals("No", post.cbacWeightLoss)
        assertEquals("No", post.cbacNightSweats)
        assertEquals("No", post.cbacAntiTBDrugs)
        assertEquals("No", post.cbacTb)
        assertEquals("No", post.cbacTBHistory)
    }

    @Test
    fun `CbacPostNew oral and skin symptom fields are readable`() {
        val post = postNew()
        assertEquals("No", post.cbacUlceration)
        assertEquals("No", post.cbacRecurrentTingling)
        assertEquals("No", post.cbacFitsHistory)
        assertEquals("No", post.cbacMouthopeningDifficulty)
        assertEquals("No", post.cbacMouthUlcers)
        assertEquals("No", post.cbacMouthUlcersGrowth)
        assertEquals("No", post.cbacMouthredpatch)
        assertEquals("No", post.cbacPainchewing)
        assertEquals("No", post.cbacTonechange)
        assertEquals("No", post.cbacHypopigmentedpatches)
        assertEquals("No", post.cbacThickenedskin)
        assertEquals("No", post.cbacNodulesonskin)
        assertEquals("No", post.cbacRecurrentNumbness)
    }

    @Test
    fun `CbacPostNew sensory and limb symptom fields are readable`() {
        val post = postNew()
        assertEquals("No", post.cbacBlurredVision)
        assertEquals("No", post.cbacDifficultyreading)
        assertEquals("No", post.cbacPainineyes)
        assertEquals("No", post.cbacRednessPain)
        assertEquals("No", post.cbacDifficultyHearing)
        assertEquals("No", post.cbacClawingfingers)
        assertEquals("No", post.cbacHandTingling)
        assertEquals("No", post.cbacInabilityCloseeyelid)
        assertEquals("No", post.cbacDifficultHoldingObjects)
        assertEquals("No", post.cbacFeetweakness)
    }

    @Test
    fun `CbacPostNew female and elderly symptom fields are readable`() {
        val post = postNew()
        assertEquals("No", post.cbacLumpBreast)
        assertEquals("No", post.cbacBloodnippleDischarge)
        assertEquals("No", post.cbacBreastsizechange)
        assertEquals("No", post.cbacBleedingPeriods)
        assertEquals("No", post.cbacBleedingMenopause)
        assertEquals("No", post.cbacBleedingIntercourse)
        assertEquals("No", post.cbacVaginalDischarge)
        assertEquals("No", post.cbacFeelingUnsteady)
        assertEquals("No", post.cbacPhysicalDisabilitySuffering)
        assertEquals("No", post.cbacNeedhelpEverydayActivities)
        assertEquals("No", post.cbacForgetnearones)
    }

    @Test
    fun `CbacPostNew mental health fields fall back to declared defaults`() {
        val post = postNew()
        assertEquals("No", post.CbacOccupationalExposure)
        assertEquals("", post.CbacBotheredProblemLast2weeks)
        assertEquals("No", post.CbacLittleInterestPleasure)
        assertEquals("No", post.CbacDepressedhopeless)
        assertEquals("", post.CbacDiscolorationSkin)
        assertEquals("Mustard", post.CbacCookingOil)
        assertEquals(0, post.CbacOccupationalExposureScore)
        assertEquals(0, post.CbacBotheredProblemLast2weeksScore)
        assertEquals(0, post.CbacLittleInterestPleasureScore)
        assertEquals(0, post.CbacDepressedhopelessScore)
        assertEquals(0, post.CbacCookingOilScore)
        assertEquals(0, post.CbacFeelingDownScore)
    }

    @Test
    fun `CbacVisitDetails required fields are readable and optionals default to null`() {
        val details = CbacVisitDetails(
            beneficiaryRegID = 500L,
            providerServiceMapID = 12,
            visitReason = "New Chief Complaint",
            visitCategory = "NCD screening",
            IdrsOrCbac = "CBAC",
            createdBy = "creator",
            vanID = 4,
            parkingPlaceID = 7
        )

        assertEquals(500L, details.beneficiaryRegID)
        assertEquals(12, details.providerServiceMapID)
        assertEquals("New Chief Complaint", details.visitReason)
        assertEquals("NCD screening", details.visitCategory)
        assertEquals("CBAC", details.IdrsOrCbac)
        assertEquals("creator", details.createdBy)
        assertEquals(4, details.vanID)
        assertEquals(7, details.parkingPlaceID)
        assertNull(details.visitNo)
        assertNull(details.subVisitCategory)
        assertNull(details.pregnancyStatus)
        assertNull(details.followUpForFpMethod)
        assertNull(details.otherFollowUpForFpMethod)
        assertNull(details.sideEffects)
        assertNull(details.otherSideEffects)
        assertNull(details.rCHID)
        assertNull(details.healthFacilityType)
        assertNull(details.healthFacilityLocation)
        assertNull(details.reportFilePath)
        assertNull(details.fileIDs)
    }

    @Test
    fun `CbacVisitDetails optional fields are readable when supplied`() {
        val details = CbacVisitDetails(
            beneficiaryRegID = 501L,
            providerServiceMapID = 13,
            visitNo = 2,
            visitReason = "Follow Up",
            visitCategory = "NCD care",
            subVisitCategory = "Sub",
            pregnancyStatus = "No",
            followUpForFpMethod = "IUCD",
            otherFollowUpForFpMethod = "Other FP",
            sideEffects = "None",
            otherSideEffects = "Other SE",
            IdrsOrCbac = "IDRS",
            rCHID = "RCH1",
            healthFacilityType = "PHC",
            healthFacilityLocation = "Village",
            reportFilePath = "/tmp/report",
            createdBy = "creator",
            vanID = 5,
            parkingPlaceID = 8,
            fileIDs = "1,2"
        )

        assertEquals(2, details.visitNo)
        assertEquals("Follow Up", details.visitReason)
        assertEquals("NCD care", details.visitCategory)
        assertEquals("Sub", details.subVisitCategory)
        assertEquals("No", details.pregnancyStatus)
        assertEquals("IUCD", details.followUpForFpMethod)
        assertEquals("Other FP", details.otherFollowUpForFpMethod)
        assertEquals("None", details.sideEffects)
        assertEquals("Other SE", details.otherSideEffects)
        assertEquals("IDRS", details.IdrsOrCbac)
        assertEquals("RCH1", details.rCHID)
        assertEquals("PHC", details.healthFacilityType)
        assertEquals("Village", details.healthFacilityLocation)
        assertEquals("/tmp/report", details.reportFilePath)
        assertEquals("1,2", details.fileIDs)
    }

    @Test
    fun `CbacRequest wraps visit details and cbac payload`() {
        val details = CbacVisitDetails(
            beneficiaryRegID = 502L,
            providerServiceMapID = 14,
            visitReason = "New Chief Complaint",
            visitCategory = "NCD screening",
            IdrsOrCbac = "CBAC",
            createdBy = "creator",
            vanID = 4,
            parkingPlaceID = 7
        )
        val wrapper = VisitDetailsWrapper(visitDetails = details)
        val request = CbacRequest(
            visitDetails = wrapper,
            cbac = postNew(),
            benFlowID = 900L,
            beneficiaryID = 502L,
            sessionID = 3,
            parkingPlaceID = 7,
            createdBy = "creator",
            vanID = 4,
            beneficiaryRegID = 502L,
            benVisitID = 901L,
            providerServiceMapID = 14,
            isFlw = true
        )

        assertEquals(details, request.visitDetails.visitDetails)
        assertEquals(6, request.cbac.totalScore)
        assertEquals(900L, request.benFlowID)
        assertEquals(502L, request.beneficiaryID)
        assertEquals(3, request.sessionID)
        assertEquals(7, request.parkingPlaceID)
        assertEquals("creator", request.createdBy)
        assertEquals(4, request.vanID)
        assertEquals(502L, request.beneficiaryRegID)
        assertEquals(901L, request.benVisitID)
        assertEquals(14, request.providerServiceMapID)
        assertEquals(true, request.isFlw)
    }
}
