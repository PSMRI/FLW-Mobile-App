package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.list

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenBasicCache
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.ChildImmunizationCategory
import org.piramalswasthya.sakhi.model.ChildImmunizationDetailsCache
import org.piramalswasthya.sakhi.model.ImmunizationCache
import org.piramalswasthya.sakhi.model.ImmunizationCategory
import org.piramalswasthya.sakhi.model.Vaccine
import org.piramalswasthya.sakhi.model.VaccineState
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class ChildImmunizationListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var vaccineDao: ImmunizationDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources

    private lateinit var viewModel: ChildImmunizationListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        val realDefaultDispatcher = Dispatchers.Default
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        every { Dispatchers.Default } returns realDefaultDispatcher
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getString(any()) } returns "All"
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { vaccineDao.getBenWithImmunizationRecords(any(), any()) } returns flowOf(emptyList())
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD) } returns emptyList()
        viewModel = ChildImmunizationListViewModel(vaccineDao, preferenceDao, context, SavedStateHandle())
    }

    private fun benDomain(benId: Long, isDeath: Boolean = false): BenBasicDomain {
        val domain = mockk<BenBasicDomain>(relaxed = true)
        every { domain.benId } returns benId
        every { domain.isDeath } returns isDeath
        return domain
    }

    private fun benCache(benId: Long, dobMillisAgo: Long, isDeath: Boolean = false): BenBasicCache {
        val ben = mockk<BenBasicCache>(relaxed = true)
        every { ben.dob } returns System.currentTimeMillis() - dobMillisAgo
        every { ben.asBasicDomainModel() } returns benDomain(benId, isDeath)
        return ben
    }

    private val day = 24L * 60 * 60 * 1000

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benWithVaccineDetails marks a taken vaccine as DONE regardless of age`() = runTest {
        val ben = benCache(benId = 1L, dobMillisAgo = 40 * day)
        val cache = ChildImmunizationDetailsCache(
            ben = ben,
            givenVaccines = listOf(
                ImmunizationCache(
                    beneficiaryId = 1L,
                    vaccineId = 2,
                    createdBy = "asha",
                    updatedBy = "asha",
                    syncState = SyncState.SYNCED
                )
            )
        )
        val vaccine = Vaccine(
            vaccineId = 2,
            vaccineName = "Unknown Vaccine",
            minAllowedAgeInMillis = 0,
            maxAllowedAgeInMillis = 10 * day,
            category = ImmunizationCategory.CHILD,
            immunizationService = ChildImmunizationCategory.BIRTH,
        )
        every { vaccineDao.getBenWithImmunizationRecords(any(), any()) } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD) } returns listOf(vaccine)

        val vm = ChildImmunizationListViewModel(vaccineDao, preferenceDao, context, SavedStateHandle())
        advanceUntilIdle()

        val details = vm.benWithVaccineDetails.first()
        assertEquals(1, details.size)
        assertEquals(VaccineState.DONE, details[0].vaccineStateList[0].state)
    }

    @Test
    fun `benWithVaccineDetails marks an untaken vaccine past its max age as MISSED`() = runTest {
        val ben = benCache(benId = 1L, dobMillisAgo = 40 * day)
        val cache = ChildImmunizationDetailsCache(ben = ben, givenVaccines = emptyList())
        val vaccine = Vaccine(
            vaccineId = 1,
            vaccineName = "Unknown Vaccine",
            minAllowedAgeInMillis = 0,
            maxAllowedAgeInMillis = 20 * day,
            category = ImmunizationCategory.CHILD,
            immunizationService = ChildImmunizationCategory.BIRTH,
        )
        every { vaccineDao.getBenWithImmunizationRecords(any(), any()) } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD) } returns listOf(vaccine)

        val vm = ChildImmunizationListViewModel(vaccineDao, preferenceDao, context, SavedStateHandle())
        advanceUntilIdle()

        val details = vm.benWithVaccineDetails.first()
        assertEquals(VaccineState.MISSED, details[0].vaccineStateList[0].state)
    }

    @Test
    fun `benWithVaccineDetails applies the one-month buffer for birth-dose vaccines`() = runTest {
        val ben = benCache(benId = 1L, dobMillisAgo = 40 * day)
        val cache = ChildImmunizationDetailsCache(ben = ben, givenVaccines = emptyList())
        val vaccine = Vaccine(
            vaccineId = 3,
            vaccineName = "Hepatitis-B Vaccine (HBV)-Birth",
            minAllowedAgeInMillis = 0,
            maxAllowedAgeInMillis = 35 * day,
            category = ImmunizationCategory.CHILD,
            immunizationService = ChildImmunizationCategory.BIRTH,
        )
        every { vaccineDao.getBenWithImmunizationRecords(any(), any()) } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD) } returns listOf(vaccine)

        val vm = ChildImmunizationListViewModel(vaccineDao, preferenceDao, context, SavedStateHandle())
        advanceUntilIdle()

        val details = vm.benWithVaccineDetails.first()
        assertEquals(VaccineState.OVERDUE, details[0].vaccineStateList[0].state)
    }

    @Test
    fun `benWithVaccineDetails marks a within-window vaccine as OVERDUE without buffer`() = runTest {
        val ben = benCache(benId = 1L, dobMillisAgo = 40 * day)
        val cache = ChildImmunizationDetailsCache(ben = ben, givenVaccines = emptyList())
        val vaccine = Vaccine(
            vaccineId = 4,
            vaccineName = "Pentavalent-1",
            minAllowedAgeInMillis = 0,
            maxAllowedAgeInMillis = 50 * day,
            category = ImmunizationCategory.CHILD,
            immunizationService = ChildImmunizationCategory.WEEK_6,
        )
        every { vaccineDao.getBenWithImmunizationRecords(any(), any()) } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD) } returns listOf(vaccine)

        val vm = ChildImmunizationListViewModel(vaccineDao, preferenceDao, context, SavedStateHandle())
        advanceUntilIdle()

        val details = vm.benWithVaccineDetails.first()
        assertEquals(VaccineState.OVERDUE, details[0].vaccineStateList[0].state)
        assertNotNull(details[0].vaccineStateList[0].dueDate)
    }

    @Test
    fun `benWithVaccineDetails excludes vaccines not yet within their min age window`() = runTest {
        val ben = benCache(benId = 1L, dobMillisAgo = 5 * day)
        val cache = ChildImmunizationDetailsCache(ben = ben, givenVaccines = emptyList())
        val vaccine = Vaccine(
            vaccineId = 5,
            vaccineName = "Pentavalent-1",
            minAllowedAgeInMillis = 100 * day,
            maxAllowedAgeInMillis = 200 * day,
            category = ImmunizationCategory.CHILD,
            immunizationService = ChildImmunizationCategory.WEEK_6,
        )
        every { vaccineDao.getBenWithImmunizationRecords(any(), any()) } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD) } returns listOf(vaccine)

        val vm = ChildImmunizationListViewModel(vaccineDao, preferenceDao, context, SavedStateHandle())
        advanceUntilIdle()

        val details = vm.benWithVaccineDetails.first()
        assertTrue(details[0].vaccineStateList.isEmpty())
    }

    @Test
    fun `immunizationBenList passes through the unfiltered list when showDueOnly is false`() = runTest {
        val ben = benCache(benId = 1L, dobMillisAgo = 40 * day)
        val cache = ChildImmunizationDetailsCache(ben = ben, givenVaccines = emptyList())
        every { vaccineDao.getBenWithImmunizationRecords(any(), any()) } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD) } returns emptyList()

        val vm = ChildImmunizationListViewModel(vaccineDao, preferenceDao, context, SavedStateHandle())
        advanceUntilIdle()

        val list = vm.immunizationBenList.first()
        assertEquals(1, list.size)
    }

    @Test
    fun `updateBottomSheetData and bottomSheetContent resolve the matching beneficiary`() = runTest {
        val ben = benCache(benId = 7L, dobMillisAgo = 40 * day)
        val cache = ChildImmunizationDetailsCache(ben = ben, givenVaccines = emptyList())
        every { vaccineDao.getBenWithImmunizationRecords(any(), any()) } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD) } returns emptyList()

        val vm = ChildImmunizationListViewModel(vaccineDao, preferenceDao, context, SavedStateHandle())
        advanceUntilIdle()
        vm.updateBottomSheetData(7L)
        advanceUntilIdle()

        assertEquals(7L, vm.getSelectedBenId())
        val bottomSheet = vm.bottomSheetContent.first()
        assertEquals(7L, bottomSheet?.ben?.benId)
    }

    @Test
    fun `isSelectedBenDeathFlow reflects the selected beneficiary's death status`() = runTest {
        val ben = benCache(benId = 9L, dobMillisAgo = 40 * day, isDeath = true)
        val cache = ChildImmunizationDetailsCache(ben = ben, givenVaccines = emptyList())
        every { vaccineDao.getBenWithImmunizationRecords(any(), any()) } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD) } returns emptyList()

        val vm = ChildImmunizationListViewModel(vaccineDao, preferenceDao, context, SavedStateHandle())
        advanceUntilIdle()
        vm.updateBottomSheetData(9L)
        advanceUntilIdle()

        assertTrue(vm.isSelectedBenDeathFlow.first())
    }

    @Test
    fun `setSearchText trims and lowercases before filtering`() = runTest {
        viewModel.setSearchText("  Hello  ")
        advanceUntilIdle()
        assertNotNull(viewModel.immunizationBenList)
    }

    // FLW-1144: 10 YEARS / 16 YEARS removed, leaving All + 7 dose stages.
    @Test
    fun `categoryData returns eight localized categories`() {
        val categories = viewModel.categoryData()
        assertEquals(8, categories.size)
    }

    @Test
    fun `getSelectedBenId defaults to zero`() {
        assertEquals(0L, viewModel.getSelectedBenId())
    }

    @Test
    fun `toCategory returns null for an unrecognised label`() {
        assertNull(viewModel.toCategory("not a category"))
    }

    // ===============================================================
    // FLW-1144 quick worklist buckets
    // ===============================================================

    /**
     * Builds a VM over one child with one vaccine whose due date (dob + minAllowedAge)
     * lands [dueInDays] from now - negative for already past due.
     */
    private fun vmWithVaccineDue(
        dueInDays: Long,
        stage: ChildImmunizationCategory = ChildImmunizationCategory.BIRTH,
        given: Boolean = false,
    ): ChildImmunizationListViewModel {
        val ageMillis = 400 * day
        val ben = benCache(benId = 1L, dobMillisAgo = ageMillis)
        val cache = ChildImmunizationDetailsCache(
            ben = ben,
            givenVaccines = if (given) listOf(
                ImmunizationCache(
                    beneficiaryId = 1L,
                    vaccineId = 1,
                    createdBy = "asha",
                    updatedBy = "asha",
                    syncState = SyncState.SYNCED
                )
            ) else emptyList()
        )
        // dueDateMillis = dob + minAllowedAge, so minAllowedAge = age + dueInDays
        val vaccine = Vaccine(
            vaccineId = 1,
            vaccineName = "Unknown Vaccine",
            minAllowedAgeInMillis = ageMillis + dueInDays * day,
            maxAllowedAgeInMillis = ageMillis + (dueInDays + 365) * day,
            category = ImmunizationCategory.CHILD,
            immunizationService = stage,
        )
        every { vaccineDao.getBenWithImmunizationRecords(any(), any()) } returns flowOf(listOf(cache))
        coEvery { vaccineDao.getVaccinesForCategory(ImmunizationCategory.CHILD) } returns listOf(vaccine)
        return ChildImmunizationListViewModel(vaccineDao, preferenceDao, context, SavedStateHandle())
    }

    @Test
    fun `due this month counts a dose whose due date has already passed`() = runTest {
        // Open-ended to the left: outstanding work carries forward, it does not drop off.
        val vm = vmWithVaccineDue(dueInDays = -400)
        advanceUntilIdle()
        assertEquals(1, vm.dueThisMonthCount.first())
    }

    @Test
    fun `due this month ignores an administered dose`() = runTest {
        val vm = vmWithVaccineDue(dueInDays = -400, given = true)
        advanceUntilIdle()
        assertEquals(0, vm.dueThisMonthCount.first())
    }

    @Test
    fun `next month excludes a dose due in the current month`() = runTest {
        val vm = vmWithVaccineDue(dueInDays = -400)
        advanceUntilIdle()
        assertEquals(0, vm.nextMonthCount.first())
    }

    @Test
    fun `next month excludes a dose due far in the future`() = runTest {
        val vm = vmWithVaccineDue(dueInDays = 200)
        advanceUntilIdle()
        assertEquals(0, vm.nextMonthCount.first())
    }

    @Test
    fun `dose stage and bucket must be satisfied by the same vaccine`() = runTest {
        // The child's only past-due dose is a BIRTH dose. Filtering by WEEK_6 must not
        // match them just because some other vaccine would satisfy the bucket.
        val vm = vmWithVaccineDue(dueInDays = -400, stage = ChildImmunizationCategory.BIRTH)
        advanceUntilIdle()
        assertEquals(1, vm.dueThisMonthCount.first())

        vm.setDoseStage(ChildImmunizationCategory.WEEK_6)
        advanceUntilIdle()
        assertEquals(0, vm.dueThisMonthCount.first())
    }

    @Test
    fun `toggleQuickFilter selects then clears on a second tap`() = runTest {
        assertNull(viewModel.selectedQuickFilter)

        viewModel.toggleQuickFilter(ChildImmunizationListViewModel.QuickFilter.DUE_THIS_MONTH)
        advanceUntilIdle()
        assertEquals(
            ChildImmunizationListViewModel.QuickFilter.DUE_THIS_MONTH,
            viewModel.selectedQuickFilter
        )

        viewModel.toggleQuickFilter(ChildImmunizationListViewModel.QuickFilter.DUE_THIS_MONTH)
        advanceUntilIdle()
        assertNull(viewModel.selectedQuickFilter)
    }

    @Test
    fun `toggleQuickFilter switching bucket replaces rather than clears`() = runTest {
        viewModel.toggleQuickFilter(ChildImmunizationListViewModel.QuickFilter.DUE_THIS_MONTH)
        advanceUntilIdle()
        viewModel.toggleQuickFilter(ChildImmunizationListViewModel.QuickFilter.NEXT_MONTH)
        advanceUntilIdle()
        assertEquals(
            ChildImmunizationListViewModel.QuickFilter.NEXT_MONTH,
            viewModel.selectedQuickFilter
        )
    }

    @Test
    fun `isFiltered is false by default and true once a bucket is picked`() = runTest {
        assertFalse(viewModel.isFiltered.first())
        viewModel.toggleQuickFilter(ChildImmunizationListViewModel.QuickFilter.NEXT_MONTH)
        advanceUntilIdle()
        assertTrue(viewModel.isFiltered.first())
    }

    @Test
    fun `search and dose stage no longer clear one another`() = runTest {
        // The old single-channel filter made these mutually exclusive.
        viewModel.setSearchText("asha")
        viewModel.setDoseStage(ChildImmunizationCategory.WEEK_6)
        advanceUntilIdle()
        assertTrue(viewModel.isFiltered.first())
        assertNotNull(viewModel.immunizationBenList.first())
    }
}
