package org.piramalswasthya.sakhi.ui.home_activity.child_care.infant_list.hbnc_form.visit

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
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HBNCCache
import org.piramalswasthya.sakhi.model.HbncHomeVisit
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HbncRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [HbncVisitViewModel]. The `nthDay` navigation argument selects which HBNC visit
 * page the dataset renders, so both the first-visit (`nthDay == 1`) and follow-up arms of the
 * `init` block are exercised.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HbncVisitViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var hbncRepo: HbncRepo

    @MockK
    private lateinit var benRepo: BenRepo

    @MockK
    private lateinit var userRepo: UserRepo

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
        every { ben.firstName } returns "Baby"
        every { ben.lastName } returns "Kumar"
        every { ben.age } returns 1

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns ben
        coEvery { benRepo.getHousehold(any()) } returns mockk<HouseholdCache>(relaxed = true)
        coEvery { hbncRepo.getHbncRecord(any(), any(), any()) } returns null
        coEvery { hbncRepo.getFirstHomeVisit(any(), any()) } returns null
        coEvery { hbncRepo.saveHbncData(any()) } returns true
    }

    private fun buildVm(hhId: Long = 1L, benId: Long = 2L, nthDay: Int = 3): HbncVisitViewModel =
        HbncVisitViewModel(
            SavedStateHandle(mapOf("hhId" to hhId, "benId" to benId, "nthDay" to nthDay)),
            context,
            preferenceDao,
            hbncRepo,
            benRepo,
            userRepo
        )

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertNotNull(vm.alertError)
        assertNotNull(vm.errorMessage)
        assertEquals(HbncVisitViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `init populates the beneficiary header`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Baby Kumar", vm.benName.value)
        assertEquals(false, vm.exists.value)
    }

    @Test
    fun `init skips the first home visit lookup on day one`() = runTest {
        var lookups = 0
        coEvery { hbncRepo.getFirstHomeVisit(any(), any()) } answers { lookups++; null }

        val vm = buildVm(nthDay = 1)
        advanceUntilIdle()

        assertEquals(0, lookups)
        assertNotNull(vm.formList)
    }

    @Test
    fun `init reads the first home visit for later days`() = runTest {
        coEvery { hbncRepo.getFirstHomeVisit(any(), any()) } returns
                mockk<HbncHomeVisit>(relaxed = true)

        val vm = buildVm(nthDay = 7)
        advanceUntilIdle()

        coVerify { hbncRepo.getFirstHomeVisit(any(), any()) }
        assertNotNull(vm.formList)
    }

    @Test
    fun `init reports an existing record when one is already saved`() = runTest {
        coEvery { hbncRepo.getHbncRecord(any(), any(), any()) } returns
                mockk<HBNCCache>(relaxed = true)

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.exists.value)
    }

    @Test
    fun `submitForm reports success when the repository saves the record`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals(HbncVisitViewModel.State.SUCCESS, vm.state.value)
    }

    @Test
    fun `submitForm reports failure when the repository rejects the record`() = runTest {
        coEvery { hbncRepo.saveHbncData(any()) } returns false

        val vm = buildVm()
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals(HbncVisitViewModel.State.FAIL, vm.state.value)
    }

    @Test
    fun `updateListOnValueChanged and resetErrorMessage delegate to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.updateListOnValueChanged(1, 0)
        vm.resetErrorMessage()
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }
}
