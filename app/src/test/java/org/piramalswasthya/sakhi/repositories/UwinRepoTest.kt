package org.piramalswasthya.sakhi.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.room.dao.UwinDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.UwinCache
import org.piramalswasthya.sakhi.model.UwinNetwork
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class UwinRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var appContext: Context
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var syncDao: SyncDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var uwinDao: UwinDao
    @MockK private lateinit var moshi: Moshi

    private lateinit var repo: UwinRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = UwinRepo(appContext, amritApiService, preferenceDao, syncDao, userRepo, uwinDao, moshi)
    }

    private fun loggedIn() {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { preferenceDao.getLoggedInUser() } returns user
        every { preferenceDao.getLastSyncedTimeStamp() } returns 0L
    }

    @Test
    fun `getAllLocalRecords delegates to dao`() {
        val live = mockk<LiveData<List<UwinCache>>>()
        every { uwinDao.getAllUwinRecords() } returns live

        assertEquals(live, repo.getAllLocalRecords())
    }

    @Test
    fun `insertLocalRecord delegates to dao`() = runTest {
        val record = mockk<UwinCache>(relaxed = true)

        repo.insertLocalRecord(record)

        coVerify { uwinDao.insert(record) }
    }

    @Test
    fun `updateLocalRecord delegates to dao`() = runTest {
        val record = mockk<UwinCache>(relaxed = true)

        repo.updateLocalRecord(record)

        coVerify { uwinDao.update(record) }
    }

    @Test
    fun `getUwinById returns dao result`() = runTest {
        val cache = mockk<UwinCache>(relaxed = true)
        coEvery { uwinDao.getUwinById(3) } returns cache

        assertEquals(cache, repo.getUwinById(3))
    }

    @Test
    fun `tryUpsync returns true when nothing to sync`() = runTest {
        coEvery { uwinDao.getUnsyncedSessions(any()) } returns emptyList()

        assertTrue(repo.tryUpsync())
    }

    @Test
    fun `postUwinSession returns false when max retries exceeded`() = runTest {
        val network = mockk<UwinNetwork>(relaxed = true)

        assertFalse(repo.postUwinSession(network, retryCount = 3))
    }

    @Test
    fun `postUwinSession returns false when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        val network = mockk<UwinNetwork>(relaxed = true)

        assertFalse(repo.postUwinSession(network))
    }

    @Test
    fun `downSyncAndPersist does nothing when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        repo.downSyncAndPersist()

        coVerify(exactly = 0) { uwinDao.replaceAll(any()) }
    }

    @Test
    fun `tryUpsync returns false when dao throws`() = runTest {
        coEvery { uwinDao.getUnsyncedSessions(any()) } throws RuntimeException("db down")

        assertFalse(repo.tryUpsync())
    }

    @Test
    fun `downSyncAndPersist returns without persisting when response unsuccessful`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns false
        coEvery { amritApiService.getAllUwinSessions(any()) } returns response

        repo.downSyncAndPersist()

        coVerify(exactly = 0) { uwinDao.replaceAll(any()) }
    }

    @Test
    fun `downSyncAndPersist returns without persisting when body is null`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        every { response.body() } returns null
        coEvery { amritApiService.getAllUwinSessions(any()) } returns response

        repo.downSyncAndPersist()

        coVerify(exactly = 0) { uwinDao.replaceAll(any()) }
    }

}
