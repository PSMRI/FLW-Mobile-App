package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.confirmed.form

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
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
}
