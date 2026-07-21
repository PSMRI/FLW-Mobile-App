package org.piramalswasthya.sakhi.ui.home_activity.monthly_recap

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.model.MonthlyRecapLanguage
import org.piramalswasthya.sakhi.model.recapLanguage
import org.piramalswasthya.sakhi.repositories.MonthlyRecapRepo
import timber.log.Timber
import javax.inject.Inject

/**
 * Persists the explicit Hindi/Assamese choice to the local recap snapshot
 * (recap-only — the global Sakhi language is never touched) and exposes the
 * already-persisted choice so it survives rotation, process death and restart.
 */
@HiltViewModel
class MonthlyRecapLanguageViewModel @Inject constructor(
    private val recapRepo: MonthlyRecapRepo,
) : ViewModel() {

    /** Recap language already persisted for this user+month (null when none). */
    val savedLanguage: LiveData<MonthlyRecapLanguage?> =
        recapRepo.observeCurrentRecap().map { it?.recapLanguage() }.asLiveData()

    private var persistRequested = false

    /**
     * Idempotent: get-or-create the snapshot (DB unique index is the final
     * duplicate guard) then persist the language. Repeated taps are ignored.
     */
    fun onLanguageSelected(language: MonthlyRecapLanguage) {
        if (persistRequested) return
        persistRequested = true
        viewModelScope.launch {
            val ok = recapRepo.setRecapLanguage(language)
            if (!ok) {
                Timber.w("Monthly Recap: language persistence skipped (no user)")
                persistRequested = false
            }
        }
    }
}
