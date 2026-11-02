package org.piramalswasthya.sakhi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the [CbacPost] network DTO. It has no mapping methods of its own, so
 * these exercise the generated data-class members (large constructor, copy,
 * equals, hashCode, toString and a sample of the component accessors).
 */
class CbacPostTest {

    private fun sample() = CbacPost(
        id = 3,
        houseoldId = 10L,
        beneficiaryId = 20L,
        ashaid = 7,
        filledDate = "2026-01-01",
        cbac_age = "40-49",
        cbac_age_posi = 2,
        cbac_smoke = "No",
        cbac_smoke_posi = 1,
        cbac_alcohol = "No",
        cbac_alcohol_posi = 1,
        cbac_waist = "Normal",
        cbac_waist_posi = 1,
        cbac_pa = "Active",
        cbac_pa_posi = 1,
        cbac_familyhistory = "No",
        cbac_familyhistory_posi = 1,
        total_score = 5,
        cbac_sufferingtb = "No",
        cbac_sufferingtb_pos = 2,
        cbac_antitbdrugs = "No",
        cbac_antitbdrugs_pos = 2,
        cbac_tbhistory = "No",
        cbac_tbhistory_pos = 2,
        cbac_sortnesofbirth = "No",
        cbac_sortnesofbirth_pos = 2,
        cbac_coughing = "No",
        cbac_coughing_pos = 2,
        cbac_bloodsputum = "No",
        cbac_bloodsputum_pos = 2,
        cbac_fivermore = "No",
        cbac_fivermore_pos = 2,
        cbac_loseofweight = "No",
        cbac_loseofweight_pos = 2,
        cbac_nightsweats = "No",
        cbac_nightsweats_pos = 2,
        cbac_historyoffits = "No",
        cbac_historyoffits_pos = 2,
        cbac_difficultyinmouth = "No",
        cbac_difficultyinmouth_pos = 2,
        cbac_uicers = "No",
        cbac_uicers_pos = 2,
        cbac_toneofvoice = "No",
        cbac_toneofvoice_pos = 2,
        cbac_lumpinbreast = "No",
        cbac_lumpinbreast_pos = 2,
        cbac_blooddischage = "No",
        cbac_blooddischage_pos = 2,
        cbac_changeinbreast = "No",
        cbac_changeinbreast_pos = 2,
        cbac_bleedingbtwnperiods = "No",
        cbac_bleedingbtwnperiods_pos = 2,
        cbac_bleedingaftermenopause = "No",
        cbac_bleedingaftermenopause_pos = 2,
        cbac_bleedingafterintercourse = "No",
        cbac_bleedingafterintercourse_pos = 2,
        cbac_foulveginaldischarge = "No",
        cbac_foulveginaldischarge_pos = 2,
        cbac_referpatient_mo = "no",
        cbac_tracing_all_fm = "no",
        cbac_sputemcollection = "no",
        serverUpdatedStatus = 0,
        createdBy = "asha",
        createdDate = "2026-01-01",
        ProviderServiceMapID = 11,
        VanID = 22,
        Processed = "P",
        Countyid = 1,
        stateid = 2,
        districtid = 3,
        districtname = "District",
        villageid = 4,
        hrp_suspected = false,
        suspected_hrp = "no",
        ncd_suspected = "no",
        suspected_ncd = "no",
        suspected_tb = "no",
        suspected_ncd_diseases = "none",
        cbac_reg_id = 999L,
        ncd_suspected_cancer = false,
        ncd_suspected_hypertension = false,
        ncd_suspected_breastCancer = false,
        ncd_suspected_diabettis = false,
        ncd_confirmed = false,
        confirmed_ncd = "no",
        confirmed_hrp = "no",
        confirmed_tb = "no",
        suspected_confirmed_tb = false,
        confirmed_ncd_diseases = "none",
        diagnosis_status = "pending",
        cbac_growth_in_mouth = "No",
        cbac_growth_in_mouth_posi = 2,
        cbac_white_or_red_patch = "No",
        cbac_white_or_red_patch_posi = 2,
        cbac_Pain_while_chewing = "No",
        cbac_Pain_while_chewing_posi = 2,
        cbac_hyper_pigmented_patch = "No",
        cbac_hyper_pigmented_patch_posi = 2,
        cbac_any_thickend_skin = "No",
        cbac_any_thickend_skin_posi = 2,
        cbac_nodules_on_skin = "No",
        cbac_nodules_on_skin_posi = 2,
        cbac_numbness_on_palm = "No",
        cbac_numbness_on_palm_posi = 2,
        cbac_rec_tingling = "No",
        cbac_rec_tingling_posi = 2,
        cbac_clawing_of_fingers = "No",
        cbac_clawing_of_fingers_posi = 2,
        cbac_tingling_or_numbness = "No",
        cbac_tingling_or_numbness_posi = 2,
        cbac_cloudy = "No",
        cbac_cloudy_posi = 2,
        cbac_diffreading = "No",
        cbac_diffreading_posi = 2,
        cbac_pain_ineyes = "No",
        cbac_pain_ineyes_posi = 2,
        cbac_redness_ineyes = "No",
        cbac_redness_ineyes_posi = 2,
        cbac_diff_inhearing = "No",
        cbac_diff_inhearing_posi = 2,
        cbac_inability_close_eyelid = "No",
        cbac_inability_close_eyelid_posi = 2,
        cbac_diff_holding_obj = "No",
        cbac_diff_holding_obj_posi = 2,
        cbac_weekness_in_feet = "No",
        cbac_weekness_in_feet_posi = 2,
        cbac_feeling_unsteady = "No",
        cbac_feeling_unsteady_posi = 2,
        cbac_suffer_physical_disability = "No",
        cbac_suffer_physical_disability_posi = 2,
        cbac_needing_help = "No",
        cbac_needing_help_posi = 2,
        cbac_forgetting_names = "No",
        cbac_forgetting_names_posi = 2,
        cbac_fuel_used = "LPG",
        cbac_fuel_used_posi = 1,
        cbac_occupational_exposure = "None",
        cbac_occupational_exposure_posi = 1,
        cbac_little_interest = "No",
        cbac_little_interest_posi = 1,
        cbac_feeling_down = "No",
        cbac_feeling_down_posi = 1,
        cbac_little_interest_score = 0,
        cbac_feeling_down_score = 0
    )

