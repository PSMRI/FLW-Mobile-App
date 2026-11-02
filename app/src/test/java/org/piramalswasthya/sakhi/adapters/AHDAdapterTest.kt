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
import org.piramalswasthya.sakhi.model.AHDCache

class AHDAdapterTest {

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<AHDCache> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.AHDAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<AHDCache>
    }

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = diffCallback()
        val old = AHDCache(id = 1)
        val same = AHDCache(id = 1, ahdPlace = "different place")
        val different = AHDCache(id = 2)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = AHDCache(id = 1, ahdPlace = "place", syncState = SyncState.UNSYNCED)
        val same = AHDCache(id = 1, ahdPlace = "place", syncState = SyncState.UNSYNCED)
        val different = AHDCache(id = 1, ahdPlace = "other", syncState = SyncState.UNSYNCED)
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_invokesLambdaWithId() {
        var captured: Int? = null
        val listener = AHDAdapter.AHDClickListener { id -> captured = id }
        listener.onClick(42)
        assertEquals(42, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = AHDAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
