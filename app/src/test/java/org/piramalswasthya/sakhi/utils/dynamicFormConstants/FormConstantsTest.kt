package org.piramalswasthya.sakhi.utils.dynamicFormConstants

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormConstantsTest {

    @Test fun `HBNC_FORM_ID is correct`() { assertEquals("hbnc_form_001", FormConstants.HBNC_FORM_ID) }
    @Test fun `CDTF_001 is correct`() { assertEquals("CDTF_001", FormConstants.CDTF_001) }
    @Test fun `ANC_FORM_ID is correct`() { assertEquals("anc_form_001", FormConstants.ANC_FORM_ID) }
    @Test fun `CHILDREN_UNDER_FIVE_ORS_FORM_ID is correct`() { assertEquals("ors_form_001", FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID) }
    @Test fun `PULSE_POLIO_CAMPAIGN_FORM_ID is correct`() { assertEquals("pulse_polio_campaign_form", FormConstants.PULSE_POLIO_CAMPAIGN_FORM_ID) }
    @Test fun `ORS_CAMPAIGN_FORM_ID is correct`() { assertEquals("ors_campaign_form", FormConstants.ORS_CAMPAIGN_FORM_ID) }
    @Test fun `CHILDREN_UNDER_FIVE_IFA_FORM_ID is correct`() { assertEquals("ifa_form_001", FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID) }
    @Test fun `CHILDREN_UNDER_FIVE_SAM_FORM_ID is correct`() { assertEquals("sam_visit_001", FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID) }
    @Test fun `ORS_FORM_NAME is correct`() { assertEquals("ors", FormConstants.ORS_FORM_NAME) }
    @Test fun `IFA_FORM_NAME is correct`() { assertEquals("ifa", FormConstants.IFA_FORM_NAME) }
    @Test fun `SAM_FORM_NAME is correct`() { assertEquals("sam", FormConstants.SAM_FORM_NAME) }
    @Test fun `IFA_DISTRIBUTION_FORM_ID is correct`() { assertEquals("ifa_distribution_form", FormConstants.IFA_DISTRIBUTION_FORM_ID) }
    @Test fun `EYE_SURGERY_FORM_NAME is correct`() { assertEquals("eye_surgery", FormConstants.EYE_SURGERY_FORM_NAME) }
    @Test fun `EYE_SURGERY_FORM_ID is correct`() { assertEquals("eye_checkup_form_001", FormConstants.EYE_SURGERY_FORM_ID) }
    @Test fun `HBYC_FORM_ID is correct`() { assertEquals("hbyc_form_001", FormConstants.HBYC_FORM_ID) }
    @Test fun `MOSQUITO_NET_FORM_ID is correct`() { assertEquals("mosquito_net_distribution_form", FormConstants.MOSQUITO_NET_FORM_ID) }
    @Test fun `MOSQUITO_NET_FORM_Name is correct`() { assertEquals("mobilizationMosquitoNet", FormConstants.MOSQUITO_NET_FORM_Name) }
    @Test fun `MDA_DISTRIBUTION_FORM_ID is correct`() { assertEquals("mda_distribution_form", FormConstants.MDA_DISTRIBUTION_FORM_ID) }
    @Test fun `MDA_DISTRIBUTION_FORM_NAME is correct`() { assertEquals("mda", FormConstants.MDA_DISTRIBUTION_FORM_NAME) }
    @Test fun `LF_MDA_CAMPAIGN is correct`() { assertEquals("LF_MDA_CAMPAIGN", FormConstants.LF_MDA_CAMPAIGN) }

    @Test fun `all form IDs are non-empty`() {
        val ids = listOf(
            FormConstants.HBNC_FORM_ID, FormConstants.CDTF_001, FormConstants.ANC_FORM_ID,
            FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID, FormConstants.PULSE_POLIO_CAMPAIGN_FORM_ID,
            FormConstants.ORS_CAMPAIGN_FORM_ID, FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID,
            FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID, FormConstants.HBYC_FORM_ID,
            FormConstants.MOSQUITO_NET_FORM_ID, FormConstants.MDA_DISTRIBUTION_FORM_ID
        )
        ids.forEach { assertTrue(it.isNotEmpty()) }
    }

    @Test fun `form IDs are unique`() {
        val ids = listOf(
            FormConstants.HBNC_FORM_ID, FormConstants.CDTF_001, FormConstants.ANC_FORM_ID,
            FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID, FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID,
            FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID, FormConstants.HBYC_FORM_ID,
            FormConstants.EYE_SURGERY_FORM_ID, FormConstants.MOSQUITO_NET_FORM_ID,
            FormConstants.MDA_DISTRIBUTION_FORM_ID, FormConstants.IFA_DISTRIBUTION_FORM_ID
        )
        assertEquals(ids.size, ids.toSet().size)
    }
}