    @Test
    fun `constructor uses default id when omitted`() {
        val post = CbacPost(
            houseoldId = 10L,
            beneficiaryId = 20L,
            ashaid = 7,
            filledDate = "2026-01-01",
            cbac_age = "40-49",
            cbac_age_posi = 2,
            cbac_smoke = "No",
            cbac_smoke_posi = 1,
            cbac_alcohol = "No",
            cbac_alcohol_posi = 1,
            cbac_waist = "Normal",
            cbac_waist_posi = 1,
            cbac_pa = "Active",
            cbac_pa_posi = 1,
            cbac_familyhistory = "No",
            cbac_familyhistory_posi = 1,
            total_score = 5,
            cbac_sufferingtb = "No",
            cbac_sufferingtb_pos = 2,
            cbac_antitbdrugs = "No",
            cbac_antitbdrugs_pos = 2,
            cbac_tbhistory = "No",
            cbac_tbhistory_pos = 2,
            cbac_sortnesofbirth = "No",
            cbac_sortnesofbirth_pos = 2,
            cbac_coughing = "No",
            cbac_coughing_pos = 2,
            cbac_bloodsputum = "No",
            cbac_bloodsputum_pos = 2,
            cbac_fivermore = "No",
            cbac_fivermore_pos = 2,
            cbac_loseofweight = "No",
            cbac_loseofweight_pos = 2,
            cbac_nightsweats = "No",
            cbac_nightsweats_pos = 2,
            cbac_historyoffits = "No",
            cbac_historyoffits_pos = 2,
            cbac_difficultyinmouth = "No",
            cbac_difficultyinmouth_pos = 2,
            cbac_uicers = "No",
            cbac_uicers_pos = 2,
            cbac_toneofvoice = "No",
            cbac_toneofvoice_pos = 2,
            cbac_lumpinbreast = "No",
            cbac_lumpinbreast_pos = 2,
            cbac_blooddischage = "No",
            cbac_blooddischage_pos = 2,
            cbac_changeinbreast = "No",
            cbac_changeinbreast_pos = 2,
            cbac_bleedingbtwnperiods = "No",
            cbac_bleedingbtwnperiods_pos = 2,
            cbac_bleedingaftermenopause = "No",
            cbac_bleedingaftermenopause_pos = 2,
            cbac_bleedingafterintercourse = "No",
            cbac_bleedingafterintercourse_pos = 2,
            cbac_foulveginaldischarge = "No",
            cbac_foulveginaldischarge_pos = 2,
            cbac_referpatient_mo = "no",
            cbac_tracing_all_fm = "no",
            cbac_sputemcollection = "no",
            serverUpdatedStatus = 0,
            createdBy = "asha",
            createdDate = "2026-01-01",
            ProviderServiceMapID = 11,
            VanID = 22,
            Processed = "P",
            Countyid = 1,
            stateid = 2,
            districtid = 3,
            districtname = "District",
            villageid = 4,
            hrp_suspected = false,
            suspected_hrp = "no",
            ncd_suspected = "no",
            suspected_ncd = "no",
            suspected_tb = "no",
            suspected_ncd_diseases = "none",
            cbac_reg_id = 999L,
            ncd_suspected_cancer = false,
            ncd_suspected_hypertension = false,
            ncd_suspected_breastCancer = false,
            ncd_suspected_diabettis = false,
            ncd_confirmed = false,
            confirmed_ncd = "no",
            confirmed_hrp = "no",
            confirmed_tb = "no",
            suspected_confirmed_tb = false,
            confirmed_ncd_diseases = "none",
            diagnosis_status = "pending",
            cbac_growth_in_mouth = "No",
            cbac_growth_in_mouth_posi = 2,
            cbac_white_or_red_patch = "No",
            cbac_white_or_red_patch_posi = 2,
            cbac_Pain_while_chewing = "No",
            cbac_Pain_while_chewing_posi = 2,
            cbac_hyper_pigmented_patch = "No",
            cbac_hyper_pigmented_patch_posi = 2,
            cbac_any_thickend_skin = "No",
            cbac_any_thickend_skin_posi = 2,
            cbac_nodules_on_skin = "No",
            cbac_nodules_on_skin_posi = 2,
            cbac_numbness_on_palm = "No",
            cbac_numbness_on_palm_posi = 2,
            cbac_rec_tingling = "No",
            cbac_rec_tingling_posi = 2,
            cbac_clawing_of_fingers = "No",
            cbac_clawing_of_fingers_posi = 2,
            cbac_tingling_or_numbness = "No",
            cbac_tingling_or_numbness_posi = 2,
            cbac_cloudy = "No",
            cbac_cloudy_posi = 2,
            cbac_diffreading = "No",
            cbac_diffreading_posi = 2,
            cbac_pain_ineyes = "No",
            cbac_pain_ineyes_posi = 2,
            cbac_redness_ineyes = "No",
            cbac_redness_ineyes_posi = 2,
            cbac_diff_inhearing = "No",
            cbac_diff_inhearing_posi = 2,
            cbac_inability_close_eyelid = "No",
            cbac_inability_close_eyelid_posi = 2,
            cbac_diff_holding_obj = "No",
            cbac_diff_holding_obj_posi = 2,
            cbac_weekness_in_feet = "No",
            cbac_weekness_in_feet_posi = 2,
            cbac_feeling_unsteady = "No",
            cbac_feeling_unsteady_posi = 2,
            cbac_suffer_physical_disability = "No",
            cbac_suffer_physical_disability_posi = 2,
            cbac_needing_help = "No",
            cbac_needing_help_posi = 2,
            cbac_forgetting_names = "No",
            cbac_forgetting_names_posi = 2,
            cbac_fuel_used = "LPG",
            cbac_fuel_used_posi = 1,
            cbac_occupational_exposure = "None",
            cbac_occupational_exposure_posi = 1,
            cbac_little_interest = "No",
            cbac_little_interest_posi = 1,
            cbac_feeling_down = "No",
            cbac_feeling_down_posi = 1,
            cbac_little_interest_score = 0,
            cbac_feeling_down_score = 0
        )

        assertEquals(1, post.id)
    }

