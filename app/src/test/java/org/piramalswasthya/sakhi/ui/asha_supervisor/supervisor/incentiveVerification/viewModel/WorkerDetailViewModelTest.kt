package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel

import android.util.Log
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class WorkerDetailViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var apiService: AmritApiService
    @MockK private lateinit var preferenceDao: PreferenceDao

    private lateinit var viewModel: WorkerDetailViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        viewModel = WorkerDetailViewModel(apiService, preferenceDao)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `uiState is not null`() {
        assertNotNull(viewModel.uiState)
    }

    @Test
    fun `actionState is not null`() {
        assertNotNull(viewModel.actionState)
    }
}
