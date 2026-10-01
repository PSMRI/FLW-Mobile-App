package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.register

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
import io.mockk.mockkStatic
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class BenRegisterCHOViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var householdRepo: HouseholdRepo
    @MockK private lateinit var amritApiService: AmritApiService

    private lateinit var viewModel: BenRegisterCHOViewModel

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
        every { preferenceDao.getLoggedInUser() } returns buildUser()
        every { preferenceDao.getLocationRecord() } returns buildLocationRecord()
        viewModel = BenRegisterCHOViewModel(
            SavedStateHandle(),
            preferenceDao,
            context,
            benRepo,
            householdRepo,
            amritApiService
        )
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
    fun `state is not null`() {
        assertNotNull(viewModel.state)
    }

    @Test
    fun `benName is not null`() {
        assertNotNull(viewModel.benName)
    }

    @Test
    fun `recordExists is not null`() {
        assertNotNull(viewModel.recordExists)
    }

    private fun buildUser() = User(
        userId = 7,
        name = "CHO Worker",
        userName = "cho1",
        password = "pwd",
        role = "CHO",
        serviceMapId = 1,
        state = LocationEntity(1, "State"),
        district = LocationEntity(2, "District"),
        block = LocationEntity(3, "Block"),
        villages = emptyList()
    )

    private fun buildLocationRecord() = LocationRecord(
        LocationEntity(1, "India"),
        LocationEntity(1, "State"),
        LocationEntity(2, "District"),
        LocationEntity(3, "Block"),
        LocationEntity(4, "Village")
    )

    @Test
    fun `init resolves logged in user and location record without crashing`() = runTest {
        advanceUntilIdle()
        assertNotNull(viewModel)
    }

    @Test
    fun `saveForm creates a household when none exists and marks SAVE_SUCCESS`() = runTest {
        advanceUntilIdle()
        viewModel.updateValueByIdAndReturnListIndex(5, "9876543210")
        coEvery { householdRepo.getRecord(0L) } returns null
        coEvery { householdRepo.persistRecord(any(), any()) } just runs
        coEvery { benRepo.substituteBenIdForDraft(any()) } just runs
        coEvery { benRepo.persistRecord(any()) } just runs

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(BenRegisterCHOViewModel.State.SAVE_SUCCESS, viewModel.state.value)
        coVerify { householdRepo.persistRecord(any(), any()) }
        coVerify { benRepo.persistRecord(any()) }
    }

    @Test
    fun `saveForm skips household creation when a household record already exists`() = runTest {
        advanceUntilIdle()
        viewModel.updateValueByIdAndReturnListIndex(5, "9876543210")
        val existingHousehold = mockk<HouseholdCache>(relaxed = true)
        coEvery { householdRepo.getRecord(0L) } returns existingHousehold
        coEvery { benRepo.substituteBenIdForDraft(any()) } just runs
        coEvery { benRepo.persistRecord(any()) } just runs

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(BenRegisterCHOViewModel.State.SAVE_SUCCESS, viewModel.state.value)
        coVerify(exactly = 0) { householdRepo.persistRecord(any(), any()) }
    }

    @Test
    fun `saveForm marks SAVE_FAILED when a required field is missing`() = runTest {
        advanceUntilIdle()
        coEvery { householdRepo.getRecord(0L) } returns mockk<HouseholdCache>(relaxed = true)

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(BenRegisterCHOViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `saveForm marks SAVE_FAILED when persisting throws`() = runTest {
        advanceUntilIdle()
        viewModel.updateValueByIdAndReturnListIndex(5, "9876543210")
        coEvery { householdRepo.getRecord(0L) } returns mockk<HouseholdCache>(relaxed = true)
        coEvery { benRepo.substituteBenIdForDraft(any()) } throws RuntimeException("boom")

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(BenRegisterCHOViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `resetState sets state back to IDLE`() = runTest {
        advanceUntilIdle()
        viewModel.updateValueByIdAndReturnListIndex(5, "9876543210")
        coEvery { householdRepo.getRecord(0L) } returns mockk<HouseholdCache>(relaxed = true)
        coEvery { benRepo.substituteBenIdForDraft(any()) } just runs
        coEvery { benRepo.persistRecord(any()) } just runs

        viewModel.saveForm()
        advanceUntilIdle()
        viewModel.resetState()

        assertEquals(BenRegisterCHOViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `updateListOnValueChanged does not throw`() = runTest {
        advanceUntilIdle()
        viewModel.updateListOnValueChanged(0, 0)
        advanceUntilIdle()
        assertNotNull(viewModel)
    }

    @Test
    fun `getIndexOfAge and getIndexOfDob return dataset indices`() = runTest {
        advanceUntilIdle()
        assertNotNull(viewModel.getIndexOfAge())
        assertNotNull(viewModel.getIndexOfDob())
    }
}
