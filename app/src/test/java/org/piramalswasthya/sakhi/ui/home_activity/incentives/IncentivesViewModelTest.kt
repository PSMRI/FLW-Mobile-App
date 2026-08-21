package org.piramalswasthya.sakhi.ui.home_activity.incentives

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.IncentiveActivityCache
import org.piramalswasthya.sakhi.model.IncentiveActivityDomain
import org.piramalswasthya.sakhi.model.IncentiveActivityWithRecords
import org.piramalswasthya.sakhi.model.IncentiveCache
import org.piramalswasthya.sakhi.model.IncentiveDomain
import org.piramalswasthya.sakhi.model.IncentiveGrouped
import org.piramalswasthya.sakhi.model.IncentiveRecordCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.UploadResponse
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.repositories.IncentiveRepo
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.ActionState
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class IncentivesViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var incentiveRepo: IncentiveRepo
    @MockK private lateinit var apiService: AmritApiService

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
        viewModel = IncentivesViewModel(pref, incentiveRepo, apiService)
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
        val vm1 = IncentivesViewModel(pref, incentiveRepo, apiService)
        val vm2 = IncentivesViewModel(pref, incentiveRepo, apiService)
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

    // =====================================================
    // fixtures for the data-driven tests below
    // =====================================================

    private fun loggedInUser(stateId: Int = 1, userId: Int = 7): User {
        val u = mockk<User>(relaxed = true)
        every { u.userId } returns userId
        every { u.state } returns LocationEntity(stateId, "state-$stateId")
        return u
    }

    private fun activityCache(
        id: Long = 1L,
        name: String = "Activity",
        group: String = "G1",
        groupName: String = "Group 1",
        fmrCodeOld: String? = null,
        rate: Int = 10
    ) = IncentiveActivityCache(
        id = id,
        name = name,
        description = "desc-$id",
        paymentParam = "param",
        isPaid = false,
        rate = rate,
        state = 1,
        district = 2,
        group = group,
        groupName = groupName,
        fmrCode = "FMR-$id",
        fmrCodeOld = fmrCodeOld
    )

    private fun recordCache(
        id: Long = 1L,
        activityId: Long = 1L,
        benId: Long = 5L,
        amount: Long = 100L,
        createdDate: Long = 150L,
        isEligible: Boolean = true
    ) = IncentiveRecordCache(
        id = id,
        activityId = activityId,
        ashaId = 7,
        benId = benId,
        amount = amount,
        name = "record-$id",
        startDate = 0L,
        endDate = 0L,
        createdDate = createdDate,
        createdBy = "asha",
        updatedDate = 0L,
        updatedBy = "asha",
        isEligible = isEligible,
        verifiedByUserName = "",
        reason = "",
        otherReason = "",
        approvalStatus = 0,
        verifiedByUserId = 0,
        isClaimed = false,
        approvalDate = "",
        calimedDate = "",
        supervisorRole = ""
    )

    private fun buildVm() = IncentivesViewModel(pref, incentiveRepo, apiService)

    private val jsonMediaType = "application/json".toMediaTypeOrNull()
    private fun jsonBody(json: String) = json.toResponseBody(jsonMediaType)

    // =====================================================
    // init / pullIncentives
    // =====================================================

    @Test
    fun `init pulls activities and records for a logged in user`() = runTest {
        every { pref.getLoggedInUser() } returns loggedInUser()
        val vm = buildVm()
        advanceUntilIdle()

        assertNotNull(vm.currentUser)
        coVerify { incentiveRepo.pullAndSaveAllIncentiveActivities(any()) }
        coVerify { incentiveRepo.pullAndSaveAllIncentiveRecords(any()) }
    }

    @Test
    fun `init flags chhattisgarh when the user state id is eight`() = runTest {
        every { pref.getLoggedInUser() } returns loggedInUser(stateId = 8)
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.isStateChhattisgarh.value)
    }

    @Test
    fun `init does not flag chhattisgarh for another state`() = runTest {
        every { pref.getLoggedInUser() } returns loggedInUser(stateId = 3)
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(false, vm.isStateChhattisgarh.value)
    }

    // =====================================================
    // getMonthNumber()
    // =====================================================

    @Test
    fun `getMonthNumber maps every month name`() {
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        months.forEachIndexed { index, month ->
            assertEquals(month, index + 1, viewModel.getMonthNumber(month))
        }
    }

    @Test
    fun `getMonthNumber returns zero for an unknown month`() {
        assertEquals(0, viewModel.getMonthNumber("Smarch"))
        assertEquals(0, viewModel.getMonthNumber(""))
    }

    // =====================================================
    // incentiveList / groupedIncentiveList / getRecordsForActivity
    // =====================================================

    @Test
    fun `incentiveList keeps only records created inside the selected range`() = runTest {
        val activity = activityCache()
        every { incentiveRepo.list } returns flowOf(
            listOf(
                IncentiveCache(recordCache(id = 1L, createdDate = 150L), activity, null),
                IncentiveCache(recordCache(id = 2L, createdDate = 900L), activity, null)
            )
        )
        val vm = buildVm()
        vm.setRange(100L, 200L)
        advanceUntilIdle()

        val filtered = vm.incentiveList.first()
        assertEquals(1, filtered.size)
        assertEquals(1L, filtered.first().record.id)
    }

    @Test
    fun `groupedIncentiveList groups the filtered records by activity`() = runTest {
        val activity = activityCache(id = 4L, name = "Home visit")
        every { incentiveRepo.list } returns flowOf(
            listOf(
                IncentiveCache(recordCache(id = 1L, activityId = 4L, amount = 40L), activity, null),
                IncentiveCache(recordCache(id = 2L, activityId = 4L, amount = 60L), activity, null)
            )
        )
        val vm = buildVm()
        vm.setRange(100L, 200L)
        advanceUntilIdle()

        val grouped = vm.groupedIncentiveList.first()
        assertEquals(1, grouped.size)
        assertEquals("Home visit", grouped.first().activityName)
        assertEquals(100L, grouped.first().totalAmount)
        assertEquals(2, grouped.first().count)
    }

    @Test
    fun `getRecordsForActivity filters by activity and orders by created date`() = runTest {
        val activityOne = activityCache(id = 1L)
        val activityTwo = activityCache(id = 2L)
        every { incentiveRepo.list } returns flowOf(
            listOf(
                IncentiveCache(recordCache(id = 1L, activityId = 1L, createdDate = 180L), activityOne, null),
                IncentiveCache(recordCache(id = 2L, activityId = 1L, createdDate = 120L), activityOne, null),
                IncentiveCache(recordCache(id = 3L, activityId = 2L, createdDate = 150L), activityTwo, null),
                IncentiveCache(recordCache(id = 4L, activityId = 1L, createdDate = 900L), activityOne, null)
            )
        )
        val vm = buildVm()
        vm.setRange(100L, 200L)
        advanceUntilIdle()

        val records = vm.getRecordsForActivity(1L).first()
        assertEquals(2, records.size)
        assertEquals(2L, records[0].record.id)
        assertEquals(1L, records[1].record.id)
    }

    @Test
    fun `items exposes the activity list as domain models`() = runTest {
        val activity = activityCache(id = 6L, name = "ANC visit")
        every { incentiveRepo.activity_list } returns flowOf(
            listOf(IncentiveActivityWithRecords(activity, listOf(recordCache(activityId = 6L))))
        )
        val vm = buildVm()
        advanceUntilIdle()

        val items = vm.items.first()
        assertEquals(1, items.size)
        assertEquals("ANC visit", items.first().activity.name)
        assertEquals(1, items.first().records.size)
    }

    @Test
    fun `setRange publishes the new bounds on the from and to flows`() = runTest {
        val vm = buildVm()
        vm.setRange(1_000L, 2_000L)
        advanceUntilIdle()

        assertEquals(1_000L, vm.from.first())
        assertEquals(2_000L, vm.to.first())
    }

    // =====================================================
    // groupIncentivesByActivity() ordering
    // =====================================================

    @Test
    fun `groupIncentivesByActivity puts monthly defaults first then zero beneficiary groups`() {
        val plain = activityCache(id = 1L, name = "Zebra")
        val zeroBen = activityCache(id = 2L, name = "Alpha")
        val monthly = activityCache(id = 3L, name = "Monthly", fmrCodeOld = "PER_MONTH")

        val list = listOf(
            IncentiveDomain(recordCache(id = 1L, activityId = 1L, benId = 9L), plain, null),
            IncentiveDomain(recordCache(id = 2L, activityId = 2L, benId = 0L), zeroBen, null),
            IncentiveDomain(recordCache(id = 3L, activityId = 3L, benId = 4L), monthly, null)
        )

        val grouped = viewModel.groupIncentivesByActivity(list)

        assertEquals(3, grouped.size)
        assertEquals("Monthly", grouped[0].activityName)
        assertTrue(grouped[0].defaultIncentive)
        assertEquals("Alpha", grouped[1].activityName)
        assertTrue(grouped[1].hasZeroBen)
        assertEquals("Zebra", grouped[2].activityName)
    }

    @Test
    fun `groupIncentivesByActivity marks a group ineligible when any record is ineligible`() {
        val activity = activityCache(id = 1L)
        val list = listOf(
            IncentiveDomain(recordCache(id = 1L, isEligible = true), activity, null),
            IncentiveDomain(recordCache(id = 2L, isEligible = false), activity, null)
        )

        val grouped = viewModel.groupIncentivesByActivity(list)

        assertEquals(1, grouped.size)
        assertEquals(false, grouped[0].isEligible)
        assertEquals("Group 1", grouped[0].groupName)
    }

    // =====================================================
    // mapToDomainDTO()
    // =====================================================

    @Test
    fun `mapToDomainDTO counts only the records inside the range and keeps group order`() = runTest {
        val vm = buildVm()
        vm.setRange(100L, 200L)
        advanceUntilIdle()

        val first = IncentiveActivityDomain(
            activityCache(id = 1L, name = "First", group = "B", rate = 25),
            listOf(
                recordCache(id = 1L, amount = 30L, createdDate = 120L),
                recordCache(id = 2L, amount = 20L, createdDate = 190L),
                recordCache(id = 3L, amount = 90L, createdDate = 900L)
            )
        )
        val second = IncentiveActivityDomain(
            activityCache(id = 2L, name = "Second", group = "A"),
            emptyList()
        )
        val third = IncentiveActivityDomain(
            activityCache(id = 3L, name = "Third", group = "B"),
            emptyList()
        )

        val dtos = vm.mapToDomainDTO(listOf(first, second, third))

        assertEquals(3, dtos.size)
        assertEquals("B", dtos[0].group)
        assertEquals(2, dtos[0].noOfClaims)
        assertEquals(50L, dtos[0].amountClaimed)
        assertEquals(25L, dtos[0].rate)
        assertEquals("Third", dtos[1].name)
        assertEquals("Second", dtos[2].name)
    }

    // =====================================================
    // uploadIncentiveDocuments()
    // =====================================================

    private fun uploadItem() = IncentiveDomain(
        record = recordCache(id = 12L),
        activity = activityCache(id = 1L),
        ben = null,
        uploadedFiles = listOf("file://one", "file://two")
    )

    @Test
    fun `uploadIncentiveDocuments reports an error when nobody is logged in`() = runTest {
        every { pref.getLoggedInUser() } returns null
        val vm = buildVm()
        advanceUntilIdle()

        vm.uploadIncentiveDocuments(uploadItem())
        advanceUntilIdle()

        val state = vm.uploadState.value
        assertTrue(state is IncentivesViewModel.UploadState.Error)
        assertEquals(
            "User not logged in",
            (state as IncentivesViewModel.UploadState.Error).message
        )
    }

    @Test
    fun `uploadIncentiveDocuments marks the item submitted on success`() = runTest {
        every { pref.getLoggedInUser() } returns loggedInUser()
        val response = UploadResponse(data = null, statusCode = 200, errorMessage = "", status = "ok")
        coEvery {
            incentiveRepo.uploadIncentiveFiles(any(), any(), any(), any(), any())
        } returns Result.success(response)

        val vm = buildVm()
        advanceUntilIdle()

        val item = uploadItem()
        vm.uploadIncentiveDocuments(item)
        advanceUntilIdle()

        assertEquals(IncentivesViewModel.UploadState.Success(response), vm.uploadState.value)
        assertTrue(item.isSubmitted)
        assertTrue(item.submittedAt > 0L)
        coVerify { incentiveRepo.uploadIncentiveFiles(12L, 7L, "G1", "Activity", item.uploadedFiles) }
    }

    @Test
    fun `uploadIncentiveDocuments survives a failing refresh after a successful upload`() = runTest {
        every { pref.getLoggedInUser() } returns loggedInUser()
        val response = UploadResponse(data = null, statusCode = 200, errorMessage = "", status = "ok")
        coEvery {
            incentiveRepo.uploadIncentiveFiles(any(), any(), any(), any(), any())
        } returns Result.success(response)

        val vm = buildVm()
        advanceUntilIdle()

        coEvery { incentiveRepo.pullAndSaveAllIncentiveRecords(any()) } throws RuntimeException("boom")
        vm.uploadIncentiveDocuments(uploadItem())
        advanceUntilIdle()

        assertEquals(IncentivesViewModel.UploadState.Success(response), vm.uploadState.value)
    }

    @Test
    fun `uploadIncentiveDocuments surfaces the failure message`() = runTest {
        every { pref.getLoggedInUser() } returns loggedInUser()
        coEvery {
            incentiveRepo.uploadIncentiveFiles(any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("network down"))

        val vm = buildVm()
        advanceUntilIdle()

        vm.uploadIncentiveDocuments(uploadItem())
        advanceUntilIdle()

        assertEquals(
            IncentivesViewModel.UploadState.Error("network down"),
            vm.uploadState.value
        )
    }

    @Test
    fun `uploadIncentiveDocuments falls back to a generic message`() = runTest {
        every { pref.getLoggedInUser() } returns loggedInUser()
        coEvery {
            incentiveRepo.uploadIncentiveFiles(any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException())

        val vm = buildVm()
        advanceUntilIdle()

        vm.uploadIncentiveDocuments(uploadItem())
        advanceUntilIdle()

        assertEquals(
            IncentivesViewModel.UploadState.Error("Upload failed"),
            vm.uploadState.value
        )
    }

    @Test
    fun `resetUploadState clears a previous error`() = runTest {
        every { pref.getLoggedInUser() } returns null
        val vm = buildVm()
        advanceUntilIdle()

        vm.uploadIncentiveDocuments(uploadItem())
        advanceUntilIdle()
        vm.resetUploadState()

        assertEquals(IncentivesViewModel.UploadState.Idle, vm.uploadState.value)
    }

    // =====================================================
    // claimIncentive()
    // =====================================================

    @Test
    fun `claimIncentive reports success for a two hundred status code`() = runTest {
        every { pref.getLoggedInUser() } returns loggedInUser()
        coEvery { apiService.claimAshaIncentive(any()) } returns Response.success(
            jsonBody("""{"statusCode":200,"updatedRecords":3}""")
        )

        val vm = buildVm()
        advanceUntilIdle()

        vm.claimIncentive("March", "2026")
        advanceUntilIdle()

        val state = vm.actionState.value
        assertTrue(state is ActionState.Success)
        assertEquals("Successfully claimed", (state as ActionState.Success).message)
        coVerify { apiService.claimAshaIncentive(mapOf("month" to 3, "year" to 2026, "claimed" to true)) }
    }

    @Test
    fun `claimIncentive surfaces the server error message for a non success status code`() = runTest {
        coEvery { apiService.claimAshaIncentive(any()) } returns Response.success(
            jsonBody("""{"statusCode":400,"errorMessage":"already claimed"}""")
        )

        val vm = buildVm()
        advanceUntilIdle()

        vm.claimIncentive("December", "2025")
        advanceUntilIdle()

        val state = vm.actionState.value
        assertTrue(state is ActionState.Error)
        assertEquals("already claimed", (state as ActionState.Error).message)
    }

    @Test
    fun `claimIncentive falls back to a default message when the payload is empty`() = runTest {
        coEvery { apiService.claimAshaIncentive(any()) } returns Response.success(jsonBody("{}"))

        val vm = buildVm()
        advanceUntilIdle()

        vm.claimIncentive("June", "2025")
        advanceUntilIdle()

        val state = vm.actionState.value
        assertTrue(state is ActionState.Error)
        assertEquals("Claim failed", (state as ActionState.Error).message)
    }

    @Test
    fun `claimIncentive reports the http code for an unsuccessful response`() = runTest {
        coEvery { apiService.claimAshaIncentive(any()) } returns Response.error(503, jsonBody(""))

        val vm = buildVm()
        advanceUntilIdle()

        vm.claimIncentive("July", "2025")
        advanceUntilIdle()

        val state = vm.actionState.value
        assertTrue(state is ActionState.Error)
        assertEquals("Server error: 503", (state as ActionState.Error).message)
    }

    @Test
    fun `claimIncentive reports the exception message when the call throws`() = runTest {
        coEvery { apiService.claimAshaIncentive(any()) } throws RuntimeException("no route to host")

        val vm = buildVm()
        advanceUntilIdle()

        vm.claimIncentive("August", "2025")
        advanceUntilIdle()

        val state = vm.actionState.value
        assertTrue(state is ActionState.Error)
        assertEquals("no route to host", (state as ActionState.Error).message)
    }

    @Test
    fun `claimIncentive reports an unknown error when the exception has no message`() = runTest {
        coEvery { apiService.claimAshaIncentive(any()) } throws RuntimeException()

        val vm = buildVm()
        advanceUntilIdle()

        vm.claimIncentive("May", "2025")
        advanceUntilIdle()

        val state = vm.actionState.value
        assertTrue(state is ActionState.Error)
        assertEquals("Unknown error", (state as ActionState.Error).message)
    }
}
