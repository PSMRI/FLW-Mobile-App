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
import org.piramalswasthya.sakhi.model.ECTDomain

class ECTrackingAdapterTest {

    private fun ect(created: Long = 1L, benId: Long = 1L) = ECTDomain(
        benId = benId,
        created = created,
        visited = 0L,
        filledOnString = "01-01-2024",
        syncState = SyncState.SYNCED
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<ECTDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.ECTrackingAdapter\$MyDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<ECTDomain>
    }

    @Test
    fun areItemsTheSame_comparesByCreated() {
        val callback = diffCallback()
        val old = ect(created = 1L)
        val same = ect(created = 1L, benId = 2L)
        val different = ect(created = 2L)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = ect(created = 1L, benId = 1L)
        val same = ect(created = 1L, benId = 1L)
        val different = ect(created = 1L, benId = 3L)
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClickedVisit_invokesLambdaWithBenIdAndCreated() {
        var capturedBenId: Long? = null
        var capturedCreated: Long? = null
        val listener = ECTrackingAdapter.ECTrackViewClickListener { benId, created ->
            capturedBenId = benId
            capturedCreated = created
        }
        listener.onClickedVisit(ect(created = 7L, benId = 4L))
        assertEquals(4L, capturedBenId)
        assertEquals(7L, capturedCreated)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = ECTrackingAdapter(ECTrackingAdapter.ECTrackViewClickListener { _, _ -> })
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
