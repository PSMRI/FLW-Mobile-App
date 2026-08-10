package org.piramalswasthya.sakhi.repositories

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.SaasBahuSammelanDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.SaasBahuSammelanCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class SaasBahuSammelanRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var appContext: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var api: AmritApiService
    @MockK private lateinit var saasBahuDao: SaasBahuSammelanDao
    @MockK private lateinit var moshi: Moshi

    private lateinit var repo: SaasBahuSammelanRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.d(any(), any()) } returns 0
        repo = SaasBahuSammelanRepo(userRepo, appContext, preferenceDao, api, saasBahuDao, moshi)
    }

    private fun loggedIn(): User {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 1
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { preferenceDao.getLoggedInUser() } returns user
        return user
    }

    @Test
    fun `saveSammelanForm inserts into dao`() = runTest {
        val cache = mockk<SaasBahuSammelanCache>(relaxed = true)

        repo.saveSammelanForm(cache)

        coVerify { saasBahuDao.insertSammelan(cache) }
    }

    @Test(expected = IllegalStateException::class)
    fun `pushUnSyncedRecords throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        repo.pushUnSyncedRecordsSaasBahuSammelan()
    }

    @Test
    fun `pushUnSyncedRecords returns true when nothing unsynced`() = runTest {
        loggedIn()
        every { saasBahuDao.getBySyncState(SyncState.UNSYNCED) } returns emptyList()

        assertTrue(repo.pushUnSyncedRecordsSaasBahuSammelan())
    }

    @Test
    fun `SaasBahuSamelanGettDataFromServer stops on unsuccessful response`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns false
        coEvery { api.getSaasBahuSammelans(any()) } returns response

        repo.SaasBahuSamelanGettDataFromServer()

        coVerify(exactly = 0) { saasBahuDao.clearAll() }
    }

    @Test
    fun `pushUnSyncedRecords returns true when upload gets non-200 response`() = runTest {
        loggedIn()
        val row = mockk<SaasBahuSammelanCache>(relaxed = true)
        every { row.sammelanImages } returns null
        every { saasBahuDao.getBySyncState(SyncState.UNSYNCED) } returns listOf(row)

        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 500
        coEvery {
            api.postSaasBahuSammelanMultipart(any(), any(), any(), any(), any())
        } returns response

        val result = repo.pushUnSyncedRecordsSaasBahuSammelan()

        assertTrue(result)
        coVerify { api.postSaasBahuSammelanMultipart(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `SaasBahuSamelanGettDataFromServer stops when body is null`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        every { response.body() } returns null
        coEvery { api.getSaasBahuSammelans(any()) } returns response

        repo.SaasBahuSamelanGettDataFromServer()

        coVerify(exactly = 0) { saasBahuDao.clearAll() }
    }

}
