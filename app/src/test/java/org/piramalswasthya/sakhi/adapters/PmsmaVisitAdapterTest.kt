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
import org.piramalswasthya.sakhi.model.AncStatus

class PmsmaVisitAdapterTest {

    private fun ancStatus(benId: Long = 1L, visitNumber: Int = 1, filledWeek: Int = 4) = AncStatus(
        benId = benId,
        visitNumber = visitNumber,
        filledWeek = filledWeek,
        syncState = null,
        anyHighRisk = false,
        placeOfAncId = null
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<AncStatus> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.PmsmaVisitAdapter\$MyDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<AncStatus>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(ancStatus(benId = 1L), ancStatus(benId = 1L, visitNumber = 2)))
        assertFalse(callback.areItemsTheSame(ancStatus(benId = 1L), ancStatus(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(ancStatus(benId = 1L, visitNumber = 2), ancStatus(benId = 1L, visitNumber = 2)))
        assertFalse(callback.areContentsTheSame(ancStatus(benId = 1L, visitNumber = 1), ancStatus(benId = 1L, visitNumber = 2)))
    }

    @Test
    fun clickListener_onClickedVisit_passesBenIdVisitNumberAndIsLast() {
        var capturedBenId: Long? = null
        var capturedVisitNumber: Int? = null
        var capturedIsLast: Boolean? = null
        val listener = PmsmaVisitAdapter.PmsmaVisitClickListener { benId, visitNumber, isLast ->
            capturedBenId = benId
            capturedVisitNumber = visitNumber
            capturedIsLast = isLast
        }
        listener.onClickedVisit(ancStatus(benId = 8L, visitNumber = 3), isLast = true)
        assertEquals(8L, capturedBenId)
        assertEquals(3, capturedVisitNumber)
        assertTrue(capturedIsLast!!)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = PmsmaVisitAdapter(PmsmaVisitAdapter.PmsmaVisitClickListener { _, _, _ -> })
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
