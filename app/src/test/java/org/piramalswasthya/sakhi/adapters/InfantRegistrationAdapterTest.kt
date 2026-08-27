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
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.InfantRegDomain

class InfantRegistrationAdapterTest {

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

    private fun deliveryOutcome(benId: Long = 1L) = DeliveryOutcomeCache(
        benId = benId,
        isActive = true,
        createdBy = "test",
        updatedBy = "test",
        syncState = SyncState.SYNCED
    )

    private fun infantReg(benId: Long = 1L, babyIndex: Int = 0) = InfantRegDomain(
        motherBen = benBasic(benId = benId),
        babyIndex = babyIndex,
        deliveryOutcome = deliveryOutcome(benId = benId),
        savedIr = null
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<InfantRegDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.InfantRegistrationAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<InfantRegDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenIdAndBabyIndex() {
        val callback = diffCallback()
        val old = infantReg(benId = 1L, babyIndex = 0)
        val same = infantReg(benId = 1L, babyIndex = 0)
        val differentBaby = infantReg(benId = 1L, babyIndex = 1)
        val differentBen = infantReg(benId = 2L, babyIndex = 0)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, differentBaby))
        assertFalse(callback.areItemsTheSame(old, differentBen))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = infantReg(benId = 1L, babyIndex = 0)
        val same = infantReg(benId = 1L, babyIndex = 0)
        val different = infantReg(benId = 1L, babyIndex = 0).copy(babyName = "Changed Name")
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_invokesLambdaWithBenIdAndBabyIndex() {
        var capturedBenId: Long? = null
        var capturedBabyIndex: Int? = null
        val listener = InfantRegistrationAdapter.ClickListener { benId, babyIndex ->
            capturedBenId = benId
            capturedBabyIndex = babyIndex
        }
        val item = infantReg(benId = 7L, babyIndex = 2)
        listener.onClickForm(item)
        assertEquals(7L, capturedBenId)
        assertEquals(2, capturedBabyIndex)
    }

    @Test
    fun clickListener_withNullLambda_doesNotThrow() {
        val listener = InfantRegistrationAdapter.ClickListener()
        listener.onClickForm(infantReg())
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = InfantRegistrationAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
