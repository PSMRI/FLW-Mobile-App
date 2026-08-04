package org.piramalswasthya.sakhi.model

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Tests for [HBNCCache.asPostModel], the Cache -> Post mapper that the
 * pre-existing HbncMappingTest deliberately skipped. A relaxed [User] mock
 * supplies the only field the mapper reads (userName); the embedded sub-forms
 * are exercised both absent (null) and present.
 */
class HBNCCacheTest {

    private fun user(): User {
        val u = mockk<User>(relaxed = true)
        every { u.userName } returns "asha01"
        return u
    }

    private fun baseCache() = HBNCCache(
        id = 4,
        benId = 42L,
        hhId = 100L,
        homeVisitDate = 3,
        syncState = SyncState.UNSYNCED
    )

    private fun visitCard() = HbncVisitCard(
        ashaName = "Asha",
        villageName = "V",
        subCenterName = "SC",
        blockName = "B",
        motherName = "Mother",
        fatherName = "Father",
        dateOfDelivery = 1_700_000_000_000L,
        placeOfDelivery = 1,
        babyGender = 2,
        typeOfDelivery = 1,
        stillBirth = 0,
        startedBreastFeeding = 1,
        dischargeDateMother = 1_700_000_000_000L,
        dischargeDateBaby = 1_700_000_000_000L,
        weightInGrams = 3000,
        registrationOfBirth = 1
    )

    private fun part1() = HbncPartI(
        dateOfVisit = 1_700_000_000_000L,
        babyAlive = 1,
        dateOfBabyDeath = 0L,
        timeOfBabyDeath = null,
        placeOfBabyDeath = 1,
        otherPlaceOfBabyDeath = null,
        isBabyPreterm = 0,
        gestationalAge = 39,
        dateOfFirstExamination = 1_700_000_000_000L,
        timeOfFirstExamination = "10:00",
        motherAlive = 1,
        dateOfMotherDeath = 0L,
        timeOfMotherDeath = null,
        placeOfMotherDeath = 0,
        otherPlaceOfMotherDeath = null,
        motherAnyProblem = "none",
        babyFirstFed = 1,
        otherBabyFirstFed = null,
        timeBabyFirstFed = "11:00",
        howBabyTookFirstFeed = 1,
        motherHasBreastFeedProblem = 0,
        motherBreastFeedProblem = null
    )

    private fun part2() = HbncPartII(
        dateOfVisit = 1_700_000_000_000L,
        babyTemperature = "37",
        babyEyeCondition = 1,
        babyUmbilicalBleed = 0,
        actionBabyUmbilicalBleed = 0,
        babyWeight = "3.2",
        babyWeightMatchesColor = 1,
        babyWeightColorOnScale = 2,
        allLimbsLimp = 0,
        feedLessStop = 0,
        cryWeakStop = 0,
        dryBaby = 1,
        wrapClothCloseToMother = 1,
        exclusiveBreastFeeding = 1,
        cordCleanDry = 1,
        unusualInBaby = 0,
        otherUnusualInBaby = null
    )

    private fun homeVisit() = HbncHomeVisit(
        dateOfVisit = 1_700_000_000_000L,
        babyAlive = 1,
        numTimesFullMeal24hr = 5,
        numPadChanged24hr = 4,
        babyKeptWarmWinter = 1,
        babyFedProperly = 1,
        babyCryContinuously = 0,
        motherTemperature = "37",
        foulDischargeFever = 0,
        motherSpeakAbnormallyFits = 0,
        motherLessNoMilk = 0,
        motherBreastProblem = 0,
        babyEyesSwollen = 0,
        babyWeight = "3.5",
        babyTemperature = "36",
        babyYellow = 0,
        babyImmunizationStatus = "done",
        babyReferred = 0,
        dateOfBabyReferral = 1_700_000_000_000L,
        placeOfBabyReferral = 1,
        otherPlaceOfBabyReferral = null,
        motherReferred = 0,
        dateOfMotherReferral = 1_700_000_000_000L,
        placeOfMotherReferral = 2,
        otherPlaceOfMotherReferral = null,
        allLimbsLimp = 0,
        feedingLessStopped = 0,
        cryWeakStopped = 0,
        bloatedStomach = 0,
        coldOnTouch = 0,
        chestDrawing = 0,
        breathFast = 0,
        pusNavel = 0,
        sup = 1,
        supName = "Sup",
        supComment = "ok",
        supSignDate = 1_700_000_000_000L
    )

