package org.piramalswasthya.sakhi.repositories

import android.app.Application
import android.net.Uri
import android.widget.Toast
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.BeneficiaryIdsAvail
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.BeneficiaryIdsAvailDao
import org.piramalswasthya.sakhi.database.room.dao.GeneralOpdDao
import org.piramalswasthya.sakhi.database.room.dao.HouseholdDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.CUFYFormResponseJsonDao
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FormResponseJsonDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.ImageUtils
import org.piramalswasthya.sakhi.model.AgeUnit
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenWithHRPTrackingCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.HouseholdNetwork
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.model.BeneficiaryDataSending
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.NetworkResult
import org.piramalswasthya.sakhi.work.WorkerUtils
import retrofit2.Response
import java.io.File
import java.io.IOException

/**
 * Unit tests for [BenRepo]. Consolidated from BenRepoTest + Extra/Extra2/Extra3/
 * Extra4: getters and companion date helpers, the family-relation update
 * delegations, death-check / child-count getters, no-user guards, ben-id draft
 * substitution, the paginated server-pull response-code branches (worker /
 * general-OPD / beneficiaries), persistRecord no-image path, and the OTP /
 * Ayushman network helper short-circuits.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BenRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Application
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var householdDao: HouseholdDao
    @MockK private lateinit var benIdGenDao: BeneficiaryIdsAvailDao
    @MockK private lateinit var infantRegRepo: InfantRegRepo
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var generalOpdDao: GeneralOpdDao
    @MockK private lateinit var tmcNetworkApiService: AmritApiService
    @MockK private lateinit var formResponseJsonDao: FormResponseJsonDao
    @MockK private lateinit var cufyFormResponseJsonDao: CUFYFormResponseJsonDao

    private lateinit var repo: BenRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = BenRepo(
            context, benDao, householdDao, benIdGenDao, infantRegRepo,
            preferenceDao, userRepo, generalOpdDao, tmcNetworkApiService,
            formResponseJsonDao, cufyFormResponseJsonDao
        )
    }

    private fun loggedIn(userId: Int = 42) {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns userId
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { user.villages } returns listOf(LocationEntity(1, "Village"))
        every { preferenceDao.getLoggedInUser() } returns user
    }

    /** Response whose code()/body() are stubbed; isSuccessful left as relaxed-default (false). */
    private fun jsonResponse(body: String, code: Int = 200): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns body
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns code
        every { response.body() } returns responseBody
        return response
    }

    /** Response with isSuccessful == true and a stubbed body string. */
    private fun successResponse(body: String, code: Int = 200): Response<ResponseBody> {
        val responseBody = mockk<ResponseBody>(relaxed = true)
        every { responseBody.string() } returns body
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        every { response.code() } returns code
        every { response.body() } returns responseBody
        return response
    }

    private suspend fun assertNoUser(block: suspend () -> Unit) {
        try {
            block()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    // =====================================================
    // updateBenToSync() Tests
    // =====================================================

    @Test
    fun `updateBenToSync calls dao with correct params`() = runTest {
        coEvery { benDao.updateBenToSync(any(), any(), any(), any()) } returns Unit

        repo.updateBenToSync(100L, SyncState.UNSYNCED)

        coVerify { benDao.updateBenToSync(100L, SyncState.UNSYNCED, "U", 2) }
    }

    // =====================================================
    // updateHousehold() Tests
    // =====================================================

    @Test
    fun `updateHousehold calls dao with correct params`() = runTest {
        coEvery { benDao.updateHofSpouseAdded(any(), any(), any(), any()) } returns Unit

        repo.updateHousehold(200L, SyncState.UNSYNCED)

        coVerify { benDao.updateHofSpouseAdded(200L, SyncState.UNSYNCED, "U", 2) }
    }

    // =====================================================
    // getHousehold() Tests
    // =====================================================

    @Test
    fun `getHousehold returns record when exists`() = runTest {
        val household = mockk<HouseholdCache>()
        coEvery { householdDao.getHousehold(100L) } returns household

        val result = repo.getHousehold(100L)

        assertNotNull(result)
        assertEquals(household, result)
    }

    @Test
    fun `getHousehold returns null when not exists`() = runTest {
        coEvery { householdDao.getHousehold(999L) } returns null

        val result = repo.getHousehold(999L)

        assertNull(result)
    }

    // =====================================================
    // getBenFromId() Tests
    // =====================================================

    @Test
    fun `getBenFromId returns ben when exists`() = runTest {
        val ben = mockk<BenRegCache>()
        coEvery { benDao.getBen(100L) } returns ben

        val result = repo.getBenFromId(100L)

        assertNotNull(result)
        assertEquals(ben, result)
    }

    @Test
    fun `getBenFromId returns null when not exists`() = runTest {
        coEvery { benDao.getBen(999L) } returns null

        val result = repo.getBenFromId(999L)

        assertNull(result)
    }

    // =====================================================
    // getBeneficiaryRecord() Tests
    // =====================================================

    @Test
    fun `getBeneficiaryRecord returns record when exists`() = runTest {
        val ben = mockk<BenRegCache>()
        coEvery { benDao.getBen(100L, 200L) } returns ben

        val result = repo.getBeneficiaryRecord(100L, 200L)

        assertNotNull(result)
    }

    @Test
    fun `getBeneficiaryRecord returns null when not exists`() = runTest {
        coEvery { benDao.getBen(999L, 999L) } returns null

        val result = repo.getBeneficiaryRecord(999L, 999L)

        assertNull(result)
    }

    // =====================================================
    // Companion Object Tests
    // =====================================================

    @Test
    fun `getCurrentDate returns formatted date string`() {
        val result = BenRepo.getCurrentDate(1577817001000L)

        assertTrue(result.contains("T"))
        assertTrue(result.endsWith(".000Z"))
    }

    @Test
    fun `getLongFromDateStr parses valid date`() {
        val dateStr = "2026-01-15T10:30:00.000Z"
        val result = BenRepo.getLongFromDateStr(dateStr)

        assertTrue(result > 0)
    }

    @Test
    fun `getLongFromDateStr throws for invalid date`() {
        try {
            BenRepo.getLongFromDateStr("not-a-date")
            assert(false) { "Should have thrown" }
        } catch (e: Exception) {
            // Expected
        }
    }

    // =====================================================
    // processNewBen() Tests
    // =====================================================

    @Test
    fun `processNewBen throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewBen()
            assert(false) { "Should have thrown IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    // ---------------- family-relation update delegations ----------------

    @Test
    fun `updateBeneficiarySpouseAdded delegates to dao`() = runTest {
        repo.updateBeneficiarySpouseAdded(10L, 20L, SyncState.UNSYNCED)
        coVerify { benDao.updateBeneficiarySpouseAdded(10L, 20L, SyncState.UNSYNCED, "U", 2) }
    }

    @Test
    fun `updateBeneficiaryChildrenAdded delegates to dao`() = runTest {
        repo.updateBeneficiaryChildrenAdded(10L, 20L, SyncState.UNSYNCED)
        coVerify { benDao.updateBeneficiaryChildrenAdded(10L, 20L, SyncState.UNSYNCED, "U", 2) }
    }

    @Test
    fun `updateFatherInChildren delegates to dao`() = runTest {
        repo.updateFatherInChildren("Ben", 5L, "Parent", SyncState.UNSYNCED)
        coVerify { benDao.updateFatherInChildren("Ben", 5L, "Parent", SyncState.UNSYNCED, "U", 2) }
    }

    @Test
    fun `updateMotherInChildren delegates to dao`() = runTest {
        repo.updateMotherInChildren("Ben", 5L, "Parent", SyncState.UNSYNCED)
        coVerify { benDao.updateMotherInChildren("Ben", 5L, "Parent", SyncState.UNSYNCED, "U", 2) }
    }

    @Test
    fun `updateSpouseOfHoF delegates to dao`() = runTest {
        repo.updateSpouseOfHoF("Ben", 5L, "Spouse", SyncState.UNSYNCED)
        coVerify { benDao.updateSpouseOfHoF("Ben", 5L, "Spouse", SyncState.UNSYNCED, "U", 2) }
    }

    @Test
    fun `updateFather delegates to dao`() = runTest {
        repo.updateFather("Ben", 5L, "Parent", SyncState.UNSYNCED)
        coVerify { benDao.updateFather("Ben", 5L, "Parent", SyncState.UNSYNCED, "U", 2) }
    }

    @Test
    fun `updateMother delegates to dao`() = runTest {
        repo.updateMother("Ben", 5L, "Parent", SyncState.UNSYNCED)
        coVerify { benDao.updateMother("Ben", 5L, "Parent", SyncState.UNSYNCED, "U", 2) }
    }

    @Test
    fun `updateMarriageAgeOfWife delegates to dao`() = runTest {
        repo.updateMarriageAgeOfWife(123L, 20, 5L, "Spouse", SyncState.UNSYNCED)
        coVerify { benDao.updateMarriageAgeOfWife(123L, 20, 5L, "Spouse", SyncState.UNSYNCED, "U", 2) }
    }

    @Test
    fun `updateMarriageAgeOfHusband delegates to dao`() = runTest {
        repo.updateMarriageAgeOfHusband(123L, 25, 5L, "Spouse", SyncState.UNSYNCED)
        coVerify { benDao.updateMarriageAgeOfHusband(123L, 25, 5L, "Spouse", SyncState.UNSYNCED, "U", 2) }
    }

    @Test
    fun `updateBabyName delegates to dao`() = runTest {
        repo.updateBabyName("Baby", 5L, "Parent", SyncState.UNSYNCED)
        coVerify { benDao.updateBabyName("Baby", 5L, "Parent", SyncState.UNSYNCED, "U", 2) }
    }

    @Test
    fun `updateSpouse delegates to dao`() = runTest {
        repo.updateSpouse("Ben", 5L, "Spouse", SyncState.UNSYNCED)
        coVerify { benDao.updateSpouse("Ben", 5L, "Spouse", SyncState.UNSYNCED, "U", 2) }
    }

    @Test
    fun `updateChildrenLastName delegates to dao`() = runTest {
        repo.updateChildrenLastName("Last", 5L, "Parent", SyncState.UNSYNCED)
        coVerify { benDao.updateChildrenLastName("Last", 5L, "Parent", SyncState.UNSYNCED, "U", 2) }
    }

    @Test
    fun `updateSpouseLastName delegates to dao`() = runTest {
        repo.updateSpouseLastName("Last", 5L, "Spouse", SyncState.UNSYNCED)
        coVerify { benDao.updateSpouseLastName("Last", 5L, "Spouse", SyncState.UNSYNCED, "U", 2) }
    }

    @Test
    fun `updateRecord delegates to dao updateBen`() = runTest {
        val ben = mockk<BenRegCache>(relaxed = true)
        repo.updateRecord(ben)
        coVerify { benDao.updateBen(ben) }
    }

    // ---------------- death-check getters ----------------

    @Test
    fun `hasPregnancyDeath delegates to dao`() = runTest {
        coEvery { benDao.checkPregnancyDeath(1L) } returns true
        assertTrue(repo.hasPregnancyDeath(1L))
    }

    @Test
    fun `hasAbortionDeath delegates to dao`() = runTest {
        coEvery { benDao.checkAbortionDeath(1L) } returns false
        assertFalse(repo.hasAbortionDeath(1L))
    }

    @Test
    fun `hasDeliveryDeath delegates to dao`() = runTest {
        coEvery { benDao.checkDeliveryDeath(1L) } returns true
        assertTrue(repo.hasDeliveryDeath(1L))
    }

    @Test
    fun `hasPncDeath delegates to dao`() = runTest {
        coEvery { benDao.checkPncDeath(1L) } returns false
        assertFalse(repo.hasPncDeath(1L))
    }

    @Test
    fun `isPncCauseOfDeathAccident delegates to dao isDeathByCause`() = runTest {
        coEvery { benDao.isDeathByCause(1L, "accident") } returns true
        assertTrue(repo.isPncCauseOfDeathAccident(1L, "accident"))
    }

    @Test
    fun `isAncCauseOfDeathAccident delegates to dao isDeathByCauseAnc`() = runTest {
        coEvery { benDao.isDeathByCauseAnc(1L, "accident") } returns true
        assertTrue(repo.isAncCauseOfDeathAccident(1L, "accident"))
    }

    @Test
    fun `isBenDead delegates to dao`() = runTest {
        coEvery { benDao.isBenDead(1L) } returns true
        assertTrue(repo.isBenDead(1L))
    }

    // ---------------- child / list getters ----------------

    @Test
    fun `getBenListFromHousehold delegates to dao`() = runTest {
        val list = listOf(mockk<BenRegCache>(relaxed = true))
        coEvery { benDao.getAllBenForHousehold(5L) } returns list
        assertEquals(list, repo.getBenListFromHousehold(5L))
    }

    @Test
    fun `getChildCountForBen delegates to dao`() = runTest {
        coEvery { benDao.getChildCountForBen(5L) } returns 3
        assertEquals(3, repo.getChildCountForBen(5L))
    }

    @Test
    fun `getChildBenListFromHousehold delegates to dao`() = runTest {
        val list = listOf(mockk<BenRegCache>(relaxed = true))
        coEvery { benDao.getChildBenForHousehold(5L, 2L, "John") } returns list
        assertEquals(list, repo.getChildBenListFromHousehold(5L, 2L, "John"))
    }

    @Test
    fun `getChildBelow15 delegates to dao getBelow15Count`() = runTest {
        coEvery { benDao.getBelow15Count(5L, 2L, "John") } returns 4
        assertEquals(4, repo.getChildBelow15(5L, 2L, "John"))
    }

    @Test
    fun `getChildAbove15 delegates to dao get15aboveCount`() = runTest {
        coEvery { benDao.get15aboveCount(5L, 2L, "John") } returns 2
        assertEquals(2, repo.getChildAbove15(5L, 2L, "John"))
    }

    @Test
    fun `getBenWithHRPT delegates to dao getHRPTrackingPregForBen`() = runTest {
        val cache = mockk<BenWithHRPTrackingCache>()
        coEvery { benDao.getHRPTrackingPregForBen(5L) } returns cache
        assertEquals(cache, repo.getBenWithHRPT(5L))
    }

    // ---------------- no-user guards ----------------

    @Test
    fun `getBenIdsGeneratedFromServer throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getBenIdsGeneratedFromServer() }
    }

    @Test
    fun `getBeneficiariesFromServerForWorker throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getBeneficiariesFromServerForWorker(0) }
    }

    @Test
    fun `getBeneficiariesFromServer throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getBeneficiariesFromServer(0) }
    }

    @Test
    fun `getGeneralOPDBeneficiariesFromServertoWorker throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.getGeneralOPDBeneficiariesFromServertoWorker(0) }
    }

    @Test
    fun `deactivateBeneficiary throws when no user`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null
        assertNoUser { repo.deactivateBeneficiary(emptyList()) }
    }

    // ---------------- getBenBasicListFromHousehold flow mapping ----------------

    @Test
    fun `getBenBasicListFromHousehold maps empty dao flow`() = runTest {
        every { benDao.getAllBasicBenForHousehold(5L) } returns flowOf(emptyList())

        assertTrue(repo.getBenBasicListFromHousehold(5L).first().isEmpty())
    }

    // ---------------- substituteBenIdForDraft / extractBenId ----------------

    @Test
    fun `substituteBenIdForDraft assigns generated id and clears draft`() = runTest {
        loggedIn(userId = 7)
        val entry = BeneficiaryIdsAvail(userId = 7, benId = -55L, benRegId = 0)
        coEvery { benIdGenDao.getEntry(7) } returns entry
        coEvery { benIdGenDao.delete(entry) } returns Unit
        val ben = mockk<BenRegCache>(relaxed = true)

        repo.substituteBenIdForDraft(ben)

        coVerify { benIdGenDao.getEntry(7) }
        coVerify { benIdGenDao.delete(entry) }
    }

    @Test
    fun `substituteBenIdForDraft throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.substituteBenIdForDraft(mockk(relaxed = true))
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    // ---------------- getBenIdsGeneratedFromServer branches ----------------

    @Test
    fun `getBenIdsGeneratedFromServer returns early when count above trigger limit`() = runTest {
        loggedIn()
        coEvery { benIdGenDao.count() } returns 95

        repo.getBenIdsGeneratedFromServer()

        coVerify(exactly = 0) { benDao.getMinBenId() }
        coVerify(exactly = 0) { benIdGenDao.insert(*anyVararg()) }
    }

    @Test
    fun `getBenIdsGeneratedFromServer inserts ids when count below trigger limit`() = runTest {
        loggedIn()
        coEvery { benIdGenDao.count() } returns 0
        coEvery { benDao.getMinBenId() } returns -1L
        coEvery { benIdGenDao.insert(*anyVararg()) } returns Unit

        repo.getBenIdsGeneratedFromServer(maxCount = 3)

        coVerify { benDao.getMinBenId() }
        coVerify { benIdGenDao.insert(*anyVararg()) }
    }

    @Test
    fun `getBenIdsGeneratedFromServer throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.getBenIdsGeneratedFromServer()
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    // ---------------- deactivateHouseHold no-user guard ----------------

    @Test
    fun `deactivateHouseHold throws when no user logged in`() = runTest {
        every { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.deactivateHouseHold(emptyList(), mockk<HouseholdNetwork>(relaxed = true))
            assertFalse("Should have thrown IllegalStateException", true)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("No user logged in") == true)
        }
    }

    // ---------------- getBeneficiariesFromServerForWorker ----------------

    @Test
    fun `getBeneficiariesFromServerForWorker returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns jsonResponse("{}", code = 500)

        assertEquals(-1, repo.getBeneficiariesFromServerForWorker(0))
    }

    @Test
    fun `getBeneficiariesFromServerForWorker returns 0 on no record found`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns jsonResponse(json)

        assertEquals(0, repo.getBeneficiariesFromServerForWorker(0))
    }

    @Test
    fun `getBeneficiariesFromServerForWorker returns -2 when token refresh succeeds`() = runTest {
        loggedIn()
        val json = """{"statusCode":5002,"errorMessage":""}"""
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-2, repo.getBeneficiariesFromServerForWorker(0))
    }

    @Test
    fun `getBeneficiariesFromServerForWorker returns -1 when token refresh fails`() = runTest {
        loggedIn()
        val json = """{"statusCode":401,"errorMessage":""}"""
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getBeneficiariesFromServerForWorker(0))
    }

    @Test
    fun `getBeneficiariesFromServerForWorker returns -1 on unexpected status`() = runTest {
        loggedIn()
        val json = """{"statusCode":9999,"errorMessage":""}"""
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getBeneficiariesFromServerForWorker(0))
    }

    // ---------------- getBeneficiariesFromServer ----------------

    @Test
    fun `getBeneficiariesFromServer returns pair zero empty on non-200 http`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns jsonResponse("{}", code = 500)

        val (pageSize, list) = repo.getBeneficiariesFromServer(0)

        assertEquals(0, pageSize)
        assertTrue(list.isEmpty())
    }

    @Test
    fun `getBeneficiariesFromServer returns zero empty when responseStatusCode not 200`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"oops"}"""
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns jsonResponse(json)

        val (pageSize, list) = repo.getBeneficiariesFromServer(0)

        assertEquals(0, pageSize)
        assertTrue(list.isEmpty())
    }

    @Test
    fun `getBeneficiariesFromServer returns zero empty when data array empty`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":{"data":[],"totalPage":0}}"""
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns jsonResponse(json)

        val (pageSize, list) = repo.getBeneficiariesFromServer(0)

        assertEquals(0, pageSize)
        assertTrue(list.isEmpty())
    }

    // ---------------- getMinBenId delegation ----------------

    @Test
    fun `getMinBenId returns dao value`() = runTest {
        coEvery { benDao.getMinBenId() } returns -55L

        assertEquals(-55L, repo.getMinBenId())
    }

    @Test
    fun `getMinBenId returns 0 when dao null`() = runTest {
        coEvery { benDao.getMinBenId() } returns null

        assertEquals(0L, repo.getMinBenId())
    }

    // ---------------- companion date helpers (round-trip) ----------------

    @Test
    fun `getCurrentDate formats as ISO with millis and Z suffix`() {
        val result = BenRepo.getCurrentDate(0L)

        assertTrue("should contain T separator", result.contains("T"))
        assertTrue("should end with .000Z", result.endsWith(".000Z"))
    }

    @Test
    fun `getCurrentDate default arg produces non-blank string`() {
        assertTrue(BenRepo.getCurrentDate().endsWith(".000Z"))
    }

    @Test
    fun `getLongFromDateStr round-trips getCurrentDate to the same second`() {
        val millis = 1_700_000_000_000L
        val str = BenRepo.getCurrentDate(millis)

        val parsed = BenRepo.getLongFromDateStr(str)

        // getCurrentDate truncates to whole seconds (.000)
        assertEquals(millis / 1000 * 1000, parsed)
    }

    // ---------------- persistRecord (no image) ----------------

    @Test
    fun `persistRecord upserts ben when userImage is null`() = runTest {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.userImage } returns null
        coEvery { benDao.upsert(ben) } returns Unit

        repo.persistRecord(ben)

        coVerify { benDao.upsert(ben) }
    }

    // ---------------- getBeneficiaryRecord benId==0 guard ----------------

    @Test
    fun `getBeneficiaryRecord returns null and skips dao when benId is 0`() = runTest {
        val result = repo.getBeneficiaryRecord(0L, 5L)

        assertNull(result)
        coVerify(exactly = 0) { benDao.getBen(any(), any()) }
    }

    // ---------------- getGeneralOPDBeneficiariesFromServertoWorker branches ----------------

    @Test
    fun `getGeneralOPD returns -1 on non-200 http`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getgeneralOPDBeneficiaries(any()) } returns jsonResponse("{}", code = 500)

        assertEquals(-1, repo.getGeneralOPDBeneficiariesFromServertoWorker(0))
    }

    @Test
    fun `getGeneralOPD returns -1 when statusCode missing`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getgeneralOPDBeneficiaries(any()) } returns jsonResponse("{}")

        assertEquals(-1, repo.getGeneralOPDBeneficiariesFromServertoWorker(0))
    }

    @Test
    fun `getGeneralOPD returns 0 on statusCode 5000`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":""}"""
        coEvery { tmcNetworkApiService.getgeneralOPDBeneficiaries(any()) } returns jsonResponse(json)

        assertEquals(0, repo.getGeneralOPDBeneficiariesFromServertoWorker(0))
    }

    @Test
    fun `getGeneralOPD returns -2 when token refresh succeeds`() = runTest {
        loggedIn()
        val json = """{"statusCode":5002,"errorMessage":""}"""
        coEvery { tmcNetworkApiService.getgeneralOPDBeneficiaries(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertEquals(-2, repo.getGeneralOPDBeneficiariesFromServertoWorker(0))
    }

    @Test
    fun `getGeneralOPD returns -1 when token refresh fails`() = runTest {
        loggedIn()
        val json = """{"statusCode":5002,"errorMessage":""}"""
        coEvery { tmcNetworkApiService.getgeneralOPDBeneficiaries(any()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertEquals(-1, repo.getGeneralOPDBeneficiariesFromServertoWorker(0))
    }

    @Test
    fun `getGeneralOPD returns 0 when statusCode 200 but data missing`() = runTest {
        loggedIn()
        val json = """{"statusCode":200}"""
        coEvery { tmcNetworkApiService.getgeneralOPDBeneficiaries(any()) } returns jsonResponse(json)

        assertEquals(0, repo.getGeneralOPDBeneficiariesFromServertoWorker(0))
    }

    @Test
    fun `getGeneralOPD returns -1 on unexpected status code`() = runTest {
        loggedIn()
        val json = """{"statusCode":9999,"errorMessage":""}"""
        coEvery { tmcNetworkApiService.getgeneralOPDBeneficiaries(any()) } returns jsonResponse(json)

        assertEquals(-1, repo.getGeneralOPDBeneficiariesFromServertoWorker(0))
    }

    // ---------------- getBeneficiaryWithId ----------------

    @Test
    fun `getBeneficiaryWithId returns null when api throws`() = runTest {
        coEvery { tmcNetworkApiService.getBenHealthID(any()) } throws RuntimeException("boom")

        assertNull(repo.getBeneficiaryWithId(1L))
    }

    // ---------------- getUserDetailsByAyushmanAbhaCardNo ----------------

    @Test
    fun `getUserDetailsByAyushman returns NetworkError when api throws`() = runTest {
        coEvery { tmcNetworkApiService.getUserDetailsByAyushmanCardNo(any()) } throws RuntimeException("boom")

        val result = repo.getUserDetailsByAyushmanAbhaCardNo("CARD1", "HH1")

        assertTrue(result is NetworkResult.NetworkError)
    }

    @Test
    fun `getUserDetailsByAyushman returns Error on empty successful body`() = runTest {
        coEvery { tmcNetworkApiService.getUserDetailsByAyushmanCardNo(any()) } returns successResponse("")

        val result = repo.getUserDetailsByAyushmanAbhaCardNo("CARD1", "HH1")

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `getUserDetailsByAyushman returns Error when data has message but no members`() = runTest {
        val json = """{"data":{"message":"no records found"}}"""
        coEvery { tmcNetworkApiService.getUserDetailsByAyushmanCardNo(any()) } returns successResponse(json)

        val result = repo.getUserDetailsByAyushmanAbhaCardNo("CARD1", "HH1")

        assertTrue(result is NetworkResult.Error)
        assertEquals("no records found", (result as NetworkResult.Error).message)
    }

    // ---------------- OTP helpers (unsuccessful response -> null) ----------------

    @Test
    fun `sendOtp returns null on unsuccessful response`() = runTest {
        coEvery { tmcNetworkApiService.sendOtp(any()) } returns jsonResponse("{}")

        assertNull(repo.sendOtp("9999999999"))
    }

    @Test
    fun `resendOtp returns null on unsuccessful response`() = runTest {
        coEvery { tmcNetworkApiService.resendOtp(any()) } returns jsonResponse("{}")

        assertNull(repo.resendOtp("9999999999"))
    }

    @Test
    fun `verifyOtp returns null on unsuccessful response`() = runTest {
        coEvery { tmcNetworkApiService.validateOtp(any()) } returns jsonResponse("{}")

        assertNull(repo.verifyOtp("9999999999", 1234))
    }

    // ---------------- deactivateHouseHold / deactivateBeneficiary bad-response ----------------

    @Test
    fun `deactivateHouseHold returns false on non-200 http`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns jsonResponse("{}", code = 500)

        assertFalse(repo.deactivateHouseHold(emptyList(), mockk<HouseholdNetwork>(relaxed = true)))
    }

    @Test
    fun `deactivateHouseHold returns false on inner unexpected status`() = runTest {
        loggedIn()
        val json = """{"statusCode":9999,"errorMessage":"weird"}"""
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns jsonResponse(json)

        assertFalse(repo.deactivateHouseHold(emptyList(), mockk<HouseholdNetwork>(relaxed = true)))
    }

    @Test
    fun `deactivateBeneficiary returns false on non-200 http`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns jsonResponse("{}", code = 500)

        assertFalse(repo.deactivateBeneficiary(emptyList()))
    }

    @Test
    fun `deactivateBeneficiary returns false on inner unexpected status`() = runTest {
        loggedIn()
        val json = """{"statusCode":9999,"errorMessage":"weird"}"""
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns jsonResponse(json)

        assertFalse(repo.deactivateBeneficiary(emptyList()))
    }

    // ---------------- getBeneficiaryWithId unsuccessful response ----------------

    @Test
    fun `getBeneficiaryWithId returns null on unsuccessful response`() = runTest {
        // jsonResponse leaves isSuccessful at the relaxed default (false).
        coEvery { tmcNetworkApiService.getBenHealthID(any()) } returns jsonResponse("{}")

        assertNull(repo.getBeneficiaryWithId(1L))
    }

    // =====================================================
    // Full server-pull payload parsing
    // =====================================================

    private fun stubLocationRecord() {
        val entity = LocationEntity(1, "Loc")
        every { preferenceDao.getLocationRecord() } returns
                LocationRecord(entity, entity, entity, entity, entity)
    }

    /**
     * A fully-populated single-record beneficiary pull payload. Every key the
     * household/beneficiary parsers read without a `has()` guard is present so
     * both parsers run to completion instead of bailing out on a JSONException.
     */
    private fun benPullPayload(totalPage: Int = 3): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 5001,
                "houseoldId": 9001,
                "ashaId": 11,
                "BenRegId": 7001,
                "isDeath": false,
                "isSpouseAdded": true,
                "isChildrenAdded": false,
                "isMarried": true,
                "doYouHavechildren": true,
                "noofAlivechildren": 2,
                "noOfchildren": 2,
                "abhaHealthDetails": {
                  "HealthIdNumber": "12-3456-7890",
                  "HealthID": "john@abdm",
                  "isNewAbha": true
                },
                "bornbirthDeatils": {},
                "householdDetails": {
                  "familyHeadName": "Head Name",
                  "familyName": "Doe",
                  "familyHeadPhoneNo": "9876543210",
                  "houseno": "12A",
                  "wardNo": "3",
                  "wardName": "Ward 3",
                  "mohallaName": "Mohalla",
                  "type_bpl_apl": "APL",
                  "bpl_aplId": 1,
                  "residentialArea": "Urban",
                  "residentialAreaId": 1,
                  "other_residentialArea": "",
                  "houseType": "Pucca",
                  "houseTypeId": 1,
                  "other_houseType": "",
                  "houseOwnerShip": "Owned",
                  "houseOwnerShipId": 1,
                  "seperateKitchen": "Yes",
                  "seperateKitchenId": 1,
                  "fuelUsed": "LPG",
                  "fuelUsedId": 1,
                  "other_fuelUsed": "",
                  "sourceofDrinkingWater": "Tap",
                  "sourceofDrinkingWaterId": 1,
                  "other_sourceofDrinkingWater": "",
                  "avalabilityofElectricity": "Yes",
                  "avalabilityofElectricityId": 1,
                  "other_avalabilityofElectricity": "",
                  "availabilityofToilet": "Yes",
                  "availabilityofToiletId": 1,
                  "other_availabilityofToilet": "",
                  "registrationType": "New",
                  "serverUpdatedStatus": 1,
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM",
                  "isDeactivate": false
                },
                "beneficiaryDetails": {
                  "registrationDate": "Jan 15, 2026 10:30:00 AM",
                  "firstName": "John",
                  "lastName": "Doe",
                  "gender": "Male",
                  "genderId": 1,
                  "age": 30,
                  "age_unit": "Years",
                  "dob": "Jan 15, 1996 10:30:00 AM",
                  "contact_number": "9876543210",
                  "fatherName": "Father Name",
                  "motherName": "Mother Name",
                  "familyHeadRelation": "Self",
                  "familyHeadRelationPosition": 1,
                  "mobilenoofRelation": "Self",
                  "mobilenoofRelationId": 1,
                  "mobileOthers": "",
                  "rchid": "RCH123",
                  "hrpStatus": false,
                  "reproductiveStatus": "Eligible Couple",
                  "reproductiveStatusId": 2,
                  "maritalstatus": "Married",
                  "maritalstatusId": 1,
                  "spousename": "Jane Doe",
                  "ageAtMarriage": 22,
                  "marriageDate": "Jan 15, 2018 10:30:00 AM",
                  "literacyId": 1,
                  "communityId": 1,
                  "community": "General",
                  "religion": "Hindu",
                  "religionID": 1,
                  "religionOthers": "",
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadhaNo": "123412341234",
                  "aadha_noId": 1,
                  "bankAccount": "1234567890",
                  "nameOfBank": "SBI",
                  "ifscCode": "SBIN0001234",
                  "need_opcareId": 0,
                  "ncd_priority": 0,
                  "guidelineId": "G1",
                  "isDeactivate": false,
                  "stateId": 1,
                  "stateName": "State",
                  "districtid": 2,
                  "districtname": "District",
                  "blockId": 3,
                  "blockName": "Block",
                  "villageId": 4,
                  "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM",
                  "updatedBy": "asha",
                  "updatedDate": "Jan 16, 2026 10:30:00 AM"
                }
              }
            ]
          }
        }
    """.trimIndent()

    /** Counts how many records reached a vararg upsert, without depending on the array type. */
    private fun varargSize(value: Any?): Int = when (value) {
        is Array<*> -> value.size
        is Collection<*> -> value.size
        else -> -1
    }

    @Test
    fun `getBeneficiariesFromServerForWorker parses and upserts households`() = runTest {
        loggedIn()
        stubLocationRecord()
        // No household stored locally -> the household parser builds a record.
        coEvery { householdDao.getHousehold(any()) } returns null
        var upsertedHouseholds = -1
        coEvery { householdDao.upsert(*anyVararg()) } answers {
            upsertedHouseholds = varargSize(arg<Any?>(0))
        }
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                jsonResponse(benPullPayload())

        assertEquals(3, repo.getBeneficiariesFromServerForWorker(0))
        assertEquals(1, upsertedHouseholds)
    }

    @Test
    fun `getBeneficiariesFromServerForWorker parses beneficiary cache when household exists`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            // Household already present -> household parser skips, ben parser runs.
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(any(), any()) } returns null
            var upsertedBens = -1
            coEvery { benDao.upsert(*anyVararg()) } answers {
                upsertedBens = varargSize(arg<Any?>(0))
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayload())

            assertEquals(3, repo.getBeneficiariesFromServerForWorker(0))
            assertEquals(1, upsertedBens)
        }

    @Test
    fun `getBeneficiariesFromServer maps payload to basic domain list`() = runTest {
        loggedIn()
        stubLocationRecord()
        coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
        coEvery { benDao.getBen(any(), any()) } returns null
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                jsonResponse(benPullPayload())

        val (pageSize, list) = repo.getBeneficiariesFromServer(0)

        assertEquals(3, pageSize)
        assertEquals(1, list.size)
        assertEquals(5001L, list[0].benId)
        assertEquals(9001L, list[0].hhId)
        assertEquals("John", list[0].benName)
        assertEquals("Head Name", list[0].familyHeadName)
    }

    @Test
    fun `getBeneficiariesFromServer skips records missing ids`() = runTest {
        loggedIn()
        val json = """
            {"statusCode":200,"data":{"totalPage":1,"data":[
              {"householdDetails":{},"beneficiaryDetails":{}}
            ]}}
        """.trimIndent()
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns jsonResponse(json)

        val result = repo.getBeneficiariesFromServer(0)

        assertTrue(result.second.isEmpty())
    }

    @Test
    fun `getBeneficiariesFromServerForWorker returns -1 for auth failure on later page`() =
        runTest {
            loggedIn()
            val json = """{"statusCode":5002,"errorMessage":""}"""
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns jsonResponse(json)

            // pageNumber != 0 short-circuits the refresh attempt into IllegalStateException.
            assertEquals(-1, repo.getBeneficiariesFromServerForWorker(2))
            coVerify(exactly = 0) { userRepo.refreshTokenTmc(any(), any()) }
        }

    // =====================================================
    // processNewBen happy path (nothing to sync)
    // =====================================================

    @Test
    fun `processNewBen returns true when nothing to sync`() = runTest {
        loggedIn()
        coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns emptyList()

        assertTrue(repo.processNewBen())

        coVerify(exactly = 0) { tmcNetworkApiService.submitRmnchDataAmrit(any()) }
    }

    // =====================================================
    // getGeneralOPDBeneficiariesFromServertoWorker success
    // =====================================================

    @Test
    fun `getGeneralOPD returns 1 and stores entries on success`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":{"entries":[]}}"""
        coEvery { tmcNetworkApiService.getgeneralOPDBeneficiaries(any()) } returns
                jsonResponse(json)

        assertEquals(1, repo.getGeneralOPDBeneficiariesFromServertoWorker(0))
        coVerify { generalOpdDao.insertAll(emptyList()) }
    }

    @Test
    fun `getGeneralOPD returns 0 when entries node missing`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":{}}"""
        coEvery { tmcNetworkApiService.getgeneralOPDBeneficiaries(any()) } returns
                jsonResponse(json)

        assertEquals(0, repo.getGeneralOPDBeneficiariesFromServertoWorker(0))
        coVerify(exactly = 0) { generalOpdDao.insertAll(any()) }
    }

    // =====================================================
    // getBeneficiaryWithId success / error branches
    // =====================================================

    @Test
    fun `getBeneficiaryWithId returns last health detail on success`() = runTest {
        val json = """
            {"statusCode":200,"data":{"BenHealthDetails":[
              {"benHealthID":1,"healthIdNumber":"11-1111-1111","beneficiaryRegID":10,"healthId":"first@abdm","isNewAbha":false},
              {"benHealthID":2,"healthIdNumber":"22-2222-2222","beneficiaryRegID":10,"healthId":"last@abdm","isNewAbha":true}
            ]}}
        """.trimIndent()
        coEvery { tmcNetworkApiService.getBenHealthID(any()) } returns successResponse(json)

        val result = repo.getBeneficiaryWithId(10L)

        assertNotNull(result)
        assertEquals("last@abdm", result?.healthId)
    }

    @Test
    fun `getBeneficiaryWithId returns null when health details array empty`() = runTest {
        val json = """{"statusCode":200,"data":{"BenHealthDetails":[]}}"""
        coEvery { tmcNetworkApiService.getBenHealthID(any()) } returns successResponse(json)

        assertNull(repo.getBeneficiaryWithId(10L))
    }

    @Test
    fun `getBeneficiaryWithId returns null on auth status with other error message`() = runTest {
        val json = """{"statusCode":5000,"errorMessage":"No record found"}"""
        coEvery { tmcNetworkApiService.getBenHealthID(any()) } returns successResponse(json)

        assertNull(repo.getBeneficiaryWithId(10L))
        coVerify(exactly = 0) { userRepo.refreshTokenTmc(any(), any()) }
    }

    @Test
    fun `getBeneficiaryWithId returns null on unexpected status code`() = runTest {
        val json = """{"statusCode":9999}"""
        coEvery { tmcNetworkApiService.getBenHealthID(any()) } returns successResponse(json)

        assertNull(repo.getBeneficiaryWithId(10L))
    }

    // =====================================================
    // OTP helper non-200 branches on a successful HTTP response
    // =====================================================

    @Test
    fun `sendOtp returns null when server reports a non-session error`() = runTest {
        val json = """{"statusCode":5000,"errorMessage":"Mobile number blocked"}"""
        coEvery { tmcNetworkApiService.sendOtp(any()) } returns successResponse(json)

        assertNull(repo.sendOtp("9999999999"))
        coVerify(exactly = 0) { userRepo.refreshTokenTmc(any(), any()) }
    }

    @Test
    fun `sendOtp returns null on unexpected status code`() = runTest {
        coEvery { tmcNetworkApiService.sendOtp(any()) } returns
                successResponse("""{"statusCode":9999}""")

        assertNull(repo.sendOtp("9999999999"))
    }

    @Test
    fun `resendOtp returns null when server reports a non-session error`() = runTest {
        val json = """{"statusCode":5002,"errorMessage":"Too many attempts"}"""
        coEvery { tmcNetworkApiService.resendOtp(any()) } returns successResponse(json)

        assertNull(repo.resendOtp("9999999999"))
        coVerify(exactly = 0) { userRepo.refreshTokenTmc(any(), any()) }
    }

    @Test
    fun `resendOtp returns null on unexpected status code`() = runTest {
        coEvery { tmcNetworkApiService.resendOtp(any()) } returns
                successResponse("""{"statusCode":9999}""")

        assertNull(repo.resendOtp("9999999999"))
    }

    // ---------------- resendOtp full branch coverage ----------------

    @Test
    fun `resendOtp returns parsed response and shows toast on statusCode 200`() = runTest {
        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns toast
        val json = """
            {
              "statusCode": 200,
              "data": {
                "data": {"response": "OTP sent successfully"},
                "statusCode": 200,
                "errorMessage": "",
                "status": "SUCCESS"
              }
            }
        """.trimIndent()
        coEvery { tmcNetworkApiService.resendOtp(any()) } returns successResponse(json)

        val result = repo.resendOtp("9999999999")

        assertNotNull(result)
        assertEquals("OTP sent successfully", result?.data?.response)
        assertEquals("SUCCESS", result?.status)
        verify { toast.show() }
    }

    @Test
    fun `resendOtp refreshes token when session expired message on statusCode 5000`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"Invalid login key or session is expired"}"""
        coEvery { tmcNetworkApiService.resendOtp(any()) } returns successResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertNull(repo.resendOtp("9999999999"))

        coVerify { userRepo.refreshTokenTmc("asha", "pwd") }
    }

    @Test
    fun `resendOtp refreshes token when session expired message on statusCode 5002`() = runTest {
        loggedIn()
        val json = """{"statusCode":5002,"errorMessage":"Invalid login key or session is expired"}"""
        coEvery { tmcNetworkApiService.resendOtp(any()) } returns successResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertNull(repo.resendOtp("9999999999"))

        coVerify { userRepo.refreshTokenTmc("asha", "pwd") }
    }

    @Test
    fun `resendOtp returns null when successful response body is null`() = runTest {
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.isSuccessful } returns true
        every { response.body() } returns null
        coEvery { tmcNetworkApiService.resendOtp(any()) } returns response

        assertNull(repo.resendOtp("9999999999"))
    }

    @Test
    fun `resendOtp returns null when response body is malformed json`() = runTest {
        coEvery { tmcNetworkApiService.resendOtp(any()) } returns
                successResponse("not-json")

        assertNull(repo.resendOtp("9999999999"))
    }

    @Test
    fun `resendOtp returns null when api call throws exception`() = runTest {
        coEvery { tmcNetworkApiService.resendOtp(any()) } throws IOException("network down")

        assertNull(repo.resendOtp("9999999999"))
    }

    @Test
    fun `verifyOtp returns null on unexpected status code`() = runTest {
        coEvery { tmcNetworkApiService.validateOtp(any()) } returns
                successResponse("""{"statusCode":9999}""")

        assertNull(repo.verifyOtp("9999999999", 1234))
    }

    // =====================================================
    // Ayushman card lookup parsing branches
    // =====================================================

    @Test
    fun `getUserDetailsByAyushman returns Error with http code on unsuccessful response`() =
        runTest {
            coEvery { tmcNetworkApiService.getUserDetailsByAyushmanCardNo(any()) } returns
                    jsonResponse("{}", code = 404)

            val result = repo.getUserDetailsByAyushmanAbhaCardNo("CARD1", "HH1")

            assertTrue(result is NetworkResult.Error)
            assertEquals(404, (result as NetworkResult.Error).code)
        }

    @Test
    fun `getUserDetailsByAyushman returns Success when nested data array present`() = runTest {
        val json = """{"data":{"statusCode":200,"data":[{"cardNo":"C1","name":"A"}]}}"""
        coEvery { tmcNetworkApiService.getUserDetailsByAyushmanCardNo(any()) } returns
                successResponse(json)

        val result = repo.getUserDetailsByAyushmanAbhaCardNo("CARD1", "HH1")

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, ((result as NetworkResult.Success<*>).data as List<*>).size)
    }

    @Test
    fun `getUserDetailsByAyushman returns Error with server status code when array empty`() =
        runTest {
            val json = """{"data":{"statusCode":404,"message":"Card not found","data":[]}}"""
            coEvery { tmcNetworkApiService.getUserDetailsByAyushmanCardNo(any()) } returns
                    successResponse(json)

            val result = repo.getUserDetailsByAyushmanAbhaCardNo("CARD1", "HH1")

            assertTrue(result is NetworkResult.Error)
            assertEquals(404, (result as NetworkResult.Error).code)
            assertEquals("Card not found", result.message)
        }

    @Test
    fun `getUserDetailsByAyushman parses a bare top level array body`() = runTest {
        val json = """[{"cardNo":"C1"},{"cardNo":"C2"}]"""
        coEvery { tmcNetworkApiService.getUserDetailsByAyushmanCardNo(any()) } returns
                successResponse(json)

        val result = repo.getUserDetailsByAyushmanAbhaCardNo("CARD1", "HH1")

        assertTrue(result is NetworkResult.Success)
        assertEquals(2, ((result as NetworkResult.Success<*>).data as List<*>).size)
    }

    @Test
    fun `getUserDetailsByAyushman parses a single userDetails object`() = runTest {
        val json = """{"userDetails":{"cardNo":"C1","name":"A"}}"""
        coEvery { tmcNetworkApiService.getUserDetailsByAyushmanCardNo(any()) } returns
                successResponse(json)

        val result = repo.getUserDetailsByAyushmanAbhaCardNo("CARD1", "HH1")

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, ((result as NetworkResult.Success<*>).data as List<*>).size)
    }

    @Test
    fun `getUserDetailsByAyushman parses a flat member object`() = runTest {
        val json = """{"cardNo":"C1","personName":"A"}"""
        coEvery { tmcNetworkApiService.getUserDetailsByAyushmanCardNo(any()) } returns
                successResponse(json)

        val result = repo.getUserDetailsByAyushmanAbhaCardNo("CARD1", "HH1")

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, ((result as NetworkResult.Success<*>).data as List<*>).size)
    }

    @Test
    fun `getUserDetailsByAyushman returns Error for an unrecognised object body`() = runTest {
        val json = """{"unrelated":"value"}"""
        coEvery { tmcNetworkApiService.getUserDetailsByAyushmanCardNo(any()) } returns
                successResponse(json)

        val result = repo.getUserDetailsByAyushmanAbhaCardNo("CARD1", "HH1")

        assertTrue(result is NetworkResult.Error)
        assertEquals("No records found for this card number", (result as NetworkResult.Error).message)
    }

    @Test
    fun `getUserDetailsByAyushman returns Error for a non-json body`() = runTest {
        coEvery { tmcNetworkApiService.getUserDetailsByAyushmanCardNo(any()) } returns
                successResponse("service unavailable")

        val result = repo.getUserDetailsByAyushmanAbhaCardNo("CARD1", "HH1")

        assertTrue(result is NetworkResult.Error)
        assertEquals("Unable to parse response", (result as NetworkResult.Error).message)
    }

    // =====================================================
    // deactivate* token-refresh / retry / malformed-body branches
    // =====================================================

    @Test
    fun `deactivateHouseHold retries until exhausted when token is refreshed`() = runTest {
        loggedIn()
        val json = """{"statusCode":5002,"errorMessage":"expired"}"""
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns jsonResponse(json, code = 200)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertFalse(repo.deactivateHouseHold(emptyList(), mockk<HouseholdNetwork>(relaxed = true)))

        // initial attempt + 3 retries
        coVerify(exactly = 4) { tmcNetworkApiService.submitRmnchDataAmrit(any()) }
    }

    @Test
    fun `deactivateHouseHold returns false when token refresh fails`() = runTest {
        loggedIn()
        val json = """{"statusCode":5002,"errorMessage":"expired"}"""
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns jsonResponse(json, code = 200)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertFalse(repo.deactivateHouseHold(emptyList(), mockk<HouseholdNetwork>(relaxed = true)))

        coVerify(exactly = 1) { tmcNetworkApiService.submitRmnchDataAmrit(any()) }
    }

    @Test
    fun `deactivateHouseHold returns false on malformed json body`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                jsonResponse("not-json", code = 200)

        assertFalse(repo.deactivateHouseHold(emptyList(), mockk<HouseholdNetwork>(relaxed = true)))
    }

    @Test
    fun `deactivateBeneficiary retries until exhausted when token is refreshed`() = runTest {
        loggedIn()
        val json = """{"statusCode":5002,"errorMessage":"expired"}"""
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns jsonResponse(json, code = 200)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertFalse(repo.deactivateBeneficiary(emptyList()))

        coVerify(exactly = 4) { tmcNetworkApiService.submitRmnchDataAmrit(any()) }
    }

    @Test
    fun `deactivateBeneficiary returns false when token refresh fails`() = runTest {
        loggedIn()
        val json = """{"statusCode":5002,"errorMessage":"expired"}"""
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns jsonResponse(json, code = 200)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertFalse(repo.deactivateBeneficiary(emptyList()))

        coVerify(exactly = 1) { tmcNetworkApiService.submitRmnchDataAmrit(any()) }
    }

    @Test
    fun `deactivateBeneficiary returns false on malformed json body`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                jsonResponse("not-json", code = 200)

        assertFalse(repo.deactivateBeneficiary(emptyList()))
    }

    // =====================================================
    // persistRecord: already-permanent image path (no ImageUtils I/O)
    // =====================================================

    @Test
    fun `persistRecord skips ImageUtils save when image already in permanent storage`() = runTest {
        mockkStatic(Uri::class)
        val filesDir = File(System.getProperty("java.io.tmpdir"), "sakhi_files_test")
        val imageFile = File(filesDir, "ben_1.jpg")
        every { context.filesDir } returns filesDir
        val uri = mockk<Uri>(relaxed = true)
        every { uri.scheme } returns "file"
        every { uri.path } returns imageFile.absolutePath
        every { Uri.parse(any()) } returns uri

        val ben = mockk<BenRegCache>(relaxed = true)
        val originalPath = "file://${imageFile.absolutePath}"
        every { ben.userImage } returns originalPath
        coEvery { benDao.upsert(ben) } returns Unit

        repo.persistRecord(ben)

        coVerify { benDao.upsert(ben) }
        verify { ben.userImage = originalPath }
    }

    // =====================================================
    // getBeneficiariesFromServerForWorker: null-body / catch branches
    // =====================================================

    @Test
    fun `getBeneficiariesFromServerForWorker returns -1 when response body is null on success`() =
        runTest {
            loggedIn()
            val response = mockk<Response<ResponseBody>>(relaxed = true)
            every { response.code() } returns 200
            every { response.body() } returns null
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns response

            assertEquals(-1, repo.getBeneficiariesFromServerForWorker(0))
        }

    @Test
    fun `getBeneficiariesFromServerForWorker returns 0 when household upsert throws`() = runTest {
        loggedIn()
        stubLocationRecord()
        coEvery { householdDao.getHousehold(any()) } returns null
        coEvery { householdDao.upsert(*anyVararg()) } throws RuntimeException("db fail")
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                jsonResponse(benPullPayload())

        assertEquals(0, repo.getBeneficiariesFromServerForWorker(0))
    }

    @Test
    fun `getBeneficiariesFromServerForWorker upserts empty array when household missing locally`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            // Household not found locally -> getBenCacheFromServerResponse's hhExists guard
            // skips the record entirely (continue), leaving the ben list empty.
            coEvery { householdDao.getHousehold(any()) } returns null
            coEvery { householdDao.upsert(*anyVararg()) } returns Unit
            var upsertedBens = -1
            coEvery { benDao.upsert(*anyVararg()) } answers {
                upsertedBens = varargSize(arg<Any?>(0))
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayload())

            assertEquals(3, repo.getBeneficiariesFromServerForWorker(0))
            assertEquals(0, upsertedBens)
        }

    // =====================================================
    // getBeneficiariesFromServer: exception / catch branches
    // =====================================================

    @Test
    fun `getBeneficiariesFromServer returns pair zero empty when api call throws`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } throws IOException("network down")

        val (pageSize, list) = repo.getBeneficiariesFromServer(0)

        assertEquals(0, pageSize)
        assertTrue(list.isEmpty())
    }

    @Test
    fun `getBeneficiariesFromServer returns partial list when household upsert throws`() = runTest {
        loggedIn()
        stubLocationRecord()
        coEvery { householdDao.getHousehold(any()) } returns null
        coEvery { benDao.getBen(any(), any()) } returns null
        coEvery { householdDao.upsert(*anyVararg()) } throws RuntimeException("db fail")
        coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                jsonResponse(benPullPayload())

        val (pageSize, list) = repo.getBeneficiariesFromServer(0)

        assertEquals(0, pageSize)
        assertEquals(1, list.size)
        assertEquals(5001L, list[0].benId)
    }

    // =====================================================
    // getGeneralOPDBeneficiariesFromServertoWorker: null-body branch
    // =====================================================

    @Test
    fun `getGeneralOPD returns -1 when response body is null on success`() = runTest {
        loggedIn()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 200
        every { response.body() } returns null
        coEvery { tmcNetworkApiService.getgeneralOPDBeneficiaries(any()) } returns response

        assertEquals(-1, repo.getGeneralOPDBeneficiariesFromServertoWorker(0))
    }

    // =====================================================
    // deactivateHouseHold / deactivateBeneficiary: null-body branch
    // =====================================================

    @Test
    fun `deactivateHouseHold returns false when response body is null on statusCode 200`() =
        runTest {
            loggedIn()
            val response = mockk<Response<ResponseBody>>(relaxed = true)
            every { response.code() } returns 200
            every { response.body() } returns null
            coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns response

            assertFalse(repo.deactivateHouseHold(emptyList(), mockk<HouseholdNetwork>(relaxed = true)))
        }

    @Test
    fun `deactivateBeneficiary returns false when response body is null on statusCode 200`() =
        runTest {
            loggedIn()
            val response = mockk<Response<ResponseBody>>(relaxed = true)
            every { response.code() } returns 200
            every { response.body() } returns null
            coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns response

            assertFalse(repo.deactivateBeneficiary(emptyList()))
        }

    // =====================================================
    // verifyOtp: toast branch and malformed-body exception
    // =====================================================

    @Test
    fun `verifyOtp shows toast and returns null on statusCode 500`() = runTest {
        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns toast
        val json = """{"statusCode":500}"""
        coEvery { tmcNetworkApiService.validateOtp(any()) } returns successResponse(json)

        val result = repo.verifyOtp("9999999999", 1234)

        assertNull(result)
        verify { toast.show() }
    }

    @Test
    fun `verifyOtp throws JSONException on malformed response body`() = runTest {
        coEvery { tmcNetworkApiService.validateOtp(any()) } returns successResponse("not-json")

        try {
            repo.verifyOtp("9999999999", 1234)
            assertFalse("Should have thrown", true)
        } catch (e: JSONException) {
            // expected: verifyOtp does not guard JSONObject parsing with try/catch
        }
    }

    // =====================================================
    // sendOtp: success/toast, refresh-token and exception branches
    // =====================================================

    @Test
    fun `sendOtp returns parsed response and shows toast on statusCode 200`() = runTest {
        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns toast
        val json = """
            {
              "statusCode": 200,
              "data": {
                "data": {"response": "OTP sent successfully"},
                "statusCode": 200,
                "errorMessage": "",
                "status": "SUCCESS"
              }
            }
        """.trimIndent()
        coEvery { tmcNetworkApiService.sendOtp(any()) } returns successResponse(json)

        val result = repo.sendOtp("9999999999")

        assertNotNull(result)
        assertEquals("OTP sent successfully", result?.data?.response)
        verify { toast.show() }
    }

    @Test
    fun `sendOtp returns null when api call throws exception`() = runTest {
        coEvery { tmcNetworkApiService.sendOtp(any()) } throws IOException("network down")

        assertNull(repo.sendOtp("9999999999"))
    }

    @Test
    fun `sendOtp refreshes token when session expired message on statusCode 5000`() = runTest {
        loggedIn()
        val json = """{"statusCode":5000,"errorMessage":"Invalid login key or session is expired"}"""
        coEvery { tmcNetworkApiService.sendOtp(any()) } returns successResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertNull(repo.sendOtp("9999999999"))

        coVerify { userRepo.refreshTokenTmc("asha", "pwd") }
    }

    // =====================================================
    // processNewBen: createBenIdAtServerByBeneficiarySending non-empty path
    // =====================================================

    private fun benCacheMock(hhId: Long, benId: Long): BenRegCache {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.householdId } returns hhId
        every { ben.beneficiaryId } returns benId
        every { ben.locationRecord } returns mockk(relaxed = true)
        every { ben.ageUnitId } returns 1
        every { ben.age } returns 5
        return ben
    }

    private fun tempFilesDir(): File {
        val dir = File(System.getProperty("java.io.tmpdir"), "benrepo_test_${System.nanoTime()}")
        dir.mkdirs()
        return dir
    }

    @Test
    fun `processNewBen creates ben id at server on success and updates final ben id`() = runTest {
        loggedIn()
        every { context.filesDir } returns tempFilesDir()
        val ben = benCacheMock(hhId = 10L, benId = -5L)
        coEvery { benDao.getAllUnsyncedBen(any()) } returns listOf(ben)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns emptyList()
        val json = """{"statusCode":200,"data":{"response":"ok","benGenId":"123","benRegId":"456"}}"""
        coEvery { tmcNetworkApiService.getBenIdFromBeneficiarySending(any<BeneficiaryDataSending>()) } returns jsonResponse(json)

        assertTrue(repo.processNewBen())

        coVerify {
            benDao.updateToFinalBenId(hhId = 10L, oldId = -5L, newBenRegId = 456L, newId = 123L, imageUri = any())
        }
        coVerify { formResponseJsonDao.updateVisitBenId(oldBenId = -5L, newBenId = 123L) }
        coVerify { infantRegRepo.getInfantRegFromChildBenId(-5L) }
    }

    @Test
    fun `processNewBen updates household head reference when matching old ben id`() = runTest {
        loggedIn()
        every { context.filesDir } returns tempFilesDir()
        val ben = benCacheMock(hhId = 30L, benId = -7L)
        coEvery { benDao.getAllUnsyncedBen(any()) } returns listOf(ben)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns emptyList()
        val household = mockk<HouseholdCache>(relaxed = true)
        every { household.benId } returns -7L
        coEvery { householdDao.getHousehold(30L) } returns household
        val json = """{"statusCode":200,"data":{"response":"ok","benGenId":"88","benRegId":"99"}}"""
        coEvery { tmcNetworkApiService.getBenIdFromBeneficiarySending(any<BeneficiaryDataSending>()) } returns jsonResponse(json)

        repo.processNewBen()

        verify { household.benId = 88L }
        coVerify { householdDao.update(household) }
    }

    @Test
    fun `processNewBen sets ben unsynced when create-ben-id response body is null`() = runTest {
        loggedIn()
        every { context.filesDir } returns tempFilesDir()
        val ben = benCacheMock(hhId = 11L, benId = -6L)
        coEvery { benDao.getAllUnsyncedBen(any()) } returns listOf(ben)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns emptyList()
        val response = mockk<Response<ResponseBody>>(relaxed = true)
        every { response.code() } returns 200
        every { response.body() } returns null
        coEvery { tmcNetworkApiService.getBenIdFromBeneficiarySending(any<BeneficiaryDataSending>()) } returns response

        assertTrue(repo.processNewBen())

        coVerify { benDao.setSyncState(11L, -6L, SyncState.UNSYNCED) }
    }

    @Test
    fun `processNewBen sets ben unsynced when create-ben-id token refresh fails`() = runTest {
        loggedIn()
        every { context.filesDir } returns tempFilesDir()
        val ben = benCacheMock(hhId = 12L, benId = -8L)
        coEvery { benDao.getAllUnsyncedBen(any()) } returns listOf(ben)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns emptyList()
        val json = """{"statusCode":5002,"errorMessage":"expired"}"""
        coEvery { tmcNetworkApiService.getBenIdFromBeneficiarySending(any<BeneficiaryDataSending>()) } returns jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertTrue(repo.processNewBen())

        coVerify { userRepo.refreshTokenTmc("asha", "pwd") }
        coVerify { benDao.setSyncState(12L, -8L, SyncState.UNSYNCED) }
        coVerify(exactly = 1) { tmcNetworkApiService.getBenIdFromBeneficiarySending(any<BeneficiaryDataSending>()) }
    }

    @Test
    fun `processNewBen retries create-ben-id after a token refresh and then succeeds`() = runTest {
        loggedIn()
        every { context.filesDir } returns tempFilesDir()
        val ben = benCacheMock(hhId = 13L, benId = -9L)
        coEvery { benDao.getAllUnsyncedBen(any()) } returns listOf(ben)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns emptyList()
        val expiredJson = """{"statusCode":5002,"errorMessage":"expired"}"""
        val successJson = """{"statusCode":200,"data":{"response":"ok","benGenId":"55","benRegId":"66"}}"""
        coEvery { tmcNetworkApiService.getBenIdFromBeneficiarySending(any<BeneficiaryDataSending>()) } returnsMany
                listOf(jsonResponse(expiredJson), jsonResponse(successJson))
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.processNewBen())

        coVerify(exactly = 2) { tmcNetworkApiService.getBenIdFromBeneficiarySending(any<BeneficiaryDataSending>()) }
        coVerify {
            benDao.updateToFinalBenId(hhId = 13L, oldId = -9L, newBenRegId = 66L, newId = 55L, imageUri = any())
        }
    }

    // =====================================================
    // processNewBen: uploadBenBatch / postDataToAmritServer non-empty path
    // =====================================================

    @Test
    fun `processNewBen pushes unsynced ben batch and marks synced on success`() = runTest {
        loggedIn()
        coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
        val ben = benCacheMock(hhId = 20L, benId = 300L)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns listOf(ben)
        val household = mockk<HouseholdCache>(relaxed = true)
        val householdNetwork = mockk<HouseholdNetwork>(relaxed = true)
        every { householdNetwork.householdId } returns "20"
        every { household.asNetworkModel(any()) } returns householdNetwork
        coEvery { householdDao.getHousehold(20L) } returns household
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                jsonResponse("""{"statusCode":200}""")

        assertTrue(repo.processNewBen())

        coVerify { benDao.benSyncedWithServer(*anyLongVararg()) }
        coVerify { householdDao.householdSyncedWithServer(*anyLongVararg()) }
        coVerify(exactly = 0) { benDao.benSyncWithServerFailed(*anyLongVararg()) }
    }

    @Test
    fun `processNewBen marks batch failed on unexpected inner status`() = runTest {
        loggedIn()
        coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
        val ben = benCacheMock(hhId = 21L, benId = 301L)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns listOf(ben)
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                jsonResponse("""{"statusCode":9999}""")

        assertTrue(repo.processNewBen())

        coVerify { benDao.benSyncWithServerFailed(*anyLongVararg()) }
        coVerify(exactly = 0) { benDao.benSyncedWithServer(*anyLongVararg()) }
    }

    @Test
    fun `processNewBen marks batch failed when push throws`() = runTest {
        loggedIn()
        coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
        val ben = benCacheMock(hhId = 22L, benId = 302L)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns listOf(ben)
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } throws RuntimeException("network down")

        assertTrue(repo.processNewBen())

        coVerify { benDao.benSyncWithServerFailed(*anyLongVararg()) }
    }

    @Test
    fun `processNewBen marks batch failed on malformed json response body`() = runTest {
        loggedIn()
        coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
        val ben = benCacheMock(hhId = 23L, benId = 303L)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns listOf(ben)
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns jsonResponse("not-json")

        assertTrue(repo.processNewBen())

        coVerify(exactly = 1) { tmcNetworkApiService.submitRmnchDataAmrit(any()) }
        coVerify { benDao.benSyncWithServerFailed(*anyLongVararg()) }
    }

    @Test
    fun `processNewBen marks single oversized record failed on 413`() = runTest {
        loggedIn()
        coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
        val ben = benCacheMock(hhId = 24L, benId = 304L)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns listOf(ben)
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns jsonResponse("{}", code = 413)

        assertTrue(repo.processNewBen())

        coVerify(exactly = 1) { tmcNetworkApiService.submitRmnchDataAmrit(any()) }
        coVerify { benDao.benSyncWithServerFailed(*anyLongVararg()) }
    }

    @Test
    fun `processNewBen splits an oversized batch of two and syncs both halves`() = runTest {
        loggedIn()
        coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
        val ben1 = benCacheMock(hhId = 25L, benId = 305L)
        val ben2 = benCacheMock(hhId = 26L, benId = 306L)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns listOf(ben1, ben2)
        coEvery { householdDao.getHousehold(any()) } returns null
        var callCount = 0
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } answers {
            callCount++
            if (callCount == 1) jsonResponse("{}", code = 413) else jsonResponse("""{"statusCode":200}""")
        }

        assertTrue(repo.processNewBen())

        coVerify(exactly = 3) { tmcNetworkApiService.submitRmnchDataAmrit(any()) }
        coVerify(exactly = 2) { benDao.benSyncedWithServer(*anyLongVararg()) }
    }

    @Test
    fun `processNewBen retries batch push after token refresh until exhausted`() = runTest {
        loggedIn()
        coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
        val ben = benCacheMock(hhId = 27L, benId = 307L)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns listOf(ben)
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                jsonResponse("""{"statusCode":5002}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        assertTrue(repo.processNewBen())

        coVerify(exactly = 4) { tmcNetworkApiService.submitRmnchDataAmrit(any()) }
        coVerify { benDao.benSyncWithServerFailed(*anyLongVararg()) }
    }

    @Test
    fun `processNewBen marks batch failed when token refresh fails on 401`() = runTest {
        loggedIn()
        coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
        val ben = benCacheMock(hhId = 28L, benId = 308L)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns listOf(ben)
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                jsonResponse("""{"statusCode":401}""")
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertTrue(repo.processNewBen())

        coVerify(exactly = 1) { tmcNetworkApiService.submitRmnchDataAmrit(any()) }
        coVerify { benDao.benSyncWithServerFailed(*anyLongVararg()) }
    }

    // =====================================================
    // getBeneficiaryWithId: session-expired retry branch
    // =====================================================

    @Test
    fun `getBeneficiaryWithId refreshes token and retries on session expired message`() = runTest {
        loggedIn()
        val expiredJson =
            """{"statusCode":5000,"errorMessage":"Invalid login key or session is expired"}"""
        val successJson = """
            {"statusCode":200,"data":{"BenHealthDetails":[
              {"benHealthID":1,"healthIdNumber":"11-1111-1111","beneficiaryRegID":10,"healthId":"a@abdm","isNewAbha":false}
            ]}}
        """.trimIndent()
        coEvery { tmcNetworkApiService.getBenHealthID(any()) } returnsMany
                listOf(successResponse(expiredJson), successResponse(successJson))
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns true

        val result = repo.getBeneficiaryWithId(10L)

        assertNotNull(result)
        assertEquals("a@abdm", result?.healthId)
        coVerify { userRepo.refreshTokenTmc("asha", "pwd") }
        coVerify(exactly = 2) { tmcNetworkApiService.getBenHealthID(any()) }
    }

    // =====================================================
    // getGeneralOPDBeneficiariesFromServertoWorker: catch branches
    // =====================================================

    @Test
    fun `getGeneralOPD returns -1 on malformed top level json`() = runTest {
        loggedIn()
        coEvery { tmcNetworkApiService.getgeneralOPDBeneficiaries(any()) } returns
                jsonResponse("not-json")

        assertEquals(-1, repo.getGeneralOPDBeneficiariesFromServertoWorker(0))
    }

    @Test
    fun `getGeneralOPD returns -1 when villages list is empty`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { user.villages } returns emptyList()
        coEvery { preferenceDao.getLoggedInUser() } returns user

        assertEquals(-1, repo.getGeneralOPDBeneficiariesFromServertoWorker(0))
    }

    // =====================================================
    // deactivateHouseHold / deactivateBeneficiary success path
    // =====================================================

    @Test
    fun `deactivateHouseHold returns true and triggers pull worker on success`() = runTest {
        loggedIn()
        mockkObject(WorkerUtils)
        every { WorkerUtils.triggerAmritPullWorker(any()) } returns Unit
        val json = """{"statusCode":200,"errorMessage":""}"""
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns jsonResponse(json)

        assertTrue(repo.deactivateHouseHold(emptyList(), mockk<HouseholdNetwork>(relaxed = true)))

        verify { WorkerUtils.triggerAmritPullWorker(context) }
    }

    @Test
    fun `deactivateBeneficiary returns true and triggers pull worker on success`() = runTest {
        loggedIn()
        mockkObject(WorkerUtils)
        every { WorkerUtils.triggerAmritPullWorker(any()) } returns Unit
        val json = """{"statusCode":200,"errorMessage":""}"""
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns jsonResponse(json)

        assertTrue(repo.deactivateBeneficiary(emptyList()))

        verify { WorkerUtils.triggerAmritPullWorker(context) }
    }

    // =====================================================
    // persistRecord: ImageUtils save success / failure branches
    // =====================================================

    @Test
    fun `persistRecord saves image via ImageUtils and updates path on success`() = runTest {
        mockkStatic(Uri::class)
        mockkObject(ImageUtils)
        val filesDir = File(System.getProperty("java.io.tmpdir"), "sakhi_files_test_ok")
        every { context.filesDir } returns filesDir
        val uri = mockk<Uri>(relaxed = true)
        every { uri.scheme } returns "content"
        every { uri.path } returns null
        every { Uri.parse(any()) } returns uri
        coEvery {
            ImageUtils.saveBenImageFromCameraToStorage(context, "content://media/5", 5L)
        } returns "file:///new/path/5.jpeg"

        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.userImage } returns "content://media/5"
        every { ben.beneficiaryId } returns 5L
        coEvery { benDao.upsert(ben) } returns Unit

        repo.persistRecord(ben)

        coVerify { benDao.upsert(ben) }
        verify { ben.userImage = "file:///new/path/5.jpeg" }
    }

    @Test
    fun `persistRecord throws and cleans up orphaned file when ImageUtils save fails`() = runTest {
        mockkStatic(Uri::class)
        mockkObject(ImageUtils)
        val filesDir = File(System.getProperty("java.io.tmpdir"), "sakhi_files_test_fail")
        every { context.filesDir } returns filesDir
        val orphanFile = File.createTempFile("orphan", ".jpg")
        val uri = mockk<Uri>(relaxed = true)
        every { uri.scheme } returns "file"
        every { uri.path } returns orphanFile.absolutePath
        every { Uri.parse(any()) } returns uri
        coEvery {
            ImageUtils.saveBenImageFromCameraToStorage(
                context, "file://${orphanFile.absolutePath}", 6L
            )
        } returns null

        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.userImage } returns "file://${orphanFile.absolutePath}"
        every { ben.beneficiaryId } returns 6L

        try {
            repo.persistRecord(ben)
            assertFalse("Should have thrown", true)
        } catch (e: IllegalStateException) {
            assertEquals("Failed to save beneficiary image", e.message)
        }
        assertFalse(orphanFile.exists())
    }

    // =====================================================
    // processNewBen: createBenIdAtServerByBeneficiarySending / uploadBenBatch
    // additional catch branches
    // =====================================================

    @Test
    fun `processNewBen swallows non refresh socket timeout when creating ben id`() = runTest {
        loggedIn()
        every { context.filesDir } returns tempFilesDir()
        val ben = benCacheMock(hhId = 60L, benId = -60L)
        coEvery { benDao.getAllUnsyncedBen(any()) } returns listOf(ben)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns emptyList()
        coEvery { tmcNetworkApiService.getBenIdFromBeneficiarySending(any<BeneficiaryDataSending>()) } throws
                java.net.SocketTimeoutException("actual timeout")

        assertTrue(repo.processNewBen())

        coVerify(exactly = 1) { tmcNetworkApiService.getBenIdFromBeneficiarySending(any<BeneficiaryDataSending>()) }
    }

    @Test
    fun `processNewBen batch push continues when kid model conversion throws`() = runTest {
        loggedIn()
        coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
        val ben = benCacheMock(hhId = 50L, benId = 500L)
        every { ben.asKidNetworkModel(any()) } throws RuntimeException("bad kid data")
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns listOf(ben)
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                jsonResponse("""{"statusCode":200}""")

        assertTrue(repo.processNewBen())

        coVerify { benDao.benSyncWithServerFailed(*anyLongVararg()) }
    }

    @Test
    fun `processNewBen marks batch failed when session appears logged out during retry`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 42
        every { user.userName } returns "asha"
        every { user.password } returns "pwd"
        every { user.villages } returns listOf(LocationEntity(1, "Village"))
        coEvery { preferenceDao.getLoggedInUser() } returnsMany listOf(user, null)
        coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
        val ben = benCacheMock(hhId = 40L, benId = 400L)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns listOf(ben)
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                jsonResponse("""{"statusCode":5002}""")

        assertTrue(repo.processNewBen())

        coVerify(exactly = 1) { tmcNetworkApiService.submitRmnchDataAmrit(any()) }
        coVerify { benDao.benSyncWithServerFailed(*anyLongVararg()) }
    }

    // =====================================================
    // getBeneficiariesFromServerForWorker: kidDetails / alternate
    // gender & age-unit branches (bornbirthDeatils non-empty)
    // =====================================================

    private fun benPullPayloadWithBirthDetails(totalPage: Int = 2): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 5002,
                "houseoldId": 9002,
                "ashaId": 11,
                "BenRegId": 7002,
                "abhaHealthDetails": {
                  "HealthIdNumber": "12-3456-7891",
                  "HealthID": "kid@abdm",
                  "isNewAbha": false
                },
                "bornbirthDeatils": {
                  "birthPlace": "Hospital"
                },
                "householdDetails": {
                  "familyHeadName": "Head Name",
                  "familyHeadPhoneNo": "9876543210",
                  "type_bpl_apl": "APL",
                  "bpl_aplId": 1,
                  "residentialArea": "Urban",
                  "residentialAreaId": 1,
                  "other_residentialArea": "",
                  "houseType": "Pucca",
                  "houseTypeId": 1,
                  "other_houseType": "",
                  "houseOwnerShip": "Owned",
                  "houseOwnerShipId": 1,
                  "seperateKitchen": "Yes",
                  "seperateKitchenId": 1,
                  "fuelUsed": "LPG",
                  "fuelUsedId": 1,
                  "other_fuelUsed": "",
                  "sourceofDrinkingWater": "Tap",
                  "sourceofDrinkingWaterId": 1,
                  "other_sourceofDrinkingWater": "",
                  "avalabilityofElectricity": "Yes",
                  "avalabilityofElectricityId": 1,
                  "other_avalabilityofElectricity": "",
                  "availabilityofToilet": "Yes",
                  "availabilityofToiletId": 1,
                  "other_availabilityofToilet": "",
                  "serverUpdatedStatus": 1,
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM"
                },
                "beneficiaryDetails": {
                  "firstName": "Kid",
                  "lastName": "Doe",
                  "gender": "Female",
                  "genderId": 2,
                  "age": 5,
                  "age_unit": "Months",
                  "dob": "Jan 15, 2021 10:30:00 AM",
                  "familyHeadRelationPosition": 3,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM",
                  "stateId": 1,
                  "stateName": "State",
                  "districtid": 2,
                  "districtname": "District",
                  "blockId": 3,
                  "blockName": "Block",
                  "villageId": 4,
                  "villageName": "Village"
                }
              }
            ]
          }
        }
    """.trimIndent()

    private fun benPullPayloadUnusualAgeUnit(totalPage: Int = 3): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 5004,
                "houseoldId": 9004,
                "ashaId": 11,
                "BenRegId": 7004,
                "abhaHealthDetails": {
                  "HealthIdNumber": "12-3456-7892",
                  "HealthID": "other@abdm",
                  "isNewAbha": false
                },
                "bornbirthDeatils": {},
                "householdDetails": {
                  "familyHeadName": "Head Name",
                  "familyHeadPhoneNo": "9876543210",
                  "type_bpl_apl": "APL",
                  "bpl_aplId": 1,
                  "residentialArea": "Urban",
                  "residentialAreaId": 1,
                  "other_residentialArea": "",
                  "houseType": "Pucca",
                  "houseTypeId": 1,
                  "other_houseType": "",
                  "houseOwnerShip": "Owned",
                  "houseOwnerShipId": 1,
                  "seperateKitchen": "Yes",
                  "seperateKitchenId": 1,
                  "fuelUsed": "LPG",
                  "fuelUsedId": 1,
                  "other_fuelUsed": "",
                  "sourceofDrinkingWater": "Tap",
                  "sourceofDrinkingWaterId": 1,
                  "other_sourceofDrinkingWater": "",
                  "avalabilityofElectricity": "Yes",
                  "avalabilityofElectricityId": 1,
                  "other_avalabilityofElectricity": "",
                  "availabilityofToilet": "Yes",
                  "availabilityofToiletId": 1,
                  "other_availabilityofToilet": "",
                  "serverUpdatedStatus": 1,
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM"
                },
                "beneficiaryDetails": {
                  "firstName": "Sam",
                  "lastName": "Doe",
                  "gender": "Other",
                  "genderId": 3,
                  "age": 30,
                  "age_unit": "Weeks",
                  "dob": "Jan 15, 1996 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM",
                  "stateId": 1,
                  "stateName": "State",
                  "districtid": 2,
                  "districtname": "District",
                  "blockId": 3,
                  "blockName": "Block",
                  "villageId": 4,
                  "villageName": "Village"
                }
              }
            ]
          }
        }
    """.trimIndent()

    private fun benPullPayloadSparseHousehold(totalPage: Int = 1): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 5003,
                "houseoldId": 9003,
                "ashaId": 11,
                "BenRegId": 7003,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {
                  "familyHeadName": "Head Name",
                  "familyHeadPhoneNo": "9876543210",
                  "type_bpl_apl": "APL",
                  "bpl_aplId": 1,
                  "residentialArea": "Urban",
                  "residentialAreaId": 1,
                  "other_residentialArea": "",
                  "houseType": "Pucca",
                  "houseTypeId": 1,
                  "other_houseType": "",
                  "houseOwnerShip": "Owned",
                  "houseOwnerShipId": 1,
                  "seperateKitchen": "Yes",
                  "seperateKitchenId": 1,
                  "fuelUsed": "LPG",
                  "fuelUsedId": 1,
                  "other_fuelUsed": "",
                  "sourceofDrinkingWater": "Tap",
                  "sourceofDrinkingWaterId": 1,
                  "other_sourceofDrinkingWater": "",
                  "avalabilityofElectricity": "Yes",
                  "avalabilityofElectricityId": 1,
                  "other_avalabilityofElectricity": "",
                  "availabilityofToilet": "Yes",
                  "availabilityofToiletId": 1,
                  "other_availabilityofToilet": "",
                  "serverUpdatedStatus": 1,
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM"
                },
                "beneficiaryDetails": {
                  "stateId": 1,
                  "stateName": "State",
                  "districtid": 2,
                  "districtname": "District",
                  "blockId": 3,
                  "blockName": "Block",
                  "villageId": 4,
                  "villageName": "Village"
                }
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker builds kidDetails and covers alternate gender age unit branches`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(any(), any()) } returns null
            var captured: BenRegCache? = null
            coEvery { benDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                val list = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }
                captured = list.filterIsInstance<BenRegCache>().firstOrNull()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadWithBirthDetails())

            assertEquals(2, repo.getBeneficiariesFromServerForWorker(0))
            assertNotNull(captured)
            assertNotNull(captured?.kidDetails)
            assertNull(captured?.genDetails)
            assertEquals(AgeUnit.MONTHS, captured?.ageUnit)
            assertEquals(2, captured?.ageUnitId)
            assertEquals(Gender.FEMALE, captured?.gender)
        }

    @Test
    fun `getBeneficiariesFromServerForWorker maps transgender and default age unit branches`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(any(), any()) } returns null
            var captured: BenRegCache? = null
            coEvery { benDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                val list = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }
                captured = list.filterIsInstance<BenRegCache>().firstOrNull()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadUnusualAgeUnit())

            assertEquals(3, repo.getBeneficiariesFromServerForWorker(0))
            assertNotNull(captured)
            assertEquals(Gender.TRANSGENDER, captured?.gender)
            assertEquals(AgeUnit.YEARS, captured?.ageUnit)
            assertEquals(3, captured?.ageUnitId)
        }

    @Test
    fun `getBeneficiariesFromServerForWorker builds household with defaults when optional fields omitted`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns null
            var captured: HouseholdCache? = null
            coEvery { householdDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                val list = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }
                captured = list.filterIsInstance<HouseholdCache>().firstOrNull()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadSparseHousehold())

            assertEquals(1, repo.getBeneficiariesFromServerForWorker(0))
            assertNotNull(captured)
            assertNull(captured?.registrationType)
            assertFalse(captured?.isDeactivate ?: true)
        }

    // =====================================================
    // verifyOtp: statusCode 200 success branch
    // =====================================================

    @Test
    fun `verifyOtp returns parsed response and marks otp verified on statusCode 200`() = runTest {
        val json = """
            {
              "statusCode": 200,
              "data": {"userName": "asha", "userId": "1"},
              "errorMessage": "",
              "status": "SUCCESS"
            }
        """.trimIndent()
        coEvery { tmcNetworkApiService.validateOtp(any()) } returns successResponse(json)

        val result = repo.verifyOtp("9999999999", 1234)

        assertNotNull(result)
        assertEquals("SUCCESS", result?.status)
        assertEquals("asha", result?.data?.userName)
    }

    // =====================================================
    // deactivateHouseHold / deactivateBeneficiary: non-empty ben list
    // exercises the asNetworkPostModel mapping lambda
    // =====================================================

    @Test
    fun `deactivateHouseHold maps a non-empty ben list before failing on non-200 http`() =
        runTest {
            loggedIn()
            val ben = benCacheMock(hhId = 80L, benId = 800L)
            coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                    jsonResponse("{}", code = 500)

            assertFalse(
                repo.deactivateHouseHold(listOf(ben), mockk<HouseholdNetwork>(relaxed = true))
            )
        }

    @Test
    fun `deactivateBeneficiary maps a non-empty ben list before failing on non-200 http`() =
        runTest {
            loggedIn()
            val ben = benCacheMock(hhId = 81L, benId = 801L)
            coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                    jsonResponse("{}", code = 500)

            assertFalse(repo.deactivateBeneficiary(listOf(ben)))
        }

    // ---------------- deactivateHouseHold / deactivateBeneficiary: ----------------
    // session appears logged out on the retry fetch of the user during a 5002 response

    @Test
    fun `deactivateHouseHold returns false when session appears logged out during retry`() =
        runTest {
            val user = mockk<User>(relaxed = true)
            every { user.userId } returns 42
            every { user.userName } returns "asha"
            every { user.password } returns "pwd"
            every { user.villages } returns listOf(LocationEntity(1, "Village"))
            coEvery { preferenceDao.getLoggedInUser() } returnsMany listOf(user, null)
            val json = """{"statusCode":5002,"errorMessage":"expired"}"""
            coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                    jsonResponse(json, code = 200)

            assertFalse(
                repo.deactivateHouseHold(emptyList(), mockk<HouseholdNetwork>(relaxed = true))
            )

            coVerify(exactly = 1) { tmcNetworkApiService.submitRmnchDataAmrit(any()) }
        }

    @Test
    fun `deactivateBeneficiary returns false when session appears logged out during retry`() =
        runTest {
            val user = mockk<User>(relaxed = true)
            every { user.userId } returns 42
            every { user.userName } returns "asha"
            every { user.password } returns "pwd"
            every { user.villages } returns listOf(LocationEntity(1, "Village"))
            coEvery { preferenceDao.getLoggedInUser() } returnsMany listOf(user, null)
            val json = """{"statusCode":5002,"errorMessage":"expired"}"""
            coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                    jsonResponse(json, code = 200)

            assertFalse(repo.deactivateBeneficiary(emptyList()))

            coVerify(exactly = 1) { tmcNetworkApiService.submitRmnchDataAmrit(any()) }
        }

    // =====================================================
    // processNewBen: postDataToAmritServer null-body-on-200 branch
    // =====================================================

    @Test
    fun `processNewBen marks batch failed when submit response body is null on success status`() =
        runTest {
            loggedIn()
            coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
            val ben = benCacheMock(hhId = 70L, benId = 700L)
            coEvery { benDao.getAllBenForSyncWithServer(any()) } returns listOf(ben)
            val response = mockk<Response<ResponseBody>>(relaxed = true)
            every { response.code() } returns 200
            every { response.body() } returns null
            coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns response

            assertTrue(repo.processNewBen())

            coVerify { benDao.benSyncWithServerFailed(*anyLongVararg()) }
        }

    // =====================================================
    // getUserDetailsByAyushman: parseFamilyMembers nested-array-key branch
    // =====================================================

    @Test
    fun `getUserDetailsByAyushman parses a JSONArray held under a nested data key`() = runTest {
        val json = """{"result":[{"cardNo":"C1"},{"cardNo":"C2"}]}"""
        coEvery { tmcNetworkApiService.getUserDetailsByAyushmanCardNo(any()) } returns
                successResponse(json)

        val result = repo.getUserDetailsByAyushmanAbhaCardNo("CARD1", "HH1")

        assertTrue(result is NetworkResult.Success)
        assertEquals(2, ((result as NetworkResult.Success<*>).data as List<*>).size)
    }

    // =====================================================
    // getBeneficiariesFromServerForWorker: gender / ageUnit / reproductiveStatusId
    // switch-branch coverage (has("gender") false, "Days" unit, unmapped genderId,
    // mobileOthers/religionOthers non-empty, isDeactivate true, and every
    // reproductiveStatusId mapping branch)
    // =====================================================

    private fun benPullPayloadGenBranches(totalPage: Int = 6): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 6001,
                "houseoldId": 9201,
                "ashaId": 11,
                "BenRegId": 7201,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Male",
                  "genderId": 4,
                  "age": 10,
                  "age_unit": "Days",
                  "dob": "Jan 15, 2016 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM",
                  "reproductiveStatusId": 0
                }
              },
              {
                "benficieryid": 6002,
                "houseoldId": 9202,
                "ashaId": 11,
                "BenRegId": 7202,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "genderId": 1,
                  "age": 30,
                  "age_unit": "Months",
                  "dob": "Jan 15, 1996 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM",
                  "reproductiveStatusId": 1
                }
              },
              {
                "benficieryid": 6003,
                "houseoldId": 9203,
                "ashaId": 11,
                "BenRegId": 7203,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Transgender",
                  "genderId": 3,
                  "age": 25,
                  "age_unit": "Years",
                  "dob": "Jan 15, 1996 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "mobileOthers": "9998887776",
                  "religionOthers": "Custom Religion",
                  "isDeactivate": true,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM",
                  "reproductiveStatusId": 4
                }
              },
              {
                "benficieryid": 6004,
                "houseoldId": 9204,
                "ashaId": 11,
                "BenRegId": 7204,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Male",
                  "genderId": 1,
                  "age": 40,
                  "age_unit": "Years",
                  "dob": "Jan 15, 1986 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM",
                  "reproductiveStatusId": 5
                }
              },
              {
                "benficieryid": 6005,
                "houseoldId": 9205,
                "ashaId": 11,
                "BenRegId": 7205,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Female",
                  "genderId": 2,
                  "age": 22,
                  "age_unit": "Years",
                  "dob": "Jan 15, 2004 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM",
                  "reproductiveStatusId": 6
                }
              },
              {
                "benficieryid": 6006,
                "houseoldId": 9206,
                "ashaId": 11,
                "BenRegId": 7206,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Male",
                  "genderId": 1,
                  "age": 33,
                  "age_unit": "Years",
                  "dob": "Jan 15, 1993 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM",
                  "reproductiveStatusId": 99
                }
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker covers gender ageUnit and reproductiveStatusId switch branches`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(any(), any()) } returns null
            var captured: List<BenRegCache> = emptyList()
            coEvery { benDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                captured = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }.filterIsInstance<BenRegCache>()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadGenBranches())

            assertEquals(6, repo.getBeneficiariesFromServerForWorker(0))
            assertEquals(6, captured.size)

            val recordA = captured.first { it.beneficiaryId == 6001L }
            assertEquals(AgeUnit.DAYS, recordA.ageUnit)
            assertEquals(1, recordA.ageUnitId)
            assertEquals(Gender.MALE, recordA.gender)
            assertEquals(0, recordA.genDetails?.reproductiveStatusId)

            val recordB = captured.first { it.beneficiaryId == 6002L }
            assertNull(recordB.ageUnit)
            assertNull(recordB.gender)
            assertEquals(1, recordB.genDetails?.reproductiveStatusId)

            val recordC = captured.first { it.beneficiaryId == 6003L }
            assertEquals("9998887776", recordC.mobileOthers)
            assertEquals("Custom Religion", recordC.religionOthers)
            assertTrue(recordC.isDeactivate)
            assertEquals(3, recordC.genDetails?.reproductiveStatusId)

            val recordD = captured.first { it.beneficiaryId == 6004L }
            assertEquals(4, recordD.genDetails?.reproductiveStatusId)

            val recordE = captured.first { it.beneficiaryId == 6005L }
            assertEquals(5, recordE.genDetails?.reproductiveStatusId)

            val recordF = captured.first { it.beneficiaryId == 6006L }
            assertEquals(5, recordF.genDetails?.reproductiveStatusId)
        }

    // =====================================================
    // getBeneficiariesFromServerForWorker: fully populated kidDetails
    // (every bornbirthDeatils has()-guarded field present)
    // =====================================================

    private fun benPullPayloadFullKidDetails(totalPage: Int = 1): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 6101,
                "houseoldId": 9301,
                "ashaId": 11,
                "BenRegId": 7301,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {
                  "birthPlace": "Hospital",
                  "birthPlaceid": 1,
                  "facilityName": "City Hospital",
                  "facilityid": 2,
                  "facilityOther": "",
                  "placeName": "Ward 5",
                  "conductedDelivery": "Doctor",
                  "conductedDeliveryid": 1,
                  "conductedDeliveryOther": "",
                  "deliveryType": "Normal",
                  "deliveryTypeid": 1,
                  "complecations": "None",
                  "complecationsid": 0,
                  "complicationsOther": "",
                  "term": "Full Term",
                  "termid": 1,
                  "gestationalAgeid": 2,
                  "corticosteroidGivenMotherid": 0,
                  "criedImmediately": "Yes",
                  "criedImmediatelyid": 1,
                  "birthDefects": "None",
                  "birthDefectsid": 0,
                  "birthDefectsOthers": "",
                  "heightAtBirth": 49.5,
                  "weightAtBirth": 3.2,
                  "feedingStarted": "Immediately",
                  "feedingStartedid": 1,
                  "birthDosage": "Full",
                  "birthDosageid": 1,
                  "opvBatchNo": "OPV123",
                  "bcdBatchNo": "BCG123",
                  "hptdBatchNo": "HPT123",
                  "vitaminkBatchNo": "VITK123",
                  "deliveryTypeOther": "",
                  "birthBCG": true,
                  "birthHepB": true,
                  "birthOPV": "true"
                },
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Female",
                  "genderId": 2,
                  "age": 5,
                  "age_unit": "Years",
                  "dob": "Jan 15, 2021 10:30:00 AM",
                  "familyHeadRelationPosition": 3,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM",
                  "childRegisteredAWCID": 1,
                  "childRegisteredSchoolID": 2,
                  "typeofSchoolID": 3
                }
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker builds a fully populated kidDetails record`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(any(), any()) } returns null
            var captured: BenRegCache? = null
            coEvery { benDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                val list = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }
                captured = list.filterIsInstance<BenRegCache>().firstOrNull()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadFullKidDetails())

            assertEquals(1, repo.getBeneficiariesFromServerForWorker(0))
            val kid = captured?.kidDetails
            assertNotNull(kid)
            assertNull(captured?.genDetails)
            assertEquals(1, kid?.childRegisteredAWCId)
            assertEquals(2, kid?.childRegisteredSchoolId)
            assertEquals(3, kid?.typeOfSchoolId)
            assertEquals("Hospital", kid?.birthPlace)
            assertEquals(1, kid?.birthPlaceId)
            assertEquals("City Hospital", kid?.facilityName)
            assertEquals(2, kid?.facilityId)
            assertEquals("Ward 5", kid?.placeName)
            assertEquals("Doctor", kid?.conductedDelivery)
            assertEquals(1, kid?.conductedDeliveryId)
            assertEquals("Normal", kid?.deliveryType)
            assertEquals(1, kid?.deliveryTypeId)
            assertEquals("None", kid?.complications)
            assertEquals(0, kid?.complicationsId)
            assertEquals("Full Term", kid?.term)
            assertEquals(1, kid?.termId)
            assertEquals(2, kid?.gestationalAgeId)
            assertEquals(0, kid?.corticosteroidGivenMotherId)
            assertEquals("Yes", kid?.criedImmediately)
            assertEquals(1, kid?.criedImmediatelyId)
            assertEquals("None", kid?.birthDefects)
            assertEquals(0, kid?.birthDefectsId)
            assertEquals(49.5, kid?.heightAtBirth)
            assertEquals(3.2, kid?.weightAtBirth)
            assertEquals("Immediately", kid?.feedingStarted)
            assertEquals(1, kid?.feedingStartedId)
            assertEquals("Full", kid?.birthDosage)
            assertEquals(1, kid?.birthDosageId)
            assertEquals("OPV123", kid?.opvBatchNo)
            assertEquals("BCG123", kid?.bcdBatchNo)
            assertEquals("HPT123", kid?.hptBatchNo)
            assertEquals("VITK123", kid?.vitaminKBatchNo)
            assertTrue(kid?.birthBCG ?: false)
            assertTrue(kid?.birthHepB ?: false)
            assertTrue(kid?.birthOPV ?: false)
        }

    // =====================================================
    // getBenCacheFromServerResponse: benExists skip, death detail
    // fields, per-record JSONException / NumberFormatException
    // recovery and the user_image branch
    // =====================================================

    @Test
    fun `getBeneficiariesFromServerForWorker skips ben cache record when ben already exists locally`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(9001L, 5001L) } returns mockk(relaxed = true)
            var upsertedBens = -1
            coEvery { benDao.upsert(*anyVararg()) } answers {
                upsertedBens = varargSize(arg<Any?>(0))
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayload())

            assertEquals(3, repo.getBeneficiariesFromServerForWorker(0))
            assertEquals(0, upsertedBens)
        }

    private fun benPullPayloadDeathDetails(totalPage: Int = 1): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 6301,
                "houseoldId": 9401,
                "ashaId": 11,
                "BenRegId": 7401,
                "isDeath": true,
                "dateOfDeath": "Jan 20, 2026 10:00:00 AM",
                "timeOfDeath": "10:00 AM",
                "reasonOfDeath": "Illness",
                "reasonOfDeathId": 2,
                "placeOfDeath": "Home",
                "placeOfDeathId": 1,
                "otherPlaceOfDeath": "",
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Male",
                  "genderId": 1,
                  "age": 30,
                  "age_unit": "Years",
                  "dob": "Jan 15, 1996 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM"
                }
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker maps full death detail fields when isDeath is true`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(any(), any()) } returns null
            var captured: BenRegCache? = null
            coEvery { benDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                val list = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }
                captured = list.filterIsInstance<BenRegCache>().firstOrNull()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadDeathDetails())

            assertEquals(1, repo.getBeneficiariesFromServerForWorker(0))
            assertNotNull(captured)
            assertTrue(captured?.isDeath ?: false)
            assertEquals("true", captured?.isDeathValue)
            assertEquals("Jan 20, 2026 10:00:00 AM", captured?.dateOfDeath)
            assertEquals("10:00 AM", captured?.timeOfDeath)
            assertEquals("Illness", captured?.reasonOfDeath)
            assertEquals(2, captured?.reasonOfDeathId)
            assertEquals("Home", captured?.placeOfDeath)
            assertEquals(1, captured?.placeOfDeathId)
            assertNull(captured?.otherPlaceOfDeath)
        }

    private fun benPullPayloadOneMissingAshaId(totalPage: Int = 2): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 6401,
                "houseoldId": 9501,
                "BenRegId": 7501,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Male",
                  "genderId": 1,
                  "age": 30,
                  "age_unit": "Years",
                  "dob": "Jan 15, 1996 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM"
                }
              },
              {
                "benficieryid": 6402,
                "houseoldId": 9502,
                "ashaId": 11,
                "BenRegId": 7502,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Female",
                  "genderId": 2,
                  "age": 28,
                  "age_unit": "Years",
                  "dob": "Jan 15, 1998 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM"
                }
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker skips record that throws JSONException but keeps processing others`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(any(), any()) } returns null
            var captured: List<BenRegCache> = emptyList()
            coEvery { benDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                captured = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }.filterIsInstance<BenRegCache>()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadOneMissingAshaId())

            assertEquals(2, repo.getBeneficiariesFromServerForWorker(0))
            assertEquals(1, captured.size)
            assertEquals(6402L, captured[0].beneficiaryId)
        }

    private fun benPullPayloadOneBadContactNumber(totalPage: Int = 2): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 6501,
                "houseoldId": 9601,
                "ashaId": 11,
                "BenRegId": 7601,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Male",
                  "genderId": 1,
                  "age": 30,
                  "age_unit": "Years",
                  "dob": "Jan 15, 1996 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "contact_number": "not-a-number",
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM"
                }
              },
              {
                "benficieryid": 6502,
                "houseoldId": 9602,
                "ashaId": 11,
                "BenRegId": 7602,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Female",
                  "genderId": 2,
                  "age": 28,
                  "age_unit": "Years",
                  "dob": "Jan 15, 1998 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM"
                }
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker skips record with non numeric contact number but keeps processing others`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(any(), any()) } returns null
            var captured: List<BenRegCache> = emptyList()
            coEvery { benDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                captured = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }.filterIsInstance<BenRegCache>()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadOneBadContactNumber())

            assertEquals(2, repo.getBeneficiariesFromServerForWorker(0))
            assertEquals(1, captured.size)
            assertEquals(6502L, captured[0].beneficiaryId)
        }

    private fun benPullPayloadWithUserImage(totalPage: Int = 1): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 6601,
                "houseoldId": 9701,
                "ashaId": 11,
                "BenRegId": 7701,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Male",
                  "genderId": 1,
                  "age": 30,
                  "age_unit": "Years",
                  "dob": "Jan 15, 1996 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "user_image": "ZmFrZS1pbWFnZS1ieXRlcw==",
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM"
                }
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker attempts to save server image when user_image field present`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(any(), any()) } returns null
            var captured: BenRegCache? = null
            coEvery { benDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                val list = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }
                captured = list.filterIsInstance<BenRegCache>().firstOrNull()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadWithUserImage())

            assertEquals(1, repo.getBeneficiariesFromServerForWorker(0))
            assertNotNull(captured)
            assertEquals(6601L, captured?.beneficiaryId)
        }

    // =====================================================
    // getBenIdsFromLocal: minBenId elvis-null branch
    // =====================================================

    @Test
    fun `getBenIdsGeneratedFromServer floors minBenId to -1 when dao returns null`() = runTest {
        loggedIn()
        coEvery { benIdGenDao.count() } returns 0
        coEvery { benDao.getMinBenId() } returns null
        var insertedCount = -1
        coEvery { benIdGenDao.insert(*anyVararg()) } answers {
            insertedCount = varargSize(arg<Any?>(0))
        }

        repo.getBenIdsGeneratedFromServer(maxCount = 3)

        coVerify { benDao.getMinBenId() }
        assertEquals(3, insertedCount)
    }

    // =====================================================
    // createBenIdAtServerByBeneficiarySending: infantReg?.let
    // null / non-null and processed-flag branches
    // =====================================================

    @Test
    fun `processNewBen skips infant reg update when no infant registration found for child ben id`() =
        runTest {
            loggedIn()
            every { context.filesDir } returns tempFilesDir()
            val ben = benCacheMock(hhId = 15L, benId = -11L)
            coEvery { benDao.getAllUnsyncedBen(any()) } returns listOf(ben)
            coEvery { benDao.getAllBenForSyncWithServer(any()) } returns emptyList()
            coEvery { infantRegRepo.getInfantRegFromChildBenId(-11L) } returns null
            val json = """{"statusCode":200,"data":{"response":"ok","benGenId":"77","benRegId":"78"}}"""
            coEvery { tmcNetworkApiService.getBenIdFromBeneficiarySending(any<BeneficiaryDataSending>()) } returns
                    jsonResponse(json)

            assertTrue(repo.processNewBen())

            coVerify(exactly = 0) { infantRegRepo.update(any()) }
        }

    @Test
    fun `processNewBen keeps infant reg processed as N and still updates child ben id when found`() =
        runTest {
            loggedIn()
            every { context.filesDir } returns tempFilesDir()
            val ben = benCacheMock(hhId = 16L, benId = -12L)
            coEvery { benDao.getAllUnsyncedBen(any()) } returns listOf(ben)
            coEvery { benDao.getAllBenForSyncWithServer(any()) } returns emptyList()
            val infantReg = mockk<InfantRegCache>(relaxed = true)
            every { infantReg.processed } returns "N"
            coEvery { infantRegRepo.getInfantRegFromChildBenId(-12L) } returns infantReg
            val json = """{"statusCode":200,"data":{"response":"ok","benGenId":"81","benRegId":"82"}}"""
            coEvery { tmcNetworkApiService.getBenIdFromBeneficiarySending(any<BeneficiaryDataSending>()) } returns
                    jsonResponse(json)

            assertTrue(repo.processNewBen())

            verify { infantReg.childBenId = 81L }
            verify { infantReg.syncState = SyncState.UNSYNCED }
            verify(exactly = 0) { infantReg.processed = "U" }
            coVerify { infantRegRepo.update(infantReg) }
        }

    // =====================================================
    // createBenIdAtServerByBeneficiarySending: 401 branch of
    // (responseStatusCode == 5002 || responseStatusCode == 401)
    // =====================================================

    @Test
    fun `processNewBen sets ben unsynced when create-ben-id returns 401 and refresh fails`() = runTest {
        loggedIn()
        every { context.filesDir } returns tempFilesDir()
        val ben = benCacheMock(hhId = 14L, benId = -10L)
        coEvery { benDao.getAllUnsyncedBen(any()) } returns listOf(ben)
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns emptyList()
        val json = """{"statusCode":401,"errorMessage":"unauthorized"}"""
        coEvery { tmcNetworkApiService.getBenIdFromBeneficiarySending(any<BeneficiaryDataSending>()) } returns
                jsonResponse(json)
        coEvery { userRepo.refreshTokenTmc(any(), any()) } returns false

        assertTrue(repo.processNewBen())

        coVerify { userRepo.refreshTokenTmc("asha", "pwd") }
        coVerify { benDao.setSyncState(14L, -10L, SyncState.UNSYNCED) }
    }

    // =====================================================
    // uploadBenBatch: kid-network-model inclusion condition
    // (it.ageUnitId != 3 || it.age < 15)
    // =====================================================

    @Test
    fun `processNewBen does not build kid network model for an adult beneficiary`() = runTest {
        loggedIn()
        val ben = benCacheMock(hhId = 71L, benId = 701L)
        every { ben.ageUnitId } returns 3
        every { ben.age } returns 20
        coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns listOf(ben)
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                jsonResponse("""{"statusCode":200}""")

        assertTrue(repo.processNewBen())

        verify(exactly = 0) { ben.asKidNetworkModel(any()) }
    }

    @Test
    fun `processNewBen builds kid network model when age is under 15 despite ageUnitId 3`() = runTest {
        loggedIn()
        val ben = benCacheMock(hhId = 72L, benId = 702L)
        every { ben.ageUnitId } returns 3
        every { ben.age } returns 10
        coEvery { benDao.getAllUnsyncedBen(any()) } returns emptyList()
        coEvery { benDao.getAllBenForSyncWithServer(any()) } returns listOf(ben)
        coEvery { tmcNetworkApiService.submitRmnchDataAmrit(any()) } returns
                jsonResponse("""{"statusCode":200}""")

        assertTrue(repo.processNewBen())

        verify { ben.asKidNetworkModel(any()) }
    }

    // =====================================================
    // getBeneficiariesFromServer (BenBasicDomain): isDeath else
    // branch and reasonOfDeathId/placeOfDeathId elvis fallback
    // =====================================================

    private fun basicDomainDeathDefaultsPayload(totalPage: Int = 1): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 8001,
                "houseoldId": 9801,
                "ashaId": 11,
                "BenRegId": 7801,
                "reasonOfDeathId": 0,
                "placeOfDeathId": 0,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": { "familyHeadName": "Head Name" },
                "beneficiaryDetails": {
                  "registrationDate": "Jan 15, 2026 10:30:00 AM",
                  "firstName": "John",
                  "lastName": "Doe",
                  "gender": "Male",
                  "age": 30,
                  "contact_number": "9876543210",
                  "fatherName": "Father Name",
                  "rchid": "RCH123",
                  "hrpStatus": false,
                  "reproductiveStatusId": 2
                }
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServer defaults isDeath false and reasonPlaceOfDeathId to -1 when zero`() =
        runTest {
            loggedIn()
            coEvery { benDao.getBen(any(), any()) } returns null
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(basicDomainDeathDefaultsPayload())

            val (pageSize, list) = repo.getBeneficiariesFromServer(0)

            assertEquals(1, pageSize)
            assertEquals(1, list.size)
            assertFalse(list[0].isDeath)
            assertEquals(-1, list[0].reasonOfDeathId)
            assertEquals(-1, list[0].placeOfDeathId)
        }

    // =====================================================
    // getBeneficiariesFromServerForWorker (BenRegCache): same
    // reasonOfDeathId/placeOfDeathId elvis fallback
    // =====================================================

    private fun benPullPayloadDeathDetailsZeroIds(totalPage: Int = 1): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 6302,
                "houseoldId": 9402,
                "ashaId": 11,
                "BenRegId": 7402,
                "isDeath": true,
                "reasonOfDeathId": 0,
                "placeOfDeathId": 0,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Male","genderId": 1,"age": 30,"age_unit": "Years",
                  "dob": "Jan 15, 1996 10:30:00 AM","familyHeadRelationPosition": 1,
                  "latitude": 12.34,"longitude": 56.78,"aadha_noId": 0,
                  "stateId": 1, "stateName": "State","districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block","villageId": 4, "villageName": "Village",
                  "createdBy": "asha","createdDate": "Jan 15, 2026 10:30:00 AM"
                }
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker maps reasonOfDeathId and placeOfDeathId to -1 when zero`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(any(), any()) } returns null
            var captured: BenRegCache? = null
            coEvery { benDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                val list = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }
                captured = list.filterIsInstance<BenRegCache>().firstOrNull()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadDeathDetailsZeroIds())

            assertEquals(1, repo.getBeneficiariesFromServerForWorker(0))
            assertEquals(-1, captured?.reasonOfDeathId)
            assertEquals(-1, captured?.placeOfDeathId)
        }

    // =====================================================
    // saveGeneralOPDData: non-empty entries mapping branch
    // =====================================================

    @Test
    fun `getGeneralOPD maps and stores non-empty entries`() = runTest {
        loggedIn()
        val json = """{"statusCode":200,"data":{"entries":[{"beneficiaryId":501}]}}"""
        coEvery { tmcNetworkApiService.getgeneralOPDBeneficiaries(any()) } returns jsonResponse(json)
        var captured: List<Any?>? = null
        coEvery { generalOpdDao.insertAll(any()) } answers {
            captured = arg<List<Any?>>(0)
        }

        assertEquals(1, repo.getGeneralOPDBeneficiariesFromServertoWorker(0))
        assertEquals(1, captured?.size)
    }

    // =====================================================
    // getBenCacheFromServerResponse: household-not-found
    // continue branch
    // =====================================================

    @Test
    fun `getBeneficiariesFromServerForWorker skips ben cache entry when household not found locally`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns null
            coEvery { benDao.getBen(any(), any()) } returns null
            var upsertedBens = -1
            coEvery { benDao.upsert(*anyVararg()) } answers {
                upsertedBens = varargSize(arg<Any?>(0))
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayload())

            assertEquals(3, repo.getBeneficiariesFromServerForWorker(0))
            assertEquals(0, upsertedBens)
        }

    // =====================================================
    // getBenCacheFromServerResponse: birthPlace else-null branch
    // (bornbirthDeatils non-empty but without the "birthPlace" key)
    // =====================================================

    private fun benPullPayloadKidNoBirthPlace(totalPage: Int = 1): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 6102,
                "houseoldId": 9302,
                "ashaId": 11,
                "BenRegId": 7302,
                "abhaHealthDetails": {},
                "bornbirthDeatils": { "birthDefects": "None" },
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Female","genderId": 2,"age": 5,"age_unit": "Years",
                  "dob": "Jan 15, 2021 10:30:00 AM","familyHeadRelationPosition": 3,
                  "latitude": 12.34,"longitude": 56.78,"aadha_noId": 0,
                  "stateId": 1, "stateName": "State","districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block","villageId": 4, "villageName": "Village",
                  "createdBy": "asha","createdDate": "Jan 15, 2026 10:30:00 AM"
                }
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker leaves birthPlace null when bornbirthDeatils lacks the key`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(any(), any()) } returns null
            var captured: BenRegCache? = null
            coEvery { benDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                val list = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }
                captured = list.filterIsInstance<BenRegCache>().firstOrNull()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadKidNoBirthPlace())

            assertEquals(1, repo.getBeneficiariesFromServerForWorker(0))
            assertNotNull(captured?.kidDetails)
            assertNull(captured?.kidDetails?.birthPlace)
            assertEquals("None", captured?.kidDetails?.birthDefects)
        }

    // =====================================================
    // getBenCacheFromServerResponse: healthIdDetails isNewAbha
    // else-false branch (abhaHealthDetails present without the
    // "isNewAbha" key)
    // =====================================================

    private fun benPullPayloadAbhaNoIsNewFlag(totalPage: Int = 1): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 6103,
                "houseoldId": 9303,
                "ashaId": 11,
                "BenRegId": 7303,
                "abhaHealthDetails": {
                  "HealthIdNumber": "33-3333-3333",
                  "HealthID": "noisnewflag@abdm"
                },
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Male","genderId": 1,"age": 30,"age_unit": "Years",
                  "dob": "Jan 15, 1996 10:30:00 AM","familyHeadRelationPosition": 1,
                  "latitude": 12.34,"longitude": 56.78,"aadha_noId": 0,
                  "stateId": 1, "stateName": "State","districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block","villageId": 4, "villageName": "Village",
                  "createdBy": "asha","createdDate": "Jan 15, 2026 10:30:00 AM"
                }
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker defaults healthIdDetails isNewAbha to false when key absent`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(any(), any()) } returns null
            var captured: BenRegCache? = null
            coEvery { benDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                val list = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }
                captured = list.filterIsInstance<BenRegCache>().firstOrNull()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadAbhaNoIsNewFlag())

            assertEquals(1, repo.getBeneficiariesFromServerForWorker(0))
            assertNotNull(captured?.healthIdDetails)
            assertFalse(captured?.healthIdDetails?.isNewAbha ?: true)
            assertEquals("noisnewflag@abdm", captured?.healthIdDetails?.healthId)
        }

    // =====================================================
    // getHouseholdCacheFromServerResponse: per-record
    // JSONException catch (malformed household/beneficiary
    // details don't abort the whole page)
    // =====================================================

    private fun benPullPayloadEmptyHouseholdAndBenDetails(totalPage: Int = 1): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 6401,
                "houseoldId": 9401,
                "ashaId": 1,
                "BenRegId": 7401,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {}
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker skips malformed household record without throwing`() =
        runTest {
            loggedIn()
            coEvery { householdDao.getHousehold(any()) } returns null
            coEvery { benDao.getBen(any(), any()) } returns null
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadEmptyHouseholdAndBenDetails())
            var upsertedHouseholds = -1
            coEvery { householdDao.upsert(*anyVararg()) } answers {
                upsertedHouseholds = varargSize(arg<Any?>(0))
            }

            assertEquals(1, repo.getBeneficiariesFromServerForWorker(0))
            assertEquals(0, upsertedHouseholds)
        }

    // =====================================================
    // getHouseholdCacheFromServerResponse: literal string "null"
    // normalization branches (houseNo/wardNo/wardName/mohallaName/
    // residentialArea)
    // =====================================================

    @Test
    fun `getBeneficiariesFromServerForWorker normalizes literal-string null household fields to null`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns null
            var captured: HouseholdCache? = null
            coEvery { householdDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                val list = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }
                captured = list.filterIsInstance<HouseholdCache>().firstOrNull()
            }
            val json = benPullPayload()
                .replace("\"houseno\": \"12A\",", "\"houseno\": \"null\",")
                .replace("\"wardNo\": \"3\",", "\"wardNo\": \"null\",")
                .replace("\"wardName\": \"Ward 3\",", "\"wardName\": \"null\",")
                .replace("\"mohallaName\": \"Mohalla\",", "\"mohallaName\": \"null\",")
                .replace("\"residentialArea\": \"Urban\",", "\"residentialArea\": \"null\",")
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns jsonResponse(json)

            assertEquals(3, repo.getBeneficiariesFromServerForWorker(0))
            assertNotNull(captured)
            assertNull(captured?.family?.houseNo)
            assertNull(captured?.family?.wardNo)
            assertNull(captured?.family?.wardName)
            assertNull(captured?.family?.mohallaName)
            assertNull(captured?.details?.residentialArea)
        }

    // =====================================================
    // getBenCacheFromServerResponse: reserved debug-id logging
    // block (benId == 700623622919L) and otherPlaceOfDeath
    // non-empty branch
    // =====================================================

    private fun benPullPayloadDebugIdWithOtherPlaceOfDeath(totalPage: Int = 1): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 700623622919,
                "houseoldId": 9801,
                "ashaId": 11,
                "BenRegId": 7801,
                "isDeath": true,
                "dateOfDeath": "Jan 20, 2026 10:00:00 AM",
                "timeOfDeath": "10:00 AM",
                "reasonOfDeath": "Illness",
                "reasonOfDeathId": 2,
                "placeOfDeath": "Other",
                "placeOfDeathId": 3,
                "otherPlaceOfDeath": "Relative's House",
                "isSpouseAdded": true,
                "isChildrenAdded": false,
                "isMarried": true,
                "doYouHavechildren": true,
                "noofAlivechildren": 1,
                "noOfchildren": 1,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {
                  "gender": "Male",
                  "genderId": 1,
                  "age": 30,
                  "age_unit": "Years",
                  "dob": "Jan 15, 1996 10:30:00 AM",
                  "familyHeadRelationPosition": 1,
                  "latitude": 12.34,
                  "longitude": 56.78,
                  "aadha_noId": 0,
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village",
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM"
                }
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker logs and maps the reserved debug beneficiary id with a non-empty otherPlaceOfDeath`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns mockk(relaxed = true)
            coEvery { benDao.getBen(any(), any()) } returns null
            var captured: BenRegCache? = null
            coEvery { benDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                val list = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }
                captured = list.filterIsInstance<BenRegCache>().firstOrNull()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadDebugIdWithOtherPlaceOfDeath())

            assertEquals(1, repo.getBeneficiariesFromServerForWorker(0))
            assertNotNull(captured)
            assertEquals(700623622919L, captured?.beneficiaryId)
            assertEquals("Relative's House", captured?.otherPlaceOfDeath)
        }

    // =====================================================
    // getHouseholdCacheFromServerResponse: in-batch dedup via
    // result.map { it.householdId }.contains(hhId) when two
    // beneficiaries in the same page share a household id
    // =====================================================

    private fun benPullPayloadSharedHousehold(totalPage: Int = 1): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 8001,
                "houseoldId": 9901,
                "ashaId": 11,
                "BenRegId": 7901,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {
                  "familyHeadName": "Shared Head",
                  "familyHeadPhoneNo": "9876543211",
                  "type_bpl_apl": "APL",
                  "bpl_aplId": 1,
                  "residentialArea": "Urban",
                  "residentialAreaId": 1,
                  "other_residentialArea": "",
                  "houseType": "Pucca",
                  "houseTypeId": 1,
                  "other_houseType": "",
                  "houseOwnerShip": "Owned",
                  "houseOwnerShipId": 1,
                  "seperateKitchen": "Yes",
                  "seperateKitchenId": 1,
                  "fuelUsed": "LPG",
                  "fuelUsedId": 1,
                  "other_fuelUsed": "",
                  "sourceofDrinkingWater": "Tap",
                  "sourceofDrinkingWaterId": 1,
                  "other_sourceofDrinkingWater": "",
                  "avalabilityofElectricity": "Yes",
                  "avalabilityofElectricityId": 1,
                  "other_avalabilityofElectricity": "",
                  "availabilityofToilet": "Yes",
                  "availabilityofToiletId": 1,
                  "other_availabilityofToilet": "",
                  "serverUpdatedStatus": 1,
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM",
                  "isDeactivate": false
                },
                "beneficiaryDetails": {
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village"
                }
              },
              {
                "benficieryid": 8002,
                "houseoldId": 9901,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {},
                "beneficiaryDetails": {}
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker creates only one household when two records in the same page share a household id`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns null
            coEvery { benDao.getBen(any(), any()) } returns null
            var upsertedHouseholds = -1
            coEvery { householdDao.upsert(*anyVararg()) } answers {
                upsertedHouseholds = varargSize(arg<Any?>(0))
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadSharedHousehold())

            assertEquals(1, repo.getBeneficiariesFromServerForWorker(0))
            assertEquals(1, upsertedHouseholds)
        }

    // =====================================================
    // getHouseholdCacheFromServerResponse: isDeactivate true
    // branch (has key, value true)
    // =====================================================

    private fun benPullPayloadHouseholdDeactivated(totalPage: Int = 1): String = """
        {
          "statusCode": 200,
          "errorMessage": "",
          "data": {
            "totalPage": $totalPage,
            "data": [
              {
                "benficieryid": 8101,
                "houseoldId": 9902,
                "ashaId": 11,
                "BenRegId": 7902,
                "abhaHealthDetails": {},
                "bornbirthDeatils": {},
                "householdDetails": {
                  "familyHeadName": "Deactivated Head",
                  "familyHeadPhoneNo": "9876543212",
                  "type_bpl_apl": "APL",
                  "bpl_aplId": 1,
                  "residentialArea": "Urban",
                  "residentialAreaId": 1,
                  "other_residentialArea": "",
                  "houseType": "Pucca",
                  "houseTypeId": 1,
                  "other_houseType": "",
                  "houseOwnerShip": "Owned",
                  "houseOwnerShipId": 1,
                  "seperateKitchen": "Yes",
                  "seperateKitchenId": 1,
                  "fuelUsed": "LPG",
                  "fuelUsedId": 1,
                  "other_fuelUsed": "",
                  "sourceofDrinkingWater": "Tap",
                  "sourceofDrinkingWaterId": 1,
                  "other_sourceofDrinkingWater": "",
                  "avalabilityofElectricity": "Yes",
                  "avalabilityofElectricityId": 1,
                  "other_avalabilityofElectricity": "",
                  "availabilityofToilet": "Yes",
                  "availabilityofToiletId": 1,
                  "other_availabilityofToilet": "",
                  "serverUpdatedStatus": 1,
                  "createdBy": "asha",
                  "createdDate": "Jan 15, 2026 10:30:00 AM",
                  "isDeactivate": true
                },
                "beneficiaryDetails": {
                  "stateId": 1, "stateName": "State",
                  "districtid": 2, "districtname": "District",
                  "blockId": 3, "blockName": "Block",
                  "villageId": 4, "villageName": "Village"
                }
              }
            ]
          }
        }
    """.trimIndent()

    @Test
    fun `getBeneficiariesFromServerForWorker maps household isDeactivate true when server flags it`() =
        runTest {
            loggedIn()
            stubLocationRecord()
            coEvery { householdDao.getHousehold(any()) } returns null
            coEvery { benDao.getBen(any(), any()) } returns null
            var captured: HouseholdCache? = null
            coEvery { householdDao.upsert(*anyVararg()) } answers {
                val arg0 = arg<Any?>(0)
                val list = when (arg0) {
                    is Array<*> -> arg0.toList()
                    is Collection<*> -> arg0.toList()
                    else -> emptyList<Any?>()
                }
                captured = list.filterIsInstance<HouseholdCache>().firstOrNull()
            }
            coEvery { tmcNetworkApiService.getBeneficiaries(any()) } returns
                    jsonResponse(benPullPayloadHouseholdDeactivated())

            assertEquals(1, repo.getBeneficiariesFromServerForWorker(0))
            assertNotNull(captured)
            assertTrue(captured?.isDeactivate ?: false)
        }
}
