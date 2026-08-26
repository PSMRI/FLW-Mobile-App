package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Tests for the pure mapper functions in HBYC.kt:
 *  - HbycPost.toCache(houseoldId)
 *  - HBYCCache.asPostModel(user, household, ben, hbycCount)
 */
class HbycMappingTest {

    private fun location() = LocationRecord(
        country = LocationEntity(1, "India"),
        state = LocationEntity(2, "State"),
        district = LocationEntity(3, "District"),
        block = LocationEntity(4, "Block"),
        village = LocationEntity(5, "Village")
    )

    private fun user() = User(
        userId = 100,
        name = "ASHA",
        userName = "asha1",
        password = "pwd",
        role = "ASHA",
        serviceMapId = 55,
        state = LocationEntity(2, "State"),
        district = LocationEntity(3, "District"),
        block = LocationEntity(4, "Block"),
        villages = emptyList()
    )

    private fun household(loc: LocationRecord = location()) = HouseholdCache(
        householdId = 500L,
        ashaId = 7,
        locationRecord = loc,
        processed = "N",
        isDraft = false
    )

    private fun ben(firstName: String? = null, genderId: Int = 0) = BenRegCache(
        householdId = 500L,
        beneficiaryId = 21L,
        isDeath = false,
        reasonOfDeathId = 0,
        placeOfDeathId = 0,
        ashaId = 7,
        isKid = true,
        isAdult = false,
        locationRecord = location(),
        syncState = SyncState.UNSYNCED,
        isDraft = false,
        firstName = firstName,
        genderId = genderId
    )

    @Test
    fun `HBYCCache asPostModel maps optional numeric fields when present`() {
        val cache = HBYCCache(
            benId = 21L,
            hhId = 500L,
            month = "6",
            subcenterName = "SC",
            year = "2024",
            primaryHealthCenterName = "PHC",
            villagePopulation = "200",
            infantPopulation = "10",
            visitdate = 1_700_000_000_000L,
            hbycAgeCategory = "5",
            byHeightLength = 3,
            wasIll = 1,
            referred = 1,
            ironFolicAcidGiven = 2,
            childrenWeighingLessReferred = 1,
            orsPacketDelivered = 3,
            orsShortage = 0,
            deathAge = "4 months",
            deathCause = "cause",
            deathPlace = 2,
            qmOrAnmInformed = 1,
            superVisorOn = 0,
            vitaminASupplementsGiven = 1,
            supplementsGiven = 1,
            weightAccordingToAge = 1,
            processed = "P",
            syncState = SyncState.SYNCED,
            createdBy = "creator",
            createdDate = 1_600_000_000_000L
        )

        val post = cache.asPostModel(
            user(), household(), ben(firstName = "Baby", genderId = 1), hbycCount = 9
        )

        assertEquals(21L, post.beneficiaryid)
        assertEquals(5, post.childVaccinatedByAge)
        assertEquals(4, post.markAgeInMonth)
        assertEquals(6, post.month)
        assertEquals(10, post.totalNumberChildVillage)
        assertEquals(200, post.villagePopulation)
        assertEquals("Baby", post.childName)
        assertEquals(1, post.gender)
        assertEquals(1, post.deadChildGender)
        assertEquals(9, post.id)
        assertEquals(9, post.loginId)
        assertEquals("500", post.houseoldId)
        assertEquals("District", post.districtName)
        assertEquals(5, post.villageid)
        assertEquals("asha1", post.ashaWorkerNameNumber)
        assertEquals("100", post.anmNameNumber)
    }

    @Test
    fun `HBYCCache asPostModel maps optional numeric fields to null when absent`() {
        val cache = HBYCCache(
            benId = 1L,
            hhId = 500L,
            hbycAgeCategory = null,
            deathAge = null,
            month = null,
            infantPopulation = null,
            villagePopulation = null,
            processed = "P",
            syncState = SyncState.SYNCED
        )

        val post = cache.asPostModel(user(), household(), ben(), hbycCount = 1)

        assertNull(post.childVaccinatedByAge)
        assertNull(post.markAgeInMonth)
        assertNull(post.month)
        assertNull(post.totalNumberChildVillage)
        assertNull(post.villagePopulation)
        assertNull(post.childName)
    }

