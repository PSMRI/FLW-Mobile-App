package org.piramalswasthya.sakhi.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

class ScreeningDtoMappingTest {

    // =====================================================
    // LeprosyScreeningDTO.toCache()
    // =====================================================

    private fun leprosyDto() = LeprosyScreeningDTO(
        benId = 11L,
        homeVisitDate = "2023-01-15",
        leprosyStatusDate = "2023-01-16",
        dateOfDeath = "null",
        houseHoldDetailsId = 500L,
        createdBy = "creator",
        createdDate = "2023-01-10",
        modifiedBy = "modifier",
        lastModDate = "2023-01-11"
    )

    @Test fun `leprosy toCache maps ids`() {
        val cache = leprosyDto().toCache()
        assertEquals(11L, cache.benId)
        assertEquals(500L, cache.houseHoldDetailsId)
    }

    @Test fun `leprosy toCache sets synced state`() {
        assertEquals(SyncState.SYNCED, leprosyDto().toCache().syncState)
    }

    @Test fun `leprosy toCache maps created and modified by`() {
        val cache = leprosyDto().toCache()
        assertEquals("creator", cache.createdBy)
        assertEquals("modifier", cache.modifiedBy)
    }

    @Test fun `leprosy toCache parses valid date to positive long`() {
        assertTrue(leprosyDto().toCache().homeVisitDate > 0L)
    }

    @Test fun `leprosy toCache parses null date literal to zero`() {
        assertEquals(0L, leprosyDto().toCache().dateOfDeath)
    }

    @Test fun `leprosy toCache defaults for visit and confirmation`() {
        val cache = leprosyDto().toCache()
        assertEquals(1, cache.currentVisitNumber)
        assertEquals(false, cache.isConfirmed)
    }

    @Test fun `leprosy toCache passes through symptom fields`() {
        val cache = leprosyDto().copy(
            leprosyStatus = "Positive",
            typeOfLeprosy = "MB",
            weaknessFeet = "Yes",
            weaknessFeetId = 2
        ).toCache()
        assertEquals("Positive", cache.leprosyStatus)
        assertEquals("MB", cache.typeOfLeprosy)
        assertEquals("Yes", cache.weaknessFeet)
        assertEquals(2, cache.weaknessFeetId)
    }

    // =====================================================
    // MalariaScreeningDTO.toCache()
    // =====================================================

    private fun malariaDto() = MalariaScreeningDTO(
        benId = 22L,
        visitId = 7L,
        caseDate = "2023-02-01",
        houseHoldDetailsId = 600L,
        screeningDate = "2023-02-02",
        beneficiaryStatus = "Alive",
        dateOfDeath = "null",
        placeOfDeath = "",
        otherPlaceOfDeath = "",
        reasonForDeath = "",
        otherReasonForDeath = "",
        rapidDiagnosticTest = "Negative",
        dateOfRdt = "2023-02-03",
        slideTestName = "Test",
        slideTestPf = "No",
        slideTestPv = "No",
        dateOfSlideTest = "2023-02-04",
        dateOfVisitBySupervisor = "null",
        followUpDate = "2023-02-10",
        caseStatus = "Open"
    )

    @Test fun `malaria toCache maps ids`() {
        val cache = malariaDto().toCache()
        assertEquals(22L, cache.benId)
        assertEquals(7L, cache.visitId)
        assertEquals(600L, cache.houseHoldDetailsId)
    }

    @Test fun `malaria toCache sets synced state`() {
        assertEquals(SyncState.SYNCED, malariaDto().toCache().syncState)
    }

    @Test fun `malaria toCache maps status fields`() {
        val cache = malariaDto().toCache()
        assertEquals("Alive", cache.beneficiaryStatus)
        assertEquals("Open", cache.caseStatus)
    }

    @Test fun `malaria toCache parses screening date to positive long`() {
        assertTrue(malariaDto().toCache().screeningDate > 0L)
    }

    @Test fun `malaria toCache parses null date literal to zero`() {
        assertEquals(0L, malariaDto().toCache().dateOfDeath)
    }

    @Test fun `malaria toCache maps symptom booleans`() {
        val cache = malariaDto().copy(headache = true, vomiting = true).toCache()
        assertEquals(true, cache.headache)
        assertEquals(true, cache.vomiting)
    }

    // =====================================================
    // MalariaConfirmedDTO.toCache()
    // =====================================================

