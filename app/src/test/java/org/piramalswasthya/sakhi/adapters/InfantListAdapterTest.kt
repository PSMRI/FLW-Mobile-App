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

class InfantListAdapterTest {

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
        syncState = SyncState.SYNCED,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenBasicDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.InfantListAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenBasicDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(benBasic(benId = 1L), benBasic(benId = 1L, hhId = 2L)))
        assertFalse(callback.areItemsTheSame(benBasic(benId = 1L), benBasic(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(benBasic(benId = 1L, hhId = 1L), benBasic(benId = 1L, hhId = 1L)))
        assertFalse(callback.areContentsTheSame(benBasic(benId = 1L, hhId = 1L), benBasic(benId = 1L, hhId = 2L)))
    }

    @Test
    fun clickListener_onClickedHbnc_invokesLambdaWithBenIdAndHhId() {
        var captured: Pair<Long, Long>? = null
        val listener = InfantListAdapter.InfantListClickListener { benId, hhId -> captured = benId to hhId }
        listener.onClickedHbnc(benBasic(benId = 5L, hhId = 9L))
        assertEquals(5L to 9L, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = InfantListAdapter(InfantListAdapter.InfantListClickListener { _, _ -> })
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
