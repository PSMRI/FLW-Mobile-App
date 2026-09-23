package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState

/**
 * Exercises [CbacResponseDto.toEntity] which contains ~40 branchy
 * `if (x.equals("yes", true)) 1 else 2` mappings. We build the DTO fully and
 * verify both the "yes" and "no" branches, plus the constant fields.
 */
class CbacResponseDtoMappingTest {

    private fun dto(ans: String, vanId: Int? = 4) = CbacResponseDto(
        id = 5,
        beneficiaryRegId = 10L,
        visitCode = 1L,
        cbacAge = "a",
        cbacAgeScore = 1,
        cbacConsumeGutka = "g",
        cbacConsumeGutkaScore = 1,
        cbacAlcohol = "al",
        cbacAlcoholScore = 1,
        cbacWaistMale = "wm",
        cbacWaistMaleScore = 1,
        cbacPhysicalActivity = "pa",
        cbacPhysicalActivityScore = 1,
        cbacFamilyHistoryBpdiabetes = "fh",
        cbacFamilyHistoryBpdiabetesScore = 1,
        cbacShortnessBreath = ans,
        cbacCough2weeks = ans,
        cbacBloodsputum = ans,
        cbacFever2weeks = ans,
        cbacWeightLoss = ans,
        cbacNightSweats = ans,
        cbacAntiTBDrugs = ans,
        cbacTb = ans,
        cbacTBHistory = ans,
        cbacUlceration = ans,
        cbacRecurrentTingling = ans,
        cbacFitsHistory = ans,
        cbacMouthopeningDifficulty = ans,
        cbacMouthUlcers = ans,
        cbacMouthUlcersGrowth = ans,
        cbacMouthredpatch = ans,
        cbacPainchewing = ans,
        cbacTonechange = ans,
        cbacHypopigmentedpatches = ans,
        cbacThickenedskin = ans,
        cbacNodulesonskin = ans,
        cbacRecurrentNumbness = ans,
        cbacBlurredVision = ans,
        cbacDifficultHoldingObjects = ans,
        cbacFeetweakness = ans,
        cbacLumpBreast = ans,
        cbacBloodnippleDischarge = ans,
        cbacBreastsizechange = ans,
        cbacBleedingPeriods = ans,
        cbacBleedingMenopause = ans,
        cbacBleedingIntercourse = ans,
        cbacVaginalDischarge = ans,
        cbacHandTingling = ans,
        cbacClawingfingers = ans,
        cbacDifficultyHearing = ans,
        cbacRednessPain = ans,
        cbacDifficultyreading = ans,
        CbacOccupationalExposure = "oe",
        CbacBotheredProblemLast2weeks = "bp",
        CbacLittleInterestPleasure = "li",
        CbacDepressedhopeless = "dh",
        CbacDiscolorationSkin = "ds",
        cbacPainineyes = ans,
        CbacCookingOil = "co",
        cbacInabilityCloseeyelid = ans,
        totalScore = 12,
        deleted = false,
        processed = "N",
        createdBy = "creator",
        createdDate = "Jan 01, 2026, 10:30:00 AM",
        lastModDate = null,
        vanId = vanId,
        parkingPlaceId = 2,
        CbacOccupationalExposureScore = 3,
        CbacLittleInterestPleasureScore = 4,
        CbacDepressedhopelessScore = 5,
        CbacCookingOilScore = 6,
        CbacFeelingDownScore = 7,
        isRefer = true,
    )

    @Test
    fun `toEntity maps constant and score fields`() {
        val entity = dto("yes").toEntity()

        assertEquals(5, entity.id)
        assertEquals(10L, entity.benId)
        assertEquals(0, entity.ashaId)
        assertEquals(12, entity.total_score)
        assertEquals(4, entity.VanID)
        assertEquals("P", entity.Processed)
        assertEquals(SyncState.SYNCED, entity.syncState)
        assertEquals(true, entity.isReffered)
        assertEquals("creator", entity.createdBy)
        // score-derived positions
        assertEquals(1, entity.cbac_age_posi)
        assertEquals(1, entity.cbac_smoke_posi)
        assertEquals(1, entity.cbac_alcohol_posi)
        assertEquals(1, entity.cbac_waist_posi) // male score preferred
        assertEquals(1, entity.cbac_pa_posi)
        assertEquals(1, entity.cbac_familyhistory_posi)
        assertEquals(4, entity.cbac_little_interest_posi)
        assertEquals(4, entity.cbac_little_interest_score)
        assertEquals(6, entity.cbac_fuel_used_posi)
        assertEquals(3, entity.cbac_occupational_exposure_posi)
        assertEquals(7, entity.cbac_feeling_down_posi)
        assertEquals(7, entity.cbac_feeling_down_score)
        assertTrue(entity.fillDate > 0L) // createdDate parsed to millis
    }

    @Test
    fun `toEntity yes answers map to position one`() {
        val entity = dto("yes").toEntity()

        assertEquals(1, entity.cbac_sufferingtb_pos)
        assertEquals(1, entity.cbac_sortnesofbirth_pos)
        assertEquals(1, entity.cbac_coughing_pos)
        assertEquals(1, entity.cbac_bloodsputum_pos)
        assertEquals(1, entity.cbac_fivermore_pos)
        assertEquals(1, entity.cbac_loseofweight_pos)
        assertEquals(1, entity.cbac_uicers_pos)
        assertEquals(1, entity.cbac_diffreading_posi)
        assertEquals(1, entity.cbac_foulveginaldischarge_pos)
        // cbacTonechange "yes" -> 1 (unique: else branch is 0, not 2)
        assertEquals(1, entity.cbac_toneofvoice_pos)
    }

    @Test
    fun `toEntity no answers map to position two and tone else is zero`() {
        val entity = dto("no").toEntity()

        assertEquals(2, entity.cbac_sufferingtb_pos)
        assertEquals(2, entity.cbac_sortnesofbirth_pos)
        assertEquals(2, entity.cbac_coughing_pos)
        assertEquals(2, entity.cbac_uicers_pos)
        assertEquals(2, entity.cbac_diffreading_posi)
        assertEquals(2, entity.cbac_lumpinbreast_pos)
        assertEquals(2, entity.cbac_bleedingbtwnperiods_pos)
        // cbacTonechange non-yes -> else branch is 0
        assertEquals(0, entity.cbac_toneofvoice_pos)
    }
}
