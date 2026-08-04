package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Tests for the pure Post -> Cache mapper functions in HBNC.kt:
 *  - HBNCVisitCardPost.toCache()
 *  - HBNCPart1Post.toCache()
 *  - HBNCPart2Post.toCache()
 *  - HBNCVisitPost.toCache()
 *  - HBNCPost.toCache(householdId)
 *
 * Skipped: HBNCCache.asPostModel(user) (requires a fully-built User with
 * LocationEntity graph; low value vs. the pure DTO mappers covered here).
 */
class HbncMappingTest {

    // ---------------------------------------------------------------
    // HBNCVisitCardPost.toCache()
    // ---------------------------------------------------------------

    @Test
    fun `HBNCVisitCardPost toCache maps fields and booleans to ints`() {
        val post = HBNCVisitCardPost(
            id = 1L,
            benId = 10L,
            visitNo = 1,
            ashaName = "Asha",
            villageName = "V",
            subCenterName = "SC",
            blockName = "B",
            motherName = "M",
            fatherName = "F",
            dateOfDelivery = null,
            placeOfDelivery = "1",
            babyGender = "2",
            typeOfDelivery = "1",
            stillBirth = true,
            startedBreastFeeding = "1",
            dischargeDateMother = null,
            dischargeDateBaby = null,
            weightInGrams = 3000,
            registrationOfBirth = false,
            createdBy = "c",
            createdDate = null,
            updatedDate = null,
            updatedBy = "u"
        )

        val cache = post.toCache()

        assertEquals("Asha", cache.ashaName)
        assertEquals(1, cache.placeOfDelivery)
        assertEquals(2, cache.babyGender)
        assertEquals(1, cache.typeOfDelivery)
        assertEquals(1, cache.stillBirth)
        assertEquals(1, cache.startedBreastFeeding)
        assertEquals(3000, cache.weightInGrams)
        assertEquals(0, cache.registrationOfBirth)
    }

    @Test
    fun `HBNCVisitCardPost toCache defaults null numeric strings to zero`() {
        val post = HBNCVisitCardPost(
            id = 1L, benId = 1L, visitNo = 1,
            ashaName = null, villageName = null, subCenterName = null, blockName = null,
            motherName = null, fatherName = null, dateOfDelivery = null,
            placeOfDelivery = null, babyGender = null, typeOfDelivery = null,
            stillBirth = false, startedBreastFeeding = null,
            dischargeDateMother = null, dischargeDateBaby = null,
            weightInGrams = 0, registrationOfBirth = true,
            createdBy = null, createdDate = null, updatedDate = null, updatedBy = null
        )

        val cache = post.toCache()

        assertEquals(0, cache.placeOfDelivery)
        assertEquals(0, cache.babyGender)
        assertEquals(0, cache.typeOfDelivery)
        assertEquals(0, cache.startedBreastFeeding)
        assertEquals(0, cache.stillBirth)
        assertEquals(1, cache.registrationOfBirth)
    }

    // ---------------------------------------------------------------
    // HBNCPart1Post.toCache()
    // ---------------------------------------------------------------

    @Test
    fun `HBNCPart1Post toCache maps fields`() {
        val post = HBNCPart1Post(
            id = 1L, benId = 5L, visitNo = 1,
            dateOfVisit = null,
            babyAlive = true,
            dateOfBabyDeath = null,
            timeOfBabyDeath = null,
            placeOfBabyDeath = "1",
            otherPlaceOfBabyDeath = null,
            isBabyPreterm = false,
            gestationalAge = "38",
            dateOfFirstExamination = null,
            timeOfFirstExamination = null,
            motherAlive = true,
            dateOfMotherDeath = null,
            timeOfMotherDeath = null,
            placeOfMotherDeath = "2",
            otherPlaceOfMotherDeath = null,
            motherAnyProblem = "none",
            babyFirstFed = "1",
            otherBabyFirstFed = null,
            timeBabyFirstFed = null,
            howBabyTookFirstFeed = "1",
            motherHasBreastFeedProblem = false,
            motherBreastFeedProblem = null,
            createdBy = "c", createdDate = null, updatedDate = null, updatedBy = "u"
        )

        val cache = post.toCache()

        assertEquals(1, cache.babyAlive)
        assertEquals(1, cache.placeOfBabyDeath)
        assertEquals(0, cache.isBabyPreterm)
        assertEquals(38, cache.gestationalAge)
        assertEquals(1, cache.motherAlive)
        assertEquals(2, cache.placeOfMotherDeath)
        assertEquals("none", cache.motherAnyProblem)
        assertEquals(1, cache.babyFirstFed)
        assertEquals(1, cache.howBabyTookFirstFeed)
        assertEquals(0, cache.motherHasBreastFeedProblem)
    }

    // ---------------------------------------------------------------
    // HBNCPart2Post.toCache()
    // ---------------------------------------------------------------

