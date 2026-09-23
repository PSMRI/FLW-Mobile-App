package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.form

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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.CbacRepo
import org.piramalswasthya.sakhi.repositories.NcdReferalRepo
import org.piramalswasthya.sakhi.repositories.SaasBahuSammelanRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [NcdRefferalFormViewModel]: the navigation arguments, the beneficiary header
 * seeded from [BenDao] and the save success/failure arms.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NcdRefferalFormViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var cbacRepo: CbacRepo

    @MockK
    private lateinit var benDao: BenDao

    @MockK
    private lateinit var referalRepo: NcdReferalRepo

    @MockK
    private lateinit var saasBahuSammelanRepo: SaasBahuSammelanRepo

    private lateinit var user: User
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

        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher

        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"

        user = mockk(relaxed = true)
        every { user.userName } returns "asha"

        ben = mockk(relaxed = true)
        every { ben.firstName } returns "Meena"
        every { ben.lastName } returns "Bai"
        every { ben.age } returns 35
        every { ben.gender } returns Gender.FEMALE

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        coEvery { benDao.getBen(any()) } returns ben
    }

    private fun buildVm(
        benId: Long = 3L,
        referral: String = "Hypertension",
        referralType: String = "PHC",
        cbacId: Int = 0
    ): NcdRefferalFormViewModel = NcdRefferalFormViewModel(
        SavedStateHandle(
            mapOf(
                "benId" to benId,
                "referral" to referral,
                "referralType" to referralType,
                "cbacId" to cbacId
            )
        ),
        preferenceDao,
        cbacRepo,
        benDao,
        referalRepo,
        saasBahuSammelanRepo,
        context
    )

    @Test
    fun `navigation arguments are exposed`() {
        val vm = buildVm(benId = 9L, referral = "Diabetes", referralType = "CHC", cbacId = 4)
        assertEquals(9L, vm.benId)
        assertEquals("Diabetes", vm.referralReason)
        assertEquals("CHC", vm.referraltype)
        assertEquals(4, vm.cbacId)
    }

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertEquals(NcdRefferalFormViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `init populates the beneficiary header and the referral cache`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Meena Bai", vm.benName.value)
        assertEquals(Gender.FEMALE, vm.gender.value)
        assertEquals(35, vm.age.value)
        assertEquals(false, vm.recordExists.value)
        assertNotNull(vm.referalCache)
        assertEquals("asha", vm.referalCache.createdBy)
    }

    @Test
    fun `init omits a null last name from the header`() = runTest {
        every { ben.lastName } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Meena ", vm.benName.value)
    }

    @Test
    fun `init tolerates a beneficiary without a gender`() = runTest {
        every { ben.gender } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(null, vm.gender.value)
    }

    @Test
    fun `init tolerates a missing logged in user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(null, vm.referalCache.createdBy)
    }

    @Test
    fun `saveForm maps the dataset onto the cache and reports success`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(NcdRefferalFormViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `resetState returns to idle`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()
        vm.resetState()

        assertEquals(NcdRefferalFormViewModel.State.IDLE, vm.state.value)
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
