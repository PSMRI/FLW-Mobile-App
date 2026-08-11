package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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

    private fun fullyPopulatedScreening() = LeprosyScreeningCache(
        id = 9,
        benId = 11L,
        homeVisitDate = 1_600_000_000_000L,
        leprosyStatusDate = 1_600_100_000_000L,
        dateOfDeath = 1_600_200_000_000L,
        houseHoldDetailsId = 500L,
        leprosyStatus = "Positive",
        referredTo = 2,
        leprosyState = "Confirmed",
        referToName = "PHC Center",
        otherReferredTo = "Other Center",
        typeOfLeprosy = "MB",
        remarks = "Needs follow up",
        beneficiaryStatus = "Alive",
        placeOfDeath = "Home",
        otherPlaceOfDeath = "N/A",
        reasonForDeath = "Illness",
        otherReasonForDeath = "N/A",
        diseaseTypeID = 3,
        beneficiaryStatusId = 1,
        leprosySymptoms = "Patch,Numbness",
        leprosySymptomsPosition = 2,
        lerosyStatusPosition = 1,
        currentVisitNumber = 2,
        visitLabel = "Visit -2",
        visitNumber = 2,
        isConfirmed = true,
        treatmentStartDate = 1_600_300_000_000L,
        totalFollowUpMonthsRequired = 12,
        treatmentEndDate = 1_600_400_000_000L,
        mdtBlisterPackRecived = "Yes",
        treatmentStatus = "Ongoing",
        createdBy = "creator",
        createdDate = 1_600_500_000_000L,
        modifiedBy = "modifier",
        lastModDate = 1_600_600_000_000L,
        recurrentUlceration = "Yes",
        recurrentUlcerationId = 2,
        recurrentTingling = "Yes",
        recurrentTinglingId = 2,
        hypopigmentedPatch = "Yes",
        hypopigmentedPatchId = 2,
        thickenedSkin = "Yes",
        thickenedSkinId = 2,
        skinNodules = "Yes",
        skinNodulesId = 2,
        skinPatchDiscoloration = "Yes",
        skinPatchDiscolorationId = 2,
        recurrentNumbness = "Yes",
        recurrentNumbnessId = 2,
        clawingFingers = "Yes",
        clawingFingersId = 2,
        tinglingNumbnessExtremities = "Yes",
        tinglingNumbnessExtremitiesId = 2,
        inabilityCloseEyelid = "Yes",
        inabilityCloseEyelidId = 2,
        difficultyHoldingObjects = "Yes",
        difficultyHoldingObjectsId = 2,
        weaknessFeet = "Yes",
        weaknessFeetId = 2,
        syncState = SyncState.SYNCED
    )

    @Test fun `toDTO maps referral and death detail fields when populated`() {
        val dto = fullyPopulatedScreening().toDTO()
        assertEquals(2, dto.referredTo)
        assertEquals("PHC Center", dto.referToName)
        assertEquals("Other Center", dto.otherReferredTo)
        assertEquals("Alive", dto.beneficiaryStatus)
        assertEquals("Home", dto.placeOfDeath)
        assertEquals("N/A", dto.otherPlaceOfDeath)
        assertEquals("Illness", dto.reasonForDeath)
        assertEquals("N/A", dto.otherReasonForDeath)
        assertEquals(3, dto.diseaseTypeID)
        assertEquals(1, dto.beneficiaryStatusId)
    }

    @Test fun `toDTO maps visit and treatment progress fields when populated`() {
        val dto = fullyPopulatedScreening().toDTO()
        assertEquals(2, dto.currentVisitNumber)
        assertEquals("Visit -2", dto.visitLabel)
        assertEquals(2, dto.visitNumber)
        assertTrue(dto.isConfirmed)
        assertEquals("Confirmed", dto.leprosyState)
        assertEquals(12, dto.totalFollowUpMonthsRequired)
        assertEquals("Yes", dto.mdtBlisterPackRecived)
        assertEquals("Ongoing", dto.treatmentStatus)
    }

    @Test fun `toDTO maps all leprosy symptom fields when populated`() {
        val dto = fullyPopulatedScreening().toDTO()
        assertEquals("Yes", dto.hypopigmentedPatch)
        assertEquals(2, dto.hypopigmentedPatchId)
        assertEquals("Yes", dto.thickenedSkin)
        assertEquals(2, dto.thickenedSkinId)
        assertEquals("Yes", dto.skinNodules)
        assertEquals(2, dto.skinNodulesId)
        assertEquals("Yes", dto.skinPatchDiscoloration)
        assertEquals(2, dto.skinPatchDiscolorationId)
        assertEquals("Yes", dto.recurrentNumbness)
        assertEquals(2, dto.recurrentNumbnessId)
        assertEquals("Yes", dto.clawingFingers)
        assertEquals(2, dto.clawingFingersId)
        assertEquals("Yes", dto.tinglingNumbnessExtremities)
        assertEquals(2, dto.tinglingNumbnessExtremitiesId)
        assertEquals("Yes", dto.inabilityCloseEyelid)
        assertEquals(2, dto.inabilityCloseEyelidId)
        assertEquals("Yes", dto.difficultyHoldingObjects)
        assertEquals(2, dto.difficultyHoldingObjectsId)
        assertEquals("Yes", dto.weaknessFeet)
        assertEquals(2, dto.weaknessFeetId)
    }

    @Test fun `toDTO default nullable fields map to null`() {
        val dto = screening().toDTO()
        assertEquals(null, dto.referToName)
        assertEquals(null, dto.otherReferredTo)
        assertEquals(null, dto.beneficiaryStatus)
        assertEquals(null, dto.placeOfDeath)
        assertEquals(null, dto.hypopigmentedPatch)
        assertEquals(null, dto.weaknessFeet)
    }

    @Test fun `equals is true when all fields match`() {
        assertEquals(fullyPopulatedScreening(), fullyPopulatedScreening())
    }

    @Test fun `equals is false when a field differs`() {
        assertNotEquals(
            fullyPopulatedScreening(),
            fullyPopulatedScreening().copy(remarks = "Different remark")
        )
    }

    @Test fun `equals is false against null and a different type`() {
        val cache = fullyPopulatedScreening()
        assertFalse(cache.equals(null))
        assertFalse(cache.equals("not a cache"))
        assertTrue(cache.equals(cache))
    }

    @Test fun `hashCode is stable and equal for equal objects`() {
        val first = fullyPopulatedScreening()
        val second = fullyPopulatedScreening()
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test fun `hashCode does not throw when optional fields are default`() {
        assertNotNull(screening().hashCode())
    }

    @Test fun `toString contains key field values`() {
        val text = fullyPopulatedScreening().toString()
        assertTrue(text.contains("LeprosyScreeningCache"))
        assertTrue(text.contains("Confirmed"))
        assertTrue(text.contains("PHC Center"))
    }

    @Test fun `copy without arguments produces an equal but distinct instance`() {
        val original = fullyPopulatedScreening()
        val copied = original.copy()
        assertEquals(original, copied)
        assertEquals(original.hashCode(), copied.hashCode())
    }

    @Test fun `destructuring exposes all component values`() {
        val cache = fullyPopulatedScreening()
        val (
            id, benId, homeVisitDate, leprosyStatusDate, dateOfDeath, houseHoldDetailsId,
            leprosyStatus, referredTo, leprosyState, referToName, otherReferredTo, typeOfLeprosy,
            remarks, beneficiaryStatus, placeOfDeath, otherPlaceOfDeath, reasonForDeath,
            otherReasonForDeath, diseaseTypeID, beneficiaryStatusId, leprosySymptoms,
            leprosySymptomsPosition, lerosyStatusPosition, currentVisitNumber, visitLabel,
            visitNumber, isConfirmed, treatmentStartDate, totalFollowUpMonthsRequired,
            treatmentEndDate, mdtBlisterPackRecived, treatmentStatus, createdBy, createdDate,
            modifiedBy, lastModDate, recurrentUlceration, recurrentUlcerationId,
            recurrentTingling, recurrentTinglingId, hypopigmentedPatch, hypopigmentedPatchId,
            thickenedSkin, thickenedSkinId, skinNodules, skinNodulesId, skinPatchDiscoloration,
            skinPatchDiscolorationId, recurrentNumbness, recurrentNumbnessId, clawingFingers,
            clawingFingersId, tinglingNumbnessExtremities, tinglingNumbnessExtremitiesId,
            inabilityCloseEyelid, inabilityCloseEyelidId, difficultyHoldingObjects,
            difficultyHoldingObjectsId, weaknessFeet, weaknessFeetId, syncState
        ) = cache

        assertEquals(9, id)
        assertEquals(11L, benId)
        assertEquals(500L, houseHoldDetailsId)
        assertEquals("Positive", leprosyStatus)
        assertEquals("Confirmed", leprosyState)
        assertEquals("PHC Center", referToName)
        assertEquals("creator", createdBy)
        assertEquals("modifier", modifiedBy)
        assertEquals(2, weaknessFeetId)
        assertEquals("Yes", weaknessFeet)
        assertEquals(SyncState.SYNCED, syncState)
        assertTrue(isConfirmed)
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

    @Test fun `followUp destructuring exposes all component values`() {
        val cache = followUp().copy(
            treatmentStatus = "Ongoing",
            mdtBlisterPackReceived = "Yes",
            treatmentCompleteDate = 1_600_000_000_000L,
            remarks = "Remark",
            leprosySymptoms = "Patch",
            typeOfLeprosy = "MB",
            leprosySymptomsPosition = 2,
            visitLabel = "Visit -3",
            leprosyStatus = "Confirmed",
            referredTo = 1,
            referToName = "PHC",
            mdtBlisterPackRecived = "Yes"
        )
        val (
            id, benId, visitNumber, followUpDate, treatmentStatus, mdtBlisterPackReceived,
            treatmentCompleteDate, remarks, homeVisitDate, leprosySymptoms, typeOfLeprosy,
            leprosySymptomsPosition, visitLabel, leprosyStatus, referredTo, referToName,
            treatmentEndDate, mdtBlisterPackRecived, treatmentStartDate, createdBy, createdDate,
            modifiedBy, lastModDate, syncState
        ) = cache

        assertEquals(22L, benId)
        assertEquals(3, visitNumber)
        assertEquals("Ongoing", treatmentStatus)
        assertEquals("Confirmed", leprosyStatus)
        assertEquals("PHC", referToName)
        assertEquals("creator", createdBy)
        assertEquals("modifier", modifiedBy)
        assertEquals(SyncState.UNSYNCED, syncState)
    }
}
