package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years

import android.content.Context
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.CUFYFormRepository

@OptIn(ExperimentalCoroutinesApi::class)
class CUFYListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var repository: CUFYFormRepository
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var context: Context

    private lateinit var viewModel: CUFYListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.childFilteredList } returns flowOf(emptyList())
        viewModel = CUFYListViewModel(recordsRepo, repository, pref, context)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benList flow is not null`() {
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `benListWithSamStatus is not null`() {
        assertNotNull(viewModel.benListWithSamStatus)
    }

    @Test
    fun `childOptionsList is not null`() {
        assertNotNull(viewModel.childOptionsList)
    }

    @Test
    fun `filterText does not throw`() = runTest {
        viewModel.filterText("abc")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }
}
