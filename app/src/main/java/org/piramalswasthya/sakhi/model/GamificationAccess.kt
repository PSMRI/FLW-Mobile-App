package org.piramalswasthya.sakhi.model

/**
 * FLW-1251: the gamification switches the server sends for one ASHA inside
 * `getUserDetail` → `data.gamification`. Saved on the phone together with the
 * [userId] it belongs to, so it is never applied to another user of the same device.
 *
 * A part is ON only when [enabled] (master) AND its own switch are true.
 */
data class GamificationAccess(
    val userId: Int,
    val enabled: Boolean,
    val badges: Boolean,
    val monthlyRecap: Boolean,
    val eveningNotifications: Boolean,
    val consistencyDashboard: Boolean,
    /** Which setting matched on the server (USER / BLOCK / … / DEFAULT). Support only. */
    val source: String? = null,
    /** Last change of the matching setting on the server, epoch ms. Support only. */
    val updatedAt: Long? = null,
)
