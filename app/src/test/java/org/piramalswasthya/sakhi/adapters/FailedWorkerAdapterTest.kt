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
import org.piramalswasthya.sakhi.model.FailedWorkerInfo

class FailedWorkerAdapterTest {

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<FailedWorkerInfo> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.FailedWorkerAdapter\$DiffCallback")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<FailedWorkerInfo>
    }

    @Test
    fun areItemsTheSame_comparesByWorkerName() {
        val callback = diffCallback()
        val old = FailedWorkerInfo(workerName = "Worker A", error = "err1")
        val same = FailedWorkerInfo(workerName = "Worker A", error = "err2")
        val different = FailedWorkerInfo(workerName = "Worker B", error = "err1")
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = FailedWorkerInfo(workerName = "Worker A", error = "err1")
        val same = FailedWorkerInfo(workerName = "Worker A", error = "err1")
        val different = FailedWorkerInfo(workerName = "Worker A", error = "err2")
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = FailedWorkerAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
