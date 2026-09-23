package org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration.eyeBottomsheet

import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.dynamicEntity.eye_surgery.EyeSurgeryFormResponseJsonEntity
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

    private fun visit(eyeSide: String) = EyeSurgeryFormResponseJsonEntity(
        benId = 1L,
        hhId = 1L,
        visitDate = "01-01-2026",
        visitMonth = "January",
        eyeSide = eyeSide,
        formId = "eye_surgery",
        version = 1,
        formDataJson = "{}"
    )

    @Test
    fun `getAvailableEyes returns both options when no visits recorded`() = runTest {
        coEvery { repository.getAllVisitsByBenId(any()) } returns emptyList()
        assertEquals(listOf("LEFT", "RIGHT", "BOTH"), viewModel.getAvailableEyes(1L))
    }

    @Test
    fun `getAvailableEyes returns empty when BOTH already completed`() = runTest {
        coEvery { repository.getAllVisitsByBenId(any()) } returns listOf(visit("BOTH"))
        assertTrue(viewModel.getAvailableEyes(1L).isEmpty())
    }

    @Test
    fun `getAvailableEyes returns empty when LEFT and RIGHT both completed`() = runTest {
        coEvery { repository.getAllVisitsByBenId(any()) } returns listOf(visit("LEFT"), visit("RIGHT"))
        assertTrue(viewModel.getAvailableEyes(1L).isEmpty())
    }

    @Test
    fun `getAvailableEyes returns RIGHT when only LEFT completed`() = runTest {
        coEvery { repository.getAllVisitsByBenId(any()) } returns listOf(visit("LEFT"))
        assertEquals(listOf("RIGHT"), viewModel.getAvailableEyes(1L))
    }

    @Test
    fun `getAvailableEyes returns LEFT when only RIGHT completed`() = runTest {
        coEvery { repository.getAllVisitsByBenId(any()) } returns listOf(visit("RIGHT"))
        assertEquals(listOf("LEFT"), viewModel.getAvailableEyes(1L))
    }
}
