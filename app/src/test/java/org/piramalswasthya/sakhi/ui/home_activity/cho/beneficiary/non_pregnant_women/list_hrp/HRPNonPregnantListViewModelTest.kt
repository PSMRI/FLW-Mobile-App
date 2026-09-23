package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.non_pregnant_women.list_hrp

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenBasicDomainForForm
import org.piramalswasthya.sakhi.model.BenWithHRNPTListDomain
import org.piramalswasthya.sakhi.model.HRPNonPregnantTrackCache
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class HRPNonPregnantListViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var hrpRepo: HRPRepo

    private lateinit var viewModel: HRPNonPregnantListViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(android.text.TextUtils::class)
        every { android.text.TextUtils.isDigitsOnly(any()) } answers {
            val str = firstArg<CharSequence>()
            str.all { it.isDigit() }
        }
        every { recordsRepo.hrpTrackingNonPregList } returns flowOf(emptyList())
        coEvery { hrpRepo.getAllNonPregTrack() } returns emptyList()
        viewModel = HRPNonPregnantListViewModel(recordsRepo, hrpRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benList is not null`() {
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `initial abha is null`() {
        assertNull(viewModel.abha.value)
    }

    @Test
    fun `initial benId is null`() {
        assertNull(viewModel.benId.value)
    }

    // =====================================================
    // filterText() Tests
    // =====================================================

    @Test
    fun `filterText does not throw`() = runTest {
        viewModel.filterText("test")
        advanceUntilIdle()
    }

    @Test
    fun `filterText with empty string does not throw`() = runTest {
        viewModel.filterText("")
        advanceUntilIdle()
    }

    // =====================================================
    // getTrackDetails() Tests
    // =====================================================

    @Test
    fun `getTrackDetails does not throw after init`() = runTest {
        advanceUntilIdle()
        viewModel.getTrackDetails()
    }

    private fun mkBen(
        benName: String = "",
        benSurname: String? = null
    ): BenBasicDomain {
        val b = mockk<BenBasicDomain>(relaxed = true)
        every { b.benName } returns benName
        every { b.benSurname } returns benSurname
        return b
    }

    private fun mkHrnptItem(ben: BenBasicDomain): BenWithHRNPTListDomain {
        val item = mockk<BenWithHRNPTListDomain>(relaxed = true)
        every { item.ben } returns ben
        return item
    }

    private fun mkBenForForm(form1Enabled: Boolean = true): BenBasicDomainForForm {
        return BenBasicDomainForForm(
            benId = 9L,
            hhId = 90L,
            regDate = "01-01-2026",
            benName = "Asha",
            gender = "Female",
            dob = 0L,
            mobileNo = "9999999999",
            familyHeadName = "Head",
            syncState = SyncState.SYNCED,
            isConsent = true,
            form1Enabled = form1Enabled
        )
    }

    @Test
    fun `setBenId updates benId livedata`() {
        viewModel.setBenId(42L)
        assertEquals(42L, viewModel.benId.value)
    }

    @Test
    fun `getTrackDetails returns null when benId was never set`() = runTest {
        advanceUntilIdle()
        assertNull(viewModel.getTrackDetails())
    }

    @Test
    fun `getTrackDetails returns track list from repo when benId is set`() = runTest {
        val tracks = listOf(HRPNonPregnantTrackCache(benId = 5L))
        coEvery { hrpRepo.getHrNonPregTrackList(5L) } returns tracks
        viewModel.setBenId(5L)
        advanceUntilIdle()
        val result = viewModel.getTrackDetails()
        assertEquals(1, result?.size)
    }

    @Test
    fun `getTrackDetails returns null when repo returns null for set benId`() = runTest {
        coEvery { hrpRepo.getHrNonPregTrackList(7L) } returns null
        viewModel.setBenId(7L)
        advanceUntilIdle()
        assertNull(viewModel.getTrackDetails())
    }

    @Test
    fun `updateBenWithForms leaves form1Enabled unchanged when repo returns null`() = runTest {
        val ben = mkBenForForm(form1Enabled = true)
        coEvery { hrpRepo.getHrNonPregTrackList(9L) } returns null
        viewModel.updateBenWithForms(ben)
        advanceUntilIdle()
        assertTrue(ben.form1Enabled)
    }

    @Test
    fun `updateBenWithForms leaves form1Enabled unchanged when track list is empty`() = runTest {
        val ben = mkBenForForm(form1Enabled = true)
        coEvery { hrpRepo.getHrNonPregTrackList(9L) } returns emptyList()
        viewModel.updateBenWithForms(ben)
        advanceUntilIdle()
        assertTrue(ben.form1Enabled)
    }

    @Test
    fun `updateBenWithForms leaves form1Enabled unchanged when visitDate is null`() = runTest {
        val ben = mkBenForForm(form1Enabled = true)
        coEvery { hrpRepo.getHrNonPregTrackList(9L) } returns listOf(
            HRPNonPregnantTrackCache(benId = 9L, visitDate = null)
        )
        viewModel.updateBenWithForms(ben)
        advanceUntilIdle()
        assertTrue(ben.form1Enabled)
    }

    @Test
    fun `updateBenWithForms sets form1Enabled true when visit month differs from current month`() = runTest {
        val ben = mkBenForForm(form1Enabled = false)
        val visitDate = Calendar.getInstance().apply { add(Calendar.MONTH, -2) }.timeInMillis
        coEvery { hrpRepo.getHrNonPregTrackList(9L) } returns listOf(
            HRPNonPregnantTrackCache(benId = 9L, visitDate = visitDate)
        )
        viewModel.updateBenWithForms(ben)
        advanceUntilIdle()
        assertTrue(ben.form1Enabled)
    }

    @Test
    fun `updateBenWithForms sets form1Enabled false when visit month matches current month`() = runTest {
        val ben = mkBenForForm(form1Enabled = true)
        val visitDate = Calendar.getInstance().timeInMillis
        coEvery { hrpRepo.getHrNonPregTrackList(9L) } returns listOf(
            HRPNonPregnantTrackCache(benId = 9L, visitDate = visitDate)
        )
        viewModel.updateBenWithForms(ben)
        advanceUntilIdle()
        assertFalse(ben.form1Enabled)
    }

    @Test
    fun `benList emits full list when filter text is blank`() = runTest {
        val itemA = mkHrnptItem(mkBen(benName = "Asha"))
        val itemB = mkHrnptItem(mkBen(benName = "Priya"))
        every { recordsRepo.hrpTrackingNonPregList } returns flowOf(listOf(itemA, itemB))
        val vm = HRPNonPregnantListViewModel(recordsRepo, hrpRepo)
        advanceUntilIdle()
        val result = vm.benList.first()
        assertEquals(2, result.size)
    }

    @Test
    fun `benList narrows results when filterText is applied`() = runTest {
        val itemA = mkHrnptItem(mkBen(benName = "Asha"))
        val itemB = mkHrnptItem(mkBen(benName = "Priya"))
        every { recordsRepo.hrpTrackingNonPregList } returns flowOf(listOf(itemA, itemB))
        val vm = HRPNonPregnantListViewModel(recordsRepo, hrpRepo)
        vm.filterText("asha")
        advanceUntilIdle()
        val result = vm.benList.first()
        assertEquals(1, result.size)
    }
}
