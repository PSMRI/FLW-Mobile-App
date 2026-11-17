package org.piramalswasthya.sakhi.helpers

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.GamificationConfigProvider.DebugOverride
import org.piramalswasthya.sakhi.helpers.GamificationConfigProvider.Mechanic
import org.piramalswasthya.sakhi.helpers.GamificationConfigProvider.State
import org.piramalswasthya.sakhi.model.GamificationAccess
import org.piramalswasthya.sakhi.model.User

/** FLW-1251: server-only gamification switch. */
class GamificationConfigProviderTest {

    private val pref: PreferenceDao = mockk(relaxed = true)

    /** In-memory stand-ins for the two prefs the provider reads and writes. */
    private var savedAccess: GamificationAccess? = null
    private var savedOverride: String? = null
    private var loggedInUserId: Int? = 7

    @Before
    fun setUp() {
        every { pref.getLoggedInUser() } answers {
            loggedInUserId?.let { id -> mockk<User> { every { userId } returns id } }
        }
        every { pref.getGamificationAccess() } answers { savedAccess }
        val accessSlot = slot<GamificationAccess>()
        every { pref.saveGamificationAccess(capture(accessSlot)) } answers {
            savedAccess = accessSlot.captured
        }
        every { pref.gamificationDebugOverride } answers { savedOverride }
        every { pref.gamificationDebugOverride = any() } answers { savedOverride = firstArg() }
    }

    private fun access(
        userId: Int = 7,
        enabled: Boolean = true,
        badges: Boolean = true,
        recap: Boolean = true,
        notifications: Boolean = true,
    ) = GamificationAccess(userId, enabled, badges, recap, notifications, false)

    private fun serverBlock(
        enabled: Boolean,
        badges: Boolean = false,
        recap: Boolean = false,
        notifications: Boolean = false,
    ) = mapOf(
        "enabled" to enabled,
        "mechanics" to mapOf(
            "badges" to badges, "monthlyRecap" to recap, "eveningNotifications" to notifications
        )
    )

    @Test
    fun `nothing received yet means everything off`() {
        val provider = GamificationConfigProvider(pref)

        assertEquals(State.ALL_OFF, provider.state.value)
        Mechanic.values().forEach { assertFalse(provider.isMechanicEnabled(it, 7)) }
    }

    @Test
    fun `nobody logged in means everything off`() {
        savedAccess = access()
        loggedInUserId = null

        assertEquals(State.ALL_OFF, GamificationConfigProvider(pref).state.value)
    }

    @Test
    fun `master switch off turns every part off`() {
        savedAccess = access(enabled = false)

        val provider = GamificationConfigProvider(pref)

        assertEquals(State.ALL_OFF, provider.state.value)
    }

    @Test
    fun `each part follows its own switch`() {
        savedAccess = access(badges = true, recap = false, notifications = true)

        val provider = GamificationConfigProvider(pref)

        assertTrue(provider.isMechanicEnabled(Mechanic.BADGES, 7))
        assertFalse(provider.isMechanicEnabled(Mechanic.MONTHLY_RECAP, 7))
        assertTrue(provider.isMechanicEnabled(Mechanic.NOTIFICATIONS, 7))
        assertFalse(provider.isMechanicEnabled(Mechanic.CONSISTENCY_DASHBOARD, 7))
    }

    @Test
    fun `a value saved for another user is ignored`() {
        savedAccess = access(userId = 99)

        assertEquals(State.ALL_OFF, GamificationConfigProvider(pref).state.value)
    }

    @Test
    fun `server flag from getUserDetail is saved and applied`() {
        val provider = GamificationConfigProvider(pref)

        provider.onUserDetail(7, serverBlock(enabled = true, badges = true))

        assertEquals(State(badges = true), provider.state.value)
        assertEquals(7, savedAccess?.userId)
    }

    @Test
    fun `missing or malformed block keeps the last saved value`() {
        savedAccess = access()
        val provider = GamificationConfigProvider(pref)

        provider.onUserDetail(7, null)
        provider.onUserDetail(7, "garbage")

        assertEquals(State.ALL_ON, provider.state.value)
        verify(exactly = 0) { pref.saveGamificationAccess(any()) }
    }

    @Test
    fun `server can switch an ASHA off again`() {
        savedAccess = access()
        val provider = GamificationConfigProvider(pref)

        provider.onUserDetail(7, serverBlock(enabled = false))

        assertEquals(State.ALL_OFF, provider.state.value)
    }

    @Test
    fun `turning badges on makes the next badge run silent`() {
        val provider = GamificationConfigProvider(pref)

        provider.onUserDetail(7, serverBlock(enabled = true, badges = true))

        verify { pref.isBadgeBackfillDone = false }
    }

    @Test
    fun `badges staying on does not reset the silent run`() {
        savedAccess = access()
        val provider = GamificationConfigProvider(pref)

        provider.onUserDetail(7, serverBlock(enabled = true, badges = true, recap = true))

        verify(exactly = 0) { pref.isBadgeBackfillDone = any() }
    }

    @Test
    fun `debug override forces on and off and goes back to the server value`() {
        val provider = GamificationConfigProvider(pref)
        // the override exists only on debug/UAT variants; skip on other release variants
        assumeTrue(provider.isDebugOverrideAllowed)

        provider.debugOverride = DebugOverride.ON
        assertEquals(State.ALL_ON, provider.state.value)

        savedAccess = access()
        provider.debugOverride = DebugOverride.OFF
        assertEquals(State.ALL_OFF, provider.state.value)

        provider.debugOverride = DebugOverride.SERVER
        assertEquals(State.ALL_ON, provider.state.value)
        assertEquals(null, savedOverride)
    }

    @Test
    fun `flag refresh from Home is throttled`() {
        val provider = GamificationConfigProvider(pref)
        val t0 = 1_000_000_000L
        val interval = GamificationConfigProvider.FLAG_REFRESH_INTERVAL_MS

        assertTrue(provider.claimFlagRefresh(t0))                 // first time: due
        assertFalse(provider.claimFlagRefresh(t0 + 60_000))       // a minute later: not due
        assertTrue(provider.claimFlagRefresh(t0 + interval))      // interval passed: due
    }

    @Test
    fun `a fetch at app open counts as a refresh`() {
        val provider = GamificationConfigProvider(pref)
        val t0 = 1_000_000_000L

        provider.markFlagRefreshed(t0)

        assertFalse(provider.claimFlagRefresh(t0 + 1_000))
    }

    @Test
    fun `a clock moved backwards makes the refresh due`() {
        val provider = GamificationConfigProvider(pref)
        val t0 = 1_000_000_000L
        provider.markFlagRefreshed(t0)

        assertTrue(provider.claimFlagRefresh(t0 - 60_000))
    }

    @Test
    fun `remote config test cohort is retired`() {
        assertFalse(GamificationConfigProvider(pref).isTestCohortUser(7))
    }
}
