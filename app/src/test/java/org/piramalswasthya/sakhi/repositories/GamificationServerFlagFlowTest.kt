package org.piramalswasthya.sakhi.repositories

import android.content.Context
import com.squareup.moshi.Moshi
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.badges.BadgeRepository
import org.piramalswasthya.sakhi.badges.domain.TaskCompletionBus
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.BadgeDao
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.GamificationConfigProvider
import org.piramalswasthya.sakhi.helpers.GamificationConfigProvider.State
import org.piramalswasthya.sakhi.helpers.GamificationRuntime
import org.piramalswasthya.sakhi.helpers.LocalMonthlyRecapAvailability
import org.piramalswasthya.sakhi.helpers.NetworkResponse
import org.piramalswasthya.sakhi.helpers.RecapClock
import org.piramalswasthya.sakhi.model.GamificationAccess
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.network.SerializeNullAdapterFactory
import org.piramalswasthya.sakhi.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.sakhi.notifications.NotificationScheduler
import org.piramalswasthya.sakhi.work.WorkerUtils
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Calendar

/**
 * FLW-1251 end-to-end with DUMMY server responses (the backend is not live yet).
 *
 * Real: HTTP (MockWebServer) → Retrofit + the app's Moshi setup → [UserRepo] (login and
 * Home refresh) → [GamificationConfigProvider] → [GamificationRuntime] (what HomeActivity
 * runs), the badge shelf ([BadgeRepository]) and the Monthly Recap gate.
 * Faked: SharedPreferences (in memory), the DAOs, WorkManager and AlarmManager calls.
 */
class GamificationServerFlagFlowTest {

    private val ashaId = 4501

    private lateinit var server: MockWebServer
    private lateinit var userRepo: UserRepo
    private lateinit var provider: GamificationConfigProvider
    private lateinit var runtime: GamificationRuntime
    private lateinit var badgeRepository: BadgeRepository
    private lateinit var recapGate: LocalMonthlyRecapAvailability

