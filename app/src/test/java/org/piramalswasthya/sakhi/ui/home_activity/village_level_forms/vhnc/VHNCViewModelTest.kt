package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.vhnc

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.VHNCCache
import org.piramalswasthya.sakhi.repositories.VLFRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class VHNCViewModelTest : BaseViewModelTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var vlfRepo: VLFRepo
    private val savedStateHandle = SavedStateHandle(mapOf("id" to 0))

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class); every { Log.d(any(), any()) } returns 0; every { Log.e(any(), any()) } returns 0; every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil); every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }; every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { vlfRepo.getVHNC(any()) } returns null
        every { vlfRepo.vhncList } returns flowOf(emptyList())
    }

    private fun buildVm(): VHNCViewModel = VHNCViewModel(savedStateHandle, preferenceDao, context, vlfRepo)

    @Test fun `viewModel initializes successfully`() { assertNotNull(buildVm()) }
    @Test fun `initial state is IDLE`() { assertEquals(VHNCViewModel.State.IDLE, buildVm().state.value) }
    @Test fun `resetState sets state to IDLE`() {
        val viewModel = buildVm()
        viewModel.resetState()
        assertEquals(VHNCViewModel.State.IDLE, viewModel.state.value)
    }
    @Test fun `formList is not null`() { assertNotNull(buildVm().formList) }

    @Test
    fun `init resolves recordExists false when no existing record`() = runTest {
        coEvery { vlfRepo.getVHNC(any()) } returns null
        val viewModel = buildVm()

        advanceUntilIdle()

        assertFalse(viewModel.recordExists.value == true)
    }

    @Test
    fun `init resolves recordExists true when record found`() = runTest {
        coEvery { vlfRepo.getVHNC(0) } returns VHNCCache(id = 5, vhncDate = "01-01-2026")
        val viewModel = buildVm()

        advanceUntilIdle()

        assertTrue(viewModel.recordExists.value == true)
    }

    @Test
    fun `saveForm sets SAVE_SUCCESS when repo save succeeds`() = runTest {
        coEvery { vlfRepo.getVHNC(any()) } returns null
        coEvery { vlfRepo.saveRecord(any<VHNCCache>()) } returns Unit
        val viewModel = buildVm()
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(VHNCViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `saveForm sets SAVE_FAILED when repo save throws`() = runTest {
        coEvery { vlfRepo.getVHNC(any()) } returns null
        coEvery { vlfRepo.saveRecord(any<VHNCCache>()) } throws RuntimeException("db error")
        val viewModel = buildVm()
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(VHNCViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `updateListOnValueChanged with unmatched formId does not throw`() = runTest {
        coEvery { vlfRepo.getVHNC(any()) } returns null
        val viewModel = buildVm()
        advanceUntilIdle()

        viewModel.updateListOnValueChanged(-999, 0)
        advanceUntilIdle()

        assertNotNull(viewModel.formList)
    }

    @Test
    fun `setImageUriToFormElement with unmatched formId does not throw`() = runTest {
        coEvery { vlfRepo.getVHNC(any()) } returns null
        val viewModel = buildVm()
        advanceUntilIdle()
        val uri = mockk<Uri>(relaxed = true)

        viewModel.setCurrentImageFormId(-999)
        viewModel.setImageUriToFormElement(uri)

        assertNotNull(viewModel.formList)
    }
}