    @Test
    fun `HBNCPart2Post toCache maps fields`() {
        val post = HBNCPart2Post(
            id = 1L, benId = 5L, visitNo = 1,
            dateOfVisit = null,
            babyTemperature = "37",
            babyEyeCondition = "1",
            babyUmbilicalBleed = true,
            actionBabyUmbilicalBleed = false,
            babyWeight = 3.2f,
            babyWeightMatchesColor = true,
            babyWeightColorOnScale = "2",
            allLimbsLimp = false,
            feedLessStop = false,
            cryWeakStop = false,
            dryBaby = true,
            wrapClothCloseToMother = true,
            exclusiveBreastFeeding = true,
            cordCleanDry = true,
            unusualInBaby = "0",
            otherUnusualInBaby = null,
            createdBy = "c", createdDate = null, updatedDate = null, updatedBy = "u"
        )

        val cache = post.toCache()

        assertEquals("37", cache.babyTemperature)
        assertEquals(1, cache.babyEyeCondition)
        assertEquals(1, cache.babyUmbilicalBleed)
        assertEquals(0, cache.actionBabyUmbilicalBleed)
        assertEquals(1, cache.babyWeightMatchesColor)
        assertEquals(2, cache.babyWeightColorOnScale)
        assertEquals(1, cache.dryBaby)
        assertEquals(1, cache.exclusiveBreastFeeding)
        assertEquals(0, cache.unusualInBaby)
        assertNotNull(cache.babyWeight)
    }

    // ---------------------------------------------------------------
    // HBNCVisitPost.toCache()
    // ---------------------------------------------------------------

    @Test
    fun `HBNCVisitPost toCache maps fields`() {
        val post = HBNCVisitPost(
            id = 1L, benId = 5L, visitNo = 3,
            dateOfVisit = null,
            babyAlive = true,
            numTimesFullMeal24hr = 5,
            numPadChanged24hr = 4,
            babyKeptWarmWinter = true,
            babyFedProperly = true,
            babyCryContinuously = false,
            motherTemperature = 37,
            foulDischargeFever = false,
            motherSpeakAbnormallyFits = false,
            motherLessNoMilk = false,
            motherBreastProblem = false,
            babyEyesSwollen = false,
            babyWeight = 3.5f,
            babyTemperature = 36,
            babyYellow = false,
            babyImmunizationStatus = "done",
            babyReferred = false,
            dateOfBabyReferral = null,
            placeOfBabyReferral = "1",
            otherPlaceOfBabyReferral = null,
            motherReferred = false,
            dateOfMotherReferral = null,
            placeOfMotherReferral = "2",
            otherPlaceOfMotherReferral = null,
            allLimbsLimp = false,
            feedingLessStopped = false,
            cryWeakStopped = false,
            bloatedStomach = false,
            coldOnTouch = false,
            chestDrawing = false,
            breathFast = false,
            pusNavel = "0",
            supervisor = "1",
            supervisorName = "Sup",
            supervisorComment = "ok",
            supervisorSignDate = null,
            createdBy = "c", createdDate = null, updatedDate = null, updatedBy = "u"
        )

        val cache = post.toCache()

        assertEquals(1, cache.babyAlive)
        assertEquals(5, cache.numTimesFullMeal24hr)
        assertEquals(4, cache.numPadChanged24hr)
        assertEquals(1, cache.babyKeptWarmWinter)
        assertEquals("37", cache.motherTemperature)
        assertEquals("36", cache.babyTemperature)
        assertEquals("done", cache.babyImmunizationStatus)
        assertEquals(1, cache.placeOfBabyReferral)
        assertEquals(2, cache.placeOfMotherReferral)
        assertEquals(0, cache.pusNavel)
        assertEquals(1, cache.sup)
        assertEquals("Sup", cache.supName)
        assertNull(cache.dateOfBabyReferral)
    }

    // ---------------------------------------------------------------
    // HBNCPost.toCache(householdId)
    // ---------------------------------------------------------------

    @Test
    fun `HBNCPost toCache with null sub-DTOs maps top level fields`() {
        val post = HBNCPost(
            id = 0,
            benId = 42L,
            hhId = 100L,
            homeVisitDate = 3,
            hbncVisitCardDTO = null,
            hbncPart1DTO = null,
            hbncPart2DTO = null,
            hbncVisitDTO = null
        )

        val cache = post.toCache(householdId = 555L)

        assertEquals(42L, cache.benId)
        assertEquals(555L, cache.hhId)
        assertEquals(3, cache.homeVisitDate)
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
        assertNull(cache.visitCard)
        assertNull(cache.part1)
        assertNull(cache.part2)
        assertNull(cache.homeVisitForm)
    }

    @Test
    fun `HBNCPost toCache with visit card DTO maps embedded card`() {
        val cardDto = HBNCVisitCardPost(
            id = 1L, benId = 42L, visitNo = 1,
            ashaName = "Asha", villageName = null, subCenterName = null, blockName = null,
            motherName = null, fatherName = null, dateOfDelivery = null,
            placeOfDelivery = "1", babyGender = "1", typeOfDelivery = "1",
            stillBirth = false, startedBreastFeeding = "1",
            dischargeDateMother = null, dischargeDateBaby = null,
            weightInGrams = 2500, registrationOfBirth = true,
            createdBy = "c", createdDate = null, updatedDate = null, updatedBy = "u"
        )
        val post = HBNCPost(
            id = 0, benId = 42L, hhId = 100L, homeVisitDate = 1,
            hbncVisitCardDTO = cardDto,
            hbncPart1DTO = null, hbncPart2DTO = null, hbncVisitDTO = null
        )

        val cache = post.toCache(householdId = 100L)

        assertNotNull(cache.visitCard)
        assertEquals("Asha", cache.visitCard!!.ashaName)
        assertEquals(2500, cache.visitCard!!.weightInGrams)
        assertEquals(1, cache.visitCard!!.registrationOfBirth)
    }
}
