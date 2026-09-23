package org.piramalswasthya.sakhi.adapters

import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.AncStatus
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.model.PMSMAStatus

class AncVisitListAdapterTest {

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

    private fun benWithAnc(
        benId: Long = 1L,
        hhId: Long = 1L,
        anc: List<AncStatus> = emptyList(),
        pmsma: List<PMSMAStatus> = emptyList()
    ) = BenWithAncListDomain(
        ben = benBasic(benId = benId, hhId = hhId),
        pwr = null,
        anc = anc,
        pmsma = pmsma,
        savedAncRecords = emptyList(),
        showAddAnc = false,
        pmsmaFillable = false,
        hasPmsma = false,
        syncState = null
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithAncListDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.AncVisitListAdapter\$MyDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithAncListDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(benWithAnc(benId = 1L), benWithAnc(benId = 1L)))
        assertFalse(callback.areItemsTheSame(benWithAnc(benId = 1L), benWithAnc(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = benWithAnc(benId = 1L)
        val same = benWithAnc(benId = 1L)
        val different = benWithAnc(benId = 1L, anc = listOf(AncStatus(benId = 1L, visitNumber = 1, filledWeek = 10, anyHighRisk = false, placeOfAncId = 1)))
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_showVisits_invokesLambdaWithBenId() {
        var captured: Long? = null
        val listener = AncVisitListAdapter.PregnancyVisitClickListener(
            showVisits = { benId -> captured = benId },
            showPmsmaVisits = { _, _ -> },
            addVisit = { _, _, _ -> },
            pmsma = { _, _, _ -> },
            callBen = { }
        )
        listener.showVisits(benWithAnc(benId = 4L))
        assertEquals(4L, captured)
    }

    @Test
    fun clickListener_addVisit_computesNextVisitNumberFromEmptyAnc() {
        var capturedVisitNumber: Int? = null
        val listener = AncVisitListAdapter.PregnancyVisitClickListener(
            showVisits = { },
            showPmsmaVisits = { _, _ -> },
            addVisit = { _, _, visitNumber -> capturedVisitNumber = visitNumber },
            pmsma = { _, _, _ -> },
            callBen = { }
        )
        listener.addVisit(benWithAnc(anc = emptyList()))
        assertEquals(1, capturedVisitNumber)
    }

    @Test
    fun clickListener_addVisit_computesNextVisitNumberFromExistingAnc() {
        var capturedVisitNumber: Int? = null
        val listener = AncVisitListAdapter.PregnancyVisitClickListener(
            showVisits = { },
            showPmsmaVisits = { _, _ -> },
            addVisit = { _, _, visitNumber -> capturedVisitNumber = visitNumber },
            pmsma = { _, _, _ -> },
            callBen = { }
        )
        val anc = listOf(
            AncStatus(benId = 1L, visitNumber = 1, filledWeek = 10, anyHighRisk = false, placeOfAncId = 1),
            AncStatus(benId = 1L, visitNumber = 3, filledWeek = 20, anyHighRisk = false, placeOfAncId = 1)
        )
        listener.addVisit(benWithAnc(anc = anc))
        assertEquals(4, capturedVisitNumber)
    }

    @Test
    fun clickListener_pmsma_computesNextVisitNumber() {
        var capturedVisitNumber: Int? = null
        val listener = AncVisitListAdapter.PregnancyVisitClickListener(
            showVisits = { },
            showPmsmaVisits = { _, _ -> },
            addVisit = { _, _, _ -> },
            pmsma = { _, _, visitNumber -> capturedVisitNumber = visitNumber },
            callBen = { }
        )
        listener.pmsma(benWithAnc(pmsma = listOf(PMSMAStatus(benId = 1L, visitNumber = 2, filledWeek = 15))))
        assertEquals(3, capturedVisitNumber)
    }

    @Test
    fun clickListener_addHomeVisit_withNullLambda_doesNotThrow() {
        val listener = AncVisitListAdapter.PregnancyVisitClickListener(
            showVisits = { },
            showPmsmaVisits = { _, _ -> },
            addVisit = { _, _, _ -> },
            pmsma = { _, _, _ -> },
            callBen = { },
            addHomeVisit = null
        )
        listener.addHomeVisit(benWithAnc(benId = 1L))
    }

    @Test
    fun clickListener_showHomeVisit_withNullLambda_returnsNull() {
        val listener = AncVisitListAdapter.PregnancyVisitClickListener(
            showVisits = { },
            showPmsmaVisits = { _, _ -> },
            addVisit = { _, _, _ -> },
            pmsma = { _, _, _ -> },
            callBen = { },
            showHomeVisit = null
        )
        assertNull(listener.showHomeVisit(benWithAnc(benId = 1L)))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = AncVisitListAdapter(hidePmsma = false)
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
