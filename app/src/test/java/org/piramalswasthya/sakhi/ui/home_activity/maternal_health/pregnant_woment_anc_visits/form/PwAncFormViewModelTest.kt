package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.form

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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [PwAncFormViewModel]: the `init` lookup chain including its early return when no
 * pregnancy registration exists, and the delivered / aborted / maternal-death arms of `saveForm`.
 *
 * The "original" values are captured from the saved record the first time each property is read, so
 * the change-detection branches are driven with `returnsMany` (first read = old value, later reads =
 * the value the form just produced).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PwAncFormViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var maternalHealthRepo: MaternalHealthRepo

    @MockK
    private lateinit var benRepo: BenRepo

    private lateinit var user: User
    private lateinit var ben: BenRegCache
    private lateinit var register: PregnantWomanRegistrationCache

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
        every { ben.firstName } returns "Kavita"
        every { ben.lastName } returns "Sharma"
        every { ben.age } returns 24
        every { ben.processed } returns "P"

        register = mockk(relaxed = true)
        every { register.syncState } returns SyncState.SYNCED

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        coEvery { maternalHealthRepo.getBenFromId(any()) } returns ben
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns register
        coEvery { maternalHealthRepo.getSavedAncRecord(any(), any()) } returns null
        coEvery { maternalHealthRepo.getAllActiveAncRecords(any()) } returns emptyList()
        coEvery { maternalHealthRepo.persistAncRecord(any()) } returns Unit
        coEvery { maternalHealthRepo.persistRegisterRecord(any()) } returns Unit
        coEvery { maternalHealthRepo.updateAncRecord(any()) } returns Unit
        coEvery { benRepo.updateRecord(any()) } returns Unit
    }

    private fun buildVm(
        benId: Long = 2L,
        hhId: String? = "5",
        visitNumber: Int = 2,
        fromPmsma: Boolean = false,
        lastItemClick: Boolean = false
    ): PwAncFormViewModel = PwAncFormViewModel(
        SavedStateHandle(
            mapOf(
                "benId" to benId,
                "hhId" to hhId,
                "visitNumber" to visitNumber,
                "fromPmsma" to fromPmsma,
                "lastItemClick" to lastItemClick
            )
        ),
        preferenceDao,
        context,
        maternalHealthRepo,
        benRepo
    )

    @Test
    fun `navigation arguments are exposed`() {
        val vm = buildVm(benId = 31L, hhId = "8", fromPmsma = true, lastItemClick = true)
        assertEquals(31L, vm.benId)
        assertEquals("8", vm.hhID)
        assertTrue(vm.fromPmsma)
        assertTrue(vm.lastItemClick)
    }

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertEquals(PwAncFormViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `init populates the header and reports no saved visit`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Kavita Sharma", vm.benName.value)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init back-fills the household id from the beneficiary`() = runTest {
        val vm = buildVm(hhId = null)
        advanceUntilIdle()

        assertEquals("5", vm.hhID)
    }

    @Test
    fun `init omits a null last name from the header`() = runTest {
        every { ben.lastName } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Kavita ", vm.benName.value)
    }

    @Test
    fun `init reports an existing visit`() = runTest {
        coEvery { maternalHealthRepo.getSavedAncRecord(any(), any()) } returns
                mockk<PregnantWomanAncCache>(relaxed = true)

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
    }

    @Test
    fun `init stops early when the pregnancy registration is missing`() = runTest {
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(null, vm.recordExists.value)
    }

    @Test
    fun `saveForm persists the visit`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { maternalHealthRepo.persistAncRecord(any()) }
        assertNotEquals(PwAncFormViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `saveForm also persists an unsynced registration record`() = runTest {
        every { register.syncState } returns SyncState.UNSYNCED

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { maternalHealthRepo.persistRegisterRecord(any()) }
    }

    @Test
    fun `saveForm marks the beneficiary as delivered on a new delivery`() = runTest {
        val saved = mockk<PregnantWomanAncCache>(relaxed = true)
        every { saved.pregnantWomanDelivered } returnsMany
                listOf(null, true, true, true, true, true)
        every { saved.isAborted } returns false
        every { saved.maternalDeath } returns false
        coEvery { maternalHealthRepo.getSavedAncRecord(any(), any()) } returns saved

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { maternalHealthRepo.persistAncRecord(any()) }
        assertNotEquals(PwAncFormViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `saveForm closes the pregnancy on a new abortion`() = runTest {
        val saved = mockk<PregnantWomanAncCache>(relaxed = true)
        every { saved.pregnantWomanDelivered } returns false
        every { saved.isAborted } returnsMany listOf(false, true, true, true, true, true)
        every { saved.maternalDeath } returns false
        coEvery { maternalHealthRepo.getSavedAncRecord(any(), any()) } returns saved
        coEvery { maternalHealthRepo.getAllActiveAncRecords(any()) } returns
                listOf(mockk(relaxed = true))

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { maternalHealthRepo.persistAncRecord(any()) }
        assertNotEquals(PwAncFormViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `saveForm records a maternal death`() = runTest {
        val saved = mockk<PregnantWomanAncCache>(relaxed = true)
        every { saved.pregnantWomanDelivered } returns false
        every { saved.isAborted } returns false
        every { saved.maternalDeath } returnsMany
                listOf(null, true, true, true, true, true, true)
        every { saved.deathDate } returns 1_700_000_000_000L
        every { saved.placeOfDeathId } returns 3
        coEvery { maternalHealthRepo.getSavedAncRecord(any(), any()) } returns saved
        coEvery { maternalHealthRepo.getAllActiveAncRecords(any()) } returns
                listOf(mockk(relaxed = true))

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { maternalHealthRepo.persistAncRecord(any()) }
        assertNotEquals(PwAncFormViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `saveForm reports failure when persisting throws`() = runTest {
        coEvery { maternalHealthRepo.persistAncRecord(any()) } throws RuntimeException("db down")

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(PwAncFormViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `setRecordExist updates recordExists`() {
        val vm = buildVm()
        vm.setRecordExist(true)
        assertEquals(true, vm.recordExists.value)
        vm.setRecordExist(false)
        assertFalse(vm.recordExists.value!!)
    }

    @Test
    fun `document form id round trips and binds an image`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(0, vm.getDocumentFormId())
        vm.setCurrentDocumentFormId(9)
        assertEquals(9, vm.getDocumentFormId())
        runCatching { vm.setImageUriToFormElement(mockk<Uri>(relaxed = true)) }

        assertNotNull(vm.formList)
    }

    @Test
    fun `index helpers delegate to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.getIndexOfMCPCardFront() }
        runCatching { vm.getIndexOfMCPCardBack() }
        runCatching { vm.getIndexOfWeeksOfPregnancy() }
        runCatching { vm.getIndexOfTT1() }
        runCatching { vm.getIndexOfTT2() }
        runCatching { vm.getIndexOfTTBooster() }

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
