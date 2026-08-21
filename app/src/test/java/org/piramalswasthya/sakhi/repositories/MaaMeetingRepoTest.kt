package org.piramalswasthya.sakhi.repositories

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import io.mockk.mockkObject
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.MaaMeetingDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.MaaMeetingEntity
import org.piramalswasthya.sakhi.model.MaaMeetingGetAllResponse
import org.piramalswasthya.sakhi.model.MaaMeetingServerItem
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.utils.HelperUtil
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class MaaMeetingRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var appContext: Context
    @MockK private lateinit var dao: MaaMeetingDao
    @MockK private lateinit var api: AmritApiService
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var moshi: Moshi

    private lateinit var repo: MaaMeetingRepo

    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)

    @Before
    override fun setUp() {
        super.setUp()
        repo = MaaMeetingRepo(appContext, dao, api, pref, moshi)
    }

    private fun loggedInUser(userId: Int = 42) {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns userId
        every { user.userName } returns "asha_user"
        every { pref.getLoggedInUser() } returns user
    }

    // ---------------- buildEntity ----------------

    @Test
    fun `buildEntity maps fields and drops null images`() {
        loggedInUser(userId = 7)

        val entity = repo.buildEntity(
            date = "01-01-2026",
            place = "Community Hall",
            participants = 12,
            villageName = "Village A",
            mitaninActivityCheckList = "check",
            noOfPragnentWoment = "3",
            noOfLactingMother = "2",
            u1 = "uri1", u2 = null, u3 = "uri3", u4 = null, u5 = null
        )

        assertEquals("01-01-2026", entity.meetingDate)
        assertEquals("Community Hall", entity.place)
        assertEquals(12, entity.participants)
        assertEquals("Village A", entity.villageName)
        assertEquals(7, entity.ashaId)
        assertEquals(listOf("uri1", "uri3"), entity.meetingImages)
        assertEquals(SyncState.UNSYNCED, entity.syncState)
    }

    @Test
    fun `buildEntity uses null ashaId when no user logged in`() {
        every { pref.getLoggedInUser() } returns null

        val entity = repo.buildEntity(
            date = null, place = null, participants = null,
            u1 = null, u2 = null, u3 = null, u4 = null, u5 = null
        )

        assertNull(entity.ashaId)
        assertTrue(entity.meetingImages!!.isEmpty())
    }

    // ---------------- save ----------------

    @Test
    fun `save inserts entity and returns generated id`() = runTest {
        val entity = mockk<MaaMeetingEntity>(relaxed = true)
        every { dao.insert(entity) } returns 55L

        val id = repo.save(entity)

        assertEquals(55L, id)
        verify { dao.insert(entity) }
    }

    // ---------------- isThreeMonthsPassedSinceLastMeeting ----------------

    @Test
    fun `isThreeMonthsPassed returns true for null or blank date`() = runTest {
        assertTrue(repo.isThreeMonthsPassedSinceLastMeeting(null))
        assertTrue(repo.isThreeMonthsPassedSinceLastMeeting("   "))
    }

    @Test
    fun `isThreeMonthsPassed returns true for unparseable date`() = runTest {
        assertTrue(repo.isThreeMonthsPassedSinceLastMeeting("not-a-date"))
    }

    @Test
    fun `isThreeMonthsPassed returns true when four months elapsed`() = runTest {
        val fourMonthsAgo = LocalDate.now().minusMonths(4).format(formatter)
        assertTrue(repo.isThreeMonthsPassedSinceLastMeeting(fourMonthsAgo))
    }

    @Test
    fun `isThreeMonthsPassed returns false when one month elapsed`() = runTest {
        val oneMonthAgo = LocalDate.now().minusMonths(1).format(formatter)
        assertFalse(repo.isThreeMonthsPassedSinceLastMeeting(oneMonthAgo))
    }

    @Test
    fun `isThreeMonthsPassed returns false for today`() = runTest {
        val today = LocalDate.now().format(formatter)
        assertFalse(repo.isThreeMonthsPassedSinceLastMeeting(today))
    }

    // ---------------- getters ----------------

    @Test
    fun `getAllMaaMeetings delegates to dao`() {
        val flow = flowOf(emptyList<MaaMeetingEntity>())
        every { dao.getAllMaaData() } returns flow

        assertEquals(flow, repo.getAllMaaMeetings())
    }

    @Test
    fun `getMaaMeetingById returns dao result`() = runTest {
        val entity = mockk<MaaMeetingEntity>(relaxed = true)
        coEvery { dao.getMaaMeetingById(9L) } returns entity

        assertEquals(entity, repo.getMaaMeetingById(9L))
    }

    // ---------------- tryUpsync early return ----------------

    @Test
    fun `tryUpsync does nothing when no unsynced rows`() = runTest {
        every { dao.getBySyncState(SyncState.UNSYNCED) } returns emptyList()

        repo.tryUpsync()

        coVerify(exactly = 0) { api.postMaaMeetingMultipart(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    // ---------------- tryUpsync push loop ----------------

    @Test
    fun `tryUpsync marks single unsynced record synced after successful upload`() = runTest {
        loggedInUser(userId = 5)
        val row = MaaMeetingEntity(
            id = 1L,
            meetingDate = "01-01-2026",
            place = "Community Hall",
            participants = 10,
            mitaninActivityCheckList = "check",
            villageName = "Village A",
            noOfPragnentWomen = "2",
            noOfLactingMother = "1",
            ashaId = 5,
            meetingImages = null,
            syncState = SyncState.UNSYNCED
        )
        every { dao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row)

        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        coEvery {
            api.postMaaMeetingMultipart(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns response

        repo.tryUpsync()

        coVerify(exactly = 1) {
            api.postMaaMeetingMultipart(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
        coVerify { dao.updateSyncState(1L, SyncState.SYNCED) }
    }

    @Test
    fun `tryUpsync leaves record unsynced when server response is unsuccessful`() = runTest {
        loggedInUser(userId = 6)
        val row = MaaMeetingEntity(
            id = 2L,
            meetingDate = "05-02-2026",
            place = "AWC",
            participants = 8,
            mitaninActivityCheckList = null,
            villageName = "Village B",
            noOfPragnentWomen = "1",
            noOfLactingMother = "0",
            ashaId = 6,
            meetingImages = emptyList(),
            syncState = SyncState.UNSYNCED
        )
        every { dao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row)

        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns false
        coEvery {
            api.postMaaMeetingMultipart(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns response

        repo.tryUpsync()

        coVerify(exactly = 0) { dao.updateSyncState(any(), SyncState.SYNCED) }
    }

    @Test
    fun `tryUpsync processes multiple unsynced records and syncs only the successful ones`() = runTest {
        loggedInUser(userId = 7)
        val row1 = MaaMeetingEntity(
            id = 10L,
            meetingDate = "01-03-2026",
            place = "Hall 1",
            participants = 4,
            mitaninActivityCheckList = null,
            villageName = "V10",
            noOfPragnentWomen = "1",
            noOfLactingMother = "1",
            ashaId = 7,
            meetingImages = null,
            syncState = SyncState.UNSYNCED
        )
        val row2 = MaaMeetingEntity(
            id = 20L,
            meetingDate = "02-03-2026",
            place = "Hall 2",
            participants = 6,
            mitaninActivityCheckList = null,
            villageName = "V20",
            noOfPragnentWomen = "2",
            noOfLactingMother = "2",
            ashaId = 7,
            meetingImages = null,
            syncState = SyncState.UNSYNCED
        )
        every { dao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row1, row2)

        val successResponse = mockk<Response<ResponseBody>>(relaxed = true)
        every { successResponse.isSuccessful } returns true
        val failureResponse = mockk<Response<ResponseBody>>(relaxed = true)
        every { failureResponse.isSuccessful } returns false

        coEvery {
            api.postMaaMeetingMultipart(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returnsMany listOf(successResponse, failureResponse)

        repo.tryUpsync()

        coVerify(exactly = 2) {
            api.postMaaMeetingMultipart(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
        coVerify(exactly = 1) { dao.updateSyncState(10L, SyncState.SYNCED) }
        coVerify(exactly = 0) { dao.updateSyncState(20L, SyncState.SYNCED) }
    }

    @Test
    fun `tryUpsync builds payload with defaults when optional fields and logged in user are null`() = runTest {
        every { pref.getLoggedInUser() } returns null
        val row = MaaMeetingEntity(
            id = 30L,
            meetingDate = null,
            place = null,
            participants = null,
            mitaninActivityCheckList = null,
            villageName = null,
            noOfPragnentWomen = null,
            noOfLactingMother = null,
            ashaId = null,
            meetingImages = null,
            syncState = SyncState.UNSYNCED
        )
        every { dao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row)

        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        coEvery {
            api.postMaaMeetingMultipart(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns response

        repo.tryUpsync()

        coVerify { dao.updateSyncState(30L, SyncState.SYNCED) }
    }

    @Test
    fun `tryUpsync propagates exception from api call and stops processing remaining records`() = runTest {
        loggedInUser(userId = 8)
        val row1 = MaaMeetingEntity(
            id = 40L,
            meetingDate = "01-04-2026",
            place = "Hall",
            participants = 3,
            mitaninActivityCheckList = null,
            villageName = "V40",
            noOfPragnentWomen = "1",
            noOfLactingMother = "1",
            ashaId = 8,
            meetingImages = null,
            syncState = SyncState.UNSYNCED
        )
        val row2 = MaaMeetingEntity(
            id = 50L,
            meetingDate = "02-04-2026",
            place = "Hall",
            participants = 3,
            mitaninActivityCheckList = null,
            villageName = "V50",
            noOfPragnentWomen = "1",
            noOfLactingMother = "1",
            ashaId = 8,
            meetingImages = null,
            syncState = SyncState.UNSYNCED
        )
        every { dao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row1, row2)

        coEvery {
            api.postMaaMeetingMultipart(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws RuntimeException("network error")

        var threw = false
        try {
            repo.tryUpsync()
        } catch (e: RuntimeException) {
            threw = true
        }

        assertTrue(threw)
        coVerify(exactly = 1) {
            api.postMaaMeetingMultipart(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
        coVerify(exactly = 0) { dao.updateSyncState(any(), SyncState.SYNCED) }
    }

    // =====================================================
    // downSyncAndPersist
    // =====================================================

    private fun successfulMeetingsResponse(body: String): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns body
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        every { response.body() } returns responseBody
        return response
    }

    private fun stubParsedMeetingsResponse(parsed: MaaMeetingGetAllResponse?) {
        val adapter = mockk<JsonAdapter<MaaMeetingGetAllResponse>>(relaxed = true)
        every { moshi.adapter(MaaMeetingGetAllResponse::class.java) } returns adapter
        every { adapter.fromJson(any<String>()) } returns parsed
    }

    @Test
    fun `downSyncAndPersist does nothing on unsuccessful http response`() = runTest {
        loggedInUser()
        every { pref.getLastSyncedTimeStamp() } returns 0L
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns false
        coEvery { api.getMaaMeetings(any()) } returns response

        repo.downSyncAndPersist()

        verify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `downSyncAndPersist does nothing when response body is null`() = runTest {
        loggedInUser()
        every { pref.getLastSyncedTimeStamp() } returns 0L
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        every { response.body() } returns null
        coEvery { api.getMaaMeetings(any()) } returns response

        repo.downSyncAndPersist()

        verify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `downSyncAndPersist does nothing when parsed body is null`() = runTest {
        loggedInUser()
        every { pref.getLastSyncedTimeStamp() } returns 0L
        coEvery { api.getMaaMeetings(any()) } returns successfulMeetingsResponse("not json")
        stubParsedMeetingsResponse(null)

        repo.downSyncAndPersist()

        verify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `downSyncAndPersist does nothing when server list is empty`() = runTest {
        loggedInUser()
        every { pref.getLastSyncedTimeStamp() } returns 0L
        val parsed = MaaMeetingGetAllResponse(data = emptyList(), statusCode = 200, status = "OK")
        assertEquals(200, parsed.statusCode)
        assertEquals("OK", parsed.status)
        coEvery { api.getMaaMeetings(any()) } returns successfulMeetingsResponse("{}")
        stubParsedMeetingsResponse(parsed)

        repo.downSyncAndPersist()

        verify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `downSyncAndPersist inserts parsed meeting entities without images`() = runTest {
        loggedInUser()
        every { pref.getLastSyncedTimeStamp() } returns 0L
        val item = MaaMeetingServerItem(
            id = 9,
            meetingDate = "15-01-2026",
            place = "Community Hall",
            mitaninActivityCheckList = "check",
            noOfLactingMother = "2",
            noOfPragnentWoment = "3",
            villageName = "Village A",
            participants = 10,
            ashaId = 42,
            meetingImages = null
        )
        coEvery { api.getMaaMeetings(any()) } returns successfulMeetingsResponse("{}")
        stubParsedMeetingsResponse(
            MaaMeetingGetAllResponse(data = listOf(item), statusCode = 200, status = "OK")
        )
        val slot = io.mockk.slot<MaaMeetingEntity>()
        every {
            dao.replaceLocalCopyWithServerMeeting(
                entity = capture(slot),
                serverId = any(),
                meetingDate = any(),
                place = any(),
                participants = any(),
                ashaId = any(),
                villageName = any()
            )
        } returns Unit

        repo.downSyncAndPersist()

        val persisted = slot.captured
        assertEquals(9L, persisted.id)
        assertEquals("2026-01-15", persisted.meetingDate)
        assertEquals("Community Hall", persisted.place)
        assertEquals("Village A", persisted.villageName)
        assertEquals(10, persisted.participants)
        assertEquals(42, persisted.ashaId)
        assertTrue(persisted.meetingImages.isNullOrEmpty())
        assertEquals(SyncState.SYNCED, persisted.syncState)
    }

    @Test
    fun `downSyncAndPersist swallows per-image decode failures and leaves images empty`() = runTest {
        loggedInUser()
        every { pref.getLastSyncedTimeStamp() } returns 0L
        val item = MaaMeetingServerItem(
            id = 15,
            meetingDate = "15-01-2026",
            place = "Community Hall",
            mitaninActivityCheckList = null,
            noOfLactingMother = null,
            noOfPragnentWoment = null,
            villageName = "Village A",
            participants = 10,
            ashaId = 42,
            meetingImages = listOf("data:image/png;base64,SGVsbG8=")
        )
        coEvery { api.getMaaMeetings(any()) } returns successfulMeetingsResponse("{}")
        stubParsedMeetingsResponse(
            MaaMeetingGetAllResponse(data = listOf(item), statusCode = 200, status = "OK")
        )
        val slot = io.mockk.slot<MaaMeetingEntity>()
        every {
            dao.replaceLocalCopyWithServerMeeting(
                entity = capture(slot),
                serverId = any(),
                meetingDate = any(),
                place = any(),
                participants = any(),
                ashaId = any(),
                villageName = any()
            )
        } returns Unit

        repo.downSyncAndPersist()

        assertTrue(slot.captured.meetingImages.isNullOrEmpty())
    }

    @Test
    fun `tryUpsync attaches multipart image parts when local record has images`() = runTest {
        loggedInUser(userId = 9)
        mockkObject(HelperUtil)
        mockkStatic(android.net.Uri::class)
        val parsedUri = mockk<android.net.Uri>(relaxed = true)
        every { android.net.Uri.parse("content://provider/img1") } returns parsedUri
        every { HelperUtil.getFileName(parsedUri, appContext) } returns "img1.jpg"
        every { appContext.contentResolver.getType(parsedUri) } returns "image/jpeg"
        val tempFile = java.io.File.createTempFile("maa_test_", ".jpg")
        tempFile.writeBytes(byteArrayOf(1, 2, 3))
        every { HelperUtil.compressImageToTemp(parsedUri, "img1.jpg", appContext) } returns tempFile

        val row = MaaMeetingEntity(
            id = 60L,
            meetingDate = "01-05-2026",
            place = "Hall",
            participants = 2,
            mitaninActivityCheckList = null,
            villageName = "V60",
            noOfPragnentWomen = "1",
            noOfLactingMother = "1",
            ashaId = 9,
            meetingImages = listOf("content://provider/img1"),
            syncState = SyncState.UNSYNCED
        )
        every { dao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row)

        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        coEvery {
            api.postMaaMeetingMultipart(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns response

        repo.tryUpsync()

        coVerify(exactly = 1) {
            api.postMaaMeetingMultipart(
                any(), any(), any(), any(), any(), any(), any(), any(), any(),
                match { it.isNotEmpty() }
            )
        }
        tempFile.delete()
    }

    @Test
    fun `downSyncAndPersist inserts multiple parsed meeting entities`() = runTest {
        loggedInUser()
        every { pref.getLastSyncedTimeStamp() } returns 0L
        val item1 = MaaMeetingServerItem(
            id = 10, meetingDate = null, place = "Hall A", mitaninActivityCheckList = null,
            noOfLactingMother = null, noOfPragnentWoment = null, villageName = "A",
            participants = 5, ashaId = 42, meetingImages = null
        )
        val item2 = MaaMeetingServerItem(
            id = 11, meetingDate = null, place = "Hall B", mitaninActivityCheckList = null,
            noOfLactingMother = null, noOfPragnentWoment = null, villageName = "B",
            participants = 6, ashaId = 42, meetingImages = null
        )
        coEvery { api.getMaaMeetings(any()) } returns successfulMeetingsResponse("{}")
        stubParsedMeetingsResponse(
            MaaMeetingGetAllResponse(data = listOf(item1, item2), statusCode = 200, status = "OK")
        )

        repo.downSyncAndPersist()

        verify(exactly = 2) {
            dao.replaceLocalCopyWithServerMeeting(
                entity = any(),
                serverId = any(),
                meetingDate = any(),
                place = any(),
                participants = any(),
                ashaId = any(),
                villageName = any()
            )
        }
    }
}