    private val pref: PreferenceDao = mockk(relaxed = true)
    private val bus: TaskCompletionBus = mockk(relaxed = true)
    private val alarm: NotificationScheduler = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    // in-memory prefs the flag code reads/writes
    private var savedAccess: GamificationAccess? = null
    private var loggedIn = true

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }

        every { pref.getLoggedInUser() } answers {
            if (loggedIn) mockk<User> { every { userId } returns ashaId } else null
        }
        every { pref.getGamificationAccess() } answers { savedAccess }
        val saved = slot<GamificationAccess>()
        every { pref.saveGamificationAccess(capture(saved)) } answers { savedAccess = saved.captured }
        every { pref.gamificationDebugOverride } returns null // follow the server

        mockkObject(WorkerUtils)
        every { WorkerUtils.triggerBadgeWorkers(any()) } just Runs
        every { WorkerUtils.cancelBadgeWorkers(any()) } just Runs
        mockkObject(TokenInsertTmcInterceptor)
        every { TokenInsertTmcInterceptor.setToken(any()) } just Runs
        every { TokenInsertTmcInterceptor.setJwt(any()) } just Runs

        // same Moshi setup as AppModule.provideMoshiInstance
        val moshi = Moshi.Builder().add(SerializeNullAdapterFactory).build()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(OkHttpClient())
            .build()
            .create(AmritApiService::class.java)

        provider = GamificationConfigProvider(pref)
        userRepo = UserRepo(
            mockk<BenDao>(relaxed = true), mockk<InAppDb>(relaxed = true),
            mockk<ImmunizationDao>(relaxed = true), pref, mockk<SyncDao>(relaxed = true),
            api, provider
        )
        runtime = GamificationRuntime(context, bus, alarm)

        val badgeDao = mockk<BadgeDao>(relaxed = true)
        every { badgeDao.getStatesFlow(any()) } returns flowOf(emptyList())
        every { badgeDao.getEarnedFlow(any()) } returns flowOf(emptyList())
        every { badgeDao.getConfigFlow() } returns flowOf(emptyList())
        badgeRepository = BadgeRepository(badgeDao, pref, provider)

        // Monthly Recap gate with a clock inside the days 1–7 window
        val clock = object : RecapClock {
            override fun now(): Calendar = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 3) }
        }
        recapGate = LocalMonthlyRecapAvailability(clock, provider, pref)
    }

    @After
    fun tearDown() {
        server.shutdown()
        unmockkAll()
    }

    // ---------------- dummy server responses ----------------

    /** Dummy getUserDetail body; [gamification] is inserted as raw JSON (null = field absent). */
    private fun userDetail(gamification: String?): MockResponse {
        val extra = gamification?.let { ""","gamification":$it""" } ?: ""
        return MockResponse().setBody(
            """{"success":true,"message":"ok","data":{
                "userId":$ashaId,"name":"Test ASHA","userName":"asha1","stateId":5,"stateName":"Assam",
                "workingDistrictId":12,"workingDistrictName":"Kamrup","serviceProviderId":1,
                "roleId":1,"roleName":"ASHA","providerServiceMapId":1,"blockId":88,"blockName":"B88",
                "villageId":"1","villageName":"V1",
                "facilityData":{"location":{"district":"Kamrup","blockOrUlb":"B88",
                  "locationType":"Rural","state":"Assam"},"facility":null,"supervisor":null,
                  "peersAtFacility":[]}$extra}}"""
        )
    }

    private fun flag(enabled: Boolean, badges: Boolean, recap: Boolean, notifications: Boolean) =
        """{"enabled":$enabled,"mechanics":{"badges":$badges,"monthlyRecap":$recap,
            "eveningNotifications":$notifications,"consistencyDashboard":false},
            "source":"BLOCK","updatedAt":1762156800000}"""

    private val allOn = flag(enabled = true, badges = true, recap = true, notifications = true)

    private fun loginResponse() = MockResponse().setBody(
        """{"statusCode":200,"errorMessage":"","data":{"key":"k","userID":$ashaId,
            "refreshToken":"r","jwtToken":"j"}}"""
    )

    /**
     * Logs in through the real [UserRepo.authenticateUser]. Only the private password
     * encryption is stubbed: CryptoUtil reads its key from the native library, which a
     * JVM test cannot load (and KeyUtilsTest relies on it staying unloadable).
     */
    private suspend fun login(): NetworkResponse<User?> {
        val repo = spyk(userRepo, recordPrivateCalls = true)
        every { repo["encrypt"](any<String>()) } returns "enc"
        return repo.authenticateUser("asha1", "pw")
    }

    /** What HomeActivity does on every state change. */
    private fun homeApplies() = runtime.apply(provider.state.value)

    // ---------------- scenarios ----------------

    @Test
    fun `login with the flag ON shows everything and starts badge work and the alarm`() = runTest {
        server.enqueue(loginResponse())
        server.enqueue(userDetail(allOn))

        val result = login()

        assertTrue(result is NetworkResponse.Success)
        server.takeRequest() // userAuthenticate
        assertEquals("/flw-api/user/getUserDetail?userId=$ashaId", server.takeRequest().path)
        assertEquals(State.ALL_ON, provider.state.value)
        assertEquals("BLOCK", savedAccess?.source)

        homeApplies()
        verify { WorkerUtils.triggerBadgeWorkers(context) }
        verify { bus.start() }
        verify { alarm.schedule() }
        assertTrue(badgeRepository.shelf.first().isNotEmpty())   // My Impact card + shelf visible
        assertTrue(recapGate.isAvailable())                       // recap card can show
    }

    @Test
    fun `login with a malformed flag still logs in and keeps everything off`() = runTest {
        server.enqueue(loginResponse())
        server.enqueue(userDetail("""{"enabled":"yes","mechanics":"all"}"""))

        val result = login()

        assertTrue(result is NetworkResponse.Success)
        assertEquals(State.ALL_OFF, provider.state.value)
    }

    @Test
    fun `old server without the field means OFF by default`() = runTest {
        server.enqueue(userDetail(null))

        userRepo.setFacilityData(ashaId)
        homeApplies()

        assertEquals(State.ALL_OFF, provider.state.value)
        verify { WorkerUtils.cancelBadgeWorkers(context) }
        verify { bus.stop() }
        verify { alarm.cancel() }
        verify(exactly = 0) { WorkerUtils.triggerBadgeWorkers(any()) }
        assertTrue(badgeRepository.shelf.first().isEmpty())       // My Impact card hidden
        assertFalse(recapGate.isAvailable())
        assertFalse(recapGate.isReplayAvailable())
    }

    @Test
    fun `server switching the ASHA off stops everything on the next Home open`() = runTest {
        server.enqueue(userDetail(allOn))
        userRepo.setFacilityData(ashaId)
        homeApplies()

        server.enqueue(userDetail(flag(enabled = false, badges = true, recap = true, notifications = true)))
        userRepo.setFacilityData(ashaId)
        homeApplies()

        assertEquals(State.ALL_OFF, provider.state.value) // master off wins over the parts
        verify { WorkerUtils.cancelBadgeWorkers(context) }
        verify { bus.stop() }
        verify { alarm.cancel() }
        assertTrue(badgeRepository.shelf.first().isEmpty())
    }

    @Test
    fun `each part follows its own switch`() = runTest {
        server.enqueue(userDetail(flag(enabled = true, badges = true, recap = false, notifications = false)))

        userRepo.setFacilityData(ashaId)
        homeApplies()

        assertEquals(State(badges = true), provider.state.value)
        verify { WorkerUtils.triggerBadgeWorkers(context) }
        verify { alarm.cancel() }
        verify(exactly = 0) { alarm.schedule() }
        assertFalse(recapGate.isAvailable())
        assertTrue(provider.isEnabledForLoggedInUser(GamificationConfigProvider.Mechanic.BADGES))
        assertFalse(provider.isEnabledForLoggedInUser(GamificationConfigProvider.Mechanic.NOTIFICATIONS))
    }

    @Test
    fun `server error or missing field keeps the last value`() = runTest {
        server.enqueue(userDetail(allOn))
        userRepo.setFacilityData(ashaId)

        server.enqueue(MockResponse().setResponseCode(500))   // server down
        userRepo.setFacilityData(ashaId)
        assertEquals(State.ALL_ON, provider.state.value)

        server.enqueue(userDetail(null))                       // backend left the field out
        userRepo.setFacilityData(ashaId)
        assertEquals(State.ALL_ON, provider.state.value)
    }

    @Test
    fun `flag saved for one ASHA is not used for another on the same phone`() = runTest {
        server.enqueue(userDetail(allOn))
        userRepo.setFacilityData(ashaId)
        assertEquals(State.ALL_ON, provider.state.value)

        savedAccess = savedAccess!!.copy(userId = 9999) // someone else's saved flag
        provider.refresh()

        assertEquals(State.ALL_OFF, provider.state.value)
    }

    @Test
    fun `logged out means everything off`() = runTest {
        server.enqueue(userDetail(allOn))
        userRepo.setFacilityData(ashaId)

        loggedIn = false
        provider.refresh()

        assertEquals(State.ALL_OFF, provider.state.value)
    }

    @Test
    fun `returning to Home re-reads the flag only when the throttle allows`() = runTest {
        server.enqueue(userDetail(allOn))
        userRepo.setFacilityData(ashaId)          // app open
        assertEquals(1, server.requestCount)

        userRepo.refreshUserDetailIfDue(ashaId)   // back on Home right away: no new request
        assertEquals(1, server.requestCount)
    }
}
