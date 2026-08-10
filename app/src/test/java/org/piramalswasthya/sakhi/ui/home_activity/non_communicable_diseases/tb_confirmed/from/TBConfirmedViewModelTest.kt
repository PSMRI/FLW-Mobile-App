package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tb_confirmed.from

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.TBRepo
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class TBConfirmedViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var tbRepo: TBRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: TBConfirmedViewModel
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
        every { Dispatchers.Default } returns testDispatcher
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        coEvery { benRepo.getBenFromId(any()) } returns null
        viewModel = TBConfirmedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(TBConfirmedViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(TBConfirmedViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
    @Test fun `benId is set from SavedStateHandle`() { assertEquals(1L, viewModel.benId) }

    @Test
    fun `longToDateString with null millis returns empty string`() {
        assertEquals("", viewModel.longToDateString(null))
    }

    @Test
    fun `longToDateString with non-null millis returns formatted date string`() {
        val millis = 1700000000000L
        val expected = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(millis))
        assertEquals(expected, viewModel.longToDateString(millis))
    }

    @Test
    fun `updateListOnValueChanged does not crash`() {
        viewModel.updateListOnValueChanged(1, 0)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `record exists true sets recordExists and followUpDates`() {
        val tbConfirmedTreatmentCache = TBConfirmedTreatmentCache(benId = 1L)
        coEvery { tbRepo.getTBConfirmed(any()) } returns tbConfirmedTreatmentCache
        coEvery { tbRepo.getTBSuspected(any()) } returns null
        coEvery { tbRepo.getAllFollowUpsForBeneficiary(any()) } returns emptyList()
        coEvery { benRepo.getBenFromId(any()) } returns null

        val localViewModel =
            TBConfirmedViewModel(savedStateHandle, preferenceDao, context, tbRepo, benRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, localViewModel.recordExists.value)
        assertNotNull(localViewModel.followUpDates.value)
    }

    @Test
    fun `saveForm with invalid default form fails validation and sets SAVE_FAILED`() {
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        viewModel.saveForm()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TBConfirmedViewModel.State.SAVE_FAILED, viewModel.state.value)
    }
}
