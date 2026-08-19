package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HbncPostAccessorTest {

    private fun visitPost() = HBNCVisitPost(
        id = 1L,
        benId = 2L,
        visitNo = 3,
        dateOfVisit = null,
        babyAlive = false,
        numTimesFullMeal24hr = 0,
        numPadChanged24hr = 0,
        babyKeptWarmWinter = false,
        babyFedProperly = false,
        babyCryContinuously = false,
        motherTemperature = 0,
        foulDischargeFever = false,
        motherSpeakAbnormallyFits = false,
        motherLessNoMilk = false,
        motherBreastProblem = false,
        babyEyesSwollen = false,
        babyWeight = null,
        babyTemperature = 0,
        babyYellow = false,
        babyImmunizationStatus = null,
        babyReferred = false,
        dateOfBabyReferral = null,
        placeOfBabyReferral = "0",
        otherPlaceOfBabyReferral = null,
        motherReferred = false,
        dateOfMotherReferral = null,
        placeOfMotherReferral = "0",
        otherPlaceOfMotherReferral = null,
        allLimbsLimp = false,
        feedingLessStopped = false,
        cryWeakStopped = false,
        bloatedStomach = false,
        coldOnTouch = false,
        chestDrawing = false,
        breathFast = false,
        pusNavel = "0",
        supervisor = "0",
        supervisorName = null,
        supervisorComment = null,
        supervisorSignDate = null,
        createdBy = null,
        createdDate = null,
        updatedDate = null,
        updatedBy = null
    )

    private fun part1Post() = HBNCPart1Post(
        id = 1L,
        benId = 2L,
        visitNo = 1,
        dateOfVisit = null,
        babyAlive = false,
        dateOfBabyDeath = null,
        timeOfBabyDeath = null,
        placeOfBabyDeath = "0",
        otherPlaceOfBabyDeath = null,
        isBabyPreterm = false,
        gestationalAge = "0",
        dateOfFirstExamination = null,
        timeOfFirstExamination = null,
        motherAlive = false,
        dateOfMotherDeath = null,
        timeOfMotherDeath = null,
        placeOfMotherDeath = "0",
        otherPlaceOfMotherDeath = null,
        motherAnyProblem = null,
        babyFirstFed = "0",
        otherBabyFirstFed = null,
        timeBabyFirstFed = null,
        howBabyTookFirstFeed = "0",
        motherHasBreastFeedProblem = false,
        motherBreastFeedProblem = null,
        createdBy = null,
        createdDate = null,
        updatedDate = null,
        updatedBy = null
    )

    private fun part2Post() = HBNCPart2Post(
        id = 1L,
        benId = 2L,
        visitNo = 2,
        dateOfVisit = null,
        babyTemperature = null,
        babyEyeCondition = "0",
        babyUmbilicalBleed = false,
        actionBabyUmbilicalBleed = false,
        babyWeight = null,
        babyWeightMatchesColor = false,
        babyWeightColorOnScale = "0",
        allLimbsLimp = false,
        feedLessStop = false,
        cryWeakStop = false,
        dryBaby = false,
        wrapClothCloseToMother = false,
        exclusiveBreastFeeding = false,
        cordCleanDry = false,
        unusualInBaby = "0",
        otherUnusualInBaby = null,
        createdBy = null,
        createdDate = null,
        updatedDate = null,
        updatedBy = null
    )

    @Test
    fun `HBNCVisitPost identity and count fields round trip`() {
        val post = visitPost()
        post.id = 100L
        post.benId = 200L
        post.visitNo = 6
        post.dateOfVisit = "01-01-2024"
        post.numTimesFullMeal24hr = 5
        post.numPadChanged24hr = 4
        post.motherTemperature = 38
        post.babyTemperature = 36
        post.babyWeight = 3.2f
        post.babyImmunizationStatus = "done"

        assertEquals(100L, post.id)
        assertEquals(200L, post.benId)
        assertEquals(6, post.visitNo)
        assertEquals("01-01-2024", post.dateOfVisit)
        assertEquals(5, post.numTimesFullMeal24hr)
        assertEquals(4, post.numPadChanged24hr)
        assertEquals(38, post.motherTemperature)
        assertEquals(36, post.babyTemperature)
        assertEquals(3.2f, post.babyWeight!!, 0.001f)
        assertEquals("done", post.babyImmunizationStatus)
    }

    @Test
    fun `HBNCVisitPost mother and baby flags round trip`() {
        val post = visitPost()
        post.babyAlive = true
        post.babyKeptWarmWinter = true
        post.babyFedProperly = true
        post.babyCryContinuously = true
        post.foulDischargeFever = true
        post.motherSpeakAbnormallyFits = true
        post.motherLessNoMilk = true
        post.motherBreastProblem = true
        post.babyEyesSwollen = true
        post.babyYellow = true

        assertTrue(post.babyAlive)
        assertTrue(post.babyKeptWarmWinter)
        assertTrue(post.babyFedProperly)
        assertTrue(post.babyCryContinuously)
        assertTrue(post.foulDischargeFever)
        assertTrue(post.motherSpeakAbnormallyFits)
        assertTrue(post.motherLessNoMilk)
        assertTrue(post.motherBreastProblem)
        assertTrue(post.babyEyesSwollen)
        assertTrue(post.babyYellow)
    }

    @Test
    fun `HBNCVisitPost referral fields round trip`() {
        val post = visitPost()
        post.babyReferred = true
        post.dateOfBabyReferral = "1690000000000"
        post.placeOfBabyReferral = "2"
        post.otherPlaceOfBabyReferral = "OtherB"
        post.motherReferred = true
        post.dateOfMotherReferral = "02-02-2024"
        post.placeOfMotherReferral = "3"
        post.otherPlaceOfMotherReferral = "OtherM"

        assertTrue(post.babyReferred)
        assertEquals("1690000000000", post.dateOfBabyReferral)
        assertEquals("2", post.placeOfBabyReferral)
        assertEquals("OtherB", post.otherPlaceOfBabyReferral)
        assertTrue(post.motherReferred)
        assertEquals("02-02-2024", post.dateOfMotherReferral)
        assertEquals("3", post.placeOfMotherReferral)
        assertEquals("OtherM", post.otherPlaceOfMotherReferral)
    }

    @Test
    fun `HBNCVisitPost danger sign and supervisor fields round trip`() {
        val post = visitPost()
        post.allLimbsLimp = true
        post.feedingLessStopped = true
        post.cryWeakStopped = true
        post.bloatedStomach = true
        post.coldOnTouch = true
        post.chestDrawing = true
        post.breathFast = true
        post.pusNavel = "1"
        post.supervisor = "1"
        post.supervisorName = "Sup"
        post.supervisorComment = "ok"
        post.supervisorSignDate = "03-03-2024"
        post.createdBy = "c"
        post.createdDate = "04-04-2024"
        post.updatedBy = "u"
        post.updatedDate = "05-05-2024"

        assertTrue(post.allLimbsLimp)
        assertTrue(post.feedingLessStopped)
        assertTrue(post.cryWeakStopped)
        assertTrue(post.bloatedStomach)
        assertTrue(post.coldOnTouch)
        assertTrue(post.chestDrawing)
        assertTrue(post.breathFast)
        assertEquals("1", post.pusNavel)
        assertEquals("1", post.supervisor)
        assertEquals("Sup", post.supervisorName)
        assertEquals("ok", post.supervisorComment)
        assertEquals("03-03-2024", post.supervisorSignDate)
        assertEquals("c", post.createdBy)
        assertEquals("04-04-2024", post.createdDate)
        assertEquals("u", post.updatedBy)
        assertEquals("05-05-2024", post.updatedDate)
    }

    @Test
    fun `HBNCVisitPost nullable fields default to null`() {
        val post = visitPost()
        assertNull(post.dateOfVisit)
        assertNull(post.babyWeight)
        assertNull(post.babyImmunizationStatus)
        assertNull(post.dateOfBabyReferral)
        assertNull(post.otherPlaceOfBabyReferral)
        assertNull(post.dateOfMotherReferral)
        assertNull(post.otherPlaceOfMotherReferral)
        assertNull(post.supervisorName)
        assertNull(post.supervisorComment)
        assertNull(post.supervisorSignDate)
        assertNull(post.createdBy)
        assertNull(post.createdDate)
        assertNull(post.updatedBy)
        assertNull(post.updatedDate)
        assertFalse(post.babyAlive)
    }

    @Test
    fun `HBNCPart1Post baby fields round trip`() {
        val post = part1Post()
        post.id = 11L
        post.benId = 22L
        post.visitNo = 2
        post.dateOfVisit = "01-01-2024"
        post.babyAlive = true
        post.dateOfBabyDeath = "02-01-2024"
        post.timeOfBabyDeath = "10:00"
        post.placeOfBabyDeath = "1"
        post.otherPlaceOfBabyDeath = "OtherB"
        post.isBabyPreterm = true
        post.gestationalAge = "36"
        post.dateOfFirstExamination = "03-01-2024"
        post.timeOfFirstExamination = "11:00"

        assertEquals(11L, post.id)
        assertEquals(22L, post.benId)
        assertEquals(2, post.visitNo)
        assertEquals("01-01-2024", post.dateOfVisit)
        assertTrue(post.babyAlive)
        assertEquals("02-01-2024", post.dateOfBabyDeath)
        assertEquals("10:00", post.timeOfBabyDeath)
        assertEquals("1", post.placeOfBabyDeath)
        assertEquals("OtherB", post.otherPlaceOfBabyDeath)
        assertTrue(post.isBabyPreterm)
        assertEquals("36", post.gestationalAge)
        assertEquals("03-01-2024", post.dateOfFirstExamination)
        assertEquals("11:00", post.timeOfFirstExamination)
    }

    @Test
    fun `HBNCPart1Post mother and feeding fields round trip`() {
        val post = part1Post()
        post.motherAlive = true
        post.dateOfMotherDeath = "04-01-2024"
        post.timeOfMotherDeath = "12:00"
        post.placeOfMotherDeath = "2"
        post.otherPlaceOfMotherDeath = "OtherM"
        post.motherAnyProblem = "fever"
        post.babyFirstFed = "1"
        post.otherBabyFirstFed = "OtherFeed"
        post.timeBabyFirstFed = "13:00"
        post.howBabyTookFirstFeed = "2"
        post.motherHasBreastFeedProblem = true
        post.motherBreastFeedProblem = "pain"
        post.createdBy = "c"
        post.createdDate = "05-01-2024"
        post.updatedBy = "u"
        post.updatedDate = "06-01-2024"

        assertTrue(post.motherAlive)
        assertEquals("04-01-2024", post.dateOfMotherDeath)
        assertEquals("12:00", post.timeOfMotherDeath)
        assertEquals("2", post.placeOfMotherDeath)
        assertEquals("OtherM", post.otherPlaceOfMotherDeath)
        assertEquals("fever", post.motherAnyProblem)
        assertEquals("1", post.babyFirstFed)
        assertEquals("OtherFeed", post.otherBabyFirstFed)
        assertEquals("13:00", post.timeBabyFirstFed)
        assertEquals("2", post.howBabyTookFirstFeed)
        assertTrue(post.motherHasBreastFeedProblem)
        assertEquals("pain", post.motherBreastFeedProblem)
        assertEquals("c", post.createdBy)
        assertEquals("05-01-2024", post.createdDate)
        assertEquals("u", post.updatedBy)
        assertEquals("06-01-2024", post.updatedDate)
    }

    @Test
    fun `HBNCPart1Post nullable fields default to null`() {
        val post = part1Post()
        assertNull(post.dateOfVisit)
        assertNull(post.dateOfBabyDeath)
        assertNull(post.timeOfBabyDeath)
        assertNull(post.otherPlaceOfBabyDeath)
        assertNull(post.dateOfFirstExamination)
        assertNull(post.timeOfFirstExamination)
        assertNull(post.dateOfMotherDeath)
        assertNull(post.timeOfMotherDeath)
        assertNull(post.otherPlaceOfMotherDeath)
        assertNull(post.motherAnyProblem)
        assertNull(post.otherBabyFirstFed)
        assertNull(post.timeBabyFirstFed)
        assertNull(post.motherBreastFeedProblem)
        assertFalse(post.isBabyPreterm)
        assertFalse(post.motherAlive)
    }

    @Test
    fun `HBNCPart2Post measurement fields round trip`() {
        val post = part2Post()
        post.id = 31L
        post.benId = 41L
        post.visitNo = 5
        post.dateOfVisit = "01-02-2024"
        post.babyTemperature = "36.5"
        post.babyEyeCondition = "1"
        post.babyWeight = 2.8f
        post.babyWeightColorOnScale = "2"
        post.unusualInBaby = "3"
        post.otherUnusualInBaby = "rash"

        assertEquals(31L, post.id)
        assertEquals(41L, post.benId)
        assertEquals(5, post.visitNo)
        assertEquals("01-02-2024", post.dateOfVisit)
        assertEquals("36.5", post.babyTemperature)
        assertEquals("1", post.babyEyeCondition)
        assertEquals(2.8f, post.babyWeight!!, 0.001f)
        assertEquals("2", post.babyWeightColorOnScale)
        assertEquals("3", post.unusualInBaby)
        assertEquals("rash", post.otherUnusualInBaby)
    }

    @Test
    fun `HBNCPart2Post boolean fields round trip`() {
        val post = part2Post()
        post.babyUmbilicalBleed = true
        post.actionBabyUmbilicalBleed = true
        post.babyWeightMatchesColor = true
        post.allLimbsLimp = true
        post.feedLessStop = true
        post.cryWeakStop = true
        post.dryBaby = true
        post.wrapClothCloseToMother = true
        post.exclusiveBreastFeeding = true
        post.cordCleanDry = true
        post.createdBy = "c"
        post.createdDate = "02-02-2024"
        post.updatedBy = "u"
        post.updatedDate = "03-02-2024"

        assertTrue(post.babyUmbilicalBleed)
        assertTrue(post.actionBabyUmbilicalBleed)
        assertTrue(post.babyWeightMatchesColor)
        assertTrue(post.allLimbsLimp)
        assertTrue(post.feedLessStop)
        assertTrue(post.cryWeakStop)
        assertTrue(post.dryBaby)
        assertTrue(post.wrapClothCloseToMother)
        assertTrue(post.exclusiveBreastFeeding)
        assertTrue(post.cordCleanDry)
        assertEquals("c", post.createdBy)
        assertEquals("02-02-2024", post.createdDate)
        assertEquals("u", post.updatedBy)
        assertEquals("03-02-2024", post.updatedDate)
    }

    @Test
    fun `HBNCPart2Post nullable fields default to null`() {
        val post = part2Post()
        assertNull(post.dateOfVisit)
        assertNull(post.babyTemperature)
        assertNull(post.babyWeight)
        assertNull(post.otherUnusualInBaby)
        assertNull(post.createdBy)
        assertNull(post.createdDate)
        assertNull(post.updatedBy)
        assertNull(post.updatedDate)
        assertFalse(post.cordCleanDry)
    }
}
