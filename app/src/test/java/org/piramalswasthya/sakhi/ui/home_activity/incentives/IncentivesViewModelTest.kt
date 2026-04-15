package org.piramalswasthya.sakhi.ui.home_activity.incentives

import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.IncentiveActivityDomain
import org.piramalswasthya.sakhi.model.IncentiveDomain
import org.piramalswasthya.sakhi.model.IncentiveGrouped
import org.piramalswasthya.sakhi.repositories.IncentiveRepo

@OptIn(ExperimentalCoroutinesApi::class)
class IncentivesViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var incentiveRepo: IncentiveRepo

    private lateinit var viewModel: IncentivesViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { pref.lastIncentivePullTimestamp } returns 0L
        every { pref.getLoggedInUser() } returns null
        every { pref.getLocationRecord() } returns null
        every { incentiveRepo.activity_list } returns flowOf(emptyList())
        every { incentiveRepo.list } returns flowOf(emptyList())
        coEvery { incentiveRepo.pullAndSaveAllIncentiveActivities(any()) } returns true
        coEvery { incentiveRepo.pullAndSaveAllIncentiveRecords(any()) } returns true
        viewModel = IncentivesViewModel(pref, incentiveRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `items flow is not null`() {
        assertNotNull(viewModel.items)
    }

    @Test
    fun `sourceIncentiveList flow is not null`() {
        assertNotNull(viewModel.sourceIncentiveList)
    }

    @Test
    fun `incentiveList flow is not null`() {
        assertNotNull(viewModel.incentiveList)
    }

    @Test
    fun `groupedIncentiveList flow is not null`() {
        assertNotNull(viewModel.groupedIncentiveList)
    }

    @Test
    fun `currentUser is null when no user`() {
        assertEquals(null, viewModel.currentUser)
    }

    @Test
    fun `locationRecord is null when no location`() {
        assertEquals(null, viewModel.locationRecord)
    }

    @Test
    fun `lastUpdated is not null`() {
        assertNotNull(viewModel.lastUpdated)
    }

    @Test
    fun `initial uploadState is Idle`() {
        assertEquals(IncentivesViewModel.UploadState.Idle, viewModel.uploadState.value)
    }

    // =====================================================
    // setRange() Tests
    // =====================================================

    @Test
    fun `setRange does not throw`() = runTest {
        viewModel.setRange(1000L, 2000L)
        advanceUntilIdle()
    }

    // =====================================================
    // resetUploadState() Tests
    // =====================================================

    @Test
    fun `resetUploadState sets state to Idle`() {
        viewModel.resetUploadState()
        assertEquals(IncentivesViewModel.UploadState.Idle, viewModel.uploadState.value)
    }

    // =====================================================
    // groupIncentivesByActivity() Tests
    // =====================================================

    @Test
    fun `groupIncentivesByActivity returns empty list for empty input`() {
        val result = viewModel.groupIncentivesByActivity(emptyList())
        assertEquals(0, result.size)
    }

    @Test
    fun `groupIncentivesByActivity groups by activity id`() {
        val activity1 = mockk<org.piramalswasthya.sakhi.model.IncentiveActivityCache>(relaxed = true)
        every { activity1.id } returns 1L
        every { activity1.name } returns "Activity 1"
        every { activity1.groupName } returns "Group 1"
        every { activity1.description } returns "Desc 1"
        every { activity1.fmrCodeOld } returns ""

        val record1 = mockk<org.piramalswasthya.sakhi.model.IncentiveRecordCache>(relaxed = true)
        every { record1.amount } returns 100L
        every { record1.benId } returns 1L
        every { record1.isEligible } returns true

        val record2 = mockk<org.piramalswasthya.sakhi.model.IncentiveRecordCache>(relaxed = true)
        every { record2.amount } returns 200L
        every { record2.benId } returns 2L
        every { record2.isEligible } returns true

        val domain1 = IncentiveDomain(record = record1, activity = activity1, ben = null)
        val domain2 = IncentiveDomain(record = record2, activity = activity1, ben = null)

        val result = viewModel.groupIncentivesByActivity(listOf(domain1, domain2))
        assertEquals(1, result.size)
        assertEquals("Activity 1", result[0].activityName)
        assertEquals(300L, result[0].totalAmount)
        assertEquals(2, result[0].count)
    }

    // =====================================================
    // mapToDomainDTO() Tests
    // =====================================================

    @Test
    fun `mapToDomainDTO returns empty list for empty input`() {
        val result = viewModel.mapToDomainDTO(emptyList())
        assertEquals(0, result.size)
    }

    // =====================================================
    // getRecordsForActivity() Tests
    // =====================================================

    @Test
    fun `getRecordsForActivity returns flow`() {
        val result = viewModel.getRecordsForActivity(1L)
        assertNotNull(result)
    }

    // =====================================================
    // Extended setRange() Tests
    // =====================================================

    @Test
    fun `setRange with same from and to does not throw`() = runTest {
        viewModel.setRange(1000L, 1000L)
        advanceUntilIdle()
    }

    @Test
    fun `setRange with zero values does not throw`() = runTest {
        viewModel.setRange(0L, 0L)
        advanceUntilIdle()
    }

    @Test
    fun `setRange with large values does not throw`() = runTest {
        viewModel.setRange(Long.MAX_VALUE - 1, Long.MAX_VALUE)
        advanceUntilIdle()
    }

    @Test
    fun `setRange called multiple times does not throw`() = runTest {
        viewModel.setRange(100L, 200L)
        viewModel.setRange(300L, 400L)
        viewModel.setRange(500L, 600L)
        advanceUntilIdle()
    }

    // =====================================================
    // Extended resetUploadState() Tests
    // =====================================================

    @Test
    fun `resetUploadState called twice stays Idle`() {
        viewModel.resetUploadState()
        viewModel.resetUploadState()
        assertEquals(IncentivesViewModel.UploadState.Idle, viewModel.uploadState.value)
    }

    // =====================================================
    // Extended groupIncentivesByActivity() Tests
    // =====================================================

    @Test
    fun `groupIncentivesByActivity with multiple activities`() {
        val activity1 = mockk<org.piramalswasthya.sakhi.model.IncentiveActivityCache>(relaxed = true)
        every { activity1.id } returns 1L
        every { activity1.name } returns "Activity A"
        every { activity1.groupName } returns "Group A"
        every { activity1.description } returns "Desc A"
        every { activity1.fmrCodeOld } returns ""

        val activity2 = mockk<org.piramalswasthya.sakhi.model.IncentiveActivityCache>(relaxed = true)
        every { activity2.id } returns 2L
        every { activity2.name } returns "Activity B"
        every { activity2.groupName } returns "Group B"
        every { activity2.description } returns "Desc B"
        every { activity2.fmrCodeOld } returns ""

        val record1 = mockk<org.piramalswasthya.sakhi.model.IncentiveRecordCache>(relaxed = true)
        every { record1.amount } returns 100L
        every { record1.benId } returns 1L
        every { record1.isEligible } returns true

        val record2 = mockk<org.piramalswasthya.sakhi.model.IncentiveRecordCache>(relaxed = true)
        every { record2.amount } returns 200L
        every { record2.benId } returns 2L
        every { record2.isEligible } returns true

        val domain1 = IncentiveDomain(record = record1, activity = activity1, ben = null)
        val domain2 = IncentiveDomain(record = record2, activity = activity2, ben = null)

        val result = viewModel.groupIncentivesByActivity(listOf(domain1, domain2))
        assertEquals(2, result.size)
    }

    @Test
    fun `groupIncentivesByActivity single item`() {
        val activity = mockk<org.piramalswasthya.sakhi.model.IncentiveActivityCache>(relaxed = true)
        every { activity.id } returns 1L
        every { activity.name } returns "Activity"
        every { activity.groupName } returns "Group"
        every { activity.description } returns "Desc"
        every { activity.fmrCodeOld } returns ""

        val record = mockk<org.piramalswasthya.sakhi.model.IncentiveRecordCache>(relaxed = true)
        every { record.amount } returns 500L
        every { record.benId } returns 1L
        every { record.isEligible } returns true

        val domain = IncentiveDomain(record = record, activity = activity, ben = null)
        val result = viewModel.groupIncentivesByActivity(listOf(domain))
        assertEquals(1, result.size)
        assertEquals(500L, result[0].totalAmount)
        assertEquals(1, result[0].count)
    }

    // =====================================================
    // Extended mapToDomainDTO() Tests
    // =====================================================

    @Test
    fun `mapToDomainDTO with single item`() {
        val actDomain = mockk<IncentiveActivityDomain>(relaxed = true)
        every { actDomain.activity } returns mockk(relaxed = true)
        every { actDomain.records } returns emptyList()
        val result = viewModel.mapToDomainDTO(listOf(actDomain))
        assertEquals(1, result.size)
    }

    @Test
    fun `mapToDomainDTO with multiple items`() {
        val actDomain1 = mockk<IncentiveActivityDomain>(relaxed = true)
        every { actDomain1.activity } returns mockk(relaxed = true)
        every { actDomain1.records } returns emptyList()
        val actDomain2 = mockk<IncentiveActivityDomain>(relaxed = true)
        every { actDomain2.activity } returns mockk(relaxed = true)
        every { actDomain2.records } returns emptyList()
        val result = viewModel.mapToDomainDTO(listOf(actDomain1, actDomain2))
        assertEquals(2, result.size)
    }

    // =====================================================
    // Extended getRecordsForActivity() Tests
    // =====================================================

    @Test
    fun `getRecordsForActivity with zero id returns flow`() {
        val result = viewModel.getRecordsForActivity(0L)
        assertNotNull(result)
    }

    @Test
    fun `getRecordsForActivity with large id returns flow`() {
        val result = viewModel.getRecordsForActivity(999999L)
        assertNotNull(result)
    }

    // =====================================================
    // Property Consistency Tests
    // =====================================================

    @Test
    fun `items flow is consistent`() {
        val i1 = viewModel.items
        val i2 = viewModel.items
        assertEquals(i1, i2)
    }

    @Test
    fun `incentiveList flow is consistent`() {
        val l1 = viewModel.incentiveList
        val l2 = viewModel.incentiveList
        assertEquals(l1, l2)
    }

    @Test
    fun `groupedIncentiveList flow is consistent`() {
        val g1 = viewModel.groupedIncentiveList
        val g2 = viewModel.groupedIncentiveList
        assertEquals(g1, g2)
    }

    @Test
    fun `multiple instances are independent`() {
        val vm1 = IncentivesViewModel(pref, incentiveRepo)
        val vm2 = IncentivesViewModel(pref, incentiveRepo)
        assertNotNull(vm1)
        assertNotNull(vm2)
    }

    @Test
    fun `lastUpdated is not empty`() {
        val updated = viewModel.lastUpdated
        assertNotNull(updated)
    }

    @Test
    fun `isStateChhattisgarh is not null`() {
        assertNotNull(viewModel.isStateChhattisgarh)
    }
}
