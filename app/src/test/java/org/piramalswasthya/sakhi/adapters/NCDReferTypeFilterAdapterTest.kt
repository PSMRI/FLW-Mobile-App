package org.piramalswasthya.sakhi.adapters

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.list.NcdRefferedListViewModel

class NCDReferTypeFilterAdapterTest {

    @Test
    fun itemCount_matchesCatDataListSize() {
        val viewModel = mockk<NcdRefferedListViewModel>(relaxed = true)
        val adapter = NCDReferTypeFilterAdapter(arrayListOf("A", "B"), viewModel = viewModel)
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun itemCount_isZeroForEmptyList() {
        val viewModel = mockk<NcdRefferedListViewModel>(relaxed = true)
        val adapter = NCDReferTypeFilterAdapter(arrayListOf(), viewModel = viewModel)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun categoryClickListener_invokesLambdaWithCategory() {
        var captured: String? = null
        val listener = NCDReferTypeFilterAdapter.CategoryClickListener { category -> captured = category }
        listener.onClicked("Hypertension")
        assertEquals("Hypertension", captured)
    }
}
