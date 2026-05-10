package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.sakhi.database.room.dao.GamificationDao
import org.piramalswasthya.sakhi.model.GamificationBadge
import org.piramalswasthya.sakhi.model.GamificationProfile
import org.piramalswasthya.sakhi.model.PointsTransaction
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single data-access point for all gamification state.
 *
 * Follows the same @Inject constructor + @Singleton pattern
 * used by every other repository in this project.
 *
 * Network sync stubs are intentionally left as TODOs here — the
 * GamificationSyncWorker calls this repo and will call the API
 * once the backend endpoint is available from FLW-API service.
 */
@Singleton
class GamificationRepo @Inject constructor(
    private val dao: GamificationDao
) {

    // ── Profile ───────────────────────────────────────────────────────────────

    suspend fun getProfile(userId: Int): GamificationProfile? =
        dao.getProfile(userId)

    suspend fun upsertProfile(profile: GamificationProfile) =
        dao.upsertProfile(profile)

    fun observeProfile(userId: Int): Flow<GamificationProfile?> =
        dao.observeProfile(userId)

    // ── Badges ────────────────────────────────────────────────────────────────

    suspend fun insertBadge(badge: GamificationBadge): Long =
        dao.insertBadge(badge)

    suspend fun hasBadge(userId: Int, badgeType: String): Boolean =
        dao.hasBadge(userId, badgeType) > 0

    fun observeBadges(userId: Int): Flow<List<GamificationBadge>> =
        dao.observeBadges(userId)

    // ── Transactions ──────────────────────────────────────────────────────────

    suspend fun insertTransaction(tx: PointsTransaction): Long =
        dao.insertTransaction(tx)

    fun observeRecentTransactions(userId: Int): Flow<List<PointsTransaction>> =
        dao.observeRecentTransactions(userId)

    // ── Sync helpers ──────────────────────────────────────────────────────────

    suspend fun getUnsyncedProfiles(): List<GamificationProfile> =
        dao.getUnsyncedProfiles()

    suspend fun getUnsyncedBadges(): List<GamificationBadge> =
        dao.getUnsyncedBadges()

    suspend fun getUnsyncedTransactions(): List<PointsTransaction> =
        dao.getUnsyncedTransactions()

    suspend fun markProfileSynced(userId: Int) =
        dao.markProfileSynced(userId)

    suspend fun markBadgesSynced(ids: List<Long>) =
        dao.markBadgesSynced(ids)

    suspend fun markTransactionsSynced(ids: List<Long>) =
        dao.markTransactionsSynced(ids)
}
