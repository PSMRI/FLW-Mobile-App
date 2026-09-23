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
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.CbacDomain

class NcdCbacAdapterTest {

    private fun cbac(cbacId: Int = 1, date: String = "01-01-2024") =
        CbacDomain(cbacId = cbacId, date = date, syncState = SyncState.UNSYNCED)

    @Test
    fun areItemsTheSame_comparesByCbacId() {
        val callback = NcdCbacAdapter.ImmunizationIconDiffCallback
        val old = cbac(cbacId = 1)
        val same = cbac(cbacId = 1, date = "02-02-2024")
        val different = cbac(cbacId = 2)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = NcdCbacAdapter.ImmunizationIconDiffCallback
        val old = cbac(cbacId = 1, date = "01-01-2024")
        val same = cbac(cbacId = 1, date = "01-01-2024")
        val different = cbac(cbacId = 1, date = "02-02-2024")
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClicked_invokesLambdaWithCbacId() {
        var captured: Int? = null
        val listener = NcdCbacAdapter.NcdCbacElementClickListener { cbacId -> captured = cbacId }
        listener.onClicked(cbac(cbacId = 7))
        assertEquals(7, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = NcdCbacAdapter(NcdCbacAdapter.NcdCbacElementClickListener {})
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
