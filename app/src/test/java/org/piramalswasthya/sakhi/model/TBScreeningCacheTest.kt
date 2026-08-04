package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [TBScreeningCache.toDTO].
 */
class TBScreeningCacheTest {

    private fun cache(visitDate: Long = 1_700_000_000_000L) = TBScreeningCache(
        id = 9,
        benId = 100L,
        visitDate = visitDate,
        coughMoreThan2Weeks = true,
        bloodInSputum = false,
        feverMoreThan2Weeks = true,
        historyOfTb = false,
        sympotomatic = "yes",
        asymptomatic = "no",
        recommandateTest = "sputum"
    )

    @Test
    fun `toDTO maps benId and forces id to zero`() {
        val dto = cache().toDTO()
        assertEquals(100L, dto.benId)
        assertEquals(0, dto.id)
    }

    @Test
    fun `toDTO carries symptom booleans`() {
        val dto = cache().toDTO()
        assertEquals(true, dto.coughMoreThan2Weeks)
        assertEquals(false, dto.bloodInSputum)
        assertEquals(true, dto.feverMoreThan2Weeks)
    }

    @Test
    fun `toDTO carries recommendation strings`() {
        val dto = cache().toDTO()
        assertEquals("yes", dto.sympotomatic)
        assertEquals("no", dto.asymptomatic)
        assertEquals("sputum", dto.recommandateTest)
    }

    @Test
    fun `toDTO formats non-zero visitDate as string`() {
        val dto = cache().toDTO()
        assertTrue(dto.visitDate != null && dto.visitDate!!.contains("T"))
    }

    @Test
    fun `toDTO formats zero visitDate to epoch date string`() {
        val dto = cache(visitDate = 0L).toDTO()
        assertNotNull(dto.visitDate)
    }
}
