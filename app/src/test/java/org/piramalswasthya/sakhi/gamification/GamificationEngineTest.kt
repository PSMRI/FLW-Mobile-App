package org.piramalswasthya.sakhi.gamification

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.model.GamificationBadge
import org.piramalswasthya.sakhi.model.GamificationProfile
import org.piramalswasthya.sakhi.model.PointsTransaction
import org.piramalswasthya.sakhi.repositories.GamificationRepo

/**
 * Unit tests for GamificationEngine.
 *
 * Uses a fake in-memory repository — no Android framework, no Room,
 * runs on the JVM in milliseconds.
 */
class GamificationEngineTest {

    // ── Fake repository ───────────────────────────────────────────────────────

    private class FakeGamificationRepo : GamificationRepo(
        dao = throw UnsupportedOperationException("not used in fake")
    ) {
        val profiles   = mutableMapOf<Int, GamificationProfile>()
        val badges     = mutableListOf<GamificationBadge>()
        val transactions = mutableListOf<PointsTransaction>()

        override suspend fun getProfile(userId: Int) = profiles[userId]

        override suspend fun upsertProfile(profile: GamificationProfile) {
            profiles[profile.userId] = profile
        }

        override fun observeProfile(userId: Int): Flow<GamificationProfile?> =
            flowOf(profiles[userId])

        override suspend fun insertBadge(badge: GamificationBadge): Long {
            badges.add(badge); return badges.size.toLong()
        }

        override suspend fun hasBadge(userId: Int, badgeType: String): Boolean =
            badges.any { it.userId == userId && it.badgeType == badgeType }

        override fun observeBadges(userId: Int): Flow<List<GamificationBadge>> =
            flowOf(badges.filter { it.userId == userId })

        override suspend fun insertTransaction(tx: PointsTransaction): Long {
            transactions.add(tx); return transactions.size.toLong()
        }

        override fun observeRecentTransactions(userId: Int): Flow<List<PointsTransaction>> =
            flowOf(transactions.filter { it.userId == userId })

        override suspend fun getUnsyncedProfiles() = profiles.values.filter { it.syncState == 0 }
        override suspend fun getUnsyncedBadges()   = badges.filter { it.syncState == 0 }
        override suspend fun getUnsyncedTransactions() = transactions.filter { it.syncState == 0 }
        override suspend fun markProfileSynced(userId: Int) {
            profiles[userId]?.let { profiles[userId] = it.copy(syncState = 2) }
        }
        override suspend fun markBadgesSynced(ids: List<Long>) {}
        override suspend fun markTransactionsSynced(ids: List<Long>) {}
    }

    private lateinit var repo: FakeGamificationRepo
    private lateinit var engine: GamificationEngine

    @Before
    fun setup() {
        repo   = FakeGamificationRepo()
        engine = GamificationEngine(repo)
    }

    // ── Points tests ──────────────────────────────────────────────────────────

    @Test
    fun `first event creates profile with correct points`() = runTest {
        engine.process(1, GamificationEvent.AncVisitCompleted("ben-1"))
        val profile = repo.profiles[1]!!
        assertEquals(20, profile.totalPoints)
    }

    @Test
    fun `points accumulate correctly across events`() = runTest {
        engine.process(1, GamificationEvent.AncVisitCompleted("b1"))
        engine.process(1, GamificationEvent.ImmunizationRecorded("b2"))
        engine.process(1, GamificationEvent.HrpCaseIdentified("b3"))
        val profile = repo.profiles[1]!!
        assertEquals(20 + 25 + 35, profile.totalPoints)
    }

    @Test
    fun `hrp case gives highest points`() = runTest {
        engine.process(1, GamificationEvent.HrpCaseIdentified("b1"))
        assertEquals(35, repo.profiles[1]!!.totalPoints)
    }

    // ── Streak tests ──────────────────────────────────────────────────────────

    @Test
    fun `same day activity does not increment streak`() {
        val profile = GamificationProfile(userId = 1, currentStreakDays = 3, lastActivityDate = "2026-05-09")
        val result  = engine.updateStreak(profile, "2026-05-09")
        assertEquals(3, result.currentStreakDays)
    }

    @Test
    fun `consecutive day increments streak`() {
        val profile = GamificationProfile(userId = 1, currentStreakDays = 3, lastActivityDate = "2026-05-08")
        val result  = engine.updateStreak(profile, "2026-05-09")
        assertEquals(4, result.currentStreakDays)
    }

