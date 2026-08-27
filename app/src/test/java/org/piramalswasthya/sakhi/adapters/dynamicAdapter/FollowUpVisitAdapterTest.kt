package org.piramalswasthya.sakhi.adapters.dynamicAdapter

import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FollowUpVisitAdapter.FollowUpVisitItem

class FollowUpVisitAdapterTest {

    private val callback = FollowUpVisitAdapter.Companion.DiffCallback

    @Test
    fun areItemsTheSame_trueForTwoHeaders() {
        assertTrue(callback.areItemsTheSame(FollowUpVisitItem.Header, FollowUpVisitItem.Header))
    }

    @Test
    fun areItemsTheSame_comparesVisitDatesBySnoAndDate() {
        val old = FollowUpVisitItem.VisitDate(sno = "1", date = "01-01-2024")
        val same = FollowUpVisitItem.VisitDate(sno = "1", date = "01-01-2024")
        val different = FollowUpVisitItem.VisitDate(sno = "2", date = "01-01-2024")
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areItemsTheSame_falseWhenTypesDiffer() {
        val visitDate = FollowUpVisitItem.VisitDate(sno = "1", date = "01-01-2024")
        assertFalse(callback.areItemsTheSame(FollowUpVisitItem.Header, visitDate))
        assertFalse(callback.areItemsTheSame(visitDate, FollowUpVisitItem.Header))
    }

    @Test
    fun areContentsTheSame_matchesAreItemsTheSameLogic() {
        val old = FollowUpVisitItem.VisitDate(sno = "1", date = "01-01-2024")
        val different = FollowUpVisitItem.VisitDate(sno = "1", date = "02-01-2024")
        assertTrue(callback.areContentsTheSame(FollowUpVisitItem.Header, FollowUpVisitItem.Header))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = FollowUpVisitAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
