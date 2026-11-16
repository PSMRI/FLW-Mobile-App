package org.piramalswasthya.sakhi.badges

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.badges.domain.BadgeCelebrations
import org.piramalswasthya.sakhi.badges.domain.BadgeConfigKeys
import org.piramalswasthya.sakhi.badges.domain.BadgeDates
import org.piramalswasthya.sakhi.badges.domain.BadgeDefinitions
import org.piramalswasthya.sakhi.badges.domain.BadgeFactsReader
import org.piramalswasthya.sakhi.badges.domain.BadgeIds
import org.piramalswasthya.sakhi.badges.domain.BadgeKind
import org.piramalswasthya.sakhi.badges.domain.StreakEngine
import org.piramalswasthya.sakhi.badges.domain.TaskCompletionBus
import org.piramalswasthya.sakhi.database.room.dao.BadgeDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.GamificationConfigProvider
import org.piramalswasthya.sakhi.model.BadgeConfigCache
import org.piramalswasthya.sakhi.model.BadgeEarnedCache
import org.piramalswasthya.sakhi.model.BadgeStateCache
import org.piramalswasthya.sakhi.model.BadgeSyncLogCache
import org.piramalswasthya.sakhi.model.User
import java.util.Calendar

class BadgeRepositoryTest {

    private val dao = mockk<BadgeDao>()
    private val pref = mockk<PreferenceDao>()
    private val gamification = mockk<GamificationConfigProvider>()
    private val switches = MutableStateFlow(GamificationConfigProvider.State(badges = true))
    private val user = mockk<User>().also { every { it.userId } returns 3 }

    private fun repo(
        states: List<BadgeStateCache> = emptyList(),
        earned: List<BadgeEarnedCache> = emptyList(),
        config: List<BadgeConfigCache> = emptyList()
    ): BadgeRepository {
        every { gamification.state } returns switches
        every { pref.getLoggedInUser() } returns user
        every { dao.getStatesFlow(3) } returns flowOf(states)
        every { dao.getEarnedFlow(3) } returns flowOf(earned)
        every { dao.getConfigFlow() } returns flowOf(config)
        return BadgeRepository(dao, pref, gamification)
    }

    @Test
    fun `shelf lists enabled badges with earned counts`() = runTest {
        val state = BadgeStateCache(3, BadgeIds.MATERNAL_JOURNEY, 2, 5, 15, lastEvaluatedAt = 1)
        val earned = listOf(
            BadgeEarnedCache(userId = 3, badgeId = BadgeIds.MATERNAL_JOURNEY, level = 1, earnedAt = 1),
            BadgeEarnedCache(userId = 3, badgeId = BadgeIds.MATERNAL_JOURNEY, level = 2, earnedAt = 1),
            BadgeEarnedCache(userId = 3, badgeId = BadgeIds.COMPLETE_WORKER, level = 1, caseRef = "q1", earnedAt = 1),
            BadgeEarnedCache(userId = 3, badgeId = BadgeIds.COMPLETE_WORKER, level = 1, caseRef = "q2", earnedAt = 1)
        )

        val shelf = repo(listOf(state), earned).shelf.first()

        // critical_referral is off by default
        assertEquals(BadgeDefinitions.ALL.size - 1, shelf.size)
        val maternal = shelf.single { it.definition.id == BadgeIds.MATERNAL_JOURNEY }
        assertEquals(state, maternal.state)
        assertEquals(2, maternal.earnedLevels)
        assertEquals(2, maternal.timesEarned)
        val worker = shelf.single { it.definition.id == BadgeIds.COMPLETE_WORKER }
        assertEquals(1, worker.earnedLevels)
        assertEquals(2, worker.timesEarned)
        assertEquals(null, worker.state)
    }

    @Test
    fun `remote kill switch empties the shelf`() = runTest {
        val shelf = repo(
            config = listOf(BadgeConfigCache(BadgeConfigKeys.FEATURE_ENABLED, "false", 0))
        ).shelf.first()
        assertTrue(shelf.isEmpty())
    }

    @Test
    fun `badges switched off or nobody logged in gives an empty shelf`() = runTest {
        switches.value = GamificationConfigProvider.State(badges = false)
        assertTrue(repo().shelf.first().isEmpty())

        switches.value = GamificationConfigProvider.State(badges = true)
        val r = repo()
        every { pref.getLoggedInUser() } returns null
        assertTrue(r.shelf.first().isEmpty())
    }

    @Test
    fun `turning badges on later fills the shelf`() = runTest {
        switches.value = GamificationConfigProvider.State(badges = false)
        val r = repo()
        val emissions = mutableListOf<Int>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            r.shelf.take(2).toList().forEach { emissions += it.size }
        }
        switches.value = GamificationConfigProvider.State(badges = true)
        job.join()
        assertEquals(listOf(0, BadgeDefinitions.ALL.size - 1), emissions)
    }
}

class BadgeDefinitionsTest {

    private fun state(id: String, level: Int, progress: Long = 0) =
        BadgeStateCache(1, id, level, progress, 1, lastEvaluatedAt = 0)

    @Test
    fun `tiered badges show the highest earned tier`() {
        val def = BadgeDefinitions.byId(BadgeIds.STEADY_SYNCER)!!
        assertEquals(def.tierIcons[0] to false, BadgeDefinitions.displayIcon(def, null))
        assertEquals(def.tierIcons[2] to true, BadgeDefinitions.displayIcon(def, state(def.id, 3)))
        // a server milestone override beyond the artwork falls back to the base icon
        assertEquals(def.iconRes to true, BadgeDefinitions.displayIcon(def, state(def.id, 9)))
    }