    @Test
    fun `asPostModel with all sub-forms null maps top level fields only`() {
        val post = baseCache().asPostModel(user())

        assertEquals(4, post.id)
        assertEquals(42L, post.benId)
        assertEquals(100L, post.hhId)
        assertEquals(3, post.homeVisitDate)
        assertNull(post.hbncVisitCardDTO)
        assertNull(post.hbncPart1DTO)
        assertNull(post.hbncPart2DTO)
        assertNull(post.hbncVisitDTO)
    }

    @Test
    fun `asPostModel maps embedded visit card`() {
        val cache = baseCache().apply { visitCard = visitCard() }

        val post = cache.asPostModel(user())

        assertNotNull(post.hbncVisitCardDTO)
        val dto = post.hbncVisitCardDTO!!
        assertEquals("Asha", dto.ashaName)
        assertEquals(42L, dto.benId)
        assertEquals(3, dto.visitNo)
        assertEquals(3000, dto.weightInGrams)
        // stillBirth 0 -> false, registrationOfBirth 1 -> true
        assertEquals(false, dto.stillBirth)
        assertTrue(dto.registrationOfBirth)
        assertEquals("asha01", dto.createdBy)
    }

    @Test
    fun `asPostModel maps embedded part1`() {
        val cache = baseCache().apply { part1 = part1() }

        val post = cache.asPostModel(user())

        assertNotNull(post.hbncPart1DTO)
        val dto = post.hbncPart1DTO!!
        assertTrue(dto.babyAlive)
        assertTrue(dto.motherAlive)
        assertEquals(false, dto.isBabyPreterm)
        assertEquals("39", dto.gestationalAge)
        assertEquals("none", dto.motherAnyProblem)
        assertEquals(false, dto.motherHasBreastFeedProblem)
    }

    @Test
    fun `asPostModel maps embedded part2`() {
        val cache = baseCache().apply { part2 = part2() }

        val post = cache.asPostModel(user())

        assertNotNull(post.hbncPart2DTO)
        val dto = post.hbncPart2DTO!!
        assertEquals("37", dto.babyTemperature)
        assertEquals("1", dto.babyEyeCondition)
        assertEquals(false, dto.babyUmbilicalBleed)
        assertTrue(dto.babyWeightMatchesColor)
        assertEquals(3.2f, dto.babyWeight)
        assertTrue(dto.dryBaby)
    }

    @Test
    fun `asPostModel maps embedded home visit form`() {
        val cache = baseCache().apply { homeVisitForm = homeVisit() }

        val post = cache.asPostModel(user())

        assertNotNull(post.hbncVisitDTO)
        val dto = post.hbncVisitDTO!!
        assertTrue(dto.babyAlive)
        assertEquals(5, dto.numTimesFullMeal24hr)
        assertEquals(4, dto.numPadChanged24hr)
        assertEquals(37, dto.motherTemperature)
        assertEquals(36, dto.babyTemperature)
        assertEquals("done", dto.babyImmunizationStatus)
        assertEquals("Sup", dto.supervisorName)
        assertEquals(3.5f, dto.babyWeight)
    }

    @Test
    fun `asPostModel maps all sub-forms together`() {
        val cache = baseCache().apply {
            visitCard = visitCard()
            part1 = part1()
            part2 = part2()
            homeVisitForm = homeVisit()
        }

        val post = cache.asPostModel(user())

        assertNotNull(post.hbncVisitCardDTO)
        assertNotNull(post.hbncPart1DTO)
        assertNotNull(post.hbncPart2DTO)
        assertNotNull(post.hbncVisitDTO)
    }
}
