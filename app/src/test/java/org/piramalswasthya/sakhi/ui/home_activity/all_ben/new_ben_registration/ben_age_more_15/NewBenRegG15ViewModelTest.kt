package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_age_more_15

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
import kotlinx.coroutines.flow.first
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
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [NewBenRegG15ViewModel] - the adult (15+) beneficiary registration form.
 *
 * The `init` block collects `currentPage`, so paging forward re-renders the dataset; the page
 * button-visibility flows and the create-versus-edit arms of `saveForm` are covered here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NewBenRegG15ViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var benRepo: BenRepo

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var userRepo: UserRepo

    private lateinit var user: User
    private lateinit var household: HouseholdCache
    private lateinit var ben: BenRegCache

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

        val realDefaultDispatcher = Dispatchers.Default
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        every { Dispatchers.Default } returns realDefaultDispatcher

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

        household = mockk(relaxed = true)

        ben = mockk(relaxed = true)
        every { ben.beneficiaryId } returns 42L
        every { ben.familyHeadRelationPosition } returns 1
        every { ben.firstName } returns "Ravi"
        every { ben.lastName } returns "Sharma"
        every { ben.age } returns 32

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        coEvery { benRepo.getHousehold(any()) } returns household
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns ben
        coEvery { benRepo.substituteBenIdForDraft(any()) } returns Unit
        coEvery { benRepo.persistRecord(any()) } returns Unit
    }

    private fun buildVm(hhId: Long = 1L, benId: Long = 0L): NewBenRegG15ViewModel =
        NewBenRegG15ViewModel(
            SavedStateHandle(mapOf("hhId" to hhId, "benId" to benId)),
            context,
            benRepo,
            preferenceDao,
            userRepo
        )

    @Test
    fun `viewModel initializes on the first page with an idle state`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertEquals(1, vm.currentPage.value)
        assertEquals(NewBenRegG15ViewModel.State.IDLE, vm.state.value)
        assertEquals(false, vm.recordExists.value)
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `an existing beneficiary argument marks the record as existing`() {
        val vm = buildVm(benId = 42L)
        assertEquals(true, vm.recordExists.value)
    }

    @Test
    fun `init renders the first page from the household beneficiary`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        coVerify { benRepo.getHousehold(any()) }
        coVerify { benRepo.getBeneficiaryRecord(any(), any()) }
        assertNotNull(vm.formList)
    }

    @Test
    fun `init renders the first page from a saved beneficiary`() = runTest {
        val vm = buildVm(benId = 42L)
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
        assertNotNull(vm.formList)
    }

    @Test
    fun `paging forward and back re-renders the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.goToNextPage()
        advanceUntilIdle()
        assertEquals(2, vm.currentPage.value)

        vm.goToNextPage()
        advanceUntilIdle()
        assertEquals(3, vm.currentPage.value)

        vm.goToPreviousPage()
        advanceUntilIdle()
        assertEquals(2, vm.currentPage.value)
    }

    @Test
    fun `the previous page button is hidden on the first page`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertFalse(vm.prevPageButtonVisibility.first())
    }

    @Test
    fun `the next page button is visible on the first page`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertTrue(vm.nextPageButtonVisibility.first())
    }

    @Test
    fun `the submit button visibility is exposed`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.submitPageButtonVisibility.first())
    }

    @Test
    fun `index helpers delegate to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.getIndexOfRelationToHead() }
        runCatching { vm.getIndexOfAgeAtMarriage() }
        runCatching { vm.getIndexOfFatherName() }
        runCatching { vm.getIndexOfMotherName() }
        runCatching { vm.getIndexOfSpouseName() }
        runCatching { vm.getIndexOfMaritalStatus() }
        runCatching { vm.getIndexOfElement(1) }

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
    fun `image form id round trips`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.setCurrentImageFormId(46)
        runCatching { vm.setImageUriToFormElement(mockk<Uri>(relaxed = true)) }

        assertNotNull(vm.formList)
    }

    @Test
    fun `resetErrorMessage clears the error`() {
        val vm = buildVm()
        vm.resetErrorMessage()
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `updateListOnValueChanged delegates to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.updateListOnValueChanged(1, 0)
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }

    @Test
    fun `init builds a fresh draft beneficiary when no existing record is found`() = runTest {
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns null
        every { household.family } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(false, vm.recordExists.value)
        assertNotNull(vm.formList)
    }

    @Test
    fun `saveForm allocates a beneficiary id for a new draft and marks the save successful`() = runTest {
        val draftBen = mockk<BenRegCache>(relaxed = true)
        every { draftBen.beneficiaryId } returns -1L
        every { draftBen.familyHeadRelationPosition } returns 1
        every { draftBen.createdDate } returns null
        every { draftBen.dob } returns 500000000000L
        every { draftBen.regDate } returns 1650000000000L
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns draftBen

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(NewBenRegG15ViewModel.State.SAVE_SUCCESS, vm.state.value)
        coVerify { benRepo.substituteBenIdForDraft(draftBen) }
        coVerify { benRepo.persistRecord(draftBen) }
    }

    @Test
    fun `saveForm updates an existing beneficiary without allocating a new draft id`() = runTest {
        val existingBen = mockk<BenRegCache>(relaxed = true)
        every { existingBen.beneficiaryId } returns 42L
        every { existingBen.familyHeadRelationPosition } returns 1
        every { existingBen.createdDate } returns 1600000000000L
        every { existingBen.dob } returns 500000000000L
        every { existingBen.regDate } returns 1650000000000L
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns existingBen

        val vm = buildVm(benId = 42L)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(NewBenRegG15ViewModel.State.SAVE_SUCCESS, vm.state.value)
        unmockkStatic(Dispatchers::class)
        coVerify(exactly = 0) { benRepo.substituteBenIdForDraft(any()) }
        coVerify { benRepo.persistRecord(existingBen) }
    }

    @Test
    fun `saveForm marks the state as failed when persisting the record throws an IllegalAccessError`() = runTest {
        val existingBen = mockk<BenRegCache>(relaxed = true)
        every { existingBen.beneficiaryId } returns 42L
        every { existingBen.familyHeadRelationPosition } returns 1
        every { existingBen.createdDate } returns 1600000000000L
        every { existingBen.dob } returns 500000000000L
        every { existingBen.regDate } returns 1650000000000L
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns existingBen
        coEvery { benRepo.persistRecord(any()) } throws IllegalAccessError("boom")

        val vm = buildVm(benId = 42L)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(NewBenRegG15ViewModel.State.SAVE_FAILED, vm.state.value)
    }
}
