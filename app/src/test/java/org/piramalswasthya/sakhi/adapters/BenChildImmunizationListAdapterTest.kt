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
import org.piramalswasthya.sakhi.model.ImmunizationDetailsDomain
import org.piramalswasthya.sakhi.model.VaccineDomain

class BenChildImmunizationListAdapterTest {

    private val sharedOnClick: (Long, Int) -> Unit = { _, _ -> }

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

    private fun details(
        benId: Long = 1L,
        vaccineStateList: List<VaccineDomain> = emptyList(),
        onClick: (Long, Int) -> Unit = sharedOnClick
    ) = ImmunizationDetailsDomain(
        ben = benBasic(benId = benId),
        vaccineStateList = vaccineStateList
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<ImmunizationDetailsDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.BenChildImmunizationListAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<ImmunizationDetailsDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(details(benId = 1L), details(benId = 1L)))
        assertFalse(callback.areItemsTheSame(details(benId = 1L), details(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = details(benId = 1L)
        val same = details(benId = 1L)
        val different = details(benId = 1L, vaccineStateList = listOf(mockk<VaccineDomain>()))
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClickedBen_invokesLambdaWithBenId() {
        var captured: Long? = null
        val listener = BenChildImmunizationListAdapter.VaccinesClickListener { benId -> captured = benId }
        listener.onClickedBen(benBasic(benId = 6L))
        assertEquals(6L, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = BenChildImmunizationListAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
