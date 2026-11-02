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
import org.piramalswasthya.sakhi.model.IncentiveActivityCache
import org.piramalswasthya.sakhi.model.IncentiveGrouped

class IncentiveGroupedAdapterTest {

    private fun activity(id: Long = 1L) = IncentiveActivityCache(
        id = id,
        name = "Activity",
        description = "Desc",
        paymentParam = "param",
        rate = 10,
        state = 1,
        district = 1,
        group = "group",
        groupName = "groupName",
        fmrCode = null,
        fmrCodeOld = null
    )

    private fun grouped(activityName: String = "Activity A", totalAmount: Long = 100L) = IncentiveGrouped(
        activityName = activityName,
        totalAmount = totalAmount,
        count = 1,
        groupName = "group",
        description = "desc",
        activity = activity()
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<IncentiveGrouped> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.IncentiveGroupedAdapter\$IncentiveGroupedDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<IncentiveGrouped>
    }

    @Test
    fun areItemsTheSame_comparesByActivityName() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(grouped(activityName = "A"), grouped(activityName = "A", totalAmount = 500L)))
        assertFalse(callback.areItemsTheSame(grouped(activityName = "A"), grouped(activityName = "B")))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(grouped(totalAmount = 100L), grouped(totalAmount = 100L)))
        assertFalse(callback.areContentsTheSame(grouped(totalAmount = 100L), grouped(totalAmount = 200L)))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = IncentiveGroupedAdapter(onItemClick = { _, _ -> })
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
