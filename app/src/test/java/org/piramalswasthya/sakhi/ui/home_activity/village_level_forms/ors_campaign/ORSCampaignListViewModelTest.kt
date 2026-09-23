package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.ors_campaign

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.ORSCampaignCache
import org.piramalswasthya.sakhi.repositories.VLFRepo
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Unit tests for [ORSCampaignListViewModel]. `allORSCampaignList` keeps only campaigns dated in the
 * current calendar year AND within the last three months, sorted most-recent-first; eligibility to
 * add a new campaign depends on whether a full month has passed since the most recent campaign.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ORSCampaignListViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var vlfRepo: VLFRepo

    private fun campaign(dateStr: String?): ORSCampaignCache {
        val json = if (dateStr == null) null else
            """{"fields":{"start_date":"$dateStr"}}"""
        return ORSCampaignCache(formDataJson = json)
    }

    private fun buildVm(list: List<ORSCampaignCache> = emptyList()): ORSCampaignListViewModel {
        every { vlfRepo.orsCampaignList } returns flowOf(list)
        coEvery { vlfRepo.getORSCampaignFromServer() } returns 1
        coEvery { vlfRepo.getAllORSCampaigns() } returns emptyList()
        return ORSCampaignListViewModel(vlfRepo)
    }

    private fun isoDate(daysAgo: Long): String =
        LocalDate.now().minusDays(daysAgo).format(DateTimeFormatter.ISO_LOCAL_DATE)

    @Test
    fun `allORSCampaignList keeps a campaign from this year within the last three months`() = runTest {
        val vm = buildVm(listOf(campaign(isoDate(10))))
        val result = vm.allORSCampaignList.first()
        assertEquals(1, result.size)
    }

    @Test
    fun `allORSCampaignList excludes a campaign older than three months`() = runTest {
        val vm = buildVm(listOf(campaign(isoDate(200))))
        val result = vm.allORSCampaignList.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `allORSCampaignList excludes a campaign from a previous year`() = runTest {
        val lastYear = LocalDate.now().minusYears(1).withMonth(1).withDayOfMonth(1)
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
        val vm = buildVm(listOf(campaign(lastYear)))
        val result = vm.allORSCampaignList.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `allORSCampaignList excludes an entry with no parsable campaign date`() = runTest {
        val vm = buildVm(listOf(campaign(null), campaign("not-a-date")))
        val result = vm.allORSCampaignList.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `allORSCampaignList sorts eligible campaigns most-recent-first`() = runTest {
        val older = campaign(isoDate(20))
        val newer = campaign(isoDate(5))
        val vm = buildVm(listOf(older, newer))
        val result = vm.allORSCampaignList.first()
        assertEquals(listOf(newer, older), result)
    }

    @Test
    fun `isCampaignAlreadyAdded is false when no campaigns exist yet`() = runTest {
        every { vlfRepo.orsCampaignList } returns flowOf(emptyList())
        coEvery { vlfRepo.getORSCampaignFromServer() } returns 1
        coEvery { vlfRepo.getAllORSCampaigns() } returns emptyList()

        val vm = ORSCampaignListViewModel(vlfRepo)
        advanceUntilIdle()

        assertEquals(false, vm.isCampaignAlreadyAdded.value)
    }

    @Test
    fun `isCampaignAlreadyAdded is true when the latest campaign is under a month old`() = runTest {
        every { vlfRepo.orsCampaignList } returns flowOf(emptyList())
        coEvery { vlfRepo.getORSCampaignFromServer() } returns 1
        coEvery { vlfRepo.getAllORSCampaigns() } returns listOf(campaign(isoDate(2)))

        val vm = ORSCampaignListViewModel(vlfRepo)
        advanceUntilIdle()

        assertEquals(true, vm.isCampaignAlreadyAdded.value)
    }

    @Test
    fun `isCampaignAlreadyAdded is false once a month has passed since the latest campaign`() = runTest {
        every { vlfRepo.orsCampaignList } returns flowOf(emptyList())
        coEvery { vlfRepo.getORSCampaignFromServer() } returns 1
        coEvery { vlfRepo.getAllORSCampaigns() } returns listOf(campaign(isoDate(45)))

        val vm = ORSCampaignListViewModel(vlfRepo)
        advanceUntilIdle()

        assertEquals(false, vm.isCampaignAlreadyAdded.value)
    }

    @Test
    fun `checkCampaignEligibility tolerates a repository exception`() = runTest {
        every { vlfRepo.orsCampaignList } returns flowOf(emptyList())
        coEvery { vlfRepo.getORSCampaignFromServer() } returns 1
        coEvery { vlfRepo.getAllORSCampaigns() } throws RuntimeException("db error")

        val vm = ORSCampaignListViewModel(vlfRepo)
        advanceUntilIdle()

        assertEquals(false, vm.isCampaignAlreadyAdded.value)
    }
}
