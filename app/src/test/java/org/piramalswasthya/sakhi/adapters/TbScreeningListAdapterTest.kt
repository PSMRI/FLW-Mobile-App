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
import org.piramalswasthya.sakhi.model.BenWithTbScreeningDomain

class TbScreeningListAdapterTest {

    private fun benBasic(benId: Long = 1L, hhId: Long = 1L) = BenBasicDomain(
        benId = benId,
        hhId = hhId,
        reproductiveStatusId = 0,
        regDate = "01-01-2020",
        benName = "Test",
        gender = "MALE",
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

    private fun item(benId: Long = 1L, hhId: Long = 1L) =
        BenWithTbScreeningDomain(ben = benBasic(benId, hhId), tb = null)

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithTbScreeningDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.TbScreeningListAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithTbScreeningDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(item(benId = 1L), item(benId = 1L)))
        assertFalse(callback.areItemsTheSame(item(benId = 1L), item(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(item(benId = 1L), item(benId = 1L)))
        assertFalse(callback.areContentsTheSame(item(benId = 1L, hhId = 1L), item(benId = 1L, hhId = 2L)))
    }

    @Test
    fun clickListener_invokesLambdaWithHhIdAndBenId() {
        var capturedHhId: Long? = null
        var capturedBenId: Long? = null
        val listener = TbScreeningListAdapter.ClickListener { hhId, benId ->
            capturedHhId = hhId
            capturedBenId = benId
        }
        listener.onClickForm(item(benId = 5L, hhId = 9L))
        assertEquals(9L, capturedHhId)
        assertEquals(5L, capturedBenId)
    }

    @Test
    fun clickListener_withNullLambda_doesNotThrow() {
        val listener = TbScreeningListAdapter.ClickListener()
        listener.onClickForm(item())
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = TbScreeningListAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
