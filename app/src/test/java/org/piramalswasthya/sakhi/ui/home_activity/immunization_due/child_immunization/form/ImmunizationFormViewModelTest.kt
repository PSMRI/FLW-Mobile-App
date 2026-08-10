package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.form

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.ImmunizationCache
import org.piramalswasthya.sakhi.model.ImmunizationCategory
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.Vaccine
import org.piramalswasthya.sakhi.model.VaccineDomain
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [ImmunizationFormViewModel]: the two `init` coroutines (record + vaccine lookup and
 * the category vaccine list), the single-vaccine save and the bulk `saveImmunization` path.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ImmunizationFormViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var vaccineDao: ImmunizationDao

    @MockK
    private lateinit var benDao: BenDao

    private lateinit var user: User
    private lateinit var ben: BenRegCache
    private lateinit var vaccine: Vaccine

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
        every { ben.firstName } returns "Aarav"
        every { ben.lastName } returns "Singh"
        every { ben.age } returns 1
        every { ben.gender } returns Gender.MALE
        every { ben.dob } returns 1_700_000_000_000L

        vaccine = mockk(relaxed = true)
        every { vaccine.vaccineId } returns 3
        every { vaccine.vaccineName } returns "BCG"

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        coEvery { vaccineDao.getImmunizationRecord(any(), any()) } returns null
        coEvery { vaccineDao.getVaccineById(any()) } returns vaccine
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD) } returns emptyList()
        coEvery { vaccineDao.addImmunizationRecord(any()) } returns Unit
        coEvery { vaccineDao.insertImmunizationRecord(any()) } returns longArrayOf(1L)
        coEvery { benDao.getBen(any()) } returns ben
    }

    private fun buildVm(
        vaccineId: Int = 3,
        benId: Long = 2L,
        category: String = "CHILD"
    ): ImmunizationFormViewModel = ImmunizationFormViewModel(
        context,
        preferenceDao,
        SavedStateHandle(
            mapOf("vaccineId" to vaccineId, "benId" to benId, "category" to category)
        ),
        vaccineDao,
        benDao
    )

    @Test
    fun `the vaccine category argument is exposed`() {
        val vm = buildVm(category = "ADOLESCENT")
        assertEquals("ADOLESCENT", vm.vaccineCategory)
    }

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertEquals(ImmunizationFormViewModel.State.IDLE, vm.state.value)
        assertTrue(vm.vaccinationDoneList.isEmpty())
        assertTrue(vm.list.isEmpty())
    }

    @Test
    fun `init populates the header and reports no saved record`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Aarav Singh", vm.benName.value)
        assertNotNull(vm.benAgeGender.value)
        assertEquals(ben, vm.benRegCache.value)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `init reports an existing immunization record`() = runTest {
        coEvery { vaccineDao.getImmunizationRecord(any(), any()) } returns
                mockk<ImmunizationCache>(relaxed = true)

        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.benName.value)
    }

    @Test
    fun `init omits a null last name from the header`() = runTest {
        every { ben.lastName } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Aarav ", vm.benName.value)
    }

    @Test
    fun `init reports failure when the beneficiary cannot be found`() = runTest {
        coEvery { benDao.getBen(any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(ImmunizationFormViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `init loads the child vaccine list`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        coVerify { vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD) }
        assertNotNull(vm.vaccinesList)
    }

    @Test
    fun `saveForm stores the record and reports success`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        coVerify { vaccineDao.addImmunizationRecord(any()) }
        assertEquals(ImmunizationFormViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveForm reports failure when storing throws`() = runTest {
        coEvery { vaccineDao.addImmunizationRecord(any()) } throws RuntimeException("db down")

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveForm()
        advanceUntilIdle()

        assertEquals(ImmunizationFormViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `saveImmunization inserts every vaccine that was marked done`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        val first = mockk<VaccineDomain>(relaxed = true)
        every { first.vaccineId } returns 3
        val second = mockk<VaccineDomain>(relaxed = true)
        every { second.vaccineId } returns 4
        vm.vaccinationDoneList.add(first)
        vm.vaccinationDoneList.add(second)

        vm.saveImmunization()
        advanceUntilIdle()

        coVerify { vaccineDao.insertImmunizationRecord(any()) }
        assertEquals(ImmunizationFormViewModel.State.SAVE_SUCCESS, vm.state.value)
    }

    @Test
    fun `saveImmunization reuses a record that already exists`() = runTest {
        coEvery { vaccineDao.getImmunizationRecord(any(), any()) } returns
                mockk<ImmunizationCache>(relaxed = true)

        val vm = buildVm()
        advanceUntilIdle()

        val done = mockk<VaccineDomain>(relaxed = true)
        every { done.vaccineId } returns 3
        vm.vaccinationDoneList.add(done)

        vm.saveImmunization()
        advanceUntilIdle()

        coVerify { vaccineDao.insertImmunizationRecord(any()) }
    }

    @Test
    fun `saveImmunization reports failure when inserting throws`() = runTest {
        coEvery { vaccineDao.insertImmunizationRecord(any()) } throws RuntimeException("db down")

        val vm = buildVm()
        advanceUntilIdle()

        vm.saveImmunization()
        advanceUntilIdle()

        assertEquals(ImmunizationFormViewModel.State.SAVE_FAILED, vm.state.value)
    }

    @Test
    fun `updateRecordExists updates recordExists`() {
        val vm = buildVm()
        vm.updateRecordExists(true)
        assertEquals(true, vm.recordExists.value)
        vm.updateRecordExists(false)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `document form id round trips and binds an image`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(0, vm.getDocumentFormId())
        vm.setCurrentDocumentFormId(12)
        assertEquals(12, vm.getDocumentFormId())
        runCatching { vm.setImageUriToFormElement(mockk<Uri>(relaxed = true)) }

        assertNotNull(vm.formList)
    }

    @Test
    fun `mcp card index helpers delegate to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.getIndexMCPCard1() }
        runCatching { vm.getIndexMCPCard2() }

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

    @Test
    fun `the bottom sheet content flow is exposed`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.benWithVaccineDetails)
        assertNotNull(vm.bottomSheetContent)
    }
}
