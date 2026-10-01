package org.piramalswasthya.sakhi.repositories

import android.content.Context
import android.util.Log
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
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
import java.io.File

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

    // =====================================================
    // pushUnSyncedRecordsAHD chunk loop (via pushUnSyncedRecords)
    // =====================================================

    @Test
    fun `pushUnSyncedRecords marks an AHD chunk synced on success`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = AHDCache(id = 101, ahdDate = "01-05-2026", syncState = SyncState.UNSYNCED)
        coEvery { dbDao.getAHD(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveAHDData(any()) } returns jsonResponse("""{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify { api.saveAHDData(any()) }
        assertEquals(SyncState.SYNCED, cache.syncState)
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords leaves an AHD chunk unsynced on http error`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = AHDCache(id = 102, syncState = SyncState.UNSYNCED)
        coEvery { dbDao.getAHD(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveAHDData(any()) } returns jsonResponse("{}", code = 500)

        assertTrue(repo.pushUnSyncedRecords())

        assertEquals(SyncState.UNSYNCED, cache.syncState)
        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords refreshes the token for an AHD chunk auth failure`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = AHDCache(id = 103, syncState = SyncState.UNSYNCED)
        coEvery { dbDao.getAHD(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveAHDData(any()) } returns jsonResponse("""{"statusCode":401}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pushUnSyncedRecords())

        coVerify { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords leaves an AHD chunk unsynced on unexpected status`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = AHDCache(id = 104, syncState = SyncState.UNSYNCED)
        coEvery { dbDao.getAHD(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveAHDData(any()) } returns jsonResponse("""{"statusCode":9999}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords catches an exception during an AHD chunk push`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = AHDCache(id = 105, syncState = SyncState.UNSYNCED)
        coEvery { dbDao.getAHD(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveAHDData(any()) } throws RuntimeException("network down")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords skips AHD chunk update when response body is null`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = AHDCache(id = 106, syncState = SyncState.UNSYNCED)
        coEvery { dbDao.getAHD(SyncState.UNSYNCED) } returns listOf(cache)
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 200
        every { response.body() } returns null
        coEvery { api.saveAHDData(any()) } returns response

        assertTrue(repo.pushUnSyncedRecords())

        assertEquals(SyncState.UNSYNCED, cache.syncState)
        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    // =====================================================
    // savefilariaMdaCampaignToServer additional branches
    // =====================================================

    @Test
    fun `savefilariaMdaCampaignToServer defaults id to zero when id key is missing`() = runTest {
        coEvery { dbDao.getFilariaMdaCampaign(0) } returns null

        repo.savefilariaMdaCampaignToServer("""[{"fields":{"start_date":"01-05-2026"}}]""")

        coVerify { dbDao.saveRecord(any<FilariaMDACampaignFormResponseJsonEntity>()) }
    }

    @Test
    fun `savefilariaMdaCampaignToServer defaults id to zero when id is json null`() = runTest {
        coEvery { dbDao.getFilariaMdaCampaign(0) } returns null

        repo.savefilariaMdaCampaignToServer("""[{"id":null,"fields":{"start_date":"01-05-2026"}}]""")

        coVerify { dbDao.saveRecord(any<FilariaMDACampaignFormResponseJsonEntity>()) }
    }

    @Test
    fun `savefilariaMdaCampaignToServer saves blank visitDate when start_date is absent`() = runTest {
        coEvery { dbDao.getFilariaMdaCampaign(9) } returns null
        val slot = slot<FilariaMDACampaignFormResponseJsonEntity>()
        coEvery { dbDao.saveRecord(capture(slot)) } just Runs

        repo.savefilariaMdaCampaignToServer("""[{"id":9,"fields":{}}]""")

        assertEquals("", slot.captured.visitDate)
        assertEquals("", slot.captured.visitYear)
    }

    @Test
    fun `savefilariaMdaCampaignToServer swallows an exception for a malformed fields node`() = runTest {
        repo.savefilariaMdaCampaignToServer("""[{"id":10,"fields":"not-an-object"}]""")

        coVerify(exactly = 0) { dbDao.saveRecord(any<FilariaMDACampaignFormResponseJsonEntity>()) }
    }

    // =====================================================
    // pushUnSyncedRecordsVHNC chunk loop (via pushUnSyncedRecords)
    // =====================================================

    @Test
    fun `pushUnSyncedRecords marks a VHNC chunk synced on success`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<VHNCCache>(relaxed = true)
        every { dbDao.getVHNC(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveVHNCData(any()) } returns jsonResponse("""{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify { api.saveVHNCData(any()) }
        verify { cache.syncState = SyncState.SYNCED }
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords leaves a VHNC chunk unsynced on http error`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<VHNCCache>(relaxed = true)
        every { dbDao.getVHNC(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveVHNCData(any()) } returns jsonResponse("{}", code = 500)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords refreshes the token for a VHNC chunk auth failure`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<VHNCCache>(relaxed = true)
        every { dbDao.getVHNC(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveVHNCData(any()) } returns jsonResponse("""{"statusCode":401}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pushUnSyncedRecords())

        coVerify { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords leaves a VHNC chunk unsynced on unexpected status`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<VHNCCache>(relaxed = true)
        every { dbDao.getVHNC(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveVHNCData(any()) } returns jsonResponse("""{"statusCode":9999}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords catches an exception during a VHNC chunk push`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<VHNCCache>(relaxed = true)
        every { dbDao.getVHNC(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveVHNCData(any()) } throws RuntimeException("network down")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords skips VHNC chunk update when response body is null`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<VHNCCache>(relaxed = true)
        every { dbDao.getVHNC(SyncState.UNSYNCED) } returns listOf(cache)
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 200
        every { response.body() } returns null
        coEvery { api.saveVHNCData(any()) } returns response

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    // =====================================================
    // pushUnSyncedRecordsPHC chunk loop (via pushUnSyncedRecords)
    // =====================================================

    @Test
    fun `pushUnSyncedRecords marks a PHC chunk synced on success`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<PHCReviewMeetingCache>(relaxed = true)
        every { dbDao.getPHC(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.savePHCData(any()) } returns jsonResponse("""{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify { api.savePHCData(any()) }
        verify { cache.syncState = SyncState.SYNCED }
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords leaves a PHC chunk unsynced on http error`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<PHCReviewMeetingCache>(relaxed = true)
        every { dbDao.getPHC(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.savePHCData(any()) } returns jsonResponse("{}", code = 500)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords refreshes the token for a PHC chunk auth failure`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<PHCReviewMeetingCache>(relaxed = true)
        every { dbDao.getPHC(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.savePHCData(any()) } returns jsonResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pushUnSyncedRecords())

        coVerify { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords leaves a PHC chunk unsynced on unexpected status`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<PHCReviewMeetingCache>(relaxed = true)
        every { dbDao.getPHC(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.savePHCData(any()) } returns jsonResponse("""{"statusCode":9999}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords catches an exception during a PHC chunk push`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<PHCReviewMeetingCache>(relaxed = true)
        every { dbDao.getPHC(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.savePHCData(any()) } throws RuntimeException("network down")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    // =====================================================
    // pushUnSyncedRecordsDeworming chunk loop (via pushUnSyncedRecords)
    // =====================================================

    @Test
    fun `pushUnSyncedRecords marks a Deworming chunk synced on success`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<DewormingCache>(relaxed = true)
        coEvery { dbDao.getDeworming(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveDewormingData(any()) } returns jsonResponse("""{"statusCode":200}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify { api.saveDewormingData(any()) }
        verify { cache.syncState = SyncState.SYNCED }
        coVerify { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords leaves a Deworming chunk unsynced on http error`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<DewormingCache>(relaxed = true)
        coEvery { dbDao.getDeworming(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveDewormingData(any()) } returns jsonResponse("{}", code = 500)

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords refreshes the token for a Deworming chunk auth failure`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<DewormingCache>(relaxed = true)
        coEvery { dbDao.getDeworming(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveDewormingData(any()) } returns jsonResponse("""{"statusCode":401}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.pushUnSyncedRecords())

        coVerify { userRepo.refreshTokenTmc(any(), any()) }
        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords leaves a Deworming chunk unsynced on unexpected status`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<DewormingCache>(relaxed = true)
        coEvery { dbDao.getDeworming(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveDewormingData(any()) } returns jsonResponse("""{"statusCode":9999}""")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords catches an exception during a Deworming chunk push`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<DewormingCache>(relaxed = true)
        coEvery { dbDao.getDeworming(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveDewormingData(any()) } throws RuntimeException("network down")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `savefilariaMdaCampaignToServer processes remaining entries after one throws`() = runTest {
        coEvery { dbDao.getFilariaMdaCampaign(201) } returns null
        coEvery { dbDao.getFilariaMdaCampaign(203) } returns
                mockk<FilariaMDACampaignFormResponseJsonEntity>(relaxed = true)

        val json = """
            [
              {"id":201,"fields":{"start_date":"01-05-2026"}},
              {"id":202,"fields":"broken"},
              {"id":203,"fields":{"start_date":"2026-05-01"}},
              {"id":204}
            ]
        """.trimIndent()

        repo.savefilariaMdaCampaignToServer(json)

        coVerify(exactly = 1) { dbDao.saveRecord(any<FilariaMDACampaignFormResponseJsonEntity>()) }
    }

    @Test
    fun `getVHNDFromServer saves new entries and skips existing, dateless or malformed ones`() = runTest {
        loggedIn()
        every { dbDao.getVHND(501) } returns null
        every { dbDao.getVHND(502) } returns mockk<VHNDCache>(relaxed = true)
        val json = """{"statusCode":200,"errorMessage":"","data":"{\"entries\":[{\"id\":501,\"vhndDate\":\"01-05-2026\"},{\"id\":502,\"vhndDate\":\"01-05-2026\"},{\"id\":503},{\"id\":\"bad\",\"vhndDate\":\"01-05-2026\"}]}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(1, repo.getVHNDFromServer())

        coVerify(exactly = 1) { dbDao.saveRecord(any<VHNDCache>()) }
    }

    @Test
    fun `getVHNCFromServer saves new entries and skips existing, dateless or malformed ones`() = runTest {
        loggedIn()
        every { dbDao.getVHNC(601) } returns null
        every { dbDao.getVHNC(602) } returns mockk<VHNCCache>(relaxed = true)
        val json = """{"statusCode":200,"errorMessage":"","data":"{\"entries\":[{\"id\":601,\"vhncDate\":\"01-05-2026\"},{\"id\":602,\"vhncDate\":\"01-05-2026\"},{\"id\":603},{\"id\":\"bad\",\"vhncDate\":\"01-05-2026\"}]}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(1, repo.getVHNCFromServer())

        coVerify(exactly = 1) { dbDao.saveRecord(any<VHNCCache>()) }
    }

    @Test
    fun `getPHCFromServer saves new entries and skips existing, dateless or malformed ones`() = runTest {
        loggedIn()
        every { dbDao.getPHC(701) } returns null
        every { dbDao.getPHC(702) } returns mockk<PHCReviewMeetingCache>(relaxed = true)
        val json = """{"statusCode":200,"errorMessage":"","data":"{\"entries\":[{\"id\":701,\"phcReviewDate\":\"01-05-2026\"},{\"id\":702,\"phcReviewDate\":\"01-05-2026\"},{\"id\":703},{\"id\":\"bad\",\"phcReviewDate\":\"01-05-2026\"}]}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(1, repo.getPHCFromServer())

        coVerify(exactly = 1) { dbDao.saveRecord(any<PHCReviewMeetingCache>()) }
    }

    @Test
    fun `getAHDFromServer saves new entries and skips existing, dateless or malformed ones`() = runTest {
        loggedIn()
        coEvery { dbDao.getAHD(801) } returns null
        coEvery { dbDao.getAHD(802) } returns mockk<AHDCache>(relaxed = true)
        val json = """{"statusCode":200,"errorMessage":"","data":"{\"entries\":[{\"id\":801,\"mobilizedForAHD\":\"yes\",\"ahdDate\":\"01-05-2026\"},{\"id\":802,\"mobilizedForAHD\":\"yes\",\"ahdDate\":\"01-05-2026\"},{\"id\":803,\"mobilizedForAHD\":\"yes\"},{\"id\":\"bad\",\"mobilizedForAHD\":\"yes\",\"ahdDate\":\"01-05-2026\"}]}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(1, repo.getAHDFromServer())

        coVerify(exactly = 1) { dbDao.saveRecord(any<AHDCache>()) }
    }

    @Test
    fun `getDewormingFromServer saves new entries and skips existing, doneless or malformed ones`() = runTest {
        loggedIn()
        coEvery { dbDao.getDeworming(901) } returns null
        coEvery { dbDao.getDeworming(902) } returns mockk<DewormingCache>(relaxed = true)
        val json = """{"statusCode":200,"errorMessage":"","data":"{\"entries\":[{\"id\":901,\"dewormingDone\":\"yes\"},{\"id\":902,\"dewormingDone\":\"yes\"},{\"id\":903},{\"id\":\"bad\",\"dewormingDone\":\"yes\"}]}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(1, repo.getDewormingFromServer())

        coVerify(exactly = 1) { dbDao.saveRecord(any<DewormingCache>()) }
    }

    @Test
    fun `getVHNDFromServer returns -1 when statusCode 5000 with a different error message`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"Something else"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getVHNDFromServer())
    }

    @Test
    fun `getVHNCFromServer returns -1 when statusCode 5000 with a different error message`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"Something else"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getVHNCFromServer())
    }

    @Test
    fun `getPHCFromServer returns -1 when statusCode 5000 with a different error message`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"Something else"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getPHCFromServer())
    }

    @Test
    fun `getAHDFromServer returns -1 when statusCode 5000 with a different error message`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"Something else"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getAHDFromServer())
    }

    @Test
    fun `getDewormingFromServer returns -1 when statusCode 5000 with a different error message`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"Something else"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getDewormingFromServer())
    }

    @Test
    fun `getVHNDFromServer returns -1 when token refresh fails`() = runTest {
        loggedIn()
        coEvery { api.getVLFData(any()) } returns jsonResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getVHNDFromServer())
    }

    @Test
    fun `getVHNDFromServer refreshes token then retries and terminates on second call`() = runTest {
        loggedIn()
        coEvery { api.getVLFData(any()) } returnsMany listOf(
            jsonResponse("""{"statusCode":5002}"""),
            jsonResponse("{}", code = 500)
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-1, repo.getVHNDFromServer())

        coVerify(exactly = 2) { api.getVLFData(any()) }
    }

    @Test
    fun `getVHNCFromServer refreshes token then retries and terminates on second call`() = runTest {
        loggedIn()
        coEvery { api.getVLFData(any()) } returnsMany listOf(
            jsonResponse("""{"statusCode":5002}"""),
            jsonResponse("{}", code = 500)
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-1, repo.getVHNCFromServer())

        coVerify(exactly = 2) { api.getVLFData(any()) }
    }

    @Test
    fun `getPHCFromServer returns -1 when token refresh fails`() = runTest {
        loggedIn()
        coEvery { api.getVLFData(any()) } returns jsonResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getPHCFromServer())
    }

    @Test
    fun `getPHCFromServer refreshes token then retries and terminates on second call`() = runTest {
        loggedIn()
        coEvery { api.getVLFData(any()) } returnsMany listOf(
            jsonResponse("""{"statusCode":5002}"""),
            jsonResponse("{}", code = 500)
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-1, repo.getPHCFromServer())

        coVerify(exactly = 2) { api.getVLFData(any()) }
    }

    @Test
    fun `getAHDFromServer returns -1 when token refresh fails`() = runTest {
        loggedIn()
        coEvery { api.getVLFData(any()) } returns jsonResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getAHDFromServer())
    }

    @Test
    fun `getAHDFromServer refreshes token then retries and terminates on second call`() = runTest {
        loggedIn()
        coEvery { api.getVLFData(any()) } returnsMany listOf(
            jsonResponse("""{"statusCode":5002}"""),
            jsonResponse("{}", code = 500)
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-1, repo.getAHDFromServer())

        coVerify(exactly = 2) { api.getVLFData(any()) }
    }

    @Test
    fun `getDewormingFromServer returns -1 when token refresh fails`() = runTest {
        loggedIn()
        coEvery { api.getVLFData(any()) } returns jsonResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getDewormingFromServer())
    }

    @Test
    fun `getDewormingFromServer refreshes token then retries and terminates on second call`() = runTest {
        loggedIn()
        coEvery { api.getVLFData(any()) } returnsMany listOf(
            jsonResponse("""{"statusCode":5002}"""),
            jsonResponse("{}", code = 500)
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-1, repo.getDewormingFromServer())

        coVerify(exactly = 2) { api.getVLFData(any()) }
    }

    @Test
    fun `getVHNDFromServer returns 0 when data entries cannot be parsed`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":"{}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.getVHNDFromServer())
    }

    @Test
    fun `getVHNCFromServer returns 0 when data entries cannot be parsed`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":"{}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.getVHNCFromServer())
    }

    @Test
    fun `getPHCFromServer returns 0 when data entries cannot be parsed`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":"{}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.getPHCFromServer())
    }

    @Test
    fun `getAHDFromServer returns 0 when data entries cannot be parsed`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":"{}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.getAHDFromServer())
    }

    @Test
    fun `getDewormingFromServer returns 0 when data entries cannot be parsed`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"errorMessage":"","data":"{}"}"""
        coEvery { api.getVLFData(any()) } returns jsonResponse(json)

        assertEquals(0, repo.getDewormingFromServer())
    }

    @Test
    fun `getORSCampaignFromServer returns -1 when token refresh fails`() = runTest {
        loggedIn()
        coEvery { api.getORSCampaignData() } returns jsonResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getORSCampaignFromServer())
    }

    @Test
    fun `getORSCampaignFromServer refreshes token then retries and terminates on second call`() = runTest {
        loggedIn()
        coEvery { api.getORSCampaignData() } returnsMany listOf(
            jsonResponse("""{"statusCode":5002}"""),
            jsonResponse("{}", code = 500)
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-1, repo.getORSCampaignFromServer())

        coVerify(exactly = 2) { api.getORSCampaignData() }
    }

    @Test
    fun `getORSCampaignFromServer stores entries from a stringified array`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":"[{\"id\":33,\"fields\":{\"campaign_date\":\"01-05-2026\"}}]"}"""
        coEvery { api.getORSCampaignData() } returns jsonResponse(json)
        coEvery { dbDao.getORSCampaign(33) } returns null

        assertEquals(1, repo.getORSCampaignFromServer())
        coVerify { dbDao.saveRecord(any<ORSCampaignCache>()) }
    }

    @Test
    fun `getORSCampaignFromServer stores a single json object`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":{"id":34,"fields":{"campaign_date":"01-05-2026"}}}"""
        coEvery { api.getORSCampaignData() } returns jsonResponse(json)
        coEvery { dbDao.getORSCampaign(34) } returns null

        assertEquals(1, repo.getORSCampaignFromServer())
        coVerify(exactly = 0) { dbDao.saveRecord(any<ORSCampaignCache>()) }
    }

    @Test
    fun `getPulsePolioCampaignFromServer refreshes token then retries and terminates on second call`() = runTest {
        loggedIn()
        coEvery { api.getPulsePolioCampaignData() } returnsMany listOf(
            jsonResponse("""{"statusCode":5002}"""),
            jsonResponse("{}", code = 500)
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-1, repo.getPulsePolioCampaignFromServer())

        coVerify(exactly = 2) { api.getPulsePolioCampaignData() }
    }

    @Test
    fun `getPulsePolioCampaignFromServer stores entries from a stringified array`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":"[{\"id\":43,\"fields\":{\"campaign_date\":\"01-05-2026\"}}]"}"""
        coEvery { api.getPulsePolioCampaignData() } returns jsonResponse(json)
        coEvery { dbDao.getPulsePolioCampaign(43) } returns null

        assertEquals(1, repo.getPulsePolioCampaignFromServer())
        coVerify { dbDao.saveRecord(any<PulsePolioCampaignCache>()) }
    }

    @Test
    fun `getPulsePolioCampaignFromServer stores a single json object`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":{"id":44,"fields":{"campaign_date":"01-05-2026"}}}"""
        coEvery { api.getPulsePolioCampaignData() } returns jsonResponse(json)
        coEvery { dbDao.getPulsePolioCampaign(44) } returns null

        assertEquals(1, repo.getPulsePolioCampaignFromServer())
        coVerify(exactly = 0) { dbDao.saveRecord(any<PulsePolioCampaignCache>()) }
    }

    @Test
    fun `getFilariaMdaCampaignFromServer refreshes token then retries and terminates on second call`() = runTest {
        loggedIn()
        coEvery { api.getFilariaMdaCampaign() } returnsMany listOf(
            jsonResponse("""{"statusCode":5002}"""),
            jsonResponse("{}", code = 500)
        )
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-1, repo.getFilariaMdaCampaignFromServer())

        coVerify(exactly = 2) { api.getFilariaMdaCampaign() }
    }

    @Test
    fun `getFilariaMdaCampaignFromServer stores a stringified single json object`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":"{\"id\":24,\"fields\":{\"start_date\":\"01-05-2026\"}}"}"""
        coEvery { api.getFilariaMdaCampaign() } returns jsonResponse(json)
        coEvery { dbDao.getFilariaMdaCampaign(24) } returns null

        assertEquals(1, repo.getFilariaMdaCampaignFromServer())
        coVerify { dbDao.saveRecord(any<FilariaMDACampaignFormResponseJsonEntity>()) }
    }

    @Test
    fun `savefilariaMdaCampaignToServer sets empty visitYear when start_date is unparsable`() = runTest {
        coEvery { dbDao.getFilariaMdaCampaign(50) } returns null
        val slot = slot<FilariaMDACampaignFormResponseJsonEntity>()
        coEvery { dbDao.saveRecord(capture(slot)) } just Runs

        repo.savefilariaMdaCampaignToServer("""[{"id":50,"fields":{"start_date":"not-a-date"}}]""")

        assertEquals("", slot.captured.visitYear)
    }

    @Test
    fun `isFormFilledForCurrentMonth reports true when dates fall in the current month`() = runTest {
        val today = java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy", java.util.Locale.ENGLISH)
        )
        val vhnd = mockk<VHNDCache>(relaxed = true)
        every { vhnd.vhndDate } returns today
        val vhnc = mockk<VHNCCache>(relaxed = true)
        every { vhnc.vhncDate } returns today
        val phc = mockk<PHCReviewMeetingCache>(relaxed = true)
        every { phc.phcReviewDate } returns today
        val ahd = mockk<AHDCache>(relaxed = true)
        every { ahd.ahdDate } returns today

        every { vlfDao.getAllVHND() } returns flowOf(listOf(vhnd))
        every { vlfDao.getAllVHNC() } returns flowOf(listOf(vhnc))
        every { vlfDao.getAllPHC() } returns flowOf(listOf(phc))
        every { vlfDao.getAllAHD() } returns flowOf(listOf(ahd))
        every { vlfDao.countDewormingInLastSixMonths() } returns flowOf(2)

        val result = repo.isFormFilledForCurrentMonth().first()

        assertTrue(result["VHND"]!!)
        assertTrue(result["VHNC"]!!)
        assertTrue(result["PHC"]!!)
        assertTrue(result["AHD"]!!)
        assertTrue(result["DEWORMING"]!!)
    }

    @Test
    fun `isFormFilledForCurrentMonth reports false when dates are unparsable`() = runTest {
        val vhnd = mockk<VHNDCache>(relaxed = true)
        every { vhnd.vhndDate } returns "not-a-date"
        every { vlfDao.getAllVHND() } returns flowOf(listOf(vhnd))
        every { vlfDao.getAllVHNC() } returns flowOf(emptyList())
        every { vlfDao.getAllPHC() } returns flowOf(emptyList())
        every { vlfDao.getAllAHD() } returns flowOf(emptyList())
        every { vlfDao.countDewormingInLastSixMonths() } returns flowOf(0)

        val result = repo.isFormFilledForCurrentMonth().first()

        assertFalse(result["VHND"]!!)
    }

    // =====================================================
    // saveMdaFilariaCampaignToServer photo handling
    // =====================================================

    private fun mdaFormData(photos: Any): String =
        JSONObject().put("fields", JSONObject().put("mda_photos", photos)).toString()

    private fun mdaCache(formDataJson: String): FilariaMDACampaignFormResponseJsonEntity {
        val cache = mockk<FilariaMDACampaignFormResponseJsonEntity>(relaxed = true)
        every { cache.formDataJson } returns formDataJson
        return cache
    }

    private fun tempPhoto(): File {
        val file = File.createTempFile("mda_test_photo_", ".jpg")
        file.writeBytes(byteArrayOf(1, 2, 3))
        file.deleteOnExit()
        return file
    }

    @Test
    fun `saveMdaFilariaCampaignToServer attaches photos listed in an encoded json array`() =
        runTest {
            val file = tempPhoto()
            val cache = mdaCache(mdaFormData(JSONArray().put(file.absolutePath).toString()))
            coEvery { api.saveFilariaMdaCampaign(any()) } returns uploadResponse(true)

            assertTrue(repo.saveMdaFilariaCampaignToServer(cache))

            coVerify { api.saveFilariaMdaCampaign(match { it.size == 2 }) }
            coVerify { dbDao.saveRecord(cache) }
        }

    @Test
    fun `saveMdaFilariaCampaignToServer attaches photos listed in a json array field`() = runTest {
        val file = tempPhoto()
        val cache = mdaCache(mdaFormData(JSONArray().put(file.absolutePath)))
        coEvery { api.saveFilariaMdaCampaign(any()) } returns uploadResponse(true)

        assertTrue(repo.saveMdaFilariaCampaignToServer(cache))

        coVerify { api.saveFilariaMdaCampaign(match { it.size == 2 }) }
    }

    @Test
    fun `saveMdaFilariaCampaignToServer skips a photo path that does not exist`() = runTest {
        val cache = mdaCache(mdaFormData("no-such-photo.jpg"))
        coEvery { api.saveFilariaMdaCampaign(any()) } returns uploadResponse(true)

        assertTrue(repo.saveMdaFilariaCampaignToServer(cache))

        coVerify { api.saveFilariaMdaCampaign(match { it.size == 1 }) }
    }

    @Test
    fun `saveMdaFilariaCampaignToServer skips a data uri it cannot decode`() = runTest {
        val cache = mdaCache(mdaFormData("data:image/jpeg;base64,QUJD"))
        coEvery { api.saveFilariaMdaCampaign(any()) } returns uploadResponse(true)

        assertTrue(repo.saveMdaFilariaCampaignToServer(cache))

        coVerify { api.saveFilariaMdaCampaign(match { it.size == 1 }) }
    }

    @Test
    fun `saveMdaFilariaCampaignToServer skips a content uri it cannot resolve`() = runTest {
        val cache = mdaCache(mdaFormData("content://media/external/images/1"))
        coEvery { api.saveFilariaMdaCampaign(any()) } returns uploadResponse(true)

        assertTrue(repo.saveMdaFilariaCampaignToServer(cache))

        coVerify { api.saveFilariaMdaCampaign(match { it.size == 1 }) }
    }

    @Test
    fun `saveMdaFilariaCampaignToServer sends only the form when there are no photos`() = runTest {
        val cache = mdaCache("""{"fields":{}}""")
        coEvery { api.saveFilariaMdaCampaign(any()) } returns uploadResponse(true)

        assertTrue(repo.saveMdaFilariaCampaignToServer(cache))

        coVerify { api.saveFilariaMdaCampaign(match { it.size == 1 }) }
    }

    @Test
    fun `saveMdaFilariaCampaignToServer sends only the form when fields are absent`() = runTest {
        val cache = mdaCache("""{"other":1}""")
        coEvery { api.saveFilariaMdaCampaign(any()) } returns uploadResponse(true)

        assertTrue(repo.saveMdaFilariaCampaignToServer(cache))

        coVerify { api.saveFilariaMdaCampaign(match { it.size == 1 }) }
    }

    @Test
    fun `saveMdaFilariaCampaignToServer returns false when the upload throws`() = runTest {
        val cache = mdaCache("")
        coEvery { api.saveFilariaMdaCampaign(any()) } throws RuntimeException("network down")

        assertFalse(repo.saveMdaFilariaCampaignToServer(cache))

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    // =====================================================
    // ORS / Pulse Polio upload photo handling
    // =====================================================

    private fun campaignFormData(photos: Any, key: String = "campaign_photos"): String =
        JSONObject().put("fields", JSONObject().put(key, photos)).toString()

    private fun orsCache(formDataJson: String?): ORSCampaignCache =
        ORSCampaignCache(id = 500, formDataJson = formDataJson, syncState = SyncState.UNSYNCED)

    private fun ppCache(formDataJson: String?): PulsePolioCampaignCache =
        PulsePolioCampaignCache(id = 501, formDataJson = formDataJson, syncState = SyncState.UNSYNCED)

    @Test
    fun `saveORSCampaignToServer attaches photos listed in an encoded json array`() = runTest {
        val file = tempPhoto()
        coEvery { api.saveORSCampaignData(any()) } returns uploadResponse(true)

        assertTrue(
            repo.saveORSCampaignToServer(
                orsCache(campaignFormData(JSONArray().put(file.absolutePath).toString()))
            )
        )

        coVerify { api.saveORSCampaignData(match { it.size == 2 }) }
    }

    @Test
    fun `saveORSCampaignToServer attaches photos listed in a json array field`() = runTest {
        val file = tempPhoto()
        coEvery { api.saveORSCampaignData(any()) } returns uploadResponse(true)

        assertTrue(
            repo.saveORSCampaignToServer(orsCache(campaignFormData(JSONArray().put(file.absolutePath))))
        )

        coVerify { api.saveORSCampaignData(match { it.size == 2 }) }
    }

    @Test
    fun `saveORSCampaignToServer reads photos from the camelCase field name`() = runTest {
        val file = tempPhoto()
        coEvery { api.saveORSCampaignData(any()) } returns uploadResponse(true)

        assertTrue(
            repo.saveORSCampaignToServer(
                orsCache(campaignFormData(JSONArray().put(file.absolutePath), key = "campaignPhotos"))
            )
        )

        coVerify { api.saveORSCampaignData(match { it.size == 2 }) }
    }

    @Test
    fun `saveORSCampaignToServer skips a photo string that is not a json array`() = runTest {
        coEvery { api.saveORSCampaignData(any()) } returns uploadResponse(true)

        assertTrue(repo.saveORSCampaignToServer(orsCache(campaignFormData("no-such-photo.jpg"))))

        coVerify { api.saveORSCampaignData(match { it.size == 1 }) }
    }

    @Test
    fun `saveORSCampaignToServer skips a data uri it cannot decode`() = runTest {
        coEvery { api.saveORSCampaignData(any()) } returns uploadResponse(true)

        assertTrue(
            repo.saveORSCampaignToServer(orsCache(campaignFormData("data:image/jpeg;base64,QUJD")))
        )

        coVerify { api.saveORSCampaignData(match { it.size == 1 }) }
    }

    @Test
    fun `saveORSCampaignToServer skips a content uri it cannot resolve`() = runTest {
        coEvery { api.saveORSCampaignData(any()) } returns uploadResponse(true)

        assertTrue(
            repo.saveORSCampaignToServer(
                orsCache(campaignFormData("content://media/external/images/9"))
            )
        )

        coVerify { api.saveORSCampaignData(match { it.size == 1 }) }
    }

    @Test
    fun `saveORSCampaignToServer sends only the form when fields are absent`() = runTest {
        coEvery { api.saveORSCampaignData(any()) } returns uploadResponse(true)

        assertTrue(repo.saveORSCampaignToServer(orsCache("""{"other":1}""")))

        coVerify { api.saveORSCampaignData(match { it.size == 1 }) }
    }

    @Test
    fun `saveORSCampaignToServer sends only the form when the payload is not json`() = runTest {
        coEvery { api.saveORSCampaignData(any()) } returns uploadResponse(true)

        assertTrue(repo.saveORSCampaignToServer(orsCache("not-json-at-all")))

        coVerify { api.saveORSCampaignData(match { it.size == 1 }) }
    }

    @Test
    fun `saveORSCampaignToServer returns false when the upload throws`() = runTest {
        val cache = orsCache(null)
        coEvery { api.saveORSCampaignData(any()) } throws RuntimeException("network down")

        assertFalse(repo.saveORSCampaignToServer(cache))

        assertEquals(SyncState.UNSYNCED, cache.syncState)
        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `savePulsePolioCampaignToServer attaches photos listed in an encoded json array`() = runTest {
        val file = tempPhoto()
        coEvery { api.savePulsePolioCampaignData(any()) } returns uploadResponse(true)

        assertTrue(
            repo.savePulsePolioCampaignToServer(
                ppCache(campaignFormData(JSONArray().put(file.absolutePath).toString()))
            )
        )

        coVerify { api.savePulsePolioCampaignData(match { it.size == 2 }) }
    }

    @Test
    fun `savePulsePolioCampaignToServer attaches photos listed in a json array field`() = runTest {
        val file = tempPhoto()
        coEvery { api.savePulsePolioCampaignData(any()) } returns uploadResponse(true)

        assertTrue(
            repo.savePulsePolioCampaignToServer(
                ppCache(campaignFormData(JSONArray().put(file.absolutePath)))
            )
        )

        coVerify { api.savePulsePolioCampaignData(match { it.size == 2 }) }
    }

    @Test
    fun `savePulsePolioCampaignToServer reads photos from the camelCase field name`() = runTest {
        val file = tempPhoto()
        coEvery { api.savePulsePolioCampaignData(any()) } returns uploadResponse(true)

        assertTrue(
            repo.savePulsePolioCampaignToServer(
                ppCache(campaignFormData(JSONArray().put(file.absolutePath), key = "campaignPhotos"))
            )
        )

        coVerify { api.savePulsePolioCampaignData(match { it.size == 2 }) }
    }

    @Test
    fun `savePulsePolioCampaignToServer skips a photo string that is not a json array`() = runTest {
        coEvery { api.savePulsePolioCampaignData(any()) } returns uploadResponse(true)

        assertTrue(
            repo.savePulsePolioCampaignToServer(ppCache(campaignFormData("no-such-photo.jpg")))
        )

        coVerify { api.savePulsePolioCampaignData(match { it.size == 1 }) }
    }

    @Test
    fun `savePulsePolioCampaignToServer skips a data uri it cannot decode`() = runTest {
        coEvery { api.savePulsePolioCampaignData(any()) } returns uploadResponse(true)

        assertTrue(
            repo.savePulsePolioCampaignToServer(
                ppCache(campaignFormData("data:image/jpeg;base64,QUJD"))
            )
        )

        coVerify { api.savePulsePolioCampaignData(match { it.size == 1 }) }
    }

    @Test
    fun `savePulsePolioCampaignToServer skips a content uri it cannot resolve`() = runTest {
        coEvery { api.savePulsePolioCampaignData(any()) } returns uploadResponse(true)

        assertTrue(
            repo.savePulsePolioCampaignToServer(
                ppCache(campaignFormData("content://media/external/images/8"))
            )
        )

        coVerify { api.savePulsePolioCampaignData(match { it.size == 1 }) }
    }

    @Test
    fun `savePulsePolioCampaignToServer sends only the form when fields are absent`() = runTest {
        coEvery { api.savePulsePolioCampaignData(any()) } returns uploadResponse(true)

        assertTrue(repo.savePulsePolioCampaignToServer(ppCache("""{"other":1}""")))

        coVerify { api.savePulsePolioCampaignData(match { it.size == 1 }) }
    }

    @Test
    fun `savePulsePolioCampaignToServer returns false when the upload throws`() = runTest {
        val cache = ppCache(null)
        coEvery { api.savePulsePolioCampaignData(any()) } throws RuntimeException("network down")

        assertFalse(repo.savePulsePolioCampaignToServer(cache))

        assertEquals(SyncState.UNSYNCED, cache.syncState)
        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    // =====================================================
    // campaign pull: unexpected data shapes and null bodies
    // =====================================================

    private fun nullBodyResponse(code: Int = 200): Response<ResponseBody> {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns null
        return response
    }

    @Test
    fun `getORSCampaignFromServer returns 0 when the data node is absent`() = runTest {
        loggedIn()
        coEvery { api.getORSCampaignData() } returns jsonResponse("""{"statusCode":200}""")

        assertEquals(0, repo.getORSCampaignFromServer())

        coVerify(exactly = 0) { dbDao.saveRecord(any<ORSCampaignCache>()) }
    }

    @Test
    fun `getORSCampaignFromServer returns 0 when the data node is a number`() = runTest {
        loggedIn()
        coEvery { api.getORSCampaignData() } returns jsonResponse("""{"statusCode":200,"data":7}""")

        assertEquals(0, repo.getORSCampaignFromServer())
    }

    @Test
    fun `getORSCampaignFromServer returns -1 when the body is null`() = runTest {
        loggedIn()
        coEvery { api.getORSCampaignData() } returns nullBodyResponse()

        assertEquals(-1, repo.getORSCampaignFromServer())
    }

    @Test
    fun `getPulsePolioCampaignFromServer returns 0 when the data node is absent`() = runTest {
        loggedIn()
        coEvery { api.getPulsePolioCampaignData() } returns jsonResponse("""{"statusCode":200}""")

        assertEquals(0, repo.getPulsePolioCampaignFromServer())
    }

    @Test
    fun `getPulsePolioCampaignFromServer returns 0 when the data node is a number`() = runTest {
        loggedIn()
        coEvery { api.getPulsePolioCampaignData() } returns
                jsonResponse("""{"statusCode":200,"data":7}""")

        assertEquals(0, repo.getPulsePolioCampaignFromServer())
    }

    @Test
    fun `getPulsePolioCampaignFromServer returns -1 on unexpected status`() = runTest {
        loggedIn()
        coEvery { api.getPulsePolioCampaignData() } returns jsonResponse("""{"statusCode":9999}""")

        assertEquals(-1, repo.getPulsePolioCampaignFromServer())
    }

    @Test
    fun `getPulsePolioCampaignFromServer returns -1 when the body is null`() = runTest {
        loggedIn()
        coEvery { api.getPulsePolioCampaignData() } returns nullBodyResponse()

        assertEquals(-1, repo.getPulsePolioCampaignFromServer())
    }

    @Test
    fun `getFilariaMdaCampaignFromServer returns 0 when the data node is absent`() = runTest {
        loggedIn()
        coEvery { api.getFilariaMdaCampaign() } returns jsonResponse("""{"statusCode":200}""")

        assertEquals(0, repo.getFilariaMdaCampaignFromServer())
    }

    @Test
    fun `getFilariaMdaCampaignFromServer returns 0 when the data node is a number`() = runTest {
        loggedIn()
        coEvery { api.getFilariaMdaCampaign() } returns
                jsonResponse("""{"statusCode":200,"data":7}""")

        assertEquals(0, repo.getFilariaMdaCampaignFromServer())
    }

    @Test
    fun `getFilariaMdaCampaignFromServer saves nothing for a stringified primitive`() = runTest {
        loggedIn()
        coEvery { api.getFilariaMdaCampaign() } returns
                jsonResponse("""{"statusCode":200,"data":"5"}""")

        assertEquals(1, repo.getFilariaMdaCampaignFromServer())

        coVerify(exactly = 0) { dbDao.saveRecord(any<FilariaMDACampaignFormResponseJsonEntity>()) }
    }

    @Test
    fun `getFilariaMdaCampaignFromServer returns 0 when the data string is not json`() = runTest {
        loggedIn()
        coEvery { api.getFilariaMdaCampaign() } returns
                jsonResponse("""{"statusCode":200,"data":"{oops"}""")

        assertEquals(0, repo.getFilariaMdaCampaignFromServer())
    }

    @Test
    fun `getFilariaMdaCampaignFromServer returns -1 when the body is null`() = runTest {
        loggedIn()
        coEvery { api.getFilariaMdaCampaign() } returns nullBodyResponse()

        assertEquals(-1, repo.getFilariaMdaCampaignFromServer())
    }

    // =====================================================
    // saveORSCampaignFromServer / savePulsePolioCampaignFromServer skip branches
    // =====================================================

    @Test
    fun `getORSCampaignFromServer skips an entry whose id is zero`() = runTest {
        loggedIn()
        coEvery { api.getORSCampaignData() } returns
                jsonResponse("""{"statusCode":200,"data":[{"id":0,"fields":{"a":1}}]}""")

        assertEquals(1, repo.getORSCampaignFromServer())

        coVerify(exactly = 0) { dbDao.saveRecord(any<ORSCampaignCache>()) }
    }

    @Test
    fun `getORSCampaignFromServer skips an entry with neither fields nor formDataJson`() = runTest {
        loggedIn()
        coEvery { api.getORSCampaignData() } returns
                jsonResponse("""{"statusCode":200,"data":[{"id":61}]}""")

        assertEquals(1, repo.getORSCampaignFromServer())

        coVerify(exactly = 0) { dbDao.saveRecord(any<ORSCampaignCache>()) }
    }

    @Test
    fun `getORSCampaignFromServer keeps going when one entry is not an object`() = runTest {
        loggedIn()
        coEvery { api.getORSCampaignData() } returns
                jsonResponse("""{"statusCode":200,"data":[5,{"id":62,"fields":{"a":1}}]}""")
        coEvery { dbDao.getORSCampaign(62) } returns null

        assertEquals(1, repo.getORSCampaignFromServer())

        coVerify(exactly = 1) { dbDao.saveRecord(any<ORSCampaignCache>()) }
    }

    @Test
    fun `getPulsePolioCampaignFromServer skips an entry whose id is zero`() = runTest {
        loggedIn()
        coEvery { api.getPulsePolioCampaignData() } returns
                jsonResponse("""{"statusCode":200,"data":[{"id":0,"fields":{"a":1}}]}""")

        assertEquals(1, repo.getPulsePolioCampaignFromServer())

        coVerify(exactly = 0) { dbDao.saveRecord(any<PulsePolioCampaignCache>()) }
    }

    @Test
    fun `getPulsePolioCampaignFromServer skips an entry with no fields node`() = runTest {
        loggedIn()
        coEvery { api.getPulsePolioCampaignData() } returns
                jsonResponse("""{"statusCode":200,"data":[{"id":63}]}""")

        assertEquals(1, repo.getPulsePolioCampaignFromServer())

        coVerify(exactly = 0) { dbDao.saveRecord(any<PulsePolioCampaignCache>()) }
    }

    @Test
    fun `getPulsePolioCampaignFromServer keeps going when one entry is not an object`() = runTest {
        loggedIn()
        coEvery { api.getPulsePolioCampaignData() } returns
                jsonResponse("""{"statusCode":200,"data":[5,{"id":64,"fields":{"a":1}}]}""")
        coEvery { dbDao.getPulsePolioCampaign(64) } returns null

        assertEquals(1, repo.getPulsePolioCampaignFromServer())

        coVerify(exactly = 1) { dbDao.saveRecord(any<PulsePolioCampaignCache>()) }
    }

    // =====================================================
    // remaining push chunk branches
    // =====================================================

    @Test
    fun `pushUnSyncedRecords catches an exception during a VHND chunk push`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<VHNDCache>(relaxed = true)
        every { dbDao.getVHND(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveVHNDData(any()) } throws RuntimeException("network down")

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords skips VHND chunk update when response body is null`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<VHNDCache>(relaxed = true)
        every { dbDao.getVHND(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveVHNDData(any()) } returns nullBodyResponse()

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords skips PHC chunk update when response body is null`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<PHCReviewMeetingCache>(relaxed = true)
        every { dbDao.getPHC(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.savePHCData(any()) } returns nullBodyResponse()

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords skips Deworming chunk update when response body is null`() = runTest {
        loggedIn()
        stubNothingUnsynced()
        val cache = mockk<DewormingCache>(relaxed = true)
        coEvery { dbDao.getDeworming(SyncState.UNSYNCED) } returns listOf(cache)
        coEvery { api.saveDewormingData(any()) } returns nullBodyResponse()

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { dbDao.saveRecord(cache) }
    }

    @Test
    fun `pushUnSyncedRecords returns true when every unsynced list is null`() = runTest {
        loggedIn()
        every { dbDao.getVHND(SyncState.UNSYNCED) } returns null
        every { dbDao.getVHNC(SyncState.UNSYNCED) } returns null
        every { dbDao.getPHC(SyncState.UNSYNCED) } returns null
        coEvery { dbDao.getAHD(SyncState.UNSYNCED) } returns null
        coEvery { dbDao.getDeworming(SyncState.UNSYNCED) } returns null

        assertTrue(repo.pushUnSyncedRecords())

        coVerify(exactly = 0) { api.saveVHNDData(any()) }
    }

    // =====================================================
    // exposed list flows map dao rows through their converters
    // =====================================================

    @Test
    fun `exposed list flows map every dao row`() = runTest {
        every { vlfDao.getAllVHND() } returns flowOf(listOf(mockk<VHNDCache>(relaxed = true)))
        every { vlfDao.getAllVHNC() } returns flowOf(listOf(mockk<VHNCCache>(relaxed = true)))
        every { vlfDao.getAllPHC() } returns
                flowOf(listOf(mockk<PHCReviewMeetingCache>(relaxed = true)))
        every { vlfDao.getAllAHD() } returns flowOf(listOf(mockk<AHDCache>(relaxed = true)))
        every { vlfDao.getAllDeworming() } returns
                flowOf(listOf(mockk<DewormingCache>(relaxed = true)))
        every { vlfDao.getAllPulsePolioCampaign() } returns
                flowOf(listOf(mockk<PulsePolioCampaignCache>(relaxed = true)))
        every { vlfDao.getAllORSCampaign() } returns
                flowOf(listOf(mockk<ORSCampaignCache>(relaxed = true)))

        val fresh = VLFRepo(database, userRepo, preferenceDao, api, vlfDao, appContext)

        assertEquals(1, fresh.vhndList.first().size)
        assertEquals(1, fresh.vhncList.first().size)
        assertEquals(1, fresh.phcList.first().size)
        assertEquals(1, fresh.ahdList.first().size)
        assertEquals(1, fresh.dewormingList.first().size)
        assertEquals(1, fresh.pulsePolioCampaignList.first().size)
        assertEquals(1, fresh.orsCampaignList.first().size)
    }
}
