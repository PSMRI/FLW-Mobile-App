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
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithHRPADomain

class HRPAdapterTest {

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
        syncState = SyncState.SYNCED,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    private fun item(benId: Long = 1L, hhId: Long = 1L, mbp: org.piramalswasthya.sakhi.model.HRPMicroBirthPlanCache? = null) =
        BenWithHRPADomain(
            ben = benBasic(benId, hhId),
            assess = null,
            lmpString = null,
            eddString = null,
            weeksOfPregnancy = null,
            mbp = mbp
        )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithHRPADomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.HRPAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithHRPADomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(item(benId = 1L), item(benId = 1L)))
        assertFalse(callback.areItemsTheSame(item(benId = 1L), item(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(item(benId = 1L), item(benId = 1L)))
        assertFalse(callback.areContentsTheSame(item(benId = 1L, hhId = 1L), item(benId = 1L, hhId = 2L)))
    }

    @Test
    fun clickListener_onClickForm1_invokesLambdaWithHhIdAndBenId() {
        var captured: Pair<Long, Long>? = null
        val listener = HRPAdapter.HRPAClickListener(
            clickedBen = {},
            clickedForm1 = { hhId, benId -> captured = hhId to benId }
        )
        listener.onClickForm1(item(benId = 5L, hhId = 9L))
        assertEquals(9L to 5L, captured)
    }

    @Test
    fun clickListener_onClickForm2AndForm3_invokeRespectiveLambdas() {
        var form2Captured: Pair<Long, Long>? = null
        var form3Captured: Pair<Long, Long>? = null
        val listener = HRPAdapter.HRPAClickListener(
            clickedBen = {},
            clickedForm2 = { hhId, benId -> form2Captured = hhId to benId },
            clickedForm3 = { hhId, benId -> form3Captured = hhId to benId }
        )
        listener.onClickForm2(item(benId = 3L, hhId = 4L))
        listener.onClickForm3(item(benId = 6L, hhId = 7L))
        assertEquals(4L to 3L, form2Captured)
        assertEquals(7L to 6L, form3Captured)
    }

    @Test
    fun clickListener_withNullFormLambdas_doesNotThrowAndReturnsNull() {
        val listener = HRPAdapter.HRPAClickListener(clickedBen = {})
        assertNull(listener.onClickForm1(item()))
        assertNull(listener.onClickForm2(item()))
        assertNull(listener.onClickForm3(item()))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = HRPAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
