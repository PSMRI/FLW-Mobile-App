package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import androidx.fragment.app.FragmentActivity
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

class BenListAdapterTest {

    private fun benBasic(benId: Long = 1L, hhId: Long = 1L, relToHeadId: Int = 1) = BenBasicDomain(
        benId = benId,
        hhId = hhId,
        reproductiveStatusId = 0,
        regDate = "01-01-2020",
        benName = "Test",
        gender = "MALE",
        dob = 0L,
        relToHeadId = relToHeadId,
        mobileNo = "9999999999",
        familyHeadName = "Head",
        syncState = null,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    private fun diffCallback(): DiffUtil.ItemCallback<BenBasicDomain> = BenListAdapter.BenDiffUtilCallBack

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
        val different = benBasic(benId = 1L, relToHeadId = 5)
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClickedBen_passesRelToHeadIdMinusOne() {
        var capturedHhId: Long? = null
        var capturedBenId: Long? = null
        var capturedRelToHeadId: Int? = null
        val listener = BenListAdapter.BenClickListener(
            clickedBen = { _, hhId, benId, relToHeadId ->
                capturedHhId = hhId
                capturedBenId = benId
                capturedRelToHeadId = relToHeadId
            },
            clickedWifeBen = { _, _, _, _ -> },
            clickedHusbandBen = { _, _, _, _ -> },
            clickedChildben = { _, _, _, _ -> },
            clickedHousehold = { _, _ -> },
            clickedABHA = { _, _, _ -> },
            clickedAddAllBenBtn = { _, _, _, _, _ -> },
            callBen = { },
            softDeleteBen = { }
        )
        listener.onClickedBen(benBasic(benId = 3L, hhId = 4L, relToHeadId = 6))
        assertEquals(4L, capturedHhId)
        assertEquals(3L, capturedBenId)
        assertEquals(5, capturedRelToHeadId)
    }

    @Test
    fun clickListener_onClickedWifeBen_passesRelToHeadIdAsIs() {
        var capturedRelToHeadId: Int? = null
        val listener = BenListAdapter.BenClickListener(
            clickedBen = { _, _, _, _ -> },
            clickedWifeBen = { _, _, _, relToHeadId -> capturedRelToHeadId = relToHeadId },
            clickedHusbandBen = { _, _, _, _ -> },
            clickedChildben = { _, _, _, _ -> },
            clickedHousehold = { _, _ -> },
            clickedABHA = { _, _, _ -> },
            clickedAddAllBenBtn = { _, _, _, _, _ -> },
            callBen = { },
            softDeleteBen = { }
        )
        listener.onClickedWifeBen(benBasic(relToHeadId = 6))
        assertEquals(6, capturedRelToHeadId)
    }

    @Test
    fun clickListener_clickedAddAllBenBtn_passesDerivedIds() {
        var received: Triple<Long, Long, Boolean>? = null
        var isIfaReceived = false
        val listener = BenListAdapter.BenClickListener(
            clickedBen = { _, _, _, _ -> },
            clickedWifeBen = { _, _, _, _ -> },
            clickedHusbandBen = { _, _, _, _ -> },
            clickedChildben = { _, _, _, _ -> },
            clickedHousehold = { _, _ -> },
            clickedABHA = { _, _, _ -> },
            clickedAddAllBenBtn = { _, benId, hhId, isMatched, isIfa ->
                received = Triple(benId, hhId, isMatched)
                isIfaReceived = isIfa
            },
            callBen = { },
            softDeleteBen = { }
        )
        val item = benBasic(benId = 9L, hhId = 10L)
        listener.clickedAddAllBenBtn(item, isMatched = true, isIFA = true)
        assertEquals(Triple(9L, 10L, true), received)
        assertTrue(isIfaReceived)
    }

    @Test
    fun submitBenIds_notifiesOnlyChangedItems() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val activity = mockk<FragmentActivity>(relaxed = true)
            val adapter = BenListAdapter(context = activity)
            adapter.submitBenIds(listOf(1L, 2L))
            adapter.submitBenIds(listOf(1L, 2L))
        } finally {
            unmockkStatic(Looper::class)
        }
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val activity = mockk<FragmentActivity>(relaxed = true)
            val adapter = BenListAdapter(context = activity)
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
