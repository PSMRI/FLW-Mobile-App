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
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithHRNPTListDomain

class HRNPTListAdapterTest {

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
        syncState = SyncState.SYNCED,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    private fun ben(benId: Long = 1L, hhId: Long = 1L) =
        BenWithHRNPTListDomain(benBasic(benId, hhId), emptyList())

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithHRNPTListDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.HRNPTListAdapter\$MyDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithHRNPTListDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        val old = ben(benId = 1L)
        val same = ben(benId = 1L, hhId = 2L)
        val different = ben(benId = 2L)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = ben(benId = 1L, hhId = 1L)
        val same = ben(benId = 1L, hhId = 1L)
        val different = ben(benId = 1L, hhId = 2L)
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClickForm1_invokesLambdaWhenPresent() {
        var captured: Pair<Long, Long>? = null
        val listener = HRNPTListAdapter.HRNPTClickListener(
            clickedBen = { },
            clickedForm1 = { hhId, benId -> captured = hhId to benId }
        )
        listener.onClickForm1(ben(benId = 3L, hhId = 5L))
        assertEquals(5L to 3L, captured)
    }

    @Test
    fun clickListener_onClickForm2_doesNotThrowWhenNull() {
        val listener = HRNPTListAdapter.HRNPTClickListener(clickedBen = { })
        listener.onClickForm2(ben())
    }

    @Test
    fun clickListener_onClickForm3_invokesLambdaWhenPresent() {
        var captured: Pair<Long, Long>? = null
        val listener = HRNPTListAdapter.HRNPTClickListener(
            clickedBen = { },
            clickedForm3 = { hhId, benId -> captured = hhId to benId }
        )
        listener.onClickForm3(ben(benId = 8L, hhId = 9L))
        assertEquals(9L to 8L, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = HRNPTListAdapter(HRNPTListAdapter.HRNPTClickListener(clickedBen = { }))
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
