package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Tests for the pure mapper functions in DeliveryOutcome.kt:
 *  - DeliveryOutcomeCache.asPostModel()
 *  - DeliveryOutcomePost.toDeliveryCache()
 */
class DeliveryOutcomeMappingTest {

    // ---------------------------------------------------------------
    // DeliveryOutcomeCache.asPostModel()
    // ---------------------------------------------------------------

    @Test
    fun `DeliveryOutcomeCache asPostModel maps core fields`() {
        val cache = DeliveryOutcomeCache(
            id = 2L,
            benId = 66L,
            isActive = true,
            dateOfDelivery = 1_600_000_000_000L,
            timeOfDelivery = "10:30",
            placeOfDelivery = "Hospital",
            typeOfDelivery = "Normal",
            deliveryOutcome = 1,
            liveBirth = 1,
            stillBirth = 0,
            isJSYBenificiary = true,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED
        )

        val post = cache.asPostModel()

        assertEquals(2L, post.id)
        assertEquals(66L, post.benId)
        assertTrue(post.isActive)
        assertEquals("10:30", post.timeOfDelivery)
        assertEquals("Hospital", post.placeOfDelivery)
        assertEquals("Normal", post.typeOfDelivery)
        assertEquals(1, post.deliveryOutcome)
        assertEquals(1, post.liveBirth)
        assertEquals(0, post.stillBirth)
        assertTrue(post.isJSYBenificiary!!)
        assertEquals("c", post.createdBy)
        assertNull(post.dateOfDischarge)
    }

    // ---------------------------------------------------------------
    // DeliveryOutcomePost.toDeliveryCache()
    // ---------------------------------------------------------------

    @Test
    fun `DeliveryOutcomePost toDeliveryCache maps core fields and sets synced`() {
        val post = DeliveryOutcomePost(
            id = 3L,
            benId = 77L,
            isActive = true,
            dateOfDelivery = "2024-02-01",
            timeOfDelivery = "09:00",
            placeOfDelivery = "Home",
            typeOfDelivery = "Assisted",
            deliveryOutcome = 2,
            liveBirth = 2,
            stillBirth = 0,
            isJSYBenificiary = false,
            createdBy = "c",
            updatedBy = "u"
        )

        val cache = post.toDeliveryCache()

        assertEquals(3L, cache.id)
        assertEquals(77L, cache.benId)
        assertTrue(cache.isActive)
        assertEquals("09:00", cache.timeOfDelivery)
        assertEquals("Home", cache.placeOfDelivery)
        assertEquals("Assisted", cache.typeOfDelivery)
        assertEquals(2, cache.deliveryOutcome)
        assertEquals(2, cache.liveBirth)
        assertEquals(0, cache.stillBirth)
        assertEquals("P", cache.processed)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    // ---------------------------------------------------------------
    // Branch-variant coverage: inactive record, non-JSY beneficiary,
    // still-birth outcome variants.
    // ---------------------------------------------------------------

    @Test
    fun `asPostModel inactive non-jsy stillbirth`() {
        val cache = DeliveryOutcomeCache(
            id = 4L,
            benId = 88L,
            isActive = false,
            dateOfDelivery = 1_600_000_000_000L,
            timeOfDelivery = "23:15",
            placeOfDelivery = "Home",
            typeOfDelivery = "Assisted",
            deliveryOutcome = 2,
            liveBirth = 0,
            stillBirth = 2,
            isJSYBenificiary = false,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED
        )
        val post = cache.asPostModel()
        assertEquals(88L, post.benId)
        assertFalse(post.isActive)
        assertEquals(2, post.deliveryOutcome)
        assertEquals(0, post.liveBirth)
        assertEquals(2, post.stillBirth)
        assertFalse(post.isJSYBenificiary!!)
    }

    @Test
    fun `toDeliveryCache jsy-true inactive`() {
        val post = DeliveryOutcomePost(
            id = 5L,
            benId = 99L,
            isActive = false,
            dateOfDelivery = "2024-03-05",
            timeOfDelivery = "06:45",
            placeOfDelivery = "Hospital",
            typeOfDelivery = "Normal",
            deliveryOutcome = 1,
            liveBirth = 1,
            stillBirth = 0,
            isJSYBenificiary = true,
            createdBy = "c",
            updatedBy = "u"
        )
        val cache = post.toDeliveryCache()
        assertEquals(99L, cache.benId)
        assertEquals("Hospital", cache.placeOfDelivery)
        assertEquals(1, cache.liveBirth)
        assertEquals(SyncState.SYNCED, cache.syncState)
        assertNotNull(cache)
    }
}
