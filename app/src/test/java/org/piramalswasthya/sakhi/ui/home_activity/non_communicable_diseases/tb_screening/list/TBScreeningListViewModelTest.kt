package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_screening.list

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithTbScreeningDomain
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.model.dynamicEntity.TPTFollowUpEntity
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.repositories.TBRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.TPTStartRepository

@OptIn(ExperimentalCoroutinesApi::class)
class TBScreeningListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var context: Context
    @MockK private lateinit var tbRepo: TBRepo
    @MockK private lateinit var tptRepo: TPTStartRepository

    private lateinit var viewModel: TBScreeningListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.tbScreeningList(hhId = 10L) } returns flowOf(emptyList())
        coEvery { tbRepo.getAllFollowUpsForBeneficiary(any()) } returns emptyList()
        coEvery { tptRepo.getFollowUps(any()) } returns emptyList()
        viewModel = TBScreeningListViewModel(
            context,
            recordsRepo,
            tbRepo,
            tptRepo,
            SavedStateHandle(
                mapOf(
                    "hhId" to 10L,
                    "fromDisease" to 0,
                    "diseaseType" to "TB"
                )
            )
        )
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benList flow is not null`() {
        assertNotNull(viewModel.benList)
    }

    // =====================================================
    // filterText() Tests
    // =====================================================

    @Test
    fun `filterText does not throw`() = runTest {
        viewModel.filterText("test")
        advanceUntilIdle()
    }

    @Test
    fun `filterText with empty string does not throw`() = runTest {
        viewModel.filterText("")
        advanceUntilIdle()
    }

    @Test
    fun `filterText with whitespace combine does not throw`() = runTest {
        viewModel.filterText("  tb  ")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `repeated filterText emissions do not throw`() = runTest {
        viewModel.filterText("a")
        viewModel.filterText("")
        viewModel.filterText("b")
        advanceUntilIdle()
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `benList collects real results once the flow is exercised`() = runTest {
        val result = viewModel.benList.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `presumptive case returns its message and skips confirmed and TPT checks`() = runTest {
        every { context.getString(R.string.tb_screening_presumptive_block) } returns "presumptive"
        val suspected = mockk<TBSuspectedCache> {
            every { isRecommendedForTpt } returns false
            every { isTBConfirmed } returns false
            every { isConfirmed } returns false
        }

        assertEquals("presumptive", viewModel.addScreeningBlockMessage(beneficiary(suspected)))
        coVerify(exactly = 0) { tbRepo.getAllFollowUpsForBeneficiary(any()) }
        coVerify(exactly = 0) { tptRepo.getFollowUps(any()) }
    }

    @Test
    fun `confirmed TB case without a treatment follow-up still blocks screening`() = runTest {
        every { context.getString(R.string.not_available) } returns "Not available"
        every { context.getString(R.string.tb_screening_confirm_followup_not_started, any()) } returns "TB follow-up not started"
        val suspected = mockk<TBSuspectedCache> {
            every { isRecommendedForTpt } returns false
            every { isTBConfirmed } returns true
            every { isConfirmed } returns true
        }

        assertEquals("TB follow-up not started", viewModel.addScreeningBlockMessage(beneficiary(suspected)))
    }

    @Test
    fun `ongoing confirmed TB treatment returns its completion date message`() = runTest {
        every { context.getString(R.string.tb_screening_confirm_treatment_ongoing, any()) } returns "TB ongoing"
        val suspected = mockk<TBSuspectedCache> {
            every { isRecommendedForTpt } returns false
            every { isTBConfirmed } returns true
            every { isConfirmed } returns true
        }
        coEvery { tbRepo.getAllFollowUpsForBeneficiary(42L) } returns listOf(
            TBConfirmedTreatmentCache(
                benId = 42L,
                expectedTreatmentCompletionDate = 1_735_689_600_000L,
                followUpDate = 1_700_000_000_000L,
                treatmentCompleted = false
            )
        )

        assertEquals("TB ongoing", viewModel.addScreeningBlockMessage(beneficiary(suspected)))
    }

    @Test
    fun `TPT recommended case without follow-up still blocks screening`() = runTest {
        every { context.getString(R.string.not_available) } returns "Not available"
        every { context.getString(R.string.tb_screening_tpt_followup_not_started, any()) } returns "TPT follow-up not started"
        val suspected = mockk<TBSuspectedCache> {
            every { isRecommendedForTpt } returns true
            every { isTBConfirmed } returns false
            every { isConfirmed } returns false
        }

        assertEquals("TPT follow-up not started", viewModel.addScreeningBlockMessage(beneficiary(suspected)))
    }

    @Test
    fun `ongoing TPT treatment returns its completion date message`() = runTest {
        every { context.getString(R.string.tb_screening_tpt_treatment_ongoing, any()) } returns "TPT ongoing"
        val suspected = mockk<TBSuspectedCache> {
            every { isRecommendedForTpt } returns true
            every { isTBConfirmed } returns false
            every { isConfirmed } returns false
        }
        coEvery { tptRepo.getFollowUps(42L) } returns listOf(
            TPTFollowUpEntity(
                benId = 42L,
                houseHoldId = 7L,
                visitNo = 1,
                followUpNo = 1,
                treatmentType = "TPT",
                treatmentStartDate = "01-01-2025",
                followUpDate = "01-02-2025",
                fieldsJson = """{"treatment_completed":"No","expected_treatment_completion_date":"31-12-2025"}"""
            )
        )

        assertEquals("TPT ongoing", viewModel.addScreeningBlockMessage(beneficiary(suspected)))
    }

    private fun beneficiary(suspected: TBSuspectedCache?): BenWithTbScreeningDomain {
        val item = mockk<BenWithTbScreeningDomain>()
        val ben = mockk<BenBasicDomain>()
        every { item.ben } returns ben
        every { ben.benId } returns 42L
        every { item.tbSuspected } returns suspected
        return item
    }
}
