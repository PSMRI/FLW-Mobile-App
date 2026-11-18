package org.piramalswasthya.sakhi.badges.domain

import android.app.NotificationManager
import android.content.Context
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.dao.BadgeDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.GamificationConfigProvider
import org.piramalswasthya.sakhi.model.BadgeConfigCache
import org.piramalswasthya.sakhi.model.BadgeEarnedCache
import org.piramalswasthya.sakhi.model.BadgeStateCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.work.WorkerUtils

class BadgeEvaluatorTest {

    private val badgeDao = mockk<BadgeDao>(relaxed = true)
    private val facts = mockk<BadgeFactsReader>()
    private val pref = mockk<PreferenceDao>(relaxed = true)
    private val celebrations = mockk<BadgeCelebrations>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val gamification = mockk<GamificationConfigProvider>()
    private val user = mockk<User>()

    private val now = System.currentTimeMillis()
    private val states = slot<List<BadgeStateCache>>()
    private val earned = slot<List<BadgeEarnedCache>>()

    private fun evaluator() =
        BadgeEvaluator(badgeDao, facts, StreakEngine(), pref, celebrations, context, gamification)

    @Before
    fun setUp() {
        mockkObject(WorkerUtils)
        every { WorkerUtils.triggerBadgeSync(any()) } just runs

        every { user.userId } returns USER
        every { user.name } returns "Sunita"
        every { pref.getLoggedInUser() } returns user
        every { pref.isBadgeBackfillDone } returns true
        every {
            gamification.isMechanicEnabled(GamificationConfigProvider.Mechanic.BADGES, USER)
        } returns true
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns
                mockk<NotificationManager>(relaxed = true)

        coEvery { badgeDao.getConfig() } returns emptyList()
        coEvery { badgeDao.getFreezes() } returns emptyList()
        coEvery { badgeDao.getEarned(USER) } returns emptyList()
        coEvery { badgeDao.getAllSyncWeeks() } returns emptyList()
        coEvery { badgeDao.upsertStates(capture(states)) } just runs
        coEvery { badgeDao.insertEarned(capture(earned)) } answers {
            firstArg<List<BadgeEarnedCache>>().indices.map { it + 1L }
        }

        // Steady Syncer: 4 consecutive weeks → level 2 (milestones 2,4,6,8)
        every { facts.activityWeeks() } returns (0 downTo -3).map { BadgeDates.weekKeyAt(it, now) }.toSet()
        // Timely Reporter: 3 consecutive months → level 2 (1,3,6,12)
        every { facts.onTimeIncentiveMonths() } returns (0 downTo -2).map { BadgeDates.monthKeyAt(it, now) }.toSet()
        // Complete Worker met now (≥3) and in past quarters only via the bounded window
        every { facts.activeDomainsSince(any(), any()) } answers {
            if (secondArg<Long>() == Long.MAX_VALUE) 4L else 3L
        }
        // Community Voice not met (needs 2)
        every { facts.meetingTypesSince(any(), any()) } returns 1L
        every { facts.completedMaternalJourneys() } returns (1..5).map { "m$it" } // level 2
        every { facts.fullyImmunizedChildren() } returns emptyList()
        every { facts.abhaGeneratedBens() } returns (1..10).map { "a$it" } // level 1
        every { facts.vulnerableBabiesCaredFor() } returns listOf("b1", "b2")
        every { facts.nrcReferredChildren() } returns listOf("n1")
    }

    @After
    fun tearDown() = unmockkObject(WorkerUtils)

    private fun state(id: String) = states.captured.single { it.badgeId == id }
    private fun earnedFor(id: String) = earned.captured.filter { it.badgeId == id }

