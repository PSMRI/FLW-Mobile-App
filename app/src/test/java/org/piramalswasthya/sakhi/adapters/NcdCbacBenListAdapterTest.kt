package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithCbacDomain
import org.piramalswasthya.sakhi.model.CbacCache

class NcdCbacBenListAdapterTest {

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

    private fun cbacCache(benId: Long = 1L, fillDate: Long = 0L) = CbacCache(
        benId = benId,
        ashaId = 1,
        fillDate = fillDate,
        syncState = SyncState.SYNCED
    )

    private fun benWithCbac(benId: Long = 1L, records: List<CbacCache> = emptyList()) =
        BenWithCbacDomain(benBasic(benId = benId), records)

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithCbacDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.NcdCbacBenListAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithCbacDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        val old = benWithCbac(benId = 1L)
        val same = benWithCbac(benId = 1L)
        val different = benWithCbac(benId = 2L)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = benWithCbac(benId = 1L)
        val same = benWithCbac(benId = 1L)
        val different = benWithCbac(benId = 1L, records = listOf(cbacCache(benId = 1L)))
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClickedView_invokesLambdaWithBenId() {
        var captured: Long? = null
        val listener = NcdCbacBenListAdapter.CbacFormClickListener(
            clickedView = { benId -> captured = benId },
            clickedNew = { _, _ -> }
        )
        listener.onClickedView(benWithCbac(benId = 8L))
        assertEquals(8L, captured)
    }

    @Test
    fun clickListener_onClickedNew_withNoSavedRecords_passesNullDueDate() {
        var capturedBenId: Long? = null
        var capturedDueDate: Long? = -1L
        val listener = NcdCbacBenListAdapter.CbacFormClickListener(
            clickedView = {},
            clickedNew = { benId, dueDate ->
                capturedBenId = benId
                capturedDueDate = dueDate
            }
        )
        listener.onClickedNew(benWithCbac(benId = 3L, records = emptyList()))
        assertEquals(3L, capturedBenId)
        assertNull(capturedDueDate)
    }

    @Test
    fun clickListener_onClickedNew_withRecentFiling_passesFutureDueDate() {
        var capturedDueDate: Long? = null
        val recentFillDate = System.currentTimeMillis()
        val listener = NcdCbacBenListAdapter.CbacFormClickListener(
            clickedView = {},
            clickedNew = { _, dueDate -> capturedDueDate = dueDate }
        )
        listener.onClickedNew(benWithCbac(benId = 3L, records = listOf(cbacCache(fillDate = recentFillDate))))
        assertEquals(recentFillDate + Konstants.minMillisBwtweenCbacFiling, capturedDueDate)
    }

    @Test
    fun clickListener_onClickedNew_withExpiredFiling_passesNullDueDate() {
        var capturedDueDate: Long? = -1L
        val oldFillDate = System.currentTimeMillis() - Konstants.minMillisBwtweenCbacFiling - 1000L
        val listener = NcdCbacBenListAdapter.CbacFormClickListener(
            clickedView = {},
            clickedNew = { _, dueDate -> capturedDueDate = dueDate }
        )
        listener.onClickedNew(benWithCbac(benId = 3L, records = listOf(cbacCache(fillDate = oldFillDate))))
        assertNull(capturedDueDate)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = NcdCbacBenListAdapter(NcdCbacBenListAdapter.CbacFormClickListener({}, { _, _ -> }))
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
