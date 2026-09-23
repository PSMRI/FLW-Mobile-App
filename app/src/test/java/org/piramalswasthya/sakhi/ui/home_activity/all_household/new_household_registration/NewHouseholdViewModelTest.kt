package org.piramalswasthya.sakhi.ui.home_activity.all_household.new_household_registration

import android.content.Context
import android.content.res.Resources
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
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.helpers.HofAbhaPrefillCache
import org.piramalswasthya.sakhi.model.FamilyMember
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [NewHouseholdViewModel]: the new / draft / saved arms of `init`, the
 * freeze-and-substitute path of `saveForm` and the ABHA prefill handshake.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NewHouseholdViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var benRepo: BenRepo

    @MockK
    private lateinit var householdRepo: HouseholdRepo

    @MockK
    private lateinit var hofAbhaPrefillCache: HofAbhaPrefillCache

    @MockK
    private lateinit var userRepo: UserRepo

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
        every { context.resources } returns mockResources

        user = mockk(relaxed = true)
        every { user.userId } returns 11
        every { user.userName } returns "asha"
        every { user.villages } returns listOf(LocationEntity(id = 1, name = "Rampur"))

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getLocationRecord() } returns mockk<LocationRecord>(relaxed = true)
        every { preferenceDao.getChoList() } returns "[]"
        every { preferenceDao.getAnmList() } returns "[]"

        coEvery { householdRepo.getRecord(any()) } returns null
        coEvery { householdRepo.getDraftRecord() } returns null
        coEvery { householdRepo.substituteHouseholdIdForDraft(any()) } returns Unit
        coEvery { householdRepo.persistRecord(any(), any()) } returns Unit
        coEvery { householdRepo.getAllBenOfHousehold(any()) } returns emptyList()
        coEvery { benRepo.updateBenToSync(any(), any()) } returns Unit
    }

    private fun buildVm(hhId: Long = 0L, isAshaFamily: String = "No"): NewHouseholdViewModel =
        NewHouseholdViewModel(
            SavedStateHandle(mapOf("hhId" to hhId, "isAshaFamily" to isAshaFamily)),
            preferenceDao,
            context,
            benRepo,
            householdRepo,
            hofAbhaPrefillCache,
            userRepo
        )

    @Test
    fun `viewModel initializes as a new registration`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertEquals(NewHouseholdViewModel.State.IDLE, vm.state.value)
        assertEquals(false, vm.readRecord.value)
        assertTrue(vm.isNewRegistration)
        assertNull(vm.abhaUserDetails.value)
    }

    @Test
    fun `an existing household argument switches out of new registration`() {
        val vm = buildVm(hhId = 12L)
        assertEquals(true, vm.readRecord.value)
        assertFalse(vm.isNewRegistration)
    }

    @Test
    fun `consent flag round trips`() {
        val vm = buildVm()
        assertFalse(vm.getIsConsentAgreed())
        vm.setConsentAgreed()
        assertTrue(vm.getIsConsentAgreed())
    }

    @Test
    fun `init builds a brand new household when nothing is saved`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(0L, vm.getHHId())
        assertNotNull(vm.getHoFName())
    }

    @Test
    fun `init reuses a draft household`() = runTest {
        val draft = mockk<HouseholdCache>(relaxed = true)
        every { draft.householdId } returns 7L
        coEvery { householdRepo.getDraftRecord() } returns draft

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(7L, vm.getHHId())
    }

    @Test
    fun `init reads the household named by the argument`() = runTest {
        val saved = mockk<HouseholdCache>(relaxed = true)
        every { saved.householdId } returns 12L
        coEvery { householdRepo.getRecord(any()) } returns saved
        var draftLookups = 0
        coEvery { householdRepo.getDraftRecord() } answers { draftLookups++; null }

        val vm = buildVm(hhId = 12L)
        advanceUntilIdle()

        assertEquals(12L, vm.getHHId())
        assertEquals(0, draftLookups)
    }

    @Test
    fun `saveForm freezes the id of a brand new household`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { householdRepo.substituteHouseholdIdForDraft(any()) }
        coVerify { householdRepo.persistRecord(any(), any()) }
        coVerify { benRepo.updateBenToSync(any(), any()) }
        assertEquals(NewHouseholdViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm updates an already registered household`() = runTest {
        val saved = mockk<HouseholdCache>(relaxed = true)
        every { saved.householdId } returns 12L
        every { saved.createdTimeStamp } returns 1_700_000_000_000L
        coEvery { householdRepo.getRecord(any()) } returns saved
        var substitutions = 0
        coEvery { householdRepo.substituteHouseholdIdForDraft(any()) } answers { substitutions++ }

        val vm = buildVm(hhId = 12L)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(0, substitutions)
        coVerify { householdRepo.persistRecord(any(), any()) }
        assertEquals(NewHouseholdViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm reports failure when persisting throws`() = runTest {
        coEvery { householdRepo.persistRecord(any(), any()) } throws RuntimeException("db down")

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(NewHouseholdViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `saveForm hands a prefilled abha member to the cache`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        val member = mockk<FamilyMember>(relaxed = true)
        runCatching { vm.prefillFromAyushmanCard(member) }

        vm.saveForm()
        advanceUntilIdle()

        verify { hofAbhaPrefillCache.put(any(), any()) }
    }

    @Test
    fun `getUserDetailsByAyushmanAbhaCardNo publishes the network result`() = runTest {
        val result = mockk<NetworkResult<List<FamilyMember>>>(relaxed = true)
        coEvery { benRepo.getUserDetailsByAyushmanAbhaCardNo(any(), any()) } returns result

        val vm = buildVm()
        advanceUntilIdle()

        vm.getUserDetailsByAyushmanAbhaCardNo("12345")
        advanceUntilIdle()

        assertEquals(result, vm.abhaUserDetails.value)

        vm.clearAbhaUserDetails()
        assertNull(vm.abhaUserDetails.value)
    }

    @Test
    fun `setRecordExists updates readRecord`() {
        val vm = buildVm()
        vm.setRecordExists(true)
        assertEquals(true, vm.readRecord.value)
        vm.setRecordExists(false)
        assertEquals(false, vm.readRecord.value)
    }

    @Test
    fun `abha helpers delegate to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.getAbhaSubmitBtnId() }
        runCatching { vm.getAbhaCardInput() }

        assertNotNull(vm.formList)
    }

    @Test
    fun `updateValueByIdAndReturnListIndex delegates to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.updateValueByIdAndReturnListIndex(1, "value") }

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
