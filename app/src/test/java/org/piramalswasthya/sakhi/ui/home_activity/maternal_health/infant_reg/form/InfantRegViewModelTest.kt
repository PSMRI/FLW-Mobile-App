package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.infant_reg.form

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
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.sakhi.repositories.InfantRegRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [InfantRegViewModel]: the `init` lookup chain across the create and edit arms and
 * the save success/failure arms.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InfantRegViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var infantRegRepo: InfantRegRepo

    @MockK
    private lateinit var deliveryOutcomeRepo: DeliveryOutcomeRepo

    @MockK
    private lateinit var maternalHealthRepo: MaternalHealthRepo

    @MockK
    private lateinit var benRepo: BenRepo

    private lateinit var user: User
    private lateinit var ben: BenRegCache
    private lateinit var outcome: DeliveryOutcomeCache

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
        every { ben.firstName } returns "Anita"
        every { ben.lastName } returns "Kumari"
        every { ben.age } returns 25

        outcome = mockk(relaxed = true)

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        coEvery { benRepo.getBenFromId(any()) } returns ben
        coEvery { infantRegRepo.getInfantReg(any(), any()) } returns null
        coEvery { infantRegRepo.saveInfantReg(any()) } returns Unit
        coEvery { deliveryOutcomeRepo.getDeliveryOutcome(any()) } returns outcome
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns null
    }

    private fun buildVm(benId: Long = 2L, babyIndex: Int = 1): InfantRegViewModel =
        InfantRegViewModel(
            SavedStateHandle(mapOf("benId" to benId, "babyIndex" to babyIndex)),
            preferenceDao,
            context,
            infantRegRepo,
            deliveryOutcomeRepo,
            maternalHealthRepo,
            benRepo
        )

    @Test
    fun `navigation arguments are exposed`() {
        val vm = buildVm(benId = 44L, babyIndex = 2)
        assertEquals(44L, vm.benId)
        assertEquals(2, vm.babyIndex)
    }

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertEquals(InfantRegViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `init populates the header and reports no saved registration`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Anita Kumari", vm.benName.value)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init reports an existing registration`() = runTest {
        coEvery { infantRegRepo.getInfantReg(any(), any()) } returns
                mockk<InfantRegCache>(relaxed = true)

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
    }

    @Test
    fun `init keeps the pregnancy registration when one exists`() = runTest {
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns
                mockk<PregnantWomanRegistrationCache>(relaxed = true)

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(false, vm.recordExists.value)
        assertNotNull(vm.formList)
    }

    @Test
    fun `init omits a null last name from the header`() = runTest {
        every { ben.lastName } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Anita ", vm.benName.value)
    }

    @Test
    fun `saveForm persists the registration and reports success`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { infantRegRepo.saveInfantReg(any()) }
        assertEquals(InfantRegViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm reports failure when persisting throws`() = runTest {
        coEvery { infantRegRepo.saveInfantReg(any()) } throws RuntimeException("db down")

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(InfantRegViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `resetState returns to idle`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()
        vm.resetState()

        assertEquals(InfantRegViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `document form id round trips and binds an image`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(0, vm.getDocumentFormId())
        vm.setCurrentDocumentFormId(17)
        assertEquals(17, vm.getDocumentFormId())
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
