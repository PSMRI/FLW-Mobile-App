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
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache

class FollowUpDatesAdapterTest {

    private fun followUp(id: Int = 1, benId: Long = 1L, visitNumber: Int = 1) = LeprosyFollowUpCache(
        id = id,
        benId = benId,
        visitNumber = visitNumber,
        createdBy = "test",
        modifiedBy = "test"
    )

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = FollowUpDatesAdapter.DiffCallback
        val old = followUp(id = 1, benId = 1L)
        val same = followUp(id = 1, benId = 2L)
        val different = followUp(id = 2, benId = 1L)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = FollowUpDatesAdapter.DiffCallback
        val old = followUp(id = 1, visitNumber = 1)
        val same = followUp(id = 1, visitNumber = 1)
        val different = followUp(id = 1, visitNumber = 2)
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = FollowUpDatesAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
