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
import org.piramalswasthya.sakhi.model.LogLevel
import org.piramalswasthya.sakhi.model.SyncLogEntry

class SyncLogAdapterTest {

    private fun entry(id: Long = 1L, message: String = "msg") = SyncLogEntry(
        id = id,
        timestamp = 1000L,
        level = LogLevel.INFO,
        tag = "TAG",
        message = message
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<SyncLogEntry> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.SyncLogAdapter\$DiffCallback")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<SyncLogEntry>
    }

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(entry(id = 1L), entry(id = 1L, message = "other")))
        assertFalse(callback.areItemsTheSame(entry(id = 1L), entry(id = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(entry(id = 1L, message = "a"), entry(id = 1L, message = "a")))
        assertFalse(callback.areContentsTheSame(entry(id = 1L, message = "a"), entry(id = 1L, message = "b")))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = SyncLogAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
