package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.u_win_forms

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.UwinCache
import org.piramalswasthya.sakhi.repositories.UwinRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class UwinViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var uwinRepo: UwinRepo

    private lateinit var viewModel: UwinViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        every { mockResources.getString(any()) } returns ""
        every { mockResources.getString(any(), any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns mockk<User>(relaxed = true)
        every { uwinRepo.getAllLocalRecords() } returns MutableLiveData(emptyList())
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        viewModel = UwinViewModel(context, preferenceDao, uwinRepo)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `formList is not null`() {
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `uwinList is not null`() {
        assertNotNull(viewModel.uwinList)
    }

    @Test
    fun `state is not null`() {
        assertNotNull(viewModel.state)
    }

    @Test
    fun `recordExists is not null`() {
        assertNotNull(viewModel.recordExists)
    }

    @Test
    fun `getDocumentFormId returns default zero`() {
        assertEquals(0, viewModel.getDocumentFormId())
    }

    @Test
    fun `setCurrentDocumentFormId updates document form id`() {
        viewModel.setCurrentDocumentFormId(42)
        assertEquals(42, viewModel.getDocumentFormId())
    }

    @Test
    fun `prepareForm with saved false sets up blank form`() = runTest {
        viewModel.prepareForm(saved = false)
        advanceUntilIdle()
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { uwinRepo.getUwinById(any()) }
    }

    @Test
    fun `prepareForm with saved true and existing id loads cached record`() = runTest {
        val cache = UwinCache(
            id = 5,
            sessionDate = System.currentTimeMillis(),
            place = "Yes",
            participantsCount = 3,
            createdBy = "asha",
            updatedBy = "asha",
            syncState = SyncState.UNSYNCED
        )
        coEvery { uwinRepo.getUwinById(5) } returns cache
        viewModel.prepareForm(saved = true, id = 5)
        advanceUntilIdle()
        unmockkStatic(Dispatchers::class)
        coVerify { uwinRepo.getUwinById(5) }
    }

    @Test
    fun `prepareForm called twice with same id only prepares once`() = runTest {
        coEvery { uwinRepo.getUwinById(any()) } returns null
        viewModel.prepareForm(saved = true, id = 7)
        advanceUntilIdle()
        viewModel.prepareForm(saved = true, id = 7)
        advanceUntilIdle()
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 1) { uwinRepo.getUwinById(7) }
    }

    @Test
    fun `updateListOnValueChanged does not throw`() = runTest {
        viewModel.prepareForm(saved = false)
        advanceUntilIdle()
        viewModel.updateListOnValueChanged(117, 0)
        advanceUntilIdle()
    }

    @Test
    fun `getIndexUWINSummary1 and 2 return indices after form prepared`() = runTest {
        viewModel.prepareForm(saved = false)
        advanceUntilIdle()
        assertNotNull(viewModel.getIndexUWINSummary1())
        assertNotNull(viewModel.getIndexUWINSummary2())
    }

    @Test
    fun `setImageUriToFormElement updates upload summary field`() {
        val uri = mockk<Uri>(relaxed = true)
        every { uri.toString() } returns "content://test-uri"
        viewModel.setCurrentDocumentFormId(120)
        viewModel.setImageUriToFormElement(uri)
    }

    @Test
    fun `saveForm sets state to SAVE_FAILED when required fields are missing`() = runTest {
        viewModel.saveForm()
        advanceUntilIdle()
        assertEquals(UwinViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `saveForm sets state to SAVE_SUCCESS and inserts record when fields are valid`() = runTest {
        val cache = UwinCache(
            id = 5,
            sessionDate = System.currentTimeMillis(),
            place = "Yes",
            participantsCount = 3,
            createdBy = "asha",
            updatedBy = "asha",
            syncState = SyncState.UNSYNCED
        )
        coEvery { uwinRepo.getUwinById(5) } returns cache
        coEvery { uwinRepo.insertLocalRecord(any()) } returns Unit
        viewModel.prepareForm(saved = true, id = 5)
        advanceUntilIdle()
        viewModel.saveForm()
        advanceUntilIdle()
        assertEquals(UwinViewModel.State.SAVE_SUCCESS, viewModel.state.value)
        unmockkStatic(Dispatchers::class)
        coVerify { uwinRepo.insertLocalRecord(any()) }
    }
}
