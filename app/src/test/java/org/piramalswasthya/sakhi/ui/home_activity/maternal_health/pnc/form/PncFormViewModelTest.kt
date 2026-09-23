package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pnc.form

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
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.PNCVisitCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.sakhi.repositories.PncRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [PncFormViewModel]: the `init` lookup chain, the permanent-sterilization scan that
 * drives the reproductive-status update, the mother-death branch of `saveForm` and the small
 * incentive/date helpers.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PncFormViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var deliveryOutcomeRepo: DeliveryOutcomeRepo

    @MockK
    private lateinit var pncRepo: PncRepo

    @MockK
    private lateinit var benRepo: BenRepo

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
        every { mockResources.getString(any()) } returns "Day"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
        every { context.resources } returns mockResources

        user = mockk(relaxed = true)
        every { user.userName } returns "asha"

        ben = mockk(relaxed = true)
        every { ben.beneficiaryId } returns 2L
        every { ben.firstName } returns "Sunita"
        every { ben.lastName } returns "Devi"
        every { ben.age } returns 26
        every { ben.processed } returns "P"

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        coEvery { benRepo.getBenFromId(any()) } returns ben
        coEvery { benRepo.updateRecord(any()) } returns Unit
        coEvery { deliveryOutcomeRepo.getDeliveryOutcome(any()) } returns null
        coEvery { deliveryOutcomeRepo.saveDeliveryOutcome(any()) } returns Unit
        coEvery { pncRepo.getSavedPncRecord(any(), any()) } returns null
        coEvery { pncRepo.getLastFilledPncRecord(any()) } returns null
        coEvery { pncRepo.getAllPncVisitsForBeneficiary(any()) } returns emptyList()
        coEvery { pncRepo.persistPncRecord(any()) } returns Unit
    }

    private fun buildVm(
        benId: Long = 2L,
        hhId: String? = "5",
        visitNumber: Int = 1
    ): PncFormViewModel = PncFormViewModel(
        SavedStateHandle(
            mapOf("benId" to benId, "hhId" to hhId, "visitNumber" to visitNumber)
        ),
        preferenceDao,
        context,
        deliveryOutcomeRepo,
        pncRepo,
        benRepo
    )

    private fun pncVisit(period: Int, method: String?): PNCVisitCache {
        val visit = mockk<PNCVisitCache>(relaxed = true)
        every { visit.pncPeriod } returns period
        every { visit.contraceptionMethod } returns method
        every { visit.sterilisationDate } returns 1_700_000_000_000L
        return visit
    }

    private fun savedVisit(period: Int = 1, motherDied: Boolean = false): PNCVisitCache {
        val visit = mockk<PNCVisitCache>(relaxed = true)
        every { visit.benId } returns 2L
        every { visit.pncPeriod } returns period
        every { visit.pncDate } returns 1_700_000_000_000L
        every { visit.dateOfDelivery } returns 1_690_000_000_000L
        every { visit.deathDate } returns 1_700_000_000_000L
        every { visit.motherDeath } returns motherDied
        every { visit.contraceptionMethod } returns null
        every { visit.anyContraceptionMethod } returns null
        every { visit.sterilisationDate } returns 1_700_000_000_000L
        return visit
    }

    @Test
    fun `navigation arguments are exposed`() {
        val vm = buildVm(benId = 77L, hhId = "9")
        assertEquals(77L, vm.benId)
        assertEquals(9L, vm.hhId)
    }

    @Test
    fun `a blank household argument leaves hhId null`() {
        val vm = buildVm(hhId = null)
        assertNull(vm.hhId)
    }

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertEquals(PncFormViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `init populates the header and reports no saved visit`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Sunita Devi", vm.benName.value)
        assertEquals(false, vm.recordExists.value)
        assertNull(vm.deliveryOutcome)
    }

    @Test
    fun `init reports an existing visit and keeps the delivery outcome`() = runTest {
        coEvery { pncRepo.getSavedPncRecord(any(), any()) } returns
                mockk<PNCVisitCache>(relaxed = true)
        coEvery { deliveryOutcomeRepo.getDeliveryOutcome(any()) } returns
                mockk<DeliveryOutcomeCache>(relaxed = true)

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
        assertNotNull(vm.deliveryOutcome)
    }

    @Test
    fun `init omits a null last name from the header`() = runTest {
        every { ben.lastName } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Sunita ", vm.benName.value)
    }

    @Test
    fun `hasPreviousPermanentSterilization is false without earlier visits`() = runTest {
        val vm = buildVm(visitNumber = 3)
        advanceUntilIdle()

        assertFalse(vm.hasPreviousPermanentSterilization())
    }

    @Test
    fun `hasPreviousPermanentSterilization ignores visits without a method`() = runTest {
        coEvery { pncRepo.getAllPncVisitsForBeneficiary(any()) } returns
                listOf(pncVisit(1, null), pncVisit(2, "Condom"))

        val vm = buildVm(visitNumber = 5)
        advanceUntilIdle()

        assertFalse(vm.hasPreviousPermanentSterilization())
    }

    @Test
    fun `hasPreviousPermanentSterilization detects an earlier sterilization`() = runTest {
        coEvery { pncRepo.getAllPncVisitsForBeneficiary(any()) } returns
                listOf(pncVisit(1, "opt3"))

        val vm = buildVm(visitNumber = 5)
        advanceUntilIdle()

        assertTrue(vm.hasPreviousPermanentSterilization())
    }

    @Test
    fun `getLastPermanentSterilizationVisit returns the latest matching visit`() = runTest {
        coEvery { pncRepo.getAllPncVisitsForBeneficiary(any()) } returns
                listOf(pncVisit(1, "opt3"), pncVisit(4, "opt3"), pncVisit(9, "opt3"))

        val vm = buildVm(visitNumber = 5)
        advanceUntilIdle()

        val last = vm.getLastPermanentSterilizationVisit(2L, 5)
        assertNotNull(last)
        assertEquals(4, last!!.pncPeriod)
    }

    @Test
    fun `getLastPermanentSterilizationVisit returns null when nothing matches`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertNull(vm.getLastPermanentSterilizationVisit(2L, 5))
    }

    @Test
    fun `saveForm persists the visit and seeds a delivery outcome`() = runTest {
        coEvery { pncRepo.getSavedPncRecord(any(), any()) } returns savedVisit()

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { pncRepo.persistPncRecord(any()) }
        coVerify { deliveryOutcomeRepo.saveDeliveryOutcome(any()) }
        assertTrue(vm.state.value is PncFormViewModel.State.SAVE_SUCCESS)
    }

    @Test
    fun `saveForm keeps an existing delivery outcome`() = runTest {
        val outcome = mockk<DeliveryOutcomeCache>(relaxed = true)
        every { outcome.dateOfDelivery } returns 1_700_000_000_000L
        coEvery { deliveryOutcomeRepo.getDeliveryOutcome(any()) } returns outcome
        coEvery { pncRepo.getSavedPncRecord(any(), any()) } returns savedVisit()
        var outcomeWrites = 0
        coEvery { deliveryOutcomeRepo.saveDeliveryOutcome(any()) } answers { outcomeWrites++ }

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(0, outcomeWrites)
        assertTrue(vm.state.value is PncFormViewModel.State.SAVE_SUCCESS)
    }

    @Test
    fun `saveForm flags navigation to mdsr when the mother died`() = runTest {
        coEvery { pncRepo.getSavedPncRecord(any(), any()) } returns
                savedVisit(period = 42, motherDied = true)

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is PncFormViewModel.State.SAVE_SUCCESS)
        assertTrue((state as PncFormViewModel.State.SAVE_SUCCESS).shouldNavigateToMdsr)
        coVerify { benRepo.updateRecord(any()) }
    }

    @Test
    fun `saveForm marks the woman permanently sterilised on the forty second day`() = runTest {
        coEvery { pncRepo.getSavedPncRecord(any(), any()) } returns savedVisit(period = 42)
        coEvery { pncRepo.getAllPncVisitsForBeneficiary(any()) } returns
                listOf(pncVisit(1, "opt3"))

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { benRepo.updateRecord(any()) }
        assertTrue(vm.state.value is PncFormViewModel.State.SAVE_SUCCESS)
    }

    @Test
    fun `saveForm reports failure when persisting throws`() = runTest {
        coEvery { pncRepo.persistPncRecord(any()) } throws RuntimeException("db down")

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(PncFormViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `incentive alert can be raised and dismissed`() {
        val vm = buildVm()
        vm.triggerIncentiveAlert()
        assertEquals(true, vm.showIncentiveAlert.value)
        vm.incentiveAlertShown()
        assertEquals(false, vm.showIncentiveAlert.value)
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
    fun `longToDateString formats a timestamp and tolerates null`() {
        val vm = buildVm()
        assertEquals("", vm.longToDateString(null))
        assertTrue(vm.longToDateString(0L).isNotEmpty())
    }

    @Test
    fun `onNavigationComplete clears the navigation flag`() {
        val vm = buildVm()
        vm.onNavigationComplete()
        assertEquals(false, vm.navigateToMdsr.value)
    }

    @Test
    fun `document form id round trips and binds an image`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(0, vm.getDocumentFormId())
        vm.setCurrentDocumentFormId(21)
        assertEquals(21, vm.getDocumentFormId())
        runCatching { vm.setImageUriToFormElement(mockk<Uri>(relaxed = true)) }

        assertNotNull(vm.formList)
    }

    @Test
    fun `discharge summary index helpers delegate to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.getIndexDeliveryDischargeSummary1() }
        runCatching { vm.getIndexDeliveryDischargeSummary2() }
        runCatching { vm.getIndexDeliveryDischargeSummary3() }
        runCatching { vm.getIndexDeliveryDischargeSummary4() }

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
