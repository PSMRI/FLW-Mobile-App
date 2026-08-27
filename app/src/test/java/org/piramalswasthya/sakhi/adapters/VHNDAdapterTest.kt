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
import org.piramalswasthya.sakhi.model.VHNDCache

class VHNDAdapterTest {

    private fun vhnd(id: Int = 1, place: String? = "Place") = VHNDCache(
        id = id,
        vhndDate = "01-01-2024",
        place = place
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<VHNDCache> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.VHNDAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<VHNDCache>
    }

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(vhnd(id = 1), vhnd(id = 1, place = "Other")))
        assertFalse(callback.areItemsTheSame(vhnd(id = 1), vhnd(id = 2)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(vhnd(id = 1), vhnd(id = 1)))
        assertFalse(callback.areContentsTheSame(vhnd(id = 1, place = "A"), vhnd(id = 1, place = "B")))
    }

    @Test
    fun clickListener_invokesLambdaWithId() {
        var captured: Int? = null
        val listener = VHNDAdapter.VHNDClickListener { id -> captured = id }
        listener.onClickForm1(vhnd(id = 7))
        assertEquals(7, captured)
    }

    @Test
    fun clickListener_withNullLambda_doesNotThrow() {
        val listener = VHNDAdapter.VHNDClickListener()
        listener.onClickForm1(vhnd(id = 7))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = VHNDAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
