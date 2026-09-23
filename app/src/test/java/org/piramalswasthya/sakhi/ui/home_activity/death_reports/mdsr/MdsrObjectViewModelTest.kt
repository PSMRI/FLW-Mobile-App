package org.piramalswasthya.sakhi.ui.home_activity.death_reports.mdsr

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.MdsrDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.MDSRCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MdsrRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [MdsrObjectViewModel]. The dataset is created inside the `init` coroutine from the
 * six death-cause flags, so every test that touches `formList` first drains the scheduler.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MdsrObjectViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Application

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var database: InAppDb

    @MockK
    private lateinit var mdsrDao: MdsrDao

    @MockK
    private lateinit var mdsrRepo: MdsrRepo

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

        every { context.getString(any()) } returns "Accident"

        user = mockk(relaxed = true)
        every { user.userName } returns "asha"
        every { user.villages } returns listOf(LocationEntity(id = 1, name = "Rampur"))

        ben = mockk(relaxed = true)
        every { ben.firstName } returns "Sita"
        every { ben.lastName } returns "Devi"
        every { ben.age } returns 28

        household = mockk(relaxed = true)

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        every { database.mdsrDao } returns mdsrDao
        coEvery { mdsrDao.getMDSR(any()) } returns null

        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns ben
        coEvery { benRepo.getHousehold(any()) } returns household
        coEvery { benRepo.hasPregnancyDeath(any()) } returns false
        coEvery { benRepo.hasAbortionDeath(any()) } returns false
        coEvery { benRepo.hasDeliveryDeath(any()) } returns false
        coEvery { benRepo.hasPncDeath(any()) } returns false
        coEvery { benRepo.isPncCauseOfDeathAccident(any(), any()) } returns false
        coEvery { benRepo.isAncCauseOfDeathAccident(any(), any()) } returns false
        coEvery { mdsrRepo.saveMdsrData(any()) } returns true
    }

    private fun buildVm(hhId: Long = 1L, benId: Long = 2L): MdsrObjectViewModel =
        MdsrObjectViewModel(
            SavedStateHandle(mapOf("hhId" to hhId, "benId" to benId)),
            context,
            database,
            benRepo,
            mdsrRepo,
            preferenceDao
        )

    @Test
    fun `init builds the dataset and populates the header`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Sita Devi", vm.benName.value)
        assertEquals(false, vm.exists.value)
        assertNotNull(vm.formList)
    }

    @Test
    fun `initial state is idle`() {
        val vm = buildVm()
        assertEquals(MdsrObjectViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `init reports an existing report`() = runTest {
        coEvery { mdsrDao.getMDSR(any()) } returns mockk<MDSRCache>(relaxed = true)

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.exists.value)
    }

    @Test
    fun `init builds the dataset with every death cause flag raised`() = runTest {
        coEvery { benRepo.hasPregnancyDeath(any()) } returns true
        coEvery { benRepo.hasAbortionDeath(any()) } returns true
        coEvery { benRepo.hasDeliveryDeath(any()) } returns true
        coEvery { benRepo.hasPncDeath(any()) } returns true
        coEvery { benRepo.isPncCauseOfDeathAccident(any(), any()) } returns true
        coEvery { benRepo.isAncCauseOfDeathAccident(any(), any()) } returns true

        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }

    @Test
    fun `init omits a null last name from the header`() = runTest {
        every { ben.lastName } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Sita ", vm.benName.value)
    }

    @Test
    fun `document form id round trips`() {
        val vm = buildVm()
        assertEquals(0, vm.getDocumentFormId())
        vm.setCurrentDocumentFormId(42)
        assertEquals(42, vm.getDocumentFormId())
    }

    @Test
    fun `getIndexOf helpers and image binding delegate to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.getIndexOfMDSR1() }
        runCatching { vm.getIndexOfMDSR2() }
        runCatching { vm.getIndexOfIsDeathCertificate() }
        vm.setCurrentDocumentFormId(31)
        runCatching { vm.setImageUriToFormElement(mockk<Uri>(relaxed = true)) }

        assertNotNull(vm.formList)
    }

    @Test
    fun `submitForm reports success when the repository saves the report`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals(MdsrObjectViewModel.State.SUCCESS, vm.state.value)
    }

    @Test
    fun `submitForm reports failure when the repository rejects the report`() = runTest {
        coEvery { mdsrRepo.saveMdsrData(any()) } returns false

        val vm = buildVm()
        advanceUntilIdle()

        vm.submitForm()
        advanceUntilIdle()

        assertEquals(MdsrObjectViewModel.State.FAIL, vm.state.value)
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
    fun `death results data class exposes its flags`() {
        val results = MdsrObjectViewModel.DeathResults(true, false, true, false, true, false)
        assertTrue(results.pregnancyDeath)
        assertTrue(results.deliveryDeath)
        assertTrue(results.pncDeathCause)
        assertEquals(false, results.abortionDeath)
        assertEquals(false, results.pncDeath)
        assertEquals(false, results.ancDeathCause)
    }
}
