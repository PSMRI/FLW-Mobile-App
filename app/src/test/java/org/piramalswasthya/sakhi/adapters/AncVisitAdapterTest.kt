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

class AncVisitAdapterTest {

    private fun status(benId: Long = 1L, visitNumber: Int = 1) = AncStatus(
        benId = benId,
        visitNumber = visitNumber,
        filledWeek = 10,
        anyHighRisk = false,
        placeOfAncId = 1
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<AncStatus> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.AncVisitAdapter\$MyDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<AncStatus>
    }

    @Test
    fun areItemsTheSame_comparesByBenIdAndVisitNumber() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(status(benId = 1L, visitNumber = 1), status(benId = 1L, visitNumber = 1)))
        assertFalse(callback.areItemsTheSame(status(benId = 1L, visitNumber = 1), status(benId = 1L, visitNumber = 2)))
        assertFalse(callback.areItemsTheSame(status(benId = 1L, visitNumber = 1), status(benId = 2L, visitNumber = 1)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = status(benId = 1L, visitNumber = 1)
        val same = status(benId = 1L, visitNumber = 1)
        val different = old.copy(filledWeek = 20)
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_invokesLambdaWithDerivedArgs() {
        var capturedBenId: Long? = null
        var capturedVisitNumber: Int? = null
        var capturedIsLast: Boolean? = null
        val listener = AncVisitAdapter.AncVisitClickListener { benId, visitNumber, isLast ->
            capturedBenId = benId
            capturedVisitNumber = visitNumber
            capturedIsLast = isLast
        }
        listener.onClickedVisit(status(benId = 5L, visitNumber = 2), isLast = true)
        assertEquals(5L, capturedBenId)
        assertEquals(2, capturedVisitNumber)
        assertTrue(capturedIsLast!!)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = AncVisitAdapter(AncVisitAdapter.AncVisitClickListener { _, _, _ -> })
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
