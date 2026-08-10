package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.NotificationDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.NotificationEntity
import org.piramalswasthya.sakhi.model.NotificationListData
import org.piramalswasthya.sakhi.model.NotificationListItemDto
import org.piramalswasthya.sakhi.model.NotificationListResponse
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Unit tests for [NotificationRepository], focused on driving every real branch of
 * [NotificationRepository.pullAndSaveNotifications] (success / no-record / unknown-status /
 * non-200 http / null-body / 401-refresh-retry / SocketTimeoutException-retry / generic
 * exception paths), plus the remaining simple public methods.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationRepositoryTest : BaseRepositoryTest() {

    @MockK private lateinit var notificationDao: NotificationDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var userRepo: UserRepo

    private lateinit var repo: NotificationRepository

    @Before
    override fun setUp() {
        super.setUp()
        repo = NotificationRepository(notificationDao, preferenceDao, amritApiService, userRepo)
    }

    private fun loggedInUser(
        userId: Int = 42,
        userName: String = "asha",
        password: String = "pwd"
    ): User {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns userId
        every { user.userName } returns userName
        every { user.password } returns password
        return user
    }

    private fun successEnvelope(): NotificationListResponse = NotificationListResponse(
        data = NotificationListData(
            notifications = listOf(
                NotificationListItemDto(
                    id = 101L,
                    title = "Title",
                    body = "Body",
                    notificationType = "GENERIC"
                )
            )
        ),
        statusCode = 200,
        status = "SUCCESS"
    )

    private fun notificationsResponse(
        envelope: NotificationListResponse?,
        httpCode: Int = 200
    ): Response<NotificationListResponse> {
        val response = mockk<Response<NotificationListResponse>>(relaxed = true)
        every { response.code() } returns httpCode
        every { response.body() } returns envelope
        every { response.errorBody() } returns null
        return response
    }

    private fun notificationEntity(id: Long = 1L): NotificationEntity = NotificationEntity(
        notificationId = id,
        userId = 1L,
        eventType = "GENERIC",
        title = "t",
        body = "b",
        createdTs = 1000L
    )

    // ---------------- pullAndSaveNotifications ----------------

    @Test
    fun `pullAndSaveNotifications returns false when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        val result = repo.pullAndSaveNotifications()

        assertFalse(result)
        coVerify(exactly = 0) { amritApiService.getNotifications() }
    }

    @Test
    fun `pullAndSaveNotifications maps and upserts rows on statusCode 200`() = runTest {
        val user = loggedInUser(userId = 3)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { amritApiService.getNotifications() } returns notificationsResponse(successEnvelope())
        val rowsSlot = slot<List<NotificationEntity>>()
        coEvery { notificationDao.upsert(capture(rowsSlot)) } returns Unit

        val result = repo.pullAndSaveNotifications()

        assertTrue(result)
        assertEquals(1, rowsSlot.captured.size)
        assertEquals(101L, rowsSlot.captured[0].notificationId)
        assertEquals(3L, rowsSlot.captured[0].userId)
    }

    @Test
    fun `pullAndSaveNotifications returns true and skips upsert on statusCode 5000`() = runTest {
        val user = loggedInUser()
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { amritApiService.getNotifications() } returns
            notificationsResponse(NotificationListResponse(statusCode = 5000, status = "NO_RECORD"))

        val result = repo.pullAndSaveNotifications()

        assertTrue(result)
        coVerify(exactly = 0) { notificationDao.upsert(any<List<NotificationEntity>>()) }
    }

    @Test
    fun `pullAndSaveNotifications returns false on unknown statusCode`() = runTest {
        val user = loggedInUser()
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { amritApiService.getNotifications() } returns
            notificationsResponse(NotificationListResponse(statusCode = 9999, status = "WEIRD"))

        val result = repo.pullAndSaveNotifications()

        assertFalse(result)
    }

    @Test
    fun `pullAndSaveNotifications returns false when http status is not 200`() = runTest {
        val user = loggedInUser()
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { amritApiService.getNotifications() } returns
            notificationsResponse(successEnvelope(), httpCode = 404)

        val result = repo.pullAndSaveNotifications()

        assertFalse(result)
        coVerify(exactly = 0) { notificationDao.upsert(any<List<NotificationEntity>>()) }
    }

    @Test
    fun `pullAndSaveNotifications returns false when body is null`() = runTest {
        val user = loggedInUser()
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { amritApiService.getNotifications() } returns
            notificationsResponse(envelope = null, httpCode = 200)

        val result = repo.pullAndSaveNotifications()

        assertFalse(result)
    }

    @Test
    fun `pullAndSaveNotifications refreshes token and retries after 401`() = runTest {
        val user = loggedInUser()
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { amritApiService.getNotifications() } returnsMany listOf(
            notificationsResponse(NotificationListResponse(statusCode = 401, status = "UNAUTHORIZED")),
            notificationsResponse(successEnvelope())
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        val result = repo.pullAndSaveNotifications()

        assertTrue(result)
        coVerify(exactly = 2) { amritApiService.getNotifications() }
        coVerify(exactly = 1) { notificationDao.upsert(any<List<NotificationEntity>>()) }
    }

    @Test
    fun `pullAndSaveNotifications returns false when 401 refresh fails`() = runTest {
        val user = loggedInUser()
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { amritApiService.getNotifications() } returns
            notificationsResponse(NotificationListResponse(statusCode = 401, status = "UNAUTHORIZED"))
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        val result = repo.pullAndSaveNotifications()

        assertFalse(result)
        coVerify(exactly = 1) { amritApiService.getNotifications() }
    }

    @Test
    fun `pullAndSaveNotifications returns false when api throws generic exception`() = runTest {
        val user = loggedInUser()
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { amritApiService.getNotifications() } throws IOException("network down")

        val result = repo.pullAndSaveNotifications()

        assertFalse(result)
    }

    @Test
    fun `pullAndSaveNotifications retries on SocketTimeoutException and succeeds`() = runTest {
        val user = loggedInUser()
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { amritApiService.getNotifications() } throws SocketTimeoutException("timeout") andThen
            notificationsResponse(successEnvelope())

        val result = repo.pullAndSaveNotifications()

        assertTrue(result)
        coVerify(exactly = 2) { amritApiService.getNotifications() }
    }

    // ---------------- markRead ----------------

    @Test
    fun `markRead marks dao and calls api successfully`() = runTest {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns "{}"
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 200
        every { response.body() } returns responseBody
        every { response.errorBody() } returns null
        coEvery { notificationDao.markRead(listOf(55L)) } returns Unit
        coEvery { amritApiService.markNotificationRead(55L) } returns response

        repo.markRead(55L)

        coVerify { notificationDao.markRead(listOf(55L)) }
        coVerify { amritApiService.markNotificationRead(55L) }
    }

    @Test
    fun `markRead swallows exception from api call`() = runTest {
        coEvery { notificationDao.markRead(listOf(9L)) } returns Unit
        coEvery { amritApiService.markNotificationRead(9L) } throws IOException("boom")

        repo.markRead(9L)

        coVerify { notificationDao.markRead(listOf(9L)) }
    }

    // ---------------- markAllRead ----------------

    @Test
    fun `markAllRead delegates to dao when user logged in`() = runTest {
        val user = loggedInUser(userId = 7)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { notificationDao.markAllRead(7L) } returns Unit

        repo.markAllRead()

        coVerify { notificationDao.markAllRead(7L) }
    }

    @Test
    fun `markAllRead does nothing when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        repo.markAllRead()

        coVerify(exactly = 0) { notificationDao.markAllRead(any()) }
    }

    // ---------------- dismiss ----------------

    @Test
    fun `dismiss delegates to dao softClear`() = runTest {
        coEvery { notificationDao.softClear(listOf(3L)) } returns Unit

        repo.dismiss(3L)

        coVerify { notificationDao.softClear(listOf(3L)) }
    }

    // ---------------- clearAll ----------------

    @Test
    fun `clearAll delegates to dao softClearAll when logged in`() = runTest {
        val user = loggedInUser(userId = 11)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { notificationDao.softClearAll(11L) } returns Unit

        repo.clearAll()

        coVerify { notificationDao.softClearAll(11L) }
    }

    @Test
    fun `clearAll does nothing when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        repo.clearAll()

        coVerify(exactly = 0) { notificationDao.softClearAll(any()) }
    }

    // ---------------- markViewed ----------------

    @Test
    fun `markViewed delegates to dao`() = runTest {
        coEvery { notificationDao.markViewed(listOf(1L, 2L)) } returns Unit

        repo.markViewed(listOf(1L, 2L))

        coVerify { notificationDao.markViewed(listOf(1L, 2L)) }
    }

    // ---------------- upsert ----------------

    @Test
    fun `upsert single delegates to dao`() = runTest {
        val entity = notificationEntity()
        coEvery { notificationDao.upsert(entity) } returns Unit

        repo.upsert(entity)

        coVerify { notificationDao.upsert(entity) }
    }

    @Test
    fun `upsert list delegates to dao`() = runTest {
        val entities = listOf(notificationEntity())
        coEvery { notificationDao.upsert(entities) } returns Unit

        repo.upsert(entities)

        coVerify { notificationDao.upsert(entities) }
    }

    // ---------------- notifications flow ----------------

    @Test
    fun `notifications emits empty list when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        val result = repo.notifications.first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `notifications emits mapped domain list when user logged in`() = runTest {
        val user = loggedInUser(userId = 5)
        every { preferenceDao.getLoggedInUser() } returns user
        val entity = notificationEntity(id = 9L)
        every { notificationDao.getForUser(5L) } returns flowOf(listOf(entity))

        val result = repo.notifications.first()

        assertEquals(1, result.size)
        assertEquals(9L, result[0].notificationId)
    }

    // ---------------- unreadCount flow ----------------

    @Test
    fun `unreadCount emits 0 when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        val result = repo.unreadCount.first()

        assertEquals(0, result)
    }

    @Test
    fun `unreadCount emits dao value when user logged in`() = runTest {
        val user = loggedInUser(userId = 5)
        every { preferenceDao.getLoggedInUser() } returns user
        every { notificationDao.unreadCount(5L) } returns flowOf(4)

        val result = repo.unreadCount.first()

        assertEquals(4, result)
    }
}