    @Test
    fun `HBYCCache destructuring exposes all component values`() {
        val cache = HBYCCache(
            id = 3,
            benId = 21L,
            hhId = 500L,
            month = "6",
            subcenterName = "SC",
            year = "2024",
            primaryHealthCenterName = "PHC",
            villagePopulation = "200",
            infantPopulation = "10",
            visitdate = 1_700_000_000_000L,
            hbycAgeCategory = "5",
            orsPacketDelivered = 3,
            ironFolicAcidGiven = 2,
            isVaccinatedByAge = 1,
            wasIll = 1,
            referred = 1,
            supplementsGiven = 1,
            byHeightLength = 3,
            childrenWeighingLessReferred = 1,
            weightAccordingToAge = 1,
            delayInDevelopment = 0,
            referredToHealthInstitite = 0,
            vitaminASupplementsGiven = 1,
            deathAge = "4 months",
            deathCause = "cause",
            qmOrAnmInformed = 1,
            deathPlace = 2,
            superVisorOn = 0,
            orsShortage = 0,
            ifaDecreased = 0,
            processed = "P",
            syncState = SyncState.SYNCED,
            createdBy = "creator",
            createdDate = 1_600_000_000_000L
        )

        val (
            id, benId, hhId, month, subcenterName, year, primaryHealthCenterName,
            villagePopulation, infantPopulation, visitdate, hbycAgeCategory, orsPacketDelivered,
            ironFolicAcidGiven, isVaccinatedByAge, wasIll, referred, supplementsGiven,
            byHeightLength, childrenWeighingLessReferred, weightAccordingToAge,
            delayInDevelopment, referredToHealthInstitite, vitaminASupplementsGiven, deathAge,
            deathCause, qmOrAnmInformed, deathPlace, superVisorOn, orsShortage, ifaDecreased,
            processed, syncState, createdBy, createdDate
        ) = cache

        assertEquals(3, id)
        assertEquals(21L, benId)
        assertEquals(500L, hhId)
        assertEquals("creator", createdBy)
        assertEquals("cause", deathCause)
        assertEquals(SyncState.SYNCED, syncState)
    }

