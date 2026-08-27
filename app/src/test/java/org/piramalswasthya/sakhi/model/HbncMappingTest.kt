package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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

    @Test
    fun `HBNCPart1Post toCache maps fields with opposite booleans`() {
        val post = HBNCPart1Post(
            id = 2L, benId = 6L, visitNo = 2,
            dateOfVisit = null,
            babyAlive = false,
            dateOfBabyDeath = null,
            timeOfBabyDeath = "09:00",
            placeOfBabyDeath = "2",
            otherPlaceOfBabyDeath = "Home",
            isBabyPreterm = true,
            gestationalAge = "34",
            dateOfFirstExamination = null,
            timeOfFirstExamination = "08:00",
            motherAlive = false,
            dateOfMotherDeath = null,
            timeOfMotherDeath = "10:00",
            placeOfMotherDeath = "1",
            otherPlaceOfMotherDeath = "Hospital",
            motherAnyProblem = null,
            babyFirstFed = "2",
            otherBabyFirstFed = "Other",
            timeBabyFirstFed = "12:00",
            howBabyTookFirstFeed = "2",
            motherHasBreastFeedProblem = true,
            motherBreastFeedProblem = "Pain",
            createdBy = "c", createdDate = null, updatedDate = null, updatedBy = "u"
        )

        val cache = post.toCache()

        assertEquals(0, cache.babyAlive)
        assertEquals(1, cache.isBabyPreterm)
        assertEquals(0, cache.motherAlive)
        assertEquals(1, cache.motherHasBreastFeedProblem)
        assertEquals(2, cache.placeOfBabyDeath)
        assertEquals(34, cache.gestationalAge)
        assertEquals(1, cache.placeOfMotherDeath)
        assertNull(cache.motherAnyProblem)
        assertEquals(2, cache.babyFirstFed)
        assertEquals(2, cache.howBabyTookFirstFeed)
        assertEquals("Pain", cache.motherBreastFeedProblem)
    }

    @Test
    fun `HBNCPart1Post copy equals hashCode and components`() {
        val post = HBNCPart1Post(
            id = 1L, benId = 5L, visitNo = 1,
            dateOfVisit = null, babyAlive = true, dateOfBabyDeath = null,
            timeOfBabyDeath = null, placeOfBabyDeath = "1", otherPlaceOfBabyDeath = null,
            isBabyPreterm = false, gestationalAge = "38", dateOfFirstExamination = null,
            timeOfFirstExamination = null, motherAlive = true, dateOfMotherDeath = null,
            timeOfMotherDeath = null, placeOfMotherDeath = "2", otherPlaceOfMotherDeath = null,
            motherAnyProblem = "none", babyFirstFed = "1", otherBabyFirstFed = null,
            timeBabyFirstFed = null, howBabyTookFirstFeed = "1", motherHasBreastFeedProblem = false,
            motherBreastFeedProblem = null, createdBy = "c", createdDate = null,
            updatedDate = null, updatedBy = "u"
        )
        val same = post.copy()
        val different = post.copy(benId = 99L)

        assertEquals(post, same)
        assertEquals(post.hashCode(), same.hashCode())
        assertNotEquals(post, different)
        assertEquals(99L, different.benId)
        assertEquals(1L, post.component1())
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

    @Test
    fun `HBNCVisitPost toCache maps fields with opposite booleans and referral date`() {
        val post = HBNCVisitPost(
            id = 2L, benId = 6L, visitNo = 4,
            dateOfVisit = null,
            babyAlive = false,
            numTimesFullMeal24hr = 3,
            numPadChanged24hr = 2,
            babyKeptWarmWinter = false,
            babyFedProperly = false,
            babyCryContinuously = true,
            motherTemperature = 38,
            foulDischargeFever = true,
            motherSpeakAbnormallyFits = true,
            motherLessNoMilk = true,
            motherBreastProblem = true,
            babyEyesSwollen = true,
            babyWeight = null,
            babyTemperature = 37,
            babyYellow = true,
            babyImmunizationStatus = null,
            babyReferred = true,
            dateOfBabyReferral = "1690000000000",
            placeOfBabyReferral = "2",
            otherPlaceOfBabyReferral = "Other",
            motherReferred = true,
            dateOfMotherReferral = null,
            placeOfMotherReferral = "1",
            otherPlaceOfMotherReferral = "OtherM",
            allLimbsLimp = true,
            feedingLessStopped = true,
            cryWeakStopped = true,
            bloatedStomach = true,
            coldOnTouch = true,
            chestDrawing = true,
            breathFast = true,
            pusNavel = "1",
            supervisor = "0",
            supervisorName = null,
            supervisorComment = null,
            supervisorSignDate = null,
            createdBy = "c", createdDate = null, updatedDate = null, updatedBy = "u"
        )

        val cache = post.toCache()

        assertEquals(0, cache.babyAlive)
        assertEquals(0, cache.babyKeptWarmWinter)
        assertEquals(1, cache.babyCryContinuously)
        assertEquals("38", cache.motherTemperature)
        assertEquals("37", cache.babyTemperature)
        assertEquals("null", cache.babyWeight)
        assertEquals(1690000000000L, cache.dateOfBabyReferral)
        assertEquals(2, cache.placeOfBabyReferral)
        assertEquals(1, cache.placeOfMotherReferral)
        assertEquals(1, cache.pusNavel)
        assertEquals(0, cache.sup)
        assertNull(cache.supName)
        assertEquals(1, cache.motherReferred)
        assertEquals(1, cache.allLimbsLimp)
        assertEquals(1, cache.feedingLessStopped)
        assertEquals(1, cache.cryWeakStopped)
        assertEquals(1, cache.bloatedStomach)
        assertEquals(1, cache.coldOnTouch)
        assertEquals(1, cache.chestDrawing)
        assertEquals(1, cache.breathFast)
    }

    @Test
    fun `HBNCVisitPost copy equals hashCode and components`() {
        val post = HBNCVisitPost(
            id = 1L, benId = 5L, visitNo = 3, dateOfVisit = null, babyAlive = true,
            numTimesFullMeal24hr = 5, numPadChanged24hr = 4, babyKeptWarmWinter = true,
            babyFedProperly = true, babyCryContinuously = false, motherTemperature = 37,
            foulDischargeFever = false, motherSpeakAbnormallyFits = false, motherLessNoMilk = false,
            motherBreastProblem = false, babyEyesSwollen = false, babyWeight = 3.5f,
            babyTemperature = 36, babyYellow = false, babyImmunizationStatus = "done",
            babyReferred = false, dateOfBabyReferral = null, placeOfBabyReferral = "1",
            otherPlaceOfBabyReferral = null, motherReferred = false, dateOfMotherReferral = null,
            placeOfMotherReferral = "2", otherPlaceOfMotherReferral = null, allLimbsLimp = false,
            feedingLessStopped = false, cryWeakStopped = false, bloatedStomach = false,
            coldOnTouch = false, chestDrawing = false, breathFast = false, pusNavel = "0",
            supervisor = "1", supervisorName = "Sup", supervisorComment = "ok",
            supervisorSignDate = null, createdBy = "c", createdDate = null, updatedDate = null,
            updatedBy = "u"
        )
        val same = post.copy()
        val different = post.copy(benId = 77L)

        assertEquals(post, same)
        assertEquals(post.hashCode(), same.hashCode())
        assertNotEquals(post, different)
        assertEquals(77L, different.benId)
        assertEquals(1L, post.component1())
    }

    @Test
    fun `HBNCVisitPost destructuring exposes all component values`() {
        val post = HBNCVisitPost(
            id = 9L, benId = 42L, visitNo = 5, dateOfVisit = null, babyAlive = true,
            numTimesFullMeal24hr = 6, numPadChanged24hr = 5, babyKeptWarmWinter = true,
            babyFedProperly = true, babyCryContinuously = false, motherTemperature = 37,
            foulDischargeFever = false, motherSpeakAbnormallyFits = false, motherLessNoMilk = false,
            motherBreastProblem = false, babyEyesSwollen = false, babyWeight = 3.1f,
            babyTemperature = 36, babyYellow = false, babyImmunizationStatus = "done",
            babyReferred = false, dateOfBabyReferral = null, placeOfBabyReferral = "1",
            otherPlaceOfBabyReferral = null, motherReferred = false, dateOfMotherReferral = null,
            placeOfMotherReferral = "2", otherPlaceOfMotherReferral = null, allLimbsLimp = false,
            feedingLessStopped = false, cryWeakStopped = false, bloatedStomach = false,
            coldOnTouch = false, chestDrawing = false, breathFast = false, pusNavel = "0",
            supervisor = "1", supervisorName = "Sup", supervisorComment = "ok",
            supervisorSignDate = null, createdBy = "c", createdDate = null, updatedDate = null,
            updatedBy = "u"
        )

        val (
            id, benId, visitNo, dateOfVisit, babyAlive, numTimesFullMeal24hr, numPadChanged24hr,
            babyKeptWarmWinter, babyFedProperly, babyCryContinuously, motherTemperature,
            foulDischargeFever, motherSpeakAbnormallyFits, motherLessNoMilk, motherBreastProblem,
            babyEyesSwollen, babyWeight, babyTemperature, babyYellow, babyImmunizationStatus,
            babyReferred, dateOfBabyReferral, placeOfBabyReferral, otherPlaceOfBabyReferral,
            motherReferred, dateOfMotherReferral, placeOfMotherReferral, otherPlaceOfMotherReferral,
            allLimbsLimp, feedingLessStopped, cryWeakStopped, bloatedStomach, coldOnTouch,
            chestDrawing, breathFast, pusNavel, supervisor, supervisorName, supervisorComment,
            supervisorSignDate, createdBy, createdDate, updatedDate, updatedBy
        ) = post

        assertEquals(9L, id)
        assertEquals(42L, benId)
        assertEquals(5, visitNo)
        assertEquals(6, numTimesFullMeal24hr)
        assertEquals(37, motherTemperature)
        assertEquals("done", babyImmunizationStatus)
        assertEquals("1", placeOfBabyReferral)
        assertEquals("Sup", supervisorName)
        assertEquals("ok", supervisorComment)
        assertEquals("c", createdBy)
        assertEquals("u", updatedBy)
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

    @Test
    fun `HBNCPost toCache with visit card DTO defaults null numeric fields to zero`() {
        val cardDto = HBNCVisitCardPost(
            id = 1L, benId = 42L, visitNo = 1,
            ashaName = null, villageName = null, subCenterName = null, blockName = null,
            motherName = null, fatherName = null, dateOfDelivery = null,
            placeOfDelivery = null, babyGender = null, typeOfDelivery = null,
            stillBirth = true, startedBreastFeeding = null,
            dischargeDateMother = null, dischargeDateBaby = null,
            weightInGrams = 0, registrationOfBirth = false,
            createdBy = null, createdDate = null, updatedDate = null, updatedBy = null
        )
        val post = HBNCPost(
            id = 0, benId = 42L, hhId = 100L, homeVisitDate = 1,
            hbncVisitCardDTO = cardDto, hbncPart1DTO = null, hbncPart2DTO = null, hbncVisitDTO = null
        )

        val cache = post.toCache(householdId = 100L)

        val card = cache.visitCard!!
        assertEquals(0, card.placeOfDelivery)
        assertEquals(0, card.babyGender)
        assertEquals(0, card.typeOfDelivery)
        assertEquals(0, card.startedBreastFeeding)
        assertEquals(1, card.stillBirth)
        assertEquals(0, card.registrationOfBirth)
    }

    @Test
    fun `HBNCPost toCache with part1 DTO maps embedded part1`() {
        val part1Dto = HBNCPart1Post(
            id = 1L, benId = 42L, visitNo = 1,
            dateOfVisit = null, babyAlive = true, dateOfBabyDeath = null,
            timeOfBabyDeath = null, placeOfBabyDeath = "1", otherPlaceOfBabyDeath = null,
            isBabyPreterm = false, gestationalAge = "38", dateOfFirstExamination = null,
            timeOfFirstExamination = null, motherAlive = true, dateOfMotherDeath = null,
            timeOfMotherDeath = null, placeOfMotherDeath = "2", otherPlaceOfMotherDeath = null,
            motherAnyProblem = "none", babyFirstFed = "1", otherBabyFirstFed = null,
            timeBabyFirstFed = null, howBabyTookFirstFeed = "1", motherHasBreastFeedProblem = false,
            motherBreastFeedProblem = null, createdBy = "c", createdDate = null,
            updatedDate = null, updatedBy = "u"
        )
        val post = HBNCPost(
            id = 0, benId = 42L, hhId = 100L, homeVisitDate = 1,
            hbncVisitCardDTO = null, hbncPart1DTO = part1Dto, hbncPart2DTO = null, hbncVisitDTO = null
        )

        val cache = post.toCache(householdId = 100L)

        assertNotNull(cache.part1)
        assertEquals(1, cache.part1!!.babyAlive)
        assertEquals(1, cache.part1!!.placeOfBabyDeath)
        assertEquals(38, cache.part1!!.gestationalAge)
        assertEquals(1, cache.part1!!.motherAlive)
        assertEquals("none", cache.part1!!.motherAnyProblem)
        assertNull(cache.visitCard)
        assertNull(cache.part2)
        assertNull(cache.homeVisitForm)
    }

    @Test
    fun `HBNCPost toCache with part2 DTO maps embedded part2`() {
        val part2Dto = HBNCPart2Post(
            id = 1L, benId = 42L, visitNo = 1, dateOfVisit = null,
            babyTemperature = "37", babyEyeCondition = "1", babyUmbilicalBleed = true,
            actionBabyUmbilicalBleed = false, babyWeight = 3.2f, babyWeightMatchesColor = true,
            babyWeightColorOnScale = "2", allLimbsLimp = false, feedLessStop = false,
            cryWeakStop = false, dryBaby = true, wrapClothCloseToMother = true,
            exclusiveBreastFeeding = true, cordCleanDry = true, unusualInBaby = "0",
            otherUnusualInBaby = null, createdBy = "c", createdDate = null,
            updatedDate = null, updatedBy = "u"
        )
        val post = HBNCPost(
            id = 0, benId = 42L, hhId = 100L, homeVisitDate = 1,
            hbncVisitCardDTO = null, hbncPart1DTO = null, hbncPart2DTO = part2Dto, hbncVisitDTO = null
        )

        val cache = post.toCache(householdId = 100L)

        assertNotNull(cache.part2)
        assertEquals("37", cache.part2!!.babyTemperature)
        assertEquals(1, cache.part2!!.babyEyeCondition)
        assertEquals(1, cache.part2!!.babyUmbilicalBleed)
        assertEquals(0, cache.part2!!.actionBabyUmbilicalBleed)
        assertEquals(1, cache.part2!!.babyWeightMatchesColor)
        assertEquals(2, cache.part2!!.babyWeightColorOnScale)
        assertEquals(1, cache.part2!!.dryBaby)
        assertEquals(0, cache.part2!!.unusualInBaby)
        assertNull(cache.visitCard)
        assertNull(cache.part1)
        assertNull(cache.homeVisitForm)
    }

    @Test
    fun `HBNCPost toCache with visit DTO maps embedded home visit true branches`() {
        val visitDto = HBNCVisitPost(
            id = 1L, benId = 42L, visitNo = 2, dateOfVisit = null, babyAlive = true,
            numTimesFullMeal24hr = 5, numPadChanged24hr = 4, babyKeptWarmWinter = true,
            babyFedProperly = true, babyCryContinuously = false, motherTemperature = 37,
            foulDischargeFever = false, motherSpeakAbnormallyFits = false, motherLessNoMilk = false,
            motherBreastProblem = true, babyEyesSwollen = false, babyWeight = 3.5f,
            babyTemperature = 36, babyYellow = false, babyImmunizationStatus = "done",
            babyReferred = false, dateOfBabyReferral = null, placeOfBabyReferral = "1",
            otherPlaceOfBabyReferral = null, motherReferred = false, dateOfMotherReferral = null,
            placeOfMotherReferral = "2", otherPlaceOfMotherReferral = null, allLimbsLimp = false,
            feedingLessStopped = false, cryWeakStopped = false, bloatedStomach = false,
            coldOnTouch = false, chestDrawing = false, breathFast = false, pusNavel = "0",
            supervisor = "1", supervisorName = "Sup", supervisorComment = "ok",
            supervisorSignDate = null, createdBy = "c", createdDate = null, updatedDate = null,
            updatedBy = "u"
        )
        val post = HBNCPost(
            id = 0, benId = 42L, hhId = 100L, homeVisitDate = 2,
            hbncVisitCardDTO = null, hbncPart1DTO = null, hbncPart2DTO = null, hbncVisitDTO = visitDto
        )

        val cache = post.toCache(householdId = 100L)

        assertNotNull(cache.homeVisitForm)
        val form = cache.homeVisitForm!!
        assertEquals(1, form.babyAlive)
        assertEquals("37", form.motherTemperature)
        assertEquals("36", form.babyTemperature)
        assertEquals(1, form.motherReferred)
        assertEquals(1, form.allLimbsLimp)
        assertEquals(1, form.feedingLessStopped)
        assertEquals(1, form.cryWeakStopped)
        assertEquals(1, form.bloatedStomach)
        assertEquals(1, form.coldOnTouch)
        assertEquals(1, form.chestDrawing)
        assertEquals(1, form.breathFast)
        assertEquals(1, form.sup)
        assertEquals("Sup", form.supName)
    }

    @Test
    fun `HBNCPost toCache with visit DTO maps embedded home visit false branches`() {
        val visitDto = HBNCVisitPost(
            id = 2L, benId = 42L, visitNo = 3, dateOfVisit = null, babyAlive = false,
            numTimesFullMeal24hr = 2, numPadChanged24hr = 1, babyKeptWarmWinter = false,
            babyFedProperly = false, babyCryContinuously = true, motherTemperature = 39,
            foulDischargeFever = true, motherSpeakAbnormallyFits = true, motherLessNoMilk = true,
            motherBreastProblem = false, babyEyesSwollen = true, babyWeight = null,
            babyTemperature = 35, babyYellow = true, babyImmunizationStatus = null,
            babyReferred = true, dateOfBabyReferral = "Jan 01, 2024", placeOfBabyReferral = "2",
            otherPlaceOfBabyReferral = "Other", motherReferred = true, dateOfMotherReferral = "Jan 02, 2024",
            placeOfMotherReferral = "1", otherPlaceOfMotherReferral = "OtherM", allLimbsLimp = true,
            feedingLessStopped = true, cryWeakStopped = true, bloatedStomach = true,
            coldOnTouch = true, chestDrawing = true, breathFast = true, pusNavel = "1",
            supervisor = "0", supervisorName = null, supervisorComment = null,
            supervisorSignDate = "Jan 03, 2024", createdBy = "c", createdDate = null,
            updatedDate = null, updatedBy = "u"
        )
        val post = HBNCPost(
            id = 0, benId = 42L, hhId = 100L, homeVisitDate = 3,
            hbncVisitCardDTO = null, hbncPart1DTO = null, hbncPart2DTO = null, hbncVisitDTO = visitDto
        )

        val cache = post.toCache(householdId = 100L)

        val form = cache.homeVisitForm!!
        assertEquals(0, form.babyAlive)
        assertEquals(0, form.motherReferred)
        assertEquals(0, form.allLimbsLimp)
        assertEquals(0, form.feedingLessStopped)
        assertEquals(0, form.cryWeakStopped)
        assertEquals(0, form.bloatedStomach)
        assertEquals(0, form.coldOnTouch)
        assertEquals(0, form.chestDrawing)
        assertEquals(0, form.breathFast)
        assertEquals(2, form.placeOfBabyReferral)
        assertEquals(1, form.placeOfMotherReferral)
        assertEquals(1, form.pusNavel)
        assertEquals(0, form.sup)
        assertNull(form.supName)
        assertNotNull(form.dateOfBabyReferral)
        assertNotNull(form.dateOfMotherReferral)
        assertNotNull(form.supSignDate)
    }

    @Test
    fun `HBNCPost copy equals hashCode and components`() {
        val post = HBNCPost(
            id = 0, benId = 42L, hhId = 100L, homeVisitDate = 1,
            hbncVisitCardDTO = null, hbncPart1DTO = null, hbncPart2DTO = null, hbncVisitDTO = null
        )
        val same = post.copy()
        val different = post.copy(benId = 99L)

        assertEquals(post, same)
        assertEquals(post.hashCode(), same.hashCode())
        assertNotEquals(post, different)
        assertEquals(99L, different.benId)
        assertEquals(0, post.component1())
    }
}
