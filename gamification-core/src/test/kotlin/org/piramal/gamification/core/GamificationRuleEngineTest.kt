package org.piramal.gamification.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [GamificationRuleEngine].
 *
 * Covers:
 * - Duplicate event ID handling (dedup)
 * - Daily cap enforcement
 * - Offline event tracking
 * - Stable reward ID across sync retries
 */
class GamificationRuleEngineTest {

    private val engine = GamificationRuleEngine()

    @Test
    fun `duplicate event ID is idempotent`() {
        val event = GamificationEvent(
            eventId = "evt-001",
            workerId = "worker-1",
            actionType = "registration",
            points = 10,
            localDate = "2026-05-07",
        )

        val p1 = engine.apply(event, WorkerProgress())
        val p2 = engine.apply(event, p1) // same event repeated

        assertEquals(10, p1.totalPoints, "first application should award points")
        assertEquals(p1.totalPoints, p2.totalPoints, "duplicate should not add points")
        assertTrue(p2.acceptedEventIds.contains("evt-001"))
    }

    @Test
    fun `daily cap stops excess points`() {
        val big = GamificationEvent("evt-001", "w1", "visit", 60, "2026-05-07")
        val alsoBig = GamificationEvent("evt-002", "w1", "visit", 60, "2026-05-07")

        val p1 = engine.apply(big, WorkerProgress())
        val p2 = engine.apply(alsoBig, p1)

        assertEquals(100, p2.totalPoints, "total should be capped at 100")
        assertEquals(60, p2.dailyPoints["2026-05-07"], "60 of 60 second-event points blocked")
    }

    @Test
    fun `offline event is tracked before sync`() {
        val offline = GamificationEvent(
            eventId = "evt-off-1",
            workerId = "w1",
            actionType = "screening",
            points = 20,
            localDate = "2026-05-07",
            syncStatus = "pending",
        )

        val result = engine.apply(offline, WorkerProgress())

        assertEquals(20, result.totalPoints)
        assertTrue(result.acceptedEventIds.contains("evt-off-1"))
    }

    @Test
    fun `synced event retry with same reward ID is stable`() {
        val synced = GamificationEvent(
            eventId = "evt-sync-1",
            workerId = "w1",
            actionType = "immunization",
            points = 15,
            localDate = "2026-05-07",
            syncStatus = "synced",
            rewardId = "reward-001",
        )

        val p1 = engine.apply(synced, WorkerProgress())
        val p2 = engine.apply(synced, p1)

        assertEquals(p1.totalPoints, p2.totalPoints, "retry must not double-count")
    }
}
