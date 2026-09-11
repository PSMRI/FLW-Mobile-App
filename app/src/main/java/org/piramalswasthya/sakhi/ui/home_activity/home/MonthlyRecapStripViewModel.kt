package org.piramalswasthya.sakhi.ui.home_activity.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.piramalswasthya.sakhi.helpers.MonthlyRecapAvailabilityProvider
import org.piramalswasthya.sakhi.helpers.RecapStoryGate
import org.piramalswasthya.sakhi.model.recapStatus
import org.piramalswasthya.sakhi.repositories.MonthlyRecapRepo
import javax.inject.Inject

/**
 * Privacy-safe strip state for the dashboard: HIDDEN / READY / RESUME / REPLAY.
 *
 * Gate order (all fail-closed): availability (unknown → HIDDEN) → the
 * EMPTY-MONTH rule (no countable work → HIDDEN, the recap never opens) → the
 * local snapshot status (none/NOT_STARTED → READY, IN_PROGRESS → RESUME,
 * COMPLETED → REPLAY). No counts or beneficiary data reach the UI.
 *
 * The story check is RE-EVALUATED whenever the recap snapshot row changes and
 * on every [refresh] (called when Home becomes visible). That matters because a
 * background sync can land the previous month's records after the first check:
 * without re-evaluation the strip would stay hidden until the process restarted.
 * Re-running is cheap — the content library is cached and, once metrics are
 * frozen, the check is a single-row read.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MonthlyRecapStripViewModel @Inject constructor(
    private val recapRepo: MonthlyRecapRepo,
    private val availability: MonthlyRecapAvailabilityProvider,
    private val storyGate: RecapStoryGate,
) : ViewModel() {

    /** Bumped to force a re-check (e.g. Home resumed, sync may have landed rows). */
    private val refreshTrigger = MutableStateFlow(0)

    val stripState: LiveData<MonthlyRecapStripState> =
        combine(refreshTrigger, recapRepo.observeCurrentRecap()) { _, recap -> recap }
            .flatMapLatest { recap ->
                flow {
                    val visible = availability.isAvailable() && storyGate.hasStory()
                    emit(
                        stripStateFor(
                            available = visible,
                            status = recap?.recapStatus(),
                        )
                    )
                }
            }
            .asLiveData()

    /** Call when the dashboard becomes visible so a post-sync month appears. */
    fun refresh() {
        viewModelScope.launch { refreshTrigger.value = refreshTrigger.value + 1 }
    }
}
