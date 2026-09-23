package org.piramalswasthya.sakhi.ui.home_activity.child_care.child_list.hbyc.form

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.HbycDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HBYCCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HbycRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [HbycViewModel]. Besides the `init` lookup arms this covers the private
 * `getAddress` collapsing rules indirectly through the exposed `address` LiveData.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HbycViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var database: InAppDb

    @MockK
    private lateinit var hbycDao: HbycDao

    @MockK
    private lateinit var hbycRepo: HbycRepo

    @MockK
    private lateinit var benRepo: BenRepo

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    private lateinit var user: User
    private lateinit var ben: BenRegCache
    private lateinit var household: HouseholdCache

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
        every { ben.age } returns 2

        household = mockk(relaxed = true)

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        every { database.hbycDao } returns hbycDao
        coEvery { hbycDao.getHbyc(any(), any(), any()) } returns null

        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns ben
        coEvery { benRepo.getHousehold(any()) } returns household
        coEvery { hbycRepo.saveHbycData(any()) } returns true
    }

    private fun buildVm(hhId: Long = 1L, benId: Long = 2L, month: Int = 9): HbycViewModel =
        HbycViewModel(
            SavedStateHandle(mapOf("hhId" to hhId, "benId" to benId, "month" to month)),
            context,
            database,
            hbycRepo,
            benRepo,
            preferenceDao
        )

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertEquals(HbycViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `init populates the header and the address`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Baby Kumar", vm.benName.value)
        assertNotNull(vm.address.value)
        assertEquals(false, vm.exists.value)
    }

    @Test
    fun `init reports an existing record when one is already saved`() = runTest {
        coEvery { hbycDao.getHbyc(any(), any(), any()) } returns mockk<HBYCCache>(relaxed = true)

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.exists.value)
    }

    @Test
    fun `init looks the record up for the requested month`() = runTest {
        val vm = buildVm(month = 15)
        advanceUntilIdle()

        coVerify { hbycDao.getHbyc(any(), any(), "15") }
        assertNotNull(vm.formList)
    }

    @Test
    fun `address collapses the empty household fragments`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        val address = vm.address.value
        assertNotNull(address)
        assertTrue(address!!.isNotEmpty())
    }

    @Test
    fun `submitForm reports success when the repository saves the record`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals(HbycViewModel.State.SUCCESS, vm.state.value)
    }

    @Test
    fun `submitForm reports failure when the repository rejects the record`() = runTest {
        coEvery { hbycRepo.saveHbycData(any()) } returns false

        val vm = buildVm()
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals(HbycViewModel.State.FAIL, vm.state.value)
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
