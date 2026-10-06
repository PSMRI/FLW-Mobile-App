package org.piramalswasthya.sakhi.adapters.dynamicAdapter

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
import org.piramalswasthya.sakhi.model.dynamicModel.MDACampaignItem

class FilariaMdaCampaignAdapterTest {

    private fun item(srNo: Int = 1, startDate: String = "01-01-2024") = MDACampaignItem(
        srNo = srNo,
        startDate = startDate,
        endDate = "02-01-2024",
        noOffamilies = "10",
        noOfIndividuals = "50"
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<MDACampaignItem> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.dynamicAdapter.FilariaMdaCampaignAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<MDACampaignItem>
    }

    @Test
    fun areItemsTheSame_comparesBySrNo() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(item(srNo = 1), item(srNo = 1, startDate = "05-01-2024")))
        assertFalse(callback.areItemsTheSame(item(srNo = 1), item(srNo = 2)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(item(srNo = 1), item(srNo = 1)))
        assertFalse(callback.areContentsTheSame(item(srNo = 1, startDate = "A"), item(srNo = 1, startDate = "B")))
    }

    @Test
    fun clickListener_invokesLambdaWithStartDate() {
        var captured: String? = null
        val listener = FilariaMdaCampaignAdapter.MdaClickListener { date -> captured = date }
        listener.onClickForm1(item(startDate = "10-02-2024"))
        assertEquals("10-02-2024", captured)
    }

    @Test
    fun clickListener_withNullLambda_doesNotThrow() {
        val listener = FilariaMdaCampaignAdapter.MdaClickListener()
        listener.onClickForm1(item())
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = FilariaMdaCampaignAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
