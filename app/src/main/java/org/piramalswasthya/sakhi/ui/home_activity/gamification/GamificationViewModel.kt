package org.piramalswasthya.sakhi.ui.home_activity.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.gamification.GamificationEngine
import org.piramalswasthya.sakhi.gamification.GamificationEvent
import org.piramalswasthya.sakhi.model.GamificationBadge
import org.piramalswasthya.sakhi.model.GamificationProfile
import org.piramalswasthya.sakhi.model.PointsTransaction
import org.piramalswasthya.sakhi.repositories.GamificationRepo
import javax.inject.Inject

data class GamificationUiState(
    val profile: GamificationProfile? = null,
    val badges: List<GamificationBadge> = emptyList(),
    val recentTransactions: List<PointsTransaction> = emptyList()
)

@HiltViewModel
class GamificationViewModel @Inject constructor(
    private val repo: GamificationRepo,
    private val engine: GamificationEngine
) : ViewModel() {

    private val _userId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<GamificationUiState> = _userId
        .filterNotNull()
        .flatMapLatest { uid ->
            combine(
                repo.observeProfile(uid),
                repo.observeBadges(uid),
                repo.observeRecentTransactions(uid)
            ) { profile, badges, transactions ->
                GamificationUiState(
                    profile = profile,
                    badges = badges,
                    recentTransactions = transactions
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GamificationUiState()
        )

    fun init(userId: Int) {
        _userId.value = userId
    }

    /**
     * Called by health-form screens after a successful save.
     * Example usage from an ANC form:
     *   gamificationViewModel.onHealthEvent(userId, GamificationEvent.AncVisitCompleted(benId.toString()))
     */
    fun onHealthEvent(userId: Int, event: GamificationEvent) {
        viewModelScope.launch {
            engine.process(userId, event)
        }
    }
}
