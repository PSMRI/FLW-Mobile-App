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
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenPncDomain
import org.piramalswasthya.sakhi.model.PNCVisitCache

class PncVisitListAdapterTest {

    private fun benBasic(benId: Long = 1L, hhId: Long = 1L) = BenBasicDomain(
        benId = benId,
        hhId = hhId,
        reproductiveStatusId = 0,
        regDate = "01-01-2020",
        benName = "Test",
        gender = "FEMALE",
        dob = 0L,
        relToHeadId = 1,
        mobileNo = "9999999999",
        familyHeadName = "Head",
        syncState = null,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    private fun pncVisit(pncPeriod: Int = 1) = PNCVisitCache(
        benId = 1L,
        pncPeriod = pncPeriod,
        isActive = true,
        createdBy = "test",
        updatedBy = "test",
        syncState = SyncState.SYNCED
    )

    private fun benPnc(
        benId: Long = 1L,
        allowFill: Boolean = true,
        savedPncRecords: List<PNCVisitCache> = emptyList()
    ) = BenPncDomain(
        ben = benBasic(benId = benId),
        deliveryDate = "01-01-2024",
        allowFill = allowFill,
        savedPncRecords = savedPncRecords
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenPncDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.PncVisitListAdapter\$MyDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenPncDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(benPnc(benId = 1L), benPnc(benId = 1L, allowFill = false)))
        assertFalse(callback.areItemsTheSame(benPnc(benId = 1L), benPnc(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(benPnc(benId = 1L, allowFill = true), benPnc(benId = 1L, allowFill = true)))
        assertFalse(callback.areContentsTheSame(benPnc(benId = 1L, allowFill = true), benPnc(benId = 1L, allowFill = false)))
    }

    @Test
    fun clickListener_showVisits_passesBenId() {
        var captured: Long? = null
        val listener = PncVisitListAdapter.PncVisitClickListener(
            showVisits = { benId -> captured = benId },
            addVisit = { _, _, _ -> }
        )
        listener.showVisits(benPnc(benId = 12L))
        assertEquals(12L, captured)
    }

    @Test
    fun clickListener_addVisit_computesNextVisitNumberOneWhenNoSavedRecords() {
        var capturedVisitNumber: Int? = null
        val listener = PncVisitListAdapter.PncVisitClickListener(
            showVisits = {},
            addVisit = { _, _, visitNumber -> capturedVisitNumber = visitNumber }
        )
        listener.addVisit(benPnc(savedPncRecords = emptyList()))
        assertEquals(1, capturedVisitNumber)
    }

    @Test
    fun clickListener_addVisit_computesNextVisitNumberFromMaxSavedRecord() {
        var capturedVisitNumber: Int? = null
        val listener = PncVisitListAdapter.PncVisitClickListener(
            showVisits = {},
            addVisit = { _, _, visitNumber -> capturedVisitNumber = visitNumber }
        )
        listener.addVisit(benPnc(savedPncRecords = listOf(pncVisit(1), pncVisit(3), pncVisit(2))))
        assertEquals(4, capturedVisitNumber)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = PncVisitListAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
