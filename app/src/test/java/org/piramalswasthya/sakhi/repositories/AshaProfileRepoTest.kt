package org.piramalswasthya.sakhi.repositories

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.ProfileDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ProfileActivityCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

/**
 * Unit tests for [AshaProfileRepo]. Consolidated from AshaProfileRepoTest +
 * ExtraTest: getSavedRecord / saveRecord delegations plus the
 * postDataToAmritServer and pullAndSaveAshaProfile response-handling / guard
 * branches.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AshaProfileRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var profileDao: ProfileDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    private val context: Context = mockk(relaxed = true)

    private lateinit var repo: AshaProfileRepo

    private val jsonMediaType = "application/json".toMediaTypeOrNull()
    private fun jsonBody(json: String) = json.toResponseBody(jsonMediaType)
    private fun emptyErrorBody() = "".toResponseBody(jsonMediaType)

    @Before
    override fun setUp() {
        super.setUp()
        repo = AshaProfileRepo(amritApiService, profileDao, preferenceDao, userRepo, context)
    }

    // =====================================================
    // getSavedRecord() Tests
    // =====================================================

    @Test
    fun `getSavedRecord returns record when exists`() = runTest {
        val profile = mockk<ProfileActivityCache>()
        coEvery { profileDao.getProfileActivityById(100L) } returns profile

        val result = repo.getSavedRecord(100L)

        assertNotNull(result)
        assertEquals(profile, result)
    }

    @Test
    fun `getSavedRecord returns null when not exists`() = runTest {
        coEvery { profileDao.getProfileActivityById(999L) } returns null

        val result = repo.getSavedRecord(999L)

        assertNull(result)
    }

    // =====================================================
    // saveRecord() Tests
    // =====================================================

    @Test
    fun `saveRecord calls dao insert`() = runTest {
        val profile = mockk<ProfileActivityCache>()
        coEvery { profileDao.insert(profile) } returns Unit

        repo.saveRecord(profile)

        coVerify(exactly = 1) { profileDao.insert(profile) }
    }

    // ---------------- postDataToAmritServer ----------------

    @Test
    fun `postDataToAmritServer returns false on non-200 http code`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        coEvery { amritApiService.submitAshaProfileData(cache) } returns
            Response.error(500, emptyErrorBody())

        assertFalse(repo.postDataToAmritServer(cache))
    }

    @Test
    fun `postDataToAmritServer returns false when api throws`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        coEvery { amritApiService.submitAshaProfileData(cache) } throws RuntimeException("network")

        assertFalse(repo.postDataToAmritServer(cache))
    }

    @Test
    fun `postDataToAmritServer returns true and skips save when employeeId missing`() = runTest {
        val cache = mockk<ProfileActivityCache>(relaxed = true)
        // statusCode 200 outer + inner, data parses to an all-null network model
        // whose employeeId resolves to 0, so saveProfileData returns early.
        val body = """{"statusCode":200,"data":"{}"}"""
        coEvery { amritApiService.submitAshaProfileData(cache) } returns Response.success(jsonBody(body))

        assertTrue(repo.postDataToAmritServer(cache))
    }

    // ---------------- pullAndSaveAshaProfile ----------------

    @Test
    fun `pullAndSaveAshaProfile returns true on statusCode 5000`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { amritApiService.getAshaProfileData(any()) } returns
            Response.success(jsonBody("""{"statusCode":5000}"""))

        assertTrue(repo.pullAndSaveAshaProfile(user))
    }

    @Test
    fun `pullAndSaveAshaProfile returns true when no data present`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { amritApiService.getAshaProfileData(any()) } returns
            Response.success(jsonBody("""{"statusCode":200}"""))

        assertTrue(repo.pullAndSaveAshaProfile(user))
    }

    @Test
    fun `pullAndSaveAshaProfile returns false when api throws`() = runTest {
        val user = mockk<User>(relaxed = true)
        coEvery { amritApiService.getAshaProfileData(any()) } throws RuntimeException("network")

        assertFalse(repo.pullAndSaveAshaProfile(user))
    }
}