    @Test
    fun `computes every enabled badge and awards earned tiers`() = runBlocking {
        evaluator().evaluateAll()

        // critical_referral is disabled by default
        assertEquals(8, states.captured.size)
        assertTrue(states.captured.none { it.badgeId == BadgeIds.CRITICAL_REFERRAL })

        with(state(BadgeIds.STEADY_SYNCER)) {
            assertEquals(2, currentLevel); assertEquals(4L, streakCount); assertEquals(6L, nextTarget)
        }
        with(state(BadgeIds.TIMELY_REPORTER)) {
            assertEquals(2, currentLevel); assertEquals(3L, progress)
        }
        with(state(BadgeIds.COMPLETE_WORKER)) {
            assertEquals(1, currentLevel); assertEquals(4L, progress); assertEquals(3L, nextTarget)
        }
        with(state(BadgeIds.COMMUNITY_VOICE)) { assertEquals(0, currentLevel) }
        with(state(BadgeIds.MATERNAL_JOURNEY)) {
            assertEquals(2, currentLevel); assertEquals(5L, progress); assertEquals(15L, nextTarget)
        }
        with(state(BadgeIds.CHILD_FULLY_PROTECTED)) { assertEquals(0, currentLevel); assertEquals(3L, nextTarget) }
        with(state(BadgeIds.DIGITAL_IDENTITY)) { assertEquals(1, currentLevel) }
        with(state(BadgeIds.VULNERABLE_BABY)) { assertEquals(2, currentLevel); assertEquals(2L, progress) }

        assertEquals(listOf(1, 2), earnedFor(BadgeIds.STEADY_SYNCER).map { it.level })
        assertEquals(listOf(1, 2), earnedFor(BadgeIds.MATERNAL_JOURNEY).map { it.level })
        // current quarter + 8 past quarters reconstructed (no history yet)
        assertEquals(9, earnedFor(BadgeIds.COMPLETE_WORKER).size)
        assertEquals(9, earnedFor(BadgeIds.COMPLETE_WORKER).map { it.caseRef }.distinct().size)
        assertTrue(earnedFor(BadgeIds.COMMUNITY_VOICE).isEmpty())
        // one award per case, tiered by position, keyed by hashed case ref
        val babies = earnedFor(BadgeIds.VULNERABLE_BABY)
        assertEquals(listOf(1, 2), babies.map { it.level })
        assertEquals(listOf(AwardKeys.forCase("b1"), AwardKeys.forCase("b2")), babies.map { it.caseRef })

        // celebrate only the top tier per badge, never for steady syncer
        verify { celebrations.publish(BadgeIds.MATERNAL_JOURNEY, 2) }
        verify(exactly = 0) { celebrations.publish(BadgeIds.MATERNAL_JOURNEY, 1) }
        verify(exactly = 0) { celebrations.publish(BadgeIds.STEADY_SYNCER, any()) }
        verify { WorkerUtils.triggerBadgeSync(context) }
        verify(exactly = 0) { pref.isBadgeBackfillDone = any() }
    }

    @Test
    fun `server config overrides milestones, grace and enables badges`() = runBlocking {
        coEvery { badgeDao.getConfig() } returns listOf(
            BadgeConfigCache(BadgeConfigKeys.milestones(BadgeIds.MATERNAL_JOURNEY), "1, 2, 3, x", now),
            BadgeConfigCache(BadgeConfigKeys.enabled(BadgeIds.CRITICAL_REFERRAL), "true", now),
            BadgeConfigCache(BadgeConfigKeys.enabled(BadgeIds.DIGITAL_IDENTITY), "false", now),
            BadgeConfigCache(BadgeConfigKeys.grace(BadgeIds.STEADY_SYNCER), "0", now)
        )

        evaluator().evaluateAll()

        assertEquals(3, state(BadgeIds.MATERNAL_JOURNEY).currentLevel)
        assertEquals(1, state(BadgeIds.CRITICAL_REFERRAL).currentLevel)
        assertTrue(states.captured.none { it.badgeId == BadgeIds.DIGITAL_IDENTITY })
        assertEquals(0, state(BadgeIds.STEADY_SYNCER).graceRemaining)
    }

    @Test
    fun `first run on an install backfills silently and marks backfill done`() = runBlocking {
        every { pref.isBadgeBackfillDone } returns false

        evaluator().evaluateAll()

        verify(exactly = 0) { celebrations.publish(any(), any()) }
        verify { pref.isBadgeBackfillDone = true }
        verify { WorkerUtils.triggerBadgeSync(context) }
    }

    @Test
    fun `already earned awards neither celebrate nor trigger a sync`() = runBlocking {
        coEvery { badgeDao.insertEarned(any()) } answers {
            firstArg<List<BadgeEarnedCache>>().map { -1L }
        }

        evaluator().evaluateAll()

        verify(exactly = 0) { celebrations.publish(any(), any()) }
        verify(exactly = 0) { WorkerUtils.triggerBadgeSync(any()) }
    }

