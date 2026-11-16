package org.piramalswasthya.sakhi.helpers

import org.piramalswasthya.sakhi.model.GamificationAccess

/**
 * FLW-1251: reads the raw `gamification` object of a `getUserDetail` response.
 *
 * Expected shape:
 * ```
 * "gamification": {
 *   "enabled": true,
 *   "mechanics": { "badges": true, "monthlyRecap": true,
 *                  "eveningNotifications": false, "consistencyDashboard": false },
 *   "source": "BLOCK",
 *   "updatedAt": 1762156800000
 * }
 * ```
 *
 * Never throws. Returns null when the object is missing or unusable (no `enabled`
 * boolean), which callers treat as "no new decision from the server" — the last saved
 * value stays. A missing or non-boolean mechanic switch counts as false.
 */
object GamificationAccessParser {

    fun parse(raw: Any?, userId: Int): GamificationAccess? {
        val block = raw as? Map<*, *> ?: return null
        val enabled = block["enabled"] as? Boolean ?: return null
        val mechanics = block["mechanics"] as? Map<*, *> ?: emptyMap<Any?, Any?>()
        fun isOn(key: String) = mechanics[key] == true
        return GamificationAccess(
            userId = userId,
            enabled = enabled,
            badges = isOn("badges"),
            monthlyRecap = isOn("monthlyRecap"),
            eveningNotifications = isOn("eveningNotifications"),
            consistencyDashboard = isOn("consistencyDashboard"),
            source = block["source"] as? String,
            updatedAt = (block["updatedAt"] as? Number)?.toLong(),
        )
    }
}
