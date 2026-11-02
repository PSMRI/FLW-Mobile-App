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
import org.piramalswasthya.sakhi.model.ChildImmunizationCategory
import org.piramalswasthya.sakhi.model.ImmunizationDetailsDomain
import org.piramalswasthya.sakhi.model.VaccineDomain
import org.piramalswasthya.sakhi.model.VaccineState

class ImmunizationBenListAdapterTest {

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
        syncState = SyncState.SYNCED,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    private fun vaccine(vaccineId: Int = 1) = VaccineDomain(
        vaccineId = vaccineId,
        vaccineName = "BCG",
        vaccineCategory = ChildImmunizationCategory.BIRTH,
        state = VaccineState.PENDING
    )

    private fun item(benId: Long = 1L, vaccineStateList: List<VaccineDomain> = emptyList()) =
        ImmunizationDetailsDomain(ben = benBasic(benId), vaccineStateList = vaccineStateList)

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<ImmunizationDetailsDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.ImmunizationBenListAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<ImmunizationDetailsDomain>
    }

    @Test
    fun areItemsTheSame_comparesByBenId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(item(benId = 1L), item(benId = 1L, vaccineStateList = listOf(vaccine()))))
        assertFalse(callback.areItemsTheSame(item(benId = 1L), item(benId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(item(benId = 1L), item(benId = 1L)))
        assertFalse(callback.areContentsTheSame(item(benId = 1L), item(benId = 1L, vaccineStateList = listOf(vaccine()))))
    }

    @Test
    fun clickListener_onClickedBen_invokesLambdaWithBenId() {
        var captured: Long? = null
        val listener = ImmunizationBenListAdapter.VaccinesClickListener(
            clickedVaccine = { captured = it },
            callBen = {}
        )
        listener.onClickedBen(benBasic(benId = 8L))
        assertEquals(8L, captured)
    }

    @Test
    fun clickListener_onClickedForCall_invokesLambdaWithBen() {
        var captured: BenBasicDomain? = null
        val listener = ImmunizationBenListAdapter.VaccinesClickListener(
            clickedVaccine = {},
            callBen = { captured = it }
        )
        val ben = benBasic(benId = 4L)
        listener.onClickedForCall(ben)
        assertEquals(ben, captured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = ImmunizationBenListAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
