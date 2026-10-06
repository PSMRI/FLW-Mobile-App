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
import org.piramalswasthya.sakhi.model.BenWithLeprosyScreeningDomain
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache

class LeprosyMemberListAdapterTest {

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

    private fun benWithLeprosy(benId: Long = 1L, leprosy: LeprosyScreeningCache? = null) =
        BenWithLeprosyScreeningDomain(
            ben = benBasic(benId = benId),
            leprosy = leprosy,
            followUps = emptyList(),
            currentFollowUp = null,
            currentVisitFollowUps = emptyList(),
            lastFollowUp = null
        )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<BenWithLeprosyScreeningDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.LeprosyMemberListAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<BenWithLeprosyScreeningDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        val old = benWithLeprosy(benId = 1L)
        val same = benWithLeprosy(benId = 1L)
        val different = benWithLeprosy(benId = 2L)
        assertTrue(callback.areItemsTheSame(old, same))
        assertFalse(callback.areItemsTheSame(old, different))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = benWithLeprosy(benId = 1L)
        val same = benWithLeprosy(benId = 1L)
        val different = benWithLeprosy(benId = 1L, leprosy = LeprosyScreeningCache(benId = 1L, houseHoldDetailsId = 1L, createdBy = "test", modifiedBy = "test"))
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClickForm_invokesLambdaWithHhIdAndBenId() {
        var capturedHhId: Long? = null
        var capturedBenId: Long? = null
        val listener = LeprosyMemberListAdapter.ClickListener(
            clickedForm = { hhId, benId ->
                capturedHhId = hhId
                capturedBenId = benId
            }
        )
        val item = benWithLeprosy(benId = 5L)
        listener.onClickForm(item)
        assertEquals(1L, capturedHhId)
        assertEquals(5L, capturedBenId)
    }

    @Test
    fun clickListener_onClickVisits_invokesLambdaWithItem() {
        var captured: BenWithLeprosyScreeningDomain? = null
        val listener = LeprosyMemberListAdapter.ClickListener(
            clickedVisits = { captured = it }
        )
        val item = benWithLeprosy(benId = 5L)
        listener.onClickVisits(item)
        assertEquals(item, captured)
    }

    @Test
    fun clickListener_withNullLambdas_doesNotThrow() {
        val listener = LeprosyMemberListAdapter.ClickListener()
        val item = benWithLeprosy()
        listener.onClickForm(item)
        listener.onClickVisits(item)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = LeprosyMemberListAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
