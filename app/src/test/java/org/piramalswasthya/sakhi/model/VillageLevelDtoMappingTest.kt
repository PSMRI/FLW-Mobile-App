package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Test

class VillageLevelDtoMappingTest {

    // =====================================================
    // PHCReviewMeetingCache.toDTO()
    // =====================================================

    private fun phc() = PHCReviewMeetingCache(
        id = 5,
        phcReviewDate = "2026-07-22"
    )

    @Test fun `phc toDTO maps id and date`() {
        val dto = phc().toDTO()
        assertEquals(5, dto.id)
        assertEquals("2026-07-22", dto.phcReviewDate)
    }

    @Test fun `phc toDTO passes through place fields`() {
        val dto = phc().copy(
            place = "Hall",
            placeId = 7,
            villageName = "V1",
            noOfBeneficiariesAttended = 12
        ).toDTO()
        assertEquals("Hall", dto.place)
        assertEquals(7, dto.placeId)
        assertEquals("V1", dto.villageName)
        assertEquals(12, dto.noOfBeneficiariesAttended)
    }

    @Test fun `phc toDTO maps images straight`() {
        val dto = phc().copy(image1 = "a", image2 = "b").toDTO()
        assertEquals("a", dto.Image1)
        assertEquals("b", dto.Image2)
    }

    @Test fun `phc toDTO passes through mitanin fields`() {
        val dto = phc().copy(
            mitaninHistory = "h",
            mitaninActivityCheckList = "c"
        ).toDTO()
        assertEquals("h", dto.mitaninHistory)
        assertEquals("c", dto.mitaninActivityCheckList)
    }

    // =====================================================
    // PHCReviewMeetingCache.toPHCDTODTO() — images swapped
    // =====================================================

    @Test fun `phc toPHCDTODTO swaps images`() {
        val out = phc().copy(image1 = "a", image2 = "b").toPHCDTODTO()
        assertEquals("b", out.image1)
        assertEquals("a", out.image2)
    }

    @Test fun `phc toPHCDTODTO keeps id date and place`() {
        val out = phc().copy(place = "Hall", placeId = 9, noOfBeneficiariesAttended = 3).toPHCDTODTO()
        assertEquals(5, out.id)
        assertEquals("2026-07-22", out.phcReviewDate)
        assertEquals("Hall", out.place)
        assertEquals(9, out.placeId)
        assertEquals(3, out.noOfBeneficiariesAttended)
    }

    // =====================================================
    // VHNCCache.toDTO()
    // =====================================================

    private fun vhnc() = VHNCCache(
        id = 3,
        vhncDate = "2026-01-01"
    )

    @Test fun `vhnc toDTO maps id and date`() {
        val dto = vhnc().toDTO()
        assertEquals(3, dto.id)
        assertEquals("2026-01-01", dto.vhncDate)
    }

    @Test fun `vhnc toDTO maps images straight`() {
        val dto = vhnc().copy(image1 = "x", image2 = "y").toDTO()
        assertEquals("x", dto.Image1)
        assertEquals("y", dto.Image2)
    }

    @Test fun `vhnc toDTO passes through committee counts`() {
        val dto = vhnc().copy(
            anm = 1, aww = 2, noOfPragnentWoment = 3,
            noOfLactingMother = 4, noOfCommittee = 5, followupPrevius = true
        ).toDTO()
        assertEquals(1, dto.anm)
        assertEquals(2, dto.aww)
        assertEquals(3, dto.noOfPragnentWoment)
        assertEquals(4, dto.noOfLactingMother)
        assertEquals(5, dto.noOfCommittee)
        assertEquals(true, dto.followupPrevius)
    }

    @Test fun `vhnc toDTO passes through village and place`() {
        val dto = vhnc().copy(villageName = "V", place = "P", noOfBeneficiariesAttended = 8).toDTO()
        assertEquals("V", dto.villageName)
        assertEquals("P", dto.place)
        assertEquals(8, dto.noOfBeneficiariesAttended)
    }

    // =====================================================
    // VHNCCache.toVhncDTODTO() — images swapped
    // =====================================================

    @Test fun `vhnc toVhncDTODTO swaps images`() {
        val out = vhnc().copy(image1 = "x", image2 = "y").toVhncDTODTO()
        assertEquals("y", out.image1)
        assertEquals("x", out.image2)
    }

    @Test fun `vhnc toVhncDTODTO keeps counts and date`() {
        val out = vhnc().copy(anm = 1, aww = 2, noOfCommittee = 5).toVhncDTODTO()
        assertEquals(3, out.id)
        assertEquals("2026-01-01", out.vhncDate)
        assertEquals(1, out.anm)
        assertEquals(2, out.aww)
        assertEquals(5, out.noOfCommittee)
    }

    // =====================================================
    // VHNDCache.toDTO()
    // =====================================================

    private fun vhnd() = VHNDCache(
        id = 4,
        vhndDate = "2026-02-02"
    )

    @Test fun `vhnd toDTO maps id and date`() {
        val dto = vhnd().toDTO()
        assertEquals(4, dto.id)
        assertEquals("2026-02-02", dto.vhndDate)
    }

    @Test fun `vhnd toDTO maps images straight`() {
        val dto = vhnd().copy(image1 = "m", image2 = "n").toDTO()
        assertEquals("m", dto.Image1)
        assertEquals("n", dto.Image2)
    }

    @Test fun `vhnd toDTO passes through education fields`() {
        val dto = vhnd().copy(
            vhndPlaceId = 6,
            pregnantWomenAnc = "a",
            lactatingMothersPnc = "b",
            childrenImmunization = "c",
            knowledgeBalancedDiet = "d",
            careDuringPregnancy = "e",
            importanceBreastfeeding = "f",
            complementaryFeeding = "g",
            hygieneSanitation = "h",
            familyPlanningHealthcare = "i"
        ).toDTO()
        assertEquals(6, dto.vhndPlaceId)
        assertEquals("a", dto.pregnantWomenAnc)
        assertEquals("b", dto.lactatingMothersPnc)
        assertEquals("c", dto.childrenImmunization)
        assertEquals("d", dto.knowledgeBalancedDiet)
        assertEquals("e", dto.careDuringPregnancy)
        assertEquals("f", dto.importanceBreastfeeding)
        assertEquals("g", dto.complementaryFeeding)
        assertEquals("h", dto.hygieneSanitation)
        assertEquals("i", dto.familyPlanningHealthcare)
    }

    // =====================================================
    // VHNDCache.toVhndDTODTO() — images swapped
    // =====================================================

    @Test fun `vhnd toVhndDTODTO swaps images`() {
        val out = vhnd().copy(image1 = "m", image2 = "n").toVhndDTODTO()
        assertEquals("n", out.image1)
        assertEquals("m", out.image2)
    }

    @Test fun `vhnd toVhndDTODTO keeps id date and education fields`() {
        val out = vhnd().copy(vhndPlaceId = 6, pregnantWomenAnc = "a", hygieneSanitation = "h").toVhndDTODTO()
        assertEquals(4, out.id)
        assertEquals("2026-02-02", out.vhndDate)
        assertEquals(6, out.vhndPlaceId)
        assertEquals("a", out.pregnantWomenAnc)
        assertEquals("h", out.hygieneSanitation)
    }
}
