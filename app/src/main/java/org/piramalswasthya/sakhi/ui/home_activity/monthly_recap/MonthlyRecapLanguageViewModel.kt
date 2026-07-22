package org.piramalswasthya.sakhi.ui.home_activity.monthly_recap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.piramalswasthya.sakhi.model.MonthlyRecapLanguage
import org.piramalswasthya.sakhi.repositories.MonthlyRecapRepo
import timber.log.Timber
import javax.inject.Inject

/**
 * Persists the explicit Hindi/Assamese choice to the local recap snapshot
 * (recap-only — the global Sakhi language is never touched).
 */
@HiltViewModel
class MonthlyRecapLanguageViewModel @Inject constructor(
    private val recapRepo: MonthlyRecapRepo,
) : ViewModel() {

    /**
     * Serializes persistence so writes run one at a time in tap order — the last
     * accepted tap is always the final stored value. Without this, two taps around
     * a rotation launch independent coroutines and a slower earlier write could
     * finish after a newer one and overwrite it. The fair mutex plus launch order
     * (each tap runs on the main thread and acquires the lock before the next tap
     * is dispatched) guarantees "latest selection wins".
     *
     * Safe to call again for a different language: the repo does get-or-create +
     * update, so the stored value can never diverge from what the screen shows.
     */
    private val languageWriteMutex = Mutex()

    fun onLanguageSelected(language: MonthlyRecapLanguage) {
        viewModelScope.launch {
            val ok = languageWriteMutex.withLock {
                recapRepo.setRecapLanguage(language)
            }
            if (!ok) Timber.w("Monthly Recap: language not persisted (no logged-in user)")
        }
    }
}
