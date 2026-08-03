package org.piramalswasthya.sakhi.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.database.room.dao.MonthlyRecapDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.MonthlyRecapMetricsCodec
import org.piramalswasthya.sakhi.helpers.RecapClock
import org.piramalswasthya.sakhi.helpers.RecapDataReadiness
import org.piramalswasthya.sakhi.helpers.RecapScene
import org.piramalswasthya.sakhi.helpers.RecapSceneComposer
import org.piramalswasthya.sakhi.helpers.previousMonthWindow
import org.piramalswasthya.sakhi.helpers.recapFreezeBlocker
import org.piramalswasthya.sakhi.model.RecapContentLibrary
import org.piramalswasthya.sakhi.model.MonthlyRecapCache
import org.piramalswasthya.sakhi.model.MonthlyRecapMetricsContract
import org.piramalswasthya.sakhi.model.MonthlyRecapMetricsPayload
import org.piramalswasthya.sakhi.model.RecapDiagnostics
import org.piramalswasthya.sakhi.model.RecapFreezeBlocker
import org.piramalswasthya.sakhi.model.RecapMetricStatus
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
    private val metricsCalculator: MonthlyRecapMetricsCalculator,
) {

    /** Serializes metric generation so one payload is frozen per user+month. */
    private val metricsMutex = Mutex()

    private suspend fun loggedInUserId(): Int? = withContext(Dispatchers.IO) {
        preferenceDao.getLoggedInUser()?.userId
    }

    private suspend fun loggedInUserName(): String? = withContext(Dispatchers.IO) {
        preferenceDao.getLoggedInUser()?.userName
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
     * The language the recap is narrated in: whatever the ASHA already uses the app
     * in. There is no separate recap language choice — she is never asked.
     *
     * The app supports more languages than the recap has content for (English is the
     * app default, and Bangla exists too, while the content file carries only Hindi
     * and Assamese). Unmatched languages fall back to Hindi inside
     * [RecapSceneComposer], so this never returns a story-less recap on its own.
     */
    private suspend fun appLanguageToken(): String = withContext(Dispatchers.IO) {
        preferenceDao.getCurrentLanguage().symbol
    }

    /** Playback foundation: NOT_STARTED → IN_PROGRESS (completed rows unaffected). */
    suspend fun markStarted() {
        val recap = getOrCreateCurrentRecap() ?: return
        recapDao.markStarted(recap.userId, recap.recapYearMonth, clock.now().timeInMillis)
    }

    /**
     * Phase 7: called when playback opens with a composed story — marks the
     * snapshot started AND records the story length so resume clamping works
     * against the real scene count.
     */
    suspend fun onPlaybackOpened(totalScenes: Int) {
        val recap = getOrCreateCurrentRecap() ?: return
        val now = clock.now().timeInMillis
        recapDao.markStarted(recap.userId, recap.recapYearMonth, now)
        if (totalScenes > 0 && recap.totalScenes != totalScenes) {
            recapDao.setTotalScenes(recap.userId, recap.recapYearMonth, totalScenes, now)
        }
    }

    /**
     * Stores the CURRENT scene index (clamped) — the scene being shown, i.e. the
     * exact resume target. Reopening an IN_PROGRESS recap starts AT this scene.
     */
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

    /**
     * Ensures the current recap snapshot carries a frozen, privacy-safe metrics
     * payload, calculating it once from verified local activity data (Phase 4.3:
     * CBAC screening events) and reusing it thereafter.
     *
     * Freeze/stability guarantees:
     * - the first valid, current-version payload wins and is never recalculated;
     * - later local edits/sync do NOT change an already-frozen recap;
     * - it uses the window frozen on the snapshot, so it is stable across the days
     *   1–7 the recap is available;
     * - concurrent callers are serialized by [metricsMutex] and a re-read inside
     *   the lock, so exactly one payload is stored and every caller returns it;
     * - a corrupt or unsupported stored payload is treated as absent and
     *   regenerated deterministically (never crashes, never silently reinterpreted);
     * - it freezes ONLY when this install's local data can be trusted for the month
     *   ([recapFreezeBlocker]) — otherwise the month is hidden and re-evaluated on the
     *   next open, so a re-installed or mid-month device can never permanently lock in
     *   a number lower than the ASHA's real work. An ALREADY-frozen payload is returned
     *   as-is and never re-gated: it passed readiness when it was written.
     *
     * Does NOT change status/progress (metric generation is not playback), the
     * language, the variant seed or the snapshot identity. Intended to be called
     * at recap generation/entry time (e.g. by Phase 6 playback) on a background
     * dispatcher — the DAO suspend calls run off the main thread. Returns null
     * only when no user is logged in.
     */
    suspend fun ensureCurrentRecapMetrics(): MonthlyRecapMetricsPayload? {
        val userName = loggedInUserName() ?: return null
        val recap = getOrCreateCurrentRecap() ?: return null
        // Fast path: a valid, current-version payload is already frozen.
        decodeIfCurrent(recap.metricsJson, recap.recapYearMonth)?.let { return it }

        return metricsMutex.withLock {
            // In-process optimisation: re-read inside the lock so a concurrent
            // caller on this instance doesn't recompute.
            val fresh = recapDao.get(recap.userId, recap.recapYearMonth)
            decodeIfCurrent(fresh?.metricsJson, recap.recapYearMonth)?.let { return@withLock it }

            val now = clock.now().timeInMillis
            val raw = metricsCalculator.calculate(
                userId = recap.userId,
                userName = userName,
                recapYearMonth = recap.recapYearMonth,
                windowStartMillis = recap.windowStartMillis,
                windowEndMillisExclusive = recap.windowEndMillis,
                generatedAt = now,
            )
            val readiness = withContext(Dispatchers.IO) {
                RecapDataReadiness(
                    isFullPullComplete = preferenceDao.isFullPullComplete,
                    localDataSince = preferenceDao.recapLocalDataSince(now),
                )
            }
            // ONE freeze decision, covering both the long-standing zero-month rule
            // and local-data readiness. See [recapFreezeBlocker] for the reasoning.
            val blocker = recapFreezeBlocker(
                readiness = readiness,
                windowStartMillis = recap.windowStartMillis,
                hasCountableWork = raw.categories.any {
                    it.status == RecapMetricStatus.AVAILABLE.name && it.categoryTotal > 0
                },
            )
            val payload = raw.copy(
                diagnostics = RecapDiagnostics(
                    freezeBlocker = blocker.name,
                    isFullPullComplete = readiness.isFullPullComplete,
                    localDataSince = readiness.localDataSince,
                    evaluatedAt = now,
                    appVersionCode = BuildConfig.VERSION_CODE,
                )
            )
            when (blocker) {
                // ZERO-MONTH GUARD (unchanged): an all-zero payload is returned but
                // NOT frozen. A first dashboard visit can run before the server pull
                // lands the previous month's records; freezing zeros then would hide
                // the recap for the whole month. Zero payloads show nothing anyway
                // (empty-month rule), so deferring the freeze until real work is
                // visible is safe — and the story is still always frozen BEFORE the
                // ASHA ever sees it.
                RecapFreezeBlocker.NO_COUNTABLE_WORK -> return@withLock payload

                // NOT READY: counts exist but the local database cannot be trusted to
                // be complete for this month. Freezing would permanently under-credit
                // her work, so the month is hidden (empty categories => the recap does
                // not open) and re-evaluated on her next visit inside the 1-7 window.
                RecapFreezeBlocker.FULL_PULL_INCOMPLETE,
                RecapFreezeBlocker.DEVICE_MISSED_PART_OF_MONTH -> {
                    Timber.i(
                        "Monthly Recap: not freezing %d — %s",
                        recap.recapYearMonth,
                        blocker.name,
                    )
                    return@withLock payload.copy(categories = emptyList())
                }

                RecapFreezeBlocker.NONE -> Unit // fall through and freeze
            }
            // Durable freeze boundary: only writes when metricsJson IS NULL.
            val updated = recapDao.setMetricsIfAbsent(
                recap.userId,
                recap.recapYearMonth,
                MonthlyRecapMetricsCodec.encode(payload),
                now,
            )
            if (updated > 0) {
                payload
            } else {
                // Another writer (e.g. a different process/instance) froze it first;
                // return the stored winner rather than our unpersisted copy.
                decodeIfCurrent(
                    recapDao.get(recap.userId, recap.recapYearMonth)?.metricsJson,
                    recap.recapYearMonth,
                ) ?: payload
            }
        }
    }

    /** Decodes stored metrics, accepting them only when current-version and same-month. */
    private fun decodeIfCurrent(json: String?, yearMonth: Int): MonthlyRecapMetricsPayload? {
        val payload = MonthlyRecapMetricsCodec.decodeOrNull(json) ?: return null
        return payload.takeIf {
            it.calculationVersion == MonthlyRecapMetricsContract.CALCULATION_VERSION &&
                    it.recapYearMonth == yearMonth
        }
    }

    /**
     * Phase 7 — builds the personalised playback story for the current snapshot:
     * ensures the frozen metrics exist, then composes scenes deterministically
     * from (frozen payload, the ASHA's app language, frozen variantSeed) via
     * [RecapSceneComposer]. Same snapshot → same story, replay-stable, offline.
     *
     * The language is read live from the app ([appLanguageToken]) rather than frozen,
     * so switching the app language re-narrates the recap. That is safe: the variant
     * seed is language-independent, so she keeps the same semantic sentences, and the
     * frozen counts are untouched.
     *
     * Returns an EMPTY list when the month has no countable work (user decision:
     * the recap does not open at all) and null when no user is logged in or the
     * content library is unusable. Read-only: does not touch status/progress.
     */
    suspend fun buildPersonalizedScenes(library: RecapContentLibrary): List<RecapScene>? {
        val payload = ensureCurrentRecapMetrics() ?: return null
        val recap = getOrCreateCurrentRecap() ?: return null
        return RecapSceneComposer(library).compose(
            payload = payload,
            languageToken = appLanguageToken(),
            variantSeed = recap.variantSeed,
        )
    }
}
