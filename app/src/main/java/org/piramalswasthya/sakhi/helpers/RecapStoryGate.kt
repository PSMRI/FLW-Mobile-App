package org.piramalswasthya.sakhi.helpers

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.model.RecapContentLibrary
import org.piramalswasthya.sakhi.repositories.MonthlyRecapRepo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 7 — the single entry point the UI uses to obtain the personalised
 * recap story (bundled content library + frozen snapshot → composed scenes).
 *
 * Encodes the user-approved EMPTY-MONTH rule at one choke point: when the month
 * has no countable work (or no user / unusable content), [composeStory] returns
 * an empty list and callers keep the recap CLOSED — the dashboard strip hides
 * and playback never opens. Fail-closed, like the availability gate.
 *
 * Production notes: the bundled library is parsed ONCE per process (memoised in
 * this @Singleton — immutable content, so no invalidation is needed) and always
 * off the main thread, so the dashboard strip check costs one cached-object
 * read + the repo's frozen-metrics fast path. Everything is on-device and
 * per-user: no network, no cross-user data — scale-out is per handset.
 */
@Singleton
class RecapStoryGate @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recapRepo: MonthlyRecapRepo,
) {

    @Volatile
    private var cachedLibrary: RecapContentLibrary? = null

    /** Parses the bundled content once per process, always on the IO dispatcher. */
    private suspend fun library(): RecapContentLibrary? =
        cachedLibrary ?: withContext(Dispatchers.IO) {
            // Benign race: a concurrent first call may parse twice; the content is
            // immutable so both results are identical and one simply wins the cache.
            RecapContentCodec.loadBundled(context).also { cachedLibrary = it }
        }

    /** Composed scene list for the current snapshot; EMPTY = do not open the recap. */
    suspend fun composeStory(): List<RecapScene> {
        val library = library() ?: return emptyList()
        return recapRepo.buildPersonalizedScenes(library) ?: emptyList()
    }

    /** True only when at least one celebration scene exists this month. */
    suspend fun hasStory(): Boolean = composeStory().isNotEmpty()
}
