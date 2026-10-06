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
import org.piramalswasthya.sakhi.model.IRSRoundScreening

class IrsRoundListAdapterTest {

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<IRSRoundScreening> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.IrsRoundListAdapter\$MyDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<IRSRoundScreening>
    }

    @Test
    fun areItemsTheSame_comparesByHouseholdId() {
        val callback = diffCallback()
        val old = IRSRoundScreening(id = 1, householdId = 100L)
        val same = IRSRoundScreening(id = 2, householdId = 100L)
        val different = IRSRoundScreening(id = 1, householdId = 200L)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = IRSRoundScreening(id = 1, rounds = 2, householdId = 100L)
        val same = IRSRoundScreening(id = 1, rounds = 2, householdId = 100L)
        val different = IRSRoundScreening(id = 1, rounds = 3, householdId = 100L)
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = IrsRoundListAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
