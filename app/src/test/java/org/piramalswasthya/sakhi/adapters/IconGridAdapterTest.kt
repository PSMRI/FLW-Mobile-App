package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.DiffUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.Icon

class IconGridAdapterTest {

    private val navDirections: NavDirections = mockk(relaxed = true)

    private fun icon(title: String = "Title", colorPrimary: Boolean = true) = Icon(
        icon = 1,
        title = title,
        count = null,
        navAction = navDirections,
        colorPrimary = colorPrimary
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<Icon> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.IconGridAdapter\$IconDiffCallback")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<Icon>
    }

    @Test
    fun areItemsTheSame_comparesByTitle() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(icon(title = "A"), icon(title = "A", colorPrimary = false)))
        assertFalse(callback.areItemsTheSame(icon(title = "A"), icon(title = "B")))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(icon(title = "A", colorPrimary = true), icon(title = "A", colorPrimary = true)))
        assertFalse(callback.areContentsTheSame(icon(title = "A", colorPrimary = true), icon(title = "A", colorPrimary = false)))
    }

    @Test
    fun clickListener_onClicked_invokesLambdaWithNavAction() {
        var captured: NavDirections? = null
        val listener = IconGridAdapter.GridIconClickListener { dest -> captured = dest }
        listener.onClicked(icon())
        assertEquals(navDirections, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = IconGridAdapter(IconGridAdapter.GridIconClickListener {}, mockk<CoroutineScope>(relaxed = true))
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
