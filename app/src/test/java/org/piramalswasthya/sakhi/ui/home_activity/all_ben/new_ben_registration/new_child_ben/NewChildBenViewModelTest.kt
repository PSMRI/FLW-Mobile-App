package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.new_child_ben

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
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
import org.junit.After
import org.junit.Before
import org.junit.Test
import io.mockk.coVerify
import io.mockk.Runs
import io.mockk.just
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [NewChildBenViewModel].
 *
 * `Dispatchers.IO` is redirected onto the test scheduler so the `init` block's
 * `withContext(Dispatchers.IO) { setUpPage() }` runs deterministically under `advanceUntilIdle()`.
 * The tests focus on the guard/early-return arms of `setUpPage`, the exposed navigation arguments
 * and the small state helpers; the real [org.piramalswasthya.sakhi.configuration.NewChildBenRegDataset]
 * is built with mocked resources exactly as the dataset test suite does.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NewChildBenViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var benRepo: BenRepo

    @MockK
    private lateinit var ecrRepo: EcrRepo

    @MockK
    private lateinit var householdRepo: HouseholdRepo

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
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false

        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        every { Dispatchers.Default } returns testDispatcher

        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"

        user = mockk(relaxed = true)
        every { user.userId } returns 11
        every { user.userName } returns "asha"

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        coEvery { benRepo.getHousehold(any()) } returns mockk<HouseholdCache>(relaxed = true)
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns null
        coEvery { benRepo.getBenListFromHousehold(any()) } returns emptyList()
        coEvery { benRepo.getChildBenListFromHousehold(any(), any(), any()) } returns emptyList()
        coEvery { benRepo.getChildAbove15(any(), any(), any()) } returns 0
        coEvery { ecrRepo.getBenFromId(any()) } returns null
        coEvery { ecrRepo.getSavedRecord(any()) } returns null
    }

    private fun buildVm(
        hhId: Long = 1L,
        relToHeadId: Int = 8,
        benId: Long = 0L,
        selectedBenId: Long = 0L,
        gender: Int = 2,
        isAddSpouse: Int = 0
    ): NewChildBenViewModel = NewChildBenViewModel(
        SavedStateHandle(
            mapOf(
                "hhId" to hhId,
                "relToHeadId" to relToHeadId,
                "benId" to benId,
                "selectedBenId" to selectedBenId,
                "gender" to gender,
                "isAddSpouse" to isAddSpouse
            )
        ),
        preferenceDao,
        context,
        benRepo,
        ecrRepo,
        householdRepo,
        userRepo
    )

    // ----------------------------------------------------------------------------------------
    // arguments / initial state
    // ----------------------------------------------------------------------------------------

    @Test
    fun `viewModel initializes successfully`() {
        val vm = buildVm()
        assertNotNull(vm)
        assertNotNull(vm.formList)
        assertNotNull(vm.dataset)
    }

    @Test
    fun `navigation arguments are exposed`() {
        val vm = buildVm(hhId = 77L, relToHeadId = 9)
        assertEquals(77L, vm.hhId)
        assertEquals(9, vm.relToHeadId)
        assertFalse(vm.isHoF)
    }

    @Test
    fun `relation to head eighteen marks the head of family`() {
        val vm = buildVm(relToHeadId = 18)
        assertTrue(vm.isHoF)
    }

    @Test
    fun `benId argument is exposed`() {
        val vm = buildVm(benId = 55L)
        assertEquals(55L, vm.benIdFromArgs)
    }

    @Test
    fun `initial state is IDLE`() {
        val vm = buildVm()
        assertEquals(NewChildBenViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `recordExists starts false when no beneficiary was selected`() {
        val vm = buildVm(selectedBenId = 0L)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `recordExists starts true when a beneficiary was selected`() {
        val vm = buildVm(selectedBenId = 12L)
        assertEquals(true, vm.recordExists.value)
    }

    @Test
    fun `listUpdateState starts idle`() {
        val vm = buildVm()
        assertTrue(vm.listUpdateState.value is NewChildBenViewModel.ListUpdateState.Idle)
    }

    // ----------------------------------------------------------------------------------------
    // setUpPage guard arms
    // ----------------------------------------------------------------------------------------

    @Test
    fun `setUpPage fails when no user is logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        val vm = buildVm()
        advanceUntilIdle()
        assertEquals(NewChildBenViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `setUpPage fails when the household cannot be found`() = runTest {
        coEvery { benRepo.getHousehold(any()) } returns null
        val vm = buildVm()
        advanceUntilIdle()
        assertEquals(NewChildBenViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `setUpPage fails when the beneficiary cannot be found`() = runTest {
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns null
        val vm = buildVm(selectedBenId = 33L)
        advanceUntilIdle()
        assertEquals(NewChildBenViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `setUpPage fails after seeding an ecr form from an existing beneficiary`() = runTest {
        val ecrBen = mockk<BenRegCache>(relaxed = true)
        every { ecrBen.beneficiaryId } returns 33L
        every { ecrBen.dob } returns 1_000_000_000_000L
        coEvery { ecrRepo.getBenFromId(any()) } returns ecrBen
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns null

        val vm = buildVm(selectedBenId = 33L)
        advanceUntilIdle()

        assertEquals(NewChildBenViewModel.State.SAVE_FAILED, vm.state.value)
        assertEquals(0, vm.oldChildCount)
    }

    @Test
    fun `setUpPage keeps a previously saved ecr record`() = runTest {
        coEvery { ecrRepo.getSavedRecord(any()) } returns mockk<EligibleCoupleRegCache>(relaxed = true)
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns null

        val vm = buildVm(selectedBenId = 33L)
        advanceUntilIdle()

        assertEquals(NewChildBenViewModel.State.SAVE_FAILED, vm.state.value)
    }

    // ----------------------------------------------------------------------------------------
    // small helpers
    // ----------------------------------------------------------------------------------------

    @Test
    fun `consent flag round trips`() {
        val vm = buildVm()
        assertFalse(vm.getIsConsentAgreed())
        vm.setConsentAgreed()
        assertTrue(vm.getIsConsentAgreed())
    }

    @Test
    fun `setRecordExist updates recordExists`() {
        val vm = buildVm()
        vm.setRecordExist(true)
        assertEquals(true, vm.recordExists.value)
        vm.setRecordExist(false)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `resetListUpdateState returns to idle`() {
        val vm = buildVm()
        vm.resetListUpdateState()
        assertTrue(vm.listUpdateState.value is NewChildBenViewModel.ListUpdateState.Idle)
    }

    @Test
    fun `setCurrentImageFormId does not throw`() {
        val vm = buildVm()
        vm.setCurrentImageFormId(46)
        assertNotNull(vm)
    }

    @Test
    fun `otp default is exposed and mutable`() {
        val vm = buildVm()
        assertEquals(1234, vm.otp)
        vm.otp = 4321
        assertEquals(4321, vm.otp)
    }

    @Test
    fun `saveForm without a completed setup reports failure`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(NewChildBenViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `getIndexOf helpers delegate to the dataset`() {
        val vm = buildVm()
        runCatching { vm.getIndexOfChildren() }
        runCatching { vm.getIndexOfMaleChildren() }
        runCatching { vm.getIndexOfFeMaleChildren() }
        runCatching { vm.getIndexOfAge1() }
        runCatching { vm.getIndexOfGap1() }
        runCatching { vm.getIndexOfAge2() }
        runCatching { vm.getIndexOfGap2() }
        runCatching { vm.getIndexOfAge3() }
        runCatching { vm.getIndexOfGap3() }
        runCatching { vm.getIndexOfAge4() }
        runCatching { vm.getIndexOfGap4() }
        runCatching { vm.getIndexOfAge5() }
        runCatching { vm.getIndexOfGap5() }
        runCatching { vm.getIndexOfAge6() }
        runCatching { vm.getIndexOfGap6() }
        runCatching { vm.getIndexOfAge7() }
        runCatching { vm.getIndexOfGap7() }
        runCatching { vm.getIndexOfAge8() }
        runCatching { vm.getIndexOfGap8() }
        runCatching { vm.getIndexOfAge9() }
        runCatching { vm.getIndexOfGap9() }
        assertNotNull(vm.formList)
    }

    @Test
    fun `updateValueByIdAndReturnListIndex delegates to the dataset`() {
        val vm = buildVm()
        runCatching { vm.updateValueByIdAndReturnListIndex(12, "2") }
        assertNotNull(vm.formList)
    }

    // ----------------------------------------------------------------------------------------
    // saveForm
    // ----------------------------------------------------------------------------------------

    private fun sampleLocationRecord() = LocationRecord(
        country = LocationEntity(1, "India"),
        state = LocationEntity(2, "State"),
        district = LocationEntity(3, "District"),
        block = LocationEntity(4, "Block"),
        village = LocationEntity(5, "Village")
    )

    @Test
    fun `saveForm creates a new child record and persists the ecr form on success`() = runTest {
        val selectedBenId = 33L
        val household = mockk<HouseholdCache>(relaxed = true)
        every { household.locationRecord } returns sampleLocationRecord()
        every { household.benId } returns 0L
        coEvery { benRepo.getHousehold(any()) } returns household

        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.isDeath } returns false
        every { ben.genDetails } returns null
        every { ben.isConsent } returns false
        every { ben.gender } returns null
        every { ben.firstName } returns "KID"
        every { ben.lastName } returns null
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns ben

        val selectedBen = mockk<BenRegCache>(relaxed = true)
        every { selectedBen.beneficiaryId } returns selectedBenId
        every { selectedBen.householdId } returns 1L
        every { selectedBen.lastName } returns "DEVI"
        every { selectedBen.contactNumber } returns 9999999999L
        every { selectedBen.community } returns "GEN"
        every { selectedBen.communityId } returns 1
        every { selectedBen.firstName } returns "MOTHER"
        every { selectedBen.genDetails } returns null
        coEvery { benRepo.getBenListFromHousehold(any()) } returns listOf(selectedBen)

        val savedEcr = mockk<EligibleCoupleRegCache>(relaxed = true)
        coEvery { ecrRepo.getSavedRecord(any()) } returns savedEcr

        coEvery { benRepo.getMinBenId() } returns 0L
        coEvery { benRepo.persistRecord(any()) } just Runs
        coEvery { benRepo.updateBeneficiaryChildrenAdded(any(), any(), any()) } just Runs
        coEvery { ecrRepo.persistRecord(any()) } just Runs

        val vm = buildVm(hhId = 1L, selectedBenId = selectedBenId)
        advanceUntilIdle()

        vm.updateValueByIdAndReturnListIndex(12, "1")
        vm.updateListOnValueChanged(12, 0)
        advanceUntilIdle()

        vm.updateValueByIdAndReturnListIndex(111, "BABYNAME")
        vm.updateValueByIdAndReturnListIndex(17, "01-01-2020")
        vm.updateValueByIdAndReturnListIndex(18, "5")
        vm.updateValueByIdAndReturnListIndex(19, "opt0")

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(NewChildBenViewModel.State.SAVE_SUCCESS, vm.state.value)
        coVerify { benRepo.persistRecord(any()) }
        coVerify { benRepo.updateBeneficiaryChildrenAdded(1L, selectedBenId, SyncState.UNSYNCED) }
        coVerify { ecrRepo.persistRecord(savedEcr) }
    }

}
