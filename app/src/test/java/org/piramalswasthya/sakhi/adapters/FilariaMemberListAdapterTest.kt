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
import org.piramalswasthya.sakhi.model.BenWithFilariaScreeningDomain
import org.piramalswasthya.sakhi.model.FilariaScreeningCache

class FilariaMemberListAdapterTest {

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
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithFilariaScreeningDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.FilariaMemberListAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithFilariaScreeningDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        val old = BenWithFilariaScreeningDomain(benBasic(benId = 1L), null)
        val same = BenWithFilariaScreeningDomain(benBasic(benId = 1L), null)
        val different = BenWithFilariaScreeningDomain(benBasic(benId = 2L), null)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = BenWithFilariaScreeningDomain(benBasic(benId = 1L), null)
        val same = BenWithFilariaScreeningDomain(benBasic(benId = 1L), null)
        val different = BenWithFilariaScreeningDomain(
            benBasic(benId = 1L),
            FilariaScreeningCache(benId = 1L, houseHoldDetailsId = 1L)
        )
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_invokesLambdaWithHhIdAndBenId() {
        var capturedHhId: Long? = null
        var capturedBenId: Long? = null
        val listener = FilariaMemberListAdapter.ClickListener { hhId, benId ->
            capturedHhId = hhId
            capturedBenId = benId
        }
        val item = BenWithFilariaScreeningDomain(benBasic(benId = 5L, hhId = 9L), null)
        listener.onClickForm(item)
        assertEquals(9L, capturedHhId)
        assertEquals(5L, capturedBenId)
    }

    @Test
    fun clickListener_withNullLambda_doesNotThrow() {
        val listener = FilariaMemberListAdapter.ClickListener()
        listener.onClickForm(BenWithFilariaScreeningDomain(benBasic(), null))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = FilariaMemberListAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
