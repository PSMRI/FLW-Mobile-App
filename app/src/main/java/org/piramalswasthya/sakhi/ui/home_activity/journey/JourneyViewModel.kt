package org.piramalswasthya.sakhi.ui.home_activity.journey

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.badges.BadgeRepository
import org.piramalswasthya.sakhi.database.room.dao.EveningNotifDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.notifications.ActivityClassifier
import org.piramalswasthya.sakhi.notifications.FormSaveInterceptor
import javax.inject.Inject

/**
 * Aggregates notification and badge data for the Journey screen
 * (Notification LLD §6): today's count, lifetime total, badge progress.
 */
@HiltViewModel
class JourneyViewModel @Inject constructor(
    private val dao: EveningNotifDao,
    private val interceptor: FormSaveInterceptor,
    badgeRepository: BadgeRepository,
    pref: PreferenceDao
) : ViewModel() {

    val userName: String = try {
        pref.getLoggedInUser()?.name ?: ""
    } catch (e: Exception) {
        ""
    }

    private val _todayCount = MutableLiveData<Int>()
    val todayCount: LiveData<Int> = _todayCount

    private val _lifetimeTotal = MutableLiveData<Long>()
    val lifetimeTotal: LiveData<Long> = _lifetimeTotal

    /** Top badges by progress toward their next milestone. */
    val topBadges = badgeRepository.shelf.map { cards ->
        cards.sortedByDescending { card ->
            val target = (card.state?.nextTarget ?: 1L).coerceAtLeast(1L)
            (card.state?.progress ?: 0L).toDouble() / target
        }.take(3)
    }.asLiveData()

    init {
        viewModelScope.launch {
            interceptor.captureToday()
            val today = ActivityClassifier.dateKey(System.currentTimeMillis())
            _todayCount.postValue(dao.countForDay(today))
            _lifetimeTotal.postValue(dao.lifetimeCount())
        }
    }
}