    @Test
    fun `skipped day resets streak to 1`() {
        val profile = GamificationProfile(userId = 1, currentStreakDays = 5, lastActivityDate = "2026-05-06")
        val result  = engine.updateStreak(profile, "2026-05-09")
        assertEquals(1, result.currentStreakDays)
    }

    @Test
    fun `longest streak is preserved when current streak breaks`() {
        val profile = GamificationProfile(
            userId = 1,
            currentStreakDays = 10,
            longestStreakDays = 10,
            lastActivityDate = "2026-05-01"
        )
        val result = engine.updateStreak(profile, "2026-05-09")
        assertEquals(1, result.currentStreakDays)
        assertEquals(10, result.longestStreakDays)  // longest preserved
    }

    @Test
    fun `longest streak updates when current exceeds it`() {
        val profile = GamificationProfile(
            userId = 1,
            currentStreakDays = 6,
            longestStreakDays = 6,
            lastActivityDate = "2026-05-08"
        )
        val result = engine.updateStreak(profile, "2026-05-09")
        assertEquals(7, result.currentStreakDays)
        assertEquals(7, result.longestStreakDays)
    }

    @Test
    fun `null last activity date starts streak at 1`() {
        val profile = GamificationProfile(userId = 1, lastActivityDate = null)
        val result  = engine.updateStreak(profile, "2026-05-09")
        assertEquals(1, result.currentStreakDays)
    }

    // ── Level tests ───────────────────────────────────────────────────────────

    @Test
    fun `level 1 at zero points`() = assertEquals(1, engine.computeLevel(0))

    @Test
    fun `level 2 at exactly 100 points`() = assertEquals(2, engine.computeLevel(100))

    @Test
    fun `level 2 at 299 points`() = assertEquals(2, engine.computeLevel(299))

    @Test
    fun `level 3 at 300 points`() = assertEquals(3, engine.computeLevel(300))

    @Test
    fun `level 8 at 3000+ points`() = assertEquals(8, engine.computeLevel(5000))

    // ── Badge deduplication tests ─────────────────────────────────────────────

    @Test
    fun `badge is awarded only once for same type`() = runTest {
        engine.process(1, GamificationEvent.AncVisitCompleted("b1"))
        engine.process(1, GamificationEvent.AncVisitCompleted("b2"))
        val ancBadges = repo.badges.count {
            it.userId == 1 && it.badgeType == BadgeCatalog.ANC_HERO.type
        }
        assertEquals(1, ancBadges)
    }

    @Test
    fun `hrp badge awarded on first hrp event`() = runTest {
        engine.process(1, GamificationEvent.HrpCaseIdentified("b1"))
        assertTrue(repo.badges.any { it.badgeType == BadgeCatalog.HRP_IDENTIFIER.type })
    }

    // ── Daily login deduplication ─────────────────────────────────────────────

    @Test
    fun `daily login bonus awarded only once per day`() = runTest {
        // Simulate profile with today already recorded
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
            .format(java.util.Date())
        repo.profiles[1] = GamificationProfile(userId = 1, totalPoints = 10, lastActivityDate = today)

        engine.process(1, GamificationEvent.DailyLogin)

        // Points should not have changed
        assertEquals(10, repo.profiles[1]!!.totalPoints)
    }

    // ── Transaction audit ─────────────────────────────────────────────────────

    @Test
    fun `each event inserts exactly one transaction`() = runTest {
        engine.process(1, GamificationEvent.AncVisitCompleted("b1"))
        engine.process(1, GamificationEvent.ImmunizationRecorded("b2"))
        assertEquals(2, repo.transactions.filter { it.userId == 1 }.size)
    }

    @Test
    fun `transaction stores correct event type`() = runTest {
        engine.process(1, GamificationEvent.HrpCaseIdentified("b1"))
        val tx = repo.transactions.first { it.userId == 1 }
        assertEquals("HRP_CASE_IDENTIFIED", tx.eventType)
    }

    // ── daysBetween helper ────────────────────────────────────────────────────

    @Test
    fun `daysBetween same date returns 0`() =
        assertEquals(0L, GamificationEngine.daysBetween("2026-05-09", "2026-05-09"))

    @Test
    fun `daysBetween consecutive days returns 1`() =
        assertEquals(1L, GamificationEngine.daysBetween("2026-05-08", "2026-05-09"))

    @Test
    fun `daysBetween month boundary works correctly`() =
        assertEquals(1L, GamificationEngine.daysBetween("2026-04-30", "2026-05-01"))
}
