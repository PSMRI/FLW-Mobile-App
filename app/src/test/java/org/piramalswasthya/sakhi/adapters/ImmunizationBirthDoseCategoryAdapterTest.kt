package org.piramalswasthya.sakhi.adapters

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.list.ChildImmunizationListViewModel

class ImmunizationBirthDoseCategoryAdapterTest {

    @Test
    fun itemCount_matchesCatDataListSize() {
        val catDataList = arrayListOf("Cat A", "Cat B", "Cat C")
        val adapter = ImmunizationBirthDoseCategoryAdapter(
            catDataList,
            null,
            mockk(relaxed = true)
        )
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun itemCount_isZeroForEmptyList() {
        val adapter = ImmunizationBirthDoseCategoryAdapter(
            arrayListOf(),
            null,
            mockk(relaxed = true)
        )
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun categoryClickListener_invokesLambdaWithCatData() {
        var captured: String? = null
        val listener = ImmunizationBirthDoseCategoryAdapter.CategoryClickListener { captured = it }
        listener.onClicked("Cat A")
        assertEquals("Cat A", captured)
    }
}
