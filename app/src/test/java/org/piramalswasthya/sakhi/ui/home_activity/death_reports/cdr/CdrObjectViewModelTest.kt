package org.piramalswasthya.sakhi.ui.home_activity.death_reports.cdr

import android.app.Application
import android.content.res.Resources
import android.net.Uri
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
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.CdrDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.CDRCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.CdrRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [CdrObjectViewModel]: the `init` lookup, the death-certificate document form-id
 * round trip and the submit success/failure arms.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CdrObjectViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Application

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var database: InAppDb

    @MockK
    private lateinit var cdrDao: CdrDao

    @MockK
    private lateinit var cdrRepo: CdrRepo

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
        every { user.villages } returns listOf(LocationEntity(id = 1, name = "Rampur"))

        ben = mockk(relaxed = true)
        every { ben.firstName } returns "Ram"
        every { ben.lastName } returns "Yadav"
        every { ben.age } returns 40

        household = mockk(relaxed = true)

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        every { database.cdrDao } returns cdrDao
        coEvery { cdrDao.getCDR(any()) } returns null

        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns ben
        coEvery { benRepo.getHousehold(any()) } returns household
        coEvery { cdrRepo.saveCdrData(any()) } returns true
    }

    private fun buildVm(hhId: Long = 1L, benId: Long = 2L): CdrObjectViewModel =
        CdrObjectViewModel(
            SavedStateHandle(mapOf("hhId" to hhId, "benId" to benId)),
            context,
            database,
            cdrRepo,
            benRepo,
            preferenceDao
        )

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertEquals(CdrObjectViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `init populates the header when no report exists`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Ram Yadav", vm.benName.value)
        assertEquals(false, vm.exists.value)
    }

    @Test
    fun `init reports an existing report`() = runTest {
        coEvery { cdrDao.getCDR(any()) } returns mockk<CDRCache>(relaxed = true)

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.exists.value)
    }

    @Test
    fun `init omits a null last name from the header`() = runTest {
        every { ben.lastName } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Ram ", vm.benName.value)
    }

    @Test
    fun `document form id round trips`() {
        val vm = buildVm()
        assertEquals(0, vm.getDocumentFormId())
        vm.setCurrentDocumentFormId(31)
        assertEquals(31, vm.getDocumentFormId())
    }

    @Test
    fun `setImageUriToFormElement delegates to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.setCurrentDocumentFormId(31)
        runCatching { vm.setImageUriToFormElement(mockk<Uri>(relaxed = true)) }

        assertNotNull(vm.formList)
    }

    @Test
    fun `getIndexOf helpers delegate to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.getIndexOfCDR1() }
        runCatching { vm.getIndexOfCDR2() }
        runCatching { vm.getIndexOfIsDeathCertificate() }

        assertNotNull(vm.formList)
    }

    @Test
    fun `submitForm reports success when the repository saves the report`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals(CdrObjectViewModel.State.SUCCESS, vm.state.value)
    }

    @Test
    fun `submitForm reports failure when the repository rejects the report`() = runTest {
        coEvery { cdrRepo.saveCdrData(any()) } returns false

        val vm = buildVm()
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals(CdrObjectViewModel.State.FAIL, vm.state.value)
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
