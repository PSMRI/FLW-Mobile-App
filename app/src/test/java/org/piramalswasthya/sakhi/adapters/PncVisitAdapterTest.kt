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
import org.piramalswasthya.sakhi.model.PncDomain

class PncVisitAdapterTest {

    private fun pnc(benId: Long = 1L, visitNumber: Int = 1) = PncDomain(
        benId = benId,
        visitNumber = visitNumber
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<PncDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.PncVisitAdapter\$MyDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<PncDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(pnc(benId = 1L), pnc(benId = 1L, visitNumber = 2)))
        assertFalse(callback.areItemsTheSame(pnc(benId = 1L), pnc(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(pnc(benId = 1L, visitNumber = 2), pnc(benId = 1L, visitNumber = 2)))
        assertFalse(callback.areContentsTheSame(pnc(benId = 1L, visitNumber = 1), pnc(benId = 1L, visitNumber = 2)))
    }

    @Test
    fun clickListener_onClickedVisit_passesBenIdAndVisitNumber() {
        var capturedBenId: Long? = null
        var capturedVisitNumber: Int? = null
        val listener = PncVisitAdapter.PncVisitClickListener { benId, visitNumber ->
            capturedBenId = benId
            capturedVisitNumber = visitNumber
        }
        listener.onClickedVisit(pnc(benId = 6L, visitNumber = 2))
        assertEquals(6L, capturedBenId)
        assertEquals(2, capturedVisitNumber)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = PncVisitAdapter(PncVisitAdapter.PncVisitClickListener { _, _ -> })
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
