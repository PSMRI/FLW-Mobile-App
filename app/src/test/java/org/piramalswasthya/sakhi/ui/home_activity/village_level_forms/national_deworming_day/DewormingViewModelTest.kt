package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.national_deworming_day

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.DewormingCache
import org.piramalswasthya.sakhi.repositories.VLFRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class DewormingViewModelTest : BaseViewModelTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var vlfRepo: VLFRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { vlfRepo.dewormingList } returns flowOf(emptyList())
        coEvery { vlfRepo.getDeworming(any()) } returns null
        coEvery { vlfRepo.saveDeworming(any<DewormingCache>()) } returns Unit
    }

    private fun buildVm(id: Int = 0): DewormingViewModel =
        DewormingViewModel(
            SavedStateHandle(mapOf("id" to id)),
            preferenceDao,
            context,
            vlfRepo
        )

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(buildVm())
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(DewormingViewModel.State.IDLE, buildVm().state.value)
    }

    @Test
    fun `resetState sets state to IDLE`() {
        val vm = buildVm()
        vm.resetState()
        assertEquals(DewormingViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `formList is not null`() {
        assertNotNull(buildVm().formList)
    }

    @Test
    fun `allDewormingList exposes the repo flow`() {
        assertNotNull(buildVm().allDewormingList)
    }

    @Test
    fun `init reports no existing record when repo has none`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init reports existing record when repo returns cache`() = runTest {
        coEvery { vlfRepo.getDeworming(7) } returns DewormingCache(id = 7, dewormingDone = "Yes")
        val vm = buildVm(id = 7)
        advanceUntilIdle()
        assertEquals(true, vm.recordExists.value)
    }

    @Test
    fun `getCurrentFormList returns the dataset elements after init`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()
        assertNotNull(vm.getCurrentFormList())
    }

    @Test
    fun `setImageUriToFormElement updates the dataset for the last selected image field`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://pic"

        vm.setCurrentImageFormId(1)
        vm.setImageUriToFormElement(uri)

        val pic1 = vm.getCurrentFormList().first { it.id == 1 }
        assertEquals("content://pic", pic1.value)
    }

    @Test
    fun `updateListOnValueChanged does not throw`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()
        vm.updateListOnValueChanged(6, 0)
        advanceUntilIdle()
        assertNotNull(vm.formList)
    }

    @Test
    fun `saveForm success moves state to SAVE_SUCCESS and persists via repo`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.getCurrentFormList().first { it.id == 6 }.value = "Yes"
        vm.getCurrentFormList().first { it.id == 3 }.value = "5"

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(DewormingViewModel.State.SAVE_SUCCESS, vm.state.value)
        coVerify { vlfRepo.saveDeworming(any<DewormingCache>()) }
    }

    @Test
    fun `saveForm sets SAVE_FAILED when repo throws`() = runTest {
        coEvery { vlfRepo.saveDeworming(any<DewormingCache>()) } throws RuntimeException("db error")
        val vm = buildVm()
        advanceUntilIdle()

        vm.getCurrentFormList().first { it.id == 6 }.value = "Yes"
        vm.getCurrentFormList().first { it.id == 3 }.value = "5"

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(DewormingViewModel.State.SAVE_FAILED, vm.state.value)
    }
}
