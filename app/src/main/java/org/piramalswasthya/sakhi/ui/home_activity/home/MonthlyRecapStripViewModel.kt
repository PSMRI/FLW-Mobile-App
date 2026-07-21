package org.piramalswasthya.sakhi.ui.home_activity.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.piramalswasthya.sakhi.helpers.MonthlyRecapAvailabilityProvider
import org.piramalswasthya.sakhi.model.recapStatus
import org.piramalswasthya.sakhi.repositories.MonthlyRecapRepo
import javax.inject.Inject

/**
 * Privacy-safe strip state for the dashboard: HIDDEN / READY / RESUME / REPLAY.
 *
 * Availability is checked first and fails closed (unknown -> HIDDEN); only then
 * does the local snapshot drive READY/RESUME/REPLAY. No counts, no beneficiary
 * data — just the state enum reaches the UI.
 */
@HiltViewModel
class MonthlyRecapStripViewModel @Inject constructor(
    recapRepo: MonthlyRecapRepo,
    availability: MonthlyRecapAvailabilityProvider,
) : ViewModel() {

    val stripState: LiveData<MonthlyRecapStripState> = flow {
        if (!availability.isAvailable()) {
            emit(MonthlyRecapStripState.HIDDEN)
        } else {
            emitAll(
                recapRepo.observeCurrentRecap()
                    .map { recap -> stripStateFor(available = true, status = recap?.recapStatus()) }
            )
        }
    }.asLiveData()
}
