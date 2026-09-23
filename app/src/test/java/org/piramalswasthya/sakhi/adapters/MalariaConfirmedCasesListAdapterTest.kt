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
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithMalariaConfirmedDomain
import org.piramalswasthya.sakhi.model.MalariaConfirmedCasesCache

class MalariaConfirmedCasesListAdapterTest {

    private fun benBasic(benId: Long = 1L, hhId: Long = 1L) = BenBasicDomain(
        benId = benId,
        hhId = hhId,
        reproductiveStatusId = 0,
        regDate = "01-01-2020",
        benName = "Test",
        gender = "MALE",
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

    private fun benWithMalariaConfirmed(
        benId: Long = 1L,
        malariaConfirmed: MalariaConfirmedCasesCache? = null,
        slideTestName: String? = null
    ) = BenWithMalariaConfirmedDomain(benBasic(benId = benId), malariaConfirmed, slideTestName)

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithMalariaConfirmedDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.MalariaConfirmedCasesListAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithMalariaConfirmedDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        val old = benWithMalariaConfirmed(benId = 1L)
        val same = benWithMalariaConfirmed(benId = 1L)
        val different = benWithMalariaConfirmed(benId = 2L)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = benWithMalariaConfirmed(benId = 1L, slideTestName = "Test")
        val same = benWithMalariaConfirmed(benId = 1L, slideTestName = "Test")
        val different = benWithMalariaConfirmed(benId = 1L, slideTestName = "Other")
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_invokesLambdaWithHhIdAndBenId() {
        var capturedHhId: Long? = null
        var capturedBenId: Long? = null
        val listener = MalariaConfirmedCasesListAdapter.ClickListener { hhId, benId ->
            capturedHhId = hhId
            capturedBenId = benId
        }
        val item = BenWithMalariaConfirmedDomain(benBasic(benId = 5L, hhId = 9L), null, null)
        listener.onClickForm(item)
        assertEquals(9L, capturedHhId)
        assertEquals(5L, capturedBenId)
    }

    @Test
    fun clickListener_withNullLambda_doesNotThrow() {
        val listener = MalariaConfirmedCasesListAdapter.ClickListener()
        listener.onClickForm(benWithMalariaConfirmed())
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = MalariaConfirmedCasesListAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