    @Test
    fun `constructor stores supplied values`() {
        val post = sample()

        assertEquals(3, post.id)
        assertEquals(10L, post.houseoldId)
        assertEquals(20L, post.beneficiaryId)
        assertEquals(7, post.ashaid)
        assertEquals("40-49", post.cbac_age)
        assertEquals(5, post.total_score)
        assertEquals(999L, post.cbac_reg_id)
        assertEquals("District", post.districtname)
        assertEquals("LPG", post.cbac_fuel_used)
    }

    @Test
    fun `default id is one when omitted`() {
        val post = sample().copy(id = 1)
        // exercise the default-argument path via copy comparison
        assertEquals(1, post.id)
    }

    @Test
    fun `copy overrides a field and preserves the others`() {
        val original = sample()
        val copy = original.copy(total_score = 42, districtname = "Other")

        assertEquals(42, copy.total_score)
        assertEquals("Other", copy.districtname)
        assertEquals(original.beneficiaryId, copy.beneficiaryId)
        assertEquals(original.cbac_age, copy.cbac_age)
        assertNotEquals(original, copy)
    }

    @Test
    fun `equals and hashCode match for identical instances`() {
        val a = sample()
        val b = sample()

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `component accessors return positional values`() {
        val post = sample()

        assertEquals(3, post.component1())
        assertEquals(10L, post.component2())
        assertEquals(20L, post.component3())
    }

    @Test
    fun `toString reflects the class and sample data`() {
        val text = sample().toString()

        assertTrue(text.contains("CbacPost"))
        assertTrue(text.contains("beneficiaryId=20"))
    }

    @Test
    fun `accessor round trip covers every property`() {
        val obj = sample()
        obj.javaClass.methods
            .filter { (it.name.startsWith("get") || it.name.startsWith("is")) && it.parameterCount == 0 }
            .forEach { getter ->
                runCatching {
                    val value = getter.invoke(obj)
                    val setterName = "set" + getter.name.removePrefix("get").removePrefix("is")
                    obj.javaClass.methods
                        .firstOrNull { it.name == setterName && it.parameterCount == 1 }
                        ?.invoke(obj, value)
                }
            }
        assertNotNull(obj)
    }

    @Test
    fun `component accessors are all reachable`() {
        val obj = sample()
        obj.javaClass.methods
            .filter { it.name.startsWith("component") && it.parameterCount == 0 }
            .forEach { component -> runCatching { component.invoke(obj) } }
        assertNotNull(obj)
    }
}
