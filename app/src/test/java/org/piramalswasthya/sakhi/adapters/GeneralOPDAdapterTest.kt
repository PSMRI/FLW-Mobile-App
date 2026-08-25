package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.GeneralOPEDBeneficiary

class GeneralOPDAdapterTest {

    private fun beneficiary(
        beneficiaryId: Long = 1L,
        preferredPhoneNum: String? = "9999999999",
        benName: String? = "Test"
    ) = GeneralOPEDBeneficiary(
        benFlowID = null,
        beneficiaryRegID = null,
        benVisitID = null,
        visitCode = null,
        benVisitNo = null,
        nurseFlag = null,
        doctorFlag = null,
        pharmacist_flag = null,
        lab_technician_flag = null,
        radiologist_flag = null,
        oncologist_flag = null,
        specialist_flag = null,
        agentId = null,
        visitDate = null,
        modified_by = null,
        modified_date = null,
        benName = benName,
        deleted = null,
        firstName = null,
        lastName = null,
        age = null,
        ben_age_val = null,
        genderID = null,
        genderName = null,
        preferredPhoneNum = preferredPhoneNum,
        fatherName = null,
        spouseName = null,
        districtName = null,
        servicePointName = null,
        registrationDate = null,
        benVisitDate = null,
        consultationDate = null,
        consultantID = null,
        consultantName = null,
        visitSession = null,
        servicePointID = null,
        districtID = null,
        villageID = null,
        vanID = null,
        beneficiaryId = beneficiaryId,
        dob = null,
        tc_SpecialistLabFlag = null,
        visitReason = null,
        village = null,
        visitCategory = null
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<GeneralOPEDBeneficiary> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.GeneralOPDAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<GeneralOPEDBeneficiary>
    }

    @Test
    fun areItemsTheSame_comparesByBeneficiaryId() {
        val callback = diffCallback()
        val old = beneficiary(beneficiaryId = 1L)
        val same = beneficiary(beneficiaryId = 1L, benName = "Different")
        val different = beneficiary(beneficiaryId = 2L)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = beneficiary(beneficiaryId = 1L, benName = "Test")
        val same = beneficiary(beneficiaryId = 1L, benName = "Test")
        val different = beneficiary(beneficiaryId = 1L, benName = "Other")
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun callClickListener_invokesLambdaWithBenIdAndMobile() {
        var capturedBenId: Long? = null
        var capturedMobile: String? = null
        val listener = GeneralOPDAdapter.CallClickListener { benId, mobileno ->
            capturedBenId = benId
            capturedMobile = mobileno
        }
        listener.onClickCall(beneficiary(beneficiaryId = 4L, preferredPhoneNum = "8888888888"))
        assertEquals(4L, capturedBenId)
        assertEquals("8888888888", capturedMobile)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = GeneralOPDAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
