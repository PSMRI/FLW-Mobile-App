package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.list

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.IRSRoundScreening
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MalariaRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class MalariaIRSViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var malariaRepo: MalariaRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var recordsRepo: RecordsRepo

    private val savedStateHandle = SavedStateHandle(
        mapOf("hhId" to 1L, "fromDisease" to 0, "diseaseType" to "")
    )
    private lateinit var viewModel: MalariaIRSViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        val realDefaultDispatcher = Dispatchers.Default
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        every { Dispatchers.Default } returns realDefaultDispatcher
        coEvery { benRepo.getBenListFromHousehold(any()) } returns emptyList()
        every { recordsRepo.getLastIRSRoundBen(any()) } returns flowOf(null)
        viewModel = MalariaIRSViewModel(savedStateHandle, preferenceDao, context, malariaRepo, benRepo, recordsRepo)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(MalariaIRSViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `resetState sets state to IDLE`() {
        viewModel.resetState()
        assertEquals(MalariaIRSViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `formList is not null`() {
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `isSubmitVisible is not null`() {
        assertNotNull(viewModel.isSubmitVisible)
    }

    @Test
    fun `hhId is set from SavedStateHandle`() {
        assertEquals(1L, viewModel.hhId)
    }

    @Test
    fun `init populates the dataset when there is no last IRS record`() = runTest {
        advanceUntilIdle()
        assertNotNull(viewModel.formList.value)
    }

    @Test
    fun `init populates the dataset from an existing last IRS record`() = runTest {
        val lastRecord = IRSRoundScreening(id = 1, householdId = 1L)
        every { recordsRepo.getLastIRSRoundBen(1L) } returns flowOf(lastRecord)

        val vm = MalariaIRSViewModel(savedStateHandle, preferenceDao, context, malariaRepo, benRepo, recordsRepo)
        advanceUntilIdle()

        assertNotNull(vm.formList.value)
    }

    @Test
    fun `checkSubmitButtonVisibility hides the submit button once four rounds are recorded`() = runTest {
        coEvery { malariaRepo.getCount(1L) } returns 4

        viewModel.checkSubmitButtonVisibility()
        advanceUntilIdle()

        assertEquals(false, viewModel.isSubmitVisible.value)
    }

    @Test
    fun `checkSubmitButtonVisibility keeps the submit button visible under four rounds`() = runTest {
        coEvery { malariaRepo.getCount(1L) } returns 1

        viewModel.checkSubmitButtonVisibility()
        advanceUntilIdle()

        assertEquals(true, viewModel.isSubmitVisible.value)
    }

    @Test
    fun `saveForm does nothing when the repository disallows submission`() = runTest {
        advanceUntilIdle()
        coEvery { malariaRepo.canSubmit(1L) } returns false

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(MalariaIRSViewModel.State.IDLE, viewModel.state.value)
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { malariaRepo.saveIRSScreening(any()) }
    }

    @Test
    fun `saveForm saves the screening and sets SAVE_SUCCESS when submission is allowed`() = runTest {
        advanceUntilIdle()
        coEvery { malariaRepo.canSubmit(1L) } returns true
        coEvery { malariaRepo.saveIRSScreening(any()) } returns Unit
        coEvery { malariaRepo.getAllActiveIRSRecords(1L) } returns emptyList()
        coEvery { malariaRepo.updateIRSRecord(any()) } returns Unit

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(MalariaIRSViewModel.State.SAVE_SUCCESS, viewModel.state.value)
        coVerify { malariaRepo.saveIRSScreening(any()) }
        coVerify { malariaRepo.updateIRSRecord(any()) }
    }

    @Test
    fun `saveForm sets SAVE_FAILED when saving throws`() = runTest {
        advanceUntilIdle()
        coEvery { malariaRepo.canSubmit(1L) } returns true
        coEvery { malariaRepo.saveIRSScreening(any()) } throws RuntimeException("boom")

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(MalariaIRSViewModel.State.SAVE_FAILED, viewModel.state.value)
    }
}
