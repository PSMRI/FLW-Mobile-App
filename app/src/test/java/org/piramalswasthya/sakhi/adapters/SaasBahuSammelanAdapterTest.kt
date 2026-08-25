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
import org.piramalswasthya.sakhi.model.SaasBahuSammelanCache

class SaasBahuSammelanAdapterTest {

    private fun sammelan(id: Long = 1L, place: String? = null) = SaasBahuSammelanCache(
        id = id,
        ashaId = 1,
        place = place
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<SaasBahuSammelanCache> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.SaasBahuSammelanAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<SaasBahuSammelanCache>
    }

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(sammelan(id = 1L), sammelan(id = 1L, place = "X")))
        assertFalse(callback.areItemsTheSame(sammelan(id = 1L), sammelan(id = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(sammelan(id = 1L, place = "A"), sammelan(id = 1L, place = "A")))
        assertFalse(callback.areContentsTheSame(sammelan(id = 1L, place = "A"), sammelan(id = 1L, place = "B")))
    }

    @Test
    fun clickListener_invokesLambdaWithId() {
        var captured: Long? = null
        val listener = SaasBahuSammelanAdapter.SaasBahuSammelanAdapterClickListener { id -> captured = id }
        listener.onClick(15L)
        assertEquals(15L, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = SaasBahuSammelanAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
