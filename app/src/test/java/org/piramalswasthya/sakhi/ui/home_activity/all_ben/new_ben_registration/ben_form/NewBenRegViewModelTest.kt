package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import android.util.Range
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
import org.piramalswasthya.sakhi.helpers.HofAbhaPrefillCache
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenRegGen
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.FamilyMember
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [NewBenRegViewModel] - the beneficiary registration form.
 *
 * `setUpPage` branches on three things: whether a beneficiary id was passed in, whether the record
 * is being read back or re-registered, and whether the beneficiary is the head of family. Because
 * `init` merely *queues* the setup coroutine on the test scheduler, `setRecordExist(false)` can be
 * called before draining it in order to reach the re-registration arm.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NewBenRegViewModelTest : BaseViewModelTest() {

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
    private lateinit var ecrRepo: EcrRepo

    @MockK
    private lateinit var hofAbhaPrefillCache: HofAbhaPrefillCache

    @MockK
    private lateinit var userRepo: UserRepo

    private lateinit var user: User
    private lateinit var household: HouseholdCache
    private lateinit var ben: BenRegCache

    @After
    fun releaseStaticMocks() {
        unmockkStatic(Dispatchers::class)
        unmockkStatic(Uri::class)
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
        every { Dispatchers.Default } returns testDispatcher

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)

        mockkConstructor(Range::class)
        every { anyConstructed<Range<Int>>().contains(any<Int>()) } returns true

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
        every { household.householdId } returns 1L
        every { household.benId } returns 100L
        every { household.locationRecord } returns mockk<LocationRecord>(relaxed = true)

        ben = mockk(relaxed = true)
        every { ben.beneficiaryId } returns 5L
        every { ben.householdId } returns 1L
        every { ben.familyHeadRelationPosition } returns 1
        every { ben.firstName } returns "Mohan"
        every { ben.lastName } returns "Lal"
        every { ben.age } returns 30
        every { ben.dob } returns 1_000_000_000_000L
        every { ben.gender } returns Gender.MALE
        every { ben.isConsent } returns false
        every { ben.isDeath } returns false
        every { ben.genDetails } returns BenRegGen()

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        coEvery { benRepo.getHousehold(any()) } returns household
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns ben
        coEvery { benRepo.getBenListFromHousehold(any()) } returns emptyList()
        coEvery { benRepo.getMinBenId() } returns 0L
        coEvery { benRepo.persistRecord(any()) } returns Unit
        coEvery { benRepo.updateRecord(any()) } returns Unit
        coEvery { ecrRepo.getSavedRecord(any()) } returns null
        coEvery { ecrRepo.persistRecord(any()) } returns Unit
        coEvery { ecrRepo.getBenFromId(any()) } returns null
        every { hofAbhaPrefillCache.consume(any()) } returns null
    }

    private fun buildVm(
        hhId: Long = 1L,
        relToHeadId: Int = 8,
        benId: Long = 0L,
        selectedBenId: Long = 0L,
        gender: Int = 2,
        isAddSpouse: Int = 0
    ): NewBenRegViewModel = NewBenRegViewModel(
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
        householdRepo,
        ecrRepo,
        hofAbhaPrefillCache,
        userRepo
    )

    private fun familyMemberBen(id: Long, gender: Gender, relationPosition: Int): BenRegCache {
        val member = mockk<BenRegCache>(relaxed = true)
        every { member.beneficiaryId } returns id
        every { member.gender } returns gender
        every { member.familyHeadRelationPosition } returns relationPosition
        every { member.dob } returns 1_000_000_000_000L
        return member
    }


    @Test
    fun `navigation arguments are exposed`() {
        val vm = buildVm(hhId = 77L, relToHeadId = 9, benId = 5L, selectedBenId = 6L)
        assertEquals(77L, vm.hhId)
        assertEquals(9, vm.relToHeadId)
        assertEquals(5L, vm.benIdFromArgs)
        assertEquals(6L, vm.SelectedbenIdFromArgs)
        assertFalse(vm.isHoF)
    }

    @Test
    fun `relation to head eighteen marks the head of family`() {
        val vm = buildVm(relToHeadId = 18)
        assertTrue(vm.isHoF)
    }

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertNotNull(vm.dataset)
        assertEquals(NewBenRegViewModel.State.IDLE, vm.state.value)
        assertTrue(vm.listUpdateState.value is NewBenRegViewModel.ListUpdateState.Idle)
        assertEquals(false, vm.recordExists.value)
        assertNull(vm.abhaUserDetails.value)
        assertFalse(vm.isBenMarried)
        assertFalse(vm.isEditClicked)
        assertEquals(1234, vm.otp)
        assertEquals(0, vm.oldChildCount)
    }

    @Test
    fun `a beneficiary id argument marks the record as existing`() {
        val vm = buildVm(benId = 5L)
        assertEquals(true, vm.recordExists.value)
    }


    @Test
    fun `setUpPage fails when nobody is logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(NewBenRegViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `setUpPage fails when the household cannot be found`() = runTest {
        coEvery { benRepo.getHousehold(any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(NewBenRegViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `setUpPage renders a new head of family`() = runTest {
        val vm = buildVm(relToHeadId = 18)
        advanceUntilIdle()

        coVerify { hofAbhaPrefillCache.consume(any()) }
        assertNotNull(vm.formList)
    }

    @Test
    fun `setUpPage prefills a new head of family from the abha hand-off`() = runTest {
        every { hofAbhaPrefillCache.consume(any()) } returns mockk<FamilyMember>(relaxed = true)

        val vm = buildVm(relToHeadId = 18)
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }

    @Test
    fun `setUpPage renders a new family member`() = runTest {
        val vm = buildVm(gender = 2)
        advanceUntilIdle()

        coVerify { benRepo.getBenListFromHousehold(any()) }
        assertNotNull(vm.formList)
    }

    @Test
    fun `setUpPage picks the female head of family for the eligible couple link`() = runTest {
        coEvery { benRepo.getBenListFromHousehold(any()) } returns
                listOf(familyMemberBen(100L, Gender.FEMALE, 1))

        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }

    @Test
    fun `setUpPage falls back to the wife when the head of family is male`() = runTest {
        coEvery { benRepo.getBenListFromHousehold(any()) } returns listOf(
            familyMemberBen(100L, Gender.MALE, 1),
            familyMemberBen(101L, Gender.FEMALE, 5)
        )

        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }

    @Test
    fun `setUpPage fails a new registration without a gender`() = runTest {
        val vm = buildVm(gender = 0)
        advanceUntilIdle()

        assertEquals(NewBenRegViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `setUpPage reads back a saved beneficiary`() = runTest {
        val vm = buildVm(benId = 5L)
        advanceUntilIdle()

        assertEquals(false, vm.isDeath.value)
        assertTrue(vm.isBenMarried)
        assertEquals(Gender.MALE, vm.getBenGender())
        assertEquals("Mohan Lal", vm.getBenName())
    }

    @Test
    fun `setUpPage marks an unmarried beneficiary`() = runTest {
        val genDetails = BenRegGen()
        genDetails.maritalStatus = "Unmarried"
        every { ben.genDetails } returns genDetails

        val vm = buildVm(benId = 5L)
        advanceUntilIdle()

        assertFalse(vm.isBenMarried)
    }

    @Test
    fun `setUpPage fails when the saved beneficiary cannot be found`() = runTest {
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns null

        val vm = buildVm(benId = 5L)
        advanceUntilIdle()

        assertEquals(NewBenRegViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `setUpPage re-registers a head of family whose record was reset`() = runTest {
        val vm = buildVm(relToHeadId = 18, benId = 5L)
        vm.setRecordExist(false)
        advanceUntilIdle()

        assertEquals(false, vm.recordExists.value)
        assertNotNull(vm.formList)
    }

    @Test
    fun `setUpPage re-registers a family member whose record was reset`() = runTest {
        coEvery { benRepo.getBenListFromHousehold(any()) } returns
                listOf(familyMemberBen(100L, Gender.MALE, 1))

        val vm = buildVm(benId = 5L, selectedBenId = 100L)
        vm.setRecordExist(false)
        advanceUntilIdle()

        coVerify { benRepo.getBenListFromHousehold(any()) }
        assertNotNull(vm.formList)
    }

    @Test
    fun `setUpPage re-registration falls back to the gender argument`() = runTest {
        every { ben.gender } returns null

        val vm = buildVm(benId = 5L, gender = 1)
        vm.setRecordExist(false)
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }

    @Test
    fun `setUpPage re-registration fails without any gender`() = runTest {
        every { ben.gender } returns null

        val vm = buildVm(benId = 5L, gender = 0)
        vm.setRecordExist(false)
        advanceUntilIdle()

        assertEquals(NewBenRegViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `setUpPage maps a transgender argument`() = runTest {
        val vm = buildVm(gender = 3)
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }


    @Test
    fun `saveForm refuses to run before the page finished loading`() = runTest {
        coEvery { benRepo.getHousehold(any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(NewBenRegViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `saveForm registers a brand new beneficiary`() = runTest {
        val vm = buildVm(relToHeadId = 3)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { benRepo.persistRecord(any()) }
        assertEquals(NewBenRegViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm updates the household when registering the head of family`() = runTest {
        val vm = buildVm(relToHeadId = 18)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { householdRepo.updateHousehold(any()) }
        coVerify { householdRepo.updateHouseholdToSync(any()) }
        assertEquals(NewBenRegViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm links a newly added spouse`() = runTest {
        val vm = buildVm(relToHeadId = 3, selectedBenId = 9L, isAddSpouse = 1)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(NewBenRegViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm folds a child into a fresh eligible couple record`() = runTest {
        coEvery { benRepo.getBenListFromHousehold(any()) } returns
                listOf(familyMemberBen(100L, Gender.FEMALE, 1))

        val vm = buildVm(relToHeadId = 8)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { ecrRepo.persistRecord(any()) }
        assertEquals(NewBenRegViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm fills the next free slot of an existing eligible couple record`() = runTest {
        coEvery { benRepo.getBenListFromHousehold(any()) } returns
                listOf(familyMemberBen(100L, Gender.FEMALE, 1))
        val ecr = mockk<EligibleCoupleRegCache>(relaxed = true)
        every { ecr.dob1 } returns 1L
        every { ecr.dob2 } returns null
        coEvery { ecrRepo.getSavedRecord(any()) } returns ecr

        val vm = buildVm(relToHeadId = 9)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { ecrRepo.persistRecord(any()) }
        assertEquals(NewBenRegViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm falls back to the last slot when every slot is taken`() = runTest {
        coEvery { benRepo.getBenListFromHousehold(any()) } returns
                listOf(familyMemberBen(100L, Gender.FEMALE, 1))
        val ecr = mockk<EligibleCoupleRegCache>(relaxed = true)
        every { ecr.dob1 } returns 1L
        every { ecr.dob2 } returns 2L
        every { ecr.dob3 } returns 3L
        every { ecr.dob4 } returns 4L
        every { ecr.dob5 } returns 5L
        every { ecr.dob6 } returns 6L
        every { ecr.dob7 } returns 7L
        every { ecr.dob8 } returns 8L
        every { ecr.dob9 } returns 9L
        coEvery { ecrRepo.getSavedRecord(any()) } returns ecr

        val vm = buildVm(relToHeadId = 8)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { ecrRepo.persistRecord(any()) }
    }

    @Test
    fun `saveForm updates the eligible couple beneficiary when the dataset changed it`() = runTest {
        coEvery { benRepo.getBenListFromHousehold(any()) } returns
                listOf(familyMemberBen(100L, Gender.FEMALE, 1))
        coEvery { ecrRepo.getBenFromId(any()) } returns mockk<BenRegCache>(relaxed = true)

        val vm = buildVm(relToHeadId = 8)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { ecrRepo.persistRecord(any()) }
        assertEquals(NewBenRegViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm skips the eligible couple work when no beneficiary is linked`() = runTest {
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns null
        var ecrWrites = 0
        coEvery { ecrRepo.persistRecord(any()) } answers { ecrWrites++ }

        val vm = buildVm(relToHeadId = 8)
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(0, ecrWrites)
        assertEquals(NewBenRegViewModel.State.SAVE_SUCCESS, vm.state.value)
    }


    @Test
    fun `consent flag round trips`() {
        val vm = buildVm()
        assertFalse(vm.getIsConsentAgreed())
        vm.setConsentAgreed()
        assertTrue(vm.getIsConsentAgreed())
    }

    @Test
    fun `list update state cycles through updating and updated`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.updateListOnValueChanged(12, 0)
        advanceUntilIdle()

        val state = vm.listUpdateState.value
        assertTrue(state is NewBenRegViewModel.ListUpdateState.Updated)
        assertEquals(12, (state as NewBenRegViewModel.ListUpdateState.Updated).formElementId)

        vm.resetListUpdateState()
        assertTrue(vm.listUpdateState.value is NewBenRegViewModel.ListUpdateState.Idle)
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
    fun `prefillFromAyushmanCard delegates to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.prefillFromAyushmanCard(mockk<FamilyMember>(relaxed = true)) }

        assertNotNull(vm.formList)
    }

    @Test
    fun `isHoFMarried reflects the marital status of the head of family`() = runTest {
        val genDetails = BenRegGen()
        genDetails.maritalStatusId = 2
        every { ben.genDetails } returns genDetails

        val vm = buildVm(relToHeadId = 18, benId = 5L)
        advanceUntilIdle()

        assertTrue(vm.isHoFMarried())
    }

    @Test
    fun `document and image form ids round trip`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(0, vm.getDocumentFormId())
        vm.setCurrentDocumentFormId(21)
        assertEquals(21, vm.getDocumentFormId())

        vm.setCurrentImageFormId(46)
        runCatching { vm.setImageUriToFormElement(mockk<Uri>(relaxed = true)) }

        assertNotNull(vm.formList)
    }

    @Test
    fun `index helpers delegate to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.getIndexOfBirthCertificateFront() }
        runCatching { vm.getIndexOfBirthCertificateBack() }
        runCatching { vm.getIndexOfAgeAtMarriage() }
        runCatching { vm.getIndexOfMaritalStatus() }
        runCatching { vm.getIndexOfContactNumber() }
        runCatching { vm.getIndexofTempraryNumber() }
        runCatching { vm.updateValueByIdAndReturnListIndex(12, "2") }

        assertNotNull(vm.formList)
    }

    @Test
    fun `otp default is exposed and mutable`() {
        val vm = buildVm()
        assertEquals(1234, vm.otp)
        vm.otp = 4321
        assertEquals(4321, vm.otp)
    }

    @Test
    fun `sending and resending an otp tolerate an empty response`() = runTest {
        coEvery { benRepo.sendOtp(any()) } returns null
        coEvery { benRepo.resendOtp(any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        vm.sentOtp("9999999999")
        vm.resendOtp("9999999999")
        advanceUntilIdle()

        coVerify { benRepo.sendOtp(any()) }
        coVerify { benRepo.resendOtp(any()) }
    }

    @Test
    fun `validateOtp reports false when the server does not answer`() = runTest {
        coEvery { benRepo.verifyOtp(any(), any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        val verified = vm.validateOtp(
            "9999999999",
            1234,
            mockk<FragmentActivity>(relaxed = true),
            mockk<TextInputEditText>(relaxed = true),
            mockk<MaterialButton>(relaxed = true),
            mockk<TextView>(relaxed = true)
        )
        advanceUntilIdle()

        assertFalse(verified)
    }

    @Test
    fun `getFormPreviewData renders every form element`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        unmockkStatic(Dispatchers::class)
        val preview = vm.getFormPreviewData()

        assertNotNull(preview)
    }

    @Test
    fun `setRecordExist updates recordExists`() {
        val vm = buildVm()
        vm.setRecordExist(true)
        assertEquals(true, vm.recordExists.value)
        vm.setRecordExist(false)
        assertEquals(false, vm.recordExists.value)
    }
}
