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
import org.piramalswasthya.sakhi.model.BenWithCbacReferDomain
import org.piramalswasthya.sakhi.model.ReferalCache

class NcdReferListAdapterTest {

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

    private fun referalCache(benId: Long = 1L) = ReferalCache(
        benId = benId,
        syncState = SyncState.UNSYNCED
    )

    private fun benWithCbacRefer(benId: Long = 1L, records: List<org.piramalswasthya.sakhi.model.CbacCache> = emptyList()) =
        BenWithCbacReferDomain(benBasic(benId = benId), records, referalCache(benId = benId))

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithCbacReferDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.NcdReferListAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithCbacReferDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        val old = benWithCbacRefer(benId = 1L)
        val same = benWithCbacRefer(benId = 1L)
        val different = benWithCbacRefer(benId = 2L)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = benWithCbacRefer(benId = 1L)
        val same = benWithCbacRefer(benId = 1L)
        val different = benWithCbacRefer(benId = 1L, records = listOf(
            org.piramalswasthya.sakhi.model.CbacCache(benId = 1L, ashaId = 1, fillDate = 1L, syncState = SyncState.SYNCED)
        ))
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_invokesLambdaWithBenIdAndHhId() {
        var capturedBenId: Long? = null
        var capturedHhId: Long? = null
        val listener = NcdReferListAdapter.NcdReferallickListener { benId, hhId ->
            capturedBenId = benId
            capturedHhId = hhId
        }
        listener.onClickedFollowUp(benBasic(benId = 7L, hhId = 11L))
        assertEquals(7L, capturedBenId)
        assertEquals(11L, capturedHhId)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = NcdReferListAdapter(
                userName = "ASHA",
                listener = NcdReferListAdapter.NcdReferallickListener { _, _ -> },
                visible = true
            )
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
