package org.piramalswasthya.sakhi.adapters

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_eligible_list.NcdEligibleListViewModel

class NCDCategoryAdapterTest {

    @Test
    fun itemCount_matchesDataListSize() {
        val viewModel = mockk<NcdEligibleListViewModel>(relaxed = true)
        val adapter = NCDCategoryAdapter(arrayListOf("A", "B", "C"), viewModel = viewModel)
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun itemCount_isZeroForEmptyList() {
        val viewModel = mockk<NcdEligibleListViewModel>(relaxed = true)
        val adapter = NCDCategoryAdapter(arrayListOf(), viewModel = viewModel)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun clickListener_invokesLambdaWithCategory() {
        var captured: String? = null
        val listener = NCDCategoryAdapter.ClickListener { category -> captured = category }
        listener.onClicked("Diabetes")
        assertEquals("Diabetes", captured)
    }
}
