package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import androidx.navigation.NavDirections
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.HbncIcon

class HBNCDayGridAdapterTest {

    private fun icon(count: Int = 1, isFilled: Boolean = false) = HbncIcon(
        hhId = 1L,
        benId = 1L,
        count = count,
        isFilled = isFilled,
        syncState = SyncState.SYNCED,
        destination = mockk<NavDirections>()
    )

    @Test
    fun areItemsTheSame_comparesByCount() {
        val callback = HBNCDayGridAdapter.HbncIconDiffCallback
        val old = icon(count = 1)
        val same = icon(count = 1, isFilled = true)
        val different = icon(count = 2)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = HBNCDayGridAdapter.HbncIconDiffCallback
        val old = icon(count = 1, isFilled = true)
        val same = old
        val different = icon(count = 1, isFilled = false)
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun hbncIconClickListener_invokesLambdaWithDestination() {
        var captured: NavDirections? = null
        val listener = HBNCDayGridAdapter.HbncIconClickListener { destination -> captured = destination }
        val item = icon()
        listener.onClicked(item)
        assertEquals(item.destination, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = HBNCDayGridAdapter(HBNCDayGridAdapter.HbncIconClickListener { })
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
