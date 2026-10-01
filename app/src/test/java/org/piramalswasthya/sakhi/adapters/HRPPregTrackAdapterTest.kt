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
import org.piramalswasthya.sakhi.model.HRPPregnantTrackDomain

class HRPPregTrackAdapterTest {

    private fun track(id: Int = 1, dateOfVisit: String? = "01-01-2024") = HRPPregnantTrackDomain(
        id = id,
        dateOfVisit = dateOfVisit,
        filledOnString = "filled",
        syncState = SyncState.SYNCED
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<HRPPregnantTrackDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.HRPPregTrackAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<HRPPregnantTrackDomain>
    }

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(track(id = 1), track(id = 1, dateOfVisit = "different")))
        assertFalse(callback.areItemsTheSame(track(id = 1), track(id = 2)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(track(id = 1), track(id = 1)))
        assertFalse(callback.areContentsTheSame(track(id = 1, dateOfVisit = "a"), track(id = 1, dateOfVisit = "b")))
    }

    @Test
    fun clickListener_onClickedBen_invokesLambdaWithTrackId() {
        var captured: Int? = null
        val listener = HRPPregTrackAdapter.HRPTrackClickListener { trackId -> captured = trackId }
        listener.onClickedBen(track(id = 42))
        assertEquals(42, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = HRPPregTrackAdapter(visit = "visit1")
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
