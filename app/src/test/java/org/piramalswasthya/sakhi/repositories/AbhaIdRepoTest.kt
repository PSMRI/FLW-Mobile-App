package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ABHAModel
import org.piramalswasthya.sakhi.network.AbhaApiService
import org.piramalswasthya.sakhi.network.AddHealthIdRecord
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.MapHIDtoBeneficiary
import org.piramalswasthya.sakhi.network.NetworkResult
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AbhaIdRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var abhaApiService: AbhaApiService
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var abhaGenerated: ABHAGenratedRepo
    @MockK private lateinit var prefDao: PreferenceDao

    private lateinit var repo: AbhaIdRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = AbhaIdRepo(abhaApiService, amritApiService, userRepo, abhaGenerated, prefDao)
    }

    // ---------------- parseAbhaErrorString ----------------

    @Test
    fun `parseAbhaErrorString returns null for null input`() {
        assertNull(repo.parseAbhaErrorString(null))
    }

    @Test
    fun `parseAbhaErrorString returns null for empty input`() {
        assertNull(repo.parseAbhaErrorString(""))
    }

    @Test
    fun `parseAbhaErrorString extracts code and message`() {
        val json = """{"error":{"code":"ABDM-1114","message":"User not found."}}"""

        val result = repo.parseAbhaErrorString(json)

        assertEquals("ABDM-1114", result?.first)
        assertEquals("User not found.", result?.second)
    }

    @Test
    fun `parseAbhaErrorString returns empty pair when error object missing`() {
        val result = repo.parseAbhaErrorString("""{"foo":"bar"}""")

        assertEquals("", result?.first)
        assertEquals("", result?.second)
    }

    @Test
    fun `parseAbhaErrorString returns null for malformed json`() {
        assertNull(repo.parseAbhaErrorString("not json at all"))
    }

    // ---------------- downloadPdfCard ----------------

    @Test
    fun `downloadPdfCard delegates to abha api`() = runTest {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        coEvery { abhaApiService.getPdfCard() } returns response

        assertEquals(response, repo.downloadPdfCard())
    }

    // ---------------- no-user paths ----------------

    @Test
    fun `mapHealthIDToBeneficiary returns error when no user logged in`() = runTest {
        every { prefDao.getLoggedInUser() } returns null
        val req = mockk<MapHIDtoBeneficiary>(relaxed = true)

        val result = repo.mapHealthIDToBeneficiary(req, null)

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
    }

    @Test
    fun `addHealthIdRecord returns error when no user logged in`() = runTest {
        every { prefDao.getLoggedInUser() } returns null
        val req = mockk<AddHealthIdRecord>(relaxed = true)

        val result = repo.addHealthIdRecord(req)

        assertTrue(result is NetworkResult.Error)
        assertEquals(-4, (result as NetworkResult.Error).code)
    }

    // ---------------- saveAbhaModelFromRequest ----------------

    @Test
    fun `saveAbhaModelFromRequest does nothing when beneficiaryId missing`() = runTest {
        val req = mockk<MapHIDtoBeneficiary>(relaxed = true)
        every { req.beneficiaryID } returns null

        repo.saveAbhaModelFromRequest(req, null)

        coVerify(exactly = 0) { abhaGenerated.saveAbhaGenrated(any<ABHAModel>()) }
    }
}
