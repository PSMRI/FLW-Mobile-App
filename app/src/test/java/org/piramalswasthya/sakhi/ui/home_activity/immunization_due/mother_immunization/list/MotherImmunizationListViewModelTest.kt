package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.mother_immunization.list

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.helpers.getTodayMillis
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.ChildImmunizationCategory
import org.piramalswasthya.sakhi.model.ImmunizationCache
import org.piramalswasthya.sakhi.model.ImmunizationCategory
import org.piramalswasthya.sakhi.model.MotherImmunizationDetailsCache
import org.piramalswasthya.sakhi.model.Vaccine
import org.piramalswasthya.sakhi.model.VaccineState

@OptIn(ExperimentalCoroutinesApi::class)
class MotherImmunizationListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var vaccineDao: ImmunizationDao

    private lateinit var viewModel: MotherImmunizationListViewModel

    private val day = 24L * 60 * 60 * 1000

    @Before
    override fun setUp() {
        super.setUp()
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.MOTHER) } returns emptyList()
        every { vaccineDao.getBenWithImmunizationRecords() } returns flowOf(emptyList())
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        viewModel = MotherImmunizationListViewModel(vaccineDao)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun benDomain(benId: Long, isDeath: Boolean = false): BenBasicDomain {
        val domain = mockk<BenBasicDomain>(relaxed = true)
        every { domain.benId } returns benId
        every { domain.isDeath } returns isDeath
        return domain
    }

    private fun benCache(benId: Long, isDeath: Boolean = false): BenBasicCache {
        val ben = mockk<BenBasicCache>(relaxed = true)
        every { ben.asBasicDomainModel() } returns benDomain(benId, isDeath)
        return ben
    }

    private fun buildVm(): MotherImmunizationListViewModel {
        val vm = MotherImmunizationListViewModel(vaccineDao)
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benWithVaccineDetails flow is not null`() {
        assertNotNull(viewModel.benWithVaccineDetails)
    }

    @Test
    fun `bottomSheetContent flow is not null`() {
        assertNotNull(viewModel.bottomSheetContent)
    }

    @Test
    fun `getSelectedBenId returns 0 initially`() {
        assertEquals(0L, viewModel.getSelectedBenId())
    }

    // =====================================================
    // updateBottomSheetData() Tests
    // =====================================================

    @Test
    fun `updateBottomSheetData does not throw`() = runTest {
        viewModel.updateBottomSheetData(42L)
        advanceUntilIdle()
    }

    @Test
    fun `updateBottomSheetData updates getSelectedBenId`() = runTest {
        viewModel.updateBottomSheetData(42L)
        advanceUntilIdle()
        assertEquals(42L, viewModel.getSelectedBenId())
    }

    // =====================================================
    // benWithVaccineDetails business logic
    // =====================================================

    @Test
    fun `benWithVaccineDetails marks a taken vaccine as DONE`() = runTest {
        val today = getTodayMillis()
        val ben = benCache(benId = 1L)
        val cache = MotherImmunizationDetailsCache(
            ben = ben,
            lmp = today - 100 * day,
            givenVaccines = listOf(
                ImmunizationCache(
                    beneficiaryId = 1L,
                    vaccineId = 10,
                    createdBy = "asha",
                    updatedBy = "asha",
                    syncState = SyncState.SYNCED,
                )
            )
        )
        val vaccine = Vaccine(
            vaccineId = 10,
            vaccineName = "TT-1",
            minAllowedAgeInMillis = 0,
            maxAllowedAgeInMillis = 200 * day,
            category = ImmunizationCategory.MOTHER,
            immunizationService = ChildImmunizationCategory.BIRTH,
        )
        every { vaccineDao.getBenWithImmunizationRecords() } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.MOTHER) } returns listOf(vaccine)

        val vm = buildVm()
        val details = vm.benWithVaccineDetails.first()

        assertEquals(1, details.size)
        assertEquals(VaccineState.DONE, details[0].vaccineStateList[0].state)
    }

    @Test
    fun `benWithVaccineDetails marks an untaken vaccine without a dependency as PENDING while within window`() =
        runTest {
            val today = getTodayMillis()
            val ben = benCache(benId = 1L)
            val cache = MotherImmunizationDetailsCache(
                ben = ben,
                lmp = today - 100 * day,
                givenVaccines = emptyList()
            )
            val vaccine = Vaccine(
                vaccineId = 11,
                vaccineName = "TT-2",
                minAllowedAgeInMillis = 0,
                maxAllowedAgeInMillis = 200 * day,
                category = ImmunizationCategory.MOTHER,
                immunizationService = ChildImmunizationCategory.BIRTH,
                dependantVaccineId = null,
            )
            every { vaccineDao.getBenWithImmunizationRecords() } returns flowOf(listOf(cache))
            coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.MOTHER) } returns listOf(vaccine)

            val vm = buildVm()
            val details = vm.benWithVaccineDetails.first()

            assertEquals(VaccineState.PENDING, details[0].vaccineStateList[0].state)
        }

    @Test
    fun `benWithVaccineDetails marks an untaken vaccine past its max age as MISSED`() = runTest {
        val today = getTodayMillis()
        val ben = benCache(benId = 1L)
        val cache = MotherImmunizationDetailsCache(
            ben = ben,
            lmp = today - 300 * day,
            givenVaccines = emptyList()
        )
        val vaccine = Vaccine(
            vaccineId = 12,
            vaccineName = "TT-Booster",
            minAllowedAgeInMillis = 0,
            maxAllowedAgeInMillis = 200 * day,
            category = ImmunizationCategory.MOTHER,
            immunizationService = ChildImmunizationCategory.BIRTH,
        )
        every { vaccineDao.getBenWithImmunizationRecords() } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.MOTHER) } returns listOf(vaccine)

        val vm = buildVm()
        val details = vm.benWithVaccineDetails.first()

        assertEquals(VaccineState.MISSED, details[0].vaccineStateList[0].state)
    }

    @Test
    fun `benWithVaccineDetails marks a dependent vaccine as PENDING when its dependency was given`() = runTest {
        val today = getTodayMillis()
        val ben = benCache(benId = 1L)
        val cache = MotherImmunizationDetailsCache(
            ben = ben,
            lmp = today - 100 * day,
            givenVaccines = listOf(
                ImmunizationCache(
                    beneficiaryId = 1L,
                    vaccineId = 20,
                    createdBy = "asha",
                    updatedBy = "asha",
                    syncState = SyncState.SYNCED,
                )
            )
        )
        val vaccine = Vaccine(
            vaccineId = 21,
            vaccineName = "TT-2",
            minAllowedAgeInMillis = 0,
            maxAllowedAgeInMillis = 200 * day,
            category = ImmunizationCategory.MOTHER,
            immunizationService = ChildImmunizationCategory.BIRTH,
            dependantVaccineId = 20,
        )
        every { vaccineDao.getBenWithImmunizationRecords() } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.MOTHER) } returns listOf(vaccine)

        val vm = buildVm()
        val details = vm.benWithVaccineDetails.first()

        assertEquals(VaccineState.PENDING, details[0].vaccineStateList[0].state)
    }

    @Test
    fun `benWithVaccineDetails marks a dependent vaccine as MISSED when its dependency was not given`() = runTest {
        val today = getTodayMillis()
        val ben = benCache(benId = 1L)
        val cache = MotherImmunizationDetailsCache(
            ben = ben,
            lmp = today - 100 * day,
            givenVaccines = emptyList()
        )
        val vaccine = Vaccine(
            vaccineId = 22,
            vaccineName = "TT-2",
            minAllowedAgeInMillis = 0,
            maxAllowedAgeInMillis = 200 * day,
            category = ImmunizationCategory.MOTHER,
            immunizationService = ChildImmunizationCategory.BIRTH,
            dependantVaccineId = 20,
        )
        every { vaccineDao.getBenWithImmunizationRecords() } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.MOTHER) } returns listOf(vaccine)

        val vm = buildVm()
        val details = vm.benWithVaccineDetails.first()

        assertEquals(VaccineState.MISSED, details[0].vaccineStateList[0].state)
    }

    @Test
    fun `benWithVaccineDetails excludes vaccines not yet within their min age window`() = runTest {
        val today = getTodayMillis()
        val ben = benCache(benId = 1L)
        val cache = MotherImmunizationDetailsCache(
            ben = ben,
            lmp = today - 10 * day,
            givenVaccines = emptyList()
        )
        val vaccine = Vaccine(
            vaccineId = 30,
            vaccineName = "TT-Late",
            minAllowedAgeInMillis = 100 * day,
            maxAllowedAgeInMillis = 200 * day,
            category = ImmunizationCategory.MOTHER,
            immunizationService = ChildImmunizationCategory.BIRTH,
        )
        every { vaccineDao.getBenWithImmunizationRecords() } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.MOTHER) } returns listOf(vaccine)

        val vm = buildVm()
        val details = vm.benWithVaccineDetails.first()

        assertTrue(details[0].vaccineStateList.isEmpty())
    }

    @Test
    fun `updateBottomSheetData and bottomSheetContent resolve the matching beneficiary`() = runTest {
        val today = getTodayMillis()
        val ben = benCache(benId = 7L)
        val cache = MotherImmunizationDetailsCache(
            ben = ben,
            lmp = today - 100 * day,
            givenVaccines = emptyList()
        )
        every { vaccineDao.getBenWithImmunizationRecords() } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.MOTHER) } returns emptyList()

        val vm = buildVm()
        vm.updateBottomSheetData(7L)
        advanceUntilIdle()

        assertEquals(7L, vm.getSelectedBenId())
        val bottomSheet = vm.bottomSheetContent.first()
        assertEquals(7L, bottomSheet?.ben?.benId)
    }

    @Test
    fun `bottomSheetContent is null when no beneficiary matches the selected id`() = runTest {
        val today = getTodayMillis()
        val ben = benCache(benId = 7L)
        val cache = MotherImmunizationDetailsCache(
            ben = ben,
            lmp = today - 100 * day,
            givenVaccines = emptyList()
        )
        every { vaccineDao.getBenWithImmunizationRecords() } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.MOTHER) } returns emptyList()

        val vm = buildVm()
        vm.updateBottomSheetData(999L)
        advanceUntilIdle()

        val bottomSheet = vm.bottomSheetContent.first()
        assertEquals(null, bottomSheet)
    }
}
