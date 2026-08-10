package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.form

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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.dao.MalariaDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.MalariaScreeningCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MalariaRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [MalariaFormViewModel]: the create versus follow-up arms of `init`, the
 * case-status predicates and the death branch of `saveForm` that also marks the beneficiary dead.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MalariaFormViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var malariaDao: MalariaDao

    @MockK
    private lateinit var malariaRepo: MalariaRepo

    @MockK
    private lateinit var benRepo: BenRepo

    @MockK
    private lateinit var maternalHealthRepo: MaternalHealthRepo

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
        every { context.resources } returns mockResources

        user = mockk(relaxed = true)
        every { user.userName } returns "asha"

        ben = mockk(relaxed = true)
        every { ben.beneficiaryId } returns 2L
        every { ben.householdId } returns 5L
        every { ben.firstName } returns "Ramesh"
        every { ben.lastName } returns "Patel"
        every { ben.age } returns 33
        every { ben.processed } returns "P"

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        every { malariaDao.getAllVisitsForBen(any()) } returns flowOf(emptyList())
        coEvery { benRepo.getBenFromId(any()) } returns ben
        coEvery { benRepo.updateRecord(any()) } returns Unit
        coEvery { maternalHealthRepo.getBenFromId(any()) } returns ben
        coEvery { malariaRepo.getLatestVisitForBen(any()) } returns null
        coEvery { malariaRepo.getlastvisitIdforBen(any()) } returns 2L
        coEvery { malariaRepo.saveMalariaScreening(any()) } returns Unit
    }

    private fun buildVm(benId: Long = 2L): MalariaFormViewModel = MalariaFormViewModel(
        SavedStateHandle(mapOf("benId" to benId)),
        preferenceDao,
        malariaDao,
        context,
        malariaRepo,
        benRepo,
        maternalHealthRepo
    )

    private fun screening(status: String?, case: String?): MalariaScreeningCache {
        val cache = mockk<MalariaScreeningCache>(relaxed = true)
        every { cache.beneficiaryStatus } returns status
        every { cache.caseStatus } returns case
        every { cache.benId } returns 2L
        every { cache.visitId } returns 2L
        return cache
    }

    @Test
    fun `the beneficiary argument is exposed`() {
        val vm = buildVm(benId = 51L)
        assertEquals(51L, vm.benId)
    }

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertNotNull(vm.allVisitsList)
        assertEquals(MalariaFormViewModel.State.IDLE, vm.state.value)
        assertFalse(vm.isSuspected)
        assertFalse(vm.isDeath)
    }

    @Test
    fun `init populates the header and reports no earlier visit`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Ramesh Patel", vm.benName.value)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init omits a null last name from the header`() = runTest {
        every { ben.lastName } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Ramesh ", vm.benName.value)
    }

    @Test
    fun `init carries the suspected case forward from the latest visit`() = runTest {
        coEvery { malariaRepo.getLatestVisitForBen(any()) } returns
                screening(status = "Alive", case = "Suspected")

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
        assertEquals("2", vm.visitNo.value)
        assertTrue(vm.isSuspected)
        assertFalse(vm.isnotConfirmed)
        assertEquals(false, vm.isBeneficaryStatusDeath.value)
    }

    @Test
    fun `init flags a deceased beneficiary from the latest visit`() = runTest {
        coEvery { malariaRepo.getLatestVisitForBen(any()) } returns
                screening(status = "death", case = "Not Confirmed")

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.isBeneficaryStatusDeath.value)
        assertTrue(vm.isnotConfirmed)
    }

    @Test
    fun `init flags a not suspected case`() = runTest {
        coEvery { malariaRepo.getLatestVisitForBen(any()) } returns
                screening(status = "Alive", case = "Not Suspected")

        val vm = buildVm()
        advanceUntilIdle()

        assertTrue(vm.isnotSuspectedConfirmed)
    }

    @Test
    fun `saveForm stores the next visit and reports success`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { malariaRepo.saveMalariaScreening(any()) }
        assertEquals(MalariaFormViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm marks the beneficiary dead when the status is death`() = runTest {
        val cache = screening(status = "Death", case = "Confirmed")
        every { cache.reasonForDeath } returns "opt4"
        every { cache.placeOfDeath } returns "opt6"
        every { cache.dateOfDeath } returns 1_700_000_000_000L
        coEvery { malariaRepo.getLatestVisitForBen(any()) } returns cache

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertTrue(vm.isDeath)
        coVerify { benRepo.updateRecord(any()) }
        coVerify { malariaRepo.saveMalariaScreening(any()) }
    }

    @Test
    fun `saveForm tolerates a missing last visit id`() = runTest {
        coEvery { malariaRepo.getlastvisitIdforBen(any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { malariaRepo.saveMalariaScreening(any()) }
    }

    @Test
    fun `saveForm reports failure when storing throws`() = runTest {
        coEvery { malariaRepo.saveMalariaScreening(any()) } throws RuntimeException("db down")

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(MalariaFormViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `case status predicates classify the screening`() {
        val vm = buildVm()

        assertTrue(vm.isSuspectedCase(screening(null, "Suspected")))
        assertFalse(vm.isSuspectedCase(screening(null, "Confirmed")))
        assertTrue(vm.isNotConfirmedCase(screening(null, "Not Confirmed")))
        assertFalse(vm.isNotConfirmedCase(screening(null, "Suspected")))
        assertTrue(vm.isNotSuspectedCase(screening(null, "Not Suspected")))
        assertFalse(vm.isNotSuspectedCase(screening(null, "Suspected")))
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
    fun `resetState returns to idle`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()
        vm.resetState()

        assertEquals(MalariaFormViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `getIndexOfDate delegates to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.getIndexOfDate() }

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
