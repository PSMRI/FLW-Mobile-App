package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.list

import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@OptIn(ExperimentalCoroutinesApi::class)
class IRSRoundListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo

    private lateinit var viewModel: IRSRoundListViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "hhId" to 1L,
        "fromDisease" to 0,
        "diseaseType" to "MALARIA"
    ))

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.iRSRoundList(any()) } returns flowOf(emptyList())
        viewModel = IRSRoundListViewModel(recordsRepo, savedStateHandle)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `hhId is set from SavedStateHandle`() {
        assertEquals(1L, viewModel.hhId)
    }

    @Test
    fun `allBenList is not null`() {
        assertNotNull(viewModel.allBenList)
    }
}
