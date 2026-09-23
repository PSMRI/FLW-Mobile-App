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
import org.piramalswasthya.sakhi.model.DewormingCache

class DewormingAdapterTest {

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<DewormingCache> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.DewormingAdapter\$DewormingDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<DewormingCache>
    }

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = diffCallback()
        val old = DewormingCache(id = 1)
        val same = DewormingCache(id = 1, dewormingDone = "Yes")
        val different = DewormingCache(id = 2)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = DewormingCache(id = 1, dewormingDone = "Yes")
        val same = DewormingCache(id = 1, dewormingDone = "Yes")
        val different = DewormingCache(id = 1, dewormingDone = "No")
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClick_invokesLambdaWithId() {
        var captured: Int? = null
        val listener = DewormingAdapter.DewormingClickListener { id -> captured = id }
        listener.onClick(7)
        assertEquals(7, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = DewormingAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
