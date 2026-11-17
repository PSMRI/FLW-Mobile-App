package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.dao.BadgeDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.GamificationConfigProvider
import org.piramalswasthya.sakhi.model.BadgeConfigCache
import org.piramalswasthya.sakhi.model.BadgeEarnedCache
import org.piramalswasthya.sakhi.model.BadgeStreakFreezeCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.BadgeApiService
import org.piramalswasthya.sakhi.network.BadgeConfigResponse
import org.piramalswasthya.sakhi.network.BadgeEarnedDTO
import org.piramalswasthya.sakhi.network.BadgeEarnedPush
import org.piramalswasthya.sakhi.network.BadgeEarnedResponse
import org.piramalswasthya.sakhi.network.BadgeFreezeDTO
import org.piramalswasthya.sakhi.network.BadgeFreezesResponse
import retrofit2.Response

class BadgeSyncWorkerTest {

    private val context = mockk<Context>(relaxed = true)
    private val api = mockk<BadgeApiService>()
    private val dao = mockk<BadgeDao>(relaxed = true)
    private val pref = mockk<PreferenceDao>()
    private val gamification = mockk<GamificationConfigProvider>()
    private val user = mockk<User>().also { every { it.userId } returns 8 }

    private fun worker() = BadgeSyncWorker(
        context, mockk<WorkerParameters>(relaxed = true), api, dao, pref, gamification
    )

    @Before
    fun setUp() {
        mockkObject(WorkerUtils)
        every { WorkerUtils.triggerAdHocBadgeEvaluation(any()) } just runs
        every { gamification.isEnabledForLoggedInUser(GamificationConfigProvider.Mechanic.BADGES) } returns true
        every { pref.getLoggedInUser() } returns user
        coEvery { api.getConfig() } returns Response.success(BadgeConfigResponse(mapOf("feature_enabled" to "true")))
        coEvery { api.getFreezes() } returns Response.success(
            BadgeFreezesResponse(listOf(BadgeFreezeDTO(null, 10, 20), BadgeFreezeDTO("steady_syncer", 30, 40)))
        )
        coEvery { api.getEarned() } returns Response.success(
            BadgeEarnedResponse(listOf(BadgeEarnedDTO("complete_worker", 1, 5, awardKey = "2026-Q1")))
        )
        coEvery { api.postEarned(any()) } returns Response.success(Unit)
        coEvery { dao.insertEarned(any()) } returns listOf(1L)
        coEvery { dao.getUnsyncedEarned(8) } returns listOf(
            BadgeEarnedCache(id = 3, userId = 8, badgeId = "maternal_journey", level = 1, caseRef = "", earnedAt = 9)
        )
    }

    @After
    fun tearDown() = unmockkObject(WorkerUtils)

    @Test
    fun `pulls config, freezes and history then pushes unsynced awards`() = runTest {
        val config = slot<List<BadgeConfigCache>>()
        val freezes = slot<List<BadgeStreakFreezeCache>>()
        val restored = slot<List<BadgeEarnedCache>>()
        val push = slot<BadgeEarnedPush>()
        coEvery { dao.upsertConfig(capture(config)) } just runs
        coEvery { dao.replaceFreezes(capture(freezes)) } just runs
        coEvery { dao.insertEarned(capture(restored)) } returns listOf(1L)
        coEvery { api.postEarned(capture(push)) } returns Response.success(Unit)

        val result = worker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        assertEquals("feature_enabled", config.captured.single().key)
        assertEquals(listOf("", "steady_syncer"), freezes.captured.map { it.badgeId })
        with(restored.captured.single()) {
            assertEquals(8, userId); assertEquals("2026-Q1", caseRef); assertTrue(synced)
        }
        verify { WorkerUtils.triggerAdHocBadgeEvaluation(context) }
        assertEquals(8, push.captured.userId)
        assertEquals("maternal_journey", push.captured.badges.single().badgeId)
        coVerify { dao.markEarnedSynced(listOf(3L)) }
    }

    @Test
    fun `restored history already present does not re-evaluate and failed push stays unsynced`() = runTest {
        coEvery { dao.insertEarned(any()) } returns listOf(-1L)
        coEvery { api.postEarned(any()) } returns Response.error(500, "".toResponseBody())
        coEvery { api.getConfig() } returns Response.success(BadgeConfigResponse(emptyMap()))

        worker().doWork()

        verify(exactly = 0) { WorkerUtils.triggerAdHocBadgeEvaluation(any()) }
        coVerify(exactly = 0) { dao.markEarnedSynced(any()) }
        coVerify(exactly = 0) { dao.upsertConfig(any()) }
    }

    @Test
    fun `every step failing still succeeds`() = runTest {
        coEvery { api.getConfig() } throws RuntimeException("404")
        coEvery { api.getFreezes() } throws RuntimeException("404")
        coEvery { api.getEarned() } throws RuntimeException("404")
        coEvery { dao.getUnsyncedEarned(any()) } throws RuntimeException("db")

        assertTrue(worker().doWork() is ListenableWorker.Result.Success)
    }

    @Test
    fun `nothing unsynced posts nothing`() = runTest {
        coEvery { dao.getUnsyncedEarned(8) } returns emptyList()
        worker().doWork()
        coVerify(exactly = 0) { api.postEarned(any()) }
    }

    @Test
    fun `logged out syncs config only`() = runTest {
        every { pref.getLoggedInUser() } returns null
        worker().doWork()
        coVerify { dao.upsertConfig(any()) }
        coVerify(exactly = 0) { api.getEarned() }

        every { pref.getLoggedInUser() } throws IllegalStateException("prefs")
        assertTrue(worker().doWork() is ListenableWorker.Result.Success)
    }

    @Test
    fun `switched off makes no badge calls`() = runTest {
        every { gamification.isEnabledForLoggedInUser(any()) } returns false
        assertTrue(worker().doWork() is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { api.getConfig() }
    }
}
