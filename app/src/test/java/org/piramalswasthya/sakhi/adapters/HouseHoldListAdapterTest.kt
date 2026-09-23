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
import org.piramalswasthya.sakhi.model.HouseHoldBasicDomain

class HouseHoldListAdapterTest {

    private fun household(hhId: Long = 1L, numMembers: Int = 2) = HouseHoldBasicDomain(
        hhId = hhId,
        headName = "Head",
        headSurname = "Surname",
        contactNumber = "9999999999",
        numMembers = numMembers
    )

    @Suppress("UNCHECKED_CAST")
    private fun diffCallback(): DiffUtil.ItemCallback<HouseHoldBasicDomain> {
        val clazz = Class.forName("org.piramalswasthya.sakhi.adapters.HouseHoldListAdapter\$HouseHoldDiffUtilCallBack")
        val field = clazz.getDeclaredField("INSTANCE")
        field.isAccessible = true
        return field.get(null) as DiffUtil.ItemCallback<HouseHoldBasicDomain>
    }

    @Test
    fun areItemsTheSame_comparesByHhId() {
        val callback = diffCallback()
        assertTrue(callback.areItemsTheSame(household(hhId = 1L), household(hhId = 1L, numMembers = 5)))
        assertFalse(callback.areItemsTheSame(household(hhId = 1L), household(hhId = 2L)))
    }

    @Test
    fun areContentsTheSame_usesFullEquality() {
        val callback = diffCallback()
        assertTrue(callback.areContentsTheSame(household(hhId = 1L, numMembers = 2), household(hhId = 1L, numMembers = 2)))
        assertFalse(callback.areContentsTheSame(household(hhId = 1L, numMembers = 2), household(hhId = 1L, numMembers = 3)))
    }

    @Test
    fun clickListener_invokesRespectiveLambdas() {
        var hhDetailsCaptured: HouseHoldBasicDomain? = null
        var showMemberCaptured: HouseHoldBasicDomain? = null
        var newBenCaptured: HouseHoldBasicDomain? = null
        var addMDACaptured: HouseHoldBasicDomain? = null
        var softDeleteCaptured: HouseHoldBasicDomain? = null
        val listener = HouseHoldListAdapter.HouseholdClickListener(
            hhDetails = { hhDetailsCaptured = it },
            showMember = { showMemberCaptured = it },
            newBen = { newBenCaptured = it },
            addMDA = { addMDACaptured = it },
            softDeleteHh = { softDeleteCaptured = it }
        )
        val item = household(hhId = 7L)
        listener.onClickedForHHDetails(item)
        listener.onClickedForMembers(item)
        listener.onClickedForNewBen(item)
        listener.onClickedAddMDA(item)
        listener.onClickSoftDeleteHh(item)
        assertEquals(item, hhDetailsCaptured)
        assertEquals(item, showMemberCaptured)
        assertEquals(item, newBenCaptured)
        assertEquals(item, addMDACaptured)
        assertEquals(item, softDeleteCaptured)
    }

    @Test
    fun itemCount_isZeroBeforeAnyListSubmitted() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        try {
            val listener = HouseHoldListAdapter.HouseholdClickListener({}, {}, {}, {}, {})
            val adapter = HouseHoldListAdapter(
                diseaseType = "TYPE",
                isDisease = false,
                pref = mockk(relaxed = true),
                clickListener = listener
            )
            assertEquals(0, adapter.itemCount)
        } finally {
            unmockkStatic(Looper::class)
        }
    }
}
