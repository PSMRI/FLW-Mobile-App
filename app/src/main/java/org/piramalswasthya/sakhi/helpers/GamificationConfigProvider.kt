package org.piramalswasthya.sakhi.helpers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FLW-1251 (2026-11-13) — SERVER-ONLY GATING. Every gamification part is now switched
 * on/off per ASHA by the server, through the `gamification` object of
 * `flw-api/user/getUserDetail` (read at login and on every app open, see
 * [onUserDetail]). A part is ON only when the server's `enabled` AND that part's own
 * switch are true. Until the server has sent a value for this ASHA, everything is OFF.
 * The Firebase Remote Config design described below is retired; its code is kept,
 * commented out, in this file.
 *
 * Debug and UAT builds have a developer override ([debugOverride]) so the feature can be
 * shown or hidden without the backend; its default follows the server.
 *
 * --- Original design (Remote Config, retired) ---
 *
 * ONE Remote Config provider for every gamification mechanic (Monthly Recap,
 * Badges, and — when built — Notifications and the Consistency Dashboard).
 *
 * Design, locked 2026-08-10: a single master switch plus a shared pilot
 * allowlist, with one boolean key PER mechanic. Not one switch for the whole
 * module (can't pilot or kill one mechanic independently of the others), and
 * not a separate Remote Config wrapper per mechanic (same plumbing built four
 * times). A mechanic is ON only when ALL of:
 *   1. [KEY_MASTER_ENABLED] is true            — the one big kill switch
 *   2. the mechanic's own key is true          — e.g. [KEY_BADGES_ENABLED]
 *   3. the pilot allowlist is empty, OR the logged-in userId is in it
 *
 * FAIL CLOSED by construction: every default in remote_config_defaults.xml is
 * false/empty, so a phone that has never fetched, or a fetch that fails, shows
 * NOTHING rather than guessing something is enabled.
 *
 * Debug builds bypass Remote Config entirely and force every mechanic ON —
 * that decision already existed per-mechanic (BadgeGates, MonthlyRecapGates)
 * and is preserved here so local dev/demo work is unaffected by this change.
 */
@Singleton
class GamificationConfigProvider @Inject constructor(
    private val pref: PreferenceDao,
) {

    enum class Mechanic(val remoteKey: String) {
        BADGES("badges_enabled"),
        MONTHLY_RECAP("monthly_recap_enabled"),
        NOTIFICATIONS("notifications_enabled"),
        CONSISTENCY_DASHBOARD("consistency_dashboard_enabled"),
    }

    /** What the logged-in ASHA may see right now. All false = gamification hidden. */
    data class State(
        val badges: Boolean = false,
        val monthlyRecap: Boolean = false,
        val notifications: Boolean = false,
    ) {
        fun isOn(mechanic: Mechanic): Boolean = when (mechanic) {
            Mechanic.BADGES -> badges
            Mechanic.MONTHLY_RECAP -> monthlyRecap
            Mechanic.NOTIFICATIONS -> notifications
            Mechanic.CONSISTENCY_DASHBOARD -> false // not built yet
        }

        companion object {
            val ALL_OFF = State()
            val ALL_ON = State(badges = true, monthlyRecap = true, notifications = true)
        }
    }

    /** Developer override, honoured only on debug/UAT builds. */
    enum class DebugOverride { SERVER, ON, OFF }

    companion object {
        const val KEY_MASTER_ENABLED = "gamification_master_enabled"
        const val KEY_PILOT_USER_IDS = "gamification_pilot_user_ids"
        /** Comma-separated userIds for a small QA cohort (window bypass, etc.). */
        const val KEY_TEST_COHORT_USER_IDS = "gamification_test_cohort_user_ids"

        /** FLW-1251: minimum gap between server-flag fetches triggered by returning to Home. */
        const val FLAG_REFRESH_INTERVAL_MS = 15 * 60 * 1000L
    }

    /** Same rule as ChildImmunizationCategory.earlyEntryEnabled: debug or UAT flavours. */
    val isDebugOverrideAllowed: Boolean =
        BuildConfig.DEBUG || BuildConfig.FLAVOR.contains("uat", ignoreCase = true)

    private val _state = MutableStateFlow(stateFor(loggedInUserId()))

    /** Current switches for the logged-in ASHA; HomeActivity and the UI react to it. */
    val state: StateFlow<State> = _state.asStateFlow()

    /**
     * The one gate every mechanic calls. [userId] is the currently logged-in
     * ASHA's id (null when nobody is logged in — always closed in that case).
     */
    fun isMechanicEnabled(mechanic: Mechanic, userId: Int?): Boolean {
        return stateFor(userId).isOn(mechanic)
    }

    /** [isMechanicEnabled] for whoever is logged in now (receivers, workers). */
    fun isEnabledForLoggedInUser(mechanic: Mechanic): Boolean =
        isMechanicEnabled(mechanic, loggedInUserId())

    /**
     * Called with the raw `data.gamification` object of every `getUserDetail` response
     * (login and app open). A missing or unusable object is "no new decision": the last
     * saved value stays. Never throws — this runs inside the login flow.
     */
    fun onUserDetail(userId: Int, rawGamification: Any?) {
        try {
            val access = GamificationAccessParser.parse(rawGamification, userId)
            if (access == null) {
                Timber.w("Gamification: no usable flag in getUserDetail, keeping last saved value")
            } else {
                pref.saveGamificationAccess(access)
            }
        } catch (e: Exception) {
            Timber.e(e, "Gamification: could not save server flag")
        }
        refresh()
    }

    /** When the server flag was last fetched (getUserDetail), epoch ms; 0 = not yet. */
    @Volatile
    private var lastFlagRefreshAt = 0L

    /** Records a getUserDetail fetch that will deliver the flag (app open, Home refresh). */
    fun markFlagRefreshed(nowMs: Long) {
        lastFlagRefreshAt = nowMs
    }

    /**
     * True (and records [nowMs]) when the flag should be fetched again because Home is
     * shown and the last fetch is at least [FLAG_REFRESH_INTERVAL_MS] old. Keeps returning
     * to Home from sending a getUserDetail request every time.
     */
    @Synchronized
    fun claimFlagRefresh(nowMs: Long): Boolean {
        val since = nowMs - lastFlagRefreshAt
        if (lastFlagRefreshAt != 0L && since in 0 until FLAG_REFRESH_INTERVAL_MS) return false
        lastFlagRefreshAt = nowMs
        return true
    }

    fun refresh() {
        val old = _state.value
        val new = stateFor(loggedInUserId())
        if (!old.badges && new.badges) {
            pref.isBadgeBackfillDone = false
        }
        _state.value = new
    }

    var debugOverride: DebugOverride
        get() {
            if (!isDebugOverrideAllowed) return DebugOverride.SERVER
            return DebugOverride.values().firstOrNull { it.name == pref.gamificationDebugOverride }
                ?: DebugOverride.SERVER
        }
        set(value) {
            if (!isDebugOverrideAllowed) return
            pref.gamificationDebugOverride =
                if (value == DebugOverride.SERVER) null else value.name
            refresh()
        }

    private fun stateFor(userId: Int?): State {
        if (userId == null) return State.ALL_OFF
        when (debugOverride) {
            DebugOverride.ON -> return State.ALL_ON
            DebugOverride.OFF -> return State.ALL_OFF
            DebugOverride.SERVER -> Unit
        }
        val access = pref.getGamificationAccess()?.takeIf { it.userId == userId }
            ?: return State.ALL_OFF
        if (!access.enabled) return State.ALL_OFF
        return State(
            badges = access.badges,
            monthlyRecap = access.monthlyRecap,
            notifications = access.eveningNotifications,
        )
    }

    private fun loggedInUserId(): Int? = try {
        pref.getLoggedInUser()?.userId
    } catch (e: Exception) {
        null
    }

    /**
     * True when [userId] is listed in [KEY_TEST_COHORT_USER_IDS]. Empty list =
     * nobody (unlike the pilot allowlist, which treats empty as everyone).
     * Used to bypass the Monthly Recap days 1–7 window for a small QA cohort.
     *
     * FLW-1251: the cohort list came from Remote Config, which is retired, so nobody is
     * in it any more.
     */
    @Suppress("UNUSED_PARAMETER")
    fun isTestCohortUser(userId: Int?): Boolean {
        return false
    }
}
