package org.piramalswasthya.sakhi.badges

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.piramalswasthya.sakhi.badges.domain.BadgeDefinition
import org.piramalswasthya.sakhi.badges.domain.BadgeDefinitions
import org.piramalswasthya.sakhi.database.room.dao.BadgeDao
import org.piramalswasthya.sakhi.model.BadgeStateCache
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UI-facing read model for the shelf and the live progress widget.
 * Reads only BADGE_STATE / BADGE_EARNED / BADGE_CONFIG (LLD §4.1).
 */
@Singleton
class BadgeRepository @Inject constructor(
    private val badgeDao: BadgeDao
) {

    data class BadgeCard(
        val definition: BadgeDefinition,
        val state: BadgeStateCache?,
        /** Distinct milestone levels permanently earned. */
        val earnedLevels: Int,
        /** For re-earnable / per-case badges: total recognitions. */
        val timesEarned: Int
    )

    /** Empty list ⇔ feature killed remotely — UI hides cleanly (LLD §5.2). */
    val shelf: Flow<List<BadgeCard>> = combine(
        badgeDao.getAllStatesFlow(),
        badgeDao.getAllEarnedFlow(),
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
