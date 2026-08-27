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
import org.piramalswasthya.sakhi.model.PulsePolioCampaignCache

class PulsePolioCampaignAdapterTest {

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<PulsePolioCampaignCache> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.PulsePolioCampaignAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<PulsePolioCampaignCache>
    }

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = diffCallback()
        val old = PulsePolioCampaignCache(id = 1)
        val same = PulsePolioCampaignCache(id = 1, formDataJson = "{}")
        val different = PulsePolioCampaignCache(id = 2)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = PulsePolioCampaignCache(id = 1, formDataJson = "{}")
        val same = PulsePolioCampaignCache(id = 1, formDataJson = "{}")
        val different = PulsePolioCampaignCache(id = 1, formDataJson = "{\"a\":1}")
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_invokesLambdaWithId() {
        var captured: Int? = null
        val listener = PulsePolioCampaignAdapter.PulsePolioCampaignClickListener { id -> captured = id }
        listener.onClick(11)
        assertEquals(11, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = PulsePolioCampaignAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
