package org.piramalswasthya.sakhi.helpers

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
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
class GamificationConfigProvider @Inject constructor() {

    enum class Mechanic(val remoteKey: String) {
        BADGES("badges_enabled"),
        MONTHLY_RECAP("monthly_recap_enabled"),
        // Reserve the keys now so Notifications and the Consistency Dashboard
        // need no new provider plumbing when their turn comes — only a new
        // enum entry, a default in the XML, and one line at their gate.
        NOTIFICATIONS("notifications_enabled"),
        CONSISTENCY_DASHBOARD("consistency_dashboard_enabled"),
    }

    companion object {
        const val KEY_MASTER_ENABLED = "gamification_master_enabled"
        const val KEY_PILOT_USER_IDS = "gamification_pilot_user_ids"
    }

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(
                remoteConfigSettings {
                    // Rollout flags should reach a phone within the hour, not
                    // wait for the app's default 12h Remote Config cache.
                    minimumFetchIntervalInSeconds = TimeUnit.HOURS.toSeconds(1)
                }
            )
            setDefaultsAsync(R.xml.remote_config_defaults)
        }
    }

    /**
     * Call once, early (SakhiApplication.onCreate), off the main thread. A failed
     * or slow fetch is NOT an error here — [remoteConfig] already holds the
     * fail-closed XML defaults until a fetch succeeds, so every caller of
     * [isMechanicEnabled] is safe to use immediately after the app starts.
     */
    fun primeAsync() {
        if (BuildConfig.DEBUG) return // debug never reads Remote Config at all
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w("GamificationConfigProvider: fetch failed, staying on cached/default values")
                }
            }
    }

    /**
     * The one gate every mechanic calls. [userId] is the currently logged-in
     * ASHA's id (null when nobody is logged in — always closed in that case).
     */
    fun isMechanicEnabled(mechanic: Mechanic, userId: Int?): Boolean {
        if (BuildConfig.DEBUG) return true

        val masterOn = safeBool(KEY_MASTER_ENABLED)
        if (!masterOn) return false

        val mechanicOn = safeBool(mechanic.remoteKey)
        if (!mechanicOn) return false

        return isUserInPilot(userId)
    }

    /** Empty allowlist = everyone (the intended state for full go-live). */
    private fun isUserInPilot(userId: Int?): Boolean {
        val raw = try {
            remoteConfig.getString(KEY_PILOT_USER_IDS)
        } catch (e: Exception) {
            Timber.e(e, "GamificationConfigProvider: bad $KEY_PILOT_USER_IDS")
            return false // malformed allowlist must never silently mean "everyone"
        }
        val ids = raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        if (ids.isEmpty()) return true
        return userId != null && userId.toString() in ids
    }

    private fun safeBool(key: String): Boolean = try {
        remoteConfig.getBoolean(key)
    } catch (e: Exception) {
        Timber.e(e, "GamificationConfigProvider: bad boolean for $key")
        false // a corrupt value must fail closed, never fail open
    }
}
