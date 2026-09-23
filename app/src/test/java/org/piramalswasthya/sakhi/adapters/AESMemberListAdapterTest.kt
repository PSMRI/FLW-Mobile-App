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
import org.piramalswasthya.sakhi.model.AESScreeningCache
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithAESScreeningDomain

class AESMemberListAdapterTest {

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

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithAESScreeningDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.AESMemberListAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithAESScreeningDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        val old = BenWithAESScreeningDomain(benBasic(benId = 1L), null)
        val new = BenWithAESScreeningDomain(benBasic(benId = 1L), null)
        val different = BenWithAESScreeningDomain(benBasic(benId = 2L), null)
        assertTrue(callback.areItemsTheSame(old, new))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_comparesFullEquality() {
        val callback = diffCallback()
        val old = BenWithAESScreeningDomain(benBasic(benId = 1L), null)
        val same = BenWithAESScreeningDomain(benBasic(benId = 1L), null)
        assertTrue(callback.areContentsTheSame(old, same))
        val differentAes = BenWithAESScreeningDomain(benBasic(benId = 1L), mockAes())
        assertFalse(callback.areContentsTheSame(old, differentAes))
    }

    private fun mockAes(): AESScreeningCache = AESScreeningCache(benId = 1L, houseHoldDetailsId = 1L)

    @Test
    fun clickListener_invokesLambdaWithHhIdAndBenId() {
        var capturedHhId: Long? = null
        var capturedBenId: Long? = null
        val listener = AESMemberListAdapter.ClickListener { hhId, benId ->
            capturedHhId = hhId
            capturedBenId = benId
        }
        val item = BenWithAESScreeningDomain(benBasic(benId = 5L, hhId = 9L), null)
        listener.onClickForm(item)
        assertEquals(9L, capturedHhId)
        assertEquals(5L, capturedBenId)
    }

    @Test
    fun clickListener_withNullLambda_doesNotThrow() {
        val listener = AESMemberListAdapter.ClickListener()
        val item = BenWithAESScreeningDomain(benBasic(), null)
        listener.onClickForm(item)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = AESMemberListAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
