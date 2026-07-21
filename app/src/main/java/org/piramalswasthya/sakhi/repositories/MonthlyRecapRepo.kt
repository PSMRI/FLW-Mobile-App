package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.database.room.dao.MonthlyRecapDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.RecapClock
import org.piramalswasthya.sakhi.helpers.previousMonthWindow
import org.piramalswasthya.sakhi.model.MonthlyRecapCache
import org.piramalswasthya.sakhi.model.MonthlyRecapLanguage
import org.piramalswasthya.sakhi.model.RecapStatus
import org.piramalswasthya.sakhi.model.recapStatus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Local, offline-first boundary for Monthly Recap snapshots.
 *
 * Identity: one snapshot per (logged-in ASHA, immediately previous month).
 * getOrCreate is idempotent — the DAO's insert-IGNORE plus the unique DB index
 * guarantee at most one row even under rapid taps, rotation, process recreation
 * or concurrent coroutines; a lost insert race re-reads the winner.
 *
 * Phase 3 deliberately leaves metricsJson NULL (real on-device calculation is
 * Phase 4) and never fabricates activity counts.
 */
@Singleton
class MonthlyRecapRepo @Inject constructor(
    private val recapDao: MonthlyRecapDao,
    private val preferenceDao: PreferenceDao,
    private val clock: RecapClock,
) {

    private suspend fun loggedInUserId(): Int? = withContext(Dispatchers.IO) {
        preferenceDao.getLoggedInUser()?.userId
    }

    /**
     * Returns the existing snapshot for (current user, previous month) or creates
     * one stable foundation snapshot. Returns null only when no user is logged in.
     */
    suspend fun getOrCreateCurrentRecap(): MonthlyRecapCache? {
        val userId = loggedInUserId() ?: return null
        val window = previousMonthWindow(clock.now())

        recapDao.get(userId, window.yearMonth)?.let { return it }

        val now = clock.now().timeInMillis
        recapDao.insert(
            MonthlyRecapCache(
                userId = userId,
                recapYearMonth = window.yearMonth,
                windowStartMillis = window.startMillis,
                windowEndMillis = window.endMillisExclusive,
                variantSeed = Random.nextLong(), // generated once; stable thereafter
                createdAt = now,
                updatedAt = now,
            )
        ) // IGNORE on conflict: if another call won the race, read the winner below.
        return recapDao.get(userId, window.yearMonth)
    }

    /** Observes the current user's previous-month snapshot (null when absent). */
    fun observeCurrentRecap(): Flow<MonthlyRecapCache?> = flow {
        val userId = loggedInUserId()
        if (userId == null) {
            emit(null)
        } else {
            emitAll(recapDao.observe(userId, previousMonthWindow(clock.now()).yearMonth))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Persists the recap-only language (never the global Sakhi language) for the
     * current snapshot, creating it first when absent. Month, variant seed,
     * metrics and completion state are preserved. Returns true on success.
     */
    suspend fun setRecapLanguage(language: MonthlyRecapLanguage): Boolean {
        val recap = getOrCreateCurrentRecap() ?: run {
            Timber.w("Monthly Recap: cannot persist language, no logged-in user")
            return false
        }
        recapDao.setLanguage(
            recap.userId, recap.recapYearMonth, language.token, clock.now().timeInMillis
        )
        return true
    }

    /** Playback foundation: NOT_STARTED → IN_PROGRESS (completed rows unaffected). */
    suspend fun markStarted() {
        val recap = getOrCreateCurrentRecap() ?: return
        recapDao.markStarted(recap.userId, recap.recapYearMonth, clock.now().timeInMillis)
    }

    /** Playback foundation: stores a coerced, generic safe-progress marker. */
    suspend fun updateSafeProgress(sceneIndex: Int) {
        val userId = loggedInUserId() ?: return
        val window = previousMonthWindow(clock.now())
        val recap = recapDao.get(userId, window.yearMonth) ?: return
        if (recap.recapStatus() != RecapStatus.IN_PROGRESS) return
        val upper = recap.totalScenes?.takeIf { it > 0 } ?: Int.MAX_VALUE
        val safe = sceneIndex.coerceIn(0, upper)
        recapDao.updateProgress(userId, window.yearMonth, safe, clock.now().timeInMillis)
    }

    /** Playback foundation: marks the snapshot completed (drives REPLAY). */
    suspend fun markCompleted() {
        val userId = loggedInUserId() ?: return
        val window = previousMonthWindow(clock.now())
        recapDao.markCompleted(userId, window.yearMonth, clock.now().timeInMillis)
    }
}
