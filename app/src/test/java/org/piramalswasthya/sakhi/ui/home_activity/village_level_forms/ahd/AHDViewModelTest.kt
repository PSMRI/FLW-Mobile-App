package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.ahd

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AHDCache
import org.piramalswasthya.sakhi.repositories.VLFRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class AHDViewModelTest : BaseViewModelTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var vlfRepo: VLFRepo
    private lateinit var viewModel: AHDViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("id" to 0))

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class); every { Log.d(any(), any()) } returns 0; every { Log.e(any(), any()) } returns 0; every { Log.isLoggable(any(), any()) } returns false; every { Log.w(any<String>(), any<String>()) } returns 0; every { Log.w(any<String>(), any<Throwable>()) } returns 0
        mockkObject(HelperUtil); every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No"); every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        viewModel = AHDViewModel(savedStateHandle, preferenceDao, context, vlfRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(AHDViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(AHDViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }

    @Test
    fun `init sets recordExists false when no AHD record exists`() = runTest {
        coEvery { vlfRepo.getAHD(0) } returns null

        val vm = AHDViewModel(savedStateHandle, preferenceDao, context, vlfRepo)
        advanceUntilIdle()

        assertFalse(vm.recordExists.value!!)
    }

    @Test
    fun `init loads an existing AHD record and sets recordExists true`() = runTest {
        val cache = AHDCache(id = 5, mobilizedForAHD = "Loudspeaker")
        coEvery { vlfRepo.getAHD(0) } returns cache

        val vm = AHDViewModel(savedStateHandle, preferenceDao, context, vlfRepo)
        advanceUntilIdle()

        assertTrue(vm.recordExists.value!!)
    }

    @Test
    fun `saveForm marks SAVE_SUCCESS after persisting the record`() = runTest {
        coEvery { vlfRepo.getAHD(0) } returns AHDCache(id = 5, mobilizedForAHD = "Yes")
        coEvery { vlfRepo.saveAHDRecord(any()) } just runs
        val vm = AHDViewModel(savedStateHandle, preferenceDao, context, vlfRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(AHDViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm marks SAVE_FAILED when persisting throws`() = runTest {
        coEvery { vlfRepo.getAHD(0) } returns AHDCache(id = 5, mobilizedForAHD = "Yes")
        coEvery { vlfRepo.saveAHDRecord(any()) } throws RuntimeException("db error")
        val vm = AHDViewModel(savedStateHandle, preferenceDao, context, vlfRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(AHDViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `saveForm marks SAVE_FAILED when the mandatory field was never filled`() = runTest {
        coEvery { vlfRepo.getAHD(0) } returns null
        val vm = AHDViewModel(savedStateHandle, preferenceDao, context, vlfRepo)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(AHDViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `updateListOnValueChanged does not throw`() = runTest {
        coEvery { vlfRepo.getAHD(0) } returns null
        val vm = AHDViewModel(savedStateHandle, preferenceDao, context, vlfRepo)
        advanceUntilIdle()

        vm.updateListOnValueChanged(0, 0)
        advanceUntilIdle()

        assertNotNull(vm)
    }

    @Test
    fun `getCurrentFormList returns the dataset form elements`() {
        assertNotNull(viewModel.getCurrentFormList())
    }
}
