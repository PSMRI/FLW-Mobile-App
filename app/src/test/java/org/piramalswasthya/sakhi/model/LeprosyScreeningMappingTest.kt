package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class LeprosyScreeningMappingTest {

    private fun screening() = LeprosyScreeningCache(
        benId = 11L,
        houseHoldDetailsId = 500L,
        createdBy = "creator",
        modifiedBy = "modifier"
    )

    // =====================================================
    // LeprosyScreeningCache.toDTO()
    // =====================================================

    @Test fun `toDTO maps benId and household`() {
        val dto = screening().toDTO()
        assertEquals(11L, dto.benId)
        assertEquals(500L, dto.houseHoldDetailsId)
    }

    @Test fun `toDTO maps createdBy and modifiedBy`() {
        val dto = screening().toDTO()
        assertEquals("creator", dto.createdBy)
        assertEquals("modifier", dto.modifiedBy)
    }

    @Test fun `toDTO default currentVisitNumber and isConfirmed`() {
        val dto = screening().toDTO()
        assertEquals(1, dto.currentVisitNumber)
        assertEquals(false, dto.isConfirmed)
    }

    @Test fun `toDTO default leprosyState`() {
        assertEquals("Screening", screening().toDTO().leprosyState)
    }

    @Test fun `toDTO formats dates as datetime strings`() {
        val dto = screening().toDTO()
        assertNotNull(dto.homeVisitDate)
        assertTrue(dto.homeVisitDate.contains("T"))
        assertTrue(dto.treatmentStartDate.contains("T"))
    }

    @Test fun `toDTO passes through symptom fields`() {
        val dto = screening().copy(
            leprosyStatus = "Positive",
            typeOfLeprosy = "PB",
            recurrentUlceration = "Yes",
            recurrentUlcerationId = 2
        ).toDTO()
        assertEquals("Positive", dto.leprosyStatus)
        assertEquals("PB", dto.typeOfLeprosy)
        assertEquals("Yes", dto.recurrentUlceration)
        assertEquals(2, dto.recurrentUlcerationId)
    }

    // =====================================================
    // LeprosyFollowUpCache.toDTO()
    // =====================================================

    private fun followUp() = LeprosyFollowUpCache(
        benId = 22L,
        visitNumber = 3,
        createdBy = "creator",
        modifiedBy = "modifier"
    )

    @Test fun `followUp toDTO maps benId and visitNumber`() {
        val dto = followUp().toDTO()
        assertEquals(22L, dto.benId)
        assertEquals(3, dto.visitNumber)
    }

    @Test fun `followUp toDTO maps created and modified by`() {
        val dto = followUp().toDTO()
        assertEquals("creator", dto.createdBy)
        assertEquals("modifier", dto.modifiedBy)
    }

    @Test fun `followUp toDTO formats followUpDate as datetime`() {
        val dto = followUp().toDTO()
        assertTrue(dto.followUpDate.contains("T"))
    }

    @Test fun `followUp toDTO passes through treatment status`() {
        val dto = followUp().copy(treatmentStatus = "Ongoing", leprosyStatus = "Confirmed").toDTO()
        assertEquals("Ongoing", dto.treatmentStatus)
        assertEquals("Confirmed", dto.leprosyStatus)
    }

    @Test fun `followUp default syncState is UNSYNCED`() {
        assertEquals(SyncState.UNSYNCED, followUp().syncState)
    }
}
