package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AncPostAccessorTest {

    private fun post() = ANCPost(
        isActive = true,
        ancVisit = 1,
        createdBy = "creator",
        updatedBy = "modifier",
        frontFilePath = null,
        backFilePath = null
    )

    @Test
    fun `defaults are applied for omitted parameters`() {
        val post = post()
        assertEquals(0L, post.id)
        assertEquals(0L, post.benId)
        assertNull(post.ancDate)
        assertTrue(post.isActive)
        assertNull(post.lmpDate)
        assertNull(post.visitDate)
        assertNull(post.weekOfPregnancy)
        assertNull(post.serialNo)
        assertNull(post.methodOfTermination)
        assertEquals(0, post.methodOfTerminationId)
        assertNull(post.terminationDoneBy)
        assertEquals(0, post.terminationDoneById)
        assertNull(post.placeOfAnc)
        assertEquals(0, post.placeOfAncId)
        assertEquals(0, post.isPaiucdId)
        assertEquals(false, post.isYesOrNo)
        assertNull(post.isPaiucd)
        assertNull(post.dateSterilisation)
        assertNull(post.remarks)
        assertNull(post.abortionImg1)
        assertNull(post.abortionImg2)
        assertNull(post.placeOfDeath)
        assertEquals(0, post.placeOfDeathId)
        assertNull(post.otherPlaceOfDeath)
        assertNull(post.providerServiceMapID)
    }

    @Test
    fun `clinical defaults are applied for omitted parameters`() {
        val post = post()
        assertEquals(1, post.ancVisit)
        assertFalse(post.isAborted)
        assertNull(post.abortionType)
        assertNull(post.abortionFacility)
        assertNull(post.abortionDate)
        assertNull(post.weightOfPW)
        assertNull(post.bpSystolic)
        assertNull(post.bpDiastolic)
        assertNull(post.pulseRate)
        assertNull(post.hb)
        assertNull(post.fundalHeight)
        assertNull(post.urineAlbuminPresent)
        assertNull(post.bloodSugarTestDone)
        assertEquals(0, post.folicAcidTabs)
        assertEquals(0, post.ifaTabs)
        assertNull(post.isHighRisk)
        assertNull(post.highRiskCondition)
        assertNull(post.otherHighRiskCondition)
        assertNull(post.referralFacility)
        assertNull(post.isHrpConfirmed)
        assertNull(post.hrpIdentifiedBy)
        assertNull(post.isMaternalDeath)
        assertNull(post.probableCauseOfDeath)
        assertNull(post.otherCauseOfDeath)
        assertNull(post.deathDate)
        assertNull(post.isBabyDelivered)
        assertNull(post.createdDate)
        assertEquals("creator", post.createdBy)
        assertNull(post.updatedDate)
        assertEquals("modifier", post.updatedBy)
        assertNull(post.frontFilePath)
        assertNull(post.backFilePath)
    }

    @Test
    fun `visit and termination setters round trip`() {
        val post = post()
        post.lmpDate = "01-01-2024"
        post.visitDate = "02-01-2024"
        post.weekOfPregnancy = 12
        post.serialNo = "S1"
        post.methodOfTermination = "Medical"
        post.methodOfTerminationId = 2
        post.terminationDoneBy = "Doctor"
        post.terminationDoneById = 3
        post.placeOfAnc = "PHC"
        post.placeOfAncId = 4

        assertEquals("01-01-2024", post.lmpDate)
        assertEquals("02-01-2024", post.visitDate)
        assertEquals(12, post.weekOfPregnancy)
        assertEquals("S1", post.serialNo)
        assertEquals("Medical", post.methodOfTermination)
        assertEquals(2, post.methodOfTerminationId)
        assertEquals("Doctor", post.terminationDoneBy)
        assertEquals(3, post.terminationDoneById)
        assertEquals("PHC", post.placeOfAnc)
        assertEquals(4, post.placeOfAncId)
    }

    @Test
    fun `paiucd remark and death setters round trip`() {
        val post = post()
        post.isPaiucdId = 5
        post.isYesOrNo = true
        post.isPaiucd = "Yes"
        post.dateSterilisation = "03-01-2024"
        post.remarks = "note"
        post.abortionImg1 = "img1"
        post.abortionImg2 = "img2"
        post.placeOfDeath = "Home"
        post.placeOfDeathId = 6
        post.otherPlaceOfDeath = "Other"
        post.providerServiceMapID = "PSM1"
        post.frontFilePath = "front"
        post.backFilePath = "back"

        assertEquals(5, post.isPaiucdId)
        assertEquals(true, post.isYesOrNo)
        assertEquals("Yes", post.isPaiucd)
        assertEquals("03-01-2024", post.dateSterilisation)
        assertEquals("note", post.remarks)
        assertEquals("img1", post.abortionImg1)
        assertEquals("img2", post.abortionImg2)
        assertEquals("Home", post.placeOfDeath)
        assertEquals(6, post.placeOfDeathId)
        assertEquals("Other", post.otherPlaceOfDeath)
        assertEquals("PSM1", post.providerServiceMapID)
        assertEquals("front", post.frontFilePath)
        assertEquals("back", post.backFilePath)
    }

    @Test
    fun `explicitly supplied clinical values are readable`() {
        val post = ANCPost(
            id = 9L,
            benId = 8L,
            ancDate = "01-01-2024",
            isActive = false,
            ancVisit = 3,
            isAborted = true,
            abortionType = "Spontaneous",
            abortionFacility = "PHC",
            abortionDate = "02-01-2024",
            weightOfPW = 55,
            bpSystolic = 120,
            bpDiastolic = 80,
            pulseRate = 72,
            hb = 11.5,
            fundalHeight = 20,
            urineAlbuminPresent = true,
            bloodSugarTestDone = false,
            folicAcidTabs = 30,
            ifaTabs = 60,
            isHighRisk = true,
            highRiskCondition = "Anemia",
            otherHighRiskCondition = "Other",
            referralFacility = "CHC",
            isHrpConfirmed = true,
            hrpIdentifiedBy = "ANM",
            isMaternalDeath = false,
            probableCauseOfDeath = "NA",
            otherCauseOfDeath = "NA",
            deathDate = "03-01-2024",
            isBabyDelivered = true,
            createdDate = "04-01-2024",
            createdBy = "creator",
            updatedDate = "05-01-2024",
            updatedBy = "modifier",
            frontFilePath = "f",
            backFilePath = "b"
        )

        assertEquals(9L, post.id)
        assertEquals(8L, post.benId)
        assertEquals("01-01-2024", post.ancDate)
        assertFalse(post.isActive)
        assertEquals(3, post.ancVisit)
        assertTrue(post.isAborted)
        assertEquals("Spontaneous", post.abortionType)
        assertEquals("PHC", post.abortionFacility)
        assertEquals("02-01-2024", post.abortionDate)
        assertEquals(55, post.weightOfPW)
        assertEquals(120, post.bpSystolic)
        assertEquals(80, post.bpDiastolic)
        assertEquals(72, post.pulseRate)
        assertEquals(11.5, post.hb!!, 0.0001)
        assertEquals(20, post.fundalHeight)
        assertEquals(true, post.urineAlbuminPresent)
        assertEquals(false, post.bloodSugarTestDone)
        assertEquals(30, post.folicAcidTabs)
        assertEquals(60, post.ifaTabs)
        assertEquals(true, post.isHighRisk)
        assertEquals("Anemia", post.highRiskCondition)
        assertEquals("Other", post.otherHighRiskCondition)
        assertEquals("CHC", post.referralFacility)
        assertEquals(true, post.isHrpConfirmed)
        assertEquals("ANM", post.hrpIdentifiedBy)
        assertEquals(false, post.isMaternalDeath)
        assertEquals("NA", post.probableCauseOfDeath)
        assertEquals("NA", post.otherCauseOfDeath)
        assertEquals("03-01-2024", post.deathDate)
        assertEquals(true, post.isBabyDelivered)
        assertEquals("04-01-2024", post.createdDate)
        assertEquals("05-01-2024", post.updatedDate)
        assertEquals("f", post.frontFilePath)
        assertEquals("b", post.backFilePath)
    }
}
