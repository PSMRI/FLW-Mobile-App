package org.piramalswasthya.sakhi.ui.home_activity.monthly_recap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
     * Persists the chosen recap language. Safe to call again for a different
     * language (e.g. after a rotation the user picks the other option): the repo
     * does get-or-create + update, so the stored value always equals the last
     * accepted tap and can never diverge from what the screen shows.
     *
     * Rapid duplicate taps are already prevented at the UI layer (debounce +
     * one-selection-per-screen), so no cross-rotation guard is needed here — that
     * guard previously survived rotation and silently dropped a second, different
     * selection, leaving the UI and the database out of sync.
     */
    fun onLanguageSelected(language: MonthlyRecapLanguage) {
        viewModelScope.launch {
            val ok = recapRepo.setRecapLanguage(language)
            if (!ok) Timber.w("Monthly Recap: language not persisted (no logged-in user)")
        }
    }
}
