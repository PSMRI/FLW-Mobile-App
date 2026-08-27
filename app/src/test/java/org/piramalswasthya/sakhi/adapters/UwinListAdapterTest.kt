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
import org.piramalswasthya.sakhi.model.UwinCache

class UwinListAdapterTest {

    private fun uwin(id: Int = 1, participantsCount: Int = 5) = UwinCache(
        id = id,
        sessionDate = 0L,
        place = "Place",
        participantsCount = participantsCount,
        createdBy = "asha",
        updatedBy = "asha",
        syncState = SyncState.UNSYNCED
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<UwinCache> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.UwinListAdapter\$UwinDiffUtilCallback")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<UwinCache>
    }

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(uwin(id = 1), uwin(id = 1, participantsCount = 9)))
        assertFalse(callback.areItemsTheSame(uwin(id = 1), uwin(id = 2)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(uwin(id = 1), uwin(id = 1)))
        assertFalse(callback.areContentsTheSame(uwin(id = 1, participantsCount = 5), uwin(id = 1, participantsCount = 6)))
    }

    @Test
    fun clickListener_invokesLambdaWithId() {
        var captured: Int? = null
        val listener = UwinListAdapter.UwinClickListener { id -> captured = id }
        listener.onClick(42)
        assertEquals(42, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = UwinListAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
