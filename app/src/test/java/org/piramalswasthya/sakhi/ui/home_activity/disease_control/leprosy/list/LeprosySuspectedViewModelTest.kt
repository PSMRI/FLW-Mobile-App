package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.list

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
class LeprosySuspectedViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo

    private lateinit var viewModel: LeprosySuspectedViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "hhId" to 1L,
        "fromDisease" to 0,
        "diseaseType" to "LEPROSY"
    ))

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.LeprosyScreeningList(any()) } returns flowOf(emptyList())
        viewModel = LeprosySuspectedViewModel(recordsRepo, savedStateHandle)
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
    fun `isFromDisease is set from SavedStateHandle`() {
        assertEquals(0, viewModel.isFromDisease)
    }

    @Test
    fun `diseaseType is set from SavedStateHandle`() {
        assertEquals("LEPROSY", viewModel.diseaseType)
    }

    @Test
    fun `allBenList is not null`() {
        assertNotNull(viewModel.allBenList)
    }
}
