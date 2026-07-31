package org.piramalswasthya.sakhi.repositories

import android.content.Context
import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.VLFDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.AHDCache
import org.piramalswasthya.sakhi.model.DewormingCache
import org.piramalswasthya.sakhi.model.ORSCampaignCache
import org.piramalswasthya.sakhi.model.PHCReviewMeetingCache
import org.piramalswasthya.sakhi.model.PulsePolioCampaignCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.VHNCCache
import org.piramalswasthya.sakhi.model.VHNDCache
import org.piramalswasthya.sakhi.model.dynamicEntity.filariaaMdaCampaign.FilariaMDACampaignFormResponseJsonEntity
import org.piramalswasthya.sakhi.network.AmritApiService
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class VLFRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var api: AmritApiService
    @MockK private lateinit var vlfDao: VLFDao
    @MockK private lateinit var appContext: Context

    // The DAO reached through database.vlfDao inside the repo.
    private val dbDao: VLFDao = mockk(relaxed = true)

    private lateinit var repo: VLFRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { database.vlfDao } returns dbDao
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = VLFRepo(database, userRepo, preferenceDao, api, vlfDao, appContext)
    }

    private fun loggedIn() {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 1
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { preferenceDao.getLoggedInUser() } returns user
    }

    private fun jsonResponse(body: String, code: Int = 200): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns body
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns responseBody
        return response
    }

    // ---------------- simple getters delegating to database.vlfDao ----------------

    @Test
    fun `getVHND returns dao result`() = runTest {
        val cache = mockk<VHNDCache>(relaxed = true)
        every { dbDao.getVHND(7) } returns cache

        assertEquals(cache, repo.getVHND(7))
    }

    @Test
    fun `getUnsyncedPulsePolioCampaign returns empty list when dao null`() = runTest {
        every { dbDao.getPulsePolioCampaign(SyncState.UNSYNCED) } returns null

        assertTrue(repo.getUnsyncedPulsePolioCampaign().isEmpty())
    }

    @Test
    fun `getUnsyncedORSCampaign returns dao list`() = runTest {
        val list = listOf(mockk<ORSCampaignCache>(relaxed = true))
        every { dbDao.getORSCampaign(SyncState.UNSYNCED) } returns list

        assertEquals(list, repo.getUnsyncedORSCampaign())
    }

    @Test
    fun `getAllPulsePolioCampaigns delegates to dao`() = runTest {
        val list = listOf(mockk<PulsePolioCampaignCache>(relaxed = true))
        coEvery { dbDao.getAllPulsePolioCampaigns() } returns list

        assertEquals(list, repo.getAllPulsePolioCampaigns())
    }

    // ---------------- getLastSubmissionDate ----------------

    @Test
    fun `getLastSubmissionDate returns null flow for unknown formId`() = runTest {
        assertNull(repo.getLastSubmissionDate("does-not-exist").first())
    }

    @Test
    fun `getLastSubmissionDate delegates to dao for vhnd`() {
        val flow = flowOf<String?>("01-01-2026")
        every { vlfDao.getLastVHNDSubmissionDate() } returns flow

        assertEquals(flow, repo.getLastSubmissionDate("vhnd"))
    }

    // ---------------- no-user guard on server pulls ----------------

    @Test(expected = IllegalStateException::class)
    fun `getORSCampaignFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        repo.getORSCampaignFromServer()
    }

    @Test(expected = IllegalStateException::class)
    fun `getVHNDFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        repo.getVHNDFromServer()
    }

    @Test(expected = IllegalStateException::class)
    fun `getFilariaMdaCampaignFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        repo.getFilariaMdaCampaignFromServer()
    }

    // ---------------- pushUnSyncedRecords coordinator ----------------

    @Test
    fun `pushUnSyncedRecords returns true when nothing unsynced`() = runTest {
        loggedIn()
        every { dbDao.getVHND(SyncState.UNSYNCED) } returns emptyList()
        every { dbDao.getVHNC(SyncState.UNSYNCED) } returns emptyList()
        every { dbDao.getPHC(SyncState.UNSYNCED) } returns emptyList()
        coEvery { dbDao.getAHD(SyncState.UNSYNCED) } returns emptyList()
        coEvery { dbDao.getDeworming(SyncState.UNSYNCED) } returns emptyList()

        assertTrue(repo.pushUnSyncedRecords())
    }

    // ---------------- savefilariaMdaCampaignToServer early exit ----------------

    @Test
    fun `savefilariaMdaCampaignToServer saves nothing for empty array`() = runTest {
        repo.savefilariaMdaCampaignToServer("[]")

        coVerify(exactly = 0) {
            dbDao.saveRecord(any<FilariaMDACampaignFormResponseJsonEntity>())
        }
    }

    // ---------------- isFormFilledForCurrentMonth ----------------

    @Test
    fun `isFormFilledForCurrentMonth reports all false for empty data`() = runTest {
        every { vlfDao.getAllVHND() } returns flowOf(emptyList())
        every { vlfDao.getAllVHNC() } returns flowOf(emptyList())
        every { vlfDao.getAllPHC() } returns flowOf(emptyList())
        every { vlfDao.getAllAHD() } returns flowOf(emptyList())
        every { vlfDao.countDewormingInLastSixMonths() } returns flowOf(0)

        val result = repo.isFormFilledForCurrentMonth().first()

        assertFalse(result["VHND"]!!)
        assertFalse(result["VHNC"]!!)
        assertFalse(result["PHC"]!!)
        assertFalse(result["AHD"]!!)
        assertFalse(result["DEWORMING"]!!)
    }

    // ---------------- simple getters delegating to database.vlfDao ----------------

    @Test
    fun `getVHNC returns dao result`() = runTest {
        val cache = mockk<VHNCCache>(relaxed = true)
        every { dbDao.getVHNC(3) } returns cache
        assertEquals(cache, repo.getVHNC(3))
    }

    @Test
    fun `getPHC returns dao result`() = runTest {
        val cache = mockk<PHCReviewMeetingCache>(relaxed = true)
        every { dbDao.getPHC(4) } returns cache
        assertEquals(cache, repo.getPHC(4))
    }

    @Test
    fun `getAHD returns dao result`() = runTest {
        val cache = mockk<AHDCache>(relaxed = true)
        coEvery { dbDao.getAHD(5) } returns cache
        assertEquals(cache, repo.getAHD(5))
    }

    @Test
    fun `getDeworming returns dao result`() = runTest {
        val cache = mockk<DewormingCache>(relaxed = true)
        coEvery { dbDao.getDeworming(6) } returns cache
        assertEquals(cache, repo.getDeworming(6))
    }

    @Test
    fun `getPulsePolioCampaign returns dao result`() = runTest {
        val cache = mockk<PulsePolioCampaignCache>(relaxed = true)
        coEvery { dbDao.getPulsePolioCampaign(8) } returns cache
        assertEquals(cache, repo.getPulsePolioCampaign(8))
    }

    @Test
    fun `getORSCampaign returns dao result`() = runTest {
        val cache = mockk<ORSCampaignCache>(relaxed = true)
        coEvery { dbDao.getORSCampaign(9) } returns cache
        assertEquals(cache, repo.getORSCampaign(9))
    }

    @Test
    fun `getFilariaMdaCampaign returns dao result`() = runTest {
        val cache = mockk<FilariaMDACampaignFormResponseJsonEntity>(relaxed = true)
        coEvery { dbDao.getFilariaMdaCampaign(11) } returns cache
        assertEquals(cache, repo.getFilariaMdaCampaign(11))
    }

    @Test
    fun `getAllORSCampaigns delegates to dao`() = runTest {
        val list = listOf(mockk<ORSCampaignCache>(relaxed = true))
        coEvery { dbDao.getAllORSCampaigns() } returns list
        assertEquals(list, repo.getAllORSCampaigns())
    }

    // ---------------- unsynced-list null vs non-null branches ----------------

    @Test
    fun `getUnsyncedFilariaMdaCampaign returns empty list when dao null`() = runTest {
        every { dbDao.getFilariaMdaCampaign(SyncState.UNSYNCED) } returns null
        assertTrue(repo.getUnsyncedFilariaMdaCampaign().isEmpty())
    }

    @Test
    fun `getUnsyncedFilariaMdaCampaign returns dao list`() = runTest {
        val list = listOf(mockk<FilariaMDACampaignFormResponseJsonEntity>(relaxed = true))
        every { dbDao.getFilariaMdaCampaign(SyncState.UNSYNCED) } returns list
        assertEquals(list, repo.getUnsyncedFilariaMdaCampaign())
    }

    @Test
    fun `getUnsyncedPulsePolioCampaign returns dao list when non-null`() = runTest {
        val list = listOf(mockk<PulsePolioCampaignCache>(relaxed = true))
        every { dbDao.getPulsePolioCampaign(SyncState.UNSYNCED) } returns list
        assertEquals(list, repo.getUnsyncedPulsePolioCampaign())
    }

    @Test
    fun `getUnsyncedORSCampaign returns empty list when dao null`() = runTest {
        every { dbDao.getORSCampaign(SyncState.UNSYNCED) } returns null
        assertTrue(repo.getUnsyncedORSCampaign().isEmpty())
    }

    // ---------------- getLastSubmissionDate delegations ----------------

    @Test
    fun `getLastSubmissionDate delegates to dao for vhnc`() {
        val flow = flowOf<String?>("02-02-2026")
        every { vlfDao.getLastVHNCSubmissionDate() } returns flow
        assertEquals(flow, repo.getLastSubmissionDate("vhnc"))
    }

    @Test
    fun `getLastSubmissionDate delegates to dao for phc_review`() {
        val flow = flowOf<String?>("03-03-2026")
        every { vlfDao.getLastPHCSubmissionDate() } returns flow
        assertEquals(flow, repo.getLastSubmissionDate("phc_review"))
    }

    @Test
    fun `getLastSubmissionDate delegates to dao for ahd`() {
        val flow = flowOf<String?>("04-04-2026")
        every { vlfDao.getLastAHDSubmissionDate() } returns flow
        assertEquals(flow, repo.getLastSubmissionDate("ahd"))
    }

    @Test
    fun `getLastSubmissionDate delegates to dao for deworming`() {
        val flow = flowOf<String?>("05-05-2026")
        every { vlfDao.getLastDewormingSubmissionDate() } returns flow
        assertEquals(flow, repo.getLastSubmissionDate("deworming"))
    }

    // ---------------- no-user guards on server pulls ----------------

    @Test(expected = IllegalStateException::class)
    fun `getVHNCFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        repo.getVHNCFromServer()
    }

    @Test(expected = IllegalStateException::class)
    fun `getPHCFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        repo.getPHCFromServer()
    }

    @Test(expected = IllegalStateException::class)
    fun `getAHDFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        repo.getAHDFromServer()
    }

    @Test(expected = IllegalStateException::class)
    fun `getDewormingFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        repo.getDewormingFromServer()
    }

    @Test(expected = IllegalStateException::class)
    fun `getPulsePolioCampaignFromServer throws when no user`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null
        repo.getPulsePolioCampaignFromServer()
    }

    // ---------------- server pull when-branches ----------------

    @Test
    fun `getVHNDFromServer returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { api.getVLFData(any()) } returns jsonResponse("{}", code = 500)
        assertEquals(-1, repo.getVHNDFromServer())
    }

    @Test
    fun `getVHNDFromServer returns 0 on no record found`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)
        assertEquals(0, repo.getVHNDFromServer())
    }

    @Test
    fun `getORSCampaignFromServer returns 1 on empty data array`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":[]}"""
        coEvery { api.getORSCampaignData() } returns jsonResponse(json)
        assertEquals(1, repo.getORSCampaignFromServer())
    }

    @Test
    fun `getPulsePolioCampaignFromServer returns 1 on empty data array`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":[]}"""
        coEvery { api.getPulsePolioCampaignData() } returns jsonResponse(json)
        assertEquals(1, repo.getPulsePolioCampaignFromServer())
    }

    // ---------------- save delegations to database.vlfDao ----------------

    @Test
    fun `saveRecord VHND delegates to dao`() = runTest {
        val cache = mockk<VHNDCache>(relaxed = true)
        repo.saveRecord(cache)
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `saveRecord VHNC delegates to dao`() = runTest {
        val cache = mockk<VHNCCache>(relaxed = true)
        repo.saveRecord(cache)
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `saveRecord PHC delegates to dao`() = runTest {
        val cache = mockk<PHCReviewMeetingCache>(relaxed = true)
        repo.saveRecord(cache)
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `saveAHDRecord delegates to dao`() = runTest {
        val cache = mockk<AHDCache>(relaxed = true)
        repo.saveAHDRecord(cache)
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `saveDeworming cache delegates to dao`() = runTest {
        val cache = mockk<DewormingCache>(relaxed = true)
        repo.saveDeworming(cache)
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `savePulsePolioCampaign delegates to dao`() = runTest {
        val cache = mockk<PulsePolioCampaignCache>(relaxed = true)
        repo.savePulsePolioCampaign(cache)
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `saveORSCampaign delegates to dao`() = runTest {
        val cache = mockk<ORSCampaignCache>(relaxed = true)
        repo.saveORSCampaign(cache)
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `saveFilariaMdaCampaign delegates to dao`() = runTest {
        val cache = mockk<FilariaMDACampaignFormResponseJsonEntity>(relaxed = true)
        repo.saveFilariaMdaCampaign(cache)
        coVerify { dbDao.saveRecord(cache) }
    }

    // ---------------- getVLFData-backed pull when-branches ----------------

    @Test
    fun `getVHNCFromServer returns 1 on success with empty entries`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":"{\"entries\":[]}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)
        assertEquals(1, repo.getVHNCFromServer())
    }

    @Test
    fun `getVHNCFromServer returns 0 on no record found`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)
        assertEquals(0, repo.getVHNCFromServer())
    }

    @Test
    fun `getVHNCFromServer returns -1 when token refresh fails`() = runTest {
        loggedIn()
        val json = """{"statusCode":5002,"errorMessage":""}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false
        assertEquals(-1, repo.getVHNCFromServer())
    }

    @Test
    fun `getPHCFromServer returns 1 on success with empty entries`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":"{\"entries\":[]}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)
        assertEquals(1, repo.getPHCFromServer())
    }

    @Test
    fun `getPHCFromServer returns 0 on no record found`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)
        assertEquals(0, repo.getPHCFromServer())
    }

    @Test
    fun `getAHDFromServer returns 1 on success with empty entries`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":"{\"entries\":[]}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)
        assertEquals(1, repo.getAHDFromServer())
    }

    @Test
    fun `getDewormingFromServer returns 1 on success with empty entries`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":"{\"entries\":[]}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)
        assertEquals(1, repo.getDewormingFromServer())
    }

    @Test
    fun `getDewormingFromServer returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { api.getVLFData(any()) } returns jsonResponse("{}", code = 500)
        assertEquals(-1, repo.getDewormingFromServer())
    }

    // ---------------- ORS / Filaria pull error branches ----------------

    @Test
    fun `getORSCampaignFromServer returns -1 on unexpected status`() = runTest {
        loggedIn()
        val json = """{"statusCode":9999}"""
        coEvery { api.getORSCampaignData() } returns jsonResponse(json)
        assertEquals(-1, repo.getORSCampaignFromServer())
    }

    @Test
    fun `getFilariaMdaCampaignFromServer returns 0 on empty data array`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":[]}"""
        coEvery { api.getFilariaMdaCampaign() } returns jsonResponse(json)
        assertEquals(0, repo.getFilariaMdaCampaignFromServer())
    }

    @Test
    fun `getFilariaMdaCampaignFromServer returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { api.getFilariaMdaCampaign() } returns jsonResponse("{}", code = 500)
        assertEquals(-1, repo.getFilariaMdaCampaignFromServer())
    }

    // =====================================================
    // multipart campaign uploads (no photos -> pure okhttp path)
    // =====================================================

    /** Response whose isSuccessful is explicitly stubbed. */
    private fun uploadResponse(successful: Boolean): Response<ResponseBody> {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns successful
        every { response.code() } returns if (successful) 200 else 500
        return response
    }

    @Test
    fun `saveORSCampaignToServer marks record synced on success`() = runTest {
        val cache = ORSCampaignCache(id = 1, formDataJson = null, syncState = SyncState.UNSYNCED)
        coEvery { api.saveORSCampaignData(any()) } returns uploadResponse(true)

        assertTrue(repo.saveORSCampaignToServer(cache))

        assertEquals(SyncState.SYNCED, cache.syncState)
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `saveORSCampaignToServer returns false on unsuccessful response`() = runTest {
        val cache = ORSCampaignCache(id = 1, formDataJson = null, syncState = SyncState.UNSYNCED)
        coEvery { api.saveORSCampaignData(any()) } returns uploadResponse(false)

        assertFalse(repo.saveORSCampaignToServer(cache))

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `saveORSCampaignToServer skips photo paths that do not exist`() = runTest {
        // JSONArray photo branch; the paths are neither data-uris nor content-uris,
        // so the code falls through to File(path).exists() == false.
        val cache = ORSCampaignCache(
            id = 2,
            formDataJson = """{"fields":{"campaign_photos":["/no/such/file-a.jpg"]}}""",
            syncState = SyncState.UNSYNCED
        )
        coEvery { api.saveORSCampaignData(any()) } returns uploadResponse(true)

        assertTrue(repo.saveORSCampaignToServer(cache))
    }

    @Test
    fun `savePulsePolioCampaignToServer marks record synced on success`() = runTest {
        val cache =
            PulsePolioCampaignCache(id = 1, formDataJson = null, syncState = SyncState.UNSYNCED)
        coEvery { api.savePulsePolioCampaignData(any()) } returns uploadResponse(true)

        assertTrue(repo.savePulsePolioCampaignToServer(cache))

        assertEquals(SyncState.SYNCED, cache.syncState)
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `savePulsePolioCampaignToServer returns false on unsuccessful response`() = runTest {
        val cache =
            PulsePolioCampaignCache(id = 1, formDataJson = null, syncState = SyncState.UNSYNCED)
        coEvery { api.savePulsePolioCampaignData(any()) } returns uploadResponse(false)

        assertFalse(repo.savePulsePolioCampaignToServer(cache))
    }

    @Test
    fun `savePulsePolioCampaignToServer skips photo paths that do not exist`() = runTest {
        val cache = PulsePolioCampaignCache(
            id = 3,
            formDataJson = """{"fields":{"campaign_photos":["/no/such/file-b.jpg"]}}""",
            syncState = SyncState.UNSYNCED
        )
        coEvery { api.savePulsePolioCampaignData(any()) } returns uploadResponse(true)

        assertTrue(repo.savePulsePolioCampaignToServer(cache))
    }

    @Test
    fun `saveMdaFilariaCampaignToServer marks record synced on success`() = runTest {
        val cache = mockk<FilariaMDACampaignFormResponseJsonEntity>(relaxed = true)
        every { cache.formDataJson } returns ""
        coEvery { api.saveFilariaMdaCampaign(any()) } returns uploadResponse(true)

        assertTrue(repo.saveMdaFilariaCampaignToServer(cache))

        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `saveMdaFilariaCampaignToServer returns false on unsuccessful response`() = runTest {
        val cache = mockk<FilariaMDACampaignFormResponseJsonEntity>(relaxed = true)
        every { cache.formDataJson } returns ""
        coEvery { api.saveFilariaMdaCampaign(any()) } returns uploadResponse(false)

        assertFalse(repo.saveMdaFilariaCampaignToServer(cache))
    }

    // =====================================================
    // savefilariaMdaCampaignToServer parsing
    // =====================================================

    @Test
    fun `savefilariaMdaCampaignToServer stores a new campaign entry`() = runTest {
        coEvery { dbDao.getFilariaMdaCampaign(7) } returns null

        repo.savefilariaMdaCampaignToServer("""[{"id":7,"fields":{"start_date":"01-05-2026"}}]""")

        coVerify { dbDao.saveRecord(any<FilariaMDACampaignFormResponseJsonEntity>()) }
    }

    @Test
    fun `savefilariaMdaCampaignToServer keeps an existing campaign entry`() = runTest {
        coEvery { dbDao.getFilariaMdaCampaign(7) } returns
                mockk<FilariaMDACampaignFormResponseJsonEntity>(relaxed = true)

        repo.savefilariaMdaCampaignToServer("""[{"id":7,"fields":{"start_date":"01-05-2026"}}]""")

        coVerify(exactly = 0) { dbDao.saveRecord(any<FilariaMDACampaignFormResponseJsonEntity>()) }
    }

    @Test
    fun `savefilariaMdaCampaignToServer ignores entries without a fields node`() = runTest {
        repo.savefilariaMdaCampaignToServer("""[{"id":7}]""")

        coVerify(exactly = 0) { dbDao.saveRecord(any<FilariaMDACampaignFormResponseJsonEntity>()) }
    }

    // =====================================================
    // Filaria MDA pull data-shape branches
    // =====================================================

    @Test
    fun `getFilariaMdaCampaignFromServer stores entries from a json array`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":[{"id":21,"fields":{"start_date":"01-05-2026"}}]}"""
        coEvery { api.getFilariaMdaCampaign() } returns jsonResponse(json)
        coEvery { dbDao.getFilariaMdaCampaign(21) } returns null

        assertEquals(1, repo.getFilariaMdaCampaignFromServer())
        coVerify { dbDao.saveRecord(any<FilariaMDACampaignFormResponseJsonEntity>()) }
    }

    @Test
    fun `getFilariaMdaCampaignFromServer stores entries from a stringified array`() = runTest {
        loggedIn()
        val json =
            """{"statusCode":200,"data":"[{\"id\":22,\"fields\":{\"start_date\":\"01-05-2026\"}}]"}"""
        coEvery { api.getFilariaMdaCampaign() } returns jsonResponse(json)
        coEvery { dbDao.getFilariaMdaCampaign(22) } returns null

        assertEquals(1, repo.getFilariaMdaCampaignFromServer())
        coVerify { dbDao.saveRecord(any<FilariaMDACampaignFormResponseJsonEntity>()) }
    }

    @Test
    fun `getFilariaMdaCampaignFromServer stores a single json object`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":{"id":23,"fields":{"start_date":"2026-05-01"}}}"""
        coEvery { api.getFilariaMdaCampaign() } returns jsonResponse(json)
        coEvery { dbDao.getFilariaMdaCampaign(23) } returns null

        assertEquals(1, repo.getFilariaMdaCampaignFromServer())
        coVerify { dbDao.saveRecord(any<FilariaMDACampaignFormResponseJsonEntity>()) }
    }

    @Test
    fun `getFilariaMdaCampaignFromServer returns -1 when token refresh fails`() = runTest {
        loggedIn()
        coEvery { api.getFilariaMdaCampaign() } returns jsonResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getFilariaMdaCampaignFromServer())
    }

    // =====================================================
    // ORS / Pulse Polio pull persistence
    // =====================================================

    @Test
    fun `getORSCampaignFromServer stores a new entry`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":[{"id":31,"fields":{"campaign_date":"01-05-2026"}}]}"""
        coEvery { api.getORSCampaignData() } returns jsonResponse(json)
        coEvery { dbDao.getORSCampaign(31) } returns null

        assertEquals(1, repo.getORSCampaignFromServer())
        coVerify { dbDao.saveRecord(any<ORSCampaignCache>()) }
    }

    @Test
    fun `getORSCampaignFromServer updates an existing entry`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":[{"id":32,"fields":{"campaign_date":"01-05-2026"}}]}"""
        coEvery { api.getORSCampaignData() } returns jsonResponse(json)
        val existing = ORSCampaignCache(id = 32, formDataJson = null, syncState = SyncState.UNSYNCED)
        coEvery { dbDao.getORSCampaign(32) } returns existing

        assertEquals(1, repo.getORSCampaignFromServer())

        assertEquals(SyncState.SYNCED, existing.syncState)
        coVerify { dbDao.saveRecord(existing) }
    }

    @Test
    fun `getPulsePolioCampaignFromServer stores a new entry`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":[{"id":41,"fields":{"campaign_date":"01-05-2026"}}]}"""
        coEvery { api.getPulsePolioCampaignData() } returns jsonResponse(json)
        coEvery { dbDao.getPulsePolioCampaign(41) } returns null

        assertEquals(1, repo.getPulsePolioCampaignFromServer())
        coVerify { dbDao.saveRecord(any<PulsePolioCampaignCache>()) }
    }

    @Test
    fun `getPulsePolioCampaignFromServer updates an existing entry`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":[{"id":42,"fields":{"campaign_date":"01-05-2026"}}]}"""
        coEvery { api.getPulsePolioCampaignData() } returns jsonResponse(json)
        val existing =
            PulsePolioCampaignCache(id = 42, formDataJson = null, syncState = SyncState.UNSYNCED)
        coEvery { dbDao.getPulsePolioCampaign(42) } returns existing

        assertEquals(1, repo.getPulsePolioCampaignFromServer())

        assertEquals(SyncState.SYNCED, existing.syncState)
        coVerify { dbDao.saveRecord(existing) }
    }

    @Test
    fun `getPulsePolioCampaignFromServer returns -1 when token refresh fails`() = runTest {
        loggedIn()
        coEvery { api.getPulsePolioCampaignData() } returns jsonResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getPulsePolioCampaignFromServer())
    }

    // =====================================================
    // getLastSubmissionDate campaign-form branches
    // =====================================================

    @Test
    fun `getLastSubmissionDate picks the latest pulse polio campaign date`() = runTest {
        every { vlfDao.getAllPulsePolioCampaignForDate() } returns flowOf(
            listOf(
                PulsePolioCampaignCache(
                    id = 1,
                    formDataJson = """{"fields":{"campaign_date":"2026-01-05"}}""",
                    syncState = SyncState.SYNCED
                ),
                PulsePolioCampaignCache(
                    id = 2,
                    formDataJson = """{"fields":{"campaign_date":"2026-03-05"}}""",
                    syncState = SyncState.SYNCED
                ),
                // No parsable fields node -> filtered out.
                PulsePolioCampaignCache(id = 3, formDataJson = null, syncState = SyncState.SYNCED)
            )
        )

        assertEquals("2026-03-05", repo.getLastSubmissionDate("pulse_polio_campaign_form").first())
    }

    @Test
    fun `getLastSubmissionDate returns null when no ors campaign has a date`() = runTest {
        every { vlfDao.getAllORSCampaignForDate() } returns flowOf(
            listOf(ORSCampaignCache(id = 1, formDataJson = "not-json", syncState = SyncState.SYNCED))
        )

        assertNull(repo.getLastSubmissionDate("ors_campaign_form").first())
    }

    @Test
    fun `getLastSubmissionDate picks the latest ors campaign date`() = runTest {
        every { vlfDao.getAllORSCampaignForDate() } returns flowOf(
            listOf(
                ORSCampaignCache(
                    id = 1,
                    formDataJson = """{"fields":{"campaign_date":"2026-02-11"}}""",
                    syncState = SyncState.SYNCED
                )
            )
        )

        assertEquals("2026-02-11", repo.getLastSubmissionDate("ors_campaign_form").first())
    }

    // =====================================================
    // pushUnSyncedRecords chunk loop
    // =====================================================

    /** All five VLF push sub-methods see an empty unsynced list unless overridden. */
    private fun stubNothingUnsynced() {
        every { dbDao.getVHND(SyncState.UNSYNCED) } returns emptyList()
        every { dbDao.getVHNC(SyncState.UNSYNCED) } returns emptyList()
        every { dbDao.getPHC(SyncState.UNSYNCED) } returns emptyList()
        coEvery { dbDao.getAHD(SyncState.UNSYNCED) } returns emptyList()
        coEvery { dbDao.getDeworming(SyncState.UNSYNCED) } returns emptyList()
    }

    @Test
    fun `pushUnSyncedRecords marks a VHND chunk synced on success`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<VHNDCache>(relaxed = true)
        every { dbDao.getVHND(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveVHNDData(any()) } returns jsonResponse("""{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify { api.saveVHNDData(any()) }
        verify { cache.syncState = SyncState.SYNCED }
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords leaves a VHND chunk unsynced on http error`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<VHNDCache>(relaxed = true)
        every { dbDao.getVHND(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveVHNDData(any()) } returns jsonResponse("{}", code = 500)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords refreshes the token for a VHND chunk auth failure`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<VHNDCache>(relaxed = true)
        every { dbDao.getVHND(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveVHNDData(any()) } returns jsonResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pushUnSyncedRecords())

        coVerify { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords leaves a VHND chunk unsynced on unexpected status`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<VHNDCache>(relaxed = true)
        every { dbDao.getVHND(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveVHNDData(any()) } returns jsonResponse("""{"statusCode":9999}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }
}
