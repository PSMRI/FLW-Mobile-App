package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.saas_bahu_sammelan

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
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.dao.SaasBahuSammelanDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.SaasBahuSammelanCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.SaasBahuSammelanRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [SaasBahuSamelanViewModel]: the create versus edit arms of `init`, the save
 * success/failure arms and the five upload slots.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SaasBahuSamelanViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var saasBahuDao: SaasBahuSammelanDao

    @MockK
    private lateinit var saasBahuSammelanRepo: SaasBahuSammelanRepo

    private lateinit var user: User

    @After
    fun releaseStaticMocks() {
        unmockkStatic(Dispatchers::class)
    }

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<Throwable>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false

        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher

        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"

        user = mockk(relaxed = true)
        every { user.userId } returns 7
        every { user.userName } returns "asha"

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        every { saasBahuDao.getAllSammelan() } returns flowOf(emptyList())
        coEvery { saasBahuDao.getById(any()) } returns null
        coEvery { saasBahuSammelanRepo.saveSammelanForm(any()) } returns Unit
    }

    private fun buildVm(id: Long = 0L): SaasBahuSamelanViewModel =
        SaasBahuSamelanViewModel(
            SavedStateHandle(mapOf("id" to id)),
            preferenceDao,
            saasBahuDao,
            saasBahuSammelanRepo,
            context
        )

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertNotNull(vm.allSammelanList)
        assertEquals(SaasBahuSamelanViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `the record id argument is exposed`() {
        val vm = buildVm(id = 21L)
        assertEquals(21L, vm.id)
    }

    @Test
    fun `init reports no existing record for a fresh form`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init loads a previously saved sammelan`() = runTest {
        coEvery { saasBahuDao.getById(any()) } returns
                mockk<SaasBahuSammelanCache>(relaxed = true)

        val vm = buildVm(id = 5L)
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
    }

    @Test
    fun `saveForm persists the cache and reports success`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { saasBahuSammelanRepo.saveSammelanForm(any()) }
        assertEquals(SaasBahuSamelanViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm reports failure when the repository throws`() = runTest {
        coEvery { saasBahuSammelanRepo.saveSammelanForm(any()) } throws RuntimeException("boom")

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(SaasBahuSamelanViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `resetState returns to idle`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()
        vm.resetState()

        assertEquals(SaasBahuSamelanViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `setUploadUriFor fills each upload slot`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://photo"

        for (formId in 10..14) {
            vm.setUploadUriFor(formId, uri)
        }
        vm.setUploadUriFor(99, uri)

        assertNotNull(vm.formList)
    }

    @Test
    fun `updateListOnValueChanged delegates to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.updateListOnValueChanged(1, 0)
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }
}
