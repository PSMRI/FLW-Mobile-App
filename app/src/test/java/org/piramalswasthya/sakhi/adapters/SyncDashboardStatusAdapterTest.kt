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
import org.piramalswasthya.sakhi.model.SyncStatusDomain

class SyncDashboardStatusAdapterTest {

    private fun status(name: String = "Household", synced: Int = 1, notSynced: Int = 0, syncing: Int = 0) =
        SyncStatusDomain(name = name, synced = synced, notSynced = notSynced, syncing = syncing)

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<SyncStatusDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.SyncDashboardStatusAdapter\$DiffCallback")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<SyncStatusDomain>
    }

    @Test
    fun areItemsTheSame_comparesByName() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(status(name = "Household"), status(name = "Household", synced = 5)))
        assertFalse(callback.areItemsTheSame(status(name = "Household"), status(name = "Beneficiary")))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(status(synced = 5), status(synced = 5)))
        assertFalse(callback.areContentsTheSame(status(synced = 5), status(synced = 6)))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = SyncDashboardStatusAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
