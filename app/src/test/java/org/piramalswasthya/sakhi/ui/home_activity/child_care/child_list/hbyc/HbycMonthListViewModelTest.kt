package org.piramalswasthya.sakhi.ui.home_activity.child_care.child_list.hbyc

import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.HbycRepo

@OptIn(ExperimentalCoroutinesApi::class)
class HbycMonthListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var hbycRepo: HbycRepo

    private lateinit var viewModel: HbycMonthListViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "benId" to 1L,
        "hhId" to 1L
    ))

    @Before
    override fun setUp() {
        super.setUp()
        every { hbycRepo.hbycList(any(), any()) } returns flowOf(emptyList())
        viewModel = HbycMonthListViewModel(hbycRepo, savedStateHandle)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `dayList is not null`() {
        assertNotNull(viewModel.dayList)
    }
}
