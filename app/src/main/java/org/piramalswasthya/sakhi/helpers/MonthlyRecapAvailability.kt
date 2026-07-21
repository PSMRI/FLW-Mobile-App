package org.piramalswasthya.sakhi.helpers

import org.piramalswasthya.sakhi.BuildConfig
import javax.inject.Inject

/**
 * Rollout gates for the Monthly Recap — CONTRACT ONLY in Phase 3.
 *
 * Tri-state per gate: true / false / null (unknown). Availability is true only
 * when every required gate is explicitly true — any false OR UNKNOWN gate fails
 * closed to unavailable (dashboard renders HIDDEN).
 *
 * The real sources (Firebase emergency switch, backend module flag, user
 * eligibility, entry/expiry window config, cached-config freshness) arrive in the
 * rollout phase. No window durations are invented here.
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
 * Local availability provider. Debug builds keep the rollout/eligibility gates
 * locally enabled for development, BUT the active-window gate now uses the real
 * first-seven-day calendar rule ([isMonthlyRecapWindowOpen]) — so the strip is
 * shown only on days 1..[ACTIVE_WINDOW_DAYS] of the month, never every day.
 *
 * Release stays fail-closed (all gates unknown → unavailable) until the real
 * Firebase/backend rollout provider replaces this. No user IDs, no network, no
 * Room dependency.
 */
class LocalMonthlyRecapAvailability @Inject constructor(
    private val clock: RecapClock,
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
            MonthlyRecapGates().available() // all unknown → fail closed
        }
}
