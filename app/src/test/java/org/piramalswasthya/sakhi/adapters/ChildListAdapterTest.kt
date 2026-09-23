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

class ChildListAdapterTest {

    private fun benBasic(benId: Long = 1L, hhId: Long = 1L, dob: Long = 0L) = BenBasicDomain(
        benId = benId,
        hhId = hhId,
        reproductiveStatusId = 0,
        regDate = "01-01-2020",
        benName = "Test",
        gender = "MALE",
        dob = dob,
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
    private fun diffCallback(): DiffUtil.ItemCallback<BenBasicDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.ChildListAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenBasicDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(benBasic(benId = 1L), benBasic(benId = 1L)))
        assertFalse(callback.areItemsTheSame(benBasic(benId = 1L), benBasic(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = benBasic(benId = 1L)
        val same = benBasic(benId = 1L)
        val different = benBasic(benId = 1L, dob = 100L)
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClickedHbyc_invokesLambdaWithDerivedArgs() {
        var received: Triple<Long, Long, Long>? = null
        val listener = ChildListAdapter.ChildListClickListener { benId, hhId, dob ->
            received = Triple(benId, hhId, dob)
        }
        listener.onClickedHbyc(benBasic(benId = 3L, hhId = 4L, dob = 5L))
        assertEquals(Triple(3L, 4L, 5L), received)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = ChildListAdapter(ChildListAdapter.ChildListClickListener { _, _, _ -> })
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