    @Test
    fun `quarterly badges show this quarter's artwork`() {
        val def = BadgeDefinitions.byId(BadgeIds.COMPLETE_WORKER)!!
        val quarter = Calendar.getInstance().get(Calendar.MONTH) / 3
        assertEquals(def.tierIcons[quarter] to false, BadgeDefinitions.displayIcon(def, null))
        assertEquals(def.tierIcons[quarter] to true, BadgeDefinitions.displayIcon(def, state(def.id, 1)))
    }

    @Test
    fun `per case badges map level or case count to tiers`() {
        val def = BadgeDefinitions.byId(BadgeIds.VULNERABLE_BABY)!!
        assertEquals(def.tierIcons[0] to false, BadgeDefinitions.displayIcon(def, null))
        assertEquals(def.tierIcons[1] to true, BadgeDefinitions.displayIcon(def, state(def.id, 2, 2)))
        assertEquals(def.tierIcons[2] to true, BadgeDefinitions.displayIcon(def, state(def.id, 0, 3)))
        assertEquals(def.iconRes to true, BadgeDefinitions.displayIcon(def, state(def.id, 0, 7)))
        val referral = BadgeDefinitions.byId(BadgeIds.CRITICAL_REFERRAL)!!
        assertEquals(R.drawable.badge_critical_referral to true,
            BadgeDefinitions.displayIcon(referral, state(referral.id, 1, 1)))
    }

    @Test
    fun `config helpers parse overrides and ignore junk`() {
        val def = BadgeDefinitions.byId(BadgeIds.DIGITAL_IDENTITY)!!
        assertEquals(def.milestones, BadgeDefinitions.effectiveMilestones(def, mapOf(BadgeConfigKeys.milestones(def.id) to "x,y")))
        assertEquals(listOf(5L, 9L), BadgeDefinitions.effectiveMilestones(def, mapOf(BadgeConfigKeys.milestones(def.id) to "5, 9")))
        assertEquals(def.defaultGraceTokens, BadgeDefinitions.effectiveGrace(def, mapOf(BadgeConfigKeys.grace(def.id) to "many")))
        assertEquals(3, BadgeDefinitions.effectiveGrace(def, mapOf(BadgeConfigKeys.grace(def.id) to "3")))
        assertTrue(BadgeDefinitions.isEnabled(def, mapOf(BadgeConfigKeys.enabled(def.id) to "maybe")))
        assertFalse(BadgeDefinitions.isEnabled(def, mapOf(BadgeConfigKeys.enabled(def.id) to "false")))
        assertTrue(BadgeDefinitions.isFeatureEnabled(emptyMap()))
        assertEquals(null, BadgeDefinitions.byId("nope"))
        assertEquals(BadgeKind.PER_CASE, BadgeDefinitions.byId(BadgeIds.CRITICAL_REFERRAL)!!.kind)
    }
}

class BadgeCelebrationsTest {

    @Test
    fun `published awards are delivered once in order`() = runTest {
        val celebrations = BadgeCelebrations()
        celebrations.publish("a", 1)
        celebrations.publish("b", 2)
        assertEquals(listOf("a" to 1, "b" to 2), celebrations.awards.take(2).toList())
    }
}

class BadgeDemoSeederTest {

    private val dao = mockk<BadgeDao>(relaxed = true)
    private val facts = mockk<BadgeFactsReader>()
    private val pref = mockk<PreferenceDao>()
    private val bus = mockk<TaskCompletionBus>(relaxed = true)
    private val seeder = BadgeDemoSeeder(dao, facts, StreakEngine(), pref, bus)
    private val user = mockk<User>().also { every { it.userId } returns 5 }

    @Test
    fun `not logged in does nothing`() = runTest {
        every { pref.getLoggedInUser() } returns null
        assertEquals("Not logged in", seeder.stage())
        coVerify(exactly = 0) { dao.insertEarned(any()) }
    }

    @Test
    fun `stages steady syncer one sync short of its first tier`() = runTest {
        every { pref.getLoggedInUser() } returns user
        coEvery { dao.getConfig() } returns emptyList()
        coEvery { dao.getAllSyncWeeks() } returns emptyList()
        every { facts.activityWeeks() } returns emptySet()
        val logs = mutableListOf<BadgeSyncLogCache>()
        coEvery { dao.insertSyncLog(capture(logs)) } just runs
        val earned = slot<List<BadgeEarnedCache>>()
        coEvery { dao.insertEarned(capture(earned)) } returns emptyList()

        val message = seeder.stage()

        assertEquals("Steady Syncer at 1 weeks — one sync unlocks tier 1", message)
        val now = System.currentTimeMillis()
        assertEquals(listOf(BadgeDates.weekKeyAt(-1, now)), logs.map { it.weekKey })
        coVerify { dao.deleteSyncWeek(BadgeDates.weekKey(now)) }
        assertEquals(6, earned.captured.size)
        assertTrue(earned.captured.all { it.userId == 5 && it.level == 1 })
        verify { bus.publish() }
    }

    @Test
    fun `already at the top tier stages nothing`() = runTest {
        every { pref.getLoggedInUser() } returns user
        coEvery { dao.getConfig() } returns emptyList()
        val now = System.currentTimeMillis()
        coEvery { dao.getAllSyncWeeks() } returns (1..9).map { BadgeDates.weekKeyAt(-it, now) }
        every { facts.activityWeeks() } returns emptySet()

        assertEquals("Steady Syncer is already at its top tier", seeder.stage())
        coVerify(exactly = 0) { dao.insertEarned(any()) }
    }
}
