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
import org.piramalswasthya.sakhi.model.BenWithEctListDomain

class ECTrackingListAdapterTest {

    private fun benBasic(benId: Long = 1L) = BenBasicDomain(
        benId = benId,
        hhId = 1L,
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

    private fun ben(benId: Long = 1L, numChildren: String = "0", allowFill: Boolean = true) =
        BenWithEctListDomain(
            ben = benBasic(benId),
            numChildren = numChildren,
            allowFill = allowFill,
            ectDate = 0L,
            lmpDate = 0L,
            savedECTRecords = emptyList()
        )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithEctListDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.ECTrackingListAdapter\$MyDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithEctListDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenBenId() {
        val callback = diffCallback()
        val old = ben(benId = 1L)
        val same = ben(benId = 1L, numChildren = "2")
        val different = ben(benId = 2L)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = ben(benId = 1L, numChildren = "1")
        val same = ben(benId = 1L, numChildren = "1")
        val different = ben(benId = 1L, numChildren = "2")
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClickedAdd_invokesLambdaWithBenIdAndAllowFill() {
        var capturedBenId: Long? = null
        var capturedAllow: Boolean? = null
        val listener = ECTrackingListAdapter.ECTrackListClickListener(
            addNewTrack = { benId, allow ->
                capturedBenId = benId
                capturedAllow = allow
            },
            showAllTracks = { }
        )
        listener.onClickedAdd(ben(benId = 6L, allowFill = false))
        assertEquals(6L, capturedBenId)
        assertEquals(false, capturedAllow)
    }

    @Test
    fun clickListener_onClickedShowAllTracks_invokesLambdaWithBenId() {
        var captured: Long? = null
        val listener = ECTrackingListAdapter.ECTrackListClickListener(
            addNewTrack = { _, _ -> },
            showAllTracks = { benId -> captured = benId }
        )
        listener.onClickedShowAllTracks(ben(benId = 8L))
        assertEquals(8L, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = ECTrackingListAdapter(
                ECTrackingListAdapter.ECTrackListClickListener(
                    addNewTrack = { _, _ -> },
                    showAllTracks = { }
                )
            )
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
