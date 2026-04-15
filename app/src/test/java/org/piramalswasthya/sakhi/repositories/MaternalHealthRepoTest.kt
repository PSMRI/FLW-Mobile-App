package org.piramalswasthya.sakhi.repositories

import android.app.Application
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.MaternalHealthDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ANCPost
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MaternalHealthRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Application
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var maternalHealthDao: MaternalHealthDao
    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao

    private lateinit var repo: MaternalHealthRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = MaternalHealthRepo(
            context, amritApiService, maternalHealthDao,
            database, userRepo, benDao, preferenceDao
        )
    }

    // =====================================================
    // Data Retrieval Tests
    // =====================================================

    @Test
    fun `getSavedRegistrationRecord returns record when exists`() = runTest {
        val record = mockk<PregnantWomanRegistrationCache>()
        coEvery { maternalHealthDao.getSavedRecord(100L) } returns record

        val result = repo.getSavedRegistrationRecord(100L)

        assertTrue(result != null)
    }

    @Test
    fun `getSavedRegistrationRecord returns null when not exists`() = runTest {
        coEvery { maternalHealthDao.getSavedRecord(100L) } returns null

        val result = repo.getSavedRegistrationRecord(100L)

        assertTrue(result == null)
    }

    @Test
    fun `getLatestActiveRegistrationRecord delegates to dao`() = runTest {
        val record = mockk<PregnantWomanRegistrationCache>()
        coEvery { maternalHealthDao.getSavedActiveRecord(100L) } returns record

        val result = repo.getLatestActiveRegistrationRecord(100L)

        assertTrue(result != null)
        coVerify { maternalHealthDao.getSavedActiveRecord(100L) }
    }

    @Test
    fun `getSavedAncRecord returns record by benId and visitNumber`() = runTest {
        val record = mockk<PregnantWomanAncCache>()
        coEvery { maternalHealthDao.getSavedRecord(100L, 2) } returns record

        val result = repo.getSavedAncRecord(100L, 2)

        assertTrue(result != null)
    }

    @Test
    fun `getLatestAncRecord returns latest anc`() = runTest {
        val record = mockk<PregnantWomanAncCache>()
        coEvery { maternalHealthDao.getLatestAnc(100L) } returns record

        val result = repo.getLatestAncRecord(100L)

        assertTrue(result != null)
    }

    @Test
    fun `getAllActiveAncRecords returns list`() = runTest {
        val records = listOf(
            mockk<PregnantWomanAncCache>(),
            mockk<PregnantWomanAncCache>()
        )
        coEvery { maternalHealthDao.getAllActiveAncRecords(100L) } returns records

        val result = repo.getAllActiveAncRecords(100L)

        assertTrue(result.size == 2)
    }

    @Test
    fun `getAllInActiveAncRecords returns list`() = runTest {
        coEvery { maternalHealthDao.getAllInActiveAncRecords(100L) } returns emptyList()

        val result = repo.getAllInActiveAncRecords(100L)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getBenFromId delegates to benDao`() = runTest {
        val ben = mockk<BenRegCache>()
        coEvery { benDao.getBen(100L) } returns ben

        val result = repo.getBenFromId(100L)

        assertTrue(result != null)
        coVerify { benDao.getBen(100L) }
    }

    // =====================================================
    // Persistence Tests
    // =====================================================

    @Test
    fun `persistRegisterRecord calls dao saveRecord`() = runTest {
        val record = mockk<PregnantWomanRegistrationCache>()
        coEvery { maternalHealthDao.saveRecord(record) } returns Unit

        repo.persistRegisterRecord(record)

        coVerify(exactly = 1) { maternalHealthDao.saveRecord(record) }
    }

    @Test
    fun `persistAncRecord calls dao saveRecord`() = runTest {
        val record = mockk<PregnantWomanAncCache>()
        coEvery { maternalHealthDao.saveRecord(record) } returns Unit

        repo.persistAncRecord(record)

        coVerify(exactly = 1) { maternalHealthDao.saveRecord(record) }
    }

    // =====================================================
    // processNewAncVisit() Tests — Sync Logic
    // =====================================================

    @Test
    fun `processNewAncVisit with empty list returns true`() = runTest {
        val user = createMockUser()
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns emptyList()

        val result = repo.processNewAncVisit()

        assertTrue(result)
    }

    @Test
    fun `processNewAncVisit syncs records successfully`() = runTest {
        val user = createMockUser()
        every { preferenceDao.getLoggedInUser() } returns user

        val ancRecord = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord.benId } returns 100L
        every { ancRecord.asPostModel() } returns mockk<ANCPost>(relaxed = true)
        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns listOf(ancRecord)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)

        // Mock successful API response
        val successBody = """{"statusCode":200,"errorMessage":"Success","data":{}}""".toResponseBody("application/json".toMediaTypeOrNull())
        val response = Response.success(successBody)
        coEvery { amritApiService.postAncForm(any()) } returns response
        coEvery { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) } returns Unit

        val result = repo.processNewAncVisit()

        assertTrue(result)
        // Verify the record was updated to SYNCED state
        coVerify(atLeast = 1) { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) }
    }

    @Test
    fun `processNewAncVisit handles record failure with isolation`() = runTest {
        val user = createMockUser()
        every { preferenceDao.getLoggedInUser() } returns user

        val ancRecord1 = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord1.benId } returns 100L
        every { ancRecord1.asPostModel() } returns mockk(relaxed = true)

        val ancRecord2 = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord2.benId } returns 200L
        // This one will fail - no ben exists
        coEvery { benDao.getBen(200L) } returns null

        val ancRecord3 = mockk<PregnantWomanAncCache>(relaxed = true)
        every { ancRecord3.benId } returns 300L
        every { ancRecord3.asPostModel() } returns mockk(relaxed = true)

        coEvery { maternalHealthDao.getAllUnprocessedAncVisits() } returns
                listOf(ancRecord1, ancRecord2, ancRecord3)
        coEvery { benDao.getBen(100L) } returns mockk(relaxed = true)
        coEvery { benDao.getBen(300L) } returns mockk(relaxed = true)

        val successBody = """{"statusCode":200,"errorMessage":"Success"}""".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { amritApiService.postAncForm(any()) } returns Response.success(successBody)
        coEvery { maternalHealthDao.updateANC(any<PregnantWomanAncCache>()) } returns Unit

        val result = repo.processNewAncVisit()

        // Should still return true even though record 2 failed
        assertTrue("Process should return true even with partial failures", result)
    }

    @Test
    fun `processNewAncVisit throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewAncVisit()
            assertTrue("Should have thrown", false)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No user logged in"))
        }
    }

    // =====================================================
    // processNewPwr() Tests
    // =====================================================

    @Test
    fun `processNewPwr with empty list returns true`() = runTest {
        val user = createMockUser()
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { maternalHealthDao.getAllUnprocessedPWRs() } returns emptyList()

        val result = repo.processNewPwr()

        assertTrue(result)
    }

    @Test
    fun `processNewPwr throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewPwr()
            assertTrue("Should have thrown", false)
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No user logged in"))
        }
    }

    // =====================================================
    // Helpers
    // =====================================================

    private fun createMockUser(): User {
        return mockk<User>(relaxed = true).also {
            every { it.userName } returns "testuser"
            every { it.userId } returns 1
            every { it.serviceMapId } returns 10
            every { it.vanId } returns 4
            every { it.password } returns "password"
        }
    }
}
