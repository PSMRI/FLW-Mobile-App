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
import org.piramalswasthya.sakhi.model.MaaMeetingEntity

class MaaMeetingAdapterTest {

    private fun meeting(id: Long = 1L, place: String? = "Place") = MaaMeetingEntity(
        id = id,
        meetingDate = "01-01-2024",
        place = place,
        participants = 10,
        ashaId = 1
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<MaaMeetingEntity> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.MaaMeetingAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<MaaMeetingEntity>
    }

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = diffCallback()
        val old = meeting(id = 1L, place = "A")
        val same = meeting(id = 1L, place = "B")
        val different = meeting(id = 2L)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = meeting(id = 1L, place = "A")
        val same = meeting(id = 1L, place = "A")
        val different = meeting(id = 1L, place = "B")
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_invokesLambdaWithId() {
        var captured: Long? = null
        val listener = MaaMeetingAdapter.MaaMeetingAdapterClickListener { id -> captured = id }
        listener.onClick(42L)
        assertEquals(42L, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = MaaMeetingAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
