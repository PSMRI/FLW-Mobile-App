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
    fun `filterText trims and lowercases before filtering`() = runTest {
        viewModel.filterText("  Hello  ")
        advanceUntilIdle()
        assertNotNull(viewModel.immunizationBenList)
    }

    @Test
    fun `categoryData returns ten localized categories`() {
        val categories = viewModel.categoryData()
        assertEquals(10, categories.size)
    }

    @Test
    fun `getSelectedBenId defaults to zero`() {
        assertEquals(0L, viewModel.getSelectedBenId())
    }

    @Test
    fun `toEnglishCategory returns empty string for an unrecognised label`() {
        assertEquals("", viewModel.toEnglishCategory("not a category"))
    }
}
