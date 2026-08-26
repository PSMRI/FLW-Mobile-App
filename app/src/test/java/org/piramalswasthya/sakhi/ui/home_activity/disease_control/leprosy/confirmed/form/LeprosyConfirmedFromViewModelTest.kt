package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.confirmed.form

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.LeprosyRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class LeprosyConfirmedFromViewModelTest : BaseViewModelTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var leprosyRepo: LeprosyRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    private lateinit var viewModel: LeprosyConfirmedFromViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))

    private fun screening(status: String?, visitNumber: Int): LeprosyScreeningCache {
        val cache = mockk<LeprosyScreeningCache>(relaxed = true)
        every { cache.beneficiaryStatus } returns status
        every { cache.currentVisitNumber } returns visitNumber
        return cache
    }

    private fun followUp(date: Long, visitNumber: Int): LeprosyFollowUpCache {
        val cache = mockk<LeprosyFollowUpCache>(relaxed = true)
        every { cache.followUpDate } returns date
        every { cache.visitNumber } returns visitNumber
        return cache
    }

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class); every { Log.d(any(), any()) } returns 0; every { Log.e(any(), any()) } returns 0; every { Log.isLoggable(any(), any()) } returns false
        mockkStatic(Dispatchers::class); every { Dispatchers.IO } returns testDispatcher
        mockkObject(HelperUtil); every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { context.resources } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }; every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
        coEvery { benRepo.getBenFromId(any()) } returns null
        // The view model throws IllegalStateException from init when no screening record is
        // found (uncaught, since it is outside the saveForm try/catch), so the baseline must
        // keep a screening record present to allow advanceUntilIdle() to run deterministically.
        coEvery { leprosyRepo.getLeprosyScreening(any()) } returns screening("Alive", 1)
        coEvery { leprosyRepo.getAllFollowUpsForBeneficiary(any()) } returns emptyList()
        viewModel = LeprosyConfirmedFromViewModel(savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(LeprosyConfirmedFromViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(LeprosyConfirmedFromViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `setRecordExist updates recordExists`() { viewModel.setRecordExist(true); assertEquals(true, viewModel.recordExists.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
    @Test fun `benId is set from SavedStateHandle`() { assertEquals(1L, viewModel.benId) }

    @Test
    fun `init populates the visit information from screening`() = runTest {
        advanceUntilIdle()
        assertEquals(true, viewModel.recordExists.value)
        assertEquals("Visit - 1", viewModel.visitInfo.value)
        assertEquals(false, viewModel.isBeneficaryStatusDeath.value)
    }

    @Test
    fun `init flags a deceased beneficiary`() = runTest {
        coEvery { leprosyRepo.getLeprosyScreening(any()) } returns screening("Death", 2)
        val vm = LeprosyConfirmedFromViewModel(savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()
        assertEquals(true, vm.isBeneficaryStatusDeath.value)
        assertEquals("Visit - 2", vm.visitInfo.value)
    }

    @Test
    fun `init has no last follow up when the visit has none`() = runTest {
        advanceUntilIdle()
        assertEquals(emptyList<LeprosyFollowUpCache>(), viewModel.followUpDates.value)
        assertNull(viewModel.lastFollowUp.value)
    }

    @Test
    fun `init picks the newest follow up of the current visit`() = runTest {
        val newest = followUp(3_000L, 1)
        coEvery { leprosyRepo.getAllFollowUpsForBeneficiary(any()) } returns
                listOf(followUp(1_000L, 1), newest, followUp(2_000L, 1))

        val vm = LeprosyConfirmedFromViewModel(savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        assertEquals(3, vm.followUpDates.value!!.size)
        assertEquals(newest, vm.lastFollowUp.value)
    }

    @Test
    fun `init populates ben name when beneficiary found`() = runTest {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.firstName } returns "Rajesh"
        every { ben.lastName } returns "Kumar"
        coEvery { benRepo.getBenFromId(any()) } returns ben

        val vm = LeprosyConfirmedFromViewModel(savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()

        assertEquals("Rajesh Kumar", vm.benName.value)
        assertNotNull(vm.benAgeGender.value)
    }

    @Test
    fun `clearErrorMessage resets error message to null`() {
        viewModel.clearErrorMessage()
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `saveForm posts SAVE_FAILED because follow up date is never set by setUpPage`() = runTest {
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(LeprosyConfirmedFromViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `updateListOnValueChanged does not throw for unknown form id`() = runTest {
        advanceUntilIdle()
        viewModel.updateListOnValueChanged(-999, 0)
        advanceUntilIdle()
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `getIndexOfDate delegates to the dataset`() = runTest {
        advanceUntilIdle()
        assertNotNull(viewModel.getIndexOfDate())
    }

    private fun futureDate(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_MONTH, 10)
        return SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cal.time)
    }

    private fun pastDate(daysAgo: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_MONTH, -daysAgo)
        return SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cal.time)
    }

    @Test
    fun `saveForm fails validation when follow-up date is blank`() = runTest {
        advanceUntilIdle()
        viewModel.formList.value.first { it.id == 12 }.value = ""

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(LeprosyConfirmedFromViewModel.State.SAVE_FAILED, viewModel.state.value)
        assertEquals("Follow-up date is required", viewModel.errorMessage.value)
    }

    @Test
    fun `saveForm fails validation when follow-up date is in the future`() = runTest {
        advanceUntilIdle()
        viewModel.formList.value.first { it.id == 12 }.value = futureDate()

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(LeprosyConfirmedFromViewModel.State.SAVE_FAILED, viewModel.state.value)
        assertEquals("Follow-up date cannot be in the future", viewModel.errorMessage.value)
    }

    @Test
    fun `saveForm fails validation when follow-up date precedes the home visit date`() = runTest {
        val screeningWithHomeVisit = screening("Alive", 1)
        every { screeningWithHomeVisit.homeVisitDate } returns System.currentTimeMillis()
        coEvery { leprosyRepo.getLeprosyScreening(any()) } returns screeningWithHomeVisit
        val vm = LeprosyConfirmedFromViewModel(savedStateHandle, preferenceDao, context, leprosyRepo, benRepo, maternalHealthRepo)
        advanceUntilIdle()
        vm.formList.value.first { it.id == 12 }.value = pastDate(10)

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(LeprosyConfirmedFromViewModel.State.SAVE_FAILED, vm.state.value)
        assertEquals("Follow-up date cannot be before home visit date", vm.errorMessage.value)
    }

    private fun completedStatusArray(): Array<String> =
        Array(80) { i -> if (i == 79) "Treatment Completed" else "opt$i" }

    private fun completedStatusArrayNotLast(): Array<String> =
        Array(80) { i -> if (i == 0) "Treatment Completed" else "opt$i" }

    private fun setFollowUpDate(vm: LeprosyConfirmedFromViewModel, date: String) {
        vm.formList.value.first { it.id == 12 }.value = date
        vm.updateListOnValueChanged(12, 0)
    }

    @Test
    fun `saveForm fails validation when treatment status is left blank`() = runTest {
        advanceUntilIdle()
        setFollowUpDate(viewModel, pastDate(5))
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(LeprosyConfirmedFromViewModel.State.SAVE_FAILED, viewModel.state.value)
        assertEquals("Treatment status is required", viewModel.errorMessage.value)
    }

    @Test
    fun `saveForm posts SAVE_SUCCESS through the plain else branch when treatment is not completed`() =
        runTest {
            advanceUntilIdle()
            setFollowUpDate(viewModel, pastDate(5))
            advanceUntilIdle()
            viewModel.formList.value.first { it.id == 19 }.value = "opt1"

            viewModel.saveForm()
            advanceUntilIdle()

            assertEquals(LeprosyConfirmedFromViewModel.State.SAVE_SUCCESS, viewModel.state.value)
            unmockkStatic(Dispatchers::class)
            coVerify(exactly = 0) { leprosyRepo.completeVisitAndStartNext(any()) }
        }

    @Test
    fun `saveForm falls back to SAVE_SUCCESS when the repo cannot complete the visit`() = runTest {
        every { mockResources.getStringArray(R.array.leprosy_treatment_status) } returns completedStatusArray()
        every { mockResources.getStringArray(R.array.leprosy_treatment_status_before_time) } returns completedStatusArray()
        coEvery { leprosyRepo.completeVisitAndStartNext(any()) } returns false
        advanceUntilIdle()
        setFollowUpDate(viewModel, pastDate(5))
        advanceUntilIdle()
        viewModel.formList.value.first { it.id == 19 }.value = "Treatment Completed"
        viewModel.updateListOnValueChanged(19, 0)
        advanceUntilIdle()
        viewModel.formList.value.first { it.id == 20 }.value = pastDate(2)

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(LeprosyConfirmedFromViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `saveForm posts VISIT_COMPLETED and refreshes screening when the repo completes the visit`() =
        runTest {
            every { mockResources.getStringArray(R.array.leprosy_treatment_status) } returns completedStatusArray()
            every { mockResources.getStringArray(R.array.leprosy_treatment_status_before_time) } returns completedStatusArray()
            coEvery { leprosyRepo.completeVisitAndStartNext(any()) } returns true
            advanceUntilIdle()
            setFollowUpDate(viewModel, pastDate(5))
            advanceUntilIdle()
            viewModel.formList.value.first { it.id == 19 }.value = "Treatment Completed"
            viewModel.updateListOnValueChanged(19, 0)
            advanceUntilIdle()
            viewModel.formList.value.first { it.id == 20 }.value = pastDate(2)

            viewModel.saveForm()
            advanceUntilIdle()

            assertEquals(LeprosyConfirmedFromViewModel.State.VISIT_COMPLETED, viewModel.state.value)
        }

    @Test
    fun `saveForm catches a repository exception during saveFollowUp and posts SAVE_FAILED`() =
        runTest {
            coEvery { leprosyRepo.saveFollowUp(any()) } throws RuntimeException("network down")
            advanceUntilIdle()
            setFollowUpDate(viewModel, pastDate(5))
            advanceUntilIdle()
            viewModel.formList.value.first { it.id == 19 }.value = "opt1"

            viewModel.saveForm()
            advanceUntilIdle()

            assertEquals(LeprosyConfirmedFromViewModel.State.SAVE_FAILED, viewModel.state.value)
        }

    @Test
    fun `saveForm skips visit completion when treatment is completed but no completion date was mapped`() =
        runTest {
            every { mockResources.getStringArray(R.array.leprosy_treatment_status) } returns completedStatusArrayNotLast()
            every { mockResources.getStringArray(R.array.leprosy_treatment_status_before_time) } returns completedStatusArrayNotLast()
            advanceUntilIdle()
            setFollowUpDate(viewModel, pastDate(5))
            advanceUntilIdle()
            viewModel.formList.value.first { it.id == 19 }.value = "Treatment Completed"

            viewModel.saveForm()
            advanceUntilIdle()

            assertEquals(LeprosyConfirmedFromViewModel.State.SAVE_SUCCESS, viewModel.state.value)
            unmockkStatic(Dispatchers::class)
            coVerify(exactly = 0) { leprosyRepo.completeVisitAndStartNext(any()) }
        }

    @Test
    fun `saveForm keeps the existing screening data when the refreshed screening is unavailable after completion`() =
        runTest {
            every { mockResources.getStringArray(R.array.leprosy_treatment_status) } returns completedStatusArray()
            every { mockResources.getStringArray(R.array.leprosy_treatment_status_before_time) } returns completedStatusArray()
            coEvery { leprosyRepo.completeVisitAndStartNext(any()) } returns true
            coEvery { leprosyRepo.getLeprosyScreening(any()) } returnsMany listOf(screening("Alive", 1), null)
            advanceUntilIdle()
            setFollowUpDate(viewModel, pastDate(5))
            advanceUntilIdle()
            viewModel.formList.value.first { it.id == 19 }.value = "Treatment Completed"
            viewModel.updateListOnValueChanged(19, 0)
            advanceUntilIdle()
            viewModel.formList.value.first { it.id == 20 }.value = pastDate(2)

            viewModel.saveForm()
            advanceUntilIdle()

            assertEquals(LeprosyConfirmedFromViewModel.State.VISIT_COMPLETED, viewModel.state.value)
        }

}
