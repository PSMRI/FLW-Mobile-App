package org.piramalswasthya.sakhi.repositories

import android.app.Application
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
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
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.BenWithHRPTrackingCache
import org.piramalswasthya.sakhi.model.HouseholdCache
import org.piramalswasthya.sakhi.model.HouseholdNetwork
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.NetworkResult
import retrofit2.Response

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
}
