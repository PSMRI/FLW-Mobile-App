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

    // ---------------------------------------------------------------
    // Branch-variant coverage: complication/death fields, discharge
    // date null-safety, createdDate/updatedDate fallback, and
    // generated data-class methods.
    // ---------------------------------------------------------------

    @Test
    fun `DeliveryOutcomeCache asPostModel maps complication and death fields`() {
        val cache = DeliveryOutcomeCache(
            id = 6L,
            benId = 44L,
            isActive = true,
            dateOfDelivery = 1_600_000_000_000L,
            timeOfDelivery = "11:00",
            placeOfDelivery = "Hospital",
            typeOfDelivery = "Normal",
            hadComplications = true,
            complication = "Hemorrhage",
            causeOfDeath = "PPH",
            otherCauseOfDeath = "Other cause",
            otherComplication = "Severe bleeding",
            deliveryOutcome = 1,
            liveBirth = 1,
            stillBirth = 0,
            dateOfDischarge = 1_601_000_000_000L,
            timeOfDischarge = "15:00",
            isJSYBenificiary = true,
            isDeath = true,
            isDeathValue = "Maternal",
            dateOfDeath = "2024-01-05",
            placeOfDeath = "Hospital",
            placeOfDeathId = 2,
            otherPlaceOfDeath = "N/A",
            mcp1File = "mcp1.jpg",
            mcp2File = "mcp2.jpg",
            jsyFile = "jsy.jpg",
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED
        )

        val post = cache.asPostModel()

        assertNotNull(post.dateOfDelivery)
        assertNotNull(post.dateOfDischarge)
        assertTrue(post.hadComplications!!)
        assertEquals("Hemorrhage", post.complication)
        assertEquals("PPH", post.causeOfDeath)
        assertEquals("Other cause", post.otherCauseOfDeath)
        assertEquals("Severe bleeding", post.otherComplication)
        assertEquals("15:00", post.timeOfDischarge)
        assertTrue(post.isDeath!!)
        assertEquals("Maternal", post.isDeathValue)
        assertEquals("2024-01-05", post.dateOfDeath)
        assertEquals("Hospital", post.placeOfDeath)
        assertEquals(2, post.placeOfDeathId)
        assertEquals("N/A", post.otherPlaceOfDeath)
        assertEquals("mcp1.jpg", post.mcp1File)
        assertEquals("mcp2.jpg", post.mcp2File)
        assertEquals("jsy.jpg", post.jsyFile)
    }

    @Test
    fun `DeliveryOutcomeCache asPostModel with null death and discharge fields`() {
        val cache = DeliveryOutcomeCache(
            benId = 45L,
            isActive = true,
            dateOfDelivery = null,
            dateOfDischarge = null,
            hadComplications = null,
            complication = null,
            causeOfDeath = null,
            isDeath = null,
            placeOfDeathId = null,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.UNSYNCED
        )

        val post = cache.asPostModel()

        assertNull(post.dateOfDelivery)
        assertNull(post.dateOfDischarge)
        assertNull(post.hadComplications)
        assertNull(post.complication)
        assertNull(post.causeOfDeath)
        assertNull(post.isDeath)
        assertNull(post.placeOfDeathId)
    }

    @Test
    fun `DeliveryOutcomePost toDeliveryCache defaults createdDate and updatedDate when null`() {
        val before = System.currentTimeMillis()
        val post = DeliveryOutcomePost(
            benId = 50L,
            isActive = true,
            dateOfDelivery = null,
            dateOfDischarge = null,
            createdDate = null,
            updatedDate = null,
            createdBy = "c",
            updatedBy = "u"
        )

        val cache = post.toDeliveryCache()
        val after = System.currentTimeMillis()

        assertNull(cache.dateOfDelivery)
        assertNull(cache.dateOfDischarge)
        assertTrue(cache.createdDate in before..after)
        assertTrue(cache.updatedDate in before..after)
    }

    @Test
    fun `DeliveryOutcomePost toDeliveryCache maps complication death and file fields`() {
        val post = DeliveryOutcomePost(
            id = 8L,
            benId = 51L,
            isActive = true,
            dateOfDelivery = "2024-04-10",
            dateOfDischarge = "2024-04-12",
            hadComplications = true,
            complication = "Obstructed labour",
            causeOfDeath = "Sepsis",
            otherCauseOfDeath = "Unknown",
            otherComplication = "Fever",
            createdDate = "2024-04-01",
            createdBy = "c",
            updatedDate = "2024-04-02",
            updatedBy = "u",
            isDeath = true,
            isDeathValue = "Neonatal",
            dateOfDeath = "2024-04-11",
            placeOfDeath = "Home",
            placeOfDeathId = 1,
            otherPlaceOfDeath = "-",
            mcp1File = "a.jpg",
            mcp2File = "b.jpg",
            jsyFile = "c.jpg"
        )

        val cache = post.toDeliveryCache()

        assertNotNull(cache.dateOfDelivery)
        assertNotNull(cache.dateOfDischarge)
        assertTrue(cache.hadComplications!!)
        assertEquals("Obstructed labour", cache.complication)
        assertEquals("Sepsis", cache.causeOfDeath)
        assertEquals("Unknown", cache.otherCauseOfDeath)
        assertEquals("Fever", cache.otherComplication)
        assertTrue(cache.isDeath!!)
        assertEquals("Neonatal", cache.isDeathValue)
        assertEquals("2024-04-11", cache.dateOfDeath)
        assertEquals("Home", cache.placeOfDeath)
        assertEquals(1, cache.placeOfDeathId)
        assertEquals("-", cache.otherPlaceOfDeath)
        assertEquals("a.jpg", cache.mcp1File)
        assertEquals("b.jpg", cache.mcp2File)
        assertEquals("c.jpg", cache.jsyFile)
    }

    @Test
    fun `DeliveryOutcomeCache equals hashCode copy and toString cover generated methods`() {
        val base = DeliveryOutcomeCache(
            id = 1L,
            benId = 1L,
            isActive = true,
            dateOfDelivery = 1_600_000_000_000L,
            timeOfDelivery = "10:00",
            placeOfDelivery = "Hospital",
            typeOfDelivery = "Normal",
            hadComplications = true,
            complication = "X",
            causeOfDeath = "Y",
            otherCauseOfDeath = "Z",
            otherComplication = "W",
            deliveryOutcome = 1,
            liveBirth = 1,
            stillBirth = 0,
            dateOfDischarge = 1_601_000_000_000L,
            timeOfDischarge = "12:00",
            isJSYBenificiary = true,
            isDeath = false,
            isDeathValue = "None",
            dateOfDeath = null,
            placeOfDeath = null,
            placeOfDeathId = 0,
            otherPlaceOfDeath = null,
            mcp1File = null,
            mcp2File = null,
            jsyFile = null,
            createdBy = "c",
            updatedBy = "u",
            syncState = SyncState.SYNCED
        )
        val sameValues = base.copy()
        val different = base.copy(liveBirth = 2)

        assertEquals(base, sameValues)
        assertEquals(base.hashCode(), sameValues.hashCode())
        assertTrue(base.toString().contains("DeliveryOutcomeCache"))
        assertFalse(base == different)
        assertFalse(base.equals(null))
        assertFalse(base.equals("not a cache"))
        assertEquals(base, base)
    }
}
