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
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm

class DeathReportBenListAdapterTest {

    private fun ben(benId: Long = 1L, hhId: Long = 1L, spouseName: String? = null) = BenBasicDomainForForm(
        benId = benId,
        hhId = hhId,
        regDate = "01-01-2020",
        benName = "Test",
        gender = "FEMALE",
        dob = 0L,
        mobileNo = "9999999999",
        spouseName = spouseName,
        familyHeadName = "Head",
        syncState = null,
        isConsent = true
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenBasicDomainForForm> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.DeathReportBenListAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenBasicDomainForForm>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(ben(benId = 1L), ben(benId = 1L)))
        assertFalse(callback.areItemsTheSame(ben(benId = 1L), ben(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = ben(benId = 1L)
        assertTrue(callback.areContentsTheSame(old, ben(benId = 1L)))
        assertFalse(callback.areContentsTheSame(old, ben(benId = 1L, hhId = 99L)))
    }

    @Test
    fun areContentsTheSame_detectsSpouseNameChange() {
        val callback = diffCallback()
        assertFalse(callback.areContentsTheSame(ben(spouseName = null), ben(spouseName = "Ramesh")))
    }

    @Test
    fun clickListener_onClickedBen_invokesLambdaWithBenId() {
        var captured: Long? = null
        val listener = DeathReportBenListAdapter.ClickListener(clickedBen = { benId -> captured = benId })
        listener.onClickedBen(ben(benId = 5L))
        assertEquals(5L, captured)
    }

    @Test
    fun clickListener_onClickForm1_invokesLambdaWithHhIdAndBenId() {
        var captured: Pair<Long, Long>? = null
        val listener = DeathReportBenListAdapter.ClickListener(
            clickedBen = { },
            clickedForm1 = { hhId, benId -> captured = hhId to benId }
        )
        listener.onClickForm1(ben(benId = 3L, hhId = 4L))
        assertEquals(4L to 3L, captured)
    }

    @Test
    fun clickListener_onClickForm1_withNullLambda_returnsNull() {
        val listener = DeathReportBenListAdapter.ClickListener(clickedBen = { })
        assertNull(listener.onClickForm1(ben()))
    }

    @Test
    fun clickListener_onClickForm2_invokesLambdaWithHhIdAndBenId() {
        var captured: Pair<Long, Long>? = null
        val listener = DeathReportBenListAdapter.ClickListener(
            clickedBen = { },
            clickedForm2 = { hhId, benId -> captured = hhId to benId }
        )
        listener.onClickForm2(ben(benId = 7L, hhId = 8L))
        assertEquals(8L to 7L, captured)
    }

    @Test
    fun clickListener_onClickForm2_withNullLambda_returnsNull() {
        val listener = DeathReportBenListAdapter.ClickListener(clickedBen = { }, clickedForm2 = null)
        assertNull(listener.onClickForm2(ben()))
    }

    @Test
    fun clickListener_onClickForm3_invokesLambdaWithHhIdAndBenId() {
        var captured: Pair<Long, Long>? = null
        val listener = DeathReportBenListAdapter.ClickListener(
            clickedBen = { },
            clickedForm3 = { hhId, benId -> captured = hhId to benId }
        )
        listener.onClickForm3(ben(benId = 9L, hhId = 10L))
        assertEquals(10L to 9L, captured)
    }

    @Test
    fun clickListener_onClickForm3_withNullLambda_returnsNull() {
        val listener = DeathReportBenListAdapter.ClickListener(clickedBen = { }, clickedForm3 = null)
        assertNull(listener.onClickForm3(ben()))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = DeathReportBenListAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
