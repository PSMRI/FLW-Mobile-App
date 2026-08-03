package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class DiseaseScreeningDtoMappingTest {

    // =====================================================
    // AESScreeningCache.toDTO()
    // =====================================================

    private fun aes() = AESScreeningCache(
        benId = 11L,
        houseHoldDetailsId = 500L
    )

    @Test fun `aes toDTO maps benId and household`() {
        val dto = aes().toDTO()
        assertEquals(11L, dto.benId)
        assertEquals(500L, dto.houseHoldDetailsId)
    }

    @Test fun `aes toDTO formats dates as datetime`() {
        val dto = aes().toDTO()
        assertTrue(dto.visitDate.contains("T"))
        assertTrue(dto.createdDate.contains("T"))
    }

    @Test fun `aes toDTO passes through status fields`() {
        val dto = aes().copy(
            aesJeCaseStatus = "Confirmed",
            beneficiaryStatus = "Alive",
            beneficiaryStatusId = 3,
            referredTo = 7,
            diseaseTypeID = 4
        ).toDTO()
        assertEquals("Confirmed", dto.aesJeCaseStatus)
        assertEquals("Alive", dto.beneficiaryStatus)
        assertEquals(3, dto.beneficiaryStatusId)
        assertEquals(7, dto.referredTo)
        assertEquals(4, dto.diseaseTypeID)
    }

    @Test fun `aes toDTO sets syncState SYNCED`() {
        assertEquals(SyncState.SYNCED, aes().toDTO().syncState)
    }

    // =====================================================
    // FilariaScreeningCache.toDTO()
    // =====================================================

    private fun filaria() = FilariaScreeningCache(
        benId = 12L,
        houseHoldDetailsId = 501L
    )

    @Test fun `filaria toDTO maps benId and household`() {
        val dto = filaria().toDTO()
        assertEquals(12L, dto.benId)
        assertEquals(501L, dto.houseHoldDetailsId)
    }

    @Test fun `filaria toDTO formats dates as datetime`() {
        val dto = filaria().toDTO()
        assertTrue(dto.mdaHomeVisitDate.contains("T"))
        assertTrue(dto.createdDate.contains("T"))
    }

    @Test fun `filaria toDTO passes through suffering and dose`() {
        val dto = filaria().copy(
            sufferingFromFilariasis = true,
            doseStatus = "Given",
            affectedBodyPart = "Leg",
            diseaseTypeID = 9
        ).toDTO()
        assertEquals(true, dto.sufferingFromFilariasis)
        assertEquals("Given", dto.doseStatus)
        assertEquals("Leg", dto.affectedBodyPart)
        assertEquals(9, dto.diseaseTypeID)
    }

    // =====================================================
    // KalaAzarScreeningCache.toDTO()
    // =====================================================

    private fun kala() = KalaAzarScreeningCache(
        benId = 13L,
        houseHoldDetailsId = 502L
    )

    @Test fun `kala toDTO maps benId and household`() {
        val dto = kala().toDTO()
        assertEquals(13L, dto.benId)
        assertEquals(502L, dto.houseHoldDetailsId)
    }

    @Test fun `kala toDTO formats dates as datetime`() {
        val dto = kala().toDTO()
        assertTrue(dto.visitDate.contains("T"))
        assertTrue(dto.dateOfRdt.contains("T"))
        assertTrue(dto.createdDate.contains("T"))
    }

    @Test fun `kala toDTO passes through case status`() {
        val dto = kala().copy(
            kalaAzarCaseStatus = "Confirmed",
            rapidDiagnosticTest = "Positive",
            referredTo = 5,
            diseaseTypeID = 2
        ).toDTO()
        assertEquals("Confirmed", dto.kalaAzarCaseStatus)
        assertEquals("Positive", dto.rapidDiagnosticTest)
        assertEquals(5, dto.referredTo)
        assertEquals(2, dto.diseaseTypeID)
    }

    @Test fun `kala toDTO sets syncState SYNCED`() {
        assertEquals(SyncState.SYNCED, kala().toDTO().syncState)
    }

    // =====================================================
    // MalariaScreeningCache.toDTO()
    // =====================================================

    private fun malaria() = MalariaScreeningCache(
        benId = 14L,
        visitId = 3L,
        houseHoldDetailsId = 503L
    )

    @Test fun `malaria toDTO maps benId visitId and household`() {
        val dto = malaria().toDTO()
        assertEquals(14L, dto.benId)
        assertEquals(3L, dto.visitId)
        assertEquals(503L, dto.houseHoldDetailsId)
    }

    @Test fun `malaria toDTO formats dates as datetime`() {
        val dto = malaria().toDTO()
        assertTrue(dto.caseDate.contains("T"))
        assertTrue(dto.screeningDate.contains("T"))
        assertTrue(dto.followUpDate.contains("T"))
    }

    @Test fun `malaria toDTO passes through symptoms and status`() {
        val dto = malaria().copy(
            caseStatus = "Positive",
            feverMoreThanTwoWeeks = true,
            headache = true,
            referredTo = 6,
            diseaseTypeID = 1
        ).toDTO()
        assertEquals("Positive", dto.caseStatus)
        assertEquals(true, dto.feverMoreThanTwoWeeks)
        assertEquals(true, dto.headache)
        assertEquals(6, dto.referredTo)
        assertEquals(1, dto.diseaseTypeID)
    }

    // =====================================================
    // MalariaConfirmedCasesCache.toDTO()
    // =====================================================

    private fun malariaConfirmed() = MalariaConfirmedCasesCache(
        benId = 15L,
        houseHoldDetailsId = 504L
    )

    @Test fun `malariaConfirmed toDTO maps benId and household`() {
        val dto = malariaConfirmed().toDTO()
        assertEquals(15L, dto.benId)
        assertEquals(504L, dto.houseHoldDetailsId)
    }

    @Test fun `malariaConfirmed toDTO formats dates as datetime`() {
        val dto = malariaConfirmed().toDTO()
        assertTrue(dto.dateOfDiagnosis.contains("T"))
        assertTrue(dto.treatmentStartDate.contains("T"))
        assertTrue(dto.treatmentCompletionDate.contains("T"))
        assertTrue(dto.referralDate.contains("T"))
    }

    @Test fun `malariaConfirmed toDTO passes through treatment`() {
        val dto = malariaConfirmed().copy(
            treatmentGiven = "ACT",
            day = "Day1"
        ).toDTO()
        assertEquals("ACT", dto.treatmentGiven)
        assertEquals("Day1", dto.day)
    }

    // =====================================================
    // IRSRoundScreening.toDTO()
    // =====================================================

    @Test fun `irs toDTO maps rounds and household`() {
        val dto = IRSRoundScreening(rounds = 2, householdId = 505L).toDTO()
        assertEquals(2, dto.rounds)
        assertEquals(505L, dto.householdId)
    }

    @Test fun `irs toDTO formats date as datetime`() {
        val dto = IRSRoundScreening(rounds = 1, householdId = 1L).toDTO()
        assertNotNull(dto.date)
        assertTrue(dto.date.contains("T"))
    }

    // =====================================================
    // TBConfirmedTreatmentCache.toDTO()
    // =====================================================

    private fun tbConfirmed() = TBConfirmedTreatmentCache(benId = 16L)

    @Test fun `tbConfirmed toDTO maps benId`() {
        assertEquals(16L, tbConfirmed().toDTO().benId)
    }

    @Test fun `tbConfirmed toDTO formats treatmentStartDate`() {
        val dto = tbConfirmed().toDTO()
        assertNotNull(dto.treatmentStartDate)
        assertTrue(dto.treatmentStartDate!!.contains("T"))
    }

    @Test fun `tbConfirmed toDTO passes through regimen and outcome`() {
        val dto = tbConfirmed().copy(
            regimenType = "New",
            monthlyFollowUpDone = "Yes",
            adherenceToMedicines = "Good",
            treatmentOutcome = "Cured",
            anyDiscomfort = false,
            treatmentCompleted = true
        ).toDTO()
        assertEquals("New", dto.regimenType)
        assertEquals("Yes", dto.monthlyFollowUpDone)
        assertEquals("Good", dto.adherenceToMedicines)
        assertEquals("Cured", dto.treatmentOutcome)
        assertEquals(false, dto.anyDiscomfort)
        assertEquals(true, dto.treatmentCompleted)
    }

    @Test fun `tbConfirmed toDTO default reasonForDeath`() {
        assertEquals("Tuberculosis", tbConfirmed().toDTO().reasonForDeath)
    }
}
