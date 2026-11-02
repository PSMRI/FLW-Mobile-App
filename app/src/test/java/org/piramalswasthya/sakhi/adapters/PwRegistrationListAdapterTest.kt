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
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithPwrDomain

class PwRegistrationListAdapterTest {

    private fun benBasic(benId: Long = 1L, hhId: Long = 1L) = BenBasicDomain(
        benId = benId,
        hhId = hhId,
        reproductiveStatusId = 0,
        regDate = "01-01-2020",
        benName = "Test",
        gender = "FEMALE",
        dob = 0L,
        relToHeadId = 1,
        mobileNo = "9999999999",
        familyHeadName = "Head",
        syncState = null,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    private fun benWithPwr(benId: Long = 1L, hhId: Long = 1L) = BenWithPwrDomain(
        ben = benBasic(benId = benId, hhId = hhId),
        pwr = null
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithPwrDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.PwRegistrationListAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithPwrDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(benWithPwr(benId = 1L), benWithPwr(benId = 1L, hhId = 2L)))
        assertFalse(callback.areItemsTheSame(benWithPwr(benId = 1L), benWithPwr(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(benWithPwr(benId = 1L, hhId = 1L), benWithPwr(benId = 1L, hhId = 1L)))
        assertFalse(callback.areContentsTheSame(benWithPwr(benId = 1L, hhId = 1L), benWithPwr(benId = 1L, hhId = 2L)))
    }

    @Test
    fun clickListener_onClickedBen_invokesLambdaWithBenId() {
        var captured: Long? = null
        val listener = PwRegistrationListAdapter.ClickListener(clickedBen = { benId -> captured = benId })
        listener.onClickedBen(benWithPwr(benId = 4L))
        assertEquals(4L, captured)
    }

    @Test
    fun clickListener_onClickForm_invokesLambdaWithHhIdAndBenId() {
        var capturedHhId: Long? = null
        var capturedBenId: Long? = null
        val listener = PwRegistrationListAdapter.ClickListener(
            clickedBen = {},
            clickedForm = { hhId, benId ->
                capturedHhId = hhId
                capturedBenId = benId
            }
        )
        listener.onClickForm(benWithPwr(benId = 5L, hhId = 9L))
        assertEquals(9L, capturedHhId)
        assertEquals(5L, capturedBenId)
    }

    @Test
    fun clickListener_onClickForm_withNullLambda_doesNotThrow() {
        val listener = PwRegistrationListAdapter.ClickListener(clickedBen = {})
        listener.onClickForm(benWithPwr(benId = 5L, hhId = 9L))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = PwRegistrationListAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
