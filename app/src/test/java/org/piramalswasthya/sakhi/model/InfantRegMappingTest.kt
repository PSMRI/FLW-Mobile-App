package org.piramalswasthya.sakhi.model

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Tests for the pure mapper functions in InfantReg.kt:
 *  - InfantRegCache.asPostModel()
 *  - InfantRegPost.toCacheModel()
 *  - BenWithDoAndIrCache.asBasicDomainModel()
 *
 * Skipped: InfantRegWithBen mappers (require a BenBasicCache graph beyond what
 * BenWithDoAndIrCache already exercises).
 */
class InfantRegMappingTest {

    private fun irBen(benId: Long = 1L) = BenBasicCache(
        benId = benId,
        hhId = 2L,
        regDate = 1_600_000_000_000L,
        benName = "Jane",
        benSurname = "Doe",
        gender = Gender.FEMALE,
        dob = 500_000_000_000L,
        relToHeadId = 1,
        mobileNo = 9998887776L,
        fatherName = "Bob",
        motherName = "Mary",
        familyHeadName = "Head",
        spouseName = null,
        rchId = "RCH1",
        hrpStatus = false,
        syncState = SyncState.SYNCED,
        reproductiveStatusId = 2,
        lastMenstrualPeriod = null,
        isKid = false,
        immunizationStatus = false,
        villageId = 5,
        abhaId = "abha1",
        isNewAbha = true,
        cbacFilled = false,
        cbacSyncState = SyncState.SYNCED,
        cdrFilled = false,
        cdrSyncState = SyncState.SYNCED,
        mdsrFilled = false,
        mdsrSyncState = SyncState.SYNCED,
        pmsmaSyncState = SyncState.SYNCED,
        pmsmaFilled = false,
        hbncFilled = false,
        hbycFilled = false,
        pwrFilled = false,
        pwrSyncState = SyncState.SYNCED,
        doSyncState = SyncState.SYNCED,
        irSyncState = SyncState.SYNCED,
        crSyncState = SyncState.SYNCED,
        ecrFilled = false,
        ectFilled = false,
        tbsnFilled = false,
        tbsnSyncState = SyncState.SYNCED,
        tbspFilled = false,
        tbspSyncState = SyncState.SYNCED,
        hrppaFilled = false,
        hrpnpaFilled = false,
        hrpmbpFilled = false,
        hrptFilled = false,
        hrptrackingDone = false,
        hrnptrackingDone = false,
        hrnptFilled = false,
        hrppaSyncState = SyncState.SYNCED,
        hrpnpaSyncState = SyncState.SYNCED,
        hrpmbpSyncState = SyncState.SYNCED,
        hrptSyncState = SyncState.SYNCED,
        hrnptSyncState = SyncState.SYNCED,
        isDelivered = false,
        pwHrp = false,
        irFilled = false,
        isMdsr = false,
        crFilled = false,
        doFilled = false,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    private fun irDeliveryOutcome(liveBirth: Int?) = DeliveryOutcomeCache(
        benId = 1L,
        isActive = true,
        liveBirth = liveBirth,
        createdBy = "asha",
        updatedBy = "asha",
        syncState = SyncState.UNSYNCED
    )

    private fun irSavedInfant(babyIndex: Int) = InfantRegCache(
        motherBenId = 1L,
        isActive = true,
        babyIndex = babyIndex,
        createdBy = "asha",
        updatedBy = "asha",
        syncState = SyncState.UNSYNCED
    )

    @Test
    fun `BenWithDoAndIrCache asBasicDomainModel returns one domain per live birth including unsaved infants`() {
        val record = BenWithDoAndIrCache(
            ben = irBen(),
            deliveryOutcomeCache = listOf(irDeliveryOutcome(liveBirth = 2)),
            savedIrRecords = listOf(irSavedInfant(babyIndex = 0))
        )

        val domains = record.asBasicDomainModel(onlySavedInfants = false)

        assertEquals(2, domains.size)
        assertNotNull(domains[0].savedIr)
        assertNull(domains[1].savedIr)
    }

    @Test
    fun `BenWithDoAndIrCache asBasicDomainModel skips unsaved infants when onlySavedInfants is true`() {
        val record = BenWithDoAndIrCache(
            ben = irBen(),
            deliveryOutcomeCache = listOf(irDeliveryOutcome(liveBirth = 2)),
            savedIrRecords = listOf(irSavedInfant(babyIndex = 0))
        )

        val domains = record.asBasicDomainModel(onlySavedInfants = true)

        assertEquals(1, domains.size)
        assertEquals(0, domains[0].babyIndex)
    }

    @Test
    fun `BenWithDoAndIrCache asBasicDomainModel returns empty list when liveBirth is zero`() {
        val record = BenWithDoAndIrCache(
            ben = irBen(),
            deliveryOutcomeCache = listOf(irDeliveryOutcome(liveBirth = 0)),
            savedIrRecords = emptyList()
        )

        val domains = record.asBasicDomainModel(onlySavedInfants = false)

        assertTrue(domains.isEmpty())
    }

    // ---------------------------------------------------------------
    // InfantRegCache.asPostModel()
    // ---------------------------------------------------------------

    @Test
    fun `InfantRegCache asPostModel maps core fields`() {
        val cache = InfantRegCache(
            id = 2L,
            childBenId = 99L,
            motherBenId = 44L,
            isActive = true,
            babyName = "Baby A",
            babyIndex = 0,
            gender = Gender.MALE,
            weight = 3.1,
            breastFeedingStarted = true,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED
        )

        val post = cache.asPostModel()

        assertEquals(2L, post.id)
        assertEquals(44L, post.benId)
        assertEquals(99L, post.childBenId)
        assertTrue(post.isActive)
        assertEquals("Baby A", post.babyName)
        assertEquals(0, post.babyIndex)
        assertEquals("MALE", post.gender)
        assertEquals(3.1, post.weight, 0.0001)
        assertTrue(post.breastFeedingStarted!!)
    }

    @Test
    fun `InfantRegCache asPostModel maps null gender and defaults weight`() {
        val cache = InfantRegCache(
            motherBenId = 1L,
            isActive = true,
            babyIndex = 1,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED
        )

        val post = cache.asPostModel()

        assertNull(post.gender)
        assertEquals(0.0, post.weight, 0.0001)
    }

    // ---------------------------------------------------------------
    // InfantRegPost.toCacheModel()
    // ---------------------------------------------------------------

    @Test
    fun `InfantRegPost toCacheModel maps core fields and sets synced`() {
        val post = InfantRegPost(
            id = 3L,
            benId = 55L,
            childBenId = 88L,
            isActive = true,
            babyName = "Baby B",
            babyIndex = 1,
            gender = "FEMALE",
            weight = 2.9,
            breastFeedingStarted = false,
            isSNCU = "Yes",
            createdBy = "c",
            updatedBy = "u"
        )

        val cache = post.toCacheModel()

        assertEquals(3L, cache.id)
        assertEquals(55L, cache.motherBenId)
        assertEquals(88L, cache.childBenId)
        assertTrue(cache.isActive)
        assertEquals("Baby B", cache.babyName)
        assertEquals(1, cache.babyIndex)
        assertEquals(2.9, cache.weight!!, 0.0001)
        assertEquals("Yes", cache.isSNCU)
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
        // gender is intentionally not mapped by toCacheModel()
        assertNull(cache.gender)
    }

    @Test
    fun `InfantRegPost copy toString and equality`() {
        val post = InfantRegPost(
            id = 3L, benId = 55L, childBenId = 88L, isActive = true, isSNCU = "Yes",
            deliveryDischargeSummary1 = "d1", deliveryDischargeSummary2 = "d2",
            deliveryDischargeSummary3 = "d3", deliveryDischargeSummary4 = "d4",
            babyName = "Baby B", babyIndex = 1, infantTerm = "Full",
            corticosteroidGiven = "Yes", gender = "FEMALE", babyCriedAtBirth = true,
            resuscitation = false, referred = "No", hadBirthDefect = "No", birthDefect = null,
            otherDefect = null, weight = 2.9, breastFeedingStarted = false,
            opv0Dose = "2024-01-01", bcgDose = "2024-01-02", hepBDose = "2024-01-03",
            vitkDose = "2024-01-04", createdDate = "2024-01-05", createdBy = "c",
            updatedDate = "2024-01-06", updatedBy = "u"
        )
        val same = post.copy()
        assertEquals(post, same)
        assertEquals(post.hashCode(), same.hashCode())
        assertNotEquals(post, post.copy(babyName = "Other"))
        assertTrue(post.toString().contains("InfantRegPost"))

        assertNotEquals(post, post.copy(id = 999L))
        assertNotEquals(post, post.copy(benId = 999L))
        assertNotEquals(post, post.copy(childBenId = 999L))
        assertNotEquals(post, post.copy(isActive = false))
        assertNotEquals(post, post.copy(isSNCU = "No"))
        assertNotEquals(post, post.copy(deliveryDischargeSummary1 = "Other"))
        assertNotEquals(post, post.copy(deliveryDischargeSummary2 = "Other"))
        assertNotEquals(post, post.copy(deliveryDischargeSummary3 = "Other"))
        assertNotEquals(post, post.copy(deliveryDischargeSummary4 = "Other"))
        assertNotEquals(post, post.copy(babyIndex = 999))
        assertNotEquals(post, post.copy(infantTerm = "Other"))
        assertNotEquals(post, post.copy(corticosteroidGiven = "Other"))
        assertNotEquals(post, post.copy(gender = "MALE"))
        assertNotEquals(post, post.copy(babyCriedAtBirth = false))
        assertNotEquals(post, post.copy(resuscitation = true))
        assertNotEquals(post, post.copy(referred = "Other"))
        assertNotEquals(post, post.copy(hadBirthDefect = "Other"))
        assertNotEquals(post, post.copy(birthDefect = "Other"))
        assertNotEquals(post, post.copy(otherDefect = "Other"))
        assertNotEquals(post, post.copy(weight = 9.9))
        assertNotEquals(post, post.copy(breastFeedingStarted = true))
        assertNotEquals(post, post.copy(opv0Dose = "Other"))
        assertNotEquals(post, post.copy(bcgDose = "Other"))
        assertNotEquals(post, post.copy(hepBDose = "Other"))
        assertNotEquals(post, post.copy(vitkDose = "Other"))
        assertNotEquals(post, post.copy(createdDate = "Other"))
        assertNotEquals(post, post.copy(createdBy = "Other"))
        assertNotEquals(post, post.copy(updatedDate = "Other"))
        assertNotEquals(post, post.copy(updatedBy = "Other"))
    }

    // ---------------------------------------------------------------
    // InfantRegDomain
    // ---------------------------------------------------------------

    @Test
    fun `InfantRegDomain omitting optional args falls back to defaults`() {
        val mother = BenBasicDomain(
            benId = 91L,
            hhId = 5L,
            reproductiveStatusId = 1,
            regDate = "01-01-2024",
            benName = "Asha",
            gender = "FEMALE",
            dob = 0L,
            relToHeadId = 1,
            mobileNo = "9999999999",
            familyHeadName = "Head",
            syncState = SyncState.UNSYNCED,
            isConsent = true,
            isSpouseAdded = false,
            isChildrenAdded = false,
            isMarried = true
        )
        val deliveryOutcome = DeliveryOutcomeCache(
            benId = 91L,
            isActive = true,
            createdBy = "asha",
            updatedBy = "asha",
            syncState = SyncState.UNSYNCED
        )

        val domain = InfantRegDomain(
            motherBen = mother,
            babyIndex = 0,
            deliveryOutcome = deliveryOutcome,
            savedIr = null
        )

        assertEquals("Baby 0 of ${mother.benFullName}", domain.babyName)
        assertNull(domain.syncState)
    }

    @Test
    fun `InfantRegDomain getCustomName returns index-based fallback when context is null`() {
        val mother = BenBasicDomain(
            benId = 91L,
            hhId = 5L,
            reproductiveStatusId = 1,
            regDate = "01-01-2024",
            benName = "Asha",
            gender = "FEMALE",
            dob = 0L,
            relToHeadId = 1,
            mobileNo = "9999999999",
            familyHeadName = "Head",
            syncState = SyncState.UNSYNCED,
            isConsent = true,
            isSpouseAdded = false,
            isChildrenAdded = false,
            isMarried = true
        )
        val deliveryOutcome = DeliveryOutcomeCache(
            benId = 91L,
            isActive = true,
            createdBy = "asha",
            updatedBy = "asha",
            syncState = SyncState.UNSYNCED
        )
        val domain = InfantRegDomain(
            motherBen = mother,
            babyIndex = 2,
            deliveryOutcome = deliveryOutcome,
            savedIr = null
        )

        assertEquals("3 Baby", domain.getCustomName(null))
    }

    @Test
    fun `InfantRegDomain getCustomName uses context resources when provided`() {
        val mother = BenBasicDomain(
            benId = 91L,
            hhId = 5L,
            reproductiveStatusId = 1,
            regDate = "01-01-2024",
            benName = "Asha",
            gender = "FEMALE",
            dob = 0L,
            relToHeadId = 1,
            mobileNo = "9999999999",
            familyHeadName = "Head",
            syncState = SyncState.UNSYNCED,
            isConsent = true,
            isSpouseAdded = false,
            isChildrenAdded = false,
            isMarried = true
        )
        val deliveryOutcome = DeliveryOutcomeCache(
            benId = 91L,
            isActive = true,
            createdBy = "asha",
            updatedBy = "asha",
            syncState = SyncState.UNSYNCED
        )
        val domain = InfantRegDomain(
            motherBen = mother,
            babyIndex = 0,
            deliveryOutcome = deliveryOutcome,
            savedIr = null
        )
        val context = mockk<Context>()
        every { context.getString(R.string.first_baby) } returns "First Baby"
        every { context.getString(R.string.baby_of, "First Baby", "Asha") } returns "First Baby of Asha"

        assertEquals("First Baby of Asha", domain.getCustomName(context))
    }

    // ---------------------------------------------------------------
    // InfantRegCache equals/hashCode/copy
    // ---------------------------------------------------------------

    @Test
    fun `InfantRegCache equals hashCode and copy cover all fields`() {
        val a = InfantRegCache(
            id = 1L,
            childBenId = 2L,
            motherBenId = 3L,
            isActive = true,
            babyName = "Baby",
            babyIndex = 0,
            infantTerm = "Term",
            corticosteroidGiven = "Yes",
            gender = Gender.MALE,
            babyCriedAtBirth = true,
            resuscitation = false,
            referred = "No",
            hadBirthDefect = "No",
            birthDefect = "None",
            isSNCU = "No",
            deliveryDischargeSummary1 = "s1",
            deliveryDischargeSummary2 = "s2",
            deliveryDischargeSummary3 = "s3",
            deliveryDischargeSummary4 = "s4",
            otherDefect = "None",
            weight = 3.2,
            breastFeedingStarted = true,
            opv0Dose = 0L,
            bcgDose = 0L,
            hepBDose = 0L,
            vitkDose = 0L,
            processed = "N",
            createdBy = "asha",
            createdDate = 0L,
            updatedBy = "asha",
            updatedDate = 0L,
            syncState = SyncState.UNSYNCED
        )

        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertTrue(a.toString().contains("InfantRegCache"))

        assertNotEquals(a, a.copy(id = 999L))
        assertNotEquals(a, a.copy(childBenId = 999L))
        assertNotEquals(a, a.copy(motherBenId = 999L))
        assertNotEquals(a, a.copy(isActive = false))
        assertNotEquals(a, a.copy(babyName = "Other"))
        assertNotEquals(a, a.copy(babyIndex = 999))
        assertNotEquals(a, a.copy(infantTerm = "Other"))
        assertNotEquals(a, a.copy(corticosteroidGiven = "Other"))
        assertNotEquals(a, a.copy(gender = Gender.FEMALE))
        assertNotEquals(a, a.copy(babyCriedAtBirth = false))
        assertNotEquals(a, a.copy(resuscitation = true))
        assertNotEquals(a, a.copy(referred = "Other"))
        assertNotEquals(a, a.copy(hadBirthDefect = "Other"))
        assertNotEquals(a, a.copy(birthDefect = "Other"))
        assertNotEquals(a, a.copy(isSNCU = "Other"))
        assertNotEquals(a, a.copy(deliveryDischargeSummary1 = "Other"))
        assertNotEquals(a, a.copy(deliveryDischargeSummary2 = "Other"))
        assertNotEquals(a, a.copy(deliveryDischargeSummary3 = "Other"))
        assertNotEquals(a, a.copy(deliveryDischargeSummary4 = "Other"))
        assertNotEquals(a, a.copy(otherDefect = "Other"))
        assertNotEquals(a, a.copy(weight = 9.9))
        assertNotEquals(a, a.copy(breastFeedingStarted = false))
        assertNotEquals(a, a.copy(opv0Dose = 1L))
        assertNotEquals(a, a.copy(bcgDose = 1L))
        assertNotEquals(a, a.copy(hepBDose = 1L))
        assertNotEquals(a, a.copy(vitkDose = 1L))
        assertNotEquals(a, a.copy(processed = "Y"))
        assertNotEquals(a, a.copy(createdBy = "Other"))
        assertNotEquals(a, a.copy(createdDate = 1L))
        assertNotEquals(a, a.copy(updatedBy = "Other"))
        assertNotEquals(a, a.copy(updatedDate = 1L))
        assertNotEquals(a, a.copy(syncState = SyncState.SYNCED))
    }

    @Test
    fun `InfantRegCache getters read back constructed values and setters mutate var fields`() {
        val a = InfantRegCache(
            id = 1L,
            childBenId = 2L,
            motherBenId = 3L,
            isActive = true,
            babyName = "Baby",
            babyIndex = 0,
            infantTerm = "Term",
            corticosteroidGiven = "Yes",
            gender = Gender.MALE,
            babyCriedAtBirth = true,
            resuscitation = false,
            referred = "No",
            hadBirthDefect = "No",
            birthDefect = "None",
            isSNCU = "No",
            deliveryDischargeSummary1 = "s1",
            deliveryDischargeSummary2 = "s2",
            deliveryDischargeSummary3 = "s3",
            deliveryDischargeSummary4 = "s4",
            otherDefect = "None",
            weight = 3.2,
            breastFeedingStarted = true,
            opv0Dose = 10L,
            bcgDose = 20L,
            hepBDose = 30L,
            vitkDose = 40L,
            processed = "N",
            createdBy = "asha",
            createdDate = 50L,
            updatedBy = "asha2",
            updatedDate = 60L,
            syncState = SyncState.UNSYNCED
        )

        assertEquals(1L, a.id)
        assertEquals(3L, a.motherBenId)
        assertEquals(50L, a.createdDate)

        a.childBenId = 999L
        assertEquals(999L, a.childBenId)
        a.isActive = false
        assertFalse(a.isActive)
        a.babyName = "Other"
        assertEquals("Other", a.babyName)
        a.babyIndex = 5
        assertEquals(5, a.babyIndex)
        a.infantTerm = "Other"
        assertEquals("Other", a.infantTerm)
        a.corticosteroidGiven = "No"
        assertEquals("No", a.corticosteroidGiven)
        a.gender = Gender.FEMALE
        assertEquals(Gender.FEMALE, a.gender)
        a.babyCriedAtBirth = false
        assertEquals(false, a.babyCriedAtBirth)
        a.resuscitation = true
        assertEquals(true, a.resuscitation)
        a.referred = "Yes"
        assertEquals("Yes", a.referred)
        a.hadBirthDefect = "Yes"
        assertEquals("Yes", a.hadBirthDefect)
        a.birthDefect = "Other"
        assertEquals("Other", a.birthDefect)
        a.isSNCU = "Yes"
        assertEquals("Yes", a.isSNCU)
        a.deliveryDischargeSummary1 = "n1"
        assertEquals("n1", a.deliveryDischargeSummary1)
        a.deliveryDischargeSummary2 = "n2"
        assertEquals("n2", a.deliveryDischargeSummary2)
        a.deliveryDischargeSummary3 = "n3"
        assertEquals("n3", a.deliveryDischargeSummary3)
        a.deliveryDischargeSummary4 = "n4"
        assertEquals("n4", a.deliveryDischargeSummary4)
        a.otherDefect = "Other2"
        assertEquals("Other2", a.otherDefect)
        a.weight = 4.5
        assertEquals(4.5, a.weight)
        a.breastFeedingStarted = false
        assertEquals(false, a.breastFeedingStarted)
        a.opv0Dose = 100L
        assertEquals(100L, a.opv0Dose)
        a.bcgDose = 200L
        assertEquals(200L, a.bcgDose)
        a.hepBDose = 300L
        assertEquals(300L, a.hepBDose)
        a.vitkDose = 400L
        assertEquals(400L, a.vitkDose)
        a.processed = "Y"
        assertEquals("Y", a.processed)
        a.createdBy = "new-creator"
        assertEquals("new-creator", a.createdBy)
        a.updatedBy = "new-updater"
        assertEquals("new-updater", a.updatedBy)
        a.updatedDate = 700L
        assertEquals(700L, a.updatedDate)
        a.syncState = SyncState.SYNCED
        assertEquals(SyncState.SYNCED, a.syncState)
    }
}