    private fun confirmedDto() = MalariaConfirmedDTO(
        diseaseId = 9,
        benId = 33L,
        houseHoldDetailsId = 700L,
        dateOfDiagnosis = "2023-03-01",
        treatmentStartDate = "2023-03-02",
        treatmentCompletionDate = "2023-03-20",
        treatmentGiven = "ACT",
        referralDate = "null",
        day = "3"
    )

    @Test fun `confirmed toCache maps ids`() {
        val cache = confirmedDto().toCache()
        assertEquals(33L, cache.benId)
        assertEquals(700L, cache.houseHoldDetailsId)
        assertEquals(9, cache.diseaseId)
    }

    @Test fun `confirmed toCache maps treatment and day`() {
        val cache = confirmedDto().toCache()
        assertEquals("ACT", cache.treatmentGiven)
        assertEquals("3", cache.day)
    }

    @Test fun `confirmed toCache sets synced state`() {
        assertEquals(SyncState.SYNCED, confirmedDto().toCache().syncState)
    }

    @Test fun `confirmed toCache parses diagnosis date to positive long`() {
        assertTrue(confirmedDto().toCache().dateOfDiagnosis > 0L)
    }

    // =====================================================
    // Generated data-class members (equals / hashCode / toString / copy)
    // =====================================================

    @Test fun `LeprosyScreeningDTO generated members behave consistently`() {
        val dto = leprosyDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("LeprosyScreeningDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals("other"))
        assertNotEquals(dto, dto.copy(benId = 12L))
        assertEquals(11L, dto.benId)
        assertEquals(500L, dto.houseHoldDetailsId)
        assertEquals("2023-01-15", dto.homeVisitDate)
        assertEquals("2023-01-16", dto.leprosyStatusDate)
        assertEquals("null", dto.dateOfDeath)
        assertEquals("creator", dto.createdBy)
        assertEquals("2023-01-10", dto.createdDate)
        assertEquals("modifier", dto.modifiedBy)
        assertEquals("2023-01-11", dto.lastModDate)
    }

    @Test fun `LeprosyScreeningDTO copy overrides only named fields`() {
        val changed = leprosyDto().copy(leprosyStatus = "Negative", typeOfLeprosy = "PB")
        assertEquals("Negative", changed.leprosyStatus)
        assertEquals("PB", changed.typeOfLeprosy)
        assertEquals(11L, changed.benId)
        assertEquals("creator", changed.createdBy)
        assertNotEquals(leprosyDto(), changed)
    }

    @Test fun `MalariaScreeningDTO generated members behave consistently`() {
        val dto = malariaDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("MalariaScreeningDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals(7))
        assertNotEquals(dto, dto.copy(benId = 23L))
        assertEquals(22L, dto.benId)
        assertEquals(7L, dto.visitId)
        assertEquals(600L, dto.houseHoldDetailsId)
        assertEquals("Alive", dto.beneficiaryStatus)
        assertEquals("Negative", dto.rapidDiagnosticTest)
        assertEquals("Test", dto.slideTestName)
        assertEquals("No", dto.slideTestPf)
        assertEquals("No", dto.slideTestPv)
        assertEquals("Open", dto.caseStatus)
    }

    @Test fun `MalariaScreeningDTO toCache maps slide test and follow up details`() {
        val cache = malariaDto().toCache()
        assertEquals("Negative", cache.rapidDiagnosticTest)
        assertEquals("Test", cache.slideTestName)
        assertEquals("No", cache.slideTestPf)
        assertEquals("No", cache.slideTestPv)
        assertTrue(cache.dateOfRdt > 0L)
        assertTrue(cache.dateOfSlideTest > 0L)
        assertTrue(cache.followUpDate > 0L)
        assertEquals(0L, cache.dateOfVisitBySupervisor)
    }

    @Test fun `MalariaConfirmedDTO generated members behave consistently`() {
        val dto = confirmedDto()
        val same = dto.copy()
        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())
        assertEquals(dto.toString(), same.toString())
        assertTrue(dto.toString().contains("MalariaConfirmedDTO"))
        assertFalse(dto.equals(null))
        assertFalse(dto.equals("x"))
        assertNotEquals(dto, dto.copy(benId = 34L))
        assertEquals(9, dto.diseaseId)
        assertEquals(33L, dto.benId)
        assertEquals(700L, dto.houseHoldDetailsId)
        assertEquals("ACT", dto.treatmentGiven)
        assertEquals("3", dto.day)
    }
}
