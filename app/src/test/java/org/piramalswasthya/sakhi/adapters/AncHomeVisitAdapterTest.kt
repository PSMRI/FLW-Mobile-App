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
import org.piramalswasthya.sakhi.model.HomeVisitDomain

class AncHomeVisitAdapterTest {

    private fun visit(id: Int = 1, benId: Long = 1L) = HomeVisitDomain(
        id = id,
        benId = benId,
        visitNumber = 1,
        visitDate = 0L,
        visitDateString = "2024-01-01",
        formDataJson = "{}",
        syncState = SyncState.SYNCED,
        isSynced = true
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<HomeVisitDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.AncHomeVisitAdapter\$MyDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<HomeVisitDomain>
    }

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(visit(id = 1), visit(id = 1, benId = 9L)))
        assertFalse(callback.areItemsTheSame(visit(id = 1), visit(id = 2)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = visit(id = 1)
        val same = visit(id = 1)
        val different = visit(id = 1, benId = 5L)
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onViewClick_invokesLambdaWithItem() {
        var captured: HomeVisitDomain? = null
        val listener = AncHomeVisitAdapter.HomeVisitClickListener { item -> captured = item }
        val item = visit(id = 3)
        listener.onViewClick(item)
        assertEquals(item, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = AncHomeVisitAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