    @Test
    fun `HbycPost toCache maps core fields and sets synced`() {
        val post = HbycPost(
            beneficiaryid = 21L,
            byHeightLenght = 3,
            childVaccinatedByAge = 5,
            childWasIll = 1,
            createdBy = "creator",
            deathCause = "cause",
            delayConstraint = 0,
            ifYesReferHospital = 1,
            ifYesThenHealth = 0,
            ironicFolicAcidSyurp = 2,
            markAgeInMonth = 4,
            month = 6,
            numberOfChildrenWeiingLess = 1,
            ors = 3,
            orsInLastMonth = 0,
            placeOfDeath = 2,
            primaryHealthCenter = "PHC",
            qmAnmWasInformed = 1,
            subCenterName = "SC",
            supperVisionFromBlock = 0,
            supplementStarted = 1,
            supplimentGiven = 1,
            totalNumberChildVillage = 10,
            weightAccordingToChildAge = 1,
            year = "2024"
        )

        val cache = post.toCache(houseoldId = 500L)

        assertEquals(21L, cache.benId)
        assertEquals(500L, cache.hhId)
        assertEquals(3, cache.byHeightLength)
        assertEquals("5", cache.hbycAgeCategory)
        assertEquals(1, cache.wasIll)
        assertEquals("creator", cache.createdBy)
        assertEquals("cause", cache.deathCause)
        assertEquals(1, cache.referred)
        assertEquals(2, cache.ironFolicAcidGiven)
        assertEquals("4 months", cache.deathAge)
        assertEquals("6", cache.month)
        assertEquals(3, cache.orsPacketDelivered)
        assertEquals(2, cache.deathPlace)
        assertEquals("PHC", cache.primaryHealthCenterName)
        assertEquals("SC", cache.subcenterName)
        assertEquals("10", cache.infantPopulation)
        assertEquals("2024", cache.year)
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test
    fun `HbycPost toCache defaults null nullable ints to zero`() {
        val post = HbycPost(
            beneficiaryid = 1L,
            createdBy = "c"
        )

        val cache = post.toCache(houseoldId = 1L)

        assertEquals(0, cache.byHeightLength)
        assertEquals(0, cache.wasIll)
        assertEquals(0, cache.referred)
        assertEquals(0, cache.ironFolicAcidGiven)
        assertEquals(0, cache.orsPacketDelivered)
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test
    fun `HbycPost copy toString and equality`() {
        val post = HbycPost(
            anmNameNumber = "anm", ashaWorkerNameNumber = "asha", beneficiaryid = 1L,
            byHeightLenght = 1, childName = "child", childVaccinatedByAge = 2, childWasIll = 1,
            createdBy = "c", createdDate = "2024-01-01", deadChildGender = 1,
            deadNameChild = "dead", deathCause = "cause", delayConstraint = 1,
            districtName = "district", gender = 1, hbycByAge = "hbyc", houseoldId = "hh1",
            id = 1, ifYesReferHospital = 1, ifYesThenHealth = 1, ironicFolicAcidSyurp = 1,
            loginId = 1, markAgeInMonth = 1, month = 1, numberOfChildrenWeiingLess = 1,
            ors = 1, orsInLastMonth = 1, placeOfDeath = 1, primaryHealthCenter = "PHC",
            qmAnmWasInformed = 1, subCenterName = "SC", supperVisionFromBlock = 1,
            supplementStarted = 1, supplimentGiven = 1, totalNumberChildVillage = 1,
            updatedBy = "u", updatedDate = "2024-01-02", villagePopulation = 1, villageid = 1,
            vistDate = "2024-01-03", vitaminASupplements = 1, weightAccordingToChildAge = 1,
            year = "2024"
        )
        val same = post.copy()
        assertEquals(post, same)
        assertEquals(post.hashCode(), same.hashCode())
        assertNotEquals(post, post.copy(year = "2025"))
        assertTrue(post.toString().contains("HbycPost"))

        assertNotEquals(post, post.copy(anmNameNumber = "Other"))
        assertNotEquals(post, post.copy(ashaWorkerNameNumber = "Other"))
        assertNotEquals(post, post.copy(beneficiaryid = 999L))
        assertNotEquals(post, post.copy(byHeightLenght = 999))
        assertNotEquals(post, post.copy(childName = "Other"))
        assertNotEquals(post, post.copy(childVaccinatedByAge = 999))
        assertNotEquals(post, post.copy(childWasIll = 999))
        assertNotEquals(post, post.copy(createdBy = "Other"))
        assertNotEquals(post, post.copy(createdDate = "Other"))
        assertNotEquals(post, post.copy(deadChildGender = 999))
        assertNotEquals(post, post.copy(deadNameChild = "Other"))
        assertNotEquals(post, post.copy(deathCause = "Other"))
        assertNotEquals(post, post.copy(delayConstraint = 999))
        assertNotEquals(post, post.copy(districtName = "Other"))
        assertNotEquals(post, post.copy(gender = 999))
        assertNotEquals(post, post.copy(hbycByAge = "Other"))
        assertNotEquals(post, post.copy(houseoldId = "Other"))
        assertNotEquals(post, post.copy(id = 999))
        assertNotEquals(post, post.copy(ifYesReferHospital = 999))
        assertNotEquals(post, post.copy(ifYesThenHealth = 999))
        assertNotEquals(post, post.copy(ironicFolicAcidSyurp = 999))
        assertNotEquals(post, post.copy(loginId = 999))
        assertNotEquals(post, post.copy(markAgeInMonth = 999))
        assertNotEquals(post, post.copy(month = 999))
        assertNotEquals(post, post.copy(numberOfChildrenWeiingLess = 999))
        assertNotEquals(post, post.copy(ors = 999))
        assertNotEquals(post, post.copy(orsInLastMonth = 999))
        assertNotEquals(post, post.copy(placeOfDeath = 999))
        assertNotEquals(post, post.copy(primaryHealthCenter = "Other"))
        assertNotEquals(post, post.copy(qmAnmWasInformed = 999))
        assertNotEquals(post, post.copy(subCenterName = "Other"))
        assertNotEquals(post, post.copy(supperVisionFromBlock = 999))
        assertNotEquals(post, post.copy(supplementStarted = 999))
        assertNotEquals(post, post.copy(supplimentGiven = 999))
        assertNotEquals(post, post.copy(totalNumberChildVillage = 999))
        assertNotEquals(post, post.copy(updatedBy = "Other"))
        assertNotEquals(post, post.copy(updatedDate = "Other"))
        assertNotEquals(post, post.copy(villagePopulation = 999))
        assertNotEquals(post, post.copy(villageid = 999))
        assertNotEquals(post, post.copy(vistDate = "Other"))
        assertNotEquals(post, post.copy(vitaminASupplements = 999))
        assertNotEquals(post, post.copy(weightAccordingToChildAge = 999))
    }

    @Test
    fun `HbycPost getters read back all constructed field values`() {
        val post = HbycPost(
            anmNameNumber = "anm", ashaWorkerNameNumber = "asha", beneficiaryid = 1L,
            byHeightLenght = 1, childName = "child", childVaccinatedByAge = 2, childWasIll = 1,
            createdBy = "c", createdDate = "2024-01-01", deadChildGender = 1,
            deadNameChild = "dead", deathCause = "cause", delayConstraint = 1,
            districtName = "district", gender = 1, hbycByAge = "hbyc", houseoldId = "hh1",
            id = 1, ifYesReferHospital = 1, ifYesThenHealth = 1, ironicFolicAcidSyurp = 1,
            loginId = 1, markAgeInMonth = 1, month = 1, numberOfChildrenWeiingLess = 1,
            ors = 1, orsInLastMonth = 1, placeOfDeath = 1, primaryHealthCenter = "PHC",
            qmAnmWasInformed = 1, subCenterName = "SC", supperVisionFromBlock = 1,
            supplementStarted = 1, supplimentGiven = 1, totalNumberChildVillage = 1,
            updatedBy = "u", updatedDate = "2024-01-02", villagePopulation = 1, villageid = 1,
            vistDate = "2024-01-03", vitaminASupplements = 1, weightAccordingToChildAge = 1,
            year = "2024"
        )

        assertEquals("anm", post.anmNameNumber)
        assertEquals("asha", post.ashaWorkerNameNumber)
        assertEquals(1L, post.beneficiaryid)
        assertEquals(1, post.byHeightLenght)
        assertEquals("child", post.childName)
        assertEquals(2, post.childVaccinatedByAge)
        assertEquals(1, post.childWasIll)
        assertEquals("c", post.createdBy)
        assertEquals("2024-01-01", post.createdDate)
        assertEquals(1, post.deadChildGender)
        assertEquals("dead", post.deadNameChild)
        assertEquals("cause", post.deathCause)
        assertEquals(1, post.delayConstraint)
        assertEquals("district", post.districtName)
        assertEquals(1, post.gender)
        assertEquals("hbyc", post.hbycByAge)
        assertEquals("hh1", post.houseoldId)
        assertEquals(1, post.id)
        assertEquals(1, post.ifYesReferHospital)
        assertEquals(1, post.ifYesThenHealth)
        assertEquals(1, post.ironicFolicAcidSyurp)
        assertEquals(1, post.loginId)
        assertEquals(1, post.markAgeInMonth)
        assertEquals(1, post.month)
        assertEquals(1, post.numberOfChildrenWeiingLess)
        assertEquals(1, post.ors)
        assertEquals(1, post.orsInLastMonth)
        assertEquals(1, post.placeOfDeath)
        assertEquals("PHC", post.primaryHealthCenter)
        assertEquals(1, post.qmAnmWasInformed)
        assertEquals("SC", post.subCenterName)
        assertEquals(1, post.supperVisionFromBlock)
        assertEquals(1, post.supplementStarted)
        assertEquals(1, post.supplimentGiven)
        assertEquals(1, post.totalNumberChildVillage)
        assertEquals("u", post.updatedBy)
        assertEquals("2024-01-02", post.updatedDate)
        assertEquals(1, post.villagePopulation)
        assertEquals(1, post.villageid)
        assertEquals("2024-01-03", post.vistDate)
        assertEquals(1, post.vitaminASupplements)
        assertEquals(1, post.weightAccordingToChildAge)
        assertEquals("2024", post.year)
    }
}
