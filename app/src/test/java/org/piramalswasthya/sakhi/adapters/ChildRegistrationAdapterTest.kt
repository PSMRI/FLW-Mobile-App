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
import org.piramalswasthya.sakhi.model.ChildRegDomain
import org.piramalswasthya.sakhi.model.InfantRegCache

class ChildRegistrationAdapterTest {

    private fun benBasic(benId: Long = 1L, hhId: Long = 1L, relToHeadId: Int = 1) = BenBasicDomain(
        benId = benId,
        hhId = hhId,
        reproductiveStatusId = 0,
        regDate = "01-01-2020",
        benName = "Test",
        gender = "MALE",
        dob = 0L,
        relToHeadId = relToHeadId,
        mobileNo = "9999999999",
        familyHeadName = "Head",
        syncState = null,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    private fun infant(motherBenId: Long = 1L, babyIndex: Int = 1) = InfantRegCache(
        motherBenId = motherBenId,
        isActive = true,
        babyIndex = babyIndex,
        opv0Dose = 0L,
        bcgDose = 0L,
        hepBDose = 0L,
        vitkDose = 0L,
        createdBy = "user",
        createdDate = 0L,
        updatedBy = "user",
        updatedDate = 0L,
        syncState = SyncState.SYNCED
    )

    private fun childReg(
        motherBenId: Long = 1L,
        babyIndex: Int = 1,
        childBen: BenBasicDomain? = null
    ) = ChildRegDomain(
        motherBen = benBasic(benId = motherBenId),
        infant = infant(motherBenId = motherBenId, babyIndex = babyIndex),
        childBen = childBen
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<ChildRegDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.ChildRegistrationAdapter\$BenDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<ChildRegDomain>
    }

    @Test
    fun areItemsTheSame_comparesByMotherBenIdAndBabyIndex() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(childReg(motherBenId = 1L, babyIndex = 1), childReg(motherBenId = 1L, babyIndex = 1)))
        assertFalse(callback.areItemsTheSame(childReg(motherBenId = 1L, babyIndex = 1), childReg(motherBenId = 1L, babyIndex = 2)))
        assertFalse(callback.areItemsTheSame(childReg(motherBenId = 1L, babyIndex = 1), childReg(motherBenId = 2L, babyIndex = 1)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        val old = childReg()
        val same = childReg()
        val different = childReg(childBen = benBasic(benId = 9L))
        assertTrue(callback.areContentsTheSame(old, same))
        assertFalse(callback.areContentsTheSame(old, different))
    }

    @Test
    fun clickListener_onClickForm_withNoChildBen_usesDefaults() {
        var received: List<Any>? = null
        val listener = ChildRegistrationAdapter.ClickListener { hhId, benId, babyIndex, childBenId, childRelToHead ->
            received = listOf(hhId, benId, babyIndex, childBenId, childRelToHead)
        }
        listener.onClickForm(childReg(motherBenId = 1L, babyIndex = 2, childBen = null))
        assertEquals(listOf(1L, 1L, 2, 0L, 0).map { it.toString() }, received?.map { it.toString() })
    }

    @Test
    fun clickListener_onClickForm_withChildBen_usesChildBenFieldsMinusOne() {
        var received: List<Any>? = null
        val listener = ChildRegistrationAdapter.ClickListener { hhId, benId, babyIndex, childBenId, childRelToHead ->
            received = listOf(hhId, benId, babyIndex, childBenId, childRelToHead)
        }
        listener.onClickForm(childReg(motherBenId = 1L, babyIndex = 2, childBen = benBasic(benId = 8L, relToHeadId = 5)))
        assertEquals(listOf(1L, 1L, 2, 8L, 4).map { it.toString() }, received?.map { it.toString() })
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val adapter = ChildRegistrationAdapter()
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
