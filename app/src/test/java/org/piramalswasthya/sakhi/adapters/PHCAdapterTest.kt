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
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache

class PHCAdapterTest {

    private fun phc(id: Int = 1, place: String? = null) = PHCReviewMeetingCache(
        id = id,
        phcReviewDate = "01-01-2024",
        place = place
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<PHCReviewMeetingCache> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.PHCAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<PHCReviewMeetingCache>
    }

    @Test
    fun areItemsTheSame_comparesById() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(phc(id = 1), phc(id = 1, place = "X")))
        assertFalse(callback.areItemsTheSame(phc(id = 1), phc(id = 2)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(phc(id = 1, place = "A"), phc(id = 1, place = "A")))
        assertFalse(callback.areContentsTheSame(phc(id = 1, place = "A"), phc(id = 1, place = "B")))
    }

    @Test
    fun clickListener_invokesLambdaWithIdWhenIdNonNull() {
        var captured: Int? = null
        val listener = PHCAdapter.PHCClickListener { id -> captured = id }
        listener.onClickForm1(phc(id = 3))
        assertEquals(3, captured)
    }

    @Test
    fun clickListener_withNullLambda_doesNotThrow() {
        val listener = PHCAdapter.PHCClickListener()
        listener.onClickForm1(phc(id = 3))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = PHCAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
