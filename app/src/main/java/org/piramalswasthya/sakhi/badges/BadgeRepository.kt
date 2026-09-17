package org.piramalswasthya.sakhi.badges

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.piramalswasthya.sakhi.badges.domain.BadgeDefinition
import org.piramalswasthya.sakhi.badges.domain.BadgeDefinitions
import org.piramalswasthya.sakhi.database.room.dao.BadgeDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BadgeStateCache
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UI-facing read model for the shelf and the live progress widget.
 * Reads only BADGE_STATE / BADGE_EARNED / BADGE_CONFIG (LLD §4.1).
 */
@Singleton
class BadgeRepository @Inject constructor(
    private val badgeDao: BadgeDao,
    private val pref: PreferenceDao
) {

    data class BadgeCard(
        val definition: BadgeDefinition,
        val state: BadgeStateCache?,
        /** Distinct milestone levels permanently earned. */
        val earnedLevels: Int,
        /** For re-earnable / per-case badges: total recognitions. */
        val timesEarned: Int
    )

    /**
     * Empty list ⇔ feature killed remotely, or nobody logged in — UI hides cleanly
     * (LLD §5.2).
     *
     * Scoped to the signed-in ASHA. The badge tables survive a drawer logout, by design, so
     * that unsynced awards are not lost; reading them unscoped would show the next ASHA to
     * use the device the previous one's shelf.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val shelf: Flow<List<BadgeCard>> = flow { emit(pref.getLoggedInUser()?.userId) }
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList()) else shelfFor(userId)
        }

    private fun shelfFor(userId: Int): Flow<List<BadgeCard>> = combine(
        badgeDao.getStatesFlow(userId),
        badgeDao.getEarnedFlow(userId),
        badgeDao.getConfigFlow()
    ) { states, earned, configRows ->
        val config = configRows.associate { it.key to it.value }
        if (!BadgeDefinitions.isFeatureEnabled(config)) return@combine emptyList()

        val stateById = states.associateBy { it.badgeId }
        BadgeDefinitions.ALL
            .filter { BadgeDefinitions.isEnabled(it, config) }
            .map { def ->
                val earnedRows = earned.filter { it.badgeId == def.id }
                BadgeCard(
                    definition = def,
                    state = stateById[def.id],
                    earnedLevels = earnedRows.map { it.level }.distinct().size,
                    timesEarned = earnedRows.size
                )
            }
    }
}
