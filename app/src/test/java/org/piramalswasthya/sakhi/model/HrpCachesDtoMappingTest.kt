package org.piramalswasthya.sakhi.model

import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class HrpCachesDtoMappingTest {

    private fun resources(): Resources = mockk(relaxed = true) {
        every { getString(any()) } returns "s"
        every { getStringArray(any()) } returns Array(20) { i -> "o$i" }
    }

    // =====================================================
    // HRPNonPregnantAssessCache.toDTO() / toHighRiskAssessDTO()
    // =====================================================

    private fun nonPregAssess() = HRPNonPregnantAssessCache(benId = 11L)

    @Test fun `nonPregAssess toDTO maps benId and id reset`() {
        val dto = nonPregAssess().toDTO()
        assertEquals(11L, dto.benId)
        assertEquals(0, dto.id)
    }

    @Test fun `nonPregAssess toDTO passes through risk fields`() {
        val dto = nonPregAssess().copy(
            noOfDeliveries = "Yes",
            timeLessThan18m = "No",
            heightShort = "Yes",
            age = "No",
            isHighRisk = true
        ).toDTO()
        assertEquals("Yes", dto.noOfDeliveries)
        assertEquals("No", dto.timeLessThan18m)
        assertEquals("Yes", dto.heightShort)
        assertEquals(true, dto.isHighRisk)
    }

    @Test fun `nonPregAssess toDTO formats visitDate with T`() {
        val dto = nonPregAssess().toDTO()
        assertNotNull(dto.visitDate)
        assertTrue(dto.visitDate!!.contains("T"))
    }

    @Test fun `nonPregAssess toHighRiskAssessDTO maps benId and id`() {
        val dto = nonPregAssess().copy(id = 7).toHighRiskAssessDTO()
        assertEquals(11L, dto.benId)
        assertEquals(7, dto.id)
    }

    @Test fun `nonPregAssess toHighRiskAssessDTO passes through fields`() {
        val dto = nonPregAssess().copy(noOfDeliveries = "Yes", age = "No").toHighRiskAssessDTO()
        assertEquals("Yes", dto.noOfDeliveries)
        assertEquals("No", dto.age)
    }

    // =====================================================
    // HRPNonPregnantTrackCache.toDTO() / asDomainModel(resources)
    // =====================================================

    private fun nonPregTrack() = HRPNonPregnantTrackCache(benId = 22L)

    @Test fun `nonPregTrack toDTO maps benId and id reset`() {
        val dto = nonPregTrack().toDTO()
        assertEquals(22L, dto.benId)
        assertEquals(0, dto.id)
    }

    @Test fun `nonPregTrack toDTO passes through clinical fields`() {
        val dto = nonPregTrack().copy(
            anemia = "Yes",
            hypertension = "No",
            systolic = 120,
            diastolic = 80,
            isPregnant = "No"
        ).toDTO()
        assertEquals("Yes", dto.anemia)
        assertEquals("No", dto.hypertension)
        assertEquals(120, dto.systolic)
        assertEquals(80, dto.diastolic)
        assertEquals("No", dto.isPregnant)
    }

    @Test fun `nonPregTrack toDTO formats visitDate with T when set`() {
        val dto = nonPregTrack().copy(visitDate = 1_600_000_000_000L).toDTO()
        assertTrue(dto.visitDate!!.contains("T"))
    }

    @Test fun `nonPregTrack asDomainModel maps id and syncState`() {
        val domain = nonPregTrack().copy(id = 5, visitDate = 1_600_000_000_000L, syncState = SyncState.SYNCED)
            .asDomainModel(resources())
        assertEquals(5, domain.id)
        assertEquals(SyncState.SYNCED, domain.syncState)
        assertNotNull(domain.dateOfVisit)
    }

    // =====================================================
    // HRPPregnantAssessCache.toDTO() / toHighRiskAssessDTO()
    // =====================================================

    private fun pregAssess() = HRPPregnantAssessCache(benId = 33L)

    @Test fun `pregAssess toDTO maps benId and id reset`() {
        val dto = pregAssess().toDTO()
        assertEquals(33L, dto.benId)
        assertEquals(0, dto.id)
    }

    @Test fun `pregAssess toDTO passes through fields`() {
        val dto = pregAssess().copy(
            rhNegative = "Yes",
            badObstetric = "No",
            multiplePregnancy = "Yes",
            isHighRisk = true
        ).toDTO()
        assertEquals("Yes", dto.rhNegative)
        assertEquals("No", dto.badObstetric)
        assertEquals("Yes", dto.multiplePregnancy)
        assertEquals(true, dto.isHighRisk)
    }

    @Test fun `pregAssess toDTO visitDate has T but zero lmp is null`() {
        val dto = pregAssess().toDTO()
        assertTrue(dto.visitDate!!.contains("T"))
    }

    @Test fun `pregAssess toDTO formats lmp and edd with T when set`() {
        val dto = pregAssess().copy(lmpDate = 1_600_000_000_000L, edd = 1_620_000_000_000L).toDTO()
        assertTrue(dto.lmpDate!!.contains("T"))
        assertTrue(dto.edd!!.contains("T"))
    }

    @Test fun `pregAssess toHighRiskAssessDTO maps benId and fields`() {
        val dto = pregAssess().copy(id = 9, noOfDeliveries = "Yes").toHighRiskAssessDTO()
        assertEquals(33L, dto.benId)
        assertEquals(9, dto.id)
        assertEquals("Yes", dto.noOfDeliveries)
    }

    // =====================================================
    // HighRiskAssessDTO.toPregnantAssess() / toNonPregnantAssess()
    // =====================================================

    private fun highRisk() = HighRiskAssessDTO(
        benId = 44L,
        noOfDeliveries = "Yes",
        timeLessThan18m = "No",
        heightShort = "No",
        age = "No"
    )

    @Test fun `highRisk toPregnantAssess maps benId and highRisk true`() {
        val cache = highRisk().toPregnantAssess()
        assertEquals(44L, cache.benId)
        assertEquals("Yes", cache.noOfDeliveries)
        assertTrue(cache.isHighRisk)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test fun `highRisk toPregnantAssess highRisk false when all no`() {
        val cache = HighRiskAssessDTO(
            benId = 44L, noOfDeliveries = "No", timeLessThan18m = "No",
            heightShort = "No", age = "No"
        ).toPregnantAssess()
        assertEquals(false, cache.isHighRisk)
    }

    @Test fun `highRisk toNonPregnantAssess maps benId and highRisk true`() {
        val cache = highRisk().toNonPregnantAssess()
        assertEquals(44L, cache.benId)
        assertEquals("No", cache.timeLessThan18m)
        assertTrue(cache.isHighRisk)
        assertEquals(SyncState.SYNCED, cache.syncState)
    }

    @Test fun `highRisk toNonPregnantAssess highRisk false when all no`() {
        val cache = HighRiskAssessDTO(
            benId = 44L, noOfDeliveries = "No", timeLessThan18m = "No",
            heightShort = "No", age = "No"
        ).toNonPregnantAssess()
        assertEquals(false, cache.isHighRisk)
    }

    // =====================================================
    // HRPPregnantTrackCache.toDTO() / asDomainModel(resources)
    // =====================================================

    private fun pregTrack() = HRPPregnantTrackCache(benId = 55L)

    @Test fun `pregTrack toDTO maps benId and id reset`() {
        val dto = pregTrack().toDTO()
        assertEquals(55L, dto.benId)
        assertEquals(0, dto.id)
    }

    @Test fun `pregTrack toDTO passes through clinical fields`() {
        val dto = pregTrack().copy(
            rdPmsa = "Yes",
            severeAnemia = "No",
            systolic = 130,
            diastolic = 90,
            visit = "ANC1"
        ).toDTO()
        assertEquals("Yes", dto.rdPmsa)
        assertEquals("No", dto.severeAnemia)
        assertEquals(130, dto.systolic)
        assertEquals(90, dto.diastolic)
        assertEquals("ANC1", dto.visit)
    }

    @Test fun `pregTrack toDTO formats visitDate with T when set`() {
        val dto = pregTrack().copy(visitDate = 1_600_000_000_000L).toDTO()
        assertTrue(dto.visitDate!!.contains("T"))
    }

    @Test fun `pregTrack asDomainModel maps id and syncState`() {
        val domain = pregTrack().copy(
            id = 3, visit = "ANC1", visitDate = 1_600_000_000_000L, syncState = SyncState.SYNCED
        ).asDomainModel(resources())
        assertEquals(3, domain.id)
        assertEquals(SyncState.SYNCED, domain.syncState)
        assertNotNull(domain.dateOfVisit)
    }
}
