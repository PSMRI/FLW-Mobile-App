package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.confirmed.list

import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.RecordsRepo

@OptIn(ExperimentalCoroutinesApi::class)
class LeprosyConfirmedViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: LeprosyConfirmedViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.LeprosyConfirmedList() } returns flowOf(emptyList())
        viewModel = LeprosyConfirmedViewModel(recordsRepo, savedStateHandle)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `allBenList is not null`() {
        assertNotNull(viewModel.allBenList)
    }
}