    @Test
    fun `earned history floors tiered state and skips quarter reconstruction`() = runBlocking {
        coEvery { badgeDao.getEarned(USER) } returns listOf(
            BadgeEarnedCache(userId = USER, badgeId = BadgeIds.MATERNAL_JOURNEY, level = 4, earnedAt = 1),
            BadgeEarnedCache(userId = USER, badgeId = BadgeIds.COMPLETE_WORKER, level = 1, caseRef = "q", earnedAt = 1),
            BadgeEarnedCache(userId = USER, badgeId = BadgeIds.VULNERABLE_BABY, level = 3, caseRef = "c", earnedAt = 1)
        )

        evaluator().evaluateAll()

        assertEquals(4, state(BadgeIds.MATERNAL_JOURNEY).currentLevel)
        // per-case badges are not floored
        assertEquals(2, state(BadgeIds.VULNERABLE_BABY).currentLevel)
        assertEquals(1, earnedFor(BadgeIds.COMPLETE_WORKER).size)
    }

    @Test
    fun `no awards means no insert`() = runBlocking {
        every { facts.activityWeeks() } returns emptySet()
        every { facts.onTimeIncentiveMonths() } returns emptySet()
        every { facts.activeDomainsSince(any(), any()) } returns 0L
        every { facts.completedMaternalJourneys() } returns emptyList()
        every { facts.abhaGeneratedBens() } returns emptyList()
        every { facts.vulnerableBabiesCaredFor() } returns emptyList()
        every { pref.isBadgeBackfillDone } returns false

        evaluator().evaluateAll()

        coVerify(exactly = 0) { badgeDao.insertEarned(any()) }
        verify { pref.isBadgeBackfillDone = true }
        assertEquals(0, state(BadgeIds.STEADY_SYNCER).currentLevel)
    }

    @Test
    fun `a failing fact skips only that badge`() = runBlocking {
        every { facts.completedMaternalJourneys() } throws IllegalStateException("boom")

        evaluator().evaluateAll()

        assertTrue(states.captured.none { it.badgeId == BadgeIds.MATERNAL_JOURNEY })
        assertEquals(7, states.captured.size)
    }

    @Test
    fun `dao read failures fall back to defaults`() = runBlocking {
        coEvery { badgeDao.getConfig() } throws RuntimeException("config")
        coEvery { badgeDao.getFreezes() } throws RuntimeException("freezes")
        coEvery { badgeDao.getEarned(USER) } throws RuntimeException("earned")

        evaluator().evaluateAll()

        assertEquals(8, states.captured.size)
    }

    @Test
    fun `skipped without a logged in user`() = runBlocking {
        every { pref.getLoggedInUser() } returns null
        evaluator().evaluateAll()
        coVerify(exactly = 0) { badgeDao.getConfig() }
    }

    @Test
    fun `skipped when reading the user throws`() = runBlocking {
        every { pref.getLoggedInUser() } throws IllegalStateException("prefs")
        evaluator().evaluateAll()
        coVerify(exactly = 0) { badgeDao.getConfig() }
    }

    @Test
    fun `skipped when badges are switched off for the ASHA`() = runBlocking {
        every { gamification.isMechanicEnabled(any(), any()) } returns false
        evaluator().evaluateAll()
        coVerify(exactly = 0) { badgeDao.getConfig() }
    }

    @Test
    fun `remote kill switch stops evaluation`() = runBlocking {
        coEvery { badgeDao.getConfig() } returns
                listOf(BadgeConfigCache(BadgeConfigKeys.FEATURE_ENABLED, "false", now))
        evaluator().evaluateAll()
        coVerify(exactly = 0) { badgeDao.upsertStates(any()) }
    }

    @Test
    fun `triggers inside the throttle window coalesce into one trailing run`() = runBlocking {
        val evaluator = evaluator()
        evaluator.evaluateAll()
        // two more triggers right away: one waits for the window, the other is dropped
        listOf(async { evaluator.evaluateAll() }, async { evaluator.evaluateAll() }).awaitAll()

        coVerify(exactly = 2) { badgeDao.upsertStates(any()) }
        assertFalse(states.captured.isEmpty())
    }

    private companion object {
        const val USER = 7
    }
}
