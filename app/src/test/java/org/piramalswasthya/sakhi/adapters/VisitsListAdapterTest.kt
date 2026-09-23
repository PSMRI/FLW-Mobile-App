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
import org.piramalswasthya.sakhi.model.MalariaScreeningCache

class VisitsListAdapterTest {

    private fun screening(id: Int = 1, caseStatus: String? = "Positive") = MalariaScreeningCache(
        id = id,
        benId = 1L,
        visitId = 1L,
        houseHoldDetailsId = 1L,
        caseStatus = caseStatus,
        caseDate = 0L,
        screeningDate = 0L,
        dateOfDeath = 0L,
        dateOfRdt = 0L,
        dateOfSlideTest = 0L,
        dateOfVisitBySupervisor = 0L,
        followUpDate = 0L
    )

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = VisitsListAdapter.DiffCallback
        assertTrue(callback.areItemsTheSame(screening(id = 1), screening(id = 1, caseStatus = "Other")))
        assertFalse(callback.areItemsTheSame(screening(id = 1), screening(id = 2)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = VisitsListAdapter.DiffCallback
        assertTrue(callback.areContentsTheSame(screening(id = 1), screening(id = 1)))
        assertFalse(callback.areContentsTheSame(screening(id = 1, caseStatus = "A"), screening(id = 1, caseStatus = "B")))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = VisitsListAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
