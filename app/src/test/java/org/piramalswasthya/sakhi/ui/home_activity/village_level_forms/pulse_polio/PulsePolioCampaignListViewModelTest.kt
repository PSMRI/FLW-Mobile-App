package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.pulse_polio

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.PulsePolioCampaignCache
import org.piramalswasthya.sakhi.repositories.VLFRepo
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PulsePolioCampaignListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var vlfRepo: VLFRepo

    private fun campaign(date: String) = PulsePolioCampaignCache(
        formDataJson = """{"fields":{"start_date":"$date"}}"""
    )

    @Before
    override fun setUp() {
        super.setUp()
        every { vlfRepo.pulsePolioCampaignList } returns flowOf(emptyList())
        coEvery { vlfRepo.getPulsePolioCampaignFromServer() } returns 1
        coEvery { vlfRepo.getAllPulsePolioCampaigns() } returns emptyList()
    }

    private fun newViewModel() = PulsePolioCampaignListViewModel(vlfRepo)

    @Test
    fun `viewModel initializes successfully`() {
        val vm = newViewModel()
        assertNotNull(vm)
    }

    @Test
    fun `checkCampaignEligibility blocks adding when two campaigns already exist this year`() = runTest {
        val year = LocalDate.now().year
        coEvery { vlfRepo.getAllPulsePolioCampaigns() } returns listOf(
            campaign("$year-01-10"),
            campaign("$year-02-10")
        )

        val vm = newViewModel()
        advanceUntilIdle()

        assertEquals(true, vm.isCampaignAlreadyAdded.value)
    }

    @Test
    fun `checkCampaignEligibility allows adding when there are no campaigns at all`() = runTest {
        coEvery { vlfRepo.getAllPulsePolioCampaigns() } returns emptyList()

        val vm = newViewModel()
        advanceUntilIdle()

        assertEquals(false, vm.isCampaignAlreadyAdded.value)
    }

    @Test
    fun `checkCampaignEligibility allows adding when the last campaign is more than 6 months old`() = runTest {
        val oldDate = LocalDate.now().minusMonths(8).toString()
        coEvery { vlfRepo.getAllPulsePolioCampaigns() } returns listOf(campaign(oldDate))

        val vm = newViewModel()
        advanceUntilIdle()

        assertEquals(false, vm.isCampaignAlreadyAdded.value)
    }

    @Test
    fun `checkCampaignEligibility blocks adding when the last campaign is within 6 months`() = runTest {
        val recentDate = LocalDate.now().minusMonths(1).toString()
        coEvery { vlfRepo.getAllPulsePolioCampaigns() } returns listOf(campaign(recentDate))

        val vm = newViewModel()
        advanceUntilIdle()

        assertEquals(true, vm.isCampaignAlreadyAdded.value)
    }

    @Test
    fun `checkCampaignEligibility sets false when the repository throws`() = runTest {
        coEvery { vlfRepo.getAllPulsePolioCampaigns() } throws RuntimeException("db error")

        val vm = newViewModel()
        advanceUntilIdle()

        assertEquals(false, vm.isCampaignAlreadyAdded.value)
    }

    @Test
    fun `init tolerates the server refresh failing`() = runTest {
        coEvery { vlfRepo.getPulsePolioCampaignFromServer() } throws RuntimeException("network error")

        val vm = newViewModel()
        advanceUntilIdle()

        assertNotNull(vm.isCampaignAlreadyAdded.value)
    }

    @Test
    fun `allPulsePolioCampaignList keeps only current-year campaigns sorted by most recent`() = runTest {
        val year = LocalDate.now().year
        every { vlfRepo.pulsePolioCampaignList } returns flowOf(
            listOf(
                campaign("$year-01-10"),
                campaign("${year - 1}-05-10"),
                campaign("$year-06-10")
            )
        )

        val vm = newViewModel()
        val list = vm.allPulsePolioCampaignList.first()

        assertEquals(2, list.size)
        assertEquals("$year-06-10", list[0].campaignDate)
    }

    @Test
    fun `allPulsePolioCampaignList returns empty list when no campaign matches the current year`() = runTest {
        every { vlfRepo.pulsePolioCampaignList } returns flowOf(
            listOf(campaign("2000-01-01"))
        )

        val vm = newViewModel()
        val list = vm.allPulsePolioCampaignList.first()

        assertEquals(0, list.size)
    }
}
