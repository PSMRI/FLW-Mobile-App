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
import org.piramalswasthya.sakhi.model.VHNCCache

class VHNCAdapterTest {

    private fun vhnc(id: Int = 1, place: String? = "Place") = VHNCCache(
        id = id,
        vhncDate = "01-01-2024",
        place = place
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<VHNCCache> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.VHNCAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<VHNCCache>
    }

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(vhnc(id = 1), vhnc(id = 1, place = "Other")))
        assertFalse(callback.areItemsTheSame(vhnc(id = 1), vhnc(id = 2)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(vhnc(id = 1), vhnc(id = 1)))
        assertFalse(callback.areContentsTheSame(vhnc(id = 1, place = "A"), vhnc(id = 1, place = "B")))
    }

    @Test
    fun clickListener_invokesLambdaWithId() {
        var captured: Int? = null
        val listener = VHNCAdapter.VHNCClickListener { id -> captured = id }
        listener.onClickForm1(vhnc(id = 7))
        assertEquals(7, captured)
    }

    @Test
    fun clickListener_withNullLambda_doesNotThrow() {
        val listener = VHNCAdapter.VHNCClickListener()
        listener.onClickForm1(vhnc(id = 7))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = VHNCAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
