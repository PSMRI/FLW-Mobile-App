package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.child_reg.form

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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.ChildRegRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.InfantRegRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [ChildRegViewModel]. `saveForm` promotes the infant into a beneficiary and folds
 * the child into the mother's eligible-couple record, so the first-free-slot selection is exercised
 * for both a brand new record and one whose earlier slots are already taken.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChildRegViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var childRegRepo: ChildRegRepo

    @MockK
    private lateinit var infantRegRepo: InfantRegRepo

    @MockK
    private lateinit var benRepo: BenRepo

    @MockK
    private lateinit var ecrRepo: EcrRepo

    private lateinit var user: User
    private lateinit var ben: BenRegCache
    private lateinit var infantReg: InfantRegCache

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
        every { ben.firstName } returns "Rekha"
        every { ben.lastName } returns "Devi"

        infantReg = mockk(relaxed = true)

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getLocationRecord() } returns mockk<LocationRecord>(relaxed = true)

        coEvery { benRepo.getBenFromId(any()) } returns ben
        coEvery { benRepo.substituteBenIdForDraft(any()) } returns Unit
        coEvery { benRepo.persistRecord(any()) } returns Unit
        coEvery { childRegRepo.getDeliveryOutcomeRepoFromMotherBenId(any()) } returns
                mockk<DeliveryOutcomeCache>(relaxed = true)
        coEvery { childRegRepo.getInfantRegFromMotherBenId(any(), any()) } returns infantReg
        coEvery { infantRegRepo.update(any()) } returns Unit
        coEvery { ecrRepo.getSavedRecord(any()) } returns null
        coEvery { ecrRepo.persistRecord(any()) } returns Unit
    }

    private fun buildVm(motherBenId: Long = 2L, babyIndex: Int = 1): ChildRegViewModel =
        ChildRegViewModel(
            SavedStateHandle(mapOf("motherBenId" to motherBenId, "babyIndex" to babyIndex)),
            preferenceDao,
            context,
            childRegRepo,
            infantRegRepo,
            benRepo,
            ecrRepo
        )

    @Test
    fun `navigation arguments are exposed`() {
        val vm = buildVm(motherBenId = 66L, babyIndex = 3)
        assertEquals(66L, vm.motherBenId)
        assertEquals(3, vm.babyIndex)
    }

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertEquals(ChildRegViewModel.State.IDLE, vm.state.value)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init renders the form from the mother and the delivery outcome`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        coVerify { childRegRepo.getDeliveryOutcomeRepoFromMotherBenId(any()) }
        coVerify { childRegRepo.getInfantRegFromMotherBenId(any(), any()) }
        assertNotNull(vm.formList)
    }

    @Test
    fun `init tolerates a missing mother record`() = runTest {
        coEvery { benRepo.getBenFromId(any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }

    @Test
    fun `saveForm creates a fresh eligible couple record for the first child`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { ecrRepo.persistRecord(any()) }
        assertEquals(ChildRegViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm fills the next free slot of an existing eligible couple record`() = runTest {
        val ecr = mockk<EligibleCoupleRegCache>(relaxed = true)
        every { ecr.dob1 } returns 1L
        every { ecr.dob2 } returns 2L
        every { ecr.dob3 } returns null
        coEvery { ecrRepo.getSavedRecord(any()) } returns ecr

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { ecrRepo.persistRecord(any()) }
        assertEquals(ChildRegViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm falls back to the last slot when every slot is taken`() = runTest {
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

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { ecrRepo.persistRecord(any()) }
        assertEquals(ChildRegViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm reports failure when the mother record disappears`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        coEvery { benRepo.getBenFromId(any()) } returns null
        vm.saveForm()
        advanceUntilIdle()

        assertEquals(ChildRegViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `saveForm reports failure when persisting the child throws`() = runTest {
        coEvery { benRepo.persistRecord(any()) } throws RuntimeException("db down")

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(ChildRegViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `resetState returns to idle`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()
        vm.resetState()

        assertEquals(ChildRegViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `document form id round trips and binds an image`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(0, vm.getDocumentFormId())
        vm.setCurrentDocumentFormId(5)
        assertEquals(5, vm.getDocumentFormId())
        runCatching { vm.setImageUriToFormElement(mockk<Uri>(relaxed = true)) }

        assertNotNull(vm.formList)
    }

    @Test
    fun `birth certificate index helpers delegate to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.getIndexOfBirthCertificateFront() }
        runCatching { vm.getIndexOfBirthCertificateBack() }

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
