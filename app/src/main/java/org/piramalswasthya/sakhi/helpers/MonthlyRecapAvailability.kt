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
    suspend fun isAvailable(): Boolean
}

/**
 * Local availability provider, backed by the shared [GamificationConfigProvider]
 * (one Remote Config provider for every gamification mechanic — see its own doc).
 *
 * Debug builds keep the rollout/eligibility gates locally enabled for
 * development; the active-window gate always uses the real first-seven-day
 * calendar rule ([isMonthlyRecapWindowOpen]) even in debug, so the strip is
 * shown only on days 1..[ACTIVE_WINDOW_DAYS] of the month, never every day.
 *
 * Release reads the real gates: `gamification_master_enabled` maps to
 * [MonthlyRecapGates.emergencySwitchOn], `monthly_recap_enabled` (plus the
 * shared pilot allowlist) maps to [MonthlyRecapGates.moduleEnabled] AND
 * [MonthlyRecapGates.userEligible] together (the provider's single
 * `isMechanicEnabled` call already folds allowlist membership in), and
 * [MonthlyRecapGates.configFresh] is true whenever a user is logged in — a
 * fetch failure does not need its own flag because Remote Config already falls
 * back to the fail-closed XML defaults on failure, which [configProvider]
 * reads regardless of fetch outcome.
 */
class LocalMonthlyRecapAvailability @Inject constructor(
    private val clock: RecapClock,
    private val configProvider: GamificationConfigProvider,
    private val pref: PreferenceDao,
) : MonthlyRecapAvailabilityProvider {
    override suspend fun isAvailable(): Boolean =
        if (BuildConfig.DEBUG) {
            MonthlyRecapGates(
                emergencySwitchOn = true,
                moduleEnabled = true,
                userEligible = true,
                activeWindowOpen = isMonthlyRecapWindowOpen(clock.now()),
                configFresh = true,
            ).available()
        } else {
            val userId = pref.getLoggedInUser()?.userId
            // The shared provider already folds master-switch + mechanic-key +
            // pilot-allowlist into one boolean, so the three legacy gate fields
            // below intentionally carry the SAME value — the tri-state struct
            // is kept only because MonthlyRecapGates is the existing, tested
            // contract MonthlyRecapStripViewModel and its tests build against.
            val recapOn = configProvider.isMechanicEnabled(
                GamificationConfigProvider.Mechanic.MONTHLY_RECAP, userId
            )
            MonthlyRecapGates(
                emergencySwitchOn = recapOn,
                moduleEnabled = recapOn,
                userEligible = recapOn,
                activeWindowOpen = isMonthlyRecapWindowOpen(clock.now()),
                configFresh = userId != null,
            ).available()
        }
}
