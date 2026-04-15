package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms

import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.VLFRepo
import org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.pulse_polio.PulsePolioCampaignListViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class PulsePolioCampaignListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var vlfRepo: VLFRepo

    private lateinit var viewModel: PulsePolioCampaignListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { vlfRepo.pulsePolioCampaignList } returns flowOf(emptyList())
        viewModel = PulsePolioCampaignListViewModel(vlfRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `allPulsePolioCampaignList flow is not null`() {
        assertNotNull(viewModel.allPulsePolioCampaignList)
    }

    @Test
    fun `isCampaignAlreadyAdded is not null`() {
        assertNotNull(viewModel.isCampaignAlreadyAdded)
    }
}
