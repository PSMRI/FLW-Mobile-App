package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [TBSuspectedCache.toDTO].
 */
class TBSuspectedCacheTest {

    private fun cache() = TBSuspectedCache(
        id = 4,
        benId = 200L,
        visitDate = 1_700_000_000_000L,
        visitLabel = "Visit 1",
        typeOfTBCase = "New",
        reasonForSuspicion = "Cough",
        hasSymptoms = true,
        isSputumCollected = true,
        sputumSubmittedAt = "PHC",
        nikshayId = "NIK-1",
        sputumTestResult = "Positive",
        isChestXRayDone = true,
        chestXRayResult = "Abnormal",
        referralFacility = "DH",
        isTBConfirmed = true,
        isDRTBConfirmed = false,
        isConfirmed = true,
        referred = true,
        followUps = "[]"
    )

    @Test
    fun `toDTO maps benId and forces id to zero`() {
        val dto = cache().toDTO()
        assertEquals(200L, dto.benId)
        assertEquals(0, dto.id)
    }

    @Test
    fun `toDTO carries flags`() {
        val dto = cache().toDTO()
        assertEquals(true, dto.hasSymptoms)
        assertEquals(true, dto.isConfirmed)
        assertEquals(true, dto.isTBConfirmed)
        assertEquals(false, dto.isDRTBConfirmed)
    }

    @Test
    fun `toDTO carries sputum and nikshay fields`() {
        val dto = cache().toDTO()
        assertEquals(true, dto.isSputumCollected)
        assertEquals("PHC", dto.sputumSubmittedAt)
        assertEquals("NIK-1", dto.nikshayId)
        assertEquals("Positive", dto.sputumTestResult)
    }

    @Test
    fun `toDTO carries case classification fields`() {
        val dto = cache().toDTO()
        assertEquals("Visit 1", dto.visitLabel)
        assertEquals("New", dto.typeOfTBCase)
        assertEquals("Cough", dto.reasonForSuspicion)
    }

    @Test
    fun `toDTO formats non-zero visitDate as string`() {
        val dto = cache().toDTO()
        assertTrue(dto.visitDate != null && dto.visitDate!!.contains("T"))
    }
}
