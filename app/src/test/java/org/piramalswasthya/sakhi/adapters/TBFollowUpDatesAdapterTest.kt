package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache

class TBFollowUpDatesAdapterTest {

    private fun confirmed(id: Int = 1, followUpDate: Long? = null, timestamp: Long = 1_000_000L) = TBConfirmedTreatmentCache(
        id = id,
        benId = 1L,
        followUpDate = followUpDate,
        treatmentStartDate = timestamp,
        createdAt = timestamp,
        updatedAt = timestamp
    )

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = TBFollowUpDatesAdapter.DiffCallback
        assertTrue(callback.areItemsTheSame(confirmed(id = 1), confirmed(id = 1, followUpDate = 123L)))
        assertFalse(callback.areItemsTheSame(confirmed(id = 1), confirmed(id = 2)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = TBFollowUpDatesAdapter.DiffCallback
        assertTrue(callback.areContentsTheSame(confirmed(id = 1, followUpDate = 123L), confirmed(id = 1, followUpDate = 123L)))
        assertFalse(callback.areContentsTheSame(confirmed(id = 1, followUpDate = 123L), confirmed(id = 1, followUpDate = 456L)))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = TBFollowUpDatesAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
