package org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration.eyeBottomsheet

import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.dynamicRepo.EyeSurgeryFormRepository

@OptIn(ExperimentalCoroutinesApi::class)
class EyeSurgeryListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: EyeSurgeryFormRepository

    private lateinit var viewModel: EyeSurgeryListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = EyeSurgeryListViewModel(repository)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `getSavedVisits returns repository result`() = runTest {
        coEvery { repository.getAllVisitsByBenId(any()) } returns emptyList()
        assertTrue(viewModel.getSavedVisits(1L).isEmpty())
    }
}
