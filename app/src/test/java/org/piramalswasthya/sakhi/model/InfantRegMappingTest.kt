package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Tests for the pure mapper functions in InfantReg.kt:
 *  - InfantRegCache.asPostModel()
 *  - InfantRegPost.toCacheModel()
 *
 * Skipped: BenWithDoAndIrCache.asBasicDomainModel() and InfantRegWithBen
 * mappers (require BenBasicCache + DeliveryOutcome graphs).
 */
class InfantRegMappingTest {

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
}
