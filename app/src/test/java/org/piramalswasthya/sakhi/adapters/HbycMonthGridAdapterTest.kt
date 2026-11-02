package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.DiffUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.HbycIcon

class HbycMonthGridAdapterTest {

    private val navDirections: NavDirections = mockk(relaxed = true)

    private fun icon(count: Int = 1, isFilled: Boolean = false) = HbycIcon(
        hhId = 1L,
        benId = 1L,
        count = count,
        isFilled = isFilled,
        syncState = null,
        destination = navDirections
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<HbycIcon> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.HbycMonthGridAdapter\$HbycIconDiffCallback")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<HbycIcon>
    }

    @Test
    fun areItemsTheSame_comparesByCount() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(icon(count = 1), icon(count = 1, isFilled = true)))
        assertFalse(callback.areItemsTheSame(icon(count = 1), icon(count = 2)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(icon(count = 1, isFilled = true), icon(count = 1, isFilled = true)))
        assertFalse(callback.areContentsTheSame(icon(count = 1, isFilled = false), icon(count = 1, isFilled = true)))
    }

    @Test
    fun clickListener_onClicked_invokesLambdaWithDestination() {
        var captured: androidx.navigation.NavDirections? = null
        val listener = HbycMonthGridAdapter.HbycIconClickListener { destination -> captured = destination }
        val item = icon()
        listener.onClicked(item)
        assertEquals(navDirections, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = HbycMonthGridAdapter(HbycMonthGridAdapter.HbycIconClickListener {})
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
