package org.piramalswasthya.sakhi.helpers

import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import javax.inject.Inject

/**
 * Rollout gates for the Monthly Recap.
 *
 * Tri-state per gate: true / false / null (unknown). Availability is true only
 * when every required gate is explicitly true — any false OR UNKNOWN gate fails
 * closed to unavailable (dashboard renders HIDDEN).
 */
data class MonthlyRecapGates(
    val emergencySwitchOn: Boolean? = null,
    val moduleEnabled: Boolean? = null,
    val userEligible: Boolean? = null,
    val activeWindowOpen: Boolean? = null,
    val configFresh: Boolean? = null,
) {
    fun available(): Boolean =
        emergencySwitchOn == true &&
                moduleEnabled == true &&
                userEligible == true &&
                activeWindowOpen == true &&
                configFresh == true
}

/** Replaceable availability boundary consumed by the dashboard strip. */
interface MonthlyRecapAvailabilityProvider {
    /** Ready / Resume strip during the active calendar window (or test cohort). */
    suspend fun isAvailable(): Boolean

    /**
     * Watch again after COMPLETED: mechanic + pilot still required, but the
     * days 1–7 window is ignored so she can replay anytime in the month.
     */
    suspend fun isReplayAvailable(): Boolean
}

/**
 * Local availability provider, backed by the shared [GamificationConfigProvider]
 * (one Remote Config provider for every gamification mechanic — see its own doc).
 *
 * Debug builds keep the rollout/eligibility gates locally enabled for
 * development. The active-window gate uses the real first-seven-day calendar
 * rule ([isMonthlyRecapWindowOpen]), except for users listed in Remote Config
 * [GamificationConfigProvider.KEY_TEST_COHORT_USER_IDS] (and every logged-in
 * debug user), who may open/replay the recap any day for QA.
 *
 * Release reads the real gates: `gamification_master_enabled` maps to
 * [MonthlyRecapGates.emergencySwitchOn], `monthly_recap_enabled` (plus the
 * shared pilot allowlist) maps to [MonthlyRecapGates.moduleEnabled] AND
 * [MonthlyRecapGates.userEligible] together (the provider's single
 * `isMechanicEnabled` call already folds allowlist membership in), and
 * [MonthlyRecapGates.configFresh] is true whenever a user is logged in — a
 * fetch failure does not need its own flag because Remote Config already falls
 * back to the XML defaults on failure, which [configProvider] reads regardless of
 * fetch outcome. Those defaults are ON, so a fetch that never succeeds leaves the
 * recap available rather than hidden.
 */
class LocalMonthlyRecapAvailability @Inject constructor(
    private val clock: RecapClock,
    private val configProvider: GamificationConfigProvider,
    private val pref: PreferenceDao,
) : MonthlyRecapAvailabilityProvider {

    override suspend fun isAvailable(): Boolean {
        val userId = pref.getLoggedInUser()?.userId
        val windowOpen =
            configProvider.isTestCohortUser(userId) || isMonthlyRecapWindowOpen(clock.now())
        return if (BuildConfig.DEBUG) {
            MonthlyRecapGates(
                emergencySwitchOn = true,
                moduleEnabled = true,
                userEligible = true,
                activeWindowOpen = windowOpen,
                configFresh = true,
            ).available()
        } else {
            val recapOn = configProvider.isMechanicEnabled(
                GamificationConfigProvider.Mechanic.MONTHLY_RECAP, userId
            )
            MonthlyRecapGates(
                emergencySwitchOn = recapOn,
                moduleEnabled = recapOn,
                userEligible = recapOn,
                activeWindowOpen = windowOpen,
                configFresh = userId != null,
            ).available()
        }
    }

    override suspend fun isReplayAvailable(): Boolean {
        val userId = pref.getLoggedInUser()?.userId
        return if (BuildConfig.DEBUG) {
            userId != null
        } else {
            configProvider.isMechanicEnabled(
                GamificationConfigProvider.Mechanic.MONTHLY_RECAP, userId
            )
        }
    }
}
