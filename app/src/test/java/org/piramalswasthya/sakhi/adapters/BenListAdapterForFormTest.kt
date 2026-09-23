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
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm

class BenListAdapterForFormTest {

    private fun ben(benId: Long = 1L, hhId: Long = 1L) = BenBasicDomainForForm(
        benId = benId,
        hhId = hhId,
        regDate = "01-01-2020",
        benName = "Test",
        gender = "MALE",
        dob = 0L,
        mobileNo = "9999999999",
        familyHeadName = "Head",
        syncState = null,
        isConsent = true
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenBasicDomainForForm> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.BenListAdapterForForm\$BenDiffUtilCallBack")
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
        val same = ben(benId = 1L)
        val different = ben(benId = 1L, hhId = 99L)
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClickedBen_invokesLambdaWithBenId() {
        var captured: Long? = null
        val listener = BenListAdapterForForm.ClickListener(clickedBen = { benId -> captured = benId })
        listener.onClickedBen(ben(benId = 5L))
        assertEquals(5L, captured)
    }

    @Test
    fun clickListener_onClickForm1_invokesLambdaWithHhIdAndBenId() {
        var captured: Pair<Long, Long>? = null
        val listener = BenListAdapterForForm.ClickListener(
            clickedBen = { },
            clickedForm1 = { hhId, benId -> captured = hhId to benId }
        )
        listener.onClickForm1(ben(benId = 3L, hhId = 4L))
        assertEquals(4L to 3L, captured)
    }

    @Test
    fun clickListener_onClickForm2_withNullLambda_returnsNull() {
        val listener = BenListAdapterForForm.ClickListener(clickedBen = { }, clickedForm2 = null)
        assertNull(listener.onClickForm2(ben()))
    }

    @Test
    fun clickListener_onClickForm3_withNullLambda_returnsNull() {
        val listener = BenListAdapterForForm.ClickListener(clickedBen = { }, clickedForm3 = null)
        assertNull(listener.onClickForm3(ben()))
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = BenListAdapterForForm()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
