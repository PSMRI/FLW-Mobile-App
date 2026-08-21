package org.piramalswasthya.sakhi.ui.home_activity.child_care.adolescent_list.form

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AdolescentHealthCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.repositories.AdolescentHealthRepo
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class AdolescentHealthFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var adolescentHealthRepo: AdolescentHealthRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: AdolescentHealthFormViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        every { mockResources.getStringArray(R.array.ahd_health_status_array) } returns
            arrayOf("Healthy", "Anemic", "Malnourished")
        every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { benRepo.getBenFromId(any()) } returns null
        viewModel = AdolescentHealthFormViewModel(savedStateHandle, preferenceDao, context, benRepo, adolescentHealthRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(AdolescentHealthFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `setRecordExist updates recordExists`() { viewModel.setRecordExist(true); assertEquals(true, viewModel.recordExists.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
    @Test fun `benId is set from SavedStateHandle`() { assertEquals(1L, viewModel.benId) }

    private fun buildBen(): BenRegCache {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.beneficiaryId } returns 1L
        every { ben.firstName } returns "Riya"
        every { ben.lastName } returns "Sharma"
        every { ben.age } returns 15
        every { ben.ageUnit } returns null
        every { ben.gender } returns null
        return ben
    }

    @Test
    fun `init populates ben name and age when ben exists with no prior record`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns buildBen()
        coEvery { adolescentHealthRepo.getAdolescentHealth(1L) } returns null

        val vm = AdolescentHealthFormViewModel(savedStateHandle, preferenceDao, context, benRepo, adolescentHealthRepo)
        advanceUntilIdle()

        assertEquals("Riya Sharma", vm.benName.value)
        assertFalse(vm.recordExists.value!!)
    }

    @Test
    fun `init formats ben name without a trailing name when lastName is null`() = runTest {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.beneficiaryId } returns 1L
        every { ben.firstName } returns "Riya"
        every { ben.lastName } returns null
        every { ben.age } returns 15
        every { ben.ageUnit } returns null
        every { ben.gender } returns null
        coEvery { benRepo.getBenFromId(1L) } returns ben
        coEvery { adolescentHealthRepo.getAdolescentHealth(1L) } returns null

        val vm = AdolescentHealthFormViewModel(savedStateHandle, preferenceDao, context, benRepo, adolescentHealthRepo)
        advanceUntilIdle()

        assertEquals("Riya ", vm.benName.value)
    }

    @Test
    fun `init loads an existing adolescent health record when present`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns buildBen()
        coEvery { adolescentHealthRepo.getAdolescentHealth(1L) } returns AdolescentHealthCache(benId = 1L)

        val vm = AdolescentHealthFormViewModel(savedStateHandle, preferenceDao, context, benRepo, adolescentHealthRepo)
        advanceUntilIdle()

        assertTrue(vm.recordExists.value!!)
    }

    @Test
    fun `init handles a missing ben gracefully`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns null
        coEvery { adolescentHealthRepo.getAdolescentHealth(1L) } returns null

        val vm = AdolescentHealthFormViewModel(savedStateHandle, preferenceDao, context, benRepo, adolescentHealthRepo)
        advanceUntilIdle()

        assertFalse(vm.recordExists.value!!)
        assertNotNull(vm)
    }

    @Test
    fun `saveForm marks state as SAVE_SUCCESS when there is no cached record`() = runTest {
        val vm = AdolescentHealthFormViewModel(savedStateHandle, preferenceDao, context, benRepo, adolescentHealthRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(AdolescentHealthFormViewModel.State.SAVE_SUCCESS, vm.state.value)
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { adolescentHealthRepo.saveAdolescentHealth(any()) }
    }

    @Test
    fun `saveForm persists the cached record and marks SAVE_SUCCESS`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns buildBen()
        coEvery { adolescentHealthRepo.getAdolescentHealth(1L) } returns AdolescentHealthCache(benId = 1L)
        coEvery { adolescentHealthRepo.saveAdolescentHealth(any()) } just runs

        val vm = AdolescentHealthFormViewModel(savedStateHandle, preferenceDao, context, benRepo, adolescentHealthRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(AdolescentHealthFormViewModel.State.SAVE_SUCCESS, vm.state.value)
        coVerify { adolescentHealthRepo.saveAdolescentHealth(any()) }
    }

    @Test
    fun `saveForm marks state as SAVE_FAILED when persisting throws`() = runTest {
        coEvery { benRepo.getBenFromId(1L) } returns buildBen()
        coEvery { adolescentHealthRepo.getAdolescentHealth(1L) } returns AdolescentHealthCache(benId = 1L)
        coEvery { adolescentHealthRepo.saveAdolescentHealth(any()) } throws RuntimeException("db error")

        val vm = AdolescentHealthFormViewModel(savedStateHandle, preferenceDao, context, benRepo, adolescentHealthRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(AdolescentHealthFormViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `updateListOnValueChanged does not throw`() = runTest {
        viewModel.updateListOnValueChanged(0, 0)
        advanceUntilIdle()
        assertNotNull(viewModel)
    }
}
