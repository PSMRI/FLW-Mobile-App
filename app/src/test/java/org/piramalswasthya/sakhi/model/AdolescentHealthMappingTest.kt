package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers [AdolescentHealthCache.toDTO] - a pure mapper that formats the
 * millis timestamps via getDateTimeStringFromLong (SimpleDateFormat, JVM-safe).
 */
class AdolescentHealthMappingTest {

    @Test
    fun `toDTO maps benId and constant id`() {
        val dto = AdolescentHealthCache(benId = 5L).toDTO()

        assertEquals(0, dto.id)
        assertEquals(5L, dto.benId)
    }

    @Test
    fun `toDTO formats visitdate and follow-up date`() {
        val dto = AdolescentHealthCache(
            benId = 5L,
            visitDate = 1_700_000_000_000L,
            followUpDate = 1_700_000_000_000L
        ).toDTO()

        // "yyyy-MM-ddTHH:mm:ss.000Z" style string, never null/blank
        assertTrue(dto.visitDate.isNotBlank())
        assertTrue(dto.followUpDate!!.isNotBlank())
    }

    @Test
    fun `toDTO carries over content fields`() {
        val dto = AdolescentHealthCache(
            benId = 9L,
            healthStatus = "Good",
            ifaTabletDistributed = true,
            quantityOfIfaTablets = 10,
            menstrualHygieneAwarenessGiven = false,
            sanitaryNapkinDistributed = true,
            noOfPacketsDistributed = 3,
            place = "PHC",
            counselingProvided = true,
            counselingType = "Nutrition",
            referredToHealthFacility = "CHC",
            referralStatus = "Done"
        ).toDTO()

        assertEquals("Good", dto.healthStatus)
        assertEquals(true, dto.ifaTabletDistributed)
        assertEquals(10, dto.quantityOfIfaTablets)
        assertFalse(dto.menstrualHygieneAwarenessGiven!!)
        assertEquals(true, dto.sanitaryNapkinDistributed)
        assertEquals(3, dto.noOfPacketsDistributed)
        assertEquals("PHC", dto.place)
        assertEquals(true, dto.counselingProvided)
        assertEquals("Nutrition", dto.counselingType)
        assertEquals("CHC", dto.referredToHealthFacility)
        assertEquals("Done", dto.referralStatus)
    }

    @Test
    fun `toDTO passes through null optional fields`() {
        val dto = AdolescentHealthCache(benId = 1L).toDTO()

        assertNull(dto.healthStatus)
        assertNull(dto.ifaTabletDistributed)
        assertNull(dto.counselingType)
    }
}
